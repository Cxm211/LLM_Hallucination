// org/jsoup/nodes/TextNodeTest.java
@Test(expected = IndexOutOfBoundsException.class)
public void testTextNodeChildNodeIndexThrows() {
    Document doc = Jsoup.parse("<div>Hello there</div>");
    Element div = doc.select("div").first();
    TextNode tn = (TextNode) div.childNode(0);
    tn.childNode(0);
}
