package za.ac.ctu.campusrent.model;

public abstract class Person {
    private final int personId;
    private final int campusId;
    private final String firstName;
    private final String lastName;
    private final String email;

    protected Person(int personId, int campusId, String firstName,
                     String lastName, String email) {
        this.personId = personId;
        this.campusId = campusId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public int getPersonId() {
        return personId;
    }

    public int getCampusId() {
        return campusId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return (firstName + " " + lastName).trim();
    }

    public abstract String getRole();

    public abstract double getDiscountRate();
}