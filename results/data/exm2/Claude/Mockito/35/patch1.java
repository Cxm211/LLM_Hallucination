public static <T> T eq(T value) {
    reportMatcher(new Equals(value));
    if (value != null && value.getClass().isPrimitive()) {
        return (T) Primitives.defaultValue(value.getClass());
    }
    return null;
}