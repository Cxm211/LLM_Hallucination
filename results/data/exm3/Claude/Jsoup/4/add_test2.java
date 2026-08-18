// org/jsoup/nodes/EntitiesTest.java
@Test public void unescapeMixedValidInvalid() {
        String escaped = "&lt; &NOTAREALENTITY &gt; &#65;";
        String result = Entities.unescape(escaped);
        assertEquals("< &NOTAREALENTITY > A", result);
    }