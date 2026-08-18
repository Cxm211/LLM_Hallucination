public static <T> T eq(T value) {
    return reportMatcher(new Equals(value)).<T>returnFor(value == null ? null : value.getClass());
}