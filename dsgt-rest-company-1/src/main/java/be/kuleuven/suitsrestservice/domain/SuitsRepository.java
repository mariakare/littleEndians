package be.kuleuven.suitsrestservice.domain;

import be.kuleuven.suitsrestservice.exceptions.ReservationException;
import be.kuleuven.suitsrestservice.exceptions.SuitNotFoundException;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;



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

    public Suit getSuitById(String id) {
        Suit suit = suits.get(id);
        if (suit == null) {
            throw new SuitNotFoundException(id);
        }
        return suit;
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


    public synchronized Reservation reserveSuits(Map<String, Integer> suitsToReserve, String userId) {

        String reservationId = UUID.randomUUID().toString();
        Reservation reservation = new Reservation(reservationId, userId, LocalDateTime.now());

        for (Map.Entry<String, Integer> entry : suitsToReserve.entrySet()) {
            String suitId = entry.getKey();
            int quantity = entry.getValue();
            Suit suit = getSuitById(suitId);
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

}




//@Component
//public class SuitsRepository {
//    @Autowired
//    private Firestore db;
//
//    public Suit getSuitById(String id) {
//        try {
//            return db.collection("suits").document(id).get().get().toObject(Suit.class);
//        } catch (InterruptedException | ExecutionException e) {
//            throw new SuitNotFoundException(id);
//        }
//    }
//
//    public List<Suit> getAllSuits() {
//        try {
//            return db.collection("suits").get().get().toObjects(Suit.class);
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException("Error fetching suits", e);
//        }
//    }
//
//    public void reserveSuit(String id, String userId) {
//        try {
//            // Use Firestore transaction for atomicity
//            db.runTransaction(transaction -> {
//                Suit suit = transaction.get(db.collection("suits").document(id)).get().toObject(Suit.class);
//                if (suit == null || suit.getAmountAvailable() <= 0) {
//                    throw new ReservationException("Suit not found or unavailable");
//                }
//                // Create a reservation document
//                Reservation reservation = new Reservation(id, userId);
//                transaction.set(db.collection("reservations").document(), reservation);
//                // Update suit quantity
//                suit.setAmountAvailable(suit.getAmountAvailable() - 1);
//                transaction.set(db.collection("suits").document(id), suit);
//                return null;
//            });
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException("Error reserving suit", e);
//        }
//    }


