public JDOMAttributeIterator(NodePointer parent, QName name) {
        this.parent = parent;
        if (parent.getNode() instanceof Element) {
            Element element = (Element) parent.getNode();
            String prefix = name.getPrefix();
            Namespace ns = null;
            String nsURI = null;
            if (prefix != null) {
                if (prefix.equals("xml")) {
                    ns = Namespace.XML_NAMESPACE;
                    nsURI = ns.getURI();
                }
                else {
                    nsURI = parent.getNamespaceURI(prefix);
                    if (nsURI == null) {
                        // No such namespace in the context
                        attributes = Collections.EMPTY_LIST;
                        return;
                    }
                    ns = Namespace.getNamespace(prefix, nsURI);
                }
            }
            else {
                ns = Namespace.NO_NAMESPACE;
                nsURI = ns.getURI();
            }

            String lname = name.getName();
            attributes = new ArrayList();
            if (!lname.equals("*")) {
                List allAttributes = element.getAttributes();
                for (int i = 0; i < allAttributes.size(); i++) {
                    Attribute attr = (Attribute) allAttributes.get(i);
                    if (attr.getName().equals(lname)) {
                        String aURI = attr.getNamespaceURI();
                        if ((nsURI == null && aURI == null) || (nsURI != null && nsURI.equals(aURI))) {
                            attributes.add(attr);
                        }
                    }
                }
            }
            else {
                List allAttributes = element.getAttributes();
                for (int i = 0; i < allAttributes.size(); i++) {
                    Attribute attr = (Attribute) allAttributes.get(i);
                    String aURI = attr.getNamespaceURI();
                    if ((nsURI == null && aURI == null) || (nsURI != null && nsURI.equals(aURI))) {
                        attributes.add(attr);
                    }
                }
            }
        }
    }