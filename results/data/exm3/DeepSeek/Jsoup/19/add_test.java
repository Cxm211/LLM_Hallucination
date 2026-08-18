// org/jsoup/safety/CleanerTest.java
@Test
public void handlesCustomProtocolsWithPreserveRelativeLinks() {
    String html = "<a href='custom:something'>Link</a>";
    Whitelist whitelist = Whitelist.basic().preserveRelativeLinks(true);
    whitelist.addProtocols("a", "href", "custom");
    String result = Jsoup.clean(html, whitelist);
    assertEquals("<a href=\"custom:something\">Link</a>", result);
}
