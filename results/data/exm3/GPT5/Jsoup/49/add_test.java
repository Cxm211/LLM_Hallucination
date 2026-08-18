// org/jsoup/nodes/ElementTest.java
@Test
public void insertChildrenMoveEarlierToLaterIndex() {
    Document doc = new Document("");
    Element body = doc.appendElement("body");
    Element div1 = body.appendElement("div1");
    Element div2 = body.appendElement("div2");
    Element div3 = body.appendElement("div3");

    ArrayList<Element> toMove = new ArrayList<Element>();
    toMove.add(div1);

    body.insertChildren(2, toMove);

    String result = doc.toString().replaceAll("\\s+", "");
    assertEquals("<body><div2></div2><div1></div1><div3></div3></body>", result);
}