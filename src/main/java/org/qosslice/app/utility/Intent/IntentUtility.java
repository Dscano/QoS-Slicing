package org.qosslice.app.utility.Intent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.qosslice.app.api.QosSliceData;

import org.onosproject.net.*;
import org.onosproject.net.meter.*;
import org.onosproject.net.flow.*;
import org.onosproject.net.intent.*;

import org.onosproject.net.intent.constraint.PartialFailureConstraint;
import org.onosproject.core.ApplicationId;


import org.onosproject.net.intf.Interface;
import org.onosproject.net.meter.MeterId;
import org.onosproject.vpls.api.VplsData;
import org.onosproject.vpls.intent.VplsIntentUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public final class IntentUtility {

    private static final Logger log = LoggerFactory.getLogger(
            IntentUtility.class);

    private static final int PRIORITY_OFFSET = 1000;
    private static final int PRIORITY_UNI = 200;

    public static final String PREFIX_UNICAST = "uni";
    private static final String SEPARATOR = "-";


    public static final ImmutableList<Constraint> PARTIAL_FAILURE_CONSTRAINT =
            ImmutableList.of(new PartialFailureConstraint());

    private IntentUtility() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Builds unicast Intents for a QosSlice.
     *
     * @param
     * @param hosts the hosts of the VPLS associated to QosSlice
     * @param appId application ID for Intents
     * @return unicast Intents for the QosSlice
     */
    public static Set<Intent> buildUniIntents(VplsData vplsData,
                                              QosSliceData qoSData,
                                              Set<Host> hosts,
                                              ApplicationId appId) {

        Set<Interface> interfaces = vplsData.interfaces();

        if (interfaces.size() < 2) {
            return ImmutableSet.of();
        }

        Set<Intent> uniIntents = Sets.newHashSet();
        ResourceGroup resourceGroup = ResourceGroup.of(vplsData.name());
        hosts.forEach(host -> {
            FilteredConnectPoint hostFcp = buildFilteredConnectedPoint(host);
            Set<FilteredConnectPoint> srcFcps =
                    interfaces.stream()
                            .map(IntentUtility::buildFilteredConnectedPoint)
                            .filter(fcp -> !fcp.equals(hostFcp))
                            .collect(Collectors.toSet());

            Key key = buildKey(PREFIX_UNICAST,
                    hostFcp.connectPoint(),
                    vplsData.name(),
                    host.mac(),
                    appId);

            Intent uniIntent = buildUniIntent(key,
                    appId,
                    srcFcps,
                    hostFcp,
                    host,
                    vplsData.encapsulationType(),
                    resourceGroup,
                    qoSData.getMeter(),
                    qoSData.getMeter2Level(),
                    qoSData.getQueue()
                    );
            uniIntents.add(uniIntent);
        });
        return uniIntents;
    }

    /**
     * Builds a unicast intent.
     *
     * @param key key to identify the intent
     * @param appId application ID for this Intent
     * @param srcs the source Connect Points
     * @param dst the destination Connect Point
     * @param host destination Host
     * @param encap the encapsulation type
     * @param resourceGroup resource group for this Intent
     * @return the generated multi-point to single-point intent
     */
    static MultiPointToSinglePointIntent buildUniIntent(Key key,
                                                        ApplicationId appId,
                                                        Set<FilteredConnectPoint> srcs,
                                                        FilteredConnectPoint dst,
                                                        Host host,
                                                        EncapsulationType encap,
                                                        ResourceGroup resourceGroup,
                                                        Boolean meter,
                                                        Boolean meters,
                                                        Boolean qos) {

        MultiPointToSinglePointIntent.Builder intentBuilder;
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthDst(host.mac())
                .build();

        TrafficTreatment.Builder treatmentBuild = DefaultTrafficTreatment.builder();

        if(meters){

            treatmentBuild.meter(MeterId.meterId(1L));
        }

        if(meter){

            treatmentBuild.meter(MeterId.meterId(Long.valueOf(key.toString().split(SEPARATOR)[1])));
        }

        if(qos || meters ){
            treatmentBuild.transition(1);
        }

        intentBuilder = MultiPointToSinglePointIntent.builder()
                .appId(appId)
                .key(key)
                .selector(selector)
                .treatment(treatmentBuild.build())
                .filteredIngressPoints(srcs)
                .filteredEgressPoint(dst)
                .constraints(PARTIAL_FAILURE_CONSTRAINT)
                .priority(PRIORITY_OFFSET + PRIORITY_UNI)
                .resourceGroup(resourceGroup);

        VplsIntentUtility.setEncap(intentBuilder, PARTIAL_FAILURE_CONSTRAINT, encap);


        return intentBuilder.build();
    }

    /**
     * Builds an intent key either for single-point to multi-point or
     * multi-point to single-point intents, based on a prefix that defines
     * the type of intent, the single connect point representing the single
     * source or destination for that intent, the name of the VPLS the intent
     * belongs to, and the destination host MAC address the intent reaches.
     *
     * @param prefix the key prefix
     * @param cPoint the connect point identifying the source/destination
     * @param vplsName the name of the VPLS
     * @param hostMac the source/destination MAC address
     * @param appId application ID for the key
     * @return the key to identify the intent
     */
    static Key buildKey(String prefix,
                        ConnectPoint cPoint,
                        String vplsName,
                        MacAddress hostMac,
                        ApplicationId appId) {
        String keyString = vplsName +
                SEPARATOR +
                prefix +
                SEPARATOR +
                cPoint.deviceId() +
                SEPARATOR +
                cPoint.port() +
                SEPARATOR +
                hostMac;

        return Key.of(keyString, appId);
    }

    /**
     * Builds filtered connected point by a given network interface.
     *
     * @param iface the network interface
     * @return the filtered connected point of a given network interface
     */

    static FilteredConnectPoint buildFilteredConnectedPoint(Interface iface) {
        Objects.requireNonNull(iface);
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();

        if (iface.vlan() != null && !iface.vlan().equals(VlanId.NONE)) {
            trafficSelector.matchVlanId(iface.vlan());
        }

        return new FilteredConnectPoint(iface.connectPoint(), trafficSelector.build());
    }

    /**
     * Builds filtered connected point by a given host.
     *
     * @param host the host
     * @return the filtered connected point of the given host
     */

    static FilteredConnectPoint buildFilteredConnectedPoint(Host host) {
        requireNonNull(host);
        TrafficSelector.Builder trafficSelector = DefaultTrafficSelector.builder();

        if (host.vlan() != null && !host.vlan().equals(VlanId.NONE)) {
            trafficSelector.matchVlanId(host.vlan());
        }
        return new FilteredConnectPoint(host.location(), trafficSelector.build());
    }
}
