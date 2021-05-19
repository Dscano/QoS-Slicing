package org.qosslice.app.cli.completer;

import com.google.common.collect.Lists;
import org.qosslice.app.cli.BandCommandEnum;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractChoicesCompleter;

import java.util.Collections;
import java.util.List;

@Service
public class BandCommandCompleter extends AbstractChoicesCompleter{

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
        BandCommandEnum bandCommandEnum = BandCommandEnum.enumFromString(argOne);
        if (bandCommandEnum != null) {
            switch (bandCommandEnum) {
                default:
                    bandCommandEnum.toStringList();
            }
        }
        return bandCommandEnum.toStringList();
    }

}
