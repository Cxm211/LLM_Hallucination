public static Set anySet() {
    return reportMatcher(new NotNull()).returnSet();
}