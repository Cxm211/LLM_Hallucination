    private int getRelativePositionByQName() {
        if (node instanceof Element) {
            Object parent = ((Element) node).getParent();
            if (!(parent instanceof Element)) {
                return 1;
            }

            List children = ((Element) parent).getContent();
            int count = 0;
            String ns = ((Element) node).getNamespaceURI();
            String local = ((Element) node).getName();
            for (int i = 0; i < children.size(); i++) {
                Object child = children.get(i);
                if (child instanceof Element) {
                    Element childElem = (Element) child;
                    String childNs = childElem.getNamespaceURI();
                    String childLocal = childElem.getName();
                    boolean nsMatch = (ns == null) ? (childNs == null) : ns.equals(childNs);
                    boolean localMatch = local.equals(childLocal);
                    if (nsMatch && localMatch) {
                        count++;
                    }
                }
                if (child == node) {
                    break;
                }
            }
            return count;
        }
        return 1;
    }