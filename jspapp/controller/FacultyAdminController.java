package com.unimelb.swen90007.jspapp.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.unimelb.swen90007.jspapp.auth.*;
import com.unimelb.swen90007.jspapp.auth.action.CreateEventAction;
import com.unimelb.swen90007.jspapp.auth.action.ViewFundingAction;
import com.unimelb.swen90007.jspapp.datasource.UnitOfWork;
import com.unimelb.swen90007.jspapp.datasource.datamapper.FacultyAdminMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.FundingApplicationMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.UserAuthorizationMapper;
import com.unimelb.swen90007.jspapp.domain.FacultyAdmin;
import com.unimelb.swen90007.jspapp.domain.FundingApplication;
import com.unimelb.swen90007.jspapp.domain.FundingStatus;
import com.unimelb.swen90007.jspapp.util.TokenGenerator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.text.View;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/facultyAdmin/*")
public class FacultyAdminController extends HttpServlet {
    private final UserAuthorizationMapper authorizationMapper
            = new UserAuthorizationMapper();

    private static final Logger logger = LogManager.getLogger(FacultyAdminController.class);
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        switch (pathInfo) {
            case "/login":
                handleLogin(request, response);
                break;
            case "/reviewFunding":
                handleReviewFundingApplication(request, response);
                break;
            case "/approveFunding":
                handleApproveFundingApplication(request, response);
                break;
            case "/rejectFunding":
                handleRejectFundingApplication(request, response);
                break;
            case "/listAllFunding":
                handleListAllFundingApplications(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint");
                break;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if ("/listAllFunding".equals(pathInfo)) {
            handleListAllFundingApplications(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint");
        }
    }

    private void handleLogin(HttpServletRequest request,
                             HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        JsonObject json = gson.fromJson(reader, JsonObject.class);

        String email = json.get("email").getAsString();
        String password = json.get("password").getAsString();

        Optional<FacultyAdmin> facultyAdminOpt = FacultyAdminMapper.findByEmail(email);
        if (facultyAdminOpt.isPresent()) {
            FacultyAdmin existingAdmin = facultyAdminOpt.get();
            if (password.equals(existingAdmin.getPassword())) {
                JsonObject responseJson = new JsonObject();
                String token = TokenGenerator.generateToken();
                authorizationMapper.insertToken(existingAdmin.getId(), token, true);

                responseJson.addProperty("token", token);
                responseJson.addProperty("message", "Login successful");
                responseJson.addProperty("facultyID", existingAdmin.getId());
                responseJson.addProperty("name", existingAdmin.getName());
                responseJson.addProperty("email", existingAdmin.getEmail());

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

    private void handleReviewFundingApplication(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            FundingApplicationMapper fundingMapper = new FundingApplicationMapper();
            JsonObject jsonObject = getRequestBody(request);
            Long applicationId = jsonObject.get("applicationId").getAsLong();

            Optional<FundingApplication> optionalApplication = fundingMapper.findById(applicationId);
            if (optionalApplication.isPresent()) {
                FundingApplication application = optionalApplication.get();
                application.setStatus(FundingStatus.IN_REVIEW);

                fundingMapper.update(application);
                UnitOfWork.getCurrent().commit();

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("Successfully set funding application to in review");
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Funding application not found");
            }
        } catch (Exception e) {
            logger.error("Error while reviewing funding application", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error reviewing funding application");
        }
    }

    private void handleApproveFundingApplication(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            FundingApplicationMapper fundingMapper = new FundingApplicationMapper();
            JsonObject jsonObject = getRequestBody(request);
            Long applicationId = jsonObject.get("applicationId").getAsLong();

            Optional<FundingApplication> optionalApplication = fundingMapper.findById(applicationId);
            if (optionalApplication.isPresent()) {
                FundingApplication application = optionalApplication.get();
                application.setStatus(FundingStatus.APPROVED);

                fundingMapper.update(application);
                UnitOfWork.getCurrent().commit();

                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("message", "Successfully approved funding application");

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(responseJson.toString());
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Funding application not found");
            }
        } catch (Exception e) {
            logger.error("Error while approving funding application", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error approving funding application");
        }
    }

    private void handleRejectFundingApplication(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            FundingApplicationMapper fundingMapper = new FundingApplicationMapper();
            JsonObject jsonObject = getRequestBody(request);
            Long applicationId = jsonObject.get("applicationId").getAsLong();

            Optional<FundingApplication> optionalApplication = fundingMapper.findById(applicationId);
            if (optionalApplication.isPresent()) {
                FundingApplication application = optionalApplication.get();
                application.setStatus(FundingStatus.REJECTED);

                fundingMapper.update(application);
                UnitOfWork.getCurrent().commit();

                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("message", "Successfully rejected funding application");

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(responseJson.toString());
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Funding application not found");
            }
        } catch (Exception e) {
            logger.error("Error while rejecting funding application", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error rejecting funding application");
        }
    }

    private void handleListAllFundingApplications(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            // Retrieve the authorization token
            JsonObject json = gson.fromJson(request.getReader(), JsonObject.class);
            String token = json.get("token").getAsString();
            UserAuthorizationMapper userAuthMapper = new UserAuthorizationMapper();
            Optional<Long> currentStudentIdOpt = userAuthMapper.findUserIDByToken(token);
            if (currentStudentIdOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        "Error: Invalid token");
                return;
            }

            // Get the view funding action
            ViewFundingAction viewFundingAction =
                    getViewFundingAction(currentStudentIdOpt);

            // Retrieve response JSON and ensure not null
            String responseJson = viewFundingAction.getResponse();
            if (responseJson == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        "Error: Invalid token");
                return;
            }

            // Send response
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(viewFundingAction.getResponse());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            logger.error("Error while listing all funding applications", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error listing all funding applications");
        }
    }

    private static ViewFundingAction getViewFundingAction(Optional<Long> currentStudentIdOpt) {
        AuthorizationProvider provider = new AuthorizationProvider();
        AuthorizationEnforcer enforcer = new AuthorizationEnforcer(provider);
        FacultyAdmin facultyAdmin = new FacultyAdmin(currentStudentIdOpt.get());
        Subject subject = new Subject(facultyAdmin);
        enforcer.loadPermissionsForSubject(subject);
        Permission permission = new Permission(PermissionType.VIEW_FUNDING,
                Permission.ANY_CLUB);
        RequestContext context = new RequestContext(subject, permission);
        ViewFundingAction viewFundingAction = new ViewFundingAction(enforcer);
        viewFundingAction.execute(context);
        return viewFundingAction;
    }

    private JsonObject getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String jsonString = sb.toString();
        return gson.fromJson(jsonString, JsonObject.class);
    }
}