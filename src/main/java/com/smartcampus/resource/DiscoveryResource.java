/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
//Discovery endpoint
//Handles the GET /api/v1 and returns API metadata with resource links
//Implements HATEOAS by providing navigatonal links in the response so clients do not need to hardcode URLs.
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new HashMap<>();
        //API metadata is returned to the client
        response.put("api", "Smart Campus Sensor & Room Management API");
        response.put("version", "1.0");
        response.put("description", "RESTful API for managing campus rooms and sensors.");
        response.put("owner", "Adeesha Induwara Liyanage");
        response.put("contact", "w2120307@westminster.ac.uk");

        Map<String, String> links = new HashMap<>();
        links.put("self",    "/api/v1");
        //HATEOAS links that helps clients navigate the API by following these
        links.put("rooms",   "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("resources", links);

        return Response.ok(response).build();
    }
}
