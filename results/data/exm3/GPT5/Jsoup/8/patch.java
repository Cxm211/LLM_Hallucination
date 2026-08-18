protected void outerHtml(StringBuilder accum) {
        Document.OutputSettings settings = ownerDocument() != null ? ownerDocument().outputSettings() : new Document.OutputSettings();
        new NodeTraversor(new OuterHtmlVisitor(accum, settings)).traverse(this);
    }