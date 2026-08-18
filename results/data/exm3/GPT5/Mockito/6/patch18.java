public static Collection anyCollection() {
        return reportMatcher(Any.ANY_NON_NULL).returnList();
    }