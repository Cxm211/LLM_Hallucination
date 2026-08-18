public static <T> T anyObject() {
    return (T) reportMatcher(new NotNull()).returnNull();
}