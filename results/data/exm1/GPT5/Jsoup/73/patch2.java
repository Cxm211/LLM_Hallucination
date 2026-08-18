public void tail(org.jsoup.nodes.Node source, int depth) {
            if (source instanceof org.jsoup.nodes.Element) {
                Object nsObj = dest != null ? dest.getUserData("jsoupNamespaces") : null;
                if (nsObj instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, String> prev = (java.util.Map<String, String>) nsObj;
                    namespaces.clear();
                    namespaces.putAll(prev);
                }
                if (dest.getParentNode() instanceof Element) {
                    dest = (Element) dest.getParentNode(); // undescend. cromulent.
                }
            }
        }