package com.unimelb.swen90007.jspapp.datasource;

import com.unimelb.swen90007.jspapp.datasource.datamapper.DataMapper;
import com.unimelb.swen90007.jspapp.domain.DomainObject;
import org.apache.logging.log4j.LogManager;

import java.sql.SQLException; 
import java.util.ArrayList;
import java.util.List;

/**
 * Manages a unit of work in a transactional context, tracking new, dirty, and
 * deleted domain objects.
 */
public class UnitOfWork {

    /**
     * Thread-local variable holding the current UnitOfWork instance for each thread.
     */
    private static final ThreadLocal<UnitOfWork> current = new ThreadLocal<>();

    /**
     * List of new domain objects to be inserted into the database.
     */
    private final List<DomainObject> newObjects = new ArrayList<>();

    /**
     * List of dirty domain objects to be updated in the database.
     */
    private final List<DomainObject> dirtyObjects = new ArrayList<>();

    /**
     * List of domain objects to be deleted from the database.
     */
    private final List<DomainObject> deletedObjects = new ArrayList<>();

    /**
     * Retrieves the current UnitOfWork instance for the current thread.
     * If none exists, a new instance is created and set.
     *
     * @return the current UnitOfWork instance
     */
    public static UnitOfWork getCurrent() {
        if (current.get() == null) {
            current.set(new UnitOfWork());
        }
        return current.get();
    }

    /**
     * Sets the current UnitOfWork instance for the current thread.
     *
     * @param unitOfWork the UnitOfWork instance to set
     */
    public static void setCurrent(UnitOfWork unitOfWork) {
        current.set(unitOfWork);
    }

    /**
     * Registers a new domain object to be managed by this UnitOfWork.
     *
     * @param obj the domain object to register as new
     */
    public void registerNew(DomainObject obj) {
        newObjects.add(obj);
    }

    /**
     * Registers a dirty domain object to be managed by this UnitOfWork.
     *
     * @param obj the domain object to register as dirty
     */
    public void registerDirty(DomainObject obj) {
        dirtyObjects.add(obj);
    }

    /**
     * Registers a domain object to be deleted by this UnitOfWork.
     *
     * @param obj the domain object to register for deletion
     */
    public void registerDeleted(DomainObject obj) {
        deletedObjects.add(obj);
    }

    /**
     * Commits the changes for all registered domain objects:
     * - Inserts new objects
     * - Updates dirty objects
     * - Deletes marked objects
     */
    public void commit() {
        for (DomainObject obj : newObjects) {
            try {
                DataMapper.getMapper(obj.getClass()).insert(obj);
            } catch (SQLException e) {
                LogManager.getLogger().error("Failed to commit object", e);
            }
        }
        for (DomainObject obj : dirtyObjects) {
            DataMapper.getMapper(obj.getClass()).update(obj);
        }
        for (DomainObject obj : deletedObjects) {
            DataMapper.getMapper(obj.getClass()).delete(obj);
        }
    }
}
