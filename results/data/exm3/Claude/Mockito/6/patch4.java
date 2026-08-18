public static long anyLong() {
    reportMatcher(new NotNull());
    return reportMatcher(Any.ANY).returnZero();
}