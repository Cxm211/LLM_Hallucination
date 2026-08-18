private String getLanguageForJDOM() {
    Object n = node;
    while (n != null) {
        if (n instanceof org.jdom.Element) {
            org.jdom.Element e = (org.jdom.Element) n;
            String attr = e.getAttributeValue("lang", org.jdom.Namespace.XML_NAMESPACE);
            if (attr != null && !attr.equals("")) {
                return attr;
            }
        }
        if (n instanceof org.jdom.Content) {
            n = ((org.jdom.Content) n).getParent();
        } else {
            n = null;
        }
    }
    return null;
}