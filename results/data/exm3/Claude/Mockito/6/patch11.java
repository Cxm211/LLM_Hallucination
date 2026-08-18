public static String anyString() {
    reportMatcher(new NotNull());
    return reportMatcher(Any.ANY).returnString();
}