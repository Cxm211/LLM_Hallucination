// org/jsoup/nodes/ElementTest.java
@Test public void testElementSiblingIndexWithIdenticalElements() {
    Document doc = Jsoup.parse("<div><p>Same</p><p>Same</p><p>Same</p></div>");
    Elements ps = doc.select("p");
    Element firstP = ps.get(0);
    Element secondP = ps.get(1);
    Element thirdP = ps.get(2);
    
    assertEquals(0, firstP.elementSiblingIndex());
    assertEquals(1, secondP.elementSiblingIndex());
    assertEquals(2, thirdP.elementSiblingIndex());
    
    assertEquals(firstP, firstP.parent().child(0));
    assertEquals(secondP, secondP.parent().child(1));
    assertEquals(thirdP, thirdP.parent().child(2));
}