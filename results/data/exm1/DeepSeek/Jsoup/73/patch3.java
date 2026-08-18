private String updateNamespaces(org.jsoup.nodes.Element el) {
            int pos = el.tagName().indexOf(":");
            return pos > 0 ? el.tagName().substring(0, pos) : "";
        }