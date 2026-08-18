// org/jsoup/nodes/EntitiesTest.java::unescapeHighCodepoint
@Test public void unescapeHighCodepoint() {
        String text = "Smile: &#x1F600;!";
        String expected = "Smile: " + new String(Character.toChars(0x1F600)) + "!";
        assertEquals(expected, Entities.unescape(text));
    }