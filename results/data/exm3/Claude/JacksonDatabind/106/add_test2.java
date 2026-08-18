// com/fasterxml/jackson/databind/node/TestTreeTraversingParser.java
public void testNumberValidIntFromLong() throws IOException
{
    final long validInt = Integer.MAX_VALUE;
    try (final JsonParser p = MAPPER.readTree("[ "+validInt+" ]").traverse()) {
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(NumberType.LONG, p.getNumberType());
        assertEquals((int)validInt, p.getIntValue());
    }
}