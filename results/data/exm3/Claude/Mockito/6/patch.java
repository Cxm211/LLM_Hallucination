public static boolean anyBoolean() {
    reportMatcher(new NotNull());
    return reportMatcher(Any.ANY).returnFalse();
}