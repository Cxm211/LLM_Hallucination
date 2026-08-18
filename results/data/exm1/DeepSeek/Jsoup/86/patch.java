public XmlDeclaration asXmlDeclaration() {
    String data = getData();
    if (data == null || data.length() < 2) return null;
    char first = data.charAt(0);
    if (first != '?' && first != '!') return null;
    String declContent = data.substring(1, data.length() - 1);
    if (declContent.isEmpty()) return null;
    Document doc = Jsoup.parse("<" + declContent + ">", baseUri(), Parser.xmlParser());
    XmlDeclaration decl = null;
    if (doc.childNodeSize() > 0) {
        Element el = doc.child(0);
        decl = new XmlDeclaration(NodeUtils.parser(doc).settings().normalizeTag(el.tagName()), data.startsWith("!"));
        decl.attributes().addAll(el.attributes());
    }
    return decl;
}