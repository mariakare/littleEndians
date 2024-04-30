package be.kuleuven.suitsrestservice.exceptions;

public class AddressNotFoundException extends RuntimeException{

        public AddressNotFoundException() {
            super("No address provided");
        }
        public AddressNotFoundException(String orderNotFound) {
            super(orderNotFound);
        }
}
