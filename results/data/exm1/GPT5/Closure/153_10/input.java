// buggy code
  private void removeDuplicateDeclarations(Node externs, Node root) {
    Callback tickler = new ScopeTicklingCallback();
    ScopeCreator scopeCreator =  new SyntacticScopeCreator(
        compiler, new DuplicateDeclarationHandler());
    NodeTraversal t = new NodeTraversal(compiler, tickler, scopeCreator);
    t.traverseRoots(externs, root);
  }

    public void onRedeclaration(
        Scope s, String name, Node n, Node parent, Node gramps,
        Node nodeWithLineNumber) {
      Preconditions.checkState(n.getType() == Token.NAME);
      Var v = s.getVar(name);

        // We allow variables to be duplicate declared if one
        // declaration appears in source and the other in externs.
        // This deals with issues where a browser built-in is declared
        // in one browser but not in another.

      // If name is "arguments", Var maybe null.
      if (v != null && v.getParentNode().getType() == Token.CATCH) {
        // Redeclaration of a catch expression variable is hard to model
        // without support for "with" expressions.
        // The EcmaScript spec (section 12.14), declares that a catch
        // "catch (e) {}" is handled like "with ({'e': e}) {}" so that
        // "var e" would refer to the scope variable, but any following
        // reference would still refer to "e" of the catch expression.
        // Until we have support for this disallow it.
        // Currently the Scope object adds the catch expression to the
        // function scope, which is technically not true but a good
        // approximation for most uses.

        // TODO(johnlenz): Consider improving how scope handles catch
        // expression.

        // Use the name of the var before it was made unique.
        name = MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName(
            name);
        compiler.report(
            JSError.make(
                NodeUtil.getSourceName(nodeWithLineNumber), nodeWithLineNumber,
                CATCH_BLOCK_VAR_ERROR, name));
      } else if (v != null && parent.getType() == Token.FUNCTION) {
        if (v.getParentNode().getType() == Token.VAR) {
          s.undeclare(v);
          s.declare(name, n, n.getJSType(), v.input);
          replaceVarWithAssignment(v.getNameNode(), v.getParentNode(),
              v.getParentNode().getParent());
        }
      } else if (parent.getType() == Token.VAR) {
        Preconditions.checkState(parent.hasOneChild());

        replaceVarWithAssignment(n, parent, gramps);
      }
    }

  private void scanRoot(Node n, Scope parent) {
    if (n.getType() == Token.FUNCTION) {
      sourceName = (String) n.getProp(Node.SOURCENAME_PROP);

      final Node fnNameNode = n.getFirstChild();
      final Node args = fnNameNode.getNext();
      final Node body = args.getNext();

      // Bleed the function name into the scope, if it hasn't
      // been declared in the outer scope.
      String fnName = fnNameNode.getString();
      if (!fnName.isEmpty() && NodeUtil.isFunctionExpression(n)) {
        declareVar(fnName, fnNameNode, n, null, null, n);
      }

      // Args: Declare function variables
      Preconditions.checkState(args.getType() == Token.LP);
      for (Node a = args.getFirstChild(); a != null;
           a = a.getNext()) {
        Preconditions.checkState(a.getType() == Token.NAME);
        declareVar(a.getString(), a, args, n, null, n);
      }

      // Body
      scanVars(body, n);
    } else {
      // It's the global block
      Preconditions.checkState(scope.getParent() == null);
      scanVars(n, null);
    }
  }

  private void scanVars(Node n, Node parent) {
    switch (n.getType()) {
      case Token.VAR:
        // Declare all variables. e.g. var x = 1, y, z;
        for (Node child = n.getFirstChild();
             child != null;) {
          Node next = child.getNext();
          Preconditions.checkState(child.getType() == Token.NAME);
          String name = child.getString();
          declareVar(name, child, n, parent, null, n);
          child = next;
        }
        return;

      case Token.FUNCTION:
        if (NodeUtil.isFunctionExpression(n)) {
          return;
        }

        String fnName = n.getFirstChild().getString();
        if (fnName.isEmpty()) {
          // This is invalid, but allow it so the checks can catch it.
          return;
        }
        declareVar(fnName, n.getFirstChild(), n, parent, null, n);
        return;   // should not examine function's children

      case Token.CATCH:
        Preconditions.checkState(n.getChildCount() == 2);
        Preconditions.checkState(n.getFirstChild().getType() == Token.NAME);
        // the first child is the catch var and the third child
        // is the code block

        final Node var = n.getFirstChild();
        final Node block = var.getNext();

        declareVar(var.getString(), var, n, parent, null, n);
        scanVars(block, n);
        return;  // only one child to scan

      case Token.SCRIPT:
        sourceName = (String) n.getProp(Node.SOURCENAME_PROP);
        break;
    }

    // Variables can only occur in statement-level nodes, so
    // we only need to traverse children in a couple special cases.
    if (NodeUtil.isControlStructure(n) || NodeUtil.isStatementBlock(n)) {
      for (Node child = n.getFirstChild();
           child != null;) {
        Node next = child.getNext();
        scanVars(child, n);
        child = next;
      }
    }
  }

    void onRedeclaration(
        Scope s, String name,
        Node n, Node parent, Node gramps, Node nodeWithLineNumber);

    public void onRedeclaration(
        Scope s, String name,
        Node n, Node parent, Node gramps, Node nodeWithLineNumber) {

      // Don't allow multiple variables to be declared at the top level scope
      if (scope.isGlobal()) {
        Scope.Var origVar = scope.getVar(name);
        Node origParent = origVar.getParentNode();
        if (origParent.getType() == Token.CATCH &&
            parent.getType() == Token.CATCH) {
          // Okay, both are 'catch(x)' variables.
          return;
        }

        boolean allowDupe = false;
        JSDocInfo info = n.getJSDocInfo();
        if (info == null) {
          info = parent.getJSDocInfo();
        }
        allowDupe =
            info != null && info.getSuppressions().contains("duplicate");

        if (!allowDupe) {
          compiler.report(
              JSError.make(sourceName, n,
                           VAR_MULTIPLY_DECLARED_ERROR,
                           name,
                           (origVar.input != null
                            ? origVar.input.getName()
                            : "??")));
        }
      } else if (name.equals(ARGUMENTS) && !NodeUtil.isVarDeclaration(n)) {
        // Disallow shadowing "arguments" as we can't handle with our current
        // scope modeling.
        compiler.report(
            JSError.make(sourceName, n,
                VAR_ARGUMENTS_SHADOWED_ERROR));
      }
    }

  private void declareVar(String name, Node n, Node parent,
                          Node gramps, com.google.javascript.rhino.jstype.JSType declaredType,
                          Node nodeWithLineNumber) {

    if (scope.isDeclared(name, false)
        || (scope.isLocal() && name.equals(ARGUMENTS))) {
      redeclarationHandler.onRedeclaration(
          scope, name, n, parent, gramps, nodeWithLineNumber);
    } else {
      scope.declare(name, n, declaredType, compiler.getInput(sourceName));
    }
  }

// relevant test
// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testDuplicateNames
  public void testDuplicateNames() {
    test("var a = function() { return 1; };a = function() { return 2; }",
         "var a = function $() { return 1; };a = function $() { return 2; }");
    assertMapping("$", "a");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testSimpleVarAssignment
  public void testSimpleVarAssignment() {
    test("var a = function() { return 1; }",
         "var a = function $a$() { return 1; }");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToProperty
  public void testAssignmentToProperty() {
    test("var a = {}; a.b = function() { return 1; }",
         "var a = {}; a.b = function $a$b$() { return 1; }");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPrototype
  public void testAssignmentToPrototype() {
    test("function a() {} a.prototype.b = function() { return 1; };",
         "function a() {} " +
         "a.prototype.b = function $a$$b$() { return 1; };");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPrototype2
  public void testAssignmentToPrototype2() {
    test("var a = {}; " +
         "a.b = function() {}; " +
         "a.b.prototype.c = function() { return 1; };",
         "var a = {}; " +
         "a.b = function $a$b$() {}; " +
         "a.b.prototype.c = function $a$b$$c$() { return 1; };");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPrototype3
  public void testAssignmentToPrototype3() {
    test("function a() {} a.prototype['b'] = function() { return 1; };",
         "function a() {} " +
         "a.prototype['b'] = function $a$$b$() { return 1; };");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPrototype4
  public void testAssignmentToPrototype4() {
    test("function a() {} a['prototype']['b'] = function() { return 1; };",
         "function a() {} " +
         "a['prototype']['b'] = function $a$$b$() { return 1; };");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testPrototypeInitializer
  public void testPrototypeInitializer() {
    test("function a(){} a.prototype = {b: function() { return 1; }};",
         "function a(){} " +
         "a.prototype = {b: function $a$$b$() { return 1; }};");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testMultiplePrototypeInitializer
  public void testMultiplePrototypeInitializer() {
    test("function a(){} a.prototype = {b: function() { return 1; }, " +
         "c: function() { return 2; }};",
         "function a(){} " +
         "a.prototype = {b: function $a$$b$() { return 1; }," +
         "c: function $a$$c$() { return 2; }};");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testRecursiveObjectLiteral
  public void testRecursiveObjectLiteral() {
    test("function a(){} a.prototype = {b: {c: function() { return 1; }}}",
         "function a(){}a.prototype={b:{c:function $a$$b$c$(){return 1}}}");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPropertyOfCallReturnValue
  public void testAssignmentToPropertyOfCallReturnValue() {
    test("document.getElementById('x').onClick = function() {};",
         "document.getElementById('x').onClick = " +
         "function $document$getElementById$onClick$() {};");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPropertyOfArrayElement
  public void testAssignmentToPropertyOfArrayElement() {
    test("var a = {}; a.b = [{}]; a.b[0].c = function() {};",
         "var a = {}; a.b = [{}]; a.b[0].c = function $a$b$0$c$() {};");
    test("var a = {b: {'c': {}}}; a.b['c'].d = function() {};",
         "var a = {b: {'c': {}}}; a.b['c'].d = function $a$b$c$d$() {};");
    test("var a = {b: {'c': {}}}; a.b[x()].d = function() {};",
         "var a = {b: {'c': {}}}; a.b[x()].d = function $a$b$x$d$() {};");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToGetElem
  public void testAssignmentToGetElem() {
    test("function() { win['x' + this.id] = function(a){}; }",
         "function() { win['x' + this.id] = function $win$x$this$id$(a){}; }");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testGetElemWithDashes
  public void testGetElemWithDashes() {
    test("var foo = {}; foo['-'] = function() {};",
         "var foo = {}; foo['-'] = function $foo$__0$() {};");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testWhatCausedIeToFail
  public void testWhatCausedIeToFail() {
    
    
    
    test("var main;" +
        "(function() {" +
        "  main = function() {" +
        "    return 5;" +
        "  };" +
        "})();" +
        "" +
        "main();",
        "var main;(function(){main=function $main$(){return 5}})();main()");
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testBasic
  public void testBasic() {
    testVarMotionWithCode("var X = 3;", Token.VAR, Token.SCRIPT);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testNamedFunction
  public void testNamedFunction() {
    testVarMotionWithCode("var X = 3; function f() {}",
        Token.VAR, Token.SCRIPT);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testNamedFunction2
  public void testNamedFunction2() {
    testVarMotionWithCode("var X = 3; function f() {} var Y;",
        Token.VAR, Token.NAME, Token.VAR, Token.SCRIPT);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testFunctionExpression
  public void testFunctionExpression() {
    testVarMotionWithCode("var X = 3, Y = function() {}; 3;",
        Token.NAME, Token.VAR, Token.NUMBER, Token.EXPR_RESULT, Token.SCRIPT);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testFunctionExpression2
  public void testFunctionExpression2() {
    testVarMotionWithCode("var X = 3; var Y = function() {}; 3;",
        Token.VAR, Token.NAME, Token.VAR, Token.NUMBER,
        Token.EXPR_RESULT, Token.SCRIPT);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltAtVarRef
  public void testHaltAtVarRef() {
    testVarMotionWithCode("var X, Y = 3; var Z = X;",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NAME);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltAtVarRef2
  public void testHaltAtVarRef2() {
    testVarMotionWithCode("var X, Y = 3; (function() {})(3, X);",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NUMBER, Token.NAME);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltAtVarRef3
  public void testHaltAtVarRef3() {
    testVarMotionWithCode("var X, Y = 3; X;",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NAME);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltAtSideEffects
  public void testHaltAtSideEffects() {
    testVarMotionWithCode("var X, Y = 3; var Z = B(3);",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NAME, Token.NUMBER);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltAtSideEffects2
  public void testHaltAtSideEffects2() {
    testVarMotionWithCode("var A = 1, X = A, Y = 3; delete A;",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NAME);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltAtSideEffects3
  public void testHaltAtSideEffects3() {
    testVarMotionWithCode("var A = 1, X = A, Y = 3; A++;",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NAME);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltAtSideEffects4
  public void testHaltAtSideEffects4() {
    testVarMotionWithCode("var A = 1, X = A, Y = 3; A--;",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NAME);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltAtSideEffects5
  public void testHaltAtSideEffects5() {
    testVarMotionWithCode("var A = 1, X = A, Y = 3; A = 'a';",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NAME, Token.STRING);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testNoHaltReadWhenValueIsImmutable
  public void testNoHaltReadWhenValueIsImmutable() {
    testVarMotionWithCode("var X = 1, Y = 3; alert();",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NAME);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltReadWhenValueHasSideEffects
  public void testHaltReadWhenValueHasSideEffects() {
    testVarMotionWithCode("var X = f(), Y = 3; alert();",
        Token.NUMBER, Token.NAME, Token.VAR);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testCatchBlock
  public void testCatchBlock() {
    testVarMotionWithCode("var X = 1; try { 4; } catch (X) {}",
        Token.VAR, Token.NUMBER, Token.EXPR_RESULT, Token.BLOCK);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testIfBranch
  public void testIfBranch() {
    testVarMotionWithCode("var X = foo(); if (X) {}",
        Token.VAR, Token.NAME);
  }

// com.google.javascript.jscomp.NodeTraversalTest::testPruningCallbackShouldTraverse1
  public void testPruningCallbackShouldTraverse1() {
    PruningCallback include =
      new PruningCallback(ImmutableSet.of(Token.SCRIPT, Token.VAR), true);

    assertTrue(include.shouldTraverse(null, new Node(Token.SCRIPT), null));
    assertTrue(include.shouldTraverse(null, new Node(Token.VAR), null));
    assertFalse(include.shouldTraverse(null, new Node(Token.NAME), null));
    assertFalse(include.shouldTraverse(null, new Node(Token.ADD), null));
  }

// com.google.javascript.jscomp.NodeTraversalTest::testPruningCallbackShouldTraverse2
  public void testPruningCallbackShouldTraverse2() {
    PruningCallback include =
      new PruningCallback(ImmutableSet.of(Token.SCRIPT, Token.VAR), false);

    assertFalse(include.shouldTraverse(null, new Node(Token.SCRIPT), null));
    assertFalse(include.shouldTraverse(null, new Node(Token.VAR), null));
    assertTrue(include.shouldTraverse(null, new Node(Token.NAME), null));
    assertTrue(include.shouldTraverse(null, new Node(Token.ADD), null));
  }

// com.google.javascript.jscomp.NodeTraversalTest::testReport
  public void testReport() {
    final List<JSError> errors = new ArrayList<JSError>();

    Compiler compiler = new Compiler(new BasicErrorManager() {

      @Override public void report(CheckLevel level, JSError error) {
        errors.add(error);
      }

      @Override public void println(CheckLevel level, JSError error) {
      }

      @Override protected void printSummary() {
      }
    });
    compiler.initCompilerOptionsIfTesting();

    NodeTraversal t = new NodeTraversal(compiler, null);
    DiagnosticType dt = DiagnosticType.warning("FOO", "{0}, {1} - {2}");

    t.report(null, dt, "Foo", "Bar", "Hello");
    assertEquals(1, errors.size());
    assertEquals("Foo, Bar - Hello", errors.get(0).description);
  }

// com.google.javascript.jscomp.NodeTraversalTest::testUnexpectedException
  public void testUnexpectedException() {
    final String TEST_EXCEPTION = "test me";

    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        throw new RuntimeException(TEST_EXCEPTION);
      }
    };

    Compiler compiler = new Compiler();
    NodeTraversal t = new NodeTraversal(compiler, cb);
    String code = "function foo() {}";
    Node tree = parse(compiler, code);

    try {
      t.traverse(tree);
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().startsWith(
          "INTERNAL COMPILER ERROR.\n" +
          "Please report this problem.\n" +
          "test me"));
    }
  }

// com.google.javascript.jscomp.NodeTraversalTest::testGetScopeRoot
  public void testGetScopeRoot() {
    Compiler compiler = new Compiler();
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.ScopedCallback() {

          @Override
          public void enterScope(NodeTraversal t) {
            Node root1 = t.getScopeRoot();
            Node root2 = t.getScope().getRootNode();
            assertEquals(root1, root2);
          }

          @Override
          public void exitScope(NodeTraversal t) {
          }

          @Override
          public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
            return true;
          }

          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
          }
        }
    );

    String code = "" +
            "var a; " +
            "function foo() {" +
            "  var b" +
            "}";
    Node tree = parse(compiler, code);
    t.traverse(tree);
  }

// com.google.javascript.jscomp.NodeTraversalTest::testGetCurrentNode
  public void testGetCurrentNode() {
    Compiler compiler = new Compiler();
    ScopeCreator creator = new SyntacticScopeCreator(compiler);
    ExpectNodeOnEnterScope callback = new ExpectNodeOnEnterScope();
    NodeTraversal t = new NodeTraversal(compiler, callback, creator);

    String code = "" +
            "var a; " +
            "function foo() {" +
            "  var b;" +
            "}";

    Node tree = parse(compiler, code);
    Scope topScope = creator.createScope(tree, null);

    
    
    callback.expect(tree.getFirstChild(), tree);
    t.traverseWithScope(tree.getFirstChild(), topScope);
    callback.assertEntered();

    
    callback.expect(tree.getFirstChild(), tree.getFirstChild());
    t.traverse(tree.getFirstChild());
    callback.assertEntered();

    
    Node fn = tree.getFirstChild().getNext();
    Scope fnScope = creator.createScope(fn, topScope);
    callback.expect(fn, fn);
    t.traverseAtScope(fnScope);
    callback.assertEntered();
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsLiteralOrConstValue
  public void testIsLiteralOrConstValue() {
    assertLiteralAndImmutable(getNode("10"));
    assertLiteralAndImmutable(getNode("-10"));
    assertLiteralButNotImmutable(getNode("[10, 20]"));
    assertLiteralButNotImmutable(getNode("{'a': 20}"));
    assertLiteralButNotImmutable(getNode("[10, , 1.0, [undefined], 'a']"));
    assertLiteralButNotImmutable(getNode("/abc/"));
    assertLiteralAndImmutable(getNode("\"string\""));
    assertLiteralAndImmutable(getNode("'aaa'"));
    assertLiteralAndImmutable(getNode("null"));
    assertLiteralAndImmutable(getNode("undefined"));
    assertLiteralAndImmutable(getNode("void 0"));
    assertNotLiteral(getNode("abc"));
    assertNotLiteral(getNode("[10, foo(), 20]"));
    assertNotLiteral(getNode("foo()"));
    assertNotLiteral(getNode("c + d"));
    assertNotLiteral(getNode("{'a': foo()}"));
    assertNotLiteral(getNode("void foo()"));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetBooleanValue
  public void testGetBooleanValue() {
    assertBooleanTrue("true");
    assertBooleanTrue("10");
    assertBooleanTrue("'0'");
    assertBooleanTrue("/a/");
    assertBooleanTrue("{}");
    assertBooleanTrue("[]");
    assertBooleanFalse("false");
    assertBooleanFalse("null");
    assertBooleanFalse("0");
    assertBooleanFalse("''");
    assertBooleanFalse("undefined");
    assertBooleanFalse("void 0");
    assertBooleanFalse("void foo()");
    assertBooleanUnknown("b");
    assertBooleanUnknown("-'0.0'");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetExpressionBooleanValue
  public void testGetExpressionBooleanValue() {
    assertExpressionBooleanTrue("a=true");
    assertExpressionBooleanFalse("a=false");

    assertExpressionBooleanTrue("a=(false,true)");
    assertExpressionBooleanFalse("a=(true,false)");

    assertExpressionBooleanTrue("a=(false || true)");
    assertExpressionBooleanFalse("a=(true && false)");

    assertExpressionBooleanTrue("a=!(true && false)");

    assertExpressionBooleanTrue("a,true");
    assertExpressionBooleanFalse("a,false");

    assertExpressionBooleanTrue("true||false");
    assertExpressionBooleanFalse("false||false");

    assertExpressionBooleanTrue("true&&true");
    assertExpressionBooleanFalse("true&&false");

    assertExpressionBooleanFalse("!true");
    assertExpressionBooleanTrue("!false");
    assertExpressionBooleanTrue("!''");

    
    assertExpressionBooleanUnknown("a *= 2");

    
    
    assertExpressionBooleanUnknown("2 + 2");

    assertExpressionBooleanTrue("a=1");
    assertExpressionBooleanTrue("a=/a/");
    assertExpressionBooleanTrue("a={}");

    assertExpressionBooleanTrue("true");
    assertExpressionBooleanTrue("10");
    assertExpressionBooleanTrue("'0'");
    assertExpressionBooleanTrue("/a/");
    assertExpressionBooleanTrue("{}");
    assertExpressionBooleanTrue("[]");
    assertExpressionBooleanFalse("false");
    assertExpressionBooleanFalse("null");
    assertExpressionBooleanFalse("0");
    assertExpressionBooleanFalse("''");
    assertExpressionBooleanFalse("undefined");
    assertExpressionBooleanFalse("void 0");
    assertExpressionBooleanFalse("void foo()");

    assertExpressionBooleanTrue("a?true:true");
    assertExpressionBooleanFalse("a?false:false");
    assertExpressionBooleanUnknown("a?true:false");
    assertExpressionBooleanUnknown("a?true:foo()");

    assertExpressionBooleanUnknown("b");
    assertExpressionBooleanUnknown("-'0.0'");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetStringValue
  public void testGetStringValue() {
    assertEquals("true", NodeUtil.getStringValue(getNode("true")));
    assertEquals("10", NodeUtil.getStringValue(getNode("10")));
    assertEquals("1", NodeUtil.getStringValue(getNode("1.0")));
    assertEquals("0", NodeUtil.getStringValue(getNode("'0'")));
    assertEquals(null, NodeUtil.getStringValue(getNode("/a/")));
    assertEquals(null, NodeUtil.getStringValue(getNode("{}")));
    assertEquals(null, NodeUtil.getStringValue(getNode("[]")));
    assertEquals("false", NodeUtil.getStringValue(getNode("false")));
    assertEquals("null", NodeUtil.getStringValue(getNode("null")));
    assertEquals("0", NodeUtil.getStringValue(getNode("0")));
    assertEquals("", NodeUtil.getStringValue(getNode("''")));
    assertEquals("undefined", NodeUtil.getStringValue(getNode("undefined")));
    assertEquals("undefined", NodeUtil.getStringValue(getNode("void 0")));
    assertEquals("undefined", NodeUtil.getStringValue(getNode("void foo()")));
    
    assertEquals("NaN", NodeUtil.getStringValue(getNode("NaN")));
    assertEquals("Infinity", NodeUtil.getStringValue(getNode("Infinity")));
    assertEquals(null, NodeUtil.getStringValue(getNode("x")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetFunctionName1
  public void testGetFunctionName1() throws Exception {
    Compiler compiler = new Compiler();
    Node parent = compiler.parseTestCode("function name(){}");

    testGetFunctionName(parent.getFirstChild(), "name");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetFunctionName2
  public void testGetFunctionName2() throws Exception {
    Compiler compiler = new Compiler();
    Node parent = compiler.parseTestCode("var name = function(){}")
        .getFirstChild().getFirstChild();

    testGetFunctionName(parent.getFirstChild(), "name");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetFunctionName3
  public void testGetFunctionName3() throws Exception {
    Compiler compiler = new Compiler();
    Node parent = compiler.parseTestCode("qualified.name = function(){}")
        .getFirstChild().getFirstChild();

    testGetFunctionName(parent.getLastChild(), "qualified.name");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetFunctionName4
  public void testGetFunctionName4() throws Exception {
    Compiler compiler = new Compiler();
    Node parent = compiler.parseTestCode("var name2 = function name1(){}")
        .getFirstChild().getFirstChild();

    testGetFunctionName(parent.getFirstChild(), "name2");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetFunctionName5
  public void testGetFunctionName5() throws Exception {
    Compiler compiler = new Compiler();
    Node n = compiler.parseTestCode("qualified.name2 = function name1(){}");
    Node parent = n.getFirstChild().getFirstChild();

    testGetFunctionName(parent.getLastChild(), "qualified.name2");
  }

// com.google.javascript.jscomp.NodeUtilTest::testContainsFunctionDeclaration
  public void testContainsFunctionDeclaration() {
    assertTrue(NodeUtil.containsFunction(
                   getNode("function foo(){}")));
    assertTrue(NodeUtil.containsFunction(
                   getNode("(b?function(){}:null)")));

    assertFalse(NodeUtil.containsFunction(
                   getNode("(b?foo():null)")));
    assertFalse(NodeUtil.containsFunction(
                    getNode("foo()")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testMayHaveSideEffects
  public void testMayHaveSideEffects() {
    assertSideEffect(true, "i++");
    assertSideEffect(true, "[b, [a, i++]]");
    assertSideEffect(true, "i=3");
    assertSideEffect(true, "[0, i=3]");
    assertSideEffect(true, "b()");
    assertSideEffect(true, "[1, b()]");
    assertSideEffect(true, "b.b=4");
    assertSideEffect(true, "b.b--");
    assertSideEffect(true, "i--");
    assertSideEffect(true, "a[0][i=4]");
    assertSideEffect(true, "a += 3");
    assertSideEffect(true, "a, b, z += 4");
    assertSideEffect(true, "a ? c : d++");
    assertSideEffect(true, "a + c++");
    assertSideEffect(true, "a + c - d()");
    assertSideEffect(true, "a + c - d()");

    assertSideEffect(true, "function foo() {}");
    assertSideEffect(true, "while(true);");
    assertSideEffect(true, "if(true){a()}");

    assertSideEffect(false, "if(true){a}");
    assertSideEffect(false, "(function() { })");
    assertSideEffect(false, "(function() { i++ })");
    assertSideEffect(false, "[function a(){}]");

    assertSideEffect(false, "a");
    assertSideEffect(false, "[b, c [d, [e]]]");
    assertSideEffect(false, "({a: x, b: y, c: z})");
    assertSideEffect(false, "/abc/gi");
    assertSideEffect(false, "'a'");
    assertSideEffect(false, "0");
    assertSideEffect(false, "a + c");
    assertSideEffect(false, "'c' + a[0]");
    assertSideEffect(false, "a[0][1]");
    assertSideEffect(false, "'a' + c");
    assertSideEffect(false, "'a' + a.name");
    assertSideEffect(false, "1, 2, 3");
    assertSideEffect(false, "a, b, 3");
    assertSideEffect(false, "(function(a, b) {  })");
    assertSideEffect(false, "a ? c : d");
    assertSideEffect(false, "'1' + navigator.userAgent");

    assertSideEffect(false, "new RegExp('foobar', 'i')");
    assertSideEffect(true, "new RegExp(SomethingWacky(), 'i')");
    assertSideEffect(false, "new Array()");
    assertSideEffect(false, "new Array");
    assertSideEffect(false, "new Array(4)");
    assertSideEffect(false, "new Array('a', 'b', 'c')");
    assertSideEffect(true, "new SomeClassINeverHeardOf()");
    assertSideEffect(true, "new SomeClassINeverHeardOf()");

    assertSideEffect(false, "({}).foo = 4");
    assertSideEffect(false, "([]).foo = 4");
    assertSideEffect(false, "(function() {}).foo = 4");

    assertSideEffect(true, "this.foo = 4");
    assertSideEffect(true, "a.foo = 4");
    assertSideEffect(true, "(function() { return n; })().foo = 4");
    assertSideEffect(true, "([]).foo = bar()");

    assertSideEffect(false, "undefined");
    assertSideEffect(false, "void 0");
    assertSideEffect(true, "void foo()");
    assertSideEffect(false, "-Infinity");
    assertSideEffect(false, "Infinity");
    assertSideEffect(false, "NaN");

    assertSideEffect(false, "({}||[]).foo = 2;");
    assertSideEffect(false, "(true ? {} : []).foo = 2;");
    assertSideEffect(false, "({},[]).foo = 2;");
  }

// com.google.javascript.jscomp.NodeUtilTest::testObjectMethodSideEffects
  public void testObjectMethodSideEffects() {
    
    assertSideEffect(false, "o.toString()");
    assertSideEffect(false, "o.valueOf()");

    
    assertSideEffect(true, "o.watch()");
  }

// com.google.javascript.jscomp.NodeUtilTest::testRegExpSideEffect
  public void testRegExpSideEffect() {
    
    assertSideEffect(false, "/abc/gi", true);
    assertSideEffect(false, "/abc/gi", false);

    
    
    
    assertSideEffect(true, "(/abc/gi).test('')", true);
    assertSideEffect(false, "(/abc/gi).test('')", false);
    assertSideEffect(true, "(/abc/gi).test(a)", true);
    assertSideEffect(false, "(/abc/gi).test(b)", false);

    assertSideEffect(true, "(/abc/gi).exec('')", true);
    assertSideEffect(false, "(/abc/gi).exec('')", false);

    
    assertSideEffect(true, "(/abc/gi).foo('')", true);
    assertSideEffect(true, "(/abc/gi).foo('')", false);

    
    assertSideEffect(true, "''.match('a')", true);
    assertSideEffect(false, "''.match('a')", false);
    assertSideEffect(true, "''.match(/(a)/)", true);
    assertSideEffect(false, "''.match(/(a)/)", false);

    assertSideEffect(true, "''.replace('a')", true);
    assertSideEffect(false, "''.replace('a')", false);

    assertSideEffect(true, "''.search('a')", true);
    assertSideEffect(false, "''.search('a')", false);

    assertSideEffect(true, "''.split('a')", true);
    assertSideEffect(false, "''.split('a')", false);

    
    assertSideEffect(true, "''.foo('a')", true);
    assertSideEffect(true, "''.foo('a')", false);

    
    
    
    
    assertSideEffect(true, "''.match(a)", true);
    assertSideEffect(true, "''.match(a)", false);
  }

// com.google.javascript.jscomp.NodeUtilTest::testMayEffectMutableState
  public void testMayEffectMutableState() {
    assertMutableState(true, "i++");
    assertMutableState(true, "[b, [a, i++]]");
    assertMutableState(true, "i=3");
    assertMutableState(true, "[0, i=3]");
    assertMutableState(true, "b()");
    assertMutableState(true, "void b()");
    assertMutableState(true, "[1, b()]");
    assertMutableState(true, "b.b=4");
    assertMutableState(true, "b.b--");
    assertMutableState(true, "i--");
    assertMutableState(true, "a[0][i=4]");
    assertMutableState(true, "a += 3");
    assertMutableState(true, "a, b, z += 4");
    assertMutableState(true, "a ? c : d++");
    assertMutableState(true, "a + c++");
    assertMutableState(true, "a + c - d()");
    assertMutableState(true, "a + c - d()");

    assertMutableState(true, "function foo() {}");
    assertMutableState(true, "while(true);");
    assertMutableState(true, "if(true){a()}");

    assertMutableState(false, "if(true){a}");
    assertMutableState(true, "(function() { })");
    assertMutableState(true, "(function() { i++ })");
    assertMutableState(true, "[function a(){}]");

    assertMutableState(false, "a");
    assertMutableState(true, "[b, c [d, [e]]]");
    assertMutableState(true, "({a: x, b: y, c: z})");
    
    
    assertMutableState(true, "/abc/gi");
    assertMutableState(false, "'a'");
    assertMutableState(false, "0");
    assertMutableState(false, "a + c");
    assertMutableState(false, "'c' + a[0]");
    assertMutableState(false, "a[0][1]");
    assertMutableState(false, "'a' + c");
    assertMutableState(false, "'a' + a.name");
    assertMutableState(false, "1, 2, 3");
    assertMutableState(false, "a, b, 3");
    assertMutableState(true, "(function(a, b) {  })");
    assertMutableState(false, "a ? c : d");
    assertMutableState(false, "'1' + navigator.userAgent");

    assertMutableState(true, "new RegExp('foobar', 'i')");
    assertMutableState(true, "new RegExp(SomethingWacky(), 'i')");
    assertMutableState(true, "new Array()");
    assertMutableState(true, "new Array");
    assertMutableState(true, "new Array(4)");
    assertMutableState(true, "new Array('a', 'b', 'c')");
    assertMutableState(true, "new SomeClassINeverHeardOf()");
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsFunctionExpression
  public void testIsFunctionExpression() {
    assertContainsAnonFunc(true, "(function(){})");
    assertContainsAnonFunc(true, "[function a(){}]");
    assertContainsAnonFunc(false, "{x: function a(){}}");
    assertContainsAnonFunc(true, "(function a(){})()");
    assertContainsAnonFunc(true, "x = function a(){};");
    assertContainsAnonFunc(true, "var x = function a(){};");
    assertContainsAnonFunc(true, "if (function a(){});");
    assertContainsAnonFunc(true, "while (function a(){});");
    assertContainsAnonFunc(true, "do; while (function a(){});");
    assertContainsAnonFunc(true, "for (function a(){};;);");
    assertContainsAnonFunc(true, "for (;function a(){};);");
    assertContainsAnonFunc(true, "for (;;function a(){});");
    assertContainsAnonFunc(true, "for (p in function a(){});");
    assertContainsAnonFunc(true, "with (function a(){}) {}");
    assertContainsAnonFunc(false, "function a(){}");
    assertContainsAnonFunc(false, "if (x) function a(){};");
    assertContainsAnonFunc(false, "if (x) { function a(){} }");
    assertContainsAnonFunc(false, "if (x); else function a(){};");
    assertContainsAnonFunc(false, "while (x) function a(){};");
    assertContainsAnonFunc(false, "do function a(){} while (0);");
    assertContainsAnonFunc(false, "for (;;) function a(){}");
    assertContainsAnonFunc(false, "for (p in o) function a(){};");
    assertContainsAnonFunc(false, "with (x) function a(){}");
  }

// com.google.javascript.jscomp.NodeUtilTest::testNewFunctionNode
  public void testNewFunctionNode() {
    Node expected = parse("function foo(p1, p2, p3) { throw 2; }");
    Node body = new Node(Token.BLOCK, new Node(Token.THROW, Node.newNumber(2)));
    List<Node> params = Lists.newArrayList(Node.newString(Token.NAME, "p1"),
                                           Node.newString(Token.NAME, "p2"),
                                           Node.newString(Token.NAME, "p3"));
    Node function = NodeUtil.newFunctionNode(
        "foo", params, body, -1, -1);
    Node actual = new Node(Token.SCRIPT);
    actual.addChildToFront(function);
    String difference = expected.checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testContainsType
  public void testContainsType() {
    assertTrue(NodeUtil.containsType(
        parse("this"), Token.THIS));
    assertTrue(NodeUtil.containsType(
        parse("function foo(){}(this)"), Token.THIS));
    assertTrue(NodeUtil.containsType(
        parse("b?this:null"), Token.THIS));

    assertFalse(NodeUtil.containsType(
        parse("a"), Token.THIS));
    assertFalse(NodeUtil.containsType(
        parse("function foo(){}"), Token.THIS));
    assertFalse(NodeUtil.containsType(
        parse("(b?foo():null)"), Token.THIS));
  }

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

// com.google.javascript.jscomp.NodeUtilTest::testLocalValue1
  public void testLocalValue1() throws Exception {
    
    assertFalse(testLocalValue("x"));
    assertFalse(testLocalValue("x()"));
    assertFalse(testLocalValue("this"));
    assertFalse(testLocalValue("arguments"));

    
    assertTrue(testLocalValue("new x()"));

    
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

// com.google.javascript.jscomp.NormalizeTest::testForIn
  public void testForIn() {
    
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

// com.google.javascript.jscomp.NormalizeTest::testNormalizeSyntheticCode
  public void testNormalizeSyntheticCode() {
    Compiler compiler = new Compiler();
    compiler.init(
        Lists.<JSSourceFile>newArrayList(),
        Lists.<JSSourceFile>newArrayList(), new CompilerOptions());
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

// com.google.javascript.jscomp.NormalizeTest::testPropertyIsConstant
  public void testPropertyIsConstant() throws Exception {
    testSame("var a = {};a.CONST = 3; var b = a.CONST;");
    Node n = getLastCompiler().getRoot();

    Set<Node> constantNodes = findNodesWithProperty(n, Node.IS_CONSTANT_NAME);
    assertEquals(2, constantNodes.size());
    for (Node hasProp : constantNodes) {
      assertEquals("CONST", hasProp.getString());
    }
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

    String expected = "var goog = {};" +
        "goog.foo = function (p1) { var p2 };" +
        "goog.foo = function (q1) { var q2 };" +
        "goog.foo = function (r1) { var r2 };" +
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

    String expected = "var goog = {};" +
        "goog.foo = function(p1, p2) { var p4; var p3};" +
        "goog.foo = function(q1, q2) { var q4; var q3};" +
        "goog.foo = function(r1, r2) { var r4; var r3};" +
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

    String expected = "var goog1 = {};" +
        "goog1.foo = function () { };" +
        "var goog2 = {};" +
        "goog2.foo = function () { var p1 };" +
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
    String src = "function foo(bar) {};" +
        "baz = function(a) {};" +
        "baz(1);" +
        "foo(baz);"; 
    testSame(src);
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
        "a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteObjLit2
  public void testNoRewriteObjLit2() throws Exception {
    String source = newlineJoin(
        "var a = {b:function fn(){return 1;}}",
        "for(c in a) (a[c])();",
        "a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteArrLit
  public void testNoRewriteArrLit() throws Exception {
    String source = newlineJoin(
        "var a = [function(){return 1;}]",
        "(a[0])();",
        "a()");
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
    StringBuilder sb = new StringBuilder("\"foo\";");
    for (int i = 0; i < 20; i++) {
      sb.append("function f");
      sb.append(i);
      sb.append("(){\"foo\"}");
    }
    replace(sb.toString());
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

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testUnaryOps
  public void testUnaryOps() {
    
    foldSame("!foo()");
    foldSame("~foo()");
    foldSame("-foo()");

    
    fold("a=!true", "a=false");
    fold("a=!10", "a=false");
    fold("a=!false", "a=true");
    fold("a=!foo()", "a=!foo()");
    fold("a=-0", "a=0");
    fold("a=-Infinity", "a=-Infinity");
    fold("a=-NaN", "a=NaN");
    fold("a=-foo()", "a=-foo()");
    fold("a=~~0", "a=0");
    fold("a=~~10", "a=10");
    fold("a=~-7", "a=6");
    fold("a=~0x100000000", "a=~0x100000000",
         PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("a=~-0x100000000", "a=~-0x100000000",
         PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("a=~.5", "~.5", PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
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

    fold("x = -1 & 0", "x = 0");
    fold("x = 0 & -1", "x = 0");
    fold("x = 1 & 4", "x = 0");
    fold("x = 2 & 3", "x = 2");

    
    
    fold("x = 1 & 1.1", "x = 1&1.1");
    fold("x = 1.1 & 1", "x = 1.1&1");
    fold("x = 1 & 3000000000", "x = 1&3000000000");
    fold("x = 3000000000 & 1", "x = 3000000000&1");

    
    fold("x = 1 | 4", "x = 5");
    fold("x = 1 | 3", "x = 3");
    fold("x = 1 | 1.1", "x = 1|1.1");
    fold("x = 1 | 3000000000", "x = 1|3000000000");
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

    fold("3000000000 << 1", "3000000000<<1",
         PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("1 << 32", "1<<32",
        PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    fold("1 << -1", "1<<32",
        PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    fold("3000000000 >> 1", "3000000000>>1",
        PeepholeFoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("1 >> 32", "1>>32",
        PeepholeFoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    fold("1.5 << 0",  "1.5<<0",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1 << .5",   "1.5<<0",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1.5 >>> 0", "1.5>>>0",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1 >>> .5",  "1.5>>>0",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1.5 >> 0",  "1.5>>0",
        PeepholeFoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1 >> .5",   "1.5>>0",
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
    fold("x = '' + []", "x = \"\"+[]");      
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldConstructor
  public void testFoldConstructor() {
    fold("x = this[new String('a')]", "x = this['a']");
    fold("x = ob[new String(12)]", "x = ob['12']");
    fold("x = ob[new String(false)]", "x = ob['false']");
    fold("x = ob[new String(null)]", "x = ob['null']");
    foldSame("x = ob[new String(a)]");
    foldSame("x = new String('a')");
    foldSame("x = (new String('a'))[3]");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testStringIndexOf
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

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testStringJoinAdd
  public void testStringJoinAdd() {
    fold("x = ['a', 'b', 'c'].join('')", "x = \"abc\"");
    fold("x = [].join(',')", "x = \"\"");
    fold("x = ['a'].join(',')", "x = \"a\"");
    fold("x = ['a', 'b', 'c'].join(',')", "x = \"a,b,c\"");
    fold("x = ['a', foo, 'b', 'c'].join(',')",
        "x = [\"a\",foo,\"b,c\"].join(\",\")");
    fold("x = [foo, 'a', 'b', 'c'].join(',')",
        "x = [foo,\"a,b,c\"].join(\",\")");
    fold("x = ['a', 'b', 'c', foo].join(',')",
        "x = [\"a,b,c\",foo].join(\",\")");

    
    fold("x = ['a=', 5].join('')", "x = \"a=5\"");
    fold("x = ['a', '5'].join(7)", "x = \"a75\"");

    
    fold("x = ['a=', false].join('')", "x = \"a=false\"");
    fold("x = ['a', '5'].join(true)", "x = \"atrue5\"");
    fold("x = ['a', '5'].join(false)", "x = \"afalse5\"");

    
    fold("x = ['a', '5', 'c'].join('a very very very long chain')",
         "x = [\"a\",\"5\",\"c\"].join(\"a very very very long chain\")");

    
    foldSame("x = ['', foo].join(',')");
    foldSame("x = ['', foo, ''].join(',')");

    fold("x = ['', '', foo, ''].join(',')", "x = [',', foo, ''].join(',')");
    fold("x = ['', '', foo, '', ''].join(',')",
         "x = [',', foo, ','].join(',')");

    fold("x = ['', '', foo, '', '', bar].join(',')",
         "x = [',', foo, ',', bar].join(',')");

    fold("x = [1,2,3].join('abcdef')",
         "x = '1abcdef2abcdef3'");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testStringJoinAdd_b1992789
  public void testStringJoinAdd_b1992789() {
    fold("x = ['a'].join('')", "x = \"a\"");
    fold("x = [foo()].join('')", "x = '' + foo()");
    fold("[foo()].join('')", "'' + foo()");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldStringSubstr
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

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldStringSubstring
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

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArithmetic
  public void testFoldArithmetic() {
    fold("x = 10 + 20", "x = 30");
    fold("x = 2 / 4", "x = 0.5");
    fold("x = 2.25 * 3", "x = 6.75");
    fold("z = x * y", "z = x * y");
    fold("x = y * 5", "x = y * 5");
    fold("x = 1 / 0", "", PeepholeFoldConstants.DIVIDE_BY_0_ERROR);
    fold("x = 3 % 2", "x = 1");
    fold("x = 3 % -2", "x = 1");
    fold("x = -1 % 3", "x = -1");
    fold("x = 1 % 0", "", PeepholeFoldConstants.DIVIDE_BY_0_ERROR);
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
    foldSame("x = (null + 1) * 2"); 
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
    fold("x = true == true", "x = true");
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
    fold("x = true === true", "x = true");
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

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldGetElem
  public void testFoldGetElem() {
    fold("x = [10, 20][0]", "x = 10");
    fold("x = [10, 20][1]", "x = 20");
    fold("x = [10, 20][0.5]", "",
        PeepholeFoldConstants.INVALID_GETELEM_INDEX_ERROR);
    fold("x = [10, 20][-1]",    "",
        PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
    fold("x = [10, 20][2]",     "",
        PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldComplex
  public void testFoldComplex() {
    fold("x = (3 / 1.0) + (1 * 2)", "x = 5");
    fold("x = (1 == 1.0) && foo() && true", "x = foo()&&true");
    fold("x = 'abc' + 5 + 10", "x = \"abc510\"");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArrayLength
  public void testFoldArrayLength() {
    
    fold("x = [].length", "x = 0");
    fold("x = [1,2,3].length", "x = 3");
    fold("x = [a,b].length", "x = 2");

    
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

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testAssignOps
  public void testAssignOps() {
    fold("x=x+y", "x+=y");
    fold("x=x*y", "x*=y");
    fold("x.y=x.y+z", "x.y+=z");
    foldSame("next().x = next().x + 1");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldAdd1
  public void testFoldAdd1() {
    foldSame("x=false+1");
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
            if (a.equals("NaN") || a.equals("Infinity")) {
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

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofObject
  public void testFoldTypeofObject() {
    test("var x = {};typeof x",
         "var x = {};\"object\"");
    
    test("var x = [];typeof x",
         "var x = [];\"object\"");
    
    
    test("var x = null;typeof x",
         "var x = null;\"object\"");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofString
  public void testFoldTypeofString() {
    test("var x = \"foo\";typeof x",
         "var x = \"foo\";\"string\"");
    
    test("var x = new String(\"foo\");typeof x",
         "var x = new String(\"foo\");\"object\"");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofNumber
  public void testFoldTypeofNumber() {
    test("var x = 10;typeof x",
         "var x = 10;\"number\"");
    
    test("var x = new Number(6);typeof x",
         "var x = new Number(6);\"object\"");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofBoolean
  public void testFoldTypeofBoolean() {
    test("var x = false;typeof x",
         "var x = false;\"boolean\"");
    
    test("var x = new Boolean(true);typeof x",
         "var x = new Boolean(true);\"object\"");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofUndefined
  public void testFoldTypeofUndefined() {
    test("var x = undefined;typeof x",
         "var x = undefined;\"undefined\""); 
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testDontFoldTypeofUnionTypes
  public void testDontFoldTypeofUnionTypes() {
    
    testSame("var x = (unknown ? {} : null);typeof x");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testDontFoldTypeofSideEffects
  public void testDontFoldTypeofSideEffects() {
    
    testSame("var x = 6 ;typeof (x++)");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testDontFoldTypeofWithTypeCheckDisabled
  public void testDontFoldTypeofWithTypeCheckDisabled() {
    disableTypeCheck();
    testSame("var x = {};typeof x");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldOneChildBlocksIntegration
  public void testFoldOneChildBlocksIntegration() {
     fold("function(){switch(foo()){default:{break}}}",
          "function(){foo()}");

     fold("function(){switch(x){default:{break}}}",
          "function(){}");

     fold("function(){switch(x){default:x;case 1:return 2}}",
          "function(){switch(x){default:case 1:return 2}}");

     
     fold("if(x){if(true){foo();foo()}else{bar();bar()}}",
          "if(x){foo();foo()}");

     fold("if(x){if(false){foo();foo()}else{bar();bar()}}",
          "if(x){bar();bar()}");

     
     fold("if(x()){}", "x()");

     fold("if(x()){} else {x()}", "x()||x()");
     fold("if(x){}", ""); 
     fold("if(a()){A()} else if (b()) {} else {C()}",
          "if(a())A();else b()||C()");

     fold("if(a()){} else if (b()) {} else {C()}",
          "a()||b()||C()");
     fold("if(a()){A()} else if (b()) {} else if (c()) {} else{D()}",
          "if(a())A();else b()||c()||D()");
     fold("if(a()){} else if (b()) {} else if (c()) {} else{D()}",
          "a()||b()||c()||D()");
     fold("if(a()){A()} else if (b()) {} else if (c()) {} else{}",
          "if(a())A();else b()||c()");

     
     fold("function foo(){if(x()){}}", "function foo(){x()}");

  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldOneChildBlocksStringCompare
  public void testFoldOneChildBlocksStringCompare() {
    
    assertResultString("if(x){if(y){var x;}}else{var z;}",
        "if(x){if(y)var x}else var z");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testNecessaryDanglingElse
  public void testNecessaryDanglingElse() {
    
    
    
    assertResultString(
        "if(x)if(y){y();z()}else;else x()", "if(x){if(y){y();z()}}else x()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldReturnsIntegration
  public void testFoldReturnsIntegration() {
    
    fold("function(){if(x)return;else return}",
         "function(){return}");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testBug1059649
  public void testBug1059649() {
    
    fold("if(x){var y=3;}var z=5", "if(x)var y=3;var z=5");

    
    foldSame("if(x){var y=3;}else{var y=4;}var z=5");
    fold("while(x){var y=3;}var z=5", "while(x)var y=3;var z=5");
    fold("for(var i=0;i<10;i++){var y=3;}var z=5",
         "for(var i=0;i<10;i++)var y=3;var z=5");
    fold("for(var i in x){var y=3;}var z=5",
         "for(var i in x)var y=3;var z=5");
    fold("do{var y=3;}while(x);var z=5", "do var y=3;while(x);var z=5");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testHookIfIntegration
  public void testHookIfIntegration() {
    fold("if (false){ x = 1; } else if (cond) { x = 2; } else { x = 3; }",
         "x=cond?2:3");

    fold("x?void 0:y()", "x||y()");
    fold("!x?void 0:y()", "(!x)||y()");
    fold("x?y():void 0", "x&&y()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testRemoveDuplicateStatementsIntegration
  public void testRemoveDuplicateStatementsIntegration() {
    fold("function z() {if (a) { return true }" +
         "else if (b) { return true }" +
         "else { return true }}",
         "function z() {return true;}");

    fold("function z() {if (a()) { return true }" +
         "else if (b()) { return true }" +
         "else { return true }}",
         "function z() {a()||b();return true;}");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldLogicalOpIntegration
  public void testFoldLogicalOpIntegration() {
    test("if(x && true) z()", "x&&z()");
    test("if(x && false) z()", "");
    fold("if(x || 3) z()", "z()");
    fold("if(x || false) z()", "x&&z()");
    test("if(x==y && false) z()", "");
    fold("if(y() || x || 3) z()", "y();z()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldBitwiseOpStringCompareIntegration
  public void testFoldBitwiseOpStringCompareIntegration() {
    assertResultString("while(-1 | 0){}", "while(1);");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testVarLiftingIntegration
  public void testVarLiftingIntegration() {
    fold("if(true);else var a;", "var a");
    fold("if(false) foo();else var a;", "var a");
    fold("if(true)var a;else;", "var a");
    fold("if(false)var a;else;", "var a");
    fold("if(false)var a,b;", "var b; var a");
    fold("if(false){var a;var a;}", "var a");
    fold("if(false)var a=function(){var b};", "var a");
    fold("if(a)if(false)var a;else var b;", "var a;if(a)var b");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testBug1438784
  public void testBug1438784() throws Exception {
    fold("for(var i=0;i<10;i++)if(x)x.y;", "for(var i=0;i<10;i++);");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldUselessWhileIntegration
  public void testFoldUselessWhileIntegration() {
    fold("while(!true) { foo() }", "");
    fold("while(!false) foo() ", "while(1) foo()");
    fold("while(!void 0) foo()", "while(1) foo()");

    
    fold("if(foo())while(false){foo()}else bar()", "foo()||bar()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldUselessForIntegration
  public void testFoldUselessForIntegration() {
    fold("for(;!true;) { foo() }", "");
    fold("for(;void 0;) { foo() }", "");
    fold("for(;undefined;) { foo() }", "");
    fold("for(;1;) foo()", "for(;;) foo()");
    fold("for(;!void 0;) foo()", "for(;;) foo()");

    
    fold("if(foo())for(;false;){foo()}else bar()", "foo()||bar()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldUselessDoIntegration
  public void testFoldUselessDoIntegration() {
    test("do { foo() } while(!true);", "foo()");
    fold("do { foo() } while(void 0);", "foo()");
    fold("do { foo() } while(undefined);", "foo()");
    fold("do { foo() } while(!void 0);", "do { foo() } while(1);");

    
    test("if(foo())do {foo()} while(false) else bar()", "foo()?foo():bar()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testMinimizeWhileConstantConditionIntegration
  public void testMinimizeWhileConstantConditionIntegration() {
    fold("while(!false) foo()", "while(1) foo()");
    fold("while(202) foo()", "while(1) foo()");
    fold("while(Infinity) foo()", "while(1) foo()");
    fold("while('text') foo()", "while(1) foo()");
    fold("while([]) foo()", "while(1) foo()");
    fold("while({}) foo()", "while(1) foo()");
    fold("while(/./) foo()", "while(1) foo()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testMinimizeExpr
  public void testMinimizeExpr() {
    test("!!true", "");

    fold("!!x()", "x()");
    test("!(!x()&&!y())", "x()||y()");
    fold("x()||!!y()", "x()||y()");

    
    fold("!(!!x()&&y())", "x()&&y()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testBug1509085
  public void testBug1509085() {
    PeepholeIntegrationTest oneRepetitiontest = new PeepholeIntegrationTest() {
      @Override
      protected int getNumRepetitions() {
        return 1;
      }
    };

    oneRepetitiontest.test("x ? x() : void 0", "x&&x();");
    oneRepetitiontest.foldSame("y = x ? x() : void 0");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testBugIssue3
  public void testBugIssue3() {
    foldSame("function foo() {" +
             "  if(sections.length != 1) children[i] = 0;" +
             "  else var selectedid = children[i]" +
             "}");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testBugIssue43
  public void testBugIssue43() {
    foldSame("function foo() {" +
             "  if (a) { var b = 1; } else { a.b = 1; }" +
             "}");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldNegativeBug
  public void testFoldNegativeBug() {
    fold("while(-3){};", "while(1);");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testNoNormalizeLabeledExpr
  public void testNoNormalizeLabeledExpr() {
    enableNormalize(true);
    foldSame("var x; foo:{x = 3;}");
    foldSame("var x; foo:x = 3;");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testShortCircuit1
  public void testShortCircuit1() {
    test("1 && a()", "a()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testShortCircuit2
  public void testShortCircuit2() {
    test("1 && a() && 2", "a()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testShortCircuit3
  public void testShortCircuit3() {
    test("a() && 1 && 2", "a()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testShortCircuit4
  public void testShortCircuit4() {
    test("a() && (1 && b())", "a() && b()");
    test("a() && 1 && b()", "a() && b()");
    test("(a() && 1) && b()", "a() && b()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testMinimizeExprCondition
  public void testMinimizeExprCondition() {
    fold("(x || true) && y()", "y()");
    fold("(x || false) && y()", "x&&y()");
    fold("(x && true) && y()", "x && y()");
    fold("(x && false) && y()", "");
    fold("a = x || false ? b : c", "a=x?b:c");
    fold("do {x()} while((x && false) && y())", "{x()}");
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

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testFoldBlock
  public void testFoldBlock() {
    fold("{{foo()}}", "foo()");
    fold("{foo();{}}", "foo()");
    fold("{{foo()}{}}", "foo()");
    fold("{{foo()}{bar()}}", "foo();bar()");
    fold("{if(false)foo(); {bar()}}", "bar()");
    fold("{if(false)if(false)if(false)foo(); {bar()}}", "bar()");

    fold("{'hi'}", "");
    fold("{x==3}", "");
    fold("{ (function(){x++}) }", "");
    fold("function(){return;}", "function(){return;}");
    fold("function(){return 3;}", "function(){return 3}");
    fold("function(){if(x)return; x=3; return; }",
         "function(){if(x)return; x=3; return; }");
    fold("{x=3;;;y=2;;;}", "x=3;y=2");

    
    fold("while(x()){x}", "while(x());");
    fold("while(x()){x()}", "while(x())x()");
    fold("for(x=0;x<100;x++){x}", "for(x=0;x<100;x++);");
    fold("for(x in y){x}", "for(x in y);");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testFoldBlocksWithManyChildren
  public void testFoldBlocksWithManyChildren() {
    fold("function f() { if (false) {} }", "function f(){}");
    fold("function f() { { if (false) {} if (true) {} {} } }",
         "function f(){}");
    fold("{var x; var y; var z; function f() { { var a; { var b; } } } }",
         "var x;var y;var z;function f(){var a;var b}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testIf
  public void testIf() {
    fold("if (1){ x=1; } else { x = 2;}", "x=1");
    fold("if (false){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (null){ x = 1; } else { x = 2; }", "x=2");
    fold("if (void 0){ x = 1; } else { x = 2; }", "x=2");
    fold("if (void foo()){ x = 1; } else { x = 2; }",
         "foo();x=2");
    fold("if (false){ x = 1; } else if (true) { x = 3; } else { x = 2; }",
         "x=3");
    fold("if (x){ x = 1; } else if (false) { x = 3; }",
         "if(x)x=1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook
  public void testHook() {
    fold("true ? a() : b()", "a()");
    fold("false ? a() : b()", "b()");

    fold("a() ? b() : true", "a() && b()");
    fold("a() ? true : b()", "a() || b()");

    fold("(a = true) ? b() : c()", "a = true; b()");
    fold("(a = false) ? b() : c()", "a = false; c()");
    fold("do {f()} while((a = true) ? b() : c())",
         "do {f()} while((a = true) , b())");
    fold("do {f()} while((a = false) ? b() : c())",
         "do {f()} while((a = false) , c())");

    fold("var x = (true) ? 1 : 0", "var x=1");
    fold("var y = (true) ? ((false) ? 12 : (cond ? 1 : 2)) : 13",
         "var y=cond?1:2");

    foldSame("var z=x?void 0:y()");
    foldSame("z=x?void 0:y()");
    foldSame("z*=x?void 0:y()");

    foldSame("var z=x?y():void 0");
    foldSame("(w?x:void 0).y=z");
    foldSame("(w?x:void 0).y+=z");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testConstantConditionWithSideEffect1
  public void testConstantConditionWithSideEffect1() {
    fold("if (b=true) x=1;", "b=true;x=1");
    fold("if (b=/ab/) x=1;", "b=/ab/;x=1");
    fold("if (b=/ab/){ x=1; } else { x=2; }", "b=/ab/;x=1");
    fold("var b;b=/ab/;if(b)x=1;", "var b;b=/ab/;x=1");
    foldSame("var b;b=f();if(b)x=1;");
    fold("var b=/ab/;if(b)x=1;", "var b=/ab/;x=1");
    foldSame("var b=f();if(b)x=1;");
    foldSame("b=b++;if(b)x=b;");
    fold("(b=0,b=1);if(b)x=b;", "b=0;b=1;x=b;");
    fold("b=1;if(foo,b)x=b;","b=1;x=b;");
    foldSame("b=1;if(foo=1,b)x=b;");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testConstantConditionWithSideEffect2
  public void testConstantConditionWithSideEffect2() {
    fold("(b=true)?x=1:x=2;", "b=true;x=1");
    fold("(b=false)?x=1:x=2;", "b=false;x=2");
    fold("if (b=/ab/) x=1;", "b=/ab/;x=1");
    fold("var b;b=/ab/;(b)?x=1:x=2;", "var b;b=/ab/;x=1");
    foldSame("var b;b=f();(b)?x=1:x=2;");
    fold("var b=/ab/;(b)?x=1:x=2;", "var b=/ab/;x=1");
    foldSame("var b=f();(b)?x=1:x=2;");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testVarLifting
  public void testVarLifting() {
    fold("if(true)var a", "var a");
    fold("if(false)var a", "var a");

    
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testFoldUselessWhile
  public void testFoldUselessWhile() {
    fold("while(false) { foo() }", "");

    fold("while(void 0) { foo() }", "");
    fold("while(undefined) { foo() }", "");

    foldSame("while(true) foo()");

    fold("while(false) { var a = 0; }", "var a");

    
    fold("while(false) { foo(); continue }", "");

    fold("while(0) { foo() }", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testFoldUselessFor
  public void testFoldUselessFor() {
    fold("for(;false;) { foo() }", "");
    fold("for(;void 0;) { foo() }", "");
    fold("for(;undefined;) { foo() }", "");
    fold("for(;true;) foo() ", "for(;;) foo() ");
    foldSame("for(;;) foo()");
    fold("for(;false;) { var a = 0; }", "var a");

    
    fold("for(;false;) { foo(); continue }", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testFoldUselessDo
  public void testFoldUselessDo() {
    fold("do { foo() } while(false);", "foo()");
    fold("do { foo() } while(void 0);", "foo()");
    fold("do { foo() } while(undefined);", "foo()");
    fold("do { foo() } while(true);", "do { foo() } while(true);");
    fold("do { var a = 0; } while(false);", "var a=0");

    
    foldSame("do { foo(); continue; } while(0)");
    foldSame("do { foo(); break; } while(0)");
    }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testMinimizeWhileConstantCondition
  public void testMinimizeWhileConstantCondition() {
    fold("while(true) foo()", "while(true) foo()");
    fold("while(0) foo()", "");
    fold("while(0.0) foo()", "");
    fold("while(NaN) foo()", "");
    fold("while(null) foo()", "");
    fold("while(undefined) foo()", "");
    fold("while('') foo()", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testFoldConstantCommaExpressions
  public void testFoldConstantCommaExpressions() {
    fold("if (true, false) {foo()}", "");
    fold("if (false, true) {foo()}", "foo()");
    fold("true, foo()", "foo()");
    fold("(1 + 2 + ''), foo()", "foo()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testSplitCommaExpressions
  public void testSplitCommaExpressions() {
    
    foldSame("while (foo(), true) boo()");
    foldSame("var a = (foo(), true);");
    foldSame("a = (foo(), true);");

    
    foldSame("a:a(),b()");

    fold("(x=2), foo()", "x=2; foo()");
    fold("foo(), boo();", "foo(); boo()");
    fold("(a(), b()), (c(), d());", "a(); b(); c(); d();");
    fold("foo(), true", "foo();");
    fold("function x(){foo(), true}", "function x(){foo();}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveUselessOps
  public void testRemoveUselessOps() {
    
    
    
    
    

    
    fold("Math.random()", "");
    fold("Math.random(f() + g())", "f(); g();");
    fold("Math.random(f(),g(),h())", "f();g();h();");

    
    foldSame("f();");
    foldSame("(function () {})();");

    
    fold("(function () {});", "");
    fold("(function f() {});", "");
    
    fold("(function () {foo();});", "");

    
    fold("+f()", "f()");
    fold("a=(+f(),g())", "a=(f(),g())");
    fold("a=(true,g())", "a=g()");
    fold("f(),true", "f()");
    fold("f() + g()", "f();g()");

    fold("for(;;+f()){}", "for(;;f()){}");
    fold("for(+f();;g()){}", "for(f();;g()){}");
    fold("for(;;Math.random(f(),g(),h())){}", "for(;;f(),g(),h()){}");

    
    fold("g() && +f()", "g() && f()");
    fold("g() || +f()", "g() || f()");
    fold("x ? g() : +f()", "x ? g() : f()");

    fold("+x()", "x()");
    fold("+x() * 2", "x()");
    fold("-(+x() * 2)", "x()");
    fold("2 -(+x() * 2)", "x()");
    fold("x().foo", "x()");
    foldSame("x().foo()");

    foldSame("x++");
    foldSame("++x");
    foldSame("x--");
    foldSame("--x");
    foldSame("x = 2");
    foldSame("x *= 2");

    
    foldSame("function f() {}");
    foldSame("var x;");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testOptimizeSwitch
  public void testOptimizeSwitch() {
    fold("switch(a){}", "");
    fold("switch(foo()){}", "foo()");
    fold("switch(a){default:}", "");
    fold("switch(a){default:break;}", "");
    fold("switch(a){default:var b;break;}", "var b");
    fold("switch(a){case 1: default:}", "");
    fold("switch(a){default: case 1:}", "");
    fold("switch(a){default: break; case 1:break;}", "");
    fold("switch(a){default: var b; break; case 1: var c; break;}",
        "var c; var b;");

    
    foldSame("function f() {switch(a){default: return; case 1: break;}}");
    foldSame("function f() {switch(a){case 1: foo();}}");
    foldSame("function f() {switch(a){case 3: case 2: case 1: foo();}}");

    fold("function f() {switch(a){case 2: case 1: default: foo();}}",
         "function f() {switch(a){default: foo();}}");
    fold("switch(a){case 1: default:break; case 2: foo()}",
         "switch(a){case 2: foo()}");
    foldSame("switch(a){case 1: goo(); default:break; case 2: foo()}");

    
    foldSame("switch(a){case 1: goo(); case 2:break; case 3: foo()}");

    
    foldSame("switch(a){case 1: var c =2; break;}");
    foldSame("function f() {switch(a){case 1: return;}}");
    foldSame("x:switch(a){case 1: break x;}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveNumber
  public void testRemoveNumber() {
    test("3", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveVarGet1
  public void testRemoveVarGet1() {
    test("a", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveVarGet2
  public void testRemoveVarGet2() {
    test("var a = 1;a", "var a = 1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveNamespaceGet1
  public void testRemoveNamespaceGet1() {
    test("var a = {};a.b", "var a = {}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveNamespaceGet2
  public void testRemoveNamespaceGet2() {
    test("var a = {};a.b=1;a.b", "var a = {};a.b=1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemovePrototypeGet1
  public void testRemovePrototypeGet1() {
    test("var a = {};a.prototype.b", "var a = {}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemovePrototypeGet2
  public void testRemovePrototypeGet2() {
    test("var a = {};a.prototype.b = 1;a.prototype.b",
         "var a = {};a.prototype.b = 1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveAdd1
  public void testRemoveAdd1() {
    test("1 + 2", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveVar1
  public void testNoRemoveVar1() {
    testSame("var a = 1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveVar2
  public void testNoRemoveVar2() {
    testSame("var a = 1, b = 2");
  }
