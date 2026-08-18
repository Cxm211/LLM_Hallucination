public static boolean isValid(String bodyHtml, Whitelist whitelist) {
        Validate.notNull(whitelist);
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        Element context = new Element(Tag.valueOf("body"), "");
        ParseErrorList errors = ParseErrorList.tracking(100);
        List<Node> nodes = treeBuilder.parseFragment(bodyHtml, context, "", errors, treeBuilder.defaultSettings());
        if (errors.size() > 0) return false;
        Document dirty = Document.createShell("");
        Element body = dirty.body();
        for (Node node : nodes) {
            body.appendChild(node);
        }
        return new Cleaner(whitelist).isValid(dirty);
    }