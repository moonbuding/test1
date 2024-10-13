package com.unimelb.swen90007.jspapp.auth.action;

import com.unimelb.swen90007.jspapp.auth.AuthorizationEnforcer;
import com.unimelb.swen90007.jspapp.auth.RequestContext;
import org.apache.logging.log4j.LogManager;

// Modify event: ModifyEventAction -- "modify", clubid
// Create event: CreateEventAction -- "create", clubid
// Delete event: DeleteEventAction --
// Add admin: AddAdminAction       -- clubid
// Remove admin: RemoveAdminAction
// Create funding: CreateFundingApplicationAction
// Get list of funding applications: ViewFundingApplicationsAction

public abstract class SecurityBaseAction {
    private final AuthorizationEnforcer enforcer;

    public SecurityBaseAction(AuthorizationEnforcer enforcer) {
        this.enforcer = enforcer;
    }

    public void execute(RequestContext context) {
        // Authorize user before performing the action
        if (enforcer.isAuthorised(context)) {
            performAction();
        } else {
            LogManager.getLogger().warn("Access denied for user: "
                    + context.getSubject().getUser().getId());
        }
    }

    protected abstract void performAction();
}
