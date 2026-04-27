package com.uob.portal.torque3.turbine;

/**
 * Simple immutable DTO implementing {@link TurbineUserContract}.
 */
public final class SimpleTurbineUser implements TurbineUserContract {

    private final Integer turbineUserId;
    private final String userName;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final String email;

    public SimpleTurbineUser(
            Integer turbineUserId,
            String userName,
            String password,
            String firstName,
            String lastName,
            String email) {
        this.turbineUserId = turbineUserId;
        this.userName = userName;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    @Override
    public Integer getTurbineUserId() {
        return turbineUserId;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getEmail() {
        return email;
    }
}
