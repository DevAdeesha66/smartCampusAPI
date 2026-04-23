/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {

    private static volatile DataStore instance; // Being volatile ensures thread safe visibility

    private final Map<String, Room> rooms = new HashMap<>(); // roomId = Room 
    private final Map<String, Sensor> sensors = new HashMap<>(); //sensorId = Sensor
    private final Map<String, List<SensorReading>> readings = new HashMap<>(); //sensorId = reading history

    private DataStore() {
        seedData();
    }

    public static DataStore getInstance() {
        if (instance == null) {
            synchronized (DataStore.class) {
                if (instance == null) {
                    instance = new DataStore();
                }
            }
        }
        return instance;
    }

    public Map<String, Room> getRooms() { return rooms; }
    public Map<String, Sensor> getSensors() { return sensors; }
    public Map<String, List<SensorReading>> getReadings() { return readings; }

    private void seedData() //Populating with sample data to make sure the API is not empty on the first run 
    
    {
        // Seed rooms
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Science Lab", 30);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);

        // Seed sensors
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001", "CO2", "ACTIVE", 412.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 0.0, "LAB-101");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);

        // Link sensors to rooms
        r1.getSensorIds().add(s1.getId());
        r1.getSensorIds().add(s2.getId());
        r2.getSensorIds().add(s3.getId());

        // Seed readings for TEMP-001
        List<SensorReading> tempReadings = new ArrayList<>();
        tempReadings.add(new SensorReading("READ-0001", System.currentTimeMillis() - 60000, 21.0));
        tempReadings.add(new SensorReading("READ-0002", System.currentTimeMillis(), 22.5));
        readings.put(s1.getId(), tempReadings);
    }
}