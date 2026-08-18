public static List anyList() {
    reportMatcher(new NotNull());
    return reportMatcher(Any.ANY).returnList();
}