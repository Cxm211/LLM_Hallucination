// org/jsoup/safety/CleanerTest.java::testIsValidOnlyComment
@Test public void testIsValidOnlyComment() {
        String html = "<!-- comment -->";
        assertFalse(Jsoup.isValid(html, Whitelist.basic()));
    }