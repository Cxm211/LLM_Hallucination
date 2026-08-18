// buggy code
    Node processFunctionNode(FunctionNode functionNode) {
      Name name = functionNode.getFunctionName();
      Boolean isUnnamedFunction = false;
      if (name == null) {
        name = new Name();
        name.setIdentifier("");
        isUnnamedFunction = true;
      }
      Node node = newNode(Token.FUNCTION);
      Node newName = transform(name);
      if (isUnnamedFunction) {
        // Old Rhino tagged the empty name node with the line number of the
        // declaration.
        newName.setLineno(functionNode.getLineno());
        // TODO(bowdidge) Mark line number of paren correctly.
        // Same problem as below - the left paren might not be on the
        // same line as the function keyword.
        int lpColumn = functionNode.getAbsolutePosition() +
            functionNode.getLp();
        newName.setCharno(position2charno(lpColumn));
      }

      node.addChildToBack(newName);
      Node lp = newNode(Token.LP);
      // The left paren's complicated because it's not represented by an
      // AstNode, so there's nothing that has the actual line number that it
      // appeared on.  We know the paren has to appear on the same line as the
      // function name (or else a semicolon will be inserted.)  If there's no
      // function name, assume the paren was on the same line as the function.
      // TODO(bowdidge): Mark line number of paren correctly.
      Name fnName = functionNode.getFunctionName();
      if (fnName != null) {
        lp.setLineno(fnName.getLineno());
      } else {
        lp.setLineno(functionNode.getLineno());
      }
      int lparenCharno = functionNode.getLp() +
          functionNode.getAbsolutePosition();

      lp.setCharno(position2charno(lparenCharno));
      for (AstNode param : functionNode.getParams()) {
        lp.addChildToBack(transform(param));
      }
      node.addChildToBack(lp);

      Node bodyNode = transform(functionNode.getBody());
      parseDirectives(bodyNode);
      node.addChildToBack(bodyNode);
     return node;
    }

// relevant test
// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType4
  public void testClassDefiningCallWithStripType4() {
    test("goog.formatter=function(){};" +
         "goog.formatter.inherits(goog.debug.Formatter)",
         null, StripCode.STRIP_TYPE_INHERIT_ERROR);
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType5
  public void testClassDefiningCallWithStripType5() {
    testSame("goog.formatter=function(){};" +
             "goog.formatter.inherits(goog.debug.FormatterFoo)");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType6
  public void testClassDefiningCallWithStripType6() {
    test("goog.formatter=function(){};" +
         "goog.formatter.inherits(goog.debug.Formatter.Foo)",
         null, StripCode.STRIP_TYPE_INHERIT_ERROR);
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType7
  public void testClassDefiningCallWithStripType7() {
    test("goog.inherits(goog.debug.TextFormatter,goog.debug.Formatter)", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType8
  public void testClassDefiningCallWithStripType8() {
    
    test("goog.debug.DebugWindow = function(){}", "");
    test("goog.inherits(goog.debug.DebugWindow,Base)", "");

    
    
    testSame("goog.debug.DebugWindowFoo=function(){}");
    testSame("goog.inherits(goog.debug.DebugWindowFoo,Base)");
    testSame("goog.debug.DebugWindowFoo");
    testSame("goog.debug.DebugWindowFoo=1");

    
    test("goog.debug.DebugWindow.Foo=function(){}", "");
    test("goog.inherits(goog.debug.DebugWindow.Foo,Base)", "");
    test("goog.debug.DebugWindow.Foo", "");
    test("goog.debug.DebugWindow.Foo=1", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPropertyWithEmptyStringKey
  public void testPropertyWithEmptyStringKey() {
    test("goog.format.NUMERIC_SCALES_BINARY_ = {'': 1};",
         "goog.format.NUMERIC_SCALES_BINARY_={\"\":1}");
  }

// com.google.javascript.jscomp.StripCodeTest::testVarinIf
  public void testVarinIf() {
    test("if(x)var logger=null;else foo()", "if(x);else foo()");
  }

// com.google.javascript.jscomp.StripCodeTest::testGetElemInIf
  public void testGetElemInIf() {
    test("var logger=null;if(x)logger[f];else foo()", "if(x);else foo()");
  }

// com.google.javascript.jscomp.StripCodeTest::testAssignInIf
  public void testAssignInIf() {
    test("var logger=null;if(x)logger=1;else foo()",
         "if(x);else foo()");
  }

// com.google.javascript.jscomp.StripCodeTest::testNamePrefix
  public void testNamePrefix() {
    test("a = function(traceZZZ) {}; a.prototype.traceXXX = {x: 1};" +
         "a.prototype.z = function() { this.traceXXX.f(); };" +
         "var traceYYY = 0;",
         "a=function(traceZZZ){};a.prototype.z=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testTypePrefix
  public void testTypePrefix() {
    test("e.f.TraceXXX = function() {}; " +
         "e.f.TraceXXX.prototype.yyy = 2;", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripCallsToStrippedNames
  public void testStripCallsToStrippedNames() {
    test("a = function() { this.logger_ = function(msg){}; };" +
         "a.prototype.b = function() { this.logger_('hi'); }",
         "a=function(){};a.prototype.b=function(){}");
    test("a = function() {};" +
         "a.prototype.logger_ = function(msg) {};" +
         "a.prototype.b = function() { this.logger_('hi'); }",
         "a=function(){};a.prototype.b=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripVarsInitializedFromStrippedNames
  public void testStripVarsInitializedFromStrippedNames() {
    test("a = function() { this.logger_ = function() { return 1; }; };" +
         "a.prototype.b = function() { " +
         "  var one = this.logger_(); if (one) foo() }",
          "a=function(){};a.prototype.b=function(){if(null)foo()}");
  }

// com.google.javascript.jscomp.StripCodeTest::testReportErrorOnStripInNestedAssignment
  public void testReportErrorOnStripInNestedAssignment() {
    
    test("(foo.logger_ = 7) + 8",
         "(foo.logger_ = 7) + 8",
         StripCode.STRIP_ASSIGNMENT_ERROR);

    
    test("(goog.debug.Logger.foo = 7) + 8",
         "(goog.debug.Logger.foo = 7) + 8",
         StripCode.STRIP_ASSIGNMENT_ERROR);

    
    test("(GA_GoogleDebugger.foo = 7) + 8",
         "(GA_GoogleDebugger.foo = 7) + 8",
         StripCode.STRIP_ASSIGNMENT_ERROR);
  }

// com.google.javascript.jscomp.StripCodeTest::testNewOperatior1
  public void testNewOperatior1() {
    test("function foo() {} foo.bar = new goog.debug.Logger();",
         "function foo() {} foo.bar = null;");
  }

// com.google.javascript.jscomp.StripCodeTest::testNewOperatior2
  public void testNewOperatior2() {
    test("function foo() {} foo.bar = (new goog.debug.Logger()).foo();",
         "function foo() {} foo.bar = null;");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting1
  public void testCrazyNesting1() {
    test("var x = {}; x[new goog.debug.Logger()] = 3;",
         "var x = {}; x[null] = 3;");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting2
  public void testCrazyNesting2() {
    test("var x = {}; x[goog.debug.Logger.getLogger()] = 3;",
         "var x = {}; x[null] = 3;");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting3
  public void testCrazyNesting3() {
    test("var x = function() {}; x(new goog.debug.Logger());",
         "var x = function() {}; x(null);");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting4
  public void testCrazyNesting4() {
    test("var x = function() {}; x(goog.debug.Logger.getLogger());",
         "var x = function() {}; x(null);");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting5
  public void testCrazyNesting5() {
    test("var x = function() {}; var y = {}; " +
         "var z = goog.debug.Logger.getLogger(); x(y[z['foo']]);",
         "var x = function() {}; var y = {}; x(y[null]);");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testBadRead
  public void testBadRead() {
    badRead("window.doStuff();");
    badRead("window.Alert('case-sensitive');");
    badRead("function foo(x) { return 'wee' + x.bad }; foo(5);");
    badRead("var p = {x:1, y:2}; alert(p.z);");

    
    
    badRead("window._unknownExportedMethod()");

    
    badRead("var p = {x:1, y:1}; alert(p.y.z.x);");
    badRead("var p = {x:1, y:1}; alert(p.z.y.x);");

    
    badRead("var p = {x:1}; p.bad.x = 2; alert(p.x);");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testBadWrite
  public void testBadWrite() {
    badWrite("function F() { this.x = 1; this.y = 2; } alert((new F()).x);");
    badWrite("var x = {}; x.a = 1; x.b = 2; alert(x.b);");
    badWrite("var p = {x:1}; p.x.y = 2;");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testNoProblem
  public void testNoProblem() {
    
    
    
    
    noProb("function foo(a, b) {" +
           "  a.x = b.y;" +
           "}" +
           "var aa = {};" +
           "var bb = {};" +
           "bb.y = 2;" +
           "foo(aa, bb);" +
           "alert(aa.x);");

    
    noProb("var x = {}; x.f = 'foo'; alert(x.f);");

    
    noProb("function P() { this.x = 0;} alert((new P()).x);");
    noProb("alert((new P()).x); function P() { this.x = 0;}");

    
    noProb("function foo(win) { win.alert('foo') }");

    
    noProb("function Foo(){}" +
           "foo.prototype.baz = function(){ alert(99) };" +
           "var f = new Foo();" +
           "f.baz();");
    noProb("var x = 'apples'; alert(x.indexOf(e));");
    noProb("window.alert(1999)");

    
    noProb("var x = {a:1, b:2}; alert(x.a + x.b);");

    
    noProb("var x = {a:1, b:2}; alert(x.a);");

    
    noProb("var x = {}; x.y = {}; x.y.z = ':-)'; alert(x.y.z);");

    noProb("");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testGet
  public void testGet() {
    badRead("var p = {x:1}; alert(p.y);");
    noProb("var p = {x:1, get y(){}}; alert(p.y);");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testSet
  public void testSet() {
    badRead("var p = {x:1}; alert(p.y);");
    noProb("var p = {x:1, set y(a){}}; alert(p.y);");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testNoWarningForDuckProperty
  public void testNoWarningForDuckProperty() {
    noProb("var x = {}; x.prop; if (x.prop) {}");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testReadPropertySetByGeneratedCode
  public void testReadPropertySetByGeneratedCode() {
    noProb("var o = {}; o[JSCompiler_renameProperty('x')] = 1; o.x;");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testReadPropertyReferencedByGeneratedCode
  public void testReadPropertyReferencedByGeneratedCode() {
    
    noProb("var o = {}; JSCompiler_renameProperty('x'); o.x;");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testSetPropertyReadByGeneratedCode
  public void testSetPropertyReadByGeneratedCode() {
    noProb("var o = {x: 1}; o[JSCompiler_renameProperty('x')];");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testSetPropertyReferencedByGeneratedCode
  public void testSetPropertyReferencedByGeneratedCode() {
    
    noProb("var o = {x: 1}; JSCompiler_renameProperty('x');");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testPropertiesReferencedByGeneratedCode
  public void testPropertiesReferencedByGeneratedCode() {
    
    
    noProb("var o = {x: 1}; JSCompiler_renameProperty('x.y'); o.y;");
  }

// com.google.javascript.jscomp.SuspiciousPropertiesCheckTest::testReadPropertySetByExternObjectLiteral
  public void testReadPropertySetByExternObjectLiteral() {
    noProb("var g = google.gears.workerPool;");
  }

// com.google.javascript.jscomp.SyntacticScopeCreatorTest::testFunctionScope
  public void testFunctionScope() {
    Scope scope = getScope("function foo() {}\n" +
                           "var x = function bar(a1) {};" +
                           "[function bar2() { var y; }];" +
                           "if (true) { function z() {} }"
                           );
    assertTrue(scope.isDeclared("foo", false));
    assertTrue(scope.isDeclared("x", false));
    assertTrue(scope.isDeclared("z", false));

    
    assertFalse(scope.isDeclared("a1", false));
    assertFalse(scope.isDeclared("bar", false));
    assertFalse(scope.isDeclared("bar2", false));
    assertFalse(scope.isDeclared("y", false));
    assertFalse(scope.isDeclared("", false));
  }

// com.google.javascript.jscomp.SyntacticScopeCreatorTest::testScopeRootNode
  public void testScopeRootNode() {
    String js = "function foo() {\n" +
        " var x = 10;" +
        "}";
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());

    Scope globalScope =
        new SyntacticScopeCreator(compiler).createScope(root, null);
    assertEquals(root, globalScope.getRootNode());

    Node fooNode = root.getFirstChild();
    assertEquals(Token.FUNCTION, fooNode.getType());
    Scope fooScope =
        new SyntacticScopeCreator(compiler).createScope(fooNode, null);
    assertEquals(fooNode, fooScope.getRootNode());
    assertTrue(fooScope.isDeclared("x", false));
  }

// com.google.javascript.jscomp.SyntacticScopeCreatorTest::testRedeclaration1
  public void testRedeclaration1() {
     String js = "var a; var a;";
     int errors = createGlobalScopeHelper(js);
     assertEquals(1, errors);
  }

// com.google.javascript.jscomp.SyntacticScopeCreatorTest::testRedeclaration2
  public void testRedeclaration2() {
    String js = "var a;  var a;";
    int errors = createGlobalScopeHelper(js);
    assertEquals(0, errors);
  }

// com.google.javascript.jscomp.SyntacticScopeCreatorTest::testFunctionScopeArguments
  public void testFunctionScopeArguments() {
    
    testScopes("function f() {var arguments}", 0);

    testScopes("var f = function arguments() {}", 1);
    testScopes("var f = function (arguments) {}", 1);
    testScopes("function f() {try {} catch(arguments) {}}", 1);
  }

// com.google.javascript.jscomp.TightenTypesTest::testTopLevelVariables
  public void testTopLevelVariables() {
    testSame(" function Foo() {}\n"
             + "var a = new Foo();\n"
             + "var b = a;\n");

    assertTrue(getType("Foo").isFunction());
    assertTrue(getType("a").isInstance());
    assertType("function (this:Foo): ()", getType("Foo"));
    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));

    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = new Foo();\n"
             + "a = new Bar();\n"
             + "var b = a;\n");

    assertTrue(getType("a").isUnion());
    assertType("(Bar,Foo)", getType("a"));
    assertType("Bar", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testNamespacedVariables
  public void testNamespacedVariables() {
    testSame("var goog = goog || {}; goog.foo = {};\n"
             + " goog.foo.Foo = function() {};\n"
             + "goog.foo.Foo.prototype.blah = function() {};\n"
             + " goog.foo.Bar = function() {};\n"
             + "goog.foo.Bar.prototype.blah = function() {};\n"
             + "function bar(a) { a.blah(); }\n"
             + "var baz = bar;\n"
             + "bar(new goog.foo.Foo);\n"
             + "baz(new goog.foo.Bar);\n");

    assertType("(goog.foo.Bar,goog.foo.Foo)", getParamType(getType("bar"), 0));
    assertType("(goog.foo.Bar,goog.foo.Foo)", getParamType(getType("baz"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testReturnSlot
  public void testReturnSlot() {
    testSame(" function Foo() {}\n"
             + "function bar() {\n"
             + "  var a = new Foo();\n"
             + "  return a;\n"
             + "}\n"
             + "var b = bar();\n");

    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testParameterSlots
  public void testParameterSlots() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "function bar(a, b) {}\n"
             + "bar(new Foo, new Foo);\n"
             + "bar(new Bar, null);\n");

    assertType("(Bar,Foo)", getParamType(getType("bar"), 0));
    assertType("Foo", getParamType(getType("bar"), 1));
    assertNull(getParamVar(getType("bar"), 2));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAliasedFunction
  public void testAliasedFunction() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "function bar(a) {}\n"
             + "var baz = bar;\n"
             + "bar(new Foo);\n"
             + "baz(new Bar);\n");

    assertType("(Bar,Foo)", getParamType(getType("bar"), 0));
    assertType("(Bar,Foo)", getParamType(getType("baz"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCatchStatement
  public void testCatchStatement() {
    testSame(BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
             " function Bar() {}\n"
             + "function bar() { try { } catch (e) { return e; } }\n"
             + " function ID10TError() {}\n"
             + "var a = bar(); throw new ID10TError();\n", null, null);

    assertType("(Error,EvalError,ID10TError,RangeError,ReferenceError,"
        + "SyntaxError,TypeError,URIError)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testConstructorParameterSlots
  public void testConstructorParameterSlots() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + " function Baz(a) {}\n"
             + "new Baz(new Foo);\n"
             + "new Baz(new Bar);\n");

    assertType("(Bar,Foo)", getParamType(getType("Baz"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallSlot
  public void testCallSlot() {
    testSame("function foo() {}\n"
             + "function bar() {}\n"
             + "function baz() {}\n"
             + "var a = foo;\n"
             + "a = bar;\n"
             + "a();\n");

    assertTrue(isCalled(getType("foo")));
    assertTrue(isCalled(getType("bar")));
    assertFalse(isCalled(getType("baz")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testObjectLiteralTraversal
  public void testObjectLiteralTraversal() {
    testSame("var foo = function() {}\n"
             + "function bar() { return { 'a': foo()} };\n"
             + "bar();");
    assertTrue(isCalled(getType("foo")));
   }

// com.google.javascript.jscomp.TightenTypesTest::testThis
  public void testThis() {
    testSame(" function Foo() {}\n"
             + "Foo.prototype.foo = function() { return this; }\n"
             + "var a = new Foo();\n"
             + "var b = a.foo();\n");

    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAssign
  public void testAssign() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = new Foo();\n"
             + "var b = a = new Bar();\n");

    assertType("(Bar,Foo)", getType("a"));
    assertType("Bar", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testComma
  public void testComma() {
    testSame(" function Foo() {b=new Foo()}\n"
             + "var b;"
             + " function Bar() {}\n"
             + "var a = (new Foo, new Bar);\n");

    assertType("Bar", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAnd
  public void testAnd() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = (new Foo && new Bar);\n");

    assertType("Bar", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testOr
  public void testOr() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + " var f = new Foo();\n"
             + " var b = new Bar();\n"
             + "var a = (f || b);\n");

    assertType("(Bar,Foo)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testHook
  public void testHook() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = (1+1 == 2) ? new Foo : new Bar;\n");

    assertType("(Bar,Foo)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testFunctionLiteral
  public void testFunctionLiteral() {
    testSame(" function Foo() {}\n"
             + "var a = (function() { return new Foo; })();\n");

    assertType("Foo", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testNameLookup
  public void testNameLookup() {
    testSame(" function Foo() {}\n"
             + "var a = new Foo;\n"
             + "var b = (function() { return a; })();\n");

    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetProp
  public void testGetProp() {
    testSame(" function Foo() {\n"
             + "  this.foo = new A();\n"
             + "}\n"
             + " function Bar() {\n"
             + "  this.foo = new B();\n"
             + "}\n"
             + " function Baz() {}\n"
             + " function A() {}\n"
             + " function B() {}\n"
             
             + " var foo = new Foo();\n"
             + " var bar = new Bar();\n"
             + " var baz = new Baz();\n" 
             + "var a = foo || bar || baz\n"
             + "var b = a.foo;\n");

    assertType("(A,B)", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetPrototypeProperty
  public void testGetPrototypeProperty() {
    testSame(" function Foo() {};\n"
             + " function Bar() {};\n"
             + "Bar.prototype.a = new Foo();\n"
             + "var a = Bar.prototype.a;\n");

    assertType("Foo", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetElem
  public void testGetElem() {
    testSame(""
             + "function Array(var_args) {}\n",
             " function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = [];\n"
             + "a[0] = new Foo;\n"
             + "a[1] = new Bar;\n"
             + "var b = a[0];\n"
             + "var c = [new Foo, new Bar];\n", null);

    assertType("Array", getType("a"));
    assertType("(Array,Bar,Foo)", getType("b"));
    assertType("Array", getType("c"));

    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + " function Baz() {\n"
             + "  this.arr = [];\n"
             + "}\n"
             + "var b = new Baz;\n"
             + "b.arr[0] = new Foo;\n"
             + "b.arr[1] = new Bar;\n"
             + "var c = b.arr;\n");

    assertType("Array", getType("c"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetElem3
  public void testGetElem3() {
    testSame(BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
             " function Foo() {}\n"
             + " function Bar() {}\n"
             + " function Baz() {\n"
             + "  this.arr = [];\n"
             + "}\n"
             + "function foo(anarr) {"
             + "}\n"
             + "var ar = [];\n"
             + "foo(ar);\n", null);

    assertType("Array", getType("ar"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testScopeDiscovery
  public void testScopeDiscovery() {
    testSame("function spam() {}\n"
             + "function foo() {}\n"
             + "function bar() {\n"
             + "  return function() { foo(); };\n"
             + "}"
             + "function baz() {\n"
             + "  return function() { bar()(); };\n"
             + "}"
             + "baz()()();\n");

    assertFalse(isCalled(getType("spam")));
    assertTrue(isCalled(getType("foo")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testSheqDiscovery
  public void testSheqDiscovery() {
    testSame("function spam() {}\n"
             + "\n"
             + "function Foo() {}\n"
             + "Foo.prototype.foo1 = function() { f1(); }\n"
             + "Foo.prototype.foo2 = function() { f2(); }\n"
             + "Foo.prototype.foo3 = function() { f3(); }\n"
             + "function baz(a) {\n"
             + "  a === null || a instanceof Foo ?\n"
             + "  Foo.prototype.foo1.call(this) :\n"
             + "  Foo.prototype.foo2.call(this);\n"
             + "}\n"
             + "function f1() {}\n"
             + "function f2() {}\n"
             + "function f3() {}\n"
             + "baz(3);\n");

    assertFalse(isCalled(getType("spam")));
    assertFalse(isCalled(getType("f3")));
    assertTrue(isCalled(getType("f1")));
    assertTrue(isCalled(getType("f2")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testSubclass
  public void testSubclass() {
    testSame("\n"
             + "function Foo() {}\n"
             + "Foo.prototype.foo = function() { return this.bar; };\n"
             + "Foo.prototype.bar = function() { return new A(); };\n"
             + "\n"
             + "function Bar() {}\n"
             + "\n"
             + "Bar.prototype.bar = function() { return new B(); };\n"
             + " function A() {}\n"
             + " function B() {}\n"
             + "var a = (new Foo()).foo()();\n"
             + "a = (new Bar()).foo()();\n");

    ConcreteType fooType =
        getPropertyType(getFunctionPrototype(getType("Foo")), "foo");
    assertType("(Bar,Foo)", getThisType(fooType));
    assertType("(A,B)", getType("a"));

    testSame("\n"
             + "function Foo() {}\n"
             + "Foo.prototype.foo = function() { return this.bar; };\n"
             + "Foo.prototype.bar = function() { return new A(); };\n"
             + "\n"
             + "function Bar() {}\n"
             + "\n"
             + "Bar.prototype.bar = function() { return new B(); };\n"
             + " function A() {}\n"
             + " function B() {}\n"
             + "var a = (new Bar()).foo()();\n");

    fooType = getPropertyType(getFunctionPrototype(getType("Foo")), "foo");
    assertType("Bar", getThisType(fooType));
    assertType("B", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testArrayAssignments
  public void testArrayAssignments() {
    testSame(" function Foo() {}\n"
             + "var a = [];\n"
             + "function foo() { return []; }\n"
             + "(a.length == 0 ? a : foo())[0] = new Foo;\n"
             + "var b = a[0];\n"
             + "var c = foo()[0];\n");

    assertType("(Array,Foo)", getType("b"));
    assertType("(Array,Foo)", getType("c"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAllPropertyReference
  public void testAllPropertyReference() {
    testSame(" function Foo() {}\n"
             + "Foo.prototype.prop = function() { this.prop2(); }\n"
             + "Foo.prototype.prop2 = function() { b = new Foo; }\n"
             + "var a = new Foo;\n"
             + "a = [][0];\n"
             + "function fun(a) {\n"
             + "  return a.prop();\n"
             + "}\n"
             + "var b;\n"
             + "fun(a);\n"
             );

    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallFunction
  public void testCallFunction() {
    testSame(" function Foo() { this.a = new A; }\n"
             + " function Bar() {\n"
             + "  Foo.call(this);\n"
             + "}\n"
             + " function A() {};\n"
             + "new Bar;");

    assertTrue(isCalled(getType("Foo")));
    assertTrue(isCalled(getType("A")));
    ConcreteType fooType = getThisType(getType("Foo"));
    assertType("A", getPropertyType(fooType, "a"));

    ConcreteType barType = getThisType(getType("Bar"));
    assertType("A", getPropertyType(barType, "a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallFunctionWithArgs
  public void testCallFunctionWithArgs() {
    testSame(" function Foo(o) { this.a = o; }\n"
             + " function Bar() {\n"
             + "  Foo.call(this, new A());\n"
             + "}\n"
             + " function A() {};\n"
             + "var b = new Bar;");

    assertTrue(isCalled(getType("Foo")));
    assertTrue(isCalled(getType("A")));

    ConcreteType barType = getThisType(getType("Bar"));
    assertType("A", getPropertyType(barType, "a"));

    ConcreteType fooType = getThisType(getType("Foo"));
    assertType("A", getPropertyType(fooType, "a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallPrototypeFunction
  public void testCallPrototypeFunction() {
    testSame(" function Foo() {}\n"
             + "Foo.prototype.a = function() { return new A; }\n"
             + "Foo.prototype.a = function() { return new A; };\n"
             + " function Bar() {}\n"
             + ""
             + "Bar.prototype.a = function() { return new B; };\n"
             + " function A() {};\n"
             + " function B() {};\n"
             + "var ret = Foo.prototype.a.call(new Bar);");

    assertType("A", getType("ret"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallPrototypeFunctionWithArgs
  public void testCallPrototypeFunctionWithArgs() {
    testSame(" function Foo() { this.p = null }\n"
             + "Foo.prototype.set = function(arg) { this.p = arg; };\n"
             + "Foo.prototype.get = function() { return this.p; };\n"
             + " function A() {};\n"
             + "Foo.prototype.set.call(new Foo, new A);\n"
             + "var ret = Foo.prototype.get.call(new Foo);");

    ConcreteType fooP = getFunctionPrototype(getType("Foo"));
    ConcreteFunctionType gFun = getPropertyType(fooP, "get").toFunction();
    ConcreteFunctionType sFun = getPropertyType(fooP, "set").toFunction();

    assertTrue(isCalled(sFun));
    assertTrue(isCalled(gFun));
    assertTrue(isCalled(getType("A")));
    assertType("A", getType("ret"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testSetTimeout
  public void testSetTimeout() {
    testSame(" function Window() {};\n"
             + "Window.prototype.setTimeout = function(f, t) {};\n"
             + " var window;",
             " function A() {}\n"
             + "A.prototype.handle = function() { foo(); };\n"
             + "function foo() {}\n"
             + "window.setTimeout((new A).handle, 3);", null);

    assertTrue(isCalled(getType("foo")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testExternType
  public void testExternType() {
    testSame(" function T() {};\n"
             + " function Ext() {};\n"
             + "\n"
             + "Ext.prototype.getT = function() {};\n"
             + " Ext.prototype.prop;\n"
             + " var ext;",
             "var b = ext.getT();\n"
             + "var p = ext.prop;", null);

    assertType("Ext", getType("ext"));
    assertType("T", getType("b"));
    assertType("T", getType("p"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testExternSubTypes
  public void testExternSubTypes() {
    testSame(" function A() {};\n"
             + " function B() {};\n"
             + " function C() {};\n"
             + " function D() {};\n"
             + " function Ext() {};\n"
             + " Ext.prototype.a;\n"
             + " Ext.prototype.b;\n"
             + " Ext.prototype.d;\n"
             + " Ext.prototype.getA = function() {};\n"
             + " Ext.prototype.getB = function() {};\n",
             "var a = (new Ext).a;\n"
             + "var a2 = (new Ext).getA();\n"
             + "var b = (new Ext).b;\n"
             + "var b2 = (new Ext).getB();\n"
             + "var d = (new Ext).d;\n", null);

    assertType("(A,B,C,D)", getType("a"));
    assertType("(A,B,C,D)", getType("a2"));
    assertType("(B,D)", getType("b"));
    assertType("(B,D)", getType("b2"));
    assertType("D", getType("d"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testExternSubTypesForObject
  public void testExternSubTypesForObject() {
    testSame(BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES
             + " function A() {};\n"
             + " function B() {};\n"
             + " "
             + "Object.prototype.eval = function(code) {};\n"
             + "\n"
             + "A.prototype.a;\n"
             + "\n"
             + "A.prototype.b = function(){};\n",
             "var a = (new A).b()", null, null);
    assertType("(A,ActiveXObject,Array,B,Boolean,Date,Error,EvalError,"
               + "Function,Number,Object,"
               + "RangeError,ReferenceError,RegExp,String,SyntaxError,"
               + "TypeError,URIError)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitPropCall
  public void testImplicitPropCall() {
    testSame(" function Window() {};\n"
             + "\n"
             + "Window.prototype.setTimeout = function(f, d) {};",
             "function foo() {};\n"
             + "(new Window).setTimeout(foo, 20);", null);

    assertTrue(isCalled(getType("foo")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitPropCallWithArgs
  public void testImplicitPropCallWithArgs() {
    testSame(" function Window() {};\n"
             + " function EventListener() {};\n"
             + "\n"
             + "Window.prototype.addEventListener = function(t, f) {};\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "(new Window).addEventListener('click', foo);", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testUntypedImplicitCallFromProperty
  public void testUntypedImplicitCallFromProperty() {
    testSame(" function Element() {};\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};"
             + " Event.prototype.erv;",
             " function foo(evt) { return bar(evt); };\n"
             + "function bar(a) { return a.type() }\n"
             + " var ar = new Element;\n"
             + "ar.onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertTrue(isCalled(getType("bar")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Event", getParamType(getType("bar"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitCallFromProperty
  public void testImplicitCallFromProperty() {
    testSame(" function Element() {};\n"
             + "\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "(new Element).onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitCallFromPropertyOfUnion
  public void testImplicitCallFromPropertyOfUnion() {
    testSame(" function Element() {};\n"
             + "\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "(new Element).onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitCallFromPropertyOfAllType
  public void testImplicitCallFromPropertyOfAllType() {
    testSame(" function Element() {};\n"
             + "\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "var elems = [];\n"
             + "var elem = elems[0];\n" 
             + "elem.onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testRestrictToCast
  public void testRestrictToCast() {
    testSame(" function Foo() {};\n"
             + "var a = [];\n"
             + "var foo = ( a[0]);\n"
             + "var u = a[0];\n"
             + "new Foo");

    assertType("Foo", getType("foo"));
    assertType("(Array,Foo)", getType("u"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testRestrictToInterfaceCast
  public void testRestrictToInterfaceCast() {
    testSame(" function Foo() {};\n"
             + " function Int() {};\n"
             + "var a = [];\n"
             + "var foo = ( a[0]);\n"
             + "new Foo");

    assertType("Foo", getType("foo"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testRestrictToCastWithNonInstantiatedTypes
  public void testRestrictToCastWithNonInstantiatedTypes() {
    testSame(
             " function Super() {}\n"
             + " function Foo() {};\n"
             + "Foo.prototype.blah = function() { foofunc() };\n"
             + " function Bar() {};\n"
             + "Bar.prototype.blah = function() { barfunc() };\n"
             + "function barfunc() {}\n"
             + "function foofunc() {}\n"
             + "var a = [];\n"
             + "var u =  (a[0]);\n"
             + "u.blah()\n"
             + "new Foo");

    assertTrue(isCalled(getType("foofunc")));
    assertFalse(isCalled(getType("barfunc")));
    assertType("Array", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testFunctionToString
  public void testFunctionToString() {
    testSame(" function Foo() {}\n"
             + "\n"
             + "function Bar() { Foo.call(this); }\n"
             + "var a = function(a) { return new Foo; };\n;"
             + "a(new Foo);\n"
             + "a(new Bar);\n"
             + "new Bar;");

    assertType("function ((Bar,Foo)): Foo", getType("a"));
    assertType("function (this:(Bar,Foo)): ()", getType("Foo"));
    assertType("function (this:Bar): ()", getType("Bar"));
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionAritySimple
  public void testFunctionAritySimple() {
    assertOk("", "");
    assertOk("a", "'a'");
    assertOk("a,b", "10, 20");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionArityWithOptionalArgs
  public void testFunctionArityWithOptionalArgs() {
    assertOk("a,b,opt_c", "1,2");
    assertOk("a,b,opt_c", "1,2,3");
    assertOk("a,opt_b,opt_c", "1");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionArityWithVarArgs
  public void testFunctionArityWithVarArgs() {
    assertOk("var_args", "");
    assertOk("var_args", "1,2");
    assertOk("a,b,var_args", "1,2");
    assertOk("a,b,var_args", "1,2,3");
    assertOk("a,b,var_args", "1,2,3,4,5");
    assertOk("a,opt_b,var_args", "1");
    assertOk("a,opt_b,var_args", "1,2");
    assertOk("a,opt_b,var_args", "1,2,3");
    assertOk("a,opt_b,var_args", "1,2,3,4,5");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testWrongNumberOfArgs
  public void testWrongNumberOfArgs() {
    assertWarning("a,b,opt_c", "1",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,opt_c", "1,2,3,4",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b", "1, 2, 3",
        WRONG_ARGUMENT_COUNT);
    assertWarning("", "1, 2, 3",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,c,d", "1, 2, 3",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,var_args", "1",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,opt_c,var_args", "1",
        WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testVarArgsLast
  public void testVarArgsLast() {
    assertWarning("a,b,var_args,c", "1,2,3,4",
        VAR_ARGS_MUST_BE_LAST);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testOptArgsLast
  public void testOptArgsLast() {
    assertWarning("a,b,opt_d,c", "1, 2, 3",
        OPTIONAL_ARG_AT_END);
    assertWarning("a,b,opt_d,c", "1, 2",
        OPTIONAL_ARG_AT_END);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc1
  public void testFunctionsWithJsDoc1() {
    testSame(" function foo(a,b,c) {} foo(1,2);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc2
  public void testFunctionsWithJsDoc2() {
    testSame(" function foo(a,b,c) {} foo(1,2,3);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc3
  public void testFunctionsWithJsDoc3() {
    testSame(" " +
             "function foo(a,b,c) {} foo(1);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc4
  public void testFunctionsWithJsDoc4() {
    testSame(" var foo = function(a) {}; foo();");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc5
  public void testFunctionsWithJsDoc5() {
    testSame(" var foo = function(a) {}; foo(1,2);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc6
  public void testFunctionsWithJsDoc6() {
    testSame(" var foo = function(a, b) {}; foo();",
             WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc7
  public void testFunctionsWithJsDoc7() {
    String fooDfn = " var foo = function(b) {};";
    testSame(fooDfn + "foo();");
    testSame(fooDfn + "foo(1);");
    testSame(fooDfn + "foo(1, 2);", WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionWithDefaultCodingConvention
  public void testFunctionWithDefaultCodingConvention() {
    convention = new DefaultCodingConvention();
    testSame("var foo = function(x) {}; foo(1, 2);");
    testSame("var foo = function(opt_x) {}; foo(1, 2);");
    testSame("var foo = function(var_args) {}; foo(1, 2);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testMethodCalls
  public void testMethodCalls() {
    final String METHOD_DEFS =
      "\n" +
      "function Foo() {}" +
      
      "function twoArg(arg1, arg2) {};" +
      "Foo.prototype.prototypeMethod = twoArg;" +
      "Foo.staticMethod = twoArg;";
    
    
    testSame(METHOD_DEFS +
        "var f = new Foo();f.prototypeMethod(1, 2, 3);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
    
    testSame(METHOD_DEFS +
        "var f = new Foo();f.prototypeMethod(1);",
        TypeCheck.WRONG_ARGUMENT_COUNT);

    
    testSame(METHOD_DEFS +
        "Foo.staticMethod(1, 2, 3);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
    
    testSame(METHOD_DEFS +
        "Foo.staticMethod(1);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckTest::testInitialTypingScope
  public void testInitialTypingScope() {
    Scope s = new TypedScopeCreator(compiler,
        new DefaultCodingConvention()).createInitialScope(
            new Node(Token.BLOCK));

    assertEquals(ARRAY_FUNCTION_TYPE, s.getVar("Array").getType());
    assertEquals(BOOLEAN_OBJECT_FUNCTION_TYPE,
        s.getVar("Boolean").getType());
    assertEquals(DATE_FUNCTION_TYPE, s.getVar("Date").getType());
    assertEquals(ERROR_FUNCTION_TYPE, s.getVar("Error").getType());
    assertEquals(EVAL_ERROR_FUNCTION_TYPE,
        s.getVar("EvalError").getType());
    assertEquals(NUMBER_OBJECT_FUNCTION_TYPE,
        s.getVar("Number").getType());
    assertEquals(OBJECT_FUNCTION_TYPE, s.getVar("Object").getType());
    assertEquals(RANGE_ERROR_FUNCTION_TYPE,
        s.getVar("RangeError").getType());
    assertEquals(REFERENCE_ERROR_FUNCTION_TYPE,
        s.getVar("ReferenceError").getType());
    assertEquals(REGEXP_FUNCTION_TYPE, s.getVar("RegExp").getType());
    assertEquals(STRING_OBJECT_FUNCTION_TYPE,
        s.getVar("String").getType());
    assertEquals(SYNTAX_ERROR_FUNCTION_TYPE,
        s.getVar("SyntaxError").getType());
    assertEquals(TYPE_ERROR_FUNCTION_TYPE,
        s.getVar("TypeError").getType());
    assertEquals(URI_ERROR_FUNCTION_TYPE,
        s.getVar("URIError").getType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck1
  public void testTypeCheck1() throws Exception {
    testTypes("function foo(){ if (foo()) return; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck2
  public void testTypeCheck2() throws Exception {
    testTypes("function foo(){ var x=foo(); x--; }",
        "increment/decrement\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck4
  public void testTypeCheck4() throws Exception {
    testTypes("function foo(){ !foo(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck5
  public void testTypeCheck5() throws Exception {
    testTypes("function foo(){ var a = +foo(); }",
        "sign operator\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck6
  public void testTypeCheck6() throws Exception {
    testTypes(
        "function foo(){" +
        "var a;if (a == foo())return;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck7
  public void testTypeCheck7() throws Exception {
    testTypes("function foo() {delete 'abc';}",
        TypeCheck.BAD_DELETE);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck8
  public void testTypeCheck8() throws Exception {
    testTypes("function foo(){do {} while (foo());}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck9
  public void testTypeCheck9() throws Exception {
    testTypes("function foo(){while (foo());}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck10
  public void testTypeCheck10() throws Exception {
    testTypes("function foo(){for (;foo(););}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck11
  public void testTypeCheck11() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a = b;",
        "assignment\n" +
        "found   : String\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck12
  public void testTypeCheck12() throws Exception {
    testTypes("function foo(){var a = 3^foo();}",
        "bad right operand to bitwise operator\n" +
        "found   : Object\n" +
        "required: (boolean|null|number|string|undefined)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck13
  public void testTypeCheck13() throws Exception {
    testTypes("var i; i=/xx/;",
        "assignment\n" +
        "found   : RegExp\n" +
        "required: (Number|String)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck14
  public void testTypeCheck14() throws Exception {
    testTypes("function foo(opt_a){}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck15
  public void testTypeCheck15() throws Exception {
    testTypes("var x;x=null;x=10;",
        "assignment\n" +
        "found   : number\n" +
        "required: (Number|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck16
  public void testTypeCheck16() throws Exception {
    testTypes("var x='';",
              "initializing variable\n" +
              "found   : string\n" +
              "required: (Number|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck17
  public void testTypeCheck17() throws Exception {
    testTypes("\n" +
        "function a(opt_foo){\nreturn (opt_foo);\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck18
  public void testTypeCheck18() throws Exception {
    testTypes("\n function a(){return new RegExp();}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck19
  public void testTypeCheck19() throws Exception {
    testTypes("\n function a(){return new Array();}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck20
  public void testTypeCheck20() throws Exception {
    testTypes("\n function a(){return new Date();}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheckBasicDowncast
  public void testTypeCheckBasicDowncast() throws Exception {
    testTypes("function foo() {}\n" +
                  " var bar = new foo();\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheckNoDowncastToNumber
  public void testTypeCheckNoDowncastToNumber() throws Exception {
    testTypes("function foo() {}\n" +
                  " var bar = new foo();\n",
        "initializing variable\n" +
        "found   : foo\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck21
  public void testTypeCheck21() throws Exception {
    testTypes("var foo;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck22
  public void testTypeCheck22() throws Exception {
    testTypes("\nfunction foo(p){}\n" +
                  "function Element(){}\n" +
                  "var v;\n" +
                  "foo(v);\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck23
  public void testTypeCheck23() throws Exception {
    testTypes("var foo; foo = null;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck24
  public void testTypeCheck24() throws Exception {
    testTypes("function MyType(){}\n" +
        "var foo; foo = null;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheckDefaultExterns
  public void testTypeCheckDefaultExterns() throws Exception {
    testTypes(" function f(x) {}" +
        "f([].length);" ,
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheckCustomExterns
  public void testTypeCheckCustomExterns() throws Exception {
    testTypes(
        DEFAULT_EXTERNS + " Array.prototype.oogabooga;",
        " function f(x) {}" +
        "f([].oogabooga);" ,
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: string", false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testParameterizedArray1
  public void testParameterizedArray1() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testParameterizedArray2
  public void testParameterizedArray2() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : Array\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testParameterizedArray3
  public void testParameterizedArray3() throws Exception {
    testTypes(" var f = function(a) { a[1] = 0; return a[0]; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testParameterizedArray4
  public void testParameterizedArray4() throws Exception {
    testTypes(" var f = function(a) { a[0] = 'a'; };",
        "assignment\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testParameterizedArray5
  public void testParameterizedArray5() throws Exception {
    testTypes(" var f = function(a) { a[0] = 'a'; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testParameterizedArray6
  public void testParameterizedArray6() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : *\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testParameterizedArray7
  public void testParameterizedArray7() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testParameterizedObject1
  public void testParameterizedObject1() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testParameterizedObject2
  public void testParameterizedObject2() throws Exception {
    testTypes(" var f = function(a) { return a['x']; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testParameterizedObject3
  public void testParameterizedObject3() throws Exception {
    testTypes(" var f = function(a) { return a['x']; };",
        "restricted index type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testParameterizedObject4
  public void testParameterizedObject4() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " var f = function(a) { return a['x']; };",
        "restricted index type\n" +
        "found   : string\n" +
        "required: E.<string>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUnionOfFunctionAndType
  public void testUnionOfFunctionAndType() throws Exception {
    testTypes(" var a;" +
        " var b = null; a = b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalParameterComparedToUndefined
  public void testOptionalParameterComparedToUndefined() throws Exception {
    testTypes("function foo(opt_a)" +
        "{if (opt_a==undefined) var b = 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalAllType
  public void testOptionalAllType() throws Exception {
    testTypes("function f(opt_x) { return opt_x }\n" +
        "var y;\n" +
        "f(y);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalUnknownNamedType
  public void testOptionalUnknownNamedType() throws Exception {
    testTypes("\n" +
        "function f(opt_x) { return opt_x; }\n" +
        "var T = function() {};",
        "inconsistent return type\n" +
        "found   : (T|undefined)\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalArgFunctionParam
  public void testOptionalArgFunctionParam() throws Exception {
    testTypes("" +
        "function f(a) {a()};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalArgFunctionParam2
  public void testOptionalArgFunctionParam2() throws Exception {
    testTypes("" +
        "function f(a) {a(3)};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalArgFunctionParam3
  public void testOptionalArgFunctionParam3() throws Exception {
    testTypes("" +
        "function f(a) {a(undefined)};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalArgFunctionParam4
  public void testOptionalArgFunctionParam4() throws Exception {
    String expectedWarning = "Function a: called with 2 argument(s). " +
        "Function requires at least 0 argument(s) and no more than 1 " +
        "argument(s).";

    testTypes("function f(a) {a(3,4)};",
              expectedWarning, false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalArgFunctionParamError
  public void testOptionalArgFunctionParamError() throws Exception {
    String expectedWarning = "Parse error. variable length argument must be " +
        "last";
    testTypes("" +
              "function f(a) {};", expectedWarning, false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalNullableArgFunctionParam
  public void testOptionalNullableArgFunctionParam() throws Exception {
    testTypes("" +
              "function f(a) {a()};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalNullableArgFunctionParam2
  public void testOptionalNullableArgFunctionParam2() throws Exception {
    testTypes("" +
              "function f(a) {a(null)};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalNullableArgFunctionParam3
  public void testOptionalNullableArgFunctionParam3() throws Exception {
    testTypes("" +
              "function f(a) {a(3)};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalArgFunctionReturn
  public void testOptionalArgFunctionReturn() throws Exception {
    testTypes("" +
              "function f() { return function(opt_x) { }; };" +
              "f()()");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalArgFunctionReturn2
  public void testOptionalArgFunctionReturn2() throws Exception {
    testTypes("" +
              "function f() { return function(opt_x) { }; };" +
              "f()({})");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanType
  public void testBooleanType() throws Exception {
    testTypes("var x = 1 < 2;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanReduction1
  public void testBooleanReduction1() throws Exception {
    testTypes("var x; x = null || \"a\";");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanReduction2
  public void testBooleanReduction2() throws Exception {
    
    
    testTypes("" +
        "(function(s) { return ((s == 'a') && s) || 'b'; })");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanReduction3
  public void testBooleanReduction3() throws Exception {
    testTypes("" +
        "(function(s) { return s && null && 3; })");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanReduction4
  public void testBooleanReduction4() throws Exception {
    testTypes("" +
        "(function(x) { return null || x || null ; })");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanReduction5
  public void testBooleanReduction5() throws Exception {
    testTypes("\n" +
        "var f = function(x) {\n" +
        "if (!x || typeof x == 'string') {\n" +
        "return x;\n" +
        "}\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanReduction6
  public void testBooleanReduction6() throws Exception {
    testTypes("\n" +
        "var f = function(x) {\n" +
        "if (!(x && typeof x != 'string')) {\n" +
        "return x;\n" +
        "}\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanReduction7
   public void testBooleanReduction7() throws Exception {
    testTypes("var T = function() {};\n" +
        "\n" +
        "var f = function(x) {\n" +
        "if (!x) {\n" +
        "return x;\n" +
        "}\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNullAnd
  public void testNullAnd() throws Exception {
    testTypes("var x;\n" +
        "var r = x && x;",
        "initializing variable\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNullOr
  public void testNullOr() throws Exception {
    testTypes("var x;\n" +
        "var r = x || x;",
        "initializing variable\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanPreservation1
  public void testBooleanPreservation1() throws Exception {
    testTypes("var x = \"a\";" +
        "x = ((x == \"a\") && x) || x == \"b\";",
        "assignment\n" +
        "found   : (boolean|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanPreservation2
  public void testBooleanPreservation2() throws Exception {
    testTypes("var x = \"a\"; x = (x == \"a\") || x;",
        "assignment\n" +
        "found   : (boolean|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanPreservation3
  public void testBooleanPreservation3() throws Exception {
    testTypes("" +
        "function f(x) { return x && x == \"a\"; }",
        "condition always evaluates to false\n" +
        "left : Function\n" +
        "right: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanPreservation4
  public void testBooleanPreservation4() throws Exception {
    testTypes("" +
        "function f(x) { return x && x == \"a\"; }",
        "inconsistent return type\n" +
        "found   : (boolean|null)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction1
  public void testTypeOfReduction1() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'number' ? String(x) : x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction2
  public void testTypeOfReduction2() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x != 'string' ? String(x) : x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction3
  public void testTypeOfReduction3() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'object' ? 1 : x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction4
  public void testTypeOfReduction4() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'undefined' ? {} : x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction5
  public void testTypeOfReduction5() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return typeof x != 'number' ? x : 'a'; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction6
  public void testTypeOfReduction6() throws Exception {
    testTypes("\n" +
        "function f(x) {\n" +
        "return typeof x == 'string' && x.length == 3 ? x : 'a';\n" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction7
  public void testTypeOfReduction7() throws Exception {
    testTypes("var f = function(x) { " +
        "return typeof x == 'number' ? x : 'a'; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction8
  public void testTypeOfReduction8() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isString(x) && x.length == 3 ? x : 'a';\n" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction9
  public void testTypeOfReduction9() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isArray(x) ? 'a' : x;\n" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction10
  public void testTypeOfReduction10() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isArray(x) ? x : [];\n" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction11
  public void testTypeOfReduction11() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isObject(x) ? x : [];\n" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction12
  public void testTypeOfReduction12() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return typeof x == 'object' ? x : []; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction13
  public void testTypeOfReduction13() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        " var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return goog.isObject(x) ? x : []; }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction14
  public void testTypeOfReduction14() throws Exception {
    
    testClosureTypes(
        CLOSURE_DEFS +
        "function f(arguments) { " +
        "  return goog.isString(arguments[0]) ? arguments[0] : 0;" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction15
  public void testTypeOfReduction15() throws Exception {
    
    testClosureTypes(
        CLOSURE_DEFS +
        "function f(arguments) { " +
        "  return typeof arguments[0] == 'string' ? arguments[0] : 0;" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction1
  public void testQualifiedNameReduction1() throws Exception {
    testTypes("var x = {};  x.a = 'a';\n" +
        " var f = function() {\n" +
        "return x.a ? x.a : 'a'; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction2
  public void testQualifiedNameReduction2() throws Exception {
    testTypes(" var T = " +
        "function(a) {this.a = a};\n" +
        " T.prototype.f = function() {\n" +
        "return this.a ? this.a : 'a'; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction3
  public void testQualifiedNameReduction3() throws Exception {
    testTypes(" var T = " +
        "function(a) {this.a = a};\n" +
        " T.prototype.f = function() {\n" +
        "return typeof this.a == 'string' ? this.a : 'a'; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction4
  public void testQualifiedNameReduction4() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        " var T = " +
        "function(a) {this.a = a};\n" +
        " T.prototype.f = function() {\n" +
        "return goog.isString(this.a) ? this.a : 'a'; }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction5a
  public void testQualifiedNameReduction5a() throws Exception {
    testTypes("var x = { a:'b' };\n" +
        " var f = function() {\n" +
        "return x.a; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction5b
  public void testQualifiedNameReduction5b() throws Exception {
    testTypes(
        "var x = { a:12 };\n" +
        "\n" +
        "var f = function() {\n" +
        "  return x.a;\n" +
        "}",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction5c
  public void testQualifiedNameReduction5c() throws Exception {
    testTypes(
        " var f = function() {\n" +
        "var x = { a:0 };\n" +
        "return (x.a) ? (x.a) : 'a'; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction6
  public void testQualifiedNameReduction6() throws Exception {
    testTypes(
        " var f = function() {\n" +
        "var x = { get a() {return 'a'}};\n" +
        "return x.a ? x.a : 'a'; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction7
  public void testQualifiedNameReduction7() throws Exception {
    testTypes(
        " var f = function() {\n" +
        "var x = { get a() {return 12}};\n" +
        "return x.a; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction7a
  public void testQualifiedNameReduction7a() throws Exception {
    
    testTypes(
        " var f = function() {\n" +
        "var x = {get a() {return 12}};\n" +
        "return x.a; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction8
  public void testQualifiedNameReduction8() throws Exception {
    testTypes(
        " var f = function() {\n" +
        "var x = {get a() {return 'a'}};\n" +
        "return x.a ? x.a : 'a'; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction9
  public void testQualifiedNameReduction9() throws Exception {
    testTypes(
        " var f = function() {\n" +
        "var x = {  set a(b) {}};\n" +
        "return x.a ? x.a : 'a'; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction10
  public void testQualifiedNameReduction10() throws Exception {
    
    
    testTypes(
        " var f = function() {\n" +
        "var x = {  set a(b) {}};\n" +
        "return x.a ? x.a : 'a'; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef1a
  public void testObjLitDef1a() throws Exception {
    testTypes(
        "var x = { a:12 };\n" +
        "x.a = 'a';",
        "assignment to property a of x\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef1b
  public void testObjLitDef1b() throws Exception {
    testTypes(
        "function f(){" +
          "var x = { a:12 };\n" +
          "x.a = 'a';" +
        "};\n" +
        "f();",
        "assignment to property a of x\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef2a
  public void testObjLitDef2a() throws Exception {
    testTypes(
        "var x = { set a(b){} };\n" +
        "x.a = 'a';",
        "assignment to property a of x\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef2b
  public void testObjLitDef2b() throws Exception {
    testTypes(
        "function f(){" +
          "var x = { set a(b){} };\n" +
          "x.a = 'a';" +
        "};\n" +
        "f();",
        "assignment to property a of x\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef3a
  public void testObjLitDef3a() throws Exception {
    testTypes(
        " var y;\n" +
        "var x = { get a(){} };\n" +
        "y = x.a;",
        "assignment\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef3b
  public void testObjLitDef3b() throws Exception {
    testTypes(
      " var y;\n" +
        "function f(){" +
          "var x = { get a(){} };\n" +
          "y = x.a;" +
        "};\n" +
        "f();",
        "assignment\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef4
  public void testObjLitDef4() throws Exception {
    testTypes(
        "var x = {" +
          " a:12 };\n",
          "assignment to property a of {a: function (): number}\n" +
          "found   : number\n" +
          "required: function (): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef5
  public void testObjLitDef5() throws Exception {
    testTypes(
        "var x = {};\n" +
        " x.a = 12;\n",
        "assignment to property a of x\n" +
        "found   : number\n" +
        "required: function (): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceOfReduction1
  public void testInstanceOfReduction1() throws Exception {
    testTypes(" var T = function() {};\n" +
        "\n" +
        "var f = function(x) {\n" +
        "if (x instanceof T) { return x; } else { return new T(); }\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceOfReduction2
  public void testInstanceOfReduction2() throws Exception {
    testTypes(" var T = function() {};\n" +
        "\n" +
        "var f = function(x) {\n" +
        "if (x instanceof T) { return ''; } else { return x; }\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInferredPropagation
  public void testPropertyInferredPropagation() throws Exception {
    testTypes("function f() { return {}; }\n" +
         "function g() { var x = f(); if (x.p) x.a = 'a'; else x.a = 'b'; }\n" +
         "function h() { var x = f(); x.a = false; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference1
  public void testPropertyInference1() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference2
  public void testPropertyInference2() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "F.prototype.baz = function() { this.x_ = null; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference3
  public void testPropertyInference3() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "F.prototype.baz = function() { this.x_ = 3; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : (boolean|number)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference4
  public void testPropertyInference4() throws Exception {
    testTypes(
        " function F() { }" +
        "F.prototype.x_ = 3;" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference5
  public void testPropertyInference5() throws Exception {
    testTypes(
        " function F() { }" +
        "F.prototype.baz = function() { this.x_ = 3; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference6
  public void testPropertyInference6() throws Exception {
    testTypes(
        " function F() { }" +
        "(new F).x_ = 3;" +
        "" +
        "F.prototype.bar = function() { return this.x_; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference7
  public void testPropertyInference7() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "(new F).x_ = 3;" +
        "" +
        "F.prototype.bar = function() { return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference8
  public void testPropertyInference8() throws Exception {
    testTypes(
        " function F() { " +
        "   this.x_ = 'x';" +
        "}" +
        "(new F).x_ = 3;" +
        "" +
        "F.prototype.bar = function() { return this.x_; };",
        "assignment to property x_ of F\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoPersistentTypeInferenceForObjectProperties
  public void testNoPersistentTypeInferenceForObjectProperties()
      throws Exception {
    testTypes("\n" +
        "function s1(o,x) { o.x = x; }\n" +
        "\n" +
        "function g1(o) { return typeof o.x == 'undefined' ? '' : o.x; }\n" +
        "\n" +
        "function s2(o,x) { o.x = x; }\n" +
        "\n" +
        "function g2(o) { return typeof o.x == 'undefined' ? 0 : o.x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoPersistentTypeInferenceForFunctionProperties
  public void testNoPersistentTypeInferenceForFunctionProperties()
      throws Exception {
    testTypes("\n" +
        "function s1(o,x) { o.x = x; }\n" +
        "\n" +
        "function g1(o) { return typeof o.x == 'undefined' ? '' : o.x; }\n" +
        "\n" +
        "function s2(o,x) { o.x = x; }\n" +
        "\n" +
        "function g2(o) { return typeof o.x == 'undefined' ? 0 : o.x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectPropertyTypeInferredInLocalScope1
  public void testObjectPropertyTypeInferredInLocalScope1() throws Exception {
    testTypes("\n" +
        "function f(o) { o.x = 1; return o.x; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectPropertyTypeInferredInLocalScope2
  public void testObjectPropertyTypeInferredInLocalScope2() throws Exception {
    testTypes("" +
        "function f(o, x) { o.x = 'a';\nif (x) {o.x = x;}\nreturn o.x; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectPropertyTypeInferredInLocalScope3
  public void testObjectPropertyTypeInferredInLocalScope3() throws Exception {
    testTypes("" +
        "function f(o, x) { if (x) {o.x = x;} else {o.x = 'a';}\nreturn o.x; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty1
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty1()
      throws Exception {
    testTypes("var T = function() { this.x = ''; };\n" +
        " T.prototype.x = 0;",
        "assignment to property x of T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty2
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty2()
      throws Exception {
    testTypes("var T = function() { this.x = ''; };\n" +
        " T.prototype.x;",
        "assignment to property x of T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty3
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty3()
      throws Exception {
    testTypes(" var n = {};\n" +
        " n.T = function() { this.x = ''; };\n" +
        " n.T.prototype.x = 0;",
        "assignment to property x of n.T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty4
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty4()
      throws Exception {
    testTypes("var n = {};\n" +
        " n.T = function() { this.x = ''; };\n" +
        " n.T.prototype.x = 0;",
        "assignment to property x of n.T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyUsedBeforeDefinition1
  public void testPropertyUsedBeforeDefinition1() throws Exception {
    testTypes(" var T = function() {};\n" +
        "" +
        "T.prototype.f = function() { return this.g(); };\n" +
        " T.prototype.g = function() { return 1; };\n",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyUsedBeforeDefinition2
  public void testPropertyUsedBeforeDefinition2() throws Exception {
    testTypes("var n = {};\n" +
        " n.T = function() {};\n" +
        "" +
        "n.T.prototype.f = function() { return this.g(); };\n" +
        " n.T.prototype.g = function() { return 1; };\n",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd1
  public void testAdd1() throws Exception {
    testTypes("function foo(){var a = 'abc'+foo();}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd2
  public void testAdd2() throws Exception {
    testTypes("function foo(){var a = foo()+4;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd3
  public void testAdd3() throws Exception {
    testTypes(" var a = 'a';" +
        " var b = 'b';" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd4
  public void testAdd4() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 'b';" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd5
  public void testAdd5() throws Exception {
    testTypes(" var a = 'a';" +
        " var b = 5;" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd6
  public void testAdd6() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 5;" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd7
  public void testAdd7() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 'b';" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd8
  public void testAdd8() throws Exception {
    testTypes(" var a = 'a';" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd9
  public void testAdd9() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd10
  public void testAdd10() throws Exception {
    
    testTypes(
        suppressMissingProperty("e", "f") +
        " var a = 5;" +
        " var c = a + d.e.f;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd11
  public void testAdd11() throws Exception {
    
    testTypes(
        suppressMissingProperty("e", "f") +
        " var a = 5;" +
        " var c = a + d.e.f;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd12
  public void testAdd12() throws Exception {
    testTypes(" function a() { return 5; }" +
        " var b = 5;" +
        " var c = a() + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd13
  public void testAdd13() throws Exception {
    testTypes(" var a = 5;" +
        " function b() { return 5; }" +
        " var c = a + b();",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd14
  public void testAdd14() throws Exception {
    testTypes(" var a = null;" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd15
  public void testAdd15() throws Exception {
    testTypes(" var a = 5;" +
        " function b() { return 5; }" +
        " var c = a + b();",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd16
  public void testAdd16() throws Exception {
    testTypes(" var a = undefined;" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd17
  public void testAdd17() throws Exception {
    testTypes(" var a = 5;" +
        " var b = undefined;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd18
  public void testAdd18() throws Exception {
    testTypes("function f() {};" +
        " var a = 'a';" +
        " var c = a + f();",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd19
  public void testAdd19() throws Exception {
    testTypes(" function f(opt_x, opt_y) {" +
        "return opt_x + opt_y;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd20
  public void testAdd20() throws Exception {
    testTypes(" function f(opt_x, opt_y) {" +
        "return opt_x + opt_y;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd21
  public void testAdd21() throws Exception {
    testTypes(" function f(opt_x, opt_y) {" +
        "return opt_x + opt_y;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison1
  public void testNumericComparison1() throws Exception {
    testTypes(" function f(a) {return a < 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison2
  public void testNumericComparison2() throws Exception {
    testTypes(" function f(a) {return a < 3;}",
        "left side of numeric comparison\n" +
        "found   : Object\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison3
  public void testNumericComparison3() throws Exception {
    testTypes(" function f(a) {return a < 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison4
  public void testNumericComparison4() throws Exception {
    testTypes(" " +
              "function f(a) {return a < 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison5
  public void testNumericComparison5() throws Exception {
    testTypes(" function f(a) {return a < 3;}",
        "left side of numeric comparison\n" +
        "found   : *\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison6
  public void testNumericComparison6() throws Exception {
    testTypes(" function foo() { if (3 >= foo()) return; }",
        "right side of numeric comparison\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison1
  public void testStringComparison1() throws Exception {
    testTypes(" function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison2
  public void testStringComparison2() throws Exception {
    testTypes(" function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison3
  public void testStringComparison3() throws Exception {
    testTypes(" function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison4
  public void testStringComparison4() throws Exception {
    testTypes(" " +
                  "function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison5
  public void testStringComparison5() throws Exception {
    testTypes(" " +
                  "function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison6
  public void testStringComparison6() throws Exception {
    testTypes(" function foo() { if ('a' >= foo()) return; }",
        "right side of comparison\n" +
        "found   : undefined\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testValueOfComparison1
  public void testValueOfComparison1() throws Exception {
    testTypes("function O() {};" +
        "O.prototype.valueOf = function() { return 1; };" +
        " function f(a,b) { return a < b; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testValueOfComparison2
  public void testValueOfComparison2() throws Exception {
    testTypes("function O() {};" +
        "O.prototype.valueOf = function() { return 1; };" +
        "" +
        "function f(a,b) { return a < b; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testValueOfComparison3
  public void testValueOfComparison3() throws Exception {
    testTypes("function O() {};" +
        "O.prototype.toString = function() { return 'o'; };" +
        "" +
        "function f(a,b) { return a < b; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGenericRelationalExpression
  public void testGenericRelationalExpression() throws Exception {
    testTypes(" " +
                  "function f(a,b) {return a < b;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof1
  public void testInstanceof1() throws Exception {
    testTypes("function foo(){" +
        "if (bar instanceof 3)return;}",
        "instanceof requires an object\n" +
        "found   : number\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof2
  public void testInstanceof2() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return;}",
        "deterministic instanceof yields false\n" +
        "found   : undefined\n" +
        "required: NoObject");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof3
  public void testInstanceof3() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof4
  public void testInstanceof4() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof5
  public void testInstanceof5() throws Exception {
    
    testTypes(" function foo(){" +
        "if (foo() instanceof Object)return;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof6
  public void testInstanceof6() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceOfReduction3
  public void testInstanceOfReduction3() throws Exception {
    testTypes(
        "\n" +
        "var f = function(x, y) {\n" +
        "  return x instanceof y;\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping1
  public void testScoping1() throws Exception {
    testTypes(
        "function foo(a){" +
        "  function bar(a){" +
        "    if (a instanceof Array)return;" +
        "  }" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping2
  public void testScoping2() throws Exception {
    testTypes(
        " var a;" +
        "function Foo() {" +
        "   var a;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping3
  public void testScoping3() throws Exception {
    testTypes("\n\nvar b;\nvar b;",
        "variable b redefined with type String, original " +
        "definition at [testcode]:3 with type (Number|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping4
  public void testScoping4() throws Exception {
    testTypes("var b; if (true) var b;",
        "variable b redefined with type String, original " +
        "definition at [testcode]:1 with type (Number|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping5
  public void testScoping5() throws Exception {
    
    
    testTypes("if (true) var b; var b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping6
  public void testScoping6() throws Exception {
    
    
    testTypes("if (true) var b; if (true) var b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping7
  public void testScoping7() throws Exception {
    testTypes("function A() {" +
        "  this.a = null;" +
        "}",
        "assignment to property a of A\n" +
        "found   : null\n" +
        "required: A");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping8
  public void testScoping8() throws Exception {
    testTypes("function A() {}" +
        "function B() {" +
        "  this.a = null;" +
        "}",
        "assignment to property a of B\n" +
        "found   : null\n" +
        "required: A");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping9
  public void testScoping9() throws Exception {
    testTypes("function B() {" +
        "  this.a = null;" +
        "}" +
        "function A() {}",
        "assignment to property a of B\n" +
        "found   : null\n" +
        "required: A");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping10
  public void testScoping10() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = function b(){};");

    
    assertTrue(p.scope.isDeclared("a", false));
    assertFalse(p.scope.isDeclared("b", false));

    
    assertEquals("function (): undefined",
        p.scope.getVar("a").getType().toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping11
  public void testScoping11() throws Exception {
    
    
    testTypes(
        "var a = function b(){ return b };",
        "inconsistent return type\n" +
        "found   : function (): number\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments1
  public void testFunctionArguments1() throws Exception {
    testFunctionType(
        "" +
        "function f(a) {}",
        "function (number): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments2
  public void testFunctionArguments2() throws Exception {
    testFunctionType(
        "" +
        "function f(opt_a) {}",
        "function ((number|undefined)): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments3
  public void testFunctionArguments3() throws Exception {
    testFunctionType(
        "" +
        "function f(a,b) {}",
        "function (?, number): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments4
  public void testFunctionArguments4() throws Exception {
    testFunctionType(
        "" +
        "function f(a,opt_a) {}",
        "function (?, (number|undefined)): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments5
  public void testFunctionArguments5() throws Exception {
    testTypes(
        "function a(opt_a,a) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments6
  public void testFunctionArguments6() throws Exception {
    testTypes(
        "function a(var_args,a) {}",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments7
  public void testFunctionArguments7() throws Exception {
    testTypes(
        "" +
        "function a(a,opt_a,var_args) {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments8
  public void testFunctionArguments8() throws Exception {
    testTypes(
        "function a(a,opt_a,var_args,b) {}",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments9
  public void testFunctionArguments9() throws Exception {
    
    testTypes(
        "function a(a,opt_a,var_args,b,c) {}",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments10
  public void testFunctionArguments10() throws Exception {
    
    testTypes(
        "function a(a,opt_a,b,c) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments11
  public void testFunctionArguments11() throws Exception {
    testTypes(
        "function a(a,opt_a,b,c,var_args,d) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments12
  public void testFunctionArguments12() throws Exception {
    testTypes("function bar(baz){}",
        "parameter foo does not appear in bar's parameter list");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments13
  public void testFunctionArguments13() throws Exception {
    
    testTypes(
        " function u() { return true; }" +
        "" +
        "function f(b) { if (u()) { b = null; } return b; }",
        "inconsistent return type\n" +
        "found   : (boolean|null)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments14
  public void testFunctionArguments14() throws Exception {
    testTypes(
        " function f(x, opt_y, var_args) {}" +
        "f('3'); f('3', 2); f('3', 2, true); f('3', 2, true, false);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments15
  public void testFunctionArguments15() throws Exception {
    testTypes(
        "" +
        "function g(f) { f(1, 2); }",
        "Function f: called with 2 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments16
  public void testFunctionArguments16() throws Exception {
    testTypes(
        "" +
        "function g(var_args) {} g(1, true);",
        "actual parameter 2 of g does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: (number|undefined)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPrintFunctionName1
  public void testPrintFunctionName1() throws Exception {
    
    testTypes(
        "var goog = {}; goog.run = function(f) {};" +
        "goog.run();",
        "Function goog.run: called with 0 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPrintFunctionName2
  public void testPrintFunctionName2() throws Exception {
    testTypes(
        " var Foo = function() {}; " +
        "Foo.prototype.run = function(f) {};" +
        "(new Foo).run();",
        "Function Foo.prototype.run: called with 0 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference1
  public void testFunctionInference1() throws Exception {
    testFunctionType(
        "function f(a) {}",
        "function (?): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference2
  public void testFunctionInference2() throws Exception {
    testFunctionType(
        "function f(a,b) {}",
        "function (?, ?): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference3
  public void testFunctionInference3() throws Exception {
    testFunctionType(
        "function f(var_args) {}",
        "function (...[?]): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference4
  public void testFunctionInference4() throws Exception {
    testFunctionType(
        "function f(a,b,c,var_args) {}",
        "function (?, ?, ?, ...[?]): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference5
  public void testFunctionInference5() throws Exception {
    testFunctionType(
        "function f(a) {}",
        "function (this:Date, ?): string");
  }
