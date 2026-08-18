// buggy code
  private void processRequireCall(NodeTraversal t, Node n, Node parent) {
    Node left = n.getFirstChild();
    Node arg = left.getNext();
    if (verifyLastArgumentIsString(t, left, arg)) {
      String ns = arg.getString();
      ProvidedName provided = providedNames.get(ns);
      if (provided == null || !provided.isExplicitlyProvided()) {
        unrecognizedRequires.add(
            new UnrecognizedRequire(n, ns, t.getSourceName()));
      } else {
        JSModule providedModule = provided.explicitModule;

        // This must be non-null, because there was an explicit provide.
        Preconditions.checkNotNull(providedModule);

        JSModule module = t.getModule();
        if (moduleGraph != null &&
            module != providedModule &&
            !moduleGraph.dependsOn(module, providedModule)) {
          compiler.report(
              t.makeError(n, XMODULE_REQUIRE_ERROR, ns,
                  providedModule.getName(),
                  module.getName()));
        }
      }

      maybeAddToSymbolTable(left);
      maybeAddStringNodeToSymbolTable(arg);

      // Requires should be removed before further processing.
      // Some clients run closure pass multiple times, first with
      // the checks for broken requires turned off. In these cases, we
      // allow broken requires to be preserved by the first run to
      // let them be caught in the subsequent run.
      if (provided != null) {
        parent.detachFromParent();
        compiler.reportCodeChange();
      }
    }
  }

// relevant test
// com.google.javascript.jscomp.AmbiguatePropertiesTest::testOneVar1
  public void testOneVar1() {
    test(" var Foo = function(){};Foo.prototype.b = 0;",
         "var Foo = function(){};Foo.prototype.a = 0;");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testOneVar2
  public void testOneVar2() {
    testSame(" var Foo = function(){};" +
             "Foo.prototype = {b: 0};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testOneVar3
  public void testOneVar3() {
    testSame(" var Foo = function(){};" +
             "Foo.prototype = {get b() {return 0}};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testOneVar4
  public void testOneVar4() {
    testSame(" var Foo = function(){};" +
             "Foo.prototype = {set b(a) {}};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testTwoVar1
  public void testTwoVar1() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.z=0;\n"
        + "Foo.prototype.z=0;\n"
        + "Foo.prototype.x=0;";
    String output = ""
        + "var Foo = function(){};\n"
        + "Foo.prototype.a=0;\n"
        + "Foo.prototype.a=0;\n"
        + "Foo.prototype.b=0;";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testTwoVar2
  public void testTwoVar2() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype={z:0, z:1, x:0};\n";
    
    testSame(js);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testTwoIndependentVar
  public void testTwoIndependentVar() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.b = 0;\n"
        + " var Bar = function(){};\n"
        + "Bar.prototype.c = 0;";
    String output = ""
        + "var Foo = function(){};"
        + "Foo.prototype.a=0;"
        + "var Bar = function(){};"
        + "Bar.prototype.a=0;";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testTwoTypesTwoVar
  public void testTwoTypesTwoVar() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.r = 0;\n"
        + "Foo.prototype.g = 0;\n"
        + " var Bar = function(){};\n"
        + "Bar.prototype.c = 0;"
        + "Bar.prototype.r = 0;";
    String output = ""
        + "var Foo = function(){};"
        + "Foo.prototype.a=0;"
        + "Foo.prototype.b=0;"
        + "var Bar = function(){};"
        + "Bar.prototype.b=0;"
        + "Bar.prototype.a=0;";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testUnion
  public void testUnion() {
    String js = ""
        + " var Foo = function(){};\n"
        + " var Bar = function(){};\n"
        + "Foo.prototype.foodoo=0;\n"
        + "Bar.prototype.bardoo=0;\n"
        + "\n"
        + "var U;\n"
        + "U.joint;"
        + "U.joint";
    String output = ""
        + "var Foo = function(){};\n"
        + "var Bar = function(){};\n"
        + "Foo.prototype.b=0;\n"
        + "Bar.prototype.b=0;\n"
        + "var U;\n"
        + "U.a;"
        + "U.a";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testUnions
  public void testUnions() {
    String js = ""
        + " var Foo = function(){};\n"
        + " var Bar = function(){};\n"
        + " var Baz = function(){};\n"
        + " var Bat = function(){};\n"
        + "Foo.prototype.lone1=0;\n"
        + "Bar.prototype.lone2=0;\n"
        + "Baz.prototype.lone3=0;\n"
        + "Bat.prototype.lone4=0;\n"
        + "\n"
        + "var U1;\n"
        + "U1.j1;"
        + "U1.j2;"
        + "\n"
        + "var U2;\n"
        + "U2.j3;"
        + "U2.j4;"
        + "\n"
        + "var U3;"
        + "U3.j5;"
        + "U3.j6";
    String output = ""
        + "var Foo = function(){};\n"
        + "var Bar = function(){};\n"
        + "var Baz = function(){};\n"
        + "var Bat = function(){};\n"
        + "Foo.prototype.c=0;\n"
        + "Bar.prototype.e=0;\n"
        + "Baz.prototype.e=0;\n"
        + "Bat.prototype.c=0;\n"
        + "var U1;\n"
        + "U1.a;"
        + "U1.b;"
        + "var U2;\n"
        + "U2.c;"
        + "U2.d;"
        + "var U3;"
        + "U3.a;"
        + "U3.b";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testExtends
  public void testExtends() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.x=0;\n"
        + " var Bar = function(){};\n"
        + "goog.inherits(Bar, Foo);\n"
        + "Bar.prototype.y=0;\n"
        + "Bar.prototype.z=0;\n"
        + " var Baz = function(){};\n"
        + "Baz.prototype.l=0;\n"
        + "Baz.prototype.m=0;\n"
        + "Baz.prototype.n=0;\n"
        + "(new Baz).m\n";
    String output = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.a=0;\n"
        + " var Bar = function(){};\n"
        + "goog.inherits(Bar, Foo);\n"
        + "Bar.prototype.b=0;\n"
        + "Bar.prototype.c=0;\n"
        + " var Baz = function(){};\n"
        + "Baz.prototype.b=0;\n"
        + "Baz.prototype.a=0;\n"
        + "Baz.prototype.c=0;\n"
        + "(new Baz).a\n";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testLotsOfVars
  public void testLotsOfVars() {
    StringBuilder js = new StringBuilder();
    StringBuilder output = new StringBuilder();
    js.append(" var Foo = function(){};\n");
    js.append(" var Bar = function(){};\n");
    output.append(js.toString());

    int vars = 10;
    for (int i = 0; i < vars; i++) {
      js.append("Foo.prototype.var" + i + " = 0;");
      js.append("Bar.prototype.var" + (i + 10000) + " = 0;");
      output.append("Foo.prototype." + (char) ('a' + i) + "=0;");
      output.append("Bar.prototype." + (char) ('a' + i) + "=0;");
    }
    test(js.toString(), output.toString());
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testLotsOfClasses
  public void testLotsOfClasses() {
    StringBuilder b = new StringBuilder();
    int classes = 10;
    for (int i = 0; i < classes; i++) {
      String c = "Foo" + i;
      b.append(" var " + c + " = function(){};\n");
      b.append(c + ".prototype.varness" + i + " = 0;");
    }
    String js = b.toString();
    test(js, js.replaceAll("varness\\d+", "a"));
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testFunctionType
  public void testFunctionType() {
    String js = ""
        + " function Foo(){};\n"
        + "\n"
        + "Foo.prototype.fun = function() { return new Bar(); };\n"
        + " function Bar(){};\n"
        + "Bar.prototype.bazz;\n"
        + "(new Foo).fun().bazz();";
    String output = ""
        + "function Foo(){};\n"
        + "Foo.prototype.a = function() { return new Bar(); };\n"
        + "function Bar(){};\n"
        + "Bar.prototype.a;\n"
        + "(new Foo).a().a();";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPrototypePropertiesAsObjLitKeys1
  public void testPrototypePropertiesAsObjLitKeys1() {
    test(" function Bar() {};" +
             "Bar.prototype = {2: function(){}, getA: function(){}};",
             " function Bar() {};" +
             "Bar.prototype = {2: function(){}, a: function(){}};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPrototypePropertiesAsObjLitKeys2
  public void testPrototypePropertiesAsObjLitKeys2() {
    testSame(" function Bar() {};" +
             "Bar.prototype = {2: function(){}, 'getA': function(){}};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testQuotedPrototypeProperty
  public void testQuotedPrototypeProperty() {
    testSame(" function Bar() {};" +
             "Bar.prototype['getA'] = function(){};" +
             "var bar = new Bar();" +
             "bar['getA']();");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testOverlappingOriginalAndGeneratedNames
  public void testOverlappingOriginalAndGeneratedNames() {
    test(" function Bar(){};"
         + "Bar.prototype.b = function(){};"
         + "Bar.prototype.a = function(){};"
         + "var bar = new Bar();"
         + "bar.b();",
         "function Bar(){};"
         + "Bar.prototype.a = function(){};"
         + "Bar.prototype.b = function(){};"
         + "var bar = new Bar();"
         + "bar.a();");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPropertyAddedToObject
  public void testPropertyAddedToObject() {
    testSame("var foo = {}; foo.prop = '';");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPropertyAddedToFunction
  public void testPropertyAddedToFunction() {
    test("var foo = function(){}; foo.prop = '';",
         "var foo = function(){}; foo.a = '';");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPropertyOfObjectOfUnknownType
  public void testPropertyOfObjectOfUnknownType() {
    testSame("var foo = x(); foo.prop = '';");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPropertyOnParamOfUnknownType
  public void testPropertyOnParamOfUnknownType() {
    testSame(" function Foo(){};\n"
             + "Foo.prototype.prop = 0;"
             + "function go(aFoo){\n"
             + "  aFoo.prop = 1;"
             + "}");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testSetPropertyOfGlobalThis
  public void testSetPropertyOfGlobalThis() {
    testSame("this.prop = 'bar'");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testReadPropertyOfGlobalThis
  public void testReadPropertyOfGlobalThis() {
    testSame("f(this.prop);");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testSetQuotedPropertyOfThis
  public void testSetQuotedPropertyOfThis() {
    testSame("this['prop'] = 'bar';");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testExternedPropertyName
  public void testExternedPropertyName() {
    test(" var Bar = function(){};"
         + " Bar.prototype.toString = function(){};"
         + "Bar.prototype.func = function(){};"
         + "var bar = new Bar();"
         + "bar.toString();",
         "var Bar = function(){};"
         + "Bar.prototype.toString = function(){};"
         + "Bar.prototype.a = function(){};"
         + "var bar = new Bar();"
         + "bar.toString();");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testExternedPropertyNameDefinedByObjectLiteral
  public void testExternedPropertyNameDefinedByObjectLiteral() {
    testSame("function Bar(){};Bar.prototype.factory");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testStaticAndInstanceMethodWithSameName
  public void testStaticAndInstanceMethodWithSameName() {
    test("function Bar(){}; Bar.getA = function(){}; " +
         "Bar.prototype.getA = function(){}; Bar.getA();" +
         "var bar = new Bar(); bar.getA();",
         "function Bar(){}; Bar.a = function(){};" +
         "Bar.prototype.a = function(){}; Bar.a();" +
         "var bar = new Bar(); bar.a();");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testStaticAndInstanceProperties
  public void testStaticAndInstanceProperties() {
    test("function Bar(){};" +
         "Bar.getA = function(){}; " +
         "Bar.prototype.getB = function(){};",
         "function Bar(){}; Bar.a = function(){};" +
         "Bar.prototype.a = function(){};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testStaticAndSubInstanceProperties
  public void testStaticAndSubInstanceProperties() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.x=0;\n"
        + " var Bar = function(){};\n"
        + "goog.inherits(Bar, Foo);\n"
        + "Bar.y=0;\n"
        + "Bar.prototype.z=0;\n";
    String output = ""
        + " var Foo = function(){};\n"
        + "Foo.a=0;\n"
        + " var Bar = function(){};\n"
        + "goog.inherits(Bar, Foo);\n"
        + "Bar.a=0;\n"
        + "Bar.prototype.a=0;\n";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testStaticWithFunctions
  public void testStaticWithFunctions() {
    String js = ""
      + " var Foo = function() {};\n"
      + "Foo.x = 0;"
      + " function f(x) { x.y = 1 }"
      + "f(Foo)";
    String output = ""
      + " var Foo = function() {};\n"
      + "Foo.a = 0;"
      + " function f(x) { x.y = 1 }"
      + "f(Foo)";
    test(js, output);

    js = ""
      + " var Foo = function() {};\n"
      + "Foo.x = 0;"
      + " function f(x) { x.y = 1; x.x = 2;}"
      + "f(Foo)";
    test(js, js);

    js = ""
      + " var Foo = function() {};\n"
      + "Foo.x = 0;"
      + " var Bar = function() {};\n"
      + "Bar.y = 0;";

    output = ""
      + " var Foo = function() {};\n"
      + "Foo.a = 0;"
      + " var Bar = function() {};\n"
      + "Bar.a = 0;";
    test(js, output);

  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testTypeMismatch
  public void testTypeMismatch() {
    testSame(EXTERNS, "var Foo = function(){};\n"
             + "var Bar = function(){};\n"
             + "Foo.prototype.b = 0;\n"
             + "\n"
             + "var F = new Bar();",
             TypeValidator.TYPE_MISMATCH_WARNING,
             "initializing variable\n"
             + "found   : Bar\n"
             + "required: (Foo|null)");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testRenamingMap
  public void testRenamingMap() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.z=0;\n"
        + "Foo.prototype.z=0;\n"
        + "Foo.prototype.x=0;\n"
        + "Foo.prototype.y=0;";
    String output = ""
        + "var Foo = function(){};\n"
        + "Foo.prototype.a=0;\n"
        + "Foo.prototype.a=0;\n"
        + "Foo.prototype.b=0;\n"
        + "Foo.prototype.c=0;";
    test(js, output);

    Map<String, String> answerMap = Maps.newHashMap();
    answerMap.put("x", "b");
    answerMap.put("y", "c");
    answerMap.put("z", "a");
    assertEquals(answerMap, lastPass.getRenamingMap());
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testInline
  public void testInline() {
    String js = ""
        + " function Foo(){}\n"
        + "Foo.prototype.x = function(){};\n"
        + "\n"
        + "function Bar(){}\n"
        + "\n"
        + "Bar.prototype.x = function() { return this.y; };\n"
        + "Bar.prototype.z = function() {};\n"
        
        + " (new Bar).y;";
    String output = ""
        + "function Foo(){}\n"
        + "Foo.prototype.a = function(){};\n"
        + "function Bar(){}\n"
        + "Bar.prototype.a = function() { return this.b; };\n"
        + "Bar.prototype.c = function() {};\n"
        
        + "(new Bar).b;";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testImplementsAndExtends
  public void testImplementsAndExtends() {
    String js = ""
        + " function Foo() {}\n"
        + "\n"
        + "function Bar(){}\n"
        + "Bar.prototype.y = function() { return 3; };\n"
        + "\n"
        + "function SubBar(){ }\n"
        + " function f(x) { x.z = 3; }\n"
        + " function g(x) { x.z = 3; }";
    String output = ""
        + "function Foo(){}\n"
        + "function Bar(){}\n"
        + "Bar.prototype.b = function() { return 3; };\n"
        + "function SubBar(){}\n"
        + "function f(x) { x.a = 3; }\n"
        + "function g(x) { x.a = 3; }";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testImplementsAndExtends2
  public void testImplementsAndExtends2() {
    String js = ""
        + " function A() {}\n"
        + "\n"
        + "function C1(){}\n"
        + "\n"
        + "function C2(){}\n"
        + " function f(x) { x.y = 3; }\n"
        + " function g(x) { x.z = 3; }\n";
    String output = ""
        + "function A(){}\n"
        + "function C1(){}\n"
        + "function C2(){}\n"
        + "function f(x) { x.a = 3; }\n"
        + "function g(x) { x.b = 3; }\n";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testExtendsInterface
  public void testExtendsInterface() {
    String js = ""
        + " function A() {}\n"
        + " function B() {}\n"
        + " function f(x) { x.y = 3; }\n"
        + " function g(x) { x.z = 3; }\n";
    String output = ""
        + "function A(){}\n"
        + "function B(){}\n"
        + "function f(x) { x.a = 3; }\n"
        + "function g(x) { x.b = 3; }\n";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testFunctionSubType
  public void testFunctionSubType() {
    String js = ""
        + "Function.prototype.a = 1;\n"
        + "function f() {}\n"
        + "f.y = 2;\n";
    String output = ""
        + "Function.prototype.a = 1;\n"
        + "function f() {}\n"
        + "f.b = 2;\n";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testFunctionSubType2
  public void testFunctionSubType2() {
    String js = ""
        + "Function.prototype.a = 1;\n"
        + " function F() {}\n"
        + "F.y = 2;\n";
    String output = ""
        + "Function.prototype.a = 1;\n"
        + "function F() {}\n"
        + "F.b = 2;\n";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPredeclaredType
  public void testPredeclaredType() {
    String js =
        "goog.addDependency('zzz.js', ['goog.Foo'], []);" +
        " " +
        "function A() {" +
        "  this.x = 3;" +
        "}" +
        "" +
        "function f(x) { x.y = 4; }";
    String result =
        "0;" +
        " " +
        "function A() {" +
        "  this.a = 3;" +
        "}" +
        "" +
        "function f(x) { x.y = 4; }";
    test(js, result);
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

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testVarConstructorName
  public void testVarConstructorName() {
    String js = "var bar = Date;" +
        "new bar();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testVarConstructorFunction
  public void testVarConstructorFunction() {
    String js = "var bar = function() {};" +
        "new bar();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testAssignConstructorName
  public void testAssignConstructorName() {
    String js = "var foo = {};" +
        "foo.bar = Date;" +
        "new foo.bar();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testAssignConstructorFunction
  public void testAssignConstructorFunction() {
    String js = "var foo = {};" +
        "foo.bar = function() {};" +
        "new foo.bar();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testConstructorFunctionReference
  public void testConstructorFunctionReference() {
    String js = "function bar() {}" +
        "new bar();";
    testSame(js);
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

// com.google.javascript.jscomp.InlinePropertiesTest::testConstInstanceProp1
  public void testConstInstanceProp1() {
    
    test(
        "\n" +
        "function C() {\n" +
        "  this.foo = 1;\n" +
        "}\n" +
        "new C().foo;",
        "function C() {\n" +
        "  this.foo = 1;\n" +
        "}\n" +
        "new C(), 1;");
  }

// com.google.javascript.jscomp.InlinePropertiesTest::testConstInstanceProp2
  public void testConstInstanceProp2() {
    
    test(
        "\n" +
        "function C() {\n" +
        "  this.foo = 1;\n" +
        "}\n" +
        "var x = new C();\n" +
        "x.foo;",
        "function C() {\n" +
        "  this.foo = 1\n" +
        "}\n" +
        "var x = new C();\n" +
        "1;\n");
  }

// com.google.javascript.jscomp.InlinePropertiesTest::testConstInstanceProp3
  public void testConstInstanceProp3() {
    
    test(
        "\n" +
        "function C() {\n" +
        "  this.foo = 1;\n" +
        "}\n" +
        "\n" +
        "var x = new C();\n" +
        "x.foo;",
        "function C() {\n" +
        "  this.foo = 1\n" +
        "}\n" +
        "var x = new C();\n" +
        "1;\n");
  }

// com.google.javascript.jscomp.InlinePropertiesTest::testConstInstanceProp4
  public void testConstInstanceProp4() {
    
    
    testSame(
        "\n" +
        "function C() {\n" +
        "  this.foo = 1;\n" +
        "}\n" +
        "\n" +
        "function B() {\n" +
        "  this.foo = 1;\n" +
        "}\n" +
        "new C().foo;\n");
  }

// com.google.javascript.jscomp.InlinePropertiesTest::testConstClassProps1
  public void testConstClassProps1() {
    
    
    testSame(
        "\n" +
        "function C() {\n" +
        "}\n" +
        "C.foo = 1;\n" +
        "C.foo;");
  }

// com.google.javascript.jscomp.InlinePropertiesTest::testConstClassProps2
  public void testConstClassProps2() {
    
    testSame(
        "\n" +
        "function C() {\n" +
        "  this.foo = 1;\n" +
        "}\n" +
        "C.foo;");
  }

// com.google.javascript.jscomp.InlinePropertiesTest::testConstClassProps3
  public void testConstClassProps3() {
    
    testSame(
        "\n" +
        "function C() {}\n" +
        "C.prototype.foo = 1;\n" +
        "c.foo;\n");
  }

// com.google.javascript.jscomp.InlinePropertiesTest::testNonConstClassProp1
  public void testNonConstClassProp1() {
    testSame(
        "\n" +
        "function C() {\n" +
        "  this.foo = 1;\n" +
        "}\n" +
        "var x = new C();\n" +
        "alert(x.foo);\n" +
        "delete x.foo;");
  }

// com.google.javascript.jscomp.InlinePropertiesTest::testNonConstClassProp2
  public void testNonConstClassProp2() {
    testSame(
        "\n" +
        "function C() {\n" +
        "  this.foo = 1;\n" +
        "}\n" +
        "var x = new C();\n" +
        "alert(x.foo);\n" +
        "x.foo = 2;");
  }

// com.google.javascript.jscomp.InlinePropertiesTest::testNonConstructorClassProp1
  public void testNonConstructorClassProp1() {
    testSame(
        "function C() {\n" +
        "  this.foo = 1;\n" +
        "  return this;\n" +
        "}\n" +
        "C().foo;");
  }

// com.google.javascript.jscomp.InlinePropertiesTest::testConditionalClassProp1
  public void testConditionalClassProp1() {
    testSame(
        "\n" +
        "function C() {\n" +
        "  if (false) this.foo = 1;\n" +
        "}\n" +
        "new C().foo;");
  }

// com.google.javascript.jscomp.InlinePropertiesTest::testConstPrototypeProp1
  public void testConstPrototypeProp1() {
    test(
        "\n" +
        "function C() {}\n" +
        "C.prototype.foo = 1;\n" +
        "new C().foo;\n",
        "function C() {}\n" +
        "C.prototype.foo = 1;\n" +
        "new C(), 1;\n");
  }

// com.google.javascript.jscomp.InlinePropertiesTest::testConstPrototypeProp2
  public void testConstPrototypeProp2() {
    test(
        "\n" +
        "function C() {}\n" +
        "C.prototype.foo = 1;\n" +
        "var x = new C();\n" +
        "x.foo;\n",
        "function C() {}\n" +
        "C.prototype.foo = 1;\n" +
        "var x = new C();\n" +
        "1;\n");
  }

// com.google.javascript.jscomp.IntegrationTest::testConstructorCycle
  public void testConstructorCycle() {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    test(options,
        " var AsyncTestCase = function() {};\n" +
        " Foo =  (AyncTestCase());",
        RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testBug1949424
  public void testBug1949424() {
    CompilerOptions options = createCompilerOptions();
    options.collapseProperties = true;
    options.closurePass = true;
    test(options, CLOSURE_BOILERPLATE + "goog.provide('FOO'); FOO.bar = 3;",
         CLOSURE_COMPILED + "var FOO$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testBug1949424_v2
  public void testBug1949424_v2() {
    CompilerOptions options = createCompilerOptions();
    options.collapseProperties = true;
    options.closurePass = true;
    test(options, CLOSURE_BOILERPLATE + "goog.provide('FOO.BAR'); FOO.BAR = 3;",
         CLOSURE_COMPILED + "var FOO$BAR = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testUnresolvedDefine
  public void testUnresolvedDefine() {
    CompilerOptions options = new CompilerOptions();
    options.closurePass = true;
    options.checkTypes = true;
    DiagnosticType[] warnings = { ProcessDefines.INVALID_DEFINE_TYPE_ERROR,
                                  RhinoErrorReporter.TYPE_PARSE_ERROR };
    String[] input = { "var goog = {};" +
                       "goog.provide('foo.bar');" +
                       " foo.bar = {};" };
    String[] output = { "var goog = {};" +
                        "var foo = {};" +
                        " foo.bar = {};" };
    test(options, input, output, warnings);
  }

// com.google.javascript.jscomp.IntegrationTest::testBug1956277
  public void testBug1956277() {
    CompilerOptions options = createCompilerOptions();
    options.collapseProperties = true;
    options.inlineVariables = true;
    test(options, "var CONST = {}; CONST.bar = null;" +
         "function f(url) { CONST.bar = url; }",
         "var CONST$bar = null; function f(url) { CONST$bar = url; }");
  }

// com.google.javascript.jscomp.IntegrationTest::testBug1962380
  public void testBug1962380() {
    CompilerOptions options = createCompilerOptions();
    options.collapseProperties = true;
    options.inlineVariables = true;
    options.generateExports = true;
    test(options,
         CLOSURE_BOILERPLATE + " goog.CONSTANT = 1;" +
         "var x = goog.CONSTANT;",
         "(function() {})('goog.CONSTANT', 1);" +
         "var x = 1;");
  }

// com.google.javascript.jscomp.IntegrationTest::testBug2410122
  public void testBug2410122() {
    CompilerOptions options = createCompilerOptions();
    options.generateExports = true;
    options.closurePass = true;
    test(options,
         "var goog = {};" +
         "function F() {}" +
         " function G() { goog.base(this); } " +
         "goog.inherits(G, F);",
         "var goog = {};" +
         "function F() {}" +
         "function G() { F.call(this); } " +
         "goog.inherits(G, F); goog.exportSymbol('G', G);");
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue90
  public void testIssue90() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;
    options.inlineVariables = true;
    options.removeDeadCode = true;
    test(options,
         "var x; x && alert(1);",
         "");
  }

// com.google.javascript.jscomp.IntegrationTest::testClosurePassOff
  public void testClosurePassOff() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = false;
    testSame(
        options,
        "var goog = {}; goog.require = function(x) {}; goog.require('foo');");
    testSame(
        options,
        "var goog = {}; goog.getCssName = function(x) {};" +
        "goog.getCssName('foo');");
  }

// com.google.javascript.jscomp.IntegrationTest::testClosurePassOn
  public void testClosurePassOn() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    test(
        options,
        "var goog = {}; goog.require = function(x) {}; goog.require('foo');",
        ProcessClosurePrimitives.MISSING_PROVIDE_ERROR);
    test(
        options,
        " var COMPILED = false;" +
        "var goog = {}; goog.getCssName = function(x) {};" +
        "goog.getCssName('foo');",
        "var COMPILED = true;" +
        "var goog = {}; goog.getCssName = function(x) {};" +
        "'foo';");
  }

// com.google.javascript.jscomp.IntegrationTest::testCssNameCheck
  public void testCssNameCheck() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.checkMissingGetCssNameLevel = CheckLevel.ERROR;
    options.checkMissingGetCssNameBlacklist = "foo";
    test(options, "var x = 'foo';",
         CheckMissingGetCssName.MISSING_GETCSSNAME);
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckEventfulDisposalWarningLevels
  public void testCheckEventfulDisposalWarningLevels() {
    CompilerOptions options = createCompilerOptions();
    options.setCheckEventfulObjectDisposalPolicy(
        CheckEventfulObjectDisposal.DisposalCheckingPolicy.ON);
    String js = "var goog = {};" + "goog.inherits = function(x, y) {};"
      + "goog.dispose = function(x) {};"
      + "goog.disposeAll = function(var_args) {};"
      + " goog.asserts.assert = function(x) { return x; };"
      + "goog.disposable = {};"
      + "\n"
      + "goog.disposable.IDisposable = function() {};"
      + "goog.disposable.IDisposable.prototype.dispose;"
      + "\n"
      + "goog.Disposable = goog.abstractMethod;"
      + ""
      + "goog.Disposable.prototype.dispose = goog.abstractMethod;"
      + ""
      + "goog.Disposable.prototype.registerDisposable = goog.abstractMethod;"
      + "goog.events = {};"
      + ""
      + "goog.events.EventHandler = function() {};"
      + ""
      + "var test = function() { this.eh = new goog.events.EventHandler(); };"
      + "goog.inherits(test, goog.Disposable);"
      + "var testObj = new test();";

    test(options, js, CheckEventfulObjectDisposal.EVENTFUL_OBJECT_NOT_DISPOSED);

    options.setWarningLevel(DiagnosticGroups.CHECK_EVENTFUL_OBJECT_DISPOSAL,
        CheckLevel.OFF);
    testSame(options, js);
  }

// com.google.javascript.jscomp.IntegrationTest::testBug2592659
  public void testBug2592659() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.checkTypes = true;
    options.checkMissingGetCssNameLevel = CheckLevel.WARNING;
    options.checkMissingGetCssNameBlacklist = "foo";
    test(options,
        "var goog = {};\n" +
        "\n" +
        "goog.getCssName = function(className, opt_modifier) {}\n" +
        "var x = goog.getCssName(123, 'a');",
        TypeValidator.TYPE_MISMATCH_WARNING);
  }

// com.google.javascript.jscomp.IntegrationTest::testTypedefBeforeOwner1
  public void testTypedefBeforeOwner1() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    test(options,
         "goog.provide('foo.Bar.Type');\n" +
         "goog.provide('foo.Bar');\n" +
         " foo.Bar.Type;\n" +
         "foo.Bar = function() {};",
         "var foo = {}; foo.Bar.Type; foo.Bar = function() {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testTypedefBeforeOwner2
  public void testTypedefBeforeOwner2() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.collapseProperties = true;
    test(options,
         "goog.provide('foo.Bar.Type');\n" +
         "goog.provide('foo.Bar');\n" +
         " foo.Bar.Type;\n" +
         "foo.Bar = function() {};",
         "var foo$Bar$Type; var foo$Bar = function() {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testExportedNames
  public void testExportedNames() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.variableRenaming = VariableRenamingPolicy.ALL;
    test(options,
         " var COMPILED = false;" +
         "var goog = {}; goog.exportSymbol('b', goog);",
         "var a = true; var c = {}; c.exportSymbol('b', c);");
    test(options,
         " var COMPILED = false;" +
         "var goog = {}; goog.exportSymbol('a', goog);",
         "var b = true; var c = {}; c.exportSymbol('a', c);");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckGlobalThisOn
  public void testCheckGlobalThisOn() {
    CompilerOptions options = createCompilerOptions();
    options.checkSuspiciousCode = true;
    options.checkGlobalThisLevel = CheckLevel.ERROR;
    test(options, "function f() { this.y = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.IntegrationTest::testSusiciousCodeOff
  public void testSusiciousCodeOff() {
    CompilerOptions options = createCompilerOptions();
    options.checkSuspiciousCode = false;
    options.checkGlobalThisLevel = CheckLevel.ERROR;
    test(options, "function f() { this.y = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckGlobalThisOff
  public void testCheckGlobalThisOff() {
    CompilerOptions options = createCompilerOptions();
    options.checkSuspiciousCode = true;
    options.checkGlobalThisLevel = CheckLevel.OFF;
    testSame(options, "function f() { this.y = 3; }");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckRequiresAndCheckProvidesOff
  public void testCheckRequiresAndCheckProvidesOff() {
    testSame(createCompilerOptions(), new String[] {
      " function Foo() {}",
      "new Foo();"
    });
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckRequiresOn
  public void testCheckRequiresOn() {
    CompilerOptions options = createCompilerOptions();
    options.checkRequires = CheckLevel.ERROR;
    test(options, new String[] {
      " function Foo() {}",
      "new Foo();"
    }, CheckRequiresForConstructors.MISSING_REQUIRE_WARNING);
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckProvidesOn
  public void testCheckProvidesOn() {
    CompilerOptions options = createCompilerOptions();
    options.checkProvides = CheckLevel.ERROR;
    test(options, new String[] {
      " function Foo() {}",
      "new Foo();"
    }, CheckProvides.MISSING_PROVIDE_WARNING);
  }

// com.google.javascript.jscomp.IntegrationTest::testGenerateExportsOff
  public void testGenerateExportsOff() {
    testSame(createCompilerOptions(), " function f() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testGenerateExportsOn
  public void testGenerateExportsOn() {
    CompilerOptions options = createCompilerOptions();
    options.generateExports = true;
    test(options, " function f() {}",
         " function f() {} goog.exportSymbol('f', f);");
  }

// com.google.javascript.jscomp.IntegrationTest::testInstrumentMemoryAllocationPassOff
  public void testInstrumentMemoryAllocationPassOff() {
    testSame(createCompilerOptions(),
        "var obj = new Object(); " +
        "var o = {}; " +
        "var a = []; " +
        "var f = function() {};" +
        "var s = 'a' + 'b'");
  }

// com.google.javascript.jscomp.IntegrationTest::testInstrumentMemoryAllocationPassOn
  public void testInstrumentMemoryAllocationPassOn() {}

// com.google.javascript.jscomp.IntegrationTest::testAngularPassOff
  public void testAngularPassOff() {
    testSame(createCompilerOptions(),
        " function f() {} " +
        " function g(a){} " +
        " var b = function f(a) {} ");
  }

// com.google.javascript.jscomp.IntegrationTest::testAngularPassOn
  public void testAngularPassOn() {
    CompilerOptions options = createCompilerOptions();
    options.angularPass = true;
    test(options,
        " function f() {} " +
        " function g(a){} " +
        " var b = function f(a, b, c) {} ",

        "function f() {} " +
        "function g(a) {} g['$inject']=['a'];" +
        "var b = function f(a, b, c) {}; b['$inject']=['a', 'b', 'c']");
  }

// com.google.javascript.jscomp.IntegrationTest::testExportTestFunctionsOff
  public void testExportTestFunctionsOff() {
    testSame(createCompilerOptions(), "function testFoo() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testExportTestFunctionsOn
  public void testExportTestFunctionsOn() {
    CompilerOptions options = createCompilerOptions();
    options.exportTestFunctions = true;
    test(options, "function testFoo() {}",
         " function testFoo() {}" +
         "goog.exportSymbol('testFoo', testFoo);");
  }

// com.google.javascript.jscomp.IntegrationTest::testExpose
  public void testExpose() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    test(options,
         "var x = {eeny: 1,  meeny: 2};" +
         " var Foo = function() {};" +
         "  Foo.prototype.miny = 3;" +
         "Foo.prototype.moe = 4;" +
         "  Foo.prototype.tiger;" +
         "function moe(a, b) { return a.meeny + b.miny + a.tiger; }" +
         "window['x'] = x;" +
         "window['Foo'] = Foo;" +
         "window['moe'] = moe;",
         "function a(){}" +
         "a.prototype.miny=3;" +
         "window.x={a:1,meeny:2};" +
         "window.Foo=a;" +
         "window.moe=function(b,c){" +
         "  return b.meeny+c.miny+b.tiger" +
         "}");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckSymbolsOff
  public void testCheckSymbolsOff() {
    CompilerOptions options = createCompilerOptions();
    testSame(options, "x = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckSymbolsOn
  public void testCheckSymbolsOn() {
    CompilerOptions options = createCompilerOptions();
    options.checkSymbols = true;
    test(options, "x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckReferencesOff
  public void testCheckReferencesOff() {
    CompilerOptions options = createCompilerOptions();
    testSame(options, "x = 3; var x = 5;");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckReferencesOn
  public void testCheckReferencesOn() {
    CompilerOptions options = createCompilerOptions();
    options.checkSymbols = true;
    options.aggressiveVarCheck = CheckLevel.ERROR;
    test(options, "x = 3; var x = 5;",
         VariableReferenceCheck.UNDECLARED_REFERENCE);
  }

// com.google.javascript.jscomp.IntegrationTest::testInferTypes
  public void testInferTypes() {
    CompilerOptions options = createCompilerOptions();
    options.inferTypes = true;
    options.checkTypes = false;
    options.closurePass = true;

    test(options,
        CLOSURE_BOILERPLATE +
        "goog.provide('Foo');  Foo = {a: 3};",
        TypeCheck.ENUM_NOT_CONSTANT);
    assertTrue(lastCompiler.getErrorManager().getTypedPercent() == 0);

    
    test(options, " var n = window.name;",
        "var n = window.name;");
    assertTrue(lastCompiler.getErrorManager().getTypedPercent() == 0);
  }

// com.google.javascript.jscomp.IntegrationTest::testTypeCheckAndInference
  public void testTypeCheckAndInference() {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    test(options, " var n = window.name;",
         TypeValidator.TYPE_MISMATCH_WARNING);
    assertTrue(lastCompiler.getErrorManager().getTypedPercent() > 0);
  }

// com.google.javascript.jscomp.IntegrationTest::testTypeNameParser
  public void testTypeNameParser() {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    test(options, " var n = window.name;",
         RhinoErrorReporter.TYPE_PARSE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testMemoizedTypedScopeCreator
  public void testMemoizedTypedScopeCreator() {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    options.ambiguateProperties = true;
    options.propertyRenaming = PropertyRenamingPolicy.ALL_UNQUOTED;
    test(options, "function someTest() {\n"
        + "  \n"
        + "  function Foo() { this.instProp = 3; }\n"
        + "  Foo.prototype.protoProp = function(a, b) {};\n"
        + "  \n"
        + "  function Bar() {}\n"
        + "  goog.inherits(Bar, Foo);\n"
        + "  var o = new Bar();\n"
        + "  o.protoProp(o.protoProp, o.instProp);\n"
        + "}",
        "function someTest() {\n"
        + "  function Foo() { this.b = 3; }\n"
        + "  function Bar() {}\n"
        + "  Foo.prototype.a = function(a, b) {};\n"
        + "  goog.c(Bar, Foo);\n"
        + "  var o = new Bar();\n"
        + "  o.a(o.a, o.b);\n"
        + "}");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckTypes
  public void testCheckTypes() {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    test(options, "var x = x || {}; x.f = function() {}; x.f(3);",
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.IntegrationTest::testReplaceCssNames
  public void testReplaceCssNames() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.gatherCssNames = true;
    test(options, "\n"
         + "var COMPILED = false;\n"
         + "goog.setCssNameMapping({'foo':'bar'});\n"
         + "function getCss() {\n"
         + "  return goog.getCssName('foo');\n"
         + "}",
         "var COMPILED = true;\n"
         + "function getCss() {\n"
         + "  return \"bar\";"
         + "}");
    assertEquals(
        ImmutableMap.of("foo", new Integer(1)),
        lastCompiler.getPassConfig().getIntermediateState().cssNames);
  }

// com.google.javascript.jscomp.IntegrationTest::testReplaceIdGeneratorsTest
  public void testReplaceIdGeneratorsTest() {
    CompilerOptions options = createCompilerOptions();
    options.replaceIdGenerators = true;

    options.setIdGenerators(ImmutableMap.<String, RenamingMap>of(
        "xid", new RenamingMap() {
      @Override
      public String get(String value) {
        return ":" + value + ":";
      }
    }));

    test(options, ""
         + "var xid = function() {};\n"
         + "function f() {\n"
         + "  return xid('foo');\n"
         + "}",
         "var xid = function() {};\n"
         + "function f() {\n"
         + "  return ':foo:';\n"
         + "}");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveClosureAsserts
  public void testRemoveClosureAsserts() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    testSame(options,
        "var goog = {};"
        + "goog.asserts.assert(goog);");
    options.removeClosureAsserts = true;
    test(options,
        "var goog = {};"
        + "goog.asserts.assert(goog);",
        "var goog = {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testDeprecation
  public void testDeprecation() {
    String code = " function f() { } function g() { f(); }";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.setWarningLevel(DiagnosticGroups.DEPRECATED, CheckLevel.ERROR);
    testSame(options, code);

    options.checkTypes = true;
    test(options, code, CheckAccessControls.DEPRECATED_NAME);
  }

// com.google.javascript.jscomp.IntegrationTest::testVisibility
  public void testVisibility() {
    String[] code = {
        " function f() { }",
        "function g() { f(); }"
    };

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.setWarningLevel(DiagnosticGroups.VISIBILITY, CheckLevel.ERROR);
    testSame(options, code);

    options.checkTypes = true;
    test(options, code, CheckAccessControls.BAD_PRIVATE_GLOBAL_ACCESS);
  }

// com.google.javascript.jscomp.IntegrationTest::testUnreachableCode
  public void testUnreachableCode() {
    String code = "function f() { return \n 3; }";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.checkUnreachableCode = CheckLevel.ERROR;
    test(options, code, CheckUnreachableCode.UNREACHABLE_CODE);
  }

// com.google.javascript.jscomp.IntegrationTest::testMissingReturn
  public void testMissingReturn() {
    String code =
        " function f() { if (f) { return 3; } }";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.checkMissingReturn = CheckLevel.ERROR;
    testSame(options, code);

    options.checkTypes = true;
    test(options, code, CheckMissingReturn.MISSING_RETURN_STATEMENT);
  }

// com.google.javascript.jscomp.IntegrationTest::testIdGenerators
  public void testIdGenerators() {
    String code =  "function f() {} f('id');";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.setIdGenerators(Sets.newHashSet("f"));
    test(options, code, "function f() {} 'a';");
  }

// com.google.javascript.jscomp.IntegrationTest::testOptimizeArgumentsArray
  public void testOptimizeArgumentsArray() {
    String code =  "function f() { return arguments[0]; }";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.optimizeArgumentsArray = true;
    String argName = "JSCompiler_OptimizeArgumentsArray_p0";
    test(options, code,
         "function f(" + argName + ") { return " + argName + "; }");
  }

// com.google.javascript.jscomp.IntegrationTest::testOptimizeParameters
  public void testOptimizeParameters() {
    String code = "function f(a) { return a; } f(true);";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.optimizeParameters = true;
    test(options, code, "function f() { var a = true; return a;} f();");
  }

// com.google.javascript.jscomp.IntegrationTest::testOptimizeReturns
  public void testOptimizeReturns() {
    String code = "function f(a) { return a; } f(true);";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.optimizeReturns = true;
    test(options, code, "function f(a) {return;} f(true);");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveAbstractMethods
  public void testRemoveAbstractMethods() {
    String code = CLOSURE_BOILERPLATE +
        "var x = {}; x.foo = goog.abstractMethod; x.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.closurePass = true;
    options.collapseProperties = true;
    test(options, code, CLOSURE_COMPILED + " var x$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testGoogDefine1
  public void testGoogDefine1() {
    String code = CLOSURE_BOILERPLATE +
        " goog.define('FLAG', true);";

    CompilerOptions options = createCompilerOptions();

    options.closurePass = true;
    options.collapseProperties = true;
    options.setDefineToBooleanLiteral("FLAG", false);

    test(options, code, CLOSURE_COMPILED + " var FLAG = false;");
  }

// com.google.javascript.jscomp.IntegrationTest::testGoogDefine2
  public void testGoogDefine2() {
    String code = CLOSURE_BOILERPLATE +
        "goog.provide('ns');" +
        " goog.define('ns.FLAG', true);";

    CompilerOptions options = createCompilerOptions();

    options.closurePass = true;
    options.collapseProperties = true;
    options.setDefineToBooleanLiteral("ns.FLAG", false);
    test(options, code, CLOSURE_COMPILED + "var ns$FLAG = false;");
  }

// com.google.javascript.jscomp.IntegrationTest::testCollapseProperties1
  public void testCollapseProperties1() {
    String code =
        "var x = {}; x.FOO = 5; x.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.collapseProperties = true;
    test(options, code, "var x$FOO = 5; var x$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testCollapseProperties2
  public void testCollapseProperties2() {
    String code =
        "var x = {}; x.FOO = 5; x.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.collapseProperties = true;
    options.collapseObjectLiterals = true;
    test(options, code, "var x$FOO = 5; var x$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testCollapseObjectLiteral1
  public void testCollapseObjectLiteral1() {
    
    String code = "var x = {}; x.FOO = 5; x.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.collapseObjectLiterals = true;
    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testCollapseObjectLiteral2
  public void testCollapseObjectLiteral2() {
    String code =
        "function f() {var x = {}; x.FOO = 5; x.bar = 3;}";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.collapseObjectLiterals = true;
    test(options, code,
        "function f(){" +
        "var JSCompiler_object_inline_FOO_0;" +
        "var JSCompiler_object_inline_bar_1;" +
        "JSCompiler_object_inline_FOO_0=5;" +
        "JSCompiler_object_inline_bar_1=3}");
  }

// com.google.javascript.jscomp.IntegrationTest::testTightenTypesWithoutTypeCheck
  public void testTightenTypesWithoutTypeCheck() {
    CompilerOptions options = createCompilerOptions();
    options.tightenTypes = true;
    test(options, "", DefaultPassConfig.TIGHTEN_TYPES_WITHOUT_TYPE_CHECK);
  }

// com.google.javascript.jscomp.IntegrationTest::testDisambiguateProperties
  public void testDisambiguateProperties() {
    String code =
        " function Foo(){} Foo.prototype.bar = 3;" +
        " function Baz(){} Baz.prototype.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.disambiguateProperties = true;
    options.checkTypes = true;
    test(options, code,
         "function Foo(){} Foo.prototype.Foo_prototype$bar = 3;" +
         "function Baz(){} Baz.prototype.Baz_prototype$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testMarkPureCalls
  public void testMarkPureCalls() {
    String testCode = "function foo() {} foo();";
    CompilerOptions options = createCompilerOptions();
    options.removeDeadCode = true;

    testSame(options, testCode);

    options.computeFunctionSideEffects = true;
    test(options, testCode, "function foo() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testMarkNoSideEffects
  public void testMarkNoSideEffects() {
    String testCode = "noSideEffects();";
    CompilerOptions options = createCompilerOptions();
    options.removeDeadCode = true;

    testSame(options, testCode);

    options.markNoSideEffectCalls = true;
    test(options, testCode, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testChainedCalls
  public void testChainedCalls() {
    CompilerOptions options = createCompilerOptions();
    options.chainCalls = true;
    test(
        options,
        " function Foo() {} " +
        "Foo.prototype.bar = function() { return this; }; " +
        "var f = new Foo();" +
        "f.bar(); " +
        "f.bar(); ",
        "function Foo() {} " +
        "Foo.prototype.bar = function() { return this; }; " +
        "var f = new Foo();" +
        "f.bar().bar();");
  }

// com.google.javascript.jscomp.IntegrationTest::testExtraAnnotationNames
  public void testExtraAnnotationNames() {
    CompilerOptions options = createCompilerOptions();
    options.setExtraAnnotationNames(Sets.newHashSet("TagA", "TagB"));
    test(
        options,
        " var f = new Foo();  f.bar();",
        "var f = new Foo(); f.bar();");
  }

// com.google.javascript.jscomp.IntegrationTest::testDevirtualizePrototypeMethods
  public void testDevirtualizePrototypeMethods() {
    CompilerOptions options = createCompilerOptions();
    options.devirtualizePrototypeMethods = true;
    test(
        options,
        " var Foo = function() {}; " +
        "Foo.prototype.bar = function() {};" +
        "(new Foo()).bar();",
        "var Foo = function() {};" +
        "var JSCompiler_StaticMethods_bar = " +
        "    function(JSCompiler_StaticMethods_bar$self) {};" +
        "JSCompiler_StaticMethods_bar(new Foo());");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckConsts
  public void testCheckConsts() {
    CompilerOptions options = createCompilerOptions();
    options.inlineConstantVars = true;
    test(options, "var FOO = true; FOO = false",
        ConstCheck.CONST_REASSIGNED_VALUE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testAllChecksOn
  public void testAllChecksOn() {
    CompilerOptions options = createCompilerOptions();
    options.checkSuspiciousCode = true;
    options.checkControlStructures = true;
    options.checkRequires = CheckLevel.ERROR;
    options.checkProvides = CheckLevel.ERROR;
    options.generateExports = true;
    options.exportTestFunctions = true;
    options.closurePass = true;
    options.checkMissingGetCssNameLevel = CheckLevel.ERROR;
    options.checkMissingGetCssNameBlacklist = "goog";
    options.syntheticBlockStartMarker = "synStart";
    options.syntheticBlockEndMarker = "synEnd";
    options.checkSymbols = true;
    options.aggressiveVarCheck = CheckLevel.ERROR;
    options.processObjectPropertyString = true;
    options.collapseProperties = true;
    test(options, CLOSURE_BOILERPLATE, CLOSURE_COMPILED);
  }

// com.google.javascript.jscomp.IntegrationTest::testTypeCheckingWithSyntheticBlocks
  public void testTypeCheckingWithSyntheticBlocks() {
    CompilerOptions options = createCompilerOptions();
    options.syntheticBlockStartMarker = "synStart";
    options.syntheticBlockEndMarker = "synEnd";
    options.checkTypes = true;

    
    
    
    testSame(
        options,
        " function f(x) {}" +
        "function g() {" +
        " synStart('foo');" +
        " var progress = 1;" +
        " f(progress);" +
        " synEnd('foo');" +
        "}");
  }

// com.google.javascript.jscomp.IntegrationTest::testCompilerDoesNotBlowUpIfUndefinedSymbols
  public void testCompilerDoesNotBlowUpIfUndefinedSymbols() {
    CompilerOptions options = createCompilerOptions();
    options.checkSymbols = true;

    
    options.setWarningLevel(
        DiagnosticGroup.forType(VarCheck.UNDEFINED_VAR_ERROR),
        CheckLevel.OFF);

    
    testSame(options, "var x = {foo: y};");
  }

// com.google.javascript.jscomp.IntegrationTest::testConstantTagsMustAlwaysBeRemoved
  public void testConstantTagsMustAlwaysBeRemoved() {
    CompilerOptions options = createCompilerOptions();

    options.variableRenaming = VariableRenamingPolicy.LOCAL;
    String originalText = "var G_GEO_UNKNOWN_ADDRESS=1;\n" +
        "function foo() {" +
        "  var localVar = 2;\n" +
        "  if (G_GEO_UNKNOWN_ADDRESS == localVar) {\n" +
        "    alert(\"A\"); }}";
    String expectedText = "var G_GEO_UNKNOWN_ADDRESS=1;" +
        "function foo(){var a=2;if(G_GEO_UNKNOWN_ADDRESS==a){alert(\"A\")}}";

    test(options, originalText, expectedText);
  }

// com.google.javascript.jscomp.IntegrationTest::testClosurePassPreservesJsDoc
  public void testClosurePassPreservesJsDoc() {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    options.closurePass = true;

    test(options,
         CLOSURE_BOILERPLATE +
         "goog.provide('Foo');  Foo = function() {};" +
         "var x = new Foo();",
         "var COMPILED=true;var goog={};goog.exportSymbol=function(){};" +
         "var Foo=function(){};var x=new Foo");
    test(options,
         CLOSURE_BOILERPLATE +
         "goog.provide('Foo');  Foo = {a: 3};",
         TypeCheck.ENUM_NOT_CONSTANT);
  }

// com.google.javascript.jscomp.IntegrationTest::testProvidedNamespaceIsConst
  public void testProvidedNamespaceIsConst() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.inlineConstantVars = true;
    options.collapseProperties = true;
    test(options,
         "var goog = {}; goog.provide('foo'); " +
         "function f() { foo = {};}",
         "var foo = {}; function f() { foo = {}; }",
         ConstCheck.CONST_REASSIGNED_VALUE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testProvidedNamespaceIsConst2
  public void testProvidedNamespaceIsConst2() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.inlineConstantVars = true;
    options.collapseProperties = true;
    test(options,
         "var goog = {}; goog.provide('foo.bar'); " +
         "function f() { foo.bar = {};}",
         "var foo$bar = {};" +
         "function f() { foo$bar = {}; }",
         ConstCheck.CONST_REASSIGNED_VALUE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testProvidedNamespaceIsConst3
  public void testProvidedNamespaceIsConst3() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.inlineConstantVars = true;
    options.collapseProperties = true;
    test(options,
         "var goog = {}; " +
         "goog.provide('foo.bar'); goog.provide('foo.bar.baz'); " +
         " foo.bar = function() {};" +
         " foo.bar.baz = function() {};",
         "var foo$bar = function(){};" +
         "var foo$bar$baz = function(){};");
  }

// com.google.javascript.jscomp.IntegrationTest::testProvidedNamespaceIsConst4
  public void testProvidedNamespaceIsConst4() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.inlineConstantVars = true;
    options.collapseProperties = true;
    test(options,
         "var goog = {}; goog.provide('foo.Bar'); " +
         "var foo = {}; foo.Bar = {};",
         "var foo = {}; foo = {}; foo.Bar = {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testProvidedNamespaceIsConst5
  public void testProvidedNamespaceIsConst5() {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.inlineConstantVars = true;
    options.collapseProperties = true;
    test(options,
         "var goog = {}; goog.provide('foo.Bar'); " +
         "foo = {}; foo.Bar = {};",
         "var foo = {}; foo = {}; foo.Bar = {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testProcessDefinesAlwaysOn
  public void testProcessDefinesAlwaysOn() {
    test(createCompilerOptions(),
         " var HI = true; HI = false;",
         "var HI = false;false;");
  }

// com.google.javascript.jscomp.IntegrationTest::testProcessDefinesAdditionalReplacements
  public void testProcessDefinesAdditionalReplacements() {
    CompilerOptions options = createCompilerOptions();
    options.setDefineToBooleanLiteral("HI", false);
    test(options,
         " var HI = true;",
         "var HI = false;");
  }

// com.google.javascript.jscomp.IntegrationTest::testReplaceMessages
  public void testReplaceMessages() {
    CompilerOptions options = createCompilerOptions();
    String prefix = "var goog = {}; goog.getMsg = function() {};";
    testSame(options, prefix + "var MSG_HI = goog.getMsg('hi');");

    options.messageBundle = new EmptyMessageBundle();
    test(options,
        prefix + " var MSG_HI = goog.getMsg('hi');",
        prefix + "var MSG_HI = 'hi';");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckGlobalNames
  public void testCheckGlobalNames() {
    CompilerOptions options = createCompilerOptions();
    options.checkGlobalNamesLevel = CheckLevel.ERROR;
    test(options, "var x = {}; var y = x.z;",
         CheckGlobalNames.UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.IntegrationTest::testInlineGetters
  public void testInlineGetters() {
    CompilerOptions options = createCompilerOptions();
    String code =
        "function Foo() {} Foo.prototype.bar = function() { return 3; };" +
        "var x = new Foo(); x.bar();";

    testSame(options, code);
    options.inlineGetters = true;

    test(options, code,
         "function Foo() {} Foo.prototype.bar = function() { return 3 };" +
         "var x = new Foo(); 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testInlineGettersWithAmbiguate
  public void testInlineGettersWithAmbiguate() {
    CompilerOptions options = createCompilerOptions();

    String code =
        "" +
        "function Foo() {}" +
        " Foo.prototype.field;" +
        "Foo.prototype.getField = function() { return this.field; };" +
        "" +
        "function Bar() {}" +
        " Bar.prototype.field;" +
        "Bar.prototype.getField = function() { return this.field; };" +
        "new Foo().getField();" +
        "new Bar().getField();";

    testSame(options, code);

    options.inlineGetters = true;

    test(options, code,
        "function Foo() {}" +
        "Foo.prototype.field;" +
        "Foo.prototype.getField = function() { return this.field; };" +
        "function Bar() {}" +
        "Bar.prototype.field;" +
        "Bar.prototype.getField = function() { return this.field; };" +
        "new Foo().field;" +
        "new Bar().field;");

    options.checkTypes = true;
    options.ambiguateProperties = true;

    
    
    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testInlineVariables
  public void testInlineVariables() {
    CompilerOptions options = createCompilerOptions();
    String code = "function foo() {} var x = 3; foo(x);";
    testSame(options, code);

    options.inlineVariables = true;
    test(options, code, "(function foo() {})(3);");

    options.propertyRenaming = PropertyRenamingPolicy.HEURISTIC;
    test(options, code, DefaultPassConfig.CANNOT_USE_PROTOTYPE_AND_VAR);
  }

// com.google.javascript.jscomp.IntegrationTest::testInlineConstants
  public void testInlineConstants() {
    CompilerOptions options = createCompilerOptions();
    String code = "function foo() {} var x = 3; foo(x); var YYY = 4; foo(YYY);";
    testSame(options, code);

    options.inlineConstantVars = true;
    test(options, code, "function foo() {} var x = 3; foo(x); foo(4);");
  }

// com.google.javascript.jscomp.IntegrationTest::testMinimizeExits
  public void testMinimizeExits() {
    CompilerOptions options = createCompilerOptions();
    String code =
        "function f() {" +
        "  if (window.foo) return; window.h(); " +
        "}";
    testSame(options, code);

    options.foldConstants = true;
    test(
        options, code,
        "function f() {" +
        "  window.foo || window.h(); " +
        "}");
  }

// com.google.javascript.jscomp.IntegrationTest::testFoldConstants
  public void testFoldConstants() {
    CompilerOptions options = createCompilerOptions();
    String code = "if (true) { window.foo(); }";
    testSame(options, code);

    options.foldConstants = true;
    test(options, code, "window.foo();");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveUnreachableCode
  public void testRemoveUnreachableCode() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() { return; f(); }";
    testSame(options, code);

    options.removeDeadCode = true;
    test(options, code, "function f() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveUnusedPrototypeProperties1
  public void testRemoveUnusedPrototypeProperties1() {
    CompilerOptions options = createCompilerOptions();
    String code = "function Foo() {} " +
        "Foo.prototype.bar = function() { return new Foo(); };";
    testSame(options, code);

    options.removeUnusedPrototypeProperties = true;
    test(options, code, "function Foo() {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveUnusedPrototypeProperties2
  public void testRemoveUnusedPrototypeProperties2() {
    CompilerOptions options = createCompilerOptions();
    String code = "function Foo() {} " +
        "Foo.prototype.bar = function() { return new Foo(); };" +
        "function f(x) { x.bar(); }";
    testSame(options, code);

    options.removeUnusedPrototypeProperties = true;
    testSame(options, code);

    options.removeUnusedVars = true;
    test(options, code, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testSmartNamePass
  public void testSmartNamePass() {
    CompilerOptions options = createCompilerOptions();
    String code = "function Foo() { this.bar(); } " +
        "Foo.prototype.bar = function() { return Foo(); };";
    testSame(options, code);

    options.smartNameRemoval = true;
    test(options, code, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testDeadAssignmentsElimination
  public void testDeadAssignmentsElimination() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() { var x = 3; 4; x = 5; return x; } f(); ";
    testSame(options, code);

    options.deadAssignmentElimination = true;
    testSame(options, code);

    options.removeUnusedVars = true;
    test(options, code, "function f() { var x = 3; 4; x = 5; return x; } f();");
  }

// com.google.javascript.jscomp.IntegrationTest::testInlineFunctions
  public void testInlineFunctions() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() { return 3; } f(); ";
    testSame(options, code);

    options.inlineFunctions = true;
    test(options, code, "3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveUnusedVars1
  public void testRemoveUnusedVars1() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f(x) {} f();";
    testSame(options, code);

    options.removeUnusedVars = true;
    test(options, code, "function f() {} f();");
  }

// com.google.javascript.jscomp.IntegrationTest::testRemoveUnusedVars2
  public void testRemoveUnusedVars2() {
    CompilerOptions options = createCompilerOptions();
    String code = "(function f(x) {})();var g = function() {}; g();";
    testSame(options, code);

    options.removeUnusedVars = true;
    test(options, code, "(function() {})();var g = function() {}; g();");

    options.anonymousFunctionNaming = AnonymousFunctionNamingPolicy.UNMAPPED;
    test(options, code, "(function f() {})();var g = function $g$() {}; g();");
  }

// com.google.javascript.jscomp.IntegrationTest::testCrossModuleCodeMotion
  public void testCrossModuleCodeMotion() {
    CompilerOptions options = createCompilerOptions();
    String[] code = new String[] {
      "var x = 1;",
      "x;",
    };
    testSame(options, code);

    options.crossModuleCodeMotion = true;
    test(options, code, new String[] {
      "",
      "var x = 1; x;",
    });
  }

// com.google.javascript.jscomp.IntegrationTest::testCrossModuleMethodMotion
  public void testCrossModuleMethodMotion() {
    CompilerOptions options = createCompilerOptions();
    String[] code = new String[] {
      "var Foo = function() {}; Foo.prototype.bar = function() {};" +
      "var x = new Foo();",
      "x.bar();",
    };
    testSame(options, code);

    options.crossModuleMethodMotion = true;
    test(options, code, new String[] {
      CrossModuleMethodMotion.STUB_DECLARATIONS +
      "var Foo = function() {};" +
      "Foo.prototype.bar=JSCompiler_stubMethod(0); var x=new Foo;",
      "Foo.prototype.bar=JSCompiler_unstubMethod(0,function(){}); x.bar()",
    });
  }

// com.google.javascript.jscomp.IntegrationTest::testFlowSensitiveInlineVariables1
  public void testFlowSensitiveInlineVariables1() {
    CompilerOptions options = createCompilerOptions();
    String code = "function f() { var x = 3; x = 5; return x; }";
    testSame(options, code);

    options.flowSensitiveInlineVariables = true;
    test(options, code, "function f() { var x = 3; return 5; }");

    String unusedVar = "function f() { var x; x = 5; return x; } f()";
    test(options, unusedVar, "function f() { var x; return 5; } f()");

    options.removeUnusedVars = true;
    test(options, unusedVar, "function f() { return 5; } f()");
  }

// com.google.javascript.jscomp.IntegrationTest::testFlowSensitiveInlineVariables2
  public void testFlowSensitiveInlineVariables2() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.SIMPLE_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    test(options,
        "function f () {\n" +
        "    var ab = 0;\n" +
        "    ab += '-';\n" +
        "    alert(ab);\n" +
        "}",
        "function f () {\n" +
        "    alert('0-');\n" +
        "}");
  }
