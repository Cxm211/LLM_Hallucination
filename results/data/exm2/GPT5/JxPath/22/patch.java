public static String getNamespaceURI(Node node) {
        if (node instanceof Document) {
            node = ((Document) node).getDocumentElement();
        }

        Element element = (Element) node;

        String uri = element.getNamespaceURI();
        if (uri == null || uri.length() == 0) {
            String prefix = getPrefix(node);
            String qname = prefix == null ? "xmlns" : "xmlns:" + prefix;
    
            Node aNode = node;
            while (aNode != null) {
                if (aNode.getNodeType() == Node.ELEMENT_NODE) {
                    Attr attr = ((Element) aNode).getAttributeNode(qname);
                    if (attr != null) {
                        return (attr.getValue() == null || attr.getValue().length() == 0) ? null : attr.getValue();
                    }
                }
                aNode = aNode.getParentNode();
            }
            return null;
        }
        return uri;
    }