// org/jsoup/nodes/ElementTest.java
@Test
public void testCloneWithNestedStructure() {
    String html = "<div><ul><li>Item 1</li><li>Item 2</li></ul></div>";
    Document doc = Jsoup.parse(html);
    Element original = doc.selectFirst("div");
    Element clone = original.clone();
    
    Element originalLi = original.selectFirst("li");
    originalLi.after("<li>New Item</li>");
    
    assertEquals(3, original.selectFirst("ul").children().size());
    assertEquals(2, clone.selectFirst("ul").children().size());
}