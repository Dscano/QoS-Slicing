package org.qosslice.app.cli;

import org.qosslice.app.api.QosSlice;
import org.qosslice.app.api.QosSliceData;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractShellCommand;


import com.google.common.collect.ImmutableSet;
import org.onosproject.net.meter.Band;
import org.onosproject.net.meter.Meter;
import org.onosproject.vpls.api.Vpls;
import org.onosproject.vpls.api.VplsData.VplsState;
import org.onosproject.vpls.api.VplsData;
import org.qosslice.app.utility.band.BandService;
import org.qosslice.app.cli.completer.QosSliceNameCompleter;
import org.qosslice.app.cli.completer.QosSliceCommandCompleter;
import org.qosslice.app.cli.completer.QosSliceOptArgCompleter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.onosproject.vpls.api.VplsData.VplsState.*;


/**
 * CLI to interact with the QoS_Slice application.
 */
@Service
@Command(scope = "onos",
        name = "qos-slice",
        description = "Manages the Slice Monitoring application")

public class QosSliceCommand extends AbstractShellCommand {
    private static final Set<VplsState> CHANGING_STATE =
            ImmutableSet.of(ADDING, REMOVING, UPDATING);

    // Color codes and style
    private static final String BOLD = "\u001B[1m";
    private static final String COLOR_ERROR = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    // Messages and string formatter
    private static final String METER_UNIT_ERROR_FOUND =
            COLOR_ERROR + "Meter unit " + BOLD + "%s" + RESET + COLOR_ERROR +
                    " isn't equal to meter unit associated to Slice %s" + RESET;

    private static final String INSERT_BAND =
            COLOR_ERROR + "Missing the " + BOLD + "band name." +
                    RESET + COLOR_ERROR + " Specifying an interface name is" +
                    " mandatory." + RESET;

    private static final String BAND_NOT_ASSOCIATE =
            COLOR_ERROR + "Band " + BOLD + "%s" + RESET + COLOR_ERROR +
                    " not associate to slice"+ "%s"+ RESET;

    private static final String INSERT_BAND_SLICE =
            COLOR_ERROR + "Slice " + BOLD + "%s haven't an associate band" +
                    RESET + COLOR_ERROR + " Assign a band is mandatory." + RESET;

    private static final String METER_ALREADY_ASSOCIATED =
            COLOR_ERROR + "Meter" + RESET + COLOR_ERROR +
                    " already associated to Slice " + BOLD + "%s" + RESET +
                    COLOR_ERROR + "" + RESET;

    private static final String METER_UNASSOCIATED =
            COLOR_ERROR + "Meter "+ RESET + COLOR_ERROR +
                    " already unassociated to Slice " + BOLD + "%s" + RESET +
                    COLOR_ERROR + "" + RESET;

    private static final String INSERT_SLICE_NAME =
            COLOR_ERROR + "Missing the " + BOLD + "Service name." + RESET +
                    COLOR_ERROR + " Specifying a Service name is mandatory." +
                    RESET;

    private static final String INSERT_VPLS_NAME =
            COLOR_ERROR + "Missing the " + BOLD + "VPLS name." + RESET +
                    COLOR_ERROR + " Specifying a VPLS name is mandatory." + RESET;

    private static final String SEPARATOR = "----------------";

    private static final String QOS_ALREADY_ASSOCIATED =
            COLOR_ERROR + "Qos" + RESET + COLOR_ERROR +
            " already associated to Slice " + BOLD + "%s" + RESET +
            COLOR_ERROR + " " + RESET;

    private static final String QOS_UNASSOCIATED =
            COLOR_ERROR + "Qos" + RESET + COLOR_ERROR +
                    " already unassociated to Slice " + BOLD + "%s" + RESET +
                    COLOR_ERROR + "" + RESET;

    private static final String SLICE_ALREADY_EXISTS =
            COLOR_ERROR + "Service Metering "+ BOLD + "%s" + RESET + COLOR_ERROR +
                    " already exists" + RESET;

    private static final String QOS_SLICE_COMMAND_NOT_FOUND =
            COLOR_ERROR + "QoS Slice command " + BOLD + "%s" + RESET + COLOR_ERROR +
                    " not found" + RESET;

    private static final String SLICE_DISPLAY = "Slice name: "+ BOLD +
            "%s" + RESET +"\nVPLS name: %s"+ "\nMeters is install: %s "+"\nState: %s"+
           "\nMeter unit: %s" +"\nAssociated Bands: %s"+"\nQueues in install: %s\n";

    private static final String VPLS_NOT_FOUND =
            COLOR_ERROR + "VPLS " + BOLD + "%s" + RESET + COLOR_ERROR +
                    " not found" + RESET;

    private static final String SLICE_NOT_FOUND =
            COLOR_ERROR + " Slice " + BOLD + "%s" + RESET + COLOR_ERROR +
                    " not found" + RESET;

    private static final String BAND_NOT_FOUND =
            COLOR_ERROR + "Band " + BOLD + "%s" + RESET + COLOR_ERROR +
                    " not found" + RESET;

    protected QosSlice qoSSlicing;
    protected Vpls vpls;
    protected BandService bandService;

    @Argument(index = 0, name = "command", description = "Command name (|add-meter|add-meters|add-band|add-qos|clean|create|list|delete|rem-meter|" +
            "rem-bands|rem-qos|show)",
            required = true, multiValued = false)
    @Completion(QosSliceCommandCompleter.class)
            String command = null;

    @Argument(index = 1, name = "vplsSlice", description = "The name of the Slice",
            required = false, multiValued = false)
    @Completion(QosSliceNameCompleter.class)
            String sliceName = null;

    @Argument(index = 2, name = "optArg", description = "The r name",
            required = false, multiValued = false)
    @Completion(QosSliceOptArgCompleter.class)
            String optArg = null;

    @Override
    protected void doExecute() {
        if (vpls == null) {
            vpls = get(Vpls.class);
        }
        if (qoSSlicing == null) {
            qoSSlicing = get(QosSlice.class);
        }
        if (bandService == null) {
            bandService = get(BandService.class);
        }

        QosSliceCommandEnum enumCommand = QosSliceCommandEnum.enumFromString(command);
        if (enumCommand != null) {
            switch (enumCommand) {
                case ADD_BAND:
                    addBand(sliceName, optArg);
                    break;
                case ADD_METERS:
                    addMeters(sliceName);
                    break;
                case ADD_METER:
                    addMeter(sliceName);
                    break;
                case ADD_QOS:
                    addQos(sliceName);
                    break;
                case CREATE:
                    create(sliceName,optArg);
                    break;
                case DELETE:
                    delete(sliceName);
                    break;
                case LIST:
                    list();
                    break;
                case REMOVE_METER:
                    removeMeter(sliceName);
                    break;
                case REMOVE_QOS:
                    removeQos(sliceName);
                    break;
                case REMOVE_BANDS:
                    removeBands(sliceName);
                    break;
                case SHOW:
                    show(sliceName);
                    break;
                case CLEAN:
                    cleanSlice();
                    break;
                default:
                    print(QOS_SLICE_COMMAND_NOT_FOUND, command);
            }
        } else {
            print(QOS_SLICE_COMMAND_NOT_FOUND, command);
        }
    }

    /**
     * Adds a Band to the Meters of the Slice.
     *
     * @param sliceName the name of the Slice
     * @param bandName  the name of the Band to add
     */
    protected void addBand(String sliceName, String bandName) {

        if (sliceName == null) {
            print(INSERT_SLICE_NAME);
            return;
        }
        if (bandName == null) {
            print(INSERT_BAND);
            return;
        }
        QosSliceData qoSData = qoSSlicing.getSlice(sliceName);
        if(qoSData == null){
            print(SLICE_NOT_FOUND, sliceName);
            return;
        }
        if( bandService.getBand(bandName) == null){
            print(BAND_NOT_FOUND, bandName);
            return;
        }
        Meter.Unit bandUnit = bandService.getBand(bandName).keySet().iterator().next();
        if(!qoSData.getBands().isEmpty()){
            if(bandUnit!= qoSData.getMeterUnit()){
            print(METER_UNIT_ERROR_FOUND, bandUnit, qoSData.getMeterUnit());
            return;
            }
        }
        qoSSlicing.addBand(qoSData,bandService.getBand(bandName));
    }

    /**
     * Adds a Meter to a Slice.
     *
     * @param sliceName the name of the Slice
     */
    protected void addMeter(String sliceName ) {

       // log.info("Dentro addMeter "+sliceName);

        if (sliceName == null) {
            print(INSERT_SLICE_NAME);
            return;
        }

        QosSliceData qoSData = qoSSlicing.getSlice(sliceName);

        if(qoSData == null){
            print(SLICE_NOT_FOUND, sliceName);
            return;
        }

        if ( qoSData.getMeter()) {
            print(METER_ALREADY_ASSOCIATED,sliceName);
            return;
        }

        if ( qoSData.getBands().isEmpty()) {
            print(INSERT_BAND_SLICE,sliceName);
            return;
        }

        qoSSlicing.addMeter(qoSData);
    }

    /**
     * Adds a Meters to a Slice.
     *
     * @param sliceName the name of the Slice
     */
    protected void addMeters(String sliceName ) {

        log.info("Dentro addMeters "+sliceName);

        if (sliceName == null) {
            print(INSERT_SLICE_NAME);
            return;
        }

        QosSliceData qoSData = qoSSlicing.getSlice(sliceName);

        if(qoSData == null){
            print(SLICE_NOT_FOUND, sliceName);
            return;
        }
        //sistemare per meters
        if ( qoSData.getMeter2Level()) {
            print(METER_ALREADY_ASSOCIATED,sliceName);
            return;
        }

        if ( qoSData.getBands().isEmpty()) {
            print(INSERT_BAND_SLICE,sliceName);
            return;
        }

        qoSSlicing.addMeter2Level(qoSData);
    }

    /**
     * Adds a Queue to a Slice.
     *
     * @param sliceName  the name of the Slice
     */
    protected void addQos(String sliceName) {

        //log.info("Dentro create addQos ");

        if (sliceName == null) {
            print(INSERT_SLICE_NAME);
            return;
        }


        QosSliceData qoSData = qoSSlicing.getSlice(sliceName);

        if(qoSData == null){
            print(SLICE_NOT_FOUND, sliceName);
            return;
        }


        if ( qoSData.getQueue()) {
            print(QOS_ALREADY_ASSOCIATED,sliceName);
            return;
        }

        qoSSlicing.addQueue(qoSData);

    }

    /**
     * Creates a new menagment performanceSlice .
     *
     * @param sliceName the name of the Slice
     * @param vplsName the name of the VLPS
     */
    protected void create(String sliceName, String vplsName) {
        if (sliceName == null || sliceName.isEmpty()) {
            print(INSERT_SLICE_NAME);
            return;
        }
        if (vplsName == null || vplsName.isEmpty()) {
            print(INSERT_VPLS_NAME);
            return;
        }
        QosSliceData qoSData = qoSSlicing.getSlice(sliceName);
        VplsData vplsData = vpls.getVpls(vplsName);

        if (qoSData != null) {
            print(SLICE_ALREADY_EXISTS, sliceName);
            return;
        }
        if (vplsData == null) {
            print(VPLS_NOT_FOUND, vplsName);
            return;
        }
        qoSSlicing.createSliceMonitoring(sliceName, vplsName);
    }

    /**
     * Remove all Slices.
     */
    protected void cleanSlice() {
        qoSSlicing.removeAllSlice();
    }

    /**
     * Deletes a Slice.
     *
     * @param sliceName the name of the Slice
     */
    protected void delete(String sliceName){

        log.info("Dentro remove Slice");

        if (sliceName == null) {
            print(INSERT_SLICE_NAME);
            return;
        }
        QosSliceData qoSData = qoSSlicing.getSlice(sliceName);
        if (qoSData == null) {
            print(SLICE_NOT_FOUND, sliceName);
            return;
        }
        if (CHANGING_STATE.contains(qoSData.state())) {
            // when a VPLS is updating, we shouldn't try modify it.
            print("VPLS %s still updating, please wait it finished", qoSData.getQosName());
            return;
        }

        qoSSlicing.removeSlice(qoSData);
    }

    /**
     * Lists the configured Slices.
     */
    protected void list() {
        List<String> sliceNames = qoSSlicing.getAllSlice().stream()
                .map(QosSliceData::getQosName)
                .collect(Collectors.toList());
        Collections.sort(sliceNames);

        sliceNames.forEach(slice -> {
            print(slice);
        });
    }

    /**
     * Remove a Meter to a Slice.
     *
     * @param sliceName the name of the Slice
     */
    protected void removeMeter(String sliceName) {

        if (sliceName == null) {
            print(INSERT_VPLS_NAME);
            return;
        }

        QosSliceData qoSData = qoSSlicing.getSlice(sliceName);

        if(qoSData == null){
            print(SLICE_NOT_FOUND, sliceName);
            return;
        }

        if(!qoSData.getMeter()){
            print(METER_UNASSOCIATED, sliceName);
            return;
        }

        qoSSlicing.removeMeter(qoSData);

    }

    /**
     * Remove a Queue to a Slice.
     *
     * @param sliceName the name of the Slice
     */
    protected void removeQos(String sliceName) {

        if (sliceName == null) {
            print(INSERT_VPLS_NAME);
            return;
        }

        QosSliceData qoSData = qoSSlicing.getSlice(sliceName);

        if(qoSData == null){
            print(SLICE_NOT_FOUND, sliceName);
            return;
        }
        if(!qoSData.getQueue()){
            print(QOS_UNASSOCIATED, sliceName);
            return;
        }
        qoSSlicing.removeQos(qoSData);
    }

    /**
     * Removes all bands from a Slice.
     *
     * @param sliceName the name of the interface to remove
     */
    protected void removeBands(String sliceName) {

        if (sliceName == null) {
            print(INSERT_SLICE_NAME);
            return;
        }

        QosSliceData qoSData = qoSSlicing.getSlice(sliceName);

        if(qoSData == null){
            print(SLICE_NOT_FOUND, sliceName);
            return;
        }

        qoSSlicing.removeBands(qoSData);
    }

    /**
     * Shows the details of one or more Slices.
     *
     * @param sliceName the name of the Slice
     */
    protected void show(String sliceName){
        if (!isNullOrEmpty(sliceName)) {
            // A VPLS name is provided. Check first if the VPLS exists
            QosSliceData qoSData = qoSSlicing.getSlice(sliceName);
            if (qoSData != null) {
                Set<String> Bands = qoSData.getBands().stream()
                        .map(Band::toString)
                        .collect(Collectors.toSet());
                print(SLICE_DISPLAY,
                        sliceName,
                        qoSData.getVplsName(),
                        qoSData.getMeter().toString(),
                        qoSData.state().toString(),
                        qoSData.getMeterUnit().toString(),
                        Bands,
                        qoSData.getQueue().toString()
                        );
            } else {
                print(SLICE_NOT_FOUND, sliceName);
            }
        } else {
            Collection<QosSliceData> qosDatas = qoSSlicing.getAllSlice();
            // No Slice names are provided. Display all Slices configured
            print(SEPARATOR);
            qosDatas.forEach(qosData -> {
                Set<String> Bands = qosData.getBands().stream()
                        .map(Band::toString)
                        .collect(Collectors.toSet());
                print(SLICE_DISPLAY,
                        qosData.getQosName(),
                        qosData.getVplsName(),
                        qosData.getMeter().toString(),
                        qosData.state().toString(),
                        qosData.getMeterUnit().toString(),
                        Bands,
                        qosData.getQueue().toString()
                );
                print(SEPARATOR);
            });
        }

    }

}


