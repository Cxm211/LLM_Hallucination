// com/fasterxml/jackson/core/json/ParserSequenceTest.java
public void testMultipleTokensInFirstParser() throws Exception {
        JsonParser p1 = JSON_FACTORY.createParser("1 2 3");
        JsonParser p2 = JSON_FACTORY.createParser("4");
        // read the first token from p1, so p1's current token is 1
        assertToken(JsonToken.VALUE_NUMBER_INT, p1.nextToken());
        assertEquals(1, p1.getIntValue());
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        // the sequence should return the current token of p1 (1) without advancing
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(1, seq.getIntValue());
        // then the next token from p1 (2)
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        // then 3 from p1
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(3, seq.getIntValue());
        // then switch to p2 and return 4
        assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(4, seq.getIntValue());
        seq.close();
    }
