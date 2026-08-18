public Element wrap(String html) {
    Validate.notEmpty(html);

    Element wrapBody = Parser.parseBodyFragment(html, baseUri).body();
    List<Element> children = new ArrayList<Element>(wrapBody.children());
    Element wrap = children.get(0);
    if (wrap == null)
        return null;

    // Detach wrap from fragment
    wrap.remove();

    Element deepest = getDeepChild(wrap);
    parentNode.replaceChild(this, wrap);
    deepest.addChild(this);

    // Handle remainder children
    for (int i = 1; i < children.size(); i++) {
        Element remainder = children.get(i);
        remainder.remove();
        wrap.appendChild(remainder);
    }
    return this;
}