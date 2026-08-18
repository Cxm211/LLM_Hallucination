// buggy code
    public static boolean isJSIdentifier(String s) {
      int length = s.length();

      if (length == 0 ||
          !Character.isJavaIdentifierStart(s.charAt(0))) {
        return false;
      }

      for (int i = 1; i < length; i++) {
        if (
            !Character.isJavaIdentifierPart(s.charAt(i))) {
          return false;
        }
      }

      return true;
    }

// relevant test
// com.google.javascript.jscomp.AliasStringsTest::testAssignment
  public void testAssignment() {
    strings = ImmutableSet.of("none", "width", "overimaginative");

    
    testSame("var foo='foo'");
    testSame("a='titanium',b='titanium',c='titanium',d='titanium'");

    
    testSame("myStr='width'");
    testSame("Bar.prototype.start='none'");
    
    test("a='overimaginative';b='overimaginative'",
         "var $$S_overimaginative='overimaginative';" +
         "a=$$S_overimaginative;b=$$S_overimaginative");

    testSame("var width=1234");
    testSame("width=1234;width=10000;width=9900;width=17;");
  }

// com.google.javascript.jscomp.AliasStringsTest::testSeveral
  public void testSeveral() {
    strings = ImmutableSet.of("", "px", "none", "width");

    test("function f() {" +
         "var styles=['width',100,'px','display','none'].join('')}",
         "var $$S_='';" +
         "var $$S_none='none';" +
         "var $$S_px='px';" +
         "var $$S_width='width';" +
         "function f() {var styles=[$$S_width,100,$$S_px,'display'," +
         "$$S_none].join($$S_)}");
  }

// com.google.javascript.jscomp.AliasStringsTest::testSortedOutput
  public void testSortedOutput() {
    strings = ImmutableSet.of("aba", "aaa", "aca", "bca", "bba");
    test("function f() {return ['aba', 'aaa', 'aca', 'bca', 'bba']}",
         "var $$S_aaa='aaa';" +
         "var $$S_aba='aba';" +
         "var $$S_aca='aca';" +
         "var $$S_bba='bba';" +
         "var $$S_bca='bca';" +
         "function f() {" +
         "  return [$$S_aba, $$S_aaa, $$S_aca, $$S_bca, $$S_bba]}");
  }

// com.google.javascript.jscomp.AliasStringsTest::testObjectLiterals
  public void testObjectLiterals() {
    strings = ImmutableSet.of("px", "!@#$%^&*()");

    test("var foo={px:435}", "var foo={px:435}");

    
    test("var foo={'px':435}", "var foo={'px':435}");
    test("bar=function f(){return {'px':435}}",
         "bar=function f(){return {'px':435}}");

    test("function f() {var foo={bar:'!@#$%^&*()'}}",
         "var $$S_$21$40$23$24$25$5e$26$2a$28$29='!@#$%^&*()';" +
         "function f() {var foo={bar:$$S_$21$40$23$24$25$5e$26$2a$28$29}}");

    test("function f() {var foo={px:435,foo:'px',bar:'baz'}}",
         "var $$S_px='px';" +
         "function f() {var foo={px:435,foo:$$S_px,bar:'baz'}}");
  }

// com.google.javascript.jscomp.AliasStringsTest::testGetProp
  public void testGetProp() {
    strings = ImmutableSet.of("px", "width");

    testSame("function f(){element.style.px=1234}");

    test("function f(){shape.width.units='px'}",
        "var $$S_px='px';function f(){shape.width.units=$$S_px}");

    test("function f(){shape['width'].units='pt'}",
         "var $$S_width='width';" +
         "function f(){shape[$$S_width].units='pt'}");
  }

// com.google.javascript.jscomp.AliasStringsTest::testFunctionCalls
  public void testFunctionCalls() {
    strings = ImmutableSet.of("", ",", "overimaginative");

    
    testSame("alert('')");
    testSame("var a=[1,2,3];a.join(',')");
    
    test("f('overimaginative', 'overimaginative')",
         "var $$S_overimaginative='overimaginative';" +
         "f($$S_overimaginative,$$S_overimaginative)");
 }

// com.google.javascript.jscomp.AliasStringsTest::testRegularExpressions
  public void testRegularExpressions() {
    strings = ImmutableSet.of("px");

    testSame("/px/.match('10px')");
  }

// com.google.javascript.jscomp.AliasStringsTest::testBlackList
  public void testBlackList() {
    test("(function (){var f=\'sec ret\';g=\"TOPseCreT\"})",
         "var $$S_sec$20ret='sec ret';" +
         "(function (){var f=$$S_sec$20ret;g=\"TOPseCreT\"})");
  }

// com.google.javascript.jscomp.AliasStringsTest::testLongStableAlias
  public void testLongStableAlias() {
    strings = ALL_STRINGS;

    

    test("a='Antidisestablishmentarianism';" +
         "b='Antidisestablishmentarianism';",
         "var $$S_Antidisestablishment_506eaf9c=" +
         "  'Antidisestablishmentarianism';"      +
         "a=$$S_Antidisestablishment_506eaf9c;"   +
         "b=$$S_Antidisestablishment_506eaf9c");

    

    test("a='AntidisestablishmentarianIsm';" +
         "b='AntidisestablishmentarianIsm';",
         "var $$S_Antidisestablishment_6823e97c=" +
         "  'AntidisestablishmentarianIsm';"      +
         "a=$$S_Antidisestablishment_6823e97c;"   +
         "b=$$S_Antidisestablishment_6823e97c");

    
  }

// com.google.javascript.jscomp.AliasStringsTest::testLongStableAliasHashCollision
  public void testLongStableAliasHashCollision() {
    strings = ALL_STRINGS;
    hashReduction = true;

    
    

    test("f('Antidisestablishmentarianism');"  +
         "f('Antidisestablishmentarianism');"  +
         "f('Antidisestablishmentarianismo');" +
         "f('Antidisestablishmentarianismo');",

         "var $$S_Antidisestablishment_0="     +
         "  'Antidisestablishmentarianism';"   +
         "var $$S_Antidisestablishment_0_1="   +
         "  'Antidisestablishmentarianismo';"  +

         "f($$S_Antidisestablishment_0);"      +
         "f($$S_Antidisestablishment_0);"      +
         "f($$S_Antidisestablishment_0_1);"    +
         "f($$S_Antidisestablishment_0_1);");
  }

// com.google.javascript.jscomp.AliasStringsTest::testStringsThatAreGlobalVarValues
  public void testStringsThatAreGlobalVarValues() {
    strings = ALL_STRINGS;

    testSame("var foo='foo'; var bar='';");

    
    testSame("var foo=['foo','bar'];");

    
    testSame("var foo=['foo',['bar']];");

    
    test("var foo=['foo', 'bar'];function bar() {return 'foo';}",
        "var $$S_foo='foo';" +
        "var foo=[$$S_foo, 'bar'];function bar() {return $$S_foo;}");

    
    testSame("var foo={'foo': 'bar'};");

    
    testSame("var foo={'foo': {'bar': 'baz'}};");

    
    
    test("var foo={'foo': 'bar'};function bar() {return 'foo';}",
        "var $$S_foo='foo';var foo={'foo': 'bar'};" +
        "function bar() {return $$S_foo;}");

    
    
    test("var foo={'foo': 'foo'};function bar() {return 'foo';}",
         "var $$S_foo='foo';" +
         "var foo={'foo': $$S_foo};function bar() {return $$S_foo;}");

  }

// com.google.javascript.jscomp.AliasStringsTest::testStringsInModules
  public void testStringsInModules() {
    strings = ALL_STRINGS;

    
    
    

    JSModule[] modules =
        createModuleBush(
            
            "function f(a) { alert('f:' + a); }" +
            "function g() { alert('ciao'); }",
            
            "f('-------hi-------');" +
            "f('bye');" +
            "function h(a) { alert('h:' + a); }",
            
            "f('-------hi-------');" +
            "h('ciao' + '------adios------');" +
            "(function() { alert('zzz'); })();",
            
            "f('-------hi-------'); alert('------adios------');" +
            "h('-----peaches-----'); h('-----peaches-----');");

    moduleGraph = new JSModuleGraph(modules);

    test(modules,
         new String[] {
             
             "var $$S_ciao = 'ciao';" +
             "var $$S_f$3a = 'f:';" +
             "function f(a) { alert($$S_f$3a + a); }" +
             "function g() { alert($$S_ciao); }",
             
             "var $$S_$2d$2d$2d$2d$2d$2d$2dhi$2d$2d$2d$2d$2d$2d$2d" +
             " = '-------hi-------';" +
             "var $$S_$2d$2d$2d$2d$2d$2d_adios$2d$2d$2d$2d$2d$2d" +
             " = '------adios------'; " +
             "var $$S_h$3a = 'h:';" +
             "f($$S_$2d$2d$2d$2d$2d$2d$2dhi$2d$2d$2d$2d$2d$2d$2d);" +
             "f('bye');" +
             "function h(a) { alert($$S_h$3a + a); }",
             
             "var $$S_zzz = 'zzz';" +
             "f($$S_$2d$2d$2d$2d$2d$2d$2dhi$2d$2d$2d$2d$2d$2d$2d);" +
             "h($$S_ciao + $$S_$2d$2d$2d$2d$2d$2d_adios$2d$2d$2d$2d$2d$2d);" +
             "(function() { alert($$S_zzz) })();",
             
             "var $$S_$2d$2d$2d$2d$2dpeaches$2d$2d$2d$2d$2d" +
             " = '-----peaches-----';" +
             "f($$S_$2d$2d$2d$2d$2d$2d$2dhi$2d$2d$2d$2d$2d$2d$2d);" +
             "alert($$S_$2d$2d$2d$2d$2d$2d_adios$2d$2d$2d$2d$2d$2d);" +
             "h($$S_$2d$2d$2d$2d$2dpeaches$2d$2d$2d$2d$2d);" +
             "h($$S_$2d$2d$2d$2d$2dpeaches$2d$2d$2d$2d$2d);",
         });
    moduleGraph = null;
  }

// com.google.javascript.jscomp.AliasStringsTest::testStringsInModules2
  public void testStringsInModules2() {
    strings = ALL_STRINGS;

    
    
    

    JSModule[] modules =
        createModuleBush(
            
            "function g() { alert('ciao'); }",
            
            "function h(a) { alert('h:' + a); }",
            
            "h('ciao' + 'adios');",
            
            "g();");

    moduleGraph = new JSModuleGraph(modules);

    test(modules,
         new String[] {
             
             "var $$S_ciao = 'ciao';" +
             "function g() { alert($$S_ciao); }",
             
             "var $$S_h$3a = 'h:';" +
             "function h(a) { alert($$S_h$3a + a); }",
             
             "h($$S_ciao + 'adios');",
             
             "g();",
         });
    moduleGraph = null;
  }

// com.google.javascript.jscomp.AliasStringsTest::testEmptyModules
  public void testEmptyModules() {
    JSModule[] modules =
      createModuleStar(
          
          "",
          
          "function foo() { f('good') }",
          
          "function foo() { f('good') }");

    moduleGraph = new JSModuleGraph(modules);
    test(modules,
        new String[] {
        
        "var $$S_good='good'",
        
        "function foo() {f($$S_good)}",
        
        "function foo() {f($$S_good)}",});

    moduleGraph = null;
  }

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

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToDefinedProperties1
  public void testRefToDefinedProperties1() {
    testSame(NAMES + "alert(a.b); alert(a.c.e);");
    testSame(GET_NAMES + "alert(a.b); alert(a.c.e);");
    testSame(SET_NAMES + "alert(a.b); alert(a.c.e);");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToDefinedProperties2
  public void testRefToDefinedProperties2() {
    testSame(NAMES + "a.x={}; alert(a.c);");
    testSame(GET_NAMES + "a.x={}; alert(a.c);");
    testSame(SET_NAMES + "a.x={}; alert(a.c);");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToDefinedProperties3
  public void testRefToDefinedProperties3() {
    testSame(NAMES + "alert(a.d);");
    testSame(GET_NAMES + "alert(a.d);");
    testSame(SET_NAMES + "alert(a.d);");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToMethod1
  public void testRefToMethod1() {
    testSame("function foo() {}; foo.call();");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToMethod2
  public void testRefToMethod2() {
    testSame("function foo() {}; foo.call.call();");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testCallUndefinedFunctionGivesNoWaring
  public void testCallUndefinedFunctionGivesNoWaring() {
    
    
    testSame("foo();");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToPropertyOfAliasedName
  public void testRefToPropertyOfAliasedName() {
    
    testSame(NAMES + "alert(a); alert(a.x);");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToUndefinedProperty1
  public void testRefToUndefinedProperty1() {
    testSame(NAMES + "alert(a.x);", UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToUndefinedProperty2
  public void testRefToUndefinedProperty2() {
    testSame(NAMES + "a.x();", UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToUndefinedProperty3
  public void testRefToUndefinedProperty3() {
    testSame(NAMES + "alert(a.c.x);", UNDEFINED_NAME_WARNING);
    testSame(GET_NAMES + "alert(a.c.x);", UNDEFINED_NAME_WARNING);
    testSame(SET_NAMES + "alert(a.c.x);", UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToUndefinedProperty4
  public void testRefToUndefinedProperty4() {
    testSame(NAMES + "alert(a.d.x);");
    testSame(GET_NAMES + "alert(a.d.x);");
    testSame(SET_NAMES + "alert(a.d.x);");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToDescendantOfUndefinedProperty1
  public void testRefToDescendantOfUndefinedProperty1() {
    testSame(NAMES + "var c = a.x.b;", UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToDescendantOfUndefinedProperty2
  public void testRefToDescendantOfUndefinedProperty2() {
    testSame(NAMES + "a.x.b();", UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToDescendantOfUndefinedProperty3
  public void testRefToDescendantOfUndefinedProperty3() {
    testSame(NAMES + "a.x.b = 3;", UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testUndefinedPrototypeMethodRefGivesNoWarning
  public void testUndefinedPrototypeMethodRefGivesNoWarning() {
    testSame("function Foo() {} var a = new Foo(); a.bar();");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testComplexPropAssignGivesNoWarning
  public void testComplexPropAssignGivesNoWarning() {
    testSame("var a = {}; var b = a.b = 3;");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testTypedefGivesNoWarning
  public void testTypedefGivesNoWarning() {
    testSame("var a = {};  a.b;");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToDescendantOfUndefinedPropertyGivesCorrectWarning
  public void testRefToDescendantOfUndefinedPropertyGivesCorrectWarning() {
    testSame("", NAMES + "a.x.b = 3;", UNDEFINED_NAME_WARNING,
             UNDEFINED_NAME_WARNING.format("a.x"));
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testNamespaceInjection
  public void testNamespaceInjection() {
    injectNamespace = true;
    testSame(NAMES + "var c = a.x.b;", UNDEFINED_NAME_WARNING);
  }

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
