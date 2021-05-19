package org.qosslice.app.utility.band;


import org.onosproject.net.meter.Band;
import org.onosproject.net.meter.Meter;

import java.util.Map;

/**
 * Provides a means to modify the Bands configuration.
 */

public interface BandService {

    /**
     * Adds a new band configuration.
     *
     */
    void add(String BandName, Map<Meter.Unit,Band> band);


    Map<Meter.Unit,Band> getBand(String bandName);


    Map<String, Map<Meter.Unit,Band>> getAllBands();

    /**
     * Remove a new band configuration.
     *
     */
    void remove(String Bandname);
}
