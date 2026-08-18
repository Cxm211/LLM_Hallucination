// buggy code
    private boolean compute(Object left, Object right) {
        left = reduce(left);
        right = reduce(right);

        if (left instanceof InitialContext) {
            ((InitialContext) left).reset();
        }
        if (right instanceof InitialContext) {
            ((InitialContext) right).reset();
        }
        if (left instanceof Iterator && right instanceof Iterator) {
            return findMatch((Iterator) left, (Iterator) right);
        }
        if (left instanceof Iterator) {
            return containsMatch((Iterator) left, right);
        }
        if (right instanceof Iterator) {
            return containsMatch((Iterator) right, left);
        }
        double ld = InfoSetUtil.doubleValue(left);
        double rd = InfoSetUtil.doubleValue(right);
        return evaluateCompare(ld == rd ? 0 : ld < rd ? -1 : 1);
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
        assertXPathValue(context, "$nan = $nan", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$nan > 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan < 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan = 0", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan > 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan < 1", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan = 1", Boolean.FALSE, Boolean.class);
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
