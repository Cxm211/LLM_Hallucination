// ===== FIXED org.jsoup.nodes.Document :: normalise() [lines 99-118] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-7-fixed/src/main/java/org/jsoup/nodes/Document.java =====
    public Document normalise() {
        Element htmlEl = findFirstElementByTagName("html", this);
        if (htmlEl == null)
            htmlEl = appendElement("html");
        if (head() == null)
            htmlEl.prependElement("head");
        if (body() == null)
            htmlEl.appendElement("body");

        // pull text nodes out of root, html, and head els, and push into body. non-text nodes are already taken care
        // of. do in inverse order to maintain text order.
        normaliseTextNodes(head());
        normaliseTextNodes(htmlEl);
        normaliseTextNodes(this);

        normaliseStructure("head", htmlEl);
        normaliseStructure("body", htmlEl);
        
        return this;
    }
