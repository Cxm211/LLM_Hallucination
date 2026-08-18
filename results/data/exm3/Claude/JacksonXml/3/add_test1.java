// com/fasterxml/jackson/dataformat/xml/stream/XmlParserNextXxxTest.java
public void testXmlLeafElementWithNextTextValue() throws Exception
{
    final String XML = "<data><name>value</name></data>";

    FromXmlParser xp = (FromXmlParser) _xmlFactory.createParser(new StringReader(XML));

    assertToken(JsonToken.START_OBJECT, xp.nextToken()); // <data>
    assertToken(JsonToken.FIELD_NAME, xp.nextToken()); // <name>
    assertEquals("name", xp.getCurrentName());

    assertEquals("value", xp.nextTextValue());

    assertToken(JsonToken.END_OBJECT, xp.nextToken()); // </data>
    xp.close();
}