package com.unimelb.swen90007.jspapp.auth;

import com.unimelb.swen90007.jspapp.domain.DomainObject;
import com.unimelb.swen90007.jspapp.domain.Student;

public class Subject {
    private DomainObject user;
    private PermissionsCollection permissions;

    public Subject(DomainObject user) {
        this.user = user;
    }

    public void setPermissions(PermissionsCollection permissions) {
        this.permissions = permissions;
    }

    public PermissionsCollection getPermissions() {
        return this.permissions;
    }

    public void setUser(Student user) {
        this.user = user;
    }

    public DomainObject getUser() {
        return user;
    }
}
