// com/fasterxml/jackson/dataformat/xml/stream/XmlParserNextXxxTest.java
public void testXmlAttributesWithNextTextValueInArray() throws Exception
{
    final String XML = "<array><item id=\"1\"/></array>";
    FromXmlParser xp = (FromXmlParser) _xmlFactory.createParser(new StringReader(XML));
    assertToken(JsonToken.START_ARRAY, xp.nextToken());
    assertToken(JsonToken.START_OBJECT, xp.nextToken());
    assertToken(JsonToken.FIELD_NAME, xp.nextToken());
    assertEquals("id", xp.getCurrentName());
    assertEquals("1", xp.nextTextValue());
    assertToken(JsonToken.END_OBJECT, xp.nextToken());
    assertToken(JsonToken.END_ARRAY, xp.nextToken());
    xp.close();
}
