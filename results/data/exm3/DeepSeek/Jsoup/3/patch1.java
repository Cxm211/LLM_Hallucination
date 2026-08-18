    public Element prepend(String html) {
        Validate.notNull(html);
        
        Element fragment = Parser.parseBodyFragment(html, baseUri).body();
        java.util.List<Node> fragmentNodes = fragment.childNodes();
        java.util.List<Node> nodes = new java.util.ArrayList<Node>();
        for (Node node : fragmentNodes) {
            if (node instanceof Element) {
                Element el = (Element) node;
                if (this.tag().equals(Tag.valueOf("table")) && el.tag().equals(Tag.valueOf("table"))) {
                    // unwrap the implicit table
                    for (Node child : el.childNodes()) {
                        child.parentNode = null;
                        nodes.add(child);
                    }
                    continue;
                }
            }
            node.parentNode = null;
            nodes.add(node);
        }
        // Now prepend in reverse order to maintain original order
        for (int i = nodes.size() - 1; i >= 0; i--) {
            prependChild(nodes.get(i));
        }
        return this;
    }