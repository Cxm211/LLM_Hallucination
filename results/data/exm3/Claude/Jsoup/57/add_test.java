// org/jsoup/nodes/ElementTest.java
@Test
public void testRemoveAttributeCaseInsensitive() {
    String html = "<a One Two THREE>Text</a>";
    Document doc = Jsoup.parse(html);
    Element a = doc.select("a").first();
    a.removeAttr("one");
    a.removeAttr("TWO");
    a.removeAttr("three");
    assertEquals("<a>Text</a>", a.outerHtml());
}