package org.qosslice.app.store;


import org.qosslice.app.QosSliceManager;
import org.qosslice.app.api.QosSliceData;
import org.qosslice.app.api.QosSliceOperation;
import org.qosslice.app.api.QosSliceStore;
import org.onlab.util.KryoNamespace;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.meter.Band;
import org.onosproject.store.AbstractStore;
import org.onosproject.store.StoreDelegate;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.EventuallyConsistentMap;
import org.onosproject.store.service.EventuallyConsistentMapEvent;
import org.onosproject.store.service.EventuallyConsistentMapListener;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.WallClockTimestamp;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static java.util.Objects.requireNonNull;

import org.onosproject.vpls.api.Vpls;
import org.qosslice.app.config.SliceAppConfig;
import org.qosslice.app.config.SliceConfig;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Component(immediate = true, service = QosSliceStore.class)
public class StoreAdditionalData
        extends AbstractStore<QosSliceStoreEvent, StoreDelegate<QosSliceStoreEvent>>
        implements QosSliceStore {

    private static final KryoNamespace APP_K = KryoNamespace.newBuilder()
            .register(KryoNamespaces.API)
            .register(QosSliceData.class)
            .register(QosSliceData.State.class)
            .register(QosSliceOperation.class)
            .build();

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected NetworkConfigService networkConfigService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected Vpls vpls;


    private EventuallyConsistentMap< String, QosSliceData> qosSliceDataStore;
    private EventuallyConsistentMapListener<String, QosSliceData> qosSliceDataListener;
    private ApplicationId appId;

    @Activate
    protected void active() {

        appId = coreService.registerApplication(QosSliceManager.QOSSLICE_APP);

        qosSliceDataStore = storageService.<String , QosSliceData>eventuallyConsistentMapBuilder()
                .withName("SLICE-Data")
                .withTimestampProvider((name, vpls_2Data) -> new WallClockTimestamp())
                .withSerializer(APP_K)
                .build();


        qosSliceDataListener = new InternalVplsDataListener(); //below in the program
        qosSliceDataStore.addListener(qosSliceDataListener);
        log.info("Started");
    }

    @Deactivate
    protected  void deactive() {
        qosSliceDataStore.removeListener(qosSliceDataListener);
        networkConfigService.removeConfig(appId);
        log.info("Stopped");
    }

    @Override
    public void addSlice(QosSliceData qoSData) {
        requireNonNull(qoSData);
        if (qoSData.getQosName().isEmpty()) {
            throw new IllegalArgumentException("Slice name is empty.");
        }
        qoSData.state(QosSliceData.State.ADDING);
        this.qosSliceDataStore.put(qoSData.getQosName(), qoSData);
    }

    @Override
    public void removeSlice(QosSliceData qoSData) {
        requireNonNull(qoSData);
        if (qoSData.getQosName().isEmpty()) {
            throw new IllegalArgumentException("Slice name is empty.");
        }
        qoSData.state(QosSliceData.State.REMOVING);
        if (!this.qosSliceDataStore.containsKey(qoSData.getQosName())) {
            // notify the delegate asynchronously if QoSSlice does not exists
            CompletableFuture.runAsync(() -> {
                QosSliceStoreEvent event = new QosSliceStoreEvent(QosSliceStoreEvent.Type.REMOVE,
                        qoSData);
                notifyDelegate(event);
            });
            return;
        }
        this.qosSliceDataStore.remove(qoSData.getQosName());
    }

    @Override
    public QosSliceData getSlice(String sliceName) {
        requireNonNull(sliceName);
        return qosSliceDataStore.get(sliceName);

    }

    @Override
    public Collection<QosSliceData> getAllSlice() {
        return qosSliceDataStore.values();
    }


    /**
     * Writes all QoSSlice data to the network configuration store.
     *
     *@param sliceDataCollection the QoSSlice data
     */

    public void writeSliceToNetConfig(Collection<QosSliceData> sliceDataCollection) {

        SliceAppConfig config = networkConfigService.addConfig(appId, SliceAppConfig.class);

        if (config == null) {
            log.debug("Slice config is not available now");
            return;
        }
        config.clearSliceConfig();

        // Setup update time for this Slice application configuration
        WallClockTimestamp ts = new WallClockTimestamp();
        config.updateTime(ts.unixTimestamp());

        sliceDataCollection.forEach(qosSlice -> {

            Set<String> Bands = qosSlice.getBands()
                    .stream()
                    .map(Band::toString)
                    .collect(Collectors.toSet());

            SliceConfig sliceConfig = new SliceConfig(qosSlice.getQosName(),
                    qosSlice.getVplsName(),
                    qosSlice.getMeter(),
                    qosSlice.getMeterUnit().toString(),
                    Bands,
                    qosSlice.getQueue()
            );
            config.addSlice(sliceConfig);
        });

        networkConfigService.applyConfig(appId, SliceAppConfig.class, config.node());
    }

    @Override
    public void updateSlice(QosSliceData qoSData) {

        switch (qoSData.state()) {
            case ADDED:
            case REMOVED:
            case FAILED:
                // state update only
                this.qosSliceDataStore.put(qoSData.getQosName(), qoSData);
                break;
            default:
                qoSData.state(QosSliceData.State.UPDATING);
                this.qosSliceDataStore.put(qoSData.getQosName(), qoSData);
                break;
        }
    }

    private class InternalVplsDataListener implements EventuallyConsistentMapListener<String, QosSliceData> {
        private static final String STATE_UPDATE = "SLICE state updated, new SLICE: {}";

        @Override
        public void event(EventuallyConsistentMapEvent<String, QosSliceData> event) {
            QosSliceData qoSData = event.value();
            writeSliceToNetConfig(getAllSlice());
            switch (event.type()) {
                case PUT:
                    // Add or Update
                    if(qoSData.state() == QosSliceData.State.ADDING){
                        QosSliceStoreEvent sliceStoreEvent = new QosSliceStoreEvent(QosSliceStoreEvent.Type.ADD, qoSData);
                        notifyDelegate(sliceStoreEvent);
                    }
                    else if (qoSData.state() == QosSliceData.State.UPDATING) {
                        QosSliceStoreEvent sliceStoreEvent = new QosSliceStoreEvent(QosSliceStoreEvent.Type.UPDATE, qoSData);
                        notifyDelegate(sliceStoreEvent);
                    } else {
                        // Do nothing here, just update state from operation service
                        log.debug(STATE_UPDATE, qoSData);
                    }
                    break;
                case REMOVE:
                    if (qoSData == null) {
                        qoSData = QosSliceData.of(event.key(),event.value().getVplsName());
                    }
                    qoSData.state(QosSliceData.State.REMOVING);
                    QosSliceStoreEvent sliceStoreEvent =
                            new QosSliceStoreEvent(QosSliceStoreEvent.Type.REMOVE, qoSData);
                    notifyDelegate(sliceStoreEvent);
                    break;
                default:
                    break;
            }

        }
    }

}
