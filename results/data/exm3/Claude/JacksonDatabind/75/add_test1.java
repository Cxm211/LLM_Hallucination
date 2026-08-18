// com/fasterxml/jackson/databind/struct/EnumFormatShapeTest.java
public void testEnumClassLevelAnnotation() throws Exception {
        Color color = Color.BLUE;
        String json = MAPPER.writeValueAsString(color);
        assertNotNull(json);
    }