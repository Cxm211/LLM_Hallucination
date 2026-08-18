public static Collection anyCollection() {
    return reportMatcher(new NotNull()).returnList();
}