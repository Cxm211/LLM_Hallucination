public static boolean anyBoolean() {
        return reportMatcher(Any.ANY_BOOLEAN).returnFalse();
    }

    public static byte anyByte() {
        return reportMatcher(Any.ANY_BYTE).returnZero();
    }

    public static char anyChar() {
        return reportMatcher(Any.ANY_CHAR).returnChar();
    }

    public static int anyInt() {
        return reportMatcher(Any.ANY_INTEGER).returnZero();
    }

    public static long anyLong() {
        return reportMatcher(Any.ANY_LONG).returnZero();
    }

    public static float anyFloat() {
        return reportMatcher(Any.ANY_FLOAT).returnZero();
    }

    public static double anyDouble() {
        return reportMatcher(Any.ANY_DOUBLE).returnZero();
    }

    public static short anyShort() {
        return reportMatcher(Any.ANY_SHORT).returnZero();
    }

    public static <T> T anyObject() {
        return (T) reportMatcher(Any.ANY).returnNull();
    }

    public static <T> T any(Class<T> clazz) {
        return (T) reportMatcher(Any.ANY_OF_CLASS).returnFor(clazz);
    }

    public static <T> T any() {
        return (T) anyObject();
    }

    public static String anyString() {
        return reportMatcher(Any.ANY_STRING).returnString();
    }

    public static List anyList() {
        return reportMatcher(Any.ANY_LIST).returnList();
    }    

    public static <T> List<T> anyListOf(Class<T> clazz) {
        return (List) reportMatcher(Any.ANY_LIST).returnList();
    }    

    public static Set anySet() {
        return reportMatcher(Any.ANY_SET).returnSet();
    }

    public static <T> Set<T> anySetOf(Class<T> clazz) {
        return (Set) reportMatcher(Any.ANY_SET).returnSet();
    }

    public static Map anyMap() {
        return reportMatcher(Any.ANY_MAP).returnMap();
    }

    public static <K, V>  Map<K, V> anyMapOf(Class<K> keyClazz, Class<V> valueClazz) {
        return (Map<K, V>) reportMatcher(Any.ANY_MAP).returnMap();
    }

    public static Collection anyCollection() {
        return reportMatcher(Any.ANY_COLLECTION).returnList();
    }    

    public static <T> Collection<T> anyCollectionOf(Class<T> clazz) {
        return (Collection) reportMatcher(Any.ANY_COLLECTION).returnList();
    }