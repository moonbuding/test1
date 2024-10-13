package com.unimelb.swen90007.jspapp.domain;

/**
 * Represents the possible statuses of a funding application.
 */
public enum FundingStatus {
    /**
     * The funding application is in draft mode and not yet submitted.
     */
    DRAFT,

    /**
     * The funding application has been submitted for review.
     */
    SUBMITTED,

    /**
     * The funding application is currently under review.
     */
    IN_REVIEW,

    /**
     * The funding application has been cancelled.
     */
    CANCELLED,

    /**
     * The funding application has been approved.
     */
    APPROVED,

    /**
     * The funding application has been rejected.
     */
    REJECTED
}