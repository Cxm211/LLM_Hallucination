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

        // move any elements that are not allowed in <head> into <body>. maintain document order relative to body
        Element headEl = head();
        Element bodyEl = body();
        if (headEl != null && bodyEl != null) {
            java.util.Set<String> headAllowed = new java.util.HashSet<String>(java.util.Arrays.asList(
                "base", "link", "meta", "title", "script", "style", "noscript"
            ));
            java.util.List<Element> toMove = new java.util.ArrayList<Element>();
            java.util.Deque<Node> stack = new java.util.ArrayDeque<Node>();
            java.util.List<Node> headChildren = headEl.childNodes;
            for (int i = headChildren.size() - 1; i >= 0; i--) {
                stack.push(headChildren.get(i));
            }
            while (!stack.isEmpty()) {
                Node n = stack.pop();
                if (n instanceof Element) {
                    Element el = (Element) n;
                    String name = el.tagName();
                    if (headAllowed.contains(name)) {
                        java.util.List<Node> cn = el.childNodes;
                        for (int i = cn.size() - 1; i >= 0; i--) {
                            stack.push(cn.get(i));
                        }
                    } else {
                        toMove.add(el);
                    }
                }
            }
            for (int i = toMove.size() - 1; i >= 0; i--) {
                Element el = toMove.get(i);
                if (el.parent() != null)
                    el.parent().removeChild(el);
                bodyEl.prependChild(el);
            }
        }
        
        return this;
    }