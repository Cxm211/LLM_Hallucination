// ===== FIXED org.jsoup.Jsoup :: isValid(String, Whitelist) [lines 249-251] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-58-fixed/src/main/java/org/jsoup/Jsoup.java =====
    public static boolean isValid(String bodyHtml, Whitelist whitelist) {
        return new Cleaner(whitelist).isValidBodyHtml(bodyHtml);
    }

// ===== FIXED org.jsoup.safety.Cleaner :: isValid(Document) [lines 74-81] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-58-fixed/src/main/java/org/jsoup/safety/Cleaner.java =====
    public boolean isValid(Document dirtyDocument) {
        Validate.notNull(dirtyDocument);

        Document clean = Document.createShell(dirtyDocument.baseUri());
        int numDiscarded = copySafeNodes(dirtyDocument.body(), clean.body());
        return numDiscarded == 0
            && dirtyDocument.head().childNodes().size() == 0; // because we only look at the body, but we start from a shell, make sure there's nothing in the head
    }
    public static List<Node> parseFragment(String fragmentHtml, Element context, String baseUri) {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        return treeBuilder.parseFragment(fragmentHtml, context, baseUri, ParseErrorList.noTracking(), treeBuilder.defaultSettings());
    }