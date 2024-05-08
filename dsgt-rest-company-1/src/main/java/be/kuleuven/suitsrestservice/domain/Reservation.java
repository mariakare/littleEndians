package be.kuleuven.suitsrestservice.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Reservation {



    public enum Status {
        PENDING,
        CONFIRMED,
        CANCELLED
    }

    private String reservationId;


    //date and time of reservation uses ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;

    //TODO: add a timeout/expiration for reservation???
    private Status status;
    private Map<String, Integer> suits; // Map of suitId to quantity

    // Constructors
    public Reservation() {
    }

    public Reservation(String reservationId, LocalDateTime timestamp) {
        this.reservationId = reservationId;
        this.timestamp = timestamp;
        this.status = Status.PENDING;
        this.suits = new HashMap<>();
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

    public Map<String, Integer> getSuits() {
        return suits;
    }

    public void setSuits(Map<String, Integer> suits) {
        this.suits = suits;
    }


    public void addSuit(String suitId, int quantity) {
        suits.put(suitId, quantity);
    }

    public void removeSuit(String suitId) {
        suits.remove(suitId);
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
                Objects.equals(suits, that.suits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId, timestamp, status, suits);
    }
}


