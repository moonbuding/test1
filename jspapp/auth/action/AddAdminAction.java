package com.unimelb.swen90007.jspapp.auth.action;

import com.unimelb.swen90007.jspapp.auth.AuthorizationEnforcer;
import com.unimelb.swen90007.jspapp.domain.Student;
import com.unimelb.swen90007.jspapp.domain.StudentClub;
import com.unimelb.swen90007.jspapp.datasource.UnitOfWork;

public class AddAdminAction extends SecurityBaseAction{
    private final Student student;
    private final StudentClub club;

    public AddAdminAction(AuthorizationEnforcer enforcer, Student student, StudentClub club) {
        super(enforcer);
        this.student = student;
        this.club = club;
    }

    @Override
    protected void performAction() {
        UnitOfWork.getCurrent().registerDirty(club);
        UnitOfWork.getCurrent().commit();
    }
}