protected void outerHtml(StringBuilder accum) {
    Document owner = ownerDocument();
    Document.OutputSettings settings = owner != null ? owner.outputSettings() : new Document.OutputSettings();
    new NodeTraversor(new OuterHtmlVisitor(accum, settings)).traverse(this);
}