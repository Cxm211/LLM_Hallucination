public static Collection anyCollection() {
    reportMatcher(Any.ANY);
    return new ArrayList(0);
}