package be.kuleuven.suitsrestservice.domain;

import be.kuleuven.suitsrestservice.exceptions.ReservationException;
import be.kuleuven.suitsrestservice.exceptions.SuitNotFoundException;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

import java.util.List;



@Component
public class SuitsRepository {

    private static final Map<String, Suit> suits = new HashMap<>();
    private final Map<String, Reservation> reservations = new HashMap<>();

    @PostConstruct
    public void initializeSuits() {

        Suit a = new Suit();
        a.setId("5268203c-de76-4921-a3e3-439db69c462a");
        a.setName("Louis Vuitton Tuxedo");
        a.setPrice(1000.0);
        a.setDescription("Classy black Louis Vuitton Tuxedo");
        a.setImageLink("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR3vnWiKIhA254Vl7zbQkViPYn3LtuLdIefOQ&s");
        a.setAmountAvailable(5);
        a.setSuitType(SuitType.TUXEDO);
        suits.put(a.getId(), a);

        Suit b = new Suit();
        b.setId("4237681a-441f-47fc-a747-8e0169bacea1");
        b.setName("Hazmat Suit");
        b.setPrice(200.0);
        b.setDescription("Protective suit for hazardous materials");
        b.setImageLink("https://s32891.pcdn.co/wp-content/uploads/2015/10/suit-710276_640-1024x1024.jpg");
        b.setAmountAvailable(50);
        b.setSuitType(SuitType.HAZMAT);
        suits.put(b.getId(), b);

        Suit c = new Suit();
        c.setId("cfd1601f-29a0-485d-8d21-7607ec0340c8");
        c.setName("Scuba diving suit");
        c.setPrice(50.0);
        c.setDescription("Best suit for underwater diving");
        c.setImageLink("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR64RaMkZguiEMk9sAqjh2m9BoXBoIPEX4XRaswfKE_Tg&s");
        c.setAmountAvailable(20);
        c.setSuitType(SuitType.DIVING);
        suits.put(c.getId(), c);

    }

    public Optional<Suit> getSuitById(String id) {
        Suit suit = suits.get(id);
         return Optional.ofNullable(suit);
    }

    public List<Suit> getAllSuits() {
        return new ArrayList<>(suits.values());
    }

    public Suit addSuit(Suit suit) {
        // Generate a unique ID (consider using UUID)
        String id = UUID.randomUUID().toString();
        suit.setId(id);
        suits.put(id, suit);
        return suit;
    }

    public Suit updateSuit(String id, Suit updatedSuit) {
        if (!suits.containsKey(id)) {
            throw new SuitNotFoundException(id);
        }
        updatedSuit.setId(id); // Ensure ID remains consistent
        suits.put(id, updatedSuit);
        return updatedSuit;
    }

    public void deleteSuit(String id) {
        if (!suits.containsKey(id)) {
            throw new SuitNotFoundException(id);
        }
        suits.remove(id);
    }


    public synchronized Reservation reserveSuits(Map<String, Integer> suitsToReserve) {

        String reservationId = UUID.randomUUID().toString();
        Reservation reservation = new Reservation(reservationId, LocalDateTime.now());

        for (Map.Entry<String, Integer> entry : suitsToReserve.entrySet()) {
            String suitId = entry.getKey();
            int quantity = entry.getValue();
            Suit suit = getSuitById(suitId).orElseThrow(() -> new SuitNotFoundException(suitId));
            if (suit.getAmountAvailable() < quantity) {
                reservation = null;
                throw new ReservationException("Not enough " + suit.getName() + " suits available.");
            }
            // Add the suit to the reservation and update availability
            reservation.addSuit(suitId, quantity);
            suit.setAmountAvailable(suit.getAmountAvailable() - quantity);
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
        for (Map.Entry<String, Integer> entry : reservation.getSuits().entrySet()) {
            String suitId = entry.getKey();
            int quantity = entry.getValue();
            Suit suit = getSuitById(suitId).orElseThrow(() -> new SuitNotFoundException(suitId));
//            if (suit.getAmountAvailable() < quantity) {
//                // Suit is no longer available, potentially reserved by someone else
//                reservation.setStatus(Reservation.Status.CANCELLED);
//                throw new ReservationException("Suit(s) in reservation are no longer available.");
//            }
//            else{
                // Suit is available but I don't know...manager cancels reservation for instance
                suit.setAmountAvailable(suit.getAmountAvailable() + quantity);
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