// com/fasterxml/jackson/core/read/NonStandardUnquotedNamesTest.java
public void testUnquotedNonASCIIWithExpansion() throws Exception {
        StringBuilder sb = new StringBuilder(6000);
        sb.append("{");
        for (int i = 0; i < 5000; i++) {
            if (i == 2500) {
                sb.append((char)256);
            } else {
                sb.append('a');
            }
        }
        sb.append(":1}");
        String JSON = sb.toString();
        JsonParser p = UNQUOTED_FIELDS_F.createParser(JSON);
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        String expectedName = sb.substring(1, 5001);
        assertEquals(expectedName, p.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }
