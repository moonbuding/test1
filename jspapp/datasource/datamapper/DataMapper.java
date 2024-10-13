package com.unimelb.swen90007.jspapp.datasource.datamapper;

import com.unimelb.swen90007.jspapp.domain.*;

import java.sql.SQLException;

/**
 * Abstract base class for data mappers, providing methods to interact with
 * domain objects.
 * Concrete implementations should handle specific types of domain objects.
 */
public abstract class DataMapper {

    /**
     * Retrieves the appropriate DataMapper for a given domain object class.
     *
     * @param clazz the class of the domain object
     * @return a DataMapper instance for the specified domain object class
     * @throws RuntimeException if no mapper is implemented for the provided
     *                          class
     */
    public static DataMapper getMapper(Class<? extends DomainObject> clazz) {
        if (clazz.equals(StudentClub.class)) {
            return new StudentClubMapper();
        } else if (clazz.equals(Event.class)) {
            return new EventMapper();
        } else if (clazz.equals(FacultyAdmin.class)) {
            return new FacultyAdminMapper();
        } else if (clazz.equals(Rsvp.class)) {
            return new RsvpMapper();
        } else if (clazz.equals(Student.class)) {
            return new StudentMapper();
        } else if (clazz.equals(Ticket.class)) {
            return new TicketMapper();
        } else if (clazz.equals(Venue.class)) {
            return new VenueMapper();
        } else if (clazz.equals(FundingApplication.class)) {
            return new FundingApplicationMapper();
        }


        throw new RuntimeException("Mapper unimplemented for "
                + clazz.getName());
    }


    /**
     * Inserts a new domain object into the data store.
     *
     * @param obj the domain object to insert
     */
    public abstract void insert(DomainObject obj) throws SQLException;

    /**
     * Updates an existing domain object in the data store.
     *
     * @param obj the domain object to update
     */
    public abstract void update(DomainObject obj);

    /**
     * Deletes a domain object from the data store.
     *
     * @param obj the domain object to delete
     */
    public abstract void delete(DomainObject obj);
}
