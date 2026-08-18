// com/fasterxml/jackson/core/json/LocationInObjectTest.java
public void testMismatchedEndMarker() throws Exception {
        final JsonFactory f = new JsonFactory();
        char[] c = "{]}".toCharArray(); // extra ']' inside object
        JsonParser p = f.createParser(c);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        try {
            p.nextToken(); // should throw exception for mismatched ']'
            fail("Expected exception");
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            // expected
        }
        p.close();
    }
