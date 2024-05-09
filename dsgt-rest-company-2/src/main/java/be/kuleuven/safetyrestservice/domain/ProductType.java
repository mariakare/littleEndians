package be.kuleuven.safetyrestservice.domain;

public enum ProductType {

    HAZMAT("hazmat"),
    GAS_MASK("gas mask"),
    GLOVES("gloves");
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
