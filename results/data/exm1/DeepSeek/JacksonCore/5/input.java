// buggy code
    private final static int _parseIndex(String str) {
        final int len = str.length();
        // [Issue#133]: beware of super long indexes; assume we never
        // have arrays over 2 billion entries so ints are fine.
        if (len == 0 || len > 10) {
            return -1;
        }
        for (int i = 0; i < len; ++i) {
            char c = str.charAt(i++);
            if (c > '9' || c < '0') {
                return -1;
            }
        }
        if (len == 10) {
            long l = NumberInput.parseLong(str);
            if (l > Integer.MAX_VALUE) {
                return -1;
            }
        }
        return NumberInput.parseInt(str);
    }

// relevant test
// com.fasterxml.jackson.core.TestJsonPointer::testSimplePath
    public void testSimplePath() throws Exception
    {
        final String INPUT = "/Image/15/name";

        JsonPointer ptr = JsonPointer.compile(INPUT);
        assertFalse(ptr.matches());
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("Image", ptr.getMatchingProperty());
        assertEquals(INPUT, ptr.toString());

        ptr = ptr.tail();
        assertNotNull(ptr);
        assertFalse(ptr.matches());
        assertEquals(15, ptr.getMatchingIndex());
        assertEquals("15", ptr.getMatchingProperty());
        assertEquals("/15/name", ptr.toString());

        ptr = ptr.tail();
        assertNotNull(ptr);
        assertFalse(ptr.matches());
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("name", ptr.getMatchingProperty());
        assertEquals("/name", ptr.toString());

        
        ptr = ptr.tail();
        assertTrue(ptr.matches());
        assertNull(ptr.tail());
        assertEquals("", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
    }

// com.fasterxml.jackson.core.TestJsonPointer::testWonkyNumber173
    public void testWonkyNumber173() throws Exception
    {
        JsonPointer ptr = JsonPointer.compile("/1e0");
        assertFalse(ptr.matches());
    }

// com.fasterxml.jackson.core.TestJsonPointer::testQuotedPath
    public void testQuotedPath() throws Exception
    {
        final String INPUT = "/w~1out/til~0de/a~1b";

        JsonPointer ptr = JsonPointer.compile(INPUT);
        assertFalse(ptr.matches());
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("w/out", ptr.getMatchingProperty());
        assertEquals(INPUT, ptr.toString());

        ptr = ptr.tail();
        assertNotNull(ptr);
        assertFalse(ptr.matches());
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("til~de", ptr.getMatchingProperty());
        assertEquals("/til~0de/a~1b", ptr.toString());

        ptr = ptr.tail();
        assertNotNull(ptr);
        assertFalse(ptr.matches());
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals("a/b", ptr.getMatchingProperty());
        assertEquals("/a~1b", ptr.toString());

        
        ptr = ptr.tail();
        assertTrue(ptr.matches());
        assertNull(ptr.tail());
    }

// com.fasterxml.jackson.core.TestJsonPointer::testLongNumbers
    public void testLongNumbers() throws Exception
    {
        final long LONG_ID = ((long) Integer.MAX_VALUE) + 1L;
        
        final String INPUT = "/User/"+LONG_ID;

        JsonPointer ptr = JsonPointer.compile(INPUT);
        assertEquals("User", ptr.getMatchingProperty());
        assertEquals(INPUT, ptr.toString());

        ptr = ptr.tail();
        assertNotNull(ptr);
        assertFalse(ptr.matches());
        
        assertEquals(-1, ptr.getMatchingIndex());
        assertEquals(String.valueOf(LONG_ID), ptr.getMatchingProperty());

        
        ptr = ptr.tail();
        assertTrue(ptr.matches());
        assertNull(ptr.tail());
    }
