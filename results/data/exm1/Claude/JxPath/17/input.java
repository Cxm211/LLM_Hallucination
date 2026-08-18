// buggy code
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

            if (equalStrings(testPrefix, nodePrefix)) {
                return true;
            }
            String testNS = null;
            if (testPrefix != null) {
                testNS = parent.getNamespaceURI(testPrefix);
            }
            String nodeNS = null;
            if (nodePrefix != null) {
                nodeNS = parent.getNamespaceURI(nodePrefix);
            }
            return equalStrings(testNS, nodeNS);
        }
        return false;
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

// relevant test
// org.apache.commons.jxpath.ri.axes.SimplePathInterpreterTest::testDoStepNoPredicatesPropertyOwner
    public void testDoStepNoPredicatesPropertyOwner() {
        
        assertValueAndPointer("/int",
                new Integer(1),
                "/int",
                "Bb",
                "BbB");

        
        assertValueAndPointer("/./int",
                new Integer(1),
                "/int",
                "Bb",
                "BbB");

        
        assertNullPointer("/foo",
                "/foo",
                "Bn");

        
        assertValueAndPointer("/nestedBean/int",
                new Integer(1),
                "/nestedBean/int",
                "BbBb",
                "BbBbB");

        
        assertValueAndPointer("/nestedBean/strings",
                bean.getNestedBean().getStrings(),
                "/nestedBean/strings",
                "BbBb",
                "BbBbC");

        
        assertNullPointer("/nestedBean/foo",
                "/nestedBean/foo",
                "BbBn");

        
        assertNullPointer("/map/foo",
                "/map[@name='foo']",
                "BbDd");

        
        assertValueAndPointer("/list/int",
                new Integer(1),
                "/list[3]/int",
                "BbBb",
                "BbBbB");

        
        assertNullPointer("/list/foo",
                "/list[1]/foo",
                "BbBn");

        
        assertNullPointer("/nestedBean/foo/bar",
                "/nestedBean/foo/bar",
                "BbBnNn");

        
        assertNullPointer("/list/int/bar",
                "/list[3]/int/bar",
                "BbBbBn");

        
        assertNullPointer("/list/foo/bar",
                "/list[1]/foo/bar",
                "BbBnNn");

        
        assertNullPointer("/map/foo/bar",
                "/map[@name='foo']/bar",
                "BbDdNn");

        
        assertValueAndPointer("/map/Key1",
                "Value 1",
                "/map[@name='Key1']",
                "BbDd",
                "BbDdB");

        
        assertValueAndPointer("/integers",
                bean.getIntegers(),
                "/integers",
                "Bb",
                "BbC");
    }

// org.apache.commons.jxpath.ri.axes.SimplePathInterpreterTest::testDoStepNoPredicatesStandard
    public void testDoStepNoPredicatesStandard() {
        
        assertValueAndPointer("/vendor/location/address/city",
                "Fruit Market",
                "/vendor/location[2]/address[1]/city[1]",
                "BbMMMM");

        
        assertNullPointer("/vendor/location/address/pity",
                "/vendor/location[1]/address[1]/pity",
                "BbMMMn");

        
        assertNullPointer("/vendor/location/address/itty/bitty",
                "/vendor/location[1]/address[1]/itty/bitty",
                "BbMMMnNn");

        
        assertNullPointer("/vendor/location/address/city/pretty",
                "/vendor/location[2]/address[1]/city[1]/pretty",
                "BbMMMMn");
    }

// org.apache.commons.jxpath.ri.axes.SimplePathInterpreterTest::testDoStepPredicatesPropertyOwner
    public void testDoStepPredicatesPropertyOwner() {
        
        assertNullPointer("/foo[@name='foo']",
                "/foo[@name='foo']",
                "BnNn");

        
        assertNullPointer("/foo[3]",
                "/foo[3]",
                "Bn");
    }

// org.apache.commons.jxpath.ri.axes.SimplePathInterpreterTest::testDoStepPredicatesStandard
    public void testDoStepPredicatesStandard() {
        
        
        assertValueAndPointer("/vendor/contact[@name='jack']",
                "Jack",
                "/vendor/contact[2]",
                "BbMM");

        
        assertValueAndPointer("/vendor/contact[2]",
                "Jack",
                "/vendor/contact[2]",
                "BbMM");

        
        assertNullPointer("/vendor/contact[5]",
                "/vendor/contact[5]",
                "BbMn");

        
        assertValueAndPointer("/vendor/contact[@name='jack'][2]",
                "Jack Black",
                "/vendor/contact[4]",
                "BbMM");

        
        assertValueAndPointer("/vendor/contact[@name='jack'][2]",
                "Jack Black",
                "/vendor/contact[4]",
                "BbMM");
    }

// org.apache.commons.jxpath.ri.axes.SimplePathInterpreterTest::testDoPredicateName
    public void testDoPredicateName() {
        
        assertValueAndPointer("/nestedBean[@name='int']",
                new Integer(1),
                "/nestedBean/int",
                "BbBb",
                "BbBbB");

        
        assertValueAndPointer("/.[@name='int']",
                new Integer(1),
                "/int",
                "Bb",
                "BbB");

        
        assertValueAndPointer("/map[@name='Key1']",
                "Value 1",
                "/map[@name='Key1']",
                "BbDd",
                "BbDdB");

        
        assertValueAndPointer("/nestedBean[@name='strings']",
                bean.getNestedBean().getStrings(),
                "/nestedBean/strings",
                "BbBb",
                "BbBbC");

        
        assertNullPointer("/nestedBean[@name='foo']",
                "/nestedBean[@name='foo']",
                "BbBn");

        
        assertValueAndPointer("/map[@name='Key3']",
                bean.getMap().get("Key3"),
                "/map[@name='Key3']",
                "BbDd",
                "BbDdC");
                
        
        assertNullPointer("/map[@name='foo']",
                "/map[@name='foo']",
                "BbDd");

        
        assertValueAndPointer("/list[@name='fruitco']",
                context.getValue("/vendor"),
                "/list[5]",
                "BbCM");

        
        assertValueAndPointer("/map/Key3[@name='key']/name",
                "Name 9",
                "/map[@name='Key3'][4][@name='key']/name",
                "BbDdCDdBb",
                "BbDdCDdBbB");

        
        assertValueAndPointer("map/Key3[@name='fruitco']",
                context.getValue("/vendor"),
                "/map[@name='Key3'][3]",
                "BbDdCM");

        
        assertValueAndPointer("/vendor[@name='fruitco']",
                context.getValue("/vendor"),
                "/vendor",
                "BbM");

        
        assertNullPointer("/vendor[@name='foo']",
                "/vendor[@name='foo']",
                "BbMn");

        assertNullPointer("/vendor[@name='foo'][3]",
                "/vendor[@name='foo'][3]",
                "BbMn");

        
        assertNullPointer("/nestedBean[@name='foo']/bar",
                "/nestedBean[@name='foo']/bar",
                "BbBnNn");

        
        assertNullPointer("/map[@name='foo']/bar",
                "/map[@name='foo']/bar",
                "BbDdNn");

        
        assertNullPointer("/vendor[@name='foo']/bar",
                "/vendor[@name='foo']/bar",
                "BbMnNn");

        
        assertNullPointer("/vendor[@name='foo'][3]/bar",
                "/vendor[@name='foo'][3]/bar",
                "BbMnNn");

        
        assertValueAndPointer("/map[@name='Key2'][@name='name']",
                "Name 6",
                "/map[@name='Key2']/name",
                "BbDdBb",
                "BbDdBbB");

        
        assertValueAndPointer("/map[@name='Key2'][@name='strings'][2]",
                "String 2",
                "/map[@name='Key2']/strings[2]",
                "BbDdBb",
                "BbDdBbB");

        
        assertValueAndPointer("map[@name='Key5'][@name='key']/name",
                "Name 9",
                "/map[@name='Key5'][@name='key']/name",
                "BbDdDdBb",
                "BbDdDdBbB");

        assertNullPointer("map[@name='Key2'][@name='foo']",
                "/map[@name='Key2'][@name='foo']",
                "BbDdBn");

        assertNullPointer("map[@name='Key2'][@name='foo'][@name='bar']",
                "/map[@name='Key2'][@name='foo'][@name='bar']",
                "BbDdBnNn");

        
        assertValueAndPointer("map[@name='Key4'][@name='fruitco']",
                context.getValue("/vendor"),
                "/map[@name='Key4']",
                "BbDdM");
    }

// org.apache.commons.jxpath.ri.axes.SimplePathInterpreterTest::testDoPredicatesStandard
    public void testDoPredicatesStandard() {
        
        assertValueAndPointer("map[@name='Key3'][@name='fruitco']",
                context.getValue("/vendor"),
                "/map[@name='Key3'][3]",
                "BbDdCM");

        
        assertNullPointer("map[@name='Key3'][@name='foo']",
                "/map[@name='Key3'][4][@name='foo']",
                "BbDdCDd");

        
        assertValueAndPointer("map[@name='Key4'][@name='fruitco']",
                context.getValue("/vendor"),
                "/map[@name='Key4']",
                "BbDdM");

        
        assertNullPointer("map[@name='Key6'][@name='fruitco']",
                "/map[@name='Key6'][@name='fruitco']",
                "BbDdCn");

        
        assertValueAndPointer("/vendor/contact[@name='jack'][2]",
                "Jack Black",
                "/vendor/contact[4]",
                "BbMM");

        
        assertNullPointer("/vendor/contact[@name='jack'][5]",
                "/vendor/contact[@name='jack'][5]",
                "BbMnNn");

        
        assertValueAndPointer("/vendor/contact/.[@name='jack']",
                "Jack",
                "/vendor/contact[2]",
                "BbMM");
    }

// org.apache.commons.jxpath.ri.axes.SimplePathInterpreterTest::testDoPredicateIndex
    public void testDoPredicateIndex() {
        
        assertValueAndPointer("/map[@name='Key2'][@name='strings'][2]",
                "String 2",
                "/map[@name='Key2']/strings[2]",
                "BbDdBb",
                "BbDdBbB");

        
        assertValueAndPointer("/nestedBean[@name='strings'][2]",
                bean.getNestedBean().getStrings()[1],
                "/nestedBean/strings[2]",
                "BbBb",
                "BbBbB");

        
        assertNullPointer("/nestedBean[@name='foo'][3]",
                "/nestedBean[@name='foo'][3]",
                "BbBn");

        
        assertNullPointer("/nestedBean[@name='strings'][5]",
                "/nestedBean/strings[5]",
                "BbBbE");

        
        assertValueAndPointer("/map[@name='Key3'][2]",
                new Integer(2),
                "/map[@name='Key3'][2]",
                "BbDd",
                "BbDdB");

        
        assertNullPointer("/map[@name='Key3'][5]",
                "/map[@name='Key3'][5]",
                "BbDdE");

        
        assertNullPointer("/map[@name='Key3'][5]/foo",
                "/map[@name='Key3'][5]/foo",
                "BbDdENn");

        
        assertValueAndPointer("/map[@name='Key5'][@name='strings'][2]",
                "String 2",
                "/map[@name='Key5'][@name='strings'][2]",
                "BbDdDd",
                "BbDdDdB");

        
        assertNullPointer("/map[@name='Key5'][@name='strings'][5]",
                "/map[@name='Key5'][@name='strings'][5]",
                "BbDdDdE");

        
        assertValueAndPointer("/map[@name='Key3'][2]",
                new Integer(2),
                "/map[@name='Key3'][2]",
                "BbDd",
                "BbDdB");

        
        assertValueAndPointer("/map[@name='Key3'][1]/name",
                "some",
                "/map[@name='Key3'][1]/name",
                "BbDdBb",
                "BbDdBbB");

        
        assertNullPointer("/map[@name='foo'][3]",
                "/map[@name='foo'][3]",
                "BbDdE");

        
        assertValueAndPointer("/integers[2]",
                new Integer(2),
                "/integers[2]",
                "Bb",
                "BbB");

        
        assertValueAndPointer("/nestedBean/strings[2]",
                bean.getNestedBean().getStrings()[1],
                "/nestedBean/strings[2]",
                "BbBb",
                "BbBbB");

        
        assertValueAndPointer("/list[3]/int",
                new Integer(1),
                "/list[3]/int",
                "BbBb",
                "BbBbB");

        
        assertNullPointer("/list[6]",
                "/list[6]",
                "BbE");

        
        assertNullPointer("/nestedBean/foo[3]",
                "/nestedBean/foo[3]",
                "BbBn");

        
        assertNullPointer("/map/foo[3]",
                "/map[@name='foo'][3]",
                "BbDdE");

        
        assertNullPointer("/nestedBean/strings[5]",
                "/nestedBean/strings[5]",
                "BbBbE");

        
        assertNullPointer("/map/Key3[5]/foo",
                "/map[@name='Key3'][5]/foo",
                "BbDdENn");

        
        assertValueAndPointer("/map[@name='Key5']/strings[2]",
                "String 2",
                "/map[@name='Key5'][@name='strings'][2]",
                "BbDdDd",
                "BbDdDdB");

        
        assertNullPointer("/map[@name='Key5']/strings[5]",
                "/map[@name='Key5'][@name='strings'][5]",
                "BbDdDdE");

        
        assertValueAndPointer("/int[1]",
                new Integer(1),
                "/int",
                "Bb",
                "BbB");

        
        assertValueAndPointer(".[1]/int",
                new Integer(1),
                "/int",
                "Bb",
                "BbB");
    }

// org.apache.commons.jxpath.ri.axes.SimplePathInterpreterTest::testInterpretExpressionPath
    public void testInterpretExpressionPath() {
        context.getVariables().declareVariable("array", new String[]{"Value1"});
        context.getVariables().declareVariable("testnull", new TestNull());

        assertNullPointer("$testnull/nothing[2]",
                "$testnull/nothing[2]",
                "VBbE");
    }

// org.apache.commons.jxpath.ri.model.ExternalXMLNamespaceTest::testAttributeDOM
    public void testAttributeDOM() {
        doTestAttribute(DocumentContainer.MODEL_DOM);
    }

// org.apache.commons.jxpath.ri.model.ExternalXMLNamespaceTest::testElementDOM
    public void testElementDOM() {
        doTestElement(DocumentContainer.MODEL_DOM);
    }

// org.apache.commons.jxpath.ri.model.ExternalXMLNamespaceTest::testCreateAndSetAttributeDOM
    public void testCreateAndSetAttributeDOM() {
        doTestCreateAndSetAttribute(DocumentContainer.MODEL_DOM);
    }

// org.apache.commons.jxpath.ri.model.XMLPreserveSpaceTest::testUnspecifiedDOM
    public void testUnspecifiedDOM() {
        doTest("unspecified", DocumentContainer.MODEL_DOM, " foo ");
    }

// org.apache.commons.jxpath.ri.model.XMLPreserveSpaceTest::testDefaultDOM
    public void testDefaultDOM() {
        doTest("default", DocumentContainer.MODEL_DOM, "foo");
    }

// org.apache.commons.jxpath.ri.model.XMLPreserveSpaceTest::testPreserveDOM
    public void testPreserveDOM() {
        doTest("preserve", DocumentContainer.MODEL_DOM, " foo ");
    }

// org.apache.commons.jxpath.ri.model.XMLPreserveSpaceTest::testNestedDOM
    public void testNestedDOM() {
        doTest("nested", DocumentContainer.MODEL_DOM, " foo ;bar; baz ");
    }

// org.apache.commons.jxpath.ri.model.XMLPreserveSpaceTest::testNestedWithCommentsDOM
    public void testNestedWithCommentsDOM() {
        doTest("nested-with-comments", DocumentContainer.MODEL_DOM, " foo ;bar; baz ");
    }

// org.apache.commons.jxpath.ri.model.XMLPreserveSpaceTest::testUnspecifiedJDOM
    public void testUnspecifiedJDOM() {
        doTest("unspecified", DocumentContainer.MODEL_JDOM, " foo ");
    }

// org.apache.commons.jxpath.ri.model.XMLPreserveSpaceTest::testDefaultJDOM
    public void testDefaultJDOM() {
        doTest("default", DocumentContainer.MODEL_JDOM, "foo");
    }

// org.apache.commons.jxpath.ri.model.XMLPreserveSpaceTest::testPreserveJDOM
    public void testPreserveJDOM() {
        doTest("preserve", DocumentContainer.MODEL_JDOM, " foo ");
    }

// org.apache.commons.jxpath.ri.model.XMLPreserveSpaceTest::testNestedJDOM
    public void testNestedJDOM() {
        doTest("nested", DocumentContainer.MODEL_JDOM, " foo ;bar; baz ");
    }

// org.apache.commons.jxpath.ri.model.XMLPreserveSpaceTest::testNestedWithCommentsJDOM
    public void testNestedWithCommentsJDOM() {
        doTest("nested-with-comments", DocumentContainer.MODEL_JDOM, " foo ;bar; baz ");
    }

// org.apache.commons.jxpath.ri.model.XMLSpaceTest::testUnspecifiedDOM
    public void testUnspecifiedDOM() {
        doTest("unspecified", DocumentContainer.MODEL_DOM, "foo");
    }

// org.apache.commons.jxpath.ri.model.XMLSpaceTest::testDefaultDOM
    public void testDefaultDOM() {
        doTest("default", DocumentContainer.MODEL_DOM, "foo");
    }

// org.apache.commons.jxpath.ri.model.XMLSpaceTest::testPreserveDOM
    public void testPreserveDOM() {
        doTest("preserve", DocumentContainer.MODEL_DOM, " foo ");
    }

// org.apache.commons.jxpath.ri.model.XMLSpaceTest::testNestedDOM
    public void testNestedDOM() {
        doTest("nested", DocumentContainer.MODEL_DOM, "foo;bar; baz ");
    }

// org.apache.commons.jxpath.ri.model.XMLSpaceTest::testNestedWithCommentsDOM
    public void testNestedWithCommentsDOM() {
        doTest("nested-with-comments", DocumentContainer.MODEL_DOM, "foo;bar; baz ");
    }

// org.apache.commons.jxpath.ri.model.XMLSpaceTest::testUnspecifiedJDOM
    public void testUnspecifiedJDOM() {
        doTest("unspecified", DocumentContainer.MODEL_JDOM, "foo");
    }

// org.apache.commons.jxpath.ri.model.XMLSpaceTest::testDefaultJDOM
    public void testDefaultJDOM() {
        doTest("default", DocumentContainer.MODEL_JDOM, "foo");
    }

// org.apache.commons.jxpath.ri.model.XMLSpaceTest::testPreserveJDOM
    public void testPreserveJDOM() {
        doTest("preserve", DocumentContainer.MODEL_JDOM, " foo ");
    }

// org.apache.commons.jxpath.ri.model.XMLSpaceTest::testNestedJDOM
    public void testNestedJDOM() {
        doTest("nested", DocumentContainer.MODEL_JDOM, "foo;bar; baz ");
    }

// org.apache.commons.jxpath.ri.model.XMLSpaceTest::testNestedWithCommentsJDOM
    public void testNestedWithCommentsJDOM() {
        doTest("nested-with-comments", DocumentContainer.MODEL_JDOM, "foo;bar; baz ");
    }

// org.apache.commons.jxpath.ri.model.dom.DOMModelTest::testGetNode
    public void testGetNode() {
        assertXPathNodeType(context, "/", Document.class);
        assertXPathNodeType(context, "/vendor/location", Element.class);
        assertXPathNodeType(context, "//location/@name", Attr.class);
        assertXPathNodeType(context, "//vendor", Element.class);
    }

// org.apache.commons.jxpath.ri.model.jdom.JDOMModelTest::testGetNode
    public void testGetNode() {
        assertXPathNodeType(context, "/", Document.class);
        assertXPathNodeType(context, "/vendor/location", Element.class);
        assertXPathNodeType(context, "//location/@name", Attribute.class);
        assertXPathNodeType(context, "//vendor", Element.class); 
    }

// org.apache.commons.jxpath.ri.model.jdom.JDOMModelTest::testID
    public void testID() {
        
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testDocumentOrder
    public void testDocumentOrder() {
        assertDocumentOrder(
            context,
            "vendor/location",
            "vendor/location/address/street",
            -1);

        assertDocumentOrder(
            context,
            "vendor/location[@id = '100']",
            "vendor/location[@id = '101']",
            -1);

        assertDocumentOrder(
            context,
            "vendor//price:amount",
            "vendor/location",
            1);
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testSetValue
    public void testSetValue() {
        assertXPathSetValue(
            context,
            "vendor/location[@id = '100']",
            "New Text");

        assertXMLSignature(
            context,
            "vendor/location[@id = '100']",
            "<E>New Text</E>",
            false,
            false,
            true,
            false);

        assertXPathSetValue(
            context,
            "vendor/location[@id = '101']",
            "Replacement Text");

        assertXMLSignature(
            context,
            "vendor/location[@id = '101']",
            "<E>Replacement Text</E>",
            false,
            false,
            true,
            false);
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testCreatePath
    public void testCreatePath() {
        
        assertXPathCreatePath(
            context,
            "/vendor[1]/location[3]",
            "",
            "/vendor[1]/location[3]");

        
        assertXPathCreatePath(
            context,
            "/vendor[1]/location[3]/address/street",
            "",
            "/vendor[1]/location[3]/address[1]/street[1]");

        
        assertXPathCreatePath(
            context,
            "/vendor[1]/location[2]/@manager",
            "",
            "/vendor[1]/location[2]/@manager");

        assertXPathCreatePath(
            context,
            "/vendor[1]/location[1]/@name",
            "local",
            "/vendor[1]/location[1]/@name");

         assertXPathCreatePathAndSetValue(
            context,
            "/vendor[1]/location[4]/@manager",
            "",
            "/vendor[1]/location[4]/@manager");
         
         context.registerNamespace("price", "priceNS");
         
         
         assertXPathCreatePath(
             context,
             "/vendor[1]/price:foo/price:bar",
             "",
             "/vendor[1]/price:foo[1]/price:bar[1]");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testCreatePathAndSetValue
    public void testCreatePathAndSetValue() {
        
        assertXPathCreatePathAndSetValue(
            context,
            "vendor/location[3]",
            "",
            "/vendor[1]/location[3]");

        
        assertXPathCreatePathAndSetValue(
            context,
            "vendor/location[3]/address/street",
            "Lemon Circle",
            "/vendor[1]/location[3]/address[1]/street[1]");

        
        assertXPathCreatePathAndSetValue(
            context,
            "vendor/location[2]/@manager",
            "John Doe",
            "/vendor[1]/location[2]/@manager");

        assertXPathCreatePathAndSetValue(
            context,
            "vendor/location[1]/@manager",
            "John Doe",
            "/vendor[1]/location[1]/@manager");
        
        assertXPathCreatePathAndSetValue(
            context,
            "/vendor[1]/location[4]/@manager",
            "James Dow",
            "/vendor[1]/location[4]/@manager");
        
        assertXPathCreatePathAndSetValue(
            context,
            "vendor/product/product:name/attribute::price:language",
            "English",
            "/vendor[1]/product[1]/product:name[1]/@price:language");
        
        context.registerNamespace("price", "priceNS");
        
        
        assertXPathCreatePathAndSetValue(
            context,
            "/vendor[1]/price:foo/price:bar",
            "123.20",
            "/vendor[1]/price:foo[1]/price:bar[1]");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testRemovePath
    public void testRemovePath() {
        
        context.removePath("vendor/location[@id = '101']//street/text()");
        assertEquals(
            "Remove DOM text",
            "",
            context.getValue("vendor/location[@id = '101']//street"));

        context.removePath("vendor/location[@id = '101']//street");
        assertEquals(
            "Remove DOM element",
            new Double(0),
            context.getValue("count(vendor/location[@id = '101']//street)"));

        context.removePath("vendor/location[@id = '100']/@name");
        assertEquals(
            "Remove DOM attribute",
            new Double(0),
            context.getValue("count(vendor/location[@id = '100']/@name)"));
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testID
    public void testID() {
        context.setIdentityManager(new IdentityManager() {
            public Pointer getPointerByID(JXPathContext context, String id) {
                NodePointer ptr = (NodePointer) context.getPointer("/");
                ptr = ptr.getValuePointer(); 
                return ptr.getPointerByID(context, id);
            }
        });

        assertXPathValueAndPointer(
            context,
            "id(101)//street",
            "Tangerine Drive",
            "id('101')/address[1]/street[1]");

        assertXPathPointerLenient(
            context,
            "id(105)/address/street",
            "id(105)/address/street");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testAxisChild
    public void testAxisChild() {
        assertXPathValue(
            context,
            "vendor/location/address/street",
            "Orchard Road");

        
        assertXPathValue(
            context,
            "vendor/location/address/city",
            "Fruit Market");
        
        
        assertXPathValue(
            context,
            "local-name(vendor/product/price:amount)",
            "amount");
        
        
        assertXPathValue(context, "local-name(vendor/location)", "location");

        
        assertXPathValue(
            context,
            "name(vendor/product/price:amount)",
            "value:amount");

        
        assertXPathValue(
            context,
            "name(vendor/location)",
            "location");

        
        assertXPathValue(
            context,
            "namespace-uri(vendor/product/price:amount)",
            "priceNS");

        
        assertXPathValue(context, "vendor/product/prix", "934.99");
        
        assertXPathValue(context, "/vendor/contact[@name='jim']", "Jim");
        
        boolean nsv = false;
        try {
            context.setLenient(false);
            context.getValue("/vendor/contact[@name='jane']");
        }
        catch (JXPathException ex) {
            nsv = true;
        }
        assertTrue("No such value: /vendor/contact[@name='jim']", nsv);
                
        nsv = false;
        try {
            context.setLenient(false);
            context.getValue("/vendor/contact[@name='jane']/*");
        }
        catch (JXPathException ex) {
            nsv = true;
        }
        assertTrue("No such value: /vendor/contact[@name='jane']/*", nsv);
        
        
        assertXPathValue(
            context,
            "count(vendor/product/price:*)",
            new Double(2));

        
        assertXPathValue(context, "count(vendor/product/*)", new Double(4));

        
        assertXPathValue(context, "vendor/product/price:amount", "45.95");
        
        
        context.registerNamespace("x", "temp");
        assertXPathValue(context, "vendor/x:pos//number", "109");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testAxisChildIndexPredicate
    public void testAxisChildIndexPredicate() {
        assertXPathValue(
            context,
            "vendor/location[2]/address/street",
            "Tangerine Drive");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testAxisDescendant
    public void testAxisDescendant() {
        
        assertXPathValue(context, "//street", "Orchard Road");

        
        assertXPathValue(context, "count(//price:*)", new Double(2));

        assertXPathValueIterator(context, "vendor//saleEnds", list("never"));

        assertXPathValueIterator(context, "vendor//promotion", list(""));

        assertXPathValueIterator(
            context,
            "vendor//saleEnds[../@stores = 'all']",
            list("never"));

        assertXPathValueIterator(
            context,
            "vendor//promotion[../@stores = 'all']",
            list(""));
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testAxisParent
    public void testAxisParent() {
        
        assertXPathPointer(
            context,
            "//street/..",
            "/vendor[1]/location[1]/address[1]");

        
        assertXPathPointerIterator(
            context,
            "//street/..",
            list(
                "/vendor[1]/location[2]/address[1]",
                "/vendor[1]/location[1]/address[1]"));

        
        assertXPathValue(
            context,
            "vendor/product/price:sale/saleEnds/parent::price:*" + "/saleEnds",
            "never");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testAxisFollowingSibling
    public void testAxisFollowingSibling() {
        
        assertXPathValue(
            context,
            "vendor/location[.//employeeCount = 10]/"
                + "following-sibling::location//street",
            "Tangerine Drive");

        
        assertXPathPointer(
            context,
            "vendor/location[.//employeeCount = 10]/"
                + "following-sibling::location//street",
            "/vendor[1]/location[2]/address[1]/street[1]");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testAxisPrecedingSibling
    public void testAxisPrecedingSibling() {
        
        assertXPathPointer(
            context,
            "//location[2]/preceding-sibling::location//street",
            "/vendor[1]/location[1]/address[1]/street[1]");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testAxisPreceding
    public void testAxisPreceding() {
        
        assertXPathPointer(
                context,
                "//location[2]/preceding-sibling::location//street",
        "/vendor[1]/location[1]/address[1]/street[1]");
        assertXPathPointer(context, "//location[2]/preceding::*[1]", "/vendor[1]/location[1]/employeeCount[1]");
        assertXPathPointer(context, "//location[2]/preceding::node()[3]", "/vendor[1]/location[1]/employeeCount[1]/text()[1]");
        assertXPathPointer(context, "//location[2]/preceding::node()[4]", "/vendor[1]/location[1]/employeeCount[1]");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testAxisAttribute
    public void testAxisAttribute() {
        
        assertXPathValue(context, "vendor/location/@id", "100");

        
        assertXPathPointer(
            context,
            "vendor/location/@id",
            "/vendor[1]/location[1]/@id");

        
        assertXPathValueIterator(
            context,
            "vendor/location/@id",
            list("100", "101"));

        
        assertXPathValue(
            context,
            "vendor/product/price:amount/@price:discount",
            "10%");
        
        
        assertXPathValue(
            context,
            "namespace-uri(vendor/product/price:amount/@price:discount)",
            "priceNS");

        
        assertXPathValue(
            context,
            "local-name(vendor/product/price:amount/@price:discount)",
            "discount");

        
        assertXPathValue(
            context,
            "name(vendor/product/price:amount/@price:discount)",
            "price:discount");

        
        assertXPathValue(
            context,
            "vendor/product/price:amount/@discount",
            "20%");

        
        assertXPathValue(
            context,
            "namespace-uri(vendor/product/price:amount/@discount)",
            "");

        
        assertXPathValue(
            context,
            "local-name(vendor/product/price:amount/@discount)",
            "discount");

        
        assertXPathValue(
            context,
            "name(vendor/product/price:amount/@discount)",
            "discount");

        
        assertXPathValueIterator(
            context,
            "vendor/product/price:amount/@price:*",
            list("10%"));

        
        assertXPathValueIterator(
            context,
            "vendor/location[1]/@*",
            set("100", "", "local"));

        
        assertXPathValueIterator(
                context,
                "vendor/product/price:amount/@*",
                
                set("10%", "20%"));

        
        assertXPathValueIterator(
            context,
            "vendor/product/price:amount/@*[namespace-uri() = '']",
            list("20%"));

        
        assertXPathValue(context, "vendor/location/@manager", "");

        
        assertXPathValueLenient(context, "vendor/location/@missing", null);

        
        assertXPathValueLenient(context, "vendor/location/@miss:missing", null);

        
        assertXPathValue(
            context,
            "vendor/location[@id='101']//street",
            "Tangerine Drive");
        
        assertXPathValueIterator(
            context,
            "/vendor/location[1]/@*[name()!= 'manager']", list("100",
            "local"));
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testAxisNamespace
    public void testAxisNamespace() {
        
        assertXPathValueAndPointer(
            context,
            "vendor/product/prix/namespace::price",
            "priceNS",
            "/vendor[1]/product[1]/prix[1]/namespace::price");

        
        assertXPathValue(
            context,
            "count(vendor/product/namespace::*)",
            new Double(3));

        
        assertXPathValue(
            context,
            "name(vendor/product/prix/namespace::price)",
            "price");

        
        assertXPathValue(
            context,
            "local-name(vendor/product/prix/namespace::price)",
            "price");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testAxisAncestor
    public void testAxisAncestor() {
        
        assertXPathValue(
            context,
            "vendor/product/price:sale/saleEnds/"
                + "ancestor::price:sale/saleEnds",
            "never");

        
        assertXPathValue(
            context,
            "vendor/product/price:sale/saleEnds/ancestor::price:*"
                + "/saleEnds",
            "never");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testAxisAncestorOrSelf
    public void testAxisAncestorOrSelf() {
        
        assertXPathValue(
            context,
            "vendor/product/price:sale/"
                + "ancestor-or-self::price:sale/saleEnds",
            "never");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testAxisFollowing
    public void testAxisFollowing() {
        assertXPathValueIterator(
            context,
            "vendor/contact/following::location//street",
            list("Orchard Road", "Tangerine Drive"));

        
        assertXPathValue(
            context,
            "//location/following::price:sale/saleEnds",
            "never");
        assertXPathPointer(context, "//location[2]/following::node()[2]", "/vendor[1]/product[1]");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testAxisSelf
    public void testAxisSelf() {
        
        assertXPathValue(
            context,
            "//price:sale/self::price:sale/saleEnds",
            "never");

        
        assertXPathValueLenient(context, "//price:sale/self::x/saleEnds", null);
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testNodeTypeComment
    public void testNodeTypeComment() {
        
        assertXPathValue(
            context,
            "//product/comment()",
            "We are not buying this product, ever");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testNodeTypeText
    public void testNodeTypeText() {
        
        
        assertXPathValue(
            context,
            "//product/text()[. != '']",
            "We love this product.");

        
        assertXPathPointer(
            context,
            "//product/text()",
            "/vendor[1]/product[1]/text()[1]");

    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testNodeTypeProcessingInstruction
    public void testNodeTypeProcessingInstruction() {
        
        assertXPathValue(
            context,
            "//product/processing-instruction()",
            "do not show anybody");

        
        assertXPathValue(
            context,
            "//product/processing-instruction('report')",
            "average only");

        
        assertXPathPointer(
            context,
            "//product/processing-instruction('report')",
            "/vendor[1]/product[1]/processing-instruction('report')[1]");

        
        assertXPathValue(
            context,
            "name(//product/processing-instruction()[1])",
            "security");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testLang
    public void testLang() {
        
        assertXPathValue(context, "//product/prix/@xml:lang", "fr");

        
        assertXPathValue(context, "//product/prix[lang('fr')]", "934.99");

        
        assertXPathValue(
            context,
            "//product/price:sale[lang('en')]/saleEnds",
            "never");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testDocument
    public void testDocument() {
        assertXPathValue(
            context,
            "$document/vendor/location[1]//street",
            "Orchard Road");

        assertXPathPointer(
            context,
            "$document/vendor/location[1]//street",
            "$document/vendor[1]/location[1]/address[1]/street[1]");

        assertXPathValue(context, "$document/vendor//street", "Orchard Road");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testContainer
    public void testContainer() {
        assertXPathValue(context, "$container/vendor//street", "Orchard Road");

        assertXPathValue(context, "$container//street", "Orchard Road");

        assertXPathPointer(
            context,
            "$container//street",
            "$container/vendor[1]/location[1]/address[1]/street[1]");

        
        assertXPathValue(
            context,
            "number(vendor/location/employeeCount)",
            new Double(10));
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testElementInVariable
    public void testElementInVariable() {
        assertXPathValue(context, "$element", "Orchard Road");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testTypeConversions
    public void testTypeConversions() {
        
        assertXPathValue(
            context,
            "vendor/location/employeeCount + 1",
            new Double(11));

        
        assertXPathValue(
            context,
            "vendor/location/employeeCount and true()",
            Boolean.TRUE);
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testBooleanFunction
    public void testBooleanFunction() {
        assertXPathValue(
            context,
            "boolean(vendor//saleEnds[../@stores = 'all'])",
            Boolean.TRUE);

        assertXPathValue(
            context,
            "boolean(vendor//promotion[../@stores = 'all'])",
            Boolean.TRUE);

        assertXPathValue(
            context,
            "boolean(vendor//promotion[../@stores = 'some'])",
            Boolean.FALSE);
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testFunctionsLastAndPosition
    public void testFunctionsLastAndPosition() {
        assertXPathPointer(
                context,
                "vendor//location[last()]",
                "/vendor[1]/location[2]");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testNamespaceMapping
    public void testNamespaceMapping() {
        context.registerNamespace("rate", "priceNS");
        context.registerNamespace("goods", "productNS");

        assertEquals("Context node namespace resolution", 
                "priceNS", 
                context.getNamespaceURI("price"));        
        
        assertEquals("Registered namespace resolution", 
                "priceNS", 
                context.getNamespaceURI("rate"));

        
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

        
        assertXPathValueAndPointer(context,
                "//product:name",
                "Box of oranges",
                "/vendor[1]/product[1]/goods:name[1]");
        
        
        JXPathContext childCtx = 
            JXPathContext.newContext(context, context.getContextBean());
        assertXPathValueAndPointer(childCtx,
                "//product:name",
                "Box of oranges",
                "/vendor[1]/product[1]/goods:name[1]");
        
        
        JXPathContext relativeCtx = 
            context.getRelativeContext(context.getPointer("/vendor"));
        assertXPathValueAndPointer(relativeCtx,
                "product/product:name",
                "Box of oranges",
                "/vendor[1]/product[1]/goods:name[1]");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testUnion
    public void testUnion() {
        assertXPathValue(context, "/vendor[1]/contact[1] | /vendor[1]/contact[4]", "John");
        assertXPathValue(context, "/vendor[1]/contact[4] | /vendor[1]/contact[1]", "John");
    }

// org.apache.commons.jxpath.ri.model.XMLModelTestCase::testNodes
    public void testNodes() {
        Pointer pointer = context.getPointer("/vendor[1]/contact[1]");
        assertFalse(pointer.getNode().equals(pointer.getValue()));
    }
