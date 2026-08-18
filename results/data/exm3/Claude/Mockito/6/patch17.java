public static <K, V>  Map<K, V> anyMapOf(Class<K> keyClazz, Class<V> valueClazz) {
    reportMatcher(new NotNull());
    return reportMatcher(Any.ANY).returnMap();
}