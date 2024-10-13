package com.unimelb.swen90007.jspapp.auth;

public enum PermissionType {

    // Event permissions
    CREATE_EVENT,
    MODIFY_EVENT,
    DELETE_EVENT,

    // Admin permissions
    ADD_ADMIN,
    REMOVE_ADMIN,

    // Funding application permissions
    VIEW_FUNDING,
    APPROVE_FUNDING,
    REJECT_FUNDING,
    CREATE_FUNDING,

}
