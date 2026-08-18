    public boolean nextNode() {
        super.setPosition(getCurrentPosition() + 1);
        if (!setStarted) {
            setStarted = true;
            if (!(nodeTest instanceof NodeNameTest)) {
                return false;
            }
            QName name = ((NodeNameTest) nodeTest).getNodeName();
            iterator =
                parentContext.getCurrentNodePointer().attributeIterator(name);
        }
        if (iterator == null) {
            return false;
        }
        if (!iterator.setPosition(iterator.getPosition() + 1)) {
            return false;
        }
        currentNodePointer = iterator.getNodePointer();
        return true;
    }

// trigger testcase
public void testAxisAttribute() {
        // attribute::
        assertXPathValue(context, "vendor/location/@id", "100");

        // attribute:: produces the correct pointer
        assertXPathPointer(
            context,
            "vendor/location/@id",
            "/vendor[1]/location[1]/@id");

        // iterate over attributes
        assertXPathValueIterator(
            context,
            "vendor/location/@id",
            list("100", "101"));

        // Using different prefixes for the same namespace
        assertXPathValue(
            context,
            "vendor/product/price:amount/@price:discount",
            "10%");
        
        // namespace uri for an attribute
        assertXPathValue(
            context,
            "namespace-uri(vendor/product/price:amount/@price:discount)",
            "priceNS");

        // local name of an attribute
        assertXPathValue(
            context,
            "local-name(vendor/product/price:amount/@price:discount)",
            "discount");

        // name for an attribute
        assertXPathValue(
            context,
            "name(vendor/product/price:amount/@price:discount)",
            "price:discount");

        // attribute:: with the default namespace
        assertXPathValue(
            context,
            "vendor/product/price:amount/@discount",
            "20%");

        // namespace uri of an attribute with the default namespace
        assertXPathValue(
            context,
            "namespace-uri(vendor/product/price:amount/@discount)",
            "");

        // local name of an attribute with the default namespace
        assertXPathValue(
            context,
            "local-name(vendor/product/price:amount/@discount)",
            "discount");

        // name of an attribute with the default namespace
        assertXPathValue(
            context,
            "name(vendor/product/price:amount/@discount)",
            "discount");

        // attribute:: with a namespace and wildcard
        assertXPathValueIterator(
            context,
            "vendor/product/price:amount/@price:*",
            list("10%"));

        // attribute:: with a wildcard
        assertXPathValueIterator(
            context,
            "vendor/location[1]/@*",
            set("100", "", "local"));

        // attribute:: with default namespace and wildcard
        assertXPathValueIterator(
                context,
                "vendor/product/price:amount/@*",
                //use a set because DOM returns attrs sorted by name, JDOM by occurrence order:
                set("10%", "20%"));

        // attribute::node()
        assertXPathValueIterator(
                context,
                "vendor/product/price:amount/attribute::node()",
                //use a set because DOM returns attrs sorted by name, JDOM by occurrence order:
                set("10%", "20%"));
        
        // attribute:: select non-ns'd attributes only
        assertXPathValueIterator(
            context,
            "vendor/product/price:amount/@*[namespace-uri() = '']",
            list("20%"));

        // Empty attribute
        assertXPathValue(context, "vendor/location/@manager", "");

        // Missing attribute
        assertXPathValueLenient(context, "vendor/location/@missing", null);

        // Missing attribute with namespace
        assertXPathValueLenient(context, "vendor/location/@miss:missing", null);

        // Using attribute in a predicate
        assertXPathValue(
            context,
            "vendor/location[@id='101']//street",
            "Tangerine Drive");
        
        assertXPathValueIterator(
            context,
            "/vendor/location[1]/@*[name()!= 'manager']", list("100",
            "local"));
    }

public void testAxisAttribute() {
        // attribute::
        assertXPathValue(context, "vendor/location/@id", "100");

        // attribute:: produces the correct pointer
        assertXPathPointer(
            context,
            "vendor/location/@id",
            "/vendor[1]/location[1]/@id");

        // iterate over attributes
        assertXPathValueIterator(
            context,
            "vendor/location/@id",
            list("100", "101"));

        // Using different prefixes for the same namespace
        assertXPathValue(
            context,
            "vendor/product/price:amount/@price:discount",
            "10%");
        
        // namespace uri for an attribute
        assertXPathValue(
            context,
            "namespace-uri(vendor/product/price:amount/@price:discount)",
            "priceNS");

        // local name of an attribute
        assertXPathValue(
            context,
            "local-name(vendor/product/price:amount/@price:discount)",
            "discount");

        // name for an attribute
        assertXPathValue(
            context,
            "name(vendor/product/price:amount/@price:discount)",
            "price:discount");

        // attribute:: with the default namespace
        assertXPathValue(
            context,
            "vendor/product/price:amount/@discount",
            "20%");

        // namespace uri of an attribute with the default namespace
        assertXPathValue(
            context,
            "namespace-uri(vendor/product/price:amount/@discount)",
            "");

        // local name of an attribute with the default namespace
        assertXPathValue(
            context,
            "local-name(vendor/product/price:amount/@discount)",
            "discount");

        // name of an attribute with the default namespace
        assertXPathValue(
            context,
            "name(vendor/product/price:amount/@discount)",
            "discount");

        // attribute:: with a namespace and wildcard
        assertXPathValueIterator(
            context,
            "vendor/product/price:amount/@price:*",
            list("10%"));

        // attribute:: with a wildcard
        assertXPathValueIterator(
            context,
            "vendor/location[1]/@*",
            set("100", "", "local"));

        // attribute:: with default namespace and wildcard
        assertXPathValueIterator(
                context,
                "vendor/product/price:amount/@*",
                //use a set because DOM returns attrs sorted by name, JDOM by occurrence order:
                set("10%", "20%"));

        // attribute::node()
        assertXPathValueIterator(
                context,
                "vendor/product/price:amount/attribute::node()",
                //use a set because DOM returns attrs sorted by name, JDOM by occurrence order:
                set("10%", "20%"));
        
        // attribute:: select non-ns'd attributes only
        assertXPathValueIterator(
            context,
            "vendor/product/price:amount/@*[namespace-uri() = '']",
            list("20%"));

        // Empty attribute
        assertXPathValue(context, "vendor/location/@manager", "");

        // Missing attribute
        assertXPathValueLenient(context, "vendor/location/@missing", null);

        // Missing attribute with namespace
        assertXPathValueLenient(context, "vendor/location/@miss:missing", null);

        // Using attribute in a predicate
        assertXPathValue(
            context,
            "vendor/location[@id='101']//street",
            "Tangerine Drive");
        
        assertXPathValueIterator(
            context,
            "/vendor/location[1]/@*[name()!= 'manager']", list("100",
            "local"));
    }
