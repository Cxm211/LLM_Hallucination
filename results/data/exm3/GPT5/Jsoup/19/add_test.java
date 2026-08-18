// org/jsoup/safety/CleanerTest.java::handlesProtocolCaseInsensitive
@Test public void handlesProtocolCaseInsensitive() {
    String html = "<a href='MAILTO:foo@bar.com'>x</a>";
    String cleaned = Jsoup.clean(html, Whitelist.basic().addProtocols("a", "href", "MAILTO"));
    assertEquals("<a href=\"MAILTO:foo@bar.com\" rel=\"nofollow\">x</a>", cleaned);
}
