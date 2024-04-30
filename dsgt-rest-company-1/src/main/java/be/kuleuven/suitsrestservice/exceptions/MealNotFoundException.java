package be.kuleuven.suitsrestservice.exceptions;

public class MealNotFoundException extends RuntimeException {

    public MealNotFoundException(String id) {
        super("Could not find meal " + id);
    }
    public MealNotFoundException() {
        super("No meal id provided");
    }

}
