package name.expenses.globals;

public enum SortDirection {
    ASC, DESC;

    public static SortDirection fromString(String direction) {
        if (direction == null || direction.isBlank()) {
            return ASC;
        }
        try {
            return valueOf(direction.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ASC;
        }
    }
}
