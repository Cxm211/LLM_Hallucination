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

// trigger testcase
@Test
    public void shouldNotAcceptNullInAllAnyPrimitiveWrapperMatchers() {
        when(mock.forInteger(anyInt())).thenReturn("0");
        when(mock.forCharacter(anyChar())).thenReturn("1");
        when(mock.forShort(anyShort())).thenReturn("2");
        when(mock.forByte(anyByte())).thenReturn("3");
        when(mock.forBoolean(anyBoolean())).thenReturn("4");
        when(mock.forLong(anyLong())).thenReturn("5");
        when(mock.forFloat(anyFloat())).thenReturn("6");
        when(mock.forDouble(anyDouble())).thenReturn("7");
        
        assertEquals(null, mock.forInteger(null));
        assertEquals(null, mock.forCharacter(null));
        assertEquals(null, mock.forShort(null));
        assertEquals(null, mock.forByte(null));
        assertEquals(null, mock.forBoolean(null));
        assertEquals(null, mock.forLong(null));
        assertEquals(null, mock.forFloat(null));
        assertEquals(null, mock.forDouble(null));
    }

@Test
    public void shouldNotAcceptNullInAnyXMatchers() {
        when(mock.oneArg(anyObject())).thenReturn("0");
        when(mock.oneArg(anyString())).thenReturn("1");
        when(mock.forList(anyList())).thenReturn("2");
        when(mock.forMap(anyMap())).thenReturn("3");
        when(mock.forCollection(anyCollection())).thenReturn("4");
        when(mock.forSet(anySet())).thenReturn("5");
        
        assertEquals(null, mock.oneArg((Object) null));
        assertEquals(null, mock.oneArg((String) null));
        assertEquals(null, mock.forList(null));
        assertEquals(null, mock.forMap(null));
        assertEquals(null, mock.forCollection(null));
        assertEquals(null, mock.forSet(null));
    }

@Test
    public void anyStringMatcher() {
        when(mock.oneArg(anyString())).thenReturn("matched");
        
        assertEquals("matched", mock.oneArg(""));
        assertEquals("matched", mock.oneArg("any string"));
        assertEquals(null, mock.oneArg((String) null));
    }

@Test
    public void shouldAllowAnyCollection() {
        when(mock.forCollection(anyCollection())).thenReturn("matched");
        
        assertEquals("matched", mock.forCollection(Arrays.asList("x", "y")));
        assertEquals(null, mock.forCollection(null));

        verify(mock, times(1)).forCollection(anyCollection());
    }

@Test
    public void shouldAllowAnyList() {
        when(mock.forList(anyList())).thenReturn("matched");
        
        assertEquals("matched", mock.forList(Arrays.asList("x", "y")));
        assertEquals(null, mock.forList(null));

        verify(mock, times(1)).forList(anyList());
    }

@Test
    public void shouldAllowAnyMap() {
        when(mock.forMap(anyMap())).thenReturn("matched");
        
        assertEquals("matched", mock.forMap(new HashMap<String, String>()));
        assertEquals(null, mock.forMap(null));

        verify(mock, times(1)).forMap(anyMap());
    }

@Test
    public void shouldAllowAnySet() {
        when(mock.forSet(anySet())).thenReturn("matched");
        
        assertEquals("matched", mock.forSet(new HashSet<String>()));
        assertEquals(null, mock.forSet(null));

        verify(mock, times(1)).forSet(anySet());
    }
