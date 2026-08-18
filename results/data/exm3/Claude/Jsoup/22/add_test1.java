// org/jsoup/nodes/NodeTest.java
@Test public void siblingNodesWithSingleChild() {
    Document doc = Jsoup.parse("<div><p>Only</p></div>");
    Element p = doc.select("p").first();
    List<Node> siblings = p.siblingNodes();
    assertEquals(0, siblings.size());
}