package org.qosslice.app.config;


import org.qosslice.app.api.QosSlice;
import org.onosproject.cluster.LeadershipService;
import org.onosproject.cluster.NodeId;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.config.*;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.net.intf.InterfaceService;
import org.onosproject.vpls.VplsManager;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.onlab.util.Tools.groupedThreads;
/**
 * Component for the management of the QoSSlice configuration.
 */
@Component(immediate = true)
public class SliceConfigManager {
    private static final Class<SliceAppConfig> CONFIG_CLASS = SliceAppConfig.class;
    private static final String NET_CONF_EVENT = "Received NetworkConfigEvent {}";
    private static final String CONFIG_NULL = "Slice monitoring configuration not defined";
    private static final int INITIAL_RELOAD_CONFIG_DELAY = 0;
    private static final int INITIAL_RELOAD_CONFIG_PERIOD = 1000;
    private static final int NUM_THREADS = 1;
    private static final String SLICE = "slice";
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final NetworkConfigListener configListener =
            new SliceConfigManager.InternalNetworkConfigListener();

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected NetworkConfigService configService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected NetworkConfigRegistry registry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected QosSlice qoSSlicing;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LeadershipService leadershipService;

    private ScheduledExecutorService reloadExecutor =
            Executors.newScheduledThreadPool(NUM_THREADS,
                    groupedThreads("onos/qosslice",
                            "config-reloader-%d",
                            log)
            );

    private ConfigFactory<ApplicationId, SliceAppConfig> vplsConfigFactory =
            new ConfigFactory<ApplicationId, SliceAppConfig>(
                    SubjectFactories.APP_SUBJECT_FACTORY, SliceAppConfig.class,  SLICE ) {
                @Override
                public SliceAppConfig createConfig() {
                    return new SliceAppConfig();
                }
            };

    protected ApplicationId appId;

    @Activate
    void activate() {
        appId = coreService.registerApplication(VplsManager.VPLS_APP);
        configService.addListener(configListener);

        // Load config when QoSSlice service started and there is a leader for QoSSlice;
        // otherwise, try again after a period.
        reloadExecutor.scheduleAtFixedRate(() -> {
            NodeId vplsLeaderNode = leadershipService.getLeader(appId.name());
            if (qoSSlicing == null || vplsLeaderNode == null) {
                return;
            }
            //reloadConfiguration();
            reloadExecutor.shutdown();
        }, INITIAL_RELOAD_CONFIG_DELAY, INITIAL_RELOAD_CONFIG_PERIOD, TimeUnit.MILLISECONDS);
        registry.registerConfigFactory(vplsConfigFactory);
    }

    @Deactivate
    void deactivate() {
        configService.removeListener(configListener);
        registry.unregisterConfigFactory(vplsConfigFactory);
    }

    /**
     * Listener for QoSSlice monitoring configuration events.
     * Reloads QoSSlice monitoring configuration when configuration added or updated.
     * Removes all QoSSlice when configuration removed or unregistered.
     */
    private class InternalNetworkConfigListener implements NetworkConfigListener {
        @Override
        public void event(NetworkConfigEvent event) {
            if (event.configClass() == CONFIG_CLASS) {
                log.debug(NET_CONF_EVENT, event.configClass());
                switch (event.type()) {
                    case CONFIG_ADDED:
                    case CONFIG_UPDATED:
                        //reloadConfiguration();
                        break;
                    case CONFIG_REMOVED:
                    case CONFIG_UNREGISTERED:
                        break;
                    default:
                        break;
                }
            }
        }
    }
}

