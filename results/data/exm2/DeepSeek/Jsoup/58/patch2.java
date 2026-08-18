    public boolean isValid(Document dirtyDocument) {
        Validate.notNull(dirtyDocument);

        Document clean = Document.createShell(dirtyDocument.baseUri());
        int numDiscarded = copySafeNodes(dirtyDocument.body(), clean.body());
        if (numDiscarded > 0) {
            return false;
        }
        // check for any filtered attributes
        for (Element el : dirtyDocument.body().getAllElements()) {
            for (Attribute attr : el.attributes()) {
                if (!whitelist.isSafeAttribute(el.tagName(), el, attr)) {
                    return false;
                }
            }
        }
        return true;
    }