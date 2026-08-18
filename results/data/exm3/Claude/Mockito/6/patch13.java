public static <T> List<T> anyListOf(Class<T> clazz) {
    reportMatcher(new NotNull());
    return (List) reportMatcher(Any.ANY).returnList();
}