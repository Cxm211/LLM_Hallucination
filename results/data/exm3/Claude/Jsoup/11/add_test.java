// org/jsoup/select/SelectorTest.java
@Test public void notWithId() {
    Document doc = Jsoup.parse("<p id=a>One</p><p id=b>Two</p><p id=c>Three</p>");
    Elements el1 = doc.select("p:not(#b)");
    assertEquals(2, el1.size());
    assertEquals("a", el1.first().id());
    assertEquals("c", el1.last().id());
}