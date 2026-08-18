// com/fasterxml/jackson/core/json/ParserSequenceTest.java::testSecondHasCurrentToken
public void testSecondHasCurrentToken() throws Exception
    {
        JsonParser p1 = JSON_FACTORY.createParser("1");
        // consume '1' and move to end to exhaust first parser
        assertToken(JsonToken.VALUE_NUMBER_INT, p1.nextToken());
        assertNull(p1.nextToken());

        JsonParser p2 = JSON_FACTORY.createParser("3 false");
        // advance p2 so that it already has current token '3'
        assertToken(JsonToken.VALUE_NUMBER_INT, p2.nextToken());

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        // should first return current token of p2 (3), not advance to 'false'
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(3, seq.getIntValue());
        assertToken(JsonToken.VALUE_FALSE, seq.nextToken());
        assertNull(seq.nextToken());
        seq.close();
    }