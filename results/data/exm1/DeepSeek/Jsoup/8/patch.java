    protected void outerHtml(StringBuilder accum) {
        Document doc = ownerDocument();
        Document.OutputSettings settings = doc != null ? doc.outputSettings() : new Document.OutputSettings();
        new NodeTraversor(new OuterHtmlVisitor(accum, settings)).traverse(this);
    }