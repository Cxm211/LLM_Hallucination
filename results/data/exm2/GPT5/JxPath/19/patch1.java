private int getRelativePositionByQNameJDOM() {
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
        return 1;
    }