// buggy code
    public Document clean(Document dirtyDocument) {
        Validate.notNull(dirtyDocument);

        Document clean = Document.createShell(dirtyDocument.baseUri());
        copySafeNodes(dirtyDocument.head(), clean.head());
        copySafeNodes(dirtyDocument.body(), clean.body());

        return clean;
    }