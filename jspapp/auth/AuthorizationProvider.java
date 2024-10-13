package com.unimelb.swen90007.jspapp.auth;

import com.unimelb.swen90007.jspapp.domain.DomainObject;
import com.unimelb.swen90007.jspapp.domain.Student;
import com.unimelb.swen90007.jspapp.domain.StudentClub;

public class AuthorizationProvider {

    // Method to fetch permissions for a user based on their student ID
    public PermissionsCollection getPermissionsForUser(DomainObject user) {
        PermissionsCollection permissions = new PermissionsCollection();

        // Club administrator permissions
        PermissionType[] adminPermissions = new PermissionType[]{
                PermissionType.CREATE_EVENT, PermissionType.MODIFY_EVENT,
                PermissionType.DELETE_EVENT, PermissionType.VIEW_FUNDING,
                PermissionType.CREATE_FUNDING, PermissionType.ADD_ADMIN,
                PermissionType.REMOVE_ADMIN
        };

        // Add permissions to students for every club they administrate
        if (user instanceof Student) {
            Student student = ((Student) user);
            for (StudentClub club : student.getClubs()) {
                for (PermissionType type : adminPermissions) {
                    permissions.addPermission(new Permission(type, club.getId()));
                }
            }
        }

        // Get permissions for FacultyAdmins
        else {
            permissions.addPermission(new Permission(
                    PermissionType.VIEW_FUNDING, Permission.ANY_CLUB));
            permissions.addPermission(new Permission(
                    PermissionType.APPROVE_FUNDING, Permission.ANY_CLUB));
            permissions.addPermission(new Permission(
                    PermissionType.REJECT_FUNDING, Permission.ANY_CLUB));
        }

        return permissions;
    }
}