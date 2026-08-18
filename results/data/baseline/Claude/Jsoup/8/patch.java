protected void outerHtml(Appendable accum) {
        try {
            new NodeTraversor(new OuterHtmlVisitor(accum, ownerDocument().outputSettings())).traverse(this);
        } catch (IOException exception) {
            throw new SerializationException(exception);
        }
    }