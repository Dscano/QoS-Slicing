package org.qosslice.app.api;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.onosproject.net.meter.*;

import java.util.*;
import static java.util.Objects.*;

public class QosSliceData {

    /**
     * States of a QosSlice.
     */
    public enum State {
        UPDATING,
        ADDING,
        REMOVING,
        ADDED,
        REMOVED,
        FAILED
    }
    protected String qosName;
    protected String vplsName;
    private Boolean meter;
    private Boolean meter2Level;
    private Meter.Unit meterUnit;
    private QosSliceData.State state;
    private Set<Band> bands;
    private Boolean queue;

    /**
     * Constructs a QosSlice data by given a qosliceData.
     *  @param qosName the given name
     *  @param vplsName the given name
     */

    private QosSliceData(String qosName, String vplsName) {

        this.qosName = qosName;
        this.vplsName = vplsName;
        this.meter = false;
        this.meter2Level = false;
        this.meterUnit = Meter.Unit.KB_PER_SEC;
        this.state = State.ADDING;
        this.bands = Sets.newHashSet();
        this.queue = false;
    }

    /**
     * Creates a QosSlice data by given name.
     *
     * @param qosName the given name
     * @return the qoslice data
     */
    public static QosSliceData of(String qosName, String nameVpls) {
        requireNonNull(qosName);
        requireNonNull(nameVpls);
        return new QosSliceData(qosName,nameVpls);
    }

    /**
     * Creates a copy of qoslice data.
     *
     * @param qoSData the qoslice data
     * @return the copy of the qoslice data
     */
    public static QosSliceData of(QosSliceData qoSData) {
        requireNonNull(qoSData);
        QosSliceData qoSDataCopy = new QosSliceData(qoSData.getQosName(), qoSData.getVplsName());
        qoSDataCopy.state(qoSData.state());
        qoSData.setMeter(qoSData.getMeter());
        qoSData.setMeter2Level(qoSData.getMeter2Level());
        qoSData.setQueue(qoSData.getQueue());
        qoSDataCopy.setMeterUnit(qoSData.getMeterUnit());
        qoSDataCopy.setBands(qoSData.getBands());
        return qoSData;
    }

    /**
     * Gets name of the QosSlice.
     * @return the name of the VPLS
     */
    public String getVplsName() {
        return vplsName;
    }

    /**
     * Gets name of the QosSlice.
     * @return the name of the Slice
     */
    public String getQosName() {
        return qosName;
    }

    public Boolean getMeter() { return meter; }

    public Boolean getMeter2Level() { return meter2Level; }

    public Meter.Unit getMeterUnit() { return meterUnit; }

    public Set<Band> getBands() { return ImmutableSet.copyOf(bands); }

    public void setMeter(Boolean meter) { this.meter = meter; }

    public void setMeter2Level(Boolean meter2Level) { this.meter2Level = meter2Level; }

    public void addBand(Band band) {
        requireNonNull(band);
        this.bands.add(band);
    }

    public void addMeter() {
        this.meter = true;
    }

    public void addMeter2Level() {
        this.meter2Level = true;
    }

    public void addQueue() { this.queue = true; }

    public Boolean getQueue() { return queue; }

    public void setBands(Set<Band> bands) { this.bands.addAll(bands); }

    public void setQueue(Boolean queue) { this.queue = queue; }

    public void setMeterUnit(Meter.Unit meterUnit) { this.meterUnit = meterUnit; }

    public void removeQueue() { this.queue = false; }

    public void removeMeter() {
        this.meter = false;
    }

    public void removeBands() { this.bands.clear(); }

    public void removeBand(Band delBand) {
        requireNonNull(delBand);
        this.bands.remove(delBand);
    }

    public QosSliceData.State state() {
        return state;
    }

    public void state(QosSliceData.State state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("Qos name", qosName)
                .add("Vpls name", vplsName)
                .add("state", state)
                .add("meter", meter)
                .add("meter2Level", meter2Level)
                .add("meter unit", meterUnit)
                .add("bands",bands.toString())
                .add("queue", queue)
                .toString();
    }

}
