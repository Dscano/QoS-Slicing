package org.qosslice.app.config;

import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Configuration of a Slice.
 */

public class SliceConfig {
    private final String nameSlice;
    private final String nameVpls;
    private final Boolean meter;
    private String meterUnit;
    private final Set<String> bands;
    private final Boolean qos;

    /**
     * Creates a new Slice monitoring configuration.
     *
     * @param nameSlice the Slice name
     * @param nameVpls the Vpls name that we want monitroring
     * @param meter the meter associated with the slice
     * @param qos the qos associate to Slice
     */
    public SliceConfig(String nameSlice, String nameVpls, Boolean meter, String meterUnit, Set<String> bands, Boolean qos) {
        this.nameSlice = checkNotNull(nameSlice);
        this.nameVpls = checkNotNull(nameVpls);
        this.meter = checkNotNull(meter);
        this.meterUnit = checkNotNull(meterUnit);
        this.bands = checkNotNull(bands);
        this.qos= checkNotNull(qos);
    }

    /**
     * The name of the Slice.
     *
     * @return the name of the Slice
     */
    public String nameSlice() {
        return nameSlice;
    }

    /**
     * The name of the vpls associate to Slice.
     *
     * @return the name of the vpls
     */

    public String nameVpls() {
        return nameVpls;
    }


    /**
     * If meters are associate to Slice.
     *
     * @return a boolean that explain if meters are used
     */

    public Boolean meter() {
        return meter;
    }


    /**
     * The name of the bands associated with the Slice.
     *
     * @return a set of bands names associated with the Slice
     */
    public Set<String> bands() { return ImmutableSet.copyOf(bands); }


    public String meterUnit() {
        return meterUnit;
    }

    /**
     * If queue are associate to Slice.
     *
     * @return a boolean that explain if queue are used
     */
    public Boolean qos() {
        return qos;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SliceConfig) {
            SliceConfig that = (SliceConfig) obj;
            return Objects.equals(nameSlice, that.nameSlice) &&
                    Objects.equals(nameVpls, that.nameVpls) &&
                    Objects.equals(meter, that.meter) &&
                    Objects.equals(meterUnit, that.meterUnit) &&
                    Objects.equals(bands, that.bands) &&
                    Objects.equals(qos, that.qos);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameSlice,nameVpls, meter, meterUnit, bands, qos);
    }
}