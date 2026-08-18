protected String getLanguage() {
    Node n = node;
    while (n != null) {
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element) n;
            String attr = e.getAttributeNS("http://www.w3.org/XML/1998/namespace", "lang");
            if (attr != null && !attr.equals("")) {
                return attr;
            }
        }
        n = n.getParentNode();
    }
    return null;
}