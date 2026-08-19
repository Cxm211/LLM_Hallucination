// ===== FIXED org.jsoup.nodes.Document :: normalise(Element) [lines 113-128] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-1-fixed/src/main/java/org/jsoup/nodes/Document.java =====
    private void normalise(Element element) {
        List<Node> toMove = new ArrayList<Node>();
        for (Node node: element.childNodes) {
            if (node instanceof TextNode) {
                TextNode tn = (TextNode) node;
                if (!tn.isBlank())
                    toMove.add(tn);
            }
        }

        for (Node node: toMove) {
            element.removeChild(node);
            body().prependChild(node);
            body().prependChild(new TextNode(" ", ""));
        }
    }
