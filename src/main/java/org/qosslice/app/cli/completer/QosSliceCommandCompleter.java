package org.qosslice.app.cli.completer;

import com.google.common.collect.Lists;
import org.qosslice.app.cli.QosSliceCommandEnum;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractChoicesCompleter;

import java.util.Collections;
import java.util.List;

/**
 * QoS_Slice command completer.
 */
@Service
public class QosSliceCommandCompleter extends AbstractChoicesCompleter {

    @Override
    public List<String> choices() {
        if (commandLine.getArguments() == null) {
            return Collections.emptyList();
        }
        List<String> argList = Lists.newArrayList();

        String argOne = null;
        if (argList.size() > 1) {
            argOne = argList.get(1);
        }
        QosSliceCommandEnum qosliceCommandEnum = QosSliceCommandEnum.enumFromString(argOne);
        if (qosliceCommandEnum != null) {
            switch (qosliceCommandEnum) {
                case CREATE:
                case LIST:
                    return Collections.emptyList();
                default:
                    QosSliceCommandEnum.toStringList();
            }
        }
        return QosSliceCommandEnum.toStringList();
    }
}