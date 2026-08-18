public boolean isValid(Document dirtyDocument) {
    Validate.notNull(dirtyDocument);
    String baseUri = dirtyDocument.baseUri();
    if (baseUri == null) baseUri = "";
    Document clean = Document.createShell(baseUri);
    int numDiscarded = copySafeNodes(dirtyDocument.body(), clean.body());
    return numDiscarded == 0;
}