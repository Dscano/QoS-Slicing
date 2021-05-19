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
Before to install QoS-Slice you have to activate the `VPLS` application, already provided in the onos master.
In the the Qos-Slicing folder you have to run this command:

    onos-app localhost install! org.qosslice.app target/qosslice-app-2.3.0-rc3.oar  
    
Once you have installed the QoS-Slicing you can configure the slices, via CLI or REST interface.
    
## Steps to configure QoS-Slice in Onos

To run a single [BMv2][BMv2] switch with GTPV1-P4 pipeline:

    sudo ./simple_switch -i 0@<iface0> -i 1@<iface1> ../main.json 
    
To configure/inspect the flow rules in each switch by hand, you have to access the switch via `Simple_switch_CLI`, provided by behavioral model, e.g., 

    sudo Simple_switch_CLI --thrift-port portNumber
    
Once you accessed the switch via CLI you are able to configure the flow rules in each table via the commands provided by the CLI, e.g. `table_add`.

To load a set of flow rules in a switch from a file you can use:

    sudo Simple_switch_CLI --thrift-port portNumber < .../Commands_s1.txt
    
[GTP.v1]: https://en.wikipedia.org/wiki/GPRS_Tunnelling_Protocol
[BMv2]: https://github.com/p4lang/behavioral-model
