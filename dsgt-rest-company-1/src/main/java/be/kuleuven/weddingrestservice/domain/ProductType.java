package be.kuleuven.weddingrestservice.domain;

public enum ProductType {

    TUXEDO("tuxedo"),
    GOWN("gown"),
    DECORATION("decoration");
    private final String value;

    ProductType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ProductType fromValue(String v) {
        for (ProductType c: ProductType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
