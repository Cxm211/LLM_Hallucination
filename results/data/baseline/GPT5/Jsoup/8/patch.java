// buggy code
    protected void outerHtml(StringBuilder accum) {
        Document.OutputSettings out = ownerDocument() != null ? ownerDocument().outputSettings() : new Document.OutputSettings();
        new NodeTraversor(new OuterHtmlVisitor(accum, out)).traverse(this);
    }
