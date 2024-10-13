package com.unimelb.swen90007.jspapp.auth.action;

import com.unimelb.swen90007.jspapp.auth.AuthorizationEnforcer;
import com.unimelb.swen90007.jspapp.datasource.UnitOfWork;
import com.unimelb.swen90007.jspapp.domain.Event;
import com.unimelb.swen90007.jspapp.domain.Venue;

public class CreateEventAction extends SecurityBaseAction {
    private final Event event;
    private final Venue venue;

    public CreateEventAction(AuthorizationEnforcer enforcer, Event event,
                             Venue venue) {
        super(enforcer);
        this.event = event;
        this.venue = venue;
    }

    @Override
    protected void performAction() {
        UnitOfWork.getCurrent().registerNew(venue);
        UnitOfWork.getCurrent().registerNew(event);
        UnitOfWork.getCurrent().commit();
    }
}