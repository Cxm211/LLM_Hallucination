public static String anyString() {
    return reportMatcher(new NotNull()).returnString();
}