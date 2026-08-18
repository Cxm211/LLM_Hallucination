// org/apache/commons/jxpath/ri/compiler/VariableTest.java
public void testUnionOfNullVariableAndNode() throws Exception {
    Document doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().parse(
                    new InputSource(new StringReader(
                            "<MAIN><A/><A/></MAIN>")));

    JXPathContext context = JXPathContext.newContext(doc);
    context.getVariables().declareVariable("var", null);
    int sz = 0;
    for (Iterator ptrs = context.iteratePointers("$var | /MAIN/A"); ptrs.hasNext(); sz++) {
        ptrs.next();
    }
    assertEquals(3, sz);
}