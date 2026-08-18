// org/jsoup/nodes/EntitiesTest.java
@Test public void unescapeUnknownEntity() {
        String escaped = "&unknownEntity; &validAmp;";
        String result = Entities.unescape(escaped);
        assertEquals("&unknownEntity; &", result);
    }