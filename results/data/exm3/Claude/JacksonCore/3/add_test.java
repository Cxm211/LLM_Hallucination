// com/fasterxml/jackson/core/json/TestLocation.java
public void testOffsetWithLargerInputOffset() throws Exception
    {
        final JsonFactory f = new JsonFactory();
        JsonLocation loc;
        JsonParser p;
        // 10 spaces before, 3 after
        byte[] b = "          [1,2]   ".getBytes("UTF-8");

        // peel off 10 from start and 3 from end
        p = f.createParser(b, 10, b.length-13);
        assertToken(JsonToken.START_ARRAY, p.nextToken());

        loc = p.getTokenLocation();
        assertEquals(0L, loc.getByteOffset());
        assertEquals(-1L, loc.getCharOffset());
        assertEquals(1, loc.getLineNr());
        assertEquals(1, loc.getColumnNr());
        
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        loc = p.getTokenLocation();
        assertEquals(1L, loc.getByteOffset());
        assertEquals(-1L, loc.getCharOffset());
        assertEquals(1, loc.getLineNr());
        assertEquals(2, loc.getColumnNr());

        p.close();
    }