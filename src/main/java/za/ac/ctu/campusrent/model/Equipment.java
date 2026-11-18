package za.ac.ctu.campusrent.model;
import java.time.LocalDateTime;

/** Represents Equipment in the CampusRent app **/
public class Equipment {
    private int equipmentId;
    private int campusId;
    private String serialNumber;
    private String equipmentName;
    private String description;
    private String category;
    private double dailyRate;
    private String conditionStatus;
    private String availabilityStatus;
    private LocalDateTime createdAt;

    //Default Constructor
    public Equipment(){
        this.createdAt = LocalDateTime.now();
    }
    //Parameterized Constructor
    public Equipment(int equipmentId, int campusId, String serialNumber, String equipmentName, String description, String category, double dailyRate, String conditionStatus, String availabilityStatus, LocalDateTime createdAt ){
        this.equipmentId = equipmentId;
        this.campusId = campusId;
        this.serialNumber = serialNumber;
        this.equipmentName = equipmentName;
        this.description = description;
        this.category = category;
        this.dailyRate = dailyRate;
        this.conditionStatus = conditionStatus;
        this.availabilityStatus = availabilityStatus;
        this.createdAt = createdAt;
    }

    //Getters and setters
    public int getEquipmentId() {
        return equipmentId;
    }
    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }
    public int getCampusId() {
        return campusId;
    }
    public void setCampusId(int campusId) {
        this.campusId = campusId;
    }
    public String getSerialNumber() {
        return serialNumber;
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    public String getEquipmentName() {
        return equipmentName;
    }
    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public double getDailyRate() {
        return dailyRate;
    }
    public void setDailyRate(double dailyRate) {
        this.dailyRate = dailyRate;
    }
    public String getConditionStatus() {
        return conditionStatus;
    }
    public void setConditionStatus(String conditionStatus) {
        this.conditionStatus = conditionStatus;
    }
    public String getAvailabilityStatus() {
        return availabilityStatus;
    }
    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    //Displays Equipment Information
    @Override
    public String toString() {
        return "Equipment{" +
                "equipmentId=" + equipmentId +
                ", equipmentName='" + equipmentName + '\'' +
                ", category='" + category + '\'' +
                ", dailyRate=" + dailyRate +
                ", availabilityStatus='" + availabilityStatus + '\'' +
                '}';
    }

}
