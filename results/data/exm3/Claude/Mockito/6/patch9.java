public static <T> T any(Class<T> clazz) {
    reportMatcher(new NotNull());
    return (T) reportMatcher(Any.ANY).returnFor(clazz);
}