// buggy code
  public void initOptions(CompilerOptions options) {
    this.options = options;
    if (errorManager == null) {
      if (outStream == null) {
        setErrorManager(
            new LoggerErrorManager(createMessageFormatter(), logger));
      } else {
        PrintStreamErrorManager printer =
            new PrintStreamErrorManager(createMessageFormatter(), outStream);
        printer.setSummaryDetailLevel(options.summaryDetailLevel);
        setErrorManager(printer);
      }
    }

    // DiagnosticGroups override the plain checkTypes option.
    if (options.enables(DiagnosticGroups.CHECK_TYPES)) {
      options.checkTypes = true;
    } else if (options.disables(DiagnosticGroups.CHECK_TYPES)) {
      options.checkTypes = false;
    } else if (!options.checkTypes) {
      // If DiagnosticGroups did not override the plain checkTypes
      // option, and checkTypes is enabled, then turn off the
      // parser type warnings.
      options.setWarningLevel(
          DiagnosticGroup.forType(
              RhinoErrorReporter.TYPE_PARSE_ERROR),
          CheckLevel.OFF);
    }

    if (options.checkGlobalThisLevel.isOn()) {
      options.setWarningLevel(
          DiagnosticGroups.GLOBAL_THIS,
          options.checkGlobalThisLevel);
    }

    if (options.getLanguageIn() == LanguageMode.ECMASCRIPT5_STRICT) {
      options.setWarningLevel(
          DiagnosticGroups.ES5_STRICT,
          CheckLevel.ERROR);
    }

    // Initialize the warnings guard.
    List<WarningsGuard> guards = Lists.newArrayList();
    guards.add(
        new SuppressDocWarningsGuard(
            getDiagnosticGroups().getRegisteredGroups()));
    guards.add(options.getWarningsGuard());

    ComposeWarningsGuard composedGuards = new ComposeWarningsGuard(guards);

    // All passes must run the variable check. This synthesizes
    // variables later so that the compiler doesn't crash. It also
    // checks the externs file for validity. If you don't want to warn
    // about missing variable declarations, we shut that specific
    // error off.
    if (!options.checkSymbols &&
        !composedGuards.enables(DiagnosticGroups.CHECK_VARIABLES)) {
      composedGuards.addGuard(new DiagnosticGroupWarningsGuard(
          DiagnosticGroups.CHECK_VARIABLES, CheckLevel.OFF));
    }

    this.warningsGuard = composedGuards;
  }

// relevant test
// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends9
  public void testGoodExtends9() throws Exception {
    testTypes(
        " function Super() {}" +
        "Super.prototype.foo = function() {};" +
        " function Sub() {}" +
        "Sub.prototype = new Super();" +
        " Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends10
  public void testGoodExtends10() throws Exception {
    testTypes(
        " function Super() {}" +
        " function Sub() {}" +
        "Sub.prototype = new Super();" +
        " function foo() { return new Sub(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends11
  public void testGoodExtends11() throws Exception {
    testTypes(
        " function Super() {}" +
        " Super.prototype.foo = function(x) {};" +
        " function Sub() {}" +
        "Sub.prototype = new Super();" +
        "(new Sub()).foo(0);",
        "actual parameter 1 of Super.prototype.foo " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends12
  public void testGoodExtends12() throws Exception {
    testTypes(
        " function Sub() {}" +
        " function Sub2() {}" +
        " function Super() {}" +
        " function foo(x) {}" +
        "foo(new Sub2());");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends13
  public void testGoodExtends13() throws Exception {
    testTypes(
        " function C() {}" +
        " function E() {}" +
        " function D() {}" +
        " function B() {}" +
        " function A() {}" +
        " function f(x) {} f(new E());",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : E\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadExtends1
  public void testBadExtends1() throws Exception {
    testTypes("function base() {}\n" +
        "function derived() {}\n",
        "Bad type annotation. Unknown type not_base");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadExtends2
  public void testBadExtends2() throws Exception {
    testTypes("function base() {\n" +
        "\n" +
        "this.baseMember = new Number(4);\n" +
        "}\n" +
        "function derived() {}\n" +
        "\n" +
        "function foo(x){ }\n" +
        "var y;\n" +
        "foo(y.baseMember);\n",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : Number\n" +
        "required: String");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadExtends3
  public void testBadExtends3() throws Exception {
    testTypes("function base() {}",
        "@extends used without @constructor or @interface for base");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadExtends4
  public void testBadExtends4() throws Exception {
    
    
    testTypes(
        " function Sub() {}" +
        " function Sub2() {}" +
        " function foo(x) {}" +
        "foo(new Sub2());",
        "Bad type annotation. Unknown type bad");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLateExtends
  public void testLateExtends() throws Exception {
    testTypes(
        CLOSURE_DEFS +
        " function Foo() {}\n" +
        "Foo.prototype.foo = function() {};\n" +
        "function Bar() {}\n" +
        "goog.inherits(Foo, Bar);\n",
        "Missing @extends tag on type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperclassMatch
  public void testSuperclassMatch() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function() {};\n" +
        " var Bar = function() {};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.inherits(Foo);\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperclassMatchWithMixin
  public void testSuperclassMatchWithMixin() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function() {};\n" +
        " var Baz = function() {};\n" +
        " var Bar = function() {};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.mixin = function(y){};" +
        "Bar.inherits(Foo);\n" +
        "Bar.mixin(Baz);\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperclassMismatch1
  public void testSuperclassMismatch1() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function() {};\n" +
        " var Bar = function() {};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.inherits(Foo);\n",
        "Missing @extends tag on type Bar");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperclassMismatch2
  public void testSuperclassMismatch2() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function(){};\n" +
        " var Bar = function(){};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.inherits(Foo);",
        "Missing @extends tag on type Bar");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperClassDefinedAfterSubClass1
  public void testSuperClassDefinedAfterSubClass1() throws Exception {
    testTypes(
        " function A() {}" +
        " function B() {}" +
        " function Base() {}" +
        " " +
        "function foo(x) { return x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperClassDefinedAfterSubClass2
  public void testSuperClassDefinedAfterSubClass2() throws Exception {
    testTypes(
        " function A() {}" +
        " function B() {}" +
        " " +
        "function foo(x) { return x; }" +
        " function Base() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDirectPrototypeAssignment1
  public void testDirectPrototypeAssignment1() throws Exception {
    testTypes(
        " function Base() {}" +
        "Base.prototype.foo = 3;" +
        " function A() {}" +
        "A.prototype = new Base();" +
        " function foo() { return (new A).foo; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDirectPrototypeAssignment2
  public void testDirectPrototypeAssignment2() throws Exception {
    
    
    testTypes(
        " function Base() {}" +
        " function A() {}" +
        "A.prototype = new Base();" +
        "A.prototype.foo = 3;" +
        " function foo() { return (new Base).foo; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDirectPrototypeAssignment3
  public void testDirectPrototypeAssignment3() throws Exception {
    
    
    testTypes(
        " var MainWidgetCreator = function() {};" +
        "" +
        "function createMainWidget(ctor) {" +
        "   function tempCtor() {};" +
        "  tempCtor.prototype = ctor.prototype;" +
        "  MainWidgetCreator.superClass_ = ctor.prototype;" +
        "  MainWidgetCreator.prototype = new tempCtor();" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodImplements1
  public void testGoodImplements1() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodImplements2
  public void testGoodImplements2() throws Exception {
    testTypes("function Base1() {}\n" +
        "function Base2() {}\n" +
        " function derived() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodImplements3
  public void testGoodImplements3() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodImplements4
  public void testGoodImplements4() throws Exception {
    testTypes("var goog = {};" +
        "" +
        "goog.abstractMethod = function() {};" +
        "\n" +
        "goog.Disposable = goog.abstractMethod;" +
        "goog.Disposable.prototype.dispose = goog.abstractMethod;" +
        "" +
        "goog.SubDisposable = function() {};" +
        " " +
        "goog.SubDisposable.prototype.dispose = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplements1
  public void testBadImplements1() throws Exception {
    testTypes("function Base1() {}\n" +
        "function Base2() {}\n" +
        " function derived() {}",
        "Bad type annotation. Unknown type nonExistent");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplements2
  public void testBadImplements2() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}",
        "@implements used without @constructor or @interface for f");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplements3
  public void testBadImplements3() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.abstractMethod = function(){};" +
        " var Disposable = goog.abstractMethod;" +
        "Disposable.prototype.method = goog.abstractMethod;" +
        "function f() {}",
        "property method on interface Disposable is not implemented by type f");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplements4
  public void testBadImplements4() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}",
        "f cannot implement this type; an interface can only extend, " +
        "but not implement interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceExtends
  public void testInterfaceExtends() throws Exception {
    testTypes("function A() {}\n" +
        "function B() {}\n" +
        " function derived() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadInterfaceExtends1
  public void testBadInterfaceExtends1() throws Exception {
    testTypes("function A() {}",
        "Bad type annotation. Unknown type nonExistent");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadInterfaceExtends2
  public void testBadInterfaceExtends2() throws Exception {
    testTypes("function A() {}\n" +
        "function B() {}",
        "B cannot extend this type; a constructor can only extend objects " +
        "and an interface can only extend interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadInterfaceExtends3
  public void testBadInterfaceExtends3() throws Exception {
    testTypes("function A() {}\n" +
        "function B() {}",
        "B cannot extend this type; a constructor can only extend objects " +
        "and an interface can only extend interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadInterfaceExtends4
  public void testBadInterfaceExtends4() throws Exception {
    
    
    
    testTypes("function A() {}\n" +
        "function B() {}\n" +
        "B.prototype = A;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadInterfaceExtends5
  public void testBadInterfaceExtends5() throws Exception {
    
    
    
    testTypes("function A() {}\n" +
        "function B() {}\n" +
        "B.prototype = A;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplementsAConstructor
  public void testBadImplementsAConstructor() throws Exception {
    testTypes("function A() {}\n" +
        "function B() {}",
        "can only implement interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplementsNonInterfaceType
  public void testBadImplementsNonInterfaceType() throws Exception {
    testTypes("function B() {}",
        "can only implement interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplementsNonObjectType
  public void testBadImplementsNonObjectType() throws Exception {
    testTypes("function S() {}",
        "can only implement interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment1
  public void testInterfaceAssignment1() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment2
  public void testInterfaceAssignment2() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;",
        "initializing variable\n" +
        "found   : T\n" +
        "required: I");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment3
  public void testInterfaceAssignment3() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment4
  public void testInterfaceAssignment4() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment5
  public void testInterfaceAssignment5() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i1 = t;\n" +
        "var i2 = t;\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment6
  public void testInterfaceAssignment6() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "var i1 = new T();\n" +
        "var i2 = i1;\n",
        "initializing variable\n" +
        "found   : I1\n" +
        "required: I2");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment7
  public void testInterfaceAssignment7() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i1 = t;\n" +
        "var i2 = t;\n" +
        "i1 = i2;\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment8
  public void testInterfaceAssignment8() throws Exception {
    testTypes("var I = function() {};\n" +
        "var i;\n" +
        "var o = i;\n" +
        "new Object().prototype = i.prototype;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment9
  public void testInterfaceAssignment9() throws Exception {
    testTypes("var I = function() {};\n" +
        "function f() { return null; }\n" +
        "var i = f();\n",
        "initializing variable\n" +
        "found   : (I|null)\n" +
        "required: I");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment10
  public void testInterfaceAssignment10() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "function f() { return new T(); }\n" +
        "var i1 = f();\n",
        "initializing variable\n" +
        "found   : (I1|I2)\n" +
        "required: I1");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment11
  public void testInterfaceAssignment11() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "function f() { return new T(); }\n" +
        "var i1 = f();\n",
        "initializing variable\n" +
        "found   : (I1|I2|T)\n" +
        "required: I1");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment12
  public void testInterfaceAssignment12() throws Exception {
    testTypes("var I = function() {};\n" +
              "var T1 = function() {};\n" +
              "var T2 = function() {};\n" +
              "function f() { return new T2(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment13
  public void testInterfaceAssignment13() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "function Super() {};\n" +
        "Super.prototype.foo = " +
        "function() { return new T(); };\n" +
        "function Sub() {}\n" +
        "Sub.prototype.foo = " +
        "function() { return new T(); };\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetprop1
  public void testGetprop1() throws Exception {
    testTypes("function foo(){foo().bar;}",
        "undefined has no properties\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetprop2
  public void testGetprop2() throws Exception {
    testTypes("var x = null; x.alert();",
        "null has no properties\n" +
        "found   : null\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetprop3
  public void testGetprop3() throws Exception {
    testTypes(
        " " +
        "function Foo() {  this.x = null; }" +
        "Foo.prototype.initX = function() { this.x = {foo: 1}; };" +
        "Foo.prototype.bar = function() {" +
        "  if (this.x == null) { this.initX(); alert(this.x.foo); }" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess1
  public void testArrayAccess1() throws Exception {
    testTypes("var a = []; var b = a['hi'];");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess2
  public void testArrayAccess2() throws Exception {
    testTypes("var a = []; var b = a[[1,2]];",
        "array access\n" +
        "found   : Array\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess3
  public void testArrayAccess3() throws Exception {
    testTypes("var bar = [];" +
        "function baz(){};" +
        "var foo = bar[baz()];",
        "array access\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess4
  public void testArrayAccess4() throws Exception {
    testTypes("function foo(){};var bar = foo()[foo()];",
        "array access\n" +
        "found   : Array\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess6
  public void testArrayAccess6() throws Exception {
    testTypes("var bar = null[1];",
        "only arrays or objects can be accessed\n" +
        "found   : null\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess7
  public void testArrayAccess7() throws Exception {
    testTypes("var bar = void 0; bar[0];",
        "only arrays or objects can be accessed\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess8
  public void testArrayAccess8() throws Exception {
    
    
    testTypes("var bar = void 0; bar[0]; bar[1];",
        "only arrays or objects can be accessed\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess9
  public void testArrayAccess9() throws Exception {
    testTypes(" function f() { return []; }" +
        "f()[{}]",
        "array access\n" +
        "found   : {}\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropAccess
  public void testPropAccess() throws Exception {
    testTypes("var f = function(x) {\n" +
        "var o = String(x);\n" +
        "if (typeof o['a'] != 'undefined') { return o['a']; }\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropAccess2
  public void testPropAccess2() throws Exception {
    testTypes("var bar = void 0; bar.baz;",
        "undefined has no properties\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropAccess3
  public void testPropAccess3() throws Exception {
    
    
    testTypes("var bar = void 0; bar.baz; bar.bax;",
        "undefined has no properties\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropAccess4
  public void testPropAccess4() throws Exception {
    testTypes(" function f(x) { return x['hi']; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase1
  public void testSwitchCase1() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "switch(a){case b:;}",
        "case expression doesn't match switch\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase2
  public void testSwitchCase2() throws Exception {
    testTypes("var a = null; switch (typeof a) { case 'foo': }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar1
  public void testVar1() throws Exception {
    TypeCheckResult p =
        parseAndTypeCheckWithScope("var a = null");

    assertEquals(createUnionType(STRING_TYPE, NULL_TYPE),
        p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar2
  public void testVar2() throws Exception {
    testTypes(" var a = function(){}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar3
  public void testVar3() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = 3;");

    assertEquals(NUMBER_TYPE, p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar4
  public void testVar4() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var a = 3; a = 'string';");

    assertEquals(createUnionType(STRING_TYPE, NUMBER_TYPE),
        p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar5
  public void testVar5() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 'hello';" +
        "var a = goog.foo;",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar6
  public void testVar6() throws Exception {
    testTypes(
        "function f() {" +
        "  return function() {" +
        "    " +
        "    var a = 7;" +
        "  };" +
        "}",
        "initializing variable\n" +
        "found   : number\n" +
        "required: Date");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar7
  public void testVar7() throws Exception {
    testTypes("var a, b;",
        "declaration of multiple variables with shared type information");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar8
  public void testVar8() throws Exception {
    testTypes("var a, b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar9
  public void testVar9() throws Exception {
    testTypes("var a;",
        "enum initializer must be an object literal or an enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar10
  public void testVar10() throws Exception {
    testTypes("var foo = 'abc';",
        "initializing variable\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar11
  public void testVar11() throws Exception {
    testTypes("var foo = 'abc';",
        "initializing variable\n" +
        "found   : string\n" +
        "required: Date");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar12
  public void testVar12() throws Exception {
    testTypes("var foo = 'abc', " +
        "bar = 5;",
        new String[] {
        "initializing variable\n" +
        "found   : string\n" +
        "required: Date",
        "initializing variable\n" +
        "found   : number\n" +
        "required: RegExp"});
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar13
  public void testVar13() throws Exception {
    
    testTypes("var a,a;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar14
  public void testVar14() throws Exception {
    testTypes(" function f() { var x; return x; }",
        "inconsistent return type\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar15
  public void testVar15() throws Exception {
    testTypes("" +
        "function f() { var x = x || {}; return x; }",
        "inconsistent return type\n" +
        "found   : {}\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAssign1
  public void testAssign1() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 'hello';",
        "assignment to property foo of goog\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAssign2
  public void testAssign2() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 3;" +
        "goog.foo = 'hello';",
        "assignment to property foo of goog\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAssign3
  public void testAssign3() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 3;" +
        "goog.foo = 4;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAssign4
  public void testAssign4() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 3;" +
        "goog.foo = 'hello';");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAssignInference
  public void testAssignInference() throws Exception {
    testTypes(
        "" +
        "function f(x) {" +
        "  var y = null;" +
        "  y = x[0];" +
        "  if (y == null) { return 4; } else { return 6; }" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOr1
  public void testOr1() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a + b || undefined;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOr2
  public void testOr2() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "var c = a + b || undefined;",
        "initializing variable\n" +
        "found   : (number|undefined)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOr3
  public void testOr3() throws Exception {
    testTypes("var a;" +
        "var c = a || 3;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOr4
  public void testOr4() throws Exception {
     testTypes("var x;x=null || \"a\";",
         "assignment\n" +
         "found   : string\n" +
         "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOr5
  public void testOr5() throws Exception {
     testTypes("var x;x=undefined || \"a\";",
         "assignment\n" +
         "found   : string\n" +
         "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnd1
  public void testAnd1() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a + b && undefined;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnd2
  public void testAnd2() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "var c = a + b && undefined;",
        "initializing variable\n" +
        "found   : (number|undefined)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnd3
  public void testAnd3() throws Exception {
    testTypes("var a;" +
        "var c = a && undefined;",
        "initializing variable\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnd4
  public void testAnd4() throws Exception {
    testTypes("function f(x){};\n" +
        "var x; var y;\n" +
        "if (x && y) { f(y) }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnd5
  public void testAnd5() throws Exception {
    testTypes("function f(x,y){};\n" +
        "var x; var y;\n" +
        "if (x && y) { f(x, y) }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnd6
  public void testAnd6() throws Exception {
    testTypes("function f(x){};\n" +
        "var x;\n" +
        "if (x && f(x)) { f(x) }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnd7
  public void testAnd7() throws Exception {
    
    
    
    
    testTypes("var x; if (x && x) {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHook
  public void testHook() throws Exception {
    testTypes("function foo(){ var x=foo()?a:b; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHookRestrictsType1
  public void testHookRestrictsType1() throws Exception {
    testTypes("" +
        "function f() { return null;}" +
        " var a = f();" +
        "" +
        "var b = a ? a : 'default';");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHookRestrictsType2
  public void testHookRestrictsType2() throws Exception {
    testTypes("" +
        "var a = null;" +
        "" +
        "var b = a ? null : a;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHookRestrictsType3
  public void testHookRestrictsType3() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = (!a) ? a : null;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHookRestrictsType4
  public void testHookRestrictsType4() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = a != null ? a : true;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHookRestrictsType5
  public void testHookRestrictsType5() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = a == null ? a : undefined;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHookRestrictsType6
  public void testHookRestrictsType6() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = a == null ? 5 : a;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHookRestrictsType7
  public void testHookRestrictsType7() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = a == undefined ? 5 : a;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWhileRestrictsType1
  public void testWhileRestrictsType1() throws Exception {
    testTypes(" function g(x) {}" +
        "\n" +
        "function f(x) {\n" +
        "while (x) {\n" +
        "if (g(x)) { x = 1; }\n" +
        "x = x-1;\n}\n}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : number\n" +
        "required: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWhileRestrictsType2
  public void testWhileRestrictsType2() throws Exception {
    testTypes("\n" +
        "function f(x) {\nvar y = 0;" +
        "while (x) {\n" +
        "y = x;\n" +
        "x = x-1;\n}\n" +
        "return y;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHigherOrderFunctions1
  public void testHigherOrderFunctions1() throws Exception {
    testTypes(
        "var f;" +
        "f(true);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHigherOrderFunctions2
  public void testHigherOrderFunctions2() throws Exception {
    testTypes(
        "var f;" +
        "var a = f();",
        "initializing variable\n" +
        "found   : Date\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHigherOrderFunctions3
  public void testHigherOrderFunctions3() throws Exception {
    testTypes(
        "var f; new f",
        "cannot instantiate non-constructor");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHigherOrderFunctions4
  public void testHigherOrderFunctions4() throws Exception {
    testTypes(
        "var f; new f",
        "cannot instantiate non-constructor");
  }

// com.google.javascript.jscomp.TypeCheckTest::testHigherOrderFunctions5
  public void testHigherOrderFunctions5() throws Exception {
    testTypes(
        " function g(x) {}" +
        " var f;" +
        "g(new f());",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : Error\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias1
  public void testConstructorAlias1() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " Foo.prototype.bar = 3;" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return (new FooAlias()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias2
  public void testConstructorAlias2() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " var FooAlias = Foo;" +
        " FooAlias.prototype.bar = 3;" +
        " function foo() { " +
        "  return (new Foo()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias3
  public void testConstructorAlias3() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " Foo.prototype.bar = 3;" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return (new FooAlias()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias4
  public void testConstructorAlias4() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        "var FooAlias = Foo;" +
        " FooAlias.prototype.bar = 3;" +
        " function foo() { " +
        "  return (new Foo()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias5
  public void testConstructorAlias5() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return new Foo(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias6
  public void testConstructorAlias6() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return new FooAlias(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias7
  public void testConstructorAlias7() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Foo = function() {};" +
        " goog.FooAlias = goog.Foo;" +
        " function foo() { " +
        "  return new goog.FooAlias(); }",
        "inconsistent return type\n" +
        "found   : goog.Foo\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias8
  public void testConstructorAlias8() throws Exception {
    testTypes(
        "var goog = {};" +
        " " +
        "goog.Foo = function(x) {};" +
        " " +
        "goog.FooAlias = goog.Foo;" +
        " function foo() { " +
        "  return new goog.FooAlias(1); }",
        "inconsistent return type\n" +
        "found   : goog.Foo\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias9
  public void testConstructorAlias9() throws Exception {
    testTypes(
        "var goog = {};" +
        " " +
        "goog.Foo = function(x) {};" +
        " goog.FooAlias = goog.Foo;" +
        " function foo() { " +
        "  return new goog.FooAlias(1); }",
        "inconsistent return type\n" +
        "found   : goog.Foo\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorAlias10
  public void testConstructorAlias10() throws Exception {
    testTypes(
        " " +
        "var Foo = function(x) {};" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return new FooAlias(1); }",
        "inconsistent return type\n" +
        "found   : Foo\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testClosure1
  public void testClosure1() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = goog.isDef(a) ? a : 'default';",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testClosure2
  public void testClosure2() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = goog.isNull(a) ? 'default' : a;",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testClosure3
  public void testClosure3() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = goog.isDefAndNotNull(a) ? a : 'default';",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testClosure4
  public void testClosure4() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = !goog.isDef(a) ? 'default' : a;",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testClosure5
  public void testClosure5() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = !goog.isNull(a) ? a : 'default';",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testClosure6
  public void testClosure6() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = !goog.isDefAndNotNull(a) ? 'default' : a;",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testClosure7
  public void testClosure7() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "goog.asserts = {};" +
        " goog.asserts.assert = function(x) { return x; };" +
        " var a = foo();" +
        "" +
        "var b = goog.asserts.assert(a);",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReturn1
  public void testReturn1() throws Exception {
    testTypes("function foo(){ return 3; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReturn2
  public void testReturn2() throws Exception {
    testTypes("function foo(){ return; }",
        "inconsistent return type\n" +
        "found   : undefined\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReturn3
  public void testReturn3() throws Exception {
    testTypes("function foo(){ return 'abc'; }",
        "inconsistent return type\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReturn4
  public void testReturn4() throws Exception {
    testTypes("\n function a(){return new Array();}",
        "inconsistent return type\n" +
        "found   : Array\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReturn5
  public void testReturn5() throws Exception {
    testTypes("function n(n){return};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReturn6
  public void testReturn6() throws Exception {
    testTypes(
        "" +
        "function a(opt_a) { return opt_a }",
        "inconsistent return type\n" +
        "found   : (number|undefined)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReturn7
  public void testReturn7() throws Exception {
    testTypes("var A = function() {};\n" +
        "var B = function() {};\n" +
        "A.f = function() { return 1; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: B");
  }

// com.google.javascript.jscomp.TypeCheckTest::testReturn8
  public void testReturn8() throws Exception {
    testTypes("var A = function() {};\n" +
        "var B = function() {};\n" +
        "A.prototype.f = function() { return 1; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: B");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredReturn1
  public void testInferredReturn1() throws Exception {
    testTypes(
        "function f() {}  function g(x) {}" +
        "g(f());",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredReturn2
  public void testInferredReturn2() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() {}; " +
        " function g(x) {}" +
        "g((new Foo()).bar());",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredReturn3
  public void testInferredReturn3() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = function() {}; " +
        " function SubFoo() {}" +
        " " +
        "SubFoo.prototype.bar = function() { return 3; }; ",
        "mismatch of the bar property type and the type of the property " +
        "it overrides from superclass Foo\n" +
        "original: function (this:Foo): undefined\n" +
        "override: function (this:SubFoo): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredReturn4
  public void testInferredReturn4() throws Exception {
    
    
    
    testTypes(
        "var x = function() {};" +
        "x =  (function() { return 3; });",
        "assignment\n" +
        "found   : function (): number\n" +
        "required: function (): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredReturn5
  public void testInferredReturn5() throws Exception {
    
    testTypes(
        "" +
        "function f() {" +
        "  var x = function() {};" +
        "  x =  (function() { return 3; });" +
        "  return x();" +
        "}",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredReturn6
  public void testInferredReturn6() throws Exception {
    testTypes(
        "" +
        "function f() {" +
        "  var x = function() {};" +
        "  if (f()) " +
        "    x =  " +
        "        (function() { return 3; });" +
        "  return x();" +
        "}",
        "inconsistent return type\n" +
        "found   : (number|undefined)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredReturn7
  public void testInferredReturn7() throws Exception {
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        "Foo.prototype.bar = function(x) { return 3; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredReturn8
  public void testInferredReturn8() throws Exception {
    reportMissingOverrides = CheckLevel.OFF;
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        " function SubFoo() {}" +
        " SubFoo.prototype.bar = " +
        "    function(x) { return 3; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredParam1
  public void testInferredParam1() throws Exception {
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        " function f(x) {}" +
        "Foo.prototype.bar = function(y) { f(y); };",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredParam2
  public void testInferredParam2() throws Exception {
    reportMissingOverrides = CheckLevel.OFF;
    testTypes(
        " function f(x) {}" +
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        " function SubFoo() {}" +
        " SubFoo.prototype.bar = " +
        "    function(x) { f(x); }",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredParam3
  public void testInferredParam3() throws Exception {
    reportMissingOverrides = CheckLevel.OFF;
    testTypes(
        " function f(x) {}" +
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        " function SubFoo() {}" +
        " SubFoo.prototype.bar = " +
        "    function(x) { f(x); }; (new SubFoo()).bar();",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : (number|undefined)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredParam4
  public void testInferredParam4() throws Exception {
    reportMissingOverrides = CheckLevel.OFF;
    testTypes(
        " function f(x) {}" +
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        " function SubFoo() {}" +
        " SubFoo.prototype.bar = " +
        "    function(x) { f(x); }; (new SubFoo()).bar();",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : (number|undefined)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredParam5
  public void testInferredParam5() throws Exception {
    reportMissingOverrides = CheckLevel.OFF;
    testTypes(
        " function f(x) {}" +
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        " function SubFoo() {}" +
        " " +
        "SubFoo.prototype.bar = " +
        "    function(x, y) { f(x); }; (new SubFoo()).bar();",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : (number|undefined)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInferredParam6
  public void testInferredParam6() throws Exception {
    reportMissingOverrides = CheckLevel.OFF;
    testTypes(
        " function f(x) {}" +
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        " function SubFoo() {}" +
        " " +
        "SubFoo.prototype.bar = " +
        "    function(x, y) { f(y); };",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : (number|undefined)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenReturn1
  public void testOverriddenReturn1() throws Exception {
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = " +
        "    function() { return {}; };" +
        " function SubFoo() {}" +
        " SubFoo.prototype.bar = " +
        "    function() { return new Foo(); }",
        "inconsistent return type\n" +
        "found   : Foo\n" +
        "required: (SubFoo|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenReturn2
  public void testOverriddenReturn2() throws Exception {
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = " +
        "    function() { return new SubFoo(); };" +
        " function SubFoo() {}" +
        " SubFoo.prototype.bar = " +
        "    function() { return new SubFoo(); }",
        "mismatch of the bar property type and the type of the " +
        "property it overrides from superclass Foo\n" +
        "original: function (this:Foo): (SubFoo|null)\n" +
        "override: function (this:SubFoo): (Foo|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis1
  public void testThis1() throws Exception {
    testTypes("var goog = {};" +
        "goog.A = function(){};" +
        "" +
        "goog.A.prototype.n = function() { return this };",
        "inconsistent return type\n" +
        "found   : goog.A\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis2
  public void testThis2() throws Exception {
    testTypes("var goog = {};" +
        "goog.A = function(){" +
        "  this.foo = null;" +
        "};" +
        "" +
        "goog.A.prototype.n = function() { return this.foo };",
        "inconsistent return type\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis3
  public void testThis3() throws Exception {
    testTypes("var goog = {};" +
        "goog.A = function(){" +
        "  this.foo = null;" +
        "  this.foo = 5;" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis4
  public void testThis4() throws Exception {
    testTypes("var goog = {};" +
        "goog.A = function(){" +
        "  this.foo = null;" +
        "};" +
        "goog.A.prototype.n = function() {" +
        "  return this.foo };",
        "inconsistent return type\n" +
        "found   : (null|string)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis5
  public void testThis5() throws Exception {
    testTypes("function h() { return this }",
        "inconsistent return type\n" +
        "found   : Date\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis6
  public void testThis6() throws Exception {
    testTypes("var goog = {};" +
        "" +
        "goog.A = function(){ return this };",
        "inconsistent return type\n" +
        "found   : goog.A\n" +
        "required: Date");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis7
  public void testThis7() throws Exception {
    testTypes("function A(){};" +
        "A.prototype.n = function() { return this };",
        "inconsistent return type\n" +
        "found   : A\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis8
  public void testThis8() throws Exception {
    testTypes("function A(){" +
        "  this.foo = null;" +
        "};" +
        "A.prototype.n = function() {" +
        "  return this.foo };",
        "inconsistent return type\n" +
        "found   : (null|string)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis9
  public void testThis9() throws Exception {
    
    testTypes("function A(){};" +
        "A.prototype.foo = 3;" +
        " A.bar = function() { return this.foo; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis10
  public void testThis10() throws Exception {
    
    testTypes("function A(){};" +
        "A.prototype.foo = 3;" +
        "" +
        "A.bar = function() { return this.foo; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis11
  public void testThis11() throws Exception {
    testTypes(
        " function f(x) {}" +
        " function Ctor() {" +
        "  " +
        "  this.method = function() {" +
        "    f(this);" +
        "  };" +
        "}",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : Date\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThisTypeOfFunction1
  public void testThisTypeOfFunction1() throws Exception {
    testTypes(
        " function f() {}" +
        "f();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThisTypeOfFunction2
  public void testThisTypeOfFunction2() throws Exception {
    testTypes(
        " function F() {}" +
        " function f() {}" +
        "f();",
        "\"function (this:F): ?\" must be called with a \"this\" type");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThisTypeOfFunction3
  public void testThisTypeOfFunction3() throws Exception {
    testTypes(
        " function F() {}" +
        "F.prototype.bar = function() {};" +
        "var f = (new F()).bar; f();",
        "\"function (this:F): undefined\" must be called with a \"this\" type");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThisTypeOfFunction4
  public void testThisTypeOfFunction4() throws Exception {
    testTypes(
        " function F() {}" +
        "F.prototype.moveTo = function(x, y) {};" +
        "F.prototype.lineTo = function(x, y) {};" +
        "function demo() {" +
        "  var path = new F();" +
        "  var points = [[1,1], [2,2]];" +
        "  for (var i = 0; i < points.length; i++) {" +
        "    (i == 0 ? path.moveTo : path.lineTo)(" +
        "       points[i][0], points[i][1]);" +
        "  }" +
        "}",
        "\"function (this:F, ?, ?): undefined\" " +
        "must be called with a \"this\" type");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGlobalThis1
  public void testGlobalThis1() throws Exception {
    testTypes(" function Window() {}" +
        " " +
        "Window.prototype.alert = function(msg) {};" +
        "this.alert(3);",
        "actual parameter 1 of Window.prototype.alert " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGlobalThis2
  public void testGlobalThis2() throws Exception {
    
    testTypes(" function Bindow() {}" +
        " " +
        "Bindow.prototype.alert = function(msg) {};" +
        "this.alert = 3;" +
        "(new Bindow()).alert(this.alert)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGlobalThis2b
  public void testGlobalThis2b() throws Exception {
    testTypes(" function Bindow() {}" +
        " " +
        "Bindow.prototype.alert = function(msg) {};" +
        " this.alert = function() { return 3; };" +
        "(new Bindow()).alert(this.alert())",
        "actual parameter 1 of Bindow.prototype.alert " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGlobalThis3
  public void testGlobalThis3() throws Exception {
    testTypes(
        " " +
        "function alert(msg) {};" +
        "this.alert(3);",
        "actual parameter 1 of global this.alert " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGlobalThis4
  public void testGlobalThis4() throws Exception {
    testTypes(
        " " +
        "var alert = function(msg) {};" +
        "this.alert(3);",
        "actual parameter 1 of global this.alert " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGlobalThis5
  public void testGlobalThis5() throws Exception {
    testTypes(
        "function f() {" +
        "   " +
        "  var alert = function(msg) {};" +
        "}" +
        "this.alert(3);",
        "Property alert never defined on global this");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGlobalThis6
  public void testGlobalThis6() throws Exception {
    testTypes(
        " " +
        "var alert = function(msg) {};" +
        "var x = 3;" +
        "x = 'msg';" +
        "this.alert(this.x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGlobalThis7
  public void testGlobalThis7() throws Exception {
    testTypes(
        " function Window() {}" +
        " " +
        "var foo = function(msg) {};" +
        "foo(this);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGlobalThis8
  public void testGlobalThis8() throws Exception {
    testTypes(
        " function Window() {}" +
        " " +
        "var foo = function(msg) {};" +
        "foo(this);",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : global this\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGlobalThis9
  public void testGlobalThis9() throws Exception {
    testTypes(
        
        
        "function Window() {}" +
        "Window.prototype.alert = function() {};" +
        "this.alert();",
        "Property alert never defined on global this");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType1
  public void testControlFlowRestrictsType1() throws Exception {
    testTypes(" function f() { return null; }" +
        " var a = f();" +
        " var b = new String('foo');" +
        " var c = null;" +
        "if (a) {" +
        "  b = a;" +
        "} else {" +
        "  c = a;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType2
  public void testControlFlowRestrictsType2() throws Exception {
    testTypes(" function f() { return null; }" +
        " var a = f();" +
        " var b = 'foo';" +
        " var c = null;" +
        "if (a) {" +
        "  b = a;" +
        "} else {" +
        "  c = a;" +
        "}",
        "assignment\n" +
        "found   : (null|string)\n" +
        "required: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType3
  public void testControlFlowRestrictsType3() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = 'foo';" +
        "if (a) {" +
        "  b = a;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType4
  public void testControlFlowRestrictsType4() throws Exception {
    testTypes(" function f(a){}" +
        " var a;" +
        "a && f(a);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType5
  public void testControlFlowRestrictsType5() throws Exception {
    testTypes(" function f(a){}" +
        " var a;" +
        "a || f(a);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType6
  public void testControlFlowRestrictsType6() throws Exception {
    testTypes(" function f(x) {}" +
        " var a;" +
        "a && f(a);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType7
  public void testControlFlowRestrictsType7() throws Exception {
    testTypes(" function f(x) {}" +
        " var a;" +
        "a && f(a);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType8
  public void testControlFlowRestrictsType8() throws Exception {
    testTypes(" function f(a){}" +
        " var a;" +
        "if (a || f(a)) {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType9
  public void testControlFlowRestrictsType9() throws Exception {
    testTypes("\n" +
        "var f = function(x) {\n" +
        "if (!x || x == 1) { return 1; } else { return x; }\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType10
  public void testControlFlowRestrictsType10() throws Exception {
    
    
    testTypes(" function f(x) {}" +
        "function g() {" +
        "  var y = null;" +
        "  for (var i = 0; i < 10; i++) {" +
        "    f(y);" +
        "    if (y != null) {" +
        "      
        "    } else {" +
        "      y = {};" +
        "    }" +
        "  }" +
        "};",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : (null|{})\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testControlFlowRestrictsType11
  public void testControlFlowRestrictsType11() throws Exception {
    testTypes(" function f(x) {}" +
        "function g() {" +
        "  var y = null;" +
        "  if (y != null) {" +
        "    for (var i = 0; i < 10; i++) {" +
        "      f(y);" +
        "    }" +
        "  }" +
        "};",
        "condition always evaluates to false\n" +
        "left : null\n" +
        "right: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase3
  public void testSwitchCase3() throws Exception {
    testTypes("" +
        "var a = new String('foo');" +
        "switch (a) { case 'A': }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase4
  public void testSwitchCase4() throws Exception {
    testTypes("" +
        "var a = 'foo';" +
        "switch (a) { case 'A':break; case null:break; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase5
  public void testSwitchCase5() throws Exception {
    testTypes("" +
        "var a = new String('foo');" +
        "switch (a) { case 'A':break; case null:break; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase6
  public void testSwitchCase6() throws Exception {
    testTypes("" +
        "var a = new Number(5);" +
        "switch (a) { case 5:break; case null:break; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase7
  public void testSwitchCase7() throws Exception {
    
    testTypes(
        "\n" +
        "function g(x) { return 5; }" +
        "function f() {" +
        "  var x = {};" +
        "  x.foo = '3';" +
        "  switch (3) { case g(x.foo): return 3; }" +
        "}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase8
  public void testSwitchCase8() throws Exception {
    
    testTypes(
        "\n" +
        "function g(x) { return 5; }" +
        "function f() {" +
        "  var x = {};" +
        "  x.foo = '3';" +
        "  switch (g(x.foo)) { case 3: return 3; }" +
        "}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck1
  public void testNoTypeCheck1() throws Exception {
    testTypes("function foo() { new 4 }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck2
  public void testNoTypeCheck2() throws Exception {
    testTypes("var foo = function() { new 4 }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck3
  public void testNoTypeCheck3() throws Exception {
    testTypes("var foo = function bar() { new 4 }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck4
  public void testNoTypeCheck4() throws Exception {
    testTypes("var foo;" +
        "foo = function() { new 4 }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck5
  public void testNoTypeCheck5() throws Exception {
    testTypes("var foo;" +
        "foo = function() { new 4 }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck6
  public void testNoTypeCheck6() throws Exception {
    testTypes("var foo;" +
        "foo = function bar() { new 4 }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck7
  public void testNoTypeCheck7() throws Exception {
    testTypes("var foo;" +
        "foo = function bar() { new 4 }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck8
  public void testNoTypeCheck8() throws Exception {
    testTypes(" var foo;" +
        "var bar = 3;  function f(x) {} f(bar);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck9
  public void testNoTypeCheck9() throws Exception {
    testTypes(" function g() { }" +
        "  var a = 1",
        "initializing variable\n" +
        "found   : number\n" +
        "required: string"
        );
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck10
  public void testNoTypeCheck10() throws Exception {
    testTypes(" function g() { }" +
        " function h() { var a = 1}",
        "initializing variable\n" +
        "found   : number\n" +
        "required: string"
        );
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck11
  public void testNoTypeCheck11() throws Exception {
    testTypes(" function g() { }" +
        " function h() { var a = 1}"
        );
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck12
  public void testNoTypeCheck12() throws Exception {
    testTypes(" function g() { }" +
        "function h() { var a = 1}"
        );
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck13
  public void testNoTypeCheck13() throws Exception {
    testTypes(" function g() { }" +
        "function h() { var a = 1;" +
        " var b = 1}",
        "initializing variable\n" +
        "found   : number\n" +
        "required: string"
        );
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoTypeCheck14
  public void testNoTypeCheck14() throws Exception {
    testTypes(" function g() { }" +
        "g(1,2,3)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testImplicitCast
  public void testImplicitCast() throws Exception {
    testTypes(" function Element() {};\n" +
             "" +
             "Element.prototype.innerHTML;",
             "(new Element).innerHTML = new Array();", null, false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testImplicitCastSubclassAccess
  public void testImplicitCastSubclassAccess() throws Exception {
    testTypes(" function Element() {};\n" +
             "" +
             "Element.prototype.innerHTML;" +
             "" +
             "function DIVElement() {};",
             "(new DIVElement).innerHTML = new Array();",
             null, false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testImplicitCastNotInExterns
  public void testImplicitCastNotInExterns() throws Exception {
    testTypes(" function Element() {};\n" +
             "" +
             "Element.prototype.innerHTML;" +
             "(new Element).innerHTML = new Array();",
             new String[] {
               "Illegal annotation on innerHTML. @implicitCast may only be " +
               "used in externs.",
               "assignment to property innerHTML of Element\n" +
               "found   : Array\n" +
               "required: string"});
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumberNode
  public void testNumberNode() throws Exception {
    Node n = typeCheck(Node.newNumber(0));

    assertEquals(NUMBER_TYPE, n.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringNode
  public void testStringNode() throws Exception {
    Node n = typeCheck(Node.newString("hello"));

    assertEquals(STRING_TYPE, n.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanNodeTrue
  public void testBooleanNodeTrue() throws Exception {
    Node trueNode = typeCheck(new Node(Token.TRUE));

    assertEquals(BOOLEAN_TYPE, trueNode.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanNodeFalse
  public void testBooleanNodeFalse() throws Exception {
    Node falseNode = typeCheck(new Node(Token.FALSE));

    assertEquals(BOOLEAN_TYPE, falseNode.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testUndefinedNode
  public void testUndefinedNode() throws Exception {
    Node p = new Node(Token.ADD);
    Node n = Node.newString(Token.NAME, "undefined");
    p.addChildToBack(n);
    p.addChildToBack(Node.newNumber(5));
    typeCheck(p);

    assertEquals(VOID_TYPE, n.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumberAutoboxing
  public void testNumberAutoboxing() throws Exception {
    testTypes("var a = 4;",
        "initializing variable\n" +
        "found   : number\n" +
        "required: (Number|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumberUnboxing
  public void testNumberUnboxing() throws Exception {
    testTypes("var a = new Number(4);",
        "initializing variable\n" +
        "found   : Number\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringAutoboxing
  public void testStringAutoboxing() throws Exception {
    testTypes("var a = 'hello';",
        "initializing variable\n" +
        "found   : string\n" +
        "required: (String|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringUnboxing
  public void testStringUnboxing() throws Exception {
    testTypes("var a = new String('hello');",
        "initializing variable\n" +
        "found   : String\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanAutoboxing
  public void testBooleanAutoboxing() throws Exception {
    testTypes("var a = true;",
        "initializing variable\n" +
        "found   : boolean\n" +
        "required: (Boolean|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanUnboxing
  public void testBooleanUnboxing() throws Exception {
    testTypes("var a = new Boolean(false);",
        "initializing variable\n" +
        "found   : Boolean\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue86
  public void testIssue86() throws Exception {
    testTypes(
        " function I() {}" +
        " I.prototype.get = function(){};" +
        " function F() {}" +
        " F.prototype.get = function() { return true; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue124
  public void testIssue124() throws Exception {
    testTypes(
        "var t = null;" +
        "function test() {" +
        "  if (t != null) { t = null; }" +
        "  t = 1;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue124b
  public void testIssue124b() throws Exception {
    testTypes(
        "var t = null;" +
        "function test() {" +
        "  if (t != null) { t = null; }" +
        "  t = undefined;" +
        "}",
        "condition always evaluates to false\n" +
        "left : (null|undefined)\n" +
        "right: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue259
  public void testIssue259() throws Exception {
    testTypes(
        " function f(x) {}" +
        "" +
        "var Clock = function() {" +
        "  " +
        "  this.Date = function() {};" +
        "  f(new this.Date());" +
        "};",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : this.Date\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue301
  public void testIssue301() throws Exception {
    testTypes(
        "Array.indexOf = function() {};" +
        "var s = 'hello';" +
        "alert(s.toLowerCase.indexOf('1'));",
        "Property indexOf never defined on String.prototype.toLowerCase");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue380
  public void testIssue380() throws Exception {
    testTypes(
        "" +
        "document.getElementById;" +
        "var list =  ['hello', 'you'];\n" +
        "list.push('?');\n" +
        "document.getElementById('node').innerHTML = list.toString();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue483
  public void testIssue483() throws Exception {
    testTypes(
        " function C() {" +
        "   this.a = [];" +
        "}" +
        "C.prototype.f = function() {" +
        "  if (this.a.length > 0) {" +
        "    g(this.a);" +
        "  }" +
        "};" +
        " function g(a) {}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : Array\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug592170
  public void testBug592170() throws Exception {
    testTypes(
        "" +
        "function foo(opt_f) {" +
        "  " +
        "  return opt_f || function () {};" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug901455
  public void testBug901455() throws Exception {
    testTypes(" function a() { return 3; }" +
        "var b = undefined === a()");
    testTypes(" function a() { return 3; }" +
        "var b = a() === undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug908701
  public void testBug908701() throws Exception {
    testTypes("var s = new String('foo');" +
        "var b = s.match(/a/) != null;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug908625
  public void testBug908625() throws Exception {
    testTypes("function A(){}" +
        "function B(){}" +
        "function foo(b){return b}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug911118
  public void testBug911118() throws Exception {
    
    Scope s = parseAndTypeCheckWithScope("var a = function(){};").scope;
    JSType type = s.getVar("a").getType();
    assertEquals("function (): undefined", type.toString());

    
    testTypes("function nullFunction() {};" +
        "var foo = nullFunction;" +
        "foo = function() {};" +
        "foo();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug909000
  public void testBug909000() throws Exception {
    testTypes("function A(){}\n" +
        "\n" +
        "function y(a) { return a }",
        "inconsistent return type\n" +
        "found   : A\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug930117
  public void testBug930117() throws Exception {
    testTypes(
        "function f(x){}" +
        "f(null);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : null\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug1484445
  public void testBug1484445() throws Exception {
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = null;" +
        " Foo.prototype.baz = null;" +
        "" +
        "function f(foo) {" +
        "  while (true) {" +
        "    if (foo.bar == null && foo.baz == null) {" +
        "      foo.bar;" +
        "    }" +
        "  }" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug1859535
  public void testBug1859535() throws Exception {
    testTypes(
        "" +
        "var inherits = function(childCtor, parentCtor) {" +
        "  " +
        "  function tempCtor() {};" +
        "  tempCtor.prototype = parentCtor.prototype;" +
        "  childCtor.superClass_ = parentCtor.prototype;" +
        "  childCtor.prototype = new tempCtor();" +
        "   childCtor.prototype.constructor = childCtor;" +
        "};" +
        "" +
        "var factory = function(constructor, var_args) {" +
        "  " +
        "  var tempCtor = function() {};" +
        "  tempCtor.prototype = constructor.prototype;" +
        "  var obj = new tempCtor();" +
        "  constructor.apply(obj, arguments);" +
        "  return obj;" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug1940591
  public void testBug1940591() throws Exception {
    testTypes(
        "" +
        "var a = {};\n" +
        "\n" +
        "a.name = 0;\n" +
        "\n" +
        "a.g = function(x) { x.name = 'a'; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug1942972
  public void testBug1942972() throws Exception {
    testTypes(
        "var google = {\n"+
        "  gears: {\n" +
        "    factory: {},\n" +
        "    workerPool: {}\n" +
        "  }\n" +
        "};\n" +
        "\n" +
        "google.gears = {factory: {}};\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug1943776
  public void testBug1943776() throws Exception {
    testTypes(
        "" +
        "function bar() {" +
        "  return {foo: []};" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug1987544
  public void testBug1987544() throws Exception {
    testTypes(
        " function foo(x) {}" +
        "var duration;" +
        "if (true && !(duration = 3)) {" +
        " foo(duration);" +
        "}",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug1940769
  public void testBug1940769() throws Exception {
    testTypes(
        " " +
        "function proto(obj) { return obj.prototype; }" +
        " function Map() {}" +
        "" +
        "function Map2() { Map.call(this); };" +
        "Map2.prototype = proto(Map);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug2335992
  public void testBug2335992() throws Exception {
    testTypes(
        " function f() { return 3; }" +
        "var x = f();" +
        "" +
        "x.y = 3;",
        "assignment to property y of x\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug2341812
  public void testBug2341812() throws Exception {
    testTypes(
        "" +
        "function EventTarget() {}" +
        "" +
        "function Node() {}" +
        " Node.prototype.index;" +
        "" +
        "function foo(x) { return x.index; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScopedConstructors1
  public void testScopedConstructors1() throws Exception {
    testTypes(
        "function foo1() { " +
        "   function Bar() { " +
        "     this.x = 3;" +
        "  }" +
        "}" +
        "function foo2() { " +
        "   function Bar() { " +
        "     this.x = 'y';" +
        "  }" +
        "  " +
        "  function baz(b) { return b.x; }" +
        "}",
        "inconsistent return type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScopedConstructors2
  public void testScopedConstructors2() throws Exception {
    testTypes(
        "" +
        "function foo1(f) {" +
        "  " +
        "  f.prototype.bar = function(g) {};" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference1
  public void testQualifiedNameInference1() throws Exception {
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = null;" +
        " Foo.prototype.baz = null;" +
        "" +
        "function f(foo) {" +
        "  while (true) {" +
        "    if (!foo.baz) break; " +
        "    foo.bar = null;" +
        "  }" +
        
        "  return foo.bar == null;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference2
  public void testQualifiedNameInference2() throws Exception {
    testTypes(
        "var x = {};" +
        "x.y = c;" +
        "function f(a, b) {" +
        "  if (a) {" +
        "    if (b) " +
        "      x.y = 2;" +
        "    else " +
        "      x.y = 1;" +
        "  }" +
        "  return x.y == null;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference3
  public void testQualifiedNameInference3() throws Exception {
    testTypes(
        "var x = {};" +
        "x.y = c;" +
        "function f(a, b) {" +
        "  if (a) {" +
        "    if (b) " +
        "      x.y = 2;" +
        "    else " +
        "      x.y = 1;" +
        "  }" +
        "  return x.y == null;" +
        "} function g() { x.y = null; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference4
  public void testQualifiedNameInference4() throws Exception {
    testTypes(
        " function f(x) {}\n" +
        "" +
        "function Foo(x) { this.x_ = x; }\n" +
        "Foo.prototype.bar = function() {" +
        "  if (this.x_) { f(this.x_); }" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference5
  public void testQualifiedNameInference5() throws Exception {
    testTypes(
        "var ns = {}; " +
        "(function() { " +
        "     ns.foo = function(x) {}; })();" +
        "(function() { ns.foo(true); })();",
        "actual parameter 1 of ns.foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference6
  public void testQualifiedNameInference6() throws Exception {
    testTypes(
        "var ns = {}; " +
        " ns.foo = function(x) {};" +
        "(function() { " +
        "    ns.foo = function(x) {};" +
        "    ns.foo(true); })();",
        "actual parameter 1 of ns.foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference7
  public void testQualifiedNameInference7() throws Exception {
    testTypes(
        "var ns = {}; " +
        "(function() { " +
        "   " +
        "  ns.Foo = function(x) {};" +
        "   function f(x) {}" +
        "  f(new ns.Foo(true));" +
        "})();",
        "actual parameter 1 of ns.Foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference8
  public void testQualifiedNameInference8() throws Exception {
    testTypes(
        "var ns = {}; " +
        "(function() { " +
        "   " +
        "  ns.Foo = function(x) {};" +
        "})();" +
        " function f(x) {}" +
        "f(new ns.Foo(true));",
        "Bad type annotation. Unknown type ns.Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference9
  public void testQualifiedNameInference9() throws Exception {
    testTypes(
        "var ns = {}; " +
        "ns.ns2 = {}; " +
        "(function() { " +
        "   " +
        "  ns.ns2.Foo = function(x) {};" +
        "   function f(x) {}" +
        "  f(new ns.ns2.Foo(true));" +
        "})();",
        "actual parameter 1 of ns.ns2.Foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference10
  public void testQualifiedNameInference10() throws Exception {
    testTypes(
        "var ns = {}; " +
        "ns.ns2 = {}; " +
        "(function() { " +
        "   " +
        "  ns.ns2.Foo = function() {};" +
        "   " +
        "  function F() {}" +
        "  (new F());" +
        "})();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSheqRefinedScope
  public void testSheqRefinedScope() throws Exception {
    Node n = parseAndTypeCheck(
        "function A() {}\n" +
        " function B() {}\n" +
        "\n" +
        "B.prototype.p = function() { return 1; }\n" +
        "\n" +
        "function f(a, b) {\n" +
        "  b.p();\n" +
        "  if (a === b) {\n" +
        "    b.p();\n" +
        "  }\n" +
        "}");
    Node nodeC = n.getLastChild().getLastChild().getLastChild().getLastChild()
        .getLastChild().getLastChild();
    JSType typeC = nodeC.getJSType();
    assertTrue(typeC.isNumber());

    Node nodeB = nodeC.getFirstChild().getFirstChild();
    JSType typeB = nodeB.getJSType();
    assertEquals("B", typeB.toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testAssignToUntypedVariable
  public void testAssignToUntypedVariable() throws Exception {
    Node n = parseAndTypeCheck("var z; z = 1;");

    Node assign = n.getLastChild().getFirstChild();
    Node node = assign.getFirstChild();
    assertFalse(node.getJSType().isUnknownType());
    assertEquals("number", node.getJSType().toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testAssignToUntypedProperty
  public void testAssignToUntypedProperty() throws Exception {
    Node n = parseAndTypeCheck(
        " function Foo() {}\n" +
        "Foo.prototype.a = 1;" +
        "(new Foo).a;");

    Node node = n.getLastChild().getFirstChild();
    assertFalse(node.getJSType().isUnknownType());
    assertTrue(node.getJSType().isNumber());
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew1
  public void testNew1() throws Exception {
    testTypes("new 4", TypeCheck.NOT_A_CONSTRUCTOR);
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew2
  public void testNew2() throws Exception {
    testTypes("var Math = {}; new Math()", TypeCheck.NOT_A_CONSTRUCTOR);
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew3
  public void testNew3() throws Exception {
    testTypes("new Date()");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew4
  public void testNew4() throws Exception {
    testTypes("function A(){}; new A();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew5
  public void testNew5() throws Exception {
    testTypes("function A(){}; new A();", TypeCheck.NOT_A_CONSTRUCTOR);
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew6
  public void testNew6() throws Exception {
    TypeCheckResult p =
      parseAndTypeCheckWithScope("function A(){};" +
      "var a = new A();");

    JSType aType = p.scope.getVar("a").getType();
    assertTrue(aType instanceof ObjectType);
    ObjectType aObjectType = (ObjectType) aType;
    assertEquals("A", aObjectType.getConstructor().getReferenceName());
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew7
  public void testNew7() throws Exception {
    testTypes("" +
        "function foo(opt_constructor) {" +
        "if (opt_constructor) { new opt_constructor; }" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew8
  public void testNew8() throws Exception {
    testTypes("" +
        "function foo(opt_constructor) {" +
        "new opt_constructor;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew9
  public void testNew9() throws Exception {
    testTypes("" +
        "function foo(opt_constructor) {" +
        "new (opt_constructor || Array);" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew10
  public void testNew10() throws Exception {
    testTypes("var goog = {};" +
        "" +
        "goog.Foo = function (opt_constructor) {" +
        "new (opt_constructor || Array);" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew11
  public void testNew11() throws Exception {
    testTypes("" +
        "function f(c1) {" +
        "  var c2 = function(){};" +
        "  c1.prototype = new c2;" +
        "}", TypeCheck.NOT_A_CONSTRUCTOR);
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew12
  public void testNew12() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = new Array();");
    Var a = p.scope.getVar("a");

    assertEquals(ARRAY_TYPE, a.getType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew13
  public void testNew13() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "function FooBar(){};" +
        "var a = new FooBar();");
    Var a = p.scope.getVar("a");

    assertTrue(a.getType() instanceof ObjectType);
    assertEquals("FooBar", a.getType().toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew14
  public void testNew14() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var FooBar = function(){};" +
        "var a = new FooBar();");
    Var a = p.scope.getVar("a");

    assertTrue(a.getType() instanceof ObjectType);
    assertEquals("FooBar", a.getType().toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew15
  public void testNew15() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var goog = {};" +
        "goog.A = function(){};" +
        "var a = new goog.A();");
    Var a = p.scope.getVar("a");

    assertTrue(a.getType() instanceof ObjectType);
    assertEquals("goog.A", a.getType().toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew16
  public void testNew16() throws Exception {
    testTypes(
        "" +
        "function Foo(x) {}" +
        "function g() { new Foo(1); }",
        "actual parameter 1 of Foo does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testName1
  public void testName1() throws Exception {
    assertEquals(VOID_TYPE, testNameNode("undefined"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testName2
  public void testName2() throws Exception {
    assertEquals(OBJECT_FUNCTION_TYPE, testNameNode("Object"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testName3
  public void testName3() throws Exception {
    assertEquals(ARRAY_FUNCTION_TYPE, testNameNode("Array"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testName4
  public void testName4() throws Exception {
    assertEquals(DATE_FUNCTION_TYPE, testNameNode("Date"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testName5
  public void testName5() throws Exception {
    assertEquals(REGEXP_FUNCTION_TYPE, testNameNode("RegExp"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation1
  public void testBitOperation1() throws Exception {
    testTypes("function foo(){ ~foo(); }",
        "operator ~ cannot be applied to undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation2
  public void testBitOperation2() throws Exception {
    testTypes("function foo(){var a = foo()<<3;}",
        "operator << cannot be applied to undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation3
  public void testBitOperation3() throws Exception {
    testTypes("function foo(){var a = 3<<foo();}",
        "operator << cannot be applied to undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation4
  public void testBitOperation4() throws Exception {
    testTypes("function foo(){var a = foo()>>>3;}",
        "operator >>> cannot be applied to undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation5
  public void testBitOperation5() throws Exception {
    testTypes("function foo(){var a = 3>>>foo();}",
        "operator >>> cannot be applied to undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation6
  public void testBitOperation6() throws Exception {
    testTypes("function foo(){var a = foo()&3;}",
        "bad left operand to bitwise operator\n" +
        "found   : Object\n" +
        "required: (boolean|null|number|string|undefined)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation7
  public void testBitOperation7() throws Exception {
    testTypes("var x = null; x |= undefined; x &= 3; x ^= '3'; x |= true;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation8
  public void testBitOperation8() throws Exception {
    testTypes("var x = void 0; x |= new Number(3);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation9
  public void testBitOperation9() throws Exception {
    testTypes("var x = void 0; x |= {};",
        "bad right operand to bitwise operator\n" +
        "found   : {}\n" +
        "required: (boolean|null|number|string|undefined)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall1
  public void testCall1() throws Exception {
    testTypes("3();", "number expressions are not callable");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall2
  public void testCall2() throws Exception {
    testTypes("function bar(foo){ bar('abc'); }",
        "actual parameter 1 of bar does not match formal parameter\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall3
  public void testCall3() throws Exception {
    
    
    testTypes("var opt_f;" +
        "var f1;" +
        "var f2 = opt_f || f1;" +
        "f2();",
        "Bad type annotation. Unknown type some.unknown.type");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall4
  public void testCall4() throws Exception {
    testTypes("var foo = function bar(a){ bar('abc'); }",
        "actual parameter 1 of bar does not match formal parameter\n" +
        "found   : string\n" +
        "required: RegExp");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall5
  public void testCall5() throws Exception {
    testTypes("var foo = function bar(a){ foo('abc'); }",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : string\n" +
        "required: RegExp");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall6
  public void testCall6() throws Exception {
    testTypes("function bar(foo){}" +
        "bar('abc');",
        "actual parameter 1 of bar does not match formal parameter\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall7
  public void testCall7() throws Exception {
    testTypes("var foo = function bar(a){};" +
        "foo('abc');",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : string\n" +
        "required: RegExp");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall8
  public void testCall8() throws Exception {
    testTypes("var f;f();",
        "(Function|number) expressions are " +
        "not callable");
  }
