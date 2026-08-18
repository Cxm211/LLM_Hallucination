private int getRelativePositionByQName() {
        // Handle JDOM Element
        if (node instanceof org.jdom.Element) {
            org.jdom.Element elem = (org.jdom.Element) node;
            Object parent = elem.getParent();
            if (!(parent instanceof org.jdom.Element)) {
                return 1;
            }
            java.util.List children = ((org.jdom.Element) parent).getContent();
            int count = 0;
            String name = elem.getQualifiedName();
            for (int i = 0; i < children.size(); i++) {
                Object child = children.get(i);
                if ((child instanceof org.jdom.Element)
                    && ((org.jdom.Element) child).getQualifiedName().equals(name)) {
                    count++;
                }
                if (child == node) {
                    break;
                }
            }
            return count;
        }
        // Handle W3C DOM Node
        if (node instanceof org.w3c.dom.Node) {
            int count = 1;
            org.w3c.dom.Node self = (org.w3c.dom.Node) node;
            org.w3c.dom.Node n = self.getPreviousSibling();
            String selfName = self.getNodeName();
            while (n != null) {
                if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    String nm = n.getNodeName();
                    if (nm.equals(selfName)) {
                        count++;
                    }
                }
                n = n.getPreviousSibling();
            }
            return count;
        }
        return 1;
    }