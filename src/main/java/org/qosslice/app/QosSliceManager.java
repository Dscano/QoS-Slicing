/*
 * Copyright 2019-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qosslice.app;

import org.qosslice.app.api.*;
import org.onosproject.app.ApplicationAdminService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.store.StoreDelegate;
import org.onosproject.net.intent.IntentListener;
import org.onosproject.net.intent.IntentService;
import org.onosproject.codec.CodecService;
import org.onosproject.net.intent.IntentEvent;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.meter.Meter;
import org.onosproject.net.meter.Band;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.vpls.VplsManager;
import org.onosproject.vpls.api.Vpls;
import org.qosslice.app.api.QosSliceOperation;
import org.qosslice.app.store.QosSliceStoreEvent;
import org.qosslice.app.rest.QosSliceCodec;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;
import java.util.Collection;


import static java.util.Objects.requireNonNull;

/**
 * Application to create performance management over network Slices.
 */
@Component(immediate = true, service = {QosSlice.class})

public class QosSliceManager implements QosSlice {

    public static final String QOSSLICE_APP = "org.qosslicing.app";
    public static final String PREFIX_UNICAST = "uni";
    private static final String SEPARATOR = "-";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String UNSUPPORTED_STORE_EVENT_TYPE =
            "Unsupported store event type {}.";
    private ApplicationId onosforwarding;
    private ApplicationId appId;
    private ApplicationId appIdvpls;
    private final boolean deactivate_onos_app = true;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected IntentService intentService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected QosSliceStore qoSSlicingStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ApplicationAdminService applicationAdminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected OperationService operationService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected Vpls vpls;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CodecService codecService;

    private StoreDelegate<QosSliceStoreEvent> sliceStoreDelegate;

    private IntentListener sliceIntentListener;

    @Activate
    protected void activate() {

        appId = coreService.registerApplication("org.qosslice.app");
        onosforwarding = coreService.getAppId("org.onosproject.fwd");
        appIdvpls = coreService.registerApplication(VplsManager.VPLS_APP);
        sliceStoreDelegate = new OperationHandler();
        sliceIntentListener = new SliceIntentListener();
        intentService.addListener(sliceIntentListener);
        qoSSlicingStore.setDelegate(sliceStoreDelegate);
        codecService.registerCodec(QosSliceData.class, new QosSliceCodec());

        if (deactivate_onos_app) {
            try {
                applicationAdminService.deactivate(onosforwarding);
                log.info("### Deactivating Onos Reactive Forwarding App ###");
            } catch (NullPointerException ne) {
                log.info(ne.getMessage());
            }
        }
    }

    @Deactivate
    protected void deactivate() {
        cfgService.unregisterProperties(getClass(), false);
        qoSSlicingStore.unsetDelegate(sliceStoreDelegate);
        intentService.removeListener(sliceIntentListener);
        codecService.unregisterCodec(QosSliceData.class);
        log.info("Stopped");
    }

    @Override
    public QosSliceData createSliceMonitoring(String qossliceName, String vplsName) {
        requireNonNull(vplsName);
        requireNonNull(qossliceName);
        QosSliceData qoSData = QosSliceData.of(qossliceName,vplsName);
        qoSSlicingStore.addSlice(qoSData);
        return qoSData;
    }

    @Override
    public void addBand(QosSliceData qoSData, Map<Meter.Unit,Band> meteringData) {
        requireNonNull(qoSData);
        requireNonNull(meteringData);
        //log.info("dentro add band");
        QosSliceData newData = QosSliceData.of(qoSData);
        newData.setMeterUnit(meteringData.keySet().iterator().next());
        newData.addBand(meteringData.values().iterator().next());
        updateSliceStatus(newData, QosSliceData.State.UPDATING);
    }

    @Override
    public void addMeter(QosSliceData qoSData) {
        requireNonNull(qoSData);
        log.info("dentro add meter");
        QosSliceData newData = QosSliceData.of(qoSData);
        newData.addMeter();
        updateSliceStatus(newData, QosSliceData.State.UPDATING);
    }


    @Override
    public void addMeter2Level(QosSliceData qoSData) {
        requireNonNull(qoSData);
        log.info("dentro add meter 2 level");
        QosSliceData newData = QosSliceData.of(qoSData);
        newData.addMeter2Level();
        updateSliceStatus(newData, QosSliceData.State.UPDATING);
    }

    @Override
    public void addQueue(QosSliceData qoSData) {
        requireNonNull(qoSData);
        //log.info("dentro add Qos");
        QosSliceData newData = QosSliceData.of(qoSData);
        newData.addQueue();
        updateSliceStatus(newData, QosSliceData.State.UPDATING);
    }

    @Override
    public void removeBands(QosSliceData qoSData) {
        requireNonNull(qoSData);
        //log.info("dentro remove Bands");
        QosSliceData newData = QosSliceData.of(qoSData);
        newData.removeBands();
        updateSliceStatus(newData, QosSliceData.State.UPDATING);
    }

    @Override
    public Band removeBand(QosSliceData qoSData, Band delBand) {
        requireNonNull(qoSData);
        requireNonNull(delBand);
        QosSliceData newData = QosSliceData.of(qoSData);
        newData.removeBand(delBand);
        updateSliceStatus(newData, QosSliceData.State.UPDATING);
        return delBand;
    }

    @Override
    public void removeMeter(QosSliceData qoSData) {
        requireNonNull(qoSData);
        //log.info("dentro remove meter");
        QosSliceData newData = QosSliceData.of(qoSData);
        newData.removeMeter();
        updateSliceStatus(newData, QosSliceData.State.UPDATING);
    }

    @Override
    public void removeQos(QosSliceData qoSData) {
        requireNonNull(qoSData);
        //log.info("dentro remove Qos");
        QosSliceData newData = QosSliceData.of(qoSData);
        newData.removeQueue();
        updateSliceStatus(newData, QosSliceData.State.UPDATING);
    }

    @Override
    public QosSliceData removeSlice(QosSliceData qoSData) {
        requireNonNull(qoSData);
        //log.info("dentro remove Slice");
        QosSliceData newData = QosSliceData.of(qoSData);
        newData.state(QosSliceData.State.REMOVING);
        qoSSlicingStore.removeSlice(qoSData);
        return qoSData;
    }

    @Override
    public void removeAllSlice() {
        Set<QosSliceData> allVplses = ImmutableSet.copyOf(qoSSlicingStore.getAllSlice());
        allVplses.forEach(this::removeSlice);
    }

    @Override
    public Collection<QosSliceData> getAllSlice() {
        return ImmutableSet.copyOf(qoSSlicingStore.getAllSlice());
    }

    @Override
    public QosSliceData getVpls(String vplsName) {
        requireNonNull(vplsName);
        return null;
    }

    @Override
    public QosSliceData getSlice(String sliceName) {
        requireNonNull(sliceName);
        return qoSSlicingStore.getSlice(sliceName);
    }

    /**
     * Updates QosSlice status to the store.
     *
     * @param qoSData the QosSlice
     * @param state the new state to the QosSlice
     */
    private void updateSliceStatus(QosSliceData qoSData, QosSliceData.State state) {
        qoSData.state(state);
        qoSSlicingStore.updateSlice(qoSData);
    }

    /**
     * A listener for intent events.
     * Updates a QosSlice if an interface added or removed.
     */
    class SliceIntentListener implements IntentListener {
        @Override
        public void event(IntentEvent event) {
            if (event.type().equals(IntentEvent.Type.INSTALL_REQ)) {
                Intent intent = event.subject();
                log.info("dentro INSTALL_REQ ");
                if (intent.key().toString().split(SEPARATOR)[2].equals(PREFIX_UNICAST)
                        && intent.appId().equals(appIdvpls)){
                    String vplsName = intent.key().toString().split(SEPARATOR)[0]+
                            SEPARATOR+
                            intent.key().toString().split(SEPARATOR)[1];
                    QosSliceData qoSData = qoSSlicingStore.getAllSlice().stream()
                                    .filter(s -> s.getVplsName().equals(vplsName))
                                    .findFirst()
                                    .orElse(null);
                    if (qoSData == null ) {
                                return;
                    }
                    updateSliceStatus(qoSData, QosSliceData.State.UPDATING);
                }
            }
            else if (event.type().equals(IntentEvent.Type.WITHDRAWN)){
                Intent intent = event.subject();
                if (intent.key().toString().split(SEPARATOR)[2].equals(PREFIX_UNICAST)
                        && intent.appId().equals(appIdvpls)){
                    String vplsName = intent.key().toString().split(SEPARATOR)[0]+SEPARATOR+
                            intent.key().toString().split(SEPARATOR)[1];
                    if ( vpls.getVpls(vplsName) != null) {
                        log.info("dentro if remove return");
                        return;
                    }
                    QosSliceData qoSData = qoSSlicingStore.getAllSlice().stream()
                            .filter(s -> s.getVplsName().equals(vplsName))
                            .findFirst()
                            .orElse(null);
                    if (qoSData == null ) {
                        return;
                    }
                    //log.info("dentro altro if remove rimozione");
                    updateSliceStatus(qoSData, QosSliceData.State.REMOVING);
                    qoSSlicingStore.removeSlice(qoSData);
                }
            }
        }
    }

    /**
     * Store delegate for QosSlice store.
     * Handles QosSlice store event and generate QosSlice operation according to event
     * type.
     */
    class OperationHandler implements StoreDelegate<QosSliceStoreEvent> {

        @Override
        public void notify(QosSliceStoreEvent event) {
            QosSliceOperation sliceOperation;
            QosSliceOperation.Operation op;
            QosSliceData qoSData = event.subject();
            switch (event.type()) {
                case ADD:
                    op = QosSliceOperation.Operation.ADD;
                    break;
                case REMOVE:
                    op = QosSliceOperation.Operation.REMOVE;
                    break;
                case UPDATE:
                    if (qoSData.state() == QosSliceData.State.FAILED ||
                            qoSData.state() == QosSliceData.State.ADDED ||
                            qoSData.state() == QosSliceData.State.REMOVED) {
                        // Update the state only. Nothing to do if it is updated
                        // to ADDED, REMOVED or FAILED
                        op = null;
                    } else {
                        op = QosSliceOperation.Operation.UPDATE;
                    }
                    break;
                default:
                    log.warn(UNSUPPORTED_STORE_EVENT_TYPE, event.type());
                    return;
            }
            if (op != null) {
                sliceOperation= QosSliceOperation.of(qoSData, op);
                operationService.submit(sliceOperation);
            }
        }
    }
}


