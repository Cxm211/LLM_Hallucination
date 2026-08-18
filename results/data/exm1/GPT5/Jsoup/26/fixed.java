// ===== FIXED org.jsoup.safety.Cleaner :: clean(Document) [lines 39-47] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-26-fixed/src/main/java/org/jsoup/safety/Cleaner.java =====
    public Document clean(Document dirtyDocument) {
        Validate.notNull(dirtyDocument);

        Document clean = Document.createShell(dirtyDocument.baseUri());
        if (dirtyDocument.body() != null) // frameset documents won't have a body. the clean doc will have empty body.
            copySafeNodes(dirtyDocument.body(), clean.body());

        return clean;
    }
