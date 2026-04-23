/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.exception;

//Exception is thrown when a POST reading is attempted on a sensor in maintenance or offline status
//Mapped to HTTP 403 Forbidden by it's ExceptionMapper
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String sensorId, String status) {
        super("Sensor '" + sensorId + "' is currently '" + status + "' and cannot accept new readings.");
    }
}
