public Element prepend(String html) {
    Validate.notNull(html);
    
    Element fragment = Parser.parseBodyFragment(html, baseUri).body();
    List<Node> nodes = fragment.childNodes();
    List<Node> copy = new ArrayList<Node>(nodes);
    for (int i = copy.size() - 1; i >= 0; i--) {
        Node node = copy.get(i);
        node.remove();
        prependChild(node);
    }
    return this;
}