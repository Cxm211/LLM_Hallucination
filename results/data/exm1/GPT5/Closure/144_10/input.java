// buggy code
  FunctionTypeBuilder inferReturnType(@Nullable JSDocInfo info) {
    returnType = info != null && info.hasReturnType() ?
        info.getReturnType().evaluate(scope, typeRegistry) :
        typeRegistry.getNativeType(UNKNOWN_TYPE);
    if (templateTypeName != null &&
        returnType.restrictByNotNullOrUndefined().isTemplateType()) {
      reportError(TEMPLATE_TYPE_EXPECTED, fnName);
    }
    return this;
  }

  FunctionType buildAndRegister() {
    if (returnType == null) {
      returnType = typeRegistry.getNativeType(UNKNOWN_TYPE);
    }

    if (parametersNode == null) {
      throw new IllegalStateException(
          "All Function types must have params and a return type");
    }

    FunctionType fnType;
    if (isConstructor) {
      fnType = getOrCreateConstructor();
    } else if (isInterface) {
      fnType = typeRegistry.createInterfaceType(fnName, sourceNode);
      if (scope.isGlobal() && !fnName.isEmpty()) {
        typeRegistry.declareType(fnName, fnType.getInstanceType());
      }
      maybeSetBaseType(fnType);
    } else {
      fnType = new FunctionBuilder(typeRegistry)
          .withName(fnName)
          .withSourceNode(sourceNode)
          .withParamsNode(parametersNode)
          .withReturnType(returnType)
          .withTypeOfThis(thisType)
          .withTemplateName(templateTypeName)
          .build();
      maybeSetBaseType(fnType);
    }

    if (implementedInterfaces != null) {
      fnType.setImplementedInterfaces(implementedInterfaces);
    }

    typeRegistry.clearTemplateTypeName();

    return fnType;
  }

    private FunctionType getFunctionType(String name,
        Node rValue, JSDocInfo info, @Nullable Node lvalueNode) {
      FunctionType functionType = null;

      // Handle function aliases.
      if (rValue != null && rValue.isQualifiedName()) {
        Var var = scope.getVar(rValue.getQualifiedName());
        if (var != null && var.getType() instanceof FunctionType) {
          functionType = (FunctionType) var.getType();
          if (functionType != null && functionType.isConstructor()) {
            typeRegistry.declareType(name, functionType.getInstanceType());
          }
        }
        return functionType;
      }

      Node owner = null;
      if (lvalueNode != null) {
        owner = getPrototypePropertyOwner(lvalueNode);
      }

      Node errorRoot = rValue == null ? lvalueNode : rValue;
      boolean isFnLiteral =
          rValue != null && rValue.getType() == Token.FUNCTION;
      Node fnRoot = isFnLiteral ? rValue : null;
      Node parametersNode = isFnLiteral ?
          rValue.getFirstChild().getNext() : null;

      if (functionType == null && info != null && info.hasType()) {
        JSType type = info.getType().evaluate(scope, typeRegistry);

        // Known to be not null since we have the FUNCTION token there.
        type = type.restrictByNotNullOrUndefined();
        if (type.isFunctionType()) {
          functionType = (FunctionType) type;
          functionType.setJSDocInfo(info);
        }
      }

      if (functionType == null) {
        if (info == null ||
            !FunctionTypeBuilder.isFunctionTypeDeclaration(info)) {
          // We don't really have any type information in the annotation.
          // Before we give up on this function, look at the object we're
          // assigning it to. For example, if the function looks like this:
          // SubFoo.prototype.bar = function() { ... };
          // We can use type information on Foo.prototype.bar and apply it
          // to this function.
          if (lvalueNode != null && lvalueNode.getType() == Token.GETPROP &&
              lvalueNode.isQualifiedName()) {
            Var var = scope.getVar(
                lvalueNode.getFirstChild().getQualifiedName());
            if (var != null) {
              ObjectType ownerType = ObjectType.cast(var.getType());
              FunctionType propType = null;
              if (ownerType != null) {
                String propName = lvalueNode.getLastChild().getString();
                propType = findOverriddenFunction(ownerType, propName);
              }

              if (propType != null) {
                functionType =
                    new FunctionTypeBuilder(
                        name, compiler, errorRoot, sourceName, scope)
                    .setSourceNode(fnRoot)
                    .inferFromOverriddenFunction(propType, parametersNode)
                    .inferThisType(info, owner)
                    .buildAndRegister();
              }
            }
          }
        }
      } // end if (functionType == null)

      if (functionType == null) {
        functionType =
            new FunctionTypeBuilder(name, compiler, errorRoot, sourceName,
                scope)
            .setSourceNode(fnRoot)
            .inferTemplateTypeName(info)
            .inferReturnType(info)
            .inferInheritance(info)
            .inferThisType(info, owner)
            .inferParameterTypes(parametersNode, info)
            .buildAndRegister();
      }

      // assigning the function type to the function node
      if (rValue != null) {
        setDeferredType(rValue, functionType);
      }

      // all done
      return functionType;
    }

  public FunctionBuilder withReturnType(JSType returnType) {
    this.returnType = returnType;
    return this;
  }

  FunctionType cloneWithNewReturnType(JSType newReturnType, boolean inferred) {
    return new FunctionType(
        registry, null, null,
        new ArrowType(
            registry, call.parameters, newReturnType, inferred),
        typeOfThis, null, false, false);
  }

// relevant test
// com.google.javascript.jscomp.NodeUtilTest::testReferencesThis
  public void testReferencesThis() {
    assertTrue(NodeUtil.referencesThis(
        parse("this")));
    assertTrue(NodeUtil.referencesThis(
        parse("function foo(){}(this)")));
    assertTrue(NodeUtil.referencesThis(
        parse("b?this:null")));

    assertFalse(NodeUtil.referencesThis(
        parse("a")));
    assertFalse(NodeUtil.referencesThis(
        parse("function foo(){}")));
    assertFalse(NodeUtil.referencesThis(
        parse("(b?foo():null)")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetNodeTypeReferenceCount
  public void testGetNodeTypeReferenceCount() {
    assertEquals(0, NodeUtil.getNodeTypeReferenceCount(
        parse("function foo(){}"), Token.THIS, 
            Predicates.<Node>alwaysTrue()));
    assertEquals(1, NodeUtil.getNodeTypeReferenceCount(
        parse("this"), Token.THIS, 
            Predicates.<Node>alwaysTrue()));
    assertEquals(2, NodeUtil.getNodeTypeReferenceCount(
        parse("this;function foo(){}(this)"), Token.THIS,
            Predicates.<Node>alwaysTrue()));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsNameReferenceCount
  public void testIsNameReferenceCount() {
    assertTrue(NodeUtil.isNameReferenced(
        parse("function foo(){}"), "foo"));
    assertTrue(NodeUtil.isNameReferenced(
        parse("var foo = function(){}"), "foo"));
    assertFalse(NodeUtil.isNameReferenced(
        parse("function foo(){}"), "undefined"));
    assertTrue(NodeUtil.isNameReferenced(
        parse("undefined"), "undefined"));
    assertTrue(NodeUtil.isNameReferenced(
        parse("undefined;function foo(){}(undefined)"), "undefined"));

    assertTrue(NodeUtil.isNameReferenced(
        parse("goo.foo"), "goo"));
    assertFalse(NodeUtil.isNameReferenced(
        parse("goo.foo"), "foo"));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetNameReferenceCount
  public void testGetNameReferenceCount() {
    assertEquals(0, NodeUtil.getNameReferenceCount(
        parse("function foo(){}"), "undefined"));
    assertEquals(1, NodeUtil.getNameReferenceCount(
        parse("undefined"), "undefined"));
    assertEquals(2, NodeUtil.getNameReferenceCount(
        parse("undefined;function foo(){}(undefined)"), "undefined"));

    assertEquals(1, NodeUtil.getNameReferenceCount(
        parse("goo.foo"), "goo"));
    assertEquals(0, NodeUtil.getNameReferenceCount(
        parse("goo.foo"), "foo"));
    assertEquals(1, NodeUtil.getNameReferenceCount(
        parse("function foo(){}"), "foo"));
    assertEquals(1, NodeUtil.getNameReferenceCount(
        parse("var foo = function(){}"), "foo"));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetVarsDeclaredInBranch
  public void testGetVarsDeclaredInBranch() {
    Compiler compiler = new Compiler();

    assertNodeNames(Sets.newHashSet("foo"),
        NodeUtil.getVarsDeclaredInBranch(
            parse("var foo;")));
    assertNodeNames(Sets.newHashSet("foo","goo"),
        NodeUtil.getVarsDeclaredInBranch(
            parse("var foo,goo;")));
    assertNodeNames(Sets.<String>newHashSet(),
        NodeUtil.getVarsDeclaredInBranch(
            parse("foo();")));
    assertNodeNames(Sets.<String>newHashSet(),
        NodeUtil.getVarsDeclaredInBranch(
            parse("function(){var foo;}")));
    assertNodeNames(Sets.newHashSet("goo"),
        NodeUtil.getVarsDeclaredInBranch(
            parse("var goo;function(){var foo;}")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsControlStructureCodeBlock
  public void testIsControlStructureCodeBlock() {
    Compiler compiler = new Compiler();

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
    Compiler compiler = new Compiler();

    Node root = parse("(function foo() {})");
    Node StatementNode = root.getFirstChild();
    assertTrue(NodeUtil.isExpressionNode(StatementNode));
    Node functionNode = StatementNode.getFirstChild();
    assertTrue(NodeUtil.isFunction(functionNode));
    assertTrue(NodeUtil.isFunctionExpression(functionNode));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsFunctionExpression2
  public void testIsFunctionExpression2() {
    Compiler compiler = new Compiler();

    Node root = parse("function foo() {}");
    Node functionNode = root.getFirstChild();
    assertTrue(NodeUtil.isFunction(functionNode));
    assertFalse(NodeUtil.isFunctionExpression(functionNode));
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveTryChild
  public void testRemoveTryChild() {
    Compiler compiler = new Compiler();

    Node root = parse("try {foo()} catch(e) {} finally {}");

    
    Node actual = root.cloneTree();

    Node tryNode = actual.getFirstChild();
    Node tryBlock = tryNode.getFirstChild();
    Node catchBlocks = tryNode.getFirstChild().getNext();
    Node finallyBlock = tryNode.getLastChild();

    NodeUtil.removeChild(tryNode, finallyBlock);
    String expected = "try {foo()} catch(e) {}";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }

    
    actual = root.cloneTree();

    tryNode = actual.getFirstChild();
    tryBlock = tryNode.getFirstChild();
    catchBlocks = tryNode.getFirstChild().getNext();
    finallyBlock = tryNode.getLastChild();

    NodeUtil.removeChild(tryNode, tryBlock);
    expected = "try {} catch(e) {} finally {}";
    difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }

    
    actual = root.cloneTree();

    tryNode = actual.getFirstChild();
    tryBlock = tryNode.getFirstChild();
    catchBlocks = tryNode.getFirstChild().getNext();
    Node catchBlock = catchBlocks.getFirstChild();
    finallyBlock = tryNode.getLastChild();

    NodeUtil.removeChild(catchBlocks, catchBlock);
    expected = "try {foo()} finally {}";
    difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }

  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveVarChild
  public void testRemoveVarChild() {
    Compiler compiler = new Compiler();

    
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
    Compiler compiler = new Compiler();

    
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
    Compiler compiler = new Compiler();

    
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
    Compiler compiler = new Compiler();

    
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
    Compiler compiler = new Compiler();

    
    Node actual = parse("foo:{a();}");

    Node parentLabel = actual.getFirstChild();
    Node childBlock = parentLabel.getLastChild();

    assertFalse(NodeUtil.tryMergeBlock(childBlock));
  }

// com.google.javascript.jscomp.NodeUtilTest::testMergeBlock3
  public void testMergeBlock3() {
    Compiler compiler = new Compiler();

    
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
    parent.putProp(Node.SOURCENAME_PROP, "foo");
    assertEquals("foo", NodeUtil.getSourceName(n));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsLabelName
  public void testIsLabelName() {
    Compiler compiler = new Compiler();

    
    String code = "a:while(1) {a; continue a; break a; break;}";
    Node actual = parse(code);

    Node labelNode = actual.getFirstChild();
    assertTrue(labelNode.getType() == Token.LABEL);
    assertTrue(NodeUtil.isLabelName(labelNode.getFirstChild()));
    assertFalse(NodeUtil.isLabelName(labelNode.getLastChild()));

    Node whileNode = labelNode.getLastChild();
    assertTrue(whileNode.getType() == Token.WHILE);
    Node whileBlock = whileNode.getLastChild();
    assertTrue(whileBlock.getType() == Token.BLOCK);
    assertFalse(NodeUtil.isLabelName(whileBlock));

    Node firstStatement = whileBlock.getFirstChild();
    assertTrue(firstStatement.getType() == Token.EXPR_RESULT);
    Node variableReference = firstStatement.getFirstChild();
    assertTrue(variableReference.getType() == Token.NAME);
    assertFalse(NodeUtil.isLabelName(variableReference));

    Node continueStatement = firstStatement.getNext();
    assertTrue(continueStatement.getType() == Token.CONTINUE);
    assertTrue(NodeUtil.isLabelName(continueStatement.getFirstChild()));

    Node firstBreak = continueStatement.getNext();
    assertTrue(firstBreak.getType() == Token.BREAK);
    assertTrue(NodeUtil.isLabelName(firstBreak.getFirstChild()));

    Node secondBreak = firstBreak.getNext();
    assertTrue(secondBreak.getType() == Token.BREAK);
    assertFalse(secondBreak.hasChildren());
    assertFalse(NodeUtil.isLabelName(secondBreak.getFirstChild()));
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

    
    testSame("\nvar window;");

    
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

// com.google.javascript.jscomp.NormalizeTest::testRenamingConstantProperties
  public void testRenamingConstantProperties() {
    
    
    
    new WithCollapse().testConstantProperties();
  }

// com.google.javascript.jscomp.ObjectPropertyStringPostprocessTest::testFooDotBar
  public void testFooDotBar() {
    testPass("goog.global, foo.bar", "foo, 'bar'");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPostprocessTest::testFooGetElemBar
  public void testFooGetElemBar() {
    testPass("goog.global, foo[bar]", "foo, bar");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPostprocessTest::testFooBar
  public void testFooBar() {
    testPass("goog.global, foo$bar", "goog.global, 'foo$bar'");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testDeclaration
  public void testDeclaration() {
    test("goog.testing.ObjectPropertyString = function() {}",
         "JSCompiler_ObjectPropertyString = function() {}");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testFooBar
  public void testFooBar() {
    test("new goog.testing.ObjectPropertyString(foo, 'bar')",
         "new JSCompiler_ObjectPropertyString(goog.global, foo.bar)");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testFooPrototypeBar
  public void testFooPrototypeBar() {
    test("new goog.testing.ObjectPropertyString(foo.prototype, 'bar')",
         "new JSCompiler_ObjectPropertyString(goog.global, " +
         "foo.prototype.bar)");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testInvalidNumArgumentsError
  public void testInvalidNumArgumentsError() {
    testSame(new String[] {"new goog.testing.ObjectPropertyString()"},
        ObjectPropertyStringPreprocess.INVALID_NUM_ARGUMENTS_ERROR);
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testQualifedNameExpectedError
  public void testQualifedNameExpectedError() {
    testSame(
        new String[] {
          "new goog.testing.ObjectPropertyString(foo[a], 'bar')"
        },
        ObjectPropertyStringPreprocess.QUALIFIED_NAME_EXPECTED_ERROR);
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testStringLiteralExpectedError
  public void testStringLiteralExpectedError() {
    testSame(new String[] {"new goog.testing.ObjectPropertyString(foo, bar)"},
        ObjectPropertyStringPreprocess.STRING_LITERAL_EXPECTED_ERROR);
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testSimple
  public void testSimple() {
    test("function foo()   { alert(arguments[0]); }",
         "function foo(p0) { alert(p0); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testNoVarArgs
  public void testNoVarArgs() {
    testSame("function(a,b,c) { alert(a + b + c) }");

    test("function(a,b,c) { alert(arguments[0]) }",
         "function(a,b,c) { alert(a) }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testMissingVarArgs
  public void testMissingVarArgs() {
    testSame("function() { alert(arguments[x]) }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testArgumentRefOnNamedParameter
  public void testArgumentRefOnNamedParameter() {
    test("function(a,b) { alert(arguments[0]) }",
         "function(a,b) { alert(a) }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testTwoVarArgs
  public void testTwoVarArgs() {
    test("function foo(a) { alert(arguments[1] + arguments[2]); }",
         "function foo(a, p0, p1) { alert(p0 + p1); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testTwoFourArgsTwoUsed
  public void testTwoFourArgsTwoUsed() {
    test("function foo() { alert(arguments[0] + arguments[3]); }",
         "function foo(p0, p1, p2, p3) { alert(p0 + p3); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testOneRequired
  public void testOneRequired() {
    test("function foo(req0, var_args) { alert(req0 + arguments[1]); }",
         "function foo(req0, var_args) { alert(req0 + var_args); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testTwoRequiredSixthVarArgReferenced
  public void testTwoRequiredSixthVarArgReferenced() {
    test("function foo(r0, r1, var_args) {alert(r0 + r1 + arguments[5]);}",
         "function foo(r0, r1, var_args, p0, p1, p2) { alert(r0 + r1 + p2); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testTwoRequiredOneOptionalFifthVarArgReferenced
  public void testTwoRequiredOneOptionalFifthVarArgReferenced() {
    test("function foo(r0, r1, opt_1)"
       + "  {alert(r0 + r1 + opt_1 + arguments[4]);}",
         "function foo(r0, r1, opt_1, p0, p1)"
       + "  {alert(r0 + r1 + opt_1 + p1); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testTwoRequiredTwoOptionalSixthVarArgReferenced
  public void testTwoRequiredTwoOptionalSixthVarArgReferenced() {
    test("function foo(r0, r1, opt_1, opt_2)"
       + "  {alert(r0 + r1 + opt_1 + opt_2 + arguments[5]);}",
         "function foo(r0, r1, opt_1, opt_2, p0, p1)"
       + "  {alert(r0 + r1 + opt_1 + opt_2 + p1); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testInnerFunctions
  public void testInnerFunctions() {
    test("function f() { function b(  ) { arguments[0]  }}",
         "function f() { function b(p0) {            p0 }}");

    test("function f(  ) { function b() { }  arguments[0] }",
         "function f(p0) { function b() { }            p0 }");

    test("function f( )  { arguments[0]; function b(  ) { arguments[0] }}",
         "function f(p1) {           p1; function b(p0) {           p0 }}");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testInnerFunctionsWithNamedArgumentInInnerFunction
  public void testInnerFunctionsWithNamedArgumentInInnerFunction() {
    test("function f() { function b(x   ) { arguments[1] }}",
         "function f() { function b(x,p0) {           p0 }}");

    test("function f(  ) { function b(x) { }  arguments[0] }",
         "function f(p0) { function b(x) { }            p0 }");

    test("function f( )  { arguments[0]; function b(x   ) { arguments[1] }}",
         "function f(p1) {           p1; function b(x,p0) {           p0 }}");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testInnerFunctionsWithNamedArgumentInOutterFunction
  public void testInnerFunctionsWithNamedArgumentInOutterFunction() {
    test("function f(x) { function b(  ) { arguments[0] }}",
         "function f(x) { function b(p0) {           p0 }}");

    test("function f(x   ) { function b() { }  arguments[1] }",
         "function f(x,p0) { function b() { }            p0 }");

    test("function f(x   ) { arguments[1]; function b(  ) { arguments[0] }}",
         "function f(x,p1) {           p1; function b(p0) {           p0 }}");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testInnerFunctionsWithNamedArgumentInInnerAndOutterFunction
  public void testInnerFunctionsWithNamedArgumentInInnerAndOutterFunction() {
    test("function f(x) { function b(x   ) { arguments[1] }}",
         "function f(x) { function b(x,p0) {           p0 }}");

    test("function f(x   ) { function b(x) { }  arguments[1] }",
         "function f(x,p0) { function b(x) { }            p0 }");

    test("function f(x   ) { arguments[1]; function b(x   ) { arguments[1] }}",
         "function f(x,p1) {           p1; function b(x,p0) {           p0 }}");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testInnerFunctionsAfterArguments
  public void testInnerFunctionsAfterArguments() {
    
    
    test("function f(  ) { arguments[0]; function b() { function c() { }} }",
         "function f(p0) {           p0; function b() { function c() { }} }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testNoOptimizationWhenGetProp
  public void testNoOptimizationWhenGetProp() {
    testSame("function f() { arguments[0]; arguments.size }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testNoOptimizationWhenIndexIsNotNumberConstant
  public void testNoOptimizationWhenIndexIsNotNumberConstant() {
    testSame("function f() { arguments[0]; arguments['callee'].length}");
    testSame("function f() { arguments[0]; arguments.callee.length}");
    testSame(
        "function f() { arguments[0]; var x = 'callee'; arguments[x].length}");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testNoOptimizationWhenArgumentIsUsedAsFunctionCall
  public void testNoOptimizationWhenArgumentIsUsedAsFunctionCall() {
    testSame("function f() {arguments[0]()}");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoRemoval
  public void testNoRemoval() {
    testSame("function foo(p1) { } foo(1); foo(2)");
    testSame("function foo(p1) { } foo(1,2); foo(3,4)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNotAFunction
  public void testNotAFunction() {
    testSame("var x = 1; x; x = 2");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalNamedFunction
  public void testRemoveOneOptionalNamedFunction() {
    test("function foo(p1) { } foo()", "function foo() {var p1} foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalVarAssignment
  public void testRemoveOneOptionalVarAssignment() {
    test("var foo = function (p1) { }; foo()",
        "var foo = function () {var p1}; foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalExpressionAssign
  public void testRemoveOneOptionalExpressionAssign() {
    test("var foo; foo = function (p1) { }; foo()",
        "var foo; foo = function () {var p1}; foo()");
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

    String expected = "var goog = {};" +
        "goog.foo = function (p1) { var p2 };" +
        "goog.foo = function (q1) { var q2 };" +
        "goog.foo = function (r1) { var r2 };" +
        "goog.foo(1); goog.foo(2); goog.foo()";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveTwoOptionalMultiplePossibleDefinition
  public void testRemoveTwoOptionalMultiplePossibleDefinition() {
    String src = "var goog = {};" +
        "goog.foo = function (p1, p2, p3, p4) { };" +
        "goog.foo = function (q1, q2, q3, q4) { };" +
        "goog.foo = function (r1, r2, r3, r4) { };" +
        "goog.foo(1,0); goog.foo(2,1); goog.foo()";

    String expected = "var goog = {};" +
        "goog.foo = function(p1, p2) { var p4; var p3};" +
        "goog.foo = function(q1, q2) { var q4; var q3};" +
        "goog.foo = function(r1, r2) { var r4; var r3};" +
        "goog.foo(1,0); goog.foo(2,1); goog.foo()";
    test(src, expected);
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

// com.google.javascript.jscomp.OptimizeParametersTest::testUnknown
  public void testUnknown() {
    String src = "var goog1 = {};" +
        "goog1.foo = function () { };" +
        "var goog2 = {};" +
        "goog2.foo = function (p1) { };" +
        "var x = getGoog();" +
        "x.foo()";

    String expected = "var goog1 = {};" +
        "goog1.foo = function () { };" +
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
    String src = "function foo(bar) {};" +
        "baz = function(a) {};" +
        "baz(1);" +
        "foo(baz);"; 
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

// com.google.javascript.jscomp.ParallelCompilerPassTest::testNoFunction
  public void testNoFunction() {
    replace("\"foo\"");
    replace("var foo");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testOneFunction
  public void testOneFunction() {
    replace("\"foo\";function foo(){\"foo\"}");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testTwoFunctions
  public void testTwoFunctions() {
    replace("\"foo\";function f1(){\"foo\"}function f2(){\"foo\"}");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testInnerFunctions
  public void testInnerFunctions() {
    replace("\"foo\";function f1(){\"foo\";function f2(){\"foo\"}}");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testManyFunctions
  public void testManyFunctions() {
    StringBuffer sb = new StringBuffer("\"foo\";");
    for (int i = 0; i < 20; i++) {
      sb.append("function f");
      sb.append(i);
      sb.append("(){\"foo\"}");
    }
    replace(sb.toString());
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testEmptyPass
  public void testEmptyPass() {
    currentPeepholePasses = ImmutableSet.<AbstractPeepholeOptimization>of();

    testSame("var x; var y;");
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationOrder
  public void testOptimizationOrder() {
    

    final List<String> visitationLog = Lists.newArrayList();

    AbstractPeepholeOptimization note1Applied = 
        new AbstractPeepholeOptimization() {
      @Override
      public Node optimizeSubtree(Node node) {
        if (node.getType() == Token.NAME) {
          visitationLog.add(node.getString() + "1");
        }

        return node;
      }
    };

    AbstractPeepholeOptimization note2Applied = 
        new AbstractPeepholeOptimization() {
      @Override
      public Node optimizeSubtree(Node node) {
        if (node.getType() == Token.NAME) {
          visitationLog.add(node.getString() + "2");
        }

        return node;
      }
    };

    currentPeepholePasses =
      ImmutableSet.<AbstractPeepholeOptimization>of(note1Applied, note2Applied);
    
    test("var x; var y", "var x; var y");

    

    assertEquals(4, visitationLog.size());
    assertEquals("x1", visitationLog.get(0));
    assertEquals("x2", visitationLog.get(1));
    assertEquals("y1", visitationLog.get(2));
    assertEquals("y2", visitationLog.get(3));
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationRemovingSubtreeChild
  public void testOptimizationRemovingSubtreeChild() {
    currentPeepholePasses = ImmutableSet.<AbstractPeepholeOptimization>of(new 
          RemoveNodesNamedXUnderVarOptimization());
    
    test("var x,y;", "var y;");
    test("var y,x;", "var y;");
    test("var x,y,x;", "var y;");
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationRemovingSubtree
  public void testOptimizationRemovingSubtree() {
    currentPeepholePasses = ImmutableSet.<AbstractPeepholeOptimization>of(new 
          RemoveNodesNamedXOptimization());
    
    test("var x,y;", "var y;");
    test("var y,x;", "var y;");
    test("var x,y,x;", "var y;");
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationRemovingSubtreeParent
  public void testOptimizationRemovingSubtreeParent() {
    currentPeepholePasses = ImmutableSet.<AbstractPeepholeOptimization>of(new 
          RemoveParentVarsForNodesNamedX());
  
    test("var x; var y", "var y");
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationsRemoveParentAfterRemoveChild
  public void testOptimizationsRemoveParentAfterRemoveChild() {
    currentPeepholePasses = ImmutableSet.<AbstractPeepholeOptimization>of(
          new RemoveNodesNamedXOptimization(),
          new RemoveParentVarsForNodesNamedX());
    
    test("var x,y; var z;", "var y; var z;");
  }

// com.google.javascript.jscomp.PeepholeOptimizationsPassTest::testOptimizationReplacingNode
  public void testOptimizationReplacingNode() {
    currentPeepholePasses = ImmutableSet.<AbstractPeepholeOptimization>of(
          new RenameYToX(),
          new RemoveParentVarsForNodesNamedX());
    
    test("var y; var z;", "var z;");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testOneRun
  public void testOneRun() {
    addOneTimePass("x");
    assertPasses("x");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testTwoRuns
  public void testTwoRuns() {
    addOneTimePass("x");
    optimizer.process(null, null);
    try {
      optimizer.process(null, null);
      fail();
    } catch (IllegalStateException e) {
      assertEquals(
          "One-time passes cannot be run multiple times: x", e.getMessage());
    }
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testLoop1
  public void testLoop1() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 0);
    assertPasses("x");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testLoop2
  public void testLoop2() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 3);
    assertPasses("x", "x", "x", "x");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testLoop3
  public void testLoop3() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 3);
    addLoopedPass(loop, "y", 1);
    assertPasses("x", "y", "x", "y", "x", "y", "x", "y");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testNotInfiniteLoop
  public void testNotInfiniteLoop() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", PhaseOptimizer.MAX_LOOPS);
    optimizer.process(null, null);
    assertEquals("There should be no errors.", 0, compiler.getErrorCount());
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testInfiniteLoop
  public void testInfiniteLoop() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", PhaseOptimizer.MAX_LOOPS + 1);
    try {
      optimizer.process(null, null);
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains(PhaseOptimizer.OPTIMIZE_LOOP_ERROR));
    }
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testCombined
  public void testCombined() {
    addOneTimePass("a");
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 3);
    addLoopedPass(loop, "y", 1);
    addOneTimePass("z");
    assertPasses("a", "x", "y", "x", "y", "x", "y", "x", "y", "z");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testSanityCheck
  public void testSanityCheck() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 1);
    addOneTimePass("z");
    optimizer.setSanityCheck(
        createPassFactory("sanity", createPass("sanity", 0), false));
    assertPasses("x", "sanity", "x", "sanity", "z", "sanity");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testConsumption1
  public void testConsumption1() {
    optimizer.consume(
        Lists.newArrayList(
            createPassFactory("a", 0, true),
            createPassFactory("b", 1, false),
            createPassFactory("c", 2, false),
            createPassFactory("d", 1, false),
            createPassFactory("e", 1, true),
            createPassFactory("f", 0, true)));
    assertPasses("a", "b", "c", "d", "b", "c", "d", "b", "c", "d", "e", "f");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testConsumption2
  public void testConsumption2() {
    optimizer.consume(
        Lists.newArrayList(
            createPassFactory("a", 2, false),
            createPassFactory("b", 1, true),
            createPassFactory("c", 1, false)));
    assertPasses("a", "a", "a", "b", "c", "c");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testConsumption3
  public void testConsumption3() {
    optimizer.consume(
        Lists.newArrayList(
            createPassFactory("a", 2, true),
            createPassFactory("b", 0, false),
            createPassFactory("c", 0, false)));
    assertPasses("a", "b", "c");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testDuplicateLoop
  public void testDuplicateLoop() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 1);
    try {
      addLoopedPass(loop, "x", 1);
      fail("Expected exception");
    } catch (IllegalArgumentException e) {}
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testPassOrdering
  public void testPassOrdering() {
    Loop loop = optimizer.addFixedPointLoop();
    List<String> optimalOrder = Lists.newArrayList(
        PhaseOptimizer.OPTIMAL_ORDER);
    Random random = new Random();
    while (optimalOrder.size() > 0) {
      addLoopedPass(
          loop, optimalOrder.remove(random.nextInt(optimalOrder.size())), 0);
    }
    optimizer.process(null, null);
    assertEquals(PhaseOptimizer.OPTIMAL_ORDER, passesRun);
  }

// com.google.javascript.jscomp.PrepareAstTest::testJsDocNormalization
  public void testJsDocNormalization() throws Exception {
    Node root = parseExpectedJs(
        "var x = { a: function() {}," +
        "         c:  ('d')};");
    Node objlit = root.getFirstChild().getFirstChild().getFirstChild()
        .getFirstChild();
    assertEquals(Token.OBJECTLIT, objlit.getType());

    Node firstKey = objlit.getFirstChild();
    Node firstVal = firstKey.getNext();

    Node secondKey = firstVal.getNext();
    Node secondVal = secondKey.getNext();
    assertNotNull(firstKey.getJSDocInfo());
    assertNotNull(firstVal.getJSDocInfo());
    assertNull(secondKey.getJSDocInfo());
    assertNotNull(secondVal.getJSDocInfo());
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

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNewDateGoogNowSimplification
  public void testNewDateGoogNowSimplification() {
    test("var x = new Date(goog.now());", "var x = new Date();");
    testSame("var x = new Date(goog.now() + 1);");
    testSame("var x = new Date(goog.now(1));");
    testSame("var x = new Date(1, goog.now());");
    testSame("var x = new Date(1);");
    testSame("var x = new Date();");
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

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSetCssNameMappingNonStringValueReturnsError
  public void testSetCssNameMappingNonStringValueReturnsError() {
    
    test("var BAR = {foo:'bar'}; goog.setCssNameMapping(BAR);", "",
        INVALID_ARGUMENT_ERROR);
    test("goog.setCssNameMapping([]);", "",
        INVALID_ARGUMENT_ERROR);
    test("goog.setCssNameMapping(false);", "",
        INVALID_ARGUMENT_ERROR);
    test("goog.setCssNameMapping(null);", "",
        INVALID_ARGUMENT_ERROR);
    test("goog.setCssNameMapping(undefined);", "",
        INVALID_ARGUMENT_ERROR);

    
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

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSimpleAdditionalProvideAtEnd
  public void testSimpleAdditionalProvideAtEnd() {
    additionalEndCode = "goog.provide('b.B'); b.B = {};";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};var b={};b.B={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSimpleDottedAdditionalProvide
  public void testSimpleDottedAdditionalProvide() {
    additionalCode = "goog.provide('a.b.B'); a.b.B = {};";
    test("goog.provide('c.d.D'); c.d.D = {};",
         "var a={};a.b={};a.b.B={};var c={};c.d={};c.d.D={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testOverlappingAdditionalProvide
  public void testOverlappingAdditionalProvide() {
    additionalCode = "goog.provide('a.B'); a.B = {};";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.B={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testOverlappingAdditionalProvideAtEnd
  public void testOverlappingAdditionalProvideAtEnd() {
    additionalEndCode = "goog.provide('a.B'); a.B = {};";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};a.B={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testOverlappingDottedAdditionalProvide
  public void testOverlappingDottedAdditionalProvide() {
    additionalCode = "goog.provide('a.b.B'); a.b.B = {};";
    test("goog.provide('a.b.C'); a.b.C = {};",
         "var a={};a.b={};a.b.B={};a.b.C={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRequireOfAdditionalProvide
  public void testRequireOfAdditionalProvide() {
    additionalCode = "goog.provide('b.B'); b.B = {};";
    test("goog.require('b.B'); goog.provide('a.A'); a.A = {};",
         "var b={};b.B={};var a={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMissingRequireWithAdditionalProvide
  public void testMissingRequireWithAdditionalProvide() {
    additionalCode = "goog.provide('b.B'); b.B = {};";
    test("goog.require('b.C'); goog.provide('a.A'); a.A = {};",
         "var b={};b.B={};var a={};a.A={};",
         MISSING_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testLateRequire
  public void testLateRequire() {
    additionalEndCode = "goog.require('a.A');";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testReorderedProvides
  public void testReorderedProvides() {
    additionalCode = "a.B = {};";  
    addAdditionalNamespace = true;
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.B={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testReorderedProvides2
  public void testReorderedProvides2() {
    additionalEndCode = "a.B = {};";
    addAdditionalNamespace = true;
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};a.B={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder1
  public void testProvideOrder1() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    
    
    test("goog.provide('a.b');" +
         "goog.provide('a.b.c');" +
         "a.b.c;" +
         "a.b = function(x,y) {};",
         "var a = {};" +
         "a.b = {};" +
         "a.b.c = {};" +
         "a.b.c;" +
         "a.b = function(x,y) {};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder2
  public void testProvideOrder2() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    
    
    test("goog.provide('a.b');" +
         "goog.provide('a.b.c');" +
         "a.b = function(x,y) {};" +
         "a.b.c;",
         "var a = {};" +
         "a.b = {};" +
         "a.b.c = {};" +
         "a.b = function(x,y) {};" +
         "a.b.c;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder3a
  public void testProvideOrder3a() {
    test("goog.provide('a.b');" +
         "a.b = function(x,y) {};" +
         "goog.provide('a.b.c');" +
         "a.b.c;",
         "var a = {};" +
         "a.b = function(x,y) {};" +
         "a.b.c = {};" +
         "a.b.c;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder3b
  public void testProvideOrder3b() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    test("goog.provide('a.b');" +
         "a.b = function(x,y) {};" +
         "goog.provide('a.b.c');" +
         "a.b.c;",
         "var a = {};" +
         "a.b = function(x,y) {};" +
         "a.b.c = {};" +
         "a.b.c;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder4a
  public void testProvideOrder4a() {
    test("goog.provide('goog.a');" +
         "goog.provide('goog.a.b');" +
         "if (x) {" +
         "  goog.a.b = 1;" +
         "} else {" +
         "  goog.a.b = 2;" +
         "}",

         "goog.a={};" +
         "if(x)" +
         "  goog.a.b=1;" +
         "else" +
         "  goog.a.b=2;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder4b
  public void testProvideOrder4b() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    test("goog.provide('goog.a');" +
         "goog.provide('goog.a.b');" +
         "if (x) {" +
         "  goog.a.b = 1;" +
         "} else {" +
         "  goog.a.b = 2;" +
         "}",

         "goog.a={};" +
         "if(x)" +
         "  goog.a.b=1;" +
         "else" +
         "  goog.a.b=2;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidProvide
  public void testInvalidProvide() {
    test("goog.provide('a.class');", null, INVALID_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase1
  public void testInvalidBase1() {
    test("goog.base(this, 'method');", null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase2
  public void testInvalidBase2() {
    test("function Foo() {}" +
         "Foo.method = function() {" +
         "  goog.base(this, 'method');" +
         "};", null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase3
  public void testInvalidBase3() {
    test(String.format(METHOD_FORMAT, "goog.base();"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase4
  public void testInvalidBase4() {
    test(String.format(METHOD_FORMAT, "goog.base(this, 'bar');"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase5
  public void testInvalidBase5() {
    test(String.format(METHOD_FORMAT, "goog.base('foo', 'method');"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase6
  public void testInvalidBase6() {
    test(String.format(METHOD_FORMAT, "goog.base.call(null, this, 'method');"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase7
  public void testInvalidBase7() {
    test("function Foo() { goog.base(this); }",
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase8
  public void testInvalidBase8() {
    test("var Foo = function() { goog.base(this); }",
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase9
  public void testInvalidBase9() {
    test("var goog = {}; goog.Foo = function() { goog.base(this); }",
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase1
  public void testValidBase1() {
    test(String.format(METHOD_FORMAT, "goog.base(this, 'method');"),
         String.format(METHOD_FORMAT, "Foo.superClass_.method.call(this)"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase2
  public void testValidBase2() {
    test(String.format(METHOD_FORMAT, "goog.base(this, 'method', 1, 2);"),
         String.format(METHOD_FORMAT,
             "Foo.superClass_.method.call(this, 1, 2)"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase3
  public void testValidBase3() {
    test(String.format(METHOD_FORMAT, "return goog.base(this, 'method');"),
         String.format(METHOD_FORMAT,
             "return Foo.superClass_.method.call(this)"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase4
  public void testValidBase4() {
    test("function Foo() { goog.base(this, 1, 2); }" + FOO_INHERITS,
         "function Foo() { BaseFoo.call(this, 1, 2); } " + FOO_INHERITS);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase5
  public void testValidBase5() {
    test("var Foo = function() { goog.base(this, 1); };" + FOO_INHERITS,
         "var Foo = function() { BaseFoo.call(this, 1); }; " + FOO_INHERITS);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase6
  public void testValidBase6() {
    test("var goog = {}; goog.Foo = function() { goog.base(this); }; " +
         "goog.inherits(goog.Foo, goog.BaseFoo);",
         "var goog = {}; goog.Foo = function() { goog.BaseFoo.call(this); }; " +
         "goog.inherits(goog.Foo, goog.BaseFoo);");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitAndExplicitProvide
  public void testImplicitAndExplicitProvide() {
    test("var goog = {}; " +
         "goog.provide('goog.foo.bar'); goog.provide('goog.foo');",
         "var goog = {}; goog.foo = {}; goog.foo.bar = {};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitProvideInIndependentModules
  public void testImplicitProvideInIndependentModules() {
    test(
        createModuleStar(
            "",
            "goog.provide('apps.A');",
            "goog.provide('apps.B');"),
        new String[] {
            "var apps = {};",
            "apps.A = {};",
            "apps.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitProvideInIndependentModules2
  public void testImplicitProvideInIndependentModules2() {
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.A');",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {}; apps.foo = {};",
            "apps.foo.A = {};",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitProvideInIndependentModules3
  public void testImplicitProvideInIndependentModules3() {
    test(
        createModuleStar(
            "var goog = {};",
            "goog.provide('goog.foo.A');",
            "goog.provide('goog.foo.B');"),
        new String[] {
            "var goog = {}; goog.foo = {};",
            "goog.foo.A = {};",
            "goog.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules1
  public void testProvideInIndependentModules1() {
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo');",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {}; apps.foo = {};",
            "",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules2
  public void testProvideInIndependentModules2() {
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo'); apps.foo = {};",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {};",
            "apps.foo = {};",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRequireOfBaseGoog
  public void testRequireOfBaseGoog() {
    test("goog.require('goog');",
         "", MISSING_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine1
  public void testBasicDefine1() {
    test(" var DEF = true", "var DEF=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine2
  public void testBasicDefine2() {
    test(" var DEF = 'a'", "var DEF=\"a\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine3
  public void testBasicDefine3() {
    test(" var DEF = 0", "var DEF=0");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineBadType
  public void testDefineBadType() {
    test(" var DEF = {}",
        null, ProcessDefines.INVALID_DEFINE_TYPE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithBadValue
  public void testDefineWithBadValue() {
    test(" var DEF = new Boolean(true);", null,
        ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithDependentValue
  public void testDefineWithDependentValue() {
    test(" var BASE = false;\n" +
         " var DEF = !BASE;",
         "var BASE=false;var DEF=!BASE");
    test("var a = {};\n" +
         " a.BASE = false;\n" +
         " a.DEF = !a.BASE;",
         "var a={};a.BASE=false;a.DEF=!a.BASE");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithInvalidDependentValue
  public void testDefineWithInvalidDependentValue() {
    test("var BASE = false;\n" +
         " var DEF = !BASE;",
         null,
          ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverriding1
  public void testOverriding1() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    overrides.put("DEF_OVERRIDE_TO_FALSE", new Node(Token.FALSE));
    test(
        " var DEF_OVERRIDE_TO_TRUE = false;" +
        " var DEF_OVERRIDE_TO_FALSE = true",
        "var DEF_OVERRIDE_TO_TRUE=true;var DEF_OVERRIDE_TO_FALSE=false");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverriding2
  public void testOverriding2() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    String normalConst = "var DEF_OVERRIDE_TO_FALSE=true;";
    testWithPrefix(
        normalConst,
        " var DEF_OVERRIDE_TO_TRUE = false",
        "var DEF_OVERRIDE_TO_TRUE=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverriding3
  public void testOverriding3() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    test(
        " var DEF_OVERRIDE_TO_TRUE = true;",
        "var DEF_OVERRIDE_TO_TRUE=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testMisspelledOverride
  public void testMisspelledOverride() {
    overrides.put("DEF_BAD_OVERIDE", new Node(Token.TRUE));
    test(" var DEF_BAD_OVERRIDE = true",
        "var DEF_BAD_OVERRIDE=true", null,
        ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testCompiledIsKnownDefine
  public void testCompiledIsKnownDefine() {
    overrides.put("COMPILED", new Node(Token.TRUE));
    testSame("");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testSimpleReassign1
  public void testSimpleReassign1() {
    test(" var DEF = false; DEF = true;",
        "var DEF=true;true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testSimpleReassign2
  public void testSimpleReassign2() {
    test(" var DEF=false;DEF=true;DEF=3",
        "var DEF=3;true;3");

    Name def = namespace.getNameIndex().get("DEF");
    assertEquals(0, def.refs.size());
    assertEquals(1, def.globalSets);
    assertNotNull(def.declaration);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testSimpleReassign3
  public void testSimpleReassign3() {
    test(" var DEF = false;var x;x = DEF = true;",
        "var DEF=true;var x;x=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDuplicateVar
  public void testDuplicateVar() {
    test(" var DEF = false; var DEF = true;",
         null, VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testAssignBeforeDeclaration1
  public void testAssignBeforeDeclaration1() {
    test("DEF=false;var b=false,DEF=true,c=false",
         null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testAssignBeforeDeclaration2
  public void testAssignBeforeDeclaration2() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    test(
        "DEF_OVERRIDE_TO_TRUE = 3;" +
        " var DEF_OVERRIDE_TO_TRUE = false;",
        null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testEmptyDeclaration
  public void testEmptyDeclaration() {
    test(" var DEF;",
         null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterCall
  public void testReassignAfterCall() {
    test("var DEF=true;externMethod();DEF=false",
        null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterRef
  public void testReassignAfterRef() {
    test("var DEF=true;var x = DEF;DEF=false",
        null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignWithExpr
  public void testReassignWithExpr() {
    test("var DEF=true;var x;DEF=x=false",
        null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterNonGlobalRef
  public void testReassignAfterNonGlobalRef() {
    test(
        "var DEF=true;" +
        "var x=function(){var y=DEF}; DEF=false",
        "var DEF=false;var x=function(){var y=DEF};false");

    Name def = namespace.getNameIndex().get("DEF");
    assertEquals(1, def.refs.size());
    assertEquals(1, def.globalSets);
    assertNotNull(def.declaration);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterRefInConditional
  public void testReassignAfterRefInConditional() {
    test(
        "var DEF=true;" +
        "if (false) {var x=DEF} DEF=false;",
        null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testAssignInNonGlobalScope
  public void testAssignInNonGlobalScope() {
    test("var DEF=true;function foo() {DEF=false};",
        null, ProcessDefines.NON_GLOBAL_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDeclareInNonGlobalScope
  public void testDeclareInNonGlobalScope() {
    test("function foo() {var DEF=true;};",
        null, ProcessDefines.NON_GLOBAL_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineAssignmentInLoop
  public void testDefineAssignmentInLoop() {
    test("var DEF=true;var x=0;while (x) {DEF=false;}",
        null, ProcessDefines.NON_GLOBAL_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testWithNoDefines
  public void testWithNoDefines() {
    testSame("var DEF=true;var x={};x.foo={}");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine1
  public void testNamespacedDefine1() {
    test("var a = {};  a.B = false; a.B = true;",
         "var a = {}; a.B = true; true;");

    Name aDotB = namespace.getNameIndex().get("a.B");
    assertEquals(0, aDotB.refs.size());
    assertEquals(1, aDotB.globalSets);
    assertNotNull(aDotB.declaration);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine2
  public void testNamespacedDefine2() {
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {};  a.B = false;",
         "var a = {}; a.B = true;");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine3
  public void testNamespacedDefine3() {
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {};", "var a = {};", null,
         ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverrideAfterAlias
  public void testOverrideAfterAlias() {
    test("var x; var DEF=true; x=DEF; DEF=false;",
         null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns1
  public void testAnnotationInExterns1() throws Exception {
    checkMarkedCalls("externSef1()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns2
  public void testAnnotationInExterns2() throws Exception {
    checkMarkedCalls("externSef2()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns3
  public void testAnnotationInExterns3() throws Exception {
    checkMarkedCalls("externNsef1()", ImmutableList.of("externNsef1"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns4
  public void testAnnotationInExterns4() throws Exception {
    checkMarkedCalls("externNsef2()", ImmutableList.of("externNsef2"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns5
  public void testAnnotationInExterns5() throws Exception {
    checkMarkedCalls("externNsef3()", ImmutableList.of("externNsef3"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns1
  public void testNamespaceAnnotationInExterns1() throws Exception {
    checkMarkedCalls("externObj.sef1()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns2
  public void testNamespaceAnnotationInExterns2() throws Exception {
    checkMarkedCalls("externObj.nsef1()", ImmutableList.of("externObj.nsef1"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns3
  public void testNamespaceAnnotationInExterns3() throws Exception {
    checkMarkedCalls("externObj.nsef2()", ImmutableList.of("externObj.nsef2"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns4
  public void testNamespaceAnnotationInExterns4() throws Exception {
    checkMarkedCalls("externObj.partialFn()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns5
  public void testNamespaceAnnotationInExterns5() throws Exception {
    
    
    
    String templateSrc = "var o = {}; o.<fnName> = function(){}; o.<fnName>()";

    
    checkMarkedCalls(templateSrc.replaceAll("<fnName>", "notPartialFn"),
                     ImmutableList.of("o.notPartialFn"));

    checkMarkedCalls(templateSrc.replaceAll("<fnName>", "partialFn"),
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns6
  public void testNamespaceAnnotationInExterns6() throws Exception {
    checkMarkedCalls("externObj.partialSharedFn()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns1
  public void testConstructorAnnotationInExterns1() throws Exception {
    checkMarkedCalls("new externSefConstructor()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns2
  public void testConstructorAnnotationInExterns2() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.sefFnOfSefObj()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns3
  public void testConstructorAnnotationInExterns3() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.nsefFnOfSefObj()",
                     ImmutableList.of("a.nsefFnOfSefObj"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns4
  public void testConstructorAnnotationInExterns4() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.externShared()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns5
  public void testConstructorAnnotationInExterns5() throws Exception {
    checkMarkedCalls("new externNsefConstructor()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns6
  public void testConstructorAnnotationInExterns6() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.sefFnOfNsefObj()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns7
  public void testConstructorAnnotationInExterns7() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.nsefFnOfNsefObj()",
                     ImmutableList.of("externNsefConstructor",
                                      "a.nsefFnOfNsefObj"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns8
  public void testConstructorAnnotationInExterns8() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.externShared()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testSharedFunctionName1
  public void testSharedFunctionName1() throws Exception {
    checkMarkedCalls("var a; " +
                     "if (true) {" +
                     "  a = new externNsefConstructor()" +
                     "} else {" +
                     "  a = new externSefConstructor()" +
                     "}" +
                     "a.externShared()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testSharedFunctionName2
  public void testSharedFunctionName2() throws Exception {
    
    
    boolean broken = true;
    if (broken) {
      checkMarkedCalls("var a; " +
                       "if (true) {" +
                       "  a = new externNsefConstructor()" +
                       "} else {" +
                       "  a = new externNsefConstructor2()" +
                       "}" +
                       "a.externShared()",
                       ImmutableList.of("externNsefConstructor",
                                        "externNsefConstructor2"));
    } else {
      checkMarkedCalls("var a; " +
                       "if (true) {" +
                       "  a = new externNsefConstructor()" +
                       "} else {" +
                       "  a = new externNsefConstructor2()" +
                       "}" +
                       "a.externShared()",
                       ImmutableList.of("externNsefConstructor",
                                        "externNsefConstructor2",
                                        "a.externShared"));
    }
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNoSideEffectsSimple
  public void testNoSideEffectsSimple() throws Exception {
    String prefix = "function f(){";
    String suffix = "} f()";
    List<String> expected = ImmutableList.of("f");

    checkMarkedCalls(
        prefix + "" + suffix, expected);
    checkMarkedCalls(
        prefix + "return 1" + suffix, expected);
    checkMarkedCalls(
        prefix + "return 1 + 2" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = 1; return a" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = 1; a = 2; return a" + suffix, expected);
    checkMarkedCalls(
        prefix + "var a = 1; a = 2; return a + 1" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo" + suffix, expected);
    checkMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo + 1" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "return externObj" + suffix, expected);
    checkMarkedCalls(
        "function g(x) { x.foo = 3; }"  +
        prefix + "return externObj.foo" + suffix, expected);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testExternCalls
  public void testExternCalls() throws Exception {
    String prefix = "function f(){";
    String suffix = "} f()";

    checkMarkedCalls(prefix + "externNsef1()" + suffix,
                     ImmutableList.of("externNsef1", "f"));
    checkMarkedCalls(prefix + "externObj.nsef1()" + suffix,
                     ImmutableList.of("externObj.nsef1", "f"));

    checkMarkedCalls(prefix + "externSef1()" + suffix,
                     ImmutableList.<String>of());
    checkMarkedCalls(prefix + "externObj.sef1()" + suffix,
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testApply
  public void testApply() throws Exception {
    checkMarkedCalls("function f() {return 42}" +
                     "f.apply()",
                     ImmutableList.of("f.apply"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCall
  public void testCall() throws Exception {
    checkMarkedCalls("function f() {return 42}" +
                     "f.call()",
                     ImmutableList.<String>of("f.call"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference1
  public void testInference1() throws Exception {
    checkMarkedCalls("function f() {return g()}" +
                     "function g() {return 42}" +
                     "f()",
                     ImmutableList.of("g", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference2
  public void testInference2() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "function f() {g()}" +
                     "function g() {a=2}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference3
  public void testInference3() throws Exception {
    checkMarkedCalls("var f = function() {return g()};" +
                     "var g = function() {return 42};" +
                     "f()",
                     ImmutableList.of("g", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference4
  public void testInference4() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "var f = function() {g()};" +
                     "var g = function() {a=2};" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference5
  public void testInference5() throws Exception {
    checkMarkedCalls("var goog = {};" +
                     "goog.f = function() {return goog.g()};" +
                     "goog.g = function() {return 42};" +
                     "goog.f()",
                     ImmutableList.of("goog.g", "goog.f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference6
  public void testInference6() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "var goog = {};" +
                     "goog.f = function() {goog.g()};" +
                     "goog.g = function() {a=2};" +
                     "goog.f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators1
  public void testUnaryOperators1() throws Exception {
    checkMarkedCalls("function f() {var x = 1; x++}" +
                     "f()",
                     ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators2
  public void testUnaryOperators2() throws Exception {
    checkMarkedCalls("var x = 1;" +
                     "function f() {x++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators3
  public void testUnaryOperators3() throws Exception {
    checkMarkedCalls("function f() {var x = {foo : 0}; x.foo++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators4
  public void testUnaryOperators4() throws Exception {
    checkMarkedCalls("var x = {foo : 0};" +
                     "function f() {x.foo++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators5
  public void testUnaryOperators5() throws Exception {
    checkMarkedCalls("function f(x) {x.foo++}" +
                     "f({foo : 0})",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testDeleteOperator1
  public void testDeleteOperator1() throws Exception {
    checkMarkedCalls("var x = {};" +
                     "function f() {delete x}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testDeleteOperator2
  public void testDeleteOperator2() throws Exception {
    checkMarkedCalls("function f() {var x = {}; delete x}" +
                     "f()",
                     ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator1
  public void testOrOperator1() throws Exception {
    checkMarkedCalls("var f = externNsef1 || externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator2
  public void testOrOperator2() throws Exception {
    checkMarkedCalls("var f = function(){} || externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator3
  public void testOrOperator3() throws Exception {
    checkMarkedCalls("var f = externNsef2 || function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperators4
  public void testOrOperators4() throws Exception {
    checkMarkedCalls("var f = function(){} || function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator1
  public void testAndOperator1() throws Exception {
    checkMarkedCalls("var f = externNsef1 && externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator2
  public void testAndOperator2() throws Exception {
    checkMarkedCalls("var f = function(){} && externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator3
  public void testAndOperator3() throws Exception {
    checkMarkedCalls("var f = externNsef2 && function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperators4
  public void testAndOperators4() throws Exception {
    checkMarkedCalls("var f = function(){} && function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator1
  public void testHookOperator1() throws Exception {
    checkMarkedCalls("var f = true ? externNsef1 : externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator2
  public void testHookOperator2() throws Exception {
    checkMarkedCalls("var f = true ? function(){} : externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator3
  public void testHookOperator3() throws Exception {
    checkMarkedCalls("var f = true ? externNsef2 : function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperators4
  public void testHookOperators4() throws Exception {
    checkMarkedCalls("var f = true ? function(){} : function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testThrow1
  public void testThrow1() throws Exception {
    checkMarkedCalls("function f(){throw Error()};\n" +
                     "f()",
                     ImmutableList.<String>of("Error"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testThrow2
  public void testThrow2() throws Exception {
    checkMarkedCalls("function A(){throw Error()};\n" +
                     "function f(){return new A()}\n" +
                     "f()",
                     ImmutableList.<String>of("Error"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAssignmentOverride
  public void testAssignmentOverride() throws Exception {
    checkMarkedCalls("function A(){}\n" +
                     "A.prototype.foo = function(){};\n" +
                     "var a = new A;\n" +
                     "a.foo();\n",
                     ImmutableList.<String>of("A", "a.foo"));

    checkMarkedCalls("function A(){}\n" +
                     "A.prototype.foo = function(){};\n" +
                     "var x = 1\n" +
                     "function f(){x = 10}\n" +
                     "var a = new A;\n" +
                     "a.foo = f;\n" +
                     "a.foo();\n",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInheritance1
  public void testInheritance1() throws Exception {
    String source =
        CompilerTypeTestCase.CLOSURE_DEFS +
        "function I(){}\n" +
        "I.prototype.foo = function(){};\n" +
        "I.prototype.bar = function(){this.foo()};\n" +
        "function A(){};\n" +
        "goog.inherits(A, I)\n;" +
        "A.prototype.foo = function(){var data=24};\n" +
        "var i = new I();i.foo();i.bar();\n" +
        "var a = new A();a.foo();a.bar();";

    checkMarkedCalls(source,
                     ImmutableList.of("this.foo", "goog.inherits",
                                      "I", "i.foo", "i.bar",
                                      "A", "a.foo", "a.bar"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInheritance2
  public void testInheritance2() throws Exception {
    String source =
        CompilerTypeTestCase.CLOSURE_DEFS +
        "function I(){}\n" +
        "I.prototype.foo = function(){};\n" +
        "I.prototype.bar = function(){this.foo()};\n" +
        "function A(){};\n" +
        "goog.inherits(A, I)\n;" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "var i = new I();i.foo();i.bar();\n" +
        "var a = new A();a.foo();a.bar();";

    checkMarkedCalls(source, ImmutableList.of("goog.inherits", "I", "A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallBeforeDefinition
  public void testCallBeforeDefinition() throws Exception {
    checkMarkedCalls("f(); function f(){}",
                     ImmutableList.of("f"));

    checkMarkedCalls("var a = {}; a.f(); a.f = function (){}",
                     ImmutableList.of("a.f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis1
  public void testConstructorThatModifiesThis1() throws Exception {
    String source = "function A(){this.foo = 1}\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis2
  public void testConstructorThatModifiesThis2() throws Exception {
    String source = "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis3
  public void testConstructorThatModifiesThis3() throws Exception {

    
    String source = "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){this.bar()};\n" +
        "A.prototype.bar = function(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis4
  public void testConstructorThatModifiesThis4() throws Exception {

    
    String source = "function A(){foo.call(this)}\n" +
        "function foo(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesGlobal1
  public void testConstructorThatModifiesGlobal1() throws Exception {
    String source = "var b = 0;" +
        "function A(){b=1};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesGlobal2
  public void testConstructorThatModifiesGlobal2() throws Exception {
    String source = "var b = 0;" +
        "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){b=1};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionThatModifiesThis
  public void testCallFunctionThatModifiesThis() throws Exception {
    String source = "function A(){}\n" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "function f(){var a = new A; return a}\n" +
        "function g(){var a = new A; a.foo(); return a}\n" +
        "f(); g()";

    checkMarkedCalls(source, ImmutableList.<String>of("A", "A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrG
  public void testCallFunctionFOrG() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){ (f || g)() }\n" +
        "h()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f || g)", "h"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGViaHook
  public void testCallFunctionFOrGViaHook() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){ (false ? f : g)() }\n" +
        "h()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f : g)", "h"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionForGorH
  public void testCallFunctionForGorH() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){}\n" +
        "function i(){ (false ? f : (g || h))() }\n" +
        "i()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f : (g || h))", "i"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGWithSideEffects
  public void testCallFunctionFOrGWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function f(){x = 10}\n" +
        "function g(){}\n" +
        "function h(){ (f || g)() }\n" +
        "function i(){ (g || f)() }\n" +
        "function j(){ (f || f)() }\n" +
        "function k(){ (g || g)() }\n" +
        "h(); i(); j(); k()";

    checkMarkedCalls(source, ImmutableList.<String>of("(g || g)", "k"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGViaHookWithSideEffects
  public void testCallFunctionFOrGViaHookWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function f(){x = 10}\n" +
        "function g(){}\n" +
        "function h(){ (false ? f : g)() }\n" +
        "function i(){ (false ? g : f)() }\n" +
        "function j(){ (false ? f : f)() }\n" +
        "function k(){ (false ? g : g)() }\n" +
        "h(); i(); j(); k()";

    checkMarkedCalls(source, ImmutableList.<String>of("(g : g)", "k"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallRegExpWithSideEffects
  public void testCallRegExpWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function k(){(/a/).exec('')}\n" +
        "k()";

    regExpHaveSideEffects = true;
    checkMarkedCalls(source, ImmutableList.<String>of());
    regExpHaveSideEffects = false;
    checkMarkedCalls(source, ImmutableList.<String>of(
        "REGEXP STRING exec", "k"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction1
  public void testAnonymousFunction1() throws Exception {
    String source = "(function (){})();";

    checkMarkedCalls(source, ImmutableList.<String>of(
        "FUNCTION"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction2
  public void testAnonymousFunction2() throws Exception {
    String source = "(Error || function (){})();";

    checkMarkedCalls(source, ImmutableList.<String>of(
        "(Error || FUNCTION)"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction3
  public void testAnonymousFunction3() throws Exception {
    String source = "var a = (Error || function (){})();";

    checkMarkedCalls(source, ImmutableList.<String>of(
        "(Error || FUNCTION)"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnonymousFunction4
  public void testAnonymousFunction4() throws Exception {
    String source = "var a = (Error || function (){});" +
                    "a();";

    
    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation1
  public void testInvalidAnnotation1() throws Exception {
    test(" function foo() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation2
  public void testInvalidAnnotation2() throws Exception {
    test("var f =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation3
  public void testInvalidAnnotation3() throws Exception {
    test(" var f = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }
