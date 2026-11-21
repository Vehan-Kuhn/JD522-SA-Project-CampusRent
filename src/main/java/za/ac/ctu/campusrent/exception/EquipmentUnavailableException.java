package za.ac.ctu.campusrent.exception;

public class EquipmentUnavailableException extends Exception {
    public EquipmentUnavailableException(String message) {
        super(message);
    }

    public EquipmentUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}