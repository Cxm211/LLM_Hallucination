protected String getLanguage() {
        Node n = node;
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                String attr = e.getAttributeNS("http://www.w3.org/XML/1998/namespace", "lang");
                if (attr == null || attr.equals("")) {
                    String tmp = e.getAttribute("xml:lang");
                    if (tmp != null && !tmp.equals("")) {
                        attr = tmp;
                    } else {
                        tmp = e.getAttribute("lang");
                        if (tmp != null && !tmp.equals("")) {
                            attr = tmp;
                        }
                    }
                }
                if (attr != null && !attr.equals("")) {
                    return attr;
                }
            }
            n = n.getParentNode();
        }
        return null;
    }