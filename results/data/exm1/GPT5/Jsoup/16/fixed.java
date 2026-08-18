// ===== FIXED org.jsoup.nodes.DocumentType :: DocumentType [lines 19-26] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-16-fixed/src/main/java/org/jsoup/nodes/DocumentType.java =====
    public DocumentType(String name, String publicId, String systemId, String baseUri) {
        super(baseUri);

        Validate.notEmpty(name);
        attr("name", name);
        attr("publicId", publicId);
        attr("systemId", systemId);
    }

// ===== FIXED org.jsoup.nodes.DocumentType :: outerHtmlHead(StringBuilder, int, Document) [lines 34-41] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-16-fixed/src/main/java/org/jsoup/nodes/DocumentType.java =====
    void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
        accum.append("<!DOCTYPE ").append(attr("name"));
        if (!StringUtil.isBlank(attr("publicId")))
            accum.append(" PUBLIC \"").append(attr("publicId")).append("\"");
        if (!StringUtil.isBlank(attr("systemId")))
            accum.append(" \"").append(attr("systemId")).append("\"");
        accum.append('>');
    }
