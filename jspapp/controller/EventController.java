package com.unimelb.swen90007.jspapp.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.unimelb.swen90007.jspapp.auth.*;
import com.unimelb.swen90007.jspapp.auth.action.CreateEventAction;
import com.unimelb.swen90007.jspapp.auth.action.ModifyEventAction;
import com.unimelb.swen90007.jspapp.datasource.datamapper.DataMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.EventMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.StudentClubMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.UserAuthorizationMapper;
import com.unimelb.swen90007.jspapp.domain.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servlet to handle HTTP requests related to events.
 * This servlet supports both retrieval of all events and searching for events
 * based on specific criteria.
 */
@WebServlet("/events/*")
public class EventController extends HttpServlet {

    private final Gson gson = new Gson();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    /**
     * Handles GET requests to retrieve event data.
     *
     * <p>
     * If the request path is "/events", it retrieves and returns all events.
     * If the request path is "/events/{id}", it retrieves and returns a specific
     * event by ID.
     * </p>
     *
     * @param request  the HttpServletRequest object containing the request data.
     * @param response the HttpServletResponse object for sending the response data.
     * @throws ServletException if a servlet-specific error occurs.
     * @throws IOException      if an I/O error occurs during request handling.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // Retrieve all events
            List<Event> events = ((EventMapper) DataMapper.getMapper(Event.class)).findAll();

            String responseJson = gson
                    .toJson(events.stream().map(this::convertEventToMap).collect(Collectors.toList()));
            respondWithJson(response, responseJson);
        } else if (pathInfo.startsWith("/student")) {
            // Retrieve events by student ID
            handleGetEvents(request, response);
        } else {
            // Retrieve event by ID
            try {
                Long eventId = Long.parseLong(pathInfo.substring(1));
                List<Event> events = ((EventMapper) DataMapper.getMapper(Event.class)).findAll();
                Optional<Event> eventOpt = events.stream()
                        .filter(event -> event.getId().equals(eventId))
                        .findFirst();

                if (eventOpt.isPresent()) {
                    Event event = eventOpt.get();
                    String responseJson = gson.toJson(convertEventToMap(event));
                    respondWithJson(response, responseJson);
                } else {
                    respondWithError(response, HttpServletResponse.SC_NOT_FOUND, "Event not found");
                }
            } catch (NumberFormatException e) {
                respondWithError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid event ID format");
            }
        }
    }

    private void handleGetEvents(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the studentId from the request
        Long studentID = Long.parseLong(request.getParameter("studentID"));

        // Get the clubs from the database
        StudentClubMapper studentClubMapper = (StudentClubMapper) DataMapper.getMapper(StudentClub.class);
        List<StudentClub> clubs = studentClubMapper.findByStudent(studentID);

        // Create a list of JSON objects containing event information
        List<JsonObject> eventInfos = new ArrayList<>();
        for (StudentClub club : clubs) {
            // Get the events of the club
            List<Event> events = club.getEvents();

            for (Event event : events) {
                JsonObject eventInfo = new JsonObject();
                Venue venue = event.getVenue();
                eventInfo.addProperty("id", event.getId());
                eventInfo.addProperty("name", event.getTitle());
                eventInfo.addProperty("date", dateFormat.format(new Date(event.getDateTime())));
                eventInfo.addProperty("time", timeFormat.format(new Date(event.getDateTime())));
                eventInfo.addProperty("attendees", event.getAttendees());
                eventInfo.addProperty("location", venue.getType().getPrettyString());
                eventInfo.addProperty("address", venue.getAddress());
                eventInfo.addProperty("capacity", venue.getCapacity());
                eventInfo.addProperty("description", event.getDescription());
                eventInfos.add(eventInfo);
            }
        }

        // Convert the list of eventInfos to JSON and send it in the response
        String json = new Gson().toJson(eventInfos);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    /**
     * Handles POST requests to search for events based on the provided search
     * criteria.
     *
     * <p>
     * This method reads a JSON object from the request body, performs a search for
     * events matching the criteria,
     * and returns the results as a JSON response.
     * </p>
     *
     * @param request  the HttpServletRequest object containing the request data.
     * @param response the HttpServletResponse object for sending the response data.
     * @throws ServletException if a servlet-specific error occurs.
     * @throws IOException      if an I/O error occurs during request handling.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        try (BufferedReader reader = request.getReader()) {
            if ("/create_event".equals(pathInfo)) {
                handleCreateEvent(request, response);
            } else if ("/modify_event".equals(pathInfo)) {
                handleModifyEvent(request, response);
            } else {
                respondWithError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
            }
        } catch (JsonParseException e) {
            respondWithError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
        } catch (Exception e) {
            respondWithError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }

    private void handleCreateEvent(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            JsonObject json = gson.fromJson(request.getReader(), JsonObject.class);
            JsonObject eventJson = json.getAsJsonObject("event");

            // Parse the request JSON
            String token = getAsStringOrNull(eventJson, "token");
            String title = getAsStringOrNull(eventJson, "name");
            String description = getAsStringOrNull(eventJson, "description");
            String date = getAsStringOrNull(eventJson, "date");
            String time = getAsStringOrNull(eventJson, "time");
            String location = getAsStringOrNull(eventJson, "location");
            String address = getAsStringOrNull(eventJson, "address");
            String capacity = getAsStringOrNull(eventJson, "capacity");
            String clubID = getAsStringOrNull(json, "clubId");

            // Data validation
            if (title == null || date == null || time == null || location == null
                    || address == null || capacity == null || clubID == null
                    || token == null) {
                throw new IllegalArgumentException("Missing required fields");
            }
            String dateTimeString = date + " " + time + ":00";
            Long dateTime = java.sql.Timestamp.valueOf(dateTimeString).getTime();
            int intCap = Integer.parseInt(capacity);
            long longClubID = Long.parseLong(clubID);
            VenueType venueType = location.equals("online") ? VenueType.ONLINE : VenueType.IN_PERSON;
            if (intCap <= 0) {
                throw new IllegalArgumentException("Non-positive capacity");
            }

            Event event = new Event(null, title, description, 0, dateTime, false);
            Venue venue = new Venue(address, venueType, intCap);
            event.setClub(new StudentClub(longClubID));
            event.setVenue(venue);

            // Finalize the changes using the authorization
            UserAuthorizationMapper userAuthMapper =
                    new UserAuthorizationMapper();
            Optional<Long> currentStudentIdOpt =
                    userAuthMapper.findUserIDByToken(token);
            if (currentStudentIdOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        "Error: Invalid token");
                return;
            }
            AuthorizationProvider provider = new AuthorizationProvider();
            AuthorizationEnforcer enforcer = new AuthorizationEnforcer(provider);
            Student student = new Student(currentStudentIdOpt.get());
            Subject subject = new Subject(student);
            enforcer.loadPermissionsForSubject(subject);
            StudentClub club = event.getClub();
            Permission permission = new Permission(PermissionType.CREATE_EVENT, club.getId());
            RequestContext context = new RequestContext(subject, permission);
            CreateEventAction createEventAction = new CreateEventAction(enforcer, event, venue);
            createEventAction.execute(context);

            // Construct the response JSON
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("id", event.getId());
            responseJson.addProperty("name", event.getTitle());
            responseJson.addProperty("date", dateFormat.format(new Date(event.getDateTime())));
            responseJson.addProperty("time", timeFormat.format(new Date(event.getDateTime())));
            responseJson.addProperty("location", event.getVenue().getType().getPrettyString());
            responseJson.addProperty("address", event.getVenue().getAddress());
            responseJson.addProperty("capacity", event.getVenue().getCapacity());
            responseJson.addProperty("description", event.getDescription());

            respondWithJson(response, gson.toJson(responseJson));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("error", "An error occurred while creating the event: " + e.getMessage());
            respondWithJson(response, gson.toJson(errorJson));
        }
    }

    private void handleModifyEvent(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Read the JSON request body
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            String payload = buffer.toString();

            // Extract JSON request fields
            JsonObject json = gson.fromJson(payload, JsonObject.class);
            String token = getAsStringOrNull(json, "token");
            Long eventId = getAsLongOrNull(json, "id");
            String title = getAsStringOrNull(json, "name");
            String description = getAsStringOrNull(json, "description");
            String date = getAsStringOrNull(json, "date");
            String time = getAsStringOrNull(json, "time");
            String location = getAsStringOrNull(json, "location");
            String address = getAsStringOrNull(json, "address");
            String capacity = getAsStringOrNull(json, "capacity");

            // Perform data validation
            if (eventId == null || title == null || date == null || time == null
                    || location == null || address == null || capacity == null
                    || token == null) {
                throw new IllegalArgumentException("Missing required fields");
            }
            String dateTimeString = date + " " + time + ":00";
            Long dateTime = java.sql.Timestamp.valueOf(dateTimeString).getTime();
            int intCapacity = Integer.parseInt(capacity);
            VenueType venueType = location.equals("online") ? VenueType.ONLINE : VenueType.IN_PERSON;

            if (intCapacity <= 0) {
                throw new IllegalArgumentException("Non-positive capacity");
            }
            Event event = new Event(eventId);

            // Ensure the new venue has enough capacity for number of attendees
            if (event.getAttendees() > intCapacity) {
                throw new IllegalArgumentException("Not enough capacity");
            }

            // Create a new venue and modify the event
            Venue venue = new Venue(address, venueType, intCapacity);
            event.setTitle(title);
            event.setDescription(description);
            event.setDateTime(dateTime);
            event.setVenue(venue);

            // Finalize the changes using the authorization
            UserAuthorizationMapper userAuthMapper =
                    new UserAuthorizationMapper();
            Optional<Long> currentStudentIdOpt =
                    userAuthMapper.findUserIDByToken(token);
            if (currentStudentIdOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        "Error: Invalid token");
                return;
            }
            AuthorizationProvider provider = new AuthorizationProvider();
            AuthorizationEnforcer enforcer = new AuthorizationEnforcer(provider);
            Student student = new Student(currentStudentIdOpt.get());
            Subject subject = new Subject(student);
            enforcer.loadPermissionsForSubject(subject);
            StudentClub club = event.getClub();
            Permission permission = new Permission(PermissionType.MODIFY_EVENT, club.getId());
            RequestContext context = new RequestContext(subject, permission);
            ModifyEventAction modifyEventAction = new ModifyEventAction(enforcer, event, venue);
            modifyEventAction.execute(context);

            // Prepare the response JSON
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("id", event.getId());
            responseJson.addProperty("name", event.getTitle());
            responseJson.addProperty("date", dateFormat.format(new Date(event.getDateTime())));
            responseJson.addProperty("time", timeFormat.format(new Date(event.getDateTime())));
            responseJson.addProperty("location", event.getVenue().getType().getPrettyString());
            responseJson.addProperty("address", event.getVenue().getAddress());
            responseJson.addProperty("capacity", event.getVenue().getCapacity());
            responseJson.addProperty("description", event.getDescription());
            respondWithJson(response, gson.toJson(responseJson));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("error", "An error occurred while processing your request: " + e.getMessage());
            respondWithJson(response, gson.toJson(errorJson));
        }
    }

    private Long getAsLongOrNull(JsonObject json, String key) {
        JsonElement element = json.get(key);
        return element != null && !element.isJsonNull() ? element.getAsLong() : null;
    }

    private String getAsStringOrNull(JsonObject json, String key) {
        JsonElement element = json.get(key);
        return element != null && !element.isJsonNull() ? element.getAsString() : null;
    }

    /**
     * Converts a list of events to JSON format, including only selected fields.
     *
     * @param events the list of events to be converted.
     * @return JSON representation of the events, including only the "name" and
     * "address" fields.
     */
    private String convertEventsToJson(List<Event> events) {
        // Transform events to a JSON-like structure with only the required fields
        List<Map<String, String>> eventsJson = events.stream()
                .map(event -> {
                    Map<String, String> eventMap = new HashMap<>();
                    eventMap.put("name", event.getClub().getName());
                    eventMap.put("address", event.getVenue().getAddress());
                    return eventMap;
                })
                .collect(Collectors.toList());

        // Convert the transformed list to JSON
        return gson.toJson(eventsJson);
    }

    /**
     * Converts a single event, specified by its ID, to JSON format.
     *
     * @param eventId the ID of the event to convert.
     * @return JSON representation of the event, including only the "name" and
     * "address" fields.
     */
    private String convertEventToJson(Long eventId) {
        Event event = new Event(eventId);
        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("name", event.getClub().getName());
        eventMap.put("address", event.getVenue().getAddress());
        return gson.toJson(eventMap);
    }

    /**
     * Converts an event to a map containing the required fields.
     *
     * @param event the event to convert.
     * @return a map containing the event's fields.
     */
    private Map<String, Object> convertEventToMap(Event event) {
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("id", event.getId());
        eventMap.put("title", event.getTitle());
        eventMap.put("dateTime", event.getDateTime());
        eventMap.put("host", event.getClub().getName());
        eventMap.put("location", event.getVenue().getAddress());
        eventMap.put("attenders", event.getAttendees());
        eventMap.put("description", event.getDescription());
        return eventMap;
    }

    /**
     * Sends a JSON response to the client.
     *
     * @param response the HttpServletResponse object for sending the response data.
     * @param json     the JSON string to send in the response.
     * @throws IOException if an I/O error occurs while writing the response.
     */
    private void respondWithJson(HttpServletResponse response, String json) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write(json);
    }

    /**
     * Sends an error response to the client.
     *
     * @param response   the HttpServletResponse object for sending the response
     *                   data.
     * @param statusCode the HTTP status code to set for the response.
     * @param message    the error message to include in the response.
     * @throws IOException if an I/O error occurs while writing the response.
     */
    private void respondWithError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("text/plain");
        response.getWriter().write(message);
    }
}