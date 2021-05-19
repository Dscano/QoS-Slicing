package org.qosslice.app.api;

import org.onosproject.net.meter.Band;
import org.onosproject.net.meter.Meter;

import java.util.Collection;
import java.util.Map;


public interface QosSlice {

    /**
     * Creates a new Slice
     *
     * @param qossliceName the name of the Slice
     * @param vplsName the name of the VPLS
     * @return a Slice instance if the operation is successful; null otherwise
     */
    QosSliceData createSliceMonitoring (String qossliceName, String vplsName);

    /**
     * Retrieves a QosSlice.
     *
     * @param qossliceName the name of the Slice
     * @return the Slice instance if the Slice exists; null otherwise
     */
    QosSliceData getSlice(String qossliceName);

    /**
     * Add Band to Meters belong to a QosSlice.
     */
    void addBand(QosSliceData qoSData, Map<Meter.Unit, Band> meteringData);

    /**
     * Adds Meters to a QosSlice.
     */
     void addMeter(QosSliceData qoSData);

    /**
     * Adds Meters 2 Level to a QosSlice.
     */
    void addMeter2Level(QosSliceData qoSData);

    /**
     * Adds Queues to a QosSlice.
     */
    void addQueue(QosSliceData qoSData);

    /**
     * Remove Meters associated to a QosSlice.
     */
    void removeMeter(QosSliceData qoSData);

    /**
     * Remove Queues associated to a QosSlice.
     */
    void removeQos(QosSliceData qoSData);

    /**
     * Remove all Bands associated to a QosSlice.
     */
    void removeBands(QosSliceData qoSData);

    /**
     * Remove a Band associated to a QosSlice.
     */
    Band removeBand(QosSliceData qoSData, Band delBand);

    /**
     * Removes a QosSlice.
     *
     * @param qoSData the Slice to be removed
     * @return the Slice removed if the operation is successful; null otherwise
     */
    QosSliceData removeSlice(QosSliceData qoSData);

    /**
     * Removes all QosSlices and cleans up the QosSlice configuration.
     */
    void removeAllSlice();

    /**
     * Get VPLS associate to a QosSlice.
     *
     * @param vplsName the VPLS associate to a Slice
     *
     */
     QosSliceData getVpls(String vplsName);

    /**
     * Gets all QosSlices.
     *
     * @return a collection of QosSlices
     */
    Collection<QosSliceData> getAllSlice();

}
