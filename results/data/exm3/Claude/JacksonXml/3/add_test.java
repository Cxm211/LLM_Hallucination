// com/fasterxml/jackson/dataformat/xml/stream/XmlParserNextXxxTest.java
public void testXmlAttributesWithNextTextValueEmpty() throws Exception
{
    final String XML = "<data max=\"\" offset=\"\"/>";

    FromXmlParser xp = (FromXmlParser) _xmlFactory.createParser(new StringReader(XML));

    assertToken(JsonToken.START_OBJECT, xp.nextToken()); // <data>
    assertToken(JsonToken.FIELD_NAME, xp.nextToken()); // <max>
    assertEquals("max", xp.getCurrentName());

    assertEquals("", xp.nextTextValue());

    assertToken(JsonToken.FIELD_NAME, xp.nextToken()); // <offset>
    assertEquals("offset", xp.getCurrentName());

    assertEquals("", xp.nextTextValue());

    assertToken(JsonToken.END_OBJECT, xp.nextToken()); // </data>
    xp.close();
}