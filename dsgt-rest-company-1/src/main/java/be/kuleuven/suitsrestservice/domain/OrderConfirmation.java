package be.kuleuven.suitsrestservice.domain;

import java.util.List;

public class OrderConfirmation {

    private String address;
    private List<Meal> orderedMeals;
    private double totalPrice;

    public OrderConfirmation(String address, List<Meal> orderedMeals, double totalPrice) {
        this.address = address;
        this.orderedMeals = orderedMeals;
        this.totalPrice = totalPrice;
    }
    //getter and setter methods
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public List<Meal> getOrderedMeals() {
        return orderedMeals;
    }
    public void setOrderedMeals(List<Meal> orderedMeals) {
        this.orderedMeals = orderedMeals;
    }
    public double getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

}
