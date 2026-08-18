    private void normaliseTextNodes(Element element) {
        List<Node> toMove = new ArrayList<Node>();
        boolean isHead = element.tagName().equals("head");
        for (Node node: element.childNodes) {
            if (node instanceof TextNode) {
                TextNode tn = (TextNode) node;
                if (!tn.isBlank())
                    toMove.add(tn);
            } else if (isHead && node instanceof Element) {
                Element el = (Element) node;
                if (el.tag().isMetadata()) {
                    Stack<Element> stack = new Stack<Element>();
                    stack.push(el);
                    while (!stack.isEmpty()) {
                        Element current = stack.pop();
                        for (Node child : current.childNodes) {
                            if (child instanceof Element) {
                                Element childEl = (Element) child;
                                if (childEl.tag().isMetadata()) {
                                    stack.push(childEl);
                                } else {
                                    toMove.add(childEl);
                                }
                            }
                        }
                    }
                } else {
                    toMove.add(el);
                }
            }
        }

        for (int i = toMove.size()-1; i >= 0; i--) {
            Node node = toMove.get(i);
            node.remove();
            if (node instanceof TextNode) {
                body().prependChild(new TextNode(" ", ""));
            }
            body().prependChild(node);
        }
    }