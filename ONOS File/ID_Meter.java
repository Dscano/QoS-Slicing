package org.onosproject.net.meter;

import org.onosproject.net.PortNumber;
import org.onosproject.net.meter.MeterId;

import java.util.Set;

public class ID_Meter{


    public ID_Meter (){ }

    public static MeterId buildMeterId(MeterId meterId, PortNumber outputport){

        //log.info("Dentro Build Meter id");

        String meterID = meterId.toString().concat("0").concat(outputport.toString()
                .replace("[","")).replace("]","");


        return MeterId.meterId(Long.parseLong(meterID));
    }


}