public static Set anySet() {
    reportMatcher(new NotNull());
    return reportMatcher(Any.ANY).returnSet();
}