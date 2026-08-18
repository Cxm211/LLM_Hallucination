public static <T> T isA(Class<T> clazz) {
    reportMatcher(new InstanceOf(clazz));
    if (clazz.isPrimitive()) {
        return (T) Primitives.defaultValue(clazz);
    }
    return null;
}