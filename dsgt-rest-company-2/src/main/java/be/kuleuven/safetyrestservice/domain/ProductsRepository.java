package be.kuleuven.safetyrestservice.domain;

import be.kuleuven.safetyrestservice.exceptions.ReservationException;
import be.kuleuven.safetyrestservice.exceptions.ProductNotFoundException;

import be.kuleuven.safetyrestservice.domain.Reservation;
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

        Product hazmat = new Product();
        hazmat.setId("a268203c-de76-4921-a3e3-439db69c462a");
        hazmat.setName("Hazmat Suit");
        hazmat.setPrice(100.0);
        hazmat.setDescription("High Quality, Full protection Hazmat Suit");
        hazmat.setImageLink("https://www.renderhub.com/cgfed/hazmat-suit/hazmat-suit-03.jpg");
        hazmat.setAmountAvailable(15);
        hazmat.setProductType(ProductType.HAZMAT);
        products.put(hazmat.getId(), hazmat);

        Product mask = new Product();
        mask.setId("b1f3b1b4-1b1b-4b1b-8b1b-1b001b18cb1b");
        mask.setName("IIR-100 Recon Gas Mask");
        mask.setPrice(80.0);
        mask.setDescription("Highest grade gas mask with 2 filters");
        mask.setImageLink("https://parcilsafety.com/cdn/shop/files/IRRYOUTHHERO.jpg?v=1711344590");
        mask.setAmountAvailable(7);
        mask.setProductType(ProductType.GAS_MASK);
        products.put(mask.getId(), mask);

        Product gloves = new Product();
        gloves.setId("c1aefy67-0100-3b1b-8xyc-3k001897qglo");
        gloves.setName("Chemmax push-fit gloves");
        gloves.setPrice(65.50);
        gloves.setDescription("Durable chemical resistant gloves. Pack of 7 gloves (NOT PAIRS)");
        gloves.setImageLink("https://www.vdp.com/resources/resized/800x522/387/439.jpg");
        gloves.setAmountAvailable(60);
        gloves.setProductType(ProductType.GLOVES);
        products.put(gloves.getId(), gloves);

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
        reservation.confirmReservation();
        // reservation.setStatus(Reservation.Status.CONFIRMED);
    }


    public List<Reservation> getAllReservations() {
        // Implement access control for manager role on broker side??
        return new ArrayList<>(reservations.values());
    }



}
