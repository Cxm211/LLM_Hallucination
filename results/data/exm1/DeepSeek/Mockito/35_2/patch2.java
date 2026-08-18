public static <T> T same(T value) {
    reportMatcher(new Same(value));
    return null;
}