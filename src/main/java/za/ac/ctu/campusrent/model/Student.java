package za.ac.ctu.campusrent.model;

public final class Student extends Person {
    private final String studentNumber;

    public Student(int id, int campusId, String firstName,
                   String lastName, String email, String studentNumber) {
        super(id, campusId, firstName, lastName, email);
        this.studentNumber = studentNumber;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    @Override
    public String getRole() {
        return "STUDENT";
    }

    @Override
    public double getDiscountRate() {
        return 0.20;
    }
}