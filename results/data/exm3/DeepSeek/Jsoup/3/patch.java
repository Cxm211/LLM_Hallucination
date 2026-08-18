    public Element append(String html) {
        Validate.notNull(html);
        
        Element fragment = Parser.parseBodyFragment(html, baseUri).body();
        java.util.List<Node> nodes = new java.util.ArrayList<Node>();
        for (Node node : fragment.childNodes()) {
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
        for (Node node : nodes) {
            appendChild(node);
        }
        return this;
    }