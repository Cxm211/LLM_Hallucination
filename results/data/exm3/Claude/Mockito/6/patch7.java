public static short anyShort() {
    reportMatcher(new NotNull());
    return reportMatcher(Any.ANY).returnZero();
}