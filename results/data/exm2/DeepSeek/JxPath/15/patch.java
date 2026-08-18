    public boolean setPosition(int position) {
        if (!prepared) {
            prepared = true;
            BasicNodeSet nodeSet = (BasicNodeSet) getNodeSet();
            ArrayList pointers = new ArrayList();
            for (int i = 0; i < contexts.length; i++) {
                EvalContext ctx = (EvalContext) contexts[i];
                while (ctx.nextSet()) {
                    while (ctx.nextNode()) {
                        NodePointer ptr = ctx.getCurrentNodePointer();
                        if (!pointers.contains(ptr)) {
                            pointers.add(ptr);
                        }
                    }
                }
            }
            java.util.Collections.sort(pointers);
            for (int i = 0; i < pointers.size(); i++) {
                nodeSet.add((NodePointer) pointers.get(i));
            }
        }
        return super.setPosition(position);
    }