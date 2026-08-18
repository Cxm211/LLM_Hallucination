protected void outerHtml(StringBuilder accum) {
        Document doc = ownerDocument();
        Document.OutputSettings out = doc != null ? doc.outputSettings() : new Document("").outputSettings();
        new NodeTraversor(new OuterHtmlVisitor(accum, out)).traverse(this);
    }