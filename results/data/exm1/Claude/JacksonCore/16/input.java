// buggy code
    protected JsonParserSequence(JsonParser[] parsers)
    {
        super(parsers[0]);
        _parsers = parsers;
        _nextParser = 1;
    }

    public JsonToken nextToken() throws IOException, JsonParseException
    {
        JsonToken t = delegate.nextToken();
        if (t != null) return t;
        while (switchToNext()) {
            t = delegate.nextToken();
            if (t != null) return t;
        }
        return null;
    }

// relevant test
// com.fasterxml.jackson.core.json.ParserSequenceTest::testSimple
    public void testSimple() throws Exception
    {
        JsonParser p1 = JSON_FACTORY.createParser("[ 1 ]");
        JsonParser p2 = JSON_FACTORY.createParser("[ 2 ]");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertEquals(2, seq.containedParsersCount());

        assertFalse(p1.isClosed());
        assertFalse(p2.isClosed());
        assertFalse(seq.isClosed());
        assertToken(JsonToken.START_ARRAY, seq.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(1, seq.getIntValue());
        assertToken(JsonToken.END_ARRAY, seq.nextToken());
        assertFalse(p1.isClosed());
        assertFalse(p2.isClosed());
        assertFalse(seq.isClosed());
        assertToken(JsonToken.START_ARRAY, seq.nextToken());

        
        assertTrue(p1.isClosed());
        assertFalse(p2.isClosed());
        assertFalse(seq.isClosed());

        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertToken(JsonToken.END_ARRAY, seq.nextToken());
        assertTrue(p1.isClosed());
        assertFalse(p2.isClosed());
        assertFalse(seq.isClosed());

        assertNull(seq.nextToken());
        assertTrue(p1.isClosed());
        assertTrue(p2.isClosed());
        assertTrue(seq.isClosed());

        seq.close();
    }

// com.fasterxml.jackson.core.json.ParserSequenceTest::testInitialized
    public void testInitialized() throws Exception
    {
        JsonParser p1 = JSON_FACTORY.createParser("1 2");
        JsonParser p2 = JSON_FACTORY.createParser("3 false");
        
        assertToken(JsonToken.VALUE_NUMBER_INT, p1.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p1.nextToken());

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(3, seq.getIntValue());
        seq.close();
    }
