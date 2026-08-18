// org/apache/commons/jxpath/ri/compiler/VariableTest.java
public void testCompareDifferentTrees() throws Exception {
        Document doc1 = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(
                        new InputSource(new StringReader(
                                "<doc1><a/></doc1>")));
        Document doc2 = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(
                        new InputSource(new StringReader(
                                "<doc2><b/></doc2>")));
        JXPathContext context1 = JXPathContext.newContext(doc1);
        JXPathContext context2 = JXPathContext.newContext(doc2);
        context1.getVariables().declareVariable("otherNode", context2.getPointer("/doc2/b"));
        int sz = 0;
        for (Iterator ptrs = context1.iteratePointers("/doc1/a | $otherNode"); ptrs.hasNext(); sz++) {
            ptrs.next();
        }
        assertEquals(2, sz);
    }
