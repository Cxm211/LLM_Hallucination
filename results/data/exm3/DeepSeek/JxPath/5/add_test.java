// org/apache/commons/jxpath/ri/compiler/VariableTest.java
public void testCompareSiblingNodePointers() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(
                        new InputSource(new StringReader(
                                "<root><a/><b/></root>")));
        JXPathContext context = JXPathContext.newContext(doc.getDocumentElement());
        int sz = 0;
        for (Iterator ptrs = context.iteratePointers("a | b"); ptrs.hasNext(); sz++) {
            ptrs.next();
        }
        assertEquals(2, sz);
    }
