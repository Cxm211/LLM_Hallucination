// buggy code
    public CoreOperationCompare(Expression arg1, Expression arg2) {

        super(new Expression[] { arg1, arg2 });
    }

    protected boolean equal(Object l, Object r) {
        if (l instanceof Pointer && r instanceof Pointer) {
            if (l.equals(r)) {
                return true;
            }
        }
        if (l instanceof Pointer) {
            l = ((Pointer) l).getValue();
        }

        if (r instanceof Pointer) {
            r = ((Pointer) r).getValue();
        }

        if (l == r) {
            return true;
        }
        if (l instanceof Boolean || r instanceof Boolean) {
            return (InfoSetUtil.booleanValue(l) == InfoSetUtil.booleanValue(r));
            }
            //if either side is NaN, no comparison returns true:
        if (l instanceof Number || r instanceof Number) {
            return (InfoSetUtil.doubleValue(l) == InfoSetUtil.doubleValue(r));
            }
            if (l instanceof String || r instanceof String) {
            return (
                InfoSetUtil.stringValue(l).equals(InfoSetUtil.stringValue(r)));
        }
        return l != null && l.equals(r);
    }

    public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        return equal(context, args[0], args[1]) ? Boolean.TRUE : Boolean.FALSE;
    }

    public CoreOperationNotEqual(Expression arg1, Expression arg2) {
        super(arg1, arg2);
    }

    public Object computeValue(org.apache.commons.jxpath.ri.EvalContext context) {
        return equal(context, args[0], args[1]) ? Boolean.FALSE : Boolean.TRUE;
    }

// relevant test
// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testConstant
    public void testConstant() {
        assertXPathExpression("1", Constant.class);
        assertXPathExpression("1.5", Constant.class);
        assertXPathExpression("'foo'", Constant.class);
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreFunction
    public void testCoreFunction() {
        assertXPathExpression("last()", CoreFunction.class);
        assertXPathExpression("position()", CoreFunction.class);
        assertXPathExpression("count(book)", CoreFunction.class);
        assertXPathExpression("id(13)", CoreFunction.class);
        assertXPathExpression("local-name()", CoreFunction.class);
        assertXPathExpression("local-name(book)", CoreFunction.class);
        assertXPathExpression("namespace-uri()", CoreFunction.class);
        assertXPathExpression("namespace-uri(book)", CoreFunction.class);
        assertXPathExpression("name()", CoreFunction.class);
        assertXPathExpression("name(book)", CoreFunction.class);
        assertXPathExpression("string(3)", CoreFunction.class);
        assertXPathExpression("concat('a', 'b')", CoreFunction.class);
        assertXPathExpression("starts-with('a', 'b')", CoreFunction.class);
        assertXPathExpression("contains('a', 'b')", CoreFunction.class);
        assertXPathExpression("substring-before('a', 1)", CoreFunction.class);
        assertXPathExpression("substring-after('a', 2)", CoreFunction.class);
        assertXPathExpression("substring('a', 2)", CoreFunction.class);
        assertXPathExpression("substring('a', 2, 3)", CoreFunction.class);
        assertXPathExpression("string-length('a')", CoreFunction.class);
        assertXPathExpression("normalize-space('a')", CoreFunction.class);
        assertXPathExpression("translate('a', 'b', 'c')", CoreFunction.class);
        assertXPathExpression("boolean('true')", CoreFunction.class);
        assertXPathExpression("not(1)", CoreFunction.class);
        assertXPathExpression("true()", CoreFunction.class);
        assertXPathExpression("false()", CoreFunction.class);
        assertXPathExpression("lang('fr')", CoreFunction.class);
        assertXPathExpression("number('12')", CoreFunction.class);
        assertXPathExpression("sum(book/price)", CoreFunction.class);
        assertXPathExpression("floor(11.4)", CoreFunction.class);
        assertXPathExpression("ceiling(11.4)", CoreFunction.class);
        assertXPathExpression("round(11.4)", CoreFunction.class);
        assertXPathExpression("key('title', 'Hobbit')", CoreFunction.class);
        assertXPathExpression("format-number(12, '##')", CoreFunction.class);
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreOperationAnd
    public void testCoreOperationAnd() {
        assertXPathExpression(
            "2 and 4",
            CoreOperationAnd.class);

        assertXPathExpression(
            "2 > 1 and 4 < 5",
            CoreOperationAnd.class);            
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreOperationOr
    public void testCoreOperationOr() {
        assertXPathExpression(
            "2 or 4",
            CoreOperationOr.class);

        assertXPathExpression(
            "2 > 1 or 4 < 5",
            CoreOperationOr.class);

        assertXPathExpression(
            "1 > 1 and 2 <= 2 or 3 = 4",
            CoreOperationOr.class);
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreOperationEqual
    public void testCoreOperationEqual() {
        assertXPathExpression(
            "2 = 4",
            CoreOperationEqual.class);

        assertXPathExpression(
            "2 + 1 = 3",
            CoreOperationEqual.class);
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreOperationNameAttributeTest
    public void testCoreOperationNameAttributeTest() {
        assertXPathExpression(
            "@name = 'bar'",
            NameAttributeTest.class);
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreOperationNotEqual
    public void testCoreOperationNotEqual() {
        assertXPathExpression(
            "2 != 4",
            CoreOperationNotEqual.class);

        assertXPathExpression(
            "2 + 1 != 3",
            CoreOperationNotEqual.class);
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreOperationLessThan
    public void testCoreOperationLessThan() {
        assertXPathExpression(
            "3<4",
            CoreOperationLessThan.class,
            "3 < 4");

        assertXPathExpression(
            "3<(2>=1)",
            CoreOperationLessThan.class,
            "3 < (2 >= 1)");
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreOperationLessThanOrEqual
    public void testCoreOperationLessThanOrEqual() {
        assertXPathExpression(
            "3<=4",
            CoreOperationLessThanOrEqual.class,
            "3 <= 4");

        assertXPathExpression(
            "3<=(2>=1)",
            CoreOperationLessThanOrEqual.class,
            "3 <= (2 >= 1)");
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreOperationGreaterThan
    public void testCoreOperationGreaterThan() {
        assertXPathExpression(
            "3>4",
            CoreOperationGreaterThan.class,
            "3 > 4");

        assertXPathExpression(
            "3>(2>=1)",
            CoreOperationGreaterThan.class,
            "3 > (2 >= 1)");

        assertXPathExpression(
            "1 > (1 and 2 <= (2 or 3) = 4)",
            CoreOperationGreaterThan.class);
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreOperationGreaterThanOrEqual
    public void testCoreOperationGreaterThanOrEqual() {
        assertXPathExpression(
            "3>=4",
            CoreOperationGreaterThanOrEqual.class,
            "3 >= 4");

        assertXPathExpression(
            "3>=(2>=1)",
            CoreOperationGreaterThanOrEqual.class,
            "3 >= (2 >= 1)");
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreOperationDivide
    public void testCoreOperationDivide() {
        assertXPathExpression(
            "2 div 4",
            CoreOperationDivide.class);

        assertXPathExpression(
            "2|3 div -3",
            CoreOperationDivide.class,
            "2 | 3 div -3");
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreOperationMod
    public void testCoreOperationMod() {
        assertXPathExpression(
            "2 mod 4",
            CoreOperationMod.class);

        assertXPathExpression(
            "2|3 mod -3",
            CoreOperationMod.class,
            "2 | 3 mod -3");
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreOperationMultiply
    public void testCoreOperationMultiply() {
        assertXPathExpression(
            "2*4",
            CoreOperationMultiply.class,
            "2 * 4");
            
        assertXPathExpression(
            "2*(3 + 1)",
            CoreOperationMultiply.class,
            "2 * (3 + 1)");
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreOperationMinus
    public void testCoreOperationMinus() {
        assertXPathExpression(
            "1 - 1",
            CoreOperationSubtract.class);
            
        assertXPathExpression(
            "1 - 1 - 2",
            CoreOperationSubtract.class);
            
        assertXPathExpression(
            "1 - (1 - 2)",
            CoreOperationSubtract.class);
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreOperationSum
    public void testCoreOperationSum() {
        assertXPathExpression(
            "3 + 1 + 4", 
            CoreOperationAdd.class);
            
        assertXPathExpression(
            "(3 + 1) + 4",
            CoreOperationAdd.class,
            "3 + 1 + 4");
            
        assertXPathExpression(
            "3 + (1 + 4)",
            CoreOperationAdd.class,
            "3 + 1 + 4");
            
        assertXPathExpression(
            "3 + -1", 
            CoreOperationAdd.class, 
            "3 + -1");
            
        assertXPathExpression(
            "2*-3 + -1",
            CoreOperationAdd.class,
            "2 * -3 + -1");
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreOperationUnaryMinus
    public void testCoreOperationUnaryMinus() {
        assertXPathExpression("-3", CoreOperationNegate.class);
        assertXPathExpression("-(3 + 1)", CoreOperationNegate.class);
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testCoreOperationUnion
    public void testCoreOperationUnion() {
        assertXPathExpression(
            "3 | 1 | 4",
            CoreOperationUnion.class);
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testExpressionPath
    public void testExpressionPath() {
        assertXPathExpression(
            "$x/foo/bar",
            ExpressionPath.class);        
        assertXPathExpression(
            "(2 + 2)/foo/bar",
            ExpressionPath.class);        
        assertXPathExpression(
            "$x[3][2 + 2]/foo/bar",
            ExpressionPath.class);        
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testExtensionFunction
    public void testExtensionFunction() {
        assertXPathExpression(
            "my:function(3, other.function())",
            ExtensionFunction.class);        
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testLocationPathAxisSelf
    public void testLocationPathAxisSelf() {
        assertXPathExpression(
            "self::foo:bar",
            LocationPath.class);
                 
        assertXPathExpression(
            ".",
            LocationPath.class);     
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testLocationPathAxisChild
    public void testLocationPathAxisChild() {
        assertXPathExpression(
            "child::foo:bar",
            LocationPath.class,
            "foo:bar");
                 
        assertXPathExpression(
            "foo:bar",
            LocationPath.class);
                 
        assertXPathExpression(
            "/foo:bar",
            LocationPath.class);
                 
        assertXPathExpression(
            "/foo/bar",
            LocationPath.class);     

        assertXPathExpression(
            "*",
            LocationPath.class);
                 
        assertXPathExpression(
            "foo:*",
            LocationPath.class);
                 
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testLocationPathAxisParent
    public void testLocationPathAxisParent() {
        assertXPathExpression(
            "parent::foo:bar",
            LocationPath.class);
                 
        assertXPathExpression(
            "..",
            LocationPath.class);     
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testLocationPathAxisAttribute
    public void testLocationPathAxisAttribute() {
        assertXPathExpression(
            "attribute::foo:bar",
            LocationPath.class,
            "@foo:bar");

        assertXPathExpression(
            "@foo:bar",
            LocationPath.class);

        assertXPathExpression(
            "../@foo:bar",
            LocationPath.class);

        assertXPathExpression(
            "@*",
            LocationPath.class);

        assertXPathExpression(
            "@*[last()]",
            LocationPath.class);
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testLocationPathAxisDescendant
    public void testLocationPathAxisDescendant() {
        assertXPathExpression(
            "descendant::foo:bar",
            LocationPath.class);
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testLocationPathAxisDescendantOrSelf
    public void testLocationPathAxisDescendantOrSelf() {
        assertXPathExpression(
            "descendant-or-self::foo:bar",
            LocationPath.class);

        assertXPathExpression(
            "//foo", 
            LocationPath.class);

        assertXPathExpression(
            "foo//bar", 
            LocationPath.class);
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testLocationPathAxisOther
    public void testLocationPathAxisOther() {
        assertXPathExpression(
            "ancestor::foo:bar",
            LocationPath.class);
            
        assertXPathExpression(
            "ancestor-or-self::foo:bar",
            LocationPath.class);
            
        assertXPathExpression(
            "namespace::foo:bar",
            LocationPath.class);

        assertXPathExpression(
            "preceding::foo:bar",
            LocationPath.class);

        assertXPathExpression(
            "preceding-sibling::foo:bar",
            LocationPath.class);

        assertXPathExpression(
            "following::foo:bar",
            LocationPath.class);

        assertXPathExpression(
            "following-sibling::foo:bar",
            LocationPath.class);
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testLocationPathNodeTest
    public void testLocationPathNodeTest() {
        assertXPathExpression(
            "node()",
            LocationPath.class);

        assertXPathExpression(
            "text()",
            LocationPath.class);

        assertXPathExpression(
            "comment()",
            LocationPath.class);

        assertXPathExpression(
            "processing-instruction()",
            LocationPath.class);

        assertXPathExpression(
            "processing-instruction('test')",
            LocationPath.class);
    }

// org.apache.commons.jxpath.ri.JXPathCompiledExpressionTest::testVariableReference
    public void testVariableReference() {
        assertXPathExpression(
            "$x",
            VariableReference.class);                

        assertXPathExpression(
            "$x:y",
            VariableReference.class);
    }

// org.apache.commons.jxpath.ri.StressTest::testThreads
    public void testThreads() throws Throwable {
        context = JXPathContext.newContext(null, new Double(100));
        Thread[] threadArray = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            threadArray[i] = new Thread(new StressRunnable());
        }
        
        for (int i = 0; i < threadArray.length; i++) {
            threadArray[i].start();
        }

        for (int i = 0; i < threadArray.length; i++) {
            try {
                threadArray[i].join();
            }
            catch (InterruptedException e) {
                assertTrue("Interrupted", false);
            }
        }

        if (exception != null) {
            throw exception;
        }
        assertEquals("Test count", THREAD_COUNT * THREAD_DURATION, count);
    }

// org.apache.commons.jxpath.ri.axes.RecursiveAxesTest::testInfiniteDescent
    public void testInfiniteDescent() {
        
        assertXPathPointer(
            context,
            "//.[name = 'three']",
            "/first/first/second");
    }

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

// org.apache.commons.jxpath.ri.compiler.ContextDependencyTest::testContextDependency
    public void testContextDependency() {
        testContextDependency("1", false);
        testContextDependency("$x", false);
        testContextDependency("/foo", false);
        testContextDependency("foo", true);
        testContextDependency("/foo[3]", false);
        testContextDependency("/foo[$x]", false);
        testContextDependency("/foo[bar]", true);
        testContextDependency("3 + 5", false);
        testContextDependency("test:func(3, 5)", true);
        testContextDependency("test:func(3, foo)", true);
    }

// org.apache.commons.jxpath.ri.compiler.ContextDependencyTest::testContextDependency
    public void testContextDependency(String xpath, boolean expected) {
        Expression expr =
            (Expression) Parser.parseExpression(xpath, new TreeCompiler());

        assertEquals(
            "Context dependency <" + xpath + ">",
            expected,
            expr.isContextDependent());
    }

// org.apache.commons.jxpath.ri.compiler.CoreFunctionTest::testCoreFunctions
    public void testCoreFunctions() {
        assertXPathValue(context, "string(2)", "2");
        assertXPathValue(context, "string($nan)", "NaN");
        assertXPathValue(context, "string(-$nan)", "NaN");
        assertXPathValue(context, "string(-2 div 0)", "-Infinity");
        assertXPathValue(context, "string(2 div 0)", "Infinity");
        assertXPathValue(context, "concat('a', 'b', 'c')", "abc");
        assertXPathValue(context, "starts-with('abc', 'ab')", Boolean.TRUE);
        assertXPathValue(context, "starts-with('xabc', 'ab')", Boolean.FALSE);
        assertXPathValue(context, "contains('xabc', 'ab')", Boolean.TRUE);
        assertXPathValue(context, "contains('xabc', 'ba')", Boolean.FALSE);
        assertXPathValue(
            context,
            "substring-before('1999/04/01', '/')",
            "1999");
        assertXPathValue(
            context,
            "substring-after('1999/04/01', '/')",
            "04/01");
        assertXPathValue(context, "substring('12345', 2, 3)", "234");
        assertXPathValue(context, "substring('12345', 2)", "2345");
        assertXPathValue(context, "substring('12345', 1.5, 2.6)", "234");
        assertXPathValue(context, "substring('12345', 0, 3)", "12");
        assertXPathValue(context, "substring('12345', 0 div 0, 3)", "");
        assertXPathValue(context, "substring('12345', 1, 0 div 0)", "");
        assertXPathValue(context, "substring('12345', -42, 1 div 0)", "12345");
        assertXPathValue(context, "substring('12345', -1 div 0, 1 div 0)", "");
        assertXPathValue(context, "substring('12345', 6, 6)", "");
        assertXPathValue(context, "substring('12345', 7, 8)", "");
        assertXPathValue(context, "substring('12345', 7)", "");
        assertXPathValue(context, "string-length('12345')", new Double(5));
        assertXPathValue(context, "normalize-space(' abc  def  ')", "abc def");
        assertXPathValue(context, "normalize-space('abc def')", "abc def");
        assertXPathValue(context, "normalize-space('   ')", "");
        assertXPathValue(context, "translate('--aaa--', 'abc-', 'ABC')", "AAA");
        assertXPathValue(context, "boolean(1)", Boolean.TRUE);
        assertXPathValue(context, "boolean(0)", Boolean.FALSE);
        assertXPathValue(context, "boolean('x')", Boolean.TRUE);
        assertXPathValue(context, "boolean('')", Boolean.FALSE);

        assertXPathValue(context, "true()", Boolean.TRUE);
        assertXPathValue(context, "false()", Boolean.FALSE);
        assertXPathValue(context, "not(false())", Boolean.TRUE);
        assertXPathValue(context, "not(true())", Boolean.FALSE);
        assertXPathValue(context, "number('1')", new Double(1));
        assertXPathValue(context, "number($bool_true)", new Double(1));
        assertXPathValue(context, "number($bool_false)", new Double(0));
        assertXPathValue(context, "floor(1.5)", new Double(1));
        assertXPathValue(context, "floor(-1.5)", new Double(-2));
        assertXPathValue(context, "ceiling(1.5)", new Double(2));
        assertXPathValue(context, "ceiling(-1.5)", new Double(-1));
        assertXPathValue(context, "round(1.5)", new Double(2));
        assertXPathValue(context, "round(-1.5)", new Double(-1));
        assertXPathValue(context, "null()", null);        
    }

// org.apache.commons.jxpath.ri.compiler.CoreFunctionTest::testIDFunction
    public void testIDFunction() {
        context.setIdentityManager(new IdentityManager() {
            public Pointer getPointerByID(JXPathContext context, String id) {
                NodePointer ptr = (NodePointer) context.getPointer("/document");
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

// org.apache.commons.jxpath.ri.compiler.CoreFunctionTest::testKeyFunction
    public void testKeyFunction() {
        context.setKeyManager(new KeyManager() {
            public Pointer getPointerByKey(
                JXPathContext context,
                String key,
                String value) 
            {
                return NodePointer.newNodePointer(null, "42", null);
            }
        });

        assertXPathValue(context, "key('a', 'b')", "42");
    }

// org.apache.commons.jxpath.ri.compiler.CoreFunctionTest::testExtendedKeyFunction
    public void testExtendedKeyFunction() {
        context.setKeyManager(new ExtendedKeyManager() {
            public Pointer getPointerByKey(JXPathContext context, String key,
                    String value) {
                return NodePointer.newNodePointer(null, "incorrect", null);
            }

            public NodeSet getNodeSetByKey(JXPathContext context,
                    String keyName, Object keyValue) {
                return new NodeSet() {

                    public List getNodes() {
                        return Arrays.asList(new Object[] { "53", "64" });
                    }

                    public List getPointers() {
                        return Arrays.asList(new NodePointer[] {
                                NodePointer.newNodePointer(null, "53", null),
                                NodePointer.newNodePointer(null, "64", null) });
                    }

                    public List getValues() {
                        return Arrays.asList(new Object[] { "53", "64" });
                    }

                };
            }
        });
        assertXPathValue(context, "key('a', 'b')", "53");
        assertXPathValue(context, "key('a', 'b')[1]", "53");
        assertXPathValue(context, "key('a', 'b')[2]", "64");
        assertXPathValueIterator(context, "key('a', 'b')", list("53", "64"));
        assertXPathValueIterator(context, "'x' | 'y'", list("x", "y"));
        assertXPathValueIterator(context, "key('a', 'x' | 'y')", list("53", "64", "53", "64"));
        assertXPathValueIterator(context, "key('a', /list[position() < 4])", list("53", "64", "53", "64", "53", "64"));
        context.getVariables().declareVariable("ints", new int[] { 0, 0 });
        assertXPathValueIterator(context, "key('a', $ints)", list("53", "64", "53", "64"));
    }

// org.apache.commons.jxpath.ri.compiler.CoreFunctionTest::testFormatNumberFunction
    public void testFormatNumberFunction() {
        
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDigit('D');
        
        context.setDecimalFormatSymbols("test", symbols);
        
        assertXPathValue(
            context,
            "format-number(123456789, '#.000000000')",
            "123456789.000000000");

        assertXPathValue(
            context,
            "format-number(123456789, '#.0')",
            "123456789.0");

        assertXPathValue(
            context, 
            "format-number(0.123456789, '##%')", 
            "12%");

        assertXPathValue(
            context,
            "format-number(123456789, '################')",
            "123456789");

        assertXPathValue(
            context,
            "format-number(123456789, 'D.0', 'test')",
            "123456789.0");

        assertXPathValue(
            context,
            "format-number(123456789, '$DDD,DDD,DDD.DD', 'test')",
            "$123,456,789");
    }

// org.apache.commons.jxpath.ri.compiler.CoreOperationTest::testInfoSetTypes
    public void testInfoSetTypes() {

        
        assertXPathValue(context, "1", new Double(1.0));
        assertXPathPointer(context, "1", "1");
        assertXPathValueIterator(context, "1", list(new Double(1.0)));

        assertXPathPointerIterator(context, "1", list("1"));

        assertXPathValue(context, "-1", new Double(-1.0));
        assertXPathValue(context, "2 + 2", new Double(4.0));
        assertXPathValue(context, "3 - 2", new Double(1.0));
        assertXPathValue(context, "1 + 2 + 3 - 4 + 5", new Double(7.0));
        assertXPathValue(context, "3 * 2", new Double(3.0 * 2.0));
        assertXPathValue(context, "3 div 2", new Double(3.0 / 2.0));
        assertXPathValue(context, "5 mod 2", new Double(1.0));

        
        assertXPathValue(context, "5.9 mod 2.1", new Double(1.0));

        assertXPathValue(context, "5 mod -2", new Double(1.0));
        assertXPathValue(context, "-5 mod 2", new Double(-1.0));
        assertXPathValue(context, "-5 mod -2", new Double(-1.0));
        assertXPathValue(context, "1 < 2", Boolean.TRUE);
        assertXPathValue(context, "1 > 2", Boolean.FALSE);
        assertXPathValue(context, "1 <= 1", Boolean.TRUE);
        assertXPathValue(context, "1 >= 2", Boolean.FALSE);
        assertXPathValue(context, "3 > 2 > 1", Boolean.FALSE);
        assertXPathValue(context, "3 > 2 and 2 > 1", Boolean.TRUE);
        assertXPathValue(context, "3 > 2 and 2 < 1", Boolean.FALSE);
        assertXPathValue(context, "3 < 2 or 2 > 1", Boolean.TRUE);
        assertXPathValue(context, "3 < 2 or 2 < 1", Boolean.FALSE);
        assertXPathValue(context, "1 = 1", Boolean.TRUE);
        assertXPathValue(context, "1 = '1'", Boolean.TRUE);
        assertXPathValue(context, "1 > 2 = 2 > 3", Boolean.TRUE);
        assertXPathValue(context, "1 > 2 = 0", Boolean.TRUE);
        assertXPathValue(context, "1 = 2", Boolean.FALSE);

        assertXPathValue(context, "$integer", new Double(1), Double.class);

        assertXPathValue(context, "2 + 3", "5.0", String.class);

        assertXPathValue(context, "2 + 3", Boolean.TRUE, boolean.class);

        assertXPathValue(context, "'true'", Boolean.TRUE, Boolean.class);
    }

// org.apache.commons.jxpath.ri.compiler.CoreOperationTest::testNodeSetOperations
    public void testNodeSetOperations() {
        assertXPathValue(context, "$array > 0", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array >= 0", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array = 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$array = 0.25", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array = 0.5", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array = 0.50000", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array = 0.75", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array < 1", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array <= 1", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array = 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$array > 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$array < 0", Boolean.FALSE, Boolean.class);
    }

// org.apache.commons.jxpath.ri.compiler.CoreOperationTest::testNan
    public void testNan() {
        assertXPathValue(context, "$nan > $nan", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan < $nan", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan >= $nan", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan <= $nan", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan >= $nan and $nan <= $nan", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan = $nan", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan != $nan", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan > 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan < 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan >= 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan <= 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan >= 0 and $nan <= 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan = 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan != 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan > 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan < 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan >= 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan <= 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan >= 1 and $nan <= 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan = 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan != 1", Boolean.FALSE, Boolean.class);
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testConstructorLookup
    public void testConstructorLookup() {
        Object[] args = new Object[] { new Integer(1), "x" };
        Function func = functions.getFunction("test", "new", args);

        assertEquals(
            "test:new(1, x)",
            func.invoke(new Context(null), args).toString(),
            "foo=1; bar=x");
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testConstructorLookupWithExpressionContext
    public void testConstructorLookupWithExpressionContext() {
        Object[] args = new Object[] { "baz" };
        Function func = functions.getFunction("test", "new", args);
        assertEquals(
            "test:new('baz')",
            func.invoke(new Context(new Integer(1)), args).toString(),
            "foo=1; bar=baz");
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testStaticMethodLookup
    public void testStaticMethodLookup() {
        Object[] args = new Object[] { new Integer(1), "x" };
        Function func = functions.getFunction("test", "build", args);
        assertEquals(
            "test:build(1, x)",
            func.invoke(new Context(null), args).toString(),
            "foo=1; bar=x");
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testStaticMethodLookupWithConversion
    public void testStaticMethodLookupWithConversion() {
        Object[] args = new Object[] { "7", new Integer(1)};
        Function func = functions.getFunction("test", "build", args);
        assertEquals(
            "test:build('7', 1)",
            func.invoke(new Context(null), args).toString(),
            "foo=7; bar=1");
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testMethodLookup
    public void testMethodLookup() {
        Object[] args = new Object[] { new TestFunctions()};
        Function func = functions.getFunction("test", "getFoo", args);
        assertEquals(
            "test:getFoo($test, 1, x)",
            func.invoke(new Context(null), args).toString(),
            "0");
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testStaticMethodLookupWithExpressionContext
    public void testStaticMethodLookupWithExpressionContext() {
        Object[] args = new Object[0];
        Function func = functions.getFunction("test", "path", args);
        assertEquals(
            "test:path()",
            func.invoke(new Context(new Integer(1)), args),
            "1");
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testMethodLookupWithExpressionContext
    public void testMethodLookupWithExpressionContext() {
        Object[] args = new Object[] { new TestFunctions()};
        Function func = functions.getFunction("test", "instancePath", args);
        assertEquals(
            "test:instancePath()",
            func.invoke(new Context(new Integer(1)), args),
            "1");
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testMethodLookupWithExpressionContextAndArgument
    public void testMethodLookupWithExpressionContextAndArgument() {
        Object[] args = new Object[] { new TestFunctions(), "*" };
        Function func = functions.getFunction("test", "pathWithSuffix", args);
        assertEquals(
            "test:pathWithSuffix('*')",
            func.invoke(new Context(new Integer(1)), args),
            "1*");
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testAllocation
    public void testAllocation() {
        
        
        assertXPathValue(context, "string(test:new())", "foo=0; bar=null");

        
        assertXPathValue(
            context,
            "string(jxpathtest:TestFunctions.new())",
            "foo=0; bar=null");

        
        assertXPathValue(
            context,
            "string(" + TestFunctions.class.getName() + ".new())",
            "foo=0; bar=null");

        
        assertXPathValue(
            context,
            "string(test:new(3, 'baz'))",
            "foo=3; bar=baz");

        
        assertXPathValue(context, "string(test:new('3', 4))", "foo=3; bar=4.0");
        
        context.getVariables().declareVariable("A", "baz");        
        assertXPathValue(
                context,
                "string(test:new(2, $A, false))",
                "foo=2; bar=baz");
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testMethodCall
    public void testMethodCall() {
        assertXPathValue(context, "length('foo')", new Integer(3));

        
        assertXPathValue(context, "call:substring('foo', 1, 2)", "o");

        
        assertXPathValue(context, "string(test:getFoo($test))", "4");
        
        
        assertXPathValue(context, "string(call:getFoo($test))", "4");

        
        assertXPathValue(context, "string(getFoo($test))", "4");

        
        assertXPathValue(
            context,
            "string(test:setFooAndBar($test, 7, 'biz'))",
            "foo=7; bar=biz");
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testCollectionMethodCall
    public void testCollectionMethodCall() {
        
        List list = new ArrayList();
        list.add("foo");
        context.getVariables().declareVariable("myList", list);

        assertXPathValue(
            context, 
            "size($myList)", 
            new Integer(1));
    
        assertXPathValue(
            context, 
            "size(beans)", 
            new Integer(2));
            
        context.getValue("add($myList, 'hello')");
        assertEquals("After adding an element", 2, list.size());
        
        JXPathContext context = JXPathContext.newContext(new ArrayList());
        assertEquals("Extension function on root collection", "0", String
                .valueOf(context.getValue("size(/)")));
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testStaticMethodCall
    public void testStaticMethodCall() {

        assertXPathValue(
            context,
            "string(test:build(8, 'goober'))",
            "foo=8; bar=goober");

        
        assertXPathValue(
            context,
            "string(jxpathtest:TestFunctions.build(8, 'goober'))",
            "foo=8; bar=goober");

        
        assertXPathValue(
            context,
            "string(" + TestFunctions.class.getName() + ".build(8, 'goober'))",
            "foo=8; bar=goober");

        
        
        assertXPathValue(context, "string(test:increment(8))", "9");
        
        
        assertXPathValue(context, "test:string(/beans/name)", "Name 1");
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testExpressionContext
    public void testExpressionContext() {
        
        
        
        assertXPathValue(
            context, 
            "//.[test:isMap()]/Key1", 
            "Value 1");

        
        
        assertXPathValue(
            context,
            "count(//.[test:count(strings) = 3])",
            new Double(7));

        
        
        assertXPathValue(
            context,
            "test:count(//strings)",
            new Integer(21));

        
        
        
        assertXPathValue(
            context,
            "test:countPointers(//strings)",
            new Integer(21));
            
        
        
        assertXPathValue(
            context,
            "/beans[contains(test:path(), '[2]')]/name",
            "Name 2");
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testCollectionReturn
    public void testCollectionReturn() {
        assertXPathValueIterator(
            context,
            "test:collection()/name",
            list("foo", "bar"));

        assertXPathPointerIterator(
            context,
            "test:collection()/name",
            list("/.[1]/name", "/.[2]/name"));
            
        assertXPathValue(
            context,
            "test:collection()/name",
            "foo");        

        assertXPathValue(
            context,
            "test:collection()/@name",
            "foo");   
        
        List list = new ArrayList();
        list.add("foo");
        list.add("bar");
        context.getVariables().declareVariable("list", list);
        Object values = context.getValue("test:items($list)");
        assertTrue("Return type: ", values instanceof Collection);
        assertEquals(
            "Return values: ",
            list,
            new ArrayList((Collection) values));
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testNodeSetReturn
    public void testNodeSetReturn() {
        assertXPathValueIterator(
            context,
            "test:nodeSet()/name",
            list("Name 1", "Name 2"));

        assertXPathValueIterator(
            context,
            "test:nodeSet()",
            list(testBean.getBeans()[0], testBean.getBeans()[1]));

        assertXPathPointerIterator(
            context,
            "test:nodeSet()/name",
            list("/beans[1]/name", "/beans[2]/name"));
            
        assertXPathValueAndPointer(
            context,
            "test:nodeSet()/name",
            "Name 1",
            "/beans[1]/name");        

        assertXPathValueAndPointer(
            context,
            "test:nodeSet()/@name",
            "Name 1",
            "/beans[1]/@name");
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testEstablishNodeSetBaseline
    public void testEstablishNodeSetBaseline() {
        assertXPathValue(
            context,
            "test:isInstance(//strings, $List.class)",
            Boolean.TRUE);
        assertXPathValue(
            context,
            "test:isInstance(//strings, $NodeSet.class)",
            Boolean.FALSE);
    }

// org.apache.commons.jxpath.ri.compiler.ExtensionFunctionTest::testBCNodeSetHack
    public void testBCNodeSetHack() {
        TypeUtils.setTypeConverter(new JXPath11CompatibleTypeConverter());
        assertXPathValue(
            context,
            "test:isInstance(//strings, $List.class)",
            Boolean.FALSE);
        assertXPathValue(
            context,
            "test:isInstance(//strings, $NodeSet.class)",
            Boolean.TRUE);
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testVariables
    public void testVariables() {
        
        assertXPathValueAndPointer(context, "$a", new Double(1), "$a");
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testVariablesInExpressions
    public void testVariablesInExpressions() {
        assertXPathValue(context, "$a = $b", Boolean.TRUE);

        assertXPathValue(context, "$a = $nan", Boolean.FALSE);

        assertXPathValue(context, "$a + 1", new Double(2));

        assertXPathValue(context, "$c", null);

        assertXPathValue(context, "$d[2]", "b");
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testInvalidVariableName
    public void testInvalidVariableName() {
        boolean exception = false;
        try {
            context.getValue("$none");
        }
        catch (Exception ex) {
            exception = true;
        }
        assertTrue(
            "Evaluating '$none', expected exception - did not get it",
            exception);
        
        exception = false;
        try {
            context.setValue("$none", new Integer(1));
        }
        catch (Exception ex) {
            exception = true;
        }
        assertTrue(
            "Setting '$none = 1', expected exception - did not get it",
            exception);
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testNestedContext
    public void testNestedContext() {
        JXPathContext nestedContext = JXPathContext.newContext(context, null);

        assertXPathValue(nestedContext, "$a", new Double(1));
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testSetValue
    public void testSetValue() {
        assertXPathSetValue(context, "$x", new Integer(1));
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testCreatePathDeclareVariable
    public void testCreatePathDeclareVariable() {
        
        assertXPathCreatePath(context, "$string", null, "$string");
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testCreatePathAndSetValueDeclareVariable
    public void testCreatePathAndSetValueDeclareVariable() {
        
        assertXPathCreatePathAndSetValue(
            context,
            "$string",
            "Value",
            "$string");
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testCreatePathDeclareVariableSetCollectionElement
    public void testCreatePathDeclareVariableSetCollectionElement() {
        
        
        assertXPathCreatePath(
            context,
            "$stringArray[2]",
            "",
            "$stringArray[2]");

        
        assertEquals(
            "Created <" + "$stringArray[1]" + ">",
            "Value1",
            context.getValue("$stringArray[1]"));
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testCreateAndSetValuePathDeclareVariableSetCollectionElement
    public void testCreateAndSetValuePathDeclareVariableSetCollectionElement() {
        
        
        assertXPathCreatePathAndSetValue(
            context,
            "$stringArray[2]",
            "Value2",
            "$stringArray[2]");

        
        assertEquals(
            "Created <" + "$stringArray[1]" + ">",
            "Value1",
            context.getValue("$stringArray[1]"));
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testCreatePathExpandCollection
    public void testCreatePathExpandCollection() {
        context.getVariables().declareVariable(
            "array",
            new String[] { "Value1" });

        
        assertXPathCreatePath(context, "$array[2]", "", "$array[2]");

        
        assertEquals(
            "Created <" + "$array[1]" + ">",
            "Value1",
            context.getValue("$array[1]"));
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testCreatePathAndSetValueExpandCollection
    public void testCreatePathAndSetValueExpandCollection() {
        context.getVariables().declareVariable(
            "array",
            new String[] { "Value1" });

        
        assertXPathCreatePathAndSetValue(
            context,
            "$array[2]",
            "Value2",
            "$array[2]");

        
        assertEquals(
            "Created <" + "$array[1]" + ">",
            "Value1",
            context.getValue("$array[1]"));
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testCreatePathDeclareVariableSetProperty
    public void testCreatePathDeclareVariableSetProperty() {
        
        
        assertXPathCreatePath(
            context,
            "$test/boolean",
            Boolean.FALSE,
            "$test/boolean");

    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testCreatePathAndSetValueDeclareVariableSetProperty
    public void testCreatePathAndSetValueDeclareVariableSetProperty() {
        
        
        assertXPathCreatePathAndSetValue(
            context,
            "$test/boolean",
            Boolean.TRUE,
            "$test/boolean");

    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testCreatePathDeclareVariableSetCollectionElementProperty
    public void testCreatePathDeclareVariableSetCollectionElementProperty() {
        
        
        
        
        
        assertXPathCreatePath(
            context,
            "$testArray[2]/boolean",
            Boolean.FALSE,
            "$testArray[2]/boolean");
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testCreatePathAndSetValueDeclVarSetCollectionElementProperty
    public void testCreatePathAndSetValueDeclVarSetCollectionElementProperty() {
        
        
        
        
        
        assertXPathCreatePathAndSetValue(
            context,
            "$testArray[2]/boolean",
            Boolean.TRUE,
            "$testArray[2]/boolean");
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testRemovePathUndeclareVariable
    public void testRemovePathUndeclareVariable() {
        
        context.getVariables().declareVariable("temp", "temp");
        context.removePath("$temp");
        assertTrue(
            "Undeclare variable",
            !context.getVariables().isDeclaredVariable("temp"));

    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testRemovePathArrayElement
    public void testRemovePathArrayElement() {
        
        context.getVariables().declareVariable(
            "temp",
            new String[] { "temp1", "temp2" });
        context.removePath("$temp[1]");
        assertEquals(
            "Remove array element",
            "temp2",
            context.getValue("$temp[1]"));
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testRemovePathCollectionElement
    public void testRemovePathCollectionElement() {
        
        context.getVariables().declareVariable("temp", list("temp1", "temp2"));
        context.removePath("$temp[1]");
        assertEquals(
            "Remove collection element",
            "temp2",
            context.getValue("$temp[1]"));
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testUnionOfVariableAndNode
    public void testUnionOfVariableAndNode() throws Exception {
        assertXPathValue(context, "count($a | /document/vendor/location)", new Double(3));
        assertXPathValue(context, "count($a | /list)", new Double(7)); 
    }

// org.apache.commons.jxpath.ri.compiler.VariableTest::testIterateVariable
    public void testIterateVariable() throws Exception {
        assertXPathValueIterator(context, "$d", list("a", "b"));
        assertXPathValue(context, "$d = 'a'", Boolean.TRUE);
        assertXPathValue(context, "$d = 'b'", Boolean.TRUE);
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testVar
    public void testVar() {
        context.getVariables().declareVariable("foo:bar", "baz");

        assertXPathValueAndPointer(context, 
            "$foo:bar", 
            "baz", 
            "$foo:bar");
        
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testVarPrimitive
    public void testVarPrimitive() {
        assertXPathValueAndPointer(context, "$string", "string", "$string");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testVarBean
    public void testVarBean() {
        assertXPathValueAndPointer(
            context,
            "$bean/int",
            new Integer(1),
            "$bean/int");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testVarMap
    public void testVarMap() {
        assertXPathValueAndPointer(
            context,
            "$map/string",
            "string",
            "$map[@name='string']");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testVarList
    public void testVarList() {
        assertXPathValueAndPointer(context, "$list[1]", "string", "$list[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testVarDocument
    public void testVarDocument() {
        assertXPathValueAndPointer(
            context,
            "$document/vendor/location/address/city",
            "Fruit Market",
            "$document/vendor[1]/location[2]/address[1]/city[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testVarElement
    public void testVarElement() {
        assertXPathValueAndPointer(
            context,
            "$element/location/address/city",
            "Fruit Market",
            "$element/location[2]/address[1]/city[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testVarContainer
    public void testVarContainer() {
        assertXPathValueAndPointer(
            context,
            "$container/vendor/location/address/city",
            "Fruit Market",
            "$container/vendor[1]/location[2]/address[1]/city[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testBeanPrimitive
    public void testBeanPrimitive() {
        assertXPathValueAndPointer(context, "string", "string", "/string");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testBeanBean
    public void testBeanBean() {
        assertXPathValueAndPointer(
            context,
            "bean/int",
            new Integer(1),
            "/bean/int");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testBeanMap
    public void testBeanMap() {
        assertXPathValueAndPointer(
            context,
            "map/string",
            "string",
            "/map[@name='string']");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testBeanList
    public void testBeanList() {
        assertXPathValueAndPointer(context, "list[1]", "string", "/list[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testBeanDocument
    public void testBeanDocument() {
        assertXPathValueAndPointer(
            context,
            "document/vendor/location/address/city",
            "Fruit Market",
            "/document/vendor[1]/location[2]/address[1]/city[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testBeanElement
    public void testBeanElement() {
        assertXPathValueAndPointer(
            context,
            "element/location/address/city",
            "Fruit Market",
            "/element/location[2]/address[1]/city[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testBeanContainer
    public void testBeanContainer() {
        assertXPathValueAndPointer(
            context,
            "container/vendor/location/address/city",
            "Fruit Market",
            "/container/vendor[1]/location[2]/address[1]/city[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testMapPrimitive
    public void testMapPrimitive() {
        assertXPathValueAndPointer(
            context,
            "map/string",
            "string",
            "/map[@name='string']");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testMapBean
    public void testMapBean() {
        assertXPathValueAndPointer(
            context,
            "map/bean/int",
            new Integer(1),
            "/map[@name='bean']/int");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testMapMap
    public void testMapMap() {
        assertXPathValueAndPointer(
            context,
            "map/map/string",
            "string",
            "/map[@name='map'][@name='string']");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testMapList
    public void testMapList() {
        assertXPathValueAndPointer(
            context,
            "map/list[1]",
            "string",
            "/map[@name='list'][1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testMapDocument
    public void testMapDocument() {
        assertXPathValueAndPointer(
            context,
            "map/document/vendor/location/address/city",
            "Fruit Market",
            "/map[@name='document']"
                + "/vendor[1]/location[2]/address[1]/city[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testMapElement
    public void testMapElement() {
        assertXPathValueAndPointer(
            context,
            "map/element/location/address/city",
            "Fruit Market",
            "/map[@name='element']/location[2]/address[1]/city[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testMapContainer
    public void testMapContainer() {
        assertXPathValueAndPointer(
            context,
            "map/container/vendor/location/address/city",
            "Fruit Market",
            "/map[@name='container']"
                + "/vendor[1]/location[2]/address[1]/city[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testListPrimitive
    public void testListPrimitive() {
        assertXPathValueAndPointer(context, "list[1]", "string", "/list[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testListBean
    public void testListBean() {
        assertXPathValueAndPointer(
            context,
            "list[2]/int",
            new Integer(1),
            "/list[2]/int");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testListMap
    public void testListMap() {
        assertXPathValueAndPointer(
            context,
            "list[3]/string",
            "string",
            "/list[3][@name='string']");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testListList
    public void testListList() {
        

        assertXPathValueAndPointer(
            context,
            "list[4]/.[1]",
            "string2",
            "/list[4]/.[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testListDocument
    public void testListDocument() {
        assertXPathValueAndPointer(
            context,
            "list[5]/vendor/location/address/city",
            "Fruit Market",
            "/list[5]/vendor[1]/location[2]/address[1]/city[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testListElement
    public void testListElement() {
        assertXPathValueAndPointer(
            context,
            "list[6]/location/address/city",
            "Fruit Market",
            "/list[6]/location[2]/address[1]/city[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testListContainer
    public void testListContainer() {
        assertXPathValueAndPointer(
            context,
            "list[7]/vendor/location/address/city",
            "Fruit Market",
            "/list[7]/vendor[1]/location[2]/address[1]/city[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testNull
    public void testNull() {

        assertXPathPointerLenient(context, "$null", "$null");

        assertXPathPointerLenient(context, "$null[3]", "$null[3]");

        assertXPathPointerLenient(
            context,
            "$testnull/nothing",
            "$testnull/nothing");

        assertXPathPointerLenient(
            context,
            "$testnull/nothing[2]",
            "$testnull/nothing[2]");

        assertXPathPointerLenient(context, "beans[8]/int", "/beans[8]/int");

        assertXPathValueIterator(
            context,
            "$testnull/nothing[1]",
            Collections.EMPTY_LIST);

        JXPathContext ctx = JXPathContext.newContext(new TestNull());
        assertXPathValue(ctx, "nothing", null);

        assertXPathValue(ctx, "child/nothing", null);

        assertXPathValue(ctx, "array[2]", null);

        assertXPathValueLenient(ctx, "nothing/something", null);

        assertXPathValueLenient(ctx, "array[2]/something", null);
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testRootAsCollection
    public void testRootAsCollection() {
        assertXPathValue(context, ".[1]/string", "string");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testCreatePath
    public void testCreatePath() {
        context = JXPathContext.newContext(new TestBean());
        context.setFactory(new TestMixedModelFactory());

        TestBean bean = (TestBean) context.getContextBean();
        bean.setMap(null);

        assertXPathCreatePath(
            context,
            "/map[@name='TestKey5']/nestedBean/int",
            new Integer(1),
            "/map[@name='TestKey5']/nestedBean/int");

        bean.setMap(null);
        assertXPathCreatePath(
            context,
            "/map[@name='TestKey5']/beans[2]/int",
            new Integer(1),
            "/map[@name='TestKey5']/beans[2]/int");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testIterateArray
    public void testIterateArray() {
        Map map = new HashMap();
        map.put("foo", new String[] { "a", "b", "c" });

        JXPathContext context = JXPathContext.newContext(map);

        assertXPathValueIterator(context, "foo", list("a", "b", "c"));
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testIteratePointersArray
    public void testIteratePointersArray() {
        Map map = new HashMap();
        map.put("foo", new String[] { "a", "b", "c" });

        JXPathContext context = JXPathContext.newContext(map);

        Iterator it = context.iteratePointers("foo");
        List actual = new ArrayList();
        while (it.hasNext()) {
            Pointer ptr = (Pointer) it.next();
            actual.add(context.getValue(ptr.asPath()));
        }
        assertEquals(
            "Iterating pointers <" + "foo" + ">",
            list("a", "b", "c"),
            actual);
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testIteratePointersArrayElementWithVariable
    public void testIteratePointersArrayElementWithVariable() {
        Map map = new HashMap();
        map.put("foo", new String[] { "a", "b", "c" });

        JXPathContext context = JXPathContext.newContext(map);
        context.getVariables().declareVariable("x", new Integer(2));
        Iterator it = context.iteratePointers("foo[$x]");
        List actual = new ArrayList();
        while (it.hasNext()) {
            Pointer ptr = (Pointer) it.next();
            actual.add(context.getValue(ptr.asPath()));
        }
        assertEquals("Iterating pointers <" + "foo" + ">", list("b"), actual);
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testIterateVector
    public void testIterateVector() {
        Map map = new HashMap();
        Vector vec = new Vector();
        vec.add(new HashMap());
        vec.add(new HashMap());

        map.put("vec", vec);
        JXPathContext context = JXPathContext.newContext(map);
        assertXPathPointerIterator(
            context,
            "/vec",
            list("/.[@name='vec'][1]", "/.[@name='vec'][2]"));
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testErrorProperty
    public void testErrorProperty() {
        context.getVariables().declareVariable(
            "e",
            new ExceptionPropertyTestBean());

        boolean ex = false;
        try {
            assertXPathValue(context, "$e/errorString", null);
        }
        catch (Throwable t) {
            ex = true;
        }
        assertTrue("Legitimate exception accessing property", ex);

        assertXPathPointer(context, "$e/errorString", "$e/errorString");

        assertXPathPointerLenient(
            context,
            "$e/errorStringArray[1]",
            "$e/errorStringArray[1]");

        assertXPathPointerIterator(
            context,
            "$e/errorString",
            list("$e/errorString"));

        assertXPathPointerIterator(
            context,
            "$e//error",
            Collections.EMPTY_LIST);
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testMatrix
    public void testMatrix() {
        assertXPathValueAndPointer(
            context,
            "$matrix[1]/.[1]",
            new Integer(3),
            "$matrix[1]/.[1]");

        context.setValue("$matrix[1]/.[1]", new Integer(2));

        assertXPathValueAndPointer(
            context,
            "matrix[1]/.[1]",
            new Integer(3),
            "/matrix[1]/.[1]");

        context.setValue("matrix[1]/.[1]", "2");

        assertXPathValue(context, "matrix[1]/.[1]", new Integer(2));

        context.getVariables().declareVariable(
            "wholebean",
            context.getContextBean());

        assertXPathValueAndPointer(
            context,
            "$wholebean/matrix[1]/.[1]",
            new Integer(2),
            "$wholebean/matrix[1]/.[1]");

        boolean ex = false;
        try {
            context.setValue("$wholebean/matrix[1]/.[2]", "4");
        }
        catch (Exception e) {
            ex = true;
        }
        assertTrue("Exception setting value of non-existent element", ex);

        ex = false;
        try {
            context.setValue("$wholebean/matrix[2]/.[1]", "4");
        }
        catch (Exception e) {
            ex = true;
        }
        assertTrue("Exception setting value of non-existent element", ex);
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testCreatePathAndSetValueWithMatrix
    public void testCreatePathAndSetValueWithMatrix() {

        context.setValue("matrix", null);

        
        
        assertXPathCreatePathAndSetValue(
            context,
            "/matrix[1]/.[1]",
            new Integer(4),
            "/matrix[1]/.[1]");
    }

// org.apache.commons.jxpath.ri.model.MixedModelTest::testCollectionPointer
    public void testCollectionPointer() {
        List list = new ArrayList();
        Map map = new HashMap();
        map.put("KeyOne", "SomeStringOne");
        map.put("KeyTwo", "SomeStringTwo");
        
        Map map2 = new HashMap();
        map2.put("KeyA", "StringA");
        map2.put("KeyB", "StringB");
        
        map.put("KeyThree", map2);
        list.add(map);
        
        List list2 = new ArrayList();
        list2.add("foo");
        list2.add(map);
        list2.add(map);
        list.add(list2);
        
        context = JXPathContext.newContext(list);
        
        assertEquals("SomeStringOne", context.getValue(".[1]/KeyOne"));
        assertEquals("StringA", context.getValue(".[1]/KeyThree/KeyA"));
        assertEquals(new Integer(3), context.getValue("size(.[1]/KeyThree)"));
        assertEquals(new Double(6.0), context.getValue("count(.[1]/KeyThree/*)"));
        assertEquals(new Double(3.0), context.getValue("count(.[1]/KeyThree/KeyA)"));
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

// org.apache.commons.jxpath.ri.model.beans.BadlyImplementedFactoryTest::testBadFactoryImplementation
    public void testBadFactoryImplementation() {
        try {
            context.createPath("foo/bar");
            fail("should fail with JXPathException caused by JXPathAbstractFactoryException");
        } catch (JXPathException e) {
            assertTrue(e.getCause() instanceof JXPathAbstractFactoryException);
        }
    }

// org.apache.commons.jxpath.ri.model.beans.BeanModelTest::testIndexedProperty
    public void testIndexedProperty() {
        JXPathContext context =
            JXPathContext.newContext(null, new TestIndexedPropertyBean());
            
        assertXPathValueAndPointer(
            context,
            "indexed[1]",
            new Integer(0),
            "/indexed[1]");
    }

// org.apache.commons.jxpath.ri.model.container.ContainerModelTest::testContainerVariableWithCollection
    public void testContainerVariableWithCollection() {
        ArrayContainer container = new ArrayContainer();
        String[] array = (String[]) container.getValue();
        
        JXPathContext context = JXPathContext.newContext(null);
        context.getVariables().declareVariable("list", container);
        
        assertXPathValueAndPointer(context, "$list", array, "$list");
        assertXPathValueAndPointer(context, "$list[1]", "foo", "$list[1]");
        assertXPathValueAndPointer(context, "$list[2]", "bar", "$list[2]");
        
        assertXPathSetValue(context, "$list[1]", "baz");
        assertEquals("Checking setValue(index)", "baz", array[0]);
    }

// org.apache.commons.jxpath.ri.model.container.ContainerModelTest::testContainerPropertyWithCollection
    public void testContainerPropertyWithCollection() {
        Bean bean = new Bean();
        List list = (List) bean.getContainer().getValue();
        
        JXPathContext context = JXPathContext.newContext(bean);
        
        assertXPathValueAndPointer(context, "/container", 
                list, "/container");
        assertXPathValueAndPointer(context, "/container[1]",
                list.get(0), "/container[1]");
        assertXPathValueAndPointer(context, "/container[2]",
                list.get(1), "/container[2]");
        
        assertXPathSetValue(context, "/container[1]", "baz");
        assertEquals("Checking setValue(index)", "baz", list.get(0));
    }

// org.apache.commons.jxpath.ri.model.container.ContainerModelTest::testContainerMapWithCollection
    public void testContainerMapWithCollection() {
        ListContainer container = new ListContainer();
        List list = (List) container.getValue();
                
        Map map = new HashMap();
        map.put("container", container);
        
        JXPathContext context = JXPathContext.newContext(map);
        
        assertXPathValueAndPointer(context, "/container", 
                list, "/.[@name='container']");
        assertXPathValueAndPointer(context, "/container[1]",
                list.get(0), "/.[@name='container'][1]");
        assertXPathValueAndPointer(context, "/container[2]",
                list.get(1), "/.[@name='container'][2]");
        
        assertXPathSetValue(context, "/container[1]", "baz");
        assertEquals("Checking setValue(index)", "baz", list.get(0));
    }

// org.apache.commons.jxpath.ri.model.container.ContainerModelTest::testContainerRootWithCollection
    public void testContainerRootWithCollection() {
        ArrayContainer container = new ArrayContainer();
        String[] array = (String[]) container.getValue();
        
        JXPathContext context = JXPathContext.newContext(container);
        context.getVariables().declareVariable("list", container);
        
        assertXPathValueAndPointer(context, "/", array, "/");
        assertXPathValueAndPointer(context, "/.[1]", "foo", "/.[1]");
        assertXPathValueAndPointer(context, "/.[2]", "bar", "/.[2]");
        
        assertXPathSetValue(context, "/.[1]", "baz");
        assertEquals("Checking setValue(index)", "baz", array[0]);    }

// org.apache.commons.jxpath.ri.model.dom.DOMModelTest::testGetNode
    public void testGetNode() {
        assertXPathNodeType(context, "/", Document.class);
        assertXPathNodeType(context, "/vendor/location", Element.class);
        assertXPathNodeType(context, "//location/@name", Attr.class);
        assertXPathNodeType(context, "//vendor", Element.class);
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testAxisChild
    public void testAxisChild() {
        assertXPathValue(context, "map/Key1", "Value 1");

        assertXPathPointer(context, "map/Key1", "/map[@name='Key1']");

        assertXPathValue(context, "map/Key2/name", "Name 6");

        assertXPathPointer(context, "map/Key2/name", "/map[@name='Key2']/name");
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testAxisDescendant
    public void testAxisDescendant() {
        assertXPathValue(context, "//Key1", "Value 1");
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testAttributeName
    public void testAttributeName() {
        assertXPathValue(context, "map[@name = 'Key1']", "Value 1");

        assertXPathPointer(
            context,
            "map[@name = 'Key1']",
            "/map[@name='Key1']");

        assertXPathPointerLenient(
            context,
            "map[@name = 'Key&quot;&apos;&quot;&apos;1']",
            "/map[@name='Key&quot;&apos;&quot;&apos;1']");

        assertXPathValue(context, "/.[@name='map']/Key2/name", "Name 6");

        assertXPathPointer(
            context,
            "/.[@name='map']/Key2/name",
            "/map[@name='Key2']/name");

        
        assertXPathValue(context, "/map[@name='Key2'][@name='name']", "Name 6");

        assertXPathPointer(
            context,
            "/map[@name='Key2'][@name='name']",
            "/map[@name='Key2']/name");

        
        assertXPathValue(
            context,
            "/.[@name='map'][@name='Key2'][@name='name']",
            "Name 6");

        assertXPathPointer(
            context,
            "/.[@name='map'][@name='Key2'][@name='name']",
            "/map[@name='Key2']/name");
                        
        ((Map)context.getValue("map")).put("Key:3", "value3");
        
        assertXPathValueAndPointer(
            context,
            "/map[@name='Key:3']",
            "value3",
            "/map[@name='Key:3']");

        assertXPathValueAndPointer(
            context,
            "/map[@name='Key:4:5']",
            null,
            "/map[@name='Key:4:5']");
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testSetPrimitiveValue
    public void testSetPrimitiveValue() {
        assertXPathSetValue(context, "map/Key1", new Integer(6));
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testSetCollection
    public void testSetCollection() {
        
        context.setValue(
            "map/Key1",
            new Integer[] { new Integer(7), new Integer(8)});

        
        assertXPathSetValue(context, "map/Key1[1]", new Integer(9));
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testSetNewKey
    public void testSetNewKey() {
        
        assertXPathSetValue(context, "map/Key4", new Integer(7));
        
        
        assertXPathPointerLenient(context, "//map/Key5", "/map/Key5");
        
        assertXPathSetValue(context, "//map/Key5", new Integer(8));
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testCreatePath
    public void testCreatePath() {
        TestBean bean = (TestBean) context.getContextBean();
        bean.setMap(null);

        
        
        assertXPathCreatePath(
            context,
            "/map[@name='TestKey1']",
            "",
            "/map[@name='TestKey1']");
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testCreatePathAndSetValue
    public void testCreatePathAndSetValue() {
        TestBean bean = (TestBean) context.getContextBean();
        bean.setMap(null);

        
        
        assertXPathCreatePathAndSetValue(
            context,
            "/map[@name='TestKey1']",
            "Test",
            "/map[@name='TestKey1']");
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testCreatePathCreateBean
    public void testCreatePathCreateBean() {
        TestBean bean = (TestBean) context.getContextBean();
        bean.setMap(null);

        
        
        
        assertXPathCreatePath(
            context,
            "/map[@name='TestKey2']/int",
            new Integer(1),
            "/map[@name='TestKey2']/int");
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testCreatePathAndSetValueCreateBean
    public void testCreatePathAndSetValueCreateBean() {
        TestBean bean = (TestBean) context.getContextBean();
        bean.setMap(null);

        
        
        
        assertXPathCreatePathAndSetValue(
            context,
            "/map[@name='TestKey2']/int",
            new Integer(4),
            "/map[@name='TestKey2']/int");
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testCreatePathCollectionElement
    public void testCreatePathCollectionElement() {
        TestBean bean = (TestBean) context.getContextBean();
        bean.setMap(null);

        assertXPathCreatePath(
            context,
            "/map/TestKey3[2]",
            null,
            "/map[@name='TestKey3'][2]");

        
        assertXPathCreatePath(
            context,
            "/map[@name='TestKey3'][3]",
            null,
            "/map[@name='TestKey3'][3]");
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testCreatePathAndSetValueCollectionElement
    public void testCreatePathAndSetValueCollectionElement() {
        TestBean bean = (TestBean) context.getContextBean();
        bean.setMap(null);

        assertXPathCreatePathAndSetValue(
            context,
            "/map/TestKey3[2]",
            "Test1",
            "/map[@name='TestKey3'][2]");

        
        assertXPathCreatePathAndSetValue(
            context,
            "/map[@name='TestKey3'][3]",
            "Test2",
            "/map[@name='TestKey3'][3]");
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testCreatePathNewCollectionElement
    public void testCreatePathNewCollectionElement() {
        TestBean bean = (TestBean) context.getContextBean();
        bean.setMap(null);

        
        assertXPathCreatePath(
            context,
            "/map/TestKey4[1]/int",
            new Integer(1),
            "/map[@name='TestKey4'][1]/int");

        bean.getMap().remove("TestKey4");

        
        assertXPathCreatePath(
            context,
            "/map/TestKey4[1]/int",
            new Integer(1),
            "/map[@name='TestKey4'][1]/int");
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testCreatePathAndSetValueNewCollectionElement
    public void testCreatePathAndSetValueNewCollectionElement() {
        TestBean bean = (TestBean) context.getContextBean();
        bean.setMap(null);

        
        assertXPathCreatePathAndSetValue(
            context,
            "/map/TestKey4[1]/int",
            new Integer(2),
            "/map[@name='TestKey4'][1]/int");

        bean.getMap().remove("TestKey4");

        
        assertXPathCreatePathAndSetValue(
            context,
            "/map/TestKey4[1]/int",
            new Integer(3),
            "/map[@name='TestKey4'][1]/int");
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testRemovePath
    public void testRemovePath() {
        TestBean bean = (TestBean) context.getContextBean();
        bean.getMap().put("TestKey1", "test");

        
        context.removePath("map[@name = 'TestKey1']");
        assertEquals(
            "Remove dynamic property value",
            null,
            context.getValue("map[@name = 'TestKey1']"));
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testRemovePathArrayElement
    public void testRemovePathArrayElement() {
        TestBean bean = (TestBean) context.getContextBean();

        bean.getMap().put("TestKey2", new String[] { "temp1", "temp2" });
        context.removePath("map[@name = 'TestKey2'][1]");
        assertEquals(
            "Remove dynamic property collection element",
            "temp2",
            context.getValue("map[@name = 'TestKey2'][1]"));
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testCollectionOfMaps
    public void testCollectionOfMaps() {
        TestBean bean = (TestBean) context.getContextBean();
        List list = new ArrayList();

        bean.getMap().put("stuff", list);        

        Map m = new HashMap();
        m.put("fruit", "apple");
        list.add(m);

        m = new HashMap();
        m.put("berry", "watermelon");
        list.add(m);

        m = new HashMap();
        m.put("fruit", "banana");
        list.add(m);

        assertXPathValueIterator(
            context,
            "/map/stuff/fruit",
            list("apple", "banana"));

        assertXPathValueIterator(
            context,
            "/map/stuff[@name='fruit']",
            list("apple", "banana"));        
    }

// org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertiesModelTest::testMapOfMaps
    public void testMapOfMaps() {
        TestBean bean = (TestBean) context.getContextBean();

        Map fruit = new HashMap();
        fruit.put("apple", "green");
        fruit.put("orange", "red");
        
        Map meat = new HashMap();
        meat.put("pork", "pig");
        meat.put("beef", "cow");
        
        bean.getMap().put("fruit", fruit);        
        bean.getMap().put("meat", meat);        
                
        assertXPathPointer(
            context,
            "//beef",
            "/map[@name='meat'][@name='beef']");
        
        assertXPathPointer(
            context,
            "map//apple",
            "/map[@name='fruit'][@name='apple']");

        
        assertXPathPointerLenient(context, "map//banana", "null()");
        
        
        assertXPathPointerLenient(
            context,
            "//fruit/pear",
            "/map[@name='fruit']/pear");
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
