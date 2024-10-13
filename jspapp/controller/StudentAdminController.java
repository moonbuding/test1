package com.unimelb.swen90007.jspapp.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.unimelb.swen90007.jspapp.auth.*;
import com.unimelb.swen90007.jspapp.auth.action.AddAdminAction;
import com.unimelb.swen90007.jspapp.auth.action.RemoveAdminAction;
import com.unimelb.swen90007.jspapp.datasource.datamapper.DataMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.StudentClubMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.StudentMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.UserAuthorizationMapper;
import com.unimelb.swen90007.jspapp.domain.Student;
import com.unimelb.swen90007.jspapp.domain.StudentClub;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebServlet("/studentAdmin/*")
public class StudentAdminController extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        switch (pathInfo) {
            case "/myClubs":
                handleMyClubs(request, response);
                break;
            case "/getAdmins":
                handleGetAdmins(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint");
        }
    }

    // Get the clubs that the student is an admin of
    private void handleMyClubs(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the current student's email from the cookies
        String studentEmail = request.getParameter("email");
        if (studentEmail == null || studentEmail.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Email not found in cookies");
            return;
        }

        StudentMapper studentMapper = (StudentMapper) DataMapper.getMapper(Student.class);
        Optional<Student> studentOpt = studentMapper.findByEmail(studentEmail);

        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            List<StudentClub> clubs = student.getClubs();

            // Create a list of JSON objects containing club information
            List<JsonObject> clubInfos = new ArrayList<>();
            for (StudentClub club : clubs) {
                JsonObject clubInfo = new JsonObject();
                clubInfo.addProperty("clubId", club.getId());
                clubInfo.addProperty("clubName", club.getName());
                clubInfos.add(clubInfo);
            }

            // Convert the list of clubInfos to JSON and send it in the response
            String json = new Gson().toJson(clubInfos);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Student not found");
        }
    }

    // Get the admins of a club
    private void handleGetAdmins(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the clubId from the request
        Long studentID = Long.parseLong(request.getParameter("studentID"));

        // Get the club from the database
        StudentClubMapper studentClubMapper = (StudentClubMapper) DataMapper.getMapper(StudentClub.class);
        List<StudentClub> clubs = studentClubMapper.findByStudent(studentID);
        Optional<StudentClub> clubOpt = clubs.isEmpty() ? Optional.empty() : Optional.of(clubs.get(0));

        if (clubOpt.isPresent()) {
            StudentClub club = clubOpt.get();

            // Get the admins of the club
            List<Student> admins = club.getAdmins();

            // Create a list of JSON objects containing admin information
            List<JsonObject> adminInfos = new ArrayList<>();
            for (Student admin : admins) {
                JsonObject adminInfo = new JsonObject();
                adminInfo.addProperty("username", admin.getName());
                adminInfo.addProperty("email", admin.getEmail());
                adminInfos.add(adminInfo);
            }

            // Convert the list of adminInfos to JSON and send it in the response
            String json = new Gson().toJson(adminInfos);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Club not found");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        switch (pathInfo) {
            case "/addAdmin":
                handleAddAdmin(request, response);
                break;
            case "/removeAdmin":
                handleRemoveAdmin(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint");
        }
    }

    private void handleAddAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject jsonObject = getRequestBody(request);
        String email = jsonObject.getAsJsonObject("admin").get("email").getAsString();
        Long clubId = jsonObject.get("clubId").getAsLong();
        String token = jsonObject.get("token").getAsString();

        StudentMapper studentMapper = (StudentMapper) DataMapper.getMapper(Student.class);
        StudentClubMapper studentClubMapper = (StudentClubMapper) DataMapper.getMapper(StudentClub.class);
        UserAuthorizationMapper userAuthMapper = new UserAuthorizationMapper();

        Optional<Long> currentStudentIdOpt = userAuthMapper.findUserIDByToken(token);
        if (currentStudentIdOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Invalid token");
            return;
        }

        Optional<Student> currentStudentOpt = studentMapper.findById(currentStudentIdOpt.get());
        Optional<Student> newAdminOpt = studentMapper.findByEmail(email);
        Optional<StudentClub> clubOpt = studentClubMapper.find(clubId);

        if (currentStudentOpt.isPresent() && newAdminOpt.isPresent() && clubOpt.isPresent()) {
            Student currentStudent = currentStudentOpt.get();
            Student newAdmin = newAdminOpt.get();
            StudentClub club = clubOpt.get();

            if (club.getAdmins().contains(newAdmin)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Error: Student is already an admin of the club");
                return;
            }

            AuthorizationProvider provider = new AuthorizationProvider();
            AuthorizationEnforcer enforcer = new AuthorizationEnforcer(provider);
            Subject subject = new Subject(currentStudent);
            enforcer.loadPermissionsForSubject(subject);
            Permission permission = new Permission(PermissionType.ADD_ADMIN, club.getId());
            RequestContext context = new RequestContext(subject, permission);

            if (enforcer.isAuthorised(context)) {
                club.addMember(newAdmin);
                AddAdminAction addAdminAction = new AddAdminAction(enforcer, newAdmin, club);
                addAdminAction.execute(context);

                response.getWriter().write("Successfully added student as admin");
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Error: Not authorized to add admin");
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Student, Club or Token not found");
        }
    }

    private void handleRemoveAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject jsonObject = getRequestBody(request);
        String email = jsonObject.getAsJsonObject("admin").get("email").getAsString();
        Long clubId = jsonObject.get("clubId").getAsLong();
        String token = jsonObject.get("token").getAsString();

        StudentMapper studentMapper = (StudentMapper) DataMapper.getMapper(Student.class);
        StudentClubMapper studentClubMapper = (StudentClubMapper) DataMapper.getMapper(StudentClub.class);
        UserAuthorizationMapper userAuthMapper = new UserAuthorizationMapper();

        Optional<Long> currentStudentIdOpt = userAuthMapper.findUserIDByToken(token);
        if (currentStudentIdOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Invalid token");
            return;
        }

        Optional<Student> currentStudentOpt = studentMapper.findById(currentStudentIdOpt.get());
        Optional<Student> adminToRemoveOpt = studentMapper.findByEmail(email);
        Optional<StudentClub> clubOpt = studentClubMapper.find(clubId);

        if (currentStudentOpt.isPresent() && adminToRemoveOpt.isPresent() && clubOpt.isPresent()) {
            Student currentStudent = currentStudentOpt.get();
            Student adminToRemove = adminToRemoveOpt.get();
            StudentClub club = clubOpt.get();

            if (!club.getAdmins().contains(adminToRemove)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Student is not an admin of the club");
                return;
            }

            AuthorizationProvider provider = new AuthorizationProvider();
            AuthorizationEnforcer enforcer = new AuthorizationEnforcer(provider);
            Subject subject = new Subject(currentStudent);
            enforcer.loadPermissionsForSubject(subject);
            Permission permission = new Permission(PermissionType.REMOVE_ADMIN, club.getId());
            RequestContext context = new RequestContext(subject, permission);

            if (enforcer.isAuthorised(context)) {
                club.removeMember(adminToRemove);
                RemoveAdminAction removeAdminAction = new RemoveAdminAction(enforcer, adminToRemove, club);
                removeAdminAction.execute(context);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("Successfully removed student as admin");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Error: Not authorized to remove admin");
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Student, Club or Token not found");
        }
    }

    private JsonObject getRequestBody(HttpServletRequest request) throws IOException {
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