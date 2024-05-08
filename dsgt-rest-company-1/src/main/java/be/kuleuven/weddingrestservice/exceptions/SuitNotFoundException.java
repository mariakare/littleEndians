package be.kuleuven.weddingrestservice.exceptions;

public class SuitNotFoundException extends RuntimeException {

    public SuitNotFoundException(String id) {
        super("Could not find suit " + id);
    }
    public SuitNotFoundException() {
        super("No suit id provided");
    }

}
