protected void outerHtml(StringBuilder accum) {
    OutputSettings outputSettings = ownerDocument() != null ? ownerDocument().outputSettings() : new OutputSettings();
    new NodeTraversor(new OuterHtmlVisitor(accum, outputSettings)).traverse(this);
}