    private Attr getAttribute(Element element, QName name) {
        String testPrefix = name.getPrefix();
        String testNS = null;

        if (testPrefix != null) {
            testNS = parent.getNamespaceURI(testPrefix);
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
                        ns = element.getNamespace(prefix);
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

// trigger testcase
public void testNamespaceMapping() {
        context.registerNamespace("rate", "priceNS");
        context.registerNamespace("goods", "productNS");

        assertEquals("Context node namespace resolution", 
                "priceNS", 
                context.getNamespaceURI("price"));        
        
        assertEquals("Registered namespace resolution", 
                "priceNS", 
                context.getNamespaceURI("rate"));

        // child:: with a namespace and wildcard
        assertXPathValue(context, 
                "count(vendor/product/rate:*)", 
                new Double(2));

        assertXPathValue(context,
                "vendor[1]/product[1]/rate:amount[1]/@rate:discount", "10%");
        assertXPathValue(context,
                "vendor[1]/product[1]/rate:amount[1]/@price:discount", "10%");
        assertXPathValue(context,
                "vendor[1]/product[1]/price:amount[1]/@rate:discount", "10%");
        assertXPathValue(context,
                "vendor[1]/product[1]/price:amount[1]/@price:discount", "10%");

        // Preference for externally registered namespace prefix
        assertXPathValueAndPointer(context,
                "//product:name",
                "Box of oranges",
                "/vendor[1]/product[1]/goods:name[1]");
        
        // Same, but with a child context        
        JXPathContext childCtx = 
            JXPathContext.newContext(context, context.getContextBean());
        assertXPathValueAndPointer(childCtx,
                "//product:name",
                "Box of oranges",
                "/vendor[1]/product[1]/goods:name[1]");
        
        // Same, but with a relative context        
        JXPathContext relativeCtx = 
            context.getRelativeContext(context.getPointer("/vendor"));
        assertXPathValueAndPointer(relativeCtx,
                "product/product:name",
                "Box of oranges",
                "/vendor[1]/product[1]/goods:name[1]");
    }

public void testNamespaceMapping() {
        context.registerNamespace("rate", "priceNS");
        context.registerNamespace("goods", "productNS");

        assertEquals("Context node namespace resolution", 
                "priceNS", 
                context.getNamespaceURI("price"));        
        
        assertEquals("Registered namespace resolution", 
                "priceNS", 
                context.getNamespaceURI("rate"));

        // child:: with a namespace and wildcard
        assertXPathValue(context, 
                "count(vendor/product/rate:*)", 
                new Double(2));

        assertXPathValue(context,
                "vendor[1]/product[1]/rate:amount[1]/@rate:discount", "10%");
        assertXPathValue(context,
                "vendor[1]/product[1]/rate:amount[1]/@price:discount", "10%");
        assertXPathValue(context,
                "vendor[1]/product[1]/price:amount[1]/@rate:discount", "10%");
        assertXPathValue(context,
                "vendor[1]/product[1]/price:amount[1]/@price:discount", "10%");

        // Preference for externally registered namespace prefix
        assertXPathValueAndPointer(context,
                "//product:name",
                "Box of oranges",
                "/vendor[1]/product[1]/goods:name[1]");
        
        // Same, but with a child context        
        JXPathContext childCtx = 
            JXPathContext.newContext(context, context.getContextBean());
        assertXPathValueAndPointer(childCtx,
                "//product:name",
                "Box of oranges",
                "/vendor[1]/product[1]/goods:name[1]");
        
        // Same, but with a relative context        
        JXPathContext relativeCtx = 
            context.getRelativeContext(context.getPointer("/vendor"));
        assertXPathValueAndPointer(relativeCtx,
                "product/product:name",
                "Box of oranges",
                "/vendor[1]/product[1]/goods:name[1]");
    }
