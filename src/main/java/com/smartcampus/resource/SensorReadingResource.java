/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.DataStore;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
//Sensor Reading Sub Resource
//Handles GET and POST on /api/v1/sensors/{sensorId}/readings
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        List<SensorReading> history = store.getReadings()
                .getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(history).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensors().get(sensorId);

        if (sensor == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 404);
            err.put("error", "Not Found");
            err.put("message", "Sensor '" + sensorId + "' not found.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        
        //Maintenance and offline sensors cannot record data
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus()) ||
            "OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            // Automatically generate UUID if the client did not supply an ID
            reading.setId(UUID.randomUUID().toString());
        }
        
        // Automatically set timestamp to the current time if the client did not supply one
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        store.getReadings()
             .computeIfAbsent(sensorId, k -> new ArrayList<>())
             .add(reading);

        //Keep sensor's currentValue in sync
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
