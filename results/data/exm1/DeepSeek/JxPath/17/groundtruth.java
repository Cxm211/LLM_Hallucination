// ===== FIXED org.apache.commons.jxpath.ri.model.dom.DOMAttributeIterator :: testAttr(Attr) [lines 68-94] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-17-fixed/src/java/org/apache/commons/jxpath/ri/model/dom/DOMAttributeIterator.java =====
    private boolean testAttr(Attr attr) {
        String nodePrefix = DOMNodePointer.getPrefix(attr);
        String nodeLocalName = DOMNodePointer.getLocalName(attr);

        if (nodePrefix != null && nodePrefix.equals("xmlns")) {
            return false;
        }

        if (nodePrefix == null && nodeLocalName.equals("xmlns")) {
            return false;
        }

        String testLocalName = name.getName();
        if (testLocalName.equals("*") || testLocalName.equals(nodeLocalName)) {
            String testPrefix = name.getPrefix();

            if (testPrefix == null || equalStrings(testPrefix, nodePrefix)) {
                return true;
            }
            if (nodePrefix == null) {
                return false;
            }
            return equalStrings(parent.getNamespaceURI(testPrefix), parent
                    .getNamespaceURI(nodePrefix));
        }
        return false;
    }

// ===== FIXED org.apache.commons.jxpath.ri.model.jdom.JDOMAttributeIterator :: JDOMAttributeIterator [lines 41-87] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-17-fixed/src/java/org/apache/commons/jxpath/ri/model/jdom/JDOMAttributeIterator.java =====
    public JDOMAttributeIterator(NodePointer parent, QName name) {
        this.parent = parent;
        if (parent.getNode() instanceof Element) {
            Element element = (Element) parent.getNode();
            String prefix = name.getPrefix();
            Namespace ns = null;
            if (prefix != null) {
                if (prefix.equals("xml")) {
                    ns = Namespace.XML_NAMESPACE;
                }
                else {
                    String uri = parent.getNamespaceResolver().getNamespaceURI(prefix);
                    if (uri != null) {
                        ns = Namespace.getNamespace(prefix, uri);
                    }
                    if (ns == null) {
                        // TBD: no attributes
                        attributes = Collections.EMPTY_LIST;
                        return;
                    }
                }
            }
            else {
                ns = Namespace.NO_NAMESPACE;
            }

            String lname = name.getName();
            if (!lname.equals("*")) {
                attributes = new ArrayList();
                Attribute attr = element.getAttribute(lname, ns);
                if (attr != null) {
                    attributes.add(attr);
                }
            }
            else {
                attributes = new ArrayList();
                List allAttributes = element.getAttributes();
                for (int i = 0; i < allAttributes.size(); i++) {
                    Attribute attr = (Attribute) allAttributes.get(i);
                    if (ns == Namespace.NO_NAMESPACE
                            || attr.getNamespace().equals(ns)) {
                        attributes.add(attr);
                    }
                }
            }
        }
    }
