public static <T> Set<T> anySetOf(Class<T> clazz) {
    reportMatcher(Any.ANY);
    return new HashSet<T>(0);
}