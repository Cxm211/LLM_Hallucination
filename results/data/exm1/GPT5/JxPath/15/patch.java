public boolean setPosition(int position) {
        if (!prepared) {
            BasicNodeSet nodeSet = (BasicNodeSet) getNodeSet();
            ArrayList pointers = new ArrayList();
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
            prepared = true;
        }
        return super.setPosition(position);
    }