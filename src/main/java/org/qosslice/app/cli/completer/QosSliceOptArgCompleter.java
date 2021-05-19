package org.qosslice.app.cli.completer;

import org.qosslice.app.api.QosSlice;
import org.qosslice.app.api.QosSliceData;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractChoicesCompleter;
import org.onosproject.net.meter.Meter;
import org.onosproject.vpls.api.Vpls;
import org.onosproject.net.meter.Band;
import org.qosslice.app.utility.band.BandService;
import org.qosslice.app.cli.QosSliceCommandEnum;

import java.util.*;
import java.util.stream.Collectors;

import static org.onosproject.cli.AbstractShellCommand.get;

/**
 * Slice optional argument completer.
 */
@Service
public class QosSliceOptArgCompleter extends AbstractChoicesCompleter {
    protected Vpls vpls;
    protected QosSlice slice;
    protected BandService bandService;

    @Override
    public List<String> choices() {
        if (slice == null) {
            slice = get(QosSlice.class);
        }
        String[] argList = commandLine.getArguments();
        if (argList == null) {
            return Collections.emptyList();
        }
        String argOne = argList[1];
        QosSliceCommandEnum qosliceCommandEnum = QosSliceCommandEnum.enumFromString(argOne);
        if (qosliceCommandEnum != null) {
            switch (qosliceCommandEnum) {
                case ADD_BAND:
                    return availableBands();
                default:
                    return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    /**
     * Returns the list of Bands available for the Slice.
     *
     * @return the list of Bands available for the Slice
     */
    private List<String> availableBands() {
        if (bandService == null) {
            bandService = get(BandService.class);
        }
        Map<String, Map<Meter.Unit,Band>> allBands = bandService.getAllBands();
        Set<Band> usedBands = slice.getAllSlice().stream()
                .map(QosSliceData::getBands)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        return allBands.keySet().stream()
                .filter(s->!allBands.get(s).containsValue(usedBands))
                .collect(Collectors.toList());
    }
}
