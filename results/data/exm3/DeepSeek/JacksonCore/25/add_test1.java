// com/fasterxml/jackson/core/read/NonStandardUnquotedNamesTest.java
public void testUnquotedInvalidCharAfterNonASCII() throws Exception {
        String JSON = "{ a" + ((char)256) + "\uffff: 1 }";
        JsonParser p = UNQUOTED_FIELDS_F.createParser(JSON);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        String expectedName = "a" + ((char)256);
        assertEquals(expectedName, p.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }
