package util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class InputValidator {

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) {
            return false;
        }
        String normalizedEmail = email.trim();
        return normalizedEmail.matches(
                "^(?=.{1,254}$)(?=.{1,64}@)[A-Za-z0-9_!#$%&'*+/=?`{|}~^.-]+@(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,63}$"
        );
    }

    public static boolean isValidDate(String date) {
        if (!isNotEmpty(date)) {
            return false;
        }
        try {
            LocalDate.parse(date.trim());
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidScore(double score) {
        return score >= 0.0 && score <= 10.0;
    }

    public static boolean isValidCredits(int credits) {
        return credits >= 1 && credits <= 6;
    }
}
