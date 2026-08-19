// ===== FIXED org.apache.commons.jxpath.ri.NamespaceResolver :: getNamespaceURI(String) [lines 125-129] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-13-fixed/src/java/org/apache/commons/jxpath/ri/NamespaceResolver.java =====
    public synchronized String getNamespaceURI(String prefix) {
        String uri = getExternallyRegisteredNamespaceURI(prefix);
        return uri == null && pointer != null ? pointer.getNamespaceURI(prefix)
                : uri;
    }

// ===== FIXED org.apache.commons.jxpath.ri.NamespaceResolver :: getPrefix(String) [lines 150-154] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-13-fixed/src/java/org/apache/commons/jxpath/ri/NamespaceResolver.java =====
    public synchronized String getPrefix(String namespaceURI) {
        String prefix = getExternallyRegisteredPrefix(namespaceURI);
        return prefix == null && pointer != null ? getPrefix(pointer,
                namespaceURI) : prefix;
    }

// ===== FIXED org.apache.commons.jxpath.ri.model.dom.DOMNodePointer :: createAttribute(JXPathContext, QName) [lines 417-443] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-13-fixed/src/java/org/apache/commons/jxpath/ri/model/dom/DOMNodePointer.java =====
    public NodePointer createAttribute(JXPathContext context, QName name) {
        if (!(node instanceof Element)) {
            return super.createAttribute(context, name);
        }
        Element element = (Element) node;
        String prefix = name.getPrefix();
        if (prefix != null) {
            String ns = null;
            NamespaceResolver nsr = getNamespaceResolver();
            if (nsr != null) {
                ns = nsr.getNamespaceURI(prefix);
            }
            if (ns == null) {
                throw new JXPathException(
                    "Unknown namespace prefix: " + prefix);
            }
            element.setAttributeNS(ns, name.toString(), "");
        }
        else {
            if (!element.hasAttribute(name.getName())) {
                element.setAttribute(name.getName(), "");
            }
        }
        NodeIterator it = attributeIterator(name);
        it.setPosition(1);
        return it.getNodePointer();
    }
