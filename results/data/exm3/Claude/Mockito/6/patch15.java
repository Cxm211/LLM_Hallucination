public static <T> Set<T> anySetOf(Class<T> clazz) {
    reportMatcher(new NotNull());
    return (Set) reportMatcher(Any.ANY).returnSet();
}