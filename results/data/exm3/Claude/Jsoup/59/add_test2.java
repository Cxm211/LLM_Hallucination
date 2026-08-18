// org/jsoup/safety/CleanerTest.java
@Test public void handlesWhitespaceOnlyAttributeName() {
        String html = "<a   =\"value\">test</a>";
        String clean = Jsoup.clean(html, Whitelist.basic());
        assertEquals("<a rel=\"nofollow\">test</a>", clean);
    }