package org.qosslice.app.utility;

import com.google.common.collect.Sets;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.*;
import org.onosproject.net.flow.instructions.Instruction;
import org.onlab.packet.Ethernet;
import org.onosproject.net.meter.ID_Meter;
import org.onosproject.net.meter.MeterId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class FlowRuleUtility {

    private static final Logger log = LoggerFactory.getLogger(
            MeterUtility.class);

    private FlowRuleUtility() {
        // Utility classes should not have a public or default constructor.
    }

    public static List<FlowRule> createFlowRule(HashMap<DeviceId, HashMap<List<MacAddress>, List<PortNumber>>> devToMacPort, ApplicationId appId) {

        log.info("*******************DENTRO CREATE FLOW RULE*****************************");

        HashMap<Long, Byte> QueuetoDscp = new HashMap<Long, Byte>();
        QueuetoDscp.put(1L, (byte) 10);
        QueuetoDscp.put(2L, (byte) 12);
        List<FlowRule> flowQos = new ArrayList<FlowRule>();

        //log.info("Hash dev to port"+devToMacPort.toString());

        devToMacPort.forEach((deviceId, macToport) -> {

            //log.info("coppia"+ deviceId + macToport);

            macToport.forEach((macAddresses, portNumber) -> {

                //AGGIUNTA PER IL TEST
                /*
                if(deviceId.equals(DeviceId.deviceId("of:0000000000000001")) || deviceId.equals(DeviceId.deviceId("of:0000000000000008"))){

                    TrafficTreatment.Builder treatmentBuilder = DefaultTrafficTreatment.builder();
                    treatmentBuilder.meter(ID_Meter.buildMeterId((MeterId.meterId(1L)),portNumber.iterator().next()));
                    treatmentBuilder.transition(2);

                    TrafficSelector selectorBuilder = DefaultTrafficSelector.builder()
                            .matchEthDst(macAddresses.iterator().next())
                            .matchEthType(Ethernet.TYPE_IPV4)
                            .matchIPDscp((byte) 0xa)
                            .build();

                    FlowRule flowRule = DefaultFlowRule.builder()
                            .forDevice(deviceId)
                            .forTable(1)
                            .withPriority(10)
                            .makePermanent()
                            .withSelector(selectorBuilder)
                            .withTreatment(treatmentBuilder.build())
                            .fromApp(appId)
                            .build();

                    //log.info("*******************"+flowRule.toString()+"*****************************");

                    flowQos.add(flowRule);
                }*/

                //log.info("NUMERO PORTA"+portNumber.toString());

                QueuetoDscp.forEach((queue, dscp) -> {

                    TrafficSelector selectorBuilder = DefaultTrafficSelector.builder()
                            .matchEthDst(macAddresses.iterator().next())
                            .matchEthType(Ethernet.TYPE_IPV4)
                            .matchIPDscp(dscp)
                            .build();

                    TrafficTreatment.Builder treatmentBuilder = DefaultTrafficTreatment.builder();
                    if (dscp != (byte) 10) {
                        //log.info("dentro if treatment");
                        treatmentBuilder.setIpDscp((byte) 0xa);
                    }
                    //log.info("NUMERO PORTA prima di setOUt"+portNumber.toString());
                    treatmentBuilder.setQueue(queue)
                            .setOutput(portNumber.iterator().next());

                    //PER IL TEST
                    /*
                    if(deviceId.equals(DeviceId.deviceId("of:0000000000000001")) || deviceId.equals(DeviceId.deviceId("of:0000000000000008"))){
                    FlowRule flowRule = DefaultFlowRule.builder()
                            .forDevice(deviceId)
                            .forTable(2)
                            .withPriority(10)
                            .makePermanent()
                            .withSelector(selectorBuilder)
                            .withTreatment(treatmentBuilder.build())
                            .fromApp(appId)
                            .build();

                    //log.info("*******************"+flowRule.toString()+"*****************************");

                    flowQos.add(flowRule);}

                    else{*/
                        FlowRule flowRule = DefaultFlowRule.builder()
                                .forDevice(deviceId)
                                .forTable(1)
                                .withPriority(10)
                                .makePermanent()
                                .withSelector(selectorBuilder)
                                .withTreatment(treatmentBuilder.build())
                                .fromApp(appId)
                                .build();

                        //log.info("*******************"+flowRule.toString()+"*****************************");

                        flowQos.add(flowRule);
                    //}

                    });

                });

            });

        //});

        /*
        log.info("************************************************");
        log.info("FLOW PRIMA DEL RETURN"+flowQos.toString());
        log.info("************************************************");
        log.info("NUMERO DI FLOW"+flowQos.size());
        */
        return flowQos;
    }

    public static List<FlowRule> createFlowRuleMeters(String qosName,HashMap<DeviceId, HashMap<List<MacAddress>, List<PortNumber>>> devToMacPort, ApplicationId appId) {

        log.info("*******************DENTRO CREATE FLOW RULE METERS*****************************");

        HashMap<Long, Byte> QueuetoDscp = new HashMap<Long, Byte>();
        //QueuetoDscp.put(1L, (byte) 10);
        //QueuetoDscp.put(2L, (byte) 12);
        List<FlowRule> flowQos = new ArrayList<FlowRule>();
        String SEPARATOR = "-";
        //log.info("Hash dev to port"+devToMacPort.toString());

        devToMacPort.forEach((deviceId, macToport) -> {

            //log.info("coppia"+ deviceId + macToport);

            macToport.forEach((macAddresses, portNumber) -> {

                //AGGIUNTA PER IL TEST

                //if(deviceId.equals(DeviceId.deviceId("of:0000000000000001")) || deviceId.equals(DeviceId.deviceId("of:0000000000000004"))){

                    TrafficTreatment.Builder treatmentBuilder = DefaultTrafficTreatment.builder();
                    treatmentBuilder.meter(ID_Meter.buildMeterId(MeterId.meterId(Long.valueOf(qosName.split(SEPARATOR)[1])), portNumber.iterator().next()));
                    //treatmentBuilder.meter(MeterId.meterId(portNumber.iterator().next().toLong()));
                    treatmentBuilder.setOutput(portNumber.iterator().next());
                    //treatmentBuilder.transition(2);

                    TrafficSelector selectorBuilder = DefaultTrafficSelector.builder()
                            .matchEthDst(macAddresses.iterator().next())
                            .matchEthType(Ethernet.TYPE_IPV4)
                            //.matchIPDscp((byte) 0xa)
                            .build();

                    FlowRule flowRule = DefaultFlowRule.builder()
                            .forDevice(deviceId)
                            .forTable(1)
                            .withPriority(10)
                            .makePermanent()
                            .withSelector(selectorBuilder)
                            .withTreatment(treatmentBuilder.build())
                            .fromApp(appId)
                            .build();

                    //log.info("*******************"+flowRule.toString()+"*****************************");

                    flowQos.add(flowRule);
                //}

                //log.info("NUMERO PORTA"+portNumber.toString());
                /*
                QueuetoDscp.forEach((queue, dscp) -> {

                    TrafficSelector selectorBuilder1 = DefaultTrafficSelector.builder()
                            .matchEthDst(macAddresses.iterator().next())
                            .matchEthType(Ethernet.TYPE_IPV4)
                            .matchIPDscp(dscp)
                            .build();

                    TrafficTreatment.Builder treatmentBuilder1 = DefaultTrafficTreatment.builder();
                    if (dscp != (byte) 10) {
                        //log.info("dentro if treatment");
                        treatmentBuilder1.setIpDscp((byte) 0xa);
                        treatmentBuilder1.meter(MeterId.meterId(portNumber.iterator().next().toLong()));
                        //treatmentBuilder1.meter(ID_Meter.buildMeterId(MeterId.meterId(Long.valueOf(qosName.split(SEPARATOR)[1])), portNumber.iterator().next()));
                    }
                    treatmentBuilder1.meter(ID_Meter.buildMeterId(MeterId.meterId(Long.valueOf(qosName.split(SEPARATOR)[1])), portNumber.iterator().next()));

                    //log.info("NUMERO PORTA prima di setOUt"+portNumber.toString());
                    treatmentBuilder1
                            .setQueue(queue)
                            .setOutput(portNumber.iterator().next());

                    //PER IL TEST
                    //if(deviceId.equals(DeviceId.deviceId("of:0000000000000001")) || deviceId.equals(DeviceId.deviceId("of:0000000000000004"))){
                    FlowRule flowRule1 = DefaultFlowRule.builder()
                            .forDevice(deviceId)
                            .forTable(2)
                            .withPriority(10)
                            .makePermanent()
                            .withSelector(selectorBuilder1)
                            .withTreatment(treatmentBuilder1.build())
                            .fromApp(appId)
                            .build();

                    //log.info("*******************"+flowRule.toString()+"*****************************");

                    flowQos.add(flowRule1);
                //}

                    /*else{
                    FlowRule flowRule = DefaultFlowRule.builder()
                            .forDevice(deviceId)
                            .forTable(1)
                            .withPriority(10)
                            .makePermanent()
                            .withSelector(selectorBuilder)
                            .withTreatment(treatmentBuilder.build())
                            .fromApp(appId)
                            .build();

                    //log.info("*******************"+flowRule.toString()+"*****************************");

                    flowQos.add(flowRule);
                    }*/

                });

            });

        //});

        //});

        /*
        log.info("************************************************");
        log.info("FLOW PRIMA DEL RETURN"+flowQos.toString());
        log.info("************************************************");
        log.info("NUMERO DI FLOW"+flowQos.size());
        */
        return flowQos;
    }

    public static HashMap<DeviceId, Set<PortNumber>> devToPort(HashMap<DeviceId, Set<PortNumber>> devToport,FlowEntry flowEntry){

        if (!devToport.containsKey(flowEntry.deviceId())) {
            flowEntry.treatment().immediate().forEach(instruction -> {
                if (instruction.type().equals(Instruction.Type.OUTPUT)) {
                    Set<PortNumber> port = Sets.newHashSet();
                    port.add(PortNumber.fromString(instruction.toString().substring(7)));
                    devToport.put(flowEntry.deviceId(), port);
                }
            });
        } else {
            flowEntry.treatment().immediate().forEach(instruction -> {

                if (instruction.type().equals(Instruction.Type.OUTPUT)) {
                    devToport.get(flowEntry.deviceId()).add(PortNumber.fromString(instruction.toString().substring(7)));
                }
            });

        }

        return devToport;
  }

    public static HashMap<DeviceId, Set<PortNumber>> devToPort(HashMap<DeviceId, Set<PortNumber>> devToport,FlowRule flowRule){

        if (!devToport.containsKey(flowRule.deviceId())) {
            flowRule.treatment().immediate().forEach(instruction -> {
                if (instruction.type().equals(Instruction.Type.OUTPUT)) {
                    Set<PortNumber> port = Sets.newHashSet();
                    port.add(PortNumber.fromString(instruction.toString().substring(7)));
                    devToport.put(flowRule.deviceId(), port);
                }
            });

        } else {
            flowRule.treatment().immediate().forEach(instruction -> {
                if (instruction.type().equals(Instruction.Type.OUTPUT)) {
                    devToport.get(flowRule.deviceId()).add(PortNumber.fromString(instruction.toString().substring(7)));
                }
            });
        }

        return devToport;
    }
}





