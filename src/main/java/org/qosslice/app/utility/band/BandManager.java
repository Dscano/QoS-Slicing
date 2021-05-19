package org.qosslice.app.utility.band;


import com.google.common.collect.Maps;
import org.onosproject.net.meter.Band;
import org.onosproject.net.meter.Meter;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


@Component(immediate = true, service = { BandService.class })
public class BandManager implements BandService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<String, Map<Meter.Unit,Band>> bandsDB = Maps.newConcurrentMap();

    @Activate
    public void activate() {

        log.info("Started");
    }

    @Deactivate
    public void deactivate() {

        bandsDB.clear();
        log.info("Stopped");
    }

    @Override
    public void add(String bandName, Map<Meter.Unit, Band> band){
        bandsDB.put(bandName,band);
    }

    @Override
    public Map<Meter.Unit,Band> getBand(String bandName){
        return bandsDB.get(bandName);
    }

    @Override
    public Map<String, Map<Meter.Unit,Band>> getAllBands(){
        return bandsDB;
    }


    @Override
    public void remove( String Bandname) {
        bandsDB.remove(Bandname);
    }

}
