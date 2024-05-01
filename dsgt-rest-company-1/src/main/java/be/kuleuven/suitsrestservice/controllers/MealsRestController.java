package be.kuleuven.suitsrestservice.controllers;

import be.kuleuven.suitsrestservice.domain.Meal;
import be.kuleuven.suitsrestservice.domain.MealsRepository;
import be.kuleuven.suitsrestservice.domain.Order;
import be.kuleuven.suitsrestservice.domain.OrderConfirmation;
import be.kuleuven.suitsrestservice.exceptions.SuitNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
public class MealsRestController {

    private final MealsRepository mealsRepository;

    @Autowired
    MealsRestController(MealsRepository mealsRepository) {
        this.mealsRepository = mealsRepository;
    }

    @GetMapping("/rest/meals/{id}")
    EntityModel<Meal> getMealById(@PathVariable String id) {
        Meal meal = mealsRepository.findMeal(id).orElseThrow(() -> new SuitNotFoundException(id));

        return mealToEntityModel(id, meal);
    }

    @GetMapping("/rest/meals")
    CollectionModel<EntityModel<Meal>> getMeals() {
        Collection<Meal> meals = mealsRepository.getAllMeal();

        List<EntityModel<Meal>> mealEntityModels = new ArrayList<>();
        for (Meal m : meals) {
            EntityModel<Meal> em = mealToEntityModel(m.getId(), m);
            mealEntityModels.add(em);
        }
        return CollectionModel.of(mealEntityModels,
                linkTo(methodOn(MealsRestController.class).getMeals()).withSelfRel());
    }

    @GetMapping("/rest/cheapest")
    EntityModel<Meal> getCheapestMeal() {
        Meal meal = mealsRepository.getCheapestMeal();
        return mealToEntityModel(meal.getId(), meal);
    }

    @GetMapping("/rest/largest")
    EntityModel<Meal> getLargestMeal() {
        Meal meal = mealsRepository.getLargestMeal();
        return mealToEntityModel(meal.getId(), meal);
    }


    @PostMapping("/rest/meals")
    ResponseEntity<EntityModel<Meal>> addMeal(@RequestBody Meal meal) {
        if (meal.getId() == null) {
            setId(meal);
        }

    mealsRepository.addMeal(meal);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mealToEntityModel(meal.getId(), meal));
}

    @PutMapping("/rest/meals/{id}")
    ResponseEntity<EntityModel<Meal>> updateMeal(@PathVariable String id, @RequestBody Meal updatedMeal) {
        Meal existingMeal = mealsRepository.findMeal(id)
                .orElseThrow(() -> new SuitNotFoundException(id));
        updatedMeal.setId(id);
        boolean updated = mealsRepository.updateMeal(updatedMeal);
        if (updated) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(mealToEntityModel(id, updatedMeal));
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    @DeleteMapping("/rest/meals/{id}")
    ResponseEntity<EntityModel<Meal>> deleteMeal(@PathVariable String id) {
        Meal meal = mealsRepository.findMeal(id)
                .orElseThrow(() -> new SuitNotFoundException(id));
        mealsRepository.deleteMeal(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(mealToEntityModel(id, meal));
    }


    @PostMapping("/rest/orders")
    public ResponseEntity<?> addOrder(@RequestBody Order order) {
        // Validate the order
//        if (order.getAddress() == null || order.getMealIds() == null || order.getMealIds().isEmpty()) {
//            return ResponseEntity.badRequest().body("Please provide a valid address and at least one meal identifier.");
//        }

        // Process the order and generate an order confirmation
        OrderConfirmation confirmation = processOrder(order);

        return ResponseEntity.status(HttpStatus.CREATED).body(confirmation);
    }

    private OrderConfirmation processOrder(Order order) {

        OrderConfirmation confirmation = mealsRepository.processOrder(order);
        return confirmation;
    }


    private void setId(Meal meal) {
        String hashedName = Integer.toHexString(meal.getName().hashCode());
        String fixedLengthId = String.format("%032d", new BigInteger(1, hashedName.getBytes()));
        meal.setId(UUID.nameUUIDFromBytes(fixedLengthId.getBytes()).toString());
    }


    private EntityModel<Meal> mealToEntityModel(String id, Meal meal) {
        return EntityModel.of(meal,
                linkTo(methodOn(MealsRestController.class).getMealById(id)).withSelfRel(),
                linkTo(methodOn(MealsRestController.class).getMeals()).withRel("rest/meals"));
    }

}
