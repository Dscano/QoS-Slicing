/*
 * Copyright 2015-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qosslice.app.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import org.onosproject.net.meter.Band;
import org.onosproject.net.meter.Meter;
import org.onosproject.rest.AbstractWebResource;

import org.onosproject.vpls.api.Vpls;
import org.onosproject.vpls.api.VplsData;
import org.qosslice.app.api.QosSlice;
import org.qosslice.app.api.QosSliceData;
import org.slf4j.Logger;


import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import static org.onlab.util.Tools.readTreeFromStream;
import static org.onlab.util.Tools.nullIsNotFound;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.Map;
/**
 * Query and programm QoS Slice.
 */
@Path("qossliceapp")
public class QosSliceWebResource extends AbstractWebResource {
    @Context
    private UriInfo uriInfo;

    private static final String QOS_NOT_FOUND = "QoS Slice is not found for ";
    private static final String VPLS_NOT_FOUND = "Vpls is not found for ";
    private static final String QOSSLICES = "qosslices";
    private static final String QOSSLICE = "qosslice";
    private static final String BANDS= "bands";
    private static final String UNIT = "unit";

    private final ObjectNode root = mapper().createObjectNode();
    private final Logger log = getLogger(getClass());
    /**
     * Gets all QosSlices. Returns array of all QosSlices in the system.
     *
     * @return 200 OK with a collection of QosSlices
     * @onos.rsModel QosSlices
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQosSlices() {
        ArrayNode slicesNode = root.putArray(QOSSLICES);
        QosSlice service = get(QosSlice.class);
        Collection<QosSliceData> qosSliceDatas = service.getAllSlice();
        if (!qosSliceDatas.isEmpty()) {
            for (QosSliceData entry : qosSliceDatas) {
                slicesNode.add(codec(QosSliceData.class).encode(entry, this));
            }
        }

        return ok(root).build();
    }

    /**
     * Gets QosSlice. Returns a QosSlice by qossliceName.
     * @param  qosSliceName  qosslice name
     * @return 200 OK with a qosslice, return 404 if no entry has been found
     * @onos.rsModel QosSlice
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{qosSliceName}")
    public Response getQosSlice(@PathParam("qosSliceName") String qosSliceName) {
        ArrayNode sliceNode = root.putArray(QOSSLICE);
        QosSlice service = get(QosSlice.class);
        final QosSliceData qosSliceData = nullIsNotFound(service.getSlice(qosSliceName),
                QOS_NOT_FOUND + qosSliceName);
        sliceNode.add(codec(QosSliceData.class).encode(qosSliceData, this));

        return ok(root).build();
    }
    /**
     * Creates new QosSlice. Creates and installs a new QosSlice.<br>
     *
     * @param stream QosSlice JSON
     * @return status of the request - CREATED if the JSON is correct,
     * BAD_REQUEST if the JSON is invalid
     *
     * @onos.rsModel QosSlicePost
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createQoSSlice(InputStream stream) {
        QosSlice service = get(QosSlice.class);
        Vpls vplservice = get(Vpls.class);
        try {
            ObjectNode jsonTree = readTreeFromStream(mapper(), stream);

            QosSliceData qosSliceData = codec(QosSliceData.class).decode(jsonTree, this);
            nullIsNotFound(vplservice.getVpls(qosSliceData.getVplsName()),
                    VPLS_NOT_FOUND + qosSliceData.getVplsName());
            service.createSliceMonitoring(qosSliceData.getQosName(),qosSliceData.getVplsName());
            if(qosSliceData.getQueue()){
                service.addQueue(qosSliceData);
            }
            if(qosSliceData.getMeter()){
                service.addMeter(qosSliceData);
            }
            UriBuilder locationBuilder = uriInfo.getBaseUriBuilder()
                    .path(QOSSLICE);
            return Response
                    .created(locationBuilder.build())
                    .build();

        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    /**
     * Adds a queues.
     *
     * @param qossliceName Qos Slice name
     * @return 204 NO CONTENT
     *
     */
    @POST
    @Path("queues/{qossliceName}")
    public Response addQueues(@PathParam("qossliceName") String qossliceName) {
        QosSlice service = get(QosSlice.class);
        log.info("dentro add queue");
        final QosSliceData qosSliceData = nullIsNotFound(service.getSlice(qossliceName),
                QOS_NOT_FOUND + qossliceName);
        service.addQueue(qosSliceData);
        return Response.noContent().build();
    }

    /**
     * Adds a queues.
     *
     * @param qossliceName Qos Slice name
     * @return 204 NO CONTENT
     *
     */
    @POST
    @Path("meters/{qossliceName}")
    public Response addMeters(@PathParam("qossliceName") String qossliceName) {
        QosSlice service = get(QosSlice.class);
        final QosSliceData qosSliceData = nullIsNotFound(service.getSlice(qossliceName),
                QOS_NOT_FOUND + qossliceName);
        service.addMeter(qosSliceData);
        return Response.noContent().build();
    }

    /**
     * Adds a queues.
     *
     * @param qossliceName Qos Slice name
     * @return 204 NO CONTENT
     *@onos.rsModel MeterPost
     */
    @POST
    @Path("meters/{qossliceName}")
    public Response addMeters(@PathParam("qossliceName") String qossliceName, InputStream stream) {
        QosSlice service = get(QosSlice.class);
        final QosSliceData qosSliceData = nullIsNotFound(service.getSlice(qossliceName),
                QOS_NOT_FOUND + qossliceName);
        try {
            ObjectNode jsonTree = readTreeFromStream(mapper(), stream);
            service.addMeter(qosSliceData);
            jsonTree .forEach(bands -> {
                Band band = codec(Band.class).decode(jsonTree, this);
                Map<Meter.Unit,Band> meteringData = new HashMap<>();
                meteringData.put(qosSliceData.getMeterUnit(),band);
                service.addBand(qosSliceData,meteringData);
            });
            UriBuilder locationBuilder = uriInfo.getBaseUriBuilder()
                    .path(BANDS)
                    .path(qossliceName);
            return Response
                    .created(locationBuilder.build())
                    .build();

        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Removes the specified QosSlice.
     *
     * @param qossliceName QosSlice name
     * @return 204 NO CONTENT
     */
    @DELETE
    @Path("{qossliceName}")
    public Response deleteQosSlice(@PathParam("qossliceName") String qossliceName) {
        QosSlice service = get(QosSlice.class);
        final QosSliceData qosSliceData = nullIsNotFound(service.getSlice(qossliceName),
                QOS_NOT_FOUND + qossliceName);
        service.removeSlice(qosSliceData);
        return Response.noContent().build();
    }

    /**
     * Removes a queues.
     *
     * @param qossliceName Qos Slice name
     * @return 204 NO CONTENT
     *
     */
    @DELETE
    @Path("queues/{qossliceName}")
    public Response deleteQueues(@PathParam("qossliceName") String qossliceName) {
        QosSlice service = get(QosSlice.class);
        final QosSliceData qosSliceData = nullIsNotFound(service.getSlice(qossliceName),
                QOS_NOT_FOUND + qossliceName);
        service.removeQos(qosSliceData);
        return Response.noContent().build();
    }

    /**
     * Removes a meters.
     *
     * @param qossliceName Qos Slice name
     * @return 204 NO CONTENT
     *
     */
    @DELETE
    @Path("meters/{qossliceName}")
    public Response deleteMeters(@PathParam("qossliceName") String qossliceName) {
        QosSlice service = get(QosSlice.class);
        final QosSliceData qosSliceData = nullIsNotFound(service.getSlice(qossliceName),
                QOS_NOT_FOUND + qossliceName);
        service.removeMeter(qosSliceData);
        return Response.noContent().build();
    }
}
