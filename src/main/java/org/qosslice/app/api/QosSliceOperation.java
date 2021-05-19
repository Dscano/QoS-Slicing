package org.qosslice.app.api;


import com.google.common.base.MoreObjects;

import static java.util.Objects.requireNonNull;

public class QosSliceOperation {


    public enum Operation {
        ADD,
        REMOVE,
        UPDATE,

    }

    private Operation op;
    private QosSliceData qoSData;

    /**
     * Defines a QosSlice operation by binding a given QosSlice and operation type.
     *
     * @param qoSData the QosSlice
     * @param op the operation
     */
    protected QosSliceOperation(QosSliceData qoSData, Operation op) {
        requireNonNull(qoSData);
        requireNonNull(op);
        // Make a copy of the QosSlice data to ensure other thread won't change it.
        this.qoSData = qoSData;
        this.op = op;
    }

    /**
     * Retrieves the operation type from the operation.
     *
     * @return the operation type
     */
    public Operation op() {
        return op;
    }

    /**
     * Retrieves the QosSlice from the operation.
     *
     * @return the VPLS
     */
    public QosSliceData vpls() {
        return qoSData;
    }

    /**
     * Defines a qoSData operation by binding a given qoSData and operation type.
     *
     * @param qoSData the qoSData
     * @param op the operation
     * @return the QosSlice operation
     */
    public static QosSliceOperation of(QosSliceData qoSData, Operation op) {
        return new QosSliceOperation(qoSData, op);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("vplsName", qoSData.toString())
                .add("op", op.toString())
                .toString();
    }



}
