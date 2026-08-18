public static List<Node> parseFragment(String fragmentHtml, Element context, String baseUri) {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        if (context == null) {
            // treat as full document parse
            Document doc = treeBuilder.parse(fragmentHtml, baseUri, ParseErrorList.noTracking(), treeBuilder.defaultSettings());
            List<Node> nodes = new ArrayList<>();
            nodes.add(doc.child(0));
            return nodes;
        }
        return treeBuilder.parseFragment(fragmentHtml, context, baseUri, ParseErrorList.noTracking(), treeBuilder.defaultSettings());
    }