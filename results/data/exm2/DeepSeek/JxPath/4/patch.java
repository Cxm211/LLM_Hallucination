protected String getLanguage() {
    Object n = node;
    while (n != null) {
        if (n instanceof org.w3c.dom.Node && ((org.w3c.dom.Node)n).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
            org.w3c.dom.Element e = (org.w3c.dom.Element) n;
            String attr = e.getAttribute("xml:lang");
            if (attr != null && !attr.equals("")) {
                return attr;
            }
        } else if (n instanceof org.jdom.Element) {
            org.jdom.Element e = (org.jdom.Element) n;
            String attr = e.getAttributeValue("lang", org.jdom.Namespace.XML_NAMESPACE);
            if (attr != null && !attr.equals("")) {
                return attr;
            }
        }
        // get parent
        if (n instanceof org.w3c.dom.Node) {
            n = ((org.w3c.dom.Node)n).getParentNode();
        } else if (n instanceof org.jdom.Content) {
            n = ((org.jdom.Content)n).getParent();
        } else {
            n = null;
        }
    }
    return null;
}