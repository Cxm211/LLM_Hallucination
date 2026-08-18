    public XmlDeclaration asXmlDeclaration() {
        String data = getData();
        if (data.length() > 1 && (data.startsWith("!") || data.startsWith("?"))) {
            String declaration = data.substring(1);
            if (data.startsWith("?") && declaration.endsWith("?")) {
                declaration = declaration.substring(0, declaration.length() - 1);
            }
            Document doc = Jsoup.parse("<" + declaration + ">", baseUri(), Parser.xmlParser());
            XmlDeclaration decl = null;
            if (doc.childNodeSize() > 0) {
                Element el = doc.child(0);
                decl = new XmlDeclaration(NodeUtils.parser(doc).settings().normalizeTag(el.tagName()), data.startsWith("!"));
                decl.attributes().addAll(el.attributes());
            }
            return decl;
        }
        return null;
    }