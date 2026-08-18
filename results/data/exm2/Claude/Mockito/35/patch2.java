public static <T> T same(T value) {
    reportMatcher(new Same(value));
    if (value != null && value.getClass().isPrimitive()) {
        return (T) Primitives.defaultValue(value.getClass());
    }
    return null;
}