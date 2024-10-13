package com.unimelb.swen90007.jspapp.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.unimelb.swen90007.jspapp.datasource.UnitOfWork;
import com.unimelb.swen90007.jspapp.datasource.datamapper.DataMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.FundingApplicationMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.StudentClubMapper;
import com.unimelb.swen90007.jspapp.domain.FundingApplication;
import com.unimelb.swen90007.jspapp.domain.StudentClub;
import com.unimelb.swen90007.jspapp.domain.FundingStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@WebServlet("/fundingApplication/*")
public class FundingApplicationController extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(FundingApplicationController.class);
    private static final long SECONDS_PER_SEM = 15778476L;
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        switch (pathInfo) {
            case "/createFunding":
                handleCreateFundingApplication(request, response);
                break;
            case "/updateFunding":
                handleUpdateFundingApplication(request, response);
                break;
            case "/cancelFunding":
                handleCancelFundingApplication(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint");
                break;
        }
    }

    private void handleCreateFundingApplication(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            FundingApplicationMapper fundingMapper = new FundingApplicationMapper();

            JsonObject jsonObject = getRequestBody(request);
            Long clubId = jsonObject.get("clubId").getAsLong();
            JsonObject fundingData = jsonObject.getAsJsonObject("funding");

            Double amount = fundingData.get("amount").getAsDouble();
            String description = fundingData.get("description").getAsString();
            List<FundingApplication> applications = fundingMapper.findByClub(clubId);

            long currentTimestamp = Instant.now().getEpochSecond();
            Integer semester = (int) (currentTimestamp / SECONDS_PER_SEM);

            // Check if a funding application already exists for this semester
            boolean canCreate = true;
            for (FundingApplication app : applications) {
                if (app.getSemester().equals(semester) && app.getStatus() != FundingStatus.CANCELLED) {
                    canCreate = false;
                    break;
                }
            }

            if (!canCreate) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Error: Funding application already exists for this semester");
                return;
            }

            StudentClubMapper clubMapper = (StudentClubMapper) DataMapper.getMapper(StudentClub.class);

            Optional<String> clubName = clubMapper.findName(clubId);
            Optional<String> clubDescription = clubMapper.findDescription(clubId);

            if (clubName.isPresent() && clubDescription.isPresent()) {
                StudentClub club = new StudentClub(clubId, clubName.get(), clubDescription.get());

                FundingApplication fundingApplication = new FundingApplication(null, description, amount,
                        FundingStatus.SUBMITTED, semester);
                fundingApplication.setClub(club);

                fundingMapper.insert(fundingApplication);
                UnitOfWork.getCurrent().commit();

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("Successfully created funding application");
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Student club not found");
            }
        } catch (Exception e) {
            logger.error("Error while creating funding application", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating funding application");
        }
    }

    private void handleUpdateFundingApplication(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            FundingApplicationMapper fundingMapper = new FundingApplicationMapper();
            JsonObject jsonObject = getRequestBody(request);
            Long applicationId = jsonObject.get("applicationId").getAsLong();
            String newDescription = jsonObject.get("description").getAsString();
            Double newAmount = jsonObject.get("amount").getAsDouble();

            Optional<FundingApplication> optionalApplication = fundingMapper.findById(applicationId);
            if (optionalApplication.isPresent()) {
                FundingApplication application = optionalApplication.get();
                FundingStatus currentStatus = application.getStatus();

                // Only allow updating applications in SUBMITTED or IN_REVIEW state
                if (currentStatus == FundingStatus.SUBMITTED || currentStatus == FundingStatus.IN_REVIEW) {
                    application.setDescription(newDescription);
                    application.setAmount(newAmount);

                    fundingMapper.update(application);
                    UnitOfWork.getCurrent().commit();

                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("Successfully updated funding application");
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Error: Funding application cannot be updated in its current state");
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Funding application not found");
            }
        } catch (Exception e) {
            logger.error("Error while updating funding application", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating funding application");
        }
    }

    private void handleCancelFundingApplication(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            FundingApplicationMapper fundingMapper = new FundingApplicationMapper();
            JsonObject jsonObject = getRequestBody(request);
            Long applicationId = jsonObject.get("applicationId").getAsLong();

            Optional<FundingApplication> optionalApplication = fundingMapper.findById(applicationId);
            if (optionalApplication.isPresent()) {
                FundingApplication application = optionalApplication.get();
                FundingStatus currentStatus = application.getStatus();

                if (currentStatus == FundingStatus.SUBMITTED || currentStatus == FundingStatus.IN_REVIEW) {
                    application.setStatus(FundingStatus.CANCELLED);

                    fundingMapper.update(application);
                    UnitOfWork.getCurrent().commit();

                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("Successfully cancelled funding application");
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Error: Funding application cannot be cancelled in its current state");
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error: Funding application not found");
            }
        } catch (Exception e) {
            logger.error("Error while cancelling funding application", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error cancelling funding application");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if ("/listFunding".equals(pathInfo)) {
            handleListFundingApplications(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid endpoint");
        }
    }

    private void handleListFundingApplications(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            Long clubId = Long.parseLong(request.getParameter("clubId"));
            FundingApplicationMapper fundingMapper = new FundingApplicationMapper();
            List<FundingApplication> applications = fundingMapper.findByClub(clubId);

            JsonArray jsonArray = new JsonArray();
            for (FundingApplication app : applications) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", app.getId());
                jsonObject.addProperty("description", app.getDescription());
                jsonObject.addProperty("amount", app.getAmount());
                jsonObject.addProperty("status", app.getStatus().toString());
                jsonObject.addProperty("semester", app.getSemester());
                jsonArray.add(jsonObject);
            }

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(jsonArray));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            logger.error("Error while listing funding applications", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error listing funding applications");
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
        return gson.fromJson(jsonString, JsonObject.class);
    }
}