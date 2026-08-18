// org/jsoup/nodes/ElementTest.java
@Test
public void booleanAttributeOutputNonBooleanAttribute() {
    Document doc = Jsoup.parse("<div data-flag='' class='test'>");
    Element div = doc.selectFirst("div");
    assertEquals("<div data-flag=\"\" class=\"test\"></div>", div.outerHtml());
}