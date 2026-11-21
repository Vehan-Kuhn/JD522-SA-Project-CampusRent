package za.ac.ctu.campusrent.model;

public enum EquipmentCategory {
    LAPTOP(150.00, 14),
    PROJECTOR(250.00, 7),
    CAMERA(180.00, 7),
    TABLET(100.00, 14),
    NETWORK_KIT(200.00, 7),
    OTHER(50.00, 7);

    private final double dailyRate;
    private final int maxLoanDays;

    EquipmentCategory(double dailyRate, int maxLoanDays) {
        this.dailyRate = dailyRate;
        this.maxLoanDays = maxLoanDays;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public int getMaxLoanDays() {
        return maxLoanDays;
    }
}