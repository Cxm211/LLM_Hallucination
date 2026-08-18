    public Element append(String html) {
        Validate.notNull(html);
        
        Element fragment = Parser.parseBodyFragment(html, baseUri).body();
        for (Node node : fragment.childNodes()) {
            appendChild(node);
        }
        return this;
    }