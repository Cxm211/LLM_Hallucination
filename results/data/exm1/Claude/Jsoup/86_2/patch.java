public XmlDeclaration asXmlDeclaration() {
    String data = getData();
    Parser parser = Parser.xmlParser();
    Document doc = Jsoup.parse("<" + data.substring(1, data.length() -1) + ">", baseUri(), parser);
    XmlDeclaration decl = null;
    if (doc.childNodeSize() > 0) {
        Element el = doc.child(0);
        decl = new XmlDeclaration(NodeUtils.parser(doc).settings().normalizeTag(el.tagName()), data.startsWith("!"));
        decl.attributes().addAll(el.attributes());
    }
    return decl;
}