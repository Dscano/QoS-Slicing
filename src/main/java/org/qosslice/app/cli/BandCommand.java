package org.qosslice.app.cli;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.action.Option;

import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.meter.Band;
import org.onosproject.net.meter.DefaultBand;
import org.onosproject.net.meter.Meter;

import org.qosslice.app.cli.completer.BandCommandCompleter;
import org.qosslice.app.utility.band.BandService;

import com.google.common.collect.Maps;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.*;

@Service
@Command(scope = "onos", name = "band", description = "Adds a Band ")

public class BandCommand extends AbstractShellCommand {

    private Meter.Unit unit;
    private Long rate;
    private Long burstSize;
    protected BandService bandService;

    private static final String BOLD = "\u001B[1m";
    private static final String COLOR_ERROR = "\u001B[31m";
    private static final String RESET = "\u001B[0m";


    private static final String BAND_ALREADY_EXISTS =
            COLOR_ERROR + "Band name " + BOLD + "%s" + RESET + COLOR_ERROR +
                    " already exists" + RESET;

    private static final String BAND_NOT_FOUND =
            COLOR_ERROR + "Band " + BOLD + "%s" + RESET + COLOR_ERROR +
                    " not found" + RESET;

    private static final String BAND_COMMAND_NOT_FOUND =
            COLOR_ERROR + "BAND command " + BOLD + "%s" + RESET + COLOR_ERROR +
                    " not found" + RESET;

    private static final String BAND_DISPLAY = "Band name: " + BOLD +
            "%s" + RESET +"\nMeter unit: %s\n" +"Parameter: %s\n";

    private static final String INSERT_BAND_NAME =
            COLOR_ERROR + "Missing the " + BOLD + "Band name." + RESET +
                    COLOR_ERROR + " Specifying a Band name is mandatory." +
                    RESET;

    private static final String INSERT_DROP_PRECEDENCE =
            COLOR_ERROR + "Missing the " + BOLD + "drop precedence value." + RESET +
                    COLOR_ERROR + " Specifying a drop precedence is mandatory." +
                    RESET;

    private static final String SEPARATOR = "----------------";

    @Option(name = "-bd", aliases = "--bandDrop",
            description = "Assign band DROP to this meter",
            required = false, multiValued = false)
    private boolean hasBandDrop = false;

    @Option(name = "-br", aliases = "--bandRemark",
            description = "Assign band REMARK to this meter",
            required = false, multiValued = false)
    private boolean hasBandRemark = false;

    @Option(name = "-dp", aliases = "--dropPrecedence",
            description = "Assign drop Precedence to this meter",
            required = false, multiValued = false)
    private String dp = null;

    @Option(name = "-up", aliases = "--unitPkts",
            description = "Assign unit Packets per Second to this meter",
            required = false, multiValued = false)
    private boolean hasPkts = false;

    @Option(name = "-uk", aliases = "--unitKbps",
            description = "Assign unit Kilobits per Second to this meter",
            required = false, multiValued = false)
    private boolean hasKbps = false;

    @Option(name = "-b", aliases = "--bandwidth", description = "Bandwidth",
            required = false, multiValued = false)
    private String bandwidthString = null;

    @Option(name = "-bs", aliases = "--burstSize", description = "Burst size",
            required = false, multiValued = false)
    private String burstSizeString = null;

    @Option(name = "-nb", aliases = "--nameBand",description = "Band Name",
            required = false, multiValued = false)
    private String nameBand = null;

    @Argument(index = 0, name = "command", description = "Command name (add-band|rem-band|show",
            required = true, multiValued = false)
    @Completion(BandCommandCompleter.class)
            String command = null;


    private Map<Meter.Unit, Band> buildBand() {

        Map<Meter.Unit, Band> bands= Maps.newConcurrentMap();

        // check units
        if (hasPkts) {
            unit = Meter.Unit.PKTS_PER_SEC;
        } else {
            unit = Meter.Unit.KB_PER_SEC;
        }
        // rate size
        if (!isNullOrEmpty(bandwidthString)) {
            rate = Long.parseLong(bandwidthString);
        }
        // burst size
        if (!isNullOrEmpty(burstSizeString)) {
            burstSize = Long.parseLong(burstSizeString);
        } else {
            burstSize = 0L;
        }
        // Create bands
        if (hasBandDrop) {

            Band band =  DefaultBand.builder()
                    .ofType(Band.Type.DROP)
                    .withRate(rate)
                    .burstSize(burstSize)
                    .build();

            bands.put(unit, band);
        }

        if (hasBandRemark) {

             Band band = DefaultBand.builder()
                    .ofType(Band.Type.REMARK)
                    .dropPrecedence(Short.valueOf(dp))
                    .withRate(rate)
                    .burstSize(burstSize)
                    .build();

            bands.put(unit, band);

        }

        return bands;
    }

    @Override
    protected void doExecute() {

        if (bandService== null) {
            bandService = get(BandService.class);
        }
        BandCommandEnum enumCommand = BandCommandEnum.enumFromString(command);
        if (enumCommand != null) {
            switch (enumCommand) {
                case ADD_BAND:
                    addBand(nameBand);
                    break;
                case REMOVE_BAND:
                    removeBand(nameBand);
                    break;
                case SHOW:
                    showBand(nameBand);
                    break;
                default:
                    print(BAND_COMMAND_NOT_FOUND, command);
            }
        } else {
            print(BAND_COMMAND_NOT_FOUND, command);
        }
    }
    protected void addBand(String bandName) {

        if (bandName == null) {
            print(INSERT_BAND_NAME);
            return;
        }
        if (bandService.getBand(bandName)!= null) {
            print(BAND_ALREADY_EXISTS, bandName);
            return;
        }
        if (hasBandRemark && dp == null) {
            print(INSERT_DROP_PRECEDENCE);
            return;
        }

        bandService.add(bandName, buildBand());
    }

    protected void removeBand(String bandName) {

        if (bandName == null) {
            print(INSERT_BAND_NAME);
            return;
        }
        if (bandService.getBand(bandName) == null) {
            print(BAND_NOT_FOUND, bandName);
            return;
        }
        bandService.remove(bandName);
    }

    protected void showBand(String nameBand) {
        if (!isNullOrEmpty(nameBand)) {
            // A Band name is provided. Check first if the VPLS exists
            Map<Meter.Unit, Band> band = bandService.getBand(nameBand);
            if (band != null) {
                print(BAND_DISPLAY,
                        nameBand,
                        band.keySet(),
                        band.values().toString().replaceAll("DefaultBand", ""));
            } else {
                print(BAND_NOT_FOUND, nameBand);
            }
        } else {
        Map<String, Map<Meter.Unit,Band>> bands = bandService.getAllBands();
        // No Band names are provided. Display all Bands configured
        print(SEPARATOR);
        bands.forEach((s, unitBandMap) ->{
            print(BAND_DISPLAY,
                    s,
                    unitBandMap.keySet(),
                    unitBandMap.values().toString().replaceAll("DefaultBand", ""));
            print(SEPARATOR);
            });
        }
    }
}

