    public boolean isValid(Document dirtyDocument) {
        Validate.notNull(dirtyDocument);

        Document clean = Document.createShell(dirtyDocument.baseUri());
        int numDiscarded = copySafeNodes(dirtyDocument.body(), clean.body());
        if (numDiscarded > 0) {
            return false;
        }
        List<Element> dirtyElements = dirtyDocument.body().getAllElements();
        List<Element> cleanElements = clean.body().getAllElements();
        if (dirtyElements.size() != cleanElements.size()) {
            return false;
        }
        for (int i = 0; i < dirtyElements.size(); i++) {
            Element dirty = dirtyElements.get(i);
            Element cleanEl = cleanElements.get(i);
            if (!dirty.attributes().equals(cleanEl.attributes())) {
                return false;
            }
        }
        return true;
    }