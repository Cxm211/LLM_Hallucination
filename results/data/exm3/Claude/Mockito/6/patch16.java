public static Map anyMap() {
    reportMatcher(new NotNull());
    return reportMatcher(Any.ANY).returnMap();
}