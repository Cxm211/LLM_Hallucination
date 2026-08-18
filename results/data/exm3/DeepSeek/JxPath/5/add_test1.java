// org/apache/commons/jxpath/ri/compiler/VariableTest.java
public void testCompareAncestorDescendant() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(
                        new InputSource(new StringReader(
                                "<root><parent><child/></parent></root>")));
        JXPathContext context = JXPathContext.newContext(doc);
        int sz = 0;
        for (Iterator ptrs = context.iteratePointers("/root/parent | /root/parent/child"); ptrs.hasNext(); sz++) {
            ptrs.next();
        }
        assertEquals(2, sz);
    }
