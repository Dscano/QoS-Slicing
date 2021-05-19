package org.qosslice.app.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Enum representing the QoS_Slice command type.
 */
public enum QosSliceCommandEnum {

    ADD_BAND("add-band"),
    ADD_METER("add-meter"),
    ADD_METERS("add-meters"),
    ADD_QOS("add-qos"),
    CREATE ("create"),
    DELETE("delete"),
    LIST("list"),
    REMOVE_METER("rem-meter"),
    REMOVE_QOS("rem-qos"),
    REMOVE_BANDS("rem-bands"),
    CLEAN("clean"),
    SHOW("show");


    private final String command;

    /**
     * Creates the enum from a string representing the command.
     *
     * @param command the text representing the command
     */
    QosSliceCommandEnum(final String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return command;
    }

    /**
     * Returns a list of command string values.
     *
     * @return the list of string values corresponding to the enums
     */
    public static List<String> toStringList() {
        return Arrays.stream(values())
                .map(QosSliceCommandEnum::toString)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Alternative method to valueOf. It returns the command type
     * corresponding to the given string. If the parameter does not match a
     * constant name, or is null, null is returned.
     *
     * @param command the string representing the encapsulation type
     * @return the EncapsulationType constant corresponding to the string given
     */
    public static QosSliceCommandEnum enumFromString(String command) {
        if (command != null && !command.isEmpty()) {
            for (QosSliceCommandEnum c : values()) {
                if (command.equalsIgnoreCase(c.toString())) {
                    return c;
                }
            }
        }
        return null;
    }
}

