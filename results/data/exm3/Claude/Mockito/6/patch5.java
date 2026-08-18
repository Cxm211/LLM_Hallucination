public static float anyFloat() {
    reportMatcher(new NotNull());
    return reportMatcher(Any.ANY).returnZero();
}