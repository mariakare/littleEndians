package be.kuleuven.foodrestservice.domain;

import java.util.List;

public class Order {
    private String address;
    private List<String> mealIds;


    public Order(String address, List<String> mealIds) {
        this.address = address;
        this.mealIds = mealIds;
    }

    public String getAddress() {
        return address;
    }

    public List<String> getMealIds() {
        return mealIds;
    }
}
