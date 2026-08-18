    public Object getValue() {
        boolean preserveSpace = false;
        Object n = node;
        while (n != null) {
            if (n instanceof Element) {
                Element e = (Element) n;
                String attr = e.getAttributeValue("space", Namespace.XML_NAMESPACE);
                if (attr != null) {
                    if (attr.equals("preserve")) {
                        preserveSpace = true;
                        break;
                    } else if (attr.equals("default")) {
                        preserveSpace = false;
                        break;
                    }
                }
            }
            n = nodeParent(n);
        }
        if (node instanceof Element) {
            String text = ((Element) node).getText();
            if (text == null) return null;
            return preserveSpace ? text : text.trim();
        }
        if (node instanceof Comment) {
            String text = ((Comment) node).getText();
            if (text == null) return null;
            return preserveSpace ? text : text.trim();
        }
        if (node instanceof Text) {
            String text = ((Text) node).getText();
            if (text == null) return null;
            return preserveSpace ? text : text.trim();
        }
        if (node instanceof CDATA) {
            String text = ((CDATA) node).getText();
            if (text == null) return null;
            return preserveSpace ? text : text.trim();
        }
        if (node instanceof ProcessingInstruction) {
            String text = ((ProcessingInstruction) node).getData();
            if (text == null) return null;
            return preserveSpace ? text : text.trim();
        }
        return null;
    }