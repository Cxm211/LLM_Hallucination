public static <T> T anyObject() {
    reportMatcher(new NotNull());
    return (T) reportMatcher(Any.ANY).returnNull();
}