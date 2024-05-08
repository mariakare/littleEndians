package be.kuleuven.weddingrestservice.domain;

import be.kuleuven.weddingrestservice.exceptions.ReservationException;
import be.kuleuven.weddingrestservice.exceptions.SuitNotFoundException;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

import java.util.List;



@Component
public class ProductsRepository {

    private static final Map<String, Product> products = new HashMap<>();
    private final Map<String, Reservation> reservations = new HashMap<>();

    @PostConstruct
    public void initializeSuits() {

        Product suit = new Product();
        suit.setId("5268203c-de76-4921-a3e3-439db69c462a");
        suit.setName("Louis Vuitton Tuxedo");
        suit.setPrice(600.0);
        suit.setDescription("Classy black Louis Vuitton Tuxedo");
        suit.setImageLink("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR3vnWiKIhA254Vl7zbQkViPYn3LtuLdIefOQ&s");
        suit.setAmountAvailable(5);
        suit.setProductType(ProductType.TUXEDO);
        products.put(suit.getId(), suit);

        Product gown = new Product();
        gown.setId("4237681a-441f-47fc-a747-8e0169bacea1");
        gown.setName("Dolce & Gabbana Bridal Gown");
        gown.setPrice(800.0);
        gown.setDescription("Gorgeous wedding dress by Dolce & Gabbana");
        gown.setImageLink("https://i.ebayimg.com/images/g/WFIAAOSwet5lbf1S/s-l400.jpg");
        gown.setAmountAvailable(7);
        gown.setProductType(ProductType.GOWN);
        products.put(gown.getId(), gown);

        Product decorations = new Product();
        decorations.setId("cfd1601f-29a0-485d-8d21-7607ec0340c8");
        decorations.setName("Decoration Set");
        decorations.setPrice(500.0);
        decorations.setDescription("Best suit for underwater diving");
        decorations.setImageLink("https://tableclothsfactory.com/cdn/shop/collections/Wedding_Ceremony_Decor.jpg?v=1675883334&width=1200");
        decorations.setAmountAvailable(20);
        decorations.setProductType(ProductType.DECORATION);
        products.put(decorations.getId(), decorations);

    }

    public Optional<Product> getProductById(String id) {
        Product product = products.get(id);
         return Optional.ofNullable(product);
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    public Product addProduct(Product product) {
        // Generate a unique ID (consider using UUID)
        String id = UUID.randomUUID().toString();
        product.setId(id);
        products.put(id, product);
        return product;
    }

    public Product updateProduct(String id, Product updatedProduct) {
        if (!products.containsKey(id)) {
            throw new SuitNotFoundException(id);
        }
        updatedProduct.setId(id); // Ensure ID remains consistent
        products.put(id, updatedProduct);
        return updatedProduct;
    }

    public void deleteProduct(String id) {
        if (!products.containsKey(id)) {
            throw new SuitNotFoundException(id);
        }
        products.remove(id);
    }


    public synchronized Reservation reserveProducts(Map<String, Integer> suitsToReserve) {

        String reservationId = UUID.randomUUID().toString();
        Reservation reservation = new Reservation(reservationId, LocalDateTime.now());

        for (Map.Entry<String, Integer> entry : suitsToReserve.entrySet()) {
            String suitId = entry.getKey();
            int quantity = entry.getValue();
            Product product = getProductById(suitId).orElseThrow(() -> new SuitNotFoundException(suitId));
            if (product.getAmountAvailable() < quantity) {
                reservation = null;
                throw new ReservationException("Not enough " + product.getName() + " suits available.");
            }
            // Add the suit to the reservation and update availability
            reservation.addSuit(suitId, quantity);
            product.setAmountAvailable(product.getAmountAvailable() - quantity);
        }

        // Store the reservation if no exception occurred
        reservations.put(reservationId, reservation);
        return reservation;
    }

    public Reservation getReservationById(String reservationId) {
        Reservation reservation = reservations.get(reservationId);
        if (reservation == null) {
            throw new ReservationException("Reservation with ID " + reservationId + " not found.");
        }
        return reservation;
    }

    public synchronized void cancelReservation(String reservationId) {

        Reservation reservation = getReservationById(reservationId);

        // I have to update suit quantities by going through the reservation, getting suits, and updating the amount available
        // Check if any suits in the reservation have become unavailable
        for (Map.Entry<String, Integer> entry : reservation.getProducts().entrySet()) {
            String suitId = entry.getKey();
            int quantity = entry.getValue();
            Product product = getProductById(suitId).orElseThrow(() -> new SuitNotFoundException(suitId));
//            if (suit.getAmountAvailable() < quantity) {
//                // Suit is no longer available, potentially reserved by someone else
//                reservation.setStatus(Reservation.Status.CANCELLED);
//                throw new ReservationException("Suit(s) in reservation are no longer available.");
//            }
//            else{
                // Suit is available but I don't know...manager cancels reservation for instance
                product.setAmountAvailable(product.getAmountAvailable() + quantity);
                reservation.setStatus(Reservation.Status.CANCELLED);
            //}
        }

    }
    //confirm reservation
    public synchronized void confirmReservation(String reservationId) {
        Reservation reservation = getReservationById(reservationId);
        reservation.setStatus(Reservation.Status.CONFIRMED);
    }


    public List<Reservation> getAllReservations() {
        // Implement access control for manager role on broker side??
        return new ArrayList<>(reservations.values());
    }



}
