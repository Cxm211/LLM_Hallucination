public static String getNamespaceURI(Node node) {
    if (node instanceof Document) {
        node = ((Document) node).getDocumentElement();
    }
    if (node.getNodeType() != Node.ELEMENT_NODE) {
        Node parent = node.getParentNode();
        while (parent != null && parent.getNodeType() != Node.ELEMENT_NODE) {
            parent = parent.getParentNode();
        }
        if (parent == null) return null;
        return getNamespaceURI(parent);
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
                    return attr.getValue();
                }
            }
            aNode = aNode.getParentNode();
        }
        return null;
    }
    return uri;
}