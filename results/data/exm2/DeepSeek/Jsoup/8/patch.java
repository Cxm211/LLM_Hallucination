    protected void outerHtml(StringBuilder accum) {
        Document.OutputSettings out = ownerDocument().outputSettings();
        if (parent() == null) {
            out = out.clone();
            out.prettyPrint(false);
        }
        new NodeTraversor(new OuterHtmlVisitor(accum, out)).traverse(this);
    }