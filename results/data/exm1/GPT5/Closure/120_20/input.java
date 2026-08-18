// buggy code
    boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop.
      for (BasicBlock block = ref.getBasicBlock();
           block != null; block = block.getParent()) {
        if (block.isFunction) {
          break;
        } else if (block.isLoop) {
          return false;
        }
      }

      return true;
    }

// relevant test
// com.google.javascript.jscomp.TypeCheckTest::testSetprop14
  public void testSetprop14() throws Exception {
    
    testTypes("\n" +
              "function Top() {}\n" +
              "\n" +
              "function Mid() {}\n" +
              "\n" +
              "Mid.prototype.foo = function() { return 1; };\n" +
              "\n" +
              "function Bottom() {}\n" +
              "\n" +
              "Bottom.prototype.foo = function() { return 3; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetpropDict1
  public void testGetpropDict1() throws Exception {
    testTypes("" +
              "function Dict1(){ this['prop'] = 123; }" +
              "" +
              "function takesDict(x) { return x.prop; }",
              "Cannot do '.' access on a dict");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetpropDict2
  public void testGetpropDict2() throws Exception {
    testTypes("" +
              "function Dict1(){ this['prop'] = 123; }" +
              "" +
              "function Dict1kid(){ this['prop'] = 123; }" +
              "" +
              "function takesDict(x) { return x.prop; }",
              "Cannot do '.' access on a dict");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetpropDict3
  public void testGetpropDict3() throws Exception {
    testTypes("" +
              "function Dict1() { this['prop'] = 123; }" +
              "" +
              "function NonDict() { this.prop = 321; }" +
              "" +
              "function takesDict(x) { return x.prop; }",
              "Cannot do '.' access on a dict");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetpropDict4
  public void testGetpropDict4() throws Exception {
    testTypes("" +
              "function Dict1() { this['prop'] = 123; }" +
              "" +
              "function Struct1() { this.prop = 123; }" +
              "" +
              "function takesNothing(x) { return x.prop; }",
              "Cannot do '.' access on a dict");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetpropDict5
  public void testGetpropDict5() throws Exception {
    testTypes("" +
              "function Dict1(){ this.prop = 123; }",
              "Cannot do '.' access on a dict");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetpropDict6
  public void testGetpropDict6() throws Exception {
    testTypes("\n" +
              "function Foo() {}\n" +
              "function Bar() {}\n" +
              "Bar.prototype = new Foo();\n" +
              "Bar.prototype.someprop = 123;\n",
              "Cannot do '.' access on a dict");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetpropDict7
  public void testGetpropDict7() throws Exception {
    testTypes("( {'x': 123}).x = 321;",
              "Cannot do '.' access on a dict");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetelemStruct1
  public void testGetelemStruct1() throws Exception {
    testTypes("" +
              "function Struct1(){ this.prop = 123; }" +
              "" +
              "function takesStruct(x) {" +
              "  var z = x;" +
              "  return z['prop'];" +
              "}",
              "Cannot do '[]' access on a struct");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetelemStruct2
  public void testGetelemStruct2() throws Exception {
    testTypes("" +
              "function Struct1(){ this.prop = 123; }" +
              "" +
              "function Struct1kid(){ this.prop = 123; }" +
              "" +
              "function takesStruct2(x) { return x['prop']; }",
              "Cannot do '[]' access on a struct");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetelemStruct3
  public void testGetelemStruct3() throws Exception {
    testTypes("" +
              "function Struct1(){ this.prop = 123; }" +
              "" +
              "function Struct1kid(){ this.prop = 123; }" +
              "var x = (new Struct1kid())['prop'];",
              "Cannot do '[]' access on a struct");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetelemStruct4
  public void testGetelemStruct4() throws Exception {
    testTypes("" +
              "function Struct1() { this.prop = 123; }" +
              "" +
              "function NonStruct() { this.prop = 321; }" +
              "" +
              "function takesStruct(x) { return x['prop']; }",
              "Cannot do '[]' access on a struct");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetelemStruct5
  public void testGetelemStruct5() throws Exception {
    testTypes("" +
              "function Struct1() { this.prop = 123; }" +
              "" +
              "function Dict1() { this['prop'] = 123; }" +
              "" +
              "function takesNothing(x) { return x['prop']; }",
              "Cannot do '[]' access on a struct");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetelemStruct6
  public void testGetelemStruct6() throws Exception {
    
    testTypes(" function Foo(){}\n" +
              "" +
              "function Bar(){ this.x = 123; }\n" +
              "var z = (new Bar())['x'];");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetelemStruct7
  public void testGetelemStruct7() throws Exception {
    testTypes("\n" +
              "function Foo() {}\n" +
              "\n" +
              "function Bar() {}\n" +
              "Bar.prototype = new Foo();\n" +
              "Bar.prototype['someprop'] = 123;\n",
              "Cannot do '[]' access on a struct");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInOnStruct
  public void testInOnStruct() throws Exception {
    testTypes("" +
              "function Foo() {}\n" +
              "if ('prop' in (new Foo())) {}",
              "Cannot use the IN operator with structs");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForinOnStruct
  public void testForinOnStruct() throws Exception {
    testTypes("" +
              "function Foo() {}\n" +
              "for (var prop in (new Foo())) {}",
              "Cannot use the IN operator with structs");
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
        "No properties on this expression\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropAccess3
  public void testPropAccess3() throws Exception {
    
    
    testTypes("var bar = void 0; bar.baz; bar.bax;",
        "No properties on this expression\n" +
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

    assertTypeEquals(createUnionType(STRING_TYPE, NULL_TYPE),
        p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar2
  public void testVar2() throws Exception {
    testTypes(" var a = function(){}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar3
  public void testVar3() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = 3;");

    assertTypeEquals(NUMBER_TYPE, p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar4
  public void testVar4() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var a = 3; a = 'string';");

    assertTypeEquals(createUnionType(STRING_TYPE, NUMBER_TYPE),
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

// com.google.javascript.jscomp.TypeCheckTest::testInferredParam7
  public void testInferredParam7() throws Exception {
    testTypes(
        " function f(x) {}" +
        "var bar =  (" +
        "    function(x, y) { f(y); });",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : (number|undefined)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenParams1
  public void testOverriddenParams1() throws Exception {
    testTypes(
        " function Foo() {}" +
        "" +
        "Foo.prototype.bar = function(var_args) {};" +
        " function SubFoo() {}" +
        "" +
        "SubFoo.prototype.bar = function(x) {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenParams2
  public void testOverriddenParams2() throws Exception {
    testTypes(
        " function Foo() {}" +
        "" +
        "Foo.prototype.bar = function(var_args) {};" +
        " function SubFoo() {}" +
        "" +
        "SubFoo.prototype.bar = function(x) {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenParams3
  public void testOverriddenParams3() throws Exception {
    testTypes(
        " function Foo() {}" +
        "" +
        "Foo.prototype.bar = function(var_args) { };" +
        " function SubFoo() {}" +
        "" +
        "SubFoo.prototype.bar = function(x) {};",
        "mismatch of the bar property type and the type of the " +
        "property it overrides from superclass Foo\n" +
        "original: function (this:Foo, ...[number]): undefined\n" +
        "override: function (this:SubFoo, number): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenParams4
  public void testOverriddenParams4() throws Exception {
    testTypes(
        " function Foo() {}" +
        "" +
        "Foo.prototype.bar = function(var_args) {};" +
        " function SubFoo() {}" +
        "" +
        "SubFoo.prototype.bar = function(x) {};",
        "mismatch of the bar property type and the type of the " +
        "property it overrides from superclass Foo\n" +
        "original: function (...[number]): ?\n" +
        "override: function (number): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenParams5
  public void testOverriddenParams5() throws Exception {
    testTypes(
        " function Foo() {}" +
        "" +
        "Foo.prototype.bar = function(x) { };" +
        " function SubFoo() {}" +
        "" +
        "SubFoo.prototype.bar = function() {};" +
        "(new SubFoo()).bar();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenParams6
  public void testOverriddenParams6() throws Exception {
    testTypes(
        " function Foo() {}" +
        "" +
        "Foo.prototype.bar = function(x) { };" +
        " function SubFoo() {}" +
        "" +
        "SubFoo.prototype.bar = function() {};" +
        "(new SubFoo()).bar(true);",
        "actual parameter 1 of SubFoo.prototype.bar " +
        "does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenParams7
  public void testOverriddenParams7() throws Exception {
    testTypes(
        " function Foo() {}" +
        "" +
        "Foo.prototype.bar = function(x) { };" +
        " function SubFoo() {}" +
        "" +
        "SubFoo.prototype.bar = function(x) {};",
        "mismatch of the bar property type and the type of the " +
        "property it overrides from superclass Foo\n" +
        "original: function (this:Foo, string): undefined\n" +
        "override: function (this:SubFoo, number): undefined");
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

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenReturn3
  public void testOverriddenReturn3() throws Exception {
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = " +
        "    function() { return null; };" +
        " function SubFoo() {}" +
        " SubFoo.prototype.bar = " +
        "    function() { return 3; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenReturn4
  public void testOverriddenReturn4() throws Exception {
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = " +
        "    function() { return null; };" +
        " function SubFoo() {}" +
        " SubFoo.prototype.bar = " +
        "    function() { return 3; }",
        "mismatch of the bar property type and the type of the " +
        "property it overrides from superclass Foo\n" +
        "original: function (this:Foo): string\n" +
        "override: function (this:SubFoo): number");
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

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenProperty1
  public void testOverriddenProperty1() throws Exception {
    testTypes(
        " function Foo() {}" +
        "" +
        "Foo.prototype.bar = {};" +
        " function SubFoo() {}" +
        "" +
        "SubFoo.prototype.bar = [];");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenProperty2
  public void testOverriddenProperty2() throws Exception {
    testTypes(
        " function Foo() {" +
        "  " +
        "  this.bar = {};" +
        "}" +
        " function SubFoo() {}" +
        "" +
        "SubFoo.prototype.bar = [];");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenProperty3
  public void testOverriddenProperty3() throws Exception {
    testTypes(
        " function Foo() {" +
        "}" +
        " Foo.prototype.data;" +
        " function SubFoo() {}" +
        " " +
        "SubFoo.prototype.data = null;",
        "mismatch of the data property type and the type " +
        "of the property it overrides from superclass Foo\n" +
        "original: string\n" +
        "override: (Object|null|string)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenProperty4
  public void testOverriddenProperty4() throws Exception {
    
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = null;" +
        " function SubFoo() {}" +
        "SubFoo.prototype.bar = 3;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenProperty5
  public void testOverriddenProperty5() throws Exception {
    
    testTypes(
        " function Foo() {}" +
        "Foo.prototype.bar = null;" +
        " function SubFoo() {}" +
        " SubFoo.prototype.bar = 3;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOverriddenProperty6
  public void testOverriddenProperty6() throws Exception {
    
    
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = null;" +
        " function SubFoo() {}" +
        "SubFoo.prototype.bar = 3;");
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

// com.google.javascript.jscomp.TypeCheckTest::testThis12
  public void testThis12() throws Exception {
    testTypes(
        " function f(x) {}" +
        " function Ctor() {}" +
        "Ctor.prototype['method'] = function() {" +
        "  f(this);" +
        "}",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : Ctor\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis13
  public void testThis13() throws Exception {
    testTypes(
        " function f(x) {}" +
        " function Ctor() {}" +
        "Ctor.prototype = {" +
        "  method: function() {" +
        "    f(this);" +
        "  }" +
        "};",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : Ctor\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testThis14
  public void testThis14() throws Exception {
    testTypes(
        " function f(x) {}" +
        "f(this.Object);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : function (new:Object, *=): ?\n" +
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

    assertTypeEquals(NUMBER_TYPE, n.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringNode
  public void testStringNode() throws Exception {
    Node n = typeCheck(Node.newString("hello"));

    assertTypeEquals(STRING_TYPE, n.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanNodeTrue
  public void testBooleanNodeTrue() throws Exception {
    Node trueNode = typeCheck(new Node(Token.TRUE));

    assertTypeEquals(BOOLEAN_TYPE, trueNode.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanNodeFalse
  public void testBooleanNodeFalse() throws Exception {
    Node falseNode = typeCheck(new Node(Token.FALSE));

    assertTypeEquals(BOOLEAN_TYPE, falseNode.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testUndefinedNode
  public void testUndefinedNode() throws Exception {
    Node p = new Node(Token.ADD);
    Node n = Node.newString(Token.NAME, "undefined");
    p.addChildToBack(n);
    p.addChildToBack(Node.newNumber(5));
    typeCheck(p);

    assertTypeEquals(VOID_TYPE, n.getJSType());
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

// com.google.javascript.jscomp.TypeCheckTest::testIIFE1
  public void testIIFE1() throws Exception {
    testTypes(
        "var namespace = {};" +
        " namespace.prop = 3;" +
        "(function(ns) {" +
        "  ns.prop = true;" +
        "})(namespace);",
        "assignment to property prop of ns\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIIFE2
  public void testIIFE2() throws Exception {
    testTypes(
        " function Foo() {}" +
        "(function(ctor) {" +
        "   ctor.prop = true;" +
        "})(Foo);" +
        " function f() { return Foo.prop; }",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIIFE3
  public void testIIFE3() throws Exception {
    testTypes(
        " function Foo() {}" +
        "(function(ctor) {" +
        "   ctor.prop = true;" +
        "})(Foo);" +
        " function f(x) {}" +
        "f(Foo.prop);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIIFE4
  public void testIIFE4() throws Exception {
    testTypes(
        " var namespace = {};" +
        "(function(ns) {" +
        "  \n" +
        "   ns.Ctor = function(x) {};" +
        "})(namespace);" +
        "new namespace.Ctor(true);",
        "actual parameter 1 of namespace.Ctor " +
        "does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIIFE5
  public void testIIFE5() throws Exception {
    
    
    
    testTypes(
        " var namespace = {};" +
        "(function(ns) {" +
        "  \n" +
        "   ns.Ctor = function() {};" +
        "    ns.Ctor.prototype.bar = true;" +
        "})(namespace);" +
        " function f(x) { return x.bar; }",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNotIIFE1
  public void testNotIIFE1() throws Exception {
    testTypes(
        " function f(x) {}" +
        " function g(x) {}" +
        "g(function(y) { f(y); }, true);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue61
  public void testIssue61() throws Exception {
    testTypes(
        "var ns = {};" +
        "(function() {" +
        "  " +
        "  ns.a = function(b) {};" +
        "})();" +
        "function d() {" +
        "  ns.a(123);" +
        "}",
        "actual parameter 1 of ns.a does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue61b
  public void testIssue61b() throws Exception {
    testTypes(
        "var ns = {};" +
        "(function() {" +
        "  " +
        "  ns.a = function(b) {};" +
        "})();" +
        "ns.a(123);",
        "actual parameter 1 of ns.a does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
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

// com.google.javascript.jscomp.TypeCheckTest::testIssue368
  public void testIssue368() throws Exception {
    testTypes(
        " function Foo(){}" +
        "\n" +
        "Foo.prototype.add = function(one, two) {};" +
        "\n" +
        "function Bar(){}" +
        "\n" +
        "Bar.prototype.add = function(ignored) {};" +
        "(new Bar()).add(1, 2);",
        "actual parameter 2 of Bar.prototype.add does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue380
  public void testIssue380() throws Exception {
    testTypes(
        "\n" +
        "document.getElementById;\n" +
        "var list =  ['hello', 'you'];\n" +
        "list.push('?');\n" +
        "document.getElementById('node').innerHTML = list.toString();",
        
        "Type annotations are not allowed here. " +
        "Are you missing parentheses?");
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

// com.google.javascript.jscomp.TypeCheckTest::testIssue537a
  public void testIssue537a() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype = {method: function() {}};" +
        "\n" +
        "function Bar() {" +
        "  Foo.call(this);" +
        "  if (this.baz()) this.method(1);" +
        "}" +
        "Bar.prototype = {" +
        "  baz: function() {" +
        "    return true;" +
        "  }" +
        "};" +
        "Bar.prototype.__proto__ = Foo.prototype;",
        "Function Foo.prototype.method: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue537b
  public void testIssue537b() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype = {method: function() {}};" +
        "\n" +
        "function Bar() {" +
        "  Foo.call(this);" +
        "  if (this.baz(1)) this.method();" +
        "}" +
        "Bar.prototype = {" +
        "  baz: function() {" +
        "    return true;" +
        "  }" +
        "};" +
        "Bar.prototype.__proto__ = Foo.prototype;",
        "Function Bar.prototype.baz: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue537c
  public void testIssue537c() throws Exception {
    testTypes(
        " function Foo() {}" +
        "\n" +
        "function Bar() {" +
        "  Foo.call(this);" +
        "  if (this.baz2()) alert(1);" +
        "}" +
        "Bar.prototype = {" +
        "  baz: function() {" +
        "    return true;" +
        "  }" +
        "};" +
        "Bar.prototype.__proto__ = Foo.prototype;",
        "Property baz2 never defined on Bar");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue537d
  public void testIssue537d() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype = {" +
        "   x: function() { new Bar(); }," +
        "   y: function() { new Bar(); }" +
        "};" +
        "\n" +
        "function Bar() {" +
        "  this.xy = 3;" +
        "}" +
        " function f() { return new Bar(); }" +
        " function g() { return new Bar(); }" +
        "Bar.prototype = {" +
        "   x: function() { new Bar(); }," +
        "   y: function() { new Bar(); }" +
        "};" +
        "Bar.prototype.__proto__ = Foo.prototype;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue586
  public void testIssue586() throws Exception {
    testTypes(
        "" +
        "var MyClass = function() {};" +
        "" +
        "MyClass.prototype.fn = function(success) {};" +
        "MyClass.prototype.test = function() {" +
        "  this.fn();" +
        "  this.fn = function() {};" +
        "};",
        "Function MyClass.prototype.fn: called with 0 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue635
  public void testIssue635() throws Exception {
    
    testTypes(
        "" +
        "function F() {}" +
        "F.prototype.bar = function() { this.baz(); };" +
        "F.prototype.baz = function() {};" +
        "" +
        "function G() {}" +
        "G.prototype.bar = F.prototype.bar;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue635b
  public void testIssue635b() throws Exception {
    testTypes(
        "" +
        "function F() {}" +
        "" +
        "function G() {}" +
        " var x = F;",
        "initializing variable\n" +
        "found   : function (new:F): undefined\n" +
        "required: function (new:G): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue669
  public void testIssue669() throws Exception {
    testTypes(
        "" +
         "function f(a) {" +
         "  var results;" +
         "  if (a) {" +
         "    results = {};" +
         "    results.prop1 = {a: 3};" +
         "  } else {" +
         "    results = {prop2: 3};" +
         "  }" +
         "  return results;" +
         "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue688
  public void testIssue688() throws Exception {
    testTypes(
        " var SOME_DEFAULT =\n" +
        "     ({first: 1, second: 2});\n" +
        "\n" +
        "function TwoNumbers() {}\n" +
        "\n" +
        "TwoNumbers.prototype.first;\n" +
        "\n" +
        "TwoNumbers.prototype.second;\n" +
        " function f() { return SOME_DEFAULT; }",
        "inconsistent return type\n" +
        "found   : (TwoNumbers|null)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue700
  public void testIssue700() throws Exception {
    testTypes(
        "\n" +
        "function temp1(opt_data) {\n" +
        "  return opt_data.text;\n" +
        "}\n" +
        "\n" +
        "\n" +
        "function temp2(opt_data) {\n" +
        "  \n" +
        "  function __inner() {\n" +
        "    return temp1(opt_data.activity);\n" +
        "  }\n" +
        "  return __inner();\n" +
        "}\n" +
        "\n" +
        "\n" +
        "function temp3(opt_data) {\n" +
        "  return 'n: ' + opt_data.n + ', t: ' + opt_data.text + '.';\n" +
        "}\n" +
        "\n" +
        "function callee() {\n" +
        "  var output = temp3({\n" +
        "    n: 0,\n" +
        "    text: 'a string',\n" +
        "    b: true\n" +
        "  })\n" +
        "  alert(output);\n" +
        "}\n" +
        "\n" +
        "callee();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue725
  public void testIssue725() throws Exception {
    testTypes(
        " var RecordType1;" +
        " var RecordType2;" +
        " function f(rec) {" +
        "  alert(rec.name2222);" +
        "}",
        "Property name2222 never defined on rec");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue726
  public void testIssue726() throws Exception {
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        " " +
        "Foo.prototype.getDeferredBar = function() { " +
        "  var self = this;" +
        "  return function() {" +
        "    self.bar(true);" +
        "  };" +
        "};",
        "actual parameter 1 of Foo.prototype.bar does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue765
  public void testIssue765() throws Exception {
    testTypes(
        "" +
        "var AnotherType = function (parent) {" +
        "    " +
        "    this.doSomething = function (stringParameter) {};" +
        "};" +
        "" +
        "var YetAnotherType = function () {" +
        "    this.field = new AnotherType(self);" +
        "    this.testfun=function(stringdata) {" +
        "        this.field.doSomething(null);" +
        "    };" +
        "};",
        "actual parameter 1 of AnotherType.doSomething " +
        "does not match formal parameter\n" +
        "found   : null\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue783
  public void testIssue783() throws Exception {
    testTypes(
        "" +
        "var Type = function () {" +
        "  " +
        "  this.me_ = this;" +
        "};" +
        "Type.prototype.doIt = function() {" +
        "  var me = this.me_;" +
        "  for (var i = 0; i < me.unknownProp; i++) {}" +
        "};",
        "Property unknownProp never defined on Type");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue791
  public void testIssue791() throws Exception {
    testTypes(
        "" +
        "function test1(obj) {}" +
        "var fnStruc1 = {};" +
        "fnStruc1.func = function() {};" +
        "test1(fnStruc1);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue810
  public void testIssue810() throws Exception {
    testTypes(
        "" +
        "var Type = function () {" +
        "};" +
        "Type.prototype.doIt = function(obj) {" +
        "  this.prop = obj.unknownProp;" +
        "};",
        "Property unknownProp never defined on obj");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue1002
  public void testIssue1002() throws Exception {
    testTypes(
        "" +
        "var I = function() {};" +
        "" +
        "var A = function() {};" +
        "" +
        "var B = function() {};" +
        "var f = function() {" +
        "  if (A === B) {" +
        "    new B();" +
        "  }" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue1023
  public void testIssue1023() throws Exception {
    testTypes(
        "" +
        "function F() {}" +
        "(function () {" +
        "  F.prototype = {" +
        "    " +
        "    bar: function (x) {  }" +
        "  };" +
        "})();" +
        "(new F()).bar(true)",
        "actual parameter 1 of F.prototype.bar does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnums
  public void testEnums() throws Exception {
    testTypes(
        "var outer = function() {" +
        "  " +
        "  var Level = {" +
        "    NONE: 0," +
        "  };" +
        "  " +
        "  var l = Level.NONE;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug592170
  public void testBug592170() throws Exception {
    testTypes(
        "" +
        "function foo(opt_f) {" +
        "  " +
        "  return opt_f || function () {};" +
        "}",
        "Type annotations are not allowed here. Are you missing parentheses?");
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
        "var google = {\n" +
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
        "assignment\n" +
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

// com.google.javascript.jscomp.TypeCheckTest::testBug7701884
  public void testBug7701884() throws Exception {
    testTypes(
        "\n" +
        "var forEach = function(x, y) {\n" +
        "  for (var i = 0; i < x.length; i++) y(x[i]);\n" +
        "};" +
        "" +
        "function f(x) {}" +
        "" +
        "function h(x) {" +
        "  var top = null;" +
        "  forEach(x, function(z) { top = z; });" +
        "  if (top) f(top);" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug8017789
  public void testBug8017789() throws Exception {
    testTypes(
        "" +
        "var f = function(isResult) {" +
        "    while (true)" +
        "        isResult['t'];" +
        "};" +
        "" +
        "var map;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypedefBeforeUse
  public void testTypedefBeforeUse() throws Exception {
    testTypes(
        "" +
        "var map;" +
        "" +
        "var f = function(isResult) {" +
        "    while (true)" +
        "        isResult['t'];" +
        "};");
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
        " var ns = {}; " +
        " ns.foo = function(x) {};" +
        "(function() { " +
        "    ns.foo = function(x) {};" +
        "    ns.foo(true); " +
        "})();",
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
    testClosureTypesMultipleWarnings(
        "var ns = {}; " +
        "(function() { " +
        "   " +
        "  ns.Foo = function(x) {};" +
        "})();" +
        " function f(x) {}" +
        "f(new ns.Foo(true));",
        Lists.newArrayList(
            "actual parameter 1 of ns.Foo does not match formal parameter\n" +
            "found   : boolean\n" +
            "required: number"));
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

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference11
  public void testQualifiedNameInference11() throws Exception {
    testTypes(
        " function Foo() {}" +
        "function f() {" +
        "  var x = new Foo();" +
        "  x.onload = function() {" +
        "    x.onload = null;" +
        "  };" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference12
  public void testQualifiedNameInference12() throws Exception {
    
    
    testTypes(
        " function f(x) {}" +
        " function Foo() {" +
        "   this.bar = 3;" +
        "  f(function() { this.bar = true; });" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference13
  public void testQualifiedNameInference13() throws Exception {
    testTypes(
        " function Foo() {}" +
        "function f(z) {" +
        "  var x = new Foo();" +
        "  if (z) {" +
        "    x.onload = function() {};" +
        "  } else {" +
        "    x.onload = null;" +
        "  };" +
        "}");
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

    assertTypeEquals(ARRAY_TYPE, a.getType());
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

// com.google.javascript.jscomp.TypeCheckTest::testNew17
  public void testNew17() throws Exception {
    testTypes("var goog = {}; goog.x = 3; new goog.x",
              "cannot instantiate non-constructor");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew18
  public void testNew18() throws Exception {
    testTypes("var goog = {};" +
              " goog.F = function() {};" +
              " goog.G = goog.F;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testName1
  public void testName1() throws Exception {
    assertTypeEquals(VOID_TYPE, testNameNode("undefined"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testName2
  public void testName2() throws Exception {
    assertTypeEquals(OBJECT_FUNCTION_TYPE, testNameNode("Object"));
  }
