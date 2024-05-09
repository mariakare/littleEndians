package be.kuleuven.safetyrestservice.domain;

import java.util.Objects;

public class Product {

    protected String id;
    protected String name;
    protected Double price;
    protected String description;
    protected String imageLink;
    protected Integer amountAvailable;
    protected ProductType productType;

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

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Product product = (Product) object;
        return Objects.equals(id, product.id) &&
                Objects.equals(price, product.price) &&
                Objects.equals(description, product.description) &&
                Objects.equals(imageLink, product.imageLink) &&
                Objects.equals(amountAvailable, product.amountAvailable) &&
                Objects.equals(productType, product.productType) &&
                productType == product.productType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, price, description, imageLink, amountAvailable, productType);
    }

}

