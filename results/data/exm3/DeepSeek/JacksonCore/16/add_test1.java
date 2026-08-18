// com/fasterxml/jackson/core/json/ParserSequenceTest.java
public void testThreeParsers() throws Exception {
        JsonParser p1 = JSON_FACTORY.createParser("true");
        JsonParser p2 = JSON_FACTORY.createParser("false");
        JsonParser p3 = JSON_FACTORY.createParser("null");
        // read the token from p1, so p1's current token is true
        assertToken(JsonToken.VALUE_TRUE, p1.nextToken());
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2, p3);
        // should return the current token of p1 (true)
        assertToken(JsonToken.VALUE_TRUE, seq.nextToken());
        // then switch to p2 and return false
        assertToken(JsonToken.VALUE_FALSE, seq.nextToken());
        // then switch to p3 and return null
        assertToken(JsonToken.VALUE_NULL, seq.nextToken());
        seq.close();
    }
