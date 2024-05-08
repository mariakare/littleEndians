package be.kuleuven.suitsrestservice.domain;

public enum SuitType {

    TUXEDO("tuxedo"),
    HAZMAT("hazmat"),
    DIVING("diving");
    private final String value;

    SuitType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SuitType fromValue(String v) {
        for (SuitType c: SuitType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
