package com.unimelb.swen90007.jspapp.auth.action;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.unimelb.swen90007.jspapp.auth.AuthorizationEnforcer;
import com.unimelb.swen90007.jspapp.datasource.datamapper.FundingApplicationMapper;
import com.unimelb.swen90007.jspapp.domain.FundingApplication;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;

import java.util.List;

public class ViewFundingAction extends SecurityBaseAction {
    private String response = null;

    public ViewFundingAction(AuthorizationEnforcer enforcer) {
        super(enforcer);
    }

    @Override
    protected void performAction() {
        try {
            FundingApplicationMapper fundingMapper = new FundingApplicationMapper();
            List<FundingApplication> applications = fundingMapper.findAll();

            JsonArray jsonArray = new JsonArray();
            for (FundingApplication app : applications) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", app.getId());
                jsonObject.addProperty("description", app.getDescription());
                jsonObject.addProperty("clubName", app.getClub().getName());
                jsonObject.addProperty("amount", app.getAmount());
                jsonObject.addProperty("status", app.getStatus().toString());
                jsonObject.addProperty("semester", app.getSemester());
                jsonArray.add(jsonObject);
            }

            Gson gson = new Gson();
            response = gson.toJson(jsonArray);
        } catch (Exception e) {
            LogManager.getLogger().error("Error while listing all funding applications", e);
        }
    }

    public String getResponse() {
        return response;
    }
}
