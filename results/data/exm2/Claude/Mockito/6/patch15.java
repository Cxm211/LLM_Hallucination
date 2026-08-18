public static <T> Set<T> anySetOf(Class<T> clazz) {
    return (Set) reportMatcher(new NotNull()).returnSet();
}