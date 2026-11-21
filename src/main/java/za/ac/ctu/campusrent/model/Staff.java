package za.ac.ctu.campusrent.model;

public final class Staff extends Person {
    private final String staffNumber;

    public Staff(int id, int campusId, String firstName,
                 String lastName, String email, String staffNumber) {
        super(id, campusId, firstName, lastName, email);
        this.staffNumber = staffNumber;
    }

    public String getStaffNumber() {
        return staffNumber;
    }

    @Override
    public String getRole() {
        return "STAFF";
    }

    @Override
    public double getDiscountRate() {
        return 0.0;
    }
}