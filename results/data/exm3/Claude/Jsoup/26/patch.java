public Document clean(Document dirtyDocument) {
    Validate.notNull(dirtyDocument);

    Document clean = Document.createShell(dirtyDocument.baseUri());
    Element dirtyBody = dirtyDocument.body();
    if (dirtyBody != null) {
        copySafeNodes(dirtyBody, clean.body());
    }

    return clean;
}