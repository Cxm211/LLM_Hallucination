public static Document parseBodyFragment(String bodyHtml, String baseUri) {
        Parser parser = new Parser(bodyHtml, baseUri, true);
        return parser.parse();
    }