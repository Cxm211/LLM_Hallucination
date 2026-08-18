public static <T> T anyObject() {
    reportMatcher(Any.ANY).returnNull();
    return null;
}