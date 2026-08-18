// org/jsoup/nodes/EntitiesTest.java
@Test public void doesNotEscapeGtInHtmlAttributeButEscapesInXhtml() {
    String docHtml = "<a title='a>b'>Test</a>";
    Document doc = Jsoup.parse(docHtml);
    Element element = doc.select("a").first();

    doc.outputSettings().escapeMode(EscapeMode.base);
    assertEquals("<a title=\"a>b\">Test</a>", element.outerHtml());

    doc.outputSettings().escapeMode(EscapeMode.xhtml);
    assertEquals("<a title=\"a&gt;b\">Test</a>", element.outerHtml());
}