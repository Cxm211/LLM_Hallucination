// org/jsoup/safety/CleanerTest.java
@Test public void testIsValidProtocol() {
        String html = "<a href='javascript:alert(1)'>click</a>";
        assertFalse(Jsoup.isValid(html, Whitelist.basic()));
    }
