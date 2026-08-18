// org/jsoup/nodes/EntitiesTest.java
@Test public void unescapeInvalidCodePoint() {
        String escaped = "&#xDFFFFF;";
        String unescaped = Entities.unescape(escaped);
        assertEquals("&#xDFFFFF;", unescaped);
    }
