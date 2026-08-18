// org/jsoup/nodes/ElementTest.java
@Test
public void testRemoveMultipleSameCaseAttributes() {
    String html = "<div attr1='val1' ATTR1='val2' Attr1='val3'>Content</div>";
    Document doc = Jsoup.parse(html);
    Element div = doc.select("div").first();
    div.removeAttr("attr1");
    String result = div.outerHtml();
    assertFalse(result.toLowerCase().contains("attr1"));
}