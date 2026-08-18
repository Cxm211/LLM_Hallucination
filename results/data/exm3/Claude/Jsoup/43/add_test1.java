// org/jsoup/nodes/ElementTest.java
@Test public void testPreviousNextSiblingWithEqualContent() {
    Document doc = Jsoup.parse("<div><span>A</span><span id='mid'>A</span><span>A</span></div>");
    Element mid = doc.getElementById("mid");
    
    Element prev = mid.previousElementSibling();
    Element next = mid.nextElementSibling();
    
    assertNotNull(prev);
    assertNotNull(next);
    assertEquals("A", prev.text());
    assertEquals("A", next.text());
    
    assertNotSame(mid, prev);
    assertNotSame(mid, next);
    assertNotSame(prev, next);
    
    assertEquals(0, prev.elementSiblingIndex());
    assertEquals(1, mid.elementSiblingIndex());
    assertEquals(2, next.elementSiblingIndex());
}