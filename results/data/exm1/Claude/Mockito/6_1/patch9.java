public static <T> T any(Class<T> clazz) {
    reportMatcher(Any.ANY).returnFor(clazz);
    return null;
}