// org/apache/commons/jxpath/ri/compiler/VariableTest.java
public void testUnionOfNodeAndVariable() throws Exception {
    Document doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().parse(
                    new InputSource(new StringReader(
                            "<MAIN><A/><A/></MAIN>")));

    JXPathContext context = JXPathContext.newContext(doc);
    context.getVariables().declareVariable("var", "varValue");
    int sz = 0;
    for (Iterator ptrs = context.iteratePointers("/MAIN/A | $var"); ptrs.hasNext(); sz++) {
        ptrs.next();
    }
    assertEquals(3, sz);
}