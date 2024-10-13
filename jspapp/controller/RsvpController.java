package com.unimelb.swen90007.jspapp.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.unimelb.swen90007.jspapp.datasource.UnitOfWork;
import com.unimelb.swen90007.jspapp.datasource.datamapper.*;
import com.unimelb.swen90007.jspapp.domain.Event;
import com.unimelb.swen90007.jspapp.domain.Rsvp;
import com.unimelb.swen90007.jspapp.domain.Student;
import com.unimelb.swen90007.jspapp.domain.Ticket;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/rsvp/*")
public class RsvpController extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        switch (pathInfo) {
            case "/create":
                handleCreateRsvp(request, response);
                break;
            case "/cancel":
                handleCancelRsvp(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Invalid endpoint");
        }
    }

    // Handles the creation of an RSVP and tickets
    private void handleCreateRsvp(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        JsonObject jsonObject = getRequestBody(request);
        Long eventId = jsonObject.get("eventID").getAsLong();
        JsonObject rsvpData = jsonObject.getAsJsonObject("RSVPData");
    
        StudentMapper studentMapper = ((StudentMapper) DataMapper.getMapper(Student.class));
        RsvpMapper rsvpMapper = ((RsvpMapper) DataMapper.getMapper(Rsvp.class));
        TicketMapper ticketMapper = ((TicketMapper) DataMapper.getMapper(Ticket.class));
        EventMapper eventMapper = ((EventMapper) DataMapper.getMapper(Event.class));
    
        // Retrieve event
        Event event = eventMapper.find(eventId).orElse(null);
        if (event == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Event not found");
            return;
        }
    
        // Retrieve venue capacity
        int venueCapacity = event.getVenue().getCapacity();
        // Retrieve current attenders count
        int currentAttenders = event.getAttendees();
    
        // Check if the event is full
        if (currentAttenders >= venueCapacity) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Event is full");
            return;
        }
    
        String rsvpEmail = rsvpData.get("rsvp").getAsString();
        String email1 = rsvpData.get("email1").getAsString();
        String email2 = rsvpData.get("email2").getAsString();
        String email3 = rsvpData.get("email3").getAsString();
        String email4 = rsvpData.get("email4").getAsString();
    
        // Retrieve student by RSVP email
        Optional<Student> studentOpt = studentMapper.findByEmail(rsvpEmail);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
    
            // Create RSVP
            Rsvp rsvp = new Rsvp();
            rsvp.setEvent(event);
            rsvp.setStudent(student);
            rsvp.setCancelled(false);
    
            try {
                // Check if RSVP already exists
                Optional<Rsvp> existingRsvp = rsvpMapper.findByEventAndStudent(event, student);
                if (existingRsvp.isPresent()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Duplicate RSVP");
                    return;
                }
                // Insert RSVP
                rsvpMapper.insert(rsvp);
    
                // Create tickets for each email
                createTicketForEmail(email1, event, rsvp, studentMapper, ticketMapper);
                createTicketForEmail(email2, event, rsvp, studentMapper, ticketMapper);
                createTicketForEmail(email3, event, rsvp, studentMapper, ticketMapper);
                createTicketForEmail(email4, event, rsvp, studentMapper, ticketMapper);
    
                // Update event attenders count
                updateEventAttenders(event, ticketMapper);
    
                response.getWriter().write(new Gson().toJson("message: Successfully created RSVP"));
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (SQLException e) {
                // Catch any SQL exceptions and return a bad request error
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Student not found");
        }
    }

    // Handles the cancellation of an RSVP and tickets
    private void handleCancelRsvp(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        JsonObject jsonObject = getRequestBody(request);
        Long rsvpId = jsonObject.get("rsvpID").getAsLong();
        String email1 = jsonObject.get("email1").getAsString();
        String email2 = jsonObject.get("email2").getAsString();
        String email3 = jsonObject.get("email3").getAsString();
        String email4 = jsonObject.get("email4").getAsString();

        StudentMapper studentMapper = ((StudentMapper) DataMapper.getMapper(Student.class));
        RsvpMapper rsvpMapper = ((RsvpMapper) DataMapper.getMapper(Rsvp.class));
        TicketMapper ticketMapper = ((TicketMapper) DataMapper.getMapper(Ticket.class));

        // Retrieve RSVP
        Optional<Rsvp> rsvpOpt = rsvpMapper.findById(rsvpId);
        if (!rsvpOpt.isPresent()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: RSVP not found");
            return;
        }

        Rsvp rsvp = rsvpOpt.get();
        Event event = rsvp.getEvent();

        // Check if the event has already occurred
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventDateTime = LocalDateTime.ofEpochSecond(event.getDateTime() / 1000, 0, ZoneOffset.UTC);

        if (now.isAfter(eventDateTime)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Error: Cannot cancel RSVP after event has occurred");
            return;
        }

        try {
            // Cancel tickets for each email
            cancelTicketForEmail(email1, rsvp, studentMapper, ticketMapper);
            cancelTicketForEmail(email2, rsvp, studentMapper, ticketMapper);
            cancelTicketForEmail(email3, rsvp, studentMapper, ticketMapper);
            cancelTicketForEmail(email4, rsvp, studentMapper, ticketMapper);

            // If all tickets are cancelled, cancel the RSVP
            if (allTicketsCancelled(rsvp, ticketMapper)) {
                rsvp.setCancelled(true);
                rsvpMapper.update(rsvp);
            }

            // Update event attenders count
            updateEventAttenders(event, ticketMapper);

            // Return success message as JSON
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("message", "Successfully cancelled RSVP and tickets");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(responseJson.toString());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    // Handles GET requests to retrieve RSVP data
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        switch (pathInfo) {
            case "/myRSVPs":
                handleGetMyRSVPs(request, response);
                break;
            case "/details":
                handleGetRsvpDetails(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint");
        }
    }

    // Handles the retrieval of RSVP details
    private void handleGetRsvpDetails(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Retrieve RSVP ID from request
        String rsvpIdStr = request.getParameter("rsvpId");
        if (rsvpIdStr == null || rsvpIdStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing rsvpId parameter");
            return;
        }

        Long rsvpId = Long.parseLong(rsvpIdStr);
        RsvpMapper rsvpMapper = ((RsvpMapper) DataMapper.getMapper(Rsvp.class));
        TicketMapper ticketMapper = ((TicketMapper) DataMapper.getMapper(Ticket.class));
        Optional<Rsvp> rsvpOpt = rsvpMapper.findById(rsvpId);

        if (rsvpOpt.isPresent()) {
            Rsvp rsvp = rsvpOpt.get();
            Event event = rsvp.getEvent();
            List<Ticket> tickets = ticketMapper.findByRsvp(rsvpId);

            Map<String, Object> rsvpMap = new HashMap<>();
            rsvpMap.put("rsvpId", rsvp.getId());
            rsvpMap.put("operateTime", rsvp.getIssueDate());
            rsvpMap.put("rsvpEmail", rsvp.getStudent().getEmail());

            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put("id", event.getId());
            eventMap.put("name", event.getTitle());
            eventMap.put("date", LocalDateTime.ofEpochSecond(event.getDateTime() / 1000, 0, ZoneOffset.UTC)
                    .toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            eventMap.put("time", LocalDateTime.ofEpochSecond(event.getDateTime() / 1000, 0, ZoneOffset.UTC)
                    .toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME));
            eventMap.put("host", event.getClub().getName());
            eventMap.put("location", event.getVenue().getAddress());
            eventMap.put("attenders", event.getAttendees());
            eventMap.put("description", event.getDescription());
            rsvpMap.put("event", eventMap);

            List<String> ticketEmails = tickets.stream()
                    .map(ticket -> ticket.getStudent().getEmail())
                    .collect(Collectors.toList());
            rsvpMap.put("ticketEmails", ticketEmails);

            // Return RSVP details as JSON
            String json = new Gson().toJson(rsvpMap);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: RSVP not found");
        }
    }

    // Handles the retrieval of RSVPs by student
    private void handleGetMyRSVPs(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Retrieve student email from request
        String studentEmail = request.getParameter("email");

        if (studentEmail == null || studentEmail.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing email parameter");
            return;
        }

        StudentMapper studentMapper = ((StudentMapper) DataMapper.getMapper(Student.class));
        RsvpMapper rsvpMapper = ((RsvpMapper) DataMapper.getMapper(Rsvp.class));

        // Retrieve student by email
        Optional<Student> studentOpt = studentMapper.findByEmail(studentEmail);

        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();

            // Retrieve RSVPs by student
            List<Rsvp> rsvps = rsvpMapper.findByStudent(student.getId());

            // Filter out cancelled RSVPs and construct JSON response with
            // necessary fields
            List<Map<String, Object>> rsvpList = rsvps.stream()
                    .filter(rsvp -> !rsvp.getCancelled())
                    .map(rsvp -> {
                        Map<String, Object> rsvpMap = new HashMap<>();
                        rsvpMap.put("rsvpId", rsvp.getId());
                        rsvpMap.put("eventName", rsvp.getEvent().getTitle());

                        // Retrieve RSVP by ID to get operateTime
                        Optional<Rsvp> rsvpOpt = rsvpMapper.findById(rsvp.getId());
                        if (rsvpOpt.isPresent()) {
                            Rsvp detailedRsvp = rsvpOpt.get();
                            rsvpMap.put("operateTime", detailedRsvp.getIssueDate());
                        }

                        return rsvpMap;
                    }).collect(Collectors.toList());

            // Return RSVPs as JSON
            String json = new Gson().toJson(rsvpList);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Student not found");
        }
    }

    private void cancelTicketForEmail(String email, Rsvp rsvp,
            StudentMapper studentMapper,
            TicketMapper ticketMapper)
            throws SQLException {
        if (email != null && !email.isEmpty()) {
            Optional<Student> studentOpt = studentMapper.findByEmail(email);
            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();
                Optional<Ticket> ticketOpt = ticketMapper.findByRsvpAndStudent(rsvp, student);
                if (ticketOpt.isPresent()) {
                    Ticket ticket = ticketOpt.get();
                    ticketMapper.delete(ticket);
                }
            }
        }
    }

    private boolean allTicketsCancelled(Rsvp rsvp, TicketMapper ticketMapper)
            throws SQLException {
        return ticketMapper.findByRsvp(rsvp.getId()).isEmpty();
    }

    private void createTicketForEmail(String email, Event event, Rsvp rsvp,
            StudentMapper studentMapper,
            TicketMapper ticketMapper)
            throws SQLException {
        if (email == null || email.isEmpty())
            return;

        Optional<Student> studentOpt = studentMapper.findByEmail(email);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();

            Ticket ticket = new Ticket();
            ticket.setEvent(event);
            ticket.setRsvp(rsvp);
            ticket.setStudent(student);
            ticketMapper.insert(ticket);
        }
    }

    private void updateEventAttenders(Event event, TicketMapper ticketMapper)
            throws SQLException {
        int attendersCount = ticketMapper.countTicketsByEvent(event);
        event.setAttendees(attendersCount);
        UnitOfWork.getCurrent().registerDirty(event);
        UnitOfWork.getCurrent().commit();
    }

    private JsonObject getRequestBody(HttpServletRequest request)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String jsonString = sb.toString();

        return new Gson().fromJson(jsonString, JsonObject.class);
    }
}