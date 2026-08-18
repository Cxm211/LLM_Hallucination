public XmlDeclaration asXmlDeclaration() {
        String data = getData();
        Document doc = Jsoup.parse("<" + data + ">", baseUri(), Parser.xmlParser());
        XmlDeclaration decl = null;
        if (doc.childNodeSize() > 0) {
            Element el = doc.child(0);
            decl = new XmlDeclaration(el.tagName(), data.startsWith("!"));
            decl.attributes().addAll(el.attributes());
        }
        return decl;
    }