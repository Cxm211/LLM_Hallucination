// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java
public void testNodeTestOnCommentAndPI() throws Exception {
        String xml = "<root><elem/></root>";
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(xml)));
        Element root = doc.getDocumentElement();
        Element elem = (Element) root.getFirstChild();
        Comment comment = doc.createComment("comment");
        ProcessingInstruction pi = doc.createProcessingInstruction("target", "data");
        root.insertBefore(comment, elem);
        root.insertBefore(pi, elem);
        JXPathContext context = JXPathContext.newContext(doc);
        assertXPathPointer(context, "//elem/preceding::node()[1]", "/root/processing-instruction()[1]");
        assertXPathPointer(context, "//elem/preceding::node()[2]", "/root/comment()[1]");
    }
