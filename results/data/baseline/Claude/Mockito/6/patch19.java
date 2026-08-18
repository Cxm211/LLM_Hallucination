public static <T> Collection<T> anyCollectionOf(Class<T> clazz) {
    reportMatcher(Any.ANY);
    return new ArrayList<T>(0);
}