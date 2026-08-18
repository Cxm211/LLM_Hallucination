public static <T> List<T> anyListOf(Class<T> clazz) {
    reportMatcher(Any.ANY);
    return new ArrayList<T>(0);
}