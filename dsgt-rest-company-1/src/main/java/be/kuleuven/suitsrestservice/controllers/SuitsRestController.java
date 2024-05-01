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
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
public class SuitsRestController {

    private final SuitsRepository suitsRepository;

    @Autowired
    SuitsRestController(SuitsRepository suitsRepository) {
        this.suitsRepository = suitsRepository;
    }


    @GetMapping("/suits/{id}")
    public ResponseEntity<EntityModel<Suit>> getSuitById(@PathVariable String id) {
        return suitsRepository.getSuitById(id)
                .map(this::toEntityModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/suits")
    public ResponseEntity<CollectionModel<EntityModel<Suit>>> getAllSuits() {
        List<EntityModel<Suit>> suitEntities = suitsRepository.getAllSuits().stream()
                .map(this::toEntityModel)
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(suitEntities,
                linkTo(methodOn(SuitsRestController.class).getAllSuits()).withSelfRel()));
    }

    // Get Reservation by ID
    @GetMapping("/suits/reservations/{id}")
    public ResponseEntity<EntityModel<Reservation>> getReservationById(@PathVariable String id) {
        try {
            Reservation reservation = suitsRepository.getReservationById(id);
            return ResponseEntity.ok(toEntityModel(reservation));
        } catch (ReservationException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Helper method to convert Suit to EntityModel with HATEOAS links
    private EntityModel<Suit> toEntityModel(Suit suit) {
        return EntityModel.of(suit,
                linkTo(methodOn(SuitsRestController.class).getSuitById(suit.getId())).withSelfRel(),
                linkTo(methodOn(SuitsRestController.class).getAllSuits()).withRel("suits"),
                Link.of("/suits/" + suit.getId() + "/reservations", "reserve")
        );
    }

    // Helper method to convert Reservation to EntityModel with HATEOAS links
    private EntityModel<Reservation> toEntityModel(Reservation reservation) {
        return EntityModel.of(reservation,
                linkTo(methodOn(SuitsRestController.class).getReservationById(reservation.getReservationId())).withSelfRel(),
                linkTo(methodOn(SuitsRestController.class).getAllSuits()).withRel("suits")
        );
    }

}
