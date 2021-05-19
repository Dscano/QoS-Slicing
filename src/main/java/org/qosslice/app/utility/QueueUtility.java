package org.qosslice.app.utility;

import org.onlab.util.Bandwidth;
import org.onosproject.net.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onosproject.net.behaviour.*;


import java.util.List;

public class QueueUtility {

    private static final Logger log = LoggerFactory.getLogger(
            QueueUtility.class);

    private QueueUtility() {
        // Utility classes should not have a public or default constructor.
    }

    public static void checkQos(List<Device> devices){

        log.info("dentro chrckQos");

        devices.forEach(device -> {



            if(device.is(QueueConfigBehaviour.class)){
                log.info("if");
                QueueConfigBehaviour qg = device.as(QueueConfigBehaviour.class);
                log.info("Obtain all queues configured on a device."+qg.getQueues().toString());
            }
            else{log.info(" else "+device.is(QueueConfigBehaviour.class));}
        });
    }

    public static QueueDescription installQueue(String queueName){

        log.info("dentro installQueue");

        QueueDescription queue = DefaultQueueDescription.builder()
                .queueId(QueueId.queueId(queueName))
                //.burst() capire meglio
                .dscp(1)
                .maxRate(Bandwidth.bps(200))
                .minRate(Bandwidth.bps(100))
                .priority(1L)
                //.type(QueueDescription.Type.PRIORITY) capire meglio
                .build();

        log.info("QueueDescription BUILDATA"+ queue.toString());

        return queue;


    }




}
