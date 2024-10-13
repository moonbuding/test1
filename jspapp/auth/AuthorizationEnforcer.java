package com.unimelb.swen90007.jspapp.auth;

public class AuthorizationEnforcer {
    private final AuthorizationProvider provider;

    public AuthorizationEnforcer(AuthorizationProvider provider) {
        this.provider = provider;
    }

    public boolean isAuthorised(RequestContext context) {
        Subject subject = context.getSubject();
        Permission action = context.getPermission();

        // Check if user is authenticated and has the required permissions
        PermissionsCollection permissions = subject.getPermissions();
        return permissions.hasPermission(action);
    }

    public void loadPermissionsForSubject(Subject subject) {
        // Load permissions for the authenticated subject
        PermissionsCollection permissions =
                provider.getPermissionsForUser(subject.getUser());
        subject.setPermissions(permissions);
    }
}
