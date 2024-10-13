package com.unimelb.swen90007.jspapp.auth.action;

import com.unimelb.swen90007.jspapp.auth.AuthorizationEnforcer;
import com.unimelb.swen90007.jspapp.datasource.UnitOfWork;
import com.unimelb.swen90007.jspapp.domain.Event;

public class DeleteEventAction extends SecurityBaseAction {
    private final Event event;

    public DeleteEventAction(AuthorizationEnforcer enforcer, Event event) {
        super(enforcer);
        this.event = event;
    }

    @Override
    protected void performAction() {
        UnitOfWork.getCurrent().registerDeleted(event);
        UnitOfWork.getCurrent().commit();
    }
}