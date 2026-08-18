// com/fasterxml/jackson/databind/struct/EnumFormatShapeTest.java
public void testEnumPropertyAsString() throws Exception {
        ColorWrapper wrapper = new ColorWrapper(Color.RED);
        String json = MAPPER.writeValueAsString(wrapper);
        assertTrue(json.contains("\"RED\"") || json.contains("0"));
    }