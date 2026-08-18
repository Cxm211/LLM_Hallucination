// buggy code
  private void visitParameterList(NodeTraversal t, Node call,
      FunctionType functionType) {
    Iterator<Node> arguments = call.children().iterator();
    arguments.next(); // skip the function name

    Iterator<Node> parameters = functionType.getParameters().iterator();
    int ordinal = 0;
    Node parameter = null;
    Node argument = null;
    while (arguments.hasNext() &&
           parameters.hasNext()) {
      // If there are no parameters left in the list, then the while loop
      // above implies that this must be a var_args function.
        parameter = parameters.next();
      argument = arguments.next();
      ordinal++;

      validator.expectArgumentMatchesParameter(t, argument,
          getJSType(argument), getJSType(parameter), call, ordinal);
    }

    int numArgs = call.getChildCount() - 1;
    int minArgs = functionType.getMinArguments();
    int maxArgs = functionType.getMaxArguments();
    if (minArgs > numArgs || maxArgs < numArgs) {
      report(t, call, WRONG_ARGUMENT_COUNT,
              validator.getReadableJSTypeName(call.getFirstChild(), false),
              String.valueOf(numArgs), String.valueOf(minArgs),
              maxArgs != Integer.MAX_VALUE ?
              " and no more than " + maxArgs + " argument(s)" : "");
    }
  }

// relevant test
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

// com.google.javascript.jscomp.RenamePropertiesTest::testOverlappingOriginalAndGeneratedNamesStable
  public void testOverlappingOriginalAndGeneratedNamesStable() {
    testStableRenaming(
        "Bar.prototype = {b: function(){}, a: function(){}}; bar.b();",
        "Bar.prototype = {a: function(){}, b: function(){}}; bar.a();",
        "Bar.prototype = {c: function(){}, b: function(){}, a: function(){}};" +
        "bar.b();",
        "Bar.prototype = {c: function(){}, a: function(){}, b: function(){}};" +
        "bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testStableWithTrickyExternsChanges
  public void testStableWithTrickyExternsChanges() {
    test("Bar.prototype = {b: function(){}, a: function(){}}; bar.b();",
         "Bar.prototype = {a: function(){}, b: function(){}}; bar.a();");
    prevUsedPropertyMap = renameProperties.getPropertyMap();
    String externs = EXTERNS + "prop.b;";
    test(externs,
         "Bar.prototype = {new_f: function(){}, b: function(){}, " +
         "a: function(){}};bar.b();",
         "Bar.prototype = {c:function(){}, b:function(){}, a:function(){}};" +
         "bar.b();", null, null);
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesWithLeadingUnderscoresStable
  public void testRenamePropertiesWithLeadingUnderscoresStable() {
    testStableRenaming(
        "Bar.prototype = {_getA: function(){}, _b: 0}; bar._getA();",
        "Bar.prototype = {a: function(){}, b: 0}; bar.a();",
        "Bar.prototype = {_getA: function(){}, _c: 1, _b: 0}; bar._getA();",
        "Bar.prototype = {a: function(){}, c: 1,  b: 0}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyAddedToObjectStable
  public void testPropertyAddedToObjectStable() {
    testStableRenaming("var foo = {}; foo.prop = '';",
                       "var foo = {}; foo.a = '';",
                       "var foo = {}; foo.prop = ''; foo.a='';",
                       "var foo = {}; foo.a = ''; foo.b='';");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testAvoidingConflictsBetQuotedAndUnquotedPropertyNamesStable
  public void testAvoidingConflictsBetQuotedAndUnquotedPropertyNamesStable() {
    testStableRenaming(
        "Bar.prototype.foo = function(){}; Bar.prototype['b'] = 0; bar.foo();",
        "Bar.prototype.a = function(){}; Bar.prototype['b'] = 0; bar.a();",
        "Bar.prototype.foo = function(){}; Bar.prototype['a'] = 0; bar.foo();",
        "Bar.prototype.b = function(){}; Bar.prototype['a'] = 0; bar.b();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesFunctionCallStable
  public void testRenamePropertiesFunctionCallStable() {
    testStableRenaming(
        "var foo = {myProp: 0}; " +
        "f(JSCompiler_renameProperty('otherProp.myProp.someProp')); " +
        "foo.myProp = 1; foo.theirProp = 2; foo.yourProp = 3;",
        "var foo = {a: 0}; f('b.a.c'); " +
        "foo.a = 1; foo.d = 2; foo.e = 3;",
        "var bar = {newProp: 0}; var foo = {myProp: 0}; " +
        "f(JSCompiler_renameProperty('otherProp.myProp.someProp')); " +
        "foo.myProp = 1; foo.theirProp = 2; foo.yourProp = 3;",
        "var bar = {f: 0}; var foo = {a: 0}; f('b.a.c'); " +
        "foo.a = 1; foo.d = 2; foo.e = 3;");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testRenamePrototypes
  public void testRenamePrototypes() {
    
    test("Bar.prototype.getFoo=function(){};Bar.getFoo(b);" +
         "Bar.prototype.getBaz=function(){}",
         "Bar.prototype.a=function(){};Bar.a(b);" +
         "Bar.prototype.b=function(){}");
    test("Bar.prototype['getFoo']=function(){};Bar.getFoo(b);" +
         "Bar.prototype['getBaz']=function(){}",
         "Bar.prototype['a']=function(){};Bar.a(b);" +
         "Bar.prototype['b']=function(){}");
    test("Bar.prototype={'getFoo':function(){},2:function(){}}",
         "Bar.prototype={a:function(){},2:function(){}}");
    test("Bar.prototype={'getFoo':function(){}," +
         "'getBar':function(){}};b.getFoo()",
         "Bar.prototype={a:function(){}," +
         "b:function(){}};b.a()");

    test("Bar.prototype={'B':function(){}," +
         "'getBar':function(){}};b.getBar()",
         "Bar.prototype={b:function(){}," +
         "a:function(){}};b.a()");

    
    test("Bar.prototype={'a':function(){}," +
         "'b':function(){}};b.b()",
         "Bar.prototype={b:function(){}," +
         "a:function(){}};b.a()");

    
    test("Bar.prototype={'_getFoo':function(){}," +
         "'getBar':function(){}};b._getFoo()",
         "Bar.prototype={_getFoo:function(){}," +
         "a:function(){}};b._getFoo()");

    
    test("Bar.prototype={'toString':function(){}," +
         "'getBar':function(){}};b.toString()",
         "Bar.prototype={toString:function(){}," +
         "a:function(){}};b.toString()");

    
    test("Bar.prototype.foo=function(){}" +
         ";bar.foo();bar.a",
         "Bar.prototype.b=function(){}" +
         ";bar.b();bar.a");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testRenameProperties
  public void testRenameProperties() {
    test("var foo; foo.prop_='bar'", "var foo;foo.a='bar'");
    test("this.prop_='bar'", "this.a='bar'");
    test("this.prop='bar'", "this.prop='bar'");
    test("this['prop_']='bar'", "this['a']='bar'");
    test("this['prop']='bar'", "this['prop']='bar'");
    test("var foo={prop1_: 'bar',prop2_: 'baz'};",
         "var foo={a:'bar',b:'baz'}");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testBoth
  public void testBoth() {
    test("Bar.prototype.getFoo_=function(){};Bar.getFoo_(b);" +
         "Bar.prototype.getBaz_=function(){}",
         "Bar.prototype.a=function(){};Bar.a(b);" +
         "Bar.prototype.b=function(){}");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testPropertyNameThatIsBothObjLitKeyAndPrototypeProperty
  public void testPropertyNameThatIsBothObjLitKeyAndPrototypeProperty() {
    
    
    
    
    test("x.prototype.myprop=function(){};y={myprop:0};z.myprop",
         "x.prototype.myprop=function(){};y={myprop:0};z.myprop");

    
    
    
    test("x.prototype.myprop_=function(){};y={myprop_:0};z.myprop_",
         "x.prototype.a=function(){};y={a:0};z.a");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testModule
  public void testModule() {
    JSModule[] modules = createModules(
        "function Bar(){} var foo; Bar.prototype.getFoo_=function(x){};" +
        "foo.getFoo_(foo);foo.doo_=foo;foo.bloo_=foo;",
        "function Far(){} var too; Far.prototype.getGoo_=function(x){};" +
        "too.getGoo_(too);too.troo_=too;too.bloo_=too;");

    test(modules, new String[] {
        "function Bar(){}var foo; Bar.prototype.a=function(x){};" +
        "foo.a(foo);foo.d=foo;foo.c=foo;",
        "function Far(){}var too; Far.prototype.b=function(x){};" +
        "too.b(too);too.e=too;too.c=too;"
    });
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStableSimple1
  public void testStableSimple1() {
    testStable(
        "Bar.prototype.getFoo=function(){};Bar.getFoo(b);" +
        "Bar.prototype.getBaz=function(){}",
        "Bar.prototype.a=function(){};Bar.a(b);" +
        "Bar.prototype.b=function(){}",
        "Bar.prototype.getBar=function(){};Bar.getBar(b);" +
        "Bar.prototype.getFoo=function(){};Bar.getFoo(b);" +
        "Bar.prototype.getBaz=function(){}",
        "Bar.prototype.c=function(){};Bar.c(b);" +
        "Bar.prototype.a=function(){};Bar.a(b);" +
        "Bar.prototype.b=function(){}");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStableSimple2
  public void testStableSimple2() {
    testStable(
        "Bar.prototype['getFoo']=function(){};Bar.getFoo(b);" +
        "Bar.prototype['getBaz']=function(){}",
        "Bar.prototype['a']=function(){};Bar.a(b);" +
        "Bar.prototype['b']=function(){}",
        "Bar.prototype['getFoo']=function(){};Bar.getFoo(b);" +
        "Bar.prototype['getBar']=function(){};" +
        "Bar.prototype['getBaz']=function(){}",
        "Bar.prototype['a']=function(){};Bar.a(b);" +
        "Bar.prototype['c']=function(){};" +
        "Bar.prototype['b']=function(){}");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStableSimple3
  public void testStableSimple3() {
    testStable(
        "Bar.prototype={'getFoo':function(){}," +
        "'getBar':function(){}};b.getFoo()",
        "Bar.prototype={a:function(){}, b:function(){}};b.a()",
        "Bar.prototype={'getFoo':function(){}," +
        "'getBaz':function(){},'getBar':function(){}};b.getFoo()",
        "Bar.prototype={a:function(){}, c:function(){}, b:function(){}};b.a()");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStableOverlap
  public void testStableOverlap() {
    testStable(
        "Bar.prototype={'a':function(){},'b':function(){}};b.b()",
        "Bar.prototype={b:function(){},a:function(){}};b.a()",
        "Bar.prototype={'a':function(){},'b':function(){}};b.b()",
        "Bar.prototype={b:function(){},a:function(){}};b.a()");
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStableTrickyExternedMethods
  public void testStableTrickyExternedMethods() {
    test("Bar.prototype={'toString':function(){}," +
         "'getBar':function(){}};b.toString()",
         "Bar.prototype={toString:function(){}," +
         "a:function(){}};b.toString()");
    prevUsedRenameMap = renamePrototypes.getPropertyMap();
    String externs = EXTERNS + "prop.a;";
    test(externs,
         "Bar.prototype={'toString':function(){}," +
         "'getBar':function(){}};b.toString()",
         "Bar.prototype={toString:function(){}," +
         "b:function(){}};b.toString()", null, null);
  }

// com.google.javascript.jscomp.RenamePrototypesTest::testStable
  public void testStable(String input1, String expected1,
                         String input2, String expected2) {
    test(input1, expected1);
    prevUsedRenameMap = renamePrototypes.getPropertyMap();
    test(input2, expected2);
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameSimple
  public void testRenameSimple() {
    test("function Foo(v1, v2) {return v1;} Foo();",
         "function a(b, c) {return b;} a();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameGlobals
  public void testRenameGlobals() {
    test("var Foo; var Bar, y; function x() { Bar++; }",
         "var a; var b, c; function d() { b++; }");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameLocals
  public void testRenameLocals() {
    test("(function (v1, v2) {}); (function (v3, v4) {});",
        "(function (a, b) {}); (function (a, b) {});");
    test("function f1(v1, v2) {}; function f2(v3, v4) {};",
        "function c(a, b) {}; function d(a, b) {};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameRedeclaredGlobals
  public void testRenameRedeclaredGlobals() {
    test("function f1(v1, v2) {f1()};" +
         "" +
         "function f1(v3, v4) {f1()};",
         "function a(b, c) {a()};" +
         "function a(b, c) {a()};");

    localRenamingOnly = true;

    test("function f1(v1, v2) {f1()};" +
        "" +
        "function f1(v3, v4) {f1()};",
        "function f1(a, b) {f1()};" +
        "function f1(a, b) {f1()};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRecursiveFunctions1
  public void testRecursiveFunctions1() {
    test("var walk = function walk(node, aFunction) {" +
         "  walk(node, aFunction);" +
         "};",
         "var d = function a(b, c) {" +
         "  a(b, c);" +
         "};");

    localRenamingOnly = true;

    test("var walk = function walk(node, aFunction) {" +
         "  walk(node, aFunction);" +
         "};",
         "var walk = function a(b, c) {" +
         "  a(b, c);" +
         "};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRecursiveFunctions2
  public void testRecursiveFunctions2() {
    preserveFunctionExpressionNames = true;

    test("var walk = function walk(node, aFunction) {" +
         "  walk(node, aFunction);" +
         "};",
         "var c = function walk(a, b) {" +
         "  walk(a, b);" +
         "};");

    localRenamingOnly = true;

    test("var walk = function walk(node, aFunction) {" +
        "  walk(node, aFunction);" +
        "};",
        "var walk = function walk(a, b) {" +
        "  walk(a, b);" +
        "};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameLocalsClashingWithGlobals
  public void testRenameLocalsClashingWithGlobals() {
    test("function a(v1, v2) {return v1;} a();",
        "function a(b, c) {return b;} a();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameNested
  public void testRenameNested() {
    test("function f1(v1, v2) { (function(v3, v4) {}) }",
         "function a(b, c) { (function(d, e) {}) }");
    test("function f1(v1, v2) { function f2(v3, v4) {} }",
         "function a(b, c) { function d(e, f) {} }");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithExterns1
  public void testRenameWithExterns1() {
    String externs = "var foo;";
    test(externs, "var bar; foo(bar);", "var a; foo(a);", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithExterns2
  public void testRenameWithExterns2() {
    String externs = "var a;";
    test(externs, "var b = 5", "var b = 5", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testDoNotRenameExportedName
  public void testDoNotRenameExportedName() {
    test("_foo()", "_foo()");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithNameOverlap
  public void testRenameWithNameOverlap() {
    test("var a = 1; var b = 2; b + b;",
         "var a = 1; var b = 2; b + b;");
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithPrefix1
  public void testRenameWithPrefix1() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {return v1} Foo();",
        "function PRE_(a, b) {return a} PRE_();");
    prefix = DEFAULT_PREFIX;

  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithPrefix2
  public void testRenameWithPrefix2() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {var v3 = v1 + v2; return v3;} Foo();",
        "function PRE_(a, b) {var c = a + b; return c;} PRE_();");
    prefix = DEFAULT_PREFIX;
  }

// com.google.javascript.jscomp.RenameVarsTest::testRenameWithPrefix3
  public void testRenameWithPrefix3() {
    prefix = "a";
    test("function Foo() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "      A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,aa,ab;" +
         "  Foo();" +
         "} Bar();",

        "function a() {return 1;}" +
         "function aa() {" +
         "  var b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A," +
         "      B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$,ba,ca;" +
         "  a();" +
         "} aa();");
    prefix = DEFAULT_PREFIX;
  }

// com.google.javascript.jscomp.RenameVarsTest::testNamingBasedOnOrderOfOccurrence
  public void testNamingBasedOnOrderOfOccurrence() {
    test("var q,p,m,n,l,k; " +
             "(function (r) {}); try { } catch(s) {}; var t = q + q;",
         "var a,b,c,d,e,f; " +
             "(function(g) {}); try { } catch(h) {}; var i = a + a;"
         );
    test("function(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z," +
         "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,$){};" +
         "var a4,a3,a2,a1,b4,b3,b2,b1,ab,ac,ad,fg;function foo(){};",
         "function(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$){};" +
         "var aa,ba,ca,da,ea,fa,ga,ha,ia,ja,ka,la;function ma(){};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimple
  public void testStableRenameSimple() {
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "a", "L 0", "b", "L 1", "c");
    testRenameMap("function Foo(v1, v2) {return v1;} Foo();",
                  "function a(b, c) {return b;} a();", expectedVariableMap);

    expectedVariableMap = makeVariableMap(
        "Foo", "a", "L 0", "b", "L 1", "c", "L 2", "d");
    testRenameMapUsingOldMap("function Foo(v1, v2, v3) {return v1;} Foo();",
         "function a(b, c, d) {return b;} a();", expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameGlobals
  public void testStableRenameGlobals() {
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "a", "Bar", "b", "y", "c", "x", "d");
    testRenameMap("var Foo; var Bar, y; function x() { Bar++; }",
                  "var a; var b, c; function d() { b++; }",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap(
        "Foo", "a", "Bar", "b", "y", "c", "x", "d", "Baz", "f", "L 0" , "e");
    testRenameMapUsingOldMap(
        "var Foo, Baz; var Bar, y; function x(R) { return R + Bar++; }",
        "var a, f; var b, c; function d(e) { return e + b++; }",
        expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithPointlesslyAnonymousFunctions
  public void testStableRenameWithPointlesslyAnonymousFunctions() {
    VariableMap expectedVariableMap = makeVariableMap("L 0", "a", "L 1", "b");
    testRenameMap("function (v1, v2) {}; function (v3, v4) {};",
                  "function (a, b) {}; function (a, b) {};",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap("L 0", "a", "L 1", "b", "L 2", "c");
    testRenameMapUsingOldMap("function (v0, v1, v2) {}; function (v3, v4) {};",
                             "function (a, b, c) {}; function (a, b) {};",
                             expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameLocalsClashingWithGlobals
  public void testStableRenameLocalsClashingWithGlobals() {
    test("function a(v1, v2) {return v1;} a();",
         "function a(b, c) {return b;} a();");
    previouslyUsedMap = renameVars.getVariableMap();
    test("function bar(){return;}function a(v1, v2) {return v1;} a();",
         "function d(){return;}function a(b, c) {return b;} a();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameNested
  public void testStableRenameNested() {
    VariableMap expectedVariableMap = makeVariableMap(
        "f1", "a", "L 0", "b", "L 1", "c", "L 2", "d", "L 3", "e");
    testRenameMap("function f1(v1, v2) { (function(v3, v4) {}) }",
                  "function a(b, c) { (function(d, e) {}) }",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap(
        "f1", "a", "L 0", "b", "L 1", "c", "L 2", "d", "L 3", "e", "L 4", "f");
    testRenameMapUsingOldMap("function f1(v1, v2) { (function(v3, v4, v5) {}) }",
                             "function a(b, c) { (function(d, e, f) {}) }",
                             expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithExterns1
  public void testStableRenameWithExterns1() {
    String externs = "var foo;";
    test(externs, "var bar; foo(bar);", "var a; foo(a);", null, null);
    previouslyUsedMap = renameVars.getVariableMap();
    test(externs, "var bar, baz; foo(bar, baz);",
         "var a, b; foo(a, b);", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithExterns2
  public void testStableRenameWithExterns2() {
    String externs = "var a;";
    test(externs, "var b = 5", "var b = 5", null, null);
    previouslyUsedMap = renameVars.getVariableMap();
    test(externs, "var b = 5, catty = 9;", "var b = 5, c=9;", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithNameOverlap
  public void testStableRenameWithNameOverlap() {
    test("var a = 1; var b = 2; b + b;",
         "var a = 1; var b = 2; b + b;");
    previouslyUsedMap = renameVars.getVariableMap();
    test("var a = 1; var c, b = 2; b + b;",
         "var a = 1; var c, b = 2; b + b;");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithAnonymousFunctions
  public void testStableRenameWithAnonymousFunctions() {
    VariableMap expectedVariableMap = makeVariableMap("L 0", "a", "foo", "b");
    testRenameMap("function foo(bar){return bar;}foo(function(h){return h;});",
                  "function b(a){return a}b(function(a){return a;})",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap("foo", "b", "L 0", "a", "L 1", "c");
    testRenameMapUsingOldMap(
        "function foo(bar) {return bar;}foo(function(g,h) {return g+h;});",
        "function b(a){return a}b(function(a,c){return a+c;})",
        expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimpleExternsChanges
  public void testStableRenameSimpleExternsChanges() {
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "a", "L 0", "b", "L 1", "c");
    testRenameMap("function Foo(v1, v2) {return v1;} Foo();",
                  "function a(b, c) {return b;} a();", expectedVariableMap);

    expectedVariableMap = makeVariableMap("L 0", "b", "L 1", "c", "L 2", "a");
    String externs = "var Foo;";
    testRenameMapUsingOldMap(externs,
                             "function Foo(v1, v2, v0) {return v1;} Foo();",
                             "function Foo(b, c, a) {return b;} Foo();",
                             expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimpleLocalNameExterned
  public void testStableRenameSimpleLocalNameExterned() {
    test("function Foo(v1, v2) {return v1;} Foo();",
         "function a(b, c) {return b;} a();");

    previouslyUsedMap = renameVars.getVariableMap();

    String externs = "var b;";
    test(externs, "function Foo(v1, v2) {return v1;} Foo(b);",
         "function a(d, c) {return d;} a(b);", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimpleGlobalNameExterned
  public void testStableRenameSimpleGlobalNameExterned() {
    test("function Foo(v1, v2) {return v1;} Foo();",
         "function a(b, c) {return b;} a();");

    previouslyUsedMap = renameVars.getVariableMap();

    String externs = "var Foo;";
    test(externs, "function Foo(v1, v2, v0) {return v1;} Foo();",
         "function Foo(b, c, a) {return b;} Foo();", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithPrefix1AndUnstableLocalNames
  public void testStableRenameWithPrefix1AndUnstableLocalNames() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {return v1} Foo();",
         "function PRE_(a, b) {return a} PRE_();");

    previouslyUsedMap = renameVars.getVariableMap();

    prefix = "PRE_";
    test("function Foo(v0, v1, v2) {return v1} Foo();",
         "function PRE_(a, b, c) {return b} PRE_();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithPrefix2
  public void testStableRenameWithPrefix2() {
    prefix = "a";
    test("function Foo() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "      A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,aa,ab;" +
         "  Foo();" +
         "} Bar();",

         "function a() {return 1;}" +
         "function aa() {" +
         "  var b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A," +
         "      B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$,ba,ca;" +
         "  a();" +
         "} aa();");

    previouslyUsedMap = renameVars.getVariableMap();

    prefix = "a";
    test("function Foo() {return 1;}" +
         "function Baz() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "      A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,aa,ab;" +
         "  Foo();" +
         "} Bar();",

         "function a() {return 1;}" +
         "function ab() {return 1;}" +
         "function aa() {" +
         "  var b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A," +
         "      B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$,ba,ca;" +
         "  a();" +
         "} aa();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testContrivedExampleWhereConsistentRenamingIsWorse
  public void testContrivedExampleWhereConsistentRenamingIsWorse() {
    previouslyUsedMap = makeVariableMap(
        "Foo", "LongString", "L 0", "b", "L 1", "c");

    test("function Foo(v1, v2) {return v1;} Foo();",
         "function LongString(b, c) {return b;} LongString();");

    previouslyUsedMap = renameVars.getVariableMap();
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "LongString", "L 0", "b", "L 1", "c");
    assertVariableMapsEqual(expectedVariableMap, previouslyUsedMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testExportSimpleSymbolReservesName
  public void testExportSimpleSymbolReservesName() {
    test("var goog, x; goog.exportSymbol('a', x);",
         "var a, b; a.exportSymbol('a', b);");
    withClosurePass = true;
    test("var goog, x; goog.exportSymbol('a', x);",
         "var b, c; b.exportSymbol('a', c);");
  }

// com.google.javascript.jscomp.RenameVarsTest::testExportComplexSymbolReservesName
  public void testExportComplexSymbolReservesName() {
    test("var goog, x; goog.exportSymbol('a.b', x);",
         "var a, b; a.exportSymbol('a.b', b);");
    withClosurePass = true;
    test("var goog, x; goog.exportSymbol('a.b', x);",
         "var b, c; b.exportSymbol('a.b', c);");
  }

// com.google.javascript.jscomp.RenameVarsTest::testExportToNonStringDoesntExplode
  public void testExportToNonStringDoesntExplode() {
    withClosurePass = true;
    test("var goog, a, b; goog.exportSymbol(a, b);",
         "var a, b, c; a.exportSymbol(b, c);");
  }

// com.google.javascript.jscomp.RenameVarsTest::testDollarSignSuperExport1
  public void testDollarSignSuperExport1() {
    useGoogleCodingConvention = false;
    
    test("var x = function($super,duper,$fantastic){}",
         "var c = function($super,    a,        b){}");

    localRenamingOnly = false;
    test("var $super = 1", "var a = 1");

    useGoogleCodingConvention = true;
    test("var x = function($super,duper,$fantastic){}",
         "var d = function(a,     b,    c        ){}");
  }

// com.google.javascript.jscomp.RenameVarsTest::testDollarSignSuperExport2
  public void testDollarSignSuperExport2() {
    boolean normalizedExpectedJs = false;
    super.enableNormalize(false);

    useGoogleCodingConvention = false;
    
    test("var x = function($super,duper,$fantastic){};" +
            "var y = function($super,duper){};",
         "var c = function($super,    a,         b){};" +
            "var d = function($super,    a){};");

    localRenamingOnly = false;
    test("var $super = 1", "var a = 1");

    useGoogleCodingConvention = true;
    test("var x = function($super,duper,$fantastic){};" +
            "var y = function($super,duper){};",
         "var d = function(a,     b,    c         ){};" +
            "var e = function(     a,    b){};");

    super.disableNormalize();
  }

// com.google.javascript.jscomp.RenameVarsTest::testPseudoNames
  public void testPseudoNames() {
    generatePseudoNames = false;
    
    test("var foo = function(a, b, c){}",
         "var d = function(a, b, c){}");

    generatePseudoNames = true;
    test("var foo = function(a, b, c){}",
         "var $foo$$ = function($a$$, $b$$, $c$$){}");
    
    test("var a = function(a, b, c){}",
         "var $a$$ = function($a$$, $b$$, $c$$){}");
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testDoNotUseReplacementMap
  public void testDoNotUseReplacementMap() {
    useReplacementMap = false;
    test("var x = goog.getCssName('goog-footer-active')",
         "var x = 'goog-footer-active'");
    test("el.className = goog.getCssName('goog-colorswatch-disabled')",
         "el.className = 'goog-colorswatch-disabled'");
    test("setClass(goog.getCssName('active-buttonbar'))",
         "setClass('active-buttonbar')");
    Map<String, Integer> expected =
        new ImmutableMap.Builder<String, Integer>()
        .put("goog", 2)
        .put("footer", 1)
        .put("active", 2)
        .put("colorswatch", 1)
        .put("disabled", 1)
        .put("buttonbar", 1)
        .build();
    assertEquals(expected, cssNames);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithUnknownStringLiterals
  public void testOneArgWithUnknownStringLiterals() {
    test("var x = goog.getCssName('unknown')",
         "var x = 'unknown'", null, UNKNOWN_SYMBOL_WARNING);
    test("el.className = goog.getCssName('ooo')",
         "el.className = 'ooo'", null, UNKNOWN_SYMBOL_WARNING);
    test("setClass(goog.getCssName('ab'))",
         "setClass('ab')", null, UNKNOWN_SYMBOL_WARNING);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithSimpleStringLiterals
  public void testOneArgWithSimpleStringLiterals() {
    test("var x = goog.getCssName('buttonbar')",
         "var x = 'b'");
    test("el.className = goog.getCssName('colorswatch')",
         "el.className = 'c'");
    test("setClass(goog.getCssName('elephant'))",
         "setClass('e')");
    Map<String, Integer> expected =
        new ImmutableMap.Builder<String, Integer>()
        .put("buttonbar", 1)
        .put("colorswatch", 1)
        .put("elephant", 1)
        .build();
    assertEquals(expected, cssNames);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithCompositeClassNames
  public void testOneArgWithCompositeClassNames() {
    test("var x = goog.getCssName('goog-footer-active')",
         "var x = 'g-f-a'");
    test("el.className = goog.getCssName('goog-colorswatch-disabled')",
         "el.className = 'g-c-d'");
    test("setClass(goog.getCssName('active-buttonbar'))",
         "setClass('a-b')");
    Map<String, Integer> expected =
        new ImmutableMap.Builder<String, Integer>()
        .put("goog", 2)
        .put("footer", 1)
        .put("active", 2)
        .put("colorswatch", 1)
        .put("disabled", 1)
        .put("buttonbar", 1)
        .build();
    assertEquals(expected, cssNames);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithCompositeClassNamesWithUnknownParts
  public void testOneArgWithCompositeClassNamesWithUnknownParts() {
    test("var x = goog.getCssName('goog-header-active')",
         "var x = 'goog-header-active'", null, UNKNOWN_SYMBOL_WARNING);
    test("el.className = goog.getCssName('goog-colorswatch-focussed')",
         "el.className = 'goog-colorswatch-focussed'",
         null, UNKNOWN_SYMBOL_WARNING);
    test("setClass(goog.getCssName('inactive-buttonbar'))",
         "setClass('inactive-buttonbar')", null, UNKNOWN_SYMBOL_WARNING);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testTwoArgsWithStringLiterals
  public void testTwoArgsWithStringLiterals() {
    test("var x = goog.getCssName('header', 'active')",
         null, UNEXPECTED_STRING_LITERAL_ERROR);
    test("el.className = goog.getCssName('footer', window)",
         null, UNEXPECTED_STRING_LITERAL_ERROR);
    test("setClass(goog.getCssName('buttonbar', 'disabled'))",
         null, UNEXPECTED_STRING_LITERAL_ERROR);
    test("setClass(goog.getCssName(goog.getCssName('buttonbar'), 'active'))",
         null, UNEXPECTED_STRING_LITERAL_ERROR);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testTwoArsWithVariableFirstArg
  public void testTwoArsWithVariableFirstArg() {
    test("var x = goog.getCssName(baseClass, 'active')",
         "var x = baseClass + '-a'");
    test("el.className = goog.getCssName(this.getClass(), 'disabled')",
         "el.className = this.getClass() + '-d'");
    test("setClass(goog.getCssName(BASE_CLASS, 'disabled'))",
         "setClass(BASE_CLASS + '-d')");
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testZeroArguments
  public void testZeroArguments() {
    test("goog.getCssName()", null,
        ReplaceCssNames.INVALID_NUM_ARGUMENTS_ERROR);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testManyArguments
  public void testManyArguments() {
    test("goog.getCssName('a', 'b', 'c')", null,
        ReplaceCssNames.INVALID_NUM_ARGUMENTS_ERROR);
    test("goog.getCssName('a', 'b', 'c', 'd')", null,
        ReplaceCssNames.INVALID_NUM_ARGUMENTS_ERROR);
    test("goog.getCssName('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i')", null,
        ReplaceCssNames.INVALID_NUM_ARGUMENTS_ERROR);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testNonStringArgument
  public void testNonStringArgument() {
    test("goog.getCssName(window);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(555);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName([]);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName({});", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(null);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(undefined);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);

    test("goog.getCssName(baseClass, window);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, 555);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, []);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, {});", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, null);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, undefined);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testNoSymbolMapStripsCallAndDoesntIssueWarnings
  public void testNoSymbolMapStripsCallAndDoesntIssueWarnings() {
    String input = "[goog.getCssName('test'), goog.getCssName(base, 'active')]";
    Compiler compiler = new Compiler();
    ErrorManager errorMan = new BasicErrorManager() {
      @Override protected void printSummary() {}
      @Override public void println(CheckLevel level, JSError error) {}
    };
    compiler.setErrorManager(errorMan);
    Node root = compiler.parseTestCode(input);
    useReplacementMap = false;
    ReplaceCssNames replacer = new ReplaceCssNames(compiler, null);
    replacer.process(null, root);
    assertEquals("[\"test\",base+\"-active\"]", compiler.toSource(root));
    assertEquals("There should be no errors", 0, errorMan.getErrorCount());
    assertEquals("There should be no warnings", 0, errorMan.getWarningCount());
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testAssign
  public void testAssign() {
    test("foo.bar = goog.events.getUniqueId('foo_bar')",
         "foo.bar = 'a'");
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testObjectLiteral
  public void testObjectLiteral() {
    test("foo = { bar : goog.events.getUniqueId('foo_bar')}",
         "foo = { bar : 'a' }");
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testTwoNamespaces
  public void testTwoNamespaces() {
    test("foo.bar = goog.events.getUniqueId('foo_bar');\n"
         + "baz.blah = goog.place.getUniqueId('baz_blah');\n",
         "foo.bar = 'a';\n"
         + "baz.blah = 'a'\n");
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testLocalCall
  public void testLocalCall() {
    testSame(new String[] {
          "function Foo() { goog.events.getUniqueId('foo'); }"
        },
        ReplaceIdGenerators.NON_GLOBAL_ID_GENERATOR_CALL);
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testConditionalCall
  public void testConditionalCall() {
    testSame(new String[] {"if (x) foo = goog.events.getUniqueId('foo')"},
             ReplaceIdGenerators.CONDITIONAL_ID_GENERATOR_CALL);
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testReplaceSimpleMessage
  public void testReplaceSimpleMessage() {
    registerMessage(new JsMessage.Builder("MSG_A")
        .appendStringPart("Hi\nthere")
        .build());

    assertOutputEquals("var MSG_A = goog.getMsg('asdf');",
        "var MSG_A=\"Hi\\nthere\"");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testNameReplacement
  public void testNameReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_B")
        .appendStringPart("One ")
        .appendPlaceholderReference("measly")
        .appendStringPart(" ph")
        .build());

    assertOutputEquals(
        "var MSG_B = goog.getMsg('asdf {$measly}', {measly: x});",
        "var MSG_B=\"One \"+(x+\" ph\")");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testGetPropReplacement
  public void testGetPropReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_C")
        .appendPlaceholderReference("amount")
        .build());

    assertOutputEquals(
        "var MSG_C = goog.getMsg('${$amount}', {amount: a.b.amount});",
        "var MSG_C=a.b.amount");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testFunctionCallReplacement
  public void testFunctionCallReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_D")
        .appendPlaceholderReference("amount")
        .build());

    assertOutputEquals(
        "var MSG_D = goog.getMsg('${$amount}', {amount: getAmt()});",
        "var MSG_D=getAmt()");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testMethodCallReplacement
  public void testMethodCallReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_E")
        .appendPlaceholderReference("amount")
        .build());

    assertOutputEquals(
        "var MSG_E = goog.getMsg('${$amount}', {amount: obj.getAmt()});",
        "var MSG_E=obj.getAmt()");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testHookReplacement
  public void testHookReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_F")
        .appendStringPart("#")
        .appendPlaceholderReference("amount")
        .appendStringPart(".")
        .build());

    assertOutputEquals(
        "var MSG_F = goog.getMsg('${$amount}', {amount: (a ? b : c)});",
        "var MSG_F=\"#\"+((a?b:c)+\".\")");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testAddReplacement
  public void testAddReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_G")
        .appendPlaceholderReference("amount")
        .build());

    assertOutputEquals(
        "var MSG_G = goog.getMsg('${$amount}', {amount: x + ''});",
        "var MSG_G=x+\"\"");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testPlaceholderValueReferencedTwice
  public void testPlaceholderValueReferencedTwice()  {
    registerMessage(new JsMessage.Builder("MSG_H")
        .appendPlaceholderReference("dick")
        .appendStringPart(", ")
        .appendPlaceholderReference("dick")
        .appendStringPart(" and ")
        .appendPlaceholderReference("jane")
        .build());

    assertOutputEquals(
        "var MSG_H = goog.getMsg('{$dick}{$jane}', {jane: x, dick: y});",
        "var MSG_H=y+(\", \"+(y+(\" and \"+x)))");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testPlaceholderNameInLowerCamelCase
  public void testPlaceholderNameInLowerCamelCase()  {
    registerMessage(new JsMessage.Builder("MSG_I")
        .appendStringPart("Sum: $")
        .appendPlaceholderReference("amtEarned")
        .build());

    assertOutputEquals(
        "var MSG_I = goog.getMsg('${$amtEarned}', {amtEarned: x});",
        "var MSG_I=\"Sum: $\"+x");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testQualifiedMessageName
  public void testQualifiedMessageName()  {
    registerMessage(new JsMessage.Builder("MSG_J")
        .appendStringPart("One ")
        .appendPlaceholderReference("measly")
        .appendStringPart(" ph")
        .build());

    assertOutputEquals(
        "a.b.c.MSG_J = goog.getMsg('asdf {$measly}', {measly: x});",
        "a.b.c.MSG_J=\"One \"+(x+\" ph\")");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testSimpleMessageReplacementMissing
  public void testSimpleMessageReplacementMissing()  {
    assertOutputEquals("var MSG_E = 'd*6a0@z>t';", "var MSG_E=\"d*6a0@z>t\"");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testStrictModeAndMessageReplacementAbsentInBundle
  public void testStrictModeAndMessageReplacementAbsentInBundle()  {
    strictReplacement = true;
    process("var MSG_E = 'Hello';");
    assertEquals(1, compiler.getErrors().length);
    assertEquals(BUNDLE_DOES_NOT_HAVE_THE_MESSAGE,
        compiler.getErrors()[0].getType());
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testStrictModeAndMessageReplacementAbsentInNonEmptyBundle
  public void testStrictModeAndMessageReplacementAbsentInNonEmptyBundle()  {
    registerMessage(new JsMessage.Builder("MSG_J")
        .appendStringPart("One ")
        .appendPlaceholderReference("measly")
        .appendStringPart(" ph")
        .build());

    strictReplacement = true;
    process("var MSG_E = 'Hello';");
    assertEquals(1, compiler.getErrors().length);
    assertEquals(BUNDLE_DOES_NOT_HAVE_THE_MESSAGE,
        compiler.getErrors()[0].getType());
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testFunctionReplacementMissing
  public void testFunctionReplacementMissing()  {
    assertOutputEquals("var MSG_F = function() {return 'asdf'};",
        "var MSG_F=function(){return\"asdf\"}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testFunctionWithParamReplacementMissing
  public void testFunctionWithParamReplacementMissing()  {
    assertOutputEquals(
        "var MSG_G = function(measly) {return 'asdf' + measly};",
        "var MSG_G=function(measly){return\"asdf\"+measly}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testPlaceholderNameInLowerUnderscoreCase
  public void testPlaceholderNameInLowerUnderscoreCase()  {
    process("var MSG_J = goog.getMsg('${$amt_earned}', {amt_earned: x});");

    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals(MESSAGE_TREE_MALFORMED, error.getType());
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testBadPlaceholderReferenceInReplacement
  public void testBadPlaceholderReferenceInReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_K")
        .appendPlaceholderReference("amount")
        .build());

    process("var MSG_K = goog.getMsg('Hi {$jane}', {jane: x});");

    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals(MESSAGE_TREE_MALFORMED, error.getType());
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStyleNoPlaceholdersVarSyntax
  public void testLegacyStyleNoPlaceholdersVarSyntax()  {
    registerMessage(new JsMessage.Builder("MSG_A")
        .appendStringPart("Hi\nthere")
        .build());
    assertOutputEquals("var MSG_A = 'd*6a0@z>t';",
        "var MSG_A=\"Hi\\nthere\"");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStyleNoPlaceholdersFunctionSyntax
  public void testLegacyStyleNoPlaceholdersFunctionSyntax()  {
    registerMessage(new JsMessage.Builder("MSG_B")
        .appendStringPart("Hi\nthere")
        .build());

    assertOutputEquals("var MSG_B = function() {return 'asdf'};",
        "var MSG_B=function(){return\"Hi\\nthere\"}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStyleOnePlaceholder
  public void testLegacyStyleOnePlaceholder()  {
    registerMessage(new JsMessage.Builder("MSG_C")
        .appendStringPart("One ")
        .appendPlaceholderReference("measly")
        .appendStringPart(" ph")
        .build());
    assertOutputEquals(
        "var MSG_C = function(measly) {return 'asdf' + measly};",
        "var MSG_C=function(measly){return\"One \"+(measly+\" ph\")}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStyleTwoPlaceholders
  public void testLegacyStyleTwoPlaceholders()  {
    registerMessage(new JsMessage.Builder("MSG_D")
        .appendPlaceholderReference("dick")
        .appendStringPart(" and ")
        .appendPlaceholderReference("jane")
        .build());
    assertOutputEquals(
        "var MSG_D = function(jane, dick) {return jane + dick};",
        "var MSG_D=function(jane,dick){return dick+(\" and \"+jane)}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStylePlaceholderNameInLowerCamelCase
  public void testLegacyStylePlaceholderNameInLowerCamelCase() {
    registerMessage(new JsMessage.Builder("MSG_E")
        .appendStringPart("Sum: $")
        .appendPlaceholderReference("amtEarned")
        .build());
    assertOutputEquals(
        "var MSG_E = function(amtEarned) {return amtEarned + 'x'};",
        "var MSG_E=function(amtEarned){return\"Sum: $\"+amtEarned}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStylePlaceholderNameInLowerUnderscoreCase
  public void testLegacyStylePlaceholderNameInLowerUnderscoreCase() {
    registerMessage(new JsMessage.Builder("MSG_F")
        .appendStringPart("Sum: $")
        .appendPlaceholderReference("amt_earned")
        .build());

    
    assertOutputEquals(
        "var MSG_F = function(amt_earned) {return amt_earned + 'x'};",
        "var MSG_F=function(amt_earned){return\"Sum: $\"+amt_earned}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStyleBadPlaceholderReferenceInReplacemen
  public void testLegacyStyleBadPlaceholderReferenceInReplacemen() {
    registerMessage(new JsMessage.Builder("MSG_B")
        .appendStringPart("Ola, ")
        .appendPlaceholderReference("chimp")
        .build());

    process("var MSG_B = function(chump) {return chump + 'x'};");
    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals("Message parse tree malformed. "
        + "Unrecognized message placeholder referenced: chimp",
        error.description);
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowError1
  public void testThrowError1() {
    testDebugStrings(
        "throw Error('xyz');",
        "throw Error('a');",
        (new String[] { "a", "xyz" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowError2
  public void testThrowError2() {
    testDebugStrings(
        "throw Error('x' +\n    'yz');",
        "throw Error('a');",
        (new String[] { "a", "xyz" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowError3
  public void testThrowError3() {
    testDebugStrings(
        "throw Error('Unhandled mail' + ' search type ' + type);",
        "throw Error('a' + '`' + type);",
        (new String[] { "a", "Unhandled mail search type `" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowError4
  public void testThrowError4() {
    testDebugStrings(
        "\n" +
        "var A = function() {};\n" +
        "A.prototype.m = function(child) {\n" +
        "  if (this.haveChild(child)) {\n" +
        "    throw Error('Node: ' + this.getDataPath() +\n" +
        "                ' already has a child named ' + child);\n" +
        "  } else if (child.parentNode) {\n" +
        "    throw Error('Node: ' + child.getDataPath() +\n" +
        "                ' already has a parent');\n" +
        "  }\n" +
        "  child.parentNode = this;\n" +
        "};",

        "var A = function(){};\n" +
        "A.prototype.m = function(child) {\n" +
        "  if (this.haveChild(child)) {\n" +
        "    throw Error('a' + '`' + this.getDataPath() + '`' + child);\n" +
        "  } else if (child.parentNode) {\n" +
        "    throw Error('b' + '`' + child.getDataPath());\n" +
        "  }\n" +
        "  child.parentNode = this;\n" +
        "};",
        (new String[] {
            "a",
            "Node: ` already has a child named `",
            "b",
            "Node: ` already has a parent",
            }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowNonStringError
  public void testThrowNonStringError() {
    
    
    testDebugStrings(
        "throw Error(x('abc'));",
        "throw Error(x('abc'));",
        (new String[] { }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowConstStringError
  public void testThrowConstStringError() {
    testDebugStrings(
        "var AA = 'uvw', AB = 'xyz'; throw Error(AB);",
        "var AA = 'uvw', AB = 'xyz'; throw Error('a');",
        (new String [] { "a", "xyz" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowNewError1
  public void testThrowNewError1() {
    testDebugStrings(
        "throw new Error('abc');",
        "throw new Error('a');",
        (new String[] { "a", "abc" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowNewError2
  public void testThrowNewError2() {
    testDebugStrings(
        "throw new Error();",
        "throw new Error();",
        new String[] {});
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testStartTracer1
  public void testStartTracer1() {
    testDebugStrings(
        "goog.debug.Trace.startTracer('HistoryManager.updateHistory');",
        "goog.debug.Trace.startTracer('a');",
        (new String[] { "a", "HistoryManager.updateHistory" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testStartTracer2
  public void testStartTracer2() {
    testDebugStrings(
        "goog$debug$Trace.startTracer('HistoryManager', 'updateHistory');",
        "goog$debug$Trace.startTracer('a', 'b');",
        (new String[] {
            "a", "HistoryManager",
            "b", "updateHistory" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testStartTracer3
  public void testStartTracer3() {
    testDebugStrings(
        "goog$debug$Trace.startTracer('ThreadlistView',\n" +
        "                             'Updating ' + array.length + ' rows');",
        "goog$debug$Trace.startTracer('a', 'b' + '`' + array.length);",
        new String[] { "a", "ThreadlistView", "b", "Updating ` rows" });
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testStartTracer4
  public void testStartTracer4() {
    testDebugStrings(
        "goog.debug.Trace.startTracer(s, 'HistoryManager.updateHistory');",
        "goog.debug.Trace.startTracer(s, 'a');",
        (new String[] { "a", "HistoryManager.updateHistory" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerInitialization
  public void testLoggerInitialization() {
    testDebugStrings(
        "goog$debug$Logger$getLogger('my.app.Application');",
        "goog$debug$Logger$getLogger('a');",
        (new String[] { "a", "my.app.Application" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject1
  public void testLoggerOnObject1() {
    testDebugStrings(
        "var x = {};" +
        "x.logger_ = goog.debug.Logger.getLogger('foo');" +
        "x.logger_.info('Some message');",
        "var x = {};" +
        "x.logger_ = goog.debug.Logger.getLogger('a');" +
        "x.logger_.info('b');",
        new String[] {
            "a", "foo",
            "b", "Some message"});
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject2
  public void testLoggerOnObject2() {
    testSame(
        "var x = {};" +
        "x.info = function(a) {};" +
        "x.info('Some message');");
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject3
  public void testLoggerOnObject3() {
    testSame(
        "\n" +
        "var x = function() {};\n" +
        "x.prototype.info = function(a) {};" +
        "(new x).info('Some message');");
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject4
  public void testLoggerOnObject4() {
    testSame("(new x).info('Some message');");
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject5
  public void testLoggerOnObject5() {
    testSame("my$Thing.logger_.info('Some message');");
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnVar
  public void testLoggerOnVar() {
    testDebugStrings(
        "var logger = goog.debug.Logger.getLogger('foo');" +
        "logger.info('Some message');",
        "var logger = goog.debug.Logger.getLogger('a');" +
        "logger.info('b');",
        new String[] {
            "a", "foo",
            "b", "Some message"});
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnThis
  public void testLoggerOnThis() {
    testDebugStrings(
        "this.logger_ = goog.debug.Logger.getLogger('foo');" +
        "this.logger_.info('Some message');",
        "this.logger_ = goog.debug.Logger.getLogger('a');" +
        "this.logger_.info('b');",
        new String[] {
            "a", "foo",
            "b", "Some message"});
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedErrorString1
  public void testRepeatedErrorString1() {
    testDebugStrings(
        "Error('abc');Error('def');Error('abc');",
        "Error('a');Error('b');Error('a');",
        (new String[] { "a", "abc", "b", "def" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedErrorString2
  public void testRepeatedErrorString2() {
    testDebugStrings(
        "Error('a:' + u + ', b:' + v); Error('a:' + x + ', b:' + y);",
        "Error('a' + '`' + u + '`' + v); Error('a' + '`' + x + '`' + y);",
        (new String[] { "a", "a:`, b:`" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedErrorString3
  public void testRepeatedErrorString3() {
    testDebugStrings(
        "var AB = 'b'; throw Error(AB); throw Error(AB);",
        "var AB = 'b'; throw Error('a'); throw Error('a');",
        (new String[] { "a", "b" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedTracerString
  public void testRepeatedTracerString() {
    testDebugStrings(
        "goog$debug$Trace.startTracer('A', 'B', 'A');",
        "goog$debug$Trace.startTracer('a', 'b', 'a');",
        (new String[] { "a", "A", "b", "B" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedLoggerString
  public void testRepeatedLoggerString() {
    testDebugStrings(
        "goog$debug$Logger$getLogger('goog.net.XhrTransport');" +
        "goog$debug$Logger$getLogger('my.app.Application');" +
        "goog$debug$Logger$getLogger('my.app.Application');",
        "goog$debug$Logger$getLogger('a');" +
        "goog$debug$Logger$getLogger('b');" +
        "goog$debug$Logger$getLogger('b');",
        new String[] {
            "a", "goog.net.XhrTransport","b", "my.app.Application" });
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedStringsWithDifferentMethods
  public void testRepeatedStringsWithDifferentMethods() {
    test(
        "throw Error('A');"
            + "goog$debug$Trace.startTracer('B', 'A');"
            + "goog$debug$Logger$getLogger('C');"
            + "goog$debug$Logger$getLogger('B');"
            + "goog$debug$Logger$getLogger('A');"
            + "throw Error('D');"
            + "throw Error('C');"
            + "throw Error('B');"
            + "throw Error('A');",
        "throw Error('a');"
            + "goog$debug$Trace.startTracer('b', 'a');"
            + "goog$debug$Logger$getLogger('c');"
            + "goog$debug$Logger$getLogger('b');"
            + "goog$debug$Logger$getLogger('a');"
            + "throw Error('d');"
            + "throw Error('c');"
            + "throw Error('b');"
            + "throw Error('a');");
  }

// com.google.javascript.jscomp.RhinoErrorReporterTest::testTrailingComma
  public void testTrailingComma() throws Exception {
    String message =
        "Parse error. Internet Explorer has a non-standard " +
        "intepretation of trailing commas. Arrays will have the wrong " +
        "length and objects will not parse at all.";
    assertError(
        "var x = [1,];",
        RhinoErrorReporter.TRAILING_COMMA,
        message);
    assertError(
        "var x = {1: 2,};",
        RhinoErrorReporter.TRAILING_COMMA,
        message);
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValue
  public void testValue() {
    testChecks(" function f(i) {}",
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testConstValue
  public void testConstValue() {
    
    
    testChecks(" function f(CONST) {}",
        "function f(CONST) {" +
        "  jscomp.typecheck.checkType(CONST, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValueWithInnerFn
  public void testValueWithInnerFn() {
    testChecks(" function f(i) { function g() {} }",
        "function f(i) {" +
        "  function g() {}" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testNullValue
  public void testNullValue() {
    testChecks(" function f(i) {}",
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, [jscomp.typecheck.nullChecker]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValues
  public void testValues() {
    testChecks(" function f(i, j) {}",
        "function f(i, j) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "  jscomp.typecheck.checkType(j, " +
        "      [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testSkipParamOK
  public void testSkipParamOK() {
    testChecks(" function f(i, j) {}",
        "function f(i, j) {" +
        "  jscomp.typecheck.checkType(j, " +
        "      [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testUnion
  public void testUnion() {
    testChecks(" function f(x) {}",
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, [" +
        "      jscomp.typecheck.valueChecker('number'), " +
        "      jscomp.typecheck.valueChecker('string')" +
        "]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testUntypedParam
  public void testUntypedParam() {
    testChecks(" function f(x) {}", "function f(x) {}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testReturn
  public void testReturn() {
    testChecks(" function f() { return 'x'; }",
        "function f() {" +
        "  return jscomp.typecheck.checkType('x', " +
        "      [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testNativeClass
  public void testNativeClass() {
    testChecks(" function f(x) {}",
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, " +
        "      [jscomp.typecheck.externClassChecker('String')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testQualifiedClass
  public void testQualifiedClass() {
    testChecks("var goog = {}; goog.Foo = function() {};" +
        " function f(x) {}",
        "var goog = {}; goog.Foo = function() {};" +
        "goog.Foo.prototype['instance_of__goog.Foo'] = true;" +
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, " +
        "    [jscomp.typecheck.classChecker('goog.Foo')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testInnerClasses
  public void testInnerClasses() {
    testChecks(
        "function f() {  function inner() {} }" +
        "function g() {  function inner() {} }",
        "function f() {" +
        "   function inner() {}" +
        "  inner.prototype['instance_of__inner'] = true;" +
        "}" +
        "function g() {" +
        "   function inner$$1() {}" +
        "  inner$$1.prototype['instance_of__inner$$1'] = true;" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testInterface
  public void testInterface() {
    testChecks("function I() {}" +
        "function f(i) {}",
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "    [jscomp.typecheck.interfaceChecker('I')])" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterface
  public void testImplementedInterface() {
    testChecks("function I() {}" +
        "function f(i) {}" +
        "function C() {}",
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testExtendedInterface
  public void testExtendedInterface() {
    testChecks("function I() {}" +
        "function J() {}" +
        "function f(i) {}" +
        "function C() {}",
        "function I() {}" +
        "function J() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;" +
        "C.prototype['implements__J'] = true;");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterfaceOrdering
  public void testImplementedInterfaceOrdering() {
    testChecks("function I() {}" +
        "function f(i) {}" +
        "function C() {}" +
        "C.prototype.f = function() {};",
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;" +
        "C.prototype.f = function() {};");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterfaceOrderingGoogInherits
  public void testImplementedInterfaceOrderingGoogInherits() {
    testChecks("var goog = {}; goog.inherits = function(x, y) {};" +
        "function I() {}" +
        "function f(i) {}" +
        "function B() {}" +
        "function C() {}" +
        "goog.inherits(C, B);" +
        "C.prototype.f = function() {};",
        "var goog = {}; goog.inherits = function(x, y) {};" +
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function B() {}" +
        "B.prototype['instance_of__B'] = true;" +
        "function C() {}" +
        "goog.inherits(C, B);" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;" +
        "C.prototype.f = function() {};");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testInnerConstructor
  public void testInnerConstructor() {
    testChecks("(function() {  function C() {} })()",
        "(function() {" +
        "  function C() {} C.prototype['instance_of__C'] = true;" +
        "})()");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testReturnNothing
  public void testReturnNothing() {
    testChecks("function f() { return; }", "function f() { return; }");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testFunctionType
  public void testFunctionType() {
    testChecks("function f() {}", "function f() {}");
  }

// com.google.javascript.jscomp.SanityCheckTest::testUnnormalizeNodeTypes
  public void testUnnormalizeNodeTypes() throws Exception {
    otherPass = new CompilerPass() {
      @Override public void process(Node externs, Node root) {
        getLastCompiler().reportCodeChange();
        root.addChildToBack(new Node(Token.EXPR_VOID, Node.newNumber(0)));
      }
    };

    boolean exceptionCaught = false;
    try {
      test("var x = 3;", "var x=3;0;0");
    } catch (IllegalStateException e) {
      assertEquals("normalizeNodeType constraints violated",
          e.getMessage());
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
  }

// com.google.javascript.jscomp.SanityCheckTest::testUnnormalized
  public void testUnnormalized() throws Exception {
    otherPass = new CompilerPass() {
      @Override public void process(Node externs, Node root) {
        getLastCompiler().setNormalized();
      }
    };

    boolean exceptionCaught = false;
    try {
      test("while(1){}", "while(1){}");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains(
          "Normalize constraints violated:\nWHILE node"));
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
  }

// com.google.javascript.jscomp.SanityCheckTest::testConstantAnnotationMismatch
  public void testConstantAnnotationMismatch() throws Exception {
    otherPass = new CompilerPass() {
      @Override public void process(Node externs, Node root) {
        getLastCompiler().reportCodeChange();
        Node name = Node.newString(Token.NAME, "x");
        name.putBooleanProp(Node.IS_CONSTANT_NAME, true);
        root.addChildToBack(new Node(Token.EXPR_RESULT, name));
        getLastCompiler().setNormalized();
      }
    };

    boolean exceptionCaught = false;
    try {
      test("var x;", "var x; x;");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains(
          "The name x is not consistently annotated as constant."));
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testOneLevel
  public void testOneLevel() {
    testScoped("var g = goog;g.dom.createElement(g.dom.TagName.DIV);",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTwoLevel
  public void testTwoLevel() {
    testScoped("var d = goog.dom;d.createElement(d.TagName.DIV);",
               "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTransitive
  public void testTransitive() {
    testScoped("var d = goog.dom;var DIV = d.TagName.DIV;d.createElement(DIV);",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTransitiveInSameVar
  public void testTransitiveInSameVar() {
    testScoped("var d = goog.dom, DIV = d.TagName.DIV;d.createElement(DIV);",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testMultipleTransitive
  public void testMultipleTransitive() {
    testScoped(
        "var g=goog;var d=g.dom;var t=d.TagName;var DIV=t.DIV;" +
            "d.createElement(DIV);",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testFourLevel
  public void testFourLevel() {
    testScoped("var DIV = goog.dom.TagName.DIV;goog.dom.createElement(DIV);",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testWorksInClosures
  public void testWorksInClosures() {
    testScoped(
        "var DIV = goog.dom.TagName.DIV;" +
            "goog.x = function() {goog.dom.createElement(DIV);};",
        "goog.x = function() {goog.dom.createElement(goog.dom.TagName.DIV);};");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testOverridden
  public void testOverridden() {
    
    
    testScopedNoChanges(
        "var g = goog;", "goog.x = function(g) {g.z()};");
    
    testScopedNoChanges(
        "var g = goog;", "goog.x = function() {var g = {}; g.z()};");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTwoScopes
  public void testTwoScopes() {
    test(
        "goog.scope(function() {var g = goog;g.method()});" +
        "goog.scope(function() {g.method();});",

        "goog.method();g.method();");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTwoSymbolsInTwoScopes
  public void testTwoSymbolsInTwoScopes() {
    test(
        "var goog = {};" +
        "goog.scope(function() { var g = goog; g.Foo = function() {}; });" +
        "goog.scope(function() { " +
        "  var Foo = goog.Foo; goog.bar = function() { return new Foo(); };" +
        "});",
        "var goog = {};" +
        "goog.Foo = function() {};" +
        "goog.bar = function() { return new goog.Foo(); };");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testAliasOfSymbolInGoogScope
  public void testAliasOfSymbolInGoogScope() {
    test(
        "var goog = {};" +
        "goog.scope(function() {" +
        "  var g = goog;" +
        "  g.Foo = function() {};" +
        "  var Foo = g.Foo;" +
        "  Foo.prototype.bar = function() {};" +
        "});",
        "var goog = {}; goog.Foo = function() {};" +
        "goog.Foo.prototype.bar = function() {};");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedFunctionReturnThis
  public void testScopedFunctionReturnThis() {
    test("goog.scope(function() { " +
         "  var g = goog; g.f = function() { return this; };" +
         "});",
         "goog.f = function() { return this; };");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedFunctionAssignsToVar
  public void testScopedFunctionAssignsToVar() {
    test("goog.scope(function() { " +
         "  var g = goog; g.f = function(x) { x = 3; return x; };" +
         "});",
         "goog.f = function(x) { x = 3; return x; };");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedFunctionThrows
  public void testScopedFunctionThrows() {
    test("goog.scope(function() { " +
         "  var g = goog; g.f = function() { throw 'error'; };" +
         "});",
         "goog.f = function() { throw 'error'; };");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testPropertiesNotChanged
  public void testPropertiesNotChanged() {
    testScopedNoChanges("var x = goog.dom;", "y.x();");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testShadowedVar
  public void testShadowedVar() {
    test("var Popup = {};" +
         "var OtherPopup = {};" +
         "goog.scope(function() {" +
         "  var Popup = OtherPopup;" +
         "  Popup.newMethod = function() { return new Popup(); };" +
         "});",
         "var Popup = {};" +
         "var OtherPopup = {};" +
         "OtherPopup.newMethod = function() { return new OtherPopup(); };");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocType
  public void testJsDocType() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocParameter
  public void testJsDocParameter() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocExtends
  public void testJsDocExtends() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocImplements
  public void testJsDocImplements() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocEnum
  public void testJsDocEnum() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocReturn
  public void testJsDocReturn() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocThis
  public void testJsDocThis() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocThrows
  public void testJsDocThrows() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocSubType
  public void testJsDocSubType() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocTypedef
  public void testJsDocTypedef() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testArrayJsDoc
  public void testArrayJsDoc() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testObjectJsDoc
  public void testObjectJsDoc() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testUnionJsDoc
  public void testUnionJsDoc() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testFunctionJsDoc
  public void testFunctionJsDoc() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTestTypes
  public void testTestTypes() {
    try {
      testTypes(
          "var x = goog.Timer;",
          ""
          + " types.actual;"
          + " types.expected;");
      fail("Test types should fail here.");
    } catch (AssertionError e) {
    }
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testNullType
  public void testNullType() {
    testTypes(
        "var x = goog.Timer;",
        " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedThis
  public void testScopedThis() {
    testScopedFailure("this.y = 10;", ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
    testScopedFailure("var x = this;",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
    testScopedFailure("fn(this);", ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testAliasRedefinition
  public void testAliasRedefinition() {
    testScopedFailure("var x = goog.dom; x = goog.events;",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testAliasNonRedefinition
  public void testAliasNonRedefinition() {
    test("var y = {}; goog.scope(function() { goog.dom = y; });",
         "var y = {}; goog.dom = y;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedReturn
  public void testScopedReturn() {
    testScopedFailure("return;", ScopedAliases.GOOG_SCOPE_USES_RETURN);
    testScopedFailure("var x = goog.dom; return;",
        ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedThrow
  public void testScopedThrow() {
    testScopedFailure("throw 'error';", ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testUsedImproperly
  public void testUsedImproperly() {
    testFailure("var x = goog.scope(function() {});",
        ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testBadParameters
  public void testBadParameters() {
    testFailure("goog.scope()", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testFailure("goog.scope(10)", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testFailure("goog.scope(function() {}, 10)",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testFailure("goog.scope(function z() {})",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testFailure("goog.scope(function(a, b, c) {})",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testNonAliasLocal
  public void testNonAliasLocal() {
    testScopedFailure("var x = 10", ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
    testScopedFailure("var x = goog.dom + 10",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
    testScopedFailure("var x = goog['dom']",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
    testScopedFailure("var x = goog.dom, y = 10",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testNameCondition
  public void testNameCondition() throws Exception {
    FlowScope blind = newScope();
    Node condition = createVar(blind, "a", createNullableType(STRING_TYPE));

    
    FlowScope informedTrue = interpreter.
        getPreciserScopeKnowingConditionOutcome(condition, blind, true);
    assertEquals(STRING_TYPE, getVarType(informedTrue, "a"));

    
    FlowScope informedFalse = interpreter.
        getPreciserScopeKnowingConditionOutcome(condition, blind, false);
    assertEquals(createNullableType(STRING_TYPE),
        getVarType(informedFalse, "a"));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testNegatedNameCondition
  public void testNegatedNameCondition() throws Exception {
    FlowScope blind = newScope();
    Node a = createVar(blind, "a", createNullableType(STRING_TYPE));
    Node condition = new Node(Token.NOT);
    condition.addChildToBack(a);

    
    FlowScope informedTrue = interpreter.
        getPreciserScopeKnowingConditionOutcome(condition, blind, true);
    assertEquals(createNullableType(STRING_TYPE),
        getVarType(informedTrue, "a"));

    
    FlowScope informedFalse = interpreter.
        getPreciserScopeKnowingConditionOutcome(condition, blind, false);
    assertEquals(STRING_TYPE, getVarType(informedFalse, "a"));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testAssignCondition1
  public void testAssignCondition1() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.ASSIGN,
        createVar(blind, "a", createNullableType(OBJECT_TYPE)),
        createVar(blind, "b", createNullableType(OBJECT_TYPE)),
        Sets.newHashSet(
            new TypedName("a", OBJECT_TYPE),
            new TypedName("b", OBJECT_TYPE)),
        Sets.newHashSet(
            new TypedName("a", NULL_TYPE),
            new TypedName("b", NULL_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition1
  public void testSheqCondition1() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        createNumber(56),
        Sets.newHashSet(new TypedName("a", NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE))));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition2
  public void testSheqCondition2() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createNumber(56),
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a", NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE))));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition3
  public void testSheqCondition3() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createVar(blind, "b", createUnionType(STRING_TYPE, BOOLEAN_TYPE)),
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a", STRING_TYPE),
            new TypedName("b", STRING_TYPE)),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE)),
            new TypedName("b",
                createUnionType(STRING_TYPE, BOOLEAN_TYPE))));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition4
  public void testSheqCondition4() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", STRING_TYPE),
            new TypedName("b", VOID_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition5
  public void testSheqCondition5() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createVar(blind, "a", createUnionType(NULL_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", NULL_TYPE),
            new TypedName("b", VOID_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testSheqCondition6
  public void testSheqCondition6() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHEQ,
        createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(NUMBER_TYPE, VOID_TYPE)),
        Sets.newHashSet(
            new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)),
        Sets.newHashSet(
            new TypedName("a",
                createUnionType(STRING_TYPE, VOID_TYPE)),
            new TypedName("b",
                createUnionType(NUMBER_TYPE, VOID_TYPE))));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition1
  public void testShneCondition1() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        createNumber(56),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE))),
        Sets.newHashSet(new TypedName("a", NUMBER_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition2
  public void testShneCondition2() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createNumber(56),
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE))),
        Sets.newHashSet(new TypedName("a", NUMBER_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition3
  public void testShneCondition3() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createVar(blind, "b", createUnionType(STRING_TYPE, BOOLEAN_TYPE)),
        createVar(blind, "a", createUnionType(STRING_TYPE, NUMBER_TYPE)),
        Sets.newHashSet(new TypedName("a",
            createUnionType(STRING_TYPE, NUMBER_TYPE)),
            new TypedName("b",
                createUnionType(STRING_TYPE, BOOLEAN_TYPE))),
        Sets.newHashSet(new TypedName("a", STRING_TYPE),
            new TypedName("b", STRING_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition4
  public void testShneCondition4() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", STRING_TYPE),
            new TypedName("b", VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition5
  public void testShneCondition5() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createVar(blind, "a", createUnionType(NULL_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(NULL_TYPE)),
        Sets.newHashSet(new TypedName("a", VOID_TYPE),
            new TypedName("b", NULL_TYPE)),
        Sets.newHashSet(new TypedName("a", NULL_TYPE),
            new TypedName("b", NULL_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testShneCondition6
  public void testShneCondition6() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.SHNE,
        createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
        createVar(blind, "b", createUnionType(NUMBER_TYPE, VOID_TYPE)),
        Sets.newHashSet(
            new TypedName("a",
                createUnionType(STRING_TYPE, VOID_TYPE)),
            new TypedName("b",
                createUnionType(NUMBER_TYPE, VOID_TYPE))),
        Sets.newHashSet(
            new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testEqCondition1
  public void testEqCondition1() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        createVar(blind, "a", createUnionType(BOOLEAN_TYPE, VOID_TYPE)),
        createNull(),
        Sets.newHashSet(new TypedName("a", VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", BOOLEAN_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testEqCondition2
  public void testEqCondition2() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.NE,
        createNull(),
        createVar(blind, "a", createUnionType(BOOLEAN_TYPE, VOID_TYPE)),
        Sets.newHashSet(new TypedName("a", BOOLEAN_TYPE)),
        Sets.newHashSet(new TypedName("a", VOID_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testEqCondition3
  public void testEqCondition3() throws Exception {
    FlowScope blind = newScope();
    
    JSType nullableOptionalNumber =
        createUnionType(NULL_TYPE, VOID_TYPE, NUMBER_TYPE);
    
    JSType nullUndefined =
        createUnionType(VOID_TYPE, NULL_TYPE);
    testBinop(blind,
        Token.EQ,
        createVar(blind, "a", nullableOptionalNumber),
        createNull(),
        Sets.newHashSet(new TypedName("a", nullUndefined)),
        Sets.newHashSet(new TypedName("a", NUMBER_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testEqCondition4
  public void testEqCondition4() throws Exception {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        createVar(blind, "a", VOID_TYPE),
        createVar(blind, "b", VOID_TYPE),
        Sets.newHashSet(
            new TypedName("a", VOID_TYPE),
            new TypedName("b", VOID_TYPE)),
        Sets.newHashSet(
            new TypedName("a", NO_TYPE),
            new TypedName("b", NO_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInequalitiesCondition1
  public void testInequalitiesCondition1() {
    for (int op : Arrays.asList(Token.LT, Token.GT, Token.LE, Token.GE)) {
      FlowScope blind = newScope();
      testBinop(blind,
          op,
          createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
          createNumber(8),
          Sets.newHashSet(
              new TypedName("a", STRING_TYPE)),
          Sets.newHashSet(new TypedName("a",
              createUnionType(STRING_TYPE, VOID_TYPE))));
    }
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInequalitiesCondition2
  public void testInequalitiesCondition2() {
    for (int op : Arrays.asList(Token.LT, Token.GT, Token.LE, Token.GE)) {
      FlowScope blind = newScope();
      testBinop(blind,
          op,
          createVar(blind, "a",
              createUnionType(STRING_TYPE, NUMBER_TYPE, VOID_TYPE)),
          createVar(blind, "b",
              createUnionType(NUMBER_TYPE, NULL_TYPE)),
          Sets.newHashSet(
              new TypedName("a",
              createUnionType(STRING_TYPE, NUMBER_TYPE)),
              new TypedName("b",
              createUnionType(NUMBER_TYPE, NULL_TYPE))),
          Sets.newHashSet(
              new TypedName("a",
              createUnionType(STRING_TYPE, NUMBER_TYPE, VOID_TYPE)),
              new TypedName("b",
              createUnionType(NUMBER_TYPE, NULL_TYPE))));
    }
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInequalitiesCondition3
  public void testInequalitiesCondition3() {
    for (int op : Arrays.asList(Token.LT, Token.GT, Token.LE, Token.GE)) {
      FlowScope blind = newScope();
      testBinop(blind,
          op,
          createUntypedNumber(8),
          createVar(blind, "a", createUnionType(STRING_TYPE, VOID_TYPE)),
          Sets.newHashSet(
              new TypedName("a", STRING_TYPE)),
          Sets.newHashSet(new TypedName("a",
              createUnionType(STRING_TYPE, VOID_TYPE))));
    }
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testAnd
  public void testAnd() {
    FlowScope blind = newScope();
    testBinop(blind,
      Token.AND,
      createVar(blind, "b", createUnionType(STRING_TYPE, NULL_TYPE)),
      createVar(blind, "a", createUnionType(NUMBER_TYPE, VOID_TYPE)),
      Sets.newHashSet(new TypedName("a", NUMBER_TYPE),
          new TypedName("b", STRING_TYPE)),
      Sets.newHashSet(new TypedName("a",
          createUnionType(NUMBER_TYPE, VOID_TYPE)),
          new TypedName("b",
          createUnionType(STRING_TYPE, NULL_TYPE))));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testTypeof1
  public void testTypeof1() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        new Node(Token.TYPEOF, createVar(blind, "a", OBJECT_TYPE)),
        Node.newString("function"),
        Sets.newHashSet(
            new TypedName("a", U2U_CONSTRUCTOR_TYPE)),
        Sets.newHashSet(
            new TypedName("a", OBJECT_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testTypeof2
  public void testTypeof2() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.EQ,
        new Node(Token.TYPEOF, createVar(blind, "a", ALL_TYPE)),
        Node.newString("function"),
        Sets.newHashSet(
            new TypedName("a", U2U_CONSTRUCTOR_TYPE)),
        Sets.newHashSet(
            new TypedName("a", ALL_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInstanceOf
  public void testInstanceOf() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.INSTANCEOF,
        createVar(blind, "x", UNKNOWN_TYPE),
        createVar(blind, "s", STRING_OBJECT_FUNCTION_TYPE),
        Sets.newHashSet(
            new TypedName("x", STRING_OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)),
        Sets.newHashSet(
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInstanceOf2
  public void testInstanceOf2() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.INSTANCEOF,
        createVar(blind, "x",
            createUnionType(STRING_OBJECT_TYPE, NUMBER_OBJECT_TYPE)),
        createVar(blind, "s", STRING_OBJECT_FUNCTION_TYPE),
        Sets.newHashSet(
            new TypedName("x", STRING_OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)),
        Sets.newHashSet(
            new TypedName("x", NUMBER_OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInstanceOf3
  public void testInstanceOf3() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.INSTANCEOF,
        createVar(blind, "x", OBJECT_TYPE),
        createVar(blind, "s", STRING_OBJECT_FUNCTION_TYPE),
        Sets.newHashSet(
            new TypedName("x", STRING_OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)),
        Sets.newHashSet(
            new TypedName("x", OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)));
  }

// com.google.javascript.jscomp.SemanticReverseAbstractInterpreterTest::testInstanceOf4
  public void testInstanceOf4() {
    FlowScope blind = newScope();
    testBinop(blind,
        Token.INSTANCEOF,
        createVar(blind, "x", ALL_TYPE),
        createVar(blind, "s", STRING_OBJECT_FUNCTION_TYPE),
        Sets.newHashSet(
            new TypedName("x", STRING_OBJECT_TYPE),
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)),
        Sets.newHashSet(
            new TypedName("s", STRING_OBJECT_FUNCTION_TYPE)));
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testDegenerateSafeMoves
  public void testDegenerateSafeMoves() {      
    
    assertSafeMoveDegenerate("src: 1; env: ; dest: 3;");
    
    
    assertSafeMoveDegenerate("src: 1; env: 2; dest: 3;");
    
    
    assertSafeMoveDegenerate("src: 1; env: x; dest: 3;");
    assertSafeMoveDegenerate("src: x; env: 1; dest: 3;");
    
    
    assertSafeMoveDegenerate("src: 1; env: x++; dest: 3;");
    
    assertSafeMoveDegenerate("src: x++; env: 1; dest: 3;");
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testVisibilitySafeMoves
  public void testVisibilitySafeMoves() {      
    
    assertSafeMoveVisibility("src: 1; env: ; dest: 3;");
    
    
    assertSafeMoveVisibility("src: 1; env: 2; dest: 3;");
    
    
    assertSafeMoveVisibility("var x; src: 1; env: x; dest: 3;");
    assertSafeMoveVisibility("var x; src: x; env: 1; dest: 3;");
    
    
    assertSafeMoveVisibility("var x; src: 1; env: x++; dest: 3;");
    assertSafeMoveVisibility("var x; src: x++; env: 1; dest: 3;");
    
    
    assertSafeMoveVisibility(
        "var x;" +
        "function f(){" +
          "var y;" +
          "src: x;" +
          "env: y++;" +
          "dest: 3;" +
          "}");
    
    
    assertSafeMoveVisibility(
        "var x;" +
        "function f(){" +
          "var y;" +
          "src: x++;" +
          "env: y;" +
          "dest: 3;" +
          "}");
    
    
    assertSafeMoveVisibility(
        "var x;" +
        "var y;" +
        "function f(){" +
          "var y;" +
          "src: x;" +
          "env: y++;" +
          "dest: 3;" +
          "}");
    
    
    assertSafeMoveVisibility(
        "var x;" +
        "var y;" +
        "function f(){" +
          "var y;" +
          "src: x++;" +
          "env: y;" +
          "dest: 3;" +
          "}");
    
    
    
    assertSafeMoveVisibility(
        "function f(){" +
          "var x;" +
          "var y;" +
          "src: x;" +
          "env: y++;" +
          "dest: 3;" +
          "function inner() {" +
            "x" +
          "}" +
         "}");
    
    
    assertSafeMoveVisibility(
        "function f(){" +
          "var x;" +
          "var y;" +
          "src: x++;" +
          "env: y;" +
          "dest: 3;" +
          "function inner() {" +
            "x" +
          "}" +
        "}");
    
    
    assertSafeMoveVisibility(
        "var x = {};" +
        "function f(){" +
          "var y;" +
          "src: x.a;" +
          "env: y++;" +
          "dest: 3;" +
          "}");
    
    
    assertSafeMoveVisibility(
        "var x = {};" +
        "function f(){" +
          "var y;" +
          "src: x.a++;" +
          "env: y;" +
          "dest: 3;" +
          "}");
    
    
    assertSafeMoveVisibility(
        "var x = {};" +
        "src: x.a;" +
        "env: (function() {" +
          "x.a++;" +
        "});" +
        "dest: 3;");
    
    
    assertSafeMoveVisibility(
        "var x = {};" +
        "src: x.a++;" +
        "env: (function() {" +
          "x.a;" +
        "});" +
        "dest: 3;");
    
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testDegenerateUnsafeMoves
  public void testDegenerateUnsafeMoves() {
    
    
    assertUnsafeMoveDegenerate("src: x++; env: foo(y); dest: 3;");
   
    
    assertUnsafeMoveDegenerate("src: foo(y); env: x++; dest: 3;");
    
    
    assertUnsafeMoveDegenerate("src: x = 7; env: y = 3; dest:3;");
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testVisibilityUnsafeMoves
  public void testVisibilityUnsafeMoves() {
    
    
    assertUnsafeMoveVisibility("var x,y; src: x++; env: y; dest: 3;");
    
    
    assertUnsafeMoveVisibility("function f() {" +
        "var x,y; src: x++; env: y; dest: 3;" +
        "}");
    
    
    assertUnsafeMoveVisibility(
        "function f() {" +
          "var x,y; src: x++; env: y; dest: 3;" +
          "function inner() {" +
            "x; y;" +
          "}" +
         "}");
            
    
    assertUnsafeMoveVisibility("var x,y; src: x.a++; env: y.b; dest: 3;");
     
    
    assertUnsafeMoveVisibility("var x,y; src: y; env: x++; dest: 3;");
    
    
    assertUnsafeMoveVisibility("function f() {" +
        "var x,y; src: x; env: y++; dest: 3;" +
        "}");
    
    
    assertUnsafeMoveVisibility(
        "function f() {" +
          "var x,y; src: x; env: y++; dest: 3;" +
          "function inner() {" +
            "x; y;" +
          "}" +
         "}");
    
    
    assertUnsafeMoveVisibility("var x,y; src: x.a; env: y.b++; dest: 3;");
    
    
    assertUnsafeMoveVisibility("var x,y; src: x = 7; env: y = 3; dest: 3;");
    
    
    assertUnsafeMoveVisibility("function f() {" +
        "var x,y; src: x = 7; env: y = 3; dest: 3;" +
        "}");
    
    
    assertUnsafeMoveVisibility(
        "function f() {" +
          "var x,y; src: x = 7; env: y = 3; dest: 3;" +
          "function inner() {" +
            "x; y;" +
          "}" +
         "}");
    
    
    assertUnsafeMoveVisibility("var x,y; src: x.a = 7; env: y.b = 3; dest: 3;");
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testVisibilityMoveCalls
  public void testVisibilityMoveCalls() {
    
    
    
    
    
    
    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "var g = function(){};" +
        "function f(){" +
          "var y;" +
          "src: g();" +
          "env: x;" +
          "dest: 3;" +
          "}");
    
    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "var g = function(){};" +
        "function f(){" +
          "var y;" +
          "src: x;" +
          "env: g();" +
          "dest: 3;" +
          "}");
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testVisibilityMergesParametersWithHeap
  public void testVisibilityMergesParametersWithHeap() {
    
    
    
    
    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "function f(y){" +
          "src: x[0]++;" +
          "env: y;" +
          "dest: 3;" +
          "}");
    
    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "function f(y){" +
          "src: x[0];" +
          "env: y++;" +
          "dest: 3;" +
          "}");
    
    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "function f(y){" +
          "src: arguments[0]++;" +
          "env: y;" +
          "dest: 3;" +
          "}");
    
    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "function f(y){" +
          "src: arguments[0];" +
          "env: y++;" +
          "dest: 3;" +
          "}");   
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testMovedSideEffectsMustHaveSameControlFlow
  public void testMovedSideEffectsMustHaveSameControlFlow() {
    
    
    assertSafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "if (l) {" +
            "src: a++;" +
            "env: 3;" +
            "dest: 3;" +
          "}" +
        "}"
    );
    
    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "if (l) {" +
            "src: a++;" +
            "env: 3;" +
          "}" +
          "if (l) {" +
          "dest: 3;" +
          "}" +
        "}"
    );
    
    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "if (l) {" +
            "src: a++;" +
            "env: 3;" +
          "} else {" +
            "dest: 3;" +
          "}" +
        "}"
    );
    
    
    assertSafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "while (l) {" +
            "src: a++;" +
            "env: 3;" +
            "dest: 3;" +
          "}" +
        "}"
    );
    
    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "while (l) {" +
            "src: a++;" +
            "env: l;" +
            "break;" +
            "dest: 3;" +
          "}" +
        "}"
    );
    
    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "while (l) {" +
            "src: a++;" +
            "env: 3;" +
            "continue;" +
            "dest: 3;" +
          "}" +
        "}"
    );
    
    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "while (l) {" +
            "src: a++;" +
            "env: 3;" +
            "return;" +
            "dest: 3;" +
          "}" +
        "}"
    );
    
    
    assertSafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "do {" +
            "src: a++;" +
            "env: 3;" +
            "dest: 3;" +
          "} while(l)" +
        "}"
    );
    
    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "do {" +
            "src: a++;" +
            "env: 3;" +
          "} while(l)" +
          "dest: 3;" +
        "}"
    );
    
    
    
    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "switch(l) {" +
            "case 17:" +
              "src: a++;" +
              "env: 3;" +
              "dest: 3;" +
            "break;" +
          "}" +
        "}"
    );
    
    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "switch(l) {" +
            "case 17:" +
              "src: a++;" +
              "env: 3;" +
            "break;" +
            "case 18:" + 
              "dest: 3;" +
            "break;" +
          "}" +
        "}"
    );
    
    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "src: a++;" +
          "env: 3;" +
        "}" +
        "function g() {" +
          "dest: 3;" +
        "}"
    );
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefineNumber
  public void testDefineNumber() throws Exception {
    checkDefinitionsInJs(
        "var a = 1",
        ImmutableSet.of("DEF NAME a -> NUMBER"));

    checkDefinitionsInJs(
        "a = 1",
        ImmutableSet.of("DEF NAME a -> NUMBER"));

    checkDefinitionsInJs(
        "a.b = 1",
        ImmutableSet.of("DEF GETPROP a.b -> NUMBER"));

    
    checkDefinitionsInJs(
        "a[\"b\"] = 1",
        ImmutableSet.<String>of());

    checkDefinitionsInJs(
        "f().b = 1",
        ImmutableSet.of("DEF GETPROP null -> NUMBER"));

    checkDefinitionsInJs(
        "({a : 1}); o.a",
        ImmutableSet.of("DEF STRING null -> NUMBER",
                        "USE GETPROP o.a -> [NUMBER]"));

    checkDefinitionsInJs(
        "var a = {b : 1}; a.b",
        ImmutableSet.of("DEF NAME a -> <null>",
                        "DEF STRING null -> NUMBER",
                        "USE NAME a -> [<null>]",
                        "USE GETPROP a.b -> [NUMBER]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefineFunction
  public void testDefineFunction() throws Exception {
    checkDefinitionsInJs(
        "var a = function(){}",
        ImmutableSet.of("DEF NAME a -> FUNCTION"));

    checkDefinitionsInJs(
        "var a = function f(){}",
        ImmutableSet.of("DEF NAME f -> FUNCTION", "DEF NAME a -> FUNCTION"));

    checkDefinitionsInJs(
        "function a(){}",
        ImmutableSet.of("DEF NAME a -> FUNCTION"));

    checkDefinitionsInJs(
        "a = function(){}",
        ImmutableSet.of("DEF NAME a -> FUNCTION"));

    checkDefinitionsInJs(
        "a.b = function(){}",
        ImmutableSet.of("DEF GETPROP a.b -> FUNCTION"));

    
    checkDefinitionsInJs(
        "a[\"b\"] = function(){}",
        ImmutableSet.<String>of());

    checkDefinitionsInJs(
        "f().b = function(){}",
        ImmutableSet.of("DEF GETPROP null -> FUNCTION"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testFunctionArgumentsBasic
  public void testFunctionArgumentsBasic() throws Exception {
    checkDefinitionsInJs(
        "function f(a){return a}",
        ImmutableSet.of("DEF NAME a -> <null>",
                        "USE NAME a -> [<null>]",
                        "DEF NAME f -> FUNCTION"));

    checkDefinitionsInJs(
        "var a = 1; function f(a){return a}",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "DEF NAME a -> <null>",
                        "USE NAME a -> [<null>, NUMBER]",
                        "DEF NAME f -> FUNCTION"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testFunctionArgumentsInExterns
  public void testFunctionArgumentsInExterns() throws Exception {
    final String DEF = "var f = function(arg1, arg2){}";
    final String USE = "f(1, 2)";

    
    checkDefinitionsInJs(
        DEF + ";" + USE,
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "DEF NAME arg1 -> <null>",
                        "DEF NAME arg2 -> <null>",
                        "USE NAME f -> [FUNCTION]"));

    
    checkDefinitions(
        DEF, USE,
        ImmutableSet.of("DEF NAME f -> EXTERN FUNCTION",
                        "USE NAME f -> [EXTERN FUNCTION]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testMultipleDefinition
  public void testMultipleDefinition() throws Exception {
    checkDefinitionsInJs(
        "a = 1; a = 2; a",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "USE NAME a -> [NUMBER x 2]"));

    checkDefinitionsInJs(
        "a = 1; a = 'a'; a",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "DEF NAME a -> STRING",
                        "USE NAME a -> [NUMBER, STRING]"));

    checkDefinitionsInJs(
        "a = 1; b = 2; a = b; a",
        ImmutableSet.of("DEF NAME a -> <null>",
                        "DEF NAME a -> NUMBER",
                        "DEF NAME b -> NUMBER",
                        "USE NAME a -> [<null>, NUMBER]",
                        "USE NAME b -> [NUMBER]"));

    checkDefinitionsInJs(
        "a = 1; b = 2; c = b; c = a; c",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "DEF NAME b -> NUMBER",
                        "DEF NAME c -> <null>",
                        "USE NAME a -> [NUMBER]",
                        "USE NAME b -> [NUMBER]",
                        "USE NAME c -> [<null> x 2]"));

    checkDefinitionsInJs(
        "function f(){} f()",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]"));

    checkDefinitionsInJs(
        "function f(){} f.call(null)",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]",
                        "USE GETPROP f.call -> [FUNCTION]"));

    checkDefinitionsInJs(
        "function f(){} f.apply(null, [])",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]",
                        "USE GETPROP f.apply -> [FUNCTION]"));

    checkDefinitionsInJs(
        "function f(){} f.foobar()",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]"));

    checkDefinitionsInJs(
        "function f(){} f(); f.call(null)",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]",
                        "USE GETPROP f.call -> [FUNCTION]"));

  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefinitionInExterns
  public void testDefinitionInExterns() throws Exception {
    String externs = "var a = 1";

    checkDefinitionsInExterns(
        externs,
        ImmutableSet.of("DEF NAME a -> EXTERN NUMBER"));

    checkDefinitions(
        externs,
        "var b = 1",
        ImmutableSet.of("DEF NAME a -> EXTERN NUMBER", "DEF NAME b -> NUMBER"));

    checkDefinitions(
        externs,
        "a = \"foo\"; a",
        ImmutableSet.of("DEF NAME a -> EXTERN NUMBER",
                        "DEF NAME a -> STRING",
                        "USE NAME a -> [EXTERN NUMBER, STRING]"));

    checkDefinitionsInExterns(
        "var a = {}; a.b = 10",
        ImmutableSet.of("DEF GETPROP a.b -> EXTERN NUMBER",
                        "DEF NAME a -> EXTERN <null>",
                        "USE NAME a -> [EXTERN <null>]"));

    checkDefinitionsInExterns(
        "var a = {}; a.b",
        ImmutableSet.of("DEF GETPROP a.b -> EXTERN <null>",
                        "DEF NAME a -> EXTERN <null>",
                        "USE NAME a -> [EXTERN <null>]"));

    checkDefinitions(
        "var a = {}",
        "a.b = 1",
        ImmutableSet.of("DEF GETPROP a.b -> NUMBER",
                        "DEF NAME a -> EXTERN <null>",
                        "USE NAME a -> [EXTERN <null>]"));

    checkDefinitions(
        "var a = {}",
        "a.b",
        ImmutableSet.of("DEF NAME a -> EXTERN <null>",
                        "USE NAME a -> [EXTERN <null>]"));

    checkDefinitionsInExterns(
        externs,
        ImmutableSet.of("DEF NAME a -> EXTERN NUMBER"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testObjectLitInExterns
  public void testObjectLitInExterns() {
    checkDefinitions(
        "var goog = {};" +
        " goog.HYBRID;" +
        " goog.Enum = {HYBRID: 0, ROADMAP: 1};",
        "goog.HYBRID; goog.Enum.ROADMAP;",
        ImmutableSet.of(
            "DEF GETPROP goog.Enum -> EXTERN <null>",
            "DEF GETPROP goog.HYBRID -> EXTERN <null>",
            "DEF NAME goog -> EXTERN <null>",
            "DEF STRING null -> EXTERN NUMBER",
            "USE GETPROP goog.Enum -> [EXTERN <null>]",
            "USE GETPROP goog.Enum.ROADMAP -> [EXTERN NUMBER]",
            "USE GETPROP goog.HYBRID -> [EXTERN <null>, EXTERN NUMBER]",
            "USE NAME goog -> [EXTERN <null>]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testCallInExterns
  public void testCallInExterns() {
    checkDefinitionsInExterns(
        "var goog = {};" +
        " goog.Response = function() {};" +
        "goog.Response.prototype.get;" +
        "goog.Response.prototype.get().get;",
        ImmutableSet.of(
            "DEF GETPROP goog.Response -> EXTERN FUNCTION",
            "DEF GETPROP goog.Response.prototype.get -> EXTERN <null>",
            "DEF GETPROP null -> EXTERN <null>",
            "DEF NAME goog -> EXTERN <null>",
            "USE GETPROP goog.Response -> [EXTERN FUNCTION]",
            "USE GETPROP goog.Response.prototype.get -> [EXTERN <null> x 2]",
            "USE NAME goog -> [EXTERN <null>]"));
  }

// com.google.javascript.jscomp.SimpleFunctionAliasAnalysisTest::testFunctionGetIsAliased
  public void testFunctionGetIsAliased() { 
    
    String source =
        "function A(){};\n" +
        "var ns = {};\n" +
        "ns.B = function() {};\n" +
        "var C = function() {}\n" +
        "var D = function() {}\n" +
        "var aliasA = A;\n" +
        "var aliasB = ns.B;\n" +
        "var aliasC = C;\n" +
        "D();";
      
    compileAndRun(source);
  
    assertFunctionAliased(true, "A");
    assertFunctionAliased(true, "ns.B");
    assertFunctionAliased(true, "C");
    assertFunctionAliased(false, "D");
    
    
    source =
        "function A(){};\n" +
        "var ns = {};\n" +
        "ns.B = function() {};\n" +
        "var C = function() {}\n" +
        "ns.D = function() {}\n" +
        "var aliasA;\n" +
        "aliasA = A;\n" +
        "var aliasB = {};\n" +
        "aliasB.foo = ns.B;\n" +
        "var aliasC;\n" +
        "aliasC = C;\n" +
        "ns.D();";
      
    compileAndRun(source);
  
    assertFunctionAliased(true, "A");
    assertFunctionAliased(true, "ns.B");
    assertFunctionAliased(true, "C");
    assertFunctionAliased(false, "ns.D");
    
    
    source =
        "function A(){};\n" +
        "var ns = {};\n" +
        "ns.B = function() {};\n" +
        "var C = function() {}\n" +
        "function D() {}\n" +
        "var foo = function(a) {}\n" +
        "foo(A);\n" +
        "foo(ns.B)\n" +
        "foo(C);\n" +
        "D();";
      
    compileAndRun(source);
  
    assertFunctionAliased(true, "A");
    assertFunctionAliased(true, "ns.B");
    assertFunctionAliased(true, "C");
    assertFunctionAliased(false, "D");
    
    
    source =
        "function A(){};\n" +
        "var ns = {};\n" +
        "ns.B = function() {};\n" +
        "var C = function() {}\n" +
        "A();\n" +
        "ns.B();\n" +
        "C();\n";
        
    compileAndRun(source);
    
    assertFunctionAliased(false, "A");
    assertFunctionAliased(false, "ns.B");
    assertFunctionAliased(false, "C");
    
    
    source =
        "function A(){};\n" +
        "var ns = {};\n" +
        "ns.B = function() {};\n" +
        "var C = function() {}\n" +
        "A.foo;\n" +
        "ns.B.prototype;\n" +
        "C[0];\n";
        
    compileAndRun(source);
    
    assertFunctionAliased(false, "A");
    assertFunctionAliased(false, "ns.B");
    assertFunctionAliased(false, "C");
  }

// com.google.javascript.jscomp.SimpleFunctionAliasAnalysisTest::testFunctionGetIsExposedToCallOrApply
  public void testFunctionGetIsExposedToCallOrApply() { 
    
    String source =
        "function A(){};\n" +
        "function B(){};\n" +
        "function C(){};\n" +
        "var x;\n" +
        "A.call(x);\n" +
        "B.apply(x);\n" +
        "C();\n";
    
    compileAndRun(source);
  
    assertFunctionExposedToCallOrApply(true, "A");
    assertFunctionExposedToCallOrApply(true, "B");
    assertFunctionExposedToCallOrApply(false, "C");
    
    source =
      "var ns = {};" +
      "ns.A = function(){};\n" +
      "ns.B = function(){};\n" +
      "ns.C = function(){};\n" +
      "var x;\n" +
      "ns.A.call(x);\n" +
      "ns.B.apply(x);\n" +
      "ns.C();\n";
  
    compileAndRun(source);

    assertFunctionExposedToCallOrApply(true, "ns.A");
    assertFunctionExposedToCallOrApply(true, "ns.B");
    assertFunctionExposedToCallOrApply(false, "ns.C");
  }

// com.google.javascript.jscomp.SourceMap2Test::testBasicMapping
  public void testBasicMapping() throws Exception {
    compileAndCheck("function __BASIC__() { }");
  }

// com.google.javascript.jscomp.SourceMap2Test::testBasicMappingGoldenOutput
  public void testBasicMappingGoldenOutput() throws Exception {
    
    checkSourceMap("function __BASIC__() { }",

                   
                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"cAkBEBEB\"],\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"mappings\":[[0,1,9],\n" +
                   "[0,1,9,\"__BASIC__\"],\n" +
                   "[0,1,18],\n" +
                   "[0,1,21],\n" +
                   "]\n" +
                   "}\n");
  }

// com.google.javascript.jscomp.SourceMap2Test::testLiteralMappings
  public void testLiteralMappings() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) { " +
                    "var __VAR__ = '__STR__'; }");
  }

// com.google.javascript.jscomp.SourceMap2Test::testLiteralMappingsGoldenOutput
  public void testLiteralMappingsGoldenOutput() throws Exception {
    
    checkSourceMap("function __BASIC__(__PARAM1__, __PARAM2__) { " +
                   "var __VAR__ = '__STR__'; }",

                   
                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"cAkBABkBA/kCA+ADMBcBgBA9\"],\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"mappings\":[[0,1,9],\n" +
                   "[0,1,9,\"__BASIC__\"],\n" +
                   "[0,1,18],\n" +
                   "[0,1,19,\"__PARAM1__\"],\n" +
                   "[0,1,31,\"__PARAM2__\"],\n" +
                   "[0,1,43],\n" +
                   "[0,1,45],\n" +
                   "[0,1,49,\"__VAR__\"],\n" +
                   "[0,1,59],\n" +
                   "]\n" +
                   "}\n");
  }

// com.google.javascript.jscomp.SourceMap2Test::testMultilineMapping
  public void testMultilineMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}");
  }

// com.google.javascript.jscomp.SourceMap2Test::testMultiFunctionMapping
  public void testMultiFunctionMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}\n\n" +

                    "function __BASIC2__(__PARAM3__, __PARAM4__) {\n" +
                    "var __VAR2__ = '__STR2__';\n" +
                    "var __ANO2__ = \"__STR3__\";\n" +
                    "}\n\n");
  }

// com.google.javascript.jscomp.SourceMap2Test::testGoldenOutput0
  public void testGoldenOutput0() throws Exception {
    
    checkSourceMap("",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"\"],\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"mappings\":[]\n" +
                   "}\n");
  }

// com.google.javascript.jscomp.SourceMap2Test::testGoldenOutput1
  public void testGoldenOutput1() throws Exception {
    detailLevel = SourceMap.DetailLevel.ALL;

    checkSourceMap("function f(foo, bar) { foo = foo + bar + 2; return foo; }",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":" +
                       "[\"cAEBABIBA/ICA+ADICA/ICA+IDA9AEYBMBA5\"],\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"mappings\":[[0,1,9],\n" +
                   "[0,1,9,\"f\"],\n" +
                   "[0,1,10],\n" +
                   "[0,1,11,\"foo\"],\n" +
                   "[0,1,16,\"bar\"],\n" +
                   "[0,1,21],\n" +
                   "[0,1,23],\n" +
                   "[0,1,23,\"foo\"],\n" +
                   "[0,1,29,\"foo\"],\n" +
                   "[0,1,35,\"bar\"],\n" +
                   "[0,1,41],\n" +
                   "[0,1,44],\n" +
                   "[0,1,51,\"foo\"],\n" +
                   "]\n" +
                   "}\n");

    detailLevel = SourceMap.DetailLevel.SYMBOLS;

    checkSourceMap("function f(foo, bar) { foo = foo + bar + 2; return foo; }",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"cAEBA/ICA+IDE9IEA8IFA7IGg6MHA5\"],\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"mappings\":[[0,1,9],\n" +
                   "[0,1,9,\"f\"],\n" +
                   "[0,1,11,\"foo\"],\n" +
                   "[0,1,16,\"bar\"],\n" +
                   "[0,1,23,\"foo\"],\n" +
                   "[0,1,29,\"foo\"],\n" +
                   "[0,1,35,\"bar\"],\n" +
                   "[0,1,51,\"foo\"],\n" +
                   "]\n" +
                   "}\n");
  }

// com.google.javascript.jscomp.SourceMap2Test::testGoldenOutput2
  public void testGoldenOutput2() throws Exception {
    checkSourceMap("function f(foo, bar) {\r\n\n\n\nfoo = foo + bar + foo;" +
                   "\nreturn foo;\n}",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":" +
                       "[\"cAEBABIBA/ICA+ADICA/ICA+IDA9IEYBMBA5\"],\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"mappings\":[[0,1,9],\n" +
                   "[0,1,9,\"f\"],\n" +
                   "[0,1,10],\n" +
                   "[0,1,11,\"foo\"],\n" +
                   "[0,1,16,\"bar\"],\n" +
                   "[0,1,21],\n" +
                   "[0,5,0],\n" +
                   "[0,5,0,\"foo\"],\n" +
                   "[0,5,6,\"foo\"],\n" +
                   "[0,5,12,\"bar\"],\n" +
                   "[0,5,18,\"foo\"],\n" +
                   "[0,6,0],\n" +
                   "[0,6,7,\"foo\"],\n" +
                   "]\n" +
                   "}\n");
  }

// com.google.javascript.jscomp.SourceMap2Test::testGoldenOutput3
  public void testGoldenOutput3() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"IA\"],\n" +
                   "\"sources\":[\"c:\\myfile.js\"],\n" +
                   "\"mappings\":[[0,1,0,\"foo\"],\n" +
                   "]\n" +
                   "}\n");
  }

// com.google.javascript.jscomp.SourceMap2Test::testGoldenOutput4
  public void testGoldenOutput4() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;   boo;   goo;",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"IAMBMB\"],\n" +
                   "\"sources\":[\"c:\\myfile.js\"],\n" +
                   "\"mappings\":[[0,1,0,\"foo\"],\n" +
                   "[0,1,7,\"boo\"],\n" +
                   "[0,1,14,\"goo\"],\n" +
                   "]\n" +
                   "}\n");
  }

// com.google.javascript.jscomp.SourceMap2Test::testGoldenOutput5
  public void testGoldenOutput5() throws Exception {
    detailLevel = SourceMap.DetailLevel.ALL;

    checkSourceMap("c:\\myfile.js",
                   "\n" +
                   "var foo=a + 'this is a really long line that will force the"
                   + " mapping to span multiple lines 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + "' + c + d + e;",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":6,\n" +
                   "\"lineMaps\":[\"\",\n" +
                   "\"\",\n" +
                   "\"\",\n" +
                   "\"\",\n" +
                   "\"MAMBABA/!!AUSC\",\n" +
                   "\"AEA9AEA8AF\"],\n" +
                   "\"sources\":[\"c:\\myfile.js\"],\n" +
                   "\"mappings\":[[0,4,0],\n" +
                   "[0,4,4,\"foo\"],\n" +
                   "[0,4,8,\"a\"],\n" +
                   "[0,4,12],\n" +
                   "[0,4,1314,\"c\"],\n" +
                   "[0,4,1318,\"d\"],\n" +
                   "[0,4,1322,\"e\"],\n" +
                   "]\n" +
                   "}\n");

    detailLevel = SourceMap.DetailLevel.SYMBOLS;

    checkSourceMap("c:\\myfile.js",
        "\n" +
        "var foo=a + 'this is a really long line that will force the"
        + " mapping to span multiple lines 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + "' + c + d + e;",

        "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":6,\n" +
        "\"lineMaps\":[\"\",\n" +
        "\"\",\n" +
        "\"\",\n" +
        "\"\",\n" +
        "\"M/MBAB\",\n" +
        "\"ACA+ADA9AE\"],\n" +
        "\"sources\":[\"c:\\myfile.js\"],\n" +
        "\"mappings\":[[0,4,4,\"foo\"],\n" +
        "[0,4,8,\"a\"],\n" +
        "[0,4,1314,\"c\"],\n" +
        "[0,4,1318,\"d\"],\n" +
        "[0,4,1322,\"e\"],\n" +
        "]\n" +
        "}\n");
  }

// com.google.javascript.jscomp.SourceMap2Test::testBasicDeterminism
  public void testBasicDeterminism() throws Exception {
    RunResult result1 = compile("file1", "foo;", "file2", "bar;");
    RunResult result2 = compile("file2", "foo;", "file1", "bar;");

    String map1 = getSourceMap(result1);
    String map2 = getSourceMap(result2);

    
    

    
    String files1 = map1.split("\n")[4];
    String files2 = map2.split("\n")[4];

    assertEquals(files1, files2);
  }

// com.google.javascript.jscomp.SourceMap2Test::testEncodingRelativeId
  public void testEncodingRelativeId() {
    assertEquals(0, getRelativeId(0, 0));
    assertEquals(64 + (-1), getRelativeId(-1, 0));
    assertEquals(64 + (-32), getRelativeId(0, 32));
    assertEquals(31, getRelativeId(31, 0));
    assertEquals(4096 + (-33), getRelativeId(0, 33));
    assertEquals(32, getRelativeId(32, 0));
  }

// com.google.javascript.jscomp.SourceMap2Test::testEncodingIdLength
  public void testEncodingIdLength() {
    assertEquals(1, LineMapEncoder.getRelativeMappingIdLength(0, 0));
    assertEquals(1, LineMapEncoder.getRelativeMappingIdLength(-1, 0));
    assertEquals(1, LineMapEncoder.getRelativeMappingIdLength(0, 32));
    assertEquals(1, LineMapEncoder.getRelativeMappingIdLength(31, 0));
    assertEquals(2, LineMapEncoder.getRelativeMappingIdLength(0, 33));
    assertEquals(2, LineMapEncoder.getRelativeMappingIdLength(32, 0));

    assertEquals(2, LineMapEncoder.getRelativeMappingIdLength(2047, 0));
    assertEquals(3, LineMapEncoder.getRelativeMappingIdLength(2048, 0));
    assertEquals(2, LineMapEncoder.getRelativeMappingIdLength(0, 2048));
    assertEquals(3, LineMapEncoder.getRelativeMappingIdLength(0, 2049));
  }

// com.google.javascript.jscomp.SourceMap2Test::testEncoding
  public void testEncoding() throws IOException {
    assertEquals("AA", getEntry(0, 0, 1));
    assertEquals("EA", getEntry(0, 0, 2));
    assertEquals("8A", getEntry(0, 0, 16));
    assertEquals("!AQA", getEntry(0, 0, 17));
    assertEquals("!ARA", getEntry(0, 0, 18));
    assertEquals("!A+A", getEntry(0, 0, 63));
    assertEquals("!A/A", getEntry(0, 0, 64));
    assertEquals("!!ABAA", getEntry(0, 0, 65));
    assertEquals("!!A//A", getEntry(0, 0, 4096));
    assertEquals("!!!ABAAA", getEntry(0, 0, 4097));

    assertEquals("Af", getEntry(31, 0, 1));
    assertEquals("BAg", getEntry(32, 0, 1));
    assertEquals("AB", getEntry(32, 31, 1));

    assertEquals("!AQf", getEntry(31, 0, 17));
    assertEquals("!BQAg", getEntry(32, 0, 17));
    assertEquals("!AQB", getEntry(32, 31, 17));

    assertEquals("!A/B", getEntry(32, 31, 64));
    assertEquals("!!ABAB", getEntry(32, 31, 65));
  }

// com.google.javascript.jscomp.SourceMapTest::testBasicMapping
  public void testBasicMapping() throws Exception {
    compileAndCheck("function __BASIC__() { }");
  }

// com.google.javascript.jscomp.SourceMapTest::testLiteralMappings
  public void testLiteralMappings() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) { " +
                    "var __VAR__ = '__STR__'; }");
  }

// com.google.javascript.jscomp.SourceMapTest::testMultilineMapping
  public void testMultilineMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}");
  }

// com.google.javascript.jscomp.SourceMapTest::testMultiFunctionMapping
  public void testMultiFunctionMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}\n\n" +

                    "function __BASIC2__(__PARAM3__, __PARAM4__) {\n" +
                    "var __VAR2__ = '__STR2__';\n" +
                    "var __ANO2__ = \"__STR3__\";\n" +
                    "}\n\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testGoldenOutput0
  public void testGoldenOutput0() throws Exception {
    
    checkSourceMap("",

                   "{ \"file\" : \"testcode\"," +
                   " \"count\": 1 }\n" +

                   "[]\n" +

                   "\n" +
                   "[]\n" +

                   "\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testGoldenOutput1
  public void testGoldenOutput1() throws Exception {
    detailLevel = SourceMap.DetailLevel.ALL;

    checkSourceMap("function f(foo, bar) { foo = foo + bar + 2; return foo; }",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0,0,0,0,0,0,1,1,2,3,3,3,2,4,4,4,2,5,7,7,7,6,8,8,8,6," +
                   "9,9,9,6,10,11,11,11,11,11,11,11,12,12,12,12,5]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"testcode\",1,9]\n" +
                   "[\"testcode\",1,9,\"f\"]\n" +
                   "[\"testcode\",1,10]\n" +
                   "[\"testcode\",1,11,\"foo\"]\n" +
                   "[\"testcode\",1,16,\"bar\"]\n" +
                   "[\"testcode\",1,21]\n" +
                   "[\"testcode\",1,23]\n" +
                   "[\"testcode\",1,23,\"foo\"]\n" +
                   "[\"testcode\",1,29,\"foo\"]\n" +
                   "[\"testcode\",1,35,\"bar\"]\n" +
                   "[\"testcode\",1,41]\n" +
                   "[\"testcode\",1,44]\n" +
                   "[\"testcode\",1,51,\"foo\"]\n");

    detailLevel = SourceMap.DetailLevel.SYMBOLS;

    checkSourceMap("function f(foo, bar) { foo = foo + bar + 2; return foo; }",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0,0,0,0,0,0,1,1,0,2,2,2,0,3,3,3,0,0,4,4,4,0,5,5,5,0," +
                   "6,6,6,0,0,0,0,0,0,0,0,0,7,7,7,7,0]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"testcode\",1,9]\n" +
                   "[\"testcode\",1,9,\"f\"]\n" +
                   "[\"testcode\",1,11,\"foo\"]\n" +
                   "[\"testcode\",1,16,\"bar\"]\n" +
                   "[\"testcode\",1,23,\"foo\"]\n" +
                   "[\"testcode\",1,29,\"foo\"]\n" +
                   "[\"testcode\",1,35,\"bar\"]\n" +
                   "[\"testcode\",1,51,\"foo\"]\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testGoldenOutput2
  public void testGoldenOutput2() throws Exception {
    checkSourceMap("function f(foo, bar) {\r\n\n\n\nfoo = foo + bar + foo;" +
                   "\nreturn foo;\n}",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0,0,0,0,0,0,1,1,2,3,3,3,2,4,4,4,2,5,7,7,7,6,8,8,8," +
                   "6,9,9,9,6,10,10,10,11,11,11,11,11,11,11,12,12,12," +
                   "12,5]\n" +

                   "\n" +
                   "[]\n" +
                   "\n" +
                   "[\"testcode\",1,9]\n" +
                   "[\"testcode\",1,9,\"f\"]\n" +
                   "[\"testcode\",1,10]\n" +
                   "[\"testcode\",1,11,\"foo\"]\n" +
                   "[\"testcode\",1,16,\"bar\"]\n" +
                   "[\"testcode\",1,21]\n" +
                   "[\"testcode\",5,0]\n" +
                   "[\"testcode\",5,0,\"foo\"]\n" +
                   "[\"testcode\",5,6,\"foo\"]\n" +
                   "[\"testcode\",5,12,\"bar\"]\n" +
                   "[\"testcode\",5,18,\"foo\"]\n" +
                   "[\"testcode\",6,0]\n" +
                   "[\"testcode\",6,7,\"foo\"]\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testGoldenOutput3
  public void testGoldenOutput3() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0]\n" +

                   "\n" +
                   "[]\n" +
                   "\n" +
                   "[\"c:\\\\myfile.js\",1,0,\"foo\"]\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testGoldenOutput4
  public void testGoldenOutput4() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;   boo;   goo;",

                   "" +
                   "{ \"file\" : \"testcode\", \"count\": 1 }\n" +
                   "[0,0,0,1,1,1,1,2,2,2,2]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"c:\\\\myfile.js\",1,0,\"foo\"]\n" +
                   "[\"c:\\\\myfile.js\",1,7,\"boo\"]\n" +
                   "[\"c:\\\\myfile.js\",1,14,\"goo\"]\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testGoldenOutput5
  public void testGoldenOutput5() throws Exception {
    detailLevel = SourceMap.DetailLevel.ALL;

    checkSourceMap("c:\\myfile.js",
                   "\n" +
                   "var foo=a + 'this is a really long line that will force the"
                   + " mapping to span multiple lines 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + "' + c + d + e;",

                   "" +
                   "{ \"file\" : \"testcode\", \"count\": 6 }\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "[0,0,0,0,1,1,1,1,2,1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3]\n" +
                   "[4,1,5,1,6]\n" +
                   "\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "\n" +
                   "[\"c:\\\\myfile.js\",4,0]\n" +
                   "[\"c:\\\\myfile.js\",4,4,\"foo\"]\n" +
                   "[\"c:\\\\myfile.js\",4,8,\"a\"]\n" +
                   "[\"c:\\\\myfile.js\",4,12]\n" +
                   "[\"c:\\\\myfile.js\",4,1314,\"c\"]\n" +
                   "[\"c:\\\\myfile.js\",4,1318,\"d\"]\n" +
                   "[\"c:\\\\myfile.js\",4,1322,\"e\"]\n");

    detailLevel = SourceMap.DetailLevel.SYMBOLS;

    checkSourceMap("c:\\myfile.js",
        "\n" +
        "var foo=a + 'this is a really long line that will force the"
        + " mapping to span multiple lines 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + "' + c + d + e;",

        "" +
        "{ \"file\" : \"testcode\", \"count\": 6 }\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "[-1,-1,-1,-1,0,0,0,0,1]\n" +
        "[2,0,3,0,4]\n" +
        "\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "\n" +
        "[\"c:\\\\myfile.js\",4,4,\"foo\"]\n" +
        "[\"c:\\\\myfile.js\",4,8,\"a\"]\n" +
        "[\"c:\\\\myfile.js\",4,1314,\"c\"]\n" +
        "[\"c:\\\\myfile.js\",4,1318,\"d\"]\n" +
        "[\"c:\\\\myfile.js\",4,1322,\"e\"]\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testBasicDeterminism
  public void testBasicDeterminism() throws Exception {
    RunResult result1 = compile("file1", "foo;", "file2", "bar;");
    RunResult result2 = compile("file2", "foo;", "file1", "bar;");

    String map1 = getSourceMap(result1);
    String map2 = getSourceMap(result2);

    
    

    
    String files1 = map1.split("\n")[4];
    String files2 = map2.split("\n")[4];

    assertEquals(files1, files2);
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInline
  public void testSpecializeInline() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B());A()};" + 
        "var B = function() {return 6};" +
        "A();",       
        
        "A();" +
        "B();" +
        "B = function() {return 7};" +
        "A();" +
        "B();"
        );

    test(modules, new String[] {
        
        "var A = function() {alert(6);A()};" +     
        "A();" +
        "var B;",    
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        "A();" +
        "B();" +
        "B = function() {return 7};" +
        "A();" +
        "B();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeCascadedInline
  public void testSpecializeCascadedInline() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B());A()};" + 
        "var B = function() {return C()};" +
        "var C = function() {return 6};" +
        "A();",       
        
        "B = function() {return 7};" +
    "A();");

    test(modules, new String[] {
        
        "var A = function() {alert(6);A()};" +     
        "A();" +
        "var B, C;",    
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return C()};" + 
        "C = function() {return 6};" + 
        "B = function() {return 7};" +
        "A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInlineWithMultipleDependents
  public void testSpecializeInlineWithMultipleDependents() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B());A()};" + 
        "var B = function() {return 6};" +
        "A();",       
        
        "B = function() {return 7};" +
        "A();",
        
        "A();"
    );

    test(modules, new String[] {
        
        "var A = function() {alert(6);A()};" +     
        "A();" +
        "var B;",    
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        "B = function() {return 7};" +
        "A();",
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        "A();",

    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInlineWithNamespaces
  public void testSpecializeInlineWithNamespaces() {
    JSModule[] modules = createModuleStar(
        
        "var ns = {};" +
        
        "ns.A = function() {alert(B());ns.A()};" + 
        "var B = function() {return 6};" +
        "ns.A();",       
        
        "B = function() {return 7};" +
    "ns.A();");

    test(modules, new String[] {
        
        "var ns = {};" +
        "ns.A = function() {alert(6);ns.A()};" +     
        "ns.A();" +
        "var B;",    
        
        "ns.A = function() {alert(B());ns.A()};" + 
        "B = function() {return 6};" + 
        "B = function() {return 7};" +
        "ns.A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInlineWithRegularFunctions
  public void testSpecializeInlineWithRegularFunctions() {
    JSModule[] modules = createModuleStar(
        
        
        "function A() {alert(B());A()}" + 
        "function B() {return 6}" +
        "A();",       
        
        "B = function() {return 7};" +
    "A();");

    test(modules, new String[] {
        
        "function A() {alert(6);A()}" +     
        "A();" +
        "var B;",    
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        
        "B = function() {return 7};" +
        "A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testDontSpecializeLocalNonAnonymousFunctions
  public void testDontSpecializeLocalNonAnonymousFunctions() {
    
    enableNormalize(false);

    JSModule[] modules = createModuleStar(
        
        "(function(){var noSpecialize = " +
            "function() {alert(6)};noSpecialize()})()",       
        
        "");

    test(modules, new String[] {
        
        "(function(){var noSpecialize = " +
            "function() {alert(6)};noSpecialize()})()", 
        
        ""
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testAddDummyVarsForRemovedFunctions
  public void testAddDummyVarsForRemovedFunctions() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B() + C());A()};" + 
        "var B = function() {return 6};" +
        "var C = function() {return 8};" +
        "A();",       
        
        "" +
    "A();");

    test(modules, new String[] {
        
        "var A = function() {alert(6 + 8);A()};" +     
        "A();" +
        "var B, C;",    
        
        "A = function() {alert(B() + C());A()};" + 
        "B = function() {return 6};" + 
        "C = function() {return 8};" + 
        "A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeRemoveUnusedProperties
  public void testSpecializeRemoveUnusedProperties() {
    JSModule[] modules = createModuleStar(
        
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" + 
        "Foo.prototype.b = function() {return 6};" +
        "Foo.prototype.c = function() {return 7};" +
        "var aliasA = Foo.prototype.a;" + 
        "var x = new Foo();" +
        "x.a();",       
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "var aliasA = Foo.prototype.a;" +
        "var x = new Foo();" +
        "x.a();", 
        
        "Foo.prototype.b = function() {return 6};" +
        "Foo.prototype.c = function() {return 7};"        
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testDontSpecializeAliasedFunctions_inline
  public void testDontSpecializeAliasedFunctions_inline() {
    JSModule[] modules = createModuleStar(
        
        
        "function A() {alert(B());A()}" + 
        "function B() {return 6}" +
        "var aliasA = A;" +
        "A();",       
        
        "B = function() {return 7};" +
        "B();");

    test(modules, new String[] {
        
        
        "function A() {alert(B());A()}" + 
        "function B() {return 6}" +
        "var aliasA = A;" +
        "A();",       
        
        "B = function() {return 7};" +
        "B();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testDontSpecializeAliasedFunctions_remove_unused_properties
  public void testDontSpecializeAliasedFunctions_remove_unused_properties() {
    JSModule[] modules = createModuleStar(
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" + 
        "Foo.prototype.b = function() {return 6};" +
        "var aliasB = Foo.prototype.b;" +
        "Foo.prototype.c = function() {return 7};" +
        "Foo.prototype.d = function() {return 7};" +
        "var aliasA = Foo.prototype.a;" + 
        "var x = new Foo();" +
        "x.a();" +
        "var aliasC = (new Foo).c",       
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "Foo.prototype.b = function() {return 6};" +
        "var aliasB = Foo.prototype.b;" +
        "Foo.prototype.c = function() {return 7};" +
        "var aliasA = Foo.prototype.a;" + 
        "var x = new Foo();" +
        "x.a();" +
        "var aliasC = (new Foo).c", 
        
        "Foo.prototype.d = function() {return 7};"        
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeDevirtualizePrototypeMethods
  public void testSpecializeDevirtualizePrototypeMethods() {
    JSModule[] modules = createModuleStar(
        
        "" +
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a();return 7};" +
        "Foo.prototype.b = function() {this.a()};" +
        "var x = new Foo();" +
        "x.a();",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "var JSCompiler_StaticMethods_a =" +
              "function(JSCompiler_StaticMethods_a$self) {" + 
           "JSCompiler_StaticMethods_a(JSCompiler_StaticMethods_a$self);" +
           "return 7" +
        "};" +
        "var x = new Foo();" +
        "JSCompiler_StaticMethods_a(x);",
        
        "Foo.prototype.a = function() {this.a();return 7};" +
        "Foo.prototype.b = function() {this.a()};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeDevirtualizePrototypeMethodsWithInline
  public void testSpecializeDevirtualizePrototypeMethodsWithInline() {
    JSModule[] modules = createModuleStar(
        
        "" +
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {return 7};" +
        "var x = new Foo();" +
        "var z = x.a();",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "var x = new Foo();" +
        "var z = 7;",
        
        "Foo.prototype.a = function() {return 7};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testRemovedFunctions
    public void testRemovedFunctions() {
      testSame("function F(){}\nvar G = function(a){};");
      
      assertEquals(ImmutableSet.of(), lastState.getRemovedFunctions());
      
      Node functionF = findFunction("F");
      
      lastState.reportRemovedFunction(functionF, functionF.getParent());
      assertEquals(ImmutableSet.of(functionF), lastState.getRemovedFunctions());

      Node functionG = findFunction("F");
      
      lastState.reportRemovedFunction(functionG, functionF.getParent());
      assertEquals(ImmutableSet.of(functionF, functionG),
          lastState.getRemovedFunctions());
      
      assertEquals(ImmutableSet.of(), lastState.getSpecializedFunctions());
    }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializedFunctions
    public void testSpecializedFunctions() {
      testSame("function F(){}\nvar G = function(a){};");
      
      assertEquals(ImmutableSet.of(), lastState.getSpecializedFunctions());
      
      Node functionF = findFunction("F");
      
      lastState.reportSpecializedFunction(functionF);
      assertEquals(ImmutableSet.of(functionF),
          lastState.getSpecializedFunctions());

      Node functionG = findFunction("F");
      
      lastState.reportSpecializedFunction(functionG);
      assertEquals(ImmutableSet.of(functionF, functionG),
          lastState.getSpecializedFunctions());
      
      assertEquals(ImmutableSet.of(), lastState.getRemovedFunctions());
    }

// com.google.javascript.jscomp.SpecializeModuleTest::testCanFixupFunction
    public void testCanFixupFunction() {
      testSame("function F(){}\n" +
               "var G = function(a){};\n" +
               "var ns = {};" +
               "ns.H = function(){};" + 
               "var ns2 = {I : function anon1(){}};" +
               "(function anon2(){})();");
      
      assertTrue(lastState.canFixupFunction(findFunction("F")));
      assertTrue(lastState.canFixupFunction(findFunction("G")));
      assertTrue(lastState.canFixupFunction(findFunction("ns.H")));
      assertFalse(lastState.canFixupFunction(findFunction("anon1")));
      assertFalse(lastState.canFixupFunction(findFunction("anon2")));
      
      
      testSame("function A(){}\n" +
          "var aliasA = A;\n");

      assertFalse(lastState.canFixupFunction(findFunction("A")));
    }

// com.google.javascript.jscomp.StrictModeCheckTest::testWith
  public void testWith() {
    test("var a; function foo(obj) { with (obj) { a = 3; }}", null,
         StrictModeCheck.WITH_DISALLOWED);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval
  public void testEval() {
    test("function foo() { eval('a'); }", null,
         StrictModeCheck.EVAL_USE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval2
  public void testEval2() {
    test("function foo(eval) {}", null,
         StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval3
  public void testEval3() {
    testSame("function foo() {} foo.eval = 3;");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval4
  public void testEval4() {
    test("function foo() { var eval = 3; }", null,
         StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval5
  public void testEval5() {
    test("function eval() {}", null, StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval6
  public void testEval6() {
    test("try {} catch (eval) {}", null, StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval7
  public void testEval7() {
    testSame("var o = {eval: 3};");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval8
  public void testEval8() {
    testSame("var a; eval: while (true) { a = 3; }");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testUnknownVariable
  public void testUnknownVariable() {
    test("function foo(a) { a = b; }", null, StrictModeCheck.UNKNOWN_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testUnknownVariable2
  public void testUnknownVariable2() {
    test("a: while (true) { a = 3; }", null, StrictModeCheck.UNKNOWN_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testUnknownVariable3
  public void testUnknownVariable3() {
    testSame("try {} catch (ex) { ex = 3; }");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments
  public void testArguments() {
    test("function foo(arguments) {}", null,
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments2
  public void testArguments2() {
    test("function foo() { var arguments = 3; }", null,
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments3
  public void testArguments3() {
    test("function arguments() {}", null,
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments4
  public void testArguments4() {
    test("try {} catch (arguments) {}", null,
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments5
  public void testArguments5() {
    testSame("var o = {arguments: 3};");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEvalAssignment
  public void testEvalAssignment() {
    noCajaChecks = true;
    test("function foo() { eval = []; }", null,
         StrictModeCheck.EVAL_ASSIGNMENT);
  }
