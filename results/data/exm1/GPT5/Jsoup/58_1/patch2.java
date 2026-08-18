public boolean isValid(Document dirtyDocument) {
        Validate.notNull(dirtyDocument);

        Document clean = Document.createShell(dirtyDocument.baseUri());
        int numDiscarded = 0;
        numDiscarded += copySafeNodes(dirtyDocument.head(), clean.head());
        numDiscarded += copySafeNodes(dirtyDocument.body(), clean.body());
        return numDiscarded == 0;
    }