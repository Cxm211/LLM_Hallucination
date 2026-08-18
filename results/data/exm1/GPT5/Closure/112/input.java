// buggy code
  private boolean inferTemplatedTypesForCall(
      Node n, FunctionType fnType) {
    final ImmutableList<TemplateType> keys = fnType.getTemplateTypeMap()
        .getTemplateKeys();
    if (keys.isEmpty()) {
      return false;
    }

    // Try to infer the template types
    Map<TemplateType, JSType> inferred = 
        inferTemplateTypesFromParameters(fnType, n);


    // Replace all template types. If we couldn't find a replacement, we
    // replace it with UNKNOWN.
    TemplateTypeReplacer replacer = new TemplateTypeReplacer(
        registry, inferred);
    Node callTarget = n.getFirstChild();

    FunctionType replacementFnType = fnType.visit(replacer)
        .toMaybeFunctionType();
    Preconditions.checkNotNull(replacementFnType);

    callTarget.setJSType(replacementFnType);
    n.setJSType(replacementFnType.getReturnType());

    return replacer.madeChanges;
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

// com.google.javascript.jscomp.CheckAccessControlsTest::testDeprecatedFunction
  public void testDeprecatedFunction() {
    testDep(" function f() {} function g() { f(); }",
            "Some Reason",
            DEPRECATED_NAME, DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningOnDeprecatedConstVariable
  public void testWarningOnDeprecatedConstVariable() {
    testDep(" var f = 4; function g() { alert(f); }",
            "Another reason",
            DEPRECATED_NAME, DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testThatNumbersArentDeprecated
  public void testThatNumbersArentDeprecated() {
    testSame(" var f = 4; var h = 3; " +
             "function g() { alert(h); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testDeprecatedFunctionVariable
  public void testDeprecatedFunctionVariable() {
    testDep(" var f = function() {}; " +
            "function g() { f(); }", "I like g...",
            DEPRECATED_NAME, DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInGlobalScope
  public void testNoWarningInGlobalScope() {
    testSame("var goog = {}; goog.makeSingleton = function(x) {};" +
        " function f() {} goog.makeSingleton(f);");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInGlobalScopeForCall
  public void testNoWarningInGlobalScopeForCall() {
    testDep(" function f() {} f();",
            "Some global scope", DEPRECATED_NAME, DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInDeprecatedFunction
  public void testNoWarningInDeprecatedFunction() {
    testSame(" function f() {} " +
             " function g() { f(); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningInNormalClass
  public void testWarningInNormalClass() {
    testDep(" function f() {}" +
            "  var Foo = function() {}; " +
            "Foo.prototype.bar = function() { f(); }",
            "FooBar", DEPRECATED_NAME, DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForProperty1
  public void testWarningForProperty1() {
    testDep(" function Foo() {}" +
            " Foo.prototype.bar = 3;" +
            "Foo.prototype.baz = function() { alert((new Foo()).bar); };",
            "A property is bad",
            DEPRECATED_PROP, DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForProperty2
  public void testWarningForProperty2() {
    testDep(" function Foo() {}" +
            " Foo.prototype.bar = 3;" +
            "Foo.prototype.baz = function() { alert(this.bar); };",
            "Zee prop, it is deprecated!",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForDeprecatedClass
  public void testWarningForDeprecatedClass() {
    testDep(" function Foo() {} " +
            "function f() { new Foo(); }",
            "Use the class 'Bar'",
            DEPRECATED_CLASS,
            DEPRECATED_CLASS_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningForDeprecatedClassInstance
  public void testNoWarningForDeprecatedClassInstance() {
    testSame(" function Foo() {} " +
             " function f(x) { return x; }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForDeprecatedSuperClass
  public void testWarningForDeprecatedSuperClass() {
    testDep(" function Foo() {} " +
            " function SubFoo() {}" +
            "function f() { new SubFoo(); }",
            "Superclass to the rescue!",
            DEPRECATED_CLASS,
            DEPRECATED_CLASS_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForDeprecatedSuperClass2
  public void testWarningForDeprecatedSuperClass2() {
    testDep(" function Foo() {} " +
            "var namespace = {}; " +
            " " +
            "namespace.SubFoo = function() {}; " +
            "function f() { new namespace.SubFoo(); }",
            "Its only weakness is Kryptoclass",
            DEPRECATED_CLASS,
            DEPRECATED_CLASS_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForPrototypeProperty
  public void testWarningForPrototypeProperty() {
    testDep(" function Foo() {}" +
            " Foo.prototype.bar = 3;" +
            "Foo.prototype.baz = function() { alert(Foo.prototype.bar); };",
            "It is now in production, use that model...",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningForNumbers
  public void testNoWarningForNumbers() {
    testSame(" function Foo() {}" +
             " Foo.prototype.bar = 3;" +
             "Foo.prototype.baz = function() { alert(3); };");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForMethod1
  public void testWarningForMethod1() {
    testDep(" function Foo() {}" +
            " Foo.prototype.bar = function() {};" +
            "Foo.prototype.baz = function() { this.bar(); };",
            "There is a madness to this method",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForMethod2
  public void testWarningForMethod2() {
    testDep(" function Foo() {} " +
            " Foo.prototype.bar; " +
            "Foo.prototype.baz = function() { this.bar(); };",
            "Stop the ringing!",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInDeprecatedClass
  public void testNoWarningInDeprecatedClass() {
    testSame(" function f() {} " +
             " " +
             "var Foo = function() {}; " +
             "Foo.prototype.bar = function() { f(); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInDeprecatedClass2
  public void testNoWarningInDeprecatedClass2() {
    testSame(" function f() {} " +
             " " +
             "var Foo = function() {}; " +
             "Foo.bar = function() { f(); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInDeprecatedStaticMethod
  public void testNoWarningInDeprecatedStaticMethod() {
    testSame(" function f() {} " +
             " " +
             "var Foo = function() {}; " +
             " Foo.bar = function() { f(); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningInStaticMethod
  public void testWarningInStaticMethod() {
    testDep(" function f() {} " +
            " " +
            "var Foo = function() {}; " +
            "Foo.bar = function() { f(); }",
            "crazy!",
            DEPRECATED_NAME,
            DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testDeprecatedObjLitKey
  public void testDeprecatedObjLitKey() {
    testDep("var f = {};  f.foo = 3; " +
            "function g() { return f.foo; }",
            "It is literally not used anymore",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForSubclassMethod
  public void testWarningForSubclassMethod() {
    testDep(" function Foo() {}" +
            "Foo.prototype.bar = function() {};" +
            " function SubFoo() {}" +
            " SubFoo.prototype.bar = function() {};" +
            "function f() { (new SubFoo()).bar(); };",
            "I have a parent class!",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForSuperClassWithDeprecatedSubclassMethod
  public void testWarningForSuperClassWithDeprecatedSubclassMethod() {
    testSame(" function Foo() {}" +
             "Foo.prototype.bar = function() {};" +
             " function SubFoo() {}" +
             " SubFoo.prototype.bar = " +
             "function() {};" +
             "function f() { (new Foo()).bar(); };");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForSuperclassMethod
  public void testWarningForSuperclassMethod() {
    testDep(" function Foo() {}" +
            " Foo.prototype.bar = function() {};" +
            " function SubFoo() {}" +
            "SubFoo.prototype.bar = function() {};" +
            "function f() { (new SubFoo()).bar(); };",
            "I have a child class!",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForSuperclassMethod2
  public void testWarningForSuperclassMethod2() {
    testDep(" function Foo() {}" +
            "" +
            "Foo.prototype.bar = function() {};" +
            " function SubFoo() {}" +
            "SubFoo.prototype.bar = function() {};" +
            "function f() { (new SubFoo()).bar(); };",
            "I have another child class...",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForBind
  public void testWarningForBind() {
    testDep(" Function.prototype.bind = function() {};" +
            "(function() {}).bind();",
            "I'm bound to this method...",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForDeprecatedClassInGlobalScope
  public void testWarningForDeprecatedClassInGlobalScope() {
    testDep(" var Foo = function() {};" +
            "new Foo();",
            "I'm a very worldly object!",
            DEPRECATED_CLASS,
            DEPRECATED_CLASS_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningForPrototypeCopying
  public void testNoWarningForPrototypeCopying() {
    testSame(" var Foo = function() {};" +
             "Foo.prototype.bar = function() {};" +
             " Foo.prototype.baz = Foo.prototype.bar;" +
             "(new Foo()).bar();");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningOnDeprecatedPrototype
  public void testNoWarningOnDeprecatedPrototype() {
    
    testSame(" var Foo = function() {};" +
        " Foo.prototype = {};" +
        "Foo.prototype.bar = function() {};");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testPrivateAccessForNames
  public void testPrivateAccessForNames() {
    testSame(" function foo_() {}; foo_();");
    test(new String[] {
      " function foo_() {};",
      "foo_();"
    }, null, BAD_PRIVATE_GLOBAL_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testPrivateAccessForProperties1
  public void testPrivateAccessForProperties1() {
    testSame(" function Foo() {}" +
        " Foo.prototype.bar_ = function() {};" +
        "Foo.prototype.baz = function() { this.bar_(); }; (new Foo).bar_();");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testPrivateAccessForProperties2
  public void testPrivateAccessForProperties2() {
    testSame(new String[] {
      " function Foo() {}",
      " Foo.prototype.bar_ = function() {};" +
      "Foo.prototype.baz = function() { this.bar_(); }; (new Foo).bar_();"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testPrivateAccessForProperties3
  public void testPrivateAccessForProperties3() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar_ = function() {}; (new Foo).bar_();",
      "Foo.prototype.baz = function() { this.bar_(); };"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testPrivateAccessForProperties4
  public void testPrivateAccessForProperties4() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar_ = function() {};",
      "Foo.prototype['baz'] = function() { (new Foo()).bar_(); };"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties1
  public void testNoPrivateAccessForProperties1() {
    test(new String[] {
      " function Foo() {} (new Foo).bar_();",
      " Foo.prototype.bar_ = function() {};" +
      "Foo.prototype.baz = function() { this.bar_(); };"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties2
  public void testNoPrivateAccessForProperties2() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};" +
      "Foo.prototype.baz = function() { this.bar_(); };",
      "(new Foo).bar_();"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties3
  public void testNoPrivateAccessForProperties3() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};",
      " function OtherFoo() { (new Foo).bar_(); }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties4
  public void testNoPrivateAccessForProperties4() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};",
      " " +
      "function SubFoo() { this.bar_(); }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties5
  public void testNoPrivateAccessForProperties5() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};",
      " " +
      "function SubFoo() {};" +
      "SubFoo.prototype.baz = function() { this.bar_(); }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties6
  public void testNoPrivateAccessForProperties6() {
    
    
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};",
      " " +
      "function SubFoo() {};" +
      "SubFoo.prototype.bar_ = function() {};"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties7
  public void testNoPrivateAccessForProperties7() {
    
    
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};" +
      " " +
      "function SubFoo() {};" +
      "SubFoo.prototype.bar_ = function() {};",
      "SubFoo.prototype.baz = function() { this.bar_(); }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties8
  public void testNoPrivateAccessForProperties8() {
    test(new String[] {
      " function Foo() {  this.bar_ = 3; }",
      " " +
      "function SubFoo() {  this.bar_ = 3; };"
    }, null, PRIVATE_OVERRIDE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties1
  public void testProtectedAccessForProperties1() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar = function() {};" +
      "(new Foo).bar();",
      "Foo.prototype.baz = function() { this.bar(); };"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties2
  public void testProtectedAccessForProperties2() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar = function() {};" +
      "(new Foo).bar();",
      "" +
      "function SubFoo() { this.bar(); }"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties3
  public void testProtectedAccessForProperties3() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar = function() {};" +
      "(new Foo).bar();",
      "" +
      "function SubFoo() { }" +
      "SubFoo.baz = function() { (new Foo).bar(); }"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties4
  public void testProtectedAccessForProperties4() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.bar = function() {};",
      "" +
      "function SubFoo() { Foo.bar(); }"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties5
  public void testProtectedAccessForProperties5() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar = function() {};" +
      "(new Foo).bar();",
      "" +
      "var SubFoo = function() { this.bar(); }"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties6
  public void testProtectedAccessForProperties6() {
    testSame(new String[] {
      "var goog = {};" +
      " goog.Foo = function() {};" +
      " goog.Foo.prototype.bar = function() {};",
      "" +
      "goog.SubFoo = function() { this.bar(); };"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoProtectedAccessForProperties1
  public void testNoProtectedAccessForProperties1() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar = function() {};",
      "(new Foo).bar();"
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoProtectedAccessForProperties2
  public void testNoProtectedAccessForProperties2() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar = function() {};",
      " function OtherFoo() { (new Foo).bar(); }"
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoProtectedAccessForProperties3
  public void testNoProtectedAccessForProperties3() {
    test(new String[] {
      " function Foo() {} " +
      " " +
      "function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};",
      " " +
      "function SubberFoo() { (new SubFoo).bar(); }"
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoProtectedAccessForProperties4
  public void testNoProtectedAccessForProperties4() {
    test(new String[] {
      " function Foo() { (new SubFoo).bar(); } ",
      " " +
      "function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};",
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoProtectedAccessForProperties5
  public void testNoProtectedAccessForProperties5() {
    test(new String[] {
      "var goog = {};" +
      " goog.Foo = function() {};" +
      " goog.Foo.prototype.bar = function() {};",
      "" +
      "goog.NotASubFoo = function() { (new goog.Foo).bar(); };"
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoExceptionsWithBadConstructors1
  public void testNoExceptionsWithBadConstructors1() {
    testSame(new String[] {
      "function Foo() { (new SubFoo).bar(); } " +
      " function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoExceptionsWithBadConstructors2
  public void testNoExceptionsWithBadConstructors2() {
    testSame(new String[] {
      " function Foo() {} " +
      "Foo.prototype.bar = function() {};" +
      "" +
      "function SubFoo() {}" +
      " " +
      "SubFoo.prototype.bar = function() { (new Foo).bar(); };"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testGoodOverrideOfProtectedProperty
  public void testGoodOverrideOfProtectedProperty() {
    testSame(new String[] {
      " function Foo() { } " +
      " Foo.prototype.bar = function() {};",
      " " +
      "function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};",
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testBadOverrideOfProtectedProperty
  public void testBadOverrideOfProtectedProperty() {
    test(new String[] {
      " function Foo() { } " +
      " Foo.prototype.bar = function() {};",
      " " +
      "function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};",
    }, null, VISIBILITY_MISMATCH);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testBadOverrideOfPrivateProperty
  public void testBadOverrideOfPrivateProperty() {
    test(new String[] {
      " function Foo() { } " +
      " Foo.prototype.bar = function() {};",
      " " +
      "function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};",
    }, null, PRIVATE_OVERRIDE);

    testSame(new String[] {
      " function Foo() { } " +
      " Foo.prototype.bar = function() {};",
      " " +
      "function SubFoo() {}" +
      "\n" +
      " SubFoo.prototype.bar = function() {};",
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testAccessOfStaticMethodOnPrivateConstructor
  public void testAccessOfStaticMethodOnPrivateConstructor() {
    testSame(new String[] {
      " function Foo() { } " +
      "Foo.create = function() { return new Foo(); };",
      "Foo.create()",
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testAccessOfStaticMethodOnPrivateQualifiedConstructor
  public void testAccessOfStaticMethodOnPrivateQualifiedConstructor() {
    testSame(new String[] {
      "var goog = {};" +
      " goog.Foo = function() { }; " +
      "goog.Foo.create = function() { return new goog.Foo(); };",
      "goog.Foo.create()",
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testInstanceofOfPrivateConstructor
  public void testInstanceofOfPrivateConstructor() {
    testSame(new String[] {
      "var goog = {};" +
      " goog.Foo = function() { }; " +
      "goog.Foo.create = function() { return new goog.Foo(); };",
      "goog instanceof goog.Foo",
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testOkAssignmentOfDeprecatedProperty
  public void testOkAssignmentOfDeprecatedProperty() {
    testSame(
        " function Foo() {" +
        "  this.bar = 3;" +
        "}");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testBadReadOfDeprecatedProperty
  public void testBadReadOfDeprecatedProperty() {
    testDep(
        " function Foo() {" +
        "  this.bar = 3;" +
        "  this.baz = this.bar;" +
        "}",
        "GRR",
        DEPRECATED_PROP,
        DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testAutoboxedDeprecatedProperty
  public void testAutoboxedDeprecatedProperty() {
    test(
        "", 
        " function String() {}" +
        " String.prototype.length;" +
        "function f() { return 'x'.length; }",
        "GRR",
        DEPRECATED_PROP_REASON,
        null);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testAutoboxedPrivateProperty
  public void testAutoboxedPrivateProperty() {
    test(
        " function String() {}" +
        " String.prototype.length;", 
        "function f() { return 'x'.length; }",
        "", 
        BAD_PRIVATE_PROPERTY_ACCESS,
        null);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNullableDeprecatedProperty
  public void testNullableDeprecatedProperty() {
    testDep(
        " function Foo() {}" +
        " Foo.prototype.length;" +
        " function f(x) { return x.length; }",
        "GRR",
        DEPRECATED_PROP,
        DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNullablePrivateProperty
  public void testNullablePrivateProperty() {
    test(new String[] {
        " function Foo() {}" +
        " Foo.prototype.length;",
        " function f(x) { return x.length; }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty1
  public void testConstantProperty1() {
    test(" function A() {" +
        " this.bar = 3;}" +
        " function B() {" +
        " this.bar = 3;this.bar += 4;}",
        null, CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty2
  public void testConstantProperty2() {
    test(" function Foo() {}" +
        " Foo.prototype.prop = 2;" +
        "var foo = new Foo();" +
        "foo.prop = 3;",
        null , CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty3
  public void testConstantProperty3() {
    testSame("var o = {  x: 1 };" +
        "o.x = 2;");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty4
  public void testConstantProperty4() {
    test(" function cat(name) {}" +
        " cat.test = 1;" +
        "cat.test *= 2;",
        null, CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty5
  public void testConstantProperty5() {
    test(" function Foo() { this.prop = 1;}" +
        " Foo.prototype.prop;" +
        "Foo.prototype.prop = 2",
        null , CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty6
  public void testConstantProperty6() {
    test(" function Foo() { this.prop = 1;}" +
        " Foo.prototype.prop = 2;",
        null , CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty7
  public void testConstantProperty7() {
    testSame(" function Foo() {} " +
      "Foo.prototype.bar_ = function() {};" +
      " " +
      "function SubFoo() {};" +
      "  SubFoo.prototype.bar_ = function() {};" +
      "SubFoo.prototype.baz = function() { this.bar_(); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty8
  public void testConstantProperty8() {
    testSame("var o = {  x: 1 };" +
        "var y = o.x;");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty9
  public void testConstantProperty9() {
    testSame(" function A() {" +
        " this.bar = 3;}" +
        " function B() {" +
        "this.bar = 4;}");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty10
  public void testConstantProperty10() {
    testSame(" function Foo() { this.prop = 1;}" +
        " Foo.prototype.prop;");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty11
  public void testConstantProperty11() {
    test(" function Foo() {}" +
        " Foo.prototype.bar;" +
        " function SubFoo() { this.bar = 5; this.bar = 6; }",
        null , CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty12
  public void testConstantProperty12() {
    testSame(" function Foo() {}" +
        " Foo.prototype.bar;" +
        " function SubFoo() { this.bar = 5; }" +
        " function SubFoo2() { this.bar = 5; }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty13
  public void testConstantProperty13() {
    test(" function Foo() {}" +
        " Foo.prototype.bar;" +
        " function SubFoo() { this.bar = 5; }" +
        " function SubSubFoo() { this.bar = 5; }",
        null , CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty14
  public void testConstantProperty14() {
    test(" function Foo() {" +
        " this.bar = 3; delete this.bar; }",
        null, CONST_PROPERTY_DELETED);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testSuppressConstantProperty
  public void testSuppressConstantProperty() {
    testSame(" function A() {" +
        " this.bar = 3;}" +
        " function B() {" +
        " this.bar = 3;this.bar += 4;}");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testSuppressConstantProperty2
  public void testSuppressConstantProperty2() {
    testSame(" function A() {" +
        " this.bar = 3;}" +
        " function B() {" +
        " this.bar = 3;this.bar += 4;}");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testFinalClassCannotBeSubclassed
  public void testFinalClassCannotBeSubclassed() {
    test(
        " Foo = function() {};\n"
        + " Bar = function() {};",
        null, EXTEND_FINAL_CLASS);
    test(
        " function Foo() {};\n"
        + " function Bar() {};",
        null, EXTEND_FINAL_CLASS);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testNoEventHandler
  public void testNoEventHandler() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() {  };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testNotFreed1
  public void testNotFreed1() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { this.eh = new goog.events.EventHandler(); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js, CheckEventfulObjectDisposal.EVENTFUL_OBJECT_NOT_DISPOSED,
        true);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testLocal
  public void testLocal() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { var eh = new goog.events.EventHandler();\n"
        + "};\n"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testLocalAggressive
  public void testLocalAggressive() {
    policy = CheckEventfulObjectDisposal.DisposalCheckingPolicy.AGGRESSIVE;
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { var eh = new goog.events.EventHandler();\n"
        + "};\n"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js, CheckEventfulObjectDisposal.EVENTFUL_OBJECT_PURELY_LOCAL,
        true);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testFreedLocal1
  public void testFreedLocal1() {
    policy = CheckEventfulObjectDisposal.DisposalCheckingPolicy.AGGRESSIVE;
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { var eh = new goog.events.EventHandler();"
        + "eh.dispose(); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testEventhandlerRemoveAll1
  public void testEventhandlerRemoveAll1() {
    policy = CheckEventfulObjectDisposal.DisposalCheckingPolicy.AGGRESSIVE;
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { this.eh = new goog.events.EventHandler(); };"
        + "test.prototype.free = function() { this.eh.removeAll(); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testEventhandlerRemoveAll2
  public void testEventhandlerRemoveAll2() {
    policy = CheckEventfulObjectDisposal.DisposalCheckingPolicy.AGGRESSIVE;
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { var eh = new goog.events.EventHandler();"
        + "eh.removeAll(); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testFreedLocal2
  public void testFreedLocal2() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { var eh = new goog.events.EventHandler();"
        + "this.registerDisposable(eh); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testFreedLocal2Aggressive
  public void testFreedLocal2Aggressive() {
    policy = CheckEventfulObjectDisposal.DisposalCheckingPolicy.AGGRESSIVE;
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { var eh = new goog.events.EventHandler();"
        + "this.registerDisposable(eh); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js, CheckEventfulObjectDisposal.EVENTFUL_OBJECT_PURELY_LOCAL,
        true);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testLocalLive1
  public void testLocalLive1() {
    policy = CheckEventfulObjectDisposal.DisposalCheckingPolicy.AGGRESSIVE;
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { var eh = new goog.events.EventHandler();"
        + "this.eh = eh;"
        + "eh.dispose(); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testLocalLive2
  public void testLocalLive2() {
    policy = CheckEventfulObjectDisposal.DisposalCheckingPolicy.AGGRESSIVE;
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { var eh = new goog.events.EventHandler();"
        + "this.eh = eh;"
        + "this.eh.dispose(); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testLocalLive3
  public void testLocalLive3() {
    policy = CheckEventfulObjectDisposal.DisposalCheckingPolicy.AGGRESSIVE;
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { var eh = new goog.events.EventHandler();"
        + "this.ehs = [];"
        + "this.ehs[0] = eh;"
        + "};"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testFreedDispose
  public void testFreedDispose() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { this.eh = new goog.events.EventHandler();"
        + "this.eh.dispose(); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testFreedGoogDispose1
  public void testFreedGoogDispose1() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { this.eh = new goog.events.EventHandler();"
        + "goog.dispose(this.eh); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testNotAllFreedGoogDispose
  public void testNotAllFreedGoogDispose() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() {"
        + "this.eh1 = new goog.events.EventHandler();"
        + "this.eh2 = new goog.events.EventHandler();"
        + "goog.dispose(this.eh1, this.eh2); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js, CheckEventfulObjectDisposal.EVENTFUL_OBJECT_NOT_DISPOSED,
        true);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testFreedGoogDisposeAll
  public void testFreedGoogDisposeAll() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { "
        + "this.eh1 = new goog.events.EventHandler();"
        + "this.eh2 = new goog.events.EventHandler();"
        + "goog.disposeAll(this.eh1, this.eh2); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testFreedRegisterDisposable
  public void testFreedRegisterDisposable() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { this.eh = new goog.events.EventHandler();"
        + "this.registerDisposable(this.eh); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testFreedRemoveAll
  public void testFreedRemoveAll() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { this.eh = new goog.events.EventHandler();"
        + "this.eh.removeAll(); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testPrivateInheritance
  public void testPrivateInheritance() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { "
        + " this.eh = new goog.events.EventHandler();"
        + "this.eh.removeAll(); };"
        + "goog.inherits(test, goog.Disposable);"
        + ""
        + "var subclass = function() {"
        + " this.eh = new goog.events.EventHandler();"
        + "this.eh.dispose();"
        + "};"
        + "var testObj = new test();";
    testSame(js, CheckEventfulObjectDisposal.OVERWRITE_PRIVATE_EVENTFUL_OBJECT,
        true);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testCustomDispose1
  public void testCustomDispose1() {
    policy = CheckEventfulObjectDisposal.DisposalCheckingPolicy.AGGRESSIVE;
    String js = CLOSURE_DEFS
        + ""
        + "customDispose = function(todispose, ctx) {"
        + " ctx.registerDisposable(todispose);"
        + " return todispose;"
        + "};"
        + "var x = new goog.events.EventHandler();"
        + "customDispose(x, OBJ);";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testCustomDispose2
  public void testCustomDispose2() {
    policy = CheckEventfulObjectDisposal.DisposalCheckingPolicy.AGGRESSIVE;
    String js = CLOSURE_DEFS
        + ""
        + "customDispose = function(todispose, ctx) {"
        + " ctx.registerDisposable(todispose);"
        + " return todispose;"
        + "};"
        + "var x = new goog.events.EventHandler();"
        + "var y = new goog.events.EventHandler();"
        + "customDispose(x, y);";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testCustomDispose3
  public void testCustomDispose3() {
    policy = CheckEventfulObjectDisposal.DisposalCheckingPolicy.AGGRESSIVE;
    String js = CLOSURE_DEFS
        + ""
        + "customDispose = function(todispose, ctx) {"
        + " ctx.registerDisposable(todispose);"
        + " return todispose;"
        + "};"
        + "var x = new goog.events.EventHandler();"
        + "customDispose(OBJ, x);";
    testSame(js, CheckEventfulObjectDisposal.EVENTFUL_OBJECT_PURELY_LOCAL,
        true);
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

// com.google.javascript.jscomp.CheckMissingReturnTest::testIssue779
  public void testIssue779() {
    testNotMissing(
        "var a = f(); try { alert(); if (a > 0) return 1; }" +
        "finally { a = 5; } return 2;");
  }

// com.google.javascript.jscomp.CheckMissingReturnTest::testConstructors
  public void testConstructors() {
    testSame(" function foo() {} ");

    final String constructorWithReturn = " function foo() {" +
        " if (!(this instanceof foo)) { return new foo; } }";
    testSame(constructorWithReturn);
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
        "var a,b,c;a||(b||c);a*(b*c);a|(b|c)");
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

// com.google.javascript.jscomp.CodePrinterTest::testBreakTrustedStrings
  public void testBreakTrustedStrings() {
    
    assertPrint("'<script>'", "\"<script>\"");
    assertPrint("'</script>'", "\"\\x3c/script>\"");
    assertPrint("\"</script> </SCRIPT>\"", "\"\\x3c/script> \\x3c/SCRIPT>\"");

    assertPrint("'-->'", "\"--\\x3e\"");
    assertPrint("']]>'", "\"]]\\x3e\"");
    assertPrint("' --></script>'", "\" --\\x3e\\x3c/script>\"");

    assertPrint("/--> <\\/script>/g", "/--\\x3e <\\/script>/g");

    
    
    assertPrint("'<!-- I am a string -->'",
        "\"\\x3c!-- I am a string --\\x3e\"");

    assertPrint("'<=&>'", "\"<=&>\"");
  }

// com.google.javascript.jscomp.CodePrinterTest::testBreakUntrustedStrings
  public void testBreakUntrustedStrings() {
    trustedStrings = false;

    
    assertPrint("'<script>'", "\"\\x3cscript\\x3e\"");
    assertPrint("'</script>'", "\"\\x3c/script\\x3e\"");
    assertPrint("\"</script> </SCRIPT>\"", "\"\\x3c/script\\x3e \\x3c/SCRIPT\\x3e\"");

    assertPrint("'-->'", "\"--\\x3e\"");
    assertPrint("']]>'", "\"]]\\x3e\"");
    assertPrint("' --></script>'", "\" --\\x3e\\x3c/script\\x3e\"");

    assertPrint("/--> <\\/script>/g", "/--\\x3e <\\/script>/g");

    
    
    assertPrint("'<!-- I am a string -->'",
        "\"\\x3c!-- I am a string --\\x3e\"");

    assertPrint("'<=&>'", "\"\\x3c\\x3d\\x26\\x3e\"");
    assertPrint("/(?=x)/", "/(?=x)/");
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
    assertPrint("var a={};for (var i = (a || a) || (\"size\" in a);;);",
        "var a={};for(var i=a||a||(\"size\"in a);;);");

    
    assertPrint("var a={}; for (var i = -(\"length\" in a); i;) {}",
        "var a={};for(var i=-(\"length\"in a);i;);");
    assertPrint("var a={};function b_(p){ return p;};" +
        "for(var i=1,j=b_(\"length\" in a);;) {}",
        "var a={};function b_(p){return p}" +
            "for(var i=1,j=b_(\"length\"in a);;);");

    
    assertPrint("var a={}; for (;(\"length\" in a);) {}",
        "var a={};for(;\"length\"in a;);");

    
    assertPrintSame("for(x,(y in z);;)foo()");
    assertPrintSame("for(var x,w=(y in z);;)foo()");

    
    assertPrintSame("for(a=c?0:(0 in d);;)foo()");
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
        "if (1) {\n" +
        "}\n");
    assertPrettyPrint("if (1) {alert(\"\");}",
        "if (1) {\n" +
        "  alert(\"\");\n" +
        "}\n");
    assertPrettyPrint("if (1)alert(\"\");",
        "if (1) {\n" +
        "  alert(\"\");\n" +
        "}\n");
    assertPrettyPrint("if (1) {alert();alert();}",
        "if (1) {\n" +
        "  alert();\n" +
        "  alert();\n" +
        "}\n");

    
    assertPrettyPrint("label: alert();",
        "label: alert();\n");

    
    assertPrettyPrint("if (1) alert();",
        "if (1) {\n" +
        "  alert();\n" +
        "}\n");
    assertPrettyPrint("for (;;) alert();",
        "for (;;) {\n" +
        "  alert();\n" +
        "}\n");

    assertPrettyPrint("while (1) alert();",
        "while (1) {\n" +
        "  alert();\n" +
        "}\n");

    
    assertPrettyPrint("if (1) {} else {alert(a);}",
        "if (1) {\n" +
        "} else {\n  alert(a);\n}\n");

    
    assertPrettyPrint("if (1) alert(a); else alert(b);",
        "if (1) {\n" +
        "  alert(a);\n" +
        "} else {\n" +
        "  alert(b);\n" +
        "}\n");

    
    assertPrettyPrint("for(;;) { alert();}",
        "for (;;) {\n" +
         "  alert();\n" +
         "}\n");
    assertPrettyPrint("for(;;) {}",
        "for (;;) {\n" +
        "}\n");
    assertPrettyPrint("for(;;) { alert(); alert(); }",
        "for (;;) {\n" +
        "  alert();\n" +
        "  alert();\n" +
        "}\n");

    
    assertPrettyPrint("do { alert(); } while(true);",
        "do {\n" +
        "  alert();\n" +
        "} while (true);\n");

    
    assertPrettyPrint("myLabel: { alert();}",
        "myLabel: {\n" +
        "  alert();\n" +
        "}\n");

    
    
    assertPrettyPrint("myLabel: for(;;) continue myLabel;",
        "myLabel: for (;;) {\n" +
        "  continue myLabel;\n" +
        "}\n");

    assertPrettyPrint("var a;", "var a;\n");

    
    assertPrettyPrint("var foo = 3+5;",
        "var foo = 3 + 5;\n");

    
    assertPrettyPrint("var foo = bar ? 3 : null;",
        "var foo = bar ? 3 : null;\n");

    
    assertPrettyPrint("function foo() { return \"foo\"; }",
        "function foo() {\n  return \"foo\";\n}\n");
    assertPrettyPrint("throw \"foo\";",
        "throw \"foo\";");

    
    assertPrettyPrint("do{ alert(); } while(true);",
        "do {\n  alert();\n} while (true);\n");
    assertPrettyPrint("while(true) { alert(); }",
        "while (true) {\n  alert();\n}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrettyPrinter2
  public void testPrettyPrinter2() {
    assertPrettyPrint(
        "if(true) f();",
        "if (true) {\n" +
        "  f();\n" +
        "}\n");

    assertPrettyPrint(
        "if (true) { f() } else { g() }",
        "if (true) {\n" +
        "  f();\n" +
        "} else {\n" +
        "  g();\n" +
        "}\n");

    assertPrettyPrint(
        "if(true) f(); for(;;) g();",
        "if (true) {\n" +
        "  f();\n" +
        "}\n" +
        "for (;;) {\n" +
        "  g();\n" +
        "}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrettyPrinter3
  public void testPrettyPrinter3() {
    assertPrettyPrint(
        "try {} catch(e) {}if (1) {alert();alert();}",
        "try {\n" +
        "} catch (e) {\n" +
        "}\n" +
        "if (1) {\n" +
        "  alert();\n" +
        "  alert();\n" +
        "}\n");

    assertPrettyPrint(
        "try {} finally {}if (1) {alert();alert();}",
        "try {\n" +
        "} finally {\n" +
        "}\n" +
        "if (1) {\n" +
        "  alert();\n" +
        "  alert();\n" +
        "}\n");

    assertPrettyPrint(
        "try {} catch(e) {} finally {} if (1) {alert();alert();}",
        "try {\n" +
        "} catch (e) {\n" +
        "} finally {\n" +
        "}\n" +
        "if (1) {\n" +
        "  alert();\n" +
        "  alert();\n" +
        "}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrettyPrinter4
  public void testPrettyPrinter4() {
    assertPrettyPrint(
        "function f() {}if (1) {alert();}",
        "function f() {\n" +
        "}\n" +
        "if (1) {\n" +
        "  alert();\n" +
        "}\n");

    assertPrettyPrint(
        "var f = function() {};if (1) {alert();}",
        "var f = function() {\n" +
        "};\n" +
        "if (1) {\n" +
        "  alert();\n" +
        "}\n");

    assertPrettyPrint(
        "(function() {})();if (1) {alert();}",
        "(function() {\n" +
        "})();\n" +
        "if (1) {\n" +
        "  alert();\n" +
        "}\n");

    assertPrettyPrint(
        "(function() {alert();alert();})();if (1) {alert();}",
        "(function() {\n" +
        "  alert();\n" +
        "  alert();\n" +
        "})();\n" +
        "if (1) {\n" +
        "  alert();\n" +
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
        + "a.Foo.prototype.foo = function(foo) {\n  return 3;\n};\n"
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

// com.google.javascript.jscomp.CodePrinterTest::testU2UFunctionTypeAnnotation1
  public void testU2UFunctionTypeAnnotation1() {
    assertTypeAnnotations(
        " var x = function() {}",
        "\n" +
        "var x = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testU2UFunctionTypeAnnotation2
  public void testU2UFunctionTypeAnnotation2() {
    
    
    assertTypeAnnotations(
        " var x = function() {}",
        "\n" +
        "var x = function() {\n};\n");
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
        "  t1.prototype = t2.prototype;\n};\n"
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
        printNode(n));
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
    assertPrint("({},42)+0", "({},42)+0");
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

    languageMode = LanguageMode.ECMASCRIPT5;
    assertPrintSame("var x={get function(){return 1}}");

    
    
    languageMode = LanguageMode.ECMASCRIPT3;
    assertPrintSame("var x={get function(){return 1}}");
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

    languageMode = LanguageMode.ECMASCRIPT5;
    assertPrintSame("var x={set function(x){}}");

    
    
    languageMode = LanguageMode.ECMASCRIPT3;
    assertPrintSame("var x={set function(x){}}");
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

// com.google.javascript.jscomp.CodePrinterTest::testIssue942
  public void testIssue942() {
    assertPrint("var x = {0: 1};", "var x={0:1}");
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

// com.google.javascript.jscomp.CodePrinterTest::testKeywordProperties1
  public void testKeywordProperties1() {
    languageMode = LanguageMode.ECMASCRIPT5;
    assertPrintSame("x.foo=2");
    assertPrintSame("x.function=2");

    languageMode = LanguageMode.ECMASCRIPT3;
    assertPrintSame("x.foo=2");
    assertPrint("x.function=2", "x[\"function\"]=2");
  }

// com.google.javascript.jscomp.CodePrinterTest::testKeywordProperties2
  public void testKeywordProperties2() {
    languageMode = LanguageMode.ECMASCRIPT5;
    assertPrintSame("x={foo:2}");
    assertPrintSame("x={function:2}");

    languageMode = LanguageMode.ECMASCRIPT3;
    assertPrintSame("x={foo:2}");
    assertPrint("x={function:2}", "x={\"function\":2}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testIssue1062
  public void testIssue1062() {
    assertPrintSame("3*(4%3*5)");
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
