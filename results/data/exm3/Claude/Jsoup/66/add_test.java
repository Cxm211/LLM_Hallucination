// org/jsoup/nodes/ElementTest.java
@Test
public void testCloneWithMultipleChildren() {
    String html = "<div><p>First</p><p>Second</p><p>Third</p></div>";
    Document doc = Jsoup.parse(html);
    Element original = doc.selectFirst("div");
    Element clone = original.clone();
    
    original.child(1).text("Modified in original");
    
    assertEquals("Second", clone.child(1).text());
    assertEquals("Modified in original", original.child(1).text());
}