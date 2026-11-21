package za.ac.ctu.campusrent.exception;

public class InvalidRateException extends IllegalArgumentException {
    public InvalidRateException(String message) {
        super(message);
    }

    public InvalidRateException(String message, Throwable cause) {
        super(message, cause);
    }
}