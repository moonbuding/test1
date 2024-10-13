package com.unimelb.swen90007.jspapp.domain;

/**
 * Represents a domain object with an identifier.
 */
public class DomainObject {

    /**
     * The unique identifier of the domain object.
     */
    private Long id;

    /**
     * Constructs a new {@code DomainObject} with no identifier.
     */
    public DomainObject() {

    }

    /**
     * Constructs a new DomainObject with the specified identifier.
     *
     * @param id the unique identifier for this domain object
     */
    public DomainObject(Long id) {
        this.id = id;
    }

    /**
     * Returns the unique identifier of this domain object.
     *
     * @return the unique identifier of this domain object
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this domain object.
     *
     * @param id the unique identifier to set
     */
    public void setId(Long id) {
        this.id = id;
    }
}
