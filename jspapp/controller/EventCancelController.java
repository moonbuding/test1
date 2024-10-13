package com.unimelb.swen90007.jspapp.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.unimelb.swen90007.jspapp.auth.*;
import com.unimelb.swen90007.jspapp.auth.action.DeleteEventAction;
import com.unimelb.swen90007.jspapp.datasource.datamapper.UserAuthorizationMapper;
import com.unimelb.swen90007.jspapp.domain.Event;
import com.unimelb.swen90007.jspapp.domain.Student;
import com.unimelb.swen90007.jspapp.domain.StudentClub;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/eventCancel/*")
public class EventCancelController extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(EventCancelController.class);
    private final Gson gson = new Gson();

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if ("/cancelEvent".equals(pathInfo)) {
            handleCancelEvent(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint");
        }
    }

    private void handleCancelEvent(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            JsonObject json = gson.fromJson(request.getReader(), JsonObject.class);

            // Parse the request JSON
            Long eventID = json.get("id").getAsLong();
            String token = json.get("token").getAsString();

            // Authorize the user to delete the event
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
            Event event = new Event(eventID);
            StudentClub club = event.getClub();
            Permission permission = new Permission(PermissionType.DELETE_EVENT, club.getId());
            RequestContext context = new RequestContext(subject, permission);
            DeleteEventAction deleteEventAction = new DeleteEventAction(enforcer, event);
            deleteEventAction.execute(context);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Successfully cancelled the event");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error cancelling the event");
        }
    }
}