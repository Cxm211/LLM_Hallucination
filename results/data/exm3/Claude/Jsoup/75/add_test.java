// org/jsoup/nodes/ElementTest.java
@Test
public void booleanAttributeOutputEmptyString() {
    Document doc = Jsoup.parse("<input disabled='' readonly='' checked='checked'>");
    Element input = doc.selectFirst("input");
    assertEquals("<input disabled readonly checked>", input.outerHtml());
}