public boolean setPosition(int position) {
        if (!prepared) {
            BasicNodeSet nodeSet = (BasicNodeSet) getNodeSet();
            java.util.TreeSet sorted = new java.util.TreeSet(new java.util.Comparator() {
                public int compare(Object o1, Object o2) {
                    return ((NodePointer) o1).compareTo((NodePointer) o2);
                }
            });
            for (int i = 0; i < contexts.length; i++) {
                EvalContext ctx = (EvalContext) contexts[i];
                while (ctx.nextSet()) {
                    while (ctx.nextNode()) {
                        NodePointer ptr = ctx.getCurrentNodePointer();
                        sorted.add(ptr);
                    }
                }
            }
            for (java.util.Iterator it = sorted.iterator(); it.hasNext();) {
                nodeSet.add((NodePointer) it.next());
            }
            prepared = true;
        }
        return super.setPosition(position);
    }