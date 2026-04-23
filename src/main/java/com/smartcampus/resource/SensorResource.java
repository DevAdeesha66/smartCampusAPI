/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.DataStore;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
//Sensor Resource
//Handles all HTTP operations on /api/v1/sensors
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(store.getSensors().values());
        
        //Filter is optional. It's only applicable when ?type= query parameter is provided
        if (type != null && !type.trim().isEmpty()) {
            sensorList = sensorList.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(sensorList).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 400);
            err.put("error", "Bad Request");
            err.put("message", "Sensor 'id' field is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 400);
            err.put("error", "Bad Request");
            err.put("message", "Sensor 'roomId' field is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        if (store.getSensors().containsKey(sensor.getId())) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 409);
            err.put("error", "Conflict");
            err.put("message", "A sensor with id '" + sensor.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(err).build();
        }
        
        //roomId must refer to an existing room
        Room room = store.getRooms().get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException // Throws LinkedResourceNotFoundException which is converted into 422 Unprocessable Entity
        (
                "Cannot create sensor: roomId '" + sensor.getRoomId() + "' does not refer to any known room."
            );
        }

        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        store.getSensors().put(sensor.getId(), sensor);
        //Link the new sensor
        room.getSensorIds().add(sensor.getId());
        
        // Initialise an empty reading history list for this sensor
        store.getReadings().put(sensor.getId(), new ArrayList<>());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 404);
            err.put("error", "Not Found");
            err.put("message", "Sensor '" + sensorId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(sensor).build();
    }

    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 404);
            err.put("error", "Not Found");
            err.put("message", "Sensor '" + sensorId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }

        Room room = store.getRooms().get(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }

        store.getSensors().remove(sensorId);
        store.getReadings().remove(sensorId);

        Map<String, Object> msg = new HashMap<>();
        msg.put("message", "Sensor '" + sensorId + "' has been successfully removed.");
        return Response.ok(msg).build();
    }

    // Sub-resource locator — delegates /readings to SensorReadingResource
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadings(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor '" + sensorId + "' does not exist.");
        }
        return new SensorReadingResource(sensorId);
    }
}