package name.expenses.utils;

import java.lang.reflect.Field;

public final class FieldValidator {
    private FieldValidator() {}

    public static boolean hasField(String fieldName, Class<?> clazz) {
        if (fieldName == null) return false;
        Class<?> current = clazz;
        while (current != null) {
            try {
                current.getDeclaredField(fieldName);
                return true;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return false;
    }
}
