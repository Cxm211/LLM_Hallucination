// org/jsoup/nodes/EntitiesTest.java
@Test public void unescapeOutOfRangeNumeric() {
        String escaped = "&#x10000; &#65537; &#160;";
        String result = Entities.unescape(escaped);
        assertEquals("&#x10000; &#65537;  ", result);
    }