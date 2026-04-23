/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.exception;

//This is thrown when a DELETE is attempted on a Room that still has Sensors assigned
//Mapped to HTTP 409 Conflitct by RoomNotEmptyException
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String roomId) {
        super("Room '" + roomId + "' cannot be deleted because it still has sensors assigned to it.");
    }
}
