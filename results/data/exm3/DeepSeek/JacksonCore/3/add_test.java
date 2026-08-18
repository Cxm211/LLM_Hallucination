// com/fasterxml/jackson/core/json/TestLocation.java
public void testOffsetWithInputOffsetAndNewline() throws Exception
    {
        final JsonFactory f = new JsonFactory();
        JsonLocation loc;
        JsonParser p;
        byte[] b = "  \\n  { }".getBytes("UTF-8");
        p = f.createParser(b, 2, b.length - 2);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        loc = p.getTokenLocation();
        assertEquals(3L, loc.getByteOffset());
        assertEquals(-1L, loc.getCharOffset());
        assertEquals(2, loc.getLineNr());
        assertEquals(3, loc.getColumnNr());
        p.close();
    }
