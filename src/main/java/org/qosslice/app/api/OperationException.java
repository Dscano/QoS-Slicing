package org.qosslice.app.api;
/**
 * Exception for QosSlice operation.
 */

public class OperationException extends RuntimeException {
        private static final long serialVersionUID = 4514685940685335886L;
        private QosSliceOperation qosSliceOperation;

        /**
         * Constructs a QosSlice operation exception with given qosSlice operation and
         * message.
         *
         * @param operation the qosSlice operation
         * @param msg the description of the exception
         */
        public OperationException(QosSliceOperation operation, String msg) {
            super(msg);
            this.qosSliceOperation = operation;
        }

        /**
         * Gets qosSlice operation for this exception.
         * @return the qosSlice operation
         */
        public QosSliceOperation qosSliceOperation() {
            return qosSliceOperation;
        }
    }

