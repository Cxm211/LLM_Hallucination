protected String getLanguage() {
        Object n = node;
        while (n != null) {
            // DOM path
            if (n instanceof org.w3c.dom.Node) {
                if (n instanceof org.w3c.dom.Element) {
                    org.w3c.dom.Element e = (org.w3c.dom.Element) n;
                    String attr = e.getAttribute("xml:lang");
                    if (attr != null && attr.length() > 0) {
                        return attr;
                    }
                }
                n = ((org.w3c.dom.Node) n).getParentNode();
                continue;
            }
            // JDOM path via reflection to avoid hard dependency
            try {
                Class<?> cls = n.getClass();
                if (cls.getName().equals("org.jdom.Element")) {
                    // Element.getAttributeValue(String name, Namespace ns)
                    java.lang.reflect.Method getAttr = cls.getMethod("getAttributeValue", String.class, Class.forName("org.jdom.Namespace"));
                    Object nsXML = Class.forName("org.jdom.Namespace").getField("XML_NAMESPACE").get(null);
                    String attr = (String) getAttr.invoke(n, "lang", nsXML);
                    if (attr != null && attr.length() > 0) {
                        return attr;
                    }
                }
                // move to parent: Content.getParent() or Element.getParent()
                java.lang.reflect.Method getParent = null;
                try {
                    getParent = cls.getMethod("getParent");
                } catch (NoSuchMethodException ignore) {
                }
                if (getParent != null) {
                    Object parent = getParent.invoke(n);
                    if (parent != null && parent.getClass().getName().equals("org.jdom.Document")) {
                        // stop at document root
                        n = null;
                    } else {
                        n = parent;
                    }
                    continue;
                }
            } catch (Throwable t) {
                // If reflection fails, stop the search gracefully
                n = null;
                continue;
            }
            // Unknown node type
            n = null;
        }
        return null;
    }