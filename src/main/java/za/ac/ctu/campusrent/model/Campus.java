package za.ac.ctu.campusrent.model;

/** Represents a Campus inside CampusRent **/
public class Campus {
    //Instance variables
    private int campusId;
    private String campusName;
    private String city;
    private String province;
    private String address;
    private String phone;
    private String email;

    //Default constructor
    public Campus() {
    }

    //Parameterized constructor
    public Campus(int campusid, String campusName, String city, String province, String address, String phone, String email) {
        this.campusId = campusid;
        this.campusName = campusName;
        this.city = city;
        this.province = province;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    //Getters and setters
    public int getCampusId() {
        return campusId;
    }

    public void setCampusId(int campusId) {
        this.campusId = campusId;
    }

    public String getCampusName() {
        return campusName;
    }

    public void setCampusName(String campusName) {
        this.campusName = campusName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    //Display Campus name and city
    @Override
    public String toString() {
        return campusName + " _ " + city;
    }
}
