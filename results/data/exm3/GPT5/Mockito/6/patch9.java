public static <T> T any(Class<T> clazz) {
        return (T) reportMatcher(Any.ANY_NON_NULL).returnFor(clazz);
    }