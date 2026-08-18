public boolean isValid(Document dirtyDocument) {
    Validate.notNull(dirtyDocument);

    Document clean = Document.createShell(dirtyDocument.baseUri());
    if (dirtyDocument.body() != null) {
        int numDiscarded = copySafeNodes(dirtyDocument.body(), clean.body());
        return numDiscarded == 0;
    }
    return true;
}