// org/jsoup/nodes/ElementTest.java
@Test
public void testRemoveAttributeIgnoreCaseSingle() {
    String html = "<a one two>Text</a>";
    Document doc = Jsoup.parse(html);
    Element a = doc.select("a").first();
    a.removeAttr("OnE");
    assertEquals("<a two>Text</a>", a.outerHtml());
}