// com/fasterxml/jackson/dataformat/xml/stream/XmlParserNextXxxTest.java
public void testXmlEmptyElementWithNextTextValue() throws Exception
{
    final String XML = "<data><name/></data>";

    FromXmlParser xp = (FromXmlParser) _xmlFactory.createParser(new StringReader(XML));

    assertToken(JsonToken.START_OBJECT, xp.nextToken()); // <data>
    assertToken(JsonToken.FIELD_NAME, xp.nextToken()); // <name>
    assertEquals("name", xp.getCurrentName());

    assertEquals("", xp.nextTextValue());

    assertToken(JsonToken.END_OBJECT, xp.nextToken()); // </data>
    xp.close();
}