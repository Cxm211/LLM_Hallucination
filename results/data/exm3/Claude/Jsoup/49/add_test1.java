// org/jsoup/nodes/ElementTest.java
@Test
    public void insertChildrenFromDifferentParents() {
        Document doc = new Document("");
        Element body = doc.appendElement("body");
        Element container1 = body.appendElement("container1");
        Element container2 = body.appendElement("container2");
        final Element div1 = container1.appendElement("div1");
        final Element div2 = container2.appendElement("div2");

        ArrayList<Element> toMove = new ArrayList<Element>();
        toMove.add(div1);
        toMove.add(div2);

        body.insertChildren(0, toMove);

        String result = doc.toString().replaceAll("\\s+", "");
        assertEquals("<body><div1></div1><div2></div2><container1></container1><container2></container2></body>", result);
    }