public static boolean anyBoolean() {
        return reportMatcher(Any.ANY).returnFalse();
    }

    public static byte anyByte() {
        return reportMatcher(Any.ANY).returnZero();
    }

    public static char anyChar() {
        return reportMatcher(Any.ANY).returnChar();
    }

    public static int anyInt() {
        return reportMatcher(Any.ANY).returnZero();
    }

    public static long anyLong() {
        return reportMatcher(Any.ANY).returnZero();
    }

    public static float anyFloat() {
        return reportMatcher(Any.ANY).returnZero();
    }

    public static double anyDouble() {
        return reportMatcher(Any.ANY).returnZero();
    }

    public static short anyShort() {
        return reportMatcher(Any.ANY).returnZero();
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
        return reportMatcher(NotNull.NOT_NULL).returnList();
    }    

    public static <T> List<T> anyListOf(Class<T> clazz) {
        return (List) reportMatcher(NotNull.NOT_NULL).returnList();
    }    

    public static Set anySet() {
        return reportMatcher(NotNull.NOT_NULL).returnSet();
    }

    public static <T> Set<T> anySetOf(Class<T> clazz) {
        return (Set) reportMatcher(NotNull.NOT_NULL).returnSet();
    }

    public static Map anyMap() {
        return reportMatcher(NotNull.NOT_NULL).returnMap();
    }

    public static <K, V>  Map<K, V> anyMapOf(Class<K> keyClazz, Class<V> valueClazz) {
        return reportMatcher(NotNull.NOT_NULL).returnMap();
    }

    public static Collection anyCollection() {
        return reportMatcher(NotNull.NOT_NULL).returnList();
    }    

    public static <T> Collection<T> anyCollectionOf(Class<T> clazz) {
        return (Collection) reportMatcher(NotNull.NOT_NULL).returnList();
    }