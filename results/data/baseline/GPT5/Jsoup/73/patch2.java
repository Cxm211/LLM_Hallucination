        public void tail(org.jsoup.nodes.Node source, int depth) {
            if (source instanceof org.jsoup.nodes.Element && dest != null) {
                org.w3c.dom.Node parent = dest.getParentNode();
                if (parent instanceof Element) {
                    dest = (Element) parent; // undescend
                } else if (parent instanceof Document) {
                    dest = null; // back to document level
                }
            }
        }
