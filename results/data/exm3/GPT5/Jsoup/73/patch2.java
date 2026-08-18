public void tail(org.jsoup.nodes.Node source, int depth) {
            if (source instanceof org.jsoup.nodes.Element) {
                // restore any namespace declarations that were set on this element
                if (dest != null) {
                    @SuppressWarnings("unchecked")
                    java.util.List<String[]> nsScope = (java.util.List<String[]>) dest.getUserData("namespacesScope");
                    if (nsScope != null) {
                        // restore in reverse order
                        for (int i = nsScope.size() - 1; i >= 0; i--) {
                            String[] entry = nsScope.get(i);
                            String prefix = entry[0];
                            String prev = entry[1];
                            if (prev == null) {
                                namespaces.remove(prefix);
                            } else {
                                namespaces.put(prefix, prev);
                            }
                        }
                    }
                }

                // undescend
                if (dest != null) {
                    org.w3c.dom.Node parent = dest.getParentNode();
                    if (parent instanceof Element) {
                        dest = (Element) parent; // undescend. cromulent.
                    } else {
                        dest = null;
                    }
                }
            }
        }