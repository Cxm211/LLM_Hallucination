// com/fasterxml/jackson/core/json/TestLocation.java::testOffsetWithInputOffsetEnd
public void testOffsetWithInputOffsetEnd() throws Exception
    {
        final JsonFactory f = new JsonFactory();
        JsonLocation loc;
        JsonParser p;
        byte[] b = "   { }  ".getBytes("UTF-8");

        p = f.createParser(b, 3, b.length-5); // parses "{ }"
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.END_OBJECT, p.nextToken());

        // Location at END_OBJECT token should be at byte offset 2 within provided slice
        loc = p.getTokenLocation();
        assertEquals(2L, loc.getByteOffset());
        assertEquals(-1L, loc.getCharOffset());
        assertEquals(1, loc.getLineNr());
        assertEquals(3, loc.getColumnNr());

        // Current location after END_OBJECT should be at byte offset 3 (end of input)
        loc = p.getCurrentLocation();
        assertEquals(3L, loc.getByteOffset());
        assertEquals(-1L, loc.getCharOffset());
        assertEquals(1, loc.getLineNr());
        assertEquals(4, loc.getColumnNr());

        p.close();
    }