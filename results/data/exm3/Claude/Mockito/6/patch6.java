public static double anyDouble() {
    reportMatcher(new NotNull());
    return reportMatcher(Any.ANY).returnZero();
}