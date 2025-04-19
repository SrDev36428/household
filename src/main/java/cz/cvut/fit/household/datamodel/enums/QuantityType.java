package cz.cvut.fit.household.datamodel.enums;

public enum QuantityType {

    KILOGRAM("kg"),
    LITRE("l"),
    PIECES("p");

    private final String type;

    QuantityType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
