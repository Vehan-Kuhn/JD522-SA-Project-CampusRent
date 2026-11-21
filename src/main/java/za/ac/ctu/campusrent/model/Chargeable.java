package za.ac.ctu.campusrent.model;

import java.text.NumberFormat;
import java.util.Locale;

public interface Chargeable {

    double getBaseAmount();

    default double calculateCharge(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("Person is required.");
        }

        return getBaseAmount() * (1.0 - person.getDiscountRate());
    }

    static String formatRand(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("en-ZA"));

        return format.format(amount);
    }
}