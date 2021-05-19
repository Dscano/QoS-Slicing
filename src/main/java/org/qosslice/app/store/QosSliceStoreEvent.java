package org.qosslice.app.store;
import org.qosslice.app.api.QosSliceData;
import org.onosproject.event.AbstractEvent;

/**
 * A class to represent a QoSSlice store related event.
 */
public class QosSliceStoreEvent extends AbstractEvent<QosSliceStoreEvent.Type, QosSliceData> {

        /**
         * QoSSlice store event type.
         */

        public enum Type {
            ADD,
            REMOVE,
            UPDATE
        }

        /**
         * Constructs a store event with given event type and QoSSlice information.
         *
         * @param type the event type
         * @param qoSData the QoSSlice
         */
        public QosSliceStoreEvent(Type type, QosSliceData qoSData) {
            super(type, qoSData);
        }

}

