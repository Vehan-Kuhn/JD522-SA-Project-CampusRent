package za.ac.ctu.campusrent.model;

public final class Administrator extends Person {

    public Administrator(int id, int campusId, String firstName,
                         String lastName, String email) {
        super(id, campusId, firstName, lastName, email);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public double getDiscountRate() {
        return 0.0;
    }
}