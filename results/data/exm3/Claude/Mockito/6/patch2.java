public static char anyChar() {
    reportMatcher(new NotNull());
    return reportMatcher(Any.ANY).returnChar();
}