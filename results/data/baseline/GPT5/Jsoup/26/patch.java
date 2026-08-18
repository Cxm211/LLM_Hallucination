public Document clean(Document dirtyDocument) {
        Validate.notNull(dirtyDocument);

        Document clean = Document.createShell(dirtyDocument.baseUri());
        org.jsoup.nodes.Element dirtyBody = dirtyDocument.body();
        org.jsoup.nodes.Element cleanBody = clean.body();
        if (dirtyBody != null && cleanBody != null) {
            copySafeNodes(dirtyBody, cleanBody);
        }

        return clean;
    }