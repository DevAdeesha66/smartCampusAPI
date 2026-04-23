/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.DataStore;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
//Room Resource
//Handles all HTTP operations on /api/v1/rooms
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(store.getRooms().values());
        return Response.ok(roomList).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 400);
            err.put("error", "Bad Request");
            err.put("message", "Room 'id' field is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 400);
            err.put("error", "Bad Request");
            err.put("message", "Room 'name' field is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        // Reject duplicate room IDs
        if (store.getRooms().containsKey(room.getId())) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 409);
            err.put("error", "Conflict");
            err.put("message", "A room with id '" + room.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(err).build();
        }
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        store.getRooms().put(room.getId(), room);
        //For successfull resource creation HTTP 201 Created is the correct response
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 404);
            err.put("error", "Not Found");
            err.put("message", "Room '" + roomId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 404);
            err.put("error", "Not Found");
            err.put("message", "Room '" + roomId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        //Can't delete a room that still has sensors assigned
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId); //Throws RoomNotEmptyException which is converted to 409 conflict
        }

        store.getRooms().remove(roomId);

        Map<String, Object> msg = new HashMap<>();
        msg.put("message", "Room '" + roomId + "' has been successfully deleted.");
        return Response.ok(msg).build();
    }
}
