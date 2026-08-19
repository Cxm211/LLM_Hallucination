// ===== FIXED org.jsoup.nodes.Node :: outerHtml(StringBuilder) [lines 362-364] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-8-fixed/src/main/java/org/jsoup/nodes/Node.java =====
    protected void outerHtml(StringBuilder accum) {
        new NodeTraversor(new OuterHtmlVisitor(accum, getOutputSettings())).traverse(this);
    }
