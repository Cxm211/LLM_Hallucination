// ===== FIXED org.apache.commons.jxpath.ri.model.dom.DOMNodePointer :: getRelativePositionByQName() [lines 556-566] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-19-fixed/src/java/org/apache/commons/jxpath/ri/model/dom/DOMNodePointer.java =====
    private int getRelativePositionByQName() {
        int count = 1;
        Node n = node.getPreviousSibling();
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE && matchesQName(n)) {
                count++;
            }
            n = n.getPreviousSibling();
        }
        return count;
    }

// ===== FIXED org.apache.commons.jxpath.ri.model.jdom.JDOMNodePointer :: getRelativePositionByQName() [lines 684-706] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-19-fixed/src/java/org/apache/commons/jxpath/ri/model/jdom/JDOMNodePointer.java =====
    private int getRelativePositionByQName() {
        if (node instanceof Element) {
            Object parent = ((Element) node).getParent();
            if (!(parent instanceof Element)) {
                return 1;
            }

            List children = ((Element) parent).getContent();
            int count = 0;
            String name = ((Element) node).getQualifiedName();
            for (int i = 0; i < children.size(); i++) {
                Object child = children.get(i);
                if (child instanceof Element && matchesQName(((Element) child))) {
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
