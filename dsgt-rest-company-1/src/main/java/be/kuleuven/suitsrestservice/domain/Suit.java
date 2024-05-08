package be.kuleuven.suitsrestservice.domain;

import java.util.Objects;

public class Suit {

    protected String id;
    protected String name;
    protected Double price;
    protected String description;
    protected String imageLink;
    protected Integer amountAvailable;
    protected SuitType suitType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public Integer getAmountAvailable() {
        return amountAvailable;
    }

    public void setAmountAvailable(Integer amountAvailable) {
        this.amountAvailable = amountAvailable;
    }

    public SuitType getSuitType() {
        return suitType;
    }

    public void setSuitType(SuitType suitType) {
        this.suitType = suitType;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Suit suit = (Suit) object;
        return Objects.equals(id, suit.id) &&
                Objects.equals(price, suit.price) &&
                Objects.equals(description, suit.description) &&
                Objects.equals(imageLink, suit.imageLink) &&
                Objects.equals(amountAvailable, suit.amountAvailable) &&
                Objects.equals(suitType, suit.suitType) &&
                suitType == suit.suitType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, price, description, imageLink, amountAvailable, suitType);
    }

}


//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Meal meal = (Meal) o;
//        return Objects.equals(id, meal.id) &&
//                Objects.equals(name, meal.name) &&
//                Objects.equals(kcal, meal.kcal) &&
//                Objects.equals(price, meal.price) &&
//                Objects.equals(description, meal.description) &&
//                mealType == meal.mealType;
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(id, name, kcal, price, description, mealType);
//    }
//}

