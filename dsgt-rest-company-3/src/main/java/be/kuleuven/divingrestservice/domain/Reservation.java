package be.kuleuven.divingrestservice.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;

public class Reservation {



    public enum Status {
        PENDING,
        CONFIRMED,
        CANCELLED,
        DELIVERED
    }

    private String reservationId;


    //date and time of reservation uses ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;

    //TODO: add a timeout/expiration for reservation???
    private Status status;
    private Map<String, Integer> products; // Map of productId to quantity
    private ScheduledExecutorService scheduler;
    // Constructors
    public Reservation() {
    }

    public Reservation(String reservationId, LocalDateTime timestamp) {
        this.reservationId = reservationId;
        this.timestamp = timestamp;
        this.status = Status.PENDING;
        this.products = new HashMap<>();
    }

    public synchronized void confirmReservation(){
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.status = Status.CONFIRMED;

        // Schedule the status change to DELIVERED after a random delay
        int delay = (int) (Math.random() * 21) + 40; // Random delay between 40 and 60 seconds
        scheduler.schedule(() -> {
            this.status = Status.DELIVERED;
        }, delay, TimeUnit.SECONDS);
        this.shutdownScheduler();
    }

    // Add a method to shut down the scheduler when the reservation is no longer needed
    public void shutdownScheduler() {
        scheduler.shutdown();
    }

    // Getters and Setters
    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status.toString();
    }

    public Status getStatusEnum() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Map<String, Integer> getProducts() {
        return products;
    }

    public void setProducts(Map<String, Integer> products) {
        this.products = products;
    }


    public void addProduct(String productId, int quantity) {
        products.put(productId, quantity);
    }

    public void removeProduct(String productId) {
        products.remove(productId);
    }

    // Equals and HashCode for object comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(reservationId, that.reservationId) &&
                Objects.equals(timestamp, that.timestamp) &&
                status == that.status &&
                Objects.equals(products, that.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId, timestamp, status, products);
    }
}