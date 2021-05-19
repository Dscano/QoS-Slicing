# QoS-Slicing

This Onos application provides network slicing and performance isolation, in terms of both connectivity and performance.

The folders/file contain:
* `ONOS File`: contains the files that you have to change in the onos master repository for utilizing the QoS-Slicing application.
* `target`: contains compiled QoS-Slice application, e.g. the oar file. The compiled file is needed to install the application on onos
* `src`: contains the application java source code. 
* `topology`: is a python script for creating a mininet network.

## Changes in ONOS master repository 

The folder `ONOS File` contains the files that you have to sobstitute in the onos master folder that you have downloaded. The files in the `ONOS File` folder 
have the same name of the files in the onos master folder. So you have to simply copy/paste it in the right folder.

## Steps to install QoS-Slice in Onos
Before to install QoS-Slice you have to activate the [VPLS][VPLS] application, already provided in the onos master. In the the Qos-Slicing folder you have to run this command:

    onos-app localhost install! org.qosslice.app target/qosslice-app-2.3.0-rc3.oar  
    
Once you have installed the QoS-Slicing you can configure the slices, via CLI or REST interface.
    
## Steps to configure QoS-Slice in Onos

To define a `slice` withount performance isolation form host h1 to host h2, you have to run these commands in the onos CLI:

        interface-add of:0000000000000001/1 h1             
        interface-add of:0000000000000004/1 h2 
        vpls create VPLS-1                                 
        vpls add-if VPLS-1 h1                              
        vpls add-if VPLS-1 h2
    
To add/enrich a `slice` with performance isolation for the bandwidth, you have to run these commands in the onos CLI:

    band -br -uk -b 1000 -dp 1 -nb band1 add-band     
    qos-slice create SLICE-1 VPLS-1                    
    qos-slice add-band SLICE-1 band1                
    qos-slice add-meter SLICE-1
    
More in details, in the switches that belonging to the slice `SLICE-1` is installed a meter of type drop that  drop the packet ttha exceed 1Mbit.
    
[VPLS]: https://wiki.onosproject.org/display/ONOS/VPLS+User+Guide
