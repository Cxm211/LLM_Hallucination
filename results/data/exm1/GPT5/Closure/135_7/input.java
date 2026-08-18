// buggy code
  private void replaceReferencesToThis(Node node, String name) {
    if (NodeUtil.isFunction(node)) {
      return;
    }

    for (Node child : node.children()) {
      if (NodeUtil.isThis(child)) {
        Node newName = Node.newString(Token.NAME, name);
        node.replaceChild(child, newName);
      } else {
        replaceReferencesToThis(child, name);
      }
    }
  }

  public boolean hasProperty(String name) {
    return super.hasProperty(name) || "prototype".equals(name);
  }

  boolean defineProperty(String name, JSType type,
      boolean inferred, boolean inExterns) {
    if ("prototype".equals(name)) {
      ObjectType objType = type.toObjectType();
      if (objType != null) {
        return setPrototype(
            new FunctionPrototypeType(
                registry, this, objType, isNativeObjectType()));
      } else {
        return false;
      }
    }
    return super.defineProperty(name, type, inferred, inExterns);
  }

// relevant test
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

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder3
  public void testProvideOrder3() {
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

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder4
  public void testProvideOrder4() {
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

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine
  public void testBasicDefine() {
    test(" var DEF = true", "var DEF=true");
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

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation4
  public void testInvalidAnnotation4() throws Exception {
    test("var f = function() {};" +
         " f.x = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation5
  public void testInvalidAnnotation5() throws Exception {
    test("var f = function() {};" +
         "f.x =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.RecordFunctionInformationTest::testFunction
  public void testFunction() {
    String g = "function g(){}";
    String fAndG = "function f(){" + g + "}";
    String js = "var h=" + fAndG + ";h()";

    FunctionInformationMap.Builder expected =
        FunctionInformationMap.newBuilder();
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(0)
        .setSourceName("testcode")
        .setLineNumber(1)
        .setModuleName("")
        .setSize(g.length())
        .setName("f::g")
        .setCompiledSource(g).build());
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(1)
        .setSourceName("testcode")
        .setLineNumber(1)
        .setModuleName("")
        .setSize(fAndG.length())
        .setName("f")
        .setCompiledSource(fAndG).build());
    expected.addModule(
        FunctionInformationMap.Module.newBuilder()
        .setName("")
        .setCompiledSource(js + ";").build());

    test(js, expected.build());
  }

// com.google.javascript.jscomp.RecordFunctionInformationTest::testModule
  public void testModule() {
    String g = "function g(){}";
    String fAndG = "function f(){" + g + "}";
    String m0_js = "var h=" + fAndG + ";h()";
    String sum = "function(a,b){return a+b}";
    String m1_js = "var x=" + sum + "(1,2)";

    FunctionInformationMap.Builder expected =
        FunctionInformationMap.newBuilder();
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(0)
        .setSourceName("i0")
        .setLineNumber(1)
        .setModuleName("m0")
        .setSize(g.length())
        .setName("f::g")
        .setCompiledSource(g).build());
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(1)
        .setSourceName("i0")
        .setLineNumber(1)
        .setModuleName("m0")
        .setSize(fAndG.length())
        .setName("f")
        .setCompiledSource(fAndG).build());
    expected.addEntry(
        FunctionInformationMap.Entry.newBuilder()
        .setId(2)
        .setSourceName("i1")
        .setLineNumber(1)
        .setModuleName("m1")
        .setSize(sum.length())
        .setName("<anonymous>")
        .setCompiledSource(sum).build());
    expected.addModule(
        FunctionInformationMap.Module.newBuilder()
        .setName("m0")
        .setCompiledSource(m0_js + ";").build());
    expected.addModule(
        FunctionInformationMap.Module.newBuilder()
        .setName("m1")
        .setCompiledSource(m1_js + ";").build());

    test(CompilerTestCase.createModules(m0_js, m1_js), expected.build());
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsParallelTest::testOneFile
  public void testOneFile() {
    runInParallel(1);
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsParallelTest::testTwoFiles
  public void testTwoFiles() {
    runInParallel(2);
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsParallelTest::testFourFiles
  public void testFourFiles() {
    runInParallel(4);
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsParallelTest::testManyFiles
  public void testManyFiles() {
    runInParallel(100);
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveNumber
  public void testRemoveNumber() {
    test("3", "");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveVarGet1
  public void testRemoveVarGet1() {
    test("a", "");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveVarGet2
  public void testRemoveVarGet2() {
    test("var a = 1;a", "var a = 1");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveNamespaceGet1
  public void testRemoveNamespaceGet1() {
    test("var a = {};a.b", "var a = {}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveNamespaceGet2
  public void testRemoveNamespaceGet2() {
    test("var a = {};a.b=1;a.b", "var a = {};a.b=1");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemovePrototypeGet1
  public void testRemovePrototypeGet1() {
    test("var a = {};a.prototype.b", "var a = {}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemovePrototypeGet2
  public void testRemovePrototypeGet2() {
    test("var a = {};a.prototype.b = 1;a.prototype.b",
         "var a = {};a.prototype.b = 1");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveAdd1
  public void testRemoveAdd1() {
    test("1 + 2", "");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveVar1
  public void testNoRemoveVar1() {
    testSame("var a = 1");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveVar2
  public void testNoRemoveVar2() {
    testSame("var a = 1, b = 2");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveAssign1
  public void testNoRemoveAssign1() {
    testSame("a = 1");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveAssign2
  public void testNoRemoveAssign2() {
    testSame("a = b = 1");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveAssign3
  public void testNoRemoveAssign3() {
    test("1 + (a = 2)", "a = 2");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveAssign4
  public void testNoRemoveAssign4() {
    testSame("x.a = 1");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveAssign5
  public void testNoRemoveAssign5() {
    testSame("x.a = x.b = 1");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveAssign6
  public void testNoRemoveAssign6() {
    test("1 + (x.a = 2)", "x.a = 2");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveCall1
  public void testNoRemoveCall1() {
    testSame("a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveCall2
  public void testNoRemoveCall2() {
    test("a()+b()", "a();b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveCall3
  public void testNoRemoveCall3() {
    testSame("a() && b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveCall4
  public void testNoRemoveCall4() {
    testSame("a() || b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveCall5
  public void testNoRemoveCall5() {
    test("a() || 1", "a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveCall6
  public void testNoRemoveCall6() {
    testSame("1 || a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveThrow1
  public void testNoRemoveThrow1() {
    testSame("function f(){throw a()}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveThrow2
  public void testNoRemoveThrow2() {
    testSame("function f(){throw a}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveThrow3
  public void testNoRemoveThrow3() {
    testSame("function f(){throw 10}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveInControlStructure1
  public void testRemoveInControlStructure1() {
    test("if(2) 1", "if(2);");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveInControlStructure2
  public void testRemoveInControlStructure2() {
    test("while(2) 1", "while(2);");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveInControlStructure3
  public void testRemoveInControlStructure3() {
    test("for(1;2;3) 4", "for(1;2;3);");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testHook1
  public void testHook1() {
    test("1 ? 2 : 3", "");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testHook2
  public void testHook2() {
    test("1 ? a() : 3", "1 && a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testHook3
  public void testHook3() {
    test("1 ? 2 : a()", "1 || a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testHook4
  public void testHook4() {
    testSame("1 ? a() : b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testHook5
  public void testHook5() {
    test("a() ? 1 : 2", "a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testHook6
  public void testHook6() {
    test("a() ? b() : 2", "a() && b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testHook7
  public void testHook7() {
    test("a() ? 1 : b()", "a() || b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testHook8
  public void testHook8() {
    testSame("a() ? b() : c()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testShortCircuit1
  public void testShortCircuit1() {
    testSame("1 && a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testShortCircuit2
  public void testShortCircuit2() {
    test("1 && a() && 2", "1 && a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testShortCircuit3
  public void testShortCircuit3() {
    test("a() && 1 && 2", "a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComma1
  public void testComma1() {
    test("1, 2", "");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComma2
  public void testComma2() {
    test("1, a()", "a()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComma3
  public void testComma3() {
    test("1, a(), b()", "a();b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComma4
  public void testComma4() {
    test("a(), b()", "a();b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComma5
  public void testComma5() {
    test("a(), b(), 1", "a();b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComplex1
  public void testComplex1() {
    test("1 && a() + b() + c()", "1 && (a(), b(), c())");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComplex2
  public void testComplex2() {
    test("1 && (a() ? b() : 1)", "1 && a() && b()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComplex3
  public void testComplex3() {
    test("1 && (a() ? b() : 1 + c())", "1 && (a() ? b() : c())");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComplex4
  public void testComplex4() {
    test("1 && (a() ? 1 : 1 + c())", "1 && (a() || c())");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testComplex5
  public void testComplex5() {
    
    testSame("(a() ? 1 : 1 + c()) && foo()");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveFunctionDeclaration1
  public void testNoRemoveFunctionDeclaration1() {
    testSame("function foo(){}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveFunctionDeclaration2
  public void testNoRemoveFunctionDeclaration2() {
    testSame("var foo = function (){}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoSimplifyFunctionArgs1
  public void testNoSimplifyFunctionArgs1() {
    testSame("f(1 + 2, 3 + g())");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoSimplifyFunctionArgs2
  public void testNoSimplifyFunctionArgs2() {
    testSame("1 && f(1 + 2, 3 + g())");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoSimplifyFunctionArgs3
  public void testNoSimplifyFunctionArgs3() {
    testSame("1 && foo(a() ? b() : 1 + c())");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveInherits1
  public void testNoRemoveInherits1() {
    testSame("var a = {}; this.b = {}; var goog = {}; goog.inherits(b, a)");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveInherits2
  public void testNoRemoveInherits2() {
    test("var a = {}; this.b = {}; var goog = {}; goog.inherits(b, a) + 1",
         "var a = {}; this.b = {}; var goog = {}; goog.inherits(b, a)");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveInherits3
  public void testNoRemoveInherits3() {
    testSame("this.a = {}; var b = {}; b.inherits(a);");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testNoRemoveInherits4
  public void testNoRemoveInherits4() {
    test("this.a = {}; var b = {}; b.inherits(a) + 1;",
         "this.a = {}; var b = {}; b.inherits(a)");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveFromLabel1
  public void testRemoveFromLabel1() {
    test("LBL: void 0", "LBL: {}");
  }

// com.google.javascript.jscomp.RemoveConstantExpressionsTest::testRemoveFromLabel2
  public void testRemoveFromLabel2() {
    test("LBL: foo() + 1 + bar()", "LBL: {foo();bar()}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testRemoveTryCatch
  public void testRemoveTryCatch() {
    test("try{var a=1;}catch(ex){var b=2;}",
         "var b;var a=1");
    test("try{var a=1;var b=2}catch(ex){var c=3;var d=4;}",
         "var d;var c;{var a=1;var b=2}");
    test("try{var a=1;var b=2}catch(ex){}",
         "{var a=1;var b=2}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testRemoveTryFinally
  public void testRemoveTryFinally() {
    test("try{var a=1;}finally{var c=3;}",
         "var a=1;var c=3");
    test("try{var a=1;var b=2}finally{var e=5;var f=6;}",
         "{var a=1;var b=2}{var e=5;var f=6}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testRemoveTryCatchFinally
  public void testRemoveTryCatchFinally() {
    test("try{var a=1;}catch(ex){var b=2;}finally{var c=3;}",
         "var b;var a=1;var c=3");
    test("try{var a=1;var b=2}catch(ex){var c=3;var d=4;}finally{var e=5;" +
         "var f=6;}",
         "var d;var c;{var a=1;var b=2}{var e=5;var f=6}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testPreserveTryBlockContainingReturnStatement
  public void testPreserveTryBlockContainingReturnStatement() {
    testSame("function(){var a;try{a=1;return}finally{a=2}}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testPreserveAnnotatedTryBlock
  public void testPreserveAnnotatedTryBlock() {
    test("try{var a=1;}catch(ex){var b=2;}",
         "try{var a=1}catch(ex){var b=2}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testIfTryFinally
  public void testIfTryFinally() {
    test("if(x)try{y}finally{z}", "if(x){y;z}");
  }

// com.google.javascript.jscomp.RemoveTryCatchTest::testIfTryCatch
  public void testIfTryCatch() {
    test("if(x)try{y;z}catch(e){}", "if(x){y;z}");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties
  public void testAnalyzeUnusedPrototypeProperties() {
    
    test(" \n" +
        "function e(){} \n" +
        "e.prototype.a = function(){};" +
        "e.prototype.b = function(){};" +
        "var x = new e; x.a()",

        "function e(){}" +
        " e.prototype.a = function(){};" +
        "var x = new e; x.a()");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties2
  public void testAnalyzeUnusedPrototypeProperties2() {
    
    
    
    
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties3
  public void testAnalyzeUnusedPrototypeProperties3() {
    
    
    test(" \n" +
        "function e(){} \n" +
           "e.prototype.a = function(){};" +
           "e.prototype.bExtern = function(){};" +
           "var x = new e;x.a()",
         "function e(){}" +
           "e.prototype.a = function(){};" +
           
           "var x = new e; x.a()");

    
    
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAliasing
  public void testAliasing() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testStatement
  public void testStatement() {
    test(" \n" +
        "function e(){}" +
           "var x = e.prototype.method1 = function(){};" +
           "var y = new e; x()",
         "function e(){}" +
           "var x = function(){};" +
           "var y = new e; x()");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testExportedMethodsByNamingConvention
  public void testExportedMethodsByNamingConvention() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testExportedMethodsByNamingConventionAlwaysExported
  public void testExportedMethodsByNamingConventionAlwaysExported() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAnalyzePrototypeProperties
  public void testAnalyzePrototypeProperties() {
    
    test("function e(){}" +
           "e.prototype.a = function(){};" +
           "e.prototype.b = function(){};" +
           "var x = new e; x.a()",
         "function e(){}" +
           "e.prototype.a = function(){};" +
           "var x = new e; x.a()");

    
    test("function e(){}" +
           "e.prototype = {a: function(){}, b: function(){}};" +
           "var x=new e; x.a()",
         "function e(){}" +
           "e.prototype = {a: function(){}};" +
           "var x = new e; x.a()");

    
    
    test("function e(){}" +
           "e.prototype.a = function(){};" +
           "e.prototype.bExtern = function(){};" +
           "var x = new e;x.a()",
         "function e(){}" +
           "e.prototype.a = function(){};" +
           "e.prototype.bExtern = function(){};" +
           "var x = new e; x.a()");
    test("function e(){}" +
           "e.prototype = {a: function(){}, bExtern: function(){}};" +
           "var x = new e; x.a()",
         "function e(){}" +
           "e.prototype = {a: function(){}, bExtern: function(){}};" +
           "var x = new e; x.a()");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing
  public void testAliasing() {
    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.method2 = function(){};" +
           
           "e.prototype.alias1 = e.prototype.method1;" +
           "e.prototype.alias2 = e.prototype.method2;" +
           "var x = new e; x.method1()",
         "function e(){}" +
           "e.prototype.method1 = function(){};" +
           "var x = new e; x.method1()");

    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.method2 = function(){};" +
           
           "e.prototype.alias1 = e.prototype.method1;" +
           "e.prototype.alias2 = e.prototype.method2;" +
           "var x=new e;x.alias1()",
         "function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.alias1 = e.prototype.method1;" +
           "var x = new e; x.alias1()");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testStatementRestriction
  public void testStatementRestriction() {
    test("function e(){}" +
           "var x = e.prototype.method1 = function(){};" +
           "var y = new e; x()",
         "function e(){}" +
           "var x = e.prototype.method1 = function(){};" +
           "var y = new e; x()");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testExportedMethodsByNamingConvention
  public void testExportedMethodsByNamingConvention() {
    String classAndItsMethodAliasedAsExtern =
        "function Foo() {}" +
        "Foo.prototype.method = function() {};" +  
        "Foo.prototype.unused = function() {};" +  
        "var _externInstance = new Foo();" +
        "Foo.prototype._externMethod = Foo.prototype.method";  

    String compiled =
        "function Foo(){}" +
        "Foo.prototype.method = function(){};" +
        "var _externInstance = new Foo;" +
        "Foo.prototype._externMethod = Foo.prototype.method";

    test(classAndItsMethodAliasedAsExtern, compiled);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testMethodsFromExternsFileNotExported
  public void testMethodsFromExternsFileNotExported() {
    canRemoveExterns = true;
    String classAndItsMethodAliasedAsExtern =
        "function Foo() {}" +
        "Foo.prototype.bar_ = function() {};" +
        "Foo.prototype.unused = function() {};" +
        "var instance = new Foo;" +
        "Foo.prototype.bar = Foo.prototype.bar_";

    String compiled =
        "function Foo(){}" +
        "var instance = new Foo;";

    test(classAndItsMethodAliasedAsExtern, compiled);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testExportedMethodsByNamingConventionAlwaysExported
  public void testExportedMethodsByNamingConventionAlwaysExported() {
    canRemoveExterns = true;
    String classAndItsMethodAliasedAsExtern =
        "function Foo() {}" +
        "Foo.prototype.method = function() {};" +  
        "Foo.prototype.unused = function() {};" +  
        "var _externInstance = new Foo();" +
        "Foo.prototype._externMethod = Foo.prototype.method";  

    String compiled =
        "function Foo(){}" +
        "Foo.prototype.method = function(){};" +
        "var _externInstance = new Foo;" +
        "Foo.prototype._externMethod = Foo.prototype.method";

    test(classAndItsMethodAliasedAsExtern, compiled);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testExternMethodsFromExternsFile
  public void testExternMethodsFromExternsFile() {
    String classAndItsMethodAliasedAsExtern =
        "function Foo() {}" +
        "Foo.prototype.bar_ = function() {};" +  
        "Foo.prototype.unused = function() {};" +  
        "var instance = new Foo;" +
        "Foo.prototype.bar = Foo.prototype.bar_";  

    String compiled =
        "function Foo(){}" +
        "Foo.prototype.bar_ = function(){};" +
        "var instance = new Foo;" +
        "Foo.prototype.bar = Foo.prototype.bar_";

    test(classAndItsMethodAliasedAsExtern, compiled);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testPropertyReferenceGraph
  public void testPropertyReferenceGraph() {
    
    
    String constructor = "function Foo() {}";
    String defA =
        "Foo.prototype.a = function() { Foo.superClass_.a.call(this); };";
    String defB = "Foo.prototype.b = function() { this.a(); };";
    String defC = "Foo.prototype.c = function() { " +
        "Foo.superClass_.c.call(this); this.b(); this.a(); };";
    String defD = "Foo.prototype.d = function() { this.c(); };";
    String defE = "Foo.prototype.e = function() { this.a(); this.f(); };";
    String defF = "Foo.prototype.f = function() { };";
    String fullClassDef = constructor + defA + defB + defC + defD + defE + defF;

    
    test(fullClassDef, "");

    
    String callA = "(new Foo()).a();";
    String callB = "(new Foo()).b();";
    String callC = "(new Foo()).c();";
    String callD = "(new Foo()).d();";
    String callE = "(new Foo()).e();";
    String callF = "(new Foo()).f();";
    test(fullClassDef + callA, constructor + defA + callA);
    test(fullClassDef + callB, constructor + defA + defB + callB);
    test(fullClassDef + callC, constructor + defA + defB + defC + callC);
    test(fullClassDef + callD, constructor + defA + defB + defC + defD + callD);
    test(fullClassDef + callE, constructor + defA + defE + defF + callE);
    test(fullClassDef + callF, constructor + defF + callF);

    test(fullClassDef + callA + callC,
         constructor + defA + defB + defC + callA + callC);
    test(fullClassDef + callB + callC,
         constructor + defA + defB + defC + callB + callC);
    test(fullClassDef + callA + callB + callC,
         constructor + defA + defB + defC + callA + callB + callC);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testPropertiesDefinedWithGetElem
  public void testPropertiesDefinedWithGetElem() {
    testSame("function Foo() {} Foo.prototype['elem'] = function() {};");
    testSame("function Foo() {} Foo.prototype[1 + 1] = function() {};");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testNeverRemoveImplicitlyUsedProperties
  public void testNeverRemoveImplicitlyUsedProperties() {
    testSame("function Foo() {} " +
             "Foo.prototype.length = 3; " +
             "Foo.prototype.toString = function() { return 'Foo'; }; " +
             "Foo.prototype.valueOf = function() { return 'Foo'; }; ");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testPropertyDefinedInBranch
  public void testPropertyDefinedInBranch() {
    test("function Foo() {} if (true) Foo.prototype.baz = function() {};",
         "if (true);");
    test("function Foo() {} while (true) Foo.prototype.baz = function() {};",
         "while (true);");
    test("function Foo() {} for (;;) Foo.prototype.baz = function() {};",
         "for (;;);");
    test("function Foo() {} do Foo.prototype.baz = function() {}; while(true);",
         "do; while(true);");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testUsingAnonymousObjectsToDefeatRemoval
  public void testUsingAnonymousObjectsToDefeatRemoval() {
    String constructor = "function Foo() {}";
    String declaration = constructor + "Foo.prototype.baz = 3;";
    test(declaration, "");
    testSame(declaration + "var x = {}; x.baz = 5;");
    testSame(declaration + "var x = {baz: 5};");
    test(declaration + "var x = {'baz': 5};",
         "var x = {'baz': 5};");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph
  public void testGlobalFunctionsInGraph() {
    test(
        "var x = function() { (new Foo).baz(); };" +
        "var y = function() { x(); };" +
        "function Foo() {}" +
        "Foo.prototype.baz = function() { y(); };",
        "");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph2
  public void testGlobalFunctionsInGraph2() {
    
    
    
    
    
    
    testSame(
        "var x = function() { (new Foo).baz(); };" +
        "var y = function() { x(); };" +
        "function Foo() { this.baz(); }" +
        "Foo.prototype.baz = function() { y(); };");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph3
  public void testGlobalFunctionsInGraph3() {
    test(
        "var x = function() { (new Foo).baz(); };" +
        "var y = function() { x(); };" +
        "function Foo() { this.baz(); }" +
        "Foo.prototype.baz = function() { x(); };",
        "var x = function() { (new Foo).baz(); };" +
        "function Foo() { this.baz(); }" +
        "Foo.prototype.baz = function() { x(); };");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph4
  public void testGlobalFunctionsInGraph4() {
    test(
        "var x = function() { (new Foo).baz(); };" +
        "var y = function() { x(); };" +
        "function Foo() { Foo.prototype.baz = function() { y(); }; }",
        "");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph5
  public void testGlobalFunctionsInGraph5() {
    test(
        "function Foo() {}" +
        "Foo.prototype.methodA = function() {};" +
        "function x() { (new Foo).methodA(); }" +
        "Foo.prototype.methodB = function() { x(); };",
        "");

    anchorUnusedVars = true;
    test(
        "function Foo() {}" +
        "Foo.prototype.methodA = function() {};" +
        "function x() { (new Foo).methodA(); }" +
        "Foo.prototype.methodB = function() { x(); };",

        "function Foo() {}" +
        "Foo.prototype.methodA = function() {};" +
        "function x() { (new Foo).methodA(); }");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph6
  public void testGlobalFunctionsInGraph6() {
    testSame(
        "function Foo() {}" +
        "Foo.prototype.methodA = function() {};" +
        "function x() { (new Foo).methodA(); }" +
        "Foo.prototype.methodB = function() { x(); };" +
        "(new Foo).methodB();");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph7
  public void testGlobalFunctionsInGraph7() {
    testSame(
        "function Foo() {}" +
        "Foo.prototype.methodA = function() {};" +
        "this.methodA();");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveUnusedVars
  public void testRemoveUnusedVars() {
    
    test("var a;var b=3;var c=function(){};var x=A();var y; var z;" +
         "function A(){B()}; function B(){C(b)}; function C(){};" +
         "function X(){Y()}; function Y(z){Z(x)}; function Z(){y};" +
         "P=function(){A()};" +
         "try{0}catch(e){a}",

         "var a;var b=3;A();function A(){B()}" +
         "function B(){C(b)}" +
         "function C(){}" +
         "P=function(){A()}" +
         ";try{0}catch(e){a}");

    
    test("var i=0;var j=0;if(i>0){var k=1;}",
         "var i=0;if(i>0);");

    
    test("for (var i in booyah) {" +
         "  if (i > 0) x += ', ';" +
         "  var arg = 'foo';" +
         "  if (arg.length > 40) {" +
         "    var unused = 'bar';" +   
         "    arg = arg.substr(0, 40) + '...';" +
         "  }" +
         "  x += arg;" +
         "}",

         "for(var i in booyah){if(i>0)x+=\", \";" +
         "var arg=\"foo\";if(arg.length>40)arg=arg.substr(0,40)+\"...\";" +
         "x+=arg}");

    
    test("function A(){}" +
         "if(0){function B(){}}win.setTimeout(function(){A()})",
         "function A(){}" +
         "if(0);win.setTimeout(function(){A()})");

    
    test("function A(){A()}function B(){B()}B()",
         "function B(){B()}B()");

    
    test("var x,y=2,z=3;A(x);B(z);var a,b,c=4;C()",
         "var x,z=3;A(x);B(z);C()");

    
    test("for(var i=0,j=0;i<10;){}" +
         "for(var x=0,y=0;;y++){}" +
         "for(var a,b;;){a}" +
         "for(var c,d;;);" +
         "for(var item in items){}",

         "for(var i=0;i<10;);" +
         "for(var y=0;;y++);" +
         "for(var a;;)a;" +
         "for(;;);" +
         "for(var item in items);");

    
    test("var a,b,c,d;var e=[b,c];var x=e[3];var f=[d];print(f[0])",
         "var d;var f=[d];print(f[0])");

    
    test("var x;function A(){var x;B()}function B(){print(x)}A()",
         "var x;function A(){B()}function B(){print(x)}A()");

    
    test("function A(){var x;return function(){print(x)}}A()",
         "function A(){var x;return function(){print(x)}}A()");

    
    test("function A(){}function B(){" +
         "var c,d,e,f,g,h;" +
         "function C(){print(c)}" +
         "var handler=function(){print(d)};" +
         "var handler2=function(){handler()};" +
         "e=function(){print(e)};" +
         "if(1){function G(){print(g)}}" +
         "arr=[function(){print(h)}];" +
         "return function(){print(f)}}B()",

         "function B(){" +
         "var e,f,h;" +
         "e=function(){print(e)};" +
         "if(1);" +
         "arr=[function(){print(h)}];" +
         "return function(){print(f)}}B()");

    
    test("var a,b=1; function _A1() {a=1}",
         "var a;function _A1(){a=1}");

    
    test("undefinedVar = 1", "undefinedVar=1");

    
    test("var a,b=foo(),c=i++,d;var e=boo();var f;print(d);",
         "var b=foo(),c=i++,d;boo();print(d)");

    test("var a,b=foo()", "foo()");
    test("var b=foo(),a", "foo()");
    test("var a,b=foo(a)", "var a,b=foo(a)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionArgRemoval
  public void testFunctionArgRemoval() {
    
    test("var b=function(c,d){return};b(1,2)",
         "var b=function(){return};b(1,2)");

    
    testSame("var b=function(c,d){return c+d};b(1,2)");
    testSame("var b=function(e,f,c,d){return c+d};b(1,2)");

    
    test("var b=function(c,d,e,f){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b(1,2)");
    test("var b=function(e,c,f,d,g){return c+d};b(1,2)",
         "var b=function(e,c,f,d){return c+d};b(1,2)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testVarInControlStructure
  public void testVarInControlStructure() {
    test("if (true) var b = 3;", "if(true);");
    test("if (true) var b = 3; else var c = 5;", "if(true);else;");
    test("while (true) var b = 3;", "while(true);");
    test("for (;;) var b = 3;", "for(;;);");
    test("do var b = 3; while(true)", "do;while(true)");
    test("with (true) var b = 3;", "with(true);");
    test("f: var b = 3;","");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRValueHoisting
  public void testRValueHoisting() {
    test("var x = foo();", "foo()");
    test("var x = {a: foo()};", "({a:foo()})");

    test("var x=function y(){}", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testModule
  public void testModule() {
    test(createModules(
             "var unreferenced=1; function x() { foo(); }" +
             "function uncalled() { var x; return 2; }",
             "var a,b; function foo() { a=1; } x()"),
         new String[] {
           "function x(){foo()}",
           "var a;function foo(){a=1}x()"
         });
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRecursiveFunction1
  public void testRecursiveFunction1() {
    testSame("(function x(){return x()})()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRecursiveFunction2
  public void testRecursiveFunction2() {
    test("var x = 3; (function x() { return x(); })();",
         "(function x(){return x()})()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionWithName1
  public void testFunctionWithName1() {
    test("var x=function f(){};x()",
         "var x=function(){};x()");

    preserveAnonymousFunctionNames = true;
    testSame("var x=function f(){};x()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionWithName2
  public void testFunctionWithName2() {
    test("foo(function bar(){})",
         "foo(function(){})");

    preserveAnonymousFunctionNames = true;
    testSame("foo(function bar(){})");    
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveGlobal
  public void testRemoveGlobal() {
    removeGlobal = false;
    testSame("var x=1");
    test("var y=function(x){var z;}", "var y=function(){}");
  }

// com.google.javascript.jscomp.RenameLabelsTest::testRenameInFunction
  public void testRenameInFunction() {
    test("function x(){ Foo:a(); }",
         "function x(){ a(); }");
    test("function x(){ Foo:{ a(); break Foo; } }",
         "function x(){ a:{ a(); break a; } }");
    test("function x() { " +
            "Foo:{ " +
              "function goo() {" +
                "Foo: {" +
                  "a(); " +
                  "break Foo; " +
                "}" +
              "}" +
            "}" +
          "}",
          "function x(){function goo(){a:{ a(); break a; }}}");
    test("function x() { " +
          "Foo:{ " +
            "function goo() {" +
              "Foo: {" +
                "a(); " +
                "break Foo; " +
              "}" +
            "}" +
            "break Foo;" +
          "}" +
        "}",
        "function x(){a:{function goo(){a:{ a(); break a; }} break a;}}");
  }

// com.google.javascript.jscomp.RenameLabelsTest::testRenameGlobals
  public void testRenameGlobals() {
    test("Foo:{a();}",
         "a();");
    test("Foo:{a(); break Foo;}",
         "a:{a(); break a;}");
    test("Foo:{Goo:a(); break Foo;}",
         "a:{a(); break a;}");
    test("Foo:{Goo:while(1){a(); continue Goo; break Foo;}}",
         "a:{b:while(1){a(); continue b;break a;}}");
    test("Foo:Goo:while(1){a(); continue Goo; break Foo;}",
         "a:b:while(1){a(); continue b;break a;}");

    test("Foo:Bar:X:{ break Bar; }",
         "a:break a;");
    test("Foo:Bar:X:{ break Bar; break X; }",
         "a:b:{ break a; break b;}");
    test("Foo:Bar:X:{ break Bar; break Foo; }",
         "a:b:{ break b; break a;}");

    test("Foo:while (1){a(); break;}",
         "while (1){a(); break;}");

    
    test("Foo:{a(); while (1) break;}",
         "a(); while (1) break;");
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameSimple
  public void testRenameSimple() {
    test("function Foo(v1, v2) {return v1;} Foo();",
         "function Foo(a, b) {return a;} Foo();");
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameGlobals
  public void testRenameGlobals() {
    testSame("var Foo; var Bar, y; function x() { Bar++; }");
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameLocals
  public void testRenameLocals() {
    test("(function (v1, v2) {}); (function (v3, v4) {});",
         "(function (a, b) {}); (function (a, b) {});");
    test("function f1(v1, v2) {}; function f2(v3, v4) {};",
         "function f1(a, b) {}; function f2(a, b) {};");
    
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameLocalsClashingWithGlobals
  public void testRenameLocalsClashingWithGlobals() {
    test("function a(v1, v2) {return v1;} a();",
         "function a(b, c) {return b;} a();");
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameNested
  public void testRenameNested() {
    test("function f1(v1, v2) { (function(v3, v4) {}) }",
         "function f1(a, b) { (function(c, d) {}) }");
    test("function f1(v1, v2) { function f2(v3, v4) {} }",
         "function f1(a, b) { function c(d, e) {} }");
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameWithExterns1
  public void testRenameWithExterns1() {
    String externs = "var bar; function alert() {}";
    test(externs,
        "function foo(bar) { alert(bar); } foo(3)",
        "function foo(a) { alert(a); } foo(3)", null, null);
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameWithExterns2
  public void testRenameWithExterns2() {
    test("var a; function alert() {}",
        "function foo(bar) { alert(a);alert(bar); } foo(3);",
        "function foo(b) { alert(a);alert(b); } foo(3);",
        null, null);
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testDoNotRenameExportedName
  public void testDoNotRenameExportedName() {
    test("_foo()", "_foo()");
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameWithNameOverlap
  public void testRenameWithNameOverlap() {
    test("function local() { var a = 1; var b = 2; b + b; }",
        "function local() { var b = 1; var a = 2; a + a; }");
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameWithPrefix1
  public void testRenameWithPrefix1() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {return v1} Foo();",
         "function Foo(a, b) {return a} Foo();");
    prefix = DEFAULT_PREFIX;
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameWithPrefix2
  public void testRenameWithPrefix2() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {var v3 = v1 + v2; return v3;} Foo();",
         "function Foo(a, b) {var c = a + b; return c;} Foo();");
    prefix = DEFAULT_PREFIX;
  }

// com.google.javascript.jscomp.RenameLocalVarsTest::testRenameWithPrefix3
  public void testRenameWithPrefix3() {
    prefix = "a";
    test("function Foo() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "      A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,aa,ab;" +
         "  Foo();" +
         "} Bar();",

         "function Foo() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A,B,C," +
         "      D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$,aa;"  +
         "  Foo();" +
         "} Bar();");
    prefix = DEFAULT_PREFIX;
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypeProperties
  public void testPrototypeProperties() {
    test("Bar.prototype.getA = function(){}; bar.getA();" +
         "Bar.prototype.getB = function(){};",
         "Bar.prototype.a = function(){}; bar.a();" +
         "Bar.prototype.b = function(){}");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypePropertiesAsObjLitKeys
  public void testPrototypePropertiesAsObjLitKeys() {
    test("Bar.prototype = {2: function(){}, getA: function(){}}; bar[2]();",
         "Bar.prototype = {2: function(){}, a: function(){}}; bar[2]();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testMixedQuotedAndUnquotedObjLitKeys
  public void testMixedQuotedAndUnquotedObjLitKeys() {
    test("Bar = {getA: function(){}, 'getB': function(){}}; bar.getA();",
         "Bar = {a: function(){}, 'getB': function(){}}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testQuotedPrototypeProperty
  public void testQuotedPrototypeProperty() {
    testSame("Bar.prototype['getA'] = function(){}; bar['getA']();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testOverlappingOriginalAndGeneratedNames
  public void testOverlappingOriginalAndGeneratedNames() {
    test("Bar.prototype = {b: function(){}, a: function(){}}; bar.b();",
         "Bar.prototype = {a: function(){}, b: function(){}}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesWithLeadingUnderscores
  public void testRenamePropertiesWithLeadingUnderscores() {
    test("Bar.prototype = {_getA: function(){}, _b: 0}; bar._getA();",
         "Bar.prototype = {a: function(){}, b: 0}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyAddedToObject
  public void testPropertyAddedToObject() {
    test("var foo = {}; foo.prop = '';",
         "var foo = {}; foo.a = '';");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyAddedToFunction
  public void testPropertyAddedToFunction() {
    test("var foo = function(){}; foo.prop = '';",
         "var foo = function(){}; foo.a = '';");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyOfObjectOfUnknownType
  public void testPropertyOfObjectOfUnknownType() {
    test("var foo = x(); foo.prop = '';",
         "var foo = x(); foo.a = '';");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testSetPropertyOfThis
  public void testSetPropertyOfThis() {
    test("this.prop = 'bar'",
         "this.a = 'bar'");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testReadPropertyOfThis
  public void testReadPropertyOfThis() {
    test("f(this.prop);",
         "f(this.a);");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testObjectLiteralInLocalScope
  public void testObjectLiteralInLocalScope() {
    test("function x() { var foo = {prop1: 'bar', prop2: 'baz'}; }",
         "function x() { var foo = {a: 'bar', b: 'baz'}; }");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testIncorrectAttemptToAccessQuotedProperty
  public void testIncorrectAttemptToAccessQuotedProperty() {
    
    test("Bar.prototype = {'B': 0, 'getFoo': function(){}}; bar.getFoo();",
         "Bar.prototype = {B: 0, getFoo: function(){}}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testSetQuotedPropertyOfThis
  public void testSetQuotedPropertyOfThis() {
    testSame("this['prop'] = 'bar';");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testExternedPropertyName
  public void testExternedPropertyName() {
    test("Bar.prototype = {toString: function(){}, foo: 0}; bar.toString();",
         "Bar.prototype = {toString: function(){}, a: 0}; bar.toString();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testExternedPropertyNameDefinedByObjectLiteral
  public void testExternedPropertyNameDefinedByObjectLiteral() {
    test("function x() { var foo = google.gears.factory; }",
         "function x() { var foo = google.gears.factory; }");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testAvoidingConflictsBetweenQuotedAndUnquotedPropertyNames
  public void testAvoidingConflictsBetweenQuotedAndUnquotedPropertyNames() {
    test("Bar.prototype.foo = function(){}; Bar.prototype['a'] = 0; bar.foo();",
         "Bar.prototype.b = function(){}; Bar.prototype['a'] = 0; bar.b();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testSamePropertyNameQuotedAndUnquoted
  public void testSamePropertyNameQuotedAndUnquoted() {
    test("Bar.prototype.prop = function(){}; y = {'prop': 0};",
         "Bar.prototype.a = function(){}; y = {'prop': 0};");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testStaticAndInstanceMethodWithSameName
  public void testStaticAndInstanceMethodWithSameName() {
    test("Bar = function(){}; Bar.getA = function(){}; " +
         "Bar.prototype.getA = function(){}; Bar.getA(); bar.getA();",
         "Bar = function(){}; Bar.a = function(){}; " +
         "Bar.prototype.a = function(){}; Bar.a(); bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesFunctionCall1
  public void testRenamePropertiesFunctionCall1() {
    test("var foo = {myProp: 0}; f(foo[JSCompiler_renameProperty('myProp')]);",
         "var foo = {a: 0}; f(foo['a']);");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesFunctionCall2
  public void testRenamePropertiesFunctionCall2() {
    test("var foo = {myProp: 0}; " +
         "f(JSCompiler_renameProperty('otherProp.myProp.someProp')); " +
         "foo.myProp = 1; foo.theirProp = 2; foo.yourProp = 3;",
         "var foo = {a: 0}; f('b.a.c'); " +
         "foo.a = 1; foo.d = 2; foo.e = 3;");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRemoveRenameFunctionStubs1
  public void testRemoveRenameFunctionStubs1() {
    test("function JSCompiler_renameProperty(x) { return x; }",
         "");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRemoveRenameFunctionStubs2
  public void testRemoveRenameFunctionStubs2() {
    test("function() { function JSCompiler_renameProperty(x) {} }" +
         "var JSCompiler_renameProperty = function(x) { return x; }; " +
         "var foo = {myProp: 0}; f(foo[JSCompiler_renameProperty('myProp')]);",
         "function() {} var foo = {a: 0}; f(foo['a']);");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testGeneratePseudoNames
  public void testGeneratePseudoNames() {
    generatePseudoNames = true;
    test("var foo={}; foo.bar=1; foo['abc']=2",
         "var foo={}; foo.$bar$=1; foo['abc']=2");
    generatePseudoNames = false;
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testModules
  public void testModules() {
    String module1Js = "function Bar(){} Bar.prototype.getA=function(x){};" +
                       "var foo;foo.getA(foo);foo.doo=foo;foo.bloo=foo;";

    String module2Js = "function Far(){} Far.prototype.getB=function(x){};" +
                       "var too;too.getB(too);too.woo=too;too.bloo=too;";

    String module3Js = "function Car(){} Car.prototype.getC=function(x){};" +
                       "var noo;noo.getC(noo);noo.zoo=noo;noo.cloo=noo;";

    JSModule module1 = new JSModule("m1");
    module1.add(JSSourceFile.fromCode("input1", module1Js));

    JSModule module2 = new JSModule("m2");
    module2.add(JSSourceFile.fromCode("input2", module2Js));

    JSModule module3 = new JSModule("m3");
    module3.add(JSSourceFile.fromCode("input3", module3Js));

    JSModule[] modules = new JSModule[] { module1, module2, module3 };
    Compiler compiler = compileModules("", modules);

    Result result = compiler.getResult();
    assertTrue(result.success);

    assertEquals("function Bar(){}Bar.prototype.b=function(x){};" +
                 "var foo;foo.b(foo);foo.f=foo;foo.a=foo;",
                 compiler.toSource(module1));

    assertEquals("function Far(){}Far.prototype.c=function(x){};" +
                 "var too;too.c(too);too.g=too;too.a=too;",
                 compiler.toSource(module2));

    
    
    
    
    
    
    assertEquals("function Car(){}Car.prototype.d=function(x){};" +
                 "var noo;noo.d(noo);noo.h=noo;noo.e=noo;",
                 compiler.toSource(module3));
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypePropertiesStable
  public void testPrototypePropertiesStable() {
    testStableRenaming(
        "Bar.prototype.getA = function(){}; bar.getA();" +
        "Bar.prototype.getB = function(){};",
        "Bar.prototype.a = function(){}; bar.a();" +
        "Bar.prototype.b = function(){}",
        "Bar.prototype.get = function(){}; bar.get();" +
        "Bar.prototype.getA = function(){}; bar.getA();" +
        "Bar.prototype.getB = function(){};",
        "Bar.prototype.c = function(){}; bar.c();" +
        "Bar.prototype.a = function(){}; bar.a();" +
        "Bar.prototype.b = function(){}");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypePropertiesAsObjLitKeysStable
  public void testPrototypePropertiesAsObjLitKeysStable() {
    testStableRenaming(
        "Bar.prototype = {2: function(){}, getA: function(){}}; bar[2]();",
        "Bar.prototype = {2: function(){}, a: function(){}}; bar[2]();",
        "Bar.prototype = {getB: function(){},getA: function(){}}; bar.getB();",
        "Bar.prototype = {b: function(){},a: function(){}}; bar.b();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testMixedQuotedAndUnquotedObjLitKeysStable
  public void testMixedQuotedAndUnquotedObjLitKeysStable() {
    testStableRenaming(
        "Bar = {getA: function(){}, 'getB': function(){}}; bar.getA();",
        "Bar = {a: function(){}, 'getB': function(){}}; bar.a();",
        "Bar = {get: function(){}, getA: function(){}, 'getB': function(){}};" +
        "bar.getA();bar.get();",
        "Bar = {b: function(){}, a: function(){}, 'getB': function(){}};" +
        "bar.a();bar.b();");
  }
