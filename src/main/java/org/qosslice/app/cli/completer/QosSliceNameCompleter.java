package org.qosslice.app.cli.completer;

import org.qosslice.app.api.QosSlice;
import org.qosslice.app.api.QosSliceData;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractChoicesCompleter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.onosproject.cli.AbstractShellCommand.get;

/**
 * Slice name completer.
 */
@Service
public class QosSliceNameCompleter extends AbstractChoicesCompleter {

    protected QosSlice qoSSlicing;

    @Override
    public List<String> choices() {
        if (qoSSlicing == null) {
            qoSSlicing = get(QosSlice.class);
        }
        Collection<QosSliceData> vplses = qoSSlicing.getAllSlice();
        return vplses.stream().map(QosSliceData::getQosName).collect(Collectors.toList());
    }
}
