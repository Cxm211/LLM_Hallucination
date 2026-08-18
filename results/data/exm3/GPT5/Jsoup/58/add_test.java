// org/jsoup/safety/CleanerTest.java::testIsValidBodyHtml
@Test public void testIsValidBodyHtmlParseError() {
        // unclosed tag should be invalid even if otherwise allowed
        assertFalse(Jsoup.isValid("<p>One", Whitelist.basic()));
    }