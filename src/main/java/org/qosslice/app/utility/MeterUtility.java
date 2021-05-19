package org.qosslice.app.utility;

import org.onosproject.net.device.DeviceService;
import org.qosslice.app.api.QosSliceData;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.meter.*;
import org.onosproject.net.*;

import java.util.*;


public final class MeterUtility {

    private MeterUtility() {
        // Utility classes should not have a public or default constructor.
    }

    public static MeterRequest buildRequestMeter(QosSliceData qoSData, DeviceId deviceId, ApplicationId appId) {

        Set<Band> bands = qoSData.getBands();

        MeterRequest.Builder builder = DefaultMeterRequest.builder()
                .forDevice(deviceId)
                .withUnit(qoSData.getMeterUnit())
                .withBands(bands)
                .fromApp(appId);

        Boolean burst = bands.stream()
                    .anyMatch(band -> !band.burst().equals(0L));

        if (burst) {
            builder = builder.burst();
        }

        MeterRequest request = builder.add();
        return request;
    }


    public static MeterRequest buildRequestMeters(QosSliceData qoSData, Port port,
                                                  DeviceId deviceId, ApplicationId appId) {

        Set<Band> bands = new HashSet<>();
        //Double rate = (int) port.portSpeed()*0.98;

        Band band =  DefaultBand.builder()
                .ofType(Band.Type.DROP)
                .withRate(Long.valueOf("4000"))
                //.ofType(Band.Type.REMARK)
                //.dropPrecedence(Short.valueOf("1"))
                //.withRate(Long.valueOf("1"))
                .burstSize(0L)
                .build();

        bands.add(band);

        MeterRequest.Builder builder = DefaultMeterRequest.builder()
                .forDevice(deviceId)
                .withUnit(qoSData.getMeterUnit())
                .withBands(bands)
                .fromApp(appId);

        MeterRequest request = builder.add();

        return request;
    }

    public static MeterRequest MeterRequestfromMeter(Meter meter) {

        MeterRequest.Builder builder = DefaultMeterRequest.builder()
                .forDevice(meter.deviceId())
                .withUnit(meter.unit())
                .withBands(meter.bands())
                .fromApp(meter.appId());

        if (meter.isBurst()) {
            builder = builder.burst();
        }

        MeterRequest request = builder.add();
        return request;
    }
}
