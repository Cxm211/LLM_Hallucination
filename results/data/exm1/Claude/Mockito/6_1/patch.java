public static boolean anyBoolean() {
    reportMatcher(Any.ANY).returnFalse();
    return false;
}