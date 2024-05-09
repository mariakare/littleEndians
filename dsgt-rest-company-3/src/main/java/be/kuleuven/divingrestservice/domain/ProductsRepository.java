package be.kuleuven.divingrestservice.domain;

import be.kuleuven.divingrestservice.exceptions.ProductNotFoundException;
import be.kuleuven.divingrestservice.exceptions.ReservationException;

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
    public void initializeProducts() {

        Product diving_suit = new Product();
        diving_suit.setId("b379314d-ef87-5a32-b4f4-54aec7ad573b");
        diving_suit.setName("Scuba Diving Wetsuit");
        diving_suit.setPrice(200.0);
        diving_suit.setDescription("Waterproof, thermal insulation high quality wetsuit");
        diving_suit.setImageLink("https://img.fruugo.com/product/6/31/397698316_max.jpg");
        diving_suit.setAmountAvailable(5);
        diving_suit.setProductType(ProductType.SCUBA);
        products.put(diving_suit.getId(), diving_suit);

        Product mask = new Product();
        //shift all letters and numbers by 2 and modify every 5th character to a number
        mask.setId("d1h5d3d6-3d3d-6d3d-8d3d-3d001d18dd3d");
        mask.setName("Snorkeling Mask");
        mask.setPrice(55.0);
        mask.setDescription("FINA Approved. Recommended by 9/10 divers");
        mask.setImageLink("https://img.fruugo.com/product/5/82/256626825_max.jpg");
        mask.setAmountAvailable(30);
        mask.setProductType(ProductType.SNORKEL_MASK);
        products.put(mask.getId(), mask);

        Product flippers = new Product();
        //modify id to UUID not used before beginning with q and shifted by 17 letters back
        flippers.setId("q268203c-de76-4921-a3e3-439db69c462a");
        flippers.setName("MARES Flippers");
        flippers.setPrice(65.50);
        flippers.setDescription("Flippers for diving. Rated best flipper in 1980. Flips people off too.");
        flippers.setImageLink("https://hips.hearstapps.com/vader-prod.s3.amazonaws.com/1658155246-mares-1658155241.jpg");
        flippers.setAmountAvailable(30);
        flippers.setProductType(ProductType.FLIPPERS);
        products.put(flippers.getId(), flippers);

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
            throw new ProductNotFoundException(id);
        }
        updatedProduct.setId(id); // Ensure ID remains consistent
        products.put(id, updatedProduct);
        return updatedProduct;
    }

    public void deleteProduct(String id) {
        if (!products.containsKey(id)) {
            throw new ProductNotFoundException(id);
        }
        products.remove(id);
    }


    public synchronized Reservation reserveProducts(Map<String, Integer> productsToReserve) {

        String reservationId = UUID.randomUUID().toString();
        Reservation reservation = new Reservation(reservationId, LocalDateTime.now());

        for (Map.Entry<String, Integer> entry : productsToReserve.entrySet()) {
            String productId = entry.getKey();
            int quantity = entry.getValue();
            Product product = getProductById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
            if (product.getAmountAvailable() < quantity) {
                reservation = null;
                throw new ReservationException("Not enough " + product.getName() + " " + product.getProductType() + " available.");
            }
            // Add the product to the reservation and update availability
            reservation.addProduct(productId, quantity);
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

        for (Map.Entry<String, Integer> entry : reservation.getProducts().entrySet()) {
            String productId = entry.getKey();
            int quantity = entry.getValue();
            Product product = getProductById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
            product.setAmountAvailable(product.getAmountAvailable() + quantity);
            reservation.setStatus(Reservation.Status.CANCELLED);
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
