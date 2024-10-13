package com.unimelb.swen90007.jspapp.domain;

/**
 * Represents a person with a name, email address and password.
 */
public abstract class Person extends DomainObject {

    /**
     * The name of the person.
     */
    protected String name;

    /**
     * The email address of the person.
     */
    protected String email;

    /**
     * The password of the person.
     */
    protected String password;

    /**
     * Default constructor.
     */
    public Person() {

    }

    /**
     * Constructs a new instance of Person with the specified id.
     *
     * @param id the id of the person
     */
    public Person(Long id) {
        super(id);
    }

    /**
     * Constructs a new instance of Person with the specified name, email and password.
     *
     * @param name     the name of the person
     * @param email    the email address of the person
     * @param password the password of the person
     */
    public Person(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    /**
     * Constructs a new instance of Person with the specified id, name, email and password.
     *
     * @param id       the id of the person
     * @param name     the name of the person
     * @param email    the email address of the person
     * @param password the password of the person
     */
    public Person(long id, String name, String email, String password) {
        super(id);
        this.name = name;
        this.email = email;
        this.password = password;
    }

    /**
     * Returns the name of the person.
     *
     * @return the name of the person
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the person.
     *
     * @param name the new name of the person
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the email address of the person.
     *
     * @return the email address of the person
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the person.
     *
     * @param email the new email address of the person
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the password of the person.
     *
     * @return the password of the person
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the person.
     *
     * @param password the new password of the person
     */
    public void setPassword(String password) {
        this.password = password;
    }
}