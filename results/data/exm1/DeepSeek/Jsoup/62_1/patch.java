boolean anyOtherEndTag(Token t, HtmlTreeBuilder tb) {
    String name = t.asEndTag().normalName();
    ArrayList<Element> stack = tb.getStack();
    boolean found = false;
    for (int pos = stack.size() - 1; pos >= 0; pos--) {
        Element node = stack.get(pos);
        if (node.nodeName().equals(name)) {
            if (!tb.isSpecial(node)) {
                tb.generateImpliedEndTags(name);
                if (!name.equals(tb.currentElement().nodeName()))
                    tb.error(this);
                tb.popStackToClose(name);
            } else {
                tb.popStackToClose(name);
            }
            found = true;
            break;
        }
    }
    if (!found)
        tb.error(this);
    return true;
}