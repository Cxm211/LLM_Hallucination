    public void outerHtml(StringBuilder accum) {
        Document doc = ownerDocument();
        OutputSettings out = doc != null ? doc.outputSettings() : new OutputSettings();
        new NodeTraversor(new OuterHtmlVisitor(accum, out)).traverse(this);
    }