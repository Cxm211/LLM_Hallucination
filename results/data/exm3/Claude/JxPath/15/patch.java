public boolean setPosition(int position) {
    if (!prepared) {
        prepared = true;
        BasicNodeSet nodeSet = (BasicNodeSet) getNodeSet();
        java.util.Set pointers = new java.util.HashSet();
        for (int i = 0; i < contexts.length; i++) {
            EvalContext ctx = (EvalContext) contexts[i];
            while (ctx.nextSet()) {
                while (ctx.nextNode()) {
                    NodePointer ptr = ctx.getCurrentNodePointer();
                    if (!pointers.contains(ptr)) {
                        nodeSet.add(ptr);
                        pointers.add(ptr);
                    }
                }
            }
        }
    }
    return super.setPosition(position);
}