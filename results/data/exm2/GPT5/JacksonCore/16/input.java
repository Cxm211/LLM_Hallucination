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

// trigger testcase
public void testInitialized() throws Exception
    {
        JsonParser p1 = JSON_FACTORY.createParser("1 2");
        JsonParser p2 = JSON_FACTORY.createParser("3 false");
        // consume '1', move to '2'
        assertToken(JsonToken.VALUE_NUMBER_INT, p1.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p1.nextToken());

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(3, seq.getIntValue());
        seq.close();
    }
