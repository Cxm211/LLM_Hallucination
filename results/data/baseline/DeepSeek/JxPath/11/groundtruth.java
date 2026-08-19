// ===== FIXED org.apache.commons.jxpath.ri.model.dom.DOMAttributeIterator :: getAttribute(Element, QName) [lines 107-136] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-11-fixed/src/java/org/apache/commons/jxpath/ri/model/dom/DOMAttributeIterator.java =====
    private Attr getAttribute(Element element, QName name) {
        String testPrefix = name.getPrefix();
        String testNS = null;

        if (testPrefix != null) {
            NamespaceResolver nsr = parent.getNamespaceResolver();
            testNS = nsr == null ? null : nsr.getNamespaceURI(testPrefix);
            testNS = testNS == null ? parent.getNamespaceURI(testPrefix) : testNS;
        }

        if (testNS != null) {
            Attr attr = element.getAttributeNodeNS(testNS, name.getName());
            if (attr != null) {
                return attr;
            }

            // This may mean that the parser does not support NS for
            // attributes, example - the version of Crimson bundled
            // with JDK 1.4.0
            NamedNodeMap nnm = element.getAttributes();
            for (int i = 0; i < nnm.getLength(); i++) {
                attr = (Attr) nnm.item(i);
                if (testAttr(attr, name)) {
                    return attr;
                }
            }
            return null;
        }
        return element.getAttributeNode(name.getName());
    }

// ===== FIXED org.apache.commons.jxpath.ri.model.jdom.JDOMAttributeIterator :: JDOMAttributeIterator [lines 42-95] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-11-fixed/src/java/org/apache/commons/jxpath/ri/model/jdom/JDOMAttributeIterator.java =====
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
                    NamespaceResolver nsr = parent.getNamespaceResolver();
                    if (nsr != null) {
                        String uri = nsr.getNamespaceURI(prefix);
                        if (uri != null) {
                            ns = Namespace.getNamespace(prefix, uri);
                        }
                    }
                    if (ns == null) {
                        ns = element.getNamespace(prefix);
                        if (ns == null) {
                            // TBD: no attributes
                            attributes = Collections.EMPTY_LIST;
                            return;
                        }
                    }
                }
            }
            else {
                ns = Namespace.NO_NAMESPACE;
            }

            String lname = name.getName();
            if (!lname.equals("*")) {
                attributes = new ArrayList();
                if (ns != null) {
                    Attribute attr = element.getAttribute(lname, ns);
                    if (attr != null) {
                        attributes.add(attr);
                    }
                }
            }
            else {
                attributes = new ArrayList();
                List allAttributes = element.getAttributes();
                for (int i = 0; i < allAttributes.size(); i++) {
                    Attribute attr = (Attribute) allAttributes.get(i);
                    if (attr.getNamespace().equals(ns)) {
                        attributes.add(attr);
                    }
                }
            }
        }
    }
