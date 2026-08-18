protected void outerHtml(StringBuilder accum) {
    Document.OutputSettings settings;
    Document doc = ownerDocument();
    if (doc != null) {
        settings = doc.outputSettings();
    } else {
        settings = new Document.OutputSettings();
    }
    new NodeTraversor(new OuterHtmlVisitor(accum, settings)).traverse(this);
}