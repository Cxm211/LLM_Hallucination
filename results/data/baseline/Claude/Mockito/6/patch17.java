public static <K, V> Map<K, V> anyMapOf(Class<K> keyClazz, Class<V> valueClazz) {
    reportMatcher(Any.ANY);
    return new HashMap<K, V>(0);
}