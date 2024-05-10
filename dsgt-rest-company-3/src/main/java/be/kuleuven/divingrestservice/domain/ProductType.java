package be.kuleuven.divingrestservice.domain;

public enum ProductType {

    SCUBA("scuba diving suit"),
    SNORKEL_MASK("snorkeling mask"),
    FLIPPERS("flippers");
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
