package be.kuleuven.safetyrestservice.exceptions;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String id) {
        super("Could not find product " + id);
    }
    public ProductNotFoundException() {
        super("No product id provided");
    }

}
