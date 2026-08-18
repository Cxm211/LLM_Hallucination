// com/fasterxml/jackson/core/json/ParserSequenceTest.java
public void testPartiallyConsumedParsers() throws Exception
{
    JsonParser p1 = JSON_FACTORY.createParser("1 2 3");
    JsonParser p2 = JSON_FACTORY.createParser("4 5");
    assertToken(JsonToken.VALUE_NUMBER_INT, p1.nextToken());
    assertToken(JsonToken.VALUE_NUMBER_INT, p2.nextToken());
    JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
    assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
    assertEquals(1, seq.getIntValue());
    assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
    assertEquals(2, seq.getIntValue());
    assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
    assertEquals(3, seq.getIntValue());
    assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
    assertEquals(4, seq.getIntValue());
    assertToken(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
    assertEquals(5, seq.getIntValue());
    assertNull(seq.nextToken());
    seq.close();
}