    private int getRelativePositionByQName() {
        if (node instanceof Element) {
            Object parent = ((Element) node).getParent();
            if (!(parent instanceof Element)) {
                return 1;
            }

            List children = ((Element) parent).getContent();
            int count = 1;
            String name = ((Element) node).getQualifiedName();
            for (int i = 0; i < children.size(); i++) {
                Object child = children.get(i);
                if ((child instanceof Element)
                    && ((Element) child).getQualifiedName().equals(name)) {
                    if (child == node) {
                        break;
                    }
                    count++;
                }
            }
            return count;
        }
        return 1;
    }