package be.kuleuven.suitsrestservice.controllers;

import be.kuleuven.suitsrestservice.domain.Meal;
import be.kuleuven.suitsrestservice.domain.MealsRepository;
import be.kuleuven.suitsrestservice.exceptions.MealNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

@RestController
public class MealsRestRpcStyleController {

    private final MealsRepository mealsRepository;

    @Autowired
    MealsRestRpcStyleController(MealsRepository mealsRepository) {
        this.mealsRepository = mealsRepository;
    }

    @GetMapping("/restrpc/meals/{id}")
    Meal getMealById(@PathVariable String id) {
        Optional<Meal> meal = mealsRepository.findMeal(id);

        return meal.orElseThrow(() -> new MealNotFoundException(id));
    }

    @GetMapping("/restrpc/meals")
    Collection<Meal> getMeals() {
        return mealsRepository.getAllMeal();
    }

    @GetMapping("/restrpc/cheapest")
    Meal getCheapestMeal() {
        return mealsRepository.getCheapestMeal();
    }

    @PostMapping("/restrpc/addMeal")
    void addMeal(@RequestBody Meal meal) {
        mealsRepository.addMeal(meal);
    }

    @PutMapping("/restrpc/updateMeal")
    boolean updateMeal(@RequestBody Meal meal) {
        return mealsRepository.updateMeal(meal);
    }

    @DeleteMapping("/restrpc/deleteMeal/{id}")
    void deleteMeal(@PathVariable String id) {
        mealsRepository.deleteMeal(id);
    }
}
