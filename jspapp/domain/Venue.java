package com.unimelb.swen90007.jspapp.domain;

import com.unimelb.swen90007.jspapp.datasource.datamapper.DataMapper;
import com.unimelb.swen90007.jspapp.datasource.datamapper.VenueMapper;

import java.util.Optional;

/**
 * The Venue class represents a venue where events can be held.
 * Includes information about the venue's type, capacity, and address.
 */
public class Venue extends DomainObject {

    /**
     * The type of the venue (online or in-person).
     */
    private VenueType type;

    /**
     * The capacity of the venue.
     */
    private Integer capacity;

    /**
     * The address of the venue (for in-person venues).
     */
    private String address;

    /**
     * Default constructor for Venue.
     */
    public Venue() {
    }

    /**
     * Constructs a Venue with the specified ID.
     *
     * @param id the ID of the venue.
     */
    public Venue(Long id) {
        super(id);
    }

    /**
     * Constructs a Venue with the specified address.
     *
     * @param address the address of the venue.
     */
    public Venue(String address) {
        this.address = address;
    }

    /**
     * Constructs a Venue with the specified ID, type, and capacity.
     *
     * @param id       the ID of the venue.
     * @param type     the type of the venue.
     * @param capacity the capacity of the venue.
     */
    public Venue(Long id, VenueType type, Integer capacity) {
        super(id);
        this.type = type;
        this.capacity = capacity;
    }

    public Venue(String address, VenueType type, Integer capacity) {
        this.type = type;
        this.address = address;
        this.capacity = capacity;
    }

    /**
     * Gets the type of the venue.
     *
     * @return the type of the venue.
     */
    public VenueType getType() {
        if (type == null) {
            ((VenueMapper) DataMapper.getMapper(Venue.class))
                    .findType(getId())
                    .ifPresent(type -> this.type = type);
        }
        return type;
    }

    /**
     * Sets the type of the venue.
     *
     * @param type the new type of the venue.
     */
    public void setType(VenueType type) {
        this.type = type;
    }

    /**
     * Gets the capacity of the venue.
     *
     * @return the capacity of the venue.
     */
    public Integer getCapacity() {
        if (capacity == null) {
            ((VenueMapper) DataMapper.getMapper(Venue.class))
                    .findCapacity(getId())
                    .ifPresent(capacity -> this.capacity = capacity);
        }
        return capacity;
    }

    /**
     * Sets the capacity of the venue.
     *
     * @param capacity the new capacity of the venue.
     */
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    /**
     * Gets the address of the venue.
     *
     * @return the address of the venue.
     */
    public String getAddress() {
        if (address == null) {
            ((VenueMapper) DataMapper.getMapper(Venue.class))
                    .findAddress(getId())
                    .ifPresent(address -> this.address = address);
        }
        return address;
    }

    /**
     * Sets the address of the venue.
     *
     * @param address the new address of the venue.
     */
    public void setAddress(String address) {
        this.address = address;
    }
}
