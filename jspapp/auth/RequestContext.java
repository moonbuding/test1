package com.unimelb.swen90007.jspapp.auth;

public class RequestContext {
    private final Subject subject;
    private final Permission permission;

    public RequestContext(Subject subject, Permission permission) {
        this.subject = subject;
        this.permission = permission;
    }

    public Subject getSubject() {
        return subject;
    }

    public Permission getPermission() {
        return permission;
    }
}