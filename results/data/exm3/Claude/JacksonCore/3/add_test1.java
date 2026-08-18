// com/fasterxml/jackson/core/json/TestLocation.java
public void testOffsetWithInputOffsetMultiline() throws Exception
    {
        final JsonFactory f = new JsonFactory();
        JsonLocation loc;
        JsonParser p;
        // 5 spaces before, 2 after, with newline
        byte[] b = "     {\n\"a\":1}  ".getBytes("UTF-8");

        // peel off 5 from start and 2 from end
        p = f.createParser(b, 5, b.length-7);
        assertToken(JsonToken.START_OBJECT, p.nextToken());

        loc = p.getTokenLocation();
        assertEquals(0L, loc.getByteOffset());
        assertEquals(1, loc.getLineNr());
        assertEquals(1, loc.getColumnNr());
        
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        loc = p.getTokenLocation();
        assertEquals(2L, loc.getByteOffset());
        assertEquals(2, loc.getLineNr());
        assertEquals(1, loc.getColumnNr());

        p.close();
    }