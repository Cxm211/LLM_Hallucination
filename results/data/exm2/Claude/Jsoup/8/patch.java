protected void outerHtml(StringBuilder accum) {
    new NodeTraversor(new OuterHtmlVisitor(accum, getOutputSettings())).traverse(this);
}

private Document.OutputSettings getOutputSettings() {
    return ownerDocument() != null ? ownerDocument().outputSettings() : new Document("").outputSettings();
}