public static int anyInt() {
    reportMatcher(new NotNull());
    return reportMatcher(Any.ANY).returnZero();
}