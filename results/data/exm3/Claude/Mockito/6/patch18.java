public static Collection anyCollection() {
    reportMatcher(new NotNull());
    return reportMatcher(Any.ANY).returnList();
}