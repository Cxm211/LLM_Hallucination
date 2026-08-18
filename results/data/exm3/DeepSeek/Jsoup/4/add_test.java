// org/jsoup/nodes/EntitiesTest.java
@Test public void unescapeSupplementaryCharacter() {
        String escaped = "&#x10000;";
        String unescaped = Entities.unescape(escaped);
        assertEquals("\uD800\uDC00", unescaped);
    }
