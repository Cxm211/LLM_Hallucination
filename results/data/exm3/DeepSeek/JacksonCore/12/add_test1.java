// com/fasterxml/jackson/core/json/LocationInObjectTest.java
public void testUnquotedFieldName() throws Exception {
        final JsonFactory f = JsonFactory.builder()
                .enable(com.fasterxml.jackson.core.JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
                .build();
        char[] c = "{fieldName:true}".toCharArray();
        JsonParser p = f.createParser(c);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("fieldName", p.nextFieldName());
        assertEquals(1L, p.getTokenLocation().getCharOffset()); // 'f' at index 1
        assertEquals(JsonToken.VALUE_TRUE, p.nextValue());
        assertEquals(11L, p.getTokenLocation().getCharOffset()); // 't' at index 11
        p.close();
    }
