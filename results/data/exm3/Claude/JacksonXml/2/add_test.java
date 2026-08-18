// com/fasterxml/jackson/dataformat/xml/misc/XmlTextTest.java
public void testMixedContentMultipleTextNodes() throws Exception
{
    WindSpeed result = MAPPER.readValue("<windSpeed units='kt'> 27 <radius>20</radius> 35 </windSpeed>",
            WindSpeed.class);
    assertEquals(27, result.value);
    assertNotNull(result.radius);
    assertEquals(20, result.radius.value);
}