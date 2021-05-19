package org.qosslice.app.api;


import org.qosslice.app.store.QosSliceStoreEvent;
import org.onosproject.store.StoreDelegate;
import org.onosproject.store.Store;

import java.util.Collection;

/**
 * Definition of the operations regarding the management of the QosSlice elements.
 */

public interface QosSliceStore extends Store<QosSliceStoreEvent, StoreDelegate<QosSliceStoreEvent>>{

    /**
     * Adds a QosSlice to the configuration.
     *
     * @param qoSData the QosSlice to add
     */
    void addSlice(QosSliceData qoSData);

    /**
     * Removes a QosSlice from the configuration.
     *
     */
    void removeSlice(QosSliceData qoSData);


    /**
     * Updates a QosSlice.
     *
     * @param qoSData the QosSlice to update
    */
    void updateSlice(QosSliceData qoSData);

    /**
     * Retrieves a QosSlice.
     *
     * @param sliceName the name of the QosSlice
     * @return the QosSlice instance if the QosSlice exists; null otherwise
     */
     QosSliceData getSlice(String sliceName);


    /**
     * Gets all the QosSlices.
     *
     * @return all the QosSlices
     */
    Collection<QosSliceData> getAllSlice();

}
