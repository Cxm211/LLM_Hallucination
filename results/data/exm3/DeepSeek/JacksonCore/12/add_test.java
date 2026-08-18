// com/fasterxml/jackson/core/json/LocationInObjectTest.java
public void testFastPathFieldNameMatch() throws Exception {
        final JsonFactory f = new JsonFactory();
        char[] c = "{\"fieldName\":123}".toCharArray();
        JsonParser p = f.createParser(c);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        com.fasterxml.jackson.core.io.SerializedString sstr = new com.fasterxml.jackson.core.io.SerializedString("fieldName");
        assertTrue(p.nextFieldName(sstr));
        assertEquals(1L, p.getTokenLocation().getCharOffset()); // opening quote at index 1
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextValue());
        assertEquals(12L, p.getTokenLocation().getCharOffset()); // number starts at index 12
        p.close();
    }
