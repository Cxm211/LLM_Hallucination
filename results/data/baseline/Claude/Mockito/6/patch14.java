public static Set anySet() {
    reportMatcher(Any.ANY);
    return new HashSet(0);
}