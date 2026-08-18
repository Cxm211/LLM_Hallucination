// org/jsoup/safety/CleanerTest.java
@Test public void handlesPreserveRelativeLinksWithCustomProtocols() {
        String html = "<a href='cid:12345'>Link</a> <a href='http://example.com'>Link2</a>";
        String result = Jsoup.clean(html, Whitelist.basic().addProtocols("a", "href", "cid").preserveRelativeLinks(true));
        assertEquals("<a href=\"cid:12345\" rel=\"nofollow\">Link</a> <a href=\"http://example.com\" rel=\"nofollow\">Link2</a>", result);
    }