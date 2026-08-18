    // Iterates the input and copies trusted nodes (tags, attributes, text) into the destination.
    private int copySafeNodes(Element root, Element destination) {
        List<Node> sourceChildren = root.childNodes();
        int numDiscarded = 0;

        for (Node source : sourceChildren) {
            if (source instanceof Element) {
                Element sourceEl = (Element) source;

                if (whitelist.isSafeTag(sourceEl.tagName())) { // safe, clone and copy safe attrs
                    ElementMeta meta = createSafeElement(sourceEl);
                    Element destChild = meta.el;
                    destination.appendChild(destChild);

                    numDiscarded += meta.numAttribsDiscarded;
                    numDiscarded += copySafeNodes(sourceEl, destChild);
                } else {
                    numDiscarded++;
                    numDiscarded += copySafeNodes(sourceEl, destination);
                }
            } else if (source instanceof TextNode) {
                TextNode sourceText = (TextNode) source;
                TextNode destText = new TextNode(sourceText.getWholeText(), source.baseUri());
                destination.appendChild(destText);
            } else if (source instanceof DataNode) {
                DataNode sourceData = (DataNode) source;
                DataNode destData = new DataNode(sourceData.getWholeData(), source.baseUri());
                destination.appendChild(destData);
            }
        }
        return numDiscarded;
    }