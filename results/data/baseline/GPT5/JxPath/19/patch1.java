private int getRelativePositionByQName() {
        if (node instanceof org.jdom.Element) {
            Object parent = ((org.jdom.Element) node).getParent();
            if (!(parent instanceof org.jdom.Element)) {
                return 1;
            }

            java.util.List children = ((org.jdom.Element) parent).getContent();
            int count = 0;
            String name = ((org.jdom.Element) node).getQualifiedName();
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
        if (node instanceof org.w3c.dom.Node) {
            int count = 1;
            org.w3c.dom.Node n = ((org.w3c.dom.Node) node).getPreviousSibling();
            while (n != null) {
                if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    String nm = n.getNodeName();
                    if (nm.equals(((org.w3c.dom.Node) node).getNodeName())) {
                        count++;
                    }
                }
                n = n.getPreviousSibling();
            }
            return count;
        }
        return 1;
    }