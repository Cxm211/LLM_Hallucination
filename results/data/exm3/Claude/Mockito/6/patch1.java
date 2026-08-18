public static byte anyByte() {
    reportMatcher(new NotNull());
    return reportMatcher(Any.ANY).returnZero();
}