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
// com.google.javascript.jscomp.NameAnalyzerTest::testIf1
  public void testIf1() {
    test("var foo = {};if(e)foo.bar=function(){};", "if(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf2
  public void testIf2() {
    test("var e = false;var foo = {};if(e)foo.bar=function(){};",
         "var e = false;if(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf3
  public void testIf3() {
    test("var e = false;var foo = {};if(e + 1)foo.bar=function(){};",
         "var e = false;if(e + 1);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf4
  public void testIf4() {
    test("var e = false, f;var foo = {};if(f=e)foo.bar=function(){};",
         "var e = false;if(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf5
  public void testIf5() {
    test("var e = false, f;var foo = {};if(f = e + 1)foo.bar=function(){};",
         "var e = false;if(e + 1);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIfElse
  public void testIfElse() {
    test("var foo = {};if(e)foo.bar=function(){};else foo.bar=function(){};",
         "if(e);else;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testWhile
  public void testWhile() {
    test("var foo = {};while(e)foo.bar=function(){};", "while(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFor
  public void testFor() {
    test("var foo = {};for(e in x)foo.bar=function(){};", "for(e in x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDo
  public void testDo() {
    test("var cond = false;do {var a = 1} while (cond)", "var cond = false;do {} while (cond)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct1
  public void testSetterInForStruct1() {
    test("var j = 0; for (var i = 1; i = 0; j++);",
         "var j = 0; for (; 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct2
  public void testSetterInForStruct2() {
    test("var Class = function() {}; " +
         "for (var i = 1; Class.prototype.property_ = 0; i++);",
         "for (var i = 1; 0; i++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct3
  public void testSetterInForStruct3() {
    test("var j = 0; for (var i = 1 + f() + g() + h(); i = 0; j++);",
         "var j = 0; for (f(), g(), h(); 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct4
  public void testSetterInForStruct4() {
    test("var i = 0;var j = 0; for (i = 1 + f() + g() + h(); i = 0; j++);",
         "var j = 0; for (f(), g(), h(); 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct5
  public void testSetterInForStruct5() {
    test("var i = 0, j = 0; for (i = f(), j = g(); 0;);",
         "for (f(), g(); 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct6
  public void testSetterInForStruct6() {
    test("var i = 0, j = 0, k = 0; for (i = f(), j = g(), k = h(); i = 0;);",
         "for (f(), g(), h(); 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct7
  public void testSetterInForStruct7() {
    test("var i = 0, j = 0, k = 0; for (i = 1, j = 2, k = 3; i = 0;);",
         "for (1, 2, 3; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct8
  public void testSetterInForStruct8() {
    test("var i = 0, j = 0, k = 0; for (i = 1, j = i, k = 2; i = 0;);",
         "var i = 0; for(i = 1, i , 2; i = 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexAssigns
  public void testComplexAssigns() {
    
    testSame("var x = 0; x += 3; x *= 5;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssigns
  public void testNestedAssigns() {
    
    testSame("var x = 0; var y = x = 3; window.alert(y);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns1
  public void testComplexNestedAssigns1() {
    
    testSame("var x = 0; var y = 2; y += x = 3; window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns2
  public void testComplexNestedAssigns2() {
    test("var x = 0; var y = 2; y += x = 3; window.alert(y);",
         "var y = 2; y += 3; window.alert(y);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns3
  public void testComplexNestedAssigns3() {
    test("var x = 0; var y = x += 3; window.alert(x);",
         "var x = 0; x += 3; window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns4
  public void testComplexNestedAssigns4() {
    testSame("var x = 0; var y = x += 3; window.alert(y);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope1
  public void testUnintendedUseOfInheritsInLocalScope1() {
    testSame("goog.mixin = function() {}; " +
             "(function() { var x = {}; var y = {}; goog.mixin(x, y); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope2
  public void testUnintendedUseOfInheritsInLocalScope2() {
    testSame("goog.mixin = function() {}; " +
             "var x = {}; var y = {}; (function() { goog.mixin(x, y); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope3
  public void testUnintendedUseOfInheritsInLocalScope3() {
    testSame("goog.mixin = function() {}; " +
             "var x = {}; var y = {}; (function() { goog.mixin(x, y); })(); " +
             "window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope4
  public void testUnintendedUseOfInheritsInLocalScope4() {
    
    
    testSame("var goog$mixin = function() {}; " +
             "(function() { var x = {}; var y = {}; goog$mixin(x, y); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope1
  public void testPrototypePropertySetInLocalScope1() {
    testSame("(function() { var x = function(){}; x.prototype.bar = 3; })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope2
  public void testPrototypePropertySetInLocalScope2() {
    testSame("var x = function(){}; (function() { x.prototype.bar = 3; })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope3
  public void testPrototypePropertySetInLocalScope3() {
    test("var x = function(){ x.prototype.bar = 3; };", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope4
  public void testPrototypePropertySetInLocalScope4() {
    test("var x = {}; x.foo = function(){ x.foo.prototype.bar = 3; };", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope5
  public void testPrototypePropertySetInLocalScope5() {
    test("var x = {}; x.prototype.foo = 3;", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope6
  public void testPrototypePropertySetInLocalScope6() {
    testSame("var x = {}; x.prototype.foo = 3; bar(x.prototype.foo)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope7
  public void testPrototypePropertySetInLocalScope7() {
    testSame("var x = {}; x.foo = 3; bar(x.foo)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference1
  public void testRValueReference1() {
    testSame("var a = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference2
  public void testRValueReference2() {
    testSame("var a = 1; 1+a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference3
  public void testRValueReference3() {
    testSame("var x = {}; x.prototype.foo = 3; var a = x.prototype.foo; 1+a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference4
  public void testRValueReference4() {
    testSame("var x = {}; x.prototype.foo = 3; x.prototype.foo");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference5
  public void testRValueReference5() {
    testSame("var x = {}; x.prototype.foo = 3; 1+x.prototype.foo");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference6
  public void testRValueReference6() {
    testSame("var x = {}; var idx = 2; x[idx]");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnhandledTopNode
  public void testUnhandledTopNode() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
             "function Bar() {}; Bar.prototype.isFoo = function() {};" +
             "var foo = new Foo(); var bar = new Bar();" +
             
             
             "var cond = foo.isBar() && bar.isFoo();" +
             "if (cond) {window.alert('hello');}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPropertyDefinedInGlobalScope
  public void testPropertyDefinedInGlobalScope() {
    testSame("function Foo() {}; var x = new Foo(); x.cssClass = 'bar';" +
             "window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testConditionallyDefinedFunction1
  public void testConditionallyDefinedFunction1() {
    testSame("var g; externfoo.x || (externfoo.x = function() { g; })");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testConditionallyDefinedFunction2
  public void testConditionallyDefinedFunction2() {
    testSame("var g; 1 || (externfoo.x = function() { g; })");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElemOnThis
  public void testGetElemOnThis() {
    testSame("var a = 3; this['foo'] = a;");
    testSame("this['foo'] = 3;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveInstanceOfOnly
  public void testRemoveInstanceOfOnly() {
    test("function Foo() {}; Foo.prototype.isBar = function() {};" +
         "var x; if (x instanceof Foo) { window.alert(x); }",
         ";var x; if (false) { window.alert(x); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLocalScopedInstanceOfOnly
  public void testRemoveLocalScopedInstanceOfOnly() {
    test("function Foo() {}; function Bar(x) { this.z = x instanceof Foo; };" +
        "externfoo.x = new Bar({});",
        ";function Bar(x) { this.z = false }; externfoo.x = new Bar({});");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveInstanceOfWithReferencedMethod
  public void testRemoveInstanceOfWithReferencedMethod() {
    test("function Foo() {}; Foo.prototype.isBar = function() {};" +
        "var x; if (x instanceof Foo) { window.alert(x.isBar()); }",
        ";var x; if (false) { window.alert(x.isBar()); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeReferencedInstanceOf
  public void testDoNotChangeReferencedInstanceOf() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
             "var x = new Foo(); if (x instanceof Foo) { window.alert(x); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeReferencedLocalScopedInstanceOf
  public void testDoNotChangeReferencedLocalScopedInstanceOf() {
    testSame("function Foo() {}; externfoo.x = new Foo();" +
        "function Bar() { if (x instanceof Foo) { window.alert(x); } };" +
        "externfoo.y = new Bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeLocalScopeReferencedInstanceOf
  public void testDoNotChangeLocalScopeReferencedInstanceOf() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
        "function Bar() { this.z = new Foo(); }; externfoo.x = new Bar();" +
        "if (x instanceof Foo) { window.alert(x); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeLocalScopeReferencedLocalScopedInstanceOf
  public void testDoNotChangeLocalScopeReferencedLocalScopedInstanceOf() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
        "function Bar() { this.z = new Foo(); };" +
        "Bar.prototype.func = function(x) {" +
          "if (x instanceof Foo) { window.alert(x); }" +
        "}; new Bar().func();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testWeirdnessOnLeftSideOfPrototype
  public void testWeirdnessOnLeftSideOfPrototype() {
    
    
    testSame("var x = 3; " +
        "(function() { this.bar = 3; }).z = function() {" +
        "  return x;" +
        "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit1
  public void testShortCircuit1() {
    test("var a = b() || 1", "b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit2
  public void testShortCircuit2() {
    test("var a = 1 || c()", "1 || c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit3
  public void testShortCircuit3() {
    test("var a = b() || c()", "b() || c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit4
  public void testShortCircuit4() {
    test("var a = b() || 3 || c()", "b() || 3 || c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit5
  public void testShortCircuit5() {
    test("var a = b() && 1", "b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit6
  public void testShortCircuit6() {
    test("var a = 1 && c()", "1 && c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit7
  public void testShortCircuit7() {
    test("var a = b() && c()", "b() && c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit8
  public void testShortCircuit8() {
    test("var a = b() && 3 && c()", "b() && 3 && c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference1
  public void testRhsReference1() {
    testSame("var a = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference2
  public void testRhsReference2() {
    testSame("var a = 1; a || b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference3
  public void testRhsReference3() {
    testSame("var a = 1; 1 || a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference4
  public void testRhsReference4() {
    test("var a = 1; var b = a || foo()", "var a = 1; a || foo()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference5
  public void testRhsReference5() {
    test("var a = 1, b = 5; a; foo(b)", "var a = 1, b = 5; a; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign1
  public void testRhsAssign1() {
    test("var foo, bar; foo || (bar = 1)",
         "var foo; foo || 1");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign2
  public void testRhsAssign2() {
    test("var foo, bar, baz; foo || (baz = bar = 1)",
         "var foo; foo || 1");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign3
  public void testRhsAssign3() {
    testSame("var foo = null; foo || (foo = 1)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign4
  public void testRhsAssign4() {
    test("var foo = null; foo = (foo || 1)", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign5
  public void testRhsAssign5() {
    test("var a = 3, foo, bar; foo || (bar = a)", "var a = 3, foo; foo || a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign6
  public void testRhsAssign6() {
    test("function Foo(){} var foo = null;" +
         "var f = function () {foo || (foo = new Foo()); return foo}",
         "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign7
  public void testRhsAssign7() {
    testSame("function Foo(){} var foo = null;" +
             "var f = function () {foo || (foo = new Foo())}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign8
  public void testRhsAssign8() {
    testSame("function Foo(){} var foo = null;" +
             "var f = function () {(foo = new Foo()) || g()}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign9
  public void testRhsAssign9() {
    test("function Foo(){} var foo = null;" +
         "var f = function () {1 + (foo = new Foo()); return foo}",
         "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign1
  public void testNestedAssign1() {
    test("var a, b = a = 1, c = 2", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign2
  public void testNestedAssign2() {
    testSame("var a, b = a = 1; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign3
  public void testNestedAssign3() {
    testSame("var a, b = a = 1; a = b = 2; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign4
  public void testNestedAssign4() {
    testSame("var a, b = a = 1; b = a = 2; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign5
  public void testNestedAssign5() {
    test("var a, b = a = 1; b = a = 2", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign15
  public void testNestedAssign15() {
    test("var a, b, c; c = b = a = 2", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign6
  public void testNestedAssign6() {
    testSame("var a, b, c; a = b = c = 1; foo(a, b, c)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign7
  public void testNestedAssign7() {
    testSame("var a = 0; a = i[j] = 1; b(a, i[j])");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign8
  public void testNestedAssign8() {
    testSame("function f(){" +
             "this.lockedToken_ = this.lastToken_ = " +
             "SETPROP_value(this.hiddenInput_, a)}f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain1
  public void testRefChain1() {
    test("var a = 1; var b = a; var c = b; var d = c", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain2
  public void testRefChain2() {
    test("var a = 1; var b = a; var c = b; var d = c || f()",
         "var a = 1; var b = a; var c = b; c || f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain3
  public void testRefChain3() {
    test("var a = 1; var b = a; var c = b; var d = c + f()", "f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain4
  public void testRefChain4() {
    test("var a = 1; var b = a; var c = b; var d = f() || c",
         "f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain5
  public void testRefChain5() {
    test("var a = 1; var b = a; var c = b; var d = f() ? g() : c",
         "f() && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain6
  public void testRefChain6() {
    test("var a = 1; var b = a; var c = b; var d = c ? f() : g()",
         "var a = 1; var b = a; var c = b; c ? f() : g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain7
  public void testRefChain7() {
    test("var a = 1; var b = a; var c = b; var d = (b + f()) ? g() : c",
         "var a = 1; var b = a; (b+f()) && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain8
  public void testRefChain8() {
    test("var a = 1; var b = a; var c = b; var d = f()[b] ? g() : 0",
         "var a = 1; var b = a; f()[b] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain9
  public void testRefChain9() {
    test("var a = 1; var b = a; var c = 5; var d = f()[b+c] ? g() : 0",
         "var a = 1; var b = a; var c = 5; f()[b+c] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain10
  public void testRefChain10() {
    test("var a = 1; var b = a; var c = b; var d = f()[b] ? g() : 0",
         "var a = 1; var b = a; f()[b] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain11
  public void testRefChain11() {
    test("var a = 1; var b = a; var d = f()[b] ? g() : 0",
         "var a = 1; var b = a; f()[b] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain12
  public void testRefChain12() {
    testSame("var a = 1; var b = a; f()[b] ? g() : 0");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain13
  public void testRefChain13() {
    test("function f(){}var a = 1; var b = a; var d = f()[b] ? g() : 0",
         "function f(){}var a = 1; var b = a; f()[b] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain14
  public void testRefChain14() {
    testSame("function f(){}var a = 1; var b = a; f()[b] ? g() : 0");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain15
  public void testRefChain15() {
    test("function f(){}var a = 1, b = a; var c = f(); var d = c[b] ? g() : 0",
         "function f(){}var a = 1, b = a; var c = f(); c[b] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain16
  public void testRefChain16() {
    testSame("function f(){}var a = 1; var b = a; var c = f(); c[b] ? g() : 0");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain17
  public void testRefChain17() {
    test("function f(){}var a = 1; var b = a; var c = f(); var d = c[b]",
         "function f(){} f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain18
  public void testRefChain18() {
    testSame("var a = 1; f()[a] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain19
  public void testRefChain19() {
    test("var a = 1; var b = [a]; var c = b; b[f()] ? g() : 0",
         "var a=1; var b=[a]; b[f()] ? g() : 0");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain20
  public void testRefChain20() {
    test("var a = 1; var b = [a]; var c = b; var d = b[f()] ? g() : 0",
         "var a=1; var b=[a]; b[f()]&&g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain21
  public void testRefChain21() {
    testSame("var a = 1; var b = 2; var c = a + b; f(c)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain22
  public void testRefChain22() {
    test("var a = 2; var b = a = 4; f(a)", "var a = 2; a = 4; f(a)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain23
  public void testRefChain23() {
    test("var a = {}; var b = a[1] || f()", "var a = {}; a[1] || f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentWithComplexLhs
  public void testAssignmentWithComplexLhs() {
    testSame("function f() { return this; }" +
             "var o = {'key': 'val'};" +
             "f().x_ = o['key'];");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentWithComplexLhs2
  public void testAssignmentWithComplexLhs2() {
    testSame("function f() { return this; }" +
             "var o = {'key': 'val'};" +
             "f().foo = function() {" +
             "  o" +
             "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentWithComplexLhs3
  public void testAssignmentWithComplexLhs3() {
    String source =
        "var o = {'key': 'val'};" +
        "function init_() {" +
        "  this.x = o['key']" +
        "}";

    test(source, "");
    testSame(source + ";init_()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentWithComplexLhs4
  public void testAssignmentWithComplexLhs4() {
    testSame("function f() { return this; }" +
             "var o = {'key': 'val'};" +
             "f().foo = function() {" +
             "  this.x = o['key']" +
             "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemovePrototypeDefinitionsOutsideGlobalScope1
  public void testNoRemovePrototypeDefinitionsOutsideGlobalScope1() {
    testSame("function f(arg){}" +
             "" +
             "(function(){" +
             "  var O = {};" +
             "  O.prototype = 'foo';" +
             "  f(O);" +
             "})()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemovePrototypeDefinitionsOutsideGlobalScope2
  public void testNoRemovePrototypeDefinitionsOutsideGlobalScope2() {
    testSame("function f(arg){}" +
             "(function h(){" +
             "  var L = {};" +
             "  L.prototype = 'foo';" +
             "  f(L);" +
             "})()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemovePrototypeDefinitionsOutsideGlobalScope4
  public void testNoRemovePrototypeDefinitionsOutsideGlobalScope4() {
    testSame("function f(arg){}" +
             "function g(){" +
             "  var N = {};" +
             "  N.prototype = 'foo';" +
             "  f(N);" +
             "}" +
             "g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemovePrototypeDefinitionsOutsideGlobalScope5
  public void testNoRemovePrototypeDefinitionsOutsideGlobalScope5() {
    
    testSame("function g(){ var R = {}; R.prototype = 'foo' } g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemovePrototypeDefinitionsInGlobalScope1
  public void testRemovePrototypeDefinitionsInGlobalScope1() {
    testSame("function f(arg){}" +
             "var M = {};" +
             "M.prototype = 'foo';" +
             "f(M);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemovePrototypeDefinitionsInGlobalScope2
  public void testRemovePrototypeDefinitionsInGlobalScope2() {
    test("var Q = {}; Q.prototype = 'foo'", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLabeledStatment
  public void testRemoveLabeledStatment() {
    test("LBL: var x = 1;", "LBL: {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLabeledStatment2
  public void testRemoveLabeledStatment2() {
    test("var x; LBL: x = f() + g()", "LBL: { f() ; g()}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLabeledStatment3
  public void testRemoveLabeledStatment3() {
    test("var x; LBL: x = 1;", "LBL: {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLabeledStatment4
  public void testRemoveLabeledStatment4() {
    test("var a; LBL: a = f()", "LBL: f()");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testSimpleVarAssignment
  public void testSimpleVarAssignment() {
    test("var a = function() { return 1; }",
         "var a = function $() { return 1; }");
    assertMapping("$", "a");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToProperty
  public void testAssignmentToProperty() {
    test("var a = {}; a.b = function() { return 1; }",
         "var a = {}; a.b = function $() { return 1; }");
    assertMapping("$", "a.b");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToPrototype
  public void testAssignmentToPrototype() {
    test("function a() {} a.prototype.b = function() { return 1; };",
         "function a() {} " +
         "a.prototype.b = function $() { return 1; };");
    assertMapping("$", "a.prototype.b");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToPrototype2
  public void testAssignmentToPrototype2() {
    test("var a = {}; " +
         "a.b = function() {}; " +
         "a.b.prototype.c = function() { return 1; };",
         "var a = {}; " +
         "a.b = function $() {}; " +
         "a.b.prototype.c = function $a() { return 1; };");
    assertMapping("$", "a.b", "$a", "a.b.prototype.c");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToPrototype3
  public void testAssignmentToPrototype3() {
    test("function a() {} a.prototype['XXX'] = function() { return 1; };",
         "function a() {} " +
         "a.prototype['XXX'] = function $() { return 1; };");
    assertMapping("$", "a.prototype[\"XXX\"]");
    test("function a() {} a.prototype['\\n'] = function() { return 1; };",
         "function a() {} " +
         "a.prototype['\\n'] = function $() { return 1; };");
    assertMapping("$", "a.prototype[\"\\n\"]");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToPrototype4
  public void testAssignmentToPrototype4() {
    test("var Y = 1; function a() {} " +
         "a.prototype[Y] = function() { return 1; };",
         "var Y = 1; function a() {} " +
         "a.prototype[Y] = function $() { return 1; };");
    assertMapping("$", "a.prototype[Y]");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToPrototype5
  public void testAssignmentToPrototype5() {
    test("function a() {} a['prototype'].b = function() { return 1; };",
         "function a() {} " +
         "a['prototype'].b = function $() { return 1; };");
    assertMapping("$", "a[\"prototype\"].b");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testPrototypeInitializer
  public void testPrototypeInitializer() {
    test("function a(){} a.prototype = {b: function() { return 1; }};",
         "function a(){} " +
         "a.prototype = {b: function $() { return 1; }};");
    assertMapping("$", "a.prototype.b");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToPropertyOfCallReturnValue
  public void testAssignmentToPropertyOfCallReturnValue() {
    test("document.getElementById('x').onClick = function() {};",
         "document.getElementById('x').onClick = " +
         "function $() {};");
    assertMapping("$", "document.getElementById(\"x\").onClick");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToPropertyOfArrayElement
  public void testAssignmentToPropertyOfArrayElement() {
    test("var a = {}; a.b = [{}]; a.b[0].c = function() {};",
         "var a = {}; a.b = [{}]; a.b[0].c = function $() {};");
    assertMapping("$", "a.b[0].c");
    test("var a = {b: {'c': {}}}; a.b['c'].d = function() {};",
         "var a = {b: {'c': {}}}; a.b['c'].d = function $() {};");
    assertMapping("$", "a.b[\"c\"].d");
    test("var a = {b: {'c': {}}}; a.b[x()].d = function() {};",
         "var a = {b: {'c': {}}}; a.b[x()].d = function $() {};");
    assertMapping("$", "a.b[x()].d");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testAssignmentToGetElem
  public void testAssignmentToGetElem() {
    test("function() { win['x' + this.id] = function(a){}; }",
         "function() { win['x' + this.id] = function $(a){}; }");

    
    assertMapping("$", "win[\"x\"+this.id]");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testGetElemWithDashes
  public void testGetElemWithDashes() {
    test("var foo = {}; foo['-'] = function() {};",
         "var foo = {}; foo['-'] = function $() {};");
    assertMapping("$", "foo[\"-\"]");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsMappedTest::testDuplicateNames
  public void testDuplicateNames() {
    test("var a = function() { return 1; };a = function() { return 2; }",
         "var a = function $() { return 1; };a = function $() { return 2; }");
    assertMapping("$", "a");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testSimpleVarAssignment
  public void testSimpleVarAssignment() {
    test("var a = function() { return 1; }",
         "var a = function $a$() { return 1; }");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToProperty
  public void testAssignmentToProperty() {
    test("var a = {}; a.b = function() { return 1; }",
         "var a = {}; a.b = function $a$b$() { return 1; }");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPrototype
  public void testAssignmentToPrototype() {
    test("function a() {} a.prototype.b = function() { return 1; };",
         "function a() {} " +
         "a.prototype.b = function $a$$b$() { return 1; };");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPrototype2
  public void testAssignmentToPrototype2() {
    test("var a = {}; " +
         "a.b = function() {}; " +
         "a.b.prototype.c = function() { return 1; };",
         "var a = {}; " +
         "a.b = function $a$b$() {}; " +
         "a.b.prototype.c = function $a$b$$c$() { return 1; };");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPrototype3
  public void testAssignmentToPrototype3() {
    test("function a() {} a.prototype['b'] = function() { return 1; };",
         "function a() {} " +
         "a.prototype['b'] = function $a$$b$() { return 1; };");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPrototype4
  public void testAssignmentToPrototype4() {
    test("function a() {} a['prototype']['b'] = function() { return 1; };",
         "function a() {} " +
         "a['prototype']['b'] = function $a$$b$() { return 1; };");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testPrototypeInitializer
  public void testPrototypeInitializer() {
    test("function a(){} a.prototype = {b: function() { return 1; }};",
         "function a(){} " +
         "a.prototype = {b: function $a$$b$() { return 1; }};");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testMultiplePrototypeInitializer
  public void testMultiplePrototypeInitializer() {
    test("function a(){} a.prototype = {b: function() { return 1; }, " +
         "c: function() { return 2; }};",
         "function a(){} " +
         "a.prototype = {b: function $a$$b$() { return 1; }," +
         "c: function $a$$c$() { return 2; }};");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testRecursiveObjectLiteral
  public void testRecursiveObjectLiteral() {
    test("function a(){} a.prototype = {b: {c: function() { return 1; }}}",
         "function a(){}a.prototype={b:{c:function $a$$b$c$(){return 1}}}");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPropertyOfCallReturnValue
  public void testAssignmentToPropertyOfCallReturnValue() {
    test("document.getElementById('x').onClick = function() {};",
         "document.getElementById('x').onClick = " +
         "function $document$getElementById$onClick$() {};");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToPropertyOfArrayElement
  public void testAssignmentToPropertyOfArrayElement() {
    test("var a = {}; a.b = [{}]; a.b[0].c = function() {};",
         "var a = {}; a.b = [{}]; a.b[0].c = function $a$b$0$c$() {};");
    test("var a = {b: {'c': {}}}; a.b['c'].d = function() {};",
         "var a = {b: {'c': {}}}; a.b['c'].d = function $a$b$c$d$() {};");
    test("var a = {b: {'c': {}}}; a.b[x()].d = function() {};",
         "var a = {b: {'c': {}}}; a.b[x()].d = function $a$b$x$d$() {};");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testAssignmentToGetElem
  public void testAssignmentToGetElem() {
    test("function() { win['x' + this.id] = function(a){}; }",
         "function() { win['x' + this.id] = function $win$x$this$id$(a){}; }");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testGetElemWithDashes
  public void testGetElemWithDashes() {
    test("var foo = {}; foo['-'] = function() {};",
         "var foo = {}; foo['-'] = function $foo$__0$() {};");
  }

// com.google.javascript.jscomp.NameAnonymousFunctionsTest::testWhatCausedIeToFail
  public void testWhatCausedIeToFail() {
    
    
    
    test("var main;" +
        "(function() {" +
        "  main = function() {" +
        "    return 5;" +
        "  };" +
        "})();" +
        "" +
        "main();",
        "var main;(function(){main=function $main$(){return 5}})();main()");
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testBasic
  public void testBasic() {
    testVarMotionWithCode("var X = 3;", Token.VAR, Token.SCRIPT);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testNamedFunction
  public void testNamedFunction() {
    testVarMotionWithCode("var X = 3; function f() {}",
        Token.VAR, Token.SCRIPT);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testNamedFunction2
  public void testNamedFunction2() {
    testVarMotionWithCode("var X = 3; function f() {} var Y;",
        Token.VAR, Token.NAME, Token.VAR, Token.SCRIPT);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testAnonymousFunction
  public void testAnonymousFunction() {
    testVarMotionWithCode("var X = 3, Y = function() {}; 3;",
        Token.NAME, Token.VAR, Token.NUMBER, Token.EXPR_RESULT, Token.SCRIPT);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testAnonymousFunction2
  public void testAnonymousFunction2() {
    testVarMotionWithCode("var X = 3; var Y = function() {}; 3;",
        Token.VAR, Token.NAME, Token.VAR, Token.NUMBER,
        Token.EXPR_RESULT, Token.SCRIPT);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltAtVarRef
  public void testHaltAtVarRef() {
    testVarMotionWithCode("var X, Y = 3; var Z = X;",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NAME);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltAtVarRef2
  public void testHaltAtVarRef2() {
    testVarMotionWithCode("var X, Y = 3; (function() {})(3, X);",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NUMBER, Token.NAME);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltAtVarRef3
  public void testHaltAtVarRef3() {
    testVarMotionWithCode("var X, Y = 3; X;",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NAME);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltAtSideEffects
  public void testHaltAtSideEffects() {
    testVarMotionWithCode("var X, Y = 3; var Z = B(3);",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NAME, Token.NUMBER);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltAtSideEffects2
  public void testHaltAtSideEffects2() {
    testVarMotionWithCode("var A = 1, X = A, Y = 3; delete A;",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NAME);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltAtSideEffects3
  public void testHaltAtSideEffects3() {
    testVarMotionWithCode("var A = 1, X = A, Y = 3; A++;",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NAME);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltAtSideEffects4
  public void testHaltAtSideEffects4() {
    testVarMotionWithCode("var A = 1, X = A, Y = 3; A--;",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NAME);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltAtSideEffects5
  public void testHaltAtSideEffects5() {
    testVarMotionWithCode("var A = 1, X = A, Y = 3; A = 'a';",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NAME, Token.STRING);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testNoHaltReadWhenValueIsImmutable
  public void testNoHaltReadWhenValueIsImmutable() {
    testVarMotionWithCode("var X = 1, Y = 3; alert();",
        Token.NUMBER, Token.NAME, Token.VAR, Token.NAME);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testHaltReadWhenValueHasSideEffects
  public void testHaltReadWhenValueHasSideEffects() {
    testVarMotionWithCode("var X = f(), Y = 3; alert();",
        Token.NUMBER, Token.NAME, Token.VAR);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testCatchBlock
  public void testCatchBlock() {
    testVarMotionWithCode("var X = 1; try { 4; } catch (X) {}",
        Token.VAR, Token.NUMBER, Token.EXPR_RESULT, Token.BLOCK);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testIfBranch
  public void testIfBranch() {
    testVarMotionWithCode("var X = foo(); if (X) {}",
        Token.VAR, Token.NAME);
  }

// com.google.javascript.jscomp.NodeTraversalTest::testPruningCallbackShouldTraverse1
  public void testPruningCallbackShouldTraverse1() {
    PruningCallback include =
      new PruningCallback(ImmutableSet.of(Token.SCRIPT, Token.VAR), true);

    assertTrue(include.shouldTraverse(null, new Node(Token.SCRIPT), null));
    assertTrue(include.shouldTraverse(null, new Node(Token.VAR), null));
    assertFalse(include.shouldTraverse(null, new Node(Token.NAME), null));
    assertFalse(include.shouldTraverse(null, new Node(Token.ADD), null));
  }

// com.google.javascript.jscomp.NodeTraversalTest::testPruningCallbackShouldTraverse2
  public void testPruningCallbackShouldTraverse2() {
    PruningCallback include =
      new PruningCallback(ImmutableSet.of(Token.SCRIPT, Token.VAR), false);

    assertFalse(include.shouldTraverse(null, new Node(Token.SCRIPT), null));
    assertFalse(include.shouldTraverse(null, new Node(Token.VAR), null));
    assertTrue(include.shouldTraverse(null, new Node(Token.NAME), null));
    assertTrue(include.shouldTraverse(null, new Node(Token.ADD), null));
  }

// com.google.javascript.jscomp.NodeTraversalTest::testReport
  public void testReport() {
    final List<JSError> errors = new ArrayList<JSError>();

    Compiler compiler = new Compiler(new BasicErrorManager() {

      @Override public void report(CheckLevel level, JSError error) {
        errors.add(error);
      }

      @Override public void println(CheckLevel level, JSError error) {
      }

      @Override protected void printSummary() {
      }
    });

    NodeTraversal t = new NodeTraversal(compiler, null);
    DiagnosticType dt = DiagnosticType.warning("FOO", "{0}, {1} - {2}");

    t.report(null, dt, "Foo", "Bar", "Hello");
    assertEquals(1, errors.size());
    assertEquals("Foo, Bar - Hello", errors.get(0).description);
  }

// com.google.javascript.jscomp.NodeTraversalTest::testUnexpectedException
  public void testUnexpectedException() {
    final String TEST_EXCEPTION = "test me";

    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        throw new RuntimeException(TEST_EXCEPTION);
      }
    };

    Compiler compiler = new Compiler();
    NodeTraversal t = new NodeTraversal(compiler, cb);
    String code = "function foo() {}";
    Node tree = parse(compiler, code);

    try {
      t.traverse(tree);
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().startsWith(
          "INTERNAL COMPILER ERROR.\n" +
          "Please report this problem.\n" +
          "test me"));
    }
  }

// com.google.javascript.jscomp.NodeTraversalTest::testGetScopeRoot
  public void testGetScopeRoot() {
    Compiler compiler = new Compiler();
    NodeTraversal t = new NodeTraversal(compiler,
        new NodeTraversal.ScopedCallback() {

          @Override
          public void enterScope(NodeTraversal t) {
            Node root1 = t.getScopeRoot();
            Node root2 = t.getScope().getRootNode();
            assertEquals(root1, root2);
          }

          @Override
          public void exitScope(NodeTraversal t) {
          }

          @Override
          public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
            return true;
          }

          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
          }
        }
    );

    String code = "" +
            "var a; " +
            "function foo() {" +
            "  var b" +
            "}";
    Node tree = parse(compiler, code);
    t.traverse(tree);
  }

// com.google.javascript.jscomp.NodeTraversalTest::testGetCurrentNode
  public void testGetCurrentNode() {
    Compiler compiler = new Compiler();
    ScopeCreator creator = new SyntacticScopeCreator(compiler);
    ExpectNodeOnEnterScope callback = new ExpectNodeOnEnterScope();
    NodeTraversal t = new NodeTraversal(compiler, callback, creator);

    String code = "" +
            "var a; " +
            "function foo() {" +
            "  var b;" +
            "}";

    Node tree = parse(compiler, code);
    Scope topScope = creator.createScope(tree, null);

    
    
    callback.expect(tree.getFirstChild(), tree);
    t.traverseWithScope(tree.getFirstChild(), topScope);
    callback.assertEntered();

    
    callback.expect(tree.getFirstChild(), tree.getFirstChild());
    t.traverse(tree.getFirstChild());
    callback.assertEntered();

    
    Node fn = tree.getFirstChild().getNext();
    Scope fnScope = creator.createScope(fn, topScope);
    callback.expect(fn, fn);
    t.traverseAtScope(fnScope);
    callback.assertEntered();
  }

// com.google.javascript.jscomp.NodeTypeNormalizerTest::testJsDocNormalization
  public void testJsDocNormalization() throws Exception {
    Node root = parseExpectedJs(
        "var x = { a: function() {}," +
        "         c:  ('d')};");
    Node objlit = root.getFirstChild().getFirstChild().getFirstChild()
        .getFirstChild();
    assertEquals(Token.OBJECTLIT, objlit.getType());

    Node firstKey = objlit.getFirstChild();
    Node firstVal = firstKey.getNext();

    Node secondKey = firstVal.getNext();
    Node secondVal = secondKey.getNext();
    assertNotNull(firstKey.getJSDocInfo());
    assertNotNull(firstVal.getJSDocInfo());
    assertNull(secondKey.getJSDocInfo());
    assertNotNull(secondVal.getJSDocInfo());
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsLiteralOrConstValue
  public void testIsLiteralOrConstValue() {
    assertLiteralAndImmutable(getNode("10"));
    assertLiteralAndImmutable(getNode("-10"));
    assertLiteralButNotImmutable(getNode("[10, 20]"));
    assertLiteralButNotImmutable(getNode("{'a': 20}"));
    assertLiteralButNotImmutable(getNode("[10, , 1.0, [undefined], 'a']"));
    assertLiteralButNotImmutable(getNode("/abc/"));
    assertLiteralAndImmutable(getNode("\"string\""));
    assertLiteralAndImmutable(getNode("'aaa'"));
    assertLiteralAndImmutable(getNode("null"));
    assertLiteralAndImmutable(getNode("undefined"));
    assertLiteralAndImmutable(getNode("void 0"));
    assertNotLiteral(getNode("abc"));
    assertNotLiteral(getNode("[10, foo(), 20]"));
    assertNotLiteral(getNode("foo()"));
    assertNotLiteral(getNode("c + d"));
    assertNotLiteral(getNode("{'a': foo()}"));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetBooleanValue
  public void testGetBooleanValue() {
    assertTrue(NodeUtil.getBooleanValue(getNode("true")));
    assertTrue(NodeUtil.getBooleanValue(getNode("10")));
    assertTrue(NodeUtil.getBooleanValue(getNode("'0'")));
    assertTrue(NodeUtil.getBooleanValue(getNode("/a/")));
    assertTrue(NodeUtil.getBooleanValue(getNode("{}")));
    assertTrue(NodeUtil.getBooleanValue(getNode("[]")));
    assertFalse(NodeUtil.getBooleanValue(getNode("false")));
    assertFalse(NodeUtil.getBooleanValue(getNode("null")));
    assertFalse(NodeUtil.getBooleanValue(getNode("0")));
    assertFalse(NodeUtil.getBooleanValue(getNode("''")));
    assertFalse(NodeUtil.getBooleanValue(getNode("undefined")));
    assertFalse(NodeUtil.getBooleanValue(getNode("void 0")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetStringValue
  public void testGetStringValue() {
    assertEquals("true", NodeUtil.getStringValue(getNode("true")));
    assertEquals("10", NodeUtil.getStringValue(getNode("10")));
    assertEquals("1", NodeUtil.getStringValue(getNode("1.0")));
    assertEquals("0", NodeUtil.getStringValue(getNode("'0'")));
    assertEquals(null, NodeUtil.getStringValue(getNode("/a/")));
    assertEquals(null, NodeUtil.getStringValue(getNode("{}")));
    assertEquals(null, NodeUtil.getStringValue(getNode("[]")));
    assertEquals("false", NodeUtil.getStringValue(getNode("false")));
    assertEquals("null", NodeUtil.getStringValue(getNode("null")));
    assertEquals("0", NodeUtil.getStringValue(getNode("0")));
    assertEquals("", NodeUtil.getStringValue(getNode("''")));
    assertEquals("undefined", NodeUtil.getStringValue(getNode("undefined")));
    assertEquals("undefined", NodeUtil.getStringValue(getNode("void 0")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetFunctionName1
  public void testGetFunctionName1() throws Exception {
    Compiler compiler = new Compiler();
    Node parent = compiler.parseTestCode("function name(){}");

    testGetFunctionName(parent.getFirstChild(), parent, "name");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetFunctionName2
  public void testGetFunctionName2() throws Exception {
    Compiler compiler = new Compiler();
    Node parent = compiler.parseTestCode("var name = function(){}")
        .getFirstChild().getFirstChild();

    testGetFunctionName(parent.getFirstChild(), parent, "name");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetFunctionName3
  public void testGetFunctionName3() throws Exception {
    Compiler compiler = new Compiler();
    Node parent = compiler.parseTestCode("qualified.name = function(){}")
        .getFirstChild().getFirstChild();

    testGetFunctionName(parent.getLastChild(), parent, "qualified.name");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetFunctionName4
  public void testGetFunctionName4() throws Exception {
    Compiler compiler = new Compiler();
    Node parent = compiler.parseTestCode("var name2 = function name1(){}")
        .getFirstChild().getFirstChild();

    testGetFunctionName(parent.getFirstChild(), parent, "name2");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetFunctionName5
  public void testGetFunctionName5() throws Exception {
    Compiler compiler = new Compiler();
    Node n = compiler.parseTestCode("qualified.name2 = function name1(){}");
    Node parent = n.getFirstChild().getFirstChild();

    testGetFunctionName(parent.getLastChild(), parent, "qualified.name2");
  }

// com.google.javascript.jscomp.NodeUtilTest::testContainsFunctionDeclaration
  public void testContainsFunctionDeclaration() {
    assertTrue(NodeUtil.containsFunctionDeclaration(
                   getNode("function foo(){}")));
    assertTrue(NodeUtil.containsFunctionDeclaration(
                   getNode("(b?function(){}:null)")));

    assertFalse(NodeUtil.containsFunctionDeclaration(
                   getNode("(b?foo():null)")));
    assertFalse(NodeUtil.containsFunctionDeclaration(
                    getNode("foo()")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testMayHaveSideEffects
  public void testMayHaveSideEffects() {
    assertSideEffect(true, "i++");
    assertSideEffect(true, "[b, [a, i++]]");
    assertSideEffect(true, "i=3");
    assertSideEffect(true, "[0, i=3]");
    assertSideEffect(true, "b()");
    assertSideEffect(true, "[1, b()]");
    assertSideEffect(true, "b.b=4");
    assertSideEffect(true, "b.b--");
    assertSideEffect(true, "i--");
    assertSideEffect(true, "a[0][i=4]");
    assertSideEffect(true, "a += 3");
    assertSideEffect(true, "a, b, z += 4");
    assertSideEffect(true, "a ? c : d++");
    assertSideEffect(true, "a + c++");
    assertSideEffect(true, "a + c - d()");
    assertSideEffect(true, "a + c - d()");

    assertSideEffect(true, "function foo() {}");
    assertSideEffect(true, "while(true);");
    assertSideEffect(true, "if(true){a()}");

    assertSideEffect(false, "if(true){a}");
    assertSideEffect(false, "(function() { })");
    assertSideEffect(false, "(function() { i++ })");
    assertSideEffect(false, "[function a(){}]");

    assertSideEffect(false, "a");
    assertSideEffect(false, "[b, c [d, [e]]]");
    assertSideEffect(false, "({a: x, b: y, c: z})");
    assertSideEffect(false, "/abc/gi");
    assertSideEffect(false, "'a'");
    assertSideEffect(false, "0");
    assertSideEffect(false, "a + c");
    assertSideEffect(false, "'c' + a[0]");
    assertSideEffect(false, "a[0][1]");
    assertSideEffect(false, "'a' + c");
    assertSideEffect(false, "'a' + a.name");
    assertSideEffect(false, "1, 2, 3");
    assertSideEffect(false, "a, b, 3");
    assertSideEffect(false, "(function(a, b) {  })");
    assertSideEffect(false, "a ? c : d");
    assertSideEffect(false, "'1' + navigator.userAgent");

    assertSideEffect(false, "new RegExp('foobar', 'i')");
    assertSideEffect(true, "new RegExp(SomethingWacky(), 'i')");
    assertSideEffect(false, "new Array()");
    assertSideEffect(false, "new Array");
    assertSideEffect(false, "new Array(4)");
    assertSideEffect(false, "new Array('a', 'b', 'c')");
    assertSideEffect(true, "new SomeClassINeverHeardOf()");
    assertSideEffect(true, "new SomeClassINeverHeardOf()");

    assertSideEffect(false, "({}).foo = 4");
    assertSideEffect(false, "([]).foo = 4");
    assertSideEffect(false, "(function() {}).foo = 4");

    assertSideEffect(true, "this.foo = 4");
    assertSideEffect(true, "a.foo = 4");
    assertSideEffect(true, "(function() { return n; })().foo = 4");
    assertSideEffect(true, "([]).foo = bar()");
  }

// com.google.javascript.jscomp.NodeUtilTest::testMayEffectMutableState
  public void testMayEffectMutableState() {
    assertMutableState(true, "i++");
    assertMutableState(true, "[b, [a, i++]]");
    assertMutableState(true, "i=3");
    assertMutableState(true, "[0, i=3]");
    assertMutableState(true, "b()");
    assertMutableState(true, "[1, b()]");
    assertMutableState(true, "b.b=4");
    assertMutableState(true, "b.b--");
    assertMutableState(true, "i--");
    assertMutableState(true, "a[0][i=4]");
    assertMutableState(true, "a += 3");
    assertMutableState(true, "a, b, z += 4");
    assertMutableState(true, "a ? c : d++");
    assertMutableState(true, "a + c++");
    assertMutableState(true, "a + c - d()");
    assertMutableState(true, "a + c - d()");

    assertMutableState(true, "function foo() {}");
    assertMutableState(true, "while(true);");
    assertMutableState(true, "if(true){a()}");

    assertMutableState(false, "if(true){a}");
    assertMutableState(false, "(function() { })");
    assertMutableState(false, "(function() { i++ })");
    assertMutableState(true, "[function a(){}]");

    assertMutableState(false, "a");
    assertMutableState(true, "[b, c [d, [e]]]");
    assertMutableState(true, "({a: x, b: y, c: z})");
    
    
    assertMutableState(true, "/abc/gi");
    assertMutableState(false, "'a'");
    assertMutableState(false, "0");
    assertMutableState(false, "a + c");
    assertMutableState(false, "'c' + a[0]");
    assertMutableState(false, "a[0][1]");
    assertMutableState(false, "'a' + c");
    assertMutableState(false, "'a' + a.name");
    assertMutableState(false, "1, 2, 3");
    assertMutableState(false, "a, b, 3");
    assertMutableState(false, "(function(a, b) {  })");
    assertMutableState(false, "a ? c : d");
    assertMutableState(false, "'1' + navigator.userAgent");

    assertMutableState(true, "new RegExp('foobar', 'i')");
    assertMutableState(true, "new RegExp(SomethingWacky(), 'i')");
    assertMutableState(true, "new Array()");
    assertMutableState(true, "new Array");
    assertMutableState(true, "new Array(4)");
    assertMutableState(true, "new Array('a', 'b', 'c')");
    assertMutableState(true, "new SomeClassINeverHeardOf()");
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsFunctionAnonymous
  public void testIsFunctionAnonymous() {
    assertContainsAnonFunc(true, "(function(){})");
    assertContainsAnonFunc(true, "[function a(){}]");
    assertContainsAnonFunc(false, "{x: function a(){}}");
    assertContainsAnonFunc(true, "(function a(){})()");
    assertContainsAnonFunc(true, "x = function a(){};");
    assertContainsAnonFunc(true, "var x = function a(){};");
    assertContainsAnonFunc(true, "if (function a(){});");
    assertContainsAnonFunc(true, "while (function a(){});");
    assertContainsAnonFunc(true, "do; while (function a(){});");
    assertContainsAnonFunc(true, "for (function a(){};;);");
    assertContainsAnonFunc(true, "for (;function a(){};);");
    assertContainsAnonFunc(true, "for (;;function a(){});");
    assertContainsAnonFunc(true, "for (p in function a(){});");
    assertContainsAnonFunc(true, "with (function a(){}) {}");
    assertContainsAnonFunc(false, "function a(){}");
    assertContainsAnonFunc(false, "if (x) function a(){};");
    assertContainsAnonFunc(false, "if (x) { function a(){} }");
    assertContainsAnonFunc(false, "if (x); else function a(){};");
    assertContainsAnonFunc(false, "while (x) function a(){};");
    assertContainsAnonFunc(false, "do function a(){} while (0);");
    assertContainsAnonFunc(false, "for (;;) function a(){}");
    assertContainsAnonFunc(false, "for (p in o) function a(){};");
    assertContainsAnonFunc(false, "with (x) function a(){}");
  }

// com.google.javascript.jscomp.NodeUtilTest::testNewFunctionNode
  public void testNewFunctionNode() {
    Node expected = parse("function foo(p1, p2, p3) { throw 2; }");
    Node body = new Node(Token.BLOCK, new Node(Token.THROW, Node.newNumber(2)));
    List<Node> params = Lists.newArrayList(Node.newString(Token.NAME, "p1"),
                                           Node.newString(Token.NAME, "p2"),
                                           Node.newString(Token.NAME, "p3"));
    FunctionNode function = NodeUtil.newFunctionNode(
        "foo", params, body, -1, -1);
    ScriptOrFnNode actual = new ScriptOrFnNode(Token.SCRIPT);
    actual.addChildToFront(function);
    String difference = expected.checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testContainsType
  public void testContainsType() {
    assertTrue(NodeUtil.containsType(
        parse("this"), Token.THIS));
    assertTrue(NodeUtil.containsType(
        parse("function foo(){}(this)"), Token.THIS));
    assertTrue(NodeUtil.containsType(
        parse("b?this:null"), Token.THIS));

    assertFalse(NodeUtil.containsType(
        parse("a"), Token.THIS));
    assertFalse(NodeUtil.containsType(
        parse("function foo(){}"), Token.THIS));
    assertFalse(NodeUtil.containsType(
        parse("(b?foo():null)"), Token.THIS));
  }

// com.google.javascript.jscomp.NodeUtilTest::testReferencesThis
  public void testReferencesThis() {
    assertTrue(NodeUtil.referencesThis(
        parse("this")));
    assertTrue(NodeUtil.referencesThis(
        parse("function foo(){}(this)")));
    assertTrue(NodeUtil.referencesThis(
        parse("b?this:null")));

    assertFalse(NodeUtil.referencesThis(
        parse("a")));
    assertFalse(NodeUtil.referencesThis(
        parse("function foo(){}")));
    assertFalse(NodeUtil.referencesThis(
        parse("(b?foo():null)")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetNodeTypeReferenceCount
  public void testGetNodeTypeReferenceCount() {
    assertEquals(0, NodeUtil.getNodeTypeReferenceCount(
        parse("function foo(){}"), Token.THIS));
    assertEquals(1, NodeUtil.getNodeTypeReferenceCount(
        parse("this"), Token.THIS));
    assertEquals(2, NodeUtil.getNodeTypeReferenceCount(
        parse("this;function foo(){}(this)"), Token.THIS));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsNameReferenceCount
  public void testIsNameReferenceCount() {
    assertTrue(NodeUtil.isNameReferenced(
        parse("function foo(){}"), "foo"));
    assertTrue(NodeUtil.isNameReferenced(
        parse("var foo = function(){}"), "foo"));
    assertFalse(NodeUtil.isNameReferenced(
        parse("function foo(){}"), "undefined"));
    assertTrue(NodeUtil.isNameReferenced(
        parse("undefined"), "undefined"));
    assertTrue(NodeUtil.isNameReferenced(
        parse("undefined;function foo(){}(undefined)"), "undefined"));

    assertTrue(NodeUtil.isNameReferenced(
        parse("goo.foo"), "goo"));
    assertFalse(NodeUtil.isNameReferenced(
        parse("goo.foo"), "foo"));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetNameReferenceCount
  public void testGetNameReferenceCount() {
    assertEquals(0, NodeUtil.getNameReferenceCount(
        parse("function foo(){}"), "undefined"));
    assertEquals(1, NodeUtil.getNameReferenceCount(
        parse("undefined"), "undefined"));
    assertEquals(2, NodeUtil.getNameReferenceCount(
        parse("undefined;function foo(){}(undefined)"), "undefined"));

    assertEquals(1, NodeUtil.getNameReferenceCount(
        parse("goo.foo"), "goo"));
    assertEquals(0, NodeUtil.getNameReferenceCount(
        parse("goo.foo"), "foo"));
    assertEquals(1, NodeUtil.getNameReferenceCount(
        parse("function foo(){}"), "foo"));
    assertEquals(1, NodeUtil.getNameReferenceCount(
        parse("var foo = function(){}"), "foo"));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetVarsDeclaredInBranch
  public void testGetVarsDeclaredInBranch() {
    Compiler compiler = new Compiler();

    assertNodeNames(Sets.newHashSet("foo"),
        NodeUtil.getVarsDeclaredInBranch(
            parse("var foo;")));
    assertNodeNames(Sets.newHashSet("foo","goo"),
        NodeUtil.getVarsDeclaredInBranch(
            parse("var foo,goo;")));
    assertNodeNames(Sets.<String>newHashSet(),
        NodeUtil.getVarsDeclaredInBranch(
            parse("foo();")));
    assertNodeNames(Sets.<String>newHashSet(),
        NodeUtil.getVarsDeclaredInBranch(
            parse("function(){var foo;}")));
    assertNodeNames(Sets.newHashSet("goo"),
        NodeUtil.getVarsDeclaredInBranch(
            parse("var goo;function(){var foo;}")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsControlStructureCodeBlock
  public void testIsControlStructureCodeBlock() {
    Compiler compiler = new Compiler();

    Node root = parse("if (x) foo(); else boo();");
    Node ifNode = root.getFirstChild();

    Node ifCondition = ifNode.getFirstChild();
    Node ifCase = ifNode.getFirstChild().getNext();
    Node elseCase = ifNode.getLastChild();

    assertFalse(NodeUtil.isControlStructureCodeBlock(ifNode, ifCondition));
    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, ifCase));
    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, elseCase));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsAnonymousFunction1
  public void testIsAnonymousFunction1() {
    Compiler compiler = new Compiler();

    Node root = parse("(function foo() {})");
    Node StatementNode = root.getFirstChild();
    assertTrue(NodeUtil.isExpressionNode(StatementNode));
    Node functionNode = StatementNode.getFirstChild();
    assertTrue(NodeUtil.isFunction(functionNode));
    assertTrue(NodeUtil.isAnonymousFunction(functionNode));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsAnonymousFunction2
  public void testIsAnonymousFunction2() {
    Compiler compiler = new Compiler();

    Node root = parse("function foo() {}");
    Node functionNode = root.getFirstChild();
    assertTrue(NodeUtil.isFunction(functionNode));
    assertFalse(NodeUtil.isAnonymousFunction(functionNode));
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveTryChild
  public void testRemoveTryChild() {
    Compiler compiler = new Compiler();

    Node root = parse("try {foo()} catch(e) {} finally {}");

    
    Node actual = root.cloneTree();

    Node tryNode = actual.getFirstChild();
    Node tryBlock = tryNode.getFirstChild();
    Node catchBlocks = tryNode.getFirstChild().getNext();
    Node finallyBlock = tryNode.getLastChild();

    NodeUtil.removeChild(tryNode, finallyBlock);
    String expected = "try {foo()} catch(e) {}";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }

    
    actual = root.cloneTree();

    tryNode = actual.getFirstChild();
    tryBlock = tryNode.getFirstChild();
    catchBlocks = tryNode.getFirstChild().getNext();
    finallyBlock = tryNode.getLastChild();

    NodeUtil.removeChild(tryNode, tryBlock);
    expected = "try {} catch(e) {} finally {}";
    difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }

    
    actual = root.cloneTree();

    tryNode = actual.getFirstChild();
    tryBlock = tryNode.getFirstChild();
    catchBlocks = tryNode.getFirstChild().getNext();
    Node catchBlock = catchBlocks.getFirstChild();
    finallyBlock = tryNode.getLastChild();

    NodeUtil.removeChild(catchBlocks, catchBlock);
    expected = "try {foo()} finally {}";
    difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }

  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveVarChild
  public void testRemoveVarChild() {
    Compiler compiler = new Compiler();

    
    Node actual = parse("var foo, goo, hoo");

    Node varNode = actual.getFirstChild();
    Node nameNode = varNode.getFirstChild();

    NodeUtil.removeChild(varNode, nameNode);
    String expected = "var goo, hoo";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }

    
    actual = parse("var foo, goo, hoo");

    varNode = actual.getFirstChild();
    nameNode = varNode.getFirstChild().getNext();

    NodeUtil.removeChild(varNode, nameNode);
    expected = "var foo, hoo";
    difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }

    
    actual = parse("var foo, hoo");

    varNode = actual.getFirstChild();
    nameNode = varNode.getFirstChild().getNext();

    NodeUtil.removeChild(varNode, nameNode);
    expected = "var foo";
    difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }

    
    actual = parse("var hoo");

    varNode = actual.getFirstChild();
    nameNode = varNode.getFirstChild();

    NodeUtil.removeChild(varNode, nameNode);
    expected = "";
    difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveLabelChild1
  public void testRemoveLabelChild1() {
    Compiler compiler = new Compiler();

    
    Node actual = parse("foo: goo()");

    Node labelNode = actual.getFirstChild();
    Node callExpressNode = labelNode.getLastChild();

    NodeUtil.removeChild(labelNode, callExpressNode);
    String expected = "";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveLabelChild2
  public void testRemoveLabelChild2() {
    
    Node actual = parse("achoo: foo: goo()");

    Node labelNode = actual.getFirstChild();
    Node callExpressNode = labelNode.getLastChild();

    NodeUtil.removeChild(labelNode, callExpressNode);
    String expected = "";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveForChild
  public void testRemoveForChild() {
    Compiler compiler = new Compiler();

    
    Node actual = parse("for(var a=0;a<0;a++)foo()");

    Node forNode = actual.getFirstChild();
    Node child = forNode.getFirstChild();

    NodeUtil.removeChild(forNode, child);
    String expected = "for(;a<0;a++)foo()";
    String difference = parse(expected).checkTreeEquals(actual);
    assertNull("Nodes do not match:\n" + difference, difference);

    
    actual = parse("for(var a=0;a<0;a++)foo()");

    forNode = actual.getFirstChild();
    child = forNode.getFirstChild().getNext();

    NodeUtil.removeChild(forNode, child);
    expected = "for(var a=0;;a++)foo()";
    difference = parse(expected).checkTreeEquals(actual);
    assertNull("Nodes do not match:\n" + difference, difference);

    
    actual = parse("for(var a=0;a<0;a++)foo()");

    forNode = actual.getFirstChild();
    child = forNode.getFirstChild().getNext().getNext();

    NodeUtil.removeChild(forNode, child);
    expected = "for(var a=0;a<0;)foo()";
    difference = parse(expected).checkTreeEquals(actual);
    assertNull("Nodes do not match:\n" + difference, difference);

    
    actual = parse("for(var a=0;a<0;a++)foo()");

    forNode = actual.getFirstChild();
    child = forNode.getLastChild();

    NodeUtil.removeChild(forNode, child);
    expected = "for(var a=0;a<0;a++);";
    difference = parse(expected).checkTreeEquals(actual);
    assertNull("Nodes do not match:\n" + difference, difference);

    
    actual = parse("for(a in ack)foo();");

    forNode = actual.getFirstChild();
    child = forNode.getLastChild();

    NodeUtil.removeChild(forNode, child);
    expected = "for(a in ack);";
    difference = parse(expected).checkTreeEquals(actual);
    assertNull("Nodes do not match:\n" + difference, difference);
  }

// com.google.javascript.jscomp.NodeUtilTest::testMergeBlock1
  public void testMergeBlock1() {
    Compiler compiler = new Compiler();

    
    Node actual = parse("{{a();b();}}");

    Node parentBlock = actual.getFirstChild();
    Node childBlock = parentBlock.getFirstChild();

    assertTrue(NodeUtil.tryMergeBlock(childBlock));
    String expected = "{a();b();}";
    String difference = parse(expected).checkTreeEquals(actual);
    assertNull("Nodes do not match:\n" + difference, difference);
  }

// com.google.javascript.jscomp.NodeUtilTest::testMergeBlock2
  public void testMergeBlock2() {
    Compiler compiler = new Compiler();

    
    Node actual = parse("foo:{a();}");

    Node parentLabel = actual.getFirstChild();
    Node childBlock = parentLabel.getLastChild();

    assertTrue(NodeUtil.tryMergeBlock(childBlock));
    String expected = "foo:a();";
    String difference = parse(expected).checkTreeEquals(actual);
    assertNull("Nodes do not match:\n" + difference, difference);
  }

// com.google.javascript.jscomp.NodeUtilTest::testMergeBlock3
  public void testMergeBlock3() {
    Compiler compiler = new Compiler();

    
    String code = "foo:{a();boo()}";
    Node actual = parse("foo:{a();boo()}");

    Node parentLabel = actual.getFirstChild();
    Node childBlock = parentLabel.getLastChild();

    assertFalse(NodeUtil.tryMergeBlock(childBlock));
    String expected = code;
    String difference = parse(expected).checkTreeEquals(actual);
    assertNull("Nodes do not match:\n" + difference, difference);
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetSourceName
  public void testGetSourceName() {
    Node n = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, n);
    parent.putProp(Node.SOURCENAME_PROP, "foo");
    assertEquals("foo", NodeUtil.getSourceName(n));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsLabelName
  public void testIsLabelName() {
    Compiler compiler = new Compiler();

    
    String code = "a:while(1) {a; continue a; break a; break;}";
    Node actual = parse(code);

    Node labelNode = actual.getFirstChild();
    assertTrue(labelNode.getType() == Token.LABEL);
    assertTrue(NodeUtil.isLabelName(labelNode.getFirstChild()));
    assertFalse(NodeUtil.isLabelName(labelNode.getLastChild()));

    Node whileNode = labelNode.getLastChild();
    assertTrue(whileNode.getType() == Token.WHILE);
    Node whileBlock = whileNode.getLastChild();
    assertTrue(whileBlock.getType() == Token.BLOCK);
    assertFalse(NodeUtil.isLabelName(whileBlock));

    Node firstStatement = whileBlock.getFirstChild();
    assertTrue(firstStatement.getType() == Token.EXPR_RESULT);
    Node variableReference = firstStatement.getFirstChild();
    assertTrue(variableReference.getType() == Token.NAME);
    assertFalse(NodeUtil.isLabelName(variableReference));

    Node continueStatement = firstStatement.getNext();
    assertTrue(continueStatement.getType() == Token.CONTINUE);
    assertTrue(NodeUtil.isLabelName(continueStatement.getFirstChild()));

    Node firstBreak = continueStatement.getNext();
    assertTrue(firstBreak.getType() == Token.BREAK);
    assertTrue(NodeUtil.isLabelName(firstBreak.getFirstChild()));

    Node secondBreak = firstBreak.getNext();
    assertTrue(secondBreak.getType() == Token.BREAK);
    assertFalse(secondBreak.hasChildren());
    assertFalse(NodeUtil.isLabelName(secondBreak.getFirstChild()));
  }

// com.google.javascript.jscomp.NormalizeTest::testSplitVar
  public void testSplitVar() {
    testSame("var a");
    test("var a, b",
         "var a; var b");
    test("var a, b, c",
         "var a; var b; var c");
    testSame("var a = 0 ");
    test("var a = 0 , b = foo()",
         "var a = 0; var b = foo()");
    test("var a = 0, b = 1, c = 2",
         "var a = 0; var b = 1; var c = 2");
    test("var a = foo(1), b = foo(2), c = foo(3)",
         "var a = foo(1); var b = foo(2); var c = foo(3)");

    
    test("for(var a = 0, b = foo(1), c = 1; c < b; c++) foo(2)",
         "var a = 0; var b = foo(1); var c = 1; for(; c < b; c++) foo(2)");

    
    test("for(;;) var b = foo(1), c = foo(2);",
        "for(;;){var b = foo(1); var c = foo(2)}");
    test("for(;;){var b = foo(1), c = foo(2);}",
         "for(;;){var b = foo(1); var c = foo(2)}");

    test("try{var b = foo(1), c = foo(2);} finally foo(3);",
         "try{var b = foo(1); var c = foo(2)} finally foo(3);");
    test("try{var b = foo(1),c = foo(2);} finally;",
         "try{var b = foo(1); var c = foo(2)} finally;");
    test("try{foo(0);} finally var b = foo(1), c = foo(2);",
         "try{foo(0);} finally {var b = foo(1); var c = foo(2)}");

    test("switch(a) {default: var b = foo(1), c = foo(2); break;}",
         "switch(a) {default: var b = foo(1); var c = foo(2); break;}");

    test("do var a = foo(1), b; while(false);",
         "do{var a = foo(1); var b} while(false);");
    test("a:var a,b,c;",
         "a:{ var a;var b; var c; }");
    test("a:for(var a,b,c;;);",
         "var a;var b; var c;a:for(;;);");
    test("if (true) a:var a,b;",
         "if (true)a:{ var a; var b; }");
  }

// com.google.javascript.jscomp.NormalizeTest::testUnhandled
  public void testUnhandled() {
    testSame("var x = y = 1");
  }

// com.google.javascript.jscomp.NormalizeTest::testFor
  public void testFor() {
    
    test("for(a = 0; a < 2 ; a++) foo();",
         "a = 0; for(; a < 2 ; a++) foo()");
    
    test("for(var a = 0; c < b ; c++) foo()",
         "var a = 0; for(; c < b ; c++) foo()");

    
    test("a:for(var a = 0; c < b ; c++) foo()",
         "var a = 0; a:for(; c < b ; c++) foo()");
    
    test("a:b:for(var a = 0; c < b ; c++) foo()",
         "var a = 0; a:b:for(; c < b ; c++) foo()");

    
    test("if(x) for(var a = 0; c < b ; c++) foo()",
         "if(x){var a = 0; for(; c < b ; c++) foo()}");

    
    test("for(init(); a < 2 ; a++) foo();",
         "init(); for(; a < 2 ; a++) foo()");
  }

// com.google.javascript.jscomp.NormalizeTest::testWhile
  public void testWhile() {
    
    test("while(c < b) foo()",
         "for(; c < b;) foo()");
  }

// com.google.javascript.jscomp.NormalizeTest::testMoveFunctions1
  public void testMoveFunctions1() throws Exception {
    test("function f() { if (x) return; foo(); function foo() {} }",
         "function f() {function foo() {} if (x) return; foo(); }");
    test("function f() { " +
            "function foo() {} " +
            "if (x) return;" +
            "foo(); " +
            "function bar() {} " +
         "}",
         "function f() {" +
           "function foo() {}" +
           "function bar() {}" +
           "if (x) return;" +
           "foo();" +
         "}");
  }

// com.google.javascript.jscomp.NormalizeTest::testMoveFunctions2
  public void testMoveFunctions2() throws Exception {
    testSame("function f() { function foo() {} }");
    test("function f() { f(); a:function bar() {} }",
         "function f() { f(); a:{function bar() {}}}");
    testSame("function f() { f(); {function bar() {}}}");
    testSame("function f() { f(); if (true) {function bar() {}}}");
  }

// com.google.javascript.jscomp.NormalizeTest::testMakeLocalNamesUnique
  public void testMakeLocalNamesUnique() {
    if (!Normalize.MAKE_LOCAL_NAMES_UNIQUE) {
      return;
    }

    
    testSame("var a;");

    
    testSame("a;");

    
    test("var a;function foo(a){var b;a}",
         "var a;function foo(a$$1){var b;a$$1}");
    test("var a;function foo(){var b;a}function boo(){var b;a}",
         "var a;function foo(){var b;a}function boo(){var b$$1;a}");
    test("function foo(a){var b}" +
         "function boo(a){var b}",
         "function foo(a){var b}" +
         "function boo(a$$1){var b$$1}");

    
    test("var a = function foo(){foo()};var b = function foo(){foo()};",
         "var a = function foo(){foo()};var b = function foo$$1(){foo$$1()};");

    
    test("try { } catch(e) {e;}",
         "try { } catch(e) {e;}");
    test("try { } catch(e) {e;}; try { } catch(e) {e;}",
         "try { } catch(e) {e;}; try { } catch(e$$1) {e$$1;}");
    test("try { } catch(e) {e; try { } catch(e) {e;}};",
         "try { } catch(e) {e; try { } catch(e$$1) {e$$1;} }; ");

    
    testSame("\nvar window;");

    
    test("function f() {var window}",
         "function f() {var window$$1}");
  }

// com.google.javascript.jscomp.NormalizeTest::testRemoveDuplicateVarDeclarations
  public void testRemoveDuplicateVarDeclarations() {
    test("function f() { var a; var a }",
         "function f() { var a; }");
    test("function f() { var a = 1; var a = 2 }",
         "function f() { var a = 1; a = 2 }");
    test("var a = 1; function f(){ var a = 2 }",
         "var a = 1; function f(){ var a$$1 = 2 }");
    test("function f() { var a = 1; lable1:var a = 2 }",
         "function f() { var a = 1; lable1:{a = 2}}");
    test("function f() { var a = 1; lable1:var a }",
         "function f() { var a = 1; lable1:{} }");
    test("function f() { var a = 1; for(var a in b); }",
         "function f() { var a = 1; for(a in b); }");
  }

// com.google.javascript.jscomp.NormalizeTest::testRenamingConstants
  public void testRenamingConstants() {
    test("var ACONST = 4;var b = ACONST;",
         "var ACONST = 4; var b = ACONST;");

    test("var a, ACONST = 4;var b = ACONST;",
         "var a; var ACONST = 4; var b = ACONST;");

    test("var ACONST; ACONST = 4; var b = ACONST;",
         "var ACONST; ACONST = 4;" +
         "var b = ACONST;");

    test("var ACONST = new Foo(); var b = ACONST;",
         "var ACONST = new Foo(); var b = ACONST;");

    test("var aa; aa=1;", "var aa;aa=1");
  }

// com.google.javascript.jscomp.NormalizeTest::testSkipRenamingExterns
  public void testSkipRenamingExterns() {
    test("var EXTERN; var ext; ext.FOO;", "var b = EXTERN; var c = ext.FOO",
         "var b = EXTERN; var c = ext.FOO", null, null);
  }

// com.google.javascript.jscomp.NormalizeTest::testRenamingConstantProperties
  public void testRenamingConstantProperties() {
    
    
    
    new WithCollapse().testConstantProperties();
  }

// com.google.javascript.jscomp.ObjectPropertyStringPostprocessTest::testFooDotBar
  public void testFooDotBar() {
    testPass("goog.global, foo.bar", "foo, 'bar'");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPostprocessTest::testFooGetElemBar
  public void testFooGetElemBar() {
    testPass("goog.global, foo[bar]", "foo, bar");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPostprocessTest::testFooBar
  public void testFooBar() {
    testPass("goog.global, foo$bar", "goog.global, 'foo$bar'");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testDeclaration
  public void testDeclaration() {
    test("goog.testing.ObjectPropertyString = function() {}",
         "JSCompiler_ObjectPropertyString = function() {}");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testFooBar
  public void testFooBar() {
    test("new goog.testing.ObjectPropertyString(foo, 'bar')",
         "new JSCompiler_ObjectPropertyString(goog.global, foo.bar)");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testFooPrototypeBar
  public void testFooPrototypeBar() {
    test("new goog.testing.ObjectPropertyString(foo.prototype, 'bar')",
         "new JSCompiler_ObjectPropertyString(goog.global, " +
         "foo.prototype.bar)");
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testInvalidNumArgumentsError
  public void testInvalidNumArgumentsError() {
    testSame(new String[] {"new goog.testing.ObjectPropertyString()"},
        ObjectPropertyStringPreprocess.INVALID_NUM_ARGUMENTS_ERROR);
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testQualifedNameExpectedError
  public void testQualifedNameExpectedError() {
    testSame(
        new String[] {
          "new goog.testing.ObjectPropertyString(foo[a], 'bar')"
        },
        ObjectPropertyStringPreprocess.QUALIFIED_NAME_EXPECTED_ERROR);
  }

// com.google.javascript.jscomp.ObjectPropertyStringPreprocessTest::testStringLiteralExpectedError
  public void testStringLiteralExpectedError() {
    testSame(new String[] {"new goog.testing.ObjectPropertyString(foo, bar)"},
        ObjectPropertyStringPreprocess.STRING_LITERAL_EXPECTED_ERROR);
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testSimple
  public void testSimple() {
    test("function foo()   { alert(arguments[0]); }",
         "function foo(p0) { alert(p0); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testNoVarArgs
  public void testNoVarArgs() {
    testSame("function(a,b,c) { alert(a + b + c) }");

    test("function(a,b,c) { alert(arguments[0]) }",
         "function(a,b,c) { alert(a) }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testMissingVarArgs
  public void testMissingVarArgs() {
    testSame("function() { alert(arguments[x]) }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testArgumentRefOnNamedParameter
  public void testArgumentRefOnNamedParameter() {
    test("function(a,b) { alert(arguments[0]) }",
         "function(a,b) { alert(a) }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testTwoVarArgs
  public void testTwoVarArgs() {
    test("function foo(a) { alert(arguments[1] + arguments[2]); }",
         "function foo(a, p0, p1) { alert(p0 + p1); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testTwoFourArgsTwoUsed
  public void testTwoFourArgsTwoUsed() {
    test("function foo() { alert(arguments[0] + arguments[3]); }",
         "function foo(p0, p1, p2, p3) { alert(p0 + p3); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testOneRequired
  public void testOneRequired() {
    test("function foo(req0, var_args) { alert(req0 + arguments[1]); }",
         "function foo(req0, var_args) { alert(req0 + var_args); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testTwoRequiredSixthVarArgReferenced
  public void testTwoRequiredSixthVarArgReferenced() {
    test("function foo(r0, r1, var_args) {alert(r0 + r1 + arguments[5]);}",
         "function foo(r0, r1, var_args, p0, p1, p2) { alert(r0 + r1 + p2); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testTwoRequiredOneOptionalFifthVarArgReferenced
  public void testTwoRequiredOneOptionalFifthVarArgReferenced() {
    test("function foo(r0, r1, opt_1)"
       + "  {alert(r0 + r1 + opt_1 + arguments[4]);}",
         "function foo(r0, r1, opt_1, p0, p1)"
       + "  {alert(r0 + r1 + opt_1 + p1); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testTwoRequiredTwoOptionalSixthVarArgReferenced
  public void testTwoRequiredTwoOptionalSixthVarArgReferenced() {
    test("function foo(r0, r1, opt_1, opt_2)"
       + "  {alert(r0 + r1 + opt_1 + opt_2 + arguments[5]);}",
         "function foo(r0, r1, opt_1, opt_2, p0, p1)"
       + "  {alert(r0 + r1 + opt_1 + opt_2 + p1); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testInnerFunctions
  public void testInnerFunctions() {
    test("function f() { function b(  ) { arguments[0]  }}",
         "function f() { function b(p0) {            p0 }}");

    test("function f(  ) { function b() { }  arguments[0] }",
         "function f(p0) { function b() { }            p0 }");

    test("function f( )  { arguments[0]; function b(  ) { arguments[0] }}",
         "function f(p1) {           p1; function b(p0) {           p0 }}");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testInnerFunctionsWithNamedArgumentInInnerFunction
  public void testInnerFunctionsWithNamedArgumentInInnerFunction() {
    test("function f() { function b(x   ) { arguments[1] }}",
         "function f() { function b(x,p0) {           p0 }}");

    test("function f(  ) { function b(x) { }  arguments[0] }",
         "function f(p0) { function b(x) { }            p0 }");

    test("function f( )  { arguments[0]; function b(x   ) { arguments[1] }}",
         "function f(p1) {           p1; function b(x,p0) {           p0 }}");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testInnerFunctionsWithNamedArgumentInOutterFunction
  public void testInnerFunctionsWithNamedArgumentInOutterFunction() {
    test("function f(x) { function b(  ) { arguments[0] }}",
         "function f(x) { function b(p0) {           p0 }}");

    test("function f(x   ) { function b() { }  arguments[1] }",
         "function f(x,p0) { function b() { }            p0 }");

    test("function f(x   ) { arguments[1]; function b(  ) { arguments[0] }}",
         "function f(x,p1) {           p1; function b(p0) {           p0 }}");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testInnerFunctionsWithNamedArgumentInInnerAndOutterFunction
  public void testInnerFunctionsWithNamedArgumentInInnerAndOutterFunction() {
    test("function f(x) { function b(x   ) { arguments[1] }}",
         "function f(x) { function b(x,p0) {           p0 }}");

    test("function f(x   ) { function b(x) { }  arguments[1] }",
         "function f(x,p0) { function b(x) { }            p0 }");

    test("function f(x   ) { arguments[1]; function b(x   ) { arguments[1] }}",
         "function f(x,p1) {           p1; function b(x,p0) {           p0 }}");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testInnerFunctionsAfterArguments
  public void testInnerFunctionsAfterArguments() {
    
    
    test("function f(  ) { arguments[0]; function b() { function c() { }} }",
         "function f(p0) {           p0; function b() { function c() { }} }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testNoOptimizationWhenGetProp
  public void testNoOptimizationWhenGetProp() {
    testSame("function f() { arguments[0]; arguments.size }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testNoOptimizationWhenIndexIsNotNumberConstant
  public void testNoOptimizationWhenIndexIsNotNumberConstant() {
    testSame("function f() { arguments[0]; arguments['callee'].length}");
    testSame("function f() { arguments[0]; arguments.callee.length}");
    testSame(
        "function f() { arguments[0]; var x = 'callee'; arguments[x].length}");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testNoOptimizationWhenArgumentIsUsedAsFunctionCall
  public void testNoOptimizationWhenArgumentIsUsedAsFunctionCall() {
    testSame("function f() {arguments[0]()}");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoRemoval
  public void testNoRemoval() {
    testSame("function foo(p1) { } foo(1); foo(2)");
    testSame("function foo(p1) { } foo(1,2); foo(3,4)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNotAFunction
  public void testNotAFunction() {
    testSame("var x = 1; x; x = 2");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalNamedFunction
  public void testRemoveOneOptionalNamedFunction() {
    test("function foo(p1) { } foo()", "function foo() {var p1} foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalVarAssignment
  public void testRemoveOneOptionalVarAssignment() {
    test("var foo = function (p1) { }; foo()",
        "var foo = function () {var p1}; foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalExpressionAssign
  public void testRemoveOneOptionalExpressionAssign() {
    test("var foo; foo = function (p1) { }; foo()",
        "var foo; foo = function () {var p1}; foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalOneRequired
  public void testRemoveOneOptionalOneRequired() {
    test("function foo(p1, p2) { } foo(1); foo(2)",
        "function foo(p1) {var p2} foo(1); foo(2)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalMultipleCalls
  public void testRemoveOneOptionalMultipleCalls() {
    test( "function foo(p1, p2) { } foo(1); foo(2); foo()",
        "function foo(p1) {var p2} foo(1); foo(2); foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalMultiplePossibleDefinition
  public void testRemoveOneOptionalMultiplePossibleDefinition() {
    String src = "var goog = {};" +
        "goog.foo = function (p1, p2) { };" +
        "goog.foo = function (q1, q2) { };" +
        "goog.foo = function (r1, r2) { };" +
        "goog.foo(1); goog.foo(2); goog.foo()";

    String expected = "var goog = {};" +
        "goog.foo = function (p1) { var p2 };" +
        "goog.foo = function (q1) { var q2 };" +
        "goog.foo = function (r1) { var r2 };" +
        "goog.foo(1); goog.foo(2); goog.foo()";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveTwoOptionalMultiplePossibleDefinition
  public void testRemoveTwoOptionalMultiplePossibleDefinition() {
    String src = "var goog = {};" +
        "goog.foo = function (p1, p2, p3, p4) { };" +
        "goog.foo = function (q1, q2, q3, q4) { };" +
        "goog.foo = function (r1, r2, r3, r4) { };" +
        "goog.foo(1,0); goog.foo(2,1); goog.foo()";

    String expected = "var goog = {};" +
        "goog.foo = function(p1, p2) { var p4; var p3};" +
        "goog.foo = function(q1, q2) { var q4; var q3};" +
        "goog.foo = function(r1, r2) { var r4; var r3};" +
        "goog.foo(1,0); goog.foo(2,1); goog.foo()";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testConstructorOptArgsNotRemoved
  public void testConstructorOptArgsNotRemoved() {
    String src =
        "" +
        "var goog = function(){};" +
        "goog.prototype.foo = function(a,b) {};" +
        "goog.prototype.bar = function(a) {};" +
        "goog.bar.inherits(goog.foo);" +
        "new goog.foo(2,3);" +
        "new goog.foo(1,2);";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testUnknown
  public void testUnknown() {
    String src = "var goog1 = {};" +
        "goog1.foo = function () { };" +
        "var goog2 = {};" +
        "goog2.foo = function (p1) { };" +
        "var x = getGoog();" +
        "x.foo()";

    String expected = "var goog1 = {};" +
        "goog1.foo = function () { };" +
        "var goog2 = {};" +
        "goog2.foo = function () { var p1 };" +
        "var x = getGoog();" +
        "x.foo()";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveVarArg
  public void testRemoveVarArg() {
    test("function foo(p1, var_args) { } foo(1); foo(2)",
        "function foo(p1) { var var_args } foo(1); foo(2)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testAliasMethodsDontGetOptimize
  public void testAliasMethodsDontGetOptimize() {
    String src =
        "var foo = function(a, b) {};" +
        "var goog = {};" +
        "goog.foo = foo;" +
        "goog.prototype.bar = goog.foo;" +
        "new goog().bar(1,2);" +
        "foo(2);";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testAliasMethodsDontGetOptimize2
  public void testAliasMethodsDontGetOptimize2() {
    String src =
        "var foo = function(a, b) {};" +
        "var bar = foo;" +
        "foo(1);" +
        "bar(2,3);";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testAliasMethodsDontGetOptimize3
  public void testAliasMethodsDontGetOptimize3() {
    String src =
        "var array = {};" +
        "array[0] = function(a, b) {};" +
        "var foo = array[0];" + 
        "foo(1);";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testAliasMethodsDontGetOptimize4
  public void testAliasMethodsDontGetOptimize4() {
    String src = "function foo(bar) {};" +
        "baz = function(a) {};" +
        "baz(1);" +
        "foo(baz);"; 
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveConstantArgument
  public void testRemoveConstantArgument() {
    
    test("function foo(p1, p2) {}; foo(1,2); foo(2,2);",
         "function foo(p1) {var p2 = 2}; foo(1); foo(2)");

    
    testSame("function foo(p1, p2) {}; foo(1); foo(2,3);");

    
    test("function foo(a,b,c){}; foo(1, 2, 3); foo(1, 2, 4); foo(2, 2, 3)",
         "function foo(a,c){var b=2}; foo(1, 3); foo(1, 4); foo(2, 3)");

    
    test("function foo(a) {}; foo(1); foo(1.0);",
         "function foo() {var a = 1;}; foo(); foo();");

    
    String src =
        "" +
        "function Person(){}; Person.prototype.run = function(a, b) {};" +
        "Person.run(1, 'a'); Person.run(2, 'a')";
    String expected =
        "function Person(){}; Person.prototype.run = " +
        "function(a) {var b = 'a'};" +
        "Person.run(1); Person.run(2)";
    test(src, expected);

  }

// com.google.javascript.jscomp.OptimizeParametersTest::testCanDeleteArgumentsAtAnyPosition
  public void testCanDeleteArgumentsAtAnyPosition() {
    
    String src =
        "function foo(a,b,c,d,e) {};" +
        "foo(1,2,3,4,5);" +
        "foo(2,2,4,4,5);";
    String expected =
        "function foo(a,c) {var b=2; var d=4; var e=5;};" +
        "foo(1,3);" +
        "foo(2,4);";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoOptimizationForExternsFunctions
  public void testNoOptimizationForExternsFunctions() {
    testSame("function _foo(x, y, z){}; _foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoOptimizationForGoogExportSymbol
  public void testNoOptimizationForGoogExportSymbol() {
    testSame("goog.exportSymbol('foo', foo);" +
             "function foo(x, y, z){}; foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoArgumentRemovalNonEqualNodes
  public void testNoArgumentRemovalNonEqualNodes() {
    testSame("function foo(a){}; foo('bar'); foo('baz');");
    testSame("function foo(a){}; foo(1.0); foo(2.0);");
    testSame("function foo(a){}; foo(true); foo(false);");
    testSame("var a = 1, b = 2; function foo(a){}; foo(a); foo(b);");
    testSame("function foo(a){}; foo(/&/g); foo(/</g);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testFunctionPassedAsParam
  public void testFunctionPassedAsParam() {
    String src =
        " function person(){}; " +
        "person.prototype.run = function(a, b) {};" +
        "person.prototype.walk = function() {};" +
        "person.prototype.foo = function() { this.run(this.walk, 0.1)};" +
        "person.foo();";
    String expected =
        "function person(){}; person.prototype.run = function(a) {" +
        "  var b = 0.1;};" +
        "person.prototype.walk = function() {};" +
        "person.prototype.foo = function() { this.run(this.walk)};" +
        "person.foo();";

    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testCallIsIgnore
  public void testCallIsIgnore() {
    testSame("var goog;" +
        "goog.foo = function(a, opt) {};" +
        "var bar = function(){goog.foo.call(this, 1)};" +
        "goog.foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testApplyIsIgnore
  public void testApplyIsIgnore() {
    testSame("var goog;" +
        "goog.foo = function(a, opt) {};" +
        "var bar = function(){goog.foo.apply(this, 1)};" +
        "goog.foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testFunctionWithReferenceToArgumentsShouldNotBeOptimize
  public void testFunctionWithReferenceToArgumentsShouldNotBeOptimize() {
    testSame("function foo(a,b,c) { return arguments.size; };" +
             "foo(1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testConstantArgumentsToConstructorCanBeOptimized
  public void testConstantArgumentsToConstructorCanBeOptimized() {
    String src = "function foo(a) {};" +
        "var bar = new foo(1);";
    String expected = "function foo() {var a=1;};" +
        "var bar = new foo();";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testOptionalArgumentsToConstructorCanBeOptimized
  public void testOptionalArgumentsToConstructorCanBeOptimized() {
    String src = "function foo(a) {};" +
        "var bar = new foo();";
    String expected = "function foo() {var a;};" +
        "var bar = new foo();";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRegexesCanBeInlined
  public void testRegexesCanBeInlined() {
    test("function foo(a) {}; foo(/abc/);",
        "function foo() {var a = /abc/}; foo();");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testConstructorUsedAsFunctionCanBeOptimized
  public void testConstructorUsedAsFunctionCanBeOptimized() {
    String src = "function foo(a) {};" +
        "var bar = new foo(1);" +
        "foo(1);";
    String expected = "function foo() {var a=1;};" +
        "var bar = new foo();" +
        "foo();";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeConstructorWhenArgumentsAreNotEqual
  public void testDoNotOptimizeConstructorWhenArgumentsAreNotEqual() {
    testSame("function Foo(a) {};" +
        "var bar = new Foo(1);" +
        "var baz = new Foo(2);");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testNoFunction
  public void testNoFunction() {
    replace("\"foo\"");
    replace("var foo");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testOneFunction
  public void testOneFunction() {
    replace("\"foo\";function foo(){\"foo\"}");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testTwoFunctions
  public void testTwoFunctions() {
    replace("\"foo\";function f1(){\"foo\"}function f2(){\"foo\"}");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testInnerFunctions
  public void testInnerFunctions() {
    replace("\"foo\";function f1(){\"foo\";function f2(){\"foo\"}}");
  }

// com.google.javascript.jscomp.ParallelCompilerPassTest::testManyFunctions
  public void testManyFunctions() {
    StringBuffer sb = new StringBuffer("\"foo\";");
    for (int i = 0; i < 20; i++) {
      sb.append("function f");
      sb.append(i);
      sb.append("(){\"foo\"}");
    }
    replace(sb.toString());
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testOneRun
  public void testOneRun() {
    addOneTimePass("x");
    assertPasses("x");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testTwoRuns
  public void testTwoRuns() {
    addOneTimePass("x");
    optimizer.process(null, null);
    try {
      optimizer.process(null, null);
      fail();
    } catch (IllegalStateException e) {
      assertEquals(
          "One-time passes cannot be run multiple times: x", e.getMessage());
    }
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testLoop1
  public void testLoop1() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 0);
    assertPasses("x");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testLoop2
  public void testLoop2() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 3);
    assertPasses("x", "x", "x", "x");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testLoop3
  public void testLoop3() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 3);
    addLoopedPass(loop, "y", 1);
    assertPasses("x", "y", "x", "y", "x", "y", "x", "y");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testNotInfiniteLoop
  public void testNotInfiniteLoop() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", PhaseOptimizer.MAX_LOOPS);
    optimizer.process(null, null);
    assertEquals("There should be no errors.", 0, compiler.getErrorCount());
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testInfiniteLoop
  public void testInfiniteLoop() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", PhaseOptimizer.MAX_LOOPS + 1);
    try {
      optimizer.process(null, null);
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains(PhaseOptimizer.OPTIMIZE_LOOP_ERROR));
    }
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testCombined
  public void testCombined() {
    addOneTimePass("a");
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 3);
    addLoopedPass(loop, "y", 1);
    addOneTimePass("z");
    assertPasses("a", "x", "y", "x", "y", "x", "y", "x", "y", "z");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testSanityCheck
  public void testSanityCheck() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 1);
    addOneTimePass("z");
    optimizer.setSanityCheck(
        createPassFactory("sanity", createPass("sanity", 0), false));
    assertPasses("x", "sanity", "x", "sanity", "z", "sanity");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testConsumption1
  public void testConsumption1() {
    optimizer.consume(
        Lists.newArrayList(
            createPassFactory("a", 0, true),
            createPassFactory("b", 1, false),
            createPassFactory("c", 2, false),
            createPassFactory("d", 1, false),
            createPassFactory("e", 1, true),
            createPassFactory("f", 0, true)));
    assertPasses("a", "b", "c", "d", "b", "c", "d", "b", "c", "d", "e", "f");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testConsumption2
  public void testConsumption2() {
    optimizer.consume(
        Lists.newArrayList(
            createPassFactory("a", 2, false),
            createPassFactory("b", 1, true),
            createPassFactory("c", 1, false)));
    assertPasses("a", "a", "a", "b", "c", "c");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testConsumption3
  public void testConsumption3() {
    optimizer.consume(
        Lists.newArrayList(
            createPassFactory("a", 2, true),
            createPassFactory("b", 0, false),
            createPassFactory("c", 0, false)));
    assertPasses("a", "b", "c");
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testDuplicateLoop
  public void testDuplicateLoop() {
    Loop loop = optimizer.addFixedPointLoop();
    addLoopedPass(loop, "x", 1);
    try {
      addLoopedPass(loop, "x", 1);
      fail("Expected exception");
    } catch (IllegalArgumentException e) {}
  }

// com.google.javascript.jscomp.PhaseOptimizerTest::testPassOrdering
  public void testPassOrdering() {
    Loop loop = optimizer.addFixedPointLoop();
    List<String> optimalOrder = Lists.newArrayList(
        PhaseOptimizer.OPTIMAL_ORDER);
    Random random = new Random();
    while (optimalOrder.size() > 0) {
      addLoopedPass(
          loop, optimalOrder.remove(random.nextInt(optimalOrder.size())), 0);
    }
    optimizer.process(null, null);
    assertEquals(PhaseOptimizer.OPTIMAL_ORDER, passesRun);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSimpleProvides
  public void testSimpleProvides() {
    test("goog.provide('foo');",
         "var foo={};");
    test("goog.provide('foo.bar');",
         "var foo={}; foo.bar={};");
    test("goog.provide('foo.bar.baz');",
         "var foo={}; foo.bar={}; foo.bar.baz={};");
    test("goog.provide('foo.bar.baz.boo');",
         "var foo={}; foo.bar={}; foo.bar.baz={}; foo.bar.baz.boo={};");
    test("goog.provide('goog.bar');",
         "goog.bar={};");  
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMultipleProvides
  public void testMultipleProvides() {
    test("goog.provide('foo.bar'); goog.provide('foo.baz');",
         "var foo={}; foo.bar={}; foo.baz={};");
    test("goog.provide('foo.bar.baz'); goog.provide('foo.boo.foo');",
         "var foo={}; foo.bar={}; foo.bar.baz={}; foo.boo={}; foo.boo.foo={};");
    test("goog.provide('foo.bar.baz'); goog.provide('foo.bar.boo');",
         "var foo={}; foo.bar={}; foo.bar.baz={}; foo.bar.boo={};");
    test("goog.provide('foo.bar.baz'); goog.provide('goog.bar.boo');",
         "var foo={}; foo.bar={}; foo.bar.baz={}; goog.bar={}; " +
         "goog.bar.boo={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalOfProvidedObjLit
  public void testRemovalOfProvidedObjLit() {
    test("goog.provide('foo'); foo = 0;",
         "var foo = 0;");
    test("goog.provide('foo'); foo = {a: 0};",
         "var foo = {a: 0};");
    test("goog.provide('foo'); foo = function(){};",
         "var foo = function(){};");
    test("goog.provide('foo'); var foo = 0;",
         "var foo = 0;");
    test("goog.provide('foo'); var foo = {a: 0};",
         "var foo = {a: 0};");
    test("goog.provide('foo'); var foo = function(){};",
         "var foo = function(){};");
    test("goog.provide('foo.bar.Baz'); foo.bar.Baz=function(){};",
         "var foo={}; foo.bar={}; foo.bar.Baz=function(){};");
    test("goog.provide('foo.bar.moo'); foo.bar.moo={E:1,S:2};",
         "var foo={}; foo.bar={}; foo.bar.moo={E:1,S:2};");
    test("goog.provide('foo.bar.moo'); foo.bar.moo={E:1}; foo.bar.moo={E:2};",
         "var foo={}; foo.bar={}; foo.bar.moo={E:1}; foo.bar.moo={E:2};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvidedDeclaredFunctionError
  public void testProvidedDeclaredFunctionError() {
    test("goog.provide('foo'); function foo(){}",
         null, FUNCTION_NAMESPACE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignment1
  public void testRemovalMultipleAssignment1() {
    test("goog.provide('foo'); foo = 0; foo = 1",
         "var foo = 0; foo = 1;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignment2
  public void testRemovalMultipleAssignment2() {
    test("goog.provide('foo'); var foo = 0; foo = 1",
         "var foo = 0; foo = 1;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignment3
  public void testRemovalMultipleAssignment3() {
    test("goog.provide('foo'); foo = 0; var foo = 1",
         "foo = 0; var foo = 1;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignment4
  public void testRemovalMultipleAssignment4() {
    test("goog.provide('foo.bar'); foo.bar = 0; foo.bar = 1",
         "var foo = {}; foo.bar = 0; foo.bar = 1");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoRemovalFunction1
  public void testNoRemovalFunction1() {
    test("goog.provide('foo'); function f(){foo = 0}",
         "var foo = {}; function f(){foo = 0}");
  }
