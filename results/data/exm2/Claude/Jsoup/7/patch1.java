private void normaliseTextNodes(Element element) {
        List<Node> toMove = new ArrayList<Node>();
        for (Node node: element.childNodes) {
            if (node instanceof TextNode) {
                TextNode tn = (TextNode) node;
                if (!tn.isBlank())
                    toMove.add(tn);
            }
        }

        for (int i = toMove.size()-1; i >= 0; i--) {
            Node node = toMove.get(i);
            element.removeChild(node);
            body().prependChild(new TextNode(" ", ""));
            body().prependChild(node);
        }
        
        // Move non-text element nodes to body if they are not meant to be in head/html/root
        List<Node> elementsToMove = new ArrayList<Node>();
        for (Node node: element.childNodes) {
            if (node instanceof Element) {
                Element el = (Element) node;
                if (!isValidHeadElement(el.tagName()) && element == head()) {
                    elementsToMove.add(el);
                }
            }
        }
        
        for (Node node: elementsToMove) {
            element.removeChild(node);
            body().prependChild(node);
        }
    }
    
    private boolean isValidHeadElement(String tagName) {
        return tagName.equals("title") || tagName.equals("base") || tagName.equals("script") ||
               tagName.equals("style") || tagName.equals("meta") || tagName.equals("link") ||
               tagName.equals("object") || tagName.equals("basefont") || tagName.equals("bgsound") ||
               tagName.equals("command");
    }