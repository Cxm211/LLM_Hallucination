// buggy code
  private void removeUnreferencedFunctionArgs(Scope fnScope) {
    // Notice that removing unreferenced function args breaks
    // Function.prototype.length. In advanced mode, we don't really care
    // about this: we consider "length" the equivalent of reflecting on
    // the function's lexical source.
    //
    // Rather than create a new option for this, we assume that if the user
    // is removing globals, then it's OK to remove unused function args.
    //
    // See http://code.google.com/p/closure-compiler/issues/detail?id=253

    Node function = fnScope.getRootNode();

    Preconditions.checkState(function.isFunction());
    if (NodeUtil.isGetOrSetKey(function.getParent())) {
      // The parameters object literal setters can not be removed.
      return;
    }

    Node argList = getFunctionArgList(function);
    boolean modifyCallers = modifyCallSites
        && callSiteOptimizer.canModifyCallers(function);
    if (!modifyCallers) {
      // Strip unreferenced args off the end of the function declaration.
      Node lastArg;
      while ((lastArg = argList.getLastChild()) != null) {
        Var var = fnScope.getVar(lastArg.getString());
        if (!referenced.contains(var)) {
          argList.removeChild(lastArg);
          compiler.reportCodeChange();
        } else {
          break;
        }
      }
    } else {
      callSiteOptimizer.optimize(fnScope, referenced);
    }
  }

// relevant test
// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionWithName2
  public void testFunctionWithName2() {
    test("foo(function bar(){})",
         "foo(function(){})");

    preserveFunctionExpressionNames = true;
    testSame("foo(function bar(){})");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveGlobal1
  public void testRemoveGlobal1() {
    removeGlobal = false;
    testSame("var x=1");
    test("var y=function(x){var z;}", "var y=function(x){}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveGlobal2
  public void testRemoveGlobal2() {
    removeGlobal = false;
    testSame("var x=1");
    test("function y(x){var z;}", "function y(x){}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveGlobal3
  public void testRemoveGlobal3() {
    removeGlobal = false;
    testSame("var x=1");
    test("function x(){function y(x){var z;}y()}",
         "function x(){function y(x){}y()}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveGlobal4
  public void testRemoveGlobal4() {
    removeGlobal = false;
    testSame("var x=1");
    test("function x(){function y(x){var z;}}",
         "function x(){}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testIssue168a
  public void testIssue168a() {
    test("function _a(){" +
         "  (function(x){ _b(); })(1);" +
         "}" +
         "function _b(){" +
         "  _a();" +
         "}",
         "function _a(){(function(){_b()})(1)}" +
         "function _b(){_a()}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testIssue168b
  public void testIssue168b() {
    removeGlobal = false;
    test("function a(){" +
         "  (function(x){ b(); })(1);" +
         "}" +
         "function b(){" +
         "  a();" +
         "}",
         "function a(){(function(x){b()})(1)}" +
         "function b(){a()}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign1
  public void testUnusedAssign1() {
    test("var x = 3; x = 5;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign2
  public void testUnusedAssign2() {
    test("function f(a) { a = 3; } this.x = f;",
         "function f(){} this.x=f");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign3
  public void testUnusedAssign3() {
    
    
    test("try { throw ''; } catch (e) { e = 3; }",
        "try{throw\"\";}catch(e){e=3}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign4
  public void testUnusedAssign4() {
    test("function f(a, b) { this.foo(b); a = 3; } this.x = f;",
        "function f(a,b){this.foo(b);}this.x=f");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign5
  public void testUnusedAssign5() {
    test("var z = function f() { f = 3; }; z();",
         "var z=function(){};z()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign5b
  public void testUnusedAssign5b() {
    test("var z = function f() { f = alert(); }; z();",
         "var z=function(){alert()};z()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign6
  public void testUnusedAssign6() {
    test("var z; z = 3;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign6b
  public void testUnusedAssign6b() {
    test("var z; z = alert();", "alert()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign7
  public void testUnusedAssign7() {
    
    test("var a = 3; for (var i in {}) { i = a; }",
         
         "var a = 3; var i; for (i in {}) {i = a;}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedAssign8
  public void testUnusedAssign8() {
    
    test("var a = 3; for (var i in {}) { i = a; } alert(a);",
         
         "var a = 3; var i; for (i in {}) {i = a} alert(a);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign1
  public void testUnusedPropAssign1() {
    test("var x = {}; x.foo = 3;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign1b
  public void testUnusedPropAssign1b() {
    test("var x = {}; x.foo = alert();", "alert()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign2
  public void testUnusedPropAssign2() {
    test("var x = {}; x['foo'] = 3;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign2b
  public void testUnusedPropAssign2b() {
    test("var x = {}; x[alert()] = alert();", "alert(),alert()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign3
  public void testUnusedPropAssign3() {
    test("var x = {}; x['foo'] = {}; x['bar'] = 3", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign3b
  public void testUnusedPropAssign3b() {
    test("var x = {}; x[alert()] = alert(); x[alert() + alert()] = alert()",
         "alert(),alert();(alert() + alert()),alert()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign4
  public void testUnusedPropAssign4() {
    test("var x = {foo: 3}; x['foo'] = 5;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign5
  public void testUnusedPropAssign5() {
    test("var x = {foo: bar()}; x['foo'] = 5;",
         "var x={foo:bar()};x[\"foo\"]=5");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign6
  public void testUnusedPropAssign6() {
    test("var x = function() {}; x.prototype.bar = function() {};", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign7
  public void testUnusedPropAssign7() {
    test("var x = {}; x[x.foo] = x.bar;", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign7b
  public void testUnusedPropAssign7b() {
    testSame("var x = {}; x[x.foo] = alert(x.bar);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUnusedPropAssign7c
  public void testUnusedPropAssign7c() {
    test("var x = {}; x[alert(x.foo)] = x.bar;",
         "var x={};x[alert(x.foo)]=x.bar");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign1
  public void testUsedPropAssign1() {
    test("function f(x) { x.bar = 3; } f({});",
         "function f(x){x.bar=3}f({})");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign2
  public void testUsedPropAssign2() {
    test("try { throw z; } catch (e) { e.bar = 3; }",
         "try{throw z;}catch(e){e.bar=3}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign3
  public void testUsedPropAssign3() {
    
    test("var x = {}; x.foo = 3; x = bar();",
         "var x={};x.foo=3;x=bar()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign4
  public void testUsedPropAssign4() {
    test("var y = foo(); var x = {}; x.foo = 3; y[x.foo] = 5;",
         "var y=foo();var x={};x.foo=3;y[x.foo]=5");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign5
  public void testUsedPropAssign5() {
    test("var y = foo(); var x = 3; y[x] = 5;",
         "var y=foo();var x=3;y[x]=5");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign6
  public void testUsedPropAssign6() {
    test("var x = newNodeInDom(doc); x.innerHTML = 'new text';",
         "var x=newNodeInDom(doc);x.innerHTML=\"new text\"");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign7
  public void testUsedPropAssign7() {
    testSame("var x = {}; for (x in alert()) { x.foo = 3; }");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign8
  public void testUsedPropAssign8() {
    testSame("for (var x in alert()) { x.foo = 3; }");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testUsedPropAssign9
  public void testUsedPropAssign9() {
    testSame(
        "var x = {}; x.foo = newNodeInDom(doc); x.foo.innerHTML = 'new test';");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies1
  public void testDependencies1() {
    test("var a = 3; var b = function() { alert(a); };", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies1b
  public void testDependencies1b() {
    test("var a = 3; var b = alert(function() { alert(a); });",
         "var a=3;alert(function(){alert(a)})");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies1c
  public void testDependencies1c() {
    test("var a = 3; var _b = function() { alert(a); };",
         "var a=3;var _b=function(){alert(a)}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies2
  public void testDependencies2() {
    test("var a = 3; var b = 3; b = function() { alert(a); };", "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies2b
  public void testDependencies2b() {
    test("var a = 3; var b = 3; b = alert(function() { alert(a); });",
         "var a=3;alert(function(){alert(a)})");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDependencies2c
  public void testDependencies2c() {
    testSame("var a=3;var _b=3;_b=function(){alert(a)}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testGlobalVarReferencesLocalVar
  public void testGlobalVarReferencesLocalVar() {
    testSame("var a=3;function f(){var b=4;a=b}alert(a + f())");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testLocalVarReferencesGlobalVar1
  public void testLocalVarReferencesGlobalVar1() {
    testSame("var a=3;function f(b, c){b=a; alert(b + c);} f();");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testLocalVarReferencesGlobalVar2
  public void testLocalVarReferencesGlobalVar2() {
    test("var a=3;function f(b, c){b=a; alert(c);} f();",
         "function f(b, c) { alert(c); } f();");
    this.modifyCallSites = true;
    test("var a=3;function f(b, c){b=a; alert(c);} f();",
         "function f(c) { alert(c); } f();");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testNestedAssign1
  public void testNestedAssign1() {
    test("var b = null; var a = (b = 3); alert(a);",
         "var a = 3; alert(a);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testNestedAssign2
  public void testNestedAssign2() {
    test("var a = 1; var b = 2; var c = (b = a); alert(c);",
         "var a = 1; var c = a; alert(c);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testNestedAssign3
  public void testNestedAssign3() {
    test("var b = 0; var z; z = z = b = 1; alert(b);",
         "var b = 0; b = 1; alert(b);");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testCallSiteInteraction
  public void testCallSiteInteraction() {
    this.modifyCallSites = true;

    testSame("var b=function(){return};b()");
    testSame("var b=function(c){return c};b(1)");
    test("var b=function(c){};b.call(null, x)",
         "var b=function(){};b.call(null)");
    test("var b=function(c){};b.apply(null, x)",
         "var b=function(){};b.apply(null, x)");

    test("var b=function(c){return};b(1)",
         "var b=function(){return};b()");
    test("var b=function(c){return};b(1,2)",
         "var b=function(){return};b()");
    test("var b=function(c){return};b(1,2);b(3,4)",
         "var b=function(){return};b();b()");

    
    
    test("var b=function(c,d){return d};b(1,2);b(3,4);b.length",
         "var b=function(c,d){return d};b(0,2);b(0,4);b.length");

    test("var b=function(c){return};b(1,2);b(3,new x())",
         "var b=function(){return};b();b(new x())");

    test("var b=function(c){return};b(1,2);b(new x(),4)",
         "var b=function(){return};b();b(new x())");

    test("var b=function(c,d){return d};b(1,2);b(new x(),4)",
         "var b=function(c,d){return d};b(0,2);b(new x(),4)");
    test("var b=function(c,d,e){return d};b(1,2,3);b(new x(),4,new x())",
         "var b=function(c,d){return d};b(0,2);b(new x(),4,new x())");

    
    test("var b=function(c,d){b(1,2);return d};b(3,4);b(5,6)",
         "var b=function(d){b(2);return d};b(4);b(6)");

    testSame("var b=function(c){return arguments};b(1,2);b(3,4)");

    
    test("var b=function(c,d){return};b(1,2)",
         "var b=function(){return};b()");

    
    testSame("var b=function(c,d){return c+d};b(1,2)");

    
    test("var b=function(e,f,c,d){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b()");
    test("var b=function(c,d,e,f){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b(1,2)");
    test("var b=function(e,c,f,d,g){return c+d};b(1,2)",
         "var b=function(c,d){return c+d};b(2)");

    
    
    test("var b=function(c,d){};var b=function(e,f){};b(1,2)",
         "var b=function(){};var b=function(){};b(1,2)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testCallSiteInteraction_contructors
  public void testCallSiteInteraction_contructors() {
    this.modifyCallSites = true;
    
    
    test("var Ctor1=function(a,b){return a};" +
        "var Ctor2=function(a,b){Ctor1.call(this,a,b)};" +
        "goog$inherits(Ctor2, Ctor1);" +
        "new Ctor2(1,2)",
        "var Ctor1=function(a){return a};" +
        "var Ctor2=function(a){Ctor1.call(this,a)};" +
        "goog$inherits(Ctor2, Ctor1);" +
        "new Ctor2(1)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testFunctionArgRemovalCausingInconsistency
  public void testFunctionArgRemovalCausingInconsistency() {
    this.modifyCallSites = true;
    
    
    
    test("var a=function(x,y){};" +
        "var b=function(z){};" +
        "a(new b, b)",
        "var a=function(){};" +
        "var b=function(){};" +
        "a(new b)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveUnusedVarsPossibleNpeCase
  public void testRemoveUnusedVarsPossibleNpeCase() {
    this.modifyCallSites = true;
    test("var a = [];" +
        "var register = function(callback) {a[0] = callback};" +
        "register(function(transformer) {});" +
        "register(function(transformer) {});",
        "var register=function(){};register();register()");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDoNotOptimizeJSCompiler_renameProperty
  public void testDoNotOptimizeJSCompiler_renameProperty() {
    this.modifyCallSites = true;

    
    test("function JSCompiler_renameProperty(a) {};" +
         "JSCompiler_renameProperty('a');",
         "function JSCompiler_renameProperty() {};" +
         "JSCompiler_renameProperty('a');");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDoNotOptimizeJSCompiler_ObjectPropertyString
  public void testDoNotOptimizeJSCompiler_ObjectPropertyString() {
    this.modifyCallSites = true;
    test("function JSCompiler_ObjectPropertyString(a, b) {};" +
         "JSCompiler_ObjectPropertyString(window,'b');",
         "function JSCompiler_ObjectPropertyString() {};" +
         "JSCompiler_ObjectPropertyString(window,'b');");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testDoNotOptimizeSetters
  public void testDoNotOptimizeSetters() {
    testSame("({set s(a) {}})");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveSingletonClass1
  public void testRemoveSingletonClass1() {
    test("function goog$addSingletonGetter(a){}" +
        "function a(){}" +
        "goog$addSingletonGetter(a);",
        "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass1
  public void testRemoveInheritedClass1() {
    test("function goog$inherits(){}" +
        "function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a); new a",
        "function a(){} new a");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass2
  public void testRemoveInheritedClass2() {
    test("function goog$inherits(){}" +
        "function goog$mixin(){}" +
        "function a(){}" +
        "function b(){}" +
        "function c(){}" +
        "goog$inherits(b,a);" +
        "goog$mixin(c.prototype,b.prototype);",
        "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass3
  public void testRemoveInheritedClass3() {
    testSame("function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a); new b");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass4
  public void testRemoveInheritedClass4() {
    testSame("function goog$inherits(){}" +
        "function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a);" +
        "function c(){}" +
        "goog$inherits(c,b); new c");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass5
  public void testRemoveInheritedClass5() {
    test("function goog$inherits(){}" +
        "function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a);" +
        "function c(){}" +
        "goog$inherits(c,b); new b",
        "function goog$inherits(){}" +
        "function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a); new b");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass6
  public void testRemoveInheritedClass6() {
    test("function goog$mixin(){}" +
        "function a(){}" +
        "function b(){}" +
        "function c(){}" +
        "function d(){}" +
        "goog$mixin(b.prototype,a.prototype);" +
        "goog$mixin(c.prototype,a.prototype); new c;" +
        "goog$mixin(d.prototype,a.prototype)",
        "function goog$mixin(){}" +
        "function a(){}" +
        "function c(){}" +
        "goog$mixin(c.prototype,a.prototype); new c");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass7
  public void testRemoveInheritedClass7() {
    test("function goog$mixin(){}" +
        "function a(){alert(goog$mixin(a, a))}" +
        "function b(){}" +
        "goog$mixin(b.prototype,a.prototype); new a",
        "function goog$mixin(){}" +
        "function a(){alert(goog$mixin(a, a))} new a");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass8
  public void testRemoveInheritedClass8() {
    test("function a(){}" +
        "function b(){}" +
        "function c(){}" +
        "b.inherits(a);c.mixin(b.prototype)",
        "");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass9
  public void testRemoveInheritedClass9() {
    testSame("function a(){}" +
        "function b(){}" +
        "function c(){}" +
        "b.inherits(a);c.mixin(b.prototype);new c");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass10
  public void testRemoveInheritedClass10() {
    test("function goog$inherits(){}" +
        "function a(){}" +
        "function b(){}" +
        "goog$inherits(b,a); new a;" +
        "var c = a; var d = a.g; new b",
        "function goog$inherits(){}" +
        "function a(){} function b(){} goog$inherits(b,a); new a; new b");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass11
  public void testRemoveInheritedClass11() {
    testSame("function goog$inherits(){}" +
        "function goog$mixin(a,b){goog$inherits(a,b)}" +
        "function a(){}" +
        "function b(){}" +
        "goog$mixin(b.prototype,a.prototype);new b");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testRemoveInheritedClass12
  public void testRemoveInheritedClass12() {
    testSame("function goog$inherits(){}" +
        "function a(){}" +
        "var b = {};" +
        "goog$inherits(b.foo, a)");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testReflectedMethods
  public void testReflectedMethods() {
    this.modifyCallSites = true;
    testSame(
        "" +
        "function Foo() {}" +
        "Foo.prototype.handle = function(x, y) { alert(y); };" +
        "var x = goog.reflect.object(Foo, {handle: 1});" +
        "for (var i in x) { x[i].call(x); }" +
        "window['Foo'] = Foo;");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testIssue618_1
  public void testIssue618_1() {
    this.removeGlobal = false;
    testSame(
        "function f() {\n" +
        "  var a = [], b;\n" +
        "  a.push(b = []);\n" +
        "  b[0] = 1;\n" +
        "  return a;\n" +
        "}");
  }

// com.google.javascript.jscomp.RemoveUnusedVarsTest::testIssue618_2
  public void testIssue618_2() {
    this.removeGlobal = false;
    testSame(
        "var b;\n" +
        "a.push(b = []);\n" +
        "b[0] = 1;\n");
  }

// com.google.javascript.jscomp.jsonml.SecureCompilerTest::testCompilerInterface
  public void testCompilerInterface() throws Exception {
    testString(SIMPLE_SOURCE);
    testInvalidString(SYNTAX_ERROR);
  }
