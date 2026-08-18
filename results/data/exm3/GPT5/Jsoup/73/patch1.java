public void head(org.jsoup.nodes.Node source, int depth) {
            if (source instanceof org.jsoup.nodes.Element) {
                org.jsoup.nodes.Element sourceEl = (org.jsoup.nodes.Element) source;

                // capture namespace changes declared on this element so we can restore on tail
                java.util.List<String[]> nsScope = new java.util.ArrayList<>();
                {
                    Attributes attributes = sourceEl.attributes();
                    for (Attribute attr : attributes) {
                        String key = attr.getKey();
                        String prefix;
                        if (key.equals(xmlnsKey)) {
                            prefix = "";
                        } else if (key.startsWith(xmlnsPrefix)) {
                            prefix = key.substring(xmlnsPrefix.length());
                        } else {
                            continue;
                        }
                        String prev = namespaces.get(prefix);
                        nsScope.add(new String[] { prefix, prev });
                    }
                }

                String prefix = updateNamespaces(sourceEl);
                String namespace = namespaces.get(prefix);

                Element el = doc.createElementNS(namespace, sourceEl.tagName());
                // attach the namespace scope info to this element for restoration on tail
                el.setUserData("namespacesScope", nsScope, null);

                copyAttributes(sourceEl, el);
                if (dest == null) { // sets up the root
                    doc.appendChild(el);
                } else {
                    dest.appendChild(el);
                }
                dest = el; // descend
            } else if (source instanceof org.jsoup.nodes.TextNode) {
                org.jsoup.nodes.TextNode sourceText = (org.jsoup.nodes.TextNode) source;
                Text text = doc.createTextNode(sourceText.getWholeText());
                if (dest != null) dest.appendChild(text);
            } else if (source instanceof org.jsoup.nodes.Comment) {
                org.jsoup.nodes.Comment sourceComment = (org.jsoup.nodes.Comment) source;
                Comment comment = doc.createComment(sourceComment.getData());
                if (dest != null) dest.appendChild(comment);
            } else if (source instanceof org.jsoup.nodes.DataNode) {
                org.jsoup.nodes.DataNode sourceData = (org.jsoup.nodes.DataNode) source;
                Text node = doc.createTextNode(sourceData.getWholeData());
                if (dest != null) dest.appendChild(node);
            } else {
                // unhandled
            }
        }