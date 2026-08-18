        public void tail(org.jsoup.nodes.Node source, int depth) {
            if (source instanceof org.jsoup.nodes.Element && dest.getParentNode() instanceof Element) {
                @SuppressWarnings("unchecked")
                java.util.HashMap<String,String> prevNamespaces = (java.util.HashMap<String,String>) dest.getUserData("prevNamespaces");
                if (prevNamespaces != null) {
                    namespaces.clear();
                    namespaces.putAll(prevNamespaces);
                }
                dest = (Element) dest.getParentNode(); // undescend. cromulent.
            }
        }