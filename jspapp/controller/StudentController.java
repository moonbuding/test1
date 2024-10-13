package com.unimelb.swen90007.jspapp.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.unimelb.swen90007.jspapp.datasource.UnitOfWork;
import com.unimelb.swen90007.jspapp.datasource.datamapper.StudentMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.UserAuthorizationMapper;
import com.unimelb.swen90007.jspapp.domain.Student;
import com.unimelb.swen90007.jspapp.util.TokenGenerator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

/**
 * This class is a controller for handling student related operations.
 */
@WebServlet("/student/*")
public class StudentController extends HttpServlet {

    private StudentMapper studentMapper = new StudentMapper();
    private UserAuthorizationMapper userAuthorizationMapper = new UserAuthorizationMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();

        if ("/register".equals(path)) {
            handleRegister(request, response);
        } else if ("/login".equals(path)) {
            handleLogin(request, response);
        } else if ("/logout".equals(path)) {
            handleLogout(request, response);
        }
    }

    public void handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(reader, JsonObject.class);

        String name = json.get("name").getAsString();
        String email = json.get("email").getAsString();
        String password = json.get("password").getAsString();
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt()); // Hash the password using BCrypt

        Optional<Student> existingStudent = studentMapper.findByEmail(email);
        if (existingStudent.isPresent()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Email already exists\"}");
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        } else {
            Student newStudent = new Student(name, email, hashedPassword); // Create a new student
            UnitOfWork.getCurrent().registerNew(newStudent);
            UnitOfWork.getCurrent().commit();

            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"User registered successfully\"}");
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    public void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(reader, JsonObject.class);

        String email = json.get("email").getAsString();
        String password = json.get("password").getAsString();

        Optional<Student> studentOpt = studentMapper.findByEmail(email);
        if (studentOpt.isPresent()) {
            Student existingStudent = studentOpt.get();
            if (BCrypt.checkpw(password, existingStudent.getPassword())) {
                JsonObject responseJson = new JsonObject();
                String token = TokenGenerator.generateToken();

                userAuthorizationMapper.insertToken(existingStudent.getId(), token, false);

                responseJson.addProperty("token", token);
                responseJson.addProperty("message", "Login successful");
                responseJson.addProperty("studentID", existingStudent.getId());
                responseJson.addProperty("name", existingStudent.getName());
                responseJson.addProperty("email", existingStudent.getEmail());

                response.setContentType("application/json");
                response.getWriter().write(responseJson.toString());
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.getWriter().write("Invalid email or password");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            response.getWriter().write("Invalid email or password");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    public void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            userAuthorizationMapper.deleteToken(token);
            response.getWriter().write("Logout successful");
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.getWriter().write("Invalid token");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}