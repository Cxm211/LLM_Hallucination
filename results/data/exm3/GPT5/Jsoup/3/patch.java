public Element append(String html) {
        Validate.notNull(html);
        
        Element fragment = Parser.parseBodyFragment(html, baseUri).body();
        List<Node> nodes = new ArrayList<Node>(fragment.childNodes());
        for (Node node : nodes) {
            appendChild(node);
        }
        return this;
    }