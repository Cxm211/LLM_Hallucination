// buggy code
  public void visit(NodeTraversal t, Node n, Node parent) {
    // VOID nodes appear when there are extra semicolons at the BLOCK level.
    // I've been unable to think of any cases where this indicates a bug,
    // and apparently some people like keeping these semicolons around,
    // so we'll allow it.
    if (n.isEmpty() ||
        n.isComma()) {
      return;
    }

    if (parent == null) {
      return;
    }

    // Do not try to remove a block or an expr result. We already handle
    // these cases when we visit the child, and the peephole passes will
    // fix up the tree in more clever ways when these are removed.
    if (parent.getType() == Token.COMMA) {
      Node gramps = parent.getParent();
      if (gramps.isCall() && parent == gramps.getFirstChild()) {
        if (n == parent.getFirstChild() && parent.getChildCount() == 2 && n.getNext().isName() && "eval".equals(n.getNext().getString())) {
      return;
        }
    }

    // This no-op statement was there so that JSDoc information could
    // be attached to the name. This check should not complain about it.
      if (n == parent.getLastChild()) {
        for (Node an : parent.getAncestors()) {
          int ancestorType = an.getType();
          if (ancestorType == Token.COMMA)
            continue;
          if (ancestorType != Token.EXPR_RESULT && ancestorType != Token.BLOCK)
            return;
          else
            break;
        }
      }
    } else if (parent.getType() != Token.EXPR_RESULT && parent.getType() != Token.BLOCK) {
      if (parent.getType() == Token.FOR && parent.getChildCount() == 4 && (n == parent.getFirstChild() ||
           n == parent.getFirstChild().getNext().getNext())) {
      } else {
      return;
      }
    }

    boolean isResultUsed = NodeUtil.isExpressionResultUsed(n);
    boolean isSimpleOp = NodeUtil.isSimpleOperatorType(n.getType());
    if (!isResultUsed &&
        (isSimpleOp || !NodeUtil.mayHaveSideEffects(n, t.getCompiler()))) {
      if (n.isQualifiedName() && n.getJSDocInfo() != null) {
        return;
      } else if (n.isExprResult()) {
        return;
      }
      String msg = "This code lacks side-effects. Is there a bug?";
      if (n.isString()) {
        msg = "Is there a missing '+' on the previous line?";
      } else if (isSimpleOp) {
        msg = "The result of the '" + Token.name(n.getType()).toLowerCase() +
            "' operator is not being used.";
      }

      t.getCompiler().report(
          t.makeError(n, level, USELESS_CODE_ERROR, msg));
      // TODO(johnlenz): determine if it is necessary to
      // try to protect side-effect free statements as well.
      if (!NodeUtil.isStatement(n)) {
        problemNodes.add(n);
      }
    }
  }

// relevant test
// com.google.javascript.jscomp.CheckGlobalNamesTest::testSuppressionOfUndefinedNamesWarning
  public void testSuppressionOfUndefinedNamesWarning() {
    testSame(new String[] {
        NAMES +
        " function Foo() { };" +
        "" +
        "Foo.prototype.bar = function() {" +
        "  alert(a.x);" +
        "  alert(a.x.b());" +
        "  a.x();" +
        "  var c = a.x.b;" +
        "  var c = a.x.b();" +
        "  a.x.b();" +
        "  a.x.b = 3;" +
        "};",
    });
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testNoWarningForSimpleVarModuleDep1
  public void testNoWarningForSimpleVarModuleDep1() {
    testSame(createModuleChain(
        NAMES,
        "var c = a;"
    ));
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testNoWarningForSimpleVarModuleDep2
  public void testNoWarningForSimpleVarModuleDep2() {
    testSame(createModuleChain(
        "var c = a;",
        NAMES
    ));
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testNoWarningForGoodModuleDep1
  public void testNoWarningForGoodModuleDep1() {
    testSame(createModuleChain(
        NAMES,
        "var c = a.b;"
    ));
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testBadModuleDep1
  public void testBadModuleDep1() {
    testSame(createModuleChain(
        "var c = a.b;",
        NAMES
    ), STRICT_MODULE_DEP_QNAME);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testBadModuleDep2
  public void testBadModuleDep2() {
    testSame(createModuleStar(
        NAMES,
        "a.xxx = 3;",
        "var x = a.xxx;"
    ), STRICT_MODULE_DEP_QNAME);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testSelfModuleDep
  public void testSelfModuleDep() {
    testSame(createModuleChain(
        NAMES + "var c = a.b;"
    ));
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testUndefinedModuleDep1
  public void testUndefinedModuleDep1() {
    testSame(createModuleChain(
        "var c = a.xxx;",
        NAMES
    ), UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testLateDefinedName1
  public void testLateDefinedName1() {
    testSame("x.y = {}; var x = {};", NAME_DEFINED_LATE_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testLateDefinedName2
  public void testLateDefinedName2() {
    testSame("var x = {}; x.y.z = {}; x.y = {};", NAME_DEFINED_LATE_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testLateDefinedName3
  public void testLateDefinedName3() {
    testSame("var x = {}; x.y.z = {}; x.y = {z: {}};",
        NAME_DEFINED_LATE_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testLateDefinedName4
  public void testLateDefinedName4() {
    testSame("var x = {}; x.y.z.bar = {}; x.y = {z: {}};",
        NAME_DEFINED_LATE_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testLateDefinedName5
  public void testLateDefinedName5() {
    testSame("var x = {};  x.y.z; x.y = {};",
        NAME_DEFINED_LATE_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testLateDefinedName6
  public void testLateDefinedName6() {
    testSame(
        "var x = {}; x.y.prototype.z = 3;" +
        " x.y = function() {};",
        NAME_DEFINED_LATE_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testOkLateDefinedName1
  public void testOkLateDefinedName1() {
    testSame("function f() { x.y = {}; } var x = {};");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testOkLateDefinedName2
  public void testOkLateDefinedName2() {
    testSame("var x = {}; function f() { x.y.z = {}; } x.y = {};");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testPathologicalCaseThatsOkAnyway
  public void testPathologicalCaseThatsOkAnyway() {
    testSame(
        "var x = {};" +
        "switch (x) { " +
        "  default: x.y.z = {}; " +
        "  case (x.y = {}): break;" +
        "}", NAME_DEFINED_LATE_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testOkGlobalDeclExpr
  public void testOkGlobalDeclExpr() {
    testSame("var x = {};  x.foo;");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testBadInterfacePropRef
  public void testBadInterfacePropRef() {
    testSame(
        " function F() {}" +
         "F.bar();",
         UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testInterfaceFunctionPropRef
  public void testInterfaceFunctionPropRef() {
    testSame(
        " function F() {}" +
         "F.call(); F.hasOwnProperty('z');");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testObjectPrototypeProperties
  public void testObjectPrototypeProperties() {
    testSame("var x = {}; var y = x.hasOwnProperty('z');");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testCustomObjectPrototypeProperties
  public void testCustomObjectPrototypeProperties() {
    testSame("Object.prototype.seal = function() {};" +
        "var x = {}; x.seal();");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testFunctionPrototypeProperties
  public void testFunctionPrototypeProperties() {
    testSame("var x = {}; var y = x.hasOwnProperty('z');");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testIndirectlyDeclaredProperties
  public void testIndirectlyDeclaredProperties() {
    testSame(
        "Function.prototype.inherits = function(ctor) {" +
        "  this.superClass_ = ctor;" +
        "};" +
        " function Foo() {}" +
        "Foo.prototype.bar = function() {};" +
        " function SubFoo() {}" +
        "SubFoo.inherits(Foo);" +
        "SubFoo.superClass_.bar();");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testGoogInheritsAlias
  public void testGoogInheritsAlias() {
    testSame(
        "Function.prototype.inherits = function(ctor) {" +
        "  this.superClass_ = ctor;" +
        "};" +
        " function Foo() {}" +
        "Foo.prototype.bar = function() {};" +
        " function SubFoo() {}" +
        "SubFoo.inherits(Foo);" +
        "SubFoo.superClass_.bar();");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testGoogInheritsAlias2
  public void testGoogInheritsAlias2() {
    testSame(
        CompilerTypeTestCase.CLOSURE_DEFS +
        " function Foo() {}" +
        "Foo.prototype.bar = function() {};" +
        " function SubFoo() {}" +
        "goog.inherits(SubFoo, Foo);" +
        "SubFoo.superClazz();",
         UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis1
  public void testGlobalThis1() throws Exception {
    testSame("var a = this;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis2
  public void testGlobalThis2() {
    testFailure("this.foo = 5;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis3
  public void testGlobalThis3() {
    testFailure("this[foo] = 5;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis4
  public void testGlobalThis4() {
    testFailure("this['foo'] = 5;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis5
  public void testGlobalThis5() {
    testFailure("(a = this).foo = 4;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis6
  public void testGlobalThis6() {
    testSame("a = this;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis7
  public void testGlobalThis7() {
    testFailure("var a = this.foo;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction1
  public void testStaticFunction1() {
    testSame("function a() { return this; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction2
  public void testStaticFunction2() {
    testFailure("function a() { this.complex = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction3
  public void testStaticFunction3() {
    testSame("var a = function() { return this; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction4
  public void testStaticFunction4() {
    testFailure("var a = function() { this.foo.bar = 6; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction5
  public void testStaticFunction5() {
    testSame("function a() { return function() { return this; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction6
  public void testStaticFunction6() {
    testSame("function a() { return function() { this.x = 8; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction7
  public void testStaticFunction7() {
    testSame("var a = function() { return function() { this.x = 8; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction8
  public void testStaticFunction8() {
    testFailure("var a = function() { return this.foo; };");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testConstructor1
  public void testConstructor1() {
    testSame("function A() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testConstructor2
  public void testConstructor2() {
    testSame("var A = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testConstructor3
  public void testConstructor3() {
    testSame("a.A = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testInterface1
  public void testInterface1() {
    testSame(
        "function A() {  this.m2; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testOverride1
  public void testOverride1() {
    testSame("function A() { } var a = new A();" +
             " a.foo = function() { this.bar = 5; };");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testThisJSDoc1
  public void testThisJSDoc1() throws Exception {
    testSame("function h() { this.foo = 56; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testThisJSDoc2
  public void testThisJSDoc2() throws Exception {
    testSame("var h = function() { this.foo = 56; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testThisJSDoc3
  public void testThisJSDoc3() throws Exception {
    testSame("foo.bar = function() { this.foo = 56; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testThisJSDoc4
  public void testThisJSDoc4() throws Exception {
    testSame("function f() { this.foo = 56; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testThisJSDoc5
  public void testThisJSDoc5() throws Exception {
    testSame("function a() { function f() { this.foo = 56; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testMethod1
  public void testMethod1() {
    testSame("A.prototype.m1 = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testMethod2
  public void testMethod2() {
    testSame("a.B.prototype.m1 = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testMethod3
  public void testMethod3() {
    testSame("a.b.c.D.prototype.m1 = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testMethod4
  public void testMethod4() {
    testSame("a.prototype['x' + 'y'] =  function() { this.foo = 3; };");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testPropertyOfMethod
  public void testPropertyOfMethod() {
    testFailure("a.protoype.b = {}; " +
        "a.prototype.b.c = function() { this.foo = 3; };");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticMethod1
  public void testStaticMethod1() {
    testFailure("a.b = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticMethod2
  public void testStaticMethod2() {
    testSame("a.b = function() { return function() { this.m2 = 5; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticMethod3
  public void testStaticMethod3() {
    testSame("a.b.c = function() { return function() { this.m2 = 5; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testMethodInStaticFunction
  public void testMethodInStaticFunction() {
    testSame("function f() { A.prototype.m1 = function() { this.m2 = 5; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunctionInMethod1
  public void testStaticFunctionInMethod1() {
    testSame("A.prototype.m1 = function() { function me() { this.m2 = 5; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunctionInMethod2
  public void testStaticFunctionInMethod2() {
    testSame("A.prototype.m1 = function() {" +
        "  function me() {" +
        "    function myself() {" +
        "      function andI() { this.m2 = 5; } } } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testInnerFunction1
  public void testInnerFunction1() {
    testFailure("function f() { function g() { return this.x; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testInnerFunction2
  public void testInnerFunction2() {
    testFailure("function f() { var g = function() { return this.x; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testInnerFunction3
  public void testInnerFunction3() {
    testFailure(
        "function f() { var x = {}; x.y = function() { return this.x; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testInnerFunction4
  public void testInnerFunction4() {
    testSame(
        "function f() { var x = {}; x.y(function() { return this.x; }); }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testIssue182a
  public void testIssue182a() {
    testFailure("var NS = {read: function() { return this.foo; }};");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testIssue182b
  public void testIssue182b() {
    testFailure("var NS = {write: function() { this.foo = 3; }};");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testIssue182c
  public void testIssue182c() {
    testFailure("var NS = {}; NS.write2 = function() { this.foo = 3; };");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testIssue182d
  public void testIssue182d() {
    testSame("function Foo() {} " +
        "Foo.prototype = {write: function() { this.foo = 3; }};");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testLendsAnnotation1
  public void testLendsAnnotation1() {
    testFailure(" function F() {}" +
        "dojo.declare(F, {foo: function() { return this.foo; }});");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testLendsAnnotation2
  public void testLendsAnnotation2() {
    testFailure(" function F() {}" +
        "dojo.declare(F,  (" +
        "    {foo: function() { return this.foo; }}));");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testLendsAnnotation3
  public void testLendsAnnotation3() {
    testSame(" function F() {}" +
        "dojo.declare(F,  (" +
        "    {foo: function() { return this.foo; }}));");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testSuppressWarning
  public void testSuppressWarning() {
    testFailure("var x = function() { this.complex = 5; };");
    testSame("" +
        "var x = function() { this.complex = 5; };");
  }

// com.google.javascript.jscomp.CheckMissingGetCssNameTest::testMissingGetCssName
  public void testMissingGetCssName() {
    testMissing("var s = 'goog-inline-block'");
    testMissing("var s = 'CSS_FOO goog-menu'");
    testMissing("alert('goog-inline-block ' + goog.getClassName('CSS_FOO'))");
    testMissing("html = '<div class=\"goog-special-thing\">Hello</div>'");
  }

// com.google.javascript.jscomp.CheckMissingGetCssNameTest::testRecognizeGetCssName
  public void testRecognizeGetCssName() {
    testNotMissing("var s = goog.getCssName('goog-inline-block')");
  }

// com.google.javascript.jscomp.CheckMissingGetCssNameTest::testIgnoreGetUniqueIdArguments
  public void testIgnoreGetUniqueIdArguments() {
    testNotMissing("var s = goog.events.getUniqueId('goog-some-event')");
    testNotMissing("var s = joe.random.getUniqueId('joe-is-a-goob')");
  }

// com.google.javascript.jscomp.CheckMissingGetCssNameTest::testIgnoreAssignmentsToIdConstant
  public void testIgnoreAssignmentsToIdConstant() {
    testNotMissing("SOME_ID = 'goog-some-id'");
    testNotMissing("SOME_PRIVATE_ID_ = 'goog-some-id'");
    testNotMissing("var SOME_ID_ = 'goog-some-id'");
  }

// com.google.javascript.jscomp.CheckMissingGetCssNameTest::testNotMissingGetCssName
  public void testNotMissingGetCssName() {
    testNotMissing("s = 'not-a-css-name'");
    testNotMissing("s = 'notagoog-css-name'");
  }

// com.google.javascript.jscomp.CheckMissingGetCssNameTest::testDontCrashIfTheresNoQualifiedName
  public void testDontCrashIfTheresNoQualifiedName() {
    testMissing("things[2].DONT_CARE_ABOUT_THIS_KIND_OF_ID = "
                + "'goog-inline-block'");
    testMissing("objects[3].doSomething('goog-inline-block')");
  }

// com.google.javascript.jscomp.CheckMissingReturnTest::testMissingReturn
  public void testMissingReturn() {
    
    testMissing("if (a) { return 1; }");

    
    testMissing("switch(1) { case 12: return 5; }");

    
    testMissing("try { foo() } catch (e) { return 5; } finally { }");

    
    testMissing(" function f() { var x; }; return 1;");
    testMissing(" function f() { return 1; };");
  }

// com.google.javascript.jscomp.CheckMissingReturnTest::testReturnNotMissing
  public void testReturnNotMissing()  {
    
    
    testNotMissing("");

    
    testSame("function f() { var x; }");
    testNotMissing("return 1;");

    
    testNotMissing("void", "var x;");
    testNotMissing("undefined", "var x;");

    
    testNotMissing("number|undefined", "var x;");
    testNotMissing("number|void", "var x;");
    testNotMissing("(number,void)", "var x;");
    testNotMissing("(number,undefined)", "var x;");
    testNotMissing("*", "var x;");

    
    testNotMissing("try { return foo() } catch (e) { } finally { }");

    
    testNotMissing(
        " function f() { return 1; }; return 1;");

    
    testNotMissing("try { return 12; } finally { return 62; }");
    testNotMissing("try { } finally { return 1; }");
    testNotMissing("switch(1) { default: return 1; }");
    testNotMissing("switch(g) { case 1: return 1; default: return 2; }");
  }

// com.google.javascript.jscomp.CheckMissingReturnTest::testFinallyStatements
  public void testFinallyStatements() {
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    
    testNotMissing("try { return 1; } finally { }");
    testNotMissing("try { } finally { return 1; }");
    testMissing("try { } finally { }");

    
    testNotMissing("try { return 1; } finally { while (true) { } }");
    testMissing("try { } finally { while (x) { } }");
    testMissing("try { } finally { while (x) { if (x) { break; } } }");
    testNotMissing(
        "try { return 2; } finally { while (x) { if (x) { break; } } }");

    
    testMissing("try { } finally { try { } finally { } }");
    testNotMissing("try { } finally { try { return 1; } finally { } }");
    testNotMissing("try { return 1; } finally { try { } finally { } }");

    
    
    
    
    
    testNotMissing("try { g(); return 1; } finally { }");

    
    
    
    
    testNotMissing(
        "try {" +
        "    function f() {" +
        "       try { return 1; }" +
        "       finally { }" +
        "   };" +
        "   return 1;" +
        "}" +
        "finally { }");
    testMissing(
        "try {" +
        "    function f() {" +
        "       try { }" +
        "       finally { }" +
        "   };" +
        "   return 1;" +
        "}" +
        "finally { }");
    testMissing(
        "try {" +
        "    function f() {" +
        "       try { return 1; }" +
        "       finally { }" +
        "   };" +
        "}" +
        "finally { }");
  }

// com.google.javascript.jscomp.CheckMissingReturnTest::testKnownConditions
  public void testKnownConditions() {
    testNotMissing("if (true) return 1");
    testMissing("if (true) {} else {return 1}");

    testMissing("if (false) return 1");
    testNotMissing("if (false) {} else {return 1}");

    testNotMissing("if (1) return 1");
    testMissing("if (1) {} else {return 1}");

    testMissing("if (0) return 1");
    testNotMissing("if (0) {} else {return 1}");

    testNotMissing("if (3) return 1");
    testMissing("if (3) {} else {return 1}");
  }

// com.google.javascript.jscomp.CheckMissingReturnTest::testKnownWhileLoop
  public void testKnownWhileLoop() {
    testNotMissing("while (1) return 1");
    testNotMissing("while (1) { if (x) {return 1} else {return 1}}");
    testNotMissing("while (0) {} return 1");

    
    
    testNotMissing("while (1) {} return 0");
    testMissing("while (false) return 1");

    
    testMissing("while(x) { return 1 }");
  }

// com.google.javascript.jscomp.CheckMissingReturnTest::testMultiConditions
  public void testMultiConditions() {
    testMissing("if (a) { } else { while (1) {return 1} }");
    testNotMissing("if (a) { return 1} else { while (1) {return 1} }");
  }

// com.google.javascript.jscomp.CheckProvidesTest::testIrrelevant
  public void testIrrelevant() {
    testSame("var str = 'g4';");
  }

// com.google.javascript.jscomp.CheckProvidesTest::testHarmlessProcedural
  public void testHarmlessProcedural() {
    testSame("goog.provide('X');  function X(){};");
  }

// com.google.javascript.jscomp.CheckProvidesTest::testHarmless
  public void testHarmless() {
    String js = "goog.provide('X');  X = function(){};";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckProvidesTest::testNoProvideInnerClass
  public void testNoProvideInnerClass() {
    testSame(
        "goog.provide('X');\n" +
        " function X(){};" +
        " X.Y = function(){};");
  }

// com.google.javascript.jscomp.CheckProvidesTest::testMissingGoogProvide
  public void testMissingGoogProvide(){
    String[] js = new String[]{" X = function(){};"};
    String warning = "missing goog.provide('X')";
    test(js, js, null, MISSING_PROVIDE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckProvidesTest::testMissingGoogProvideWithNamespace
  public void testMissingGoogProvideWithNamespace(){
    String[] js = new String[]{"goog = {}; " +
                               " goog.X = function(){};"};
    String warning = "missing goog.provide('goog.X')";
    test(js, js, null, MISSING_PROVIDE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckProvidesTest::testGoogProvideInWrongFileShouldCreateWarning
  public void testGoogProvideInWrongFileShouldCreateWarning(){
    String bad = " X = function(){};";
    String good = "goog.provide('X'); goog.provide('Y');" +
                  " X = function(){};" +
                  " Y = function(){};";
    String[] js = new String[] {good, bad};
    String warning = "missing goog.provide('X')";
    test(js, js, null, MISSING_PROVIDE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckProvidesTest::testGoogProvideMissingConstructorIsOkForNow
  public void testGoogProvideMissingConstructorIsOkForNow(){
    
    
    testSame(new String[]{"goog.provide('Y'); X = function(){};"});
  }

// com.google.javascript.jscomp.CheckProvidesTest::testIgnorePrivateConstructor
  public void testIgnorePrivateConstructor() {
    String js = " X_ = function(){};";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckProvidesTest::testIgnorePrivatelyAnnotatedConstructor
  public void testIgnorePrivatelyAnnotatedConstructor() {
    testSame(" X = function(){};");
    testSame(" X = function(){};");
  }

// com.google.javascript.jscomp.CheckRegExpTest::testRegExp
  public void testRegExp() {
    
    testReference("RegExp();", false);
    testReference("var x = RegExp();", false);
    testReference("new RegExp();", false);
    testReference("var x = new RegExp();", false);

    
    testReference("x instanceof RegExp;", false);

    
    testReference("RegExp.test();", true);
    testReference("var x = RegExp.test();", true);
    testReference("RegExp.exec();", true);
    testReference("RegExp.$1;", true);
    testReference("RegExp.foobar;", true);
    testReference("delete RegExp;", true);

    
    testReference("var x = RegExp;", true);
    testReference("f(RegExp);", true);
    testReference("new f(RegExp);", true);
    testReference("var x = RegExp; x.test()", true);

    
    testReference("var x;", false);

    
    testReference("function f() {var RegExp; RegExp.test();}", false);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithNoNewNodes
  public void testPassWithNoNewNodes() {
    String js = "var str = 'g4'; ";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithOneNew
  public void testPassWithOneNew() {
    String js =
        "var goog = {};" +
        "goog.require('foo.bar.goo'); var bar = new foo.bar.goo();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithOneNewOuterClass
  public void testPassWithOneNewOuterClass() {
    String js =
        "var goog = {};" +
        "goog.require('goog.foo.Bar'); var bar = new goog.foo.Bar.Baz();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithOneNewOuterClassWithUpperPrefix
  public void testPassWithOneNewOuterClassWithUpperPrefix() {
    String js =
        "var goog = {};" +
        "goog.require('goog.foo.IDBar'); var bar = new goog.foo.IDBar.Baz();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testFailWithOneNew
  public void testFailWithOneNew() {
    String[] js = new String[] {"var foo = {}; var bar = new foo.bar();"};
    String warning = "'foo.bar' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithTwoNewNodes
  public void testPassWithTwoNewNodes() {
    String js =
        "var goog = {};" +
        "goog.require('goog.foo.Bar');goog.require('goog.foo.Baz');" +
        "var str = new goog.foo.Bar('g4'), num = new goog.foo.Baz(5); ";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithNestedNewNodes
  public void testPassWithNestedNewNodes() {
    String js =
        "var goog = {}; goog.require('goog.foo.Bar'); " +
        "var str = new goog.foo.Bar(new goog.foo.Bar('5')); ";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testFailWithNestedNewNodes
  public void testFailWithNestedNewNodes() {
    String[] js =
        new String[] {"var goog = {}; goog.require('goog.foo.Bar'); "
            + "var str = new goog.foo.Bar(new goog.foo.Baz('5')); "};
    String warning = "'goog.foo.Baz' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithLocalFunctions
  public void testPassWithLocalFunctions() {
    String js =
        " function tempCtor() {}; var foo = new tempCtor();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithLocalVariables
  public void testPassWithLocalVariables() {
    String js =
        " var nodeCreator = function() {};"
            + "var newNode = new nodeCreator();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testFailWithLocalVariableInMoreThanOneFile
  public void testFailWithLocalVariableInMoreThanOneFile() {
    
    
    String localVar =
        " function tempCtor() {}" +
        "function baz(){" + "  function tempCtor() {}; "
            + "var foo = new tempCtor();}";
    String[] js = new String[] {localVar, " var foo = new tempCtor();"};
    String warning = "'tempCtor' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testNewNodesMetaTraditionalFunctionForm
  public void testNewNodesMetaTraditionalFunctionForm() {
    
    
    
    String js =
        " function Bar(){}; "
            + "Bar.prototype.bar = function(){ return new Bar();};";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testNewNodesMeta
  public void testNewNodesMeta() {
    String js =
        "var goog = {};" +
        "goog.ui.Option = function(){};"
            + "goog.ui.Option.optionDecorator = function(){"
            + "  return new goog.ui.Option(); };";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testShouldWarnWhenInstantiatingObjectsDefinedInGlobalScope
  public void testShouldWarnWhenInstantiatingObjectsDefinedInGlobalScope() {
    
    
    String good =
        " function Bar(){}; "
            + "Bar.prototype.bar = function(){return new Bar();};";
    String bad = " function Foo(){ var bar = new Bar();}";
    String[] js = new String[] {good, bad};
    String warning = "'Bar' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testShouldWarnWhenInstantiatingGlobalClassesFromGlobalScope
  public void testShouldWarnWhenInstantiatingGlobalClassesFromGlobalScope() {
    
    
    String good =
      " function Baz(){}; "
          + "Baz.prototype.bar = function(){return new Baz();};";
    String bad = "var baz = new Baz()";
    String[] js = new String[] {good, bad};
    String warning = "'Baz' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testIgnoresNativeObject
  public void testIgnoresNativeObject() {
    String externs = " function String(val) {}";
    String js = "var str = new String('4');";
    test(externs, js, js, null, null);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testNewNodesWithMoreThanOneFile
  public void testNewNodesWithMoreThanOneFile() {
    
    String[] js = new String[] {
        "var goog = {};" +
        " function Bar() {}" +
        "goog.require('Bar');",
        "var bar = new Bar();"};
    String warning = "'Bar' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithoutWarningsAndMultipleFiles
  public void testPassWithoutWarningsAndMultipleFiles() {
    String[] js = new String[] {
        "var goog = {};" +
        "goog.require('Foo'); var foo = new Foo();",
        "goog.require('Bar'); var bar = new Bar();"};
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testFailWithWarningsAndMultipleFiles
  public void testFailWithWarningsAndMultipleFiles() {
    
    String[] js = new String[] {
        "var goog = {};" +
        " function Bar() {}" +
        "goog.require('Bar');",
        "var bar = new Bar();"};
    String warning = "'Bar' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testCanStillCallNumberWithoutNewOperator
  public void testCanStillCallNumberWithoutNewOperator() {
    String externs = " function Number(opt_value) {}";
    String js = "var n = Number('42');";
    test(externs, js, js, null, null);
    js = "var n = Number();";
    test(externs, js, js, null, null);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testRequiresAreCaughtBeforeProcessed
  public void testRequiresAreCaughtBeforeProcessed() {
    String js = "var foo = {}; var bar = new foo.bar.goo();";
    SourceFile input = SourceFile.fromCode("foo.js", js);
    Compiler compiler = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    opts.checkRequires = CheckLevel.WARNING;
    opts.closurePass = true;

    Result result = compiler.compile(ImmutableList.<SourceFile>of(),
        ImmutableList.of(input), opts);
    JSError[] warnings = result.warnings;
    assertNotNull(warnings);
    assertTrue(warnings.length > 0);

    String expectation = "'foo.bar.goo' used but not goog.require'd";

    for (JSError warning : warnings) {
      if (expectation.equals(warning.description)) {
        return;
      }
    }

    fail("Could not find the following warning:" + expectation);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testNoWarningsForThisConstructor
  public void testNoWarningsForThisConstructor() {
    String js =
      "var goog = {};" +
      "goog.Foo = function() {};" +
      "goog.Foo.bar = function() {" +
      "  return new this.constructor; " +
      "};";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testBug2062487
  public void testBug2062487() {
    testSame(
      "var goog = {};" +
      "goog.Foo = function() {" +
      "   this.x_ = function() {};" +
      "  this.y_ = new this.x_();" +
      "};");
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testIgnoreDuplicateWarningsForSingleClasses
  public void testIgnoreDuplicateWarningsForSingleClasses(){
    
    String[] js = new String[]{
      "var goog = {};" +
      "goog.Foo = function() {};" +
      "goog.Foo.bar = function(){" +
      "  var first = new goog.Forgot();" +
      "  var second = new goog.Forgot();" +
      "};"};
    String warning = "'goog.Forgot' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::test
  public void test(String js, String expected, DiagnosticType warning) {
    test(js, expected, null, warning);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::test
  public void test(String js, DiagnosticType warning) {
    test(js, js, null, warning);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testUselessCode
  public void testUselessCode() {
    test("function f(x) { if(x) return; }", ok);
    test("function f(x) { if(x); }", "function f(x) { if(x); }", e);

    test("if(x) x = y;", ok);
    test("if(x) x == bar();", "if(x) JSCOMPILER_PRESERVE(x == bar());", e);

    test("x = 3;", ok);
    test("x == 3;", "JSCOMPILER_PRESERVE(x == 3);", e);

    test("var x = 'test'", ok);
    test("var x = 'test'\n'str'",
         "var x = 'test'\nJSCOMPILER_PRESERVE('str')", e);

    test("", ok);
    test("foo();;;;bar();;;;", ok);

    test("var a, b; a = 5, b = 6", ok);
    test("var a, b; a = 5, b == 6",
         "var a, b; a = 5, JSCOMPILER_PRESERVE(b == 6)", e);
    test("var a, b; a = (5, 6)",
         "var a, b; a = (JSCOMPILER_PRESERVE(5), 6)", e);
    test("var a, b; a = (bar(), 6, 7)",
         "var a, b; a = (bar(), JSCOMPILER_PRESERVE(6), 7)", e);
    test("var a, b; a = (bar(), bar(), 7, 8)",
         "var a, b; a = (bar(), bar(), JSCOMPILER_PRESERVE(7), 8)", e);
    test("var a, b; a = (b = 7, 6)", ok);
    test("function x(){}\nfunction f(a, b){}\nf(1,(x(), 2));", ok);
    test("function x(){}\nfunction f(a, b){}\nf(1,(2, 3));",
         "function x(){}\nfunction f(a, b){}\n" +
         "f(1,(JSCOMPILER_PRESERVE(2), 3));", e);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testUselessCodeInFor
  public void testUselessCodeInFor() {
    test("for(var x = 0; x < 100; x++) { foo(x) }", ok);
    test("for(; true; ) { bar() }", ok);
    test("for(foo(); true; foo()) { bar() }", ok);
    test("for(void 0; true; foo()) { bar() }",
         "for(JSCOMPILER_PRESERVE(void 0); true; foo()) { bar() }", e);
    test("for(foo(); true; void 0) { bar() }",
         "for(foo(); true; JSCOMPILER_PRESERVE(void 0)) { bar() }", e);
    test("for(foo(); true; (1, bar())) { bar() }",
         "for(foo(); true; (JSCOMPILER_PRESERVE(1), bar())) { bar() }", e);

    test("for(foo in bar) { foo() }", ok);
    test("for (i = 0; el = el.previousSibling; i++) {}", ok);
    test("for (i = 0; el = el.previousSibling; i++);", ok);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testTypeAnnotations
  public void testTypeAnnotations() {
    test("x;", "JSCOMPILER_PRESERVE(x);", e);
    test("a.b.c.d;", "JSCOMPILER_PRESERVE(a.b.c.d);", e);
    test(" a.b.c.d;", ok);
    test("if (true) {  a.b.c.d; }", ok);

    test("function A() { this.foo; }",
         "function A() { JSCOMPILER_PRESERVE(this.foo); }", e);
    test("function A() {  this.foo; }", ok);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testJSDocComments
  public void testJSDocComments() {
    test("function A() {  this.foo; }", ok);
    test("function A() {  this.foo; }",
         "function A() { " +
         "  JSCOMPILER_PRESERVE(this.foo); }", e);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testIssue80
  public void testIssue80() {
    test("(0, eval)('alert');", ok);
    test("(0, foo)('alert');", "(JSCOMPILER_PRESERVE(0), foo)('alert');", e);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testIsue504
  public void testIsue504() {
    test("void f();", "JSCOMPILER_PRESERVE(void f());", null, e,
        "Suspicious code. The result of the 'void' operator is not being used.");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testCorrectSimple
  public void testCorrectSimple() {
    testSame("var x");
    testSame("var x = 1");
    testSame("var x = 1; x = 2;");
    testSame("if (x) { var x = 1 }");
    testSame("if (x) { var x = 1 } else { var y = 2 }");
    testSame("while(x) {}");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testIncorrectSimple
  public void testIncorrectSimple() {
    assertUnreachable("function f() { return; x=1; }");
    assertUnreachable("function f() { return; x=1; x=1; }");
    assertUnreachable("function f() { return; var x = 1; }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testCorrectIfReturns
  public void testCorrectIfReturns() {
    testSame("function f() { if (x) { return } }");
    testSame("function f() { if (x) { return } return }");
    testSame("function f() { if (x) { if (y) { return } } else { return }}");
    testSame("function f()" +
        "{ if (x) { if (y) { return } return } else { return }}");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testInCorrectIfReturns
  public void testInCorrectIfReturns() {
    assertUnreachable(
        "function f() { if (x) { return } else { return } return }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testCorrectSwitchReturn
  public void testCorrectSwitchReturn() {
    testSame("function f() { switch(x) { default: return; case 1: x++; }}");
    testSame("function f() {" +
        "switch(x) { default: return; case 1: x++; } return }");
    testSame("function f() {" +
        "switch(x) { default: return; case 1: return; }}");
    testSame("function f() {" +
        "switch(x) { case 1: return; } return }");
    testSame("function f() {" +
        "switch(x) { case 1: case 2: return; } return }");
    testSame("function f() {" +
        "switch(x) { case 1: return; case 2: return; } return }");
    testSame("function f() {" +
        "switch(x) { case 1 : return; case 2: return; } return }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testInCorrectSwitchReturn
  public void testInCorrectSwitchReturn() {
    assertUnreachable("function f() {" +
        "switch(x) { default: return; case 1: return; } return }");
    assertUnreachable("function f() {" +
        "switch(x) { default: return; return; case 1: return; } }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testCorrectLoopBreaksAndContinues
  public void testCorrectLoopBreaksAndContinues() {
    testSame("while(1) { foo(); break }");
    testSame("while(1) { foo(); continue }");
    testSame("for(;;) { foo(); break }");
    testSame("for(;;) { foo(); continue }");
    testSame("for(;;) { if (x) { break } }");
    testSame("for(;;) { if (x) { continue } }");
    testSame("do { foo(); continue} while(1)");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testInCorrectLoopBreaksAndContinues
  public void testInCorrectLoopBreaksAndContinues() {
    assertUnreachable("while(1) { foo(); break; bar()}");
    assertUnreachable("while(1) { foo(); continue; bar() }");
    assertUnreachable("for(;;) { foo(); break; bar() }");
    assertUnreachable("for(;;) { foo(); continue; bar() }");
    assertUnreachable("for(;;) { if (x) { break; bar() } }");
    assertUnreachable("for(;;) { if (x) { continue; bar() } }");
    assertUnreachable("do { foo(); continue; bar()} while(1)");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testUncheckedWhileInDo
  public void testUncheckedWhileInDo() {
    assertUnreachable("do { foo(); break} while(1)");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testUncheckedConditionInFor
  public void testUncheckedConditionInFor() {
    assertUnreachable("for(var x = 0; x < 100; x++) { break };");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testFunctionDeclaration
  public void testFunctionDeclaration() {
    
    testSame("function f() { return; function ff() { }}");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testVarDeclaration
  public void testVarDeclaration() {
    assertUnreachable("function f() { return; var x = 1 }");
    
    assertUnreachable("function f() { return; var x }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testReachableTryCatchFinally
  public void testReachableTryCatchFinally() {
    testSame("try { } finally {  }");
    testSame("try { foo(); } finally bar(); ");
    testSame("try { foo() } finally { bar() }");
    testSame("try { foo(); } catch (e) {e()} finally bar(); ");
    testSame("try { foo() } catch (e) {e()} finally { bar() }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testUnreachableCatch
  public void testUnreachableCatch() {
    assertUnreachable("try { var x = 0 } catch (e) { }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testSpuriousBreak
  public void testSpuriousBreak() {
    testSame("switch (x) { default: throw x; break; }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testInstanceOfThrowsException
  public void testInstanceOfThrowsException() {
    testSame("function f() {try { if (value instanceof type) return true; } " +
             "catch (e) { }}");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testFalseCondition
  public void testFalseCondition() {
    assertUnreachable("if(false) { }");
    assertUnreachable("if(0) { }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testUnreachableLoop
  public void testUnreachableLoop() {
    assertUnreachable("while(false) {}");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testInfiniteLoop
  public void testInfiniteLoop() {
    testSame("while (true) { foo(); break; }");

    
    assertUnreachable("while(true) {} foo()");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testSuppression
  public void testSuppression() {
    assertUnreachable("if(false) { }");

    testSame(
        "\n" +
        "if(false) { }");

    testSame(
        "\n" +
        "function f() { if(false) { } }");

    testSame(
        "\n" +
        "function f() { if(false) { } }");

    assertUnreachable(
        "\n" +
        "function f() { if(false) { } }\n" +
        "function g() { if(false) { } }\n");

    testSame(
        "\n" +
        "function f() {\n" +
        "  function g() { if(false) { } }\n" +
        "  if(false) { } }\n");

    assertUnreachable(
        "function f() {\n" +
        "  \n" +
        "  function g() { if(false) { } }\n" +
        "  if(false) { } }\n");

    testSame(
        "function f() {\n" +
        "  \n" +
        "  function g() { if(false) { } }\n" +
        "}\n");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testRemoveAbstract
  public void testRemoveAbstract() {
    test("function Foo() {}; Foo.prototype.doSomething = goog.abstractMethod;",
        "function Foo() {};");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testRemoveMultiplySetAbstract
  public void testRemoveMultiplySetAbstract() {
    test("function Foo() {}; Foo.prototype.doSomething = " +
        "Foo.prototype.doSomethingElse = Foo.prototype.oneMore = " +
        "goog.abstractMethod;",
        "function Foo() {};");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testDoNotRemoveNormal
  public void testDoNotRemoveNormal() {
    testSame("function Foo() {}; Foo.prototype.doSomething = function() {};");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testDoNotRemoveOverride
  public void testDoNotRemoveOverride() {
    test("function Foo() {}; Foo.prototype.doSomething = goog.abstractMethod;" +
         "function Bar() {}; goog.inherits(Bar, Foo);" +
         "Bar.prototype.doSomething = function() {}",
         "function Foo() {}; function Bar() {}; goog.inherits(Bar, Foo);" +
         "Bar.prototype.doSomething = function() {}");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testDoNotRemoveNonQualifiedName
  public void testDoNotRemoveNonQualifiedName() {
    testSame("document.getElementById('x').y = goog.abstractMethod;");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testStopRemovalAtNonQualifiedName
  public void testStopRemovalAtNonQualifiedName() {
    test("function Foo() {}; function Bar() {};" +
         "Foo.prototype.x = document.getElementById('x').y = Bar.prototype.x" +
         " = goog.abstractMethod;",
         "function Foo() {}; function Bar() {};" +
         "Foo.prototype.x = document.getElementById('x').y = " +
         "goog.abstractMethod;");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testAssertionRemoval1
  public void testAssertionRemoval1() {
    test("var x = goog.asserts.assert(y(), 'message');", "var x = y();");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testAssertionRemoval2
  public void testAssertionRemoval2() {
    test("goog.asserts.assert(y(), 'message');", "");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testAssertionRemoval3
  public void testAssertionRemoval3() {
    test("goog.asserts.assert();", "");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testAssertionRemoval4
  public void testAssertionRemoval4() {
    test("var x = goog.asserts.assert();", "var x = void 0;");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testVarAndOptionalParams
  public void testVarAndOptionalParams() {
    Node args = new Node(Token.PARAM_LIST,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"));
    Node optArgs = new Node(Token.PARAM_LIST,
        Node.newString(Token.NAME, "opt_a"),
        Node.newString(Token.NAME, "opt_b"));

    assertFalse(conv.isVarArgsParameter(args.getFirstChild()));
    assertFalse(conv.isVarArgsParameter(args.getLastChild()));
    assertFalse(conv.isVarArgsParameter(optArgs.getFirstChild()));
    assertFalse(conv.isVarArgsParameter(optArgs.getLastChild()));

    assertFalse(conv.isOptionalParameter(args.getFirstChild()));
    assertFalse(conv.isOptionalParameter(args.getLastChild()));
    assertFalse(conv.isOptionalParameter(optArgs.getFirstChild()));
    assertFalse(conv.isOptionalParameter(optArgs.getLastChild()));
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInlineName
  public void testInlineName() {
    assertFalse(conv.isConstant("a"));
    assertFalse(conv.isConstant("XYZ123_"));
    assertFalse(conv.isConstant("ABC"));
    assertFalse(conv.isConstant("ABCdef"));
    assertFalse(conv.isConstant("aBC"));
    assertFalse(conv.isConstant("A"));
    assertFalse(conv.isConstant("_XYZ123"));
    assertFalse(conv.isConstant("a$b$XYZ123_"));
    assertFalse(conv.isConstant("a$b$ABC_DEF"));
    assertFalse(conv.isConstant("a$b$A"));
    assertFalse(conv.isConstant("a$b$a"));
    assertFalse(conv.isConstant("a$b$ABCdef"));
    assertFalse(conv.isConstant("a$b$aBC"));
    assertFalse(conv.isConstant("a$b$"));
    assertFalse(conv.isConstant("$"));
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testExportedName
  public void testExportedName() {
    assertFalse(conv.isExported("_a"));
    assertFalse(conv.isExported("_a_"));
    assertFalse(conv.isExported("a"));

    assertFalse(conv.isExported("$super", false));
    assertTrue(conv.isExported("$super", true));
    assertTrue(conv.isExported("$super"));
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testPrivateName
  public void testPrivateName() {
    assertFalse(conv.isPrivate("a_"));
    assertFalse(conv.isPrivate("a"));
    assertFalse(conv.isPrivate("_a_"));
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testEnumKey
  public void testEnumKey() {
    assertTrue(conv.isValidEnumKey("A"));
    assertTrue(conv.isValidEnumKey("123"));
    assertTrue(conv.isValidEnumKey("FOO_BAR"));

    assertTrue(conv.isValidEnumKey("a"));
    assertTrue(conv.isValidEnumKey("someKeyInCamelCase"));
    assertTrue(conv.isValidEnumKey("_FOO_BAR"));
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection1
  public void testInheritanceDetection1() {
    assertNotClassDefining("goog.foo(A, B);");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection2
  public void testInheritanceDetection2() {
    assertDefinesClasses("goog.inherits(A, B);", "A", "B");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection3
  public void testInheritanceDetection3() {
    assertDefinesClasses("A.inherits(B);", "A", "B");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection4
  public void testInheritanceDetection4() {
    assertDefinesClasses("goog.inherits(goog.A, goog.B);", "goog.A", "goog.B");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection5
  public void testInheritanceDetection5() {
    assertDefinesClasses("goog.A.inherits(goog.B);", "goog.A", "goog.B");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection6
  public void testInheritanceDetection6() {
    assertNotClassDefining("A.inherits(this.B);");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection7
  public void testInheritanceDetection7() {
    assertNotClassDefining("this.A.inherits(B);");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection8
  public void testInheritanceDetection8() {
    assertNotClassDefining("goog.inherits(A, B, C);");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection9
  public void testInheritanceDetection9() {
    assertDefinesClasses("A.mixin(B.prototype);",
        "A", "B");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection10
  public void testInheritanceDetection10() {
    assertDefinesClasses("goog.mixin(A.prototype, B.prototype);",
        "A", "B");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection11
  public void testInheritanceDetection11() {
    assertNotClassDefining("A.mixin(B)");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection12
  public void testInheritanceDetection12() {
    assertNotClassDefining("goog.mixin(A.prototype, B)");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection13
  public void testInheritanceDetection13() {
    assertNotClassDefining("goog.mixin(A, B)");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection14
  public void testInheritanceDetection14() {
    assertNotClassDefining("goog$mixin((function(){}).prototype)");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetectionPostCollapseProperties
  public void testInheritanceDetectionPostCollapseProperties() {
    assertDefinesClasses("goog$inherits(A, B);", "A", "B");
    assertNotClassDefining("goog$inherits(A);");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testObjectLiteralCast
  public void testObjectLiteralCast() {
    assertNotObjectLiteralCast("goog.reflect.object();");
    assertNotObjectLiteralCast("goog.reflect.object(A);");
    assertNotObjectLiteralCast("goog.reflect.object(1, {});");
    assertObjectLiteralCast("goog.reflect.object(A, {});");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testFunctionBind
  public void testFunctionBind() {
    assertNotFunctionBind("goog.bind()");  
    assertFunctionBind("goog.bind(f)");
    assertFunctionBind("goog.bind(f, obj)");
    assertFunctionBind("goog.bind(f, obj, p1)");

    assertNotFunctionBind("goog$bind()");  
    assertFunctionBind("goog$bind(f)");
    assertFunctionBind("goog$bind(f, obj)");
    assertFunctionBind("goog$bind(f, obj, p1)");

    assertNotFunctionBind("goog.partial()");  
    assertFunctionBind("goog.partial(f)");
    assertFunctionBind("goog.partial(f, obj)");
    assertFunctionBind("goog.partial(f, obj, p1)");

    assertNotFunctionBind("goog$partial()");  
    assertFunctionBind("goog$partial(f)");
    assertFunctionBind("goog$partial(f, obj)");
    assertFunctionBind("goog$partial(f, obj, p1)");

    assertFunctionBind("(function(){}).bind()");
    assertFunctionBind("(function(){}).bind(obj)");
    assertFunctionBind("(function(){}).bind(obj, p1)");

    assertNotFunctionBind("Function.prototype.bind.call()");
    assertFunctionBind("Function.prototype.bind.call(obj)");
    assertFunctionBind("Function.prototype.bind.call(obj, p1)");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testRequire
  public void testRequire() {
    assertRequire("goog.require('foo')");
    assertNotRequire("goog.require(foo)");
    assertNotRequire("goog.require()");
    assertNotRequire("foo()");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testApplySubclassRelationship
  public void testApplySubclassRelationship() {
    JSTypeRegistry registry = new JSTypeRegistry(null);

    Node nodeA = new Node(Token.FUNCTION);
    FunctionType ctorA = registry.createConstructorType("A", nodeA,
        new Node(Token.PARAM_LIST), null);

    Node nodeB = new Node(Token.FUNCTION);
    FunctionType ctorB = registry.createConstructorType("B", nodeB,
        new Node(Token.PARAM_LIST), null);

    conv.applySubclassRelationship(ctorA, ctorB, SubclassType.INHERITS);

    assertTrue(ctorB.getPrototype().hasOwnProperty("constructor"));
    assertEquals(nodeB, ctorB.getPrototype().getPropertyNode("constructor"));

    assertTrue(ctorB.hasOwnProperty("superClass_"));
    assertEquals(nodeB, ctorB.getPropertyNode("superClass_"));
  }

// com.google.javascript.jscomp.ClosureOptimizePrimitivesTest::testObjectCreateNonConstKey
  public void testObjectCreateNonConstKey() {
    testSame("goog.object.create('a',1,2,3,foo,bar);");
  }

// com.google.javascript.jscomp.ClosureOptimizePrimitivesTest::testObjectCreateOddParams
  public void testObjectCreateOddParams() {
    testSame("goog.object.create('a',1,2);");
  }

// com.google.javascript.jscomp.ClosureOptimizePrimitivesTest::testObjectCreate1
  public void testObjectCreate1() {
    test("var a = goog.object.create()", "var a = {}");
  }

// com.google.javascript.jscomp.ClosureOptimizePrimitivesTest::testObjectCreate2
  public void testObjectCreate2() {
    test("var a = goog$object$create('b',goog$object$create('c','d'))",
         "var a = {'b':{'c':'d'}};");
  }

// com.google.javascript.jscomp.ClosureOptimizePrimitivesTest::testObjectCreate3
  public void testObjectCreate3() {
    test("var a = goog.object.create(1,2)", "var a = {1:2}");
  }

// com.google.javascript.jscomp.ClosureOptimizePrimitivesTest::testObjectCreate4
  public void testObjectCreate4() {
    test("alert(goog.object.create(1,2).toString())",
         "alert({1:2}.toString())");
  }

// com.google.javascript.jscomp.ClosureOptimizePrimitivesTest::testObjectCreate5
  public void testObjectCreate5() {
    test("goog.object.create('a',2).toString()", "({'a':2}).toString()");
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDef1
  public void testGoogIsDef1() throws Exception {
    testClosureFunction("goog.isDef",
        createOptionalType(NUMBER_TYPE),
        NUMBER_TYPE,
        createOptionalType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDef2
  public void testGoogIsDef2() throws Exception {
    testClosureFunction("goog.isDef",
        createNullableType(NUMBER_TYPE),
        createNullableType(NUMBER_TYPE),
        createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsNull1
  public void testGoogIsNull1() throws Exception {
    testClosureFunction("goog.isNull",
        createOptionalType(NUMBER_TYPE),
        NULL_TYPE,
        createOptionalType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsNull2
  public void testGoogIsNull2() throws Exception {
    testClosureFunction("goog.isNull",
        createNullableType(NUMBER_TYPE),
        NULL_TYPE,
        NUMBER_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDefAndNotNull1
  public void testGoogIsDefAndNotNull1() throws Exception {
    testClosureFunction("goog.isDefAndNotNull",
        createOptionalType(NUMBER_TYPE),
        NUMBER_TYPE,
        createOptionalType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDefAndNotNull2
  public void testGoogIsDefAndNotNull2() throws Exception {
    testClosureFunction("goog.isDefAndNotNull",
        createNullableType(NUMBER_TYPE),
        NUMBER_TYPE,
        createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDefAndNotNull3
  public void testGoogIsDefAndNotNull3() throws Exception {
    testClosureFunction("goog.isDefAndNotNull",
        createOptionalType(createNullableType(NUMBER_TYPE)),
        NUMBER_TYPE,
        createOptionalType(createNullableType(NUMBER_TYPE)));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsString1
  public void testGoogIsString1() throws Exception {
    testClosureFunction("goog.isString",
        createNullableType(STRING_TYPE),
        STRING_TYPE,
        NULL_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsString2
  public void testGoogIsString2() throws Exception {
    testClosureFunction("goog.isString",
        createNullableType(NUMBER_TYPE),
        createNullableType(NUMBER_TYPE),
        createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsBoolean1
  public void testGoogIsBoolean1() throws Exception {
    testClosureFunction("goog.isBoolean",
        createNullableType(BOOLEAN_TYPE),
        BOOLEAN_TYPE,
        NULL_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsBoolean2
  public void testGoogIsBoolean2() throws Exception {
    testClosureFunction("goog.isBoolean",
        createUnionType(BOOLEAN_TYPE, STRING_TYPE, NO_OBJECT_TYPE),
        BOOLEAN_TYPE,
        createUnionType(STRING_TYPE, NO_OBJECT_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsNumber
  public void testGoogIsNumber() throws Exception {
    testClosureFunction("goog.isNumber",
        createNullableType(NUMBER_TYPE),
        NUMBER_TYPE,
        NULL_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsFunction
  public void testGoogIsFunction() throws Exception {
    testClosureFunction("goog.isFunction",
        createNullableType(OBJECT_FUNCTION_TYPE),
        OBJECT_FUNCTION_TYPE,
        NULL_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsArray
  public void testGoogIsArray() throws Exception {
    testClosureFunction("goog.isArray",
        OBJECT_TYPE,
        ARRAY_TYPE,
        OBJECT_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsArrayOnNull
  public void testGoogIsArrayOnNull() throws Exception {
    testClosureFunction("goog.isArray",
        null,
        ARRAY_TYPE,
        null);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsFunctionOnNull
  public void testGoogIsFunctionOnNull() throws Exception {
    testClosureFunction("goog.isFunction",
        null,
        U2U_CONSTRUCTOR_TYPE,
        null);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsObjectOnNull
  public void testGoogIsObjectOnNull() throws Exception {
    testClosureFunction("goog.isObject",
        null,
        OBJECT_TYPE,
        null);
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testSimple
  public void testSimple() {
    inFunction("var x; var y; x=1; x; y=1; y; return y",
               "var x;        x=1; x; x=1; x; return x");

    inFunction("var x,y; x=1; x; y=1; y",
               "var x  ; x=1; x; x=1; x");

    inFunction("var x,y; x=1; y=2; y; x");

    inFunction("y=0; var x, y; y; x=0; x",
               "y=0; var y   ; y; y=0;y");

    inFunction("var x,y; x=1; y=x; y",
               "var x  ; x=1; x=x; x");

    inFunction("var x,y; x=1; y=x+1; y",
               "var x  ; x=1; x=x+1; x");

    inFunction("x=1; x; y=2; y; var x; var y",
               "x=1; x; x=2; x; var x");

    inFunction("var x=1; var y=x+1; return y",
               "var x=1;     x=x+1; return x");

    inFunction("var x=1; var y=0; x+=1; y");

    inFunction("var x=1; x+=1; var y=0; y",
               "var x=1; x+=1;     x=0; x");

    inFunction("var x=1; foo(bar(x+=1)); var y=0; y",
               "var x=1; foo(bar(x+=1));     x=0; x");

    inFunction("var y, x=1; f(x+=1, y)");

    inFunction("var x; var y; y += 1, y, x = 1; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testMergeThreeVarNames
  public void testMergeThreeVarNames() {
    inFunction("var x,y,z; x=1; x; y=1; y; z=1; z",
               "var x    ; x=1; x; x=1; x; x=1; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testDifferentBlock
  public void testDifferentBlock() {
    inFunction("if(1) { var x = 0; x } else { var y = 0; y }",
               "if(1) { var x = 0; x } else {     x = 0; x }");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testLoops
  public void testLoops() {
    inFunction("var x; while(1) { x; x = 1; var y = 1; y }");
    inFunction("var y = 1; y; while(1) { var x = 1; x }",
               "var y = 1; y; while(1) {     y = 1; y }");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testEscaped
  public void testEscaped() {
    inFunction("var x = 1; x; function f() { x };  var y = 0; y; f()");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testFor
  public void testFor() {
    inFunction("var x = 1; x; for (;;) var y; y = 1; y",
               "var x = 1; x; for (;;)      ; x = 1; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testForIn
  public void testForIn() {
    
    inFunction("var x = 1, k; x;      ; for (var y in k) { y }",
               "var x = 1, k; x;      ; for (var y in k) { y }");

    inFunction("var x = 1, k; x; y = 1; for (var y in k) { y }",
               "var x = 1, k; x; x = 1; for (    x in k) { x }");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testLoopInductionVar
  public void testLoopInductionVar() {
    inFunction(
        "for(var x = 0; x < 10; x++){}" +
        "for(var y = 0; y < 10; y++){}" +
        "for(var z = 0; z < 10; z++){}",

        "for(var x = 0; x < 10; x++){}" +
        "for(x = 0; x < 10; x++){}" +
        "for(x = 0; x < 10; x++){}");

    inFunction(
        "for(var x = 0; x < 10; x++){z}" +
        "for(var y = 0, z = 0; y < 10; y++){z}",

        "for(var x = 0; x < 10; x++){z}" +
        "for(var x = 0, z = 0; x < 10; x++){z}");

    inFunction("var x = 1; x; for (var y; y=1; ) {y}",
               "var x = 1; x; for (     ; x=1; ) {x}");

    inFunction("var x = 1; x; y = 1; while(y) var y; y",
               "var x = 1; x; x = 1; while(x); x");

    inFunction("var x = 1; x; f:var y; y=1",
               "var x = 1; x; x=1");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testSwitchCase
  public void testSwitchCase() {
    inFunction("var x = 1; switch(x) { case 1: var y; case 2: } y = 1; y",
               "var x = 1; switch(x) { case 1:        case 2: } x = 1; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testDuplicatedVar
  public void testDuplicatedVar() {
    
    inFunction("z = 1; var x = 0; x; z; var y = 2, z = 1; y; z;",
               "z = 1; var x = 0; x; z; var x = 2, z = 1; x; z;");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testTryCatch
  public void testTryCatch() {
    inFunction("try {} catch (e) { } var x = 4; x;",
               "try {} catch (e) { } var x = 4; x;");
    inFunction("var x = 4; x; try {} catch (e) { }",
               "var x = 4; x; try {} catch (e) { }");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testDeadAssignment
  public void testDeadAssignment() {
    inFunction("var x = 6; var y; y = 4 ; x");
    inFunction("var y = 3; var y; y += 4; x");
    inFunction("var y = 3; var y; y ++  ; x");
    inFunction("y = 3; var x; var y = 1 ; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testParameter
  public void testParameter() {
    test("function FUNC(param) {var x = 0; x}",
         "function FUNC(param) {param = 0; param}");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testParameter2
  public void testParameter2() {
    
    test("function FUNC(x,y) {x = 0; x; y = 0; y}");
    test("function FUNC(x,y,z) {x = 0; x; y = 0; z = 0; z}");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testParameter3
  public void testParameter3() {
    
    test("function FUNC(x) {var y; y = 0; x; y}");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testParameter4
  public void testParameter4() {
    
    
    test("function FUNC(x, y) {var a,b; y; a=0; a; x; b=0; b}",
         "function FUNC(x, y) {var a; y; a=0; a; x; a=0; a}");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testParameter4b
  public void testParameter4b() {
    
    test("function FUNC(x, y, z) {var a,b; y; a=0; a; x; b=0; b}",
         "function FUNC(x, y, z) {         y; y=0; y; x; x=0; x}");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testLiveRangeChangeWithinCfgNode
  public void testLiveRangeChangeWithinCfgNode() {
    inFunction("var x, y; x = 1, y = 2, y, x");
    inFunction("var x, y; x = 1,x; y");

    
    inFunction("var x; var y; y = 1, y, x = 1; x");
    inFunction("var x; var y; y = 1; y, x = 1; x", "var x; x = 1; x, x = 1; x");
    inFunction("var x, y; y = 1, x = 1, x, y += 1, y");
    inFunction("var x, y; y = 1, x = 1, x, y ++, y");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testLiveRangeChangeWithinCfgNode2
  public void testLiveRangeChangeWithinCfgNode2() {
    inFunction("var x; var y; var a; var b;" +
               "y = 1, a = 1, y, a, x = 1, b = 1; x; b");
    inFunction("var x; var y; var a; var b;" +
               "y = 1, a = 1, y, a, x = 1; x; b = 1; b",
               "var x; var y; var a;       " +
               "y = 1, a = 1, y, a, x = 1; x; x = 1; x");
    inFunction("var x; var y; var a; var b;" +
               "y = 1, a = 1, y, x = 1; a; x; b = 1; b",
               "var x; var y; var a;       " +
               "y = 1, a = 1, y, x = 1; a; x; x = 1; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testFunctionNameReuse
  public void testFunctionNameReuse() {

  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testBug1401831
  public void testBug1401831() {
    
    
    String src = "function f(opt_a2) {" +
        "  var buffer;" +
        "  if (opt_a2) {" +
        "    for(var i = 0; i < arguments.length; i++) {" +
        "      buffer += arguments[i];" +
        "    }" +
        "  }" +
        "  return buffer;" +
        "}";
    test(src, src);
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testDeterministic
  public void testDeterministic() {
    
    
    
    
    
    
    
    
    
    
    inFunction("var a,b,c,d,e;" +
               "  a=1; b=1; a; b;" +
               "  b=1; c=1; b; c;" +
               "  c=1; d=1; c; d;" +
               "  d=1; e=1; d; e;" +
               "  e=1; a=1; e; a;",

               "var a,b,    e;" +
               "  a=1; b=1; a; b;" +
               "  b=1; a=1; b; a;" +
               "  a=1; b=1; a; b;" +
               "  b=1; e=1; b; e;" +
               "  e=1; a=1; e; a;");

    
    
    
    
    
    inFunction("var d,a,b,c,e;" +
               "  a=1; b=1; a; b;" +
               "  b=1; c=1; b; c;" +
               "  c=1; d=1; c; d;" +
               "  d=1; e=1; d; e;" +
               "  e=1; a=1; e; a;",

               "var d,  b,c  ;" +
               "  d=1; b=1; d; b;" +
               "  b=1; c=1; b; c;" +
               "  c=1; d=1; c; d;" +
               "  d=1; b=1; d; b;" +
               "  b=1; d=1; b; d;");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testVarLiveRangeCross
  public void testVarLiveRangeCross() {
    inFunction("var a={}; var b=a.S(); b",
               "var a={};     a=a.S(); a");
    inFunction("var a={}; var b=a.S(), c=b.SS(); b; c",
               "var a={}; var b=a.S(), a=b.SS(); b; a");
    inFunction("var a={}; var b=a.S(), c=a.SS(), d=a.SSS(); b; c; d",
               "var a={}; var b=a.S(), c=a.SS(), a=a.SSS(); b; c; a");
    inFunction("var a={}; var b=a.S(), c=a.SS(), d=a.SSS(); b; c; d",
               "var a={}; var b=a.S(), c=a.SS(), a=a.SSS(); b; c; a");
    inFunction("var a={}; d=1; d; var b=a.S(), c=a.SS(), d=a.SSS(); b; c; d");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testBug1445366
  public void testBug1445366() {
    
    inFunction(
        " var iframe = getFrame();" +
        " try {" +
        "   var win = iframe.contentWindow;" +
        " } catch (e) {" +
        " } finally {" +
        "   if (win)" +
        "     this.setupWinUtil_();" +
        "   else" +
        "     this.load();" +
        " }");

    
    inFunction(
        " var iframe = getFrame();" +
        " var win = iframe.contentWindow;" +
        " if (win)" +
        "   this.setupWinUtil_();" +
        " else" +
        "   this.load();",

        " var iframe = getFrame();" +
        " iframe = iframe.contentWindow;" +
        " if (iframe)" +
        "   this.setupWinUtil_();" +
        " else" +
        "   this.load();");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testCannotReuseAnyParamsBug
  public void testCannotReuseAnyParamsBug() {
    testSame("function handleKeyboardShortcut(e, key, isModifierPressed) {\n" +
        "  if (!isModifierPressed) {\n" +
        "    return false;\n" +
        "  }\n" +
        "  var command;\n" +
        "  switch (key) {\n" +
        "    case 'b': 
        "      command = COMMAND.BOLD;\n" +
        "      break;\n" +
        "    case 'i': 
        "      command = COMMAND.ITALIC;\n" +
        "      break;\n" +
        "    case 'u': 
        "      command = COMMAND.UNDERLINE;\n" +
        "      break;\n" +
        "    case 's': 
        "      return true;\n" +
        "  }\n" +
        "\n" +
        "  if (command) {\n" +
        "    this.fieldObject.execCommand(command);\n" +
        "    return true;\n" +
        "  }\n" +
        "\n" +
        "  return false;\n" +
        "};");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testForInWithAssignment
  public void testForInWithAssignment() {
    inFunction(
      "var _f = function (commands) {" +
          "  var k, v, ref;" +
          "  for (k in ref = commands) {" +
          "    v = ref[k];" +
          "    alert(k + ':' + v);" +
          "  }" +
          "}",

      "var _f = function (commands) {" +
          "  var k,ref;" +
          "  for (k in ref = commands) {" +
          "    commands = ref[k];" +
          "    alert(k + ':' + commands);" +
          "  }" +
          "}"
        );
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testUsePseduoNames
  public void testUsePseduoNames() {
    usePseudoName = true;
    inFunction("var x   = 0; print(x  ); var   y = 1; print(  y)",
               "var x_y = 0; print(x_y);     x_y = 1; print(x_y)");

    inFunction("var x_y = 1; var x   = 0; print(x  ); var     y = 1;" +
               "print(  y); print(x_y);",

               "var x_y = 1; var x_y$ = 0; print(x_y$);     x_y$ = 1;" + "" +
               "print(x_y$); print(x_y);");

    inFunction("var x_y = 1; function f() {" +
               "var x    = 0; print(x  ); var y = 1; print( y);" +
               "print(x_y);}",

               "var x_y = 1; function f() {" +
               "var x_y$ = 0; print(x_y$); x_y$ = 1; print(x_y$);" +
               "print(x_y);}");

    inFunction("var x   = 0; print(x  ); var   y = 1; print(  y); " +
               "var closure_var; function bar() { print(closure_var); }",
               "var x_y = 0; print(x_y);     x_y = 1; print(x_y); " +
               "var closure_var; function bar() { print(closure_var); }");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testMaxVars
  public void testMaxVars() {
    String code = "";
    for (int i = 0;
         i < LiveVariablesAnalysis.MAX_VARIABLES_TO_ANALYZE + 1; i++) {
      code += String.format("var x%d = 0; print(x%d);", i, i);
    }
    inFunction(code);
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrint
  public void testPrint() {
    assertPrint("10 + a + b", "10+a+b");
    assertPrint("10 + (30*50)", "10+30*50");
    assertPrint("with(x) { x + 3; }", "with(x)x+3");
    assertPrint("\"aa'a\"", "\"aa'a\"");
    assertPrint("\"aa\\\"a\"", "'aa\"a'");
    assertPrint("function foo()\n{return 10;}", "function foo(){return 10}");
    assertPrint("a instanceof b", "a instanceof b");
    assertPrint("typeof(a)", "typeof a");
    assertPrint(
        "var foo = x ? { a : 1 } : {a: 3, b:4, \"default\": 5, \"foo-bar\": 6}",
        "var foo=x?{a:1}:{a:3,b:4,\"default\":5,\"foo-bar\":6}");

    
    assertPrint("function foo(){throw 'error';}",
        "function foo(){throw\"error\";}");
    
    assertPrint("if (true) function foo(){return}",
        "if(true){function foo(){return}}");

    assertPrint("var x = 10; { var y = 20; }", "var x=10;var y=20");

    assertPrint("while (x-- > 0);", "while(x-- >0);");
    assertPrint("x-- >> 1", "x-- >>1");

    assertPrint("(function () {})(); ",
        "(function(){})()");

    
    assertPrint("var a,b,c,d;a || (b&& c) && (a || d)",
        "var a,b,c,d;a||b&&c&&(a||d)");
    assertPrint("var a,b,c; a || (b || c); a * (b * c); a | (b | c)",
        "var a,b,c;a||b||c;a*b*c;a|b|c");
    assertPrint("var a,b,c; a / b / c;a / (b / c); a - (b - c);",
        "var a,b,c;a/b/c;a/(b/c);a-(b-c)");
    assertPrint("var a,b; a = b = 3;",
        "var a,b;a=b=3");
    assertPrint("var a,b,c,d; a = (b = c = (d = 3));",
        "var a,b,c,d;a=b=c=d=3");
    assertPrint("var a,b,c; a += (b = c += 3);",
        "var a,b,c;a+=b=c+=3");
    assertPrint("var a,b,c; a *= (b -= c);",
        "var a,b,c;a*=b-=c");

    
    assertPrint("'<script>'", "\"<script>\"");
    assertPrint("'</script>'", "\"<\\/script>\"");
    assertPrint("\"</script> </SCRIPT>\"", "\"<\\/script> <\\/SCRIPT>\"");

    assertPrint("'-->'", "\"--\\>\"");
    assertPrint("']]>'", "\"]]\\>\"");
    assertPrint("' --></script>'", "\" --\\><\\/script>\"");

    assertPrint("/--> <\\/script>/g", "/--\\> <\\/script>/g");

    
    
    assertPrint("'<!-- I am a string -->'", "\"<\\!-- I am a string --\\>\"");

    
    assertPrint("a ? delete b[0] : 3", "a?delete b[0]:3");
    assertPrint("(delete a[0])/10", "delete a[0]/10");

    

    
    assertPrint("new A", "new A");
    assertPrint("new A()", "new A");
    assertPrint("new A('x')", "new A(\"x\")");

    
    assertPrint("new A().a()", "(new A).a()");
    assertPrint("(new A).a()", "(new A).a()");

    
    assertPrint("new A('y').a()", "(new A(\"y\")).a()");

    
    assertPrint("new A.B", "new A.B");
    assertPrint("new A.B()", "new A.B");
    assertPrint("new A.B('z')", "new A.B(\"z\")");

    
    assertPrint("(new A.B).a()", "(new A.B).a()");
    assertPrint("new A.B().a()", "(new A.B).a()");
    
    assertPrint("new A.B('w').a()", "(new A.B(\"w\")).a()");

    
    assertPrint("x + +y", "x+ +y");
    assertPrint("x - (-y)", "x- -y");
    assertPrint("x++ +y", "x++ +y");
    assertPrint("x-- -y", "x-- -y");
    assertPrint("x++ -y", "x++-y");

    
    assertPrint("foo:for(;;){break foo;}", "foo:for(;;)break foo");
    assertPrint("foo:while(1){continue foo;}", "foo:while(1)continue foo");

    
    assertPrint("({})", "({})");
    assertPrint("var x = {};", "var x={}");
    assertPrint("({}).x", "({}).x");
    assertPrint("({})['x']", "({})[\"x\"]");
    assertPrint("({}) instanceof Object", "({})instanceof Object");
    assertPrint("({}) || 1", "({})||1");
    assertPrint("1 || ({})", "1||{}");
    assertPrint("({}) ? 1 : 2", "({})?1:2");
    assertPrint("0 ? ({}) : 2", "0?{}:2");
    assertPrint("0 ? 1 : ({})", "0?1:{}");
    assertPrint("typeof ({})", "typeof{}");
    assertPrint("f({})", "f({})");

    
    assertPrint("(function(){})", "(function(){})");
    assertPrint("(function(){})()", "(function(){})()");
    assertPrint("(function(){})instanceof Object",
        "(function(){})instanceof Object");
    assertPrint("(function(){}).bind().call()",
        "(function(){}).bind().call()");
    assertPrint("var x = function() { };", "var x=function(){}");
    assertPrint("var x = function() { }();", "var x=function(){}()");
    assertPrint("(function() {}), 2", "(function(){}),2");

    
    assertPrint("(function f(){})", "(function f(){})");

    
    assertPrint("function f(){}", "function f(){}");

    
    assertPrint("({ 'a': 4, '\\u0100': 4 })", "({\"a\":4,\"\\u0100\":4})");
    assertPrint("({ a: 4, '\\u0100': 4 })", "({a:4,\"\\u0100\":4})");

    
    assertPrint("if (true) { alert();}", "if(true)alert()");
    assertPrint("if (false) {} else {alert(\"a\");}",
        "if(false);else alert(\"a\")");
    assertPrint("for(;;) { alert();};", "for(;;)alert()");

    assertPrint("do { alert(); } while(true);",
        "do alert();while(true)");
    assertPrint("myLabel: { alert();}",
        "myLabel:alert()");
    assertPrint("myLabel: for(;;) continue myLabel;",
        "myLabel:for(;;)continue myLabel");

    
    assertPrint("if (true) var x; x = 4;", "if(true)var x;x=4");

    
    assertPrint("\\u00fb", "\\u00fb");
    assertPrint("\\u00fa=1", "\\u00fa=1");
    assertPrint("function \\u00f9(){}", "function \\u00f9(){}");
    assertPrint("x.\\u00f8", "x.\\u00f8");
    assertPrint("x.\\u00f8", "x.\\u00f8");
    assertPrint("abc\\u4e00\\u4e01jkl", "abc\\u4e00\\u4e01jkl");

    
    assertPrint("! ! true", "!!true");
    assertPrint("!(!(true))", "!!true");
    assertPrint("typeof(void(0))", "typeof void 0");
    assertPrint("typeof(void(!0))", "typeof void!0");
    assertPrint("+ - + + - + 3", "+-+ +-+3"); 
    assertPrint("+(--x)", "+--x");
    assertPrint("-(++x)", "-++x");

    
    assertPrint("-(--x)", "- --x");
    assertPrint("!(~~5)", "!~~5");
    assertPrint("~(a/b)", "~(a/b)");

    
    assertPrint("new (foo.bar()).factory(baz)", "new (foo.bar().factory)(baz)");
    assertPrint("new (bar()).factory(baz)", "new (bar().factory)(baz)");
    assertPrint("new (new foobar(x)).factory(baz)",
        "new (new foobar(x)).factory(baz)");

    
    assertPrint("a ? b : (c ? d : e)", "a?b:c?d:e");
    assertPrint("a ? (b ? c : d) : e", "a?b?c:d:e");
    assertPrint("(a ? b : c) ? d : e", "(a?b:c)?d:e");

    
    assertPrint("if (x) if (y); else;", "if(x)if(y);else;");

    
    assertPrint("a,b,c", "a,b,c");
    assertPrint("(a,b),c", "a,b,c");
    assertPrint("a,(b,c)", "a,b,c");
    assertPrint("x=a,b,c", "x=a,b,c");
    assertPrint("x=(a,b),c", "x=(a,b),c");
    assertPrint("x=a,(b,c)", "x=a,b,c");
    assertPrint("x=a,y=b,z=c", "x=a,y=b,z=c");
    assertPrint("x=(a,y=b,z=c)", "x=(a,y=b,z=c)");
    assertPrint("x=[a,b,c,d]", "x=[a,b,c,d]");
    assertPrint("x=[(a,b,c),d]", "x=[(a,b,c),d]");
    assertPrint("x=[(a,(b,c)),d]", "x=[(a,b,c),d]");
    assertPrint("x=[a,(b,c,d)]", "x=[a,(b,c,d)]");
    assertPrint("var x=(a,b)", "var x=(a,b)");
    assertPrint("var x=a,b,c", "var x=a,b,c");
    assertPrint("var x=(a,b),c", "var x=(a,b),c");
    assertPrint("var x=a,b=(c,d)", "var x=a,b=(c,d)");
    assertPrint("foo(a,b,c,d)", "foo(a,b,c,d)");
    assertPrint("foo((a,b,c),d)", "foo((a,b,c),d)");
    assertPrint("foo((a,(b,c)),d)", "foo((a,b,c),d)");
    assertPrint("f(a+b,(c,d,(e,f,g)))", "f(a+b,(c,d,e,f,g))");
    assertPrint("({}) , 1 , 2", "({}),1,2");
    assertPrint("({}) , {} , {}", "({}),{},{}");

    
    assertPrint("if (x){}", "if(x);");
    assertPrint("if(x);", "if(x);");
    assertPrint("if(x)if(y);", "if(x)if(y);");
    assertPrint("if(x){if(y);}", "if(x)if(y);");
    assertPrint("if(x){if(y){};;;}", "if(x)if(y);");
    assertPrint("if(x){;;function y(){};;}", "if(x){function y(){}}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrintArray
  public void testPrintArray() {
    assertPrint("[void 0, void 0]", "[void 0,void 0]");
    assertPrint("[undefined, undefined]", "[undefined,undefined]");
    assertPrint("[ , , , undefined]", "[,,,undefined]");
    assertPrint("[ , , , 0]", "[,,,0]");
  }

// com.google.javascript.jscomp.CodePrinterTest::testHook
  public void testHook() {
    assertPrint("a ? b = 1 : c = 2", "a?b=1:c=2");
    assertPrint("x = a ? b = 1 : c = 2", "x=a?b=1:c=2");
    assertPrint("(x = a) ? b = 1 : c = 2", "(x=a)?b=1:c=2");

    assertPrint("x, a ? b = 1 : c = 2", "x,a?b=1:c=2");
    assertPrint("x, (a ? b = 1 : c = 2)", "x,a?b=1:c=2");
    assertPrint("(x, a) ? b = 1 : c = 2", "(x,a)?b=1:c=2");

    assertPrint("a ? (x, b) : c = 2", "a?(x,b):c=2");
    assertPrint("a ? b = 1 : (x,c)", "a?b=1:(x,c)");

    assertPrint("a ? b = 1 : c = 2 + x", "a?b=1:c=2+x");
    assertPrint("(a ? b = 1 : c = 2) + x", "(a?b=1:c=2)+x");
    assertPrint("a ? b = 1 : (c = 2) + x", "a?b=1:(c=2)+x");

    assertPrint("a ? (b?1:2) : 3", "a?b?1:2:3");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrintInOperatorInForLoop
  public void testPrintInOperatorInForLoop() {
    
    
    
    assertPrint("var a={}; for (var i = (\"length\" in a); i;) {}",
        "var a={};for(var i=(\"length\"in a);i;);");
    assertPrint("var a={}; for (var i = (\"length\" in a) ? 0 : 1; i;) {}",
        "var a={};for(var i=(\"length\"in a)?0:1;i;);");
    assertPrint("var a={}; for (var i = (\"length\" in a) + 1; i;) {}",
        "var a={};for(var i=(\"length\"in a)+1;i;);");
    assertPrint("var a={};for (var i = (\"length\" in a|| \"size\" in a);;);",
        "var a={};for(var i=(\"length\"in a)||(\"size\"in a);;);");
    assertPrint("var a={};for (var i = a || a || (\"size\" in a);;);",
        "var a={};for(var i=a||a||(\"size\"in a);;);");

    
    assertPrint("var a={}; for (var i = -(\"length\" in a); i;) {}",
        "var a={};for(var i=-(\"length\"in a);i;);");
    assertPrint("var a={};function b_(p){ return p;};" +
        "for(var i=1,j=b_(\"length\" in a);;) {}",
        "var a={};function b_(p){return p}" +
            "for(var i=1,j=b_(\"length\"in a);;);");

    
    assertPrint("var a={}; for (;(\"length\" in a);) {}",
        "var a={};for(;\"length\"in a;);");
  }

// com.google.javascript.jscomp.CodePrinterTest::testLiteralProperty
  public void testLiteralProperty() {
    assertPrint("(64).toString()", "(64).toString()");
  }

// com.google.javascript.jscomp.CodePrinterTest::testAmbiguousElseClauses
  public void testAmbiguousElseClauses() {
    assertPrintNode("if(x)if(y);else;",
        new Node(Token.IF,
            Node.newString(Token.NAME, "x"),
            new Node(Token.BLOCK,
                new Node(Token.IF,
                    Node.newString(Token.NAME, "y"),
                    new Node(Token.BLOCK),

                    
                    new Node(Token.BLOCK)))));

    assertPrintNode("if(x){if(y);}else;",
        new Node(Token.IF,
            Node.newString(Token.NAME, "x"),
            new Node(Token.BLOCK,
                new Node(Token.IF,
                    Node.newString(Token.NAME, "y"),
                    new Node(Token.BLOCK))),

            
            new Node(Token.BLOCK)));

    assertPrintNode("if(x)if(y);else{if(z);}else;",
        new Node(Token.IF,
            Node.newString(Token.NAME, "x"),
            new Node(Token.BLOCK,
                new Node(Token.IF,
                    Node.newString(Token.NAME, "y"),
                    new Node(Token.BLOCK),
                    new Node(Token.BLOCK,
                        new Node(Token.IF,
                            Node.newString(Token.NAME, "z"),
                            new Node(Token.BLOCK))))),

            
            new Node(Token.BLOCK)));
  }

// com.google.javascript.jscomp.CodePrinterTest::testLineBreak
  public void testLineBreak() {
    
    assertLineBreak("function a() {}\n" +
        "function b() {}",
        "function a(){}\n" +
        "function b(){}\n");

    
    assertLineBreak("var a = {};\n" +
        "a.foo = function () {}\n" +
        "function b() {}",
        "var a={};a.foo=function(){};\n" +
        "function b(){}\n");

    
    assertLineBreak("var a = {\n" +
        "  b: function() {},\n" +
        "  c: function() {}\n" +
        "};\n" +
        "alert(a);",

        "var a={b:function(){},\n" +
        "c:function(){}};\n" +
        "alert(a)");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPreferLineBreakAtEndOfFile
  public void testPreferLineBreakAtEndOfFile() {
    
    assertLineBreakAtEndOfFile(
        "\"1234567890\";",
        "\"1234567890\"",
        "\"1234567890\"");

    
    assertLineBreakAtEndOfFile(
        "\"123456789012345678901234567890\";\"1234567890\"",
        "\"123456789012345678901234567890\";\n\"1234567890\"",
        "\"123456789012345678901234567890\"; \"1234567890\";\n");
    assertLineBreakAtEndOfFile(
        "var12345678901234567890123456 instanceof Object;",
        "var12345678901234567890123456 instanceof\nObject",
        "var12345678901234567890123456 instanceof Object;\n");

    
    assertLineBreakAtEndOfFile(
        "\"1234567890\";\"12345678901234567890\";",
        "\"1234567890\";\"12345678901234567890\"",
        "\"1234567890\";\"12345678901234567890\";\n");

    
    assertLineBreakAtEndOfFile(
        "\"123456789012345678901234567890\";\"12345678901234567890\";",
        "\"123456789012345678901234567890\";\n\"12345678901234567890\"",
        "\"123456789012345678901234567890\";\n\"12345678901234567890\";\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrettyPrinter
  public void testPrettyPrinter() {
    
    
    assertPrettyPrint("(function(){})();","(function() {\n})();\n");
    assertPrettyPrint("var a = (function() {});alert(a);",
        "var a = function() {\n};\nalert(a);\n");

    
    
    assertPrettyPrint("if (1) {}",
        "if(1) {\n" +
        "}\n");
    assertPrettyPrint("if (1) {alert(\"\");}",
        "if(1) {\n" +
        "  alert(\"\")\n" +
        "}\n");
    assertPrettyPrint("if (1)alert(\"\");",
        "if(1) {\n" +
        "  alert(\"\")\n" +
        "}\n");
    assertPrettyPrint("if (1) {alert();alert();}",
        "if(1) {\n" +
        "  alert();\n" +
        "  alert()\n" +
        "}\n");

    
    assertPrettyPrint("label: alert();",
        "label:alert();\n");

    
    assertPrettyPrint("if (1) alert();",
        "if(1) {\n" +
        "  alert()\n" +
        "}\n");
    assertPrettyPrint("for (;;) alert();",
        "for(;;) {\n" +
        "  alert()\n" +
        "}\n");

    assertPrettyPrint("while (1) alert();",
        "while(1) {\n" +
        "  alert()\n" +
        "}\n");

    
    assertPrettyPrint("if (1) {} else {alert(a);}",
        "if(1) {\n" +
        "}else {\n  alert(a)\n}\n");

    
    assertPrettyPrint("if (1) alert(a); else alert(b);",
        "if(1) {\n" +
        "  alert(a)\n" +
        "}else {\n" +
        "  alert(b)\n" +
        "}\n");

    
    assertPrettyPrint("for(;;) { alert();}",
        "for(;;) {\n" +
         "  alert()\n" +
         "}\n");
    assertPrettyPrint("for(;;) {}",
        "for(;;) {\n" +
        "}\n");
    assertPrettyPrint("for(;;) { alert(); alert(); }",
        "for(;;) {\n" +
        "  alert();\n" +
        "  alert()\n" +
        "}\n");

    
    assertPrettyPrint("do { alert(); } while(true);",
        "do {\n" +
        "  alert()\n" +
        "}while(true);\n");

    
    assertPrettyPrint("myLabel: { alert();}",
        "myLabel: {\n" +
        "  alert()\n" +
        "}\n");

    
    
    assertPrettyPrint("myLabel: for(;;) continue myLabel;",
        "myLabel:for(;;) {\n" +
        "  continue myLabel\n" +
        "}\n");

    assertPrettyPrint("var a;", "var a;\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrettyPrinter2
  public void testPrettyPrinter2() {
    assertPrettyPrint(
        "if(true) f();",
        "if(true) {\n" +
        "  f()\n" +
        "}\n");

    assertPrettyPrint(
        "if (true) { f() } else { g() }",
        "if(true) {\n" +
        "  f()\n" +
        "}else {\n" +
        "  g()\n" +
        "}\n");

    assertPrettyPrint(
        "if(true) f(); for(;;) g();",
        "if(true) {\n" +
        "  f()\n" +
        "}\n" +
        "for(;;) {\n" +
        "  g()\n" +
        "}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrettyPrinter3
  public void testPrettyPrinter3() {
    assertPrettyPrint(
        "try {} catch(e) {}if (1) {alert();alert();}",
        "try {\n" +
        "}catch(e) {\n" +
        "}\n" +
        "if(1) {\n" +
        "  alert();\n" +
        "  alert()\n" +
        "}\n");

    assertPrettyPrint(
        "try {} finally {}if (1) {alert();alert();}",
        "try {\n" +
        "}finally {\n" +
        "}\n" +
        "if(1) {\n" +
        "  alert();\n" +
        "  alert()\n" +
        "}\n");

    assertPrettyPrint(
        "try {} catch(e) {} finally {} if (1) {alert();alert();}",
        "try {\n" +
        "}catch(e) {\n" +
        "}finally {\n" +
        "}\n" +
        "if(1) {\n" +
        "  alert();\n" +
        "  alert()\n" +
        "}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrettyPrinter4
  public void testPrettyPrinter4() {
    assertPrettyPrint(
        "function f() {}if (1) {alert();}",
        "function f() {\n" +
        "}\n" +
        "if(1) {\n" +
        "  alert()\n" +
        "}\n");

    assertPrettyPrint(
        "var f = function() {};if (1) {alert();}",
        "var f = function() {\n" +
        "};\n" +
        "if(1) {\n" +
        "  alert()\n" +
        "}\n");

    assertPrettyPrint(
        "(function() {})();if (1) {alert();}",
        "(function() {\n" +
        "})();\n" +
        "if(1) {\n" +
        "  alert()\n" +
        "}\n");

    assertPrettyPrint(
        "(function() {alert();alert();})();if (1) {alert();}",
        "(function() {\n" +
        "  alert();\n" +
        "  alert()\n" +
        "})();\n" +
        "if(1) {\n" +
        "  alert()\n" +
        "}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotations
  public void testTypeAnnotations() {
    assertTypeAnnotations(
        " function Foo(){}",
        "\n"
        + "function Foo() {\n}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsTypeDef
  public void testTypeAnnotationsTypeDef() {
    
    
    
    assertTypeAnnotations(
        " goog.java.Long;\n"
        + "\n"
        + "function f(a){};\n",
        "goog.java.Long;\n"
        + "\n"
        + "function f(a) {\n}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsAssign
  public void testTypeAnnotationsAssign() {
    assertTypeAnnotations(" var Foo = function(){}",
        "\n"
        + "var Foo = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsNamespace
  public void testTypeAnnotationsNamespace() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){}",
        "var a = {};\n"
        + "\n"
        + "a.Foo = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsMemberSubclass
  public void testTypeAnnotationsMemberSubclass() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){};"
        + " a.Bar = function(){}",
        "var a = {};\n"
        + "\n"
        + "a.Foo = function() {\n};\n"
        + "\n"
        + "a.Bar = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsInterface
  public void testTypeAnnotationsInterface() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){};"
        + " a.Bar = function(){}",
        "var a = {};\n"
        + "\n"
        + "a.Foo = function() {\n};\n"
        + "\n"
        + "a.Bar = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsMultipleInterface
  public void testTypeAnnotationsMultipleInterface() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo1 = function(){};"
        + " a.Foo2 = function(){};"
        + ""
        + "a.Bar = function(){}",
        "var a = {};\n"
        + "\n"
        + "a.Foo1 = function() {\n};\n"
        + "\n"
        + "a.Foo2 = function() {\n};\n"
        + "\n"
        + "a.Bar = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsMember
  public void testTypeAnnotationsMember() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){}"
        + "\n"
        + "a.Foo.prototype.foo = function(foo) { return 3; };"
        + ""
        + "a.Foo.prototype.bar = '';",
        "var a = {};\n"
        + "\n"
        + "a.Foo = function() {\n};\n"
        + "\n"
        + "a.Foo.prototype.foo = function(foo) {\n  return 3\n};\n"
        + "\n"
        + "a.Foo.prototype.bar = \"\";\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsImplements
  public void testTypeAnnotationsImplements() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){};\n"
        + " a.I = function(){};\n"
        + " a.I2 = function(){};\n"
        + " a.Bar = function(){}",
        "var a = {};\n"
        + "\n"
        + "a.Foo = function() {\n};\n"
        + "\n"
        + "a.I = function() {\n};\n"
        + "\n"
        + "a.I2 = function() {\n};\n"
        + "\n"
        + "a.Bar = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsDispatcher1
  public void testTypeAnnotationsDispatcher1() {
    assertTypeAnnotations(
        "var a = {};\n" +
        "\n" +
        "a.Foo = function(){}",
        "var a = {};\n" +
        "\n" +
        "a.Foo = function() {\n" +
        "};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsDispatcher2
  public void testTypeAnnotationsDispatcher2() {
    assertTypeAnnotations(
        "var a = {};\n" +
        "\n" +
        "a.Foo = function(){}\n" +
        "\n" +
        "a.Foo.prototype.foo = function() {};",

        "var a = {};\n" +
        "\n" +
        "a.Foo = function() {\n" +
        "};\n" +
        "\n" +
        "a.Foo.prototype.foo = function() {\n" +
        "};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testU2UFunctionTypeAnnotation
  public void testU2UFunctionTypeAnnotation() {
    assertTypeAnnotations(
        " var x = function() {}",
        "\nvar x = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testEmitUnknownParamTypesAsAllType
  public void testEmitUnknownParamTypesAsAllType() {
    assertTypeAnnotations(
        "var a = function(x) {}",
        "\n" +
        "var a = function(x) {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testOptionalTypesAnnotation
  public void testOptionalTypesAnnotation() {
    assertTypeAnnotations(
        "\n" +
        "var a = function(x) {}",
        "\n" +
        "var a = function(x) {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testVariableArgumentsTypesAnnotation
  public void testVariableArgumentsTypesAnnotation() {
    assertTypeAnnotations(
        "\n" +
        "var a = function(x) {}",
        "\n" +
        "var a = function(x) {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTempConstructor
  public void testTempConstructor() {
    assertTypeAnnotations(
        "var x = function() {\n\nfunction t1() {}\n" +
        " \nfunction t2() {}\n" +
        " t1.prototype = t2.prototype}",
        "\nvar x = function() {\n" +
        "  \n" +
        "function t1() {\n  }\n" +
        "  \n" +
        "function t2() {\n  }\n" +
        "  t1.prototype = t2.prototype\n};\n"
    );
  }

// com.google.javascript.jscomp.CodePrinterTest::testEnumAnnotation1
  public void testEnumAnnotation1() {
    assertTypeAnnotations(
        " var Enum = {FOO: 'x', BAR: 'y'};",
        "\nvar Enum = {FOO:\"x\", BAR:\"y\"};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testEnumAnnotation2
  public void testEnumAnnotation2() {
    assertTypeAnnotations(
        "var goog = goog || {};" +
        " goog.Enum = {FOO: 'x', BAR: 'y'};" +
        " goog.Enum2 = goog.x ? {} : goog.Enum;",
        "var goog = goog || {};\n" +
        "\ngoog.Enum = {FOO:\"x\", BAR:\"y\"};\n" +
        "\ngoog.Enum2 = goog.x ? {} : goog.Enum;\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testSubtraction
  public void testSubtraction() {
    Compiler compiler = new Compiler();
    Node n = compiler.parseTestCode("x - -4");
    assertEquals(0, compiler.getErrorCount());

    assertEquals(
        "x- -4",
        new CodePrinter.Builder(n).setLineLengthThreshold(
            CodePrinter.DEFAULT_LINE_LENGTH_THRESHOLD).build());
  }

// com.google.javascript.jscomp.CodePrinterTest::testFunctionWithCall
  public void testFunctionWithCall() {
    assertPrint(
        "var user = new function() {"
        + "alert(\"foo\")}",
        "var user=new function(){"
        + "alert(\"foo\")}");
    assertPrint(
        "var user = new function() {"
        + "this.name = \"foo\";"
        + "this.local = function(){alert(this.name)};}",
        "var user=new function(){"
        + "this.name=\"foo\";"
        + "this.local=function(){alert(this.name)}}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testLineLength
  public void testLineLength() {
    
    assertLineLength("var aba,bcb,cdc",
        "var aba,bcb," +
        "\ncdc");

    
    assertLineLength(
        "\"foo\"+\"bar,baz,bomb\"+\"whee\"+\";long-string\"\n+\"aaa\"",
        "\"foo\"+\"bar,baz,bomb\"+" +
        "\n\"whee\"+\";long-string\"+" +
        "\n\"aaa\"");

    
    assertLineLength("var abazaba=1234",
        "var abazaba=" +
        "\n1234");

    
    assertLineLength("var abab=1;var bab=2",
        "var abab=1;" +
        "\nvar bab=2");

    
    assertLineLength("var a=/some[reg](ex),with.*we?rd|chars/i;var b=a",
        "var a=/some[reg](ex),with.*we?rd|chars/i;" +
        "\nvar b=a");

    
    assertLineLength("var a=\"foo,{bar};baz\";var b=a",
        "var a=\"foo,{bar};baz\";" +
        "\nvar b=a");

    
    assertLineLength("var a=\"a\";a++;var b=\"bbb\";",
        "var a=\"a\";a++;\n" +
        "var b=\"bbb\"");
  }

// com.google.javascript.jscomp.CodePrinterTest::testParsePrintParse
  public void testParsePrintParse() {
    testReparse("3;");
    testReparse("var a = b;");
    testReparse("var x, y, z;");
    testReparse("try { foo() } catch(e) { bar() }");
    testReparse("try { foo() } catch(e) { bar() } finally { stuff() }");
    testReparse("try { foo() } finally { stuff() }");
    testReparse("throw 'me'");
    testReparse("function foo(a) { return a + 4; }");
    testReparse("function foo() { return; }");
    testReparse("var a = function(a, b) { foo(); return a + b; }");
    testReparse("b = [3, 4, 'paul', \"Buchhe it\",,5];");
    testReparse("v = (5, 6, 7, 8)");
    testReparse("d = 34.0; x = 0; y = .3; z = -22");
    testReparse("d = -x; t = !x + ~y;");
    testReparse("'hi';  stuff(a,b) \n" +
            " foo(); 
            " bar();");
    testReparse("a = b++ + ++c; a = b++-++c; a = - --b; a = - ++b;");
    testReparse("a++; b= a++; b = ++a; b = a--; b = --a; a+=2; b-=5");
    testReparse("a = (2 + 3) * 4;");
    testReparse("a = 1 + (2 + 3) + 4;");
    testReparse("x = a ? b : c; x = a ? (b,3,5) : (foo(),bar());");
    testReparse("a = b | c || d ^ e " +
            "&& f & !g != h << i <= j < k >>> l > m * n % !o");
    testReparse("a == b; a != b; a === b; a == b == a;" +
            " (a == b) == a; a == (b == a);");
    testReparse("if (a > b) a = b; if (b < 3) a = 3; else c = 4;");
    testReparse("if (a == b) { a++; } if (a == 0) { a++; } else { a --; }");
    testReparse("for (var i in a) b += i;");
    testReparse("for (var i = 0; i < 10; i++){ b /= 2;" +
            " if (b == 2)break;else continue;}");
    testReparse("for (x = 0; x < 10; x++) a /= 2;");
    testReparse("for (;;) a++;");
    testReparse("while(true) { blah(); }while(true) blah();");
    testReparse("do stuff(); while(a>b);");
    testReparse("[0, null, , true, false, this];");
    testReparse("s.replace(/absc/, 'X').replace(/ab/gi, 'Y');");
    testReparse("new Foo; new Bar(a, b,c);");
    testReparse("with(foo()) { x = z; y = t; } with(bar()) a = z;");
    testReparse("delete foo['bar']; delete foo;");
    testReparse("var x = { 'a':'paul', 1:'3', 2:(3,4) };");
    testReparse("switch(a) { case 2: case 3: stuff(); break;" +
        "case 4: morestuff(); break; default: done();}");
    testReparse("x = foo['bar'] + foo['my stuff'] + foo[bar] + f.stuff;");
    testReparse("a.v = b.v; x['foo'] = y['zoo'];");
    testReparse("'test' in x; 3 in x; a in x;");
    testReparse("'foo\"bar' + \"foo'c\" + 'stuff\\n and \\\\more'");
    testReparse("x.__proto__;");
  }

// com.google.javascript.jscomp.CodePrinterTest::testDoLoopIECompatiblity
  public void testDoLoopIECompatiblity() {
    
    assertPrint("function f(){if(e1){do foo();while(e2)}else foo()}",
        "function f(){if(e1){do foo();while(e2)}else foo()}");

    assertPrint("function f(){if(e1)do foo();while(e2)else foo()}",
        "function f(){if(e1){do foo();while(e2)}else foo()}");

    assertPrint("if(x){do{foo()}while(y)}else bar()",
        "if(x){do foo();while(y)}else bar()");

    assertPrint("if(x)do{foo()}while(y);else bar()",
        "if(x){do foo();while(y)}else bar()");

    assertPrint("if(x){do{foo()}while(y)}",
        "if(x){do foo();while(y)}");

    assertPrint("if(x)do{foo()}while(y);",
        "if(x){do foo();while(y)}");

    assertPrint("if(x)A:do{foo()}while(y);",
        "if(x){A:do foo();while(y)}");

    assertPrint("var i = 0;a: do{b: do{i++;break b;} while(0);} while(0);",
        "var i=0;a:do{b:do{i++;break b}while(0)}while(0)");
  }

// com.google.javascript.jscomp.CodePrinterTest::testFunctionSafariCompatiblity
  public void testFunctionSafariCompatiblity() {
    
    assertPrint("function f(){if(e1){function goo(){return true}}else foo()}",
        "function f(){if(e1){function goo(){return true}}else foo()}");

    assertPrint("function f(){if(e1)function goo(){return true}else foo()}",
        "function f(){if(e1){function goo(){return true}}else foo()}");

    assertPrint("if(e1){function goo(){return true}}",
        "if(e1){function goo(){return true}}");

    assertPrint("if(e1)function goo(){return true}",
        "if(e1){function goo(){return true}}");

    assertPrint("if(e1)A:function goo(){return true}",
        "if(e1){A:function goo(){return true}}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testExponents
  public void testExponents() {
    assertPrintNumber("1", 1);
    assertPrintNumber("10", 10);
    assertPrintNumber("100", 100);
    assertPrintNumber("1E3", 1000);
    assertPrintNumber("1E4", 10000);
    assertPrintNumber("1E5", 100000);
    assertPrintNumber("-1", -1);
    assertPrintNumber("-10", -10);
    assertPrintNumber("-100", -100);
    assertPrintNumber("-1E3", -1000);
    assertPrintNumber("-12341234E4", -123412340000L);
    assertPrintNumber("1E18", 1000000000000000000L);
    assertPrintNumber("1E5", 100000.0);
    assertPrintNumber("100000.1", 100000.1);

    assertPrintNumber("1E-6", 0.000001);
    assertPrintNumber("-0x38d7ea4c68001", -0x38d7ea4c68001L);
    assertPrintNumber("0x38d7ea4c68001", 0x38d7ea4c68001L);
  }

// com.google.javascript.jscomp.CodePrinterTest::testDirectEval
  public void testDirectEval() {
    assertPrint("eval('1');", "eval(\"1\")");
  }

// com.google.javascript.jscomp.CodePrinterTest::testIndirectEval
  public void testIndirectEval() {
    Node n = parse("eval('1');");
    assertPrintNode("eval(\"1\")", n);
    n.getFirstChild().getFirstChild().getFirstChild().putBooleanProp(
        Node.DIRECT_EVAL, false);
    assertPrintNode("(0,eval)(\"1\")", n);
  }

// com.google.javascript.jscomp.CodePrinterTest::testFreeCall1
  public void testFreeCall1() {
    assertPrint("foo(a);", "foo(a)");
    assertPrint("x.foo(a);", "x.foo(a)");
  }

// com.google.javascript.jscomp.CodePrinterTest::testFreeCall2
  public void testFreeCall2() {
    Node n = parse("foo(a);");
    assertPrintNode("foo(a)", n);
    Node call =  n.getFirstChild().getFirstChild();
    assertTrue(call.isCall());
    call.putBooleanProp(Node.FREE_CALL, true);
    assertPrintNode("foo(a)", n);
  }

// com.google.javascript.jscomp.CodePrinterTest::testFreeCall3
  public void testFreeCall3() {
    Node n = parse("x.foo(a);");
    assertPrintNode("x.foo(a)", n);
    Node call =  n.getFirstChild().getFirstChild();
    assertTrue(call.isCall());
    call.putBooleanProp(Node.FREE_CALL, true);
    assertPrintNode("(0,x.foo)(a)", n);
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrintScript
  public void testPrintScript() {
    
    
    Node ast = new Node(Token.SCRIPT,
        new Node(Token.EXPR_RESULT, Node.newString("f")),
        new Node(Token.EXPR_RESULT, Node.newString("g")));
    String result = new CodePrinter.Builder(ast).setPrettyPrint(true).build();
    assertEquals("\"f\";\n\"g\";\n", result);
  }

// com.google.javascript.jscomp.CodePrinterTest::testObjectLit
  public void testObjectLit() {
    assertPrint("({x:1})", "({x:1})");
    assertPrint("var x=({x:1})", "var x={x:1}");
    assertPrint("var x={'x':1}", "var x={\"x\":1}");
    assertPrint("var x={1:1}", "var x={1:1}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testObjectLit2
  public void testObjectLit2() {
    assertPrint("var x={1:1}", "var x={1:1}");
    assertPrint("var x={'1':1}", "var x={1:1}");
    assertPrint("var x={'1.0':1}", "var x={\"1.0\":1}");
    assertPrint("var x={1.5:1}", "var x={\"1.5\":1}");

  }

// com.google.javascript.jscomp.CodePrinterTest::testObjectLit3
  public void testObjectLit3() {
    assertPrint("var x={3E9:1}",
                "var x={3E9:1}");
    assertPrint("var x={'3000000000':1}", 
                "var x={3E9:1}");
    assertPrint("var x={'3000000001':1}",
                "var x={3000000001:1}");
    assertPrint("var x={'6000000001':1}",  
                "var x={6000000001:1}");
    assertPrint("var x={\"12345678901234567\":1}",  
                "var x={\"12345678901234567\":1}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testObjectLit4
  public void testObjectLit4() {
    
    assertPrint(
        "var x={\"123456789012345671234567890123456712345678901234567\":1}",
        "var x={\"123456789012345671234567890123456712345678901234567\":1}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testGetter
  public void testGetter() {
    assertPrint("var x = {}", "var x={}");
    assertPrint("var x = {get a() {return 1}}", "var x={get a(){return 1}}");
    assertPrint(
      "var x = {get a() {}, get b(){}}",
      "var x={get a(){},get b(){}}");

    assertPrint(
      "var x = {get 'a'() {return 1}}",
      "var x={get \"a\"(){return 1}}");

    assertPrint(
      "var x = {get 1() {return 1}}",
      "var x={get 1(){return 1}}");

    assertPrint(
      "var x = {get \"()\"() {return 1}}",
      "var x={get \"()\"(){return 1}}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testSetter
  public void testSetter() {
    assertPrint("var x = {}", "var x={}");
    assertPrint(
       "var x = {set a(y) {return 1}}",
       "var x={set a(y){return 1}}");

    assertPrint(
      "var x = {get 'a'() {return 1}}",
      "var x={get \"a\"(){return 1}}");

    assertPrint(
      "var x = {set 1(y) {return 1}}",
      "var x={set 1(y){return 1}}");

    assertPrint(
      "var x = {set \"(x)\"(y) {return 1}}",
      "var x={set \"(x)\"(y){return 1}}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testNegCollapse
  public void testNegCollapse() {
    
    
    assertPrint("var x = - - 2;", "var x=2");
    assertPrint("var x = - (2);", "var x=-2");
  }

// com.google.javascript.jscomp.CodePrinterTest::testStrict
  public void testStrict() {
    String result = parsePrint("var x", false, false, 0, false, true);
    assertEquals("'use strict';var x", result);
  }

// com.google.javascript.jscomp.CodePrinterTest::testArrayLiteral
  public void testArrayLiteral() {
    assertPrint("var x = [,];","var x=[,]");
    assertPrint("var x = [,,];","var x=[,,]");
    assertPrint("var x = [,s,,];","var x=[,s,,]");
    assertPrint("var x = [,s];","var x=[,s]");
    assertPrint("var x = [s,];","var x=[s]");
  }

// com.google.javascript.jscomp.CodePrinterTest::testZero
  public void testZero() {
    assertPrint("var x ='\\0';", "var x=\"\\x00\"");
    assertPrint("var x ='\\x00';", "var x=\"\\x00\"");
    assertPrint("var x ='\\u0000';", "var x=\"\\x00\"");
    assertPrint("var x ='\\u00003';", "var x=\"\\x003\"");
  }

// com.google.javascript.jscomp.CodePrinterTest::testUnicode
  public void testUnicode() {
    assertPrint("var x ='\\x0f';", "var x=\"\\u000f\"");
    assertPrint("var x ='\\x68';", "var x=\"h\"");
    assertPrint("var x ='\\x7f';", "var x=\"\\u007f\"");
  }

// com.google.javascript.jscomp.CodePrinterTest::testUnicodeKeyword
  public void testUnicodeKeyword() {
    
    assertPrint("var \\u0069\\u0066 = 1;", "var i\\u0066=1");
    
    assertPrint("var v\\u0061\\u0072 = 1;", "var va\\u0072=1");
    
    assertPrint("var w\\u0068\\u0069\\u006C\\u0065 = 1;"
        + "\\u0077\\u0068il\\u0065 = 2;"
        + "\\u0077h\\u0069le = 3;",
        "var whil\\u0065=1;whil\\u0065=2;whil\\u0065=3");
  }

// com.google.javascript.jscomp.CodePrinterTest::testNumericKeys
  public void testNumericKeys() {
    assertPrint("var x = {010: 1};", "var x={8:1}");
    assertPrint("var x = {'010': 1};", "var x={\"010\":1}");

    assertPrint("var x = {0x10: 1};", "var x={16:1}");
    assertPrint("var x = {'0x10': 1};", "var x={\"0x10\":1}");

    
    assertPrint("var x = {.2: 1};", "var x={\"0.2\":1}");
    assertPrint("var x = {'.2': 1};", "var x={\".2\":1}");

    assertPrint("var x = {0.2: 1};", "var x={\"0.2\":1}");
    assertPrint("var x = {'0.2': 1};", "var x={\"0.2\":1}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testIssue582
  public void testIssue582() {
    assertPrint("var x = -0.0;", "var x=-0");
  }

// com.google.javascript.jscomp.CodePrinterTest::testIssue601
  public void testIssue601() {
    assertPrint("'\\v' == 'v'", "\"\\v\"==\"v\"");
    assertPrint("'\\u000B' == '\\v'", "\"\\x0B\"==\"\\v\"");
    assertPrint("'\\x0B' == '\\v'", "\"\\x0B\"==\"\\v\"");
  }

// com.google.javascript.jscomp.CodePrinterTest::testIssue620
  public void testIssue620() {
    assertPrint("alert(/ / / / /);", "alert(/ 
    assertPrint("alert(/ 
  }

// com.google.javascript.jscomp.CodePrinterTest::testIssue5746867
  public void testIssue5746867() {
    assertPrint("var a = { '$\\\\' : 5 };", "var a={\"$\\\\\":5}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testCommaSpacing
  public void testCommaSpacing() {
    assertPrint("var a = (b = 5, c = 5);",
        "var a=(b=5,c=5)");
    assertPrettyPrint("var a = (b = 5, c = 5);",
        "var a = (b = 5, c = 5);\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testManyCommas
  public void testManyCommas() {
    int numCommas = 10000;
    List<String> numbers = Lists.newArrayList("0", "1");
    Node current = new Node(Token.COMMA, Node.newNumber(0), Node.newNumber(1));
    for (int i = 2; i < numCommas; i++) {
      current = new Node(Token.COMMA, current);

      
      int num = i % 1000;
      numbers.add(String.valueOf(num));
      current.addChildToBack(Node.newNumber(num));
    }

    String expected = Joiner.on(",").join(numbers);
    String actual = printNode(current).replace("\n", "");
    assertEquals(expected, actual);
  }

// com.google.javascript.jscomp.CodePrinterTest::testManyAdds
  public void testManyAdds() {
    int numAdds = 10000;
    List<String> numbers = Lists.newArrayList("0", "1");
    Node current = new Node(Token.ADD, Node.newNumber(0), Node.newNumber(1));
    for (int i = 2; i < numAdds; i++) {
      current = new Node(Token.ADD, current);

      
      int num = i % 1000;
      numbers.add(String.valueOf(num));
      current.addChildToBack(Node.newNumber(num));
    }

    String expected = Joiner.on("+").join(numbers);
    String actual = printNode(current).replace("\n", "");
    assertEquals(expected, actual);
  }

// com.google.javascript.jscomp.CodePrinterTest::testMinusNegativeZero
  public void testMinusNegativeZero() {
    
    
    assertPrint("x- -0", "x- -0");
  }

// com.google.javascript.jscomp.CodePrinterTest::testStringEscapeSequences
  public void testStringEscapeSequences() {
    
    assertPrintSame("var x=\"\\b\"");
    assertPrintSame("var x=\"\\f\"");
    assertPrintSame("var x=\"\\n\"");
    assertPrintSame("var x=\"\\r\"");
    assertPrintSame("var x=\"\\t\"");
    assertPrintSame("var x=\"\\v\"");
    assertPrint("var x=\"\\\"\"", "var x='\"'");
    assertPrint("var x=\"\\\'\"", "var x=\"'\"");

    
    assertPrint("var x=\"\\u000A\"", "var x=\"\\n\"");
    assertPrint("var x=\"\\u000D\"", "var x=\"\\r\"");
    assertPrintSame("var x=\"\\u2028\"");
    assertPrintSame("var x=\"\\u2029\"");

    
    assertPrintSame("var x=/\\b/");
    assertPrintSame("var x=/\\f/");
    assertPrintSame("var x=/\\n/");
    assertPrintSame("var x=/\\r/");
    assertPrintSame("var x=/\\t/");
    assertPrintSame("var x=/\\v/");
    assertPrintSame("var x=/\\u000A/");
    assertPrintSame("var x=/\\u000D/");
    assertPrintSame("var x=/\\u2028/");
    assertPrintSame("var x=/\\u2029/");
  }
