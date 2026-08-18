public Document normalise() {
    Element htmlEl = findFirstElementByTagName("html", this);
    if (htmlEl == null)
        htmlEl = appendElement("html");
    if (head() == null)
        htmlEl.prependElement("head");
    if (body() == null)
        htmlEl.appendElement("body");

    List<Node> beforeBodyTextNodes = new ArrayList<>();
    List<Node> afterBodyTextNodes = new ArrayList<>();
    collectTextNodes(this, beforeBodyTextNodes, afterBodyTextNodes);

    for (Node node : beforeBodyTextNodes) {
        node.remove();
    }
    for (Node node : afterBodyTextNodes) {
        node.remove();
    }

    for (int i = beforeBodyTextNodes.size()-1; i >= 0; i--) {
        Node node = beforeBodyTextNodes.get(i);
        body().prependChild(new TextNode(" ", ""));
        body().prependChild(node);
    }
    for (Node node : afterBodyTextNodes) {
        body().appendChild(new TextNode(" ", ""));
        body().appendChild(node);
    }

    return this;
}

private void collectTextNodes(Node node, List<Node> beforeList, List<Node> afterList) {
    if (!(node instanceof Element)) return;
    Element el = (Element) node;
    if (el.tagName().equals("body")) return;

    if (el.tagName().equals("html")) {
        boolean bodyFound = false;
        for (Node child : el.childNodes) {
            if (child instanceof TextNode) {
                TextNode tn = (TextNode) child;
                if (!tn.isBlank()) {
                    if (bodyFound) {
                        afterList.add(tn);
                    } else {
                        beforeList.add(tn);
                    }
                }
            } else if (child instanceof Element) {
                Element childEl = (Element) child;
                if (childEl.tagName().equals("body")) {
                    bodyFound = true;
                } else {
                    collectTextNodes(child, beforeList, afterList);
                }
            }
        }
    } else {
        // root or head: all text nodes are before body
        for (Node child : el.childNodes) {
            if (child instanceof TextNode) {
                TextNode tn = (TextNode) child;
                if (!tn.isBlank()) {
                    beforeList.add(tn);
                }
            } else if (child instanceof Element) {
                Element childEl = (Element) child;
                if (!childEl.tagName().equals("body")) {
                    collectTextNodes(child, beforeList, afterList);
                }
            }
        }
    }
}