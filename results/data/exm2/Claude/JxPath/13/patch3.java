    public NodePointer createAttribute(JXPathContext context, QName name) {
        if (!(node instanceof Element)) {
            return super.createAttribute(context, name);
        }
        Element element = (Element) node;
        String prefix = name.getPrefix();
        if (prefix != null) {
            String ns = getNamespaceURI(prefix);
            if (ns == null) {
                throw new JXPathException(
                    "Unknown namespace prefix: " + prefix);
            }
            element.setAttributeNS(ns, name.toString(), "");
        }
        else {
            String attrName = name.getName();
            if (attrName == null) {
                attrName = "";
            }
            if (!element.hasAttribute(attrName)) {
                element.setAttribute(attrName, "");
            }
        }
        NodeIterator it = attributeIterator(name);
        it.setPosition(1);
        return it.getNodePointer();
    }