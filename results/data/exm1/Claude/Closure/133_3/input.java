// buggy code
  private String getRemainingJSDocLine() {
    String result = stream.getRemainingJSDocLine();
    return result;
  }

// relevant test
// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteGet1
  public void testNoRewriteGet1() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {get foo(){return f}};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteGet2
  public void testNoRewriteGet2() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {get foo(){return 1}};",
        "var o = new a;",
        "o.foo");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteSet1
  public void testNoRewriteSet1() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {set foo(a){}};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteSet2
  public void testNoRewriteSet2() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {set foo(a){}};",
        "var o = new a;",
        "o.foo = 1");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNotImplementedMethod
  public void testNoRewriteNotImplementedMethod() throws Exception {
    testSame(newlineJoin("function a(){}",
                         "var o = new a;",
                         "o.foo()"));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testWrapper
  public void testWrapper() {
    testSame("(function() {})()");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSameModule1
  public void testRewriteSameModule1() throws Exception {
    JSModule[] modules = createModuleStar(
        
        semicolonJoin(ModuleTestInput.DEFINITION,
                      ModuleTestInput.USE),
        
        "");

    test(modules, new String[] {
        
        semicolonJoin(ModuleTestInput.REWRITTEN_DEFINITION,
                      ModuleTestInput.REWRITTEN_USE),
        
        "",
      });
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSameModule2
  public void testRewriteSameModule2() throws Exception {
    JSModule[] modules = createModuleStar(
        
        "",
        
        semicolonJoin(ModuleTestInput.DEFINITION,
                      ModuleTestInput.USE));

    test(modules, new String[] {
        
        "",
        
        semicolonJoin(ModuleTestInput.REWRITTEN_DEFINITION,
                      ModuleTestInput.REWRITTEN_USE)
      });
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSameModule3
  public void testRewriteSameModule3() throws Exception {
    JSModule[] modules = createModuleStar(
        
        semicolonJoin(ModuleTestInput.USE,
                      ModuleTestInput.DEFINITION),
        
        "");

    test(modules, new String[] {
        
        semicolonJoin(ModuleTestInput.REWRITTEN_USE,
                      ModuleTestInput.REWRITTEN_DEFINITION),
        
        ""
      });
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteDefinitionBeforeUse
  public void testRewriteDefinitionBeforeUse() throws Exception {
    JSModule[] modules = createModuleStar(
        
        ModuleTestInput.DEFINITION,
        
        ModuleTestInput.USE);

    test(modules, new String[] {
        
        ModuleTestInput.REWRITTEN_DEFINITION,
        
        ModuleTestInput.REWRITTEN_USE
      });
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteUseBeforeDefinition
  public void testNoRewriteUseBeforeDefinition() throws Exception {
    JSModule[] modules = createModuleStar(
        
        ModuleTestInput.USE,
        
        ModuleTestInput.DEFINITION);

    testSame(modules);
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testOneType1
  public void testOneType1() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    testSets(false, js, js, "{a=[[Foo.prototype]]}");
    testSets(true, js, js, "{a=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testOneType2
  public void testOneType2() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = {a: 0};\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    String expected = "{a=[[Foo.prototype]]}";
    testSets(false, js, js, expected);
    testSets(true, js, js, expected);
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testOneType3
  public void testOneType3() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = { get a() {return  0},"
        + "                  set a(b) {} };\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    String expected = "{a=[[Foo.prototype]]}";
    testSets(false, js, js, expected);
    testSets(true, js, js, expected);
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testPrototypeAndInstance
  public void testPrototypeAndInstance() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    testSets(false, js, js, "{a=[[Foo.prototype]]}");
    testSets(true, js, js, "{a=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testPrototypeAndInstance2
  public void testPrototypeAndInstance2() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "new Foo().a = 0;";
    testSets(false, js, js, "{a=[[Foo.prototype]]}");
    testSets(true, js, js, "{a=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoTypes1
  public void testTwoTypes1() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoTypes2
  public void testTwoTypes2() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = {a: 0};"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype = {a: 0};"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;";

    String output = ""
        + "function Foo(){}"
        + "Foo.prototype = {Foo_prototype$a: 0};"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "function Bar(){}"
        + "Bar.prototype = {Bar_prototype$a: 0};"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0";

    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoTypes3
  public void testTwoTypes3() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = { get a() {return  0},"
        + "                  set a(b) {} };\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype = { get a() {return  0},"
        + "                  set a(b) {} };\n"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;";

    String output = ""
        + "function Foo(){}"
        + "Foo.prototype = { get Foo_prototype$a() {return  0},"
        + "                  set Foo_prototype$a(b) {} };\n"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "function Bar(){}"
        + "Bar.prototype = { get Bar_prototype$a() {return  0},"
        + "                  set Bar_prototype$a(b) {} };\n"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0";

    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoFields
  public void testTwoFields() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + "Foo.prototype.b = 0;"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + "F.b = 0;";
    String output = "function Foo(){}Foo.prototype.a=0;Foo.prototype.b=0;"
        + "var F=new Foo;F.a=0;F.b=0";
    testSets(false, js, output, "{a=[[Foo.prototype]], b=[[Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Foo.prototype]], b=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoSeparateFieldsTwoTypes
  public void testTwoSeparateFieldsTwoTypes() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + "Foo.prototype.b = 0;"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + "F.b = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;"
        + "Bar.prototype.b = 0;"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;"
        + "B.b = 0;";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "Foo.prototype.Foo_prototype$b=0;"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "F.Foo_prototype$b=0;"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "Bar.prototype.Bar_prototype$b=0;"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0;"
        + "B.Bar_prototype$b=0";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]],"
                                + " b=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]],"
                               + " b=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionType
  public void testUnionType() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;\n"
        + "B = new Foo;\n"
        + "B.a = 0;\n"
        + " function Baz() {}\n"
        + "Baz.prototype.a = 0;\n";
    testSets(false, js,
             "{a=[[Bar.prototype, Foo.prototype], [Baz.prototype]]}");
    testSets(true, js, "{a=[[Bar.prototype, Foo.prototype], [Baz.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testIgnoreUnknownType
  public void testIgnoreUnknownType() {
    String js = ""
        + "\n"
        + "function Foo() {}\n"
        + "Foo.prototype.blah = 3;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.blah = 0;\n"
        + "var U = function() { return {} };\n"
        + "U().blah();";
    String expected = ""
        + "function Foo(){}Foo.prototype.blah=3;var F = new Foo;F.blah=0;"
        + "var U=function(){return{}};U().blah()";
    testSets(false, js, expected, "{}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, expected, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testIgnoreUnknownType1
  public void testIgnoreUnknownType1() {
    String js = ""
        + "\n"
        + "function Foo() {}\n"
        + "Foo.prototype.blah = 3;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.blah = 0;\n"
        + "\n"
        + "var U = function() { return {} };\n"
        + "U().blah();";
    String expected = ""
        + "function Foo(){}Foo.prototype.blah=3;var F = new Foo;F.blah=0;"
        + "var U=function(){return{}};U().blah()";
    testSets(false, js, expected, "{blah=[[Foo.prototype]]}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, expected, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testIgnoreUnknownType2
  public void testIgnoreUnknownType2() {
    String js = ""
        + "\n"
        + "function Foo() {}\n"
        + "Foo.prototype.blah = 3;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.blah = 0;\n"
        + "\n"
        + "function Bar() {}\n"
        + "Bar.prototype.blah = 3;\n"
        + "\n"
        + "var U = function() { return {} };\n"
        + "U().blah();";
    String expected = ""
        + "function Foo(){}Foo.prototype.blah=3;var F = new Foo;F.blah=0;"
        + "function Bar(){}Bar.prototype.blah=3;"
        + "var U=function(){return{}};U().blah()";
    testSets(false, js, expected, "{}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, expected, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionTypeTwoFields
  public void testUnionTypeTwoFields() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "Foo.prototype.b = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "Bar.prototype.b = 0;\n"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;\n"
        + "B.b = 0;\n"
        + "B = new Foo;\n"
        + " function Baz() {}\n"
        + "Baz.prototype.a = 0;\n"
        + "Baz.prototype.b = 0;\n";
    testSets(false, js, "{a=[[Bar.prototype, Foo.prototype], [Baz.prototype]],"
                 + " b=[[Bar.prototype, Foo.prototype], [Baz.prototype]]}");
    testSets(true, js, "{a=[[Bar.prototype, Foo.prototype], [Baz.prototype]],"
                 + " b=[[Bar.prototype, Foo.prototype], [Baz.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testCast
  public void testCast() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;"
        + "\n"
        + "var F = new Foo;\n"
        + "((F)).a = 0;";
    String output = ""
        + "function Foo(){}Foo.prototype.Foo_prototype$a=0;"
        + "function Bar(){}Bar.prototype.Bar_prototype$a=0;"
        + "var F=new Foo;F.Bar_prototype$a=0;";
    String ttOutput = ""
        + "function Foo(){}Foo.prototype.Foo_prototype$a=0;"
        + "function Bar(){}Bar.prototype.Bar_prototype$a=0;"
        + "var F=new Foo;F.Unique$1$a=0;";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, ttOutput,
        "{a=[[Bar.prototype], [Foo.prototype], [Unique$1]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testConstructorFields
  public void testConstructorFields() {
    String js = ""
      + "\n"
      + "var Foo = function() { this.a = 0; };\n"
      + " function Bar() {}\n"
      + "Bar.prototype.a = 0;"
      + "new Foo";
    String output = ""
        + "var Foo=function(){this.Foo$a=0};"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "new Foo";
    String ttOutput = ""
        + "var Foo=function(){this.Foo_prototype$a=0};"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "new Foo";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo]]}");
    testSets(true, js, ttOutput, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testStaticProperty
  public void testStaticProperty() {
    String js = ""
      + " function Foo() {} \n"
      + " function Bar() {}\n"
      + "Foo.a = 0;"
      + "Bar.a = 0;";
    String output = ""
        + "function Foo(){}"
        + "function Bar(){}"
        + "Foo.function__new_Foo___undefined$a = 0;"
        + "Bar.function__new_Bar___undefined$a = 0;";

    testSets(false, js, output,
        "{a=[[function (new:Bar): undefined]," +
        " [function (new:Foo): undefined]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSupertypeWithSameField
  public void testSupertypeWithSameField() {
    String js = ""
      + " function Foo() {}\n"
      + "Foo.prototype.a = 0;\n"
      + " function Bar() {}\n"
      + "\n"
      + "Bar.prototype.a = 0;\n"
      + " var B = new Bar;\n"
      + "B.a = 0;"
      + " function Baz() {}\n"
      + "Baz.prototype.a = function(){};\n";

    String output = ""
        + "function Foo(){}Foo.prototype.Foo_prototype$a=0;"
        + "function Bar(){}Bar.prototype.Foo_prototype$a=0;"
        + "var B = new Bar;B.Foo_prototype$a=0;"
        + "function Baz(){}Baz.prototype.Baz_prototype$a=function(){};";
    String ttOutput = ""
        + "function Foo(){}Foo.prototype.Foo_prototype$a=0;"
        + "function Bar(){}Bar.prototype.Bar_prototype$a=0;"
        + "var B = new Bar;B.Bar_prototype$a=0;"
        + "function Baz(){}Baz.prototype.Baz_prototype$a=function(){};";
    testSets(false, js, output, "{a=[[Baz.prototype], [Foo.prototype]]}");
    testSets(true, js, ttOutput,
        "{a=[[Bar.prototype], [Baz.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testScopedType
  public void testScopedType() {
    String js = ""
        + "var g = {};\n"
        + " g.Foo = function() {}\n"
        + "g.Foo.prototype.a = 0;"
        + " g.Bar = function() {}\n"
        + "g.Bar.prototype.a = 0;";
    String output = ""
        + "var g={};"
        + "g.Foo=function(){};"
        + "g.Foo.prototype.g_Foo_prototype$a=0;"
        + "g.Bar=function(){};"
        + "g.Bar.prototype.g_Bar_prototype$a=0;";
    testSets(false, js, output, "{a=[[g.Bar.prototype], [g.Foo.prototype]]}");
    testSets(true, js, output, "{a=[[g.Bar.prototype], [g.Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnresolvedType
  public void testUnresolvedType() {
    
    String js = ""
        + "var g = {};"
        + " "
        + "var Foo = function() {};\n"
        + "Foo.prototype.a = 0;"
        + " var Bar = function() {};\n"
        + "Bar.prototype.a = 0;";
    String output = ""
        + "var g={};"
        + "var Foo=function(){};"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "var Bar=function(){};"
        + "Bar.prototype.Bar_prototype$a=0;";
    testSets(false, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testNamedType
  public void testNamedType() {
    String js = ""
        + "var g = {};"
        + " var Foo = function() {}\n"
        + "Foo.prototype.a = 0;"
        + " var Bar = function() {}\n"
        + "Bar.prototype.a = 0;"
        + " g.Late = function() {}";
    String output = ""
        + "var g={};"
        + "var Foo=function(){};"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "var Bar=function(){};"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "g.Late = function(){}";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnknownType
  public void testUnknownType() {
    String js = ""
        + " var Foo = function() {};\n"
        + " var Bar = function() {};\n"
        + " function fun() {}\n"
        + "Foo.prototype.a = fun();\n"
        + "fun().a;\n"
        + "Bar.prototype.a = 0;";
    String ttOutput = ""
        + "var Foo=function(){};\n"
        + "var Bar=function(){};\n"
        + "function fun(){}\n"
        + "Foo.prototype.Foo_prototype$a=fun();\n"
        + "fun().Unique$1$a;\n"
        + "Bar.prototype.Bar_prototype$a=0;";
    testSets(false, js, js, "{}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES, js, ttOutput,
             "{a=[[Bar.prototype], [Foo.prototype], [Unique$1]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testEnum
  public void testEnum() {
    String js = ""
        + " var En = {\n"
        + "  A: 'first',\n"
        + "  B: 'second'\n"
        + "};\n"
        + "var EA = En.A;\n"
        + "var EB = En.B;\n"
        + " function Foo(){};\n"
        + "Foo.prototype.A = 0;\n"
        + "Foo.prototype.B = 0;\n";
    String output = ""
        + "var En={A:'first',B:'second'};"
        + "var EA=En.A;"
        + "var EB=En.B;"
        + "function Foo(){};"
        + "Foo.prototype.Foo_prototype$A=0;"
        + "Foo.prototype.Foo_prototype$B=0";
    String ttOutput = ""
        + "var En={A:'first',B:'second'};"
        + "var EA=En.A;"
        + "var EB=En.B;"
        + "function Foo(){};"
        + "Foo.prototype.Foo_prototype$A=0;"
        + "Foo.prototype.Foo_prototype$B=0";
    testSets(false, js, output, "{A=[[Foo.prototype]], B=[[Foo.prototype]]}");
    testSets(true, js, ttOutput, "{A=[[Foo.prototype]], B=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testEnumOfObjects
  public void testEnumOfObjects() {
    String js = ""
        + " function Formatter() {}"
        + "Formatter.prototype.format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter()\n"
        + "};\n"
        + "Enum.A.format();\n";
    String output = ""
        + " function Formatter() {}"
        + "Formatter.prototype.Formatter_prototype$format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.Unrelated_prototype$format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter()\n"
        + "};\n"
        + "Enum.A.Formatter_prototype$format();\n";
    testSets(false, js, output,
        "{format=[[Formatter.prototype], [Unrelated.prototype]]}");

    
    
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testEnumOfObjects2
  public void testEnumOfObjects2() {
    String js = ""
        + " function Formatter() {}"
        + "Formatter.prototype.format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter(),\n"
        + "  B: new Formatter()\n"
        + "};\n"
        + "function f() {\n"
        + "  var formatter = window.toString() ? Enum.A : Enum.B;\n"
        + "  formatter.format();\n"
        + "}";
    String output = ""
        + " function Formatter() {}"
        + "Formatter.prototype.format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter(),\n"
        + "  B: new Formatter()\n"
        + "};\n"
        + "function f() {\n"
        + "  var formatter = window.toString() ? Enum.A : Enum.B;\n"
        + "  formatter.format();\n"
        + "}";
    testSets(false, js, output, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testEnumOfObjects3
  public void testEnumOfObjects3() {
    String js = ""
        + " function Formatter() {}"
        + "Formatter.prototype.format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter(),\n"
        + "  B: new Formatter()\n"
        + "};\n"
        + " var SubEnum = {\n"
        + "  C: Enum.A\n"
        + "};\n"
        + "function f() {\n"
        + "  var formatter = SubEnum.C\n"
        + "  formatter.format();\n"
        + "}";
    String output = ""
        + " function Formatter() {}"
        + "Formatter.prototype.Formatter_prototype$format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.Unrelated_prototype$format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter(),\n"
        + "  B: new Formatter()\n"
        + "};\n"
        + " var SubEnum = {\n"
        + "  C: Enum.A\n"
        + "};\n"
        + "function f() {\n"
        + "  var formatter = SubEnum.C\n"
        + "  formatter.Formatter_prototype$format();\n"
        + "}";
    testSets(false, js, output,
        "{format=[[Formatter.prototype], [Unrelated.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUntypedExterns
  public void testUntypedExterns() {
    String externs =
        BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES
        + "var window;"
        + "window.alert = function() {x};";
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "Foo.prototype.alert = 0;\n"
        + "Foo.prototype.window = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "Bar.prototype.alert = 0;\n"
        + "Bar.prototype.window = 0;\n"
        + "window.alert();";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "Foo.prototype.alert=0;"
        + "Foo.prototype.Foo_prototype$window=0;"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "Bar.prototype.alert=0;"
        + "Bar.prototype.Bar_prototype$window=0;"
        + "window.alert();";

    testSets(false, externs, js, output, "{a=[[Bar.prototype], [Foo.prototype]]"
             + ", window=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, externs, js, output, "{a=[[Bar.prototype], [Foo.prototype]],"
             + " window=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionTypeInvalidation
  public void testUnionTypeInvalidation() {
    String externs = ""
        + " function Baz() {}"
        + "Baz.prototype.a";
    String js = ""
        + " function Ind() {this.a=0}\n"
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 1\n;"
        + "F = new Bar;\n"
        + "\n"
        + "var Z = new Baz;\n"
        + "Z.a = 1\n;"
        + "\n"
        + "var B = new Baz;\n"
        + "B.a = 1;\n"
        + "B = new Bar;\n";
    
    
    String output = ""
        + "function Ind() { this.Ind$a = 0; }"
        + "function Foo() {}"
        + "Foo.prototype.a = 0;"
        + "function Bar() {}"
        + "Bar.prototype.a = 0;"
        + "var F = new Foo;"
        + "F.a = 1;"
        + "F = new Bar;"
        + "var Z = new Baz;"
        + "Z.a = 1;"
        + "var B = new Baz;"
        + "B.a = 1;"
        + "B = new Bar;";
    String ttOutput = ""
        + "function Ind() { this.Unique$1$a = 0; }"
        + "function Foo() {}"
        + "Foo.prototype.a = 0;"
        + "function Bar() {}"
        + "Bar.prototype.a = 0;"
        + "var F = new Foo;"
        + "F.a = 1;"
        + "F = new Bar;"
        + "var Z = new Baz;"
        + "Z.a = 1;"
        + "var B = new Baz;"
        + "B.a = 1;"
        + "B = new Bar;";
    testSets(false, externs, js, output, "{a=[[Ind]]}");
    testSets(true, externs, js, ttOutput, "{a=[[Unique$1]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionAndExternTypes
  public void testUnionAndExternTypes() {
    String externs = ""
      + " function Foo() { }"
      + "Foo.prototype.a = 4;\n";
    String js = ""
      + " function Bar() { this.a = 2; }\n"
      + " function Baz() { this.a = 3; }\n"
      + " function Buz() { this.a = 4; }\n"
      + " function T1() { this.a = 3; }\n"
      + " function T2() { this.a = 3; }\n"
      + " var b;\n"
      + " var c;\n"
      + " var d;\n"
      + "b.a = 5; c.a = 6; d.a = 7;";
    String output = ""
      + " function Bar() { this.a = 2; }\n"
      + " function Baz() { this.a = 3; }\n"
      + " function Buz() { this.a = 4; }\n"
      + " function T1() { this.T1$a = 3; }\n"
      + " function T2() { this.T2$a = 3; }\n"
      + " var b;\n"
      + " var c;\n"
      + " var d;\n"
      + "b.a = 5; c.a = 6; d.a = 7;";

    
    
    testSets(false, externs, js, output, "{a=[[T1], [T2]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTypedExterns
  public void testTypedExterns() {
    String externs = ""
        + " function Window() {};\n"
        + "Window.prototype.alert;"
        + ""
        + "var window;";
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.alert = 0;\n"
        + "window.alert('blarg');";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Foo_prototype$alert=0;"
        + "window.alert('blarg');";
    testSets(false, externs, js, output, "{alert=[[Foo.prototype]]}");
    testSets(true, externs, js, output, "{alert=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSubtypesWithSameField
  public void testSubtypesWithSameField() {
    String js = ""
        + " function Top() {}\n"
        + " function Foo() {}\n"
        + "Foo.prototype.a;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a;\n"
        + ""
        + "function foo(top) {\n"
        + "  var x = top.a;\n"
        + "}\n"
        + "foo(new Foo);\n"
        + "foo(new Bar);\n";
    testSets(false, js, "{}");
    testSets(true, js, "{a=[[Bar.prototype, Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSupertypeReferenceOfSubtypeProperty
  public void testSupertypeReferenceOfSubtypeProperty() {
    String externs = ""
        + " function Ext() {}"
        + "Ext.prototype.a;";
    String js = ""
        + " function Foo() {}\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a;\n"
        + ""
        + "function foo(foo) {\n"
        + "  var x = foo.a;\n"
        + "}\n";
    String result = ""
        + "function Foo() {}\n"
        + "function Bar() {}\n"
        + "Bar.prototype.Bar_prototype$a;\n"
        + "function foo(foo$$1) {\n"
        + "  var x = foo$$1.Bar_prototype$a;\n"
        + "}\n";
    testSets(false, externs, js, result, "{a=[[Bar.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testObjectLiteralNotRenamed
  public void testObjectLiteralNotRenamed() {
    String js = ""
        + "var F = {a:'a', b:'b'};"
        + "F.a = 'z';";
    testSets(false, js, js, "{}");
    testSets(true, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testObjectLiteralReflected
  public void testObjectLiteralReflected() {
    String js = ""
        + "var goog = {};"
        + "goog.reflect = {};"
        + "goog.reflect.object = function(x, y) { return y; };"
        + " function F() {}"
        + " F.prototype.foo = 3;"
        + " function G() {}"
        + " G.prototype.foo = 3;"
        + "goog.reflect.object(F, {foo: 5});";
    String result = ""
        + "var goog = {};"
        + "goog.reflect = {};"
        + "goog.reflect.object = function(x, y) { return y; };"
        + "function F() {}"
        + "F.prototype.F_prototype$foo = 3;"
        + "function G() {}"
        + "G.prototype.G_prototype$foo = 3;"
        + "goog.reflect.object(F, {F_prototype$foo: 5});";
    testSets(false, js, result, "{foo=[[F.prototype], [G.prototype]]}");
    testSets(true, js, result, "{foo=[[F.prototype], [G.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testObjectLiteralLends
  public void testObjectLiteralLends() {
    String js = ""
        + "var mixin = function(x) { return x; };"
        + " function F() {}"
        + " F.prototype.foo = 3;"
        + " function G() {}"
        + " G.prototype.foo = 3;"
        + "mixin( ({foo: 5}));";
    String result = ""
        + "var mixin = function(x) { return x; };"
        + "function F() {}"
        + "F.prototype.F_prototype$foo = 3;"
        + "function G() {}"
        + "G.prototype.G_prototype$foo = 3;"
        + "mixin( ({F_prototype$foo: 5}));";
    testSets(false, js, result, "{foo=[[F.prototype], [G.prototype]]}");
    testSets(true, js, result, "{foo=[[F.prototype], [G.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testClosureInherits
  public void testClosureInherits() {
    String js = ""
        + "var goog = {};"
        + "\n"
        + "goog.inherits = function(childCtor, parentCtor) {\n"
        + "  \n"
        + "  function tempCtor() {};\n"
        + "  tempCtor.prototype = parentCtor.prototype;\n"
        + "  childCtor.superClass_ = parentCtor.prototype;\n"
        + "  childCtor.prototype = new tempCtor();\n"
        + "  childCtor.prototype.constructor = childCtor;\n"
        + "};"
        + " function Top() {}\n"
        + "Top.prototype.f = function() {};"
        + " function Foo() {}\n"
        + "goog.inherits(Foo, Top);\n"
        + "\n"
        + "Foo.prototype.f = function() {"
        + "  Foo.superClass_.f();"
        + "};\n"
        + " function Bar() {}\n"
        + "goog.inherits(Bar, Foo);\n"
        + "\n"
        + "Bar.prototype.f = function() {"
        + "  Bar.superClass_.f();"
        + "};\n"
        + "(new Bar).f();\n";
    testSets(false, js, "{f=[[Top.prototype]]}");
    testSets(true, js, "{constructor=[[Bar.prototype, Foo.prototype]], "
                 + "f=[[Bar.prototype], [Foo.prototype], [Top.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSkipNativeFunctionMethod
  public void testSkipNativeFunctionMethod() {
    String externs = ""
        + ""
        + "function Function(var_args) {}"
        + "Function.prototype.call = function() {};";
    String js = ""
        + " function Foo(){};"
        + ""
        + "function Bar() { Foo.call(this); };"; 
    testSame(externs, js, null);
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSkipNativeObjectMethod
  public void testSkipNativeObjectMethod() {
    String externs = ""
        + " function Object(opt_v) {}"
        + "Object.prototype.hasOwnProperty;";
    String js = ""
        + " function Foo(){};"
        + "(new Foo).hasOwnProperty('x');";
    testSets(false, externs, js, js, "{}");
    testSets(true, externs, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testExtendNativeType
  public void testExtendNativeType() {
    String externs = ""
        + ""
        + "function Date(opt_1, opt_2, opt_3, opt_4, opt_5, opt_6, opt_7) {}"
        + " Date.prototype.toString = function() {}";
    String js = ""
        + " function SuperDate() {};\n"
        + "(new SuperDate).toString();";
    testSets(true, externs, js, js, "{}");
    testSets(false, externs, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testStringFunction
  public void testStringFunction() {
    
    
    String externs = ""
         + "function String(opt_str) {};\n"
         + "\n"
         + "String.prototype.toString = function() { };\n";
    String js = ""
         + " function Foo() {};\n"
         + "Foo.prototype.foo = function() {};\n"
         + "String.prototype.foo = function() {};\n"
         + "var a = 'str'.toString().foo();\n";
    String output = ""
         + "function Foo() {};\n"
         + "Foo.prototype.Foo_prototype$foo = function() {};\n"
         + "String.prototype.String_prototype$foo = function() {};\n"
         + "var a = 'str'.toString().String_prototype$foo();\n";

    testSets(false, externs, js, output,
             "{foo=[[Foo.prototype], [String.prototype]]}");
    testSets(true, externs, js, output,
             "{foo=[[Foo.prototype], [String.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnusedTypeInExterns
  public void testUnusedTypeInExterns() {
    String externs = ""
        + " function Foo() {};\n"
        + "Foo.prototype.a";
    String js = ""
        + " function Bar() {};\n"
        + "Bar.prototype.a;"
        + " function Baz() {};\n"
        + "Baz.prototype.a;";
    String output = ""
        + " function Bar() {};\n"
        + "Bar.prototype.Bar_prototype$a;"
        + " function Baz() {};\n"
        + "Baz.prototype.Baz_prototype$a";
    testSets(false, externs, js, output,
             "{a=[[Bar.prototype], [Baz.prototype]]}");
    testSets(true, externs, js, output,
             "{a=[[Bar.prototype], [Baz.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testInterface
  public void testInterface() {
    String js = ""
        + " function I() {};\n"
        + "I.prototype.a;\n"
        + " function Foo() {};\n"
        + "Foo.prototype.a;\n"
        + "\n"
        + "var F = new Foo;"
        + "var x = F.a;";
    testSets(false, js, "{a=[[Foo.prototype, I.prototype]]}");
    testSets(true, js, "{a=[[Foo.prototype], [I.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testInterfaceOfSuperclass
  public void testInterfaceOfSuperclass() {
    String js = ""
        + " function I() {};\n"
        + "I.prototype.a;\n"
        + " function Foo() {};\n"
        + "Foo.prototype.a;\n"
        + " function Bar() {};\n"
        + "Bar.prototype.a;\n"
        + "\n"
        + "var B = new Bar;"
        + "B.a = 0";
    testSets(false, js, "{a=[[Foo.prototype, I.prototype]]}");
    testSets(true, js,
        "{a=[[Bar.prototype], [Foo.prototype], [I.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoInterfacesWithSomeInheritance
  public void testTwoInterfacesWithSomeInheritance() {
    String js = ""
        + " function I() {};\n"
        + "I.prototype.a;\n"
        + " function I2() {};\n"
        + "I2.prototype.a;\n"
        + " function Foo() {};\n"
        + "Foo.prototype.a;\n"
        + "\n"
        + "function Bar() {};\n"
        + "Bar.prototype.a;\n"
        + "\n"
        + "var B = new Bar;"
        + "B.a = 0";
    testSets(false, js, "{a=[[Foo.prototype, I.prototype, I2.prototype]]}");
    testSets(true, js, "{a=[[Bar.prototype], [Foo.prototype], "
                       + "[I.prototype], [I2.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testInvalidatingInterface
  public void testInvalidatingInterface() {
    String js = ""
        + " function I2() {};\n"
        + "I2.prototype.a;\n"
        + " function Bar() {}\n"
        + "\n"
        + "var i = new Bar;\n" 
        + ""
        + "function Foo() {};\n"
        + "\n"
        + "Foo.prototype.a = 0;\n"
        + "(new Foo).a = 0;"
        + " function I() {};\n"
        + "I.prototype.a;\n";
    testSets(false, js, "{}", TypeValidator.TYPE_MISMATCH_WARNING);
    testSets(true, js, "{}", TypeValidator.TYPE_MISMATCH_WARNING);
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testMultipleInterfaces
  public void testMultipleInterfaces() {
    String js = ""
        + " function I() {};\n"
        + " function I2() {};\n"
        + "I2.prototype.a;\n"
        + ""
        + "function Foo() {};\n"
        + ""
        + "Foo.prototype.a = 0;\n"
        + "(new Foo).a = 0";
    testSets(false, js, "{a=[[Foo.prototype, I2.prototype]]}");
    testSets(true, js, "{a=[[Foo.prototype], [I2.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testInterfaceWithSupertypeImplementor
  public void testInterfaceWithSupertypeImplementor() {
    String js = ""
        + " function C() {}\n"
        + "C.prototype.foo = function() {};\n"
        + " function A (){}\n"
        + "A.prototype.foo = function() {};\n"
        + "\n"
        + "function B() {}\n"
        + " var b = new B();\n"
        + "b.foo();\n";
    testSets(false, js, "{foo=[[A.prototype, C.prototype]]}");
    testSets(true, js, "{foo=[[A.prototype], [C.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSuperInterface
  public void testSuperInterface() {
    String js = ""
        + " function I() {};\n"
        + "I.prototype.a;\n"
        + " function I2() {};\n"
        + ""
        + "function Foo() {};\n"
        + "\n"
        + "Foo.prototype.a = 0;\n"
        + "(new Foo).a = 0";
    testSets(false, js, "{a=[[Foo.prototype, I.prototype]]}");
    testSets(true, js, "{a=[[Foo.prototype], [I.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testInterfaceUnionWithCtor
  public void testInterfaceUnionWithCtor() {
    String js = ""
        + " function I() {};\n"
        + " I.prototype.addEventListener;\n"
        + " function Impl() {};\n"
        + " Impl.prototype.addEventListener;"
        + " function C() {};\n"
        + " C.prototype.addEventListener;"
        + ""
        + "function f(x) { x.addEventListener(); };\n"
        + "f(new C()); f(new Impl());";

    testSets(false, js, js,
        "{addEventListener=[[C.prototype, I.prototype, Impl.prototype]]}");

    
    
    String tightenedOutput = ""
        + "function I() {};\n"
        + "I.prototype.I_prototype$addEventListener;\n"
        + "function Impl() {};\n"
        + "Impl.prototype.C_prototype$addEventListener;"
        + "function C() {};\n"
        + "C.prototype.C_prototype$addEventListener;"
        + ""
        + "function f(x) { x.C_prototype$addEventListener(); };\n"
        + "f(new C()); f(new Impl());";

    testSets(true, js, tightenedOutput,
        "{addEventListener=[[C.prototype, Impl.prototype], [I.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testExternInterfaceUnionWithCtor
  public void testExternInterfaceUnionWithCtor() {
    String externs = ""
        + " function I() {};\n"
        + " I.prototype.addEventListener;\n"
        + " function Impl() {};\n"
        + " Impl.prototype.addEventListener;";

    String js = ""
        + " function C() {};\n"
        + " C.prototype.addEventListener;"
        + ""
        + "function f(x) { x.addEventListener(); };\n"
        + "f(new C()); f(new Impl());";

    testSets(false, externs, js, js, "{}");
    testSets(true, externs, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testMismatchInvalidation
  public void testMismatchInvalidation() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "\n"
        + "var F = new Bar;\n"
        + "F.a = 0;";

    testSets(false, "", js, js, "{}", TypeValidator.TYPE_MISMATCH_WARNING,
             "initializing variable\n"
             + "found   : Bar\n"
             + "required: (Foo|null)");
    testSets(true, "", js, js, "{}", TypeValidator.TYPE_MISMATCH_WARNING,
             "initializing variable\n"
             + "found   : Bar\n"
             + "required: (Foo|null)");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testBadCast
  public void testBadCast() {
    String js = " function Foo() {};\n"
        + "Foo.prototype.a = 0;\n"
        + " function Bar() {};\n"
        + "Bar.prototype.a = 0;\n"
        + "var a =  (new Bar);\n"
        + "a.a = 4;";
    testSets(false, "", js, js, "{}",
             TypeValidator.INVALID_CAST,
             "invalid cast - must be a subtype or supertype\n"
             + "from: Bar\n"
             + "to  : Foo");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testDeterministicNaming
  public void testDeterministicNaming() {
    String js =
        "function A() {}\n"
        + "A.prototype.f = function() {return 'a';};\n"
        + "function B() {}\n"
        + "B.prototype.f = function() {return 'b';};\n"
        + "function C() {}\n"
        + "C.prototype.f = function() {return 'c';};\n"
        + "var ab = 1 ? new B : new A;\n"
        + "var n = ab.f();\n";

    String output =
        "function A() {}\n"
        + "A.prototype.A_prototype$f = function() { return'a'; };\n"
        + "function B() {}\n"
        + "B.prototype.A_prototype$f = function() { return'b'; };\n"
        + "function C() {}\n"
        + "C.prototype.C_prototype$f = function() { return'c'; };\n"
        + "var ab = 1 ? new B : new A; var n = ab.A_prototype$f();\n";

    for (int i = 0; i < 5; i++) {
      testSets(false, js, output,
          "{f=[[A.prototype, B.prototype], [C.prototype]]}");

      testSets(true, js, output,
          "{f=[[A.prototype, B.prototype], [C.prototype]]}");
    }
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testObjectLiteral
  public void testObjectLiteral() {
    String js = " function Foo() {}\n"
        + "Foo.prototype.a;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a;\n"
        + "var F = ({ a: 'a' });\n";

    String output = "function Foo() {}\n"
        + "Foo.prototype.Foo_prototype$a;\n"
        + "function Bar() {}\n"
        + "Bar.prototype.Bar_prototype$a;\n"
        + "var F = { Foo_prototype$a: 'a' };\n";

    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testCustomInherits
  public void testCustomInherits() {
    String js = "Object.prototype.inheritsFrom = function(shuper) {\n" +
        "  \n" +
        "  function Inheriter() { }\n" +
        "  Inheriter.prototype = shuper.prototype;\n" +
        "  this.prototype = new Inheriter();\n" +
        "  this.superConstructor = shuper;\n" +
        "};\n" +
        "function Foo(var1, var2, strength) {\n" +
        "  Foo.superConstructor.call(this, strength);\n" +
        "}" +
        "Foo.inheritsFrom(Object);";

    String externs = "" +
        "function Function(var_args) {}" +
        "Function.prototype.call = function(var_args) {};";

    testSets(false, externs, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSkipNativeFunctionStaticProperty
  public void testSkipNativeFunctionStaticProperty() {
    String js = ""
      + "\n"
      + "function addSingletonGetter(ctor) { ctor.a; }\n"
      + " function Foo() {}\n"
      + "Foo.a = 0;"
      + " function Bar() {}\n"
      + "Bar.a = 0;";

    String output = ""
        + "function addSingletonGetter(ctor){ctor.a}"
        + "function Foo(){}"
        + "Foo.a=0;"
        + "function Bar(){}"
        + "Bar.a=0";

    testSets(false, js, output, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testErrorOnProtectedProperty
  public void testErrorOnProtectedProperty() {
    test("function addSingletonGetter(foo) { foo.foobar = 'a'; };", null,
         DisambiguateProperties.Warnings.INVALIDATION);
    assertTrue(getLastCompiler().getErrors()[0].toString().contains("foobar"));
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testMismatchForbiddenInvalidation
  public void testMismatchForbiddenInvalidation() {
    test(" function F() {}" +
         " F.prototype.foobar = 3;" +
         " function g() { return new F(); }",
         null,
         DisambiguateProperties.Warnings.INVALIDATION);
    assertTrue(getLastCompiler().getErrors()[0].toString()
        .contains("Consider fixing errors"));
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionTypeInvalidationError
  public void testUnionTypeInvalidationError() {
    String externs = ""
        + " function Baz() {}"
        + "Baz.prototype.foobar";
    String js = ""
        + " function Ind() {this.foobar=0}\n"
        + " function Foo() {}\n"
        + "Foo.prototype.foobar = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.foobar = 0;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.foobar = 1\n;"
        + "F = new Bar;\n"
        + "\n"
        + "var Z = new Baz;\n"
        + "Z.foobar = 1\n;";

    test(
        externs, js, "",
        DisambiguateProperties.Warnings.INVALIDATION_ON_TYPE, null);
    assertTrue(getLastCompiler().getErrors()[0].toString()
        .contains("foobar"));
   }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression1
  public void testCanExposeExpression1() {
    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "while(foo());", "foo");
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "while(x = goo()&&foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "while(x += goo()&&foo()){}", "foo");

    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "do{}while(foo());", "foo");
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "for(;foo(););", "foo");
    
    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "for(;;foo());", "foo");
    
    

    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "switch(1){case foo():;}", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression2
  public void testCanExposeExpression2() {
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "x = foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "var x = foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "if(foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "switch(foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "switch(foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "function f(){ return foo();}", "foo");

    helperCanExposeExpression(
        DecompositionType.MOVABLE, "x = foo() && 1", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "x = foo() || 1", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "x = foo() ? 0 : 1", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "(function(a){b = a})(foo())", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression3
  public void testCanExposeExpression3() {
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "x = 0 && foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "x = 1 || foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "var x = 1 ? foo() : 0", "foo");

    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "goo() && foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "x = goo() && foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "x += goo() && foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "var x = goo() && foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "if(goo() && foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "switch(goo() && foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "switch(goo() && foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "switch(x = goo() && foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE,
        "function f(){ return goo() && foo();}", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression4
  public void testCanExposeExpression4() {
    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "if (goo.a(1, foo()));", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression5
  public void testCanExposeExpression5() {
    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "if (goo['a'](foo()));", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression6
  public void testCanExposeExpression6() {
    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "z:if (goo.a(1, foo()));", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression7
  public void testCanExposeExpression7() {
    
    helperCanExposeFunctionExpression(
        DecompositionType.MOVABLE,
        "(function(map){descriptions_=map})(\n" +
            "function(){\n" +
                "var ret={};\n" +
                "ret[INIT]='a';\n" +
                "ret[MIGRATION_BANNER_DISMISS]='b';\n" +
                "return ret\n" +
            "}()\n" +
        ");", 2);
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression8
  public void testCanExposeExpression8() {
    
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE,
        "HangoutStarter.prototype.launchHangout = function() {\n" +
        "  var self = a.b;\n" +
        "  var myUrl = new goog.Uri(getDomServices_(self).getDomHelper()." +
        "getWindow().location.href);\n" +
        "};",
        "getDomServices_");

    
    helperExposeExpression(
        "HangoutStarter.prototype.launchHangout = function() {\n" +
        "  var self = a.b;\n" +
        "  var myUrl = new goog.Uri(getDomServices_(self).getDomHelper()." +
        "getWindow().location.href);\n" +
        "};",
        "getDomServices_",
        "HangoutStarter.prototype.launchHangout = function() {" +
        "  var self = a.b;" +
        "  var temp_const$$0 = goog.Uri;" +
        "  var myUrl = new temp_const$$0(getDomServices_(self)." +
        "      getDomHelper().getWindow().location.href)}");

    
    helperMoveExpression(
        "HangoutStarter.prototype.launchHangout = function() {" +
        "  var self = a.b;" +
        "  var temp_const$$0 = goog.Uri;" +
        "  var myUrl = new temp_const$$0(getDomServices_(self)." +
        "      getDomHelper().getWindow().location.href)}",
        "getDomServices_",
        "HangoutStarter.prototype.launchHangout = function() {" +
        "  var self=a.b;" +
        "  var temp_const$$0=goog.Uri;" +
        "  var result$$0=getDomServices_(self);" +
        "  var myUrl=new temp_const$$0(result$$0.getDomHelper()." +
        "      getWindow().location.href)}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression1
  public void testMoveExpression1() {
    
    helperMoveExpression("foo()", "foo", "var result$$0 = foo(); result$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression2
  public void testMoveExpression2() {
    helperMoveExpression(
        "x = foo()",
        "foo",
        "var result$$0 = foo(); x = result$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression3
  public void testMoveExpression3() {
    helperMoveExpression(
        "var x = foo()",
        "foo",
        "var result$$0 = foo(); var x = result$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression4
  public void testMoveExpression4() {
    helperMoveExpression(
        "if(foo()){}",
        "foo",
        "var result$$0 = foo(); if (result$$0);");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression5
  public void testMoveExpression5() {
    helperMoveExpression(
        "switch(foo()){}",
        "foo",
        "var result$$0 = foo(); switch(result$$0){}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression6
  public void testMoveExpression6() {
    helperMoveExpression(
        "switch(1 + foo()){}",
        "foo",
        "var result$$0 = foo(); switch(1 + result$$0){}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression7
  public void testMoveExpression7() {
    helperMoveExpression(
        "function f(){ return foo();}",
        "foo",
        "function f(){ var result$$0 = foo(); return result$$0;}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression8
  public void testMoveExpression8() {
    helperMoveExpression(
        "x = foo() && 1",
        "foo",
        "var result$$0 = foo(); x = result$$0 && 1");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression9
  public void testMoveExpression9() {
    helperMoveExpression(
        "x = foo() || 1",
        "foo",
        "var result$$0 = foo(); x = result$$0 || 1");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression10
  public void testMoveExpression10() {
    helperMoveExpression(
        "x = foo() ? 0 : 1",
        "foo",
        "var result$$0 = foo(); x = result$$0 ? 0 : 1");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression1
  public void testExposeExpression1() {
    helperExposeExpression(
        "x = 0 && foo()",
        "foo",
        "var temp$$0; if (temp$$0 = 0) temp$$0 = foo(); x = temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression2
  public void testExposeExpression2() {
    helperExposeExpression(
        "x = 1 || foo()",
        "foo",
        "var temp$$0; if (temp$$0 = 1); else temp$$0 = foo(); x = temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression3
  public void testExposeExpression3() {
    helperExposeExpression(
        "var x = 1 ? foo() : 0",
        "foo",
        "var temp$$0;" +
        " if (1) temp$$0 = foo(); else temp$$0 = 0;var x = temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression4
  public void testExposeExpression4() {
    helperExposeExpression(
        "goo() && foo()",
        "foo",
        "if (goo()) foo();");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression5
  public void testExposeExpression5() {
    helperExposeExpression(
        "x = goo() && foo()",
        "foo",
        "var temp$$0; if (temp$$0 = goo()) temp$$0 = foo(); x = temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression6
  public void testExposeExpression6() {
    helperExposeExpression(
        "var x = 1 + (goo() && foo())",
        "foo",
        "var temp$$0; if (temp$$0 = goo()) temp$$0 = foo();" +
        "var x = 1 + temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression7
  public void testExposeExpression7() {
    helperExposeExpression(
        "if(goo() && foo());",
        "foo",
        "var temp$$0;" +
        "if (temp$$0 = goo()) temp$$0 = foo();" +
        "if(temp$$0);");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression8
  public void testExposeExpression8() {
    helperExposeExpression(
        "switch(goo() && foo()){}",
        "foo",
        "var temp$$0;" +
        "if (temp$$0 = goo()) temp$$0 = foo();" +
        "switch(temp$$0){}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression9
  public void testExposeExpression9() {
    helperExposeExpression(
        "switch(1 + goo() + foo()){}",
        "foo",
        "var temp_const$$0 = 1 + goo();" +
        "switch(temp_const$$0 + foo()){}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression10
  public void testExposeExpression10() {
    helperExposeExpression(
        "function f(){ return goo() && foo();}",
        "foo",
        "function f(){" +
          "var temp$$0; if (temp$$0 = goo()) temp$$0 = foo();" +
          "return temp$$0;" +
         "}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression11
  public void testExposeExpression11() {
    
    
    helperExposeExpression(
        "if (goo(1, goo(2), (1 ? foo() : 0)));",
        "foo",
        "var temp_const$$1 = goo;" +
        "var temp_const$$0 = goo(2);" +
        "var temp$$2;" +
        "if (1) temp$$2 = foo(); else temp$$2 = 0;" +
        "if (temp_const$$1(1, temp_const$$0, temp$$2));");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposePlusEquals1
  public void testExposePlusEquals1() {
    helperExposeExpression(
        "var x = 0; x += foo() + 1",
        "foo",
        "var x = 0; var temp_const$$0 = x;" +
        "x = temp_const$$0 + (foo() + 1);");

    helperExposeExpression(
        "var x = 0; y = (x += foo()) + x",
        "foo",
        "var x = 0; var temp_const$$0 = x;" +
        "y = (x = temp_const$$0 + foo()) + x");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposePlusEquals2
  public void testExposePlusEquals2() {
    helperExposeExpression(
        "var x = {}; x.a += foo() + 1",
        "foo",
        "var x = {}; var temp_const$$0 = x;" +
        "var temp_const$$1 = temp_const$$0.a;" +
        "temp_const$$0.a = temp_const$$1 + (foo() + 1);");

    helperExposeExpression(
        "var x = {}; y = (x.a += foo()) + x.a",
        "foo",
        "var x = {}; var temp_const$$0 = x;" +
        "var temp_const$$1 = temp_const$$0.a;" +
        "y = (temp_const$$0.a = temp_const$$1 + foo()) + x.a");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposePlusEquals3
  public void testExposePlusEquals3() {
    helperExposeExpression(
        " var XX = {};\n" +
        "XX.a += foo() + 1",
        "foo",
        "var XX = {}; var temp_const$$0 = XX.a;" +
        "XX.a = temp_const$$0 + (foo() + 1);");

    helperExposeExpression(
        "var XX = {}; y = (XX.a += foo()) + XX.a",
        "foo",
        "var XX = {}; var temp_const$$0 = XX.a;" +
        "y = (XX.a = temp_const$$0 + foo()) + XX.a");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposePlusEquals4
  public void testExposePlusEquals4() {
    helperExposeExpression(
        "var x = {}; goo().a += foo() + 1",
        "foo",
        "var x = {};" +
        "var temp_const$$0 = goo();" +
        "var temp_const$$1 = temp_const$$0.a;" +
        "temp_const$$0.a = temp_const$$1 + (foo() + 1);");

    helperExposeExpression(
        "var x = {}; y = (goo().a += foo()) + goo().a",
        "foo",
        "var x = {};" +
        "var temp_const$$0 = goo();" +
        "var temp_const$$1 = temp_const$$0.a;" +
        "y = (temp_const$$0.a = temp_const$$1 + foo()) + goo().a");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposePlusEquals5
  public void testExposePlusEquals5() {
    helperExposeExpression(
        "var x = {}; goo().a.b += foo() + 1",
        "foo",
        "var x = {};" +
        "var temp_const$$0 = goo().a;" +
        "var temp_const$$1 = temp_const$$0.b;" +
        "temp_const$$0.b = temp_const$$1 + (foo() + 1);");

    helperExposeExpression(
        "var x = {}; y = (goo().a.b += foo()) + goo().a",
        "foo",
        "var x = {};" +
        "var temp_const$$0 = goo().a;" +
        "var temp_const$$1 = temp_const$$0.b;" +
        "y = (temp_const$$0.b = temp_const$$1 + foo()) + goo().a");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeObjectLit1
  public void testExposeObjectLit1() {
    
    
    
    
    helperMoveExpression(
        "var x = {get a() {}, b: foo()};",
        "foo",
        "var result$$0=foo();var x = {get a() {}, b: result$$0};");

    helperMoveExpression(
        "var x = {set a(p) {}, b: foo()};",
        "foo",
        "var result$$0=foo();var x = {set a(p) {}, b: result$$0};");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbol
  public void testExportSymbol() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportSymbol('foobar', a.b.c)",
                    "\n" +
                    "var foobar = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolDefinedInVar
  public void testExportSymbolDefinedInVar() throws Exception {
    compileAndCheck("var a = function(d, e, f) {};" +
                    "goog.exportSymbol('foobar', a)",
                    "\n" +
                    "var foobar = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportProperty
  public void testExportProperty() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportProperty(a.b, 'cprop', a.b.c)",
                    "var a;\n" +
                    "a.b;\n" +
                    "\n" +
                    "a.b.cprop = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple
  public void testExportMultiple() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('a.b', a.b);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);" +
                    "goog.exportProperty(a.b.prototype, 'c', a.b.prototype.c);",

                    "var a;\n" +
                    "\n" +
                    "a.b = function(p1) {\n};\n" +
                    "\n" +
                    "a.b.c = function(d, e, f) {\n};\n" +
                    "\n" +
                    "a.b.prototype.c = function(g, h, i) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple2
  public void testExportMultiple2() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('hello', a);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);" +
                    "goog.exportProperty(a.b.prototype, 'c', a.b.prototype.c);",

                    "\n" +
                    "var hello = {};\n" +
                    "hello.b;\n" +
                    "\n" +
                    "hello.b.c = function(d, e, f) {\n};\n" +
                    "\n" +
                    "hello.b.prototype.c = function(g, h, i) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple3
  public void testExportMultiple3() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('prefix', a.b);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);",

                    "\n" +
                    "var prefix = function(p1) {\n};\n" +
                    "\n" +
                    "prefix.c = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportNonStaticSymbol
  public void testExportNonStaticSymbol() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; var d = {}; a.b.c = d;" +
                    "goog.exportSymbol('foobar', a.b.c)",
                    "var foobar;\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportNonStaticSymbol2
  public void testExportNonStaticSymbol2() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; var d = null; a.b.c = d;" +
                    "goog.exportSymbol('foobar', a.b.c())",
                    "var foobar;\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportNonexistentProperty
  public void testExportNonexistentProperty() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportProperty(a.b, 'none', a.b.none)",
                    "var a;\n" +
                    "a.b;\n" +
                    "a.b.none;\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithTypeAnnotation
  public void testExportSymbolWithTypeAnnotation() {

    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function(param1, param2) {" +
                      "return param1 + param2;" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "\n" +
                    "var externalName = function(param1, param2) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithTemplateAnnotation
  public void testExportSymbolWithTemplateAnnotation() {

    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function(param1) {" +
                      "return param1;" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "\n" +
                    "var externalName = function(param1) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithMultipleTemplateAnnotation
  public void testExportSymbolWithMultipleTemplateAnnotation() {

    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function(param1) {" +
                      "return  (param1);" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "\n" +
                    "var externalName = function(param1) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithoutTypeCheck
  public void testExportSymbolWithoutTypeCheck() {
    
    
    setRunCheckTypes(false);

    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function(param1, param2) {" +
                      "return param1 + param2;" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "var externalName = function(param1, param2) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithConstructor
  public void testExportSymbolWithConstructor() {
    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function() {" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "\n" +
                    "var externalName = function() {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithConstructorWithoutTypeCheck
  public void testExportSymbolWithConstructorWithoutTypeCheck() {
    
    
    
    
    

    setRunCheckTypes(false);

    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function() {" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "var externalName = function() {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportFunctionWithOptionalArguments1
  public void testExportFunctionWithOptionalArguments1() {
    compileAndCheck("var internalName;\n" +
        "\n" +
        "internalName = function(a) {" +
        "};" +
        "goog.exportSymbol('externalName', internalName)",
        "\n" +
        "var externalName = function(a) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportFunctionWithOptionalArguments2
  public void testExportFunctionWithOptionalArguments2() {
    compileAndCheck("var internalName;\n" +
        "\n" +
        "internalName = function(a) {" +
        "  return 6;\n" +
        "};" +
        "goog.exportSymbol('externalName', internalName)",
        "\n" +
        "var externalName = function(a) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportFunctionWithOptionalArguments3
  public void testExportFunctionWithOptionalArguments3() {
    compileAndCheck("var internalName;\n" +
        "\n" +
        "internalName = function(a) {" +
        "  return a;\n" +
        "};" +
        "goog.exportSymbol('externalName', internalName)",
        "\n" +
        "var externalName = function(a) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportFunctionWithVariableArguments
  public void testExportFunctionWithVariableArguments() {
    compileAndCheck("var internalName;\n" +
        "\n" +
        "internalName = function(a) {" +
        "  return 6;\n" +
        "};" +
        "goog.exportSymbol('externalName', internalName)",
        "\n" +
        "var externalName = function(a) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportEnum
   public void testExportEnum() {
     
     
     
     
     compileAndCheck(
         " var E = {A:8, B:9};" +
         "goog.exportSymbol('E', E);",
         "\n" +
         "var E = {A:1, B:2};\n");
   }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportDontEmitPrototypePathPrefix
  public void testExportDontEmitPrototypePathPrefix() {
    compileAndCheck(
        "\n" +
        "var Foo = function() {};" +
        "\n" +
        "Foo.prototype.m = function() {return 6;};\n" +
        "goog.exportSymbol('Foo', Foo);\n" +
        "goog.exportProperty(Foo.prototype, 'm', Foo.prototype.m);",
        "\n" +
        "var Foo = function() {\n};\n" +
        "\n" +
        "Foo.prototype.m = function() {\n};\n"
    );
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testUseExportsAsExterns
  public void testUseExportsAsExterns() {
    String librarySource =
    "\n" +
    "var InternalName = function(a) {" +
    "};" +
    "goog.exportSymbol('ExternalName', InternalName)";

    String clientSource =
      "var a = new ExternalName(6);\n" +
      "\n" +
      "var b = function(x) {};";

    Result libraryCompileResult = compileAndExportExterns(librarySource);

    assertEquals(0, libraryCompileResult.warnings.length);
    assertEquals(0, libraryCompileResult.errors.length);

    String generatedExterns = libraryCompileResult.externExport;

    Result clientCompileResult = compileAndExportExterns(clientSource,
        generatedExterns);

    assertEquals(0, clientCompileResult.warnings.length);
    assertEquals(0, clientCompileResult.errors.length);
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testWarnOnExportFunctionWithUnknownReturnType
  public void testWarnOnExportFunctionWithUnknownReturnType() {
    String librarySource =
      "var InternalName = function() {" +
      "  return 6;" +
      "};" +
      "goog.exportSymbol('ExternalName', InternalName)";

      Result libraryCompileResult = compileAndExportExterns(librarySource);

      assertEquals(1, libraryCompileResult.warnings.length);
      assertEquals(0, libraryCompileResult.errors.length);
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testDontWarnOnExportConstructorWithUnknownReturnType
  public void testDontWarnOnExportConstructorWithUnknownReturnType() {
    String librarySource =
      "\n " +
      "var InternalName = function() {" +
      "};" +
      "goog.exportSymbol('ExternalName', InternalName)";

      Result libraryCompileResult = compileAndExportExterns(librarySource);

      assertEquals(0, libraryCompileResult.warnings.length);
      assertEquals(0, libraryCompileResult.errors.length);
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testTypedef
  public void testTypedef() {
    compileAndCheck(
        " var Coord;\n" +
        "\n" +
        "var fn = function(a) {};" +
        "goog.exportSymbol('fn', fn);",
        "\n" +
        "var fn = function(a) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportParamWithNull
  public void testExportParamWithNull() throws Exception {
    compileAndCheck(
        "\n" +
        "var f = function(d) {};\n" +
        "goog.exportSymbol('foobar', f)\n",
        "\n" +
        "var foobar = function(d) {\n" +
        "};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportConstructor
  public void testExportConstructor() throws Exception {
    compileAndCheck(" var a = function() {};" +
                    "goog.exportSymbol('foobar', a)",
                    "\n" +
                    "var foobar = function() {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testWarnOnExportFunctionWithUnknownParameterTypes
  public void testWarnOnExportFunctionWithUnknownParameterTypes() {
    
    String librarySource =
      "\n " +
      "var InternalName = function(a,b,c) {" +
      "  return 6;" +
      "};" +
      "goog.exportSymbol('ExternalName', InternalName)";

      Result libraryCompileResult = compileAndExportExterns(librarySource);

      assertEquals(2, libraryCompileResult.warnings.length);
      assertEquals(0, libraryCompileResult.errors.length);
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSimpleAssign
  public void testSimpleAssign() {
    inline("var x; x = 1; print(x)", "var x; print(1)");
    inline("var x; x = 1; x", "var x; 1");
    inline("var x; x = 1; var a = x", "var x; var a = 1");
    inline("var x; x = 1; x = x + 1", "var x; x = 1 + 1");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSimpleVar
  public void testSimpleVar() {
    inline("var x = 1; print(x)", "var x; print(1)");
    inline("var x = 1; x", "var x; 1");
    inline("var x = 1; var a = x", "var x; var a = 1");
    inline("var x = 1; x = x + 1", "var x; x = 1 + 1");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSimpleForIn
  public void testSimpleForIn() {
    inline("var a,b,x = a in b; x",
           "var a,b,x; a in b");
    noInline("var a, b; var x = a in b; print(1); x");
    noInline("var a,b,x = a in b; delete a[b]; x");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testExported
  public void testExported() {
    noInline("var _x = 1; print(_x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineIncrement
  public void testDoNotInlineIncrement() {
    noInline("var x = 1; x++;");
    noInline("var x = 1; x--;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineAssignmentOp
  public void testDoNotInlineAssignmentOp() {
    noInline("var x = 1; x += 1;");
    noInline("var x = 1; x -= 1;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineIntoLhsOfAssign
  public void testDoNotInlineIntoLhsOfAssign() {
    noInline("var x = 1; x += 3;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiUse
  public void testMultiUse() {
    noInline("var x; x = 1; print(x); print (x);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiUseInSameCfgNode
  public void testMultiUseInSameCfgNode() {
    noInline("var x; x = 1; print(x) || print (x);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiUseInTwoDifferentPath
  public void testMultiUseInTwoDifferentPath() {
    noInline("var x = 1; if (print) { print(x) } else { alert(x) }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testAssignmentBeforeDefinition
  public void testAssignmentBeforeDefinition() {
    inline("x = 1; var x = 0; print(x)","x = 1; var x; print(0)" );
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testVarInConditionPath
  public void testVarInConditionPath() {
    noInline("if (foo) { var x = 0 } print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiDefinitionsBeforeUse
  public void testMultiDefinitionsBeforeUse() {
    inline("var x = 0; x = 1; print(x)", "var x = 0; print(1)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiDefinitionsInSameCfgNode
  public void testMultiDefinitionsInSameCfgNode() {
    noInline("var x; (x = 1) || (x = 2); print(x)");
    noInline("var x; x = (1 || (x = 2)); print(x)");
    noInline("var x;(x = 1) && (x = 2); print(x)");
    noInline("var x;x = (1 && (x = 2)); print(x)");
    noInline("var x; x = 1 , x = 2; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNotReachingDefinitions
  public void testNotReachingDefinitions() {
    noInline("var x; if (foo) { x = 0 } print (x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineLoopCarriedDefinition
  public void testNoInlineLoopCarriedDefinition() {
    
    noInline("var x; while(true) { print(x); x = 1; }");

    
    noInline("var x = 0; while(true) { print(x); x = 1; }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotExitLoop
  public void testDoNotExitLoop() {
    noInline("while (z) { var x = 3; } var y = x;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineWithinLoop
  public void testDoNotInlineWithinLoop() {
    noInline("var y = noSFX(); do { var z = y.foo(); } while (true);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineCatchExpression1
  public void testDoNotInlineCatchExpression1() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  throw Error(\"\");\n" +
        "}catch(err) {" +
        "   a = err;\n" +
        "}\n" +
        "return a.stack\n");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineCatchExpression1a
  public void testDoNotInlineCatchExpression1a() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  throw Error(\"\");\n" +
        "}catch(err) {" +
        "   a = err + 1;\n" +
        "}\n" +
        "return a.stack\n");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineCatchExpression2
  public void testDoNotInlineCatchExpression2() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  if (x) {throw Error(\"\");}\n" +
        "}catch(err) {" +
        "   a = err;\n" +
        "}\n" +
        "return a.stack\n");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineCatchExpression3
  public void testDoNotInlineCatchExpression3() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  throw Error(\"\");\n" +
        "} catch(err) {" +
        "  err = x;\n" +
        "  a = err;\n" +
        "}\n" +
        "return a.stack\n");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineCatchExpression4
  public void testDoNotInlineCatchExpression4() {
    
    noInline(
        "try {\n" +
        " stuff();\n" +
        "} catch (e) {\n" +
        " x = e;\n" +
        " print(x);\n" +
        "}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDefinitionAfterUse
  public void testDefinitionAfterUse() {
    inline("var x = 0; print(x); x = 1", "var x; print(0); x = 1");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineSameVariableInStraightLine
  public void testInlineSameVariableInStraightLine() {
    inline("var x; x = 1; print(x); x = 2; print(x)",
        "var x; print(1); print(2)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineInDifferentPaths
  public void testInlineInDifferentPaths() {
    inline("var x; if (print) {x = 1; print(x)} else {x = 2; print(x)}",
        "var x; if (print) {print(1)} else {print(2)}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineInMergedPath
  public void testNoInlineInMergedPath() {
    noInline(
        "var x,y;x = 1;while(y) { if(y){ print(x) } else { x = 1 } } print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineIntoExpressions
  public void testInlineIntoExpressions() {
    inline("var x = 1; print(x + 1);", "var x; print(1 + 1)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions1
  public void testInlineExpressions1() {
    inline("var a, b; var x = a+b; print(x)", "var a, b; var x; print(a+b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions2
  public void testInlineExpressions2() {
    
    noInline("var a, b; var x = a + b; a = 1; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions3
  public void testInlineExpressions3() {
    inline("var a,b,x; x=a+b; x=a-b ; print(x)",
           "var a,b,x; x=a+b; print(a-b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions4
  public void testInlineExpressions4() {
    
    noInline("var a,b,x; x=a+b, x=a-b; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions5
  public void testInlineExpressions5() {
    noInline("var a; var x = a = 1; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions6
  public void testInlineExpressions6() {
    noInline("var a, x; a = 1 + (x = 1); print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression7
  public void testInlineExpression7() {
    
    noInline("var x = foo() + 1; bar(); print(x)");

    
    
    
    noInline("var x = foo() + 1; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression8
  public void testInlineExpression8() {
    
    inline(
        "var a,b;" +
        "var x = a + b; print(x);      x = a - b; print(x)",
        "var a,b;" +
        "var x;         print(a + b);             print(a - b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression9
  public void testInlineExpression9() {
    
    inline(
        "var a,b;" +
        "var x; if (g) { x= a + b; print(x)    }  x = a - b; print(x)",
        "var a,b;" +
        "var x; if (g) {           print(a + b)}             print(a - b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression10
  public void testInlineExpression10() {
    
    noInline("var x, y; x = ((y = 1), print(y))");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions11
  public void testInlineExpressions11() {
    inline("var x; x = x + 1; print(x)", "var x; print(x + 1)");
    noInline("var x; x = x + 1; print(x); print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions12
  public void testInlineExpressions12() {
    
    
    noInline("var x = 10; x = c++; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions13
  public void testInlineExpressions13() {
    inline("var a = 1, b = 2;" +
           "var x = a;" +
           "var y = b;" +
           "var z = x + y;" +
           "var i = z;" +
           "var j = z + y;" +
           "var k = i;",

           "var a, b;" +
           "var x;" +
           "var y = 2;" +
           "var z = 1 + y;" +
           "var i;" +
           "var j = z + y;" +
           "var k = z;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineIfDefinitionMayNotReach
  public void testNoInlineIfDefinitionMayNotReach() {
    noInline("var x; if (x=1) {} x;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineEscapedToInnerFunction
  public void testNoInlineEscapedToInnerFunction() {
    noInline("var x = 1; function foo() { x = 2 }; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineLValue
  public void testNoInlineLValue() {
    noInline("var x; if (x = 1) { print(x) }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSwitchCase
  public void testSwitchCase() {
    inline("var x = 1; switch(x) { }", "var x; switch(1) { }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testShadowedVariableInnerFunction
  public void testShadowedVariableInnerFunction() {
    inline("var x = 1; print(x) || (function() {  var x; x = 1; print(x)})()",
        "var x; print(1) || (function() {  var x; print(1)})()");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testCatch
  public void testCatch() {
    noInline("var x = 0; try { } catch (x) { }");
    noInline("try { } catch (x) { print(x) }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineGetProp
  public void testNoInlineGetProp() {
    
    noInline("var x = a.b.c; j.c = 1; print(x);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineGetProp2
  public void testNoInlineGetProp2() {
    noInline("var x = 1 * a.b.c; j.c = 1; print(x);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineGetProp3
  public void testNoInlineGetProp3() {
    
    inline("var x = function(){1 * a.b.c}; print(x);",
           "var x; print(function(){1 * a.b.c});");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineGetEle
  public void testNoInlineGetEle() {
    
    noInline("var x = a[i]; a[j] = 2; print(x); ");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineConstructors
  public void testNoInlineConstructors() {
    noInline("var x = new Iterator(); x.next();");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineArrayLits
  public void testNoInlineArrayLits() {
    noInline("var x = []; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineObjectLits
  public void testNoInlineObjectLits() {
    noInline("var x = {}; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineRegExpLits
  public void testNoInlineRegExpLits() {
    noInline("var x = /y/; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineConstructorCallsIntoLoop
  public void testInlineConstructorCallsIntoLoop() {
    
    noInline("var x = new Iterator();" +
             "for(i = 0; i < 10; i++) {j = x.next()}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testRemoveWithLabels
  public void testRemoveWithLabels() {
    inline("var x = 1; L: x = 2; print(x)", "var x = 1; L:{} print(2)");
    inline("var x = 1; L: M: x = 2; print(x)", "var x = 1; L:M:{} print(2)");
    inline("var x = 1; L: M: N: x = 2; print(x)",
           "var x = 1; L:M:N:{} print(2)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect1
  public void testInlineAcrossSideEffect1() {
    
    
    
    
    noInline("var y; var x = noSFX(y); print(x)");
    
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect2
  public void testInlineAcrossSideEffect2() {
    
    
    

    
    noInline("var y; var x = noSFX(y), z = hasSFX(y); print(x)");
    noInline("var y; var x = noSFX(y), z = new hasSFX(y); print(x)");
    noInline("var y; var x = new noSFX(y), z = new hasSFX(y); print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect3
  public void testInlineAcrossSideEffect3() {
    
    noInline("var y; var x = noSFX(y); hasSFX(y), print(x)");
    noInline("var y; var x = noSFX(y); new hasSFX(y), print(x)");
    noInline("var y; var x = new noSFX(y); new hasSFX(y), print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect4
  public void testInlineAcrossSideEffect4() {
    
    
    noInline("var y; var x = noSFX(y); hasSFX(y); print(x)");
    noInline("var y; var x = noSFX(y); new hasSFX(y); print(x)");
    noInline("var y; var x = new noSFX(y); new hasSFX(y); print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testCanInlineAcrossNoSideEffect
  public void testCanInlineAcrossNoSideEffect() {
    
    
    
    noInline(
        "var y; var x = noSFX(y), z = noSFX(); noSFX(); noSFX(), print(x)");
    
    
    
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDependOnOuterScopeVariables
  public void testDependOnOuterScopeVariables() {
    noInline("var x; function foo() { var y = x; x = 0; print(y) }");
    noInline("var x; function foo() { var y = x; x++; print(y) }");

    
    
    
    noInline("var x; function foo() { var y = x; print(y) }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineIfNameIsLeftSideOfAssign
  public void testInlineIfNameIsLeftSideOfAssign() {
    inline("var x = 1; x = print(x) + 1", "var x; x = print(1) + 1");
    inline("var x = 1; L: x = x + 2", "var x; L: x = 1 + 2");
    inline("var x = 1; x = (x = x + 1)", "var x; x = (x = 1 + 1)");

    noInline("var x = 1; x = (x = (x = 10) + x)");
    noInline("var x = 1; x = (f(x) + (x = 10) + x);");
    noInline("var x = 1; x=-1,foo(x)");
    noInline("var x = 1; x-=1,foo(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineArguments
  public void testInlineArguments() {
    testSame("function _func(x) { print(x) }");
    testSame("function _func(x,y) { if(y) { x = 1 }; print(x) }");

    test("function f(x, y) { x = 1; print(x) }",
         "function f(x, y) { print(1) }");

    test("function f(x, y) { if (y) { x = 1; print(x) }}",
         "function f(x, y) { if (y) { print(1) }}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInvalidInlineArguments1
  public void testInvalidInlineArguments1() {
    testSame("function f(x, y) { x = 1; arguments[0] = 2; print(x) }");
    testSame("function f(x, y) { x = 1; var z = arguments;" +
        "z[0] = 2; z[1] = 3; print(x)}");
    testSame("function g(a){a[0]=2} function f(x){x=1;g(arguments);print(x)}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInvalidInlineArguments2
  public void testInvalidInlineArguments2() {
    testSame("function f(c) {var f = c; arguments[0] = this;" +
             "f.apply(this, arguments); return this;}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testForIn
  public void testForIn() {
    noInline("var x; var y = {}; for(x in y){}");
    noInline("var x; var y = {}; var z; for(x in z = y){print(z)}");
    noInline("var x; var y = {}; var z; for(x in y){print(z)}");

  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNotOkToSkipCheckPathBetweenNodes
  public void testNotOkToSkipCheckPathBetweenNodes() {
    noInline("var x; for(x = 1; foo(x);) {}");
    noInline("var x; for(; x = 1;foo(x)) {}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testIssue698
  public void testIssue698() {
    
    
    
    inline(
        "var x = ''; "
        + "unknown.length < 2 && (unknown='0' + unknown);"
        + "x = x + unknown; "
        + "unknown.length < 3 && (unknown='0' + unknown);"
        + "x = x + unknown; "
        + "return x;",
        "var x; "
        + "unknown.length < 2 && (unknown='0' + unknown);"
        + "x = '' + unknown; "
        + "unknown.length < 3 && (unknown='0' + unknown);"
        + "x = x + unknown; "
        + "return x;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testIssue777
  public void testIssue777() {
    test(
        "function f(cmd, ta) {" +
        "  var temp = cmd;" +
        "  var temp2 = temp >> 2;" +
        "  cmd = STACKTOP;" +
        "  for (var src = temp2, dest = cmd >> 2, stop = src + 37;" +
        "       src < stop;" +
        "       src++, dest++) {" +
        "    HEAP32[dest] = HEAP32[src];" +
        "  }" +
        "  temp = ta;" +
        "  temp2 = temp >> 2;" +
        "  ta = STACKTOP;" +
        "  STACKTOP += 8;" +
        "  HEAP32[ta >> 2] = HEAP32[temp2];" +
        "  HEAP32[ta + 4 >> 2] = HEAP32[temp2 + 1];" +
        "}",
        "function f(cmd, ta){" +
        "  var temp;" +
        "  var temp2 = cmd >> 2;" +
        "  cmd = STACKTOP;" +
        "  var src = temp2;" +
        "  var dest = cmd >> 2;" +
        "  var stop = src + 37;" +
        "  for(;src<stop;src++,dest++)HEAP32[dest]=HEAP32[src];" +
        "  temp2 = ta >> 2;" +
        "  ta = STACKTOP;" +
        "  STACKTOP += 8;" +
        "  HEAP32[ta>>2] = HEAP32[temp2];" +
        "  HEAP32[ta+4>>2] = HEAP32[temp2+1];" +
        "}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testTransitiveDependencies1
  public void testTransitiveDependencies1() {
    test(
        "function f(x) { var a = x; var b = a; x = 3; return b; }",
        "function f(x) { var a;     var b = x; x = 3; return b; }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testTransitiveDependencies2
  public void testTransitiveDependencies2() {
    test(
        "function f(x) { var a = x; var b = a; var c = b; x = 3; return c; }",
        "function f(x) { var a    ; var b = x; var c    ; x = 3; return b; }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testIssue794a
  public void testIssue794a() {
    noInline(
        "var x = 1; " +
        "try { x += someFunction(); } catch (e) {}" +
        "x += 1;" +
        "try { x += someFunction(); } catch (e) {}" +
        "return x;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testIssue794b
  public void testIssue794b() {
    noInline(
        "var x = 1; " +
        "try { x = x + someFunction(); } catch (e) {}" +
        "x = x + 1;" +
        "try { x = x + someFunction(); } catch (e) {}" +
        "return x;");
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst1
  public void testReplaceReturnConst1() {
    String source = "a.prototype.foo = function() {return \"foobar\"}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    RETURNARG_HELPER,
                    "a.prototype.foo = JSCompiler_returnArg(\"foobar\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst2
  public void testReplaceReturnConst2() {
    checkCompilesToSame("a.prototype.foo = function() {return foobar}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst3
  public void testReplaceReturnConst3() {
    String source = "a.prototype.foo = function() {return void 0;}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    RETURNARG_HELPER,
                    "a.prototype.foo = JSCompiler_returnArg(void 0)",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceGetter1
  public void testReplaceGetter1() {
    String source = "a.prototype.foo = function() {return this.foo_}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    GET_HELPER,
                    "a.prototype.foo = JSCompiler_get(\"foo_\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceGetter2
  public void testReplaceGetter2() {
    checkCompilesToSame("a.prototype.foo = function() {return}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter1
  public void testReplaceSetter1() {
    String source = "a.prototype.foo = function(v) {this.foo_ = v}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    SET_HELPER,
                    "a.prototype.foo = JSCompiler_set(\"foo_\")",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter2
  public void testReplaceSetter2() {
    String source = "a.prototype.foo = function(v, v2) {this.foo_ = v}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    SET_HELPER,
                    "a.prototype.foo = JSCompiler_set(\"foo_\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter3
  public void testReplaceSetter3() {
    checkCompilesToSame("a.prototype.foo = function() {this.foo_ = v}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter4
  public void testReplaceSetter4() {
    checkCompilesToSame(
        "a.prototype.foo = function(v, v2) {this.foo_ = v2}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction1
  public void testReplaceEmptyFunction1() {
    String source = "a.prototype.foo = function() {}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    EMPTY_HELPER,
                    "a.prototype.foo = JSCompiler_emptyFn()",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction2
  public void testReplaceEmptyFunction2() {
    checkCompilesToSame("function foo() {}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction3
  public void testReplaceEmptyFunction3() {
    String source = "var foo = function() {}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    EMPTY_HELPER,
                    "var foo = JSCompiler_emptyFn()",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceIdentityFunction1
  public void testReplaceIdentityFunction1() {
    String source = "a.prototype.foo = function(a) {return a}";
    checkCompilesToSame(source, 2);
    checkCompilesTo(source,
                    IDENTITY_HELPER,
                    "a.prototype.foo = JSCompiler_identityFn()",
                    3);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceIdentityFunction2
  public void testReplaceIdentityFunction2() {
    checkCompilesToSame("a.prototype.foo = function(a) {return a + 1}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testIssue538
  public void testIssue538() {
    checkCompilesToSame(      "\n" +
        "WebInspector.Setting = function() {}\n" +
        "WebInspector.Setting.prototype = {\n" +
        "    get name0(){return this._name;},\n" +
        "    get name1(){return this._name;},\n" +
        "    get name2(){return this._name;},\n" +
        "    get name3(){return this._name;},\n" +
        "    get name4(){return this._name;},\n" +
        "    get name5(){return this._name;},\n" +
        "    get name6(){return this._name;},\n" +
        "    get name7(){return this._name;},\n" +
        "    get name8(){return this._name;},\n" +
        "    get name9(){return this._name;},\n" +
        "}", 1);
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testValidBuiltInTypeRedefinition
  public void testValidBuiltInTypeRedefinition() throws Exception {
    testSame(ALL_NATIVE_EXTERN_TYPES, "", null);
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentReturnType
  public void testBuiltInTypeDifferentReturnType() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String, *=): number\n"
        + "expected: function (new:String, *=): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentNumParams
  public void testBuiltInTypeDifferentNumParams() throws Exception {
    testSame(
        "\n"
        + "function String() {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String): string\n"
        + "expected: function (new:String, *=): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentNumParams2
  public void testBuiltInTypeDifferentNumParams2() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str, opt_nothing) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String, ?=, ?=): string\n"
        + "expected: function (new:String, *=): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentParamType
  public void testBuiltInTypeDifferentParamType() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String, ?=): string\n"
        + "expected: function (new:String, *=): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBadFunctionTypeDefinition
  public void testBadFunctionTypeDefinition() throws Exception {
    testSame(
        "function Function(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type Function\n"
        + "found   : function (new:Function, ?=): ?\n"
        + "expected: function (new:Function, ...[*]): ?");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testInlineJsDoc
  public void testInlineJsDoc() throws Exception {
    testSame(
        " function f( x) { return x; }", "",
        TypeValidator.TYPE_MISMATCH_WARNING,
        "inconsistent return type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testInlineJsDoc2
  public void testInlineJsDoc2() throws Exception {
    testSame(
        " " +
        "function f( x) { return x; }" +
        " var x = f(1);", "",
        TypeValidator.TYPE_MISMATCH_WARNING,
        "initializing variable\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testExternSubTypes
  public void testExternSubTypes() throws Exception {
    testSame(ALL_NATIVE_EXTERN_TYPES, "", null);

    List<FunctionType> subtypes = ((ObjectType) getLastCompiler()
        .getTypeRegistry().getType("Error")).getConstructor().getSubTypes();
    for (FunctionType type : subtypes) {
      String typeName = type.getInstanceType().toString();
      FunctionType typeInRegistry = ((ObjectType) getLastCompiler()
          .getTypeRegistry().getType(typeName)).getConstructor();
      assertTrue(typeInRegistry == type);
    }
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportSymbol
  public void testExportSymbol() {
    test("function foo() {}",
        "function foo(){}google_exportSymbol(\"foo\",foo)");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportSymbolAndProperties
  public void testExportSymbolAndProperties() {
    test("function foo() {}" +
         "foo.prototype.bar = function() {}",
         "function foo(){}" +
         "google_exportSymbol(\"foo\",foo);" +
         "foo.prototype.bar=function(){};" +
         "goog.exportProperty(foo.prototype,\"bar\",foo.prototype.bar)");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportSymbolAndConstantProperties
  public void testExportSymbolAndConstantProperties() {
    test("function foo() {}" +
         "foo.BAR = 5;",
         "function foo(){}" +
         "google_exportSymbol(\"foo\",foo);" +
         "foo.BAR=5;" +
         "goog.exportProperty(foo,\"BAR\",foo.BAR)");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportVars
  public void testExportVars() {
    test("var FOO = 5",
         "var FOO=5;" +
         "google_exportSymbol(\"FOO\",FOO)");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testNoExport
  public void testNoExport() {
    test("var FOO = 5", "var FOO=5");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testNestedVarAssign
  public void testNestedVarAssign() {
    test("var BAR;\nvar FOO = BAR = 5",
         null, FindExportableNodes.NON_GLOBAL_ERROR);
  }

// com.google.javascript.jscomp.GenerateExportsTest::testNestedAssign
  public void testNestedAssign() {
    test("var BAR;var FOO = {};\nFOO.test = BAR = 5",
         null, FindExportableNodes.NON_GLOBAL_ERROR);
  }

// com.google.javascript.jscomp.GenerateExportsTest::testNonGlobalScopeExport
  public void testNonGlobalScopeExport() {
    test("(function() { var FOO = 5 })()",
         null, FindExportableNodes.NON_GLOBAL_ERROR);
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportClass
  public void testExportClass() {
    test(" function G() {} foo();",
         "function G() {} google_exportSymbol('G', G); foo();");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportSubclass
  public void testExportSubclass() {
    test("var goog = {}; function F() {}" +
         " function G() {} goog.inherits(G, F);",
         "var goog = {}; function F() {}" +
         "function G() {} goog.inherits(G, F); google_exportSymbol('G', G);");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportEnum
  public void testExportEnum() {
    
    test(" var E = {A:1, B:2};",
         " var E = {A:1, B:2};" +
         "google_exportSymbol('E', E);");
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testNativeCtor
  public void testNativeCtor() {
    testSame(
        " " +
        "function Object(x) {};",
        "var x = new Object();" +
        " var y = new Object();", null);
    assertEquals(
        "Object.",
        findGlobalNameType("x").getJSDocInfo().getBlockDescription());
    assertEquals(
        "Object.",
        findGlobalNameType("y").getJSDocInfo().getBlockDescription());
    assertEquals(
        "Object.",
        globalScope.getVar("y").getType().getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testStructuralFunctions
  public void testStructuralFunctions() {
    testSame(
        " " +
        "function Object(x) {};",
        " " +
        "function fn(x) {};" +
        "var goog = {};" +
        " goog.x = new Object();" +
        " goog.y = fn;", null);
    assertEquals(
        "(Object|null)",
        globalScope.getVar("goog.x").getType().toString());
    assertEquals(
        "Object.",
        globalScope.getVar("goog.x").getType().restrictByNotNullOrUndefined()
        .getJSDocInfo().getBlockDescription());
    assertEquals(
        "Another function.",
        globalScope.getVar("goog.y").getType()
        .getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testInstanceObject
  public void testInstanceObject() {
    
    testSame(
        " function Foo() {}" +
        "var f = new Foo();" +
        " f.bar = 4;");
    ObjectType type = (ObjectType) globalScope.getVar("f").getType();
    assertEquals("Foo", type.toString());
    assertFalse(type.hasProperty("bar"));
    assertNull(type.getOwnPropertyJSDocInfo("bar"));
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testInterface
  public void testInterface() {
    testSame(
        " function Foo() {}" +
        "var f = new Foo();" +
        " f.bar = 4;");
    ObjectType type = (ObjectType) globalScope.getVar("Foo").getType();
    assertEquals(
        "An interface.",
        type.getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testNamespacedCtor
  public void testNamespacedCtor() {
    testSame(
        "var goog = {};" +
        " goog.Foo = function() {};" +
        "goog.Foo.bar = goog.Foo;" +
        "" +
        "goog.Foo.prototype.baz = goog.Foo;" +
        " var x = new goog.Foo();");
    assertEquals(
        "Hello!",
        findGlobalNameType("x").getJSDocInfo().getBlockDescription());
    assertEquals(
        "Hello!",
        findGlobalNameType("goog.Foo").getJSDocInfo().getBlockDescription());
    assertEquals(
        "Hello!",
        findGlobalNameType(
            "goog.Foo.bar").getJSDocInfo().getBlockDescription());

    assertEquals(
        "Hello!",
        findGlobalNameType(
            "goog.Foo.prototype.baz").getJSDocInfo().getBlockDescription());

    ObjectType proto = (ObjectType) findGlobalNameType("goog.Foo.prototype");
    assertEquals(
        "Bye!",
        proto.getPropertyType("baz").getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testAbstractMethod
  public void testAbstractMethod() {
    testSame(
        " var abstractMethod;" +
        " function Foo() {}" +
        "" +
        "Foo.prototype.bar = abstractMethod;");
    FunctionType abstractMethod =
        (FunctionType) findGlobalNameType("abstractMethod");
    assertNull(abstractMethod.getJSDocInfo());

    FunctionType ctor = (FunctionType) findGlobalNameType("Foo");
    ObjectType proto = ctor.getInstanceType().getImplicitPrototype();
    FunctionType method = (FunctionType) proto.getPropertyType("bar");
    assertEquals(
        "Block description.",
        method.getJSDocInfo().getBlockDescription());
    assertEquals(
        "Block description.",
        proto.getOwnPropertyJSDocInfo("bar").getBlockDescription());
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

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineVariablesConstants
  public void testInlineVariablesConstants() {
    test("var ABC=2; var x = ABC;", "var x=2");
    test("var AA = 'aa'; AA;", "'aa'");
    test("var A_A=10; A_A + A_A;", "10+10");
    test("var AA=1", "");
    test("var AA; AA=1", "1");
    test("var AA; if (false) AA=1; AA;", "if (false) 1; 1;");
    testSame("var AA; if (false) AA=1; else AA=2; AA;");

    test("var AA;(function () {AA=1})()",
         "(function () {1})()");

    
    testSame("var x = AA;");

    
    testSame("var AA = '1234567890'; foo(AA); foo(AA); foo(AA);");

    test("var AA = '123456789012345';AA;",
         "'123456789012345'");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testNoInlineArraysOrRegexps
  public void testNoInlineArraysOrRegexps() {
    testSame("var AA = [10,20]; AA[0]");
    testSame("var AA = [10,20]; AA.push(1); AA[0]");
    testSame("var AA = /x/; AA.test('1')");
    testSame(" var aa = /x/; aa.test('1')");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineVariablesConstantsJsDocStyle
  public void testInlineVariablesConstantsJsDocStyle() {
    test("var abc=2; var x = abc;", "var x=2");
    test("var aa = 'aa'; aa;", "'aa'");
    test("var a_a=10; a_a + a_a;", "10+10");
    test("var aa=1;", "");
    test("var aa; aa=1;", "1");
    test("var aa;(function () {aa=1})()", "(function () {1})()");
    test("var aa;(function () {aa=1})(); var z=aa",
         "(function () {1})(); var z=1");
    testSame("var aa;(function () {var y; aa=y})(); var z=aa");

    
    testSame("var aa = '1234567890'; foo(aa); foo(aa); foo(aa);");

    test("var aa = '123456789012345';aa;",
         "'123456789012345'");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineConditionallyDefinedConstant1
  public void testInlineConditionallyDefinedConstant1() {
    
    
    
    
    test("if (x) var ABC = 2; if (y) f(ABC);",
         "if (x); if (y) f(2);");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineConditionallyDefinedConstant2
  public void testInlineConditionallyDefinedConstant2() {
    test("if (x); else var ABC = 2; if (y) f(ABC);",
         "if (x); else; if (y) f(2);");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineConditionallyDefinedConstant3
  public void testInlineConditionallyDefinedConstant3() {
    test("if (x) { var ABC = 2; } if (y) { f(ABC); }",
         "if (x) {} if (y) { f(2); }");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineDefinedConstant
  public void testInlineDefinedConstant() {
    test(
        "\n" +
        "var aa = '1234567890';\n" +
        "foo(aa); foo(aa); foo(aa);",
        "foo('1234567890');foo('1234567890');foo('1234567890')");

    test(
        "\n" +
        "var ABC = '1234567890';\n" +
        "foo(ABC); foo(ABC); foo(ABC);",
        "foo('1234567890');foo('1234567890');foo('1234567890')");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineVariablesConstantsWithInlineAllStringsOn
  public void testInlineVariablesConstantsWithInlineAllStringsOn() {
    inlineAllStrings = true;
    test("var AA = '1234567890'; foo(AA); foo(AA); foo(AA);",
         "foo('1234567890'); foo('1234567890'); foo('1234567890')");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testNoInlineWithoutConstDeclaration
  public void testNoInlineWithoutConstDeclaration() {
    testSame("var abc = 2; var x = abc;");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testNoInlineAliases
  public void testNoInlineAliases() {
    testSame("var XXX = new Foo(); var yyy = XXX; bar(yyy)");
    testSame("var xxx = new Foo(); var YYY = xxx; bar(YYY)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineGlobal
  public void testInlineGlobal() {
    test("var x = 1; var z = x;", "var z = 1;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineExportedName
  public void testNoInlineExportedName() {
    testSame("var _x = 1; var z = _x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineExportedName2
  public void testNoInlineExportedName2() {
    testSame("var f = function() {}; var _x = f;" +
             "var y = function() { _x(); }; var _y = f;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotInlineIncrement
  public void testDoNotInlineIncrement() {
    testSame("var x = 1; x++;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotInlineDecrement
  public void testDoNotInlineDecrement() {
    testSame("var x = 1; x--;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotInlineIntoLhsOfAssign
  public void testDoNotInlineIntoLhsOfAssign() {
    testSame("var x = 1; x += 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineIntoRhsOfAssign
  public void testInlineIntoRhsOfAssign() {
    test("var x = 1; var y = x;", "var y = 1;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction
  public void testInlineInFunction() {
    test("function baz() { var x = 1; var z = x; }",
        "function baz() { var z = 1; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction2
  public void testInlineInFunction2() {
    test("function baz() { " +
            "var a = new obj();"+
            "result = a;" +
         "}",
         "function baz() { " +
            "result = new obj()" +
         "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction3
  public void testInlineInFunction3() {
    testSame(
        "function baz() { " +
           "var a = new obj();" +
           "(function(){a;})();" +
           "result = a;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction4
  public void testInlineInFunction4() {
    testSame(
        "function baz() { " +
           "var a = new obj();" +
           "foo.result = a;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction5
  public void testInlineInFunction5() {
    testSame(
        "function baz() { " +
           "var a = (foo = new obj());" +
           "foo.x();" +
           "result = a;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAcrossModules
  public void testInlineAcrossModules() {
    
    test(createModules("var a = 2;", "var b = a;"),
        new String[] { "", "var b = 2;" });
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitConditional1
  public void testDoNotExitConditional1() {
    testSame("if (true) { var x = 1; } var z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitConditional2
  public void testDoNotExitConditional2() {
    testSame("if (true) var x = 1; var z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitConditional3
  public void testDoNotExitConditional3() {
    testSame("var x; if (true) x=1; var z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitLoop
  public void testDoNotExitLoop() {
    testSame("while (z) { var x = 3; } var y = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitForLoop
  public void testDoNotExitForLoop() {
    test("for (var i = 1; false; false) var z = i;",
         "for (;false;false) var z = 1;");
    testSame("for (; false; false) var i = 1; var z = i;");
    testSame("for (var i in {}); var z = i;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotEnterSubscope
  public void testDoNotEnterSubscope() {
    testSame(
        "var x = function() {" +
        "  var self = this; " +
        "  return function() { var y = self; };" +
        "}");
    testSame(
        "var x = function() {" +
        "  var y = [1]; " +
        "  return function() { var z = y; };" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitTry
  public void testDoNotExitTry() {
    testSame("try { var x = y; } catch (e) {} var z = y; ");
    testSame("try { throw e; var x = 1; } catch (e) {} var z = x; ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotEnterCatch
  public void testDoNotEnterCatch() {
    testSame("try { } catch (e) { var z = e; } ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotEnterFinally
  public void testDoNotEnterFinally() {
    testSame("try { throw e; var x = 1; } catch (e) {} " +
             "finally  { var z = x; } ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideIfConditional
  public void testInsideIfConditional() {
    test("var a = foo(); if (a) { alert(3); }", "if (foo()) { alert(3); }");
    test("var a; a = foo(); if (a) { alert(3); }", "if (foo()) { alert(3); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testOnlyReadAtInitialization
  public void testOnlyReadAtInitialization() {
    test("var a; a = foo();", "foo();");
    test("var a; if (a = foo()) { alert(3); }", "if (foo()) { alert(3); }");
    test("var a; switch (a = foo()) {}", "switch(foo()) {}");
    test("var a; function f(){ return a = foo(); }",
         "function f(){ return foo(); }");
    test("function f(){ var a; return a = foo(); }",
         "function f(){ return foo(); }");
    test("var a; with (a = foo()) { alert(3); }", "with (foo()) { alert(3); }");

    test("var a; b = (a = foo());", "b = foo();");
    test("var a; while(a = foo()) { alert(3); }",
         "while(foo()) { alert(3); }");
    test("var a; for(;a = foo();) { alert(3); }",
         "for(;foo();) { alert(3); }");
    test("var a; do {} while(a = foo()) { alert(3); }",
         "do {} while(foo()) { alert(3); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testImmutableWithSingleReferenceAfterInitialzation
  public void testImmutableWithSingleReferenceAfterInitialzation() {
    test("var a; a = 1;", "1;");
    test("var a; if (a = 1) { alert(3); }", "if (1) { alert(3); }");
    test("var a; switch (a = 1) {}", "switch(1) {}");
    test("var a; function f(){ return a = 1; }",
         "function f(){ return 1; }");
    test("function f(){ var a; return a = 1; }",
         "function f(){ return 1; }");
    test("var a; with (a = 1) { alert(3); }", "with (1) { alert(3); }");

    test("var a; b = (a = 1);", "b = 1;");
    test("var a; while(a = 1) { alert(3); }",
         "while(1) { alert(3); }");
    test("var a; for(;a = 1;) { alert(3); }",
         "for(;1;) { alert(3); }");
    test("var a; do {} while(a = 1) { alert(3); }",
         "do {} while(1) { alert(3); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testSingleReferenceAfterInitialzation
  public void testSingleReferenceAfterInitialzation() {
    test("var a; a = foo();a;", "foo();");
    testSame("var a; if (a = foo()) { alert(3); } a;");
    testSame("var a; switch (a = foo()) {} a;");
    testSame("var a; function f(){ return a = foo(); } a;");
    testSame("function f(){ var a; return a = foo(); a;}");
    testSame("var a; with (a = foo()) { alert(3); } a;");
    testSame("var a; b = (a = foo()); a;");
    testSame("var a; while(a = foo()) { alert(3); } a;");
    testSame("var a; for(;a = foo();) { alert(3); } a;");
    testSame("var a; do {} while(a = foo()) { alert(3); } a;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideIfBranch
  public void testInsideIfBranch() {
    testSame("var a = foo(); if (1) { alert(a); }");
  }
