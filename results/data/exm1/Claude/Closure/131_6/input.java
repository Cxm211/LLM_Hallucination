// buggy code
    public static boolean isJSIdentifier(String s) {
      int length = s.length();

      if (length == 0 ||
          !Character.isJavaIdentifierStart(s.charAt(0))) {
        return false;
      }

      for (int i = 1; i < length; i++) {
        if (
            !Character.isJavaIdentifierPart(s.charAt(i))) {
          return false;
        }
      }

      return true;
    }

// relevant test
// com.google.javascript.jscomp.NodeUtilTest::testIsControlStructureCodeBlock
  public void testIsControlStructureCodeBlock() {
    Node root = parse("if (x) foo(); else boo();");
    Node ifNode = root.getFirstChild();

    Node ifCondition = ifNode.getFirstChild();
    Node ifCase = ifNode.getFirstChild().getNext();
    Node elseCase = ifNode.getLastChild();

    assertFalse(NodeUtil.isControlStructureCodeBlock(ifNode, ifCondition));
    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, ifCase));
    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, elseCase));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsFunctionExpression1
  public void testIsFunctionExpression1() {
    Node root = parse("(function foo() {})");
    Node statementNode = root.getFirstChild();
    assertTrue(statementNode.isExprResult());
    Node functionNode = statementNode.getFirstChild();
    assertTrue(functionNode.isFunction());
    assertTrue(NodeUtil.isFunctionExpression(functionNode));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsFunctionExpression2
  public void testIsFunctionExpression2() {
    Node root = parse("function foo() {}");
    Node functionNode = root.getFirstChild();
    assertTrue(functionNode.isFunction());
    assertFalse(NodeUtil.isFunctionExpression(functionNode));
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveChildBlock
  public void testRemoveChildBlock() {
    
    Node actual = parse("{{x()}}");

    Node outerBlockNode = actual.getFirstChild();
    Node innerBlockNode = outerBlockNode.getFirstChild();
    innerBlockNode.setIsSyntheticBlock(true);

    NodeUtil.removeChild(outerBlockNode, innerBlockNode);
    String expected = "{{}}";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveTryChild1
  public void testRemoveTryChild1() {
    
    Node actual = parse("try {foo()} catch(e) {} finally {}");

    Node tryNode = actual.getFirstChild();
    Node finallyBlock = tryNode.getLastChild();

    NodeUtil.removeChild(tryNode, finallyBlock);
    String expected = "try {foo()} catch(e) {}";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveTryChild2
  public void testRemoveTryChild2() {
    
    Node actual = parse("try {foo()} catch(e) {} finally {}");

    Node tryNode = actual.getFirstChild();
    Node tryBlock = tryNode.getFirstChild();

    NodeUtil.removeChild(tryNode, tryBlock);
    String expected = "try {} catch(e) {} finally {}";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveTryChild3
  public void testRemoveTryChild3() {
    
    Node actual = parse("try {foo()} catch(e) {} finally {}");

    Node tryNode = actual.getFirstChild();
    Node catchBlocks = tryNode.getFirstChild().getNext();
    Node catchBlock = catchBlocks.getFirstChild();

    NodeUtil.removeChild(catchBlocks, catchBlock);
    String expected = "try {foo()} finally {}";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveTryChild4
  public void testRemoveTryChild4() {
    
    Node actual = parse("try {foo()} catch(e) {} finally {}");

    Node tryNode = actual.getFirstChild();
    Node catchBlocks = tryNode.getFirstChild().getNext();

    NodeUtil.removeChild(tryNode, catchBlocks);
    String expected = "try {foo()} finally {}";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveTryChild5
  public void testRemoveTryChild5() {
    Node actual = parse("try {foo()} catch(e) {} finally {}");

    Node tryNode = actual.getFirstChild();
    Node catchBlocks = tryNode.getFirstChild().getNext();
    Node catchBlock = catchBlocks.getFirstChild();

    NodeUtil.removeChild(catchBlocks, catchBlock);
    String expected = "try {foo()} finally {}";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveVarChild
  public void testRemoveVarChild() {
    
    Node actual = parse("var foo, goo, hoo");

    Node varNode = actual.getFirstChild();
    Node nameNode = varNode.getFirstChild();

    NodeUtil.removeChild(varNode, nameNode);
    String expected = "var goo, hoo";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }

    
    actual = parse("var foo, goo, hoo");

    varNode = actual.getFirstChild();
    nameNode = varNode.getFirstChild().getNext();

    NodeUtil.removeChild(varNode, nameNode);
    expected = "var foo, hoo";
    difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }

    
    actual = parse("var foo, hoo");

    varNode = actual.getFirstChild();
    nameNode = varNode.getFirstChild().getNext();

    NodeUtil.removeChild(varNode, nameNode);
    expected = "var foo";
    difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }

    
    actual = parse("var hoo");

    varNode = actual.getFirstChild();
    nameNode = varNode.getFirstChild();

    NodeUtil.removeChild(varNode, nameNode);
    expected = "";
    difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveLabelChild1
  public void testRemoveLabelChild1() {
    
    Node actual = parse("foo: goo()");

    Node labelNode = actual.getFirstChild();
    Node callExpressNode = labelNode.getLastChild();

    NodeUtil.removeChild(labelNode, callExpressNode);
    String expected = "";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveLabelChild2
  public void testRemoveLabelChild2() {
    
    Node actual = parse("achoo: foo: goo()");

    Node labelNode = actual.getFirstChild();
    Node callExpressNode = labelNode.getLastChild();

    NodeUtil.removeChild(labelNode, callExpressNode);
    String expected = "";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveForChild
  public void testRemoveForChild() {
    
    Node actual = parse("for(var a=0;a<0;a++)foo()");

    Node forNode = actual.getFirstChild();
    Node child = forNode.getFirstChild();

    NodeUtil.removeChild(forNode, child);
    String expected = "for(;a<0;a++)foo()";
    String difference = parse(expected).checkTreeEquals(actual);
    assertNull("Nodes do not match:\n" + difference, difference);

    
    actual = parse("for(var a=0;a<0;a++)foo()");

    forNode = actual.getFirstChild();
    child = forNode.getFirstChild().getNext();

    NodeUtil.removeChild(forNode, child);
    expected = "for(var a=0;;a++)foo()";
    difference = parse(expected).checkTreeEquals(actual);
    assertNull("Nodes do not match:\n" + difference, difference);

    
    actual = parse("for(var a=0;a<0;a++)foo()");

    forNode = actual.getFirstChild();
    child = forNode.getFirstChild().getNext().getNext();

    NodeUtil.removeChild(forNode, child);
    expected = "for(var a=0;a<0;)foo()";
    difference = parse(expected).checkTreeEquals(actual);
    assertNull("Nodes do not match:\n" + difference, difference);

    
    actual = parse("for(var a=0;a<0;a++)foo()");

    forNode = actual.getFirstChild();
    child = forNode.getLastChild();

    NodeUtil.removeChild(forNode, child);
    expected = "for(var a=0;a<0;a++);";
    difference = parse(expected).checkTreeEquals(actual);
    assertNull("Nodes do not match:\n" + difference, difference);

    
    actual = parse("for(a in ack)foo();");

    forNode = actual.getFirstChild();
    child = forNode.getLastChild();

    NodeUtil.removeChild(forNode, child);
    expected = "for(a in ack);";
    difference = parse(expected).checkTreeEquals(actual);
    assertNull("Nodes do not match:\n" + difference, difference);
  }

// com.google.javascript.jscomp.NodeUtilTest::testMergeBlock1
  public void testMergeBlock1() {
    
    Node actual = parse("{{a();b();}}");

    Node parentBlock = actual.getFirstChild();
    Node childBlock = parentBlock.getFirstChild();

    assertTrue(NodeUtil.tryMergeBlock(childBlock));
    String expected = "{a();b();}";
    String difference = parse(expected).checkTreeEquals(actual);
    assertNull("Nodes do not match:\n" + difference, difference);
  }

// com.google.javascript.jscomp.NodeUtilTest::testMergeBlock2
  public void testMergeBlock2() {
    
    Node actual = parse("foo:{a();}");

    Node parentLabel = actual.getFirstChild();
    Node childBlock = parentLabel.getLastChild();

    assertFalse(NodeUtil.tryMergeBlock(childBlock));
  }

// com.google.javascript.jscomp.NodeUtilTest::testMergeBlock3
  public void testMergeBlock3() {
    
    String code = "foo:{a();boo()}";
    Node actual = parse("foo:{a();boo()}");

    Node parentLabel = actual.getFirstChild();
    Node childBlock = parentLabel.getLastChild();

    assertFalse(NodeUtil.tryMergeBlock(childBlock));
    String expected = code;
    String difference = parse(expected).checkTreeEquals(actual);
    assertNull("Nodes do not match:\n" + difference, difference);
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetSourceName
  public void testGetSourceName() {
    Node n = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, n);
    parent.setSourceFileForTesting("foo");

    assertEquals("foo", NodeUtil.getSourceName(n));
  }

// com.google.javascript.jscomp.NodeUtilTest::testLocalValue1
  public void testLocalValue1() throws Exception {
    
    assertFalse(testLocalValue("x"));
    assertFalse(testLocalValue("x()"));
    assertFalse(testLocalValue("this"));
    assertFalse(testLocalValue("arguments"));

    
    
    assertFalse(testLocalValue("new x()"));

    
    assertFalse(testLocalValue("(new x()).y"));
    assertFalse(testLocalValue("(new x())['y']"));

    
    assertTrue(testLocalValue("null"));
    assertTrue(testLocalValue("undefined"));
    assertTrue(testLocalValue("Infinity"));
    assertTrue(testLocalValue("NaN"));
    assertTrue(testLocalValue("1"));
    assertTrue(testLocalValue("'a'"));
    assertTrue(testLocalValue("true"));
    assertTrue(testLocalValue("false"));
    assertTrue(testLocalValue("[]"));
    assertTrue(testLocalValue("{}"));

    
    assertTrue(testLocalValue("[x]"));
    assertTrue(testLocalValue("{'a':x}"));

    
    assertTrue(testLocalValue("++x"));
    assertTrue(testLocalValue("--x"));

    
    assertFalse(testLocalValue("x++"));
    assertFalse(testLocalValue("x--"));

    
    assertTrue(testLocalValue("x=1"));
    assertFalse(testLocalValue("x=[]"));
    assertFalse(testLocalValue("x=y"));
    
    
    assertTrue(testLocalValue("x+=y"));
    assertTrue(testLocalValue("x*=y"));
    
    
    assertTrue(testLocalValue("x==y"));
    assertTrue(testLocalValue("x!=y"));
    assertTrue(testLocalValue("x>y"));
    
    assertTrue(testLocalValue("(1,2)"));
    assertTrue(testLocalValue("(x,1)"));
    assertFalse(testLocalValue("(x,y)"));

    
    assertTrue(testLocalValue("1||2"));
    assertFalse(testLocalValue("x||1"));
    assertFalse(testLocalValue("x||y"));
    assertFalse(testLocalValue("1||y"));

    
    assertTrue(testLocalValue("1&&2"));
    assertFalse(testLocalValue("x&&1"));
    assertFalse(testLocalValue("x&&y"));
    assertFalse(testLocalValue("1&&y"));

    
    assertTrue(testLocalValue("x?1:2"));
    assertFalse(testLocalValue("x?x:2"));
    assertFalse(testLocalValue("x?1:x"));
    assertFalse(testLocalValue("x?x:y"));

    
    assertTrue(testLocalValue("!y"));
    assertTrue(testLocalValue("~y"));
    assertTrue(testLocalValue("y + 1"));
    assertTrue(testLocalValue("y + z"));
    assertTrue(testLocalValue("y * z"));

    assertTrue(testLocalValue("'a' in x"));
    assertTrue(testLocalValue("typeof x"));
    assertTrue(testLocalValue("x instanceof y"));

    assertTrue(testLocalValue("void x"));
    assertTrue(testLocalValue("void 0"));

    assertFalse(testLocalValue("{}.x"));

    assertTrue(testLocalValue("{}.toString()"));
    assertTrue(testLocalValue("o.toString()"));

    assertFalse(testLocalValue("o.valueOf()"));

    assertTrue(testLocalValue("delete a.b"));
  }

// com.google.javascript.jscomp.NodeUtilTest::testLocalValue2
  public void testLocalValue2() {
    Node newExpr = getNode("new x()");
    assertFalse(NodeUtil.evaluatesToLocalValue(newExpr));

    Preconditions.checkState(newExpr.isNew());
    Node.SideEffectFlags flags = new Node.SideEffectFlags();

    flags.clearAllFlags();
    newExpr.setSideEffectFlags(flags.valueOf());

    assertTrue(NodeUtil.evaluatesToLocalValue(newExpr));

    flags.clearAllFlags();
    flags.setMutatesThis();
    newExpr.setSideEffectFlags(flags.valueOf());

    assertTrue(NodeUtil.evaluatesToLocalValue(newExpr));

    flags.clearAllFlags();
    flags.setReturnsTainted();
    newExpr.setSideEffectFlags(flags.valueOf());

    assertTrue(NodeUtil.evaluatesToLocalValue(newExpr));

    flags.clearAllFlags();
    flags.setThrows();
    newExpr.setSideEffectFlags(flags.valueOf());

    assertFalse(NodeUtil.evaluatesToLocalValue(newExpr));

    flags.clearAllFlags();
    flags.setMutatesArguments();
    newExpr.setSideEffectFlags(flags.valueOf());

    assertFalse(NodeUtil.evaluatesToLocalValue(newExpr));

    flags.clearAllFlags();
    flags.setMutatesGlobalState();
    newExpr.setSideEffectFlags(flags.valueOf());

    assertFalse(NodeUtil.evaluatesToLocalValue(newExpr));
  }

// com.google.javascript.jscomp.NodeUtilTest::testCallSideEffects
  public void testCallSideEffects() {
    Node callExpr = getNode("new x().method()");
    assertTrue(NodeUtil.functionCallHasSideEffects(callExpr));

    Node newExpr = callExpr.getFirstChild().getFirstChild();
    Preconditions.checkState(newExpr.isNew());
    Node.SideEffectFlags flags = new Node.SideEffectFlags();

    
    flags.clearAllFlags();
    newExpr.setSideEffectFlags(flags.valueOf());
    flags.clearAllFlags();
    callExpr.setSideEffectFlags(flags.valueOf());

    assertTrue(NodeUtil.evaluatesToLocalValue(callExpr));
    assertFalse(NodeUtil.functionCallHasSideEffects(callExpr));
    assertFalse(NodeUtil.mayHaveSideEffects(callExpr));

    
    flags.clearAllFlags();
    newExpr.setSideEffectFlags(flags.valueOf());
    flags.clearAllFlags();
    flags.setMutatesThis();
    callExpr.setSideEffectFlags(flags.valueOf());

    assertTrue(NodeUtil.evaluatesToLocalValue(callExpr));
    assertFalse(NodeUtil.functionCallHasSideEffects(callExpr));
    assertFalse(NodeUtil.mayHaveSideEffects(callExpr));

    
    flags.clearAllFlags();
    newExpr.setSideEffectFlags(flags.valueOf());
    flags.clearAllFlags();
    flags.setMutatesThis();
    flags.setReturnsTainted();
    callExpr.setSideEffectFlags(flags.valueOf());

    assertFalse(NodeUtil.evaluatesToLocalValue(callExpr));
    assertFalse(NodeUtil.functionCallHasSideEffects(callExpr));
    assertFalse(NodeUtil.mayHaveSideEffects(callExpr));

    
    flags.clearAllFlags();
    newExpr.setSideEffectFlags(flags.valueOf());
    flags.clearAllFlags();
    flags.setReturnsTainted();
    callExpr.setSideEffectFlags(flags.valueOf());

    assertFalse(NodeUtil.evaluatesToLocalValue(callExpr));
    assertFalse(NodeUtil.functionCallHasSideEffects(callExpr));
    assertFalse(NodeUtil.mayHaveSideEffects(callExpr));

    
    
    flags.clearAllFlags();
    flags.setMutatesGlobalState();
    newExpr.setSideEffectFlags(flags.valueOf());
    flags.clearAllFlags();
    callExpr.setSideEffectFlags(flags.valueOf());

    assertTrue(NodeUtil.evaluatesToLocalValue(callExpr));
    assertFalse(NodeUtil.functionCallHasSideEffects(callExpr));
    assertTrue(NodeUtil.mayHaveSideEffects(callExpr));
  }

// com.google.javascript.jscomp.NodeUtilTest::testValidDefine
  public void testValidDefine() {
    assertTrue(testValidDefineValue("1"));
    assertTrue(testValidDefineValue("-3"));
    assertTrue(testValidDefineValue("true"));
    assertTrue(testValidDefineValue("false"));
    assertTrue(testValidDefineValue("'foo'"));

    assertFalse(testValidDefineValue("x"));
    assertFalse(testValidDefineValue("null"));
    assertFalse(testValidDefineValue("undefined"));
    assertFalse(testValidDefineValue("NaN"));

    assertTrue(testValidDefineValue("!true"));
    assertTrue(testValidDefineValue("-true"));
    assertTrue(testValidDefineValue("1 & 8"));
    assertTrue(testValidDefineValue("1 + 8"));
    assertTrue(testValidDefineValue("'a' + 'b'"));

    assertFalse(testValidDefineValue("1 & foo"));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetNumberValue
  public void testGetNumberValue() {
    
    assertEquals(1.0, NodeUtil.getNumberValue(getNode("'\\uFEFF1'")));
    assertEquals(0.0, NodeUtil.getNumberValue(getNode("''")));
    assertEquals(0.0, NodeUtil.getNumberValue(getNode("' '")));
    assertEquals(0.0, NodeUtil.getNumberValue(getNode("' \\t'")));
    assertEquals(0.0, NodeUtil.getNumberValue(getNode("'+0'")));
    assertEquals(-0.0, NodeUtil.getNumberValue(getNode("'-0'")));
    assertEquals(2.0, NodeUtil.getNumberValue(getNode("'+2'")));
    assertEquals(-1.6, NodeUtil.getNumberValue(getNode("'-1.6'")));
    assertEquals(16.0, NodeUtil.getNumberValue(getNode("'16'")));
    assertEquals(16.0, NodeUtil.getNumberValue(getNode("' 16 '")));
    assertEquals(16.0, NodeUtil.getNumberValue(getNode("' 16 '")));
    assertEquals(12300.0, NodeUtil.getNumberValue(getNode("'123e2'")));
    assertEquals(12300.0, NodeUtil.getNumberValue(getNode("'123E2'")));
    assertEquals(1.23, NodeUtil.getNumberValue(getNode("'123e-2'")));
    assertEquals(1.23, NodeUtil.getNumberValue(getNode("'123E-2'")));
    assertEquals(-1.23, NodeUtil.getNumberValue(getNode("'-123e-2'")));
    assertEquals(-1.23, NodeUtil.getNumberValue(getNode("'-123E-2'")));
    assertEquals(1.23, NodeUtil.getNumberValue(getNode("'+123e-2'")));
    assertEquals(1.23, NodeUtil.getNumberValue(getNode("'+123E-2'")));
    assertEquals(12300.0, NodeUtil.getNumberValue(getNode("'+123e+2'")));
    assertEquals(12300.0, NodeUtil.getNumberValue(getNode("'+123E+2'")));

    assertEquals(15.0, NodeUtil.getNumberValue(getNode("'0xf'")));
    assertEquals(15.0, NodeUtil.getNumberValue(getNode("'0xF'")));

    
    
    assertEquals(null, NodeUtil.getNumberValue(getNode("'-0xf'")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("'-0xF'")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("'+0xf'")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("'+0xF'")));

    assertEquals(16.0, NodeUtil.getNumberValue(getNode("'0X10'")));
    assertEquals(Double.NaN, NodeUtil.getNumberValue(getNode("'0X10.8'")));
    assertEquals(77.0, NodeUtil.getNumberValue(getNode("'077'")));
    assertEquals(-77.0, NodeUtil.getNumberValue(getNode("'-077'")));
    assertEquals(-77.5, NodeUtil.getNumberValue(getNode("'-077.5'")));
    assertEquals(
        Double.NEGATIVE_INFINITY,
        NodeUtil.getNumberValue(getNode("'-Infinity'")));
    assertEquals(
        Double.POSITIVE_INFINITY,
        NodeUtil.getNumberValue(getNode("'Infinity'")));
    assertEquals(
        Double.POSITIVE_INFINITY,
        NodeUtil.getNumberValue(getNode("'+Infinity'")));
    
    assertEquals(null, NodeUtil.getNumberValue(getNode("'-infinity'")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("'infinity'")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("'+infinity'")));

    assertEquals(Double.NaN, NodeUtil.getNumberValue(getNode("'NaN'")));
    assertEquals(
        Double.NaN, NodeUtil.getNumberValue(getNode("'some unknown string'")));
    assertEquals(Double.NaN, NodeUtil.getNumberValue(getNode("'123 blah'")));

    
    assertEquals(1.0, NodeUtil.getNumberValue(getNode("1")));
    
    assertEquals(-1.0, NodeUtil.getNumberValue(getNode("-1")));
    
    assertEquals(null, NodeUtil.getNumberValue(getNode("+1")));
    assertEquals(22.0, NodeUtil.getNumberValue(getNode("22")));
    assertEquals(18.0, NodeUtil.getNumberValue(getNode("022")));
    assertEquals(34.0, NodeUtil.getNumberValue(getNode("0x22")));

    assertEquals(
        1.0, NodeUtil.getNumberValue(getNode("true")));
    assertEquals(
        0.0, NodeUtil.getNumberValue(getNode("false")));
    assertEquals(
        0.0, NodeUtil.getNumberValue(getNode("null")));
    assertEquals(
        Double.NaN, NodeUtil.getNumberValue(getNode("void 0")));
    assertEquals(
        Double.NaN, NodeUtil.getNumberValue(getNode("void f")));
    
    assertEquals(
        null, NodeUtil.getNumberValue(getNode("void f()")));
    assertEquals(
        Double.NaN, NodeUtil.getNumberValue(getNode("NaN")));
    assertEquals(
        Double.POSITIVE_INFINITY,
        NodeUtil.getNumberValue(getNode("Infinity")));
    assertEquals(
        Double.NEGATIVE_INFINITY,
        NodeUtil.getNumberValue(getNode("-Infinity")));

    
    assertEquals(null, NodeUtil.getNumberValue(getNode("infinity")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("-infinity")));

    
    assertEquals(null, NodeUtil.getNumberValue(getNode("x")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("x.y")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("1/2")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("1-2")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("+1")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsNumbericResult
  public void testIsNumbericResult() {
    assertTrue(NodeUtil.isNumericResult(getNode("1")));
    assertFalse(NodeUtil.isNumericResult(getNode("true")));
    assertTrue(NodeUtil.isNumericResult(getNode("+true")));
    assertTrue(NodeUtil.isNumericResult(getNode("+1")));
    assertTrue(NodeUtil.isNumericResult(getNode("-1")));
    assertTrue(NodeUtil.isNumericResult(getNode("-Infinity")));
    assertTrue(NodeUtil.isNumericResult(getNode("Infinity")));
    assertTrue(NodeUtil.isNumericResult(getNode("NaN")));
    assertFalse(NodeUtil.isNumericResult(getNode("undefined")));
    assertFalse(NodeUtil.isNumericResult(getNode("void 0")));

    assertTrue(NodeUtil.isNumericResult(getNode("a << b")));
    assertTrue(NodeUtil.isNumericResult(getNode("a >> b")));
    assertTrue(NodeUtil.isNumericResult(getNode("a >>> b")));

    assertFalse(NodeUtil.isNumericResult(getNode("a == b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a != b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a === b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a !== b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a < b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a > b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a <= b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a >= b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a in b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a instanceof b")));

    assertFalse(NodeUtil.isNumericResult(getNode("'a'")));
    assertFalse(NodeUtil.isNumericResult(getNode("'a'+b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a+'b'")));
    assertFalse(NodeUtil.isNumericResult(getNode("a+b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a()")));
    assertFalse(NodeUtil.isNumericResult(getNode("''.a")));
    assertFalse(NodeUtil.isNumericResult(getNode("a.b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a.b()")));
    assertFalse(NodeUtil.isNumericResult(getNode("a().b()")));
    assertFalse(NodeUtil.isNumericResult(getNode("new a()")));

    
    assertFalse(NodeUtil.isNumericResult(getNode("([1,2])")));
    assertFalse(NodeUtil.isNumericResult(getNode("({a:1})")));

    
    assertTrue(NodeUtil.isNumericResult(getNode("1 && 2")));
    assertTrue(NodeUtil.isNumericResult(getNode("1 || 2")));
    assertTrue(NodeUtil.isNumericResult(getNode("a ? 2 : 3")));
    assertTrue(NodeUtil.isNumericResult(getNode("a,1")));
    assertTrue(NodeUtil.isNumericResult(getNode("a=1")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsBooleanResult
  public void testIsBooleanResult() {
    assertFalse(NodeUtil.isBooleanResult(getNode("1")));
    assertTrue(NodeUtil.isBooleanResult(getNode("true")));
    assertFalse(NodeUtil.isBooleanResult(getNode("+true")));
    assertFalse(NodeUtil.isBooleanResult(getNode("+1")));
    assertFalse(NodeUtil.isBooleanResult(getNode("-1")));
    assertFalse(NodeUtil.isBooleanResult(getNode("-Infinity")));
    assertFalse(NodeUtil.isBooleanResult(getNode("Infinity")));
    assertFalse(NodeUtil.isBooleanResult(getNode("NaN")));
    assertFalse(NodeUtil.isBooleanResult(getNode("undefined")));
    assertFalse(NodeUtil.isBooleanResult(getNode("void 0")));

    assertFalse(NodeUtil.isBooleanResult(getNode("a << b")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a >> b")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a >>> b")));

    assertTrue(NodeUtil.isBooleanResult(getNode("a == b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a != b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a === b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a !== b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a < b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a > b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a <= b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a >= b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a in b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a instanceof b")));

    assertFalse(NodeUtil.isBooleanResult(getNode("'a'")));
    assertFalse(NodeUtil.isBooleanResult(getNode("'a'+b")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a+'b'")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a+b")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a()")));
    assertFalse(NodeUtil.isBooleanResult(getNode("''.a")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a.b")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a.b()")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a().b()")));
    assertFalse(NodeUtil.isBooleanResult(getNode("new a()")));
    assertTrue(NodeUtil.isBooleanResult(getNode("delete a")));

    
    assertFalse(NodeUtil.isBooleanResult(getNode("([true,false])")));
    assertFalse(NodeUtil.isBooleanResult(getNode("({a:true})")));

    
    assertTrue(NodeUtil.isBooleanResult(getNode("true && false")));
    assertTrue(NodeUtil.isBooleanResult(getNode("true || false")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a ? true : false")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a,true")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a=true")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a=1")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testMayBeString
  public void testMayBeString() {
    assertFalse(NodeUtil.mayBeString(getNode("1")));
    assertFalse(NodeUtil.mayBeString(getNode("true")));
    assertFalse(NodeUtil.mayBeString(getNode("+true")));
    assertFalse(NodeUtil.mayBeString(getNode("+1")));
    assertFalse(NodeUtil.mayBeString(getNode("-1")));
    assertFalse(NodeUtil.mayBeString(getNode("-Infinity")));
    assertFalse(NodeUtil.mayBeString(getNode("Infinity")));
    assertFalse(NodeUtil.mayBeString(getNode("NaN")));
    assertFalse(NodeUtil.mayBeString(getNode("undefined")));
    assertFalse(NodeUtil.mayBeString(getNode("void 0")));
    assertFalse(NodeUtil.mayBeString(getNode("null")));

    assertFalse(NodeUtil.mayBeString(getNode("a << b")));
    assertFalse(NodeUtil.mayBeString(getNode("a >> b")));
    assertFalse(NodeUtil.mayBeString(getNode("a >>> b")));

    assertFalse(NodeUtil.mayBeString(getNode("a == b")));
    assertFalse(NodeUtil.mayBeString(getNode("a != b")));
    assertFalse(NodeUtil.mayBeString(getNode("a === b")));
    assertFalse(NodeUtil.mayBeString(getNode("a !== b")));
    assertFalse(NodeUtil.mayBeString(getNode("a < b")));
    assertFalse(NodeUtil.mayBeString(getNode("a > b")));
    assertFalse(NodeUtil.mayBeString(getNode("a <= b")));
    assertFalse(NodeUtil.mayBeString(getNode("a >= b")));
    assertFalse(NodeUtil.mayBeString(getNode("a in b")));
    assertFalse(NodeUtil.mayBeString(getNode("a instanceof b")));

    assertTrue(NodeUtil.mayBeString(getNode("'a'")));
    assertTrue(NodeUtil.mayBeString(getNode("'a'+b")));
    assertTrue(NodeUtil.mayBeString(getNode("a+'b'")));
    assertTrue(NodeUtil.mayBeString(getNode("a+b")));
    assertTrue(NodeUtil.mayBeString(getNode("a()")));
    assertTrue(NodeUtil.mayBeString(getNode("''.a")));
    assertTrue(NodeUtil.mayBeString(getNode("a.b")));
    assertTrue(NodeUtil.mayBeString(getNode("a.b()")));
    assertTrue(NodeUtil.mayBeString(getNode("a().b()")));
    assertTrue(NodeUtil.mayBeString(getNode("new a()")));

    
    assertFalse(NodeUtil.mayBeString(getNode("1 && 2")));
    assertFalse(NodeUtil.mayBeString(getNode("1 || 2")));
    assertFalse(NodeUtil.mayBeString(getNode("1 ? 2 : 3")));
    assertFalse(NodeUtil.mayBeString(getNode("1,2")));
    assertFalse(NodeUtil.mayBeString(getNode("a=1")));
    assertFalse(NodeUtil.mayBeString(getNode("1+1")));
    assertFalse(NodeUtil.mayBeString(getNode("true+true")));
    assertFalse(NodeUtil.mayBeString(getNode("null+null")));
    assertFalse(NodeUtil.mayBeString(getNode("NaN+NaN")));

    
    assertTrue(NodeUtil.mayBeString(getNode("([1,2])")));
    assertTrue(NodeUtil.mayBeString(getNode("({a:1})")));
    assertTrue(NodeUtil.mayBeString(getNode("({}+1)")));
    assertTrue(NodeUtil.mayBeString(getNode("(1+{})")));
    assertTrue(NodeUtil.mayBeString(getNode("([]+1)")));
    assertTrue(NodeUtil.mayBeString(getNode("(1+[])")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testValidNames
  public void testValidNames() {
    assertTrue(NodeUtil.isValidPropertyName("a"));
    assertTrue(NodeUtil.isValidPropertyName("a3"));
    assertFalse(NodeUtil.isValidPropertyName("3a"));
    assertFalse(NodeUtil.isValidPropertyName("a."));
    assertFalse(NodeUtil.isValidPropertyName(".a"));
    assertFalse(NodeUtil.isValidPropertyName("a.b"));
    assertFalse(NodeUtil.isValidPropertyName("true"));
    assertFalse(NodeUtil.isValidPropertyName("a.true"));
    assertFalse(NodeUtil.isValidPropertyName("a..b"));

    assertTrue(NodeUtil.isValidSimpleName("a"));
    assertTrue(NodeUtil.isValidSimpleName("a3"));
    assertFalse(NodeUtil.isValidSimpleName("3a"));
    assertFalse(NodeUtil.isValidSimpleName("a."));
    assertFalse(NodeUtil.isValidSimpleName(".a"));
    assertFalse(NodeUtil.isValidSimpleName("a.b"));
    assertFalse(NodeUtil.isValidSimpleName("true"));
    assertFalse(NodeUtil.isValidSimpleName("a.true"));
    assertFalse(NodeUtil.isValidSimpleName("a..b"));

    assertTrue(NodeUtil.isValidQualifiedName("a"));
    assertTrue(NodeUtil.isValidQualifiedName("a3"));
    assertFalse(NodeUtil.isValidQualifiedName("3a"));
    assertFalse(NodeUtil.isValidQualifiedName("a."));
    assertFalse(NodeUtil.isValidQualifiedName(".a"));
    assertTrue(NodeUtil.isValidQualifiedName("a.b"));
    assertFalse(NodeUtil.isValidQualifiedName("true"));
    assertFalse(NodeUtil.isValidQualifiedName("a.true"));
    assertFalse(NodeUtil.isValidQualifiedName("a..b"));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetNearestFunctionName
  public void testGetNearestFunctionName() {
    testFunctionName("(function() {})()", null);
    testFunctionName("function a() {}", "a");
    testFunctionName("(function a() {})", "a");
    testFunctionName("({a:function () {}})", "a");
    testFunctionName("({get a() {}})", "a");
    testFunctionName("({set a(b) {}})", "a");
    testFunctionName("({set a(b) {}})", "a");
    testFunctionName("({1:function () {}})", "1");
    testFunctionName("var a = function a() {}", "a");
    testFunctionName("var a;a = function a() {}", "a");
    testFunctionName("var o;o.a = function a() {}", "o.a");
    testFunctionName("this.a = function a() {}", "this.a");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetBestLValue
  public void testGetBestLValue() {
    assertEquals("x", getFunctionLValue("var x = function() {};"));
    assertEquals("x", getFunctionLValue("x = function() {};"));
    assertEquals("x", getFunctionLValue("function x() {};"));
    assertEquals("x", getFunctionLValue("var x = y ? z : function() {};"));
    assertEquals("x", getFunctionLValue("var x = y ? function() {} : z;"));
    assertEquals("x", getFunctionLValue("var x = y && function() {};"));
    assertEquals("x", getFunctionLValue("var x = y || function() {};"));
    assertEquals("x", getFunctionLValue("var x = (y, function() {});"));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsNaN
  public void testIsNaN() {
    assertEquals(true, NodeUtil.isNaN(getNode("NaN")));
    assertEquals(false, NodeUtil.isNaN(getNode("Infinity")));
    assertEquals(false, NodeUtil.isNaN(getNode("x")));
    assertEquals(true, NodeUtil.isNaN(getNode("0/0")));
    assertEquals(false, NodeUtil.isNaN(getNode("1/0")));
    assertEquals(false, NodeUtil.isNaN(getNode("0/1")));
    assertEquals(false, NodeUtil.isNaN(IR.number(0.0)));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsExecutedExactlyOnce
  public void testIsExecutedExactlyOnce() {
    assertEquals(true, executedOnceTestCase("x;"));

    assertEquals(true, executedOnceTestCase("x && 1;"));
    assertEquals(false, executedOnceTestCase("1 && x;"));

    assertEquals(false, executedOnceTestCase("1 && (x && 1);"));

    assertEquals(true, executedOnceTestCase("x || 1;"));
    assertEquals(false, executedOnceTestCase("1 || x;"));

    assertEquals(false, executedOnceTestCase("1 && (x || 1);"));

    assertEquals(true, executedOnceTestCase("x ? 1 : 2;"));
    assertEquals(false, executedOnceTestCase("1 ? 1 : x;"));
    assertEquals(false, executedOnceTestCase("1 ? x : 2;"));

    assertEquals(false, executedOnceTestCase("1 && (x ? 1 : 2);"));

    assertEquals(true, executedOnceTestCase("if (x) {}"));
    assertEquals(false, executedOnceTestCase("if (true) {x;}"));
    assertEquals(false, executedOnceTestCase("if (true) {} else {x;}"));

    assertEquals(false, executedOnceTestCase("if (1) { if (x) {} }"));

    assertEquals(true, executedOnceTestCase("for(x;;){}"));
    assertEquals(false, executedOnceTestCase("for(;x;){}"));
    assertEquals(false, executedOnceTestCase("for(;;x){}"));
    assertEquals(false, executedOnceTestCase("for(;;){x;}"));

    assertEquals(false, executedOnceTestCase("if (1) { for(x;;){} }"));

    assertEquals(false, executedOnceTestCase("for(x in {}){}"));
    assertEquals(true, executedOnceTestCase("for({}.a in x){}"));
    assertEquals(false, executedOnceTestCase("for({}.a in {}){x}"));

    assertEquals(false, executedOnceTestCase("if (1) { for(x in {}){} }"));

    assertEquals(true, executedOnceTestCase("switch (x) {}"));
    assertEquals(false, executedOnceTestCase("switch (1) {case x:}"));
    assertEquals(false, executedOnceTestCase("switch (1) {case 1: x}"));
    assertEquals(false, executedOnceTestCase("switch (1) {default: x}"));

    assertEquals(false, executedOnceTestCase("if (1) { switch (x) {} }"));

    assertEquals(false, executedOnceTestCase("while (x) {}"));
    assertEquals(false, executedOnceTestCase("while (1) {x}"));

    assertEquals(false, executedOnceTestCase("do {} while (x)"));
    assertEquals(false, executedOnceTestCase("do {x} while (1)"));

    assertEquals(false, executedOnceTestCase("try {x} catch (e) {}"));
    assertEquals(false, executedOnceTestCase("try {} catch (e) {x}"));
    assertEquals(true, executedOnceTestCase("try {} finally {x}"));

    assertEquals(false, executedOnceTestCase("if (1) { try {} finally {x} }"));
  }

// com.google.javascript.jscomp.NodeUtilTest::testNewQualifiedNameNode1
  public void testNewQualifiedNameNode1() {
    Node actual = NodeUtil.newQualifiedNameNode(
        new GoogleCodingConvention(), "ns.prop");
    Node expected = IR.getprop(
        IR.name("ns"),
        IR.string("prop"));
    assertNodeTreesEqual(expected, actual);
  }

// com.google.javascript.jscomp.NodeUtilTest::testNewQualifiedNameNode2
  public void testNewQualifiedNameNode2() {
    Node actual = NodeUtil.newQualifiedNameNode(
        new GoogleCodingConvention(), "this.prop");
    Node expected = IR.getprop(
        IR.thisNode(),
        IR.string("prop"));
    assertNodeTreesEqual(expected, actual);
  }

// com.google.javascript.jscomp.NormalizeTest::testSplitVar
  public void testSplitVar() {
    testSame("var a");
    test("var a, b",
         "var a; var b");
    test("var a, b, c",
         "var a; var b; var c");
    testSame("var a = 0 ");
    test("var a = 0 , b = foo()",
         "var a = 0; var b = foo()");
    test("var a = 0, b = 1, c = 2",
         "var a = 0; var b = 1; var c = 2");
    test("var a = foo(1), b = foo(2), c = foo(3)",
         "var a = foo(1); var b = foo(2); var c = foo(3)");

    
    test("for(var a = 0, b = foo(1), c = 1; c < b; c++) foo(2)",
         "var a = 0; var b = foo(1); var c = 1; for(; c < b; c++) foo(2)");

    
    test("for(;;) var b = foo(1), c = foo(2);",
        "for(;;){var b = foo(1); var c = foo(2)}");
    test("for(;;){var b = foo(1), c = foo(2);}",
         "for(;;){var b = foo(1); var c = foo(2)}");

    test("try{var b = foo(1), c = foo(2);} finally foo(3);",
         "try{var b = foo(1); var c = foo(2)} finally foo(3);");
    test("try{var b = foo(1),c = foo(2);} finally;",
         "try{var b = foo(1); var c = foo(2)} finally;");
    test("try{foo(0);} finally var b = foo(1), c = foo(2);",
         "try{foo(0);} finally {var b = foo(1); var c = foo(2)}");

    test("switch(a) {default: var b = foo(1), c = foo(2); break;}",
         "switch(a) {default: var b = foo(1); var c = foo(2); break;}");

    test("do var a = foo(1), b; while(false);",
         "do{var a = foo(1); var b} while(false);");
    test("a:var a,b,c;",
         "a:{ var a;var b; var c; }");
    test("a:for(var a,b,c;;);",
         "var a;var b; var c;a:for(;;);");
    test("if (true) a:var a,b;",
         "if (true)a:{ var a; var b; }");
  }

// com.google.javascript.jscomp.NormalizeTest::testDuplicateVarInExterns
  public void testDuplicateVarInExterns() {
    test("var extern;",
         " var extern = 3;", "var extern = 3;",
         null, null);
  }

// com.google.javascript.jscomp.NormalizeTest::testUnhandled
  public void testUnhandled() {
    testSame("var x = y = 1");
  }

// com.google.javascript.jscomp.NormalizeTest::testFor
  public void testFor() {
    
    test("for(a = 0; a < 2 ; a++) foo();",
         "a = 0; for(; a < 2 ; a++) foo()");
    
    test("for(var a = 0; c < b ; c++) foo()",
         "var a = 0; for(; c < b ; c++) foo()");

    
    test("a:for(var a = 0; c < b ; c++) foo()",
         "var a = 0; a:for(; c < b ; c++) foo()");
    
    test("a:b:for(var a = 0; c < b ; c++) foo()",
         "var a = 0; a:b:for(; c < b ; c++) foo()");

    
    test("if(x) for(var a = 0; c < b ; c++) foo()",
         "if(x){var a = 0; for(; c < b ; c++) foo()}");

    
    test("for(init(); a < 2 ; a++) foo();",
         "init(); for(; a < 2 ; a++) foo()");
  }

// com.google.javascript.jscomp.NormalizeTest::testForIn1
  public void testForIn1() {
    
    testSame("for(a in b) foo();");

    
    test("for(var a in b) foo()",
         "var a; for(a in b) foo()");

    
    test("a:for(var a in b) foo()",
         "var a; a:for(a in b) foo()");
    
    test("a:b:for(var a in b) foo()",
         "var a; a:b:for(a in b) foo()");

    
    test("if (x) for(var a in b) foo()",
         "if (x) { var a; for(a in b) foo() }");
  }

// com.google.javascript.jscomp.NormalizeTest::testForIn2
  public void testForIn2() {
    
    test("for(var a = foo() in b) foo()",
         "var a = foo(); for(a in b) foo()");
  }

// com.google.javascript.jscomp.NormalizeTest::testWhile
  public void testWhile() {
    
    test("while(c < b) foo()",
         "for(; c < b;) foo()");
  }

// com.google.javascript.jscomp.NormalizeTest::testMoveFunctions1
  public void testMoveFunctions1() throws Exception {
    test("function f() { if (x) return; foo(); function foo() {} }",
         "function f() {function foo() {} if (x) return; foo(); }");
    test("function f() { " +
            "function foo() {} " +
            "if (x) return;" +
            "foo(); " +
            "function bar() {} " +
         "}",
         "function f() {" +
           "function foo() {}" +
           "function bar() {}" +
           "if (x) return;" +
           "foo();" +
         "}");
  }

// com.google.javascript.jscomp.NormalizeTest::testMoveFunctions2
  public void testMoveFunctions2() throws Exception {
    testSame("function f() { function foo() {} }");
    test("function f() { f(); a:function bar() {} }",
         "function f() { f(); a:{ var bar = function () {} }}");
    test("function f() { f(); {function bar() {}}}",
         "function f() { f(); {var bar = function () {}}}");
    test("function f() { f(); if (true) {function bar() {}}}",
         "function f() { f(); if (true) {var bar = function () {}}}");
  }

// com.google.javascript.jscomp.NormalizeTest::testNormalizeFunctionDeclarations
  public void testNormalizeFunctionDeclarations() throws Exception {
    testSame("function f() {}");
    testSame("var f = function () {}");
    test("var f = function f() {}",
         "var f = function f$$1() {}");
    testSame("var f = function g() {}");
    test("a:function g() {}",
         "a:{ var g = function () {} }");
    test("{function g() {}}",
         "{var g = function () {}}");
    testSame("if (function g() {}) {}");
    test("if (true) {function g() {}}",
         "if (true) {var g = function () {}}");
    test("if (true) {} else {function g() {}}",
         "if (true) {} else {var g = function () {}}");
    testSame("switch (function g() {}) {}");
    test("switch (1) { case 1: function g() {}}",
         "switch (1) { case 1: var g = function () {}}");

    testSameInFunction("function f() {}");
    testInFunction("f(); a:function g() {}",
                   "f(); a:{ var g = function () {} }");
    testInFunction("f(); {function g() {}}",
                   "f(); {var g = function () {}}");
    testInFunction("f(); if (true) {function g() {}}",
                   "f(); if (true) {var g = function () {}}");
    testInFunction("if (true) {} else {function g() {}}",
                   "if (true) {} else {var g = function () {}}");
  }

// com.google.javascript.jscomp.NormalizeTest::testMakeLocalNamesUnique
  public void testMakeLocalNamesUnique() {
    if (!Normalize.MAKE_LOCAL_NAMES_UNIQUE) {
      return;
    }

    
    testSame("var a;");

    
    testSame("a;");

    
    test("var a;function foo(a){var b;a}",
         "var a;function foo(a$$1){var b;a$$1}");
    test("var a;function foo(){var b;a}function boo(){var b;a}",
         "var a;function foo(){var b;a}function boo(){var b$$1;a}");
    test("function foo(a){var b}" +
         "function boo(a){var b}",
         "function foo(a){var b}" +
         "function boo(a$$1){var b$$1}");

    
    test("var a = function foo(){foo()};var b = function foo(){foo()};",
         "var a = function foo(){foo()};var b = function foo$$1(){foo$$1()};");

    
    test("try { } catch(e) {e;}",
         "try { } catch(e) {e;}");
    test("try { } catch(e) {e;}; try { } catch(e) {e;}",
         "try { } catch(e) {e;}; try { } catch(e$$1) {e$$1;}");
    test("try { } catch(e) {e; try { } catch(e) {e;}};",
         "try { } catch(e) {e; try { } catch(e$$1) {e$$1;} }; ");

    
    test("\nvar window;", "var window;");

    
    test("\nvar window;" +
         "\nvar window;", "var window;");

    
    test("function f() {var window}",
         "function f() {var window$$1}");
  }

// com.google.javascript.jscomp.NormalizeTest::testRemoveDuplicateVarDeclarations1
  public void testRemoveDuplicateVarDeclarations1() {
    test("function f() { var a; var a }",
         "function f() { var a; }");
    test("function f() { var a = 1; var a = 2 }",
         "function f() { var a = 1; a = 2 }");
    test("var a = 1; function f(){ var a = 2 }",
         "var a = 1; function f(){ var a$$1 = 2 }");
    test("function f() { var a = 1; lable1:var a = 2 }",
         "function f() { var a = 1; lable1:{a = 2}}");
    test("function f() { var a = 1; lable1:var a }",
         "function f() { var a = 1; lable1:{} }");
    test("function f() { var a = 1; for(var a in b); }",
         "function f() { var a = 1; for(a in b); }");
  }

// com.google.javascript.jscomp.NormalizeTest::testRemoveDuplicateVarDeclarations2
  public void testRemoveDuplicateVarDeclarations2() {
    test("var e = 1; function f(){ try {} catch (e) {} var e = 2 }",
         "var e = 1; function f(){ try {} catch (e$$2) {} var e$$1 = 2 }");
  }

// com.google.javascript.jscomp.NormalizeTest::testRemoveDuplicateVarDeclarations3
  public void testRemoveDuplicateVarDeclarations3() {
    test("var f = 1; function f(){}",
         "f = 1; function f(){}");
    test("var f; function f(){}",
         "function f(){}");
    test("if (a) { var f = 1; } else { function f(){} }",
         "if (a) { var f = 1; } else { f = function (){} }");

    test("function f(){} var f = 1;",
         "function f(){} f = 1;");
    test("function f(){} var f;",
         "function f(){}");
    test("if (a) { function f(){} } else { var f = 1; }",
         "if (a) { var f = function (){} } else { f = 1; }");

    
    
    test("function f(){} function f(){}",
         "function f(){} function f(){}",
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
    test("if (a) { function f(){} } else { function f(){} }",
         "if (a) { var f = function (){} } else { f = function (){} }");
  }

// com.google.javascript.jscomp.NormalizeTest::testRenamingConstants
  public void testRenamingConstants() {
    test("var ACONST = 4;var b = ACONST;",
         "var ACONST = 4; var b = ACONST;");

    test("var a, ACONST = 4;var b = ACONST;",
         "var a; var ACONST = 4; var b = ACONST;");

    test("var ACONST; ACONST = 4; var b = ACONST;",
         "var ACONST; ACONST = 4;" +
         "var b = ACONST;");

    test("var ACONST = new Foo(); var b = ACONST;",
         "var ACONST = new Foo(); var b = ACONST;");

    test("var aa; aa=1;", "var aa;aa=1");
  }

// com.google.javascript.jscomp.NormalizeTest::testSkipRenamingExterns
  public void testSkipRenamingExterns() {
    test("var EXTERN; var ext; ext.FOO;", "var b = EXTERN; var c = ext.FOO",
         "var b = EXTERN; var c = ext.FOO", null, null);
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166a
  public void testIssue166a() {
    test("try { throw 1 } catch(e) {  var e=2 }",
         "try { throw 1 } catch(e) { var e=2 }",
         Normalize.CATCH_BLOCK_VAR_ERROR);
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166b
  public void testIssue166b() {
    test("function a() {" +
         "try { throw 1 } catch(e) {  var e=2 }" +
         "};",
         "function a() {" +
         "try { throw 1 } catch(e) { var e=2 }" +
         "}",
         Normalize.CATCH_BLOCK_VAR_ERROR);
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166c
  public void testIssue166c() {
    test("var e = 0; try { throw 1 } catch(e) {" +
             " var e=2 }",
         "var e = 0; try { throw 1 } catch(e) { var e=2 }",
         Normalize.CATCH_BLOCK_VAR_ERROR);
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166d
  public void testIssue166d() {
    test("function a() {" +
         "var e = 0; try { throw 1 } catch(e) {" +
             " var e=2 }" +
         "};",
         "function a() {" +
         "var e = 0; try { throw 1 } catch(e) { var e=2 }" +
         "}",
         Normalize.CATCH_BLOCK_VAR_ERROR);
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166e
  public void testIssue166e() {
    test("var e = 2; try { throw 1 } catch(e) {}",
         "var e = 2; try { throw 1 } catch(e$$1) {}");
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166f
  public void testIssue166f() {
    test("function a() {" +
         "var e = 2; try { throw 1 } catch(e) {}" +
         "}",
         "function a() {" +
         "var e = 2; try { throw 1 } catch(e$$1) {}" +
         "}");
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue
  public void testIssue() {
    super.allowExternsChanges(true);
    test("var a,b,c; var a,b", "a(), b()", "a(), b()", null, null);
  }

// com.google.javascript.jscomp.NormalizeTest::testNormalizeSyntheticCode
  public void testNormalizeSyntheticCode() {
    Compiler compiler = new Compiler();
    compiler.init(
        Lists.<SourceFile>newArrayList(),
        Lists.<SourceFile>newArrayList(), new CompilerOptions());
    Node code = Normalize.parseAndNormalizeSyntheticCode(
        compiler, "function f(x) {} function g(x) {}", "prefix_");
    assertEquals(
        "function f(x$$prefix_0){}function g(x$$prefix_1){}",
        compiler.toSource(code));
  }

// com.google.javascript.jscomp.NormalizeTest::testIsConstant
  public void testIsConstant() throws Exception {
    testSame("var CONST = 3; var b = CONST;");
    Node n = getLastCompiler().getRoot();

    Set<Node> constantNodes = findNodesWithProperty(n, Node.IS_CONSTANT_NAME);
    assertEquals(2, constantNodes.size());
    for (Node hasProp : constantNodes) {
      assertEquals("CONST", hasProp.getString());
    }
  }

// com.google.javascript.jscomp.NormalizeTest::testPropertyIsConstant1
  public void testPropertyIsConstant1() throws Exception {
    testSame("var a = {};a.CONST = 3; var b = a.CONST;");
    Node n = getLastCompiler().getRoot();

    Set<Node> constantNodes = findNodesWithProperty(n, Node.IS_CONSTANT_NAME);
    assertEquals(2, constantNodes.size());
    for (Node hasProp : constantNodes) {
      assertEquals("CONST", hasProp.getString());
    }
  }

// com.google.javascript.jscomp.NormalizeTest::testPropertyIsConstant2
  public void testPropertyIsConstant2() throws Exception {
    testSame("var a = {CONST: 3}; var b = a.CONST;");
    Node n = getLastCompiler().getRoot();

    Set<Node> constantNodes = findNodesWithProperty(n, Node.IS_CONSTANT_NAME);
    assertEquals(2, constantNodes.size());
    for (Node hasProp : constantNodes) {
      assertEquals("CONST", hasProp.getString());
    }
  }

// com.google.javascript.jscomp.NormalizeTest::testGetterPropertyIsConstant
  public void testGetterPropertyIsConstant() throws Exception {
    testSame("var a = { get CONST() {return 3} }; " +
             "var b = a.CONST;");
    Node n = getLastCompiler().getRoot();

    Set<Node> constantNodes = findNodesWithProperty(n, Node.IS_CONSTANT_NAME);
    assertEquals(2, constantNodes.size());
    for (Node hasProp : constantNodes) {
      assertEquals("CONST", hasProp.getString());
    }
  }

// com.google.javascript.jscomp.NormalizeTest::testSetterPropertyIsConstant
  public void testSetterPropertyIsConstant() throws Exception {
    
    testSame("var a = { set CONST(b) {throw 'invalid'} }; " +
             "var c = a.CONST;");
    Node n = getLastCompiler().getRoot();

    Set<Node> constantNodes = findNodesWithProperty(n, Node.IS_CONSTANT_NAME);
    assertEquals(2, constantNodes.size());
    for (Node hasProp : constantNodes) {
      assertEquals("CONST", hasProp.getString());
    }
  }

// com.google.javascript.jscomp.NormalizeTest::testExposeSimple
  public void testExposeSimple() {
    test("var x = {};  x.y = 3; x.y = 5;",
         "var x = {}; x['y'] = 3; x['y'] = 5;");
  }

// com.google.javascript.jscomp.NormalizeTest::testExposeComplex
  public void testExposeComplex() {
    test(
        "var x = { a: 1, b: 2};"
        + "x.a = 3;  x.b = 5;",
        "var x = {'a': 1, 'b': 2};"
        + "x['a'] = 3; x['b'] = 5;");
  }

// com.google.javascript.jscomp.NormalizeTest::testRenamingConstantProperties
  public void testRenamingConstantProperties() {
    
    
    
    new WithCollapse().testConstantProperties();
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoRemoval
  public void testNoRemoval() {
    testSame("function foo(p1) { } foo(1); foo(2)");
    testSame("function foo(p1) { } foo(1,2); foo(3,4)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testSimpleRemoval
  public void testSimpleRemoval() {
    test("function foo(p1) { } foo(); foo()",
         "function foo() {var p1;} foo(); foo()");
    test("function foo(p1) { } foo(1); foo(1)",
         "function foo() {var p1 = 1;} foo(); foo()");
    test("function foo(p1) { } foo(1,2); foo(1,4)",
         "function foo() {var p1 = 1;} foo(2); foo(4)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNotAFunction
  public void testNotAFunction() {
    testSame("var x = 1; x; x = 2");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalNamedFunction
  public void testRemoveOneOptionalNamedFunction() {
    test("function foo(p1) { } foo()", "function foo() {var p1} foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDifferentScopes
  public void testDifferentScopes() {
    test("function f(a, b) {} f(1, 2); f(1, 3); " +
        "function h() {function g(a) {} g(4); g(5);} f(1, 2);",
        "function f(b) {var a = 1} f(2); f(3); " +
        "function h() {function g(a) {} g(4); g(5);} f(2);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testOptimizeOnlyImmutableValues
  public void testOptimizeOnlyImmutableValues() {
    test("function foo(a) {}; foo(undefined);",
         "function foo() {var a = undefined}; foo()");
    test("function foo(a) {}; foo(null);",
        "function foo() {var a = null}; foo()");
    test("function foo(a) {}; foo(1);",
         "function foo() {var a = 1}; foo()");
    test("function foo(a) {}; foo('abc');",
        "function foo() {var a = 'abc'}; foo()");

    test("var foo = function(a) {}; foo(undefined);",
         "var foo = function() {var a = undefined}; foo()");
    test("var foo = function(a) {}; foo(null);",
         "var foo = function() {var a = null}; foo()");
    test("var foo = function(a) {}; foo(1);",
         "var foo = function() {var a = 1}; foo()");
    test("var foo = function(a) {}; foo('abc');",
         "var foo = function() {var a = 'abc'}; foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalVarAssignment
  public void testRemoveOneOptionalVarAssignment() {
    test("var foo = function (p1) { }; foo()",
        "var foo = function () {var p1}; foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoOptimizeCall
  public void testDoOptimizeCall() {
    testSame("var foo = function () {}; foo(); foo.call();");
    
    testSame("var foo = function () {}; foo(); foo.call(this);");
    testSame("var foo = function (a, b) {}; foo(1); foo.call(this, 1);");
    testSame("var foo = function () {}; foo(); foo.call(null);");
    testSame("var foo = function (a, b) {}; foo(1); foo.call(null, 1);");

    testSame("var foo = function () {}; foo.call();");
    
    testSame("var foo = function () {}; foo.call(this);");
    testSame("var foo = function (a, b) {}; foo.call(this, 1);");
    testSame("var foo = function () {}; foo.call(null);");
    testSame("var foo = function (a, b) {}; foo.call(null, 1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoOptimizeApply
  public void testDoOptimizeApply() {
    testSame("var foo = function () {}; foo(); foo.apply();");
    testSame("var foo = function () {}; foo(); foo.apply(this);");
    testSame("var foo = function (a, b) {}; foo(1); foo.apply(this, 1);");
    testSame("var foo = function () {}; foo(); foo.apply(null);");
    testSame("var foo = function (a, b) {}; foo(1); foo.apply(null, []);");

    testSame("var foo = function () {}; foo.apply();");
    testSame("var foo = function () {}; foo.apply(this);");
    testSame("var foo = function (a, b) {}; foo.apply(this, 1);");
    testSame("var foo = function () {}; foo.apply(null);");
    testSame("var foo = function (a, b) {}; foo.apply(null, []);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalExpressionAssign
  public void testRemoveOneOptionalExpressionAssign() {
    
    
    testSame("var foo; foo = function (p1) { }; foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalOneRequired
  public void testRemoveOneOptionalOneRequired() {
    test("function foo(p1, p2) { } foo(1); foo(2)",
        "function foo(p1) {var p2} foo(1); foo(2)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalMultipleCalls
  public void testRemoveOneOptionalMultipleCalls() {
    test( "function foo(p1, p2) { } foo(1); foo(2); foo()",
        "function foo(p1) {var p2} foo(1); foo(2); foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalMultiplePossibleDefinition
  public void testRemoveOneOptionalMultiplePossibleDefinition() {
    
    String src = "var goog = {};" +
        "goog.foo = function (p1, p2) { };" +
        "goog.foo = function (q1, q2) { };" +
        "goog.foo = function (r1, r2) { };" +
        "goog.foo(1); goog.foo(2); goog.foo()";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveTwoOptionalMultiplePossibleDefinition
  public void testRemoveTwoOptionalMultiplePossibleDefinition() {
    
    String src = "var goog = {};" +
        "goog.foo = function (p1, p2, p3, p4) { };" +
        "goog.foo = function (q1, q2, q3, q4) { };" +
        "goog.foo = function (r1, r2, r3, r4) { };" +
        "goog.foo(1,0); goog.foo(2,1); goog.foo()";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testConstructorOptArgsNotRemoved
  public void testConstructorOptArgsNotRemoved() {
    String src =
        "" +
        "var goog = function(){};" +
        "goog.prototype.foo = function(a,b) {};" +
        "goog.prototype.bar = function(a) {};" +
        "goog.bar.inherits(goog.foo);" +
        "new goog.foo(2,3);" +
        "new goog.foo(1,2);";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMultipleUnknown
  public void testMultipleUnknown() {
    
    String src = "var goog1 = {};" +
        "goog1.foo = function () { };" +
        "var goog2 = {};" +
        "goog2.foo = function (p1) { };" +
        "var x = getGoog();" +
        "x.foo()";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testSingleUnknown
  public void testSingleUnknown() {
    String src =
        "var goog2 = {};" +
        "goog2.foo = function (p1) { };" +
        "var x = getGoog();" +
        "x.foo()";

    String expected =
        "var goog2 = {};" +
        "goog2.foo = function () { var p1 };" +
        "var x = getGoog();" +
        "x.foo()";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveVarArg
  public void testRemoveVarArg() {
    test("function foo(p1, var_args) { } foo(1); foo(2)",
        "function foo(p1) { var var_args } foo(1); foo(2)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testAliasMethodsDontGetOptimize
  public void testAliasMethodsDontGetOptimize() {
    String src =
        "var foo = function(a, b) {};" +
        "var goog = {};" +
        "goog.foo = foo;" +
        "goog.prototype.bar = goog.foo;" +
        "new goog().bar(1,2);" +
        "foo(2);";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testAliasMethodsDontGetOptimize2
  public void testAliasMethodsDontGetOptimize2() {
    String src =
        "var foo = function(a, b) {};" +
        "var bar = foo;" +
        "foo(1);" +
        "bar(2,3);";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testAliasMethodsDontGetOptimize3
  public void testAliasMethodsDontGetOptimize3() {
    String src =
        "var array = {};" +
        "array[0] = function(a, b) {};" +
        "var foo = array[0];" + 
        "foo(1);";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testAliasMethodsDontGetOptimize4
  public void testAliasMethodsDontGetOptimize4() {
    

    test(
      "function foo(bar) {};" +
      "baz = function(a) {};" +
      "baz(1);" +
      "foo(baz);",
      "function foo() {var bar = baz};" +
      "baz = function(a) {};" +
      "baz(1);" +
      "foo();");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMethodsDefinedInArraysDontGetOptimized
  public void testMethodsDefinedInArraysDontGetOptimized() {
    String src =
        "var array = [true, function (a) {}];" +
        "array[1](1)";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMethodsDefinedInObjectDontGetOptimized
  public void testMethodsDefinedInObjectDontGetOptimized() {
    String src =
      "var object = { foo: function bar() {} };" +
      "object.foo(1)";
    testSame(src);
    src =
      "var object = { foo: function bar() {} };" +
      "object['foo'](1)";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveConstantArgument
  public void testRemoveConstantArgument() {
    
    test("function foo(p1, p2) {}; foo(1,2); foo(2,2);",
         "function foo(p1) {var p2 = 2}; foo(1); foo(2)");

    
    testSame("function foo(p1, p2) {}; foo(1); foo(2,3);");

    
    test("function foo(a,b,c){}; foo(1, 2, 3); foo(1, 2, 4); foo(2, 2, 3)",
         "function foo(a,c){var b=2}; foo(1, 3); foo(1, 4); foo(2, 3)");

    
    test("function foo(a) {}; foo(1); foo(1.0);",
         "function foo() {var a = 1;}; foo(); foo();");

    
    String src =
        "" +
        "function Person(){}; Person.prototype.run = function(a, b) {};" +
        "Person.run(1, 'a'); Person.run(2, 'a')";
    String expected =
        "function Person(){}; Person.prototype.run = " +
        "function(a) {var b = 'a'};" +
        "Person.run(1); Person.run(2)";
    test(src, expected);

  }

// com.google.javascript.jscomp.OptimizeParametersTest::testCanDeleteArgumentsAtAnyPosition
  public void testCanDeleteArgumentsAtAnyPosition() {
    
    String src =
        "function foo(a,b,c,d,e) {};" +
        "foo(1,2,3,4,5);" +
        "foo(2,2,4,4,5);";
    String expected =
        "function foo(a,c) {var b=2; var d=4; var e=5;};" +
        "foo(1,3);" +
        "foo(2,4);";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoOptimizationForExternsFunctions
  public void testNoOptimizationForExternsFunctions() {
    testSame("function _foo(x, y, z){}; _foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoOptimizationForGoogExportSymbol
  public void testNoOptimizationForGoogExportSymbol() {
    testSame("goog.exportSymbol('foo', foo);" +
             "function foo(x, y, z){}; foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoArgumentRemovalNonEqualNodes
  public void testNoArgumentRemovalNonEqualNodes() {
    testSame("function foo(a){}; foo('bar'); foo('baz');");
    testSame("function foo(a){}; foo(1.0); foo(2.0);");
    testSame("function foo(a){}; foo(true); foo(false);");
    testSame("var a = 1, b = 2; function foo(a){}; foo(a); foo(b);");
    testSame("function foo(a){}; foo(/&/g); foo(/</g);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testFunctionPassedAsParam
  public void testFunctionPassedAsParam() {
    String src =
        " function person(){}; " +
        "person.prototype.run = function(a, b) {};" +
        "person.prototype.walk = function() {};" +
        "person.prototype.foo = function() { this.run(this.walk, 0.1)};" +
        "person.foo();";
    String expected =
        "function person(){}; person.prototype.run = function(a) {" +
        "  var b = 0.1;};" +
        "person.prototype.walk = function() {};" +
        "person.prototype.foo = function() { this.run(this.walk)};" +
        "person.foo();";

    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testCallIsIgnore
  public void testCallIsIgnore() {
    testSame("var goog;" +
        "goog.foo = function(a, opt) {};" +
        "var bar = function(){goog.foo.call(this, 1)};" +
        "goog.foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testApplyIsIgnore
  public void testApplyIsIgnore() {
    testSame("var goog;" +
        "goog.foo = function(a, opt) {};" +
        "var bar = function(){goog.foo.apply(this, 1)};" +
        "goog.foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testFunctionWithReferenceToArgumentsShouldNotBeOptimize
  public void testFunctionWithReferenceToArgumentsShouldNotBeOptimize() {
    testSame("function foo(a,b,c) { return arguments.size; };" +
             "foo(1);");
    testSame("var foo = function(a,b,c) { return arguments.size }; foo(1);");
    testSame("var foo = function bar(a,b,c) { return arguments.size }; " +
             "foo(2); bar(2);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testFunctionWithTwoNames
  public void testFunctionWithTwoNames() {
    testSame("var foo = function bar(a,b) {};");
    testSame("var foo = function bar(a,b) {}; foo(1)");
    testSame("var foo = function bar(a,b) {}; bar(1);");
    testSame("var foo = function bar(a,b) {}; foo(1); foo(2)");
    testSame("var foo = function bar(a,b) {}; foo(1); bar(1)");
    testSame("var foo = function bar(a,b) {}; foo(1); bar(2)");
    testSame("var foo = function bar(a,b) {}; foo(1,2); bar(2,1)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRecursion
  public void testRecursion() {
    test("var foo = function (a,b) {foo(1, b)}; foo(1, 2)",
         "var foo = function (b) {var a=1; foo(b)}; foo(2)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testConstantArgumentsToConstructorCanBeOptimized
  public void testConstantArgumentsToConstructorCanBeOptimized() {
    String src = "function foo(a) {};" +
        "var bar = new foo(1);";
    String expected = "function foo() {var a=1;};" +
        "var bar = new foo();";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testOptionalArgumentsToConstructorCanBeOptimized
  public void testOptionalArgumentsToConstructorCanBeOptimized() {
    String src = "function foo(a) {};" +
        "var bar = new foo();";
    String expected = "function foo() {var a;};" +
        "var bar = new foo();";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRegexesCanBeInlined
  public void testRegexesCanBeInlined() {
    test("function foo(a) {}; foo(/abc/);",
         "function foo() {var a = /abc/}; foo();");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testConstructorUsedAsFunctionCanBeOptimized
  public void testConstructorUsedAsFunctionCanBeOptimized() {
    String src = "function foo(a) {};" +
        "var bar = new foo(1);" +
        "foo(1);";
    String expected = "function foo() {var a=1;};" +
        "var bar = new foo();" +
        "foo();";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeConstructorWhenArgumentsAreNotEqual
  public void testDoNotOptimizeConstructorWhenArgumentsAreNotEqual() {
    testSame("function Foo(a) {};" +
        "var bar = new Foo(1);" +
        "var baz = new Foo(2);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeArrayElements
  public void testDoNotOptimizeArrayElements() {
    testSame("var array = [function (a, b) {}];");
    testSame("var array = [function f(a, b) {}]");

    testSame("var array = [function (a, b) {}];" +
        "array[0](1, 2);" +
        "array[0](1);");

    testSame("var array = [];" +
        "function foo(a, b) {};" +
        "array[0] = foo;");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testOptimizeThis
  public void testOptimizeThis() {
    String src = "function foo() {" +
        "var bar = function (a, b) {};" +
        "this.bar = function (a, b) {};" +
        "this.bar(3);" +
        "bar(2);}";
    String expected = "function foo() {" +
        "var bar = function () {var b; var a = 2;};" +
        "this.bar = function () {var b; var a = 3;};" +
        "this.bar();" +
        "bar();}";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeWhenArgumentsPassedAsParameter
  public void testDoNotOptimizeWhenArgumentsPassedAsParameter() {
    testSame("function foo(a) {}; foo(arguments)");
    testSame("function foo(a) {}; foo(arguments[0])");

    test("function foo(a, b) {}; foo(arguments, 1)",
         "function foo(a) {var b = 1}; foo(arguments)");

    test("function foo(a, b) {}; foo(arguments)",
         "function foo(a) {var b}; foo(arguments)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeGoogExportFunctions
  public void testDoNotOptimizeGoogExportFunctions() {
    testSame("function foo(a, b) {}; foo(); goog.export_function(foo);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeJSCompiler_renameProperty
  public void testDoNotOptimizeJSCompiler_renameProperty() {
    testSame("function JSCompiler_renameProperty(a) {return a};" +
             "JSCompiler_renameProperty('a');");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeJSCompiler_ObjectPropertyString
  public void testDoNotOptimizeJSCompiler_ObjectPropertyString() {
    testSame("function JSCompiler_ObjectPropertyString(a, b) {return a[b]};" +
             "JSCompiler_renameProperty(window,'b');");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMutableValues1
  public void testMutableValues1() {
    test("function foo(p1) {} foo()",
         "function foo() {var p1} foo()");
    test("function foo(p1) {} foo(1)",
         "function foo() {var p1=1} foo()");
    test("function foo(p1) {} foo([])",
         "function foo() {var p1=[]} foo()");
    test("function foo(p1) {} foo({})",
         "function foo() {var p1={}} foo()");
    test("var x;function foo(p1) {} foo(x)",
         "var x;function foo() {var p1=x} foo()");
    test("var x;function foo(p1) {} foo(x())",
         "var x;function foo() {var p1=x()} foo()");
    test("var x;function foo(p1) {} foo(new x())",
         "var x;function foo() {var p1=new x()} foo()");
    test("var x;function foo(p1) {} foo('' + x)",
         "var x;function foo() {var p1='' + x} foo()");

    testSame("function foo(p1) {} foo(this)");
    testSame("function foo(p1) {} foo(arguments)");
    testSame("function foo(p1) {} foo(function(){})");
    testSame("function foo(p1) {} (function () {var x;foo(x)})()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMutableValues2
  public void testMutableValues2() {
    test("function foo(p1, p2) {} foo(1, 2)",
         "function foo() {var p1=1; var p2 = 2} foo()");
    test("var x; var y; function foo(p1, p2) {} foo(x(), y())",
         "var x; var y; function foo() {var p1=x(); var p2 = y()} foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMutableValues3
  public void testMutableValues3() {
    test(
        "var x; var y; var z;" +
        "function foo(p1, p2) {}" +
        "foo(x(), y()); foo(x(),y())",
        "var x; var y; var z;" +
        "function foo() {var p1=x(); var p2=y()}" +
        "foo(); foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMutableValues4
  public void testMutableValues4() {
    
    
    
    testSame(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "foo(x(), y(), z()); foo(x(),y(),3)");

    
    
    testSame(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "foo(x, y(), z()); foo(x,y(),3)");

    
    
    test(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "foo([], y(), z()); foo([],y(),3)",
        "var x; var y; var z;" +
        "function foo(p2, p3) {var p1=[]}" +
        "foo(y(), z()); foo(y(),3)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMutableValues5
  public void testMutableValues5() {
    test(
        "var x; var y; var z;" +
        "function foo(p1, p2) {}" +
        "new foo(new x(), y()); new foo(new x(),y())",
        "var x; var y; var z;" +
        "function foo() {var p1=new x(); var p2=y()}" +
        "new foo(); new foo()");

    test(
        "var x; var y; var z;" +
        "function foo(p1, p2) {}" +
        "new foo(x(), y()); new foo(x(),y())",
        "var x; var y; var z;" +
        "function foo() {var p1=x(); var p2=y()}" +
        "new foo(); new foo()");

    testSame(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "new foo(x(), y(), z()); new foo(x(),y(),3)");

    testSame(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "new foo(x, y(), z()); new foo(x,y(),3)");

    test(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "new foo([], y(), z()); new foo([],y(),3)",
        "var x; var y; var z;" +
        "function foo(p2, p3) {var p1=[]}" +
        "new foo(y(), z()); new foo(y(),3)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testShadows
  public void testShadows() {
    testSame("function foo(a) {}" +
             "var x;" +
             "function f() {" +
             "  var x;" +
             "  function g() {" +
             "    foo(x());" +
             "  }" +
             "};" +
             "foo(x())");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testCrash
  public void testCrash() {
    test(
        "function foo(a) {}" +
        "foo({o:1});" +
        "foo({o:1})",
        "function foo() {var a = {o:1}}" +
        "foo();" +
        "foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testGlobalCatch
  public void testGlobalCatch() {
    testSame("function foo(a) {} try {} catch (e) {foo(e)}");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNamelessParameter1
  public void testNamelessParameter1() {
    test("f(g()); function f(){}",
         "f(); function f(){g()}");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNamelessParameter2
  public void testNamelessParameter2() {
    test("f(g(),h()); function f(){}",
         "f(); function f(){g();h()}");
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteUsedResult1
  public void testNoRewriteUsedResult1() throws Exception {
    String source = newlineJoin(
        "function a(){return 1}",
        "var x = a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteUsedResult2
  public void testNoRewriteUsedResult2() throws Exception {
    String source = newlineJoin(
        "var a = function(){return 1}",
        "a(); var b = a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult1
  public void testRewriteUnusedResult1() throws Exception {
    String source = newlineJoin(
        "function a(){return 1}",
        "a()");
    String expected = newlineJoin(
        "function a(){return}",
        "a()");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult2
  public void testRewriteUnusedResult2() throws Exception {
    String source = newlineJoin(
        "var a; a = function(){return 1}",
        "a()");
    String expected = newlineJoin(
        "var a; a = function(){return}",
        "a()");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult3
  public void testRewriteUnusedResult3() throws Exception {
    String source = newlineJoin(
        "var a = function(){return 1}",
        "a()");
    String expected = newlineJoin(
        "var a = function(){return}",
        "a()");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult4a
  public void testRewriteUnusedResult4a() throws Exception {
    String source = newlineJoin(
        "var a = function(){return a()}",
        "a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult4b
  public void testRewriteUnusedResult4b() throws Exception {
    String source = newlineJoin(
        "var a = function b(){return b()}",
        "a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult4c
  public void testRewriteUnusedResult4c() throws Exception {
    String source = newlineJoin(
        "function a(){return a()}",
        "a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult5
  public void testRewriteUnusedResult5() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return args};",
        "var o = new a;",
        "o.foo()");
    String expected = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return};",
        "var o = new a;",
        "o.foo()");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult6
  public void testRewriteUnusedResult6() throws Exception {
    String source = newlineJoin(
        "function a(){return (g = 1)}",
        "a()");
    String expected = newlineJoin(
        "function a(){g = 1;return}",
        "a()");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult7a
  public void testRewriteUnusedResult7a() throws Exception {
    String source = newlineJoin(
        "function a() { return 1 }",
        "function b() { return a() }",
        "function c() { return b() }",
        "c();");

    String expected = newlineJoin(
        "function a() { return 1 }",
        "function b() { return a() }",
        "function c() { b(); return }",
        "c();");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult7b
  public void testRewriteUnusedResult7b() throws Exception {
    String source = newlineJoin(
        "c();",
        "function c() { return b() }",
        "function b() { return a() }",
        "function a() { return 1 }");

    
    String expected = newlineJoin(
        "c();",
        "function c() { b(); return }",
        "function b() { return a() }",
        "function a() { return 1 }");
    test(source, expected);

    
    source = expected;
    expected = newlineJoin(
        "c();",
        "function c() { b(); return }",
        "function b() { a(); return }",
        "function a() { return 1 }");
    test(source, expected);

    
    source = expected;
    expected = newlineJoin(
        "c();",
        "function c() { b(); return }",
        "function b() { a(); return }",
        "function a() { return }");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult8
  public void testRewriteUnusedResult8() throws Exception {
    String source = newlineJoin(
        "function a() { return c() }",
        "function b() { return a() }",
        "function c() { return b() }",
        "c();");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteObjLit1
  public void testNoRewriteObjLit1() throws Exception {
    String source = newlineJoin(
        "var a = {b:function(){return 1;}}",
        "for(c in a) (a[c])();",
        "a.b()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteObjLit2
  public void testNoRewriteObjLit2() throws Exception {
    String source = newlineJoin(
        "var a = {b:function fn(){return 1;}}",
        "for(c in a) (a[c])();",
        "a.b()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteArrLit
  public void testNoRewriteArrLit() throws Exception {
    String source = newlineJoin(
        "var a = [function(){return 1;}]",
        "(a[0])();");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testPrototypeMethod1
  public void testPrototypeMethod1() throws Exception {
    String source = newlineJoin(
        "function c(){}",
        "c.prototype.a = function(){return 1}",
        "var x = new c;",
        "x.a()");
    String result = newlineJoin(
        "function c(){}",
        "c.prototype.a = function(){return}",
        "var x = new c;",
        "x.a()");
    test(source, result);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testPrototypeMethod2
  public void testPrototypeMethod2() throws Exception {
    String source = newlineJoin(
        "function c(){}",
        "c.prototype.a = function(){return 1}",
        "goog.reflect.object({a: 'v'})",
        "var x = new c;",
        "x.a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testPrototypeMethod3
  public void testPrototypeMethod3() throws Exception {
    String source = newlineJoin(
        "function c(){}",
        "c.prototype.a = function(){return 1}",
        "var x = new c;",
        "for(var key in goog.reflect.object({a: 'v'})){ x[key](); }",
        "x.a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testPrototypeMethod4
  public void testPrototypeMethod4() throws Exception {
    String source = newlineJoin(
        "function c(){}",
        "c.prototype.a = function(){return 1}",
        "var x = new c;",
        "for(var key in goog.reflect.object({a: 'v'})){ x[key](); }");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testCallOrApply
  public void testCallOrApply() throws Exception {
    
    testSame("function a() {return 1}; a.call(new foo);");

    testSame("function a() {return 1}; a.apply(new foo);");
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUseSiteRemoval
  public void testRewriteUseSiteRemoval() throws Exception {
    String source = newlineJoin(
        "function a() { return {\"_id\" : 1} }",
        "a();");
    String expected = newlineJoin(
        "function a() { return }",
        "a();");
    test(source, expected);
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testUndefinedComparison1
  public void testUndefinedComparison1() {
    fold("undefined == undefined", "true");
    fold("undefined == null", "true");
    fold("undefined == void 0", "true");

    fold("undefined == 0", "false");
    fold("undefined == 1", "false");
    fold("undefined == 'hi'", "false");
    fold("undefined == true", "false");
    fold("undefined == false", "false");

    fold("undefined === undefined", "true");
    fold("undefined === null", "false");
    fold("undefined === void 0", "true");

    foldSame("undefined == this");
    foldSame("undefined == x");

    fold("undefined != undefined", "false");
    fold("undefined != null", "false");
    fold("undefined != void 0", "false");

    fold("undefined != 0", "true");
    fold("undefined != 1", "true");
    fold("undefined != 'hi'", "true");
    fold("undefined != true", "true");
    fold("undefined != false", "true");

    fold("undefined !== undefined", "false");
    fold("undefined !== void 0", "false");
    fold("undefined !== null", "true");

    foldSame("undefined != this");
    foldSame("undefined != x");

    fold("undefined < undefined", "false");
    fold("undefined > undefined", "false");
    fold("undefined >= undefined", "false");
    fold("undefined <= undefined", "false");

    fold("0 < undefined", "false");
    fold("true > undefined", "false");
    fold("'hi' >= undefined", "false");
    fold("null <= undefined", "false");

    fold("undefined < 0", "false");
    fold("undefined > true", "false");
    fold("undefined >= 'hi'", "false");
    fold("undefined <= null", "false");

    fold("null == undefined", "true");
    fold("0 == undefined", "false");
    fold("1 == undefined", "false");
    fold("'hi' == undefined", "false");
    fold("true == undefined", "false");
    fold("false == undefined", "false");
    fold("null === undefined", "false");
    fold("void 0 === undefined", "true");

    fold("undefined == NaN", "false");
    fold("NaN == undefined", "false");
    fold("undefined == Infinity", "false");
    fold("Infinity == undefined", "false");
    fold("undefined == -Infinity", "false");
    fold("-Infinity == undefined", "false");
    fold("({}) == undefined", "false");
    fold("undefined == ({})", "false");
    fold("([]) == undefined", "false");
    fold("undefined == ([])", "false");
    fold("(/a/g) == undefined", "false");
    fold("undefined == (/a/g)", "false");
    fold("(function(){}) == undefined", "false");
    fold("undefined == (function(){})", "false");

    fold("undefined != NaN", "true");
    fold("NaN != undefined", "true");
    fold("undefined != Infinity", "true");
    fold("Infinity != undefined", "true");
    fold("undefined != -Infinity", "true");
    fold("-Infinity != undefined", "true");
    fold("({}) != undefined", "true");
    fold("undefined != ({})", "true");
    fold("([]) != undefined", "true");
    fold("undefined != ([])", "true");
    fold("(/a/g) != undefined", "true");
    fold("undefined != (/a/g)", "true");
    fold("(function(){}) != undefined", "true");
    fold("undefined != (function(){})", "true");

    foldSame("this == undefined");
    foldSame("x == undefined");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testUndefinedComparison2
  public void testUndefinedComparison2() {
    fold("\"123\" !== void 0", "true");
    fold("\"123\" === void 0", "false");

    fold("void 0 !== \"123\"", "true");
    fold("void 0 === \"123\"", "false");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testUndefinedComparison3
  public void testUndefinedComparison3() {
    fold("\"123\" !== undefined", "true");
    fold("\"123\" === undefined", "false");

    fold("undefined !== \"123\"", "true");
    fold("undefined === \"123\"", "false");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testUndefinedComparison4
  public void testUndefinedComparison4() {
    fold("1 !== void 0", "true");
    fold("1 === void 0", "false");

    fold("null !== void 0", "true");
    fold("null === void 0", "false");

    fold("undefined !== void 0", "false");
    fold("undefined === void 0", "true");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testNullComparison1
  public void testNullComparison1() {
    fold("null == undefined", "true");
    fold("null == null", "true");
    fold("null == void 0", "true");

    fold("null == 0", "false");
    fold("null == 1", "false");
    fold("null == 'hi'", "false");
    fold("null == true", "false");
    fold("null == false", "false");

    fold("null === undefined", "false");
    fold("null === null", "true");
    fold("null === void 0", "false");

    foldSame("null == this");
    foldSame("null == x");

    fold("null != undefined", "false");
    fold("null != null", "false");
    fold("null != void 0", "false");

    fold("null != 0", "true");
    fold("null != 1", "true");
    fold("null != 'hi'", "true");
    fold("null != true", "true");
    fold("null != false", "true");

    fold("null !== undefined", "true");
    fold("null !== void 0", "true");
    fold("null !== null", "false");

    foldSame("null != this");
    foldSame("null != x");

    fold("null < null", "false");
    fold("null > null", "false");
    fold("null >= null", "true");
    fold("null <= null", "true");

    foldSame("0 < null"); 
    fold("true > null", "true");
    foldSame("'hi' >= null"); 
    fold("null <= null", "true");

    foldSame("null < 0");  
    fold("null > true", "false");
    foldSame("null >= 'hi'"); 
    fold("null <= null", "true");

    fold("null == null", "true");
    fold("0 == null", "false");
    fold("1 == null", "false");
    fold("'hi' == null", "false");
    fold("true == null", "false");
    fold("false == null", "false");
    fold("null === null", "true");
    fold("void 0 === null", "false");

    fold("null == NaN", "false");
    fold("NaN == null", "false");
    fold("null == Infinity", "false");
    fold("Infinity == null", "false");
    fold("null == -Infinity", "false");
    fold("-Infinity == null", "false");
    fold("({}) == null", "false");
    fold("null == ({})", "false");
    fold("([]) == null", "false");
    fold("null == ([])", "false");
    fold("(/a/g) == null", "false");
    fold("null == (/a/g)", "false");
    fold("(function(){}) == null", "false");
    fold("null == (function(){})", "false");

    fold("null != NaN", "true");
    fold("NaN != null", "true");
    fold("null != Infinity", "true");
    fold("Infinity != null", "true");
    fold("null != -Infinity", "true");
    fold("-Infinity != null", "true");
    fold("({}) != null", "true");
    fold("null != ({})", "true");
    fold("([]) != null", "true");
    fold("null != ([])", "true");
    fold("(/a/g) != null", "true");
    fold("null != (/a/g)", "true");
    fold("(function(){}) != null", "true");
    fold("null != (function(){})", "true");

    foldSame("({a:f()}) == null");
    foldSame("null == ({a:f()})");
    foldSame("([f()]) == null");
    foldSame("null == ([f()])");

    foldSame("this == null");
    foldSame("x == null");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testUnaryOps
  public void testUnaryOps() {
    
    foldSame("!foo()");
    foldSame("~foo()");
    foldSame("-foo()");

    
    fold("a=!true", "a=false");
    fold("a=!10", "a=false");
    fold("a=!false", "a=true");
    fold("a=!foo()", "a=!foo()");
    fold("a=-0", "a=-0.0");
    fold("a=-(0)", "a=-0.0");
    fold("a=-Infinity", "a=-Infinity");
    fold("a=-NaN", "a=NaN");
    fold("a=-foo()", "a=-foo()");
    fold("a=~~0", "a=0");
    fold("a=~~10", "a=10");
    fold("a=~-7", "a=6");

    fold("a=+true", "a=1");
    fold("a=+10", "a=10");
    fold("a=+false", "a=0");
    foldSame("a=+foo()");
    foldSame("a=+f");
    fold("a=+(f?true:false)", "a=+(f?1:0)"); 
    fold("a=+0", "a=0");
    fold("a=+Infinity", "a=Infinity");
    fold("a=+NaN", "a=NaN");
    fold("a=+-7", "a=-7");
    fold("a=+.5", "a=.5");

    fold("a=~0x100000000", "a=~0x100000000",
         PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("a=~-0x100000000", "a=~-0x100000000",
         PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    testSame("a=~.5", PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testUnaryOpsStringCompare
  public void testUnaryOpsStringCompare() {
    
    assertResultString("a=-1", "a=-1");
    assertResultString("a=~0", "a=-1");
    assertResultString("a=~1", "a=-2");
    assertResultString("a=~101", "a=-102");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLogicalOp
  public void testFoldLogicalOp() {
    fold("x = true && x", "x = x");
    foldSame("x = [foo()] && x");

    fold("x = false && x", "x = false");
    fold("x = true || x", "x = true");
    fold("x = false || x", "x = x");
    fold("x = 0 && x", "x = 0");
    fold("x = 3 || x", "x = 3");
    fold("x = false || 0", "x = 0");

    
    fold("a = x && true", "a=x&&true");
    fold("a = x && false", "a=x&&false");
    fold("a = x || 3", "a=x||3");
    fold("a = x || false", "a=x||false");
    fold("a = b ? c : x || false", "a=b?c:x||false");
    fold("a = b ? x || false : c", "a=b?x||false:c");
    fold("a = b ? c : x && true", "a=b?c:x&&true");
    fold("a = b ? x && true : c", "a=b?x&&true:c");

    
    foldSame("a = x || false ? b : c");
    foldSame("a = x && true ? b : c");

    fold("x = foo() || true || bar()", "x = foo()||true");
    fold("x = foo() || false || bar()", "x = foo()||bar()");
    fold("x = foo() || true && bar()", "x = foo()||bar()");
    fold("x = foo() || false && bar()", "x = foo()||false");
    fold("x = foo() && false && bar()", "x = foo()&&false");
    fold("x = foo() && true && bar()", "x = foo()&&bar()");
    fold("x = foo() && false || bar()", "x = foo()&&false||bar()");

    fold("1 && b()", "b()");
    fold("a() && (1 && b())", "a() && b()");
    
    
    fold("(a() && 1) && b()", "(a() && 1) && b()");

    
    
    
    foldSame("x = foo() && true || bar()");
    foldSame("foo() && true || bar()");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldBitwiseOp
  public void testFoldBitwiseOp() {
    fold("x = 1 & 1", "x = 1");
    fold("x = 1 & 2", "x = 0");
    fold("x = 3 & 1", "x = 1");
    fold("x = 3 & 3", "x = 3");

    fold("x = 1 | 1", "x = 1");
    fold("x = 1 | 2", "x = 3");
    fold("x = 3 | 1", "x = 3");
    fold("x = 3 | 3", "x = 3");

    fold("x = 1 ^ 1", "x = 0");
    fold("x = 1 ^ 2", "x = 3");
    fold("x = 3 ^ 1", "x = 2");
    fold("x = 3 ^ 3", "x = 0");

    fold("x = -1 & 0", "x = 0");
    fold("x = 0 & -1", "x = 0");
    fold("x = 1 & 4", "x = 0");
    fold("x = 2 & 3", "x = 2");

    
    
    fold("x = 1 & 1.1", "x = 1");
    fold("x = 1.1 & 1", "x = 1");
    fold("x = 1 & 3000000000", "x = 0");
    fold("x = 3000000000 & 1", "x = 0");

    
    fold("x = 1 | 4", "x = 5");
    fold("x = 1 | 3", "x = 3");
    fold("x = 1 | 1.1", "x = 1");
    foldSame("x = 1 | 3E9");
    fold("x = 1 | 3000000001", "x = -1294967295");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldBitwiseOp2
  public void testFoldBitwiseOp2() {
    fold("x = y & 1 & 1", "x = y & 1");
    fold("x = y & 1 & 2", "x = y & 0");
    fold("x = y & 3 & 1", "x = y & 1");
    fold("x = 3 & y & 1", "x = y & 1");
    fold("x = y & 3 & 3", "x = y & 3");
    fold("x = 3 & y & 3", "x = y & 3");

    fold("x = y | 1 | 1", "x = y | 1");
    fold("x = y | 1 | 2", "x = y | 3");
    fold("x = y | 3 | 1", "x = y | 3");
    fold("x = 3 | y | 1", "x = y | 3");
    fold("x = y | 3 | 3", "x = y | 3");
    fold("x = 3 | y | 3", "x = y | 3");

    fold("x = y ^ 1 ^ 1", "x = y ^ 0");
    fold("x = y ^ 1 ^ 2", "x = y ^ 3");
    fold("x = y ^ 3 ^ 1", "x = y ^ 2");
    fold("x = 3 ^ y ^ 1", "x = y ^ 2");
    fold("x = y ^ 3 ^ 3", "x = y ^ 0");
    fold("x = 3 ^ y ^ 3", "x = y ^ 0");

    fold("x = Infinity | NaN", "x=0");
    fold("x = 12 | NaN", "x=12");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldingMixTypesLate
  public void testFoldingMixTypesLate() {
    late = true;
    fold("x = x + '2'", "x+='2'");
    fold("x = +x + +'2'", "x = +x + 2");
    fold("x = x - '2'", "x-=2");
    fold("x = x ^ '2'", "x^=2");
    fold("x = '2' ^ x", "x^=2");
    fold("x = '2' & x", "x&=2");
    fold("x = '2' | x", "x|=2");

    fold("x = '2' | y", "x=2|y");
    fold("x = y | '2'", "x=y|2");
    fold("x = y | (a && '2')", "x=y|(a&&2)");
    fold("x = y | (a,'2')", "x=y|(a,2)");
    fold("x = y | (a?'1':'2')", "x=y|(a?1:2)");
    fold("x = y | ('x'?'1':'2')", "x=y|('x'?1:2)");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldingMixTypesEarly
  public void testFoldingMixTypesEarly() {
    late = false;
    foldSame("x = x + '2'");
    fold("x = +x + +'2'", "x = +x + 2");
    fold("x = x - '2'", "x = x - 2");
    fold("x = x ^ '2'", "x = x ^ 2");
    fold("x = '2' ^ x", "x = 2 ^ x");
    fold("x = '2' & x", "x = 2 & x");
    fold("x = '2' | x", "x = 2 | x");

    fold("x = '2' | y", "x=2|y");
    fold("x = y | '2'", "x=y|2");
    fold("x = y | (a && '2')", "x=y|(a&&2)");
    fold("x = y | (a,'2')", "x=y|(a,2)");
    fold("x = y | (a?'1':'2')", "x=y|(a?1:2)");
    fold("x = y | ('x'?'1':'2')", "x=y|('x'?1:2)");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldingAdd
  public void testFoldingAdd() {
    fold("x = null + true", "x=1");
    foldSame("x = a + true");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldBitwiseOpStringCompare
  public void testFoldBitwiseOpStringCompare() {
    assertResultString("x = -1 | 0", "x=-1");
    
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldBitShifts
  public void testFoldBitShifts() {
    fold("x = 1 << 0", "x = 1");
    fold("x = -1 << 0", "x = -1");
    fold("x = 1 << 1", "x = 2");
    fold("x = 3 << 1", "x = 6");
    fold("x = 1 << 8", "x = 256");

    fold("x = 1 >> 0", "x = 1");
    fold("x = -1 >> 0", "x = -1");
    fold("x = 1 >> 1", "x = 0");
    fold("x = 2 >> 1", "x = 1");
    fold("x = 5 >> 1", "x = 2");
    fold("x = 127 >> 3", "x = 15");
    fold("x = 3 >> 1", "x = 1");
    fold("x = 3 >> 2", "x = 0");
    fold("x = 10 >> 1", "x = 5");
    fold("x = 10 >> 2", "x = 2");
    fold("x = 10 >> 5", "x = 0");

    fold("x = 10 >>> 1", "x = 5");
    fold("x = 10 >>> 2", "x = 2");
    fold("x = 10 >>> 5", "x = 0");
    fold("x = -1 >>> 1", "x = 2147483647"); 
    fold("x = -1 >>> 0", "x = 4294967295"); 
    fold("x = -2 >>> 0", "x = 4294967294"); 

    testSame("3000000000 << 1",
         PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    testSame("1 << 32",
        PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    testSame("1 << -1",
        PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    testSame("3000000000 >> 1",
        PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    testSame("1 >> 32",
        PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    testSame("1.5 << 0",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    testSame("1 << .5",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    testSame("1.5 >>> 0",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    testSame("1 >>> .5",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    testSame("1.5 >> 0",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    testSame("1 >> .5",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldBitShiftsStringCompare
  public void testFoldBitShiftsStringCompare() {
    
    assertResultString("x = -1 << 1", "x=-2");
    assertResultString("x = -1 << 8", "x=-256");
    assertResultString("x = -1 >> 1", "x=-1");
    assertResultString("x = -2 >> 1", "x=-1");
    assertResultString("x = -1 >> 0", "x=-1");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testStringAdd
  public void testStringAdd() {
    fold("x = 'a' + \"bc\"", "x = \"abc\"");
    fold("x = 'a' + 5", "x = \"a5\"");
    fold("x = 5 + 'a'", "x = \"5a\"");
    fold("x = 'a' + ''", "x = \"a\"");
    fold("x = \"a\" + foo()", "x = \"a\"+foo()");
    fold("x = foo() + 'a' + 'b'", "x = foo()+\"ab\"");
    fold("x = (foo() + 'a') + 'b'", "x = foo()+\"ab\"");  
    fold("x = foo() + 'a' + 'b' + 'cd' + bar()", "x = foo()+\"abcd\"+bar()");
    fold("x = foo() + 2 + 'b'", "x = foo()+2+\"b\"");  
    fold("x = foo() + 'a' + 2", "x = foo()+\"a2\"");
    fold("x = '' + null", "x = \"null\"");
    fold("x = true + '' + false", "x = \"truefalse\"");
    fold("x = '' + []", "x = ''");      
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testIssue821
  public void testIssue821() {
    foldSame("var a =(Math.random()>0.5? '1' : 2 ) + 3 + 4;");
    foldSame("var a = ((Math.random() ? 0 : 1) ||" +
             "(Math.random()>0.5? '1' : 2 )) + 3 + 4;");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldConstructor
  public void testFoldConstructor() {
    fold("x = this[new String('a')]", "x = this['a']");
    fold("x = ob[new String(12)]", "x = ob['12']");
    fold("x = ob[new String(false)]", "x = ob['false']");
    fold("x = ob[new String(null)]", "x = ob['null']");
    fold("x = 'a' + new String('b')", "x = 'ab'");
    fold("x = 'a' + new String(23)", "x = 'a23'");
    fold("x = 2 + new String(1)", "x = '21'");
    foldSame("x = ob[new String(a)]");
    foldSame("x = new String('a')");
    foldSame("x = (new String('a'))[3]");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArithmetic
  public void testFoldArithmetic() {
    fold("x = 10 + 20", "x = 30");
    fold("x = 2 / 4", "x = 0.5");
    fold("x = 2.25 * 3", "x = 6.75");
    fold("z = x * y", "z = x * y");
    fold("x = y * 5", "x = y * 5");
    fold("x = 1 / 0", "x = 1 / 0");
    fold("x = 3 % 2", "x = 1");
    fold("x = 3 % -2", "x = 1");
    fold("x = -1 % 3", "x = -1");
    fold("x = 1 % 0", "x = 1 % 0");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArithmetic2
  public void testFoldArithmetic2() {
    foldSame("x = y + 10 + 20");
    foldSame("x = y / 2 / 4");
    fold("x = y * 2.25 * 3", "x = y * 6.75");
    fold("z = x * y", "z = x * y");
    fold("x = y * 5", "x = y * 5");
    fold("x = y + (z * 24 * 60 * 60 * 1000)", "x = y + z * 864E5");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArithmetic3
  public void testFoldArithmetic3() {
    fold("x = null * undefined", "x = NaN");
    fold("x = null * 1", "x = 0");
    fold("x = (null - 1) * 2", "x = -2");
    fold("x = (null + 1) * 2", "x = 2");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArithmeticInfinity
  public void testFoldArithmeticInfinity() {
    fold("x=-Infinity-2", "x=-Infinity");
    fold("x=Infinity-2", "x=Infinity");
    fold("x=Infinity*5", "x=Infinity");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArithmeticStringComp
  public void testFoldArithmeticStringComp() {
    
    assertResultString("x = 10 - 20", "x=-10");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldComparison
  public void testFoldComparison() {
    fold("x = 0 == 0", "x = true");
    fold("x = 1 == 2", "x = false");
    fold("x = 'abc' == 'def'", "x = false");
    fold("x = 'abc' == 'abc'", "x = true");
    fold("x = \"\" == ''", "x = true");
    fold("x = foo() == bar()", "x = foo()==bar()");

    fold("x = 1 != 0", "x = true");
    fold("x = 'abc' != 'def'", "x = true");
    fold("x = 'a' != 'a'", "x = false");

    fold("x = 1 < 20", "x = true");
    fold("x = 3 < 3", "x = false");
    fold("x = 10 > 1.0", "x = true");
    fold("x = 10 > 10.25", "x = false");
    fold("x = y == y", "x = y==y");
    fold("x = y < y", "x = false");
    fold("x = y > y", "x = false");
    fold("x = 1 <= 1", "x = true");
    fold("x = 1 <= 0", "x = false");
    fold("x = 0 >= 0", "x = true");
    fold("x = -1 >= 9", "x = false");

    fold("x = true == true", "x = true");
    fold("x = false == false", "x = true");
    fold("x = false == null", "x = false");
    fold("x = false == true", "x = false");
    fold("x = true == null", "x = false");

    fold("0 == 0", "true");
    fold("1 == 2", "false");
    fold("'abc' == 'def'", "false");
    fold("'abc' == 'abc'", "true");
    fold("\"\" == ''", "true");
    foldSame("foo() == bar()");

    fold("1 != 0", "true");
    fold("'abc' != 'def'", "true");
    fold("'a' != 'a'", "false");

    fold("1 < 20", "true");
    fold("3 < 3", "false");
    fold("10 > 1.0", "true");
    fold("10 > 10.25", "false");
    foldSame("x == x");
    fold("x < x", "false");
    fold("x > x", "false");
    fold("1 <= 1", "true");
    fold("1 <= 0", "false");
    fold("0 >= 0", "true");
    fold("-1 >= 9", "false");

    fold("true == true", "true");
    fold("false == null", "false");
    fold("false == true", "false");
    fold("true == null", "false");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldComparison2
  public void testFoldComparison2() {
    fold("x = 0 === 0", "x = true");
    fold("x = 1 === 2", "x = false");
    fold("x = 'abc' === 'def'", "x = false");
    fold("x = 'abc' === 'abc'", "x = true");
    fold("x = \"\" === ''", "x = true");
    fold("x = foo() === bar()", "x = foo()===bar()");

    fold("x = 1 !== 0", "x = true");
    fold("x = 'abc' !== 'def'", "x = true");
    fold("x = 'a' !== 'a'", "x = false");

    fold("x = y === y", "x = y===y");

    fold("x = true === true", "x = true");
    fold("x = false === false", "x = true");
    fold("x = false === null", "x = false");
    fold("x = false === true", "x = false");
    fold("x = true === null", "x = false");

    fold("0 === 0", "true");
    fold("1 === 2", "false");
    fold("'abc' === 'def'", "false");
    fold("'abc' === 'abc'", "true");
    fold("\"\" === ''", "true");
    foldSame("foo() === bar()");

    
    foldSame("1 === '1'");
    foldSame("1 === true");
    foldSame("1 !== '1'");
    foldSame("1 !== true");

    fold("1 !== 0", "true");
    fold("'abc' !== 'def'", "true");
    fold("'a' !== 'a'", "false");

    foldSame("x === x");

    fold("true === true", "true");
    fold("false === null", "false");
    fold("false === true", "false");
    fold("true === null", "false");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldComparison3
  public void testFoldComparison3() {
    fold("x = !1 == !0", "x = false");

    fold("x = !0 == !0", "x = true");
    fold("x = !1 == !1", "x = true");
    fold("x = !1 == null", "x = false");
    fold("x = !1 == !0", "x = false");
    fold("x = !0 == null", "x = false");

    fold("!0 == !0", "true");
    fold("!1 == null", "false");
    fold("!1 == !0", "false");
    fold("!0 == null", "false");

    fold("x = !0 === !0", "x = true");
    fold("x = !1 === !1", "x = true");
    fold("x = !1 === null", "x = false");
    fold("x = !1 === !0", "x = false");
    fold("x = !0 === null", "x = false");

    fold("!0 === !0", "true");
    fold("!1 === null", "false");
    fold("!1 === !0", "false");
    fold("!0 === null", "false");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldGetElem
  public void testFoldGetElem() {
    fold("x = [,10][0]", "x = void 0");
    fold("x = [10, 20][0]", "x = 10");
    fold("x = [10, 20][1]", "x = 20");

    testSame("x = [10, 20][0.5]",
        PeepholeFoldConstants.INVALID_GETELEM_INDEX_ERROR);
    testSame("x = [10, 20][-1]",
        PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
    testSame("x = [10, 20][2]",
        PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);

    foldSame("x = [foo(), 0][1]");
    fold("x = [0, foo()][1]", "x = foo()");
    foldSame("x = [0, foo()][0]");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldComplex
  public void testFoldComplex() {
    fold("x = (3 / 1.0) + (1 * 2)", "x = 5");
    fold("x = (1 == 1.0) && foo() && true", "x = foo()&&true");
    fold("x = 'abc' + 5 + 10", "x = \"abc510\"");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLeft
  public void testFoldLeft() {
    foldSame("(+x - 1) + 2"); 
    fold("(+x + 1) + 2", "+x + 3");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArrayLength
  public void testFoldArrayLength() {
    
    fold("x = [].length", "x = 0");
    fold("x = [1,2,3].length", "x = 3");
    fold("x = [a,b].length", "x = 2");

    
    fold("x = [,,1].length", "x = 3");

    
    fold("x = [foo(), 0].length", "x = [foo(),0].length");
    fold("x = y.length", "x = y.length");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldStringLength
  public void testFoldStringLength() {
    
    fold("x = ''.length", "x = 0");
    fold("x = '123'.length", "x = 3");

    
    fold("x = '123\u01dc'.length", "x = 4");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldTypeof
  public void testFoldTypeof() {
    fold("x = typeof 1", "x = \"number\"");
    fold("x = typeof 'foo'", "x = \"string\"");
    fold("x = typeof true", "x = \"boolean\"");
    fold("x = typeof false", "x = \"boolean\"");
    fold("x = typeof null", "x = \"object\"");
    fold("x = typeof undefined", "x = \"undefined\"");
    fold("x = typeof void 0", "x = \"undefined\"");
    fold("x = typeof []", "x = \"object\"");
    fold("x = typeof [1]", "x = \"object\"");
    fold("x = typeof [1,[]]", "x = \"object\"");
    fold("x = typeof {}", "x = \"object\"");
    fold("x = typeof function() {}", "x = 'function'");

    foldSame("x = typeof[1,[foo()]]");
    foldSame("x = typeof{bathwater:baby()}");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldInstanceOf
  public void testFoldInstanceOf() {
    
    fold("64 instanceof Object", "false");
    fold("64 instanceof Number", "false");
    fold("'' instanceof Object", "false");
    fold("'' instanceof String", "false");
    fold("true instanceof Object", "false");
    fold("true instanceof Boolean", "false");
    fold("!0 instanceof Object", "false");
    fold("!0 instanceof Boolean", "false");
    fold("false instanceof Object", "false");
    fold("null instanceof Object", "false");
    fold("undefined instanceof Object", "false");
    fold("NaN instanceof Object", "false");
    fold("Infinity instanceof Object", "false");

    
    fold("[] instanceof Object", "true");
    fold("({}) instanceof Object", "true");

    
    foldSame("new Foo() instanceof Object");
    
    foldSame("[] instanceof Foo");
    foldSame("({}) instanceof Foo");

    fold("(function() {}) instanceof Object", "true");

    
    foldSame("x instanceof Foo");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testDivision
  public void testDivision() {
    
    fold("print(1/3)", "print(1/3)");

    
    
    fold("print(1/2)", "print(0.5)");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testAssignOpsLate
  public void testAssignOpsLate() {
    late = true;
    fold("x=x+y", "x+=y");
    foldSame("x=y+x");
    fold("x=x*y", "x*=y");
    fold("x=y*x", "x*=y");
    fold("x.y=x.y+z", "x.y+=z");
    foldSame("next().x = next().x + 1");

    fold("x=x-y", "x-=y");
    foldSame("x=y-x");
    fold("x=x|y", "x|=y");
    fold("x=y|x", "x|=y");
    fold("x=x*y", "x*=y");
    fold("x=y*x", "x*=y");
    fold("x.y=x.y+z", "x.y+=z");
    foldSame("next().x = next().x + 1");
    
    fold("({a:1}).a = ({a:1}).a + 1", "({a:1}).a = 2");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testAssignOpsEarly
 public void testAssignOpsEarly() {
    late = false;
    foldSame("x=x+y");
    foldSame("x=y+x");
    foldSame("x=x*y");
    foldSame("x=y*x");
    foldSame("x.y=x.y+z");
    foldSame("next().x = next().x + 1");

    foldSame("x=x-y");
    foldSame("x=y-x");
    foldSame("x=x|y");
    foldSame("x=y|x");
    foldSame("x=x*y");
    foldSame("x=y*x");
    foldSame("x.y=x.y+z");
    foldSame("next().x = next().x + 1");
    
    fold("({a:1}).a = ({a:1}).a + 1", "({a:1}).a = 2");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldAdd1
  public void testFoldAdd1() {
    fold("x=false+1","x=1");
    fold("x=true+1","x=2");
    fold("x=1+false","x=1");
    fold("x=1+true","x=2");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLiteralNames
  public void testFoldLiteralNames() {
    foldSame("NaN == NaN");
    foldSame("Infinity == Infinity");
    foldSame("Infinity == NaN");
    fold("undefined == NaN", "false");
    fold("undefined == Infinity", "false");

    foldSame("Infinity >= Infinity");
    foldSame("NaN >= NaN");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLiteralsTypeMismatches
  public void testFoldLiteralsTypeMismatches() {
    fold("true == true", "true");
    fold("true == false", "false");
    fold("true == null", "false");
    fold("false == null", "false");

    
    fold("null <= null", "true"); 
    fold("null >= null", "true");
    fold("null > null", "false");
    fold("null < null", "false");

    fold("false >= null", "true"); 
    fold("false <= null", "true");
    fold("false > null", "false");
    fold("false < null", "false");

    fold("true >= null", "true");  
    fold("true <= null", "false");
    fold("true > null", "true");
    fold("true < null", "false");

    fold("true >= false", "true");  
    fold("true <= false", "false");
    fold("true > false", "true");
    fold("true < false", "false");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLeftChildConcat
  public void testFoldLeftChildConcat() {
    foldSame("x +5 + \"1\"");
    fold("x+\"5\" + \"1\"", "x + \"51\"");
    
    fold("\"a\"+(\"b\"+c)","\"ab\"+c");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLeftChildOp
  public void testFoldLeftChildOp() {
    fold("x * Infinity * 2", "x * Infinity");
    foldSame("x - Infinity - 2"); 
    foldSame("x - 1 + Infinity");
    foldSame("x - 2 + 1");
    foldSame("x - 2 + 3");
    foldSame("1 + x - 2 + 1");
    foldSame("1 + x - 2 + 3");
    foldSame("1 + x - 2 + 3 - 1");
    foldSame("f(x)-0");
    foldSame("x-0-0");
    foldSame("x+2-2+2");
    foldSame("x+2-2+2-2");
    foldSame("x-2+2");
    foldSame("x-2+2-2");
    foldSame("x-2+2-2+2");

    foldSame("1+x-0-NaN");
    foldSame("1+f(x)-0-NaN");
    foldSame("1+x-0+NaN");
    foldSame("1+f(x)-0+NaN");

    foldSame("1+x+NaN"); 
    foldSame("x+2-2");   
    foldSame("x+2");  
    foldSame("x-2");  
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldSimpleArithmeticOp
  public void testFoldSimpleArithmeticOp() {
    foldSame("x*NaN");
    foldSame("NaN/y");
    foldSame("f(x)-0");
    foldSame("f(x)*1");
    foldSame("1*f(x)");
    foldSame("0+a+b");
    foldSame("0-a-b");
    foldSame("a+b-0");
    foldSame("(1+x)*NaN");

    foldSame("(1+f(x))*NaN"); 
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLiteralsAsNumbers
  public void testFoldLiteralsAsNumbers() {
    fold("x/'12'","x/12");
    fold("x/('12'+'6')", "x/126");
    fold("true*x", "1*x");
    fold("x/false", "x/0");  
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testNotFoldBackToTrueFalse
  public void testNotFoldBackToTrueFalse() {
    late = false;
    fold("!0", "true");
    fold("!1", "false");
    fold("!3", "false");

    late = true;
    foldSame("!0");
    foldSame("!1");
    fold("!3", "false");
    foldSame("false");
    foldSame("true");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldBangConstants
  public void testFoldBangConstants() {
    fold("1 + !0", "2");
    fold("1 + !1", "1");
    fold("'a ' + !1", "'a false'");
    fold("'a ' + !0", "'a true'");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldMixed
  public void testFoldMixed() {
    fold("''+[1]", "'1'");
    foldSame("false+[]"); 
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldVoid
  public void testFoldVoid() {
    foldSame("void 0");
    fold("void 1", "void 0");
    fold("void x", "void 0");
    fold("void x()", "void x()");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testObjectLiteral
  public void testObjectLiteral() {
    test("(!{})", "false");
    test("(!{a:1})", "false");
    testSame("(!{a:foo()})");
    testSame("(!{'a':foo()})");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testArrayLiteral
  public void testArrayLiteral() {
    test("(![])", "false");
    test("(![1])", "false");
    test("(![a])", "false");
    testSame("(![foo()])");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testIssue601
  public void testIssue601() {
    testSame("'\\v' == 'v'");
    testSame("'v' == '\\v'");
    testSame("'\\u000B' == '\\v'");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldObjectLiteralRef1
  public void testFoldObjectLiteralRef1() {
    
    testSame("var x = ({a:foo(),b:bar()}).a");
    testSame("var x = ({a:1,b:bar()}).a");
    testSame("function f() { return {b:foo(), a:2}.a; }");

    
    testSame("({a:x}).a = 1");
    test("({a:x}).a += 1", "({a:x}).a = x + 1");
    testSame("({a:x}).a ++");
    testSame("({a:x}).a --");

    
    testSame("({a:function(){return this}}).a");
    testSame("({get a() {return this}}).a");
    testSame("({set a(b) {return this}}).a");

    
    testSame("({}).a");

    
    testSame("({}).a");
    testSame("({set a(b) {}}).a");
    
    test("({a:1,set a(b) {}}).a", "1");

    
    test("({get a() {}}).a", "(function (){})()");
    
    test("({get a() {},set a(b) {}}).a", "(function (){})()");

    
    test("var x = ({a:function(){return 1}}).a",
         "var x = function(){return 1}");

    test("var x = ({a:1}).a", "var x = 1");
    test("var x = ({a:1, a:2}).a", "var x = 2");
    test("var x = ({a:1, a:foo()}).a", "var x = foo()");
    test("var x = ({a:foo()}).a", "var x = foo()");

    test("function f() { return {a:1, b:2}.a; }",
         "function f() { return 1; }");

    
    test("var x = ({'a':1})['a']", "var x = 1");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldObjectLiteralRef2
  public void testFoldObjectLiteralRef2() {
    late = false;
    test("({a:x}).a += 1", "({a:x}).a = x + 1");
    late = true;
    testSame("({a:x}).a += 1");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testIEString
  public void testIEString() {
    testSame("!+'\\v1'");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testIssue522
  public void testIssue522() {
    testSame("[][1] = 1;");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testInvertibleOperators
  public void testInvertibleOperators() {
    Map<String, String> inverses = ImmutableMap.<String, String>builder()
        .put("==", "!=")
        .put("===", "!==")
        .put("<=", ">")
        .put("<", ">=")
        .put(">=", "<")
        .put(">", "<=")
        .put("!=", "==")
        .put("!==", "===")
        .build();
    Set<String> comparators = ImmutableSet.of("<=", "<", ">=", ">");
    Set<String> equalitors = ImmutableSet.of("==", "===");
    Set<String> uncomparables = ImmutableSet.of("undefined", "void 0");
    List<String> operators = ImmutableList.copyOf(inverses.values());
    for (int iOperandA = 0; iOperandA < LITERAL_OPERANDS.size(); iOperandA++) {
      for (int iOperandB = 0;
           iOperandB < LITERAL_OPERANDS.size();
           iOperandB++) {
        for (int iOp = 0; iOp < operators.size(); iOp++) {
          String a = LITERAL_OPERANDS.get(iOperandA);
          String b = LITERAL_OPERANDS.get(iOperandB);
          String op = operators.get(iOp);
          String inverse = inverses.get(op);

          
          if (comparators.contains(op) &&
              (uncomparables.contains(a) || uncomparables.contains(b))) {
            assertSameResults(join(a, op, b), "false");
            assertSameResults(join(a, inverse, b), "false");
          } else if (a.equals(b) && equalitors.contains(op)) {
            if (a.equals("NaN") ||
                a.equals("Infinity") ||
                a.equals("-Infinity")) {
              foldSame(join(a, op, b));
              foldSame(join(a, inverse, b));
            } else {
              assertSameResults(join(a, op, b), "true");
              assertSameResults(join(a, inverse, b), "false");
            }
          } else {
            assertNotSameResults(join(a, op, b), join(a, inverse, b));
          }
        }
      }
    }
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testCommutativeOperators
  public void testCommutativeOperators() {
    late = true;
    List<String> operators =
        ImmutableList.of(
            "==",
            "!=",
            "===",
            "!==",
            "*",
            "|",
            "&",
            "^");
    for (int iOperandA = 0; iOperandA < LITERAL_OPERANDS.size(); iOperandA++) {
      for (int iOperandB = iOperandA;
           iOperandB < LITERAL_OPERANDS.size();
           iOperandB++) {
        for (int iOp = 0; iOp < operators.size(); iOp++) {
          String a = LITERAL_OPERANDS.get(iOperandA);
          String b = LITERAL_OPERANDS.get(iOperandB);
          String op = operators.get(iOp);

          
          
          assertSameResultsOrUncollapsed(join(a, op, b), join(b, op, a));
        }
      }
    }
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testConvertToNumberNegativeInf
  public void testConvertToNumberNegativeInf() {
    foldSame("var x = 3 * (r ? Infinity : -Infinity);");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testStringIndexOf
  public void testStringIndexOf() {
    fold("x = 'abcdef'.indexOf('b')", "x = 1");
    fold("x = 'abcdefbe'.indexOf('b', 2)", "x = 6");
    fold("x = 'abcdef'.indexOf('bcd')", "x = 1");
    fold("x = 'abcdefsdfasdfbcdassd'.indexOf('bcd', 4)", "x = 13");

    fold("x = 'abcdef'.lastIndexOf('b')", "x = 1");
    fold("x = 'abcdefbe'.lastIndexOf('b')", "x = 6");
    fold("x = 'abcdefbe'.lastIndexOf('b', 5)", "x = 1");

    
    
    fold("x = 'abc1def'.indexOf(1)", "x = 3");
    fold("x = 'abcNaNdef'.indexOf(NaN)", "x = 3");
    fold("x = 'abcundefineddef'.indexOf(undefined)", "x = 3");
    fold("x = 'abcnulldef'.indexOf(null)", "x = 3");
    fold("x = 'abctruedef'.indexOf(true)", "x = 3");

    
    
    foldSame("x = NaN.indexOf('bcd')");
    foldSame("x = undefined.indexOf('bcd')");
    foldSame("x = null.indexOf('bcd')");
    foldSame("x = true.indexOf('bcd')");
    foldSame("x = false.indexOf('bcd')");

    
    foldSame("x = 'abcdef'.indexOf(/b./)");
    foldSame("x = 'abcdef'.indexOf({a:2})");
    foldSame("x = 'abcdef'.indexOf([1,2])");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testStringJoinAddSparse
  public void testStringJoinAddSparse() {
    fold("x = [,,'a'].join(',')", "x = ',,a'");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testNoStringJoin
  public void testNoStringJoin() {
    foldSame("x = [].join(',',2)");
    foldSame("x = [].join(f)");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testStringJoinAdd
  public void testStringJoinAdd() {
    fold("x = ['a', 'b', 'c'].join('')", "x = \"abc\"");
    fold("x = [].join(',')", "x = \"\"");
    fold("x = ['a'].join(',')", "x = \"a\"");
    fold("x = ['a', 'b', 'c'].join(',')", "x = \"a,b,c\"");
    fold("x = ['a', foo, 'b', 'c'].join(',')",
        "x = [\"a\",foo,\"b,c\"].join()");
    fold("x = [foo, 'a', 'b', 'c'].join(',')",
        "x = [foo,\"a,b,c\"].join()");
    fold("x = ['a', 'b', 'c', foo].join(',')",
        "x = [\"a,b,c\",foo].join()");

    
    fold("x = ['a=', 5].join('')", "x = \"a=5\"");
    fold("x = ['a', '5'].join(7)", "x = \"a75\"");

    
    fold("x = ['a=', false].join('')", "x = \"a=false\"");
    fold("x = ['a', '5'].join(true)", "x = \"atrue5\"");
    fold("x = ['a', '5'].join(false)", "x = \"afalse5\"");

    
    fold("x = ['a', '5', 'c'].join('a very very very long chain')",
         "x = [\"a\",\"5\",\"c\"].join(\"a very very very long chain\")");

    
    foldSame("x = ['', foo].join('-')");
    foldSame("x = ['', foo, ''].join()");

    fold("x = ['', '', foo, ''].join(',')",
         "x = [',', foo, ''].join()");
    fold("x = ['', '', foo, '', ''].join(',')",
         "x = [',', foo, ','].join()");

    fold("x = ['', '', foo, '', '', bar].join(',')",
         "x = [',', foo, ',', bar].join()");

    fold("x = [1,2,3].join('abcdef')",
         "x = '1abcdef2abcdef3'");

    fold("x = [1,2].join()", "x = '1,2'");
    fold("x = [null,undefined,''].join(',')", "x = ',,'");
    fold("x = [null,undefined,0].join(',')", "x = ',,0'");
    
    foldSame("x = [[1,2],[3,4]].join()"); 
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testStringJoinAdd_b1992789
  public void testStringJoinAdd_b1992789() {
    fold("x = ['a'].join('')", "x = \"a\"");
    fold("x = [foo()].join('')", "x = '' + foo()");
    fold("[foo()].join('')", "'' + foo()");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testFoldStringSubstr
  public void testFoldStringSubstr() {
    fold("x = 'abcde'.substr(0,2)", "x = 'ab'");
    fold("x = 'abcde'.substr(1,2)", "x = 'bc'");
    fold("x = 'abcde'['substr'](1,3)", "x = 'bcd'");
    fold("x = 'abcde'.substr(2)", "x = 'cde'");

    
    foldSame("x = 'abcde'.substr(-1)");
    foldSame("x = 'abcde'.substr(1, -2)");
    foldSame("x = 'abcde'.substr(1, 2, 3)");
    foldSame("x = 'a'.substr(0, 2)");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testFoldStringSubstring
  public void testFoldStringSubstring() {
    fold("x = 'abcde'.substring(0,2)", "x = 'ab'");
    fold("x = 'abcde'.substring(1,2)", "x = 'b'");
    fold("x = 'abcde'['substring'](1,3)", "x = 'bc'");
    fold("x = 'abcde'.substring(2)", "x = 'cde'");

    
    foldSame("x = 'abcde'.substring(-1)");
    foldSame("x = 'abcde'.substring(1, -2)");
    foldSame("x = 'abcde'.substring(1, 2, 3)");
    foldSame("x = 'a'.substring(0, 2)");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testFoldStringCharAt
  public void testFoldStringCharAt() {
    fold("x = 'abcde'.charAt(0)", "x = 'a'");
    fold("x = 'abcde'.charAt(1)", "x = 'b'");
    fold("x = 'abcde'.charAt(2)", "x = 'c'");
    fold("x = 'abcde'.charAt(3)", "x = 'd'");
    fold("x = 'abcde'.charAt(4)", "x = 'e'");
    foldSame("x = 'abcde'.charAt(5)");  
    foldSame("x = 'abcde'.charAt(-1)");  
    foldSame("x = 'abcde'.charAt(y)");
    foldSame("x = 'abcde'.charAt()");  
    foldSame("x = 'abcde'.charAt(0, ++z)");  
    foldSame("x = 'abcde'.charAt(null)");  
    foldSame("x = 'abcde'.charAt(true)");  
    fold("x = '\\ud834\udd1e'.charAt(0)", "x = '\\ud834'");
    fold("x = '\\ud834\udd1e'.charAt(1)", "x = '\\udd1e'");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testFoldStringCharCodeAt
  public void testFoldStringCharCodeAt() {
    fold("x = 'abcde'.charCodeAt(0)", "x = 97");
    fold("x = 'abcde'.charCodeAt(1)", "x = 98");
    fold("x = 'abcde'.charCodeAt(2)", "x = 99");
    fold("x = 'abcde'.charCodeAt(3)", "x = 100");
    fold("x = 'abcde'.charCodeAt(4)", "x = 101");
    foldSame("x = 'abcde'.charCodeAt(5)");  
    foldSame("x = 'abcde'.charCodeAt(-1)");  
    foldSame("x = 'abcde'.charCodeAt(y)");
    foldSame("x = 'abcde'.charCodeAt()");  
    foldSame("x = 'abcde'.charCodeAt(0, ++z)");  
    foldSame("x = 'abcde'.charCodeAt(null)");  
    foldSame("x = 'abcde'.charCodeAt(true)");  
    fold("x = '\\ud834\udd1e'.charCodeAt(0)", "x = 55348");
    fold("x = '\\ud834\udd1e'.charCodeAt(1)", "x = 56606");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testFoldStringSplit
  public void testFoldStringSplit() {
    late = false;
    fold("x = 'abcde'.split('foo')", "x = ['abcde']");
    fold("x = 'abcde'.split()", "x = ['abcde']");
    fold("x = 'abcde'.split(null)", "x = ['abcde']");
    fold("x = 'a b c d e'.split(' ')", "x = ['a','b','c','d','e']");
    fold("x = 'a b c d e'.split(' ', 0)", "x = []");
    fold("x = 'abcde'.split('cd')", "x = ['ab','e']");
    fold("x = 'a b c d e'.split(' ', 1)", "x = ['a']");
    fold("x = 'a b c d e'.split(' ', 3)", "x = ['a','b','c']");
    fold("x = 'a b c d e'.split(null, 1)", "x = ['a b c d e']");
    fold("x = 'aaaaa'.split('a')", "x = ['', '', '', '', '', '']");
    fold("x = 'xyx'.split('x')", "x = ['', 'y', '']");

    
    fold("x = 'abcde'.split('')", "x = ['a','b','c','d','e']");
    fold("x = 'abcde'.split('', 3)", "x = ['a','b','c']");

    
    fold("x = ''.split('')", "x = []");

    
    fold("x = 'aaa'.split('aaa')", "x = ['','']");
    fold("x = ' '.split(' ')", "x = ['','']");

    foldSame("x = 'abcde'.split(/ /)");
    foldSame("x = 'abcde'.split(' ', -1)");

    late = true;
    foldSame("x = 'a b c d e'.split(' ')");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testJoinBug
  public void testJoinBug() {
    fold("var x = [].join();", "var x = '';");
    fold("var x = [x].join();", "var x = '' + x;");
    foldSame("var x = [x,y].join();");
    foldSame("var x = [x,y,z].join();");

    foldSame("shape['matrix'] = [\n" +
            "    Number(headingCos2).toFixed(4),\n" +
            "    Number(-headingSin2).toFixed(4),\n" +
            "    Number(headingSin2 * yScale).toFixed(4),\n" +
            "    Number(headingCos2 * yScale).toFixed(4),\n" +
            "    0,\n" +
            "    0\n" +
            "  ].join()");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testToUpper
  public void testToUpper() {
    fold("'a'.toUpperCase()", "'A'");
    fold("'A'.toUpperCase()", "'A'");
    fold("'aBcDe'.toUpperCase()", "'ABCDE'");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testToLower
  public void testToLower() {
    fold("'A'.toLowerCase()", "'a'");
    fold("'a'.toLowerCase()", "'a'");
    fold("'aBcDe'.toLowerCase()", "'abcde'");
  }

// com.google.javascript.jscomp.PeepholeReplaceKnownMethodsTest::testFoldParseNumbers
  public void testFoldParseNumbers() {
    enableNormalize();
    enableEcmaScript5(true);

    fold("x = parseInt('123')", "x = 123");
    fold("x = parseInt(' 123')", "x = 123");
    fold("x = parseInt('123', 10)", "x = 123");
    fold("x = parseInt('0xA')", "x = 10");
    fold("x = parseInt('0xA', 16)", "x = 10");
    fold("x = parseInt('07', 8)", "x = 7");
    fold("x = parseInt('08')", "x = 8");
    fold("x = parseInt('0')", "x = 0");
    fold("x = parseFloat('0')", "x = 0");
    fold("x = parseFloat('1.23')", "x = 1.23");
    fold("x = parseFloat('1.2300')", "x = 1.23");
    fold("x = parseFloat(' 0.3333')", "x = 0.3333");
    fold("x = parseFloat('0100')", "x = 100");
    fold("x = parseFloat('0100.000')", "x = 100");

    
    fold("x = parseInt(' 0xF', 16)", "x = 15");
    fold("x = parseInt(' F', 16)", "x = 15");
    fold("x = parseInt('17', 8)", "x = 15");
    fold("x = parseInt('015', 10)", "x = 15");
    fold("x = parseInt('1111', 2)", "x = 15");
    fold("x = parseInt('12', 13)", "x = 15");
    fold("x = parseInt(021, 8)", "x = 15");
    fold("x = parseInt(15.99, 10)", "x = 15");
    fold("x = parseFloat('3.14')", "x = 3.14");
    fold("x = parseFloat(3.14)", "x = 3.14");

    
    foldSame("x = parseInt('FXX123', 16)");
    foldSame("x = parseInt('15*3', 10)");
    foldSame("x = parseInt('15e2', 10)");
    foldSame("x = parseInt('15px', 10)");
    foldSame("x = parseInt('-0x08')");
    foldSame("x = parseInt('1', -1)");
    foldSame("x = parseFloat('3.14more non-digit characters')");
    foldSame("x = parseFloat('314e-2')");
    foldSame("x = parseFloat('0.0314E+2')");
    foldSame("x = parseFloat('3.333333333333333333333333')");

    
    foldSame("x = parseInt('0xa', 10)");
    foldSame("x = parseInt('')");

    enableEcmaScript5(false);
    foldSame("x = parseInt('08')");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldOneChildBlocks
  public void testFoldOneChildBlocks() {
    late = false;
    fold("function f(){if(x)a();x=3}",
        "function f(){x&&a();x=3}");
    fold("function f(){if(x){a()}x=3}",
        "function f(){x&&a();x=3}");
    fold("function f(){if(x){return 3}}",
        "function f(){if(x)return 3}");
    fold("function f(){if(x){a()}}",
        "function f(){x&&a()}");
    fold("function f(){if(x){throw 1}}", "function f(){if(x)throw 1;}");

    
    fold("function f(){if(x){foo()}}", "function f(){x&&foo()}");
    fold("function f(){if(x){foo()}else{bar()}}",
         "function f(){x?foo():bar()}");

    
    fold("function f(){if(x){a.b=1}}", "function f(){if(x)a.b=1}");
    fold("function f(){if(x){a.b*=1}}", "function f(){x&&(a.b*=1)}");
    fold("function f(){if(x){a.b+=1}}", "function f(){x&&(a.b+=1)}");
    fold("function f(){if(x){++a.b}}", "function f(){x&&++a.b}");
    fold("function f(){if(x){a.foo()}}", "function f(){x&&a.foo()}");

    
    fold("function f(){try{foo()}catch(e){bar(e)}finally{baz()}}",
         "function f(){try{foo()}catch(e){bar(e)}finally{baz()}}");

    
    fold("function f(){switch(x){case 1:break}}",
         "function f(){switch(x){case 1:break}}");

    
    fold("function f(){if(e1){do foo();while(e2)}else foo2()}",
         "function f(){if(e1){do foo();while(e2)}else foo2()}");
    
    fold("if(x){do{foo()}while(y)}else bar()",
         "if(x){do foo();while(y)}else bar()");

    
    fold("function f(){if(x){if(y)foo()}}",
         "function f(){x&&y&&foo()}");
    fold("function f(){if(x){if(y)foo();else bar()}}",
         "function f(){x&&(y?foo():bar())}");
    fold("function f(){if(x){if(y)foo()}else bar()}",
         "function f(){x?y&&foo():bar()}");
    fold("function f(){if(x){if(y)foo();else bar()}else{baz()}}",
         "function f(){x?y?foo():bar():baz()}");

    fold("if(e1){while(e2){if(e3){foo()}}}else{bar()}",
         "if(e1)while(e2)e3&&foo();else bar()");

    fold("if(e1){with(e2){if(e3){foo()}}}else{bar()}",
         "if(e1)with(e2)e3&&foo();else bar()");

    fold("if(a||b){if(c||d){var x;}}", "if(a||b)if(c||d)var x");
    fold("if(x){ if(y){var x;}else{var z;} }",
         "if(x)if(y)var x;else var z");

    
    
    
    fold("if(x){ if(y){var x;}else{var z;} }else{var w}",
         "if(x)if(y)var x;else var z;else var w");
    fold("if (x) {var x;}else { if (y) { var y;} }",
         "if(x)var x;else if(y)var y");

    
    fold("if(a){if(b){f1();f2();}else if(c){f3();}}else {if(d){f4();}}",
         "if(a)if(b){f1();f2()}else c&&f3();else d&&f4()");

    fold("function f(){foo()}", "function f(){foo()}");
    fold("switch(x){case y: foo()}", "switch(x){case y:foo()}");
    fold("try{foo()}catch(ex){bar()}finally{baz()}",
         "try{foo()}catch(ex){bar()}finally{baz()}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldReturns
  public void testFoldReturns() {
    fold("function f(){if(x)return 1;else return 2}",
         "function f(){return x?1:2}");
    fold("function f(){if(x)return 1;return 2}",
         "function f(){return x?1:2}");
    fold("function f(){if(x)return;return 2}",
         "function f(){return x?void 0:2}");
    fold("function f(){if(x)return 1+x;else return 2-x}",
         "function f(){return x?1+x:2-x}");
    fold("function f(){if(x)return 1+x;return 2-x}",
         "function f(){return x?1+x:2-x}");
    fold("function f(){if(x)return y += 1;else return y += 2}",
         "function f(){return x?(y+=1):(y+=2)}");

    fold("function f(){if(x)return;else return 2-x}",
         "function f(){if(x);else return 2-x}");
    fold("function f(){if(x)return;return 2-x}",
         "function f(){return x?void 0:2-x}");
    fold("function f(){if(x)return x;else return}",
         "function f(){if(x)return x;{}}");
    fold("function f(){if(x)return x;return}",
         "function f(){if(x)return x}");

    foldSame("function f(){for(var x in y) { return x.y; } return k}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testCombineIfs1
  public void testCombineIfs1() {
    fold("function f() {if (x) return 1; if (y) return 1}",
         "function f() {if (x||y) return 1;}");
    fold("function f() {if (x) return 1; if (y) foo(); else return 1}",
         "function f() {if ((!x)&&y) foo(); else return 1;}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testCombineIfs2
  public void testCombineIfs2() {
    
    foldSame("function f() {if (x) throw 1; if (y) throw 1}");
    
    fold("function f(){ if (x) g(); if (y) g() }",
         "function f(){ x&&g(); y&&g() }");
    
    fold("function f(){ if (x) y = 0; if (y) y = 0; }",
         "function f(){ x&&(y = 0); y&&(y = 0); }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testCombineIfs3
  public void testCombineIfs3() {
    foldSame("function f() {if (x) return 1; if (y) {g();f()}}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldAssignments
  public void testFoldAssignments() {
    fold("function f(){if(x)y=3;else y=4;}", "function f(){y=x?3:4}");
    fold("function f(){if(x)y=1+a;else y=2+a;}", "function f(){y=x?1+a:2+a}");

    
    fold("function f(){if(x)y+=1;else y+=2;}", "function f(){y+=x?1:2}");
    fold("function f(){if(x)y-=1;else y-=2;}", "function f(){y-=x?1:2}");
    fold("function f(){if(x)y%=1;else y%=2;}", "function f(){y%=x?1:2}");
    fold("function f(){if(x)y|=1;else y|=2;}", "function f(){y|=x?1:2}");

    
    foldSame("function f(){x ? y-=1 : y+=2}");

    
    foldSame("function f(){x ? y-=1 : z-=1}");

    
    foldSame("function f(){x ? y().a=3 : y().a=4}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveDuplicateStatements
  public void testRemoveDuplicateStatements() {
    fold("if (a) { x = 1; x++ } else { x = 2; x++ }",
         "x=(a) ? 1 : 2; x++");
    fold("if (a) { x = 1; x++; y += 1; z = pi; }" +
         " else  { x = 2; x++; y += 1; z = pi; }",
         "x=(a) ? 1 : 2; x++; y += 1; z = pi;");
    fold("function z() {" +
         "if (a) { foo(); return !0 } else { goo(); return !0 }" +
         "}",
         "function z() {(a) ? foo() : goo(); return !0}");
    fold("function z() {if (a) { foo(); x = true; return true " +
         "} else { goo(); x = true; return true }}",
         "function z() {(a) ? foo() : goo(); x = !0; return !0}");

    fold("function z() {" +
         "  if (a) { bar(); foo(); return true }" +
         "    else { bar(); goo(); return true }" +
         "}",
         "function z() {" +
         "  if (a) { bar(); foo(); }" +
         "    else { bar(); goo(); }" +
         "  return !0;" +
         "}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testNotCond
  public void testNotCond() {
    fold("function f(){if(!x)foo()}", "function f(){x||foo()}");
    fold("function f(){if(!x)b=1}", "function f(){x||(b=1)}");
    fold("if(!x)z=1;else if(y)z=2", "if(x){y&&(z=2);}else{z=1;}");
    fold("if(x)y&&(z=2);else z=1;", "x ? y&&(z=2) : z=1");
    foldSame("function f(){if(!(x=1))a.b=1}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testAndParenthesesCount
  public void testAndParenthesesCount() {
    fold("function f(){if(x||y)a.foo()}", "function f(){(x||y)&&a.foo()}");
    fold("function f(){if(x.a)x.a=0}",
         "function f(){x.a&&(x.a=0)}");
    foldSame("function f(){if(x()||y()){x()||y()}}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldLogicalOpStringCompare
  public void testFoldLogicalOpStringCompare() {
    
    
    assertResultString("if(foo() && false) z()", "foo()&&0&&z()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldNot
  public void testFoldNot() {
    fold("while(!(x==y)){a=b;}" , "while(x!=y){a=b;}");
    fold("while(!(x!=y)){a=b;}" , "while(x==y){a=b;}");
    fold("while(!(x===y)){a=b;}", "while(x!==y){a=b;}");
    fold("while(!(x!==y)){a=b;}", "while(x===y){a=b;}");
    
    foldSame("while(!(x>y)){a=b;}");
    foldSame("while(!(x>=y)){a=b;}");
    foldSame("while(!(x<y)){a=b;}");
    foldSame("while(!(x<=y)){a=b;}");
    foldSame("while(!(x<=NaN)){a=b;}");

    
    fold("x = !(y() && true)", "x = !y()");
    
    fold("x = !true", "x = !1");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldRegExpConstructor
  public void testFoldRegExpConstructor() {
    enableNormalize();

    
    fold("x = new RegExp",                    "x = RegExp()");
    
    fold("x = new RegExp(\"\")",              "x = RegExp(\"\")");
    fold("x = new RegExp(\"\", \"i\")",       "x = RegExp(\"\",\"i\")");
    
    testSame("x = RegExp(\"foobar\", \"bogus\")",
         PeepholeSubstituteAlternateSyntax.INVALID_REGULAR_EXPRESSION_FLAGS);
    
    fold("x = new RegExp(\"foobar\")",        "x = /foobar/");
    fold("x = RegExp(\"foobar\")",            "x = /foobar/");
    fold("x = new RegExp(\"foobar\", \"i\")", "x = /foobar/i");
    
    fold("x = new RegExp(\"\\\\.\", \"i\")",  "x = /\\./i");
    fold("x = new RegExp(\"/\", \"\")",       "x = /\\//");
    fold("x = new RegExp(\"[/]\", \"\")",     "x = /[/]/");
    fold("x = new RegExp(\"///\", \"\")",     "x = /\\/\\/\\//");
    fold("x = new RegExp(\"\\\\\\/\", \"\")", "x = /\\//");
    fold("x = new RegExp(\"\\n\")",           "x = /\\n/");
    fold("x = new RegExp('\\\\\\r')",         "x = /\\r/");

    
    
    String longRegexp = "";
    for (int i = 0; i < 200; i++) longRegexp += "x";
    foldSame("x = RegExp(\"" + longRegexp + "\")");

    
    
    disableNormalize();

    foldSame("x = new RegExp(\"foobar\")");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testVersionSpecificRegExpQuirks
  public void testVersionSpecificRegExpQuirks() {
    enableNormalize();

    
    enableEcmaScript5(false);
    fold("x = new RegExp(\"foobar\", \"g\")",
         "x = RegExp(\"foobar\",\"g\")");
    fold("x = new RegExp(\"foobar\", \"ig\")",
         "x = RegExp(\"foobar\",\"ig\")");
    
    enableEcmaScript5(true);
    fold("x = new RegExp(\"foobar\", \"ig\")",
         "x = /foobar/ig");
    
    
    enableEcmaScript5(false);
    fold("x = new RegExp(\"\\u2028\")", "x = RegExp(\"\\u2028\")");
    fold("x = new RegExp(\"\\\\\\\\u2028\")", "x = /\\\\u2028/");
    
    enableEcmaScript5(true);
    fold("x = new RegExp(\"\\u2028\\u2029\")", "x = /\\u2028\\u2029/");
    fold("x = new RegExp(\"\\\\u2028\")", "x = /\\u2028/");
    fold("x = new RegExp(\"\\\\\\\\u2028\")", "x = /\\\\u2028/");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldRegExpConstructorStringCompare
  public void testFoldRegExpConstructorStringCompare() {
    
    
    assertResultString("x=new RegExp(\"\\n\", \"i\")", "x=/\\n/i", true);
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testContainsUnicodeEscape
  public void testContainsUnicodeEscape() throws Exception {
    assertTrue(!PeepholeSubstituteAlternateSyntax.containsUnicodeEscape(""));
    assertTrue(!PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("foo"));
    assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape(
        "\u2028"));
    assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape(
        "\\u2028"));
    assertTrue(
        PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("foo\\u2028"));
    assertTrue(!PeepholeSubstituteAlternateSyntax.containsUnicodeEscape(
        "foo\\\\u2028"));
    assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape(
            "foo\\\\u2028bar\\u2028"));
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldLiteralObjectConstructors
  public void testFoldLiteralObjectConstructors() {
    enableNormalize();

    
    fold("x = new Object", "x = ({})");
    fold("x = new Object()", "x = ({})");
    fold("x = Object()", "x = ({})");

    disableNormalize();
    
    foldSame("x = new Object");
    foldSame("x = new Object()");
    foldSame("x = Object()");

    enableNormalize();

    
    foldSame("x = " +
         "(function f(){function Object(){this.x=4};return new Object();})();");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldLiteralArrayConstructors
  public void testFoldLiteralArrayConstructors() {
    enableNormalize();

    
    fold("x = new Array", "x = []");
    fold("x = new Array()", "x = []");
    fold("x = Array()", "x = []");

    
    fold("x = new Array(0)", "x = []");
    fold("x = Array(0)", "x = []");
    fold("x = new Array(\"a\")", "x = [\"a\"]");
    fold("x = Array(\"a\")", "x = [\"a\"]");

    
    fold("x = new Array(7)", "x = Array(7)");
    fold("x = Array(7)", "x = Array(7)");
    fold("x = new Array(y)", "x = Array(y)");
    fold("x = Array(y)", "x = Array(y)");
    fold("x = new Array(foo())", "x = Array(foo())");
    fold("x = Array(foo())", "x = Array(foo())");

    
    fold("x = new Array(1, 2, 3, 4)", "x = [1, 2, 3, 4]");
    fold("x = Array(1, 2, 3, 4)", "x = [1, 2, 3, 4]");
    fold("x = new Array('a', 1, 2, 'bc', 3, {}, 'abc')",
         "x = ['a', 1, 2, 'bc', 3, {}, 'abc']");
    fold("x = Array('a', 1, 2, 'bc', 3, {}, 'abc')",
         "x = ['a', 1, 2, 'bc', 3, {}, 'abc']");
    fold("x = new Array(Array(1, '2', 3, '4'))", "x = [[1, '2', 3, '4']]");
    fold("x = Array(Array(1, '2', 3, '4'))", "x = [[1, '2', 3, '4']]");
    fold("x = new Array(Object(), Array(\"abc\", Object(), Array(Array())))",
         "x = [{}, [\"abc\", {}, [[]]]]");
    fold("x = new Array(Object(), Array(\"abc\", Object(), Array(Array())))",
         "x = [{}, [\"abc\", {}, [[]]]]");

    disableNormalize();
    
    foldSame("x = new Array");
    foldSame("x = new Array()");
    foldSame("x = Array()");

    foldSame("x = new Array(0)");
    foldSame("x = Array(0)");
    foldSame("x = new Array(\"a\")");
    foldSame("x = Array(\"a\")");
    foldSame("x = new Array(7)");
    foldSame("x = Array(7)");
    foldSame("x = new Array(foo())");
    foldSame("x = Array(foo())");

    foldSame("x = new Array(1, 2, 3, 4)");
    foldSame("x = Array(1, 2, 3, 4)");
    foldSame("x = new Array('a', 1, 2, 'bc', 3, {}, 'abc')");
    foldSame("x = Array('a', 1, 2, 'bc', 3, {}, 'abc')");
    foldSame("x = new Array(Array(1, '2', 3, '4'))");
    foldSame("x = Array(Array(1, '2', 3, '4'))");
    foldSame("x = new Array(" +
        "Object(), Array(\"abc\", Object(), Array(Array())))");
    foldSame("x = new Array(" +
        "Object(), Array(\"abc\", Object(), Array(Array())))");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testMinimizeExprCondition
  public void testMinimizeExprCondition() {
    fold("(x ? true : false) && y()", "x&&y()");
    fold("(x ? false : true) && y()", "(!x)&&y()");
    fold("(x ? true : y) && y()", "(x || y)&&y()");
    fold("(x ? y : false) && y()", "(x && y)&&y()");
    fold("(x && true) && y()", "x && y()");
    fold("(x && false) && y()", "0&&y()");
    fold("(x || true) && y()", "1&&y()");
    fold("(x || false) && y()", "x&&y()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testMinimizeWhileCondition
  public void testMinimizeWhileCondition() {
    
    fold("while(!!true) foo()", "while(1) foo()");
    
    fold("while(!!x) foo()", "while(x) foo()");
    fold("while(!(!x&&!y)) foo()", "while(x||y) foo()");
    fold("while(x||!!y) foo()", "while(x||y) foo()");
    fold("while(!(!!x&&y)) foo()", "while(!x||!y) foo()");
    fold("while(!(!x&&y)) foo()", "while(x||!y) foo()");
    fold("while(!(x||!y)) foo()", "while(!x&&y) foo()");
    fold("while(!(x||y)) foo()", "while(!x&&!y) foo()");
    fold("while(!(!x||y-z)) foo()", "while(x&&!(y-z)) foo()");
    fold("while(!(!(x/y)||z+w)) foo()", "while(x/y&&!(z+w)) foo()");
    foldSame("while(!(x+y||z)) foo()");
    foldSame("while(!(x&&y*z)) foo()");
    fold("while(!(!!x&&y)) foo()", "while(!x||!y) foo()");
    fold("while(x&&!0) foo()", "while(x) foo()");
    fold("while(x||!1) foo()", "while(x) foo()");
    fold("while(!((x,y)&&z)) foo()", "while(!(x,y)||!z) foo()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testMinimizeForCondition
  public void testMinimizeForCondition() {
    
    
    fold("for(;!!true;) foo()", "for(;1;) foo()");
    
    fold("for(!!true;;) foo()", "for(!0;;) foo()");

    
    fold("for(;!!x;) foo()", "for(;x;) foo()");

    
    foldSame("for(a in b) foo()");
    foldSame("for(a in {}) foo()");
    foldSame("for(a in []) foo()");
    fold("for(a in !!true) foo()", "for(a in !0) foo()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testMinimizeCondition_example1
  public void testMinimizeCondition_example1() {
    
    fold("if(!!(f() > 20)) {foo();foo()}", "if(f() > 20){foo();foo()}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldLoopBreakLate
  public void testFoldLoopBreakLate() {
    late = true;
    fold("for(;;) if (a) break", "for(;!a;);");
    foldSame("for(;;) if (a) { f(); break }");
    fold("for(;;) if (a) break; else f()", "for(;!a;) { { f(); } }");
    fold("for(;a;) if (b) break", "for(;a && !b;);");
    fold("for(;a;) { if (b) break; if (c) break; }",
         "for(;(a && !b);) if (c) break;");
    fold("for(;(a && !b);) if (c) break;", "for(;(a && !b) && !c;);");

    
    enableNormalize(true);
    fold("while(true) if (a) break", "for(;1&&!a;);");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldLoopBreakEarly
  public void testFoldLoopBreakEarly() {
    late = false;
    foldSame("for(;;) if (a) break");
    foldSame("for(;;) if (a) { f(); break }");
    foldSame("for(;;) if (a) break; else f()");
    foldSame("for(;a;) if (b) break");
    foldSame("for(;a;) { if (b) break; if (c) break; }");

    foldSame("while(1) if (a) break");
    enableNormalize(true);
    foldSame("while(1) if (a) break");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldConditionalVarDeclaration
  public void testFoldConditionalVarDeclaration() {
    fold("if(x) var y=1;else y=2", "var y=x?1:2");
    fold("if(x) y=1;else var y=2", "var y=x?1:2");

    foldSame("if(x) var y = 1; z = 2");
    foldSame("if(x||y) y = 1; var z = 2");

    foldSame("if(x) { var y = 1; print(y)} else y = 2 ");
    foldSame("if(x) var y = 1; else {y = 2; print(y)}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldReturnResult
  public void testFoldReturnResult() {
    fold("function f(){return false;}", "function f(){return !1}");
    foldSame("function f(){return null;}");
    fold("function f(){return void 0;}",
         "function f(){return}");
    fold("function f(){return;}",
         "function f(){}");
    foldSame("function f(){return void foo();}");
    fold("function f(){return undefined;}",
         "function f(){return}");
    fold("function f(){if(a()){return undefined;}}",
         "function f(){if(a()){return}}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldStandardConstructors
  public void testFoldStandardConstructors() {
    foldSame("new Foo('a')");
    foldSame("var x = new goog.Foo(1)");
    foldSame("var x = new String(1)");
    foldSame("var x = new Number(1)");
    foldSame("var x = new Boolean(1)");

    enableNormalize();

    fold("var x = new Object('a')", "var x = Object('a')");
    fold("var x = new RegExp('')", "var x = RegExp('')");
    fold("var x = new Error('20')", "var x = Error(\"20\")");
    fold("var x = new Array(20)", "var x = Array(20)");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testSubsituteReturn
  public void testSubsituteReturn() {

    fold("function f() { while(x) { return }}",
         "function f() { while(x) { break }}");

    foldSame("function f() { while(x) { return 5 } }");

    foldSame("function f() { a: { return 5 } }");

    fold("function f() { while(x) { return 5}  return 5}",
         "function f() { while(x) { break }    return 5}");

    fold("function f() { while(x) { return x}  return x}",
         "function f() { while(x) { break }    return x}");

    fold("function f() { while(x) { if (y) { return }}}",
         "function f() { while(x) { if (y) { break  }}}");

    fold("function f() { while(x) { if (y) { return }} return}",
         "function f() { while(x) { if (y) { break  }}}");

    fold("function f() { while(x) { if (y) { return 5 }} return 5}",
         "function f() { while(x) { if (y) { break    }} return 5}");

    
    
    fold("function f() { while(x) { if (y) { return x } x = 1} return x}",
         "function f() { while(x) { if (y) { break    } x = 1} return x}");

    
    fold("function f() { while(x) { if (y) { return x } return x} return x}",
         "function f() { while(x) { if (y) {} break }return x}");

    
    foldSame("function f() { while(x) { while (y) { return } } }");

    foldSame("function f() { while(1) { return 7}  return 5}");

    foldSame("function f() {" +
             "  try { while(x) {return f()}} catch (e) { } return f()}");

    foldSame("function f() {" +
             "  try { while(x) {return f()}} finally {alert(1)} return f()}");

    
    fold("function f() {" +
         "  try { while(x) { return f() } return f() } catch (e) { } }",
         "function f() {" +
         "  try { while(x) { break } return f() } catch (e) { } }");

    
    foldSame("function f() {" +
             "  try { while(x) { return foo() } } finally { alert(1) } "  +
             "  return foo()}");

    
    fold("function f() {" +
         "  try { while(x) { return 1 } } finally { alert(1) } return 1}",
         "function f() {" +
         "  try { while(x) { break    } } finally { alert(1) } return 1}"
         );

    foldSame("function f() { try{ return a } finally { a = 2 } return a; }");

    fold(
      "function f() { switch(a){ case 1: return a; default: g();} return a;}",
      "function f() { switch(a){ case 1: break; default: g();} return a; }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testSubsituteBreakForThrow
  public void testSubsituteBreakForThrow() {

    foldSame("function f() { while(x) { throw Error }}");

    fold("function f() { while(x) { throw Error } throw Error }",
         "function f() { while(x) { break } throw Error}");
    foldSame("function f() { while(x) { throw Error(1) } throw Error(2)}");
    foldSame("function f() { while(x) { throw Error(1) } return Error(2)}");

    foldSame("function f() { while(x) { throw 5 } }");

    foldSame("function f() { a: { throw 5 } }");

    fold("function f() { while(x) { throw 5}  throw 5}",
         "function f() { while(x) { break }   throw 5}");

    fold("function f() { while(x) { throw x}  throw x}",
         "function f() { while(x) { break }   throw x}");

    foldSame("function f() { while(x) { if (y) { throw Error }}}");

    fold("function f() { while(x) { if (y) { throw Error }} throw Error}",
         "function f() { while(x) { if (y) { break }} throw Error}");

    fold("function f() { while(x) { if (y) { throw 5 }} throw 5}",
         "function f() { while(x) { if (y) { break    }} throw 5}");

    
    
    fold("function f() { while(x) { if (y) { throw x } x = 1} throw x}",
         "function f() { while(x) { if (y) { break    } x = 1} throw x}");

    
    fold("function f() { while(x) { if (y) { throw x } throw x} throw x}",
         "function f() { while(x) { if (y) {} break }throw x}");

    
    foldSame("function f() { while(x) { while (y) { throw Error } } }");

    foldSame("function f() { while(1) { throw 7}  throw 5}");

    foldSame("function f() {" +
             "  try { while(x) {throw f()}} catch (e) { } throw f()}");

    foldSame("function f() {" +
             "  try { while(x) {throw f()}} finally {alert(1)} throw f()}");

    
    fold("function f() {" +
         "  try { while(x) { throw f() } throw f() } catch (e) { } }",
         "function f() {" +
         "  try { while(x) { break } throw f() } catch (e) { } }");

    
    foldSame("function f() {" +
             "  try { while(x) { throw foo() } } finally { alert(1) } "  +
             "  throw foo()}");

    
    fold("function f() {" +
         "  try { while(x) { throw 1 } } finally { alert(1) } throw 1}",
         "function f() {" +
         "  try { while(x) { break    } } finally { alert(1) } throw 1}"
         );

    foldSame("function f() { try{ throw a } finally { a = 2 } throw a; }");

    fold(
      "function f() { switch(a){ case 1: throw a; default: g();} throw a;}",
      "function f() { switch(a){ case 1: break; default: g();} throw a; }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveDuplicateReturn
  public void testRemoveDuplicateReturn() {
    fold("function f() { return; }",
         "function f(){}");
    foldSame("function f() { return a; }");
    fold("function f() { if (x) { return a } return a; }",
         "function f() { if (x) {} return a; }");
    foldSame(
      "function f() { try { if (x) { return a } } catch(e) {} return a; }");
    foldSame(
      "function f() { try { if (x) {} } catch(e) {} return 1; }");

    
    foldSame(
      "function f() { try { if (x) { return a } } finally { a++ } return a; }");
    
    
    fold("function f() { try { if (x) { return 1 } } finally {} return 1; }",
         "function f() { try { if (x) {} } finally {} return 1; }");

    fold("function f() { switch(a){ case 1: return a; } return a; }",
         "function f() { switch(a){ case 1: } return a; }");

    fold("function f() { switch(a){ " +
         "  case 1: return a; case 2: return a; } return a; }",
         "function f() { switch(a){ " +
         "  case 1: break; case 2: } return a; }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveDuplicateThrow
  public void testRemoveDuplicateThrow() {
    foldSame("function f() { throw a; }");
    fold("function f() { if (x) { throw a } throw a; }",
         "function f() { if (x) {} throw a; }");
    foldSame(
      "function f() { try { if (x) {throw a} } catch(e) {} throw a; }");
    foldSame(
      "function f() { try { if (x) {throw 1} } catch(e) {f()} throw 1; }");
    foldSame(
      "function f() { try { if (x) {throw 1} } catch(e) {f()} throw 1; }");
    foldSame(
      "function f() { try { if (x) {throw 1} } catch(e) {throw 1}}");
    fold(
      "function f() { try { if (x) {throw 1} } catch(e) {throw 1} throw 1; }",
      "function f() { try { if (x) {throw 1} } catch(e) {} throw 1; }");

    
    foldSame(
      "function f() { try { if (x) { throw a } } finally { a++ } throw a; }");
    
    
    fold("function f() { try { if (x) { throw 1 } } finally {} throw 1; }",
         "function f() { try { if (x) {} } finally {} throw 1; }");

    fold("function f() { switch(a){ case 1: throw a; } throw a; }",
         "function f() { switch(a){ case 1: } throw a; }");

    fold("function f() { switch(a){ " +
             "case 1: throw a; case 2: throw a; } throw a; }",
         "function f() { switch(a){ case 1: break; case 2: } throw a; }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testNestedIfCombine
  public void testNestedIfCombine() {
    fold("if(x)if(y){while(1){}}", "if(x&&y){while(1){}}");
    fold("if(x||z)if(y){while(1){}}", "if((x||z)&&y){while(1){}}");
    fold("if(x)if(y||z){while(1){}}", "if((x)&&(y||z)){while(1){}}");
    foldSame("if(x||z)if(y||z){while(1){}}");
    fold("if(x)if(y){if(z){while(1){}}}", "if(x&&y&&z){while(1){}}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldTrueFalse
  public void testFoldTrueFalse() {
    fold("x = true", "x = !0");
    fold("x = false", "x = !1");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testIssue291
  public void testIssue291() {
    fold("if (true) { f.onchange(); }", "if (1) f.onchange();");
    foldSame("if (f) { f.onchange(); }");
    foldSame("if (f) { f.bar(); } else { f.onchange(); }");
    fold("if (f) { f.bonchange(); }", "f && f.bonchange();");
    foldSame("if (f) { f['x'](); }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testUndefined
  public void testUndefined() {
    foldSame("var x = undefined");
    foldSame("function f(f) {var undefined=2;var x = undefined;}");
    this.enableNormalize();
    fold("var x = undefined", "var x=void 0");
    foldSame(
        "var undefined = 1;" +
        "function f() {var undefined=2;var x = undefined;}");
    foldSame("function f(undefined) {}");
    foldSame("try {} catch(undefined) {}");
    foldSame("for (undefined in {}) {}");
    foldSame("undefined++;");
    fold("undefined += undefined;", "undefined += void 0;");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testSplitCommaExpressions
  public void testSplitCommaExpressions() {
    late = false;
    
    foldSame("while (foo(), !0) boo()");
    foldSame("var a = (foo(), !0);");
    foldSame("a = (foo(), !0);");

    
    foldSame("a:a(),b()");

    fold("(x=2), foo()", "x=2; foo()");
    fold("foo(), boo();", "foo(); boo()");
    fold("(a(), b()), (c(), d());", "a(); b(); (c(), d());");
    fold("a(); b(); (c(), d());", "a(); b(); c(); d();");
    fold("foo(), true", "foo();true");
    fold("foo();true", "foo();1");
    fold("function x(){foo(), !0}", "function x(){foo(); !0}");
    fold("function x(){foo(); !0}", "function x(){foo(); 1}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testComma1
  public void testComma1() {
    late = false;
    fold("1, 2", "1; 2");
    fold("1; 2", "1; 1");
    late = true;
    foldSame("1, 2");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testComma2
  public void testComma2() {
    late = false;
    test("1, a()", "1; a()");
    late = true;
    foldSame("1, a()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testComma3
  public void testComma3() {
    late = false;
    test("1, a(), b()", "1; a(); b()");
    late = true;
    foldSame("1, a(), b()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testComma4
  public void testComma4() {
    late = false;
    test("a(), b()", "a();b()");
    late = true;
    foldSame("a(), b()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testComma5
  public void testComma5() {
    late = false;
    test("a(), b(), 1", "a();b();1");
    late = true;
    foldSame("a(), b(), 1");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testObjectLiteral
  public void testObjectLiteral() {
    test("({})", "1");
    test("({a:1})", "1");
    testSame("({a:foo()})");
    testSame("({'a':foo()})");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testArrayLiteral
  public void testArrayLiteral() {
    test("([])", "1");
    test("([1])", "1");
    test("([a])", "1");
    testSame("([foo()])");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testStringArraySplitting
  public void testStringArraySplitting() {
    testSame("var x=['1','2','3','4']");
    testSame("var x=['1','2','3','4','5']");
    test("var x=['1','2','3','4','5','6']",
         "var x='123456'.split('')");
    test("var x=['1','2','3','4','5','00']",
         "var x='1 2 3 4 5 00'.split(' ')");
    test("var x=['1','2','3','4','5','6','7']",
        "var x='1234567'.split('')");
    test("var x=['1','2','3','4','5','6','00']",
         "var x='1 2 3 4 5 6 00'.split(' ')");
    test("var x=[' ,',',',',',',',',',',']",
         "var x=' ,;,;,;,;,;,'.split(';')");
    test("var x=[',,',' ',',',',',',',',']",
         "var x=',,; ;,;,;,;,'.split(';')");
    test("var x=['a,',' ',',',',',',',',']",
         "var x='a,; ;,;,;,;,'.split(';')");

    
    testSame("var x=[',', ' ', ';', '{', '}']");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveElseCause
  public void testRemoveElseCause() {
    test("function f() {" +
         " if(x) return 1;" +
         " else if(x) return 2;" +
         " else if(x) return 3 }",
         "function f() {" +
         " if(x) return 1;" +
         "{ if(x) return 2;" +
         "{ if(x) return 3 } } }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveElseCause1
  public void testRemoveElseCause1() {
    test("function f() { if (x) throw 1; else f() }",
         "function f() { if (x) throw 1; { f() } }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveElseCause2
  public void testRemoveElseCause2() {
    test("function f() { if (x) return 1; else f() }",
         "function f() { if (x) return 1; { f() } }");
    test("function f() { if (x) return; else f() }",
         "function f() { if (x) {} else { f() } }");
    
    testSame("function f() { if (x) return; f() }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveElseCause3
  public void testRemoveElseCause3() {
    testSame("function f() { a:{if (x) break a; else f() } }");
    testSame("function f() { if (x) { a:{ break a } } else f() }");
    testSame("function f() { if (x) a:{ break a } else f() }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveElseCause4
  public void testRemoveElseCause4() {
    testSame("function f() { if (x) { if (y) { return 1; } } else f() }");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testIssue925
  public void testIssue925() {
    test(
        "if (x[--y] === 1) {\n" +
        "    x[y] = 0;\n" +
        "} else {\n" +
        "    x[y] = 1;\n" +
        "}",
        "(x[--y] === 1) ? x[y] = 0 : x[y] = 1;");

    test(
        "if (x[--y]) {\n" +
        "    a = 0;\n" +
        "} else {\n" +
        "    a = 1;\n" +
        "}",
        "a = (x[--y]) ? 0 : 1;");

    test("if (x++) { x += 2 } else { x += 3 }",
         "x++ ? x += 2 : x += 3");

    test("if (x++) { x = x + 2 } else { x = x + 3 }",
        "x = x++ ? x + 2 : x + 3");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testBindToCall1
  public void testBindToCall1() {
    test("(goog.bind(f))()", "f()");
    test("(goog.bind(f,a))()", "f.call(a)");
    test("(goog.bind(f,a,b))()", "f.call(a,b)");

    test("(goog.bind(f))(a)", "f(a)");
    test("(goog.bind(f,a))(b)", "f.call(a,b)");
    test("(goog.bind(f,a,b))(c)", "f.call(a,b,c)");

    test("(goog.partial(f))()", "f()");
    test("(goog.partial(f,a))()", "f(a)");
    test("(goog.partial(f,a,b))()", "f(a,b)");

    test("(goog.partial(f))(a)", "f(a)");
    test("(goog.partial(f,a))(b)", "f(a,b)");
    test("(goog.partial(f,a,b))(c)", "f(a,b,c)");

    test("((function(){}).bind())()", "((function(){}))()");
    test("((function(){}).bind(a))()", "((function(){})).call(a)");
    test("((function(){}).bind(a,b))()", "((function(){})).call(a,b)");

    test("((function(){}).bind())(a)", "((function(){}))(a)");
    test("((function(){}).bind(a))(b)", "((function(){})).call(a,b)");
    test("((function(){}).bind(a,b))(c)", "((function(){})).call(a,b,c)");

    
    testSame("(f.bind())()");
    testSame("(f.bind(a))()");
    testSame("(f.bind())(a)");
    testSame("(f.bind(a))(b)");

    
    testSame("(goog.bind(f)).call(g)");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testBindToCall2
  public void testBindToCall2() {
    test("(goog$bind(f))()", "f()");
    test("(goog$bind(f,a))()", "f.call(a)");
    test("(goog$bind(f,a,b))()", "f.call(a,b)");

    test("(goog$bind(f))(a)", "f(a)");
    test("(goog$bind(f,a))(b)", "f.call(a,b)");
    test("(goog$bind(f,a,b))(c)", "f.call(a,b,c)");

    test("(goog$partial(f))()", "f()");
    test("(goog$partial(f,a))()", "f(a)");
    test("(goog$partial(f,a,b))()", "f(a,b)");

    test("(goog$partial(f))(a)", "f(a)");
    test("(goog$partial(f,a))(b)", "f(a,b)");
    test("(goog$partial(f,a,b))(c)", "f(a,b,c)");

    
    testSame("(goog$bind(f)).call(g)");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testBindToCall3
  public void testBindToCall3() {
    
    
    
    
    
    
    new StringCompareTestCase().testBindToCall3();
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testSimpleFunctionCall
  public void testSimpleFunctionCall() {
    test("var a = String(23)", "var a = '' + 23");
    test("var a = String('hello')", "var a = '' + 'hello'");
    testSame("var a = String('hello', bar());");
    testSame("var a = String({valueOf: function() { return 1; }});");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testBindToCall3
    public void testBindToCall3() {
      test("(goog.bind(f.m))()", "(0,f.m)()");
      test("(goog.bind(f.m,a))()", "f.m.call(a)");

      test("(goog.bind(f.m))(a)", "(0,f.m)(a)");
      test("(goog.bind(f.m,a))(b)", "f.m.call(a,b)");

      test("(goog.partial(f.m))()", "(0,f.m)()");
      test("(goog.partial(f.m,a))()", "(0,f.m)(a)");

      test("(goog.partial(f.m))(a)", "(0,f.m)(a)");
      test("(goog.partial(f.m,a))(b)", "(0,f.m)(a,b)");

      
      testSame("f.m.bind()()");
      testSame("f.m.bind(a)()");
      testSame("f.m.bind()(a)");
      testSame("f.m.bind(a)(b)");

      
      testSame("goog.bind(f.m).call(g)");
    }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSimpleProvides
  public void testSimpleProvides() {
    test("goog.provide('foo');",
         "var foo={};");
    test("goog.provide('foo.bar');",
         "var foo={}; foo.bar={};");
    test("goog.provide('foo.bar.baz');",
         "var foo={}; foo.bar={}; foo.bar.baz={};");
    test("goog.provide('foo.bar.baz.boo');",
         "var foo={}; foo.bar={}; foo.bar.baz={}; foo.bar.baz.boo={};");
    test("goog.provide('goog.bar');",
         "goog.bar={};");  
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMultipleProvides
  public void testMultipleProvides() {
    test("goog.provide('foo.bar'); goog.provide('foo.baz');",
         "var foo={}; foo.bar={}; foo.baz={};");
    test("goog.provide('foo.bar.baz'); goog.provide('foo.boo.foo');",
         "var foo={}; foo.bar={}; foo.bar.baz={}; foo.boo={}; foo.boo.foo={};");
    test("goog.provide('foo.bar.baz'); goog.provide('foo.bar.boo');",
         "var foo={}; foo.bar={}; foo.bar.baz={}; foo.bar.boo={};");
    test("goog.provide('foo.bar.baz'); goog.provide('goog.bar.boo');",
         "var foo={}; foo.bar={}; foo.bar.baz={}; goog.bar={}; " +
         "goog.bar.boo={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalOfProvidedObjLit
  public void testRemovalOfProvidedObjLit() {
    test("goog.provide('foo'); foo = 0;",
         "var foo = 0;");
    test("goog.provide('foo'); foo = {a: 0};",
         "var foo = {a: 0};");
    test("goog.provide('foo'); foo = function(){};",
         "var foo = function(){};");
    test("goog.provide('foo'); var foo = 0;",
         "var foo = 0;");
    test("goog.provide('foo'); var foo = {a: 0};",
         "var foo = {a: 0};");
    test("goog.provide('foo'); var foo = function(){};",
         "var foo = function(){};");
    test("goog.provide('foo.bar.Baz'); foo.bar.Baz=function(){};",
         "var foo={}; foo.bar={}; foo.bar.Baz=function(){};");
    test("goog.provide('foo.bar.moo'); foo.bar.moo={E:1,S:2};",
         "var foo={}; foo.bar={}; foo.bar.moo={E:1,S:2};");
    test("goog.provide('foo.bar.moo'); foo.bar.moo={E:1}; foo.bar.moo={E:2};",
         "var foo={}; foo.bar={}; foo.bar.moo={E:1}; foo.bar.moo={E:2};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvidedDeclaredFunctionError
  public void testProvidedDeclaredFunctionError() {
    test("goog.provide('foo'); function foo(){}",
         null, FUNCTION_NAMESPACE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignment1
  public void testRemovalMultipleAssignment1() {
    test("goog.provide('foo'); foo = 0; foo = 1",
         "var foo = 0; foo = 1;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignment2
  public void testRemovalMultipleAssignment2() {
    test("goog.provide('foo'); var foo = 0; foo = 1",
         "var foo = 0; foo = 1;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignment3
  public void testRemovalMultipleAssignment3() {
    test("goog.provide('foo'); foo = 0; var foo = 1",
         "foo = 0; var foo = 1;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignment4
  public void testRemovalMultipleAssignment4() {
    test("goog.provide('foo.bar'); foo.bar = 0; foo.bar = 1",
         "var foo = {}; foo.bar = 0; foo.bar = 1");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoRemovalFunction1
  public void testNoRemovalFunction1() {
    test("goog.provide('foo'); function f(){foo = 0}",
         "var foo = {}; function f(){foo = 0}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoRemovalFunction2
  public void testNoRemovalFunction2() {
    test("goog.provide('foo'); function f(){var foo = 0}",
         "var foo = {}; function f(){var foo = 0}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignmentInIf1
  public void testRemovalMultipleAssignmentInIf1() {
    test("goog.provide('foo'); if (true) { var foo = 0 } else { foo = 1 }",
         "if (true) { var foo = 0 } else { foo = 1 }");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignmentInIf2
  public void testRemovalMultipleAssignmentInIf2() {
    test("goog.provide('foo'); if (true) { foo = 0 } else { var foo = 1 }",
         "if (true) { foo = 0 } else { var foo = 1 }");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignmentInIf3
  public void testRemovalMultipleAssignmentInIf3() {
    test("goog.provide('foo'); if (true) { foo = 0 } else { foo = 1 }",
         "if (true) { var foo = 0 } else { foo = 1 }");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignmentInIf4
  public void testRemovalMultipleAssignmentInIf4() {
    test("goog.provide('foo.bar');" +
         "if (true) { foo.bar = 0 } else { foo.bar = 1 }",
         "var foo = {}; if (true) { foo.bar = 0 } else { foo.bar = 1 }");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMultipleDeclarationError1
  public void testMultipleDeclarationError1() {
    String rest = "if (true) { foo.bar = 0 } else { foo.bar = 1 }";
    test("goog.provide('foo.bar');" + "var foo = {};" + rest,
         "var foo = {};" + "var foo = {};" + rest);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMultipleDeclarationError2
  public void testMultipleDeclarationError2() {
    test("goog.provide('foo.bar');" +
         "if (true) { var foo = {}; foo.bar = 0 } else { foo.bar = 1 }",
         "var foo = {};" +
         "if (true) {" +
         "  var foo = {}; foo.bar = 0" +
         "} else {" +
         "  foo.bar = 1" +
         "}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMultipleDeclarationError3
  public void testMultipleDeclarationError3() {
    test("goog.provide('foo.bar');" +
         "if (true) { foo.bar = 0 } else { var foo = {}; foo.bar = 1 }",
         "var foo = {};" +
         "if (true) {" +
         "  foo.bar = 0" +
         "} else {" +
         "  var foo = {}; foo.bar = 1" +
         "}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideAfterDeclarationError
  public void testProvideAfterDeclarationError() {
    test("var x = 42; goog.provide('x');",
         "var x = 42; var x = {}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideErrorCases
  public void testProvideErrorCases() {
    test("goog.provide();", "", NULL_ARGUMENT_ERROR);
    test("goog.provide(5);", "", INVALID_ARGUMENT_ERROR);
    test("goog.provide([]);", "", INVALID_ARGUMENT_ERROR);
    test("goog.provide({});", "", INVALID_ARGUMENT_ERROR);
    test("goog.provide('foo', 'bar');", "", TOO_MANY_ARGUMENTS_ERROR);
    test("goog.provide('foo'); goog.provide('foo');", "",
        DUPLICATE_NAMESPACE_ERROR);
    test("goog.provide('foo.bar'); goog.provide('foo'); goog.provide('foo');",
        "", DUPLICATE_NAMESPACE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalOfRequires
  public void testRemovalOfRequires() {
    test("goog.provide('foo'); goog.require('foo');",
         "var foo={};");
    test("goog.provide('foo.bar'); goog.require('foo.bar');",
         "var foo={}; foo.bar={};");
    test("goog.provide('foo.bar.baz'); goog.require('foo.bar.baz');",
         "var foo={}; foo.bar={}; foo.bar.baz={};");
    test("goog.provide('foo'); var x = 3; goog.require('foo'); something();",
         "var foo={}; var x = 3; something();");
    testSame("foo.require('foo.bar');");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRequireErrorCases
  public void testRequireErrorCases() {
    test("goog.require();", "", NULL_ARGUMENT_ERROR);
    test("goog.require(5);", "", INVALID_ARGUMENT_ERROR);
    test("goog.require([]);", "", INVALID_ARGUMENT_ERROR);
    test("goog.require({});", "", INVALID_ARGUMENT_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testLateProvides
  public void testLateProvides() {
    test("goog.require('foo'); goog.provide('foo');",
         "var foo={};", LATE_PROVIDE_ERROR);
    test("goog.require('foo.bar'); goog.provide('foo.bar');",
         "var foo={}; foo.bar={};", LATE_PROVIDE_ERROR);
    test("goog.provide('foo.bar'); goog.require('foo'); goog.provide('foo');",
         "var foo={}; foo.bar={};", LATE_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMissingProvides
  public void testMissingProvides() {
    test("goog.require('foo');",
         "", MISSING_PROVIDE_ERROR);
    test("goog.provide('foo'); goog.require('Foo');",
         "var foo={};", MISSING_PROVIDE_ERROR);
    test("goog.provide('foo'); goog.require('foo.bar');",
         "var foo={};", MISSING_PROVIDE_ERROR);
    test("goog.provide('foo'); var EXPERIMENT_FOO = true; " +
             "if (EXPERIMENT_FOO) {goog.require('foo.bar');}",
         "var foo={}; var EXPERIMENT_FOO = true; if (EXPERIMENT_FOO) {}",
         MISSING_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testAddDependency
  public void testAddDependency() {
    test("goog.addDependency('x.js', ['A', 'B'], []);", "0");

    Compiler compiler = getLastCompiler();
    assertTrue(compiler.getTypeRegistry().isForwardDeclaredType("A"));
    assertTrue(compiler.getTypeRegistry().isForwardDeclaredType("B"));
    assertFalse(compiler.getTypeRegistry().isForwardDeclaredType("C"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidSetCssNameMapping
  public void testValidSetCssNameMapping() {
    test("goog.setCssNameMapping({foo:'bar',\"biz\":'baz'});", "");
    CssRenamingMap map = getLastCompiler().getCssRenamingMap();
    assertNotNull(map);
    assertEquals("bar", map.get("foo"));
    assertEquals("baz", map.get("biz"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidSetCssNameMappingWithType
  public void testValidSetCssNameMappingWithType() {
    test("goog.setCssNameMapping({foo:'bar',\"biz\":'baz'}, 'BY_PART');", "");
    CssRenamingMap map = getLastCompiler().getCssRenamingMap();
    assertNotNull(map);
    assertEquals("bar", map.get("foo"));
    assertEquals("baz", map.get("biz"));

    test("goog.setCssNameMapping({foo:'bar',biz:'baz','biz-foo':'baz-bar'}," +
        " 'BY_WHOLE');", "");
    map = getLastCompiler().getCssRenamingMap();
    assertNotNull(map);
    assertEquals("bar", map.get("foo"));
    assertEquals("baz", map.get("biz"));
    assertEquals("baz-bar", map.get("biz-foo"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSetCssNameMappingNonStringValueReturnsError
  public void testSetCssNameMappingNonStringValueReturnsError() {
    
    test("var BAR = {foo:'bar'}; goog.setCssNameMapping(BAR);", "",
        EXPECTED_OBJECTLIT_ERROR);
    test("goog.setCssNameMapping([]);", "",
        EXPECTED_OBJECTLIT_ERROR);
    test("goog.setCssNameMapping(false);", "",
        EXPECTED_OBJECTLIT_ERROR);
    test("goog.setCssNameMapping(null);", "",
        EXPECTED_OBJECTLIT_ERROR);
    test("goog.setCssNameMapping(undefined);", "",
        EXPECTED_OBJECTLIT_ERROR);

    
    test("var BAR = 'bar'; goog.setCssNameMapping({foo:BAR});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
    test("goog.setCssNameMapping({foo:6});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
    test("goog.setCssNameMapping({foo:false});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
    test("goog.setCssNameMapping({foo:null});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
    test("goog.setCssNameMapping({foo:undefined});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSetCssNameMappingValidity
  public void testSetCssNameMappingValidity() {
    
    test("goog.setCssNameMapping({'a': 'b', 'a-a': 'c'})", "", null,
        INVALID_CSS_RENAMING_MAP);

    
    test("goog.setCssNameMapping({'a': 'b', 'a-a': 'c'}, 'BY_WHOLE')", "", null,
        INVALID_CSS_RENAMING_MAP);

    
    test("goog.setCssNameMapping({foo:'bar'}, 'UNKNOWN');", "",
        INVALID_STYLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testBadCrossModuleRequire
  public void testBadCrossModuleRequire() {
    test(
        createModuleStar(
            "",
            "goog.provide('goog.ui');",
            "goog.require('goog.ui');"),
        new String[] {
          "",
          "goog.ui = {};",
          ""
        },
        null,
        XMODULE_REQUIRE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testGoodCrossModuleRequire1
  public void testGoodCrossModuleRequire1() {
    test(
        createModuleStar(
            "goog.provide('goog.ui');",
            "",
            "goog.require('goog.ui');"),
        new String[] {
            "goog.ui = {};",
            "",
            "",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testGoodCrossModuleRequire2
  public void testGoodCrossModuleRequire2() {
    test(
        createModuleStar(
            "",
            "",
            "goog.provide('goog.ui'); goog.require('goog.ui');"),
        new String[] {
            "",
            "",
            "goog.ui = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSimpleAdditionalProvide
  public void testSimpleAdditionalProvide() {
    additionalCode = "goog.provide('b.B'); b.B = {};";
    test("goog.provide('a.A'); a.A = {};",
         "var b={};b.B={};var a={};a.A={};");
  }
