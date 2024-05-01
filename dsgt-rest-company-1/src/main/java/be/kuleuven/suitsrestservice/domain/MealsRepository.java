package be.kuleuven.suitsrestservice.domain;

import be.kuleuven.suitsrestservice.exceptions.ReservationException;
import be.kuleuven.suitsrestservice.exceptions.SuitNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class MealsRepository {
    // map: id -> meal
    private static final Map<String, Meal> meals = new HashMap<>();

    @PostConstruct
    public void initData() {

        Meal a = new Meal();
        a.setId("5268203c-de76-4921-a3e3-439db69c462a");
        a.setName("Steak");
        a.setDescription("Steak with fries");
        a.setMealType(MealType.MEAT);
        a.setKcal(1100);
        a.setPrice((10.00));

        meals.put(a.getId(), a);

        Meal b = new Meal();
        b.setId("4237681a-441f-47fc-a747-8e0169bacea1");
        b.setName("Portobello");
        b.setDescription("Portobello Mushroom Burger");
        b.setMealType(MealType.VEGAN);
        b.setKcal(637);
        b.setPrice((7.00));

        meals.put(b.getId(), b);

        Meal c = new Meal();
        c.setId("cfd1601f-29a0-485d-8d21-7607ec0340c8");
        c.setName("Fish and Chips");
        c.setDescription("Fried fish with chips");
        c.setMealType(MealType.FISH);
        c.setKcal(950);
        c.setPrice(5.00);

        meals.put(c.getId(), c);
    }

    public Optional<Meal> findMeal(String id) {
        Assert.notNull(id, "The meal id must not be null");
        Meal meal = meals.get(id);
        return Optional.ofNullable(meal);
    }

    public Meal getCheapestMeal() {
        return meals.values().stream().min(Comparator.comparing(Meal::getPrice)).orElse(null);
    }

    public void addMeal(Meal meal) {
        meals.put(meal.getId(), meal);
    }

    public void deleteMeal(String id) {
        meals.remove(id);
    }

    public boolean updateMeal(Meal meal) {
        if(meals.containsKey(meal.getId())) {
            meals.put(meal.getId(), meal);
            return true;
        }
        return false;
    }

    public Meal getLargestMeal() {
        return meals.values().stream().max(Comparator.comparing(Meal::getKcal)).orElse(null);
    }


    public Collection<Meal> getAllMeal() {
        return meals.values();
    }

    public OrderConfirmation processOrder(Order order) {

        verifyOrderLegality(order);

        List<Meal> orderedMeals = new ArrayList<>();
        double totalPrice = 0.0;

        for (String mealId : order.getMealIds()) {
            Meal meal = findMeal(mealId)
                    .orElseThrow(() -> new SuitNotFoundException("Meal with ID " + mealId + " not found."));
            orderedMeals.add(meal);
            totalPrice += meal.getPrice();
        }

        return new OrderConfirmation(order.getAddress(), orderedMeals, totalPrice);
    }

    private void verifyOrderLegality(Order order) {
        if(order.getMealIds()==null && order.getAddress()==null){
            throw new ReservationException("Please provide both an address as well as mealIds.");
        }

        if (order.getMealIds() == null || order.getMealIds().isEmpty()) {
            throw new SuitNotFoundException();
        }
        if(order.getAddress()==null){
            throw new ReservationException();
        }
    }
}
