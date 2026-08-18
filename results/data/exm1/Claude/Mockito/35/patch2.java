public static <T> T same(T value) {
    return reportMatcher(new Same(value)).<T>returnFor(value == null ? null : value.getClass());
}