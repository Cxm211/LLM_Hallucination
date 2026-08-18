// org/apache/commons/jxpath/ri/model/ExternalXMLNamespaceTest.java::testElementDOM
public void testProcessingInstructionWildcardDOM() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        javax.xml.parsers.DocumentBuilder b = f.newDocumentBuilder();
        org.w3c.dom.Document doc = b.newDocument();
        org.w3c.dom.ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\"");
        assertTrue(org.apache.commons.jxpath.ri.model.dom.DOMNodePointer.testNode(pi, new org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest(null)));
    }