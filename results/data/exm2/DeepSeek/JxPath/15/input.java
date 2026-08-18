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
                            nodeSet.add(ptr);
                            pointers.add(ptr);
                        }
                    }
                }
            }
        }
        return super.setPosition(position);
    }

// trigger testcase
public void testUnion() {
        assertXPathValue(context, "/vendor[1]/contact[1] | /vendor[1]/contact[4]", "John");
        assertXPathValue(context, "/vendor[1]/contact[4] | /vendor[1]/contact[1]", "John");
    }

public void testUnion() {
        assertXPathValue(context, "/vendor[1]/contact[1] | /vendor[1]/contact[4]", "John");
        assertXPathValue(context, "/vendor[1]/contact[4] | /vendor[1]/contact[1]", "John");
    }
