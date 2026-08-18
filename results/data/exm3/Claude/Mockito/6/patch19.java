public static <T> Collection<T> anyCollectionOf(Class<T> clazz) {
    reportMatcher(new NotNull());
    return (Collection) reportMatcher(Any.ANY).returnList();
}