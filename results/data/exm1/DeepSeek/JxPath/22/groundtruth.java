// ===== FIXED org.apache.commons.jxpath.ri.model.dom.DOMNodePointer :: getNamespaceURI(Node) [lines 672-697] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-22-fixed/src/java/org/apache/commons/jxpath/ri/model/dom/DOMNodePointer.java =====
    public static String getNamespaceURI(Node node) {
        if (node instanceof Document) {
            node = ((Document) node).getDocumentElement();
        }

        Element element = (Element) node;

        String uri = element.getNamespaceURI();
        if (uri == null) {
            String prefix = getPrefix(node);
            String qname = prefix == null ? "xmlns" : "xmlns:" + prefix;
    
            Node aNode = node;
            while (aNode != null) {
                if (aNode.getNodeType() == Node.ELEMENT_NODE) {
                    Attr attr = ((Element) aNode).getAttributeNode(qname);
                    if (attr != null) {
                        uri = attr.getValue();
                        break;
                    }
                }
                aNode = aNode.getParentNode();
            }
        }
        return "".equals(uri) ? null : uri;
    }
