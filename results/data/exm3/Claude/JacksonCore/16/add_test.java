// com/fasterxml/jackson/core/json/ParserSequenceTest.java
public void testUninitializedParsers() throws Exception
{
    JsonParser p1 = JSON_FACTORY.createParser("1 2");
    JsonParser p2 = JSON_FACTORY.createParser("3 false");
    JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
    assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
    assertEquals(1, seq.getIntValue());
    assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
    assertEquals(2, seq.getIntValue());
    assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
    assertEquals(3, seq.getIntValue());
    assertToken(JsonToken.VALUE_FALSE, seq.nextToken());
    seq.close();
}