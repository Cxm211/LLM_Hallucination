    public static boolean isValid(String bodyHtml, Whitelist whitelist) {
        Document doc = Document.createShell("");
        Element body = doc.body();
        ParseErrorList errorList = ParseErrorList.tracking(10);
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        List<Node> nodes = treeBuilder.parseFragment(bodyHtml, body, "", errorList, treeBuilder.defaultSettings());
        if (!errorList.isEmpty()) {
            return false;
        }
        for (Node node : nodes) {
            body.appendChild(node);
        }
        return new Cleaner(whitelist).isValid(doc);
    }