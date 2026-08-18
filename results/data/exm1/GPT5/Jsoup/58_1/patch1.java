public static List<Node> parseFragment(String fragmentHtml, Element context, String baseUri) {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        if (context == null) {
            Document doc = treeBuilder.parse(fragmentHtml, baseUri, ParseErrorList.noTracking(), treeBuilder.defaultSettings());
            List<Node> nodes = new ArrayList<Node>(1);
            if (doc.childNodeSize() > 0)
                nodes.add(doc.child(0));
            return nodes;
        }
        return treeBuilder.parseFragment(fragmentHtml, context, baseUri, ParseErrorList.noTracking(), treeBuilder.defaultSettings());
    }