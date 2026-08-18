// org/jsoup/helper/W3CDomTest.java
@Test
public void handlesAttributeNamesStartingWithInvalidCharacters() {
    String html = "<html><body -attr='value1' .attr='value2' 123='value3' :valid='value4' _valid='value5'></body></html>";
    org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
    Element body = jsoupDoc.select("body").first();
    
    Document w3Doc = new W3CDom().fromJsoup(jsoupDoc);
    org.w3c.dom.Element w3Body = (org.w3c.dom.Element) w3Doc.getElementsByTagName("body").item(0);
    
    // Attributes starting with invalid characters should be skipped
    assertFalse(w3Body.hasAttribute("attr")); // -attr becomes "attr" which starts with invalid
    assertFalse(w3Body.hasAttribute(".attr")); // stays .attr, invalid start
    assertFalse(w3Body.hasAttribute("123")); // stays 123, invalid start
    
    // Valid starting characters should be preserved
    assertTrue(w3Body.hasAttribute(":valid"));
    assertTrue(w3Body.hasAttribute("_valid"));
}