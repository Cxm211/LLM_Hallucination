// org/jsoup/nodes/EntitiesTest.java::unescapeOutOfRangeNumeric
@Test public void unescapeOutOfRangeNumeric() {
        String input = "&#66000;"; // > 0xFFFF
        assertEquals(input, Entities.unescape(input));
    }