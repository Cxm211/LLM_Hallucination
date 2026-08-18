public void head(org.jsoup.nodes.Node source, int depth) {
            if (source instanceof org.jsoup.nodes.Element) {
                org.jsoup.nodes.Element sourceEl = (org.jsoup.nodes.Element) source;

                String prefix = updateNamespaces(sourceEl);
                String namespace = null;
                String searchKey = prefix.isEmpty() ? "xmlns" : "xmlns:" + prefix;
                org.jsoup.nodes.Element current = sourceEl;
                while (current != null) {
                    if (current.hasAttr(searchKey)) {
                        namespace = current.attr(searchKey);
                        break;
                    }
                    current = current.parent();
                }
                Element el = doc.createElementNS(namespace, sourceEl.tagName());
                copyAttributes(sourceEl, el);
                if (dest == null) {
                    doc.appendChild(el);
                } else {
                    dest.appendChild(el);
                }
                dest = el;
            } else if (source instanceof org.jsoup.nodes.TextNode) {
                org.jsoup.nodes.TextNode sourceText = (org.jsoup.nodes.TextNode) source;
                Text text = doc.createTextNode(sourceText.getWholeText());
                dest.appendChild(text);
            } else if (source instanceof org.jsoup.nodes.Comment) {
                org.jsoup.nodes.Comment sourceComment = (org.jsoup.nodes.Comment) source;
                Comment comment = doc.createComment(sourceComment.getData());
                dest.appendChild(comment);
            } else if (source instanceof org.jsoup.nodes.DataNode) {
                org.jsoup.nodes.DataNode sourceData = (org.jsoup.nodes.DataNode) source;
                Text node = doc.createTextNode(sourceData.getWholeData());
                dest.appendChild(node);
            } else {
                // unhandled
            }
        }