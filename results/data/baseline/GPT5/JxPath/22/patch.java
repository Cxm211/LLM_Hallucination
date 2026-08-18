public static String getNamespaceURI(Node node) {
        if (node == null) {
            return null;
        }
        if (node instanceof Document) {
            node = ((Document) node).getDocumentElement();
            if (node == null) {
                return null;
            }
        }

        if (!(node instanceof Element)) {
            return node.getNamespaceURI();
        }

        Element element = (Element) node;

        String uri = element.getNamespaceURI();
        if (uri == null || uri.length() == 0) {
            String prefix = getPrefix(node);
            if (prefix != null && prefix.length() == 0) {
                prefix = null;
            }
            String qname = prefix == null ? "xmlns" : "xmlns:" + prefix;

            Node aNode = node;
            while (aNode != null) {
                if (aNode.getNodeType() == Node.ELEMENT_NODE) {
                    Attr attr = ((Element) aNode).getAttributeNode(qname);
                    if (attr != null) {
                        String val = attr.getValue();
                        return (val != null && val.length() > 0) ? val : null;
                    }
                }
                aNode = aNode.getParentNode();
            }
            return null;
        }
        return uri;
    }