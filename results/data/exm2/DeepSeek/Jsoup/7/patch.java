public Document normalise() {
    Element htmlEl = findFirstElementByTagName("html", this);
    if (htmlEl == null)
        htmlEl = appendElement("html");
    if (head() == null)
        htmlEl.prependElement("head");
    if (body() == null)
        htmlEl.appendElement("body");

    // pull text nodes out of root, html, and head els, and push into body. non-text nodes are already taken care
    // of. do in inverse order to maintain text order.
    normaliseTextNodes(head());
    normaliseTextNodes(htmlEl);
    normaliseTextNodes(this);

    // move non-metadata elements from head to body
    java.util.Set<String> metadataTags = new java.util.HashSet<String>(java.util.Arrays.asList("title", "base", "link", "meta", "style", "script", "noscript"));
    java.util.List<Element> toMoveElements = new java.util.ArrayList<Element>();
    java.util.Deque<Element> stack = new java.util.ArrayDeque<Element>();
    stack.push(head());
    while (!stack.isEmpty()) {
        Element current = stack.pop();
        for (Node node : current.childNodes) {
            if (node instanceof Element) {
                Element el = (Element) node;
                if (metadataTags.contains(el.tagName().toLowerCase())) {
                    stack.push(el);
                } else {
                    toMoveElements.add(el);
                }
            }
        }
    }
    for (int i = toMoveElements.size() - 1; i >= 0; i--) {
        Element el = toMoveElements.get(i);
        el.remove();
        body().prependChild(el);
    }

    return this;
}