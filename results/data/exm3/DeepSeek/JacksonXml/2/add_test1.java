// com/fasterxml/jackson/dataformat/xml/misc/XmlTextTest.java
public void testMixedContentMultipleAttributes() throws Exception {
    WindSpeed result = MAPPER.readValue("<windSpeed units='kt' extra='bar'>27<radius>20</radius></windSpeed>", WindSpeed.class);
    assertEquals(27, result.value);
    assertNotNull(result.radius);
    assertEquals(20, result.radius.value);
}
