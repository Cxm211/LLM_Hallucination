// com/fasterxml/jackson/dataformat/xml/misc/XmlTextTest.java
public void testMixedContentTextBeforeMultipleElements() throws Exception
{
    WindSpeed result = MAPPER.readValue("<windSpeed units='kt'> 27 <radius>20</radius><extra>30</extra></windSpeed>",
            WindSpeed.class);
    assertEquals(27, result.value);
    assertNotNull(result.radius);
    assertEquals(20, result.radius.value);
}