// ===== FIXED org.jsoup.nodes.DocumentType :: DocumentType [lines 19-25] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-40-fixed/src/main/java/org/jsoup/nodes/DocumentType.java =====
    public DocumentType(String name, String publicId, String systemId, String baseUri) {
        super(baseUri);

        attr("name", name);
        attr("publicId", publicId);
        attr("systemId", systemId);
    }
