package in.mrinmoy.example.authentication.model;

import java.util.HashMap;
import java.util.Map;

public enum TransactionType {
    CREDIT("CREDIT"), DEBIT("DEBIT");

    private final static Map<String, TransactionType> CONSTANTS = new HashMap<>();

    static {
        for (TransactionType c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    public static TransactionType fromValue(String value) {
        TransactionType constant = CONSTANTS.get(value.toUpperCase());
        if (constant == null) {
            throw new IllegalArgumentException(value);
        }
        return constant;
    }
}
