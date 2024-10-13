package com.unimelb.swen90007.jspapp.auth;

import java.util.Objects;

public class Permission {
    public static final Long ANY_CLUB = Long.MAX_VALUE;
    private PermissionType type;
    private Long clubID;

    public PermissionType getType() {
        return type;
    }

    public Permission(PermissionType type, Long clubID) {
        this.type = type;
        this.clubID = clubID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return type == that.type && Objects.equals(clubID, that.clubID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, clubID);
    }

    public void setType(PermissionType type) {
        this.type = type;
    }

    public Long getClubID() {
        return clubID;
    }

    public void setClubID(Long clubID) {
        this.clubID = clubID;
    }
}
