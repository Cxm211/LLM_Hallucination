// com/fasterxml/jackson/databind/convert/TestConvertingSerializer.java::testMapLikeTypeDetection
public void testMapLikeTypeDetection() throws Exception {
    com.fasterxml.jackson.databind.ObjectMapper m = new com.fasterxml.jackson.databind.ObjectMapper();
    com.fasterxml.jackson.databind.JavaType t = m.getTypeFactory().constructType(java.util.Map.class);
    assertTrue(t.isMapLikeType());
}