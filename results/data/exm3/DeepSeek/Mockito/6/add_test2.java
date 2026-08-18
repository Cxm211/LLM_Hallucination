// org/mockitousage/matchers/NewMatchersTest.java
@Test
    public void shouldNotAcceptNullInAnyXOfMatchers() {
        when(mock.forList(anyListOf(String.class))).thenReturn("list");
        when(mock.forSet(anySetOf(Integer.class))).thenReturn("set");
        when(mock.forMap(anyMapOf(String.class, Object.class))).thenReturn("map");
        when(mock.forCollection(anyCollectionOf(Number.class))).thenReturn("collection");
        
        assertEquals("list", mock.forList(Arrays.asList("a", "b")));
        assertEquals("set", mock.forSet(new HashSet<>(Arrays.asList(1, 2))));
        assertEquals("map", mock.forMap(new HashMap<String, String>()));
        assertEquals("collection", mock.forCollection(Arrays.asList(1, 2)));
        
        assertEquals(null, mock.forList(null));
        assertEquals(null, mock.forSet(null));
        assertEquals(null, mock.forMap(null));
        assertEquals(null, mock.forCollection(null));
    }
