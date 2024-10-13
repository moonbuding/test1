package com.unimelb.swen90007.jspapp.auth;

import java.util.HashSet;
import java.util.Set;

public class PermissionsCollection {
    private final Set<Permission> permissions = new HashSet<>();

    public void addPermission(Permission permission) {
        permissions.add(permission);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
}