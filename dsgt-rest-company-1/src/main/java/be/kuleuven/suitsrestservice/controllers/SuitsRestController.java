package be.kuleuven.suitsrestservice.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import be.kuleuven.suitsrestservice.domain.Suit;
import be.kuleuven.suitsrestservice.domain.SuitsRepository;
import be.kuleuven.suitsrestservice.exceptions.SuitNotFoundException;

import be.kuleuven.suitsrestservice.domain.Reservation;
import be.kuleuven.suitsrestservice.exceptions.ReservationException;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
public class SuitsRestController {

    private final SuitsRepository suitsRepository;

    @Autowired
    SuitsRestController(SuitsRepository suitsRepository) {
        this.suitsRepository = suitsRepository;
    }



    // 1. Get All Products
    @GetMapping("/suits")
    public ResponseEntity<CollectionModel<EntityModel<Suit>>> getAllProducts() {
        List<Suit> allSuits = suitsRepository.getAllSuits();
        List<EntityModel<Suit>> suitEntities = allSuits.stream()
                .map(suit -> EntityModel.of(suit,
                        linkTo(methodOn(SuitsRestController.class).getProductById(suit.getId())).withSelfRel(),
                        linkTo(methodOn(SuitsRestController.class).getAllProducts()).withRel("suits"),
                        linkTo(methodOn(SuitsRestController.class).reserveProduct(null)).withRel("reserve")))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(suitEntities));
    }
    @GetMapping("/suits/{id}")
    public ResponseEntity<EntityModel<Suit>> getProductById(@PathVariable String id) {
        Optional<Suit> suit_maybe = suitsRepository.getSuitById(id);
        if (suit_maybe.isEmpty()) {
            throw new SuitNotFoundException("Suit with id " + id + " not found");
        }
        Suit suit = suit_maybe.get();
        return ResponseEntity.ok(EntityModel.of(suit,
                linkTo(methodOn(SuitsRestController.class).getProductById(suit.getId())).withSelfRel(),
                linkTo(methodOn(SuitsRestController.class).getAllProducts()).withRel("suits"),
                linkTo(methodOn(SuitsRestController.class).reserveProduct(null)).withRel("reserve")));
    }

    // 3. Reserve Product
    @PostMapping("/suits/reserve")
    public ResponseEntity<EntityModel<Reservation>> reserveProduct(@RequestBody Map<String, Integer> suitsToReserve) {
        Reservation reservation = suitsRepository.reserveSuits(suitsToReserve);
        return ResponseEntity.status(HttpStatus.CREATED).body(EntityModel.of(reservation,
                linkTo(methodOn(SuitsRestController.class).getReservationById(reservation.getReservationId())).withSelfRel(),
                linkTo(methodOn(SuitsRestController.class).cancelReservation(reservation.getReservationId())).withRel("cancel"),
                linkTo(methodOn(SuitsRestController.class).confirmReservation(reservation.getReservationId())).withRel("confirm")));
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<EntityModel<Reservation>> getReservationById(@PathVariable String id) {
        Reservation reservation = suitsRepository.getReservationById(id);
        List<Link> links = List.of(
                linkTo(methodOn(SuitsRestController.class).getReservationById(reservation.getReservationId())).withSelfRel());
        if (reservation.getStatusEnum() == Reservation.Status.PENDING) {
            links.add(linkTo(methodOn(SuitsRestController.class).cancelReservation(id)).withRel("cancel"));
            links.add(linkTo(methodOn(SuitsRestController.class).confirmReservation(id)).withRel("confirm"));
        }
        return ResponseEntity.ok(EntityModel.of(reservation, links));
    }

    // 5. Cancel Reservation
    @PostMapping("/reservations/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable String id) {
        suitsRepository.cancelReservation(id);
        return ResponseEntity.ok().build();
    }

    // 6. Confirm Reservation (Buy)
    @PostMapping("/reservations/{id}/confirm")
    public ResponseEntity<?> confirmReservation(@PathVariable String id) {
        suitsRepository.confirmReservation(id);
        return ResponseEntity.ok().build();
    }


//    @GetMapping("/suits/{id}")
//    public ResponseEntity<EntityModel<Suit>> getSuitById(@PathVariable String id) {
//        return suitsRepository.getSuitById(id)
//                .map(this::toEntityModel)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @GetMapping("/suits")
//    public ResponseEntity<CollectionModel<EntityModel<Suit>>> getAllSuits() {
//        List<EntityModel<Suit>> suitEntities = suitsRepository.getAllSuits().stream()
//                .map(this::toEntityModel)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(CollectionModel.of(suitEntities,
//                linkTo(methodOn(SuitsRestController.class).getAllSuits()).withSelfRel()));
//    }
//
//    // 2. Reserve Suits
//    @PostMapping("/suits/reserve")
//    public ResponseEntity<EntityModel<Reservation>> reserve(@RequestBody Map<String, Integer> suitsToReserve) {
//        Reservation reservation = suitsRepository.reserveSuits(suitsToReserve);
//        return ResponseEntity.status(HttpStatus.CREATED).body(toEntityModel(reservation));
//    }
//
//
//    @PostMapping("/suits/reservations/{reservationId}/buy")
//    public ResponseEntity<?> buySuits(@PathVariable String reservationId, @RequestBody Map<String, String> body) {
//        try {
//            suitsRepository.confirmReservation(reservationId);
//            return ResponseEntity.ok(EntityModel.of(reservation,
//                    linkTo(methodOn(SuitsRestController.class).getReservationById(reservationId)).withSelfRel(),
//                    linkTo(methodOn(SuitsRestController.class).getAllSuits()).withRel("suits")));
//        } catch (ReservationException ex) {
//            return ResponseEntity.badRequest().body(ex.getMessage());
//        }
//    }
//
//    @DeleteMapping("/suits/reservations/{reservationId}/cancel")
//    public ResponseEntity<?> cancelReservation(@PathVariable String reservationId) {
//        try {
//            suitsRepository.cancelReservation(reservationId);
//            return ResponseEntity.noContent().build(); // 204 No Content
//        } catch (ReservationException ex) {
//            return ResponseEntity.badRequest().body(ex.getMessage());
//        }
//    }
//
//
//
//    // Get Reservation by ID
//    @GetMapping("/suits/reservations/{id}")
//    public ResponseEntity<EntityModel<Reservation>> getReservationById(@PathVariable String id) {
//        try {
//            Reservation reservation = suitsRepository.getReservationById(id);
//            return ResponseEntity.ok(toEntityModel(reservation));
//        } catch (ReservationException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }

//    // Helper method to convert Suit to EntityModel with HATEOAS links
//    private EntityModel<Suit> toEntityModel(Suit suit) {
//        return EntityModel.of(suit,
//                linkTo(methodOn(SuitsRestController.class).getSuitById(suit.getId())).withSelfRel(),
//                linkTo(methodOn(SuitsRestController.class).getAllSuits()).withRel("suits"),
//                Link.of("/suits/" + suit.getId() + "/reservations", "reserve")
//        );
//    }
//
//    // Helper method to convert Reservation to EntityModel with HATEOAS links
//    private EntityModel<Reservation> toEntityModel(Reservation reservation) {
//        return EntityModel.of(reservation,
//                linkTo(methodOn(SuitsRestController.class).getReservationById(reservation.getReservationId())).withSelfRel(),
//                linkTo(methodOn(SuitsRestController.class).getAllSuits()).withRel("suits")
//        );
//    }

}
