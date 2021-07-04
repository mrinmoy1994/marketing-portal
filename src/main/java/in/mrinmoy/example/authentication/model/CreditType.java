package in.mrinmoy.example.authentication.model;

import java.util.HashMap;
import java.util.Map;

public enum CreditType {
    BONUS("BONUS"), COMMISSION("BONUS");

    private final static Map<String, CreditType> CONSTANTS = new HashMap<>();

    static {
        for (CreditType c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private final String value;

    CreditType(String value) {
        this.value = value;
    }

    public static CreditType fromValue(String value) {
        CreditType constant = CONSTANTS.get(value.toUpperCase());
        if (constant == null) {
            throw new IllegalArgumentException(value);
        }
        return constant;
    }
}
