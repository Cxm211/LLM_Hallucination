public void tail(org.jsoup.nodes.Node source, int depth) {
            if (source instanceof org.jsoup.nodes.Element) {
                if (!namespaceStack.isEmpty()) {
                    namespaces = namespaceStack.pop();
                }
                if (dest.getParentNode() instanceof Element) {
                    dest = (Element) dest.getParentNode();
                }
            }
        }