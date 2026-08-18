// org/jsoup/safety/CleanerTest.java
@Test
public void handlesCustomProtocolsOnCustomTag() {
    String html = "<mytag myattr='custom:value'>Text</mytag>";
    Whitelist whitelist = new Whitelist()
        .addTags("mytag")
        .addAttributes("mytag", "myattr")
        .addProtocols("mytag", "myattr", "custom");
    String result = Jsoup.clean(html, whitelist);
    assertEquals("<mytag myattr=\"custom:value\">Text</mytag>", result);
}
