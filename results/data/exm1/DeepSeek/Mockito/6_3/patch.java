    public static boolean anyBoolean() {
        return any(Boolean.class);
    }

    public static byte anyByte() {
        return any(Byte.class);
    }

    public static char anyChar() {
        return any(Character.class);
    }

    public static int anyInt() {
        return any(Integer.class);
    }

    public static long anyLong() {
        return any(Long.class);
    }

    public static float anyFloat() {
        return any(Float.class);
    }

    public static double anyDouble() {
        return any(Double.class);
    }

    public static short anyShort() {
        return any(Short.class);
    }

    public static <T> T anyObject() {
        return (T) reportMatcher(Any.ANY).returnNull();
    }

    public static <T> T any(Class<T> clazz) {
        return (T) reportMatcher(Any.ANY).returnFor(clazz);
    }

    public static <T> T any() {
        return (T) anyObject();
    }

    public static String anyString() {
        return reportMatcher(Any.ANY).returnString();
    }

    public static List anyList() {
        return reportMatcher(Any.ANY).returnList();
    }

    public static <T> List<T> anyListOf(Class<T> clazz) {
        return (List) reportMatcher(Any.ANY).returnList();
    }

    public static Set anySet() {
        return reportMatcher(Any.ANY).returnSet();
    }

    public static <T> Set<T> anySetOf(Class<T> clazz) {
        return (Set) reportMatcher(Any.ANY).returnSet();
    }

    public static Map anyMap() {
        return reportMatcher(Any.ANY).returnMap();
    }

    public static <K, V>  Map<K, V> anyMapOf(Class<K> keyClazz, Class<V> valueClazz) {
        return reportMatcher(Any.ANY).returnMap();
    }

    public static Collection anyCollection() {
        return reportMatcher(Any.ANY).returnList();
    }

    public static <T> Collection<T> anyCollectionOf(Class<T> clazz) {
        return (Collection) reportMatcher(Any.ANY).returnList();
    }