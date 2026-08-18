// org/jsoup/nodes/ElementTest.java
@Test
    public void insertChildrenAtMiddlePosition() {
        Document doc = new Document("");
        Element body = doc.appendElement("body");
        body.appendElement("div1");
        body.appendElement("div2");
        body.appendElement("div5");
        final Element div3 = body.appendElement("div3");
        final Element div4 = body.appendElement("div4");

        ArrayList<Element> toMove = new ArrayList<Element>();
        toMove.add(div3);
        toMove.add(div4);

        body.insertChildren(2, toMove);

        String result = doc.toString().replaceAll("\\s+", "");
        assertEquals("<body><div1></div1><div2></div2><div3></div3><div4></div4><div5></div5></body>", result);
    }