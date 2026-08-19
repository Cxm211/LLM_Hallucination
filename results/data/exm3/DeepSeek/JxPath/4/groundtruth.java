// ===== FIXED org.apache.commons.jxpath.ri.model.dom.DOMNodePointer :: getLanguage() [lines 310-312] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-4-fixed/src/java/org/apache/commons/jxpath/ri/model/dom/DOMNodePointer.java =====
    protected String getLanguage() {
        return findEnclosingAttribute(node, "xml:lang");
    }

// ===== FIXED org.apache.commons.jxpath.ri.model.dom.DOMNodePointer :: stringValue(Node) [lines 641-662] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-4-fixed/src/java/org/apache/commons/jxpath/ri/model/dom/DOMNodePointer.java =====
    private String stringValue(Node node) {
        int nodeType = node.getNodeType();
        if (nodeType == Node.COMMENT_NODE) {
            return "";
        }
        boolean trim = !"preserve".equals(findEnclosingAttribute(node, "xml:space"));
        if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
            String text = node.getNodeValue();
            return text == null ? "" : trim ? text.trim() : text;
        }
        if (nodeType == Node.PROCESSING_INSTRUCTION_NODE) {
            String text = ((ProcessingInstruction) node).getData();
            return text == null ? "" : trim ? text.trim() : text;
        }
        NodeList list = node.getChildNodes();
        StringBuffer buf = new StringBuffer(16);
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            buf.append(stringValue(child));
        }
        return buf.toString();
    }

// ===== FIXED org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer :: getLanguage() [lines 437-439] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-4-fixed/src/java/org/apache/commons/jxpath/ri/model/jdom/JDOMNodePointer.java =====
    protected String getLanguage() {
        return findEnclosingAttribute(node, "lang", Namespace.XML_NAMESPACE);
    }

// ===== FIXED org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer :: getValue() [lines 238-265] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-4-fixed/src/java/org/apache/commons/jxpath/ri/model/jdom/JDOMNodePointer.java =====
    public Object getValue() {
        if (node instanceof Element) {
            StringBuffer buf = new StringBuffer();
            for (NodeIterator children = childIterator(null, false, null); children.setPosition(children.getPosition() + 1);) {
                NodePointer ptr = children.getNodePointer();
                if (ptr.getImmediateNode() instanceof Element || ptr.getImmediateNode() instanceof Text) {
                    buf.append(ptr.getValue());
                }
            }
            return buf.toString();
        }
        if (node instanceof Comment) {
            String text = ((Comment) node).getText();
            if (text != null) {
                text = text.trim();
            }
            return text;
        }
        String result = null;
        if (node instanceof Text) {
            result = ((Text) node).getText();
        }
        if (node instanceof ProcessingInstruction) {
            result = ((ProcessingInstruction) node).getData();
        }
        boolean trim = !"preserve".equals(findEnclosingAttribute(node, "space", Namespace.XML_NAMESPACE));
        return result != null && trim ? result.trim() : result;
    }
