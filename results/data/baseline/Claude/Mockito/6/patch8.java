public static <T> T anyObject() {
    reportMatcher(Any.ANY);
    return null;
}