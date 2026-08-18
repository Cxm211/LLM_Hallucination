// buggy code
  String getReadableJSTypeName(Node n, boolean dereference) {

    // The best type name is the actual type name.

    // If we're analyzing a GETPROP, the property may be inherited by the
    // prototype chain. So climb the prototype chain and find out where
    // the property was originally defined.
    if (n.isGetProp()) {
      ObjectType objectType = getJSType(n.getFirstChild()).dereference();
      if (objectType != null) {
        String propName = n.getLastChild().getString();
        if (objectType.getConstructor() != null &&
            objectType.getConstructor().isInterface()) {
          objectType = FunctionType.getTopDefiningInterface(
              objectType, propName);
        } else {
          // classes
          while (objectType != null && !objectType.hasOwnProperty(propName)) {
            objectType = objectType.getImplicitPrototype();
          }
        }

        // Don't show complex function names or anonymous types.
        // Instead, try to get a human-readable type name.
        if (objectType != null &&
            (objectType.getConstructor() != null ||
             objectType.isFunctionPrototypeType())) {
          return objectType.toString() + "." + propName;
        }
      }
    }

    JSType type = getJSType(n);
    if (dereference) {
      ObjectType dereferenced = type.dereference();
      if (dereferenced != null) {
        type = dereferenced;
      }
    }
    if (type.isFunctionPrototypeType() ||
        (type.toObjectType() != null &&
         type.toObjectType().getConstructor() != null)) {
      return type.toString();
    }
    String qualifiedName = n.getQualifiedName();
    if (qualifiedName != null) {
      return qualifiedName;
    } else if (type.isFunctionType()) {
      // Don't show complex function names.
      return "function";
    } else {
      return type.toString();
    }
  }

// relevant test
// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject5
  public void testReferenceInAnonymousObject5() {
    test("function CreateClass(a) {}" +
         "var a = {};" +
         "a.b = CreateClass({c: function() {}});" +
         "a.d = CreateClass({c: a.b.prototype.c});",
         "function CreateClass(a$$1) {}" +
         "var a$b = CreateClass({c: function() {}});" +
         "var a$d = CreateClass({c: a$b.prototype.c});");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCrashInCommaOperator
  public void testCrashInCommaOperator() {
    test("var a = {}; a.b = function() {},a.b();",
         "var a$b; a$b=function() {},a$b();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCrashInNestedAssign
  public void testCrashInNestedAssign() {
    test("var a = {}; if (a.b = function() {}) a.b();",
         "var a$b; if (a$b=function() {}) { a$b(); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testTwinReferenceCancelsChildCollapsing
  public void testTwinReferenceCancelsChildCollapsing() {
    test("var a = {}; if (a.b = function() {}) { a.b.c = 3; a.b(a.b.c); }",
         "var a$b; if (a$b = function() {}) { a$b.c = 3; a$b(a$b.c); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign
  public void testPropWithDollarSign() {
    test("var a = {$: 3}", "var a$$0 = 3;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign2
  public void testPropWithDollarSign2() {
    test("var a = {$: function(){}}", "var a$$0 = function(){};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign3
  public void testPropWithDollarSign3() {
    test("var a = {b: {c: 3}, b$c: function(){}}",
         "var a$b$c = 3; var a$b$0c = function(){};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign4
  public void testPropWithDollarSign4() {
    test("var a = {$$: {$$$: 3}};", "var a$$0$0$$0$0$0 = 3;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign5
  public void testPropWithDollarSign5() {
    test("var a = {b: {$0c: true}, b$0c: false};",
         "var a$b$$00c = true; var a$b$00c = false;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testConstKey
  public void testConstKey() {
    test("var foo = {A: 3};", "var foo$A = 3;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertyOnGlobalCtor
  public void testPropertyOnGlobalCtor() {
    test(" function Map() {} Map.foo = 3; Map;",
         "function Map() {} var Map$foo = 3; Map;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertyOnGlobalInterface
  public void testPropertyOnGlobalInterface() {
    test(" function Map() {} Map.foo = 3; Map;",
         "function Map() {} var Map$foo = 3; Map;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertyOnGlobalFunction
  public void testPropertyOnGlobalFunction() {
    testSame("function Map() {} Map.foo = 3; Map;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testIssue389
  public void testIssue389() {
    test(
        "function alias() {}" +
        "var dojo = {};" +
        "dojo.gfx = {};" +
        "dojo.declare = function() {};" +
        "" +
        "dojo.gfx.Shape = function() {};" +
        "dojo.gfx.Shape = dojo.declare('dojo.gfx.Shape');" +
        "alias(dojo);",
        "function alias() {}" +
        "var dojo = {};" +
        "dojo.gfx = {};" +
        "dojo.declare = function() {};" +
        "" +
        "var dojo$gfx$Shape = function() {};" +
        "dojo$gfx$Shape = dojo.declare('dojo.gfx.Shape');" +
        "alias(dojo);",
        null,
        CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasedTopLevelName
  public void testAliasedTopLevelName() {
    testSame(
        "function alias() {}" +
        "var dojo = {};" +
        "dojo.gfx = {};" +
        "dojo.declare = function() {};" +
        "dojo.gfx.Shape = {SQUARE: 2};" +
        "dojo.gfx.Shape = dojo.declare('dojo.gfx.Shape');" +
        "alias(dojo);" +
        "alias(dojo$gfx$Shape$SQUARE);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasedTopLevelEnum
  public void testAliasedTopLevelEnum() {
    test(
        "function alias() {}" +
        "var dojo = {};" +
        "dojo.gfx = {};" +
        "dojo.declare = function() {};" +
        "" +
        "dojo.gfx.Shape = {SQUARE: 2};" +
        "dojo.gfx.Shape = dojo.declare('dojo.gfx.Shape');" +
        "alias(dojo);" +
        "alias(dojo.gfx.Shape.SQUARE);",
        "function alias() {}" +
        "var dojo = {};" +
        "dojo.gfx = {};" +
        "dojo.declare = function() {};" +
        "" +
        "var dojo$gfx$Shape = {SQUARE: 2};" +
        "dojo$gfx$Shape = dojo.declare('dojo.gfx.Shape');" +
        "alias(dojo);" +
        "alias(dojo$gfx$Shape.SQUARE);",
        null,
        CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAssignFunctionBeforeDefinition
  public void testAssignFunctionBeforeDefinition() {
    testSame(
        "f = function() {};" +
        "var f = null;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjectLitBeforeDefinition
  public void testObjectLitBeforeDefinition() {
    testSame(
        "a = {b: 3};" +
        "var a = null;" +
        "this.c = a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testTypedef1
  public void testTypedef1() {
    test("var foo = {};" +
         " foo.Baz;",
         "var foo = {}; var foo$Baz;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testTypedef2
  public void testTypedef2() {
    test("var foo = {};" +
         " foo.Bar.Baz;" +
         "foo.Bar = function() {};",
         "var foo$Bar$Baz; var foo$Bar = function(){};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testDelete1
  public void testDelete1() {
    testSame(
        "var foo = {};" +
        "foo.bar = 3;" +
        "delete foo.bar;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testDelete2
  public void testDelete2() {
    test(
        "var foo = {};" +
        "foo.bar = 3;" +
        "foo.baz = 3;" +
        "delete foo.bar;",
        "var foo = {};" +
        "foo.bar = 3;" +
        "var foo$baz = 3;" +
        "delete foo.bar;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testDelete3
  public void testDelete3() {
    testSame(
        "var foo = {bar: 3};" +
        "delete foo.bar;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testDelete4
  public void testDelete4() {
    test(
        "var foo = {bar: 3, baz: 3};" +
        "delete foo.bar;",
        "var foo$baz=3;var foo={bar:3};delete foo.bar");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testDelete5
  public void testDelete5() {
    test(
        "var x = {};" +
        "x.foo = {};" +
        "x.foo.bar = 3;" +
        "delete x.foo.bar;",
        "var x$foo = {};" +
        "x$foo.bar = 3;" +
        "delete x$foo.bar;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testDelete6
  public void testDelete6() {
    test(
        "var x = {};" +
        "x.foo = {};" +
        "x.foo.bar = 3;" +
        "x.foo.baz = 3;" +
        "delete x.foo.bar;",
        "var x$foo = {};" +
        "x$foo.bar = 3;" +
        "var x$foo$baz = 3;" +
        "delete x$foo.bar;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testDelete7
  public void testDelete7() {
    test(
        "var x = {};" +
        "x.foo = {bar: 3};" +
        "delete x.foo.bar;",
        "var x$foo = {bar: 3};" +
        "delete x$foo.bar;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testDelete8
  public void testDelete8() {
    test(
        "var x = {};" +
        "x.foo = {bar: 3, baz: 3};" +
        "delete x.foo.bar;",
        "var x$foo$baz = 3; var x$foo = {bar: 3};" +
        "delete x$foo.bar;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testDelete9
  public void testDelete9() {
    testSame(
        "var x = {};" +
        "x.foo = {};" +
        "x.foo.bar = 3;" +
        "delete x.foo;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testDelete10
  public void testDelete10() {
    testSame(
        "var x = {};" +
        "x.foo = {bar: 3};" +
        "delete x.foo;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testDelete11
  public void testDelete11() {
    
    test(
        "var x = {};" +
        "x.foo = {};" +
        " x.foo.Bar = function() {};" +
        "delete x.foo;",
        "var x = {};" +
        "x.foo = {};" +
        "var x$foo$Bar = function() {};" +
        "delete x.foo;",
        null,
        CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPreserveConstructorDoc
  public void testPreserveConstructorDoc() {
    test("var foo = {};" +
         "\n" +
         "foo.bar = function() {}",
         "var foo$bar = function() {}");

    Node root = getLastCompiler().getRoot();

    Node fooBarNode = findQualifiedNameNode("foo$bar", root);
    Node varNode = fooBarNode.getParent();
    assertTrue(varNode.isVar());
    assertTrue(varNode.getJSDocInfo().isConstructor());
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testTypeDefAlias1
  public void testTypeDefAlias1() {
    test(
        " var D = function() {};\n" +
        " D.L = function() {};\n" +
        " D.L.A = new D.L();\n" +
        "\n" +
        " var M = {};\n" +
        " M.L = D.L;\n" +
        "\n" +
        "use(M.L.A);",

        "var D = function() {};\n" +
        "var D$L = function() {};\n" +
        "var D$L$A = new D$L();\n" +
        "var M$L = null\n" +
        "use(D$L$A);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testTypeDefAlias2
  public void testTypeDefAlias2() {
    
    
    
    
    test(
        " var D = function() {};\n" +
        " D.L = function() {};\n" +
        " D.L.A = new D.L();\n" +
        "\n" +
        " var M = {};\n" +
        "if (random) {  M.L = D.L; }\n" +
        "\n" +
        "use(M.L);\n" +
        "use(M.L.A);\n",

        "var D = function() {};\n" +
        "var D$L = function() {};\n" +
        "var D$L$A = new D$L();\n" +
        "if (random) { var M$L = D$L; }\n" +
        "use(M$L);\n" +
        "use(M$L.A);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalAliasWithProperties1
  public void testGlobalAliasWithProperties1() {
    test("var ns = {}; " +
        " ns.Foo = function() {};\n" +
        " ns.Foo.EventType = {A:1, B:2};" +
        " ns.Bar = ns.Foo;\n" +
        "var x = function() {use(ns.Bar.EventType.A)};\n" +
        "use(x);",
        "var ns$Foo = function(){};" +
        "var ns$Foo$EventType$A = 1;" +
        "var ns$Foo$EventType$B = 2;" +
        "var ns$Bar = null;" +
        "var x = function(){use(ns$Foo$EventType$A)};" +
        "use(x);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalAliasWithProperties2
  public void testGlobalAliasWithProperties2() {
    
    
    
    
    test("var ns = {}; " +
        " ns.Foo = function() {};\n" +
        " ns.Foo.EventType = {A:1, B:2};" +
        " ns.Bar = ns.Foo;\n" +
        " ns.Bar.EventType = ns.Foo.EventType;\n" +
        "var x = function() {use(ns.Bar.EventType.A)};\n" +
        "use(x)",
        "var ns$Foo = function(){};" +
        "var ns$Foo$EventType = {A:1, B:2};" +
        "var ns$Bar = null;" +
        "ns$Foo$EventType = ns$Foo$EventType;\n" +
        "var x = function(){use(ns$Foo$EventType.A)};" +
        "use(x);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalAliasWithProperties3
  public void testGlobalAliasWithProperties3() {
    test("var ns = {}; " +
        " ns.Foo = function() {};\n" +
        " ns.Foo.EventType = {A:1, B:2};" +
        " ns.Bar = ns.Foo;\n" +
        " ns.Bar.Other = {X:1, Y:2};\n" +
        "var x = function() {use(ns.Bar.Other.X)};\n" +
        "use(x)",
        "var ns$Foo=function(){};" +
        "var ns$Foo$EventType$A=1;" +
        "var ns$Foo$EventType$B=2;" +
        "var ns$Bar=null;" +
        "var ns$Foo$Other$X=1;" +
        "var ns$Foo$Other$Y=2;" +
        "var x=function(){use(ns$Foo$Other$X)};" +
        "use(x)\n");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalAliasWithProperties4
  public void testGlobalAliasWithProperties4() {
    testSame("" +
        "var nullFunction = function(){};\n" +
        "var blob = {};\n" +
        "blob.init = nullFunction;\n" +
        "use(blob)");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalAliasWithProperties5
  public void testGlobalAliasWithProperties5() {
    testSame(
        " var blob = function() {}",
        "var nullFunction = function(){};\n" +
        "blob.init = nullFunction;\n" +
        "use(blob.init)",
        null);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAliasOfEnumWithInstanceofCheck
  public void testLocalAliasOfEnumWithInstanceofCheck() {
    test(
        "\n" +
        "var Enums = function() {\n" +
        "};\n" +
        "\n" +
        "\n" +
        "Enums.Fruit = {\n" +
        " APPLE: 1,\n" +
        " BANANA: 2,\n" +
        "};\n" +
        "\n" +
        "function foo(f) {\n" +
        " if (f instanceof Enums) { alert('what?'); return; }\n" +
        "\n" +
        " var Fruit = Enums.Fruit;\n" +
        " if (f == Fruit.APPLE) alert('apple');\n" +
        " if (f == Fruit.BANANA) alert('banana');\n" +
        "}",
        "var Enums = function() {};\n" +
        "var Enums$Fruit$APPLE = 1;\n" +
        "var Enums$Fruit$BANANA = 2;\n" +
        "function foo(f) {\n" +
        " if (f instanceof Enums) { alert('what?'); return; }\n" +
        " var Fruit = null;\n" +
        " if (f == Enums$Fruit$APPLE) alert('apple');\n" +
        " if (f == Enums$Fruit$BANANA) alert('banana');\n" +
        "}",
        null);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCollapsePropertiesOfClass1
  public void testCollapsePropertiesOfClass1() {
    test(
        "\n" +
        "var namespace = function() {};\n" +
        "goog.inherits(namespace, Object);\n" +
        "\n" +
        "namespace.includeExtraParam = true;\n" +
        "\n" +
        "\n" +
        "namespace.Param = {\n" +
        "  param1: 1,\n" +
        "  param2: 2\n" +
        "};\n" +
        "\n" +
        "if (namespace.includeExtraParam) {\n" +
        "  namespace.Param.optParam = 3;\n" +
        "}\n" +
        "\n" +
        "function f() {\n" +
        "  var Param = namespace.Param;\n" +
        "  log(namespace.Param.optParam);\n" +
        "  log(Param.optParam);\n" +
        "}",
        "var namespace = function() {};\n" +
        "goog.inherits(namespace, Object);\n" +
        "var namespace$includeExtraParam = true;\n" +
        "var namespace$Param$param1 = 1;\n" +
        "var namespace$Param$param2 = 2;\n" +
        "if (namespace$includeExtraParam) {\n" +
        "  var namespace$Param$optParam = 3;\n" +
        "}\n" +
        "function f() {\n" +
        "  var Param = null;\n" +
        "  log(namespace$Param$optParam);\n" +
        "  log(namespace$Param$optParam);\n" +
        "}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCollapsePropertiesOfClass2
  public void testCollapsePropertiesOfClass2() {
    test(
        "var goog = goog || {};\n" +
        "goog.addSingletonGetter = function(cls) {};\n" +
        "\n" +
        "var a = {};\n" +
        "\n" +
        "\n" +
        "a.b = function() {};\n" +
        "goog.addSingletonGetter(a.b);\n" +
        "a.b.prototype.get = function(key) {};\n" +
        "\n" +
        "\n" +
        "a.b.c = function() {};\n" +
        "a.b.c.XXX = new a.b.c();\n" +
        "\n" +
        "function f() {\n" +
        "  var x = a.b.getInstance();\n" +
        "  var Key = a.b.c;\n" +
        "  x.get(Key.XXX);\n" +
        "}",

        "var goog = goog || {};\n" +
        "var goog$addSingletonGetter = function(cls) {};\n" +
        "var a$b = function() {};\n" +
        "goog$addSingletonGetter(a$b);\n" +
        "a$b.prototype.get = function(key) {};\n" +
        "var a$b$c = function() {};\n" +
        "var a$b$c$XXX = new a$b$c();\n" +
        "\n" +
        "function f() {\n" +
        "  var x = a$b.getInstance();\n" +
        "  var Key = null;\n" +
        "  x.get(a$b$c$XXX);\n" +
        "}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalCatch
  public void testGlobalCatch() throws Exception {
    testSame(
        "try {" +
        "  throw Error();" +
        "} catch (e) {" +
        "  console.log(e.name)" +
        "}");
  }

// com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testCollapsing
  public void testCollapsing() throws Exception {
    
    test("var a;var b;",
         "var a,b;");
    
    test("var a = 1;var b = 1;",
         "var a=1,b=1;");
    
    test("var a, b;",
         "var a,b;");
    
    test("var a = 1, b = 1;",
         "var a=1,b=1;");
    
    test("var a;var b, c;var d;",
         "var a,b,c,d;");
    
    test("var a = 1;var b = 2, c = 3;var d = 4;",
         "var a=1,b=2,c=3,d=4;");
  }

// com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testIssue820
  public void testIssue820() throws Exception {
    
    
    testSame("function f(a){ var b=1; a=2; var c; }");
  }

// com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testIfElseVarDeclarations
  public void testIfElseVarDeclarations() throws Exception {
    testSame("if (x) var a = 1; else var b = 2;");
  }

// com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testAggressiveRedeclaration
  public void testAggressiveRedeclaration() {
    test("var x = 2; foo(x);     x = 3; var y = 2;",
         "var x = 2; foo(x); var x = 3,     y = 2;");

    test("var x = 2; foo(x);     x = 3; x = 1; var y = 2;",
         "var x = 2; foo(x); var x = 3, x = 1,     y = 2;");

    test("var x = 2; foo(x);     x = 3; x = 1; var y = 2; var z = 4",
         "var x = 2; foo(x); var x = 3, x = 1,     y = 2,     z = 4");

    test("var x = 2; foo(x);     x = 3; x = 1; var y = 2; var z = 4; x = 5",
         "var x = 2; foo(x); var x = 3, x = 1,     y = 2,     z = 4, x = 5");
  }

// com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testAggressiveRedeclarationInFor
  public void testAggressiveRedeclarationInFor() {
    testSame("for(var x = 1; x = 2; x = 3) {x = 4}");
    testSame("for(var x = 1; y = 2; z = 3) {var a = 4}");
    testSame("var x; for(x = 1; x = 2; z = 3) {x = 4}");
  }

// com.google.javascript.jscomp.CollapseVariableDeclarationsTest::testIssue397
  public void testIssue397() {
    test("var x; var y = 3; x = 5;",
         "var x, y = 3; x = 5;");
    testSame("var x; x = 5; var z = 7;");
    test("var x; var y = 3; x = 5; var z = 7;",
         "var x, y = 3; x = 5; var z = 7;");
    test("var a = 1; var x; var y = 3; x = 5;",
         "var a = 1, x, y = 3; x = 5;");
  }

// com.google.javascript.jscomp.CombinedCompilerPassTest::testIndividualPasses
  public void testIndividualPasses() {
    for (TestHelper test : createStringTests()) {
      CombinedCompilerPass pass =
          new CombinedCompilerPass(compiler, test.getTraversal());
      pass.process(null, createPostOrderAlphabet());
      test.checkResults();
    }
  }

// com.google.javascript.jscomp.CombinedCompilerPassTest::testCombinedPasses
  public void testCombinedPasses() {
    List<TestHelper> tests  = createStringTests();
    Callback[] callbacks = new Callback[tests.size()];
    int i = 0;
    for (TestHelper test : tests) {
      callbacks[i++] = test.getTraversal();
    }
    CombinedCompilerPass pass =
        new CombinedCompilerPass(compiler, callbacks);
    pass.process(null, createPostOrderAlphabet());
    for (TestHelper test : tests) {
      test.checkResults();
    }
  }

// com.google.javascript.jscomp.CombinedCompilerPassTest::testScopes
  public void testScopes() {
    Node root =
        compiler.parseTestCode("var y = function() { var x = function() { };}");

    ScopeRecordingCallback c1 = new ScopeRecordingCallback();
    c1.ignore("y");
    ScopeRecordingCallback c2 = new ScopeRecordingCallback();
    c2.ignore("x");
    ScopeRecordingCallback c3 = new ScopeRecordingCallback();

    CombinedCompilerPass pass = new CombinedCompilerPass(compiler, c1, c2, c3);
    pass.process(null, root);

    assertEquals(1, c1.getVisitedScopes().size());
    assertEquals(2, c2.getVisitedScopes().size());
    assertEquals(3, c3.getVisitedScopes().size());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testUnknownAnnotation
  public void testUnknownAnnotation() {
    args.add("--warning_level=VERBOSE");
    test(" function f() {}",
         RhinoErrorReporter.BAD_JSDOC_ANNOTATION);

    args.add("--extra_annotation_name=unknownTag");
    testSame(" function f() {}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering1
  public void testWarningGuardOrdering1() {
    args.add("--jscomp_error=globalThis");
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering2
  public void testWarningGuardOrdering2() {
    args.add("--jscomp_off=globalThis");
    args.add("--jscomp_error=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering3
  public void testWarningGuardOrdering3() {
    args.add("--jscomp_warning=globalThis");
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering4
  public void testWarningGuardOrdering4() {
    args.add("--jscomp_off=globalThis");
    args.add("--jscomp_warning=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSimpleModeLeavesUnusedParams
  public void testSimpleModeLeavesUnusedParams() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    testSame("window.f = function(a) {};");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testAdvancedModeRemovesUnusedParams
  public void testAdvancedModeRemovesUnusedParams() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("window.f = function(a) {};", "window.a = function() {};");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOffByDefault
  public void testCheckGlobalThisOffByDefault() {
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOnWithAdvancedMode
  public void testCheckGlobalThisOnWithAdvancedMode() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOnWithErrorFlag
  public void testCheckGlobalThisOnWithErrorFlag() {
    args.add("--jscomp_error=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOff
  public void testCheckGlobalThisOff() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckingOffByDefault
  public void testTypeCheckingOffByDefault() {
    test("function f(x) { return x; } f();",
         "function f(a) { return a; } f();");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testReflectedMethods
  public void testReflectedMethods() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test(
        "" +
        "function Foo() {}" +
        "Foo.prototype.handle = function(x, y) { alert(y); };" +
        "var x = goog.reflect.object(Foo, {handle: 1});" +
        "for (var i in x) { x[i].call(x); }" +
        "window['Foo'] = Foo;",
        "function a() {}" +
        "a.prototype.a = function(e, d) { alert(d); };" +
        "var b = goog.c.b(a, {a: 1}),c;" +
        "for (c in b) { b[c].call(b); }" +
        "window.Foo = a;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testInlineVariables
  public void testInlineVariables() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    
    
    
    test(
        " function F() { this.a = 0; }" +
        "F.prototype.inc = function() { this.a++; return 10; };" +
        "F.prototype.bar = function() { " +
        "  var c = 3; var val = this.inc(); this.a += val + c;" +
        "};" +
        "window['f'] = new F();" +
        "window['f']['inc'] = window['f'].inc;" +
        "window['f']['bar'] = window['f'].bar;" +
        "use(window['f'].a)",
        "function a(){ this.a = 0; }" +
        "a.prototype.b = function(){ this.a++; return 10; };" +
        "a.prototype.c = function(){ var b=this.b(); this.a += b + 3; };" +
        "window.f = new a;" +
        "window.f.inc = window.f.b;" +
        "window.f.bar = window.f.c;" +
        "use(window.f.a);");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypedAdvanced
  public void testTypedAdvanced() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--use_types_for_optimization");
    test(
        "\n" +
        "function Foo() {}\n" +
        "Foo.prototype.handle1 = function(x, y) { alert(y); };\n" +
        "\n" +
        "function Bar() {}\n" +
        "Bar.prototype.handle1 = function(x, y) {};\n" +
        "new Foo().handle1(1, 2);\n" +
        "new Bar().handle1(1, 2);\n",
        "alert(2)");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckingOnWithVerbose
  public void testTypeCheckingOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test("function f(x) { return x; } f();", TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeParsingOffByDefault
  public void testTypeParsingOffByDefault() {
    testSame(" function f(a) { return a; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeParsingOnWithVerbose
  public void testTypeParsingOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f(a) { return a; }",
         RhinoErrorReporter.TYPE_PARSE_ERROR);
    test(" function f(a) { return a; }",
         RhinoErrorReporter.TYPE_PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckOverride1
  public void testTypeCheckOverride1() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=checkTypes");
    testSame("var x = x || {}; x.f = function() {}; x.f(3);");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckOverride2
  public void testTypeCheckOverride2() {
    args.add("--warning_level=DEFAULT");
    testSame("var x = x || {}; x.f = function() {}; x.f(3);");

    args.add("--jscomp_warning=checkTypes");
    test("var x = x || {}; x.f = function() {}; x.f(3);",
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOffForDefault
  public void testCheckSymbolsOffForDefault() {
    args.add("--warning_level=DEFAULT");
    test("x = 3; var y; var y;", "x=3; var y;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOnForVerbose
  public void testCheckSymbolsOnForVerbose() {
    args.add("--warning_level=VERBOSE");
    test("x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
    test("var y; var y;", VarCheck.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOverrideForVerbose
  public void testCheckSymbolsOverrideForVerbose() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=undefinedVars");
    testSame("x = 3;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOverrideForQuiet
  public void testCheckSymbolsOverrideForQuiet() {
    args.add("--warning_level=QUIET");
    args.add("--jscomp_error=undefinedVars");
    test("x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties1
  public void testCheckUndefinedProperties1() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_error=missingProperties");
    test("var x = {}; var y = x.bar;", TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties2
  public void testCheckUndefinedProperties2() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=missingProperties");
    test("var x = {}; var y = x.bar;", CheckGlobalNames.UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties3
  public void testCheckUndefinedProperties3() {
    args.add("--warning_level=VERBOSE");
    test("function f() {var x = {}; var y = x.bar;}",
        TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDuplicateParams
  public void testDuplicateParams() {
    test("function f(a, a) {}", RhinoErrorReporter.DUPLICATE_PARAM);
    assertTrue(lastCompiler.hasHaltingErrors());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag
  public void testDefineFlag() {
    args.add("--define=FOO");
    args.add("--define=\"BAR=5\"");
    args.add("--D"); args.add("CCC");
    args.add("-D"); args.add("DDD");
    test(" var FOO = false;" +
         " var BAR = 3;" +
         " var CCC = false;" +
         " var DDD = false;",
         "var FOO = !0, BAR = 5, CCC = !0, DDD = !0;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag2
  public void testDefineFlag2() {
    args.add("--define=FOO='x\"'");
    test(" var FOO = \"a\";",
         "var FOO = \"x\\\"\";");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag3
  public void testDefineFlag3() {
    args.add("--define=FOO=\"x'\"");
    test(" var FOO = \"a\";",
         "var FOO = \"x'\";");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testScriptStrictModeNoWarning
  public void testScriptStrictModeNoWarning() {
    test("'use strict';", "");
    test("'no use strict';", CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testFunctionStrictModeNoWarning
  public void testFunctionStrictModeNoWarning() {
    test("function f() {'use strict';}", "function f() {}");
    test("function f() {'no use strict';}",
         CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testQuietMode
  public void testQuietMode() {
    args.add("--warning_level=DEFAULT");
    test(" var x;",
         RhinoErrorReporter.PARSE_ERROR);
    args.add("--warning_level=QUIET");
    testSame(" var x;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testProcessClosurePrimitives
  public void testProcessClosurePrimitives() {
    test("var goog = {}; goog.provide('goog.dom');",
         "var goog = {dom:{}};");
    args.add("--process_closure_primitives=false");
    testSame("var goog = {}; goog.provide('goog.dom');");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testGetMsgWiring
  public void testGetMsgWiring() throws Exception {
    test("var goog = {}; goog.getMsg = function(x) { return x; };" +
         " var MSG_FOO = goog.getMsg('foo');",
         "var goog={getMsg:function(a){return a}}, " +
         "MSG_FOO=goog.getMsg('foo');");
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("var goog = {}; goog.getMsg = function(x) { return x; };" +
         " var MSG_FOO = goog.getMsg('foo');" +
         "window['foo'] = MSG_FOO;",
         "window.foo = 'foo';");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCssNameWiring
  public void testCssNameWiring() throws Exception {
    test("var goog = {}; goog.getCssName = function() {};" +
         "goog.setCssNameMapping = function() {};" +
         "goog.setCssNameMapping({'goog': 'a', 'button': 'b'});" +
         "var a = goog.getCssName('goog-button');" +
         "var b = goog.getCssName('css-button');" +
         "var c = goog.getCssName('goog-menu');" +
         "var d = goog.getCssName('css-menu');",
         "var goog = { getCssName: function() {}," +
         "             setCssNameMapping: function() {} }," +
         "    a = 'a-b'," +
         "    b = 'css-b'," +
         "    c = 'a-menu'," +
         "    d = 'css-menu';");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue70a
  public void testIssue70a() {
    test("function foo({}) {}", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue70b
  public void testIssue70b() {
    test("function foo([]) {}", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue81
  public void testIssue81() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    useStringComparison = true;
    test("eval('1'); var x = eval; x('2');",
         "eval(\"1\");(0,eval)(\"2\");");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue115
  public void testIssue115() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--jscomp_off=es5Strict");
    args.add("--warning_level=VERBOSE");
    test("function f() { " +
         "  var arguments = Array.prototype.slice.call(arguments, 0);" +
         "  return arguments[0]; " +
         "}",
         "function f() { " +
         "  arguments = Array.prototype.slice.call(arguments, 0);" +
         "  return arguments[0]; " +
         "}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue297
  public void testIssue297() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    test("function f(p) {" +
         " var x;" +
         " return ((x=p.id) && (x=parseInt(x.substr(1)))) && x>0;" +
         "}",
         "function f(b) {" +
         " var a;" +
         " return ((a=b.id) && (a=parseInt(a.substr(1)))) && 0<a;" +
         "}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testHiddenSideEffect
  public void testHiddenSideEffect() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("element.offsetWidth;",
         "element.offsetWidth", CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue504
  public void testIssue504() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("void function() { alert('hi'); }();",
         "alert('hi');void 0", CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue601
  public void testIssue601() {
    args.add("--compilation_level=WHITESPACE_ONLY");
    test("function f() { return '\\v' == 'v'; } window['f'] = f;",
         "function f(){return'\\v'=='v'}window['f']=f");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue601b
  public void testIssue601b() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("function f() { return '\\v' == 'v'; } window['f'] = f;",
         "window.f=function(){return'\\v'=='v'}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue601c
  public void testIssue601c() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("function f() { return '\\u000B' == 'v'; } window['f'] = f;",
         "window.f=function(){return'\\u000B'=='v'}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue846
  public void testIssue846() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    testSame(
        "try { new Function('this is an error'); } catch(a) { alert('x'); }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSideEffectIntegration
  public void testSideEffectIntegration() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("" +
         "var Foo = function() {};" +

         "Foo.prototype.blah = function() {" +
         "  Foo.bar_(this)" +
         "};" +

         "Foo.bar_ = function(f) {" +
         "  f.x = 5;" +
         "};" +

         "var y = new Foo();" +

         "Foo.bar_({});" +

         
         
         "y.blah();" +

         "alert(y);",
         "var a = new function(){}; a.a = 5; alert(a);");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag1
  public void testDebugFlag1() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug=false");
    test("function foo(a) {}",
         "function foo(a) {}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag2
  public void testDebugFlag2() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug=true");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag3
  public void testDebugFlag3() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--warning_level=QUIET");
    args.add("--debug=false");
    test("function Foo() {}" +
         "Foo.x = 1;" +
         "function f() {throw new Foo().x;} f();",
         "throw (new function() {}).a;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag4
  public void testDebugFlag4() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--warning_level=QUIET");
    args.add("--debug=true");
    test("function Foo() {}" +
        "Foo.x = 1;" +
        "function f() {throw new Foo().x;} f();",
        "throw (new function Foo() {}).$x$;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testBooleanFlag1
  public void testBooleanFlag1() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testBooleanFlag2
  public void testBooleanFlag2() {
    args.add("--debug");
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testHelpFlag
  public void testHelpFlag() {
    args.add("--help");
    assertFalse(
        createCommandLineRunner(
            new String[] {"function f() {}"}).shouldRunCompiler());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testExternsLifting1
  public void testExternsLifting1() throws Exception{
    String code = " function f() {}";
    test(new String[] {code},
         new String[] {});

    assertEquals(2, lastCompiler.getExternsForTesting().size());

    CompilerInput extern = lastCompiler.getExternsForTesting().get(1);
    assertNull(extern.getModule());
    assertTrue(extern.isExtern());
    assertEquals(code, extern.getCode());

    assertEquals(1, lastCompiler.getInputsForTesting().size());

    CompilerInput input = lastCompiler.getInputsForTesting().get(0);
    assertNotNull(input.getModule());
    assertFalse(input.isExtern());
    assertEquals("", input.getCode());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testExternsLifting2
  public void testExternsLifting2() {
    args.add("--warning_level=VERBOSE");
    test(new String[] {" function f() {}", "f(3);"},
         new String[] {"f(3);"},
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOff
  public void testSourceSortingOff() {
    args.add("--compilation_level=WHITESPACE_ONLY");
    testSame(
        new String[] {
          "goog.require('beer');",
          "goog.provide('beer');"
        });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOn
  public void testSourceSortingOn() {
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');"
         },
         new String[] {
           "var beer = {};",
           ""
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOn2
  public void testSourceSortingOn2() {
    test(new String[] {
          "goog.provide('a');",
          "goog.require('a');\n" +
          "var COMPILED = false;",
         },
         new String[] {
           "var a={};",
           "var COMPILED=!1"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOn3
  public void testSourceSortingOn3() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.addDependency('sym', [], []);\nvar x = 3;",
          "var COMPILED = false;",
         },
         new String[] {
          "var COMPILED = !1;",
          "var x = 3;"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingCircularDeps1
  public void testSourceSortingCircularDeps1() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.provide('gin'); goog.require('tonic'); var gin = {};",
          "goog.provide('tonic'); goog.require('gin'); var tonic = {};",
          "goog.require('gin'); goog.require('tonic');"
         },
         JSModule.CIRCULAR_DEPENDENCY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingCircularDeps2
  public void testSourceSortingCircularDeps2() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.provide('roses.lime.juice');",
          "goog.provide('gin'); goog.require('tonic'); var gin = {};",
          "goog.provide('tonic'); goog.require('gin'); var tonic = {};",
          "goog.require('gin'); goog.require('tonic');",
          "goog.provide('gimlet');" +
          "     goog.require('gin'); goog.require('roses.lime.juice');"
         },
         JSModule.CIRCULAR_DEPENDENCY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn1
  public void testSourcePruningOn1() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           ""
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn2
  public void testSourcePruningOn2() {
    args.add("--closure_entry_point=guinness");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "var guinness = {};"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn3
  public void testSourcePruningOn3() {
    args.add("--closure_entry_point=scotch");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn4
  public void testSourcePruningOn4() {
    args.add("--closure_entry_point=scotch");
    args.add("--closure_entry_point=beer");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn5
  public void testSourcePruningOn5() {
    args.add("--closure_entry_point=shiraz");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         Compiler.MISSING_ENTRY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn6
  public void testSourcePruningOn6() {
    args.add("--closure_entry_point=scotch");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "",
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn7
  public void testSourcePruningOn7() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "var COMPILED = false;",
         },
         new String[] {
          "var COMPILED = !1;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn8
  public void testSourcePruningOn8() {
    args.add("--only_closure_dependencies");
    args.add("--closure_entry_point=scotch");
    args.add("--warning_level=VERBOSE");
    test(new String[] {
          "\n" +
          "var externVar;",
          "goog.provide('scotch'); var x = externVar;"
         },
         new String[] {
           "var scotch = {}, x = externVar;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testModuleEntryPoint
  public void testModuleEntryPoint() throws Exception {
    useModules = ModulePattern.STAR;
    args.add("--only_closure_dependencies");
    args.add("--closure_entry_point=m1:a");
    test(
        new String[] {
          "goog.provide('a');",
          "goog.provide('b');"
        },
        
        
        new String[] {
          "",
          "var a = {};"
        });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testNoCompile
  public void testNoCompile() {
    args.add("--warning_level=VERBOSE");
    test(new String[] {
          "\n" +
          "goog.provide('x');\n" +
          "var dupeVar;",
          "var dupeVar;"
         },
         new String[] {
           "var dupeVar;"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDependencySortingWhitespaceMode
  public void testDependencySortingWhitespaceMode() {
    args.add("--manage_closure_dependencies");
    args.add("--compilation_level=WHITESPACE_ONLY");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');\ngoog.require('hops');",
          "goog.provide('hops');",
         },
         new String[] {
          "goog.provide('hops');",
          "goog.provide('beer');\ngoog.require('hops');",
          "goog.require('beer');"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testForwardDeclareDroppedTypes
  public void testForwardDeclareDroppedTypes() {
    args.add("--manage_closure_dependencies=true");

    args.add("--warning_level=VERBOSE");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');  function f(x) {}",
          "goog.provide('Scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {}; function f(a) {}",
           ""
         });

    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');  function f(x) {}"
         },
         new String[] {
           "var beer = {}; function f(a) {}",
           ""
         },
         RhinoErrorReporter.TYPE_PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testOnlyClosureDependenciesEmptyEntryPoints
  public void testOnlyClosureDependenciesEmptyEntryPoints() throws Exception {
    
    args.add("--use_only_custom_externs=true");

    args.add("--only_closure_dependencies=true");
    try {
      CommandLineRunner runner = createCommandLineRunner(new String[0]);
      runner.doRun();
      fail("Expected FlagUsageException");
    } catch (FlagUsageException e) {
      assertTrue(e.getMessage(),
          e.getMessage().contains("only_closure_dependencies"));
    }
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testOnlyClosureDependenciesOneEntryPoint
  public void testOnlyClosureDependenciesOneEntryPoint() throws Exception {
    args.add("--only_closure_dependencies=true");
    args.add("--closure_entry_point=beer");
    test(new String[] {
          "goog.require('beer'); var beerRequired = 1;",
          "goog.provide('beer');\ngoog.require('hops');\nvar beerProvided = 1;",
          "goog.provide('hops'); var hopsProvided = 1;",
          "goog.provide('scotch'); var scotchProvided = 1;",
          "goog.require('scotch');\nvar includeFileWithoutProvides = 1;",
          "\nvar COMPILED = false;",
         },
         new String[] {
           "var COMPILED = !1;",
           "var hops = {}, hopsProvided = 1;",
           "var beer = {}, beerProvided = 1;"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion1
  public void testSourceMapExpansion1() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    args.add("--create_source_map=%outname%.map");
    testSame("var x = 3;");
    assertEquals("/path/to/out.js.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(), null));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion2
  public void testSourceMapExpansion2() {
    useModules = ModulePattern.CHAIN;
    args.add("--create_source_map=%outname%.map");
    args.add("--module_output_path_prefix=foo");
    testSame(new String[] {"var x = 3;", "var y = 5;"});
    assertEquals("foo.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(), null));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion3
  public void testSourceMapExpansion3() {
    useModules = ModulePattern.CHAIN;
    args.add("--create_source_map=%outname%.map");
    args.add("--module_output_path_prefix=foo_");
    testSame(new String[] {"var x = 3;", "var y = 5;"});
    assertEquals("foo_m0.js.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(),
            lastCompiler.getModuleGraph().getRootModule()));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapFormat1
  public void testSourceMapFormat1() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    testSame("var x = 3;");
    assertEquals(SourceMap.Format.DEFAULT,
        lastCompiler.getOptions().sourceMapFormat);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapFormat2
  public void testSourceMapFormat2() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    args.add("--source_map_format=V3");
    testSame("var x = 3;");
    assertEquals(SourceMap.Format.V3,
        lastCompiler.getOptions().sourceMapFormat);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testModuleWrapperBaseNameExpansion
  public void testModuleWrapperBaseNameExpansion() throws Exception {
    useModules = ModulePattern.CHAIN;
    args.add("--module_wrapper=m0:%s 
    testSame(new String[] {
      "var x = 3;",
      "var y = 4;"
    });

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.writeModuleOutput(
        builder,
        lastCompiler.getModuleGraph().getRootModule());
    assertEquals("var x=3; 
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCharSetExpansion
  public void testCharSetExpansion() {
    testSame("");
    assertEquals("US-ASCII", lastCompiler.getOptions().outputCharset);
    args.add("--charset=UTF-8");
    testSame("");
    assertEquals("UTF-8", lastCompiler.getOptions().outputCharset);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testChainModuleManifest
  public void testChainModuleManifest() throws Exception {
    useModules = ModulePattern.CHAIN;
    testSame(new String[] {
          "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphManifestOrBundleTo(
        lastCompiler.getModuleGraph(), builder, true);
    assertEquals(
        "{m0}\n" +
        "i0\n" +
        "\n" +
        "{m1:m0}\n" +
        "i1\n" +
        "\n" +
        "{m2:m1}\n" +
        "i2\n" +
        "\n" +
        "{m3:m2}\n" +
        "i3\n",
        builder.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testStarModuleManifest
  public void testStarModuleManifest() throws Exception {
    useModules = ModulePattern.STAR;
    testSame(new String[] {
          "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphManifestOrBundleTo(
        lastCompiler.getModuleGraph(), builder, true);
    assertEquals(
        "{m0}\n" +
        "i0\n" +
        "\n" +
        "{m1:m0}\n" +
        "i1\n" +
        "\n" +
        "{m2:m0}\n" +
        "i2\n" +
        "\n" +
        "{m3:m0}\n" +
        "i3\n",
        builder.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testOutputModuleGraphJson
  public void testOutputModuleGraphJson() throws Exception {
    useModules = ModulePattern.STAR;
    testSame(new String[] {
        "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphJsonTo(builder);
    assertTrue(builder.toString().indexOf("transitive-dependencies") != -1);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testVersionFlag
  public void testVersionFlag() {
    args.add("--version");
    testSame("");
    assertEquals(
        0,
        new String(errReader.toByteArray()).indexOf(
            "Closure Compiler (http://code.google.com/closure/compiler)\n" +
            "Version: "));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testVersionFlag2
  public void testVersionFlag2() {
    lastArg = "--version";
    testSame("");
    assertEquals(
        0,
        new String(errReader.toByteArray()).indexOf(
            "Closure Compiler (http://code.google.com/closure/compiler)\n" +
            "Version: "));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testPrintAstFlag
  public void testPrintAstFlag() {
    args.add("--print_ast=true");
    testSame("");
    assertEquals(
        "digraph AST {\n" +
        "  node [color=lightblue2, style=filled];\n" +
        "  node0 [label=\"BLOCK\"];\n" +
        "  node1 [label=\"SCRIPT\"];\n" +
        "  node0 -> node1 [weight=1];\n" +
        "  node1 -> RETURN [label=\"UNCOND\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "  node0 -> RETURN [label=\"SYN_BLOCK\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "  node0 -> node1 [label=\"UNCOND\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "}\n\n",
        new String(outReader.toByteArray()));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSyntheticExterns
  public void testSyntheticExterns() {
    externs = ImmutableList.of(
        SourceFile.fromCode("externs", "myVar.property;"));
    test("var theirVar = {}; var myVar = {}; var yourVar = {};",
         VarCheck.UNDEFINED_EXTERN_VAR_ERROR);

    args.add("--jscomp_off=externsValidation");
    args.add("--warning_level=VERBOSE");
    test("var theirVar = {}; var myVar = {}; var yourVar = {};",
         "var theirVar={},myVar={},yourVar={};");

    args.add("--jscomp_off=externsValidation");
    args.add("--warning_level=VERBOSE");
    test("var theirVar = {}; var myVar = {}; var myVar = {};",
         VarCheck.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testGoogAssertStripping
  public void testGoogAssertStripping() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("goog.asserts.assert(false)",
         "");
    args.add("--debug");
    test("goog.asserts.assert(false)", "goog.$asserts$.$assert$(!1)");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testMissingReturnCheckOnWithVerbose
  public void testMissingReturnCheckOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f() {f()} f();",
        CheckMissingReturn.MISSING_RETURN_STATEMENT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testGenerateExports
  public void testGenerateExports() {
    args.add("--generate_exports=true");
    test(" foo.prototype.x = function() {};",
        "foo.prototype.x=function(){};"+
        "goog.exportSymbol(\"foo.prototype.x\",foo.prototype.x);");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDepreciationWithVerbose
  public void testDepreciationWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f() {}; f()",
       CheckAccessControls.DEPRECATED_NAME);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTwoParseErrors
  public void testTwoParseErrors() {
    
    
    Compiler compiler = compile(new String[] {
      "var a b;",
      "var b c;"
    });
    assertEquals(2, compiler.getErrors().length);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES3ByDefault
  public void testES3ByDefault() {
    useStringComparison = true;
    test(
        "var x = f.function",
        "var x=f[\"function\"];",
        RhinoErrorReporter.INVALID_ES3_PROP_NAME);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5ChecksByDefault
  public void testES5ChecksByDefault() {
    testSame("var x = 3; delete x;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5ChecksInVerbose
  public void testES5ChecksInVerbose() {
    args.add("--warning_level=VERBOSE");
    test("function f(x) { delete x; }", StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5
  public void testES5() {
    args.add("--language_in=ECMASCRIPT5");
    test("var x = f.function", "var x = f.function");
    test("var let", "var let");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5Strict
  public void testES5Strict() {
    args.add("--language_in=ECMASCRIPT5_STRICT");
    test("var x = f.function", "'use strict';var x = f.function");
    test("var let", RhinoErrorReporter.PARSE_ERROR);
    test("function f(x) { delete x; }", StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5StrictUseStrict
  public void testES5StrictUseStrict() {
    args.add("--language_in=ECMASCRIPT5_STRICT");
    Compiler compiler = compile(new String[] {"var x = f.function"});
    String outputSource = compiler.toSource();
    assertEquals("'use strict'", outputSource.substring(0, 12));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5StrictUseStrictMultipleInputs
  public void testES5StrictUseStrictMultipleInputs() {
    args.add("--language_in=ECMASCRIPT5_STRICT");
    Compiler compiler = compile(new String[] {"var x = f.function",
        "var y = f.function", "var z = f.function"});
    String outputSource = compiler.toSource();
    assertEquals("'use strict'", outputSource.substring(0, 12));
    assertEquals(outputSource.substring(13).indexOf("'use strict'"), -1);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWithKeywordDefault
  public void testWithKeywordDefault() {
    test("var x = {}; with (x) {}", ControlStructureCheck.USE_OF_WITH);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWithKeywordWithEs5ChecksOff
  public void testWithKeywordWithEs5ChecksOff() {
    args.add("--jscomp_off=es5Strict");
    testSame("var x = {}; with (x) {}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testNoSrCFilesWithManifest
  public void testNoSrCFilesWithManifest() throws IOException {
    args.add("--use_only_custom_externs=true");
    args.add("--output_manifest=test.MF");
    CommandLineRunner runner = createCommandLineRunner(new String[0]);
    String expectedMessage = "";
    try {
      runner.doRun();
    } catch (FlagUsageException e) {
      expectedMessage = e.getMessage();
    }
    assertEquals(expectedMessage, "Bad --js flag. " +
      "Manifest files cannot be generated when the input is from stdin.");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTransformAMD
  public void testTransformAMD() {
    args.add("--transform_amd_modules");
    test("define({test: 1})", "exports = {test: 1}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testProcessCJS
  public void testProcessCJS() {
    useStringComparison = true;
    args.add("--process_common_js_modules");
    args.add("--common_js_entry_module=foo/bar");
    setFilename(0, "foo/bar.js");
    String expected = "var module$foo$bar={test:1};";
    test("exports.test = 1", expected);
    assertEquals(expected + "\n", outReader.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testProcessCJSWithModuleOutput
  public void testProcessCJSWithModuleOutput() {
    useStringComparison = true;
    args.add("--process_common_js_modules");
    args.add("--common_js_entry_module=foo/bar");
    args.add("--module=auto");
    setFilename(0, "foo/bar.js");
    test("exports.test = 1",
        "var module$foo$bar={test:1};");
    
    assertEquals("", outReader.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testFormattingSingleQuote
  public void testFormattingSingleQuote() {
    testSame("var x = '';");
    assertEquals("var x=\"\";", lastCompiler.toSource());

    args.add("--formatting=SINGLE_QUOTES");
    testSame("var x = '';");
    assertEquals("var x='';", lastCompiler.toSource());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTransformAMDAndProcessCJS
  public void testTransformAMDAndProcessCJS() {
    useStringComparison = true;
    args.add("--transform_amd_modules");
    args.add("--process_common_js_modules");
    args.add("--common_js_entry_module=foo/bar");
    setFilename(0, "foo/bar.js");
    test("define({foo: 1})",
        "var module$foo$bar={},module$foo$bar={foo:1};");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testModuleJSON
  public void testModuleJSON() {
    useStringComparison = true;
    args.add("--transform_amd_modules");
    args.add("--process_common_js_modules");
    args.add("--common_js_entry_module=foo/bar");
    args.add("--output_module_dependencies=test.json");
    setFilename(0, "foo/bar.js");
    test("define({foo: 1})",
        "var module$foo$bar={},module$foo$bar={foo:1};");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testOutputSameAsInput
  public void testOutputSameAsInput() {
    args.add("--js_output_file=" + getFilename(0));
    test("", AbstractCommandLineRunner.OUTPUT_SAME_AS_INPUT_ERROR);
  }

// com.google.javascript.jscomp.CompilerTest::testCodeBuilderColumnAfterReset
  public void testCodeBuilderColumnAfterReset() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    String js = "foo();\ngoo();";
    cb.append(js);
    assertEquals(js, cb.toString());
    assertEquals(1, cb.getLineIndex());
    assertEquals(6, cb.getColumnIndex());

    cb.reset();

    assertTrue(cb.toString().isEmpty());
    assertEquals(1, cb.getLineIndex());
    assertEquals(6, cb.getColumnIndex());
  }

// com.google.javascript.jscomp.CompilerTest::testCodeBuilderAppend
  public void testCodeBuilderAppend() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("foo();");
    assertEquals(0, cb.getLineIndex());
    assertEquals(6, cb.getColumnIndex());

    cb.append("goo();");

    assertEquals(0, cb.getLineIndex());
    assertEquals(12, cb.getColumnIndex());

    
    cb.append("blah();\ngoo();");

    assertEquals(1, cb.getLineIndex());
    assertEquals(6, cb.getColumnIndex());
  }

// com.google.javascript.jscomp.CompilerTest::testCyclicalDependencyInInputs
  public void testCyclicalDependencyInInputs() {
    List<SourceFile> inputs = Lists.newArrayList(
        SourceFile.fromCode(
            "gin", "goog.provide('gin'); goog.require('tonic'); var gin = {};"),
        SourceFile.fromCode("tonic",
            "goog.provide('tonic'); goog.require('gin'); var tonic = {};"),
        SourceFile.fromCode(
            "mix", "goog.require('gin'); goog.require('tonic');"));
    CompilerOptions options = new CompilerOptions();
    options.ideMode = true;
    options.setManageClosureDependencies(true);
    Compiler compiler = new Compiler();
    compiler.init(ImmutableList.<SourceFile>of(), inputs, options);
    compiler.parseInputs();
    assertEquals(compiler.externAndJsRoot, compiler.jsRoot.getParent());
    assertEquals(compiler.externAndJsRoot, compiler.externsRoot.getParent());
    assertNotNull(compiler.externAndJsRoot);

    Node jsRoot = compiler.jsRoot;
    assertEquals(3, jsRoot.getChildCount());
  }

// com.google.javascript.jscomp.CompilerTest::testLocalUndefined
  public void testLocalUndefined() throws Exception {
    
    
    
    
    
    
    
    
    CompilerOptions options = new CompilerOptions();
    CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(
        options);
    Compiler compiler = new Compiler();
    SourceFile externs = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js",
        "(function (undefined) { alert(undefined); })();");
    compiler.compile(externs, input, options);
  }

// com.google.javascript.jscomp.CompilerTest::testCommonJSProvidesAndRequire
  public void testCommonJSProvidesAndRequire() throws Exception {
    List<SourceFile> inputs = Lists.newArrayList(
        SourceFile.fromCode("gin.js", "require('tonic')"),
        SourceFile.fromCode("tonic.js", ""),
        SourceFile.fromCode("mix.js", "require('gin'); require('tonic');"));
    List<String> entryPoints = Lists.newArrayList("module$mix");

    Compiler compiler = initCompilerForCommonJS(inputs, entryPoints);
    JSModuleGraph graph = compiler.getModuleGraph();
    assertEquals(4, graph.getModuleCount());
    List<CompilerInput> result = graph.manageDependencies(entryPoints,
        compiler.getInputsForTesting());
    assertEquals("[root]", result.get(0).getName());
    assertEquals("[module$tonic]", result.get(1).getName());
    assertEquals("[module$gin]", result.get(2).getName());
    assertEquals("tonic.js", result.get(3).getName());
    assertEquals("gin.js", result.get(4).getName());
    assertEquals("mix.js", result.get(5).getName());
  }

// com.google.javascript.jscomp.CompilerTest::testCommonJSMissingRequire
  public void testCommonJSMissingRequire() throws Exception {
    List<SourceFile> inputs = Lists.newArrayList(
        SourceFile.fromCode("gin.js", "require('missing')"));
    Compiler compiler = initCompilerForCommonJS(
        inputs, ImmutableList.of("module$gin"));
    compiler.processAMDAndCommonJSModules();

    assertEquals(1, compiler.getErrorManager().getErrorCount());
    String error = compiler.getErrorManager().getErrors()[0].toString();
    assertTrue(
        "Unexpected error: " + error,
        error.contains(
            "required entry point \"module$missing\" never provided"));
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantDefinition1
  public void testConstantDefinition1() {
    testSame("var XYZ = 1;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantDefinition2
  public void testConstantDefinition2() {
    testSame("var a$b$XYZ = 1;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantInitializedInAnonymousNamespace1
  public void testConstantInitializedInAnonymousNamespace1() {
    testSame("var XYZ; (function(){ XYZ = 1; })();");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantInitializedInAnonymousNamespace2
  public void testConstantInitializedInAnonymousNamespace2() {
    testSame("var a$b$XYZ; (function(){ a$b$XYZ = 1; })();");
  }

// com.google.javascript.jscomp.ConstCheckTest::testObjectModified
  public void testObjectModified() {
    testSame("var IE = true, XYZ = {a:1,b:1}; if (IE) XYZ['c'] = 1;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testObjectPropertyInitializedLate
  public void testObjectPropertyInitializedLate() {
    testSame("var XYZ = {}; for (var i = 0; i < 10; i++) { XYZ[i] = i; }");
  }

// com.google.javascript.jscomp.ConstCheckTest::testObjectRedefined1
  public void testObjectRedefined1() {
    testError("var XYZ = {}; XYZ = 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefined1
  public void testConstantRedefined1() {
    testError("var XYZ = 1; XYZ = 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefined2
  public void testConstantRedefined2() {
    testError("var a$b$XYZ = 1; a$b$XYZ = 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefinedInLocalScope1
  public void testConstantRedefinedInLocalScope1() {
    testError("var XYZ = 1; (function(){ XYZ = 2; })();");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefinedInLocalScope2
  public void testConstantRedefinedInLocalScope2() {
    testError("var a$b$XYZ = 1; (function(){ a$b$XYZ = 2; })();");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefinedInLocalScopeOutOfOrder
  public void testConstantRedefinedInLocalScopeOutOfOrder() {
    testError("function f() { XYZ = 2; } var XYZ = 1;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPostIncremented1
  public void testConstantPostIncremented1() {
    testError("var XYZ = 1; XYZ++;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPostIncremented2
  public void testConstantPostIncremented2() {
    testError("var a$b$XYZ = 1; a$b$XYZ++;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPreIncremented1
  public void testConstantPreIncremented1() {
    testError("var XYZ = 1; XYZ++;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPreIncremented2
  public void testConstantPreIncremented2() {
    testError("var a$b$XYZ = 1; a$b$XYZ++;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPostDecremented1
  public void testConstantPostDecremented1() {
    testError("var XYZ = 1; XYZ--;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPostDecremented2
  public void testConstantPostDecremented2() {
    testError("var a$b$XYZ = 1; a$b$XYZ--;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPreDecremented1
  public void testConstantPreDecremented1() {
    testError("var XYZ = 1; XYZ--;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPreDecremented2
  public void testConstantPreDecremented2() {
    testError("var a$b$XYZ = 1; a$b$XYZ--;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedArithmeticAssignment1
  public void testAbbreviatedArithmeticAssignment1() {
    testError("var XYZ = 1; XYZ += 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedArithmeticAssignment2
  public void testAbbreviatedArithmeticAssignment2() {
    testError("var a$b$XYZ = 1; a$b$XYZ %= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedBitAssignment1
  public void testAbbreviatedBitAssignment1() {
    testError("var XYZ = 1; XYZ |= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedBitAssignment2
  public void testAbbreviatedBitAssignment2() {
    testError("var a$b$XYZ = 1; a$b$XYZ &= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedShiftAssignment1
  public void testAbbreviatedShiftAssignment1() {
    testError("var XYZ = 1; XYZ >>= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedShiftAssignment2
  public void testAbbreviatedShiftAssignment2() {
    testError("var a$b$XYZ = 1; a$b$XYZ <<= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstAnnotation
  public void testConstAnnotation() {
    testError(" var xyz = 1; xyz = 3;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstSuppression
  public void testConstSuppression() {
    testSame("\n" +
             " var xyz = 1; xyz = 3;");
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleStatements
  public void testSimpleStatements() {
    String src = "var a; a = a; a = a";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.SCRIPT, Token.VAR, Branch.UNCOND);
    assertCrossEdge(cfg, Token.VAR, Token.EXPR_RESULT, Branch.UNCOND);
    assertCrossEdge(cfg, Token.EXPR_RESULT, Token.EXPR_RESULT, Branch.UNCOND);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleIf
  public void testSimpleIf() {
    String src = "var x; if (x) { x() } else { x() };";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.SCRIPT, Token.VAR, Branch.UNCOND);
    assertCrossEdge(cfg, Token.VAR, Token.IF, Branch.UNCOND);
    assertDownEdge(cfg, Token.IF, Token.BLOCK, Branch.ON_TRUE);
    assertDownEdge(cfg, Token.BLOCK, Token.EXPR_RESULT, Branch.UNCOND);
    assertNoEdge(cfg, Token.EXPR_RESULT, Token.CALL);
    assertDownEdge(cfg, Token.IF, Token.BLOCK, Branch.ON_FALSE);
    assertReturnEdge(cfg, Token.EMPTY);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testBreakingBlock
  public void testBreakingBlock() {
    
    String src = "X: { while(1) { break } }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertUpEdge(cfg, Token.BREAK, Token.BLOCK, Branch.UNCOND);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testBreakingTryBlock
  public void testBreakingTryBlock() {
    String src = "a: try { break a; } finally {} if(x) {}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.BREAK, Token.IF, Branch.UNCOND);

    src = "a: try {} finally {break a;} if(x) {}";
    cfg = createCfg(src);
    assertCrossEdge(cfg, Token.BREAK, Token.IF, Branch.UNCOND);

    src = "a: try {} catch(e) {break a;} if(x) {}";
    cfg = createCfg(src);
    assertCrossEdge(cfg, Token.BREAK, Token.IF, Branch.UNCOND);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testWithStatement
  public void testWithStatement() {
    String src = "var x, y; with(x) { y() }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.WITH, Token.BLOCK, Branch.UNCOND);
    assertNoEdge(cfg, Token.WITH, Token.NAME);
    assertNoEdge(cfg, Token.NAME, Token.BLOCK);
    assertDownEdge(cfg, Token.BLOCK, Token.EXPR_RESULT, Branch.UNCOND);
    assertReturnEdge(cfg, Token.EXPR_RESULT);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleWhile
  public void testSimpleWhile() {
    String src = "var x; while (x) { x(); if (x) { break; } x() }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.WHILE, Token.BLOCK, Branch.ON_TRUE);
    assertDownEdge(cfg, Token.BLOCK, Token.EXPR_RESULT, Branch.UNCOND);
    assertDownEdge(cfg, Token.IF, Token.BLOCK, Branch.ON_TRUE);
    assertReturnEdge(cfg, Token.BREAK);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleSwitch
  public void testSimpleSwitch() {
    String src = "var x; switch(x){ case(1): x(); case('x'): x(); break" +
        "; default: x();}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.VAR, Token.SWITCH, Branch.UNCOND);
    assertNoEdge(cfg, Token.SWITCH, Token.NAME);
    
    assertDownEdge(cfg, Token.SWITCH, Token.CASE, Branch.UNCOND);
    assertCrossEdge(cfg, Token.CASE, Token.CASE, Branch.ON_FALSE);
    assertCrossEdge(cfg, Token.CASE, Token.DEFAULT_CASE, Branch.ON_FALSE);
    
    assertDownEdge(cfg, Token.CASE, Token.BLOCK, Branch.ON_TRUE);
    assertDownEdge(cfg, Token.BLOCK, Token.EXPR_RESULT, Branch.UNCOND);
    assertNoEdge(cfg, Token.EXPR_RESULT, Token.CALL);
    assertNoEdge(cfg, Token.CALL, Token.NAME);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleNoDefault
  public void testSimpleNoDefault() {
    String src = "var x; switch(x){ case(1): break; } x();";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.CASE, Token.EXPR_RESULT, Branch.ON_FALSE);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSwitchDefaultFirst
  public void testSwitchDefaultFirst() {
    
    String src = "var x; switch(x){ default: break; case 1: break; }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.SWITCH, Token.CASE, Branch.UNCOND);
    assertCrossEdge(cfg, Token.CASE, Token.DEFAULT_CASE, Branch.ON_FALSE);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSwitchDefaultInMiddle
  public void testSwitchDefaultInMiddle() {
    
    String src = "var x; switch(x){ case 1: break; default: break; " +
        "case 2: break; }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.SWITCH, Token.CASE, Branch.UNCOND);
    assertCrossEdge(cfg, Token.CASE, Token.CASE, Branch.ON_FALSE);
    assertCrossEdge(cfg, Token.CASE, Token.DEFAULT_CASE, Branch.ON_FALSE);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSwitchEmpty
  public void testSwitchEmpty() {
    
    String src = "var x; switch(x){}; x()";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.SWITCH, Token.EMPTY, Branch.UNCOND);
    assertCrossEdge(cfg, Token.EMPTY, Token.EXPR_RESULT, Branch.UNCOND);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testReturnThrowingException
  public void testReturnThrowingException() {
    String src = "function f() {try { return a(); } catch (e) {e()}}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.RETURN, Token.BLOCK, Branch.ON_EX);
    assertDownEdge(cfg, Token.BLOCK, Token.CATCH, Branch.UNCOND);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleFor
  public void testSimpleFor() {
    String src = "var a; for (var x = 0; x < 100; x++) { a(); }";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"VAR\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"VAR\"];\n" +
      "  node1 -> node3 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 [label=\"FOR\"];\n" +
      "  node0 -> node4 [weight=1];\n" +
      "  node4 -> node3 [weight=1];\n" +
      "  node5 [label=\"NAME\"];\n" +
      "  node3 -> node5 [weight=1];\n" +
      "  node6 [label=\"NUMBER\"];\n" +
      "  node5 -> node6 [weight=1];\n" +
      "  node3 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node7 [label=\"LT\"];\n" +
      "  node4 -> node7 [weight=1];\n" +
      "  node8 [label=\"NAME\"];\n" +
      "  node7 -> node8 [weight=1];\n" +
      "  node9 [label=\"NUMBER\"];\n" +
      "  node7 -> node9 [weight=1];\n" +
      "  node10 [label=\"INC\"];\n" +
      "  node4 -> node10 [weight=1];\n" +
      "  node11 [label=\"NAME\"];\n" +
      "  node10 -> node11 [weight=1];\n" +
      "  node10 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node12 [label=\"BLOCK\"];\n" +
      "  node4 -> node12 [weight=1];\n" +
      "  node13 [label=\"EXPR_RESULT\"];\n" +
      "  node12 -> node13 [weight=1];\n" +
      "  node14 [label=\"CALL\"];\n" +
      "  node13 -> node14 [weight=1];\n" +
      "  node15 [label=\"NAME\"];\n" +
      "  node14 -> node15 [weight=1];\n" +
      "  node13 -> node10 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node12 -> node13 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> RETURN " +
      "[label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> node12 " +
      "[label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleForWithContinue
  public void testSimpleForWithContinue() {
    String src = "var a; for (var x = 0; x < 100; x++) {a();continue;a()}";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"VAR\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"VAR\"];\n" +
      "  node1 -> node3 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 [label=\"FOR\"];\n" +
      "  node0 -> node4 [weight=1];\n" +
      "  node4 -> node3 [weight=1];\n" +
      "  node5 [label=\"NAME\"];\n" +
      "  node3 -> node5 [weight=1];\n" +
      "  node6 [label=\"NUMBER\"];\n" +
      "  node5 -> node6 [weight=1];\n" +
      "  node3 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node7 [label=\"LT\"];\n" +
      "  node4 -> node7 [weight=1];\n" +
      "  node8 [label=\"NAME\"];\n" +
      "  node7 -> node8 [weight=1];\n" +
      "  node9 [label=\"NUMBER\"];\n" +
      "  node7 -> node9 [weight=1];\n" +
      "  node10 [label=\"INC\"];\n" +
      "  node4 -> node10 [weight=1];\n" +
      "  node11 [label=\"NAME\"];\n" +
      "  node10 -> node11 [weight=1];\n" +
      "  node10 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node12 [label=\"BLOCK\"];\n" +
      "  node4 -> node12 [weight=1];\n" +
      "  node13 [label=\"EXPR_RESULT\"];\n" +
      "  node12 -> node13 [weight=1];\n" +
      "  node14 [label=\"CALL\"];\n" +
      "  node13 -> node14 [weight=1];\n" +
      "  node15 [label=\"NAME\"];\n" +
      "  node14 -> node15 [weight=1];\n" +
      "  node16 [label=\"CONTINUE\"];\n" +
      "  node13 -> node16 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node12 -> node16 [weight=1];\n" +
      "  node16 -> node10 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node17 [label=\"EXPR_RESULT\"];\n" +
      "  node12 -> node17 [weight=1];\n" +
      "  node18 [label=\"CALL\"];\n" +
      "  node17 -> node18 [weight=1];\n" +
      "  node19 [label=\"NAME\"];\n" +
      "  node18 -> node19 [weight=1];\n" +
      "  node17 -> node10 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node12 -> node13 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> RETURN " +
      "[label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> node12 " +
      "[label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testNestedFor
  public void testNestedFor() {
    
    String src = "var a,b;a();for(var x=0;x<100;x++){for(var y=0;y<100;y++){" +
      "continue;b();}}";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"VAR\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"NAME\"];\n" +
      "  node1 -> node3 [weight=1];\n" +
      "  node4 [label=\"EXPR_RESULT\"];\n" +
      "  node1 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node4 [weight=1];\n" +
      "  node5 [label=\"CALL\"];\n" +
      "  node4 -> node5 [weight=1];\n" +
      "  node6 [label=\"NAME\"];\n" +
      "  node5 -> node6 [weight=1];\n" +
      "  node7 [label=\"VAR\"];\n" +
      "  node4 -> node7 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node8 [label=\"FOR\"];\n" +
      "  node0 -> node8 [weight=1];\n" +
      "  node8 -> node7 [weight=1];\n" +
      "  node9 [label=\"NAME\"];\n" +
      "  node7 -> node9 [weight=1];\n" +
      "  node10 [label=\"NUMBER\"];\n" +
      "  node9 -> node10 [weight=1];\n" +
      "  node7 -> node8 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node11 [label=\"LT\"];\n" +
      "  node8 -> node11 [weight=1];\n" +
      "  node12 [label=\"NAME\"];\n" +
      "  node11 -> node12 [weight=1];\n" +
      "  node13 [label=\"NUMBER\"];\n" +
      "  node11 -> node13 [weight=1];\n" +
      "  node14 [label=\"INC\"];\n" +
      "  node8 -> node14 [weight=1];\n" +
      "  node15 [label=\"NAME\"];\n" +
      "  node14 -> node15 [weight=1];\n" +
      "  node14 -> node8 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node16 [label=\"BLOCK\"];\n" +
      "  node8 -> node16 [weight=1];\n" +
      "  node17 [label=\"FOR\"];\n" +
      "  node16 -> node17 [weight=1];\n" +
      "  node18 [label=\"VAR\"];\n" +
      "  node17 -> node18 [weight=1];\n" +
      "  node19 [label=\"NAME\"];\n" +
      "  node18 -> node19 [weight=1];\n" +
      "  node20 [label=\"NUMBER\"];\n" +
      "  node19 -> node20 [weight=1];\n" +
      "  node18 -> node17 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node21 [label=\"LT\"];\n" +
      "  node17 -> node21 [weight=1];\n" +
      "  node22 [label=\"NAME\"];\n" +
      "  node21 -> node22 [weight=1];\n" +
      "  node23 [label=\"NUMBER\"];\n" +
      "  node21 -> node23 [weight=1];\n" +
      "  node24 [label=\"INC\"];\n" +
      "  node17 -> node24 [weight=1];\n" +
      "  node25 [label=\"NAME\"];\n" +
      "  node24 -> node25 [weight=1];\n" +
      "  node24 -> node17 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node26 [label=\"BLOCK\"];\n" +
      "  node17 -> node26 [weight=1];\n" +
      "  node27 [label=\"CONTINUE\"];\n" +
      "  node26 -> node27 [weight=1];\n" +
      "  node27 -> node24 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node28 [label=\"EXPR_RESULT\"];\n" +
      "  node26 -> node28 [weight=1];\n" +
      "  node29 [label=\"CALL\"];\n" +
      "  node28 -> node29 [weight=1];\n" +
      "  node30 [label=\"NAME\"];\n" +
      "  node29 -> node30 [weight=1];\n" +
      "  node28 -> node24 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node26 -> node27 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node17 -> node14 " +
      "[label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node17 -> node26 " +
      "[label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node16 -> node18 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node8 -> RETURN " +
      "[label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node8 -> node16 " +
      "[label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testNestedDoWithBreak
  public void testNestedDoWithBreak() {
    
    String src = "var a;do{do{break}while(a);do{a()}while(a)}while(a);";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"VAR\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"BLOCK\"];\n" +
      "  node1 -> node3 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 [label=\"DO\"];\n" +
      "  node0 -> node4 [weight=1];\n" +
      "  node4 -> node3 [weight=1];\n" +
      "  node5 [label=\"DO\"];\n" +
      "  node3 -> node5 [weight=1];\n" +
      "  node6 [label=\"BLOCK\"];\n" +
      "  node5 -> node6 [weight=1];\n" +
      "  node7 [label=\"BREAK\"];\n" +
      "  node6 -> node7 [weight=1];\n" +
      "  node8 [label=\"BLOCK\"];\n" +
      "  node7 -> node8 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node6 -> node7 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node9 [label=\"NAME\"];\n" +
      "  node5 -> node9 [weight=1];\n" +
      "  node5 -> node6 " +
      "[label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node5 -> node8 " +
      "[label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node10 [label=\"DO\"];\n" +
      "  node3 -> node10 [weight=1];\n" +
      "  node10 -> node8 [weight=1];\n" +
      "  node11 [label=\"EXPR_RESULT\"];\n" +
      "  node8 -> node11 [weight=1];\n" +
      "  node12 [label=\"CALL\"];\n" +
      "  node11 -> node12 [weight=1];\n" +
      "  node13 [label=\"NAME\"];\n" +
      "  node12 -> node13 [weight=1];\n" +
      "  node11 -> node10 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node8 -> node11 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node14 [label=\"NAME\"];\n" +
      "  node10 -> node14 [weight=1];\n" +
      "  node10 -> node4 " +
      "[label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node10 -> node8 " +
      "[label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node3 -> node6 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node15 [label=\"NAME\"];\n" +
      "  node4 -> node15 [weight=1];\n" +
      "  node4 -> RETURN " +
      "[label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> node3 " +
      "[label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testForIn
  public void testForIn() {
    String src = "var a,b;for(a in b){a()};";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"VAR\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"NAME\"];\n" +
      "  node1 -> node3 [weight=1];\n" +
      "  node4 [label=\"NAME\"];\n" +
      "  node1 -> node4 [label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node5 [label=\"FOR\"];\n" +
      "  node0 -> node5 [weight=1];\n" +
      "  node6 [label=\"NAME\"];\n" +
      "  node5 -> node6 [weight=1];\n" +
      "  node5 -> node4 [weight=1];\n" +
      "  node4 -> node5 [label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node7 [label=\"BLOCK\"];\n" +
      "  node5 -> node7 [weight=1];\n" +
      "  node8 [label=\"EXPR_RESULT\"];\n" +
      "  node7 -> node8 [weight=1];\n" +
      "  node9 [label=\"CALL\"];\n" +
      "  node8 -> node9 [weight=1];\n" +
      "  node10 [label=\"NAME\"];\n" +
      "  node9 -> node10 [weight=1];\n" +
      "  node8 -> node5 [label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node7 -> node8 [label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node11 [label=\"EMPTY\"];\n" +
      "  node5 -> node11 [label=\"ON_FALSE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node5 -> node7 [label=\"ON_TRUE\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node11 [weight=1];\n" +
      "  node11 -> RETURN [label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node1 [label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testThrow
  public void testThrow() {
    String src = "function f() { throw 1; f() }";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"FUNCTION\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"PARAM_LIST\"];\n" +
      "  node1 -> node3 [weight=1];\n" +
      "  node4 [label=\"BLOCK\"];\n" +
      "  node1 -> node4 [weight=1];\n" +
      "  node5 [label=\"THROW\"];\n" +
      "  node4 -> node5 [weight=1];\n" +
      "  node6 [label=\"NUMBER\"];\n" +
      "  node5 -> node6 [weight=1];\n" +
      "  node7 [label=\"EXPR_RESULT\"];\n" +
      "  node4 -> node7 [weight=1];\n" +
      "  node8 [label=\"CALL\"];\n" +
      "  node7 -> node8 [weight=1];\n" +
      "  node9 [label=\"NAME\"];\n" +
      "  node8 -> node9 [weight=1];\n" +
      "  node7 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> node5 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node1 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleFunction
  public void testSimpleFunction() {
    String src = "function f() { f() } f()";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"FUNCTION\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"PARAM_LIST\"];\n" +
      "  node1 -> node3 [weight=1];\n" +
      "  node4 [label=\"BLOCK\"];\n" +
      "  node1 -> node4 [weight=1];\n" +
      "  node5 [label=\"EXPR_RESULT\"];\n" +
      "  node4 -> node5 [weight=1];\n" +
      "  node6 [label=\"CALL\"];\n" +
      "  node5 -> node6 [weight=1];\n" +
      "  node7 [label=\"NAME\"];\n" +
      "  node6 -> node7 [weight=1];\n" +
      "  node5 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> node5 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node1 -> node4 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node8 [label=\"EXPR_RESULT\"];\n" +
      "  node0 -> node8 [weight=1];\n" +
      "  node9 [label=\"CALL\"];\n" +
      "  node8 -> node9 [weight=1];\n" +
      "  node10 [label=\"NAME\"];\n" +
      "  node9 -> node10 [weight=1];\n" +
      "  node8 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node8 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleCatch
  public void testSimpleCatch() {
    String src = "try{ throw x; x(); x['stuff']; x.x; x} catch (e) { e() }";
    String expected = "digraph AST {\n"
        + "  node [color=lightblue2, style=filled];\n"
        + "  node0 [label=\"SCRIPT\"];\n"
        + "  node1 [label=\"TRY\"];\n"
        + "  node0 -> node1 [weight=1];\n"
        + "  node2 [label=\"BLOCK\"];\n"
        + "  node1 -> node2 [weight=1];\n"
        + "  node3 [label=\"THROW\"];\n"
        + "  node2 -> node3 [weight=1];\n"
        + "  node4 [label=\"NAME\"];\n"
        + "  node3 -> node4 [weight=1];\n"
        + "  node5 [label=\"BLOCK\"];\n"
        + "  node3 -> node5 [label=\"ON_EX\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node6 [label=\"EXPR_RESULT\"];\n"
        + "  node2 -> node6 [weight=1];\n"
        + "  node7 [label=\"CALL\"];\n"
        + "  node6 -> node7 [weight=1];\n"
        + "  node8 [label=\"NAME\"];\n"
        + "  node7 -> node8 [weight=1];\n"
        + "  node9 [label=\"EXPR_RESULT\"];\n"
        + "  node6 -> node5 [label=\"ON_EX\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node6 -> node9 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node2 -> node9 [weight=1];\n"
        + "  node10 [label=\"GETELEM\"];\n"
        + "  node9 -> node10 [weight=1];\n"
        + "  node11 [label=\"NAME\"];\n"
        + "  node10 -> node11 [weight=1];\n"
        + "  node12 [label=\"STRING\"];\n"
        + "  node10 -> node12 [weight=1];\n"
        + "  node13 [label=\"EXPR_RESULT\"];\n"
        + "  node9 -> node13 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node9 -> node5 [label=\"ON_EX\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node2 -> node13 [weight=1];\n"
        + "  node14 [label=\"GETPROP\"];\n"
        + "  node13 -> node14 [weight=1];\n"
        + "  node15 [label=\"NAME\"];\n"
        + "  node14 -> node15 [weight=1];\n"
        + "  node16 [label=\"STRING\"];\n"
        + "  node14 -> node16 [weight=1];\n"
        + "  node17 [label=\"EXPR_RESULT\"];\n"
        + "  node13 -> node17 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node13 -> node5 [label=\"ON_EX\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node2 -> node17 [weight=1];\n"
        + "  node18 [label=\"NAME\"];\n"
        + "  node17 -> node18 [weight=1];\n"
        + "  node17 -> RETURN [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node2 -> node3 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node1 -> node5 [weight=1];\n"
        + "  node19 [label=\"CATCH\"];\n"
        + "  node5 -> node19 [weight=1];\n"
        + "  node20 [label=\"NAME\"];\n"
        + "  node19 -> node20 [weight=1];\n"
        + "  node21 [label=\"BLOCK\"];\n"
        + "  node19 -> node21 [weight=1];\n"
        + "  node22 [label=\"EXPR_RESULT\"];\n"
        + "  node21 -> node22 [weight=1];\n"
        + "  node23 [label=\"CALL\"];\n"
        + "  node22 -> node23 [weight=1];\n"
        + "  node24 [label=\"NAME\"];\n"
        + "  node23 -> node24 [weight=1];\n"
        + "  node22 -> RETURN [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node21 -> node22 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node19 -> node21 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node5 -> node19 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node1 -> node2 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node0 -> node1 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testFunctionWithinTry
  public void testFunctionWithinTry() {
    
    String src = "try { function f() {throw 1;} } catch (e) { }";
    String expected = "digraph AST {\n"
        + "  node [color=lightblue2, style=filled];\n"
        + "  node0 [label=\"SCRIPT\"];\n"
        + "  node1 [label=\"TRY\"];\n"
        + "  node0 -> node1 [weight=1];\n"
        + "  node2 [label=\"BLOCK\"];\n"
        + "  node1 -> node2 [weight=1];\n"
        + "  node3 [label=\"FUNCTION\"];\n"
        + "  node2 -> node3 [weight=1];\n"
        + "  node4 [label=\"NAME\"];\n"
        + "  node3 -> node4 [weight=1];\n"
        + "  node5 [label=\"PARAM_LIST\"];\n"
        + "  node3 -> node5 [weight=1];\n"
        + "  node6 [label=\"BLOCK\"];\n"
        + "  node3 -> node6 [weight=1];\n"
        + "  node7 [label=\"THROW\"];\n"
        + "  node6 -> node7 [weight=1];\n"
        + "  node8 [label=\"NUMBER\"];\n"
        + "  node7 -> node8 [weight=1];\n"
        + "  node6 -> node7 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node3 -> node6 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node2 -> RETURN [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node9 [label=\"BLOCK\"];\n"
        + "  node1 -> node9 [weight=1];\n"
        + "  node10 [label=\"CATCH\"];\n"
        + "  node9 -> node10 [weight=1];\n"
        + "  node11 [label=\"NAME\"];\n"
        + "  node10 -> node11 [weight=1];\n"
        + "  node12 [label=\"BLOCK\"];\n"
        + "  node10 -> node12 [weight=1];\n"
        + "  node12 -> RETURN [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node10 -> node12 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node9 -> node10 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node1 -> node2 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node0 -> node1 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testNestedCatch
  public void testNestedCatch() {
    
    String src = "try{try{throw 1;}catch(e){throw 2}}catch(f){}";
    String expected = "digraph AST {\n"
        + "  node [color=lightblue2, style=filled];\n"
        + "  node0 [label=\"SCRIPT\"];\n"
        + "  node1 [label=\"TRY\"];\n"
        + "  node0 -> node1 [weight=1];\n"
        + "  node2 [label=\"BLOCK\"];\n"
        + "  node1 -> node2 [weight=1];\n"
        + "  node3 [label=\"TRY\"];\n"
        + "  node2 -> node3 [weight=1];\n"
        + "  node4 [label=\"BLOCK\"];\n"
        + "  node3 -> node4 [weight=1];\n"
        + "  node5 [label=\"THROW\"];\n"
        + "  node4 -> node5 [weight=1];\n"
        + "  node6 [label=\"NUMBER\"];\n"
        + "  node5 -> node6 [weight=1];\n"
        + "  node7 [label=\"BLOCK\"];\n"
        + "  node5 -> node7 [label=\"ON_EX\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node4 -> node5 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node3 -> node7 [weight=1];\n"
        + "  node8 [label=\"CATCH\"];\n"
        + "  node7 -> node8 [weight=1];\n"
        + "  node9 [label=\"NAME\"];\n"
        + "  node8 -> node9 [weight=1];\n"
        + "  node10 [label=\"BLOCK\"];\n"
        + "  node8 -> node10 [weight=1];\n"
        + "  node11 [label=\"THROW\"];\n"
        + "  node10 -> node11 [weight=1];\n"
        + "  node12 [label=\"NUMBER\"];\n"
        + "  node11 -> node12 [weight=1];\n"
        + "  node13 [label=\"BLOCK\"];\n"
        + "  node11 -> node13 [label=\"ON_EX\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node10 -> node11 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node8 -> node10 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node7 -> node8 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node3 -> node4 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node2 -> node3 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node1 -> node13 [weight=1];\n"
        + "  node14 [label=\"CATCH\"];\n"
        + "  node13 -> node14 [weight=1];\n"
        + "  node15 [label=\"NAME\"];\n"
        + "  node14 -> node15 [weight=1];\n"
        + "  node16 [label=\"BLOCK\"];\n"
        + "  node14 -> node16 [weight=1];\n"
        + "  node16 -> RETURN [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node14 -> node16 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node13 -> node14 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node1 -> node2 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node0 -> node1 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleFinally
  public void testSimpleFinally() {
    String src = "try{var x; foo()}finally{}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.TRY, Token.BLOCK, Branch.UNCOND);
    assertDownEdge(cfg, Token.BLOCK, Token.VAR, Branch.UNCOND);
    
    assertCrossEdge(cfg, Token.EXPR_RESULT, Token.BLOCK, Branch.UNCOND);
    
    assertNoEdge(cfg, Token.BLOCK, Token.BLOCK);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSimpleCatchFinally
  public void testSimpleCatchFinally() {
    
    String src = "try{ if(a){throw 1}else{a} } catch(e){a}finally{a}";
    String expected = "digraph AST {\n"
        + "  node [color=lightblue2, style=filled];\n"
        + "  node0 [label=\"SCRIPT\"];\n"
        + "  node1 [label=\"TRY\"];\n"
        + "  node0 -> node1 [weight=1];\n"
        + "  node2 [label=\"BLOCK\"];\n"
        + "  node1 -> node2 [weight=1];\n"
        + "  node3 [label=\"IF\"];\n"
        + "  node2 -> node3 [weight=1];\n"
        + "  node4 [label=\"NAME\"];\n"
        + "  node3 -> node4 [weight=1];\n"
        + "  node5 [label=\"BLOCK\"];\n"
        + "  node3 -> node5 [weight=1];\n"
        + "  node6 [label=\"THROW\"];\n"
        + "  node5 -> node6 [weight=1];\n"
        + "  node7 [label=\"NUMBER\"];\n"
        + "  node6 -> node7 [weight=1];\n"
        + "  node8 [label=\"BLOCK\"];\n"
        + "  node6 -> node8 [label=\"ON_EX\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node5 -> node6 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node9 [label=\"BLOCK\"];\n"
        + "  node3 -> node9 [weight=1];\n"
        + "  node10 [label=\"EXPR_RESULT\"];\n"
        + "  node9 -> node10 [weight=1];\n"
        + "  node11 [label=\"NAME\"];\n"
        + "  node10 -> node11 [weight=1];\n"
        + "  node12 [label=\"BLOCK\"];\n"
        + "  node10 -> node12 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node9 -> node10 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node3 -> node5 [label=\"ON_TRUE\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node3 -> node9 [label=\"ON_FALSE\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node2 -> node3 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node1 -> node8 [weight=1];\n"
        + "  node13 [label=\"CATCH\"];\n"
        + "  node8 -> node13 [weight=1];\n"
        + "  node14 [label=\"NAME\"];\n"
        + "  node13 -> node14 [weight=1];\n"
        + "  node15 [label=\"BLOCK\"];\n"
        + "  node13 -> node15 [weight=1];\n"
        + "  node16 [label=\"EXPR_RESULT\"];\n"
        + "  node15 -> node16 [weight=1];\n"
        + "  node17 [label=\"NAME\"];\n"
        + "  node16 -> node17 [weight=1];\n"
        + "  node16 -> node12 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node15 -> node16 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node13 -> node15 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node8 -> node13 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node1 -> node12 [weight=1];\n"
        + "  node18 [label=\"EXPR_RESULT\"];\n"
        + "  node12 -> node18 [weight=1];\n"
        + "  node19 [label=\"NAME\"];\n"
        + "  node18 -> node19 [weight=1];\n"
        + "  node18 -> RETURN [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node12 -> node18 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node1 -> node2 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "  node0 -> node1 [label=\"UNCOND\", " +
                "fontcolor=\"red\", weight=0.01, color=\"red\"];\n"
        + "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testComplicatedFinally2
  public void testComplicatedFinally2() {
    
    String src = "while(1){try{" +
      "if(a){a;continue;}else if(b){b;break;} else if(c) throw 1; else a}" +
      "catch(e){}finally{c()}bar}foo";

    ControlFlowGraph<Node> cfg = createCfg(src);
    
    assertCrossEdge(cfg, Token.CONTINUE, Token.BLOCK, Branch.UNCOND);
    assertCrossEdge(cfg, Token.BREAK, Token.BLOCK, Branch.UNCOND);
    assertCrossEdge(cfg, Token.THROW, Token.BLOCK, Branch.ON_EX);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testDeepNestedBreakwithFinally
  public void testDeepNestedBreakwithFinally() {
    String src = "X:while(1){try{while(2){try{var a;break X;}" +
        "finally{}}}finally{}}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.WHILE, Token.BLOCK, Branch.ON_TRUE);
    assertDownEdge(cfg, Token.BLOCK, Token.TRY, Branch.UNCOND);
    assertDownEdge(cfg, Token.BLOCK, Token.VAR, Branch.UNCOND);
    
    assertCrossEdge(cfg, Token.BREAK, Token.BLOCK, Branch.UNCOND);
    
    assertCrossEdge(cfg, Token.BLOCK, Token.BLOCK, Branch.ON_EX);
    assertCrossEdge(cfg, Token.WHILE, Token.BLOCK, Branch.ON_FALSE);
    assertReturnEdge(cfg, Token.BLOCK);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testDeepNestedFinally
  public void testDeepNestedFinally() {
    String src = "try{try{try{throw 1}" +
        "finally{1;var a}}finally{2;if(a);}}finally{3;a()}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.THROW, Token.BLOCK, Branch.ON_EX);
    assertCrossEdge(cfg, Token.VAR, Token.BLOCK, Branch.UNCOND);
    assertCrossEdge(cfg, Token.IF, Token.BLOCK, Branch.ON_EX);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testReturn
  public void testReturn() {
    String src = "function f() { return; }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertReturnEdge(cfg, Token.RETURN);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testReturnInFinally
  public void testReturnInFinally() {
    String src = "function f(x){ try{} finally {return x;} }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertReturnEdge(cfg, Token.RETURN);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testReturnInFinally2
  public void testReturnInFinally2() {
    String src = "function f(x){" +
      " try{ try{}finally{var dummy; return x;} } finally {} }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.VAR, Token.RETURN, Branch.UNCOND);
    assertCrossEdge(cfg, Token.RETURN, Token.BLOCK, Branch.UNCOND);
    assertReturnEdge(cfg, Token.BLOCK);
    assertNoReturnEdge(cfg, Token.RETURN);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testReturnInTry
  public void testReturnInTry() {
    String src = "function f(x){ try{x; return x()} finally {} var y;}";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.EXPR_RESULT, Token.RETURN, Branch.UNCOND);
    assertCrossEdge(cfg, Token.RETURN, Token.BLOCK, Branch.UNCOND);
    assertCrossEdge(cfg, Token.BLOCK, Token.VAR, Branch.UNCOND);
    assertReturnEdge(cfg, Token.VAR);
    assertReturnEdge(cfg, Token.BLOCK);
    assertNoReturnEdge(cfg, Token.RETURN);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testOptionNotToTraverseFunctions
  public void testOptionNotToTraverseFunctions() {
    String src = "var x = 1; function f() { x = null; }";
    String expectedWhenNotTraversingFunctions = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"VAR\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"NUMBER\"];\n" +
      "  node2 -> node3 [weight=1];\n" +
      "  node1 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 [label=\"FUNCTION\"];\n" +
      "  node0 -> node4 [weight=1];\n" +
      "  node5 [label=\"NAME\"];\n" +
      "  node4 -> node5 [weight=1];\n" +
      "  node6 [label=\"PARAM_LIST\"];\n" +
      "  node4 -> node6 [weight=1];\n" +
      "  node7 [label=\"BLOCK\"];\n" +
      "  node4 -> node7 [weight=1];\n" +
      "  node8 [label=\"EXPR_RESULT\"];\n" +
      "  node7 -> node8 [weight=1];\n" +
      "  node9 [label=\"ASSIGN\"];\n" +
      "  node8 -> node9 [weight=1];\n" +
      "  node10 [label=\"NAME\"];\n" +
      "  node9 -> node10 [weight=1];\n" +
      "  node11 [label=\"NULL\"];\n" +
      "  node9 -> node11 [weight=1];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    String expected = "digraph AST {\n" +
      "  node [color=lightblue2, style=filled];\n" +
      "  node0 [label=\"SCRIPT\"];\n" +
      "  node1 [label=\"VAR\"];\n" +
      "  node0 -> node1 [weight=1];\n" +
      "  node2 [label=\"NAME\"];\n" +
      "  node1 -> node2 [weight=1];\n" +
      "  node3 [label=\"NUMBER\"];\n" +
      "  node2 -> node3 [weight=1];\n" +
      "  node1 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 [label=\"FUNCTION\"];\n" +
      "  node0 -> node4 [weight=1];\n" +
      "  node5 [label=\"NAME\"];\n" +
      "  node4 -> node5 [weight=1];\n" +
      "  node6 [label=\"PARAM_LIST\"];\n" +
      "  node4 -> node6 [weight=1];\n" +
      "  node7 [label=\"BLOCK\"];\n" +
      "  node4 -> node7 [weight=1];\n" +
      "  node8 [label=\"EXPR_RESULT\"];\n" +
      "  node7 -> node8 [weight=1];\n" +
      "  node9 [label=\"ASSIGN\"];\n" +
      "  node8 -> node9 [weight=1];\n" +
      "  node10 [label=\"NAME\"];\n" +
      "  node9 -> node10 [weight=1];\n" +
      "  node11 [label=\"NULL\"];\n" +
      "  node9 -> node11 [weight=1];\n" +
      "  node8 -> RETURN " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node7 -> node8 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node4 -> node7 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "  node0 -> node1 " +
      "[label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
      "}\n";
    testCfg(src, expected);
    testCfg(src, expectedWhenNotTraversingFunctions, false);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testInstanceOf
  public void testInstanceOf() {
    String src = "try { x instanceof 'x' } catch (e) { }";
    ControlFlowGraph<Node> cfg = createCfg(src, true);
    assertCrossEdge(cfg, Token.EXPR_RESULT, Token.BLOCK, Branch.ON_EX);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testSynBlock
  public void testSynBlock() {
    String src = "START(); var x; END(); var y;";
    ControlFlowGraph<Node> cfg = createCfg(src, true);
    assertCrossEdge(cfg, Token.BLOCK, Token.EXPR_RESULT, Branch.SYN_BLOCK);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testPartialTraversalOfScope
  public void testPartialTraversalOfScope() {
    Compiler compiler = new Compiler();
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true, true);

    Node script1 = compiler.parseSyntheticCode("cfgtest", "var foo;");
    Node script2 = compiler.parseSyntheticCode("cfgtest2", "var bar;");
    
    new Node(Token.BLOCK, script1, script2);

    cfa.process(null, script1);
    ControlFlowGraph<Node> cfg = cfa.getCfg();

    assertNotNull(cfg.getNode(script1));
    assertNull(cfg.getNode(script2));
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testForLoopOrder
  public void testForLoopOrder() {
    assertNodeOrder(
        createCfg("for (var i = 0; i < 5; i++) { var x = 3; } if (true) {}"),
        Lists.newArrayList(
            Token.SCRIPT, Token.VAR, Token.FOR, Token.BLOCK, Token.VAR,
            Token.INC ,
            Token.IF, Token.BLOCK));
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testLabelledForInLoopOrder
  public void testLabelledForInLoopOrder() {
    assertNodeOrder(
        createCfg("var i = 0; var y = {}; " +
            "label: for (var x in y) { " +
            "    if (x) { break label; } else { i++ } x(); }"),
        Lists.newArrayList(
            Token.SCRIPT, Token.VAR, Token.VAR, Token.NAME,
            Token.FOR, Token.BLOCK,
            Token.IF, Token.BLOCK, Token.BREAK,
            Token.BLOCK, Token.EXPR_RESULT, Token.EXPR_RESULT));
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testLocalFunctionOrder
  public void testLocalFunctionOrder() {
    ControlFlowGraph<Node> cfg =
        createCfg("function f() { while (x) { x++; } } var x = 3;");
    assertNodeOrder(
        cfg,
        Lists.newArrayList(
            Token.SCRIPT, Token.VAR,

            Token.FUNCTION, Token.BLOCK,
            Token.WHILE, Token.BLOCK, Token.EXPR_RESULT));
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testDoWhileOrder
  public void testDoWhileOrder() {
    assertNodeOrder(
        createCfg("do { var x = 3; } while (true); void x;"),
        Lists.newArrayList(
            Token.SCRIPT, Token.BLOCK, Token.VAR, Token.DO, Token.EXPR_RESULT));
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testBreakInFinally1
  public void testBreakInFinally1() {
    String src =
        "f = function() {\n" +
        "  var action;\n" +
        "  a: {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "      break a\n" +  
        "    }\n" +
        "  }\n" +
        "  alert(action)\n" + 
        "};";
    String expected =
        "digraph AST {\n" +
        "  node [color=lightblue2, style=filled];\n" +
        "  node0 [label=\"SCRIPT\"];\n" +
        "  node1 [label=\"EXPR_RESULT\"];\n" +
        "  node0 -> node1 [weight=1];\n" +
        "  node2 [label=\"ASSIGN\"];\n" +
        "  node1 -> node2 [weight=1];\n" +
        "  node3 [label=\"NAME\"];\n" +
        "  node2 -> node3 [weight=1];\n" +
        "  node4 [label=\"FUNCTION\"];\n" +
        "  node2 -> node4 [weight=1];\n" +
        "  node5 [label=\"NAME\"];\n" +
        "  node4 -> node5 [weight=1];\n" +
        "  node6 [label=\"PARAM_LIST\"];\n" +
        "  node4 -> node6 [weight=1];\n" +
        "  node7 [label=\"BLOCK\"];\n" +
        "  node4 -> node7 [weight=1];\n" +
        "  node8 [label=\"VAR\"];\n" +
        "  node7 -> node8 [weight=1];\n" +
        "  node9 [label=\"NAME\"];\n" +
        "  node8 -> node9 [weight=1];\n" +
        "  node10 [label=\"LABEL\"];\n" +
        "  node7 -> node10 [weight=1];\n" +
        "  node11 [label=\"LABEL_NAME\"];\n" +
        "  node10 -> node11 [weight=1];\n" +
        "  node12 [label=\"BLOCK\"];\n" +
        "  node10 -> node12 [weight=1];\n" +
        "  node13 [label=\"VAR\"];\n" +
        "  node12 -> node13 [weight=1];\n" +
        "  node14 [label=\"NAME\"];\n" +
        "  node13 -> node14 [weight=1];\n" +
        "  node15 [label=\"NULL\"];\n" +
        "  node14 -> node15 [weight=1];\n" +
        "  node16 [label=\"TRY\"];\n" +
        "  node12 -> node16 [weight=1];\n" +
        "  node17 [label=\"BLOCK\"];\n" +
        "  node16 -> node17 [weight=1];\n" +
        "  node18 [label=\"EXPR_RESULT\"];\n" +
        "  node17 -> node18 [weight=1];\n" +
        "  node19 [label=\"ASSIGN\"];\n" +
        "  node18 -> node19 [weight=1];\n" +
        "  node20 [label=\"NAME\"];\n" +
        "  node19 -> node20 [weight=1];\n" +
        "  node21 [label=\"NEW\"];\n" +
        "  node19 -> node21 [weight=1];\n" +
        "  node22 [label=\"NAME\"];\n" +
        "  node21 -> node22 [weight=1];\n" +
        "  node23 [label=\"BLOCK\"];\n" +
        "  node16 -> node23 [weight=1];\n" +
        "  node24 [label=\"BLOCK\"];\n" +
        "  node16 -> node24 [weight=1];\n" +
        "  node25 [label=\"EXPR_RESULT\"];\n" +
        "  node24 -> node25 [weight=1];\n" +
        "  node26 [label=\"ASSIGN\"];\n" +
        "  node25 -> node26 [weight=1];\n" +
        "  node27 [label=\"NAME\"];\n" +
        "  node26 -> node27 [weight=1];\n" +
        "  node28 [label=\"NAME\"];\n" +
        "  node26 -> node28 [weight=1];\n" +
        "  node29 [label=\"BREAK\"];\n" +
        "  node24 -> node29 [weight=1];\n" +
        "  node30 [label=\"LABEL_NAME\"];\n" +
        "  node29 -> node30 [weight=1];\n" +
        "  node31 [label=\"EXPR_RESULT\"];\n" +
        "  node7 -> node31 [weight=1];\n" +
        "  node32 [label=\"CALL\"];\n" +
        "  node31 -> node32 [weight=1];\n" +
        "  node33 [label=\"NAME\"];\n" +
        "  node32 -> node33 [weight=1];\n" +
        "  node34 [label=\"NAME\"];\n" +
        "  node32 -> node34 [weight=1];\n" +
        "  node1 -> RETURN [label=\"UNCOND\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "  node0 -> node1 [label=\"UNCOND\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "}\n";
    testCfg(src, expected);
  }

// com.google.javascript.jscomp.ControlFlowAnalysisTest::testBreakInFinally2
  public void testBreakInFinally2() {
    String src =
      "var action;\n" +
      "a: {\n" +
      "  var proto = null;\n" +
      "  try {\n" +
      "    proto = new Proto\n" +
      "  } finally {\n" +
      "    action = proto;\n" +
      "    break a\n" +
      "  }\n" +
      "}\n" +
      "alert(action)\n";

    ControlFlowGraph<Node> cfg = createCfg(src);
    assertCrossEdge(cfg, Token.BREAK, Token.EXPR_RESULT, Branch.UNCOND);
    assertNoEdge(cfg, Token.BREAK, Token.BLOCK);
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testWhile
  public void testWhile() {
    assertNoError("while(1) { break; }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testNextedWhile
  public void testNextedWhile() {
    assertNoError("while(1) { while(1) { break; } }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testBreak
  public void testBreak() {
    assertInvalidBreak("break;");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinue
  public void testContinue() {
    assertInvalidContinue("continue;");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testBreakCrossFunction
  public void testBreakCrossFunction() {
    assertInvalidBreak("while(1) { function f() { break; } }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testBreakCrossFunctionInFor
  public void testBreakCrossFunctionInFor() {
    assertInvalidBreak("while(1) {for(var f = function () { break; };;) {}}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitch
  public void testContinueToSwitch() {
    assertInvalidContinue("switch(1) {case(1): continue; }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitchWithNoCases
  public void testContinueToSwitchWithNoCases() {
    assertNoError("switch(1){}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitchWithTwoCases
  public void testContinueToSwitchWithTwoCases() {
    assertInvalidContinue("switch(1){case(1):break;case(2):continue;}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitchWithDefault
  public void testContinueToSwitchWithDefault() {
    assertInvalidContinue("switch(1){case(1):break;case(2):default:continue;}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToLabelSwitch
  public void testContinueToLabelSwitch() {
    assertInvalidLabeledContinue(
        "while(1) {a: switch(1) {case(1): continue a; }}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueOutsideSwitch
  public void testContinueOutsideSwitch() {
    assertNoError("b: while(1) { a: switch(1) { case(1): continue b; } }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueNotCrossFunction1
  public void testContinueNotCrossFunction1() {
    assertNoError("a:switch(1){case(1):function f(){a:while(1){continue a;}}}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueNotCrossFunction2
  public void testContinueNotCrossFunction2() {
    assertUndefinedLabel(
        "a:switch(1){case(1):function f(){while(1){continue a;}}}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testUseOfWith1
  public void testUseOfWith1() {
    testSame("with(a){}", ControlStructureCheck.USE_OF_WITH);
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testUseOfWith2
  public void testUseOfWith2() {
    testSame("" +
             "with(a){}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testUseOfWith3
  public void testUseOfWith3() {
    testSame(
        "function f(expr, context) {\n" +
        "  try {\n" +
        "     with (context) {\n" +
        "      return eval('[' + expr + '][0]');\n" +
        "    }\n" +
        "  } catch (e) {\n" +
        "    return null;\n" +
        "  }\n" +
        "};\n");
  }

// com.google.javascript.jscomp.ConvertToDottedPropertiesTest::testConvert
  public void testConvert() {
    test("a['p']", "a.p");
    test("a['_p_']", "a._p_");
    test("a['_']", "a._");
    test("a['$']", "a.$");
    test("a.b.c['p']", "a.b.c.p");
    test("a.b['c'].p", "a.b.c.p");
    test("a['p']();", "a.p();");
    test("a()['p']", "a().p");
    
    test("a['\u0041A']", "a.AA");
  }

// com.google.javascript.jscomp.ConvertToDottedPropertiesTest::testDoNotConvert
  public void testDoNotConvert() {
    testSame("a[0]");
    testSame("a['']");
    testSame("a[' ']");
    testSame("a[',']");
    testSame("a[';']");
    testSame("a[':']");
    testSame("a['.']");
    testSame("a['0']");
    testSame("a['p ']");
    testSame("a['p' + '']");
    testSame("a[p]");
    testSame("a[P]");
    testSame("a[$]");
    testSame("a[p()]");
    testSame("a['default']");
    
    testSame("a['A\u0004']");
    
    
    test("a['\u1d17A']", "a['\u1d17A']");
    
    
    test("a['\u00d1StuffAfter']", "a['\u00d1StuffAfter']");
  }

// com.google.javascript.jscomp.ConvertToDottedPropertiesTest::testQuotedProps
  public void testQuotedProps() {
    testSame("({'':0})");
    testSame("({'1.0':0})");
    testSame("({'\u1d17A':0})");
    testSame("({'a\u0004b':0})");
  }

// com.google.javascript.jscomp.ConvertToDottedPropertiesTest::test5746867
  public void test5746867() {
    testSame("var a = { '$\\\\' : 5 };");
    testSame("var a = { 'x\\\\u0041$\\\\' : 5 };");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFold1
  public void testFold1() {
    test("function f() { if (x) return; y(); }",
         "function f(){x||y()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers1
  public void testFoldWithMarkers1() {
    testSame("function f(){startMarker();if(x)return;endMarker();y()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers1a
  public void testFoldWithMarkers1a() {
    testSame("function f(){startMarker();if(x)return;endMarker()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFold2
  public void testFold2() {
    test("function f() { if (x) return; y(); if (a) return; b(); }",
         "function f(){if(!x){y();a||b()}}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers2
  public void testFoldWithMarkers2() {
    testSame("function f(){startMarker(\"FOO\");startMarker(\"BAR\");" +
             "if(x)return;endMarker(\"BAR\");y();if(a)return;" +
             "endMarker(\"FOO\");b()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testUnmatchedStartMarker
  public void testUnmatchedStartMarker() {
    testSame("startMarker()", CreateSyntheticBlocks.UNMATCHED_START_MARKER);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testUnmatchedEndMarker1
  public void testUnmatchedEndMarker1() {
    testSame("endMarker()", CreateSyntheticBlocks.UNMATCHED_END_MARKER);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testUnmatchedEndMarker2
  public void testUnmatchedEndMarker2() {
    test("if(y){startMarker();x()}endMarker()",
        "if(y){startMarker();x()}endMarker()", null,
         CreateSyntheticBlocks.UNMATCHED_END_MARKER);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testInvalid1
  public void testInvalid1() {
    test("startMarker() && true",
        "startMarker()", null,
         CreateSyntheticBlocks.INVALID_MARKER_USAGE);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testInvalid2
  public void testInvalid2() {
    test("false && endMarker()",
        "", null,
         CreateSyntheticBlocks.INVALID_MARKER_USAGE);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testDenormalize
  public void testDenormalize() {
    testSame("startMarker();for(;;);endMarker()");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testNonMarkingUse
  public void testNonMarkingUse() {
    testSame("function foo(endMarker){}");
    testSame("function foo(){startMarker:foo()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testContainingBlockPreservation
  public void testContainingBlockPreservation() {
    testSame("if(y){startMarker();x();endMarker()}");
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement1
  public void testFunctionMovement1() {
    
    
    
    
    
    
    

    JSModule[] modules = createModuleStar(
      
      "function f1(a) { alert(a); }" +
      "function f2(a) { alert(a); }" +
      "function f3(a) { alert(a); }" +
      "function f4() { alert(1); }" +
      "function g() { alert('ciao'); }",
      
      "f1('hi'); f3('bye'); var a = f4;" +
      "function h(a) { alert('h:' + a); }",
      
      "f2('hi'); f2('hi'); f3('bye');");

    test(modules, new String[] {
      
      "function f3(a) { alert(a); }" +
      "function g() { alert('ciao'); }",
      
      "function f4() { alert(1); }" +
      "function f1(a) { alert(a); }" +
      "f1('hi'); f3('bye'); var a = f4;" +
      "function h(a) { alert('h:' + a); }",
      
      "function f2(a) { alert(a); }" +
      "f2('hi'); f2('hi'); f3('bye');",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement2
  public void testFunctionMovement2() {
    
    JSModule[] modules = createModuleStar(
      
      "function f(a) { alert(a); }" +
      "function g() {var f = 1; f++}",
      
      "f(1);");

    test(modules, new String[] {
      
      "function g() {var f = 1; f++}",
      
      "function f(a) { alert(a); }" +
      "f(1);",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement3
  public void testFunctionMovement3() {
    
    JSModule[] modules = createModuleStar(
      
      "function f(a) { alert(a); }" +
      "function g(f) {f++}",
      
      "f(1);");

    test(modules, new String[] {
      
      "function g(f) {f++}",
      
      "function f(a) { alert(a); }" +
      "f(1);",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement4
  public void testFunctionMovement4() {
    
    JSModule[] modules = createModuleStar(
      
      "function f(){return function(a){}}",
      
      "var a = f();"
    );

    test(modules, new String[] {
      
      "",
      
      "function f(){return function(a){}}" +
      "var a = f();",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement5
  public void testFunctionMovement5() {
    
    JSModule[] modules = createModuleStar(
      
      "function f(n){return (n<1)?1:f(n-1)}",
      
      "var a = f(4);"
    );

    test(modules, new String[] {
      
      "",
      
      "function f(n){return (n<1)?1:f(n-1)}" +
      "var a = f(4);",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement5b
  public void testFunctionMovement5b() {
    
    JSModule[] modules = createModuleStar(
      
      "var f = function(n){return (n<1)?1:f(n-1)};",
      
      "var a = f(4);"
    );

    test(modules, new String[] {
      
      "",
      
      "var f = function(n){return (n<1)?1:f(n-1)};" +
      "var a = f(4);",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement6
  public void testFunctionMovement6() {
    
    JSModule[] modules = createModuleChain(
      
      "function f(){return 1}",
      
      "var a = f();",
      
      "var b = f();"
    );

    test(modules, new String[] {
      
      "",
      
      "function f(){return 1}" +
      "var a = f();",
      
      "var b = f();",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement7
  public void testFunctionMovement7() {
    
    JSModule[] modules = createModules(
      
      "function f(){return 1}",
      
      "",
      
      "var a = f();",
      
      "var b = f();",
      
      "var c = f();"
    );

    modules[1].addDependency(modules[0]);
    modules[2].addDependency(modules[1]);
    modules[3].addDependency(modules[1]);
    modules[4].addDependency(modules[1]);

    test(modules, new String[] {
      
      "",
      
      "function f(){return 1}",
      
      "var a = f();",
      
      "var b = f();",
      
      "var c = f();",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement8
  public void testFunctionMovement8() {
    
    JSModule[] modules = createModuleChain(
      
      "var v = function f(){return 1}",
      
      "v();"
    );

    test(modules, new String[] {
      
      "",
      
      "var v = function f(){return 1};" +
      "v();",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionNonMovement1
  public void testFunctionNonMovement1() {
    
    
    
    
    
    testSame(createModuleStar(
      
      "function f(){};f.prototype.bar=new f;" +
      "if(a)function f2(){}" +
      "{{while(a)function f3(){}}}",
      
      "var a = new f();f2();f3();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionNonMovement2
  public void testFunctionNonMovement2() {
    
    
    testSame(createModuleStar(
      
      "function f(){return 1}",
      
      "var a = f();",
      
      "var b = f();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement1
  public void testClassMovement1() {
    test(createModuleStar(
             
             "function f(){} f.prototype.bar=function (){};",
             
             "var a = new f();"),
         new String[] {
           "",
           "function f(){} f.prototype.bar=function (){};" +
           "var a = new f();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement2
  public void testClassMovement2() {
    
    test(createModuleChain(
             
             "function f(){} f.prototype.bar=3; f.prototype.baz=5;",
             
             "f.prototype.baq = 7;",
             
             "f.prototype.baz = 9;",
             
             "var a = new f();"),
         new String[] {
           
           "",
           
           "",
           
           "function f(){} f.prototype.bar=3; f.prototype.baz=5;" +
           "f.prototype.baq = 7;" +
           "f.prototype.baz = 9;",
           
           "var a = new f();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement3
  public void testClassMovement3() {
    
    test(createModuleChain(
             
             "var f = function() {}; f.prototype.bar=3; f.prototype.baz=5;",
             
             "f = 7;",
             
             "f = 9;",
             
             "f = 11;"),
         new String[] {
           
           "",
           
           "",
           
           "var f = function() {}; f.prototype.bar=3; f.prototype.baz=5;" +
           "f = 7;" +
           "f = 9;",
           
           "f = 11;"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement4
  public void testClassMovement4() {
    testSame(createModuleStar(
                 
                 "function f(){} f.prototype.bar=3; f.prototype.baz=5;",
                 
                 "f.prototype.baq = 7;",
                 
                 "var a = new f();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement5
  public void testClassMovement5() {
    JSModule[] modules = createModules(
        
        "function f(){} f.prototype.bar=3; f.prototype.baz=5;",
        
        "",
        
        "f.prototype.baq = 7;",
        
        "var a = new f();");

    modules[1].addDependency(modules[0]);
    modules[2].addDependency(modules[1]);
    modules[3].addDependency(modules[1]);

    test(modules,
         new String[] {
           
           "",
           
           "function f(){} f.prototype.bar=3; f.prototype.baz=5;",
           
           "f.prototype.baq = 7;",
           
           "var a = new f();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement6
  public void testClassMovement6() {
    test(createModuleChain(
             
             "function Foo(){} function Bar(){} goog.inherits(Bar, Foo);" +
             "new Foo();",
             
             "new Bar();"),
         new String[] {
           
           "function Foo(){} new Foo();",
           
           "function Bar(){} goog.inherits(Bar, Foo); new Bar();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement7
  public void testClassMovement7() {
    testSame(createModuleChain(
                 
                 "function Foo(){} function Bar(){} goog.inherits(Bar, Foo);" +
                 "new Bar();",
                 
                 "new Foo();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testStubMethodMovement1
  public void testStubMethodMovement1() {
    test(createModuleChain(
             
             "function Foo(){} " +
             "Foo.prototype.bar = JSCompiler_stubMethod(x);",
             
             "new Foo();"),
        new String[] {
          
          "",
          "function Foo(){} " +
          "Foo.prototype.bar = JSCompiler_stubMethod(x);" +
          "new Foo();"
        });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testStubMethodMovement2
  public void testStubMethodMovement2() {
    test(createModuleChain(
             
             "function Foo(){} " +
             "Foo.prototype.bar = JSCompiler_unstubMethod(x);",
             
             "new Foo();"),
        new String[] {
          
          "",
          "function Foo(){} " +
          "Foo.prototype.bar = JSCompiler_unstubMethod(x);" +
          "new Foo();"
        });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testNoMoveSideEffectProperty
  public void testNoMoveSideEffectProperty() {
    testSame(createModuleChain(
                 
                 "function Foo(){} " +
                 "Foo.prototype.bar = createSomething();",
                 
                 "new Foo();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testAssignMovement
  public void testAssignMovement() {
    test(createModuleChain(
             
             "var f = 3;" +
             "f = 5;",
             
             "var h = f;"),
        new String[] {
          
          "",
          
          "var f = 3;" +
          "f = 5;" +
          "var h = f;"
        });

    
    testSame(createModuleChain(
                 
                 "var f = 3;" +
                 "var g = f = 5;",
                 
                 "var h = f;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testNoClassMovement2
  public void testNoClassMovement2() {
    test(createModuleChain(
             
             "var f = {};" +
             "f.h = 5;",
             
             "var h = f;"),
        new String[] {
          
          "",
          
          "var f = {};" +
          "f.h = 5;" +
          "var h = f;"
        });

    
    testSame(createModuleChain(
                 
                 "var f = {};" +
                 "var g = f.h = 5;",
                 
                 "var h = f;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testLiteralMovement1
  public void testLiteralMovement1() {
    test(createModuleChain(
             
             "var f = {'hi': 'mom', 'bye': function() {}};",
             
             "var h = f;"),
        new String[] {
          
          "",
          
          "var f = {'hi': 'mom', 'bye': function() {}};" +
          "var h = f;"
        });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testLiteralMovement2
  public void testLiteralMovement2() {
    testSame(createModuleChain(
                 
                 "var f = {'hi': 'mom', 'bye': goog.nullFunction};",
                 
                 "var h = f;"));
  }
