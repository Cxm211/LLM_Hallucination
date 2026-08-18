// org/jsoup/nodes/ElementTest.java
@Test
    public void moveTwoChildrenFromStartToEnd() {
        Document doc = new Document("");
        Element body = doc.appendElement("body");
        body.appendElement("div1");
        body.appendElement("div2");
        body.appendElement("div3");
        body.appendElement("div4");
        Element div1 = body.child(0);
        Element div2 = body.child(1);
        ArrayList<Element> toMove = new ArrayList<Element>();
        toMove.add(div1);
        toMove.add(div2);
        body.insertChildren(4, toMove);
        String result = doc.toString().replaceAll("\\s+", "");
        assertEquals("<body><div3></div3><div4></div4><div1></div1><div2></div2></body>", result);
    }
