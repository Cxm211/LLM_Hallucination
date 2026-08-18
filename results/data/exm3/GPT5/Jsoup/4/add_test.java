// org/jsoup/nodes/EntitiesTest.java::caseSensitive
@Test public void unescapeSupplementaryAndDollar() {
        assertEquals("\uD83D\uDE00", Entities.unescape("&#x1F600;"));
        assertEquals("$", Entities.unescape("&#36;"));
    }