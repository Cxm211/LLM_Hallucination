// org/jsoup/nodes/ElementTest.java
@Test
    public void moveFirstChildToEnd() {
        Document doc = new Document("");
        Element body = doc.appendElement("body");
        body.appendElement("div1");
        body.appendElement("div2");
        body.appendElement("div3");
        body.appendElement("div4");
        Element div1 = body.child(0);
        body.insertChildren(4, div1);
        String result = doc.toString().replaceAll("\\s+", "");
        assertEquals("<body><div2></div2><div3></div3><div4></div4><div1></div1></body>", result);
    }
