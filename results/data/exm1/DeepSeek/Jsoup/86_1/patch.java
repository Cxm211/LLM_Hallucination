public XmlDeclaration asXmlDeclaration() {
        String data = getData();
        if (data == null || data.length() < 2) return null;
        boolean isProcessingInstruction = data.startsWith("?");
        String declContent;
        if (isProcessingInstruction) {
            declContent = data.substring(1, data.length() - 1);
        } else if (data.startsWith("!")) {
            declContent = data.substring(1);
        } else {
            return null;
        }
        Document doc = Jsoup.parse("<" + declContent + ">", baseUri(), Parser.xmlParser());
        XmlDeclaration decl = null;
        if (doc.childNodeSize() > 0) {
            Element el = doc.child(0);
            decl = new XmlDeclaration(NodeUtils.parser(doc).settings().normalizeTag(el.tagName()), isProcessingInstruction);
            decl.attributes().addAll(el.attributes());
        }
        return decl;
    }