package be.kuleuven.weddingrestservice.controllers;

import be.kuleuven.weddingrestservice.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import be.kuleuven.weddingrestservice.domain.ProductsRepository;
import be.kuleuven.weddingrestservice.exceptions.ProductNotFoundException;

import be.kuleuven.weddingrestservice.domain.Reservation;
import org.springframework.web.servlet.view.RedirectView;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
public class ProductsRestController {

    private final ProductsRepository productsRepository;

    @Autowired
    ProductsRestController(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @GetMapping("/")
    public RedirectView redirectToProducts() {
        return new RedirectView("/products");
    }

    // 1. Get All Products
    @GetMapping("/products")
    public ResponseEntity<CollectionModel<EntityModel<Product>>> getAllProducts() {
        List<Product> allProducts = productsRepository.getAllProducts();
        List<EntityModel<Product>> productEntities = allProducts.stream()
                .map(product -> EntityModel.of(product,
                        linkTo(methodOn(ProductsRestController.class).getProductById(product.getId())).withSelfRel(),
                        linkTo(methodOn(ProductsRestController.class).getAllProducts()).withRel("products"),
                        linkTo(methodOn(ProductsRestController.class).reserveProduct(null)).withRel("reserve")))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(productEntities));
    }
    @GetMapping("/products/{id}")
    public ResponseEntity<EntityModel<Product>> getProductById(@PathVariable String id) {
        Optional<Product> product_maybe = productsRepository.getProductById(id);
        if (product_maybe.isEmpty()) {
            throw new ProductNotFoundException("with id " + id);
        }
        Product product = product_maybe.get();
        return ResponseEntity.ok(EntityModel.of(product,
                linkTo(methodOn(ProductsRestController.class).getProductById(product.getId())).withSelfRel(),
                linkTo(methodOn(ProductsRestController.class).getAllProducts()).withRel("products"),
                linkTo(methodOn(ProductsRestController.class).reserveProduct(null)).withRel("reserve")));
    }

    // 3. Reserve Product
    @PostMapping("/products/reserve")
    public ResponseEntity<EntityModel<Reservation>> reserveProduct(@RequestBody Map<String, Integer> productsToReserve) {
        Reservation reservation = productsRepository.reserveProducts(productsToReserve);
        return ResponseEntity.status(HttpStatus.CREATED).body(EntityModel.of(reservation,
                linkTo(methodOn(ProductsRestController.class).getReservationById(reservation.getReservationId())).withSelfRel(),
                linkTo(methodOn(ProductsRestController.class).cancelReservation(reservation.getReservationId())).withRel("cancel"),
                linkTo(methodOn(ProductsRestController.class).confirmReservation(reservation.getReservationId())).withRel("confirm")));
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<EntityModel<Reservation>> getReservationById(@PathVariable String id) {
        Reservation reservation = productsRepository.getReservationById(id);
        List<Link> links = new ArrayList<>(List.of(
                linkTo(methodOn(ProductsRestController.class).getReservationById(reservation.getReservationId())).withSelfRel()));
        if (reservation.getStatusEnum().equals(Reservation.Status.PENDING)) {
            links.add(linkTo(methodOn(ProductsRestController.class).cancelReservation(id)).withRel("cancel"));
            links.add(linkTo(methodOn(ProductsRestController.class).confirmReservation(id)).withRel("confirm"));
        }

        return ResponseEntity.ok(EntityModel.of(reservation, links));
    }

    // 5. Cancel Reservation
    @PostMapping("/reservations/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable String id) {
        productsRepository.cancelReservation(id);
        return ResponseEntity.ok().build();
    }

    // 6. Confirm Reservation (Buy)
    @PostMapping("/reservations/{id}/confirm")
    public ResponseEntity<?> confirmReservation(@PathVariable String id) {
        productsRepository.confirmReservation(id);
        return ResponseEntity.ok().build();
    }

}
