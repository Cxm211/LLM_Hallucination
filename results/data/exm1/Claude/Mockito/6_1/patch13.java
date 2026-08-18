public static <T> List<T> anyListOf(Class<T> clazz) {
    reportMatcher(Any.ANY).returnList();
    return null;
}