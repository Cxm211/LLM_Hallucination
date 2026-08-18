private String stringValue(Object current) {
        StringBuilder sb = new StringBuilder(32);
        // Depth-first collect text nodes only
        collectText(current, sb);
        return sb.toString();
    }

    // Helper: recursively collect text content while honoring xml:space
    private void collectText(Object n, StringBuilder out) {
        if (n == null) {
            return;
        }
        // DOM branch
        if (n instanceof org.w3c.dom.Node) {
            org.w3c.dom.Node dn = (org.w3c.dom.Node) n;
            short t = dn.getNodeType();
            switch (t) {
                case org.w3c.dom.Node.TEXT_NODE:
                case org.w3c.dom.Node.CDATA_SECTION_NODE: {
                    String text = dn.getNodeValue();
                    if (text != null) {
                        if (isPreserveXMLSpaceDOM(dn)) {
                            out.append(text);
                        } else {
                            out.append(text.trim());
                        }
                    }
                    return;
                }
                case org.w3c.dom.Node.ELEMENT_NODE: {
                    org.w3c.dom.Node child = dn.getFirstChild();
                    while (child != null) {
                        // only elements/text/cdata contribute; comments/PIs skipped
                        short ct = child.getNodeType();
                        if (ct == org.w3c.dom.Node.TEXT_NODE || ct == org.w3c.dom.Node.CDATA_SECTION_NODE || ct == org.w3c.dom.Node.ELEMENT_NODE) {
                            collectText(child, out);
                        }
                        child = child.getNextSibling();
                    }
                    return;
                }
                default:
                    return; // ignore comments, PIs, etc.
            }
        }
        // JDOM branch via reflection
        try {
            String cn = n.getClass().getName();
            if (cn.equals("org.jdom.Text") || cn.equals("org.jdom.CDATA")) {
                java.lang.reflect.Method getText = n.getClass().getMethod("getText");
                String text = (String) getText.invoke(n);
                if (text != null) {
                    if (isPreserveXMLSpaceJDOM(n)) {
                        out.append(text);
                    } else {
                        out.append(text.trim());
                    }
                }
                return;
            }
            if (cn.equals("org.jdom.Element")) {
                // Iterate content
                java.lang.reflect.Method getContent = n.getClass().getMethod("getContent");
                java.util.List<?> content = (java.util.List<?>) getContent.invoke(n);
                for (Object c : content) {
                    String cName = c.getClass().getName();
                    if (cName.equals("org.jdom.Text") || cName.equals("org.jdom.CDATA") || cName.equals("org.jdom.Element")) {
                        collectText(c, out);
                    }
                }
                return;
            }
            // Other JDOM nodes (Comment/PI) ignored
        } catch (Throwable ignore) {
            // If reflection fails, do nothing
        }
    }

    // Determine xml:space for DOM node by walking ancestors
    private boolean isPreserveXMLSpaceDOM(org.w3c.dom.Node n) {
        org.w3c.dom.Node cur = n;
        while (cur != null) {
            if (cur.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                org.w3c.dom.Element e = (org.w3c.dom.Element) cur;
                String val = e.getAttribute("xml:space");
                if (val != null && val.length() > 0) {
                    return "preserve".equals(val);
                }
            }
            cur = cur.getParentNode();
        }
        return false; // default
    }

    // Determine xml:space for JDOM node by walking ancestors via reflection
    private boolean isPreserveXMLSpaceJDOM(Object n) {
        try {
            Object cur = n;
            while (cur != null) {
                String cn = cur.getClass().getName();
                if (cn.equals("org.jdom.Element")) {
                    Object nsXML = Class.forName("org.jdom.Namespace").getField("XML_NAMESPACE").get(null);
                    java.lang.reflect.Method getAttr = cur.getClass().getMethod("getAttributeValue", String.class, Class.forName("org.jdom.Namespace"));
                    String val = (String) getAttr.invoke(cur, "space", nsXML);
                    if (val != null && val.length() > 0) {
                        return "preserve".equals(val);
                    }
                }
                // move to parent
                java.lang.reflect.Method getParent = null;
                try {
                    getParent = cur.getClass().getMethod("getParent");
                } catch (NoSuchMethodException ignore) {
                }
                if (getParent == null) {
                    break;
                }
                Object parent = getParent.invoke(cur);
                if (parent != null && parent.getClass().getName().equals("org.jdom.Document")) {
                    cur = null; // reached the document
                } else {
                    cur = parent;
                }
            }
        } catch (Throwable ignore) {
        }
        return false;
    }