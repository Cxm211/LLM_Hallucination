public static String getNamespaceURI(Node node) {
        if (node == null) {
            return null;
        }

        // If the node is a Document, use its document element
        if (node instanceof Document) {
            Element de = ((Document) node).getDocumentElement();
            if (de == null) {
                return null;
            }
            node = de;
        }

        // Find an element context for namespace resolution
        Element element = null;
        if (node instanceof Element) {
            element = (Element) node;
        } else if (node instanceof Attr) {
            element = ((Attr) node).getOwnerElement();
        } else {
            // Walk up to find the nearest element ancestor
            Node p = node;
            while (p != null && p.getNodeType() != Node.ELEMENT_NODE) {
                p = p.getParentNode();
            }
            if (p instanceof Element) {
                element = (Element) p;
            }
        }

        if (element == null) {
            return null;
        }

        String uri = element.getNamespaceURI();
        // Some DOM implementations may return an empty string for no namespace; treat it like null
        if (uri == null || uri.length() == 0) {
            String prefix = getPrefix(node);
            String qname = prefix == null ? "xmlns" : "xmlns:" + prefix;

            Node aNode = element;
            while (aNode != null) {
                if (aNode.getNodeType() == Node.ELEMENT_NODE) {
                    Attr attr = ((Element) aNode).getAttributeNode(qname);
                    if (attr != null) {
                        return attr.getValue();
                    }
                }
                aNode = aNode.getParentNode();
            }
            return null;
        }
        return uri;
    }