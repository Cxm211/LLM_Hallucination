// buggy code
  public Iterator<Var> getVars() {
    return vars.values().iterator();
  }

    public void enterScope(NodeTraversal t) {
      Node n = t.getCurrentNode().getParent();
      if (n != null && isCallToScopeMethod(n)) {
        transformation = transformationHandler.logAliasTransformation(
            n.getSourceFileName(), getSourceRegion(n));
      }
    }

    private void report(NodeTraversal t, Node n, DiagnosticType error,
        String... arguments) {
      compiler.report(t.makeError(n, error, arguments));
      hasErrors = true;
    }

    public void visit(NodeTraversal t, Node n, Node parent) {
      if (isCallToScopeMethod(n)) {
        validateScopeCall(t, n, n.getParent());
      }



      // Validate the top level of the goog.scope block.
      if (t.getScopeDepth() == 2) {
        int type = n.getType();
        if (type == Token.NAME && parent.getType() == Token.VAR) {
          if (n.hasChildren() && n.getFirstChild().isQualifiedName()) {
            String name = n.getString();
            Var aliasVar = t.getScope().getVar(name);
            aliases.put(name, aliasVar);
            aliasDefinitionsInOrder.add(n);

            String qualifiedName =
                aliasVar.getInitialValue().getQualifiedName();
            transformation.addAlias(name, qualifiedName);
            // Return early, to ensure that we don't record a definition
            // twice.
            return;
          } else {
            report(t, n, GOOG_SCOPE_NON_ALIAS_LOCAL, n.getString());
          }
        }
        if (type == Token.NAME && NodeUtil.isAssignmentOp(parent) &&
            n == parent.getFirstChild()) {
            report(t, n, GOOG_SCOPE_ALIAS_REDEFINED, n.getString());
        }

        if (type == Token.RETURN) {
          report(t, n, GOOG_SCOPE_USES_RETURN);
        } else if (type == Token.THIS) {
          report(t, n, GOOG_SCOPE_REFERENCES_THIS);
        } else if (type == Token.THROW) {
          report(t, n, GOOG_SCOPE_USES_THROW);
        }
      }

      // Validate all descendent scopes of the goog.scope block.
      if (t.getScopeDepth() >= 2) {
        // Check if this name points to an alias.
        if (n.getType() == Token.NAME) {
          String name = n.getString();
          Var aliasVar = aliases.get(name);
          if (aliasVar != null &&
              t.getScope().getVar(name) == aliasVar) {
          // Note, to support the transitive case, it's important we don't
          // clone aliasedNode here.  For example,
          // var g = goog; var d = g.dom; d.createElement('DIV');
          // The node in aliasedNode (which is "g") will be replaced in the
          // changes pass above with "goog".  If we cloned here, we'd end up
          // with <code>g.dom.createElement('DIV')</code>.
          Node aliasedNode = aliasVar.getInitialValue();
          aliasUsages.add(new AliasedNode(n, aliasedNode));
          }
        }

        JSDocInfo info = n.getJSDocInfo();
        if (info != null) {
          for (Node node : info.getTypeNodes()) {
            fixTypeNode(node);
          }
        }

        // TODO(robbyw): Error for goog.scope not at root.
      }
    }

// relevant test
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

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeInstanceOfGetElem
  public void testDoNotChangeInstanceOfGetElem() {
    testSame("var goog = {};" +
        "function f(obj, name) {" +
        "  if (obj instanceof goog[name]) {" +
        "    return name;" +
        "  }" +
        "}" +
        "window['f'] = f;");
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

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithOr1
  public void testAssignWithOr1() {
    testSame("var foo = null;" +
        "var f = window.a || function () {return foo}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithOr2
  public void testAssignWithOr2() {
    test("var foo = null;" +
        "var f = window.a || function () {return foo};",
        "var foo = null"); 
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithAnd1
  public void testAssignWithAnd1() {
    testSame("var foo = null;" +
        "var f = window.a && function () {return foo}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithAnd2
  public void testAssignWithAnd2() {
    test("var foo = null;" +
        "var f = window.a && function () {return foo};",
        "var foo = null;");  
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook1
  public void testAssignWithHook1() {
    testSame("function Foo(){} var foo = null;" +
        "var f = window.a ? " +
        "    function () {return new Foo()} : function () {return foo}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook2
  public void testAssignWithHook2() {
    test("function Foo(){} var foo = null;" +
        "var f = window.a ? " +
        "    function () {return new Foo()} : function () {return foo};",
        "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook3
  public void testAssignWithHook3() {
    testSame("function Foo(){} var foo = null; var f = {};" +
        "f.b = window.a ? " +
        "    function () {return new Foo()} : function () {return foo}; f.b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook4
  public void testAssignWithHook4() {
    test("function Foo(){} var foo = null; var f = {};" +
        "f.b = window.a ? " +
        "    function () {return new Foo()} : function () {return foo};",
        "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook5
  public void testAssignWithHook5() {
    testSame("function Foo(){} var foo = null; var f = {};" +
        "f.b = window.a ? function () {return new Foo()} :" +
        "    window.b ? function () {return foo} :" +
        "    function() { return Foo }; f.b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook6
  public void testAssignWithHook6() {
    test("function Foo(){} var foo = null; var f = {};" +
        "f.b = window.a ? function () {return new Foo()} :" +
        "    window.b ? function () {return foo} :" +
        "    function() { return Foo };",
        "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign1
  public void testNestedAssign1() {
    test("var a, b = a = 1, c = 2", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign2
  public void testNestedAssign2() {
    test("var a, b = a = 1; foo(b)",
         "var b = 1; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign3
  public void testNestedAssign3() {
    test("var a, b = a = 1; a = b = 2; foo(b)",
         "var b = 1; b = 2; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign4
  public void testNestedAssign4() {
    test("var a, b = a = 1; b = a = 2; foo(b)",
         "var b = 1; b = 2; foo(b)");
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

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias1
  public void testPreservePropertyMutationsToAlias1() {
    
    
    
    testSame("var a = {}; var b = a; b.x = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias2
  public void testPreservePropertyMutationsToAlias2() {
    
    test("var a = {}; var b = a; var c = a; b.x = 1; a",
         "var a = {}; var b = a; b.x = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias3
  public void testPreservePropertyMutationsToAlias3() {
    
    testSame("var a = {}; var b = a; var c = b; c.x = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias4
 public void testPreservePropertyMutationsToAlias4() {
    
    testSame("var a = {}; var b = a; b['x'] = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias5
  public void testPreservePropertyMutationsToAlias5() {
    
    testSame("function testCall(o){}" +
             "var DATA = {'prop': 'foo','attr': {}};" +
             "var SUBDATA = DATA['attr'];" +
             "SUBDATA['subprop'] = 'bar';" +
             "testCall(DATA);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias6
  public void testPreservePropertyMutationsToAlias6() {
    
    testSame("function testCall(o){}" +
             "var DATA = {'prop': 'foo','attr': {}};" +
             "var SUBDATA = DATA['attr'];" +
             "var SUBSUBDATA = SUBDATA['subprop'];" +
             "SUBSUBDATA['subsubprop'] = 'bar';" +
             "testCall(DATA);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias7
  public void testPreservePropertyMutationsToAlias7() {
    
    test("var a = {}; var b = {}; b.x = 0;" +
         "var goog = {}; goog.inherits(b, a); a",
         "var a = {}; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias8
  public void testPreservePropertyMutationsToAlias8() {
    
    test("var a = {};" +
         "var b = {}; b.x = 0;" +
         "var c = {}; c.y = 0;" +
         "var goog = {}; goog.inherits(b, a); goog.inherits(c, a); c",
         "var a = {}; var c = {}; c.y = 0;" +
         "var goog = {}; goog.inherits(c, a); c");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPreservePropertyMutationsToAlias9
  public void testPreservePropertyMutationsToAlias9() {
    testSame("var a = {b: {}};" +
         "var c = a.b; c.d = 3;" +
         "a.d = 3; a.d;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveAlias
  public void testRemoveAlias() {
    test("var a = {b: {}};" +
         "var c = a.b;" +
         "a.d = 3; a.d;",
         "var a = {b: {}}; a.d = 3; a.d;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSingletonGetter1
  public void testSingletonGetter1() {
    test("function Foo() {} goog.addSingletonGetter(Foo);", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSingletonGetter2
  public void testSingletonGetter2() {
    test("function Foo() {} goog$addSingletonGetter(Foo);", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSingletonGetter3
  public void testSingletonGetter3() {
    
    testSame("function Foo() {} goog$addSingletonGetter(Foo);" +
        "this.x = Foo.getInstance();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveWindowPropertyAlias1
  public void testNoRemoveWindowPropertyAlias1() {
     testSame(
         "var self_ = window.gbar;\n" +
         "self_.qs = function() {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveWindowPropertyAlias2
  public void testNoRemoveWindowPropertyAlias2() {
    testSame(
        "var self_ = window;\n" +
        "self_.qs = function() {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveWindowPropertyAlias3
  public void testNoRemoveWindowPropertyAlias3() {
    testSame(
        "var self_ = window;\n" +
        "self_['qs'] = function() {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAlias0
  public void testNoRemoveAlias0() {
    testSame(
        "var x = {}; function f() { return x; }; " +
        "f().style.display = 'block';" +
        "alert(x.style)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAlias1
  public void testNoRemoveAlias1() {
    testSame(
        "var x = {}; function f() { return x; };" +
        "var map = f();\n" +
        "map.style.display = 'block';" +
        "alert(x.style)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAlias2
  public void testNoRemoveAlias2() {
    testSame(
        "var x = {};" +
        "var map = (function () { return x; })();\n" +
        "map.style = 'block';" +
        "alert(x.style)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAlias3
  public void testNoRemoveAlias3() {
    testSame(
        "var x = {}; function f() { return x; };" +
        "var map = {}\n" +
        "map[1] = f();\n" +
        "map[1].style.display = 'block';");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAliasOfExternal0
  public void testNoRemoveAliasOfExternal0() {
    testSame(
        "document.getElementById('foo').style.display = 'block';");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAliasOfExternal1
  public void testNoRemoveAliasOfExternal1() {
    testSame(
        "var map = document.getElementById('foo');\n" +
        "map.style.display = 'block';");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAliasOfExternal2
  public void testNoRemoveAliasOfExternal2() {
    testSame(
        "var map = {}\n" +
        "map[1] = document.getElementById('foo');\n" +
        "map[1].style.display = 'block';");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveThrowReference1
  public void testNoRemoveThrowReference1() {
    testSame(
      "var e = {}\n" +
      "throw e;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveThrowReference2
  public void testNoRemoveThrowReference2() {
    testSame(
      "function e() {}\n" +
      "throw new e();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testClassDefinedInObjectLit1
  public void testClassDefinedInObjectLit1() {
    test(
      "var data = {Foo: function() {}};" +
      "data.Foo.prototype.toString = function() {};",
      "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testClassDefinedInObjectLit2
  public void testClassDefinedInObjectLit2() {
    test(
      "var data = {}; data.bar = {Foo: function() {}};" +
      "data.bar.Foo.prototype.toString = function() {};",
      "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testClassDefinedInObjectLit3
  public void testClassDefinedInObjectLit3() {
    test(
      "var data = {bar: {Foo: function() {}}};" +
      "data.bar.Foo.prototype.toString = function() {};",
      "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testClassDefinedInObjectLit4
  public void testClassDefinedInObjectLit4() {
    test(
      "var data = {};" +
      "data.baz = {bar: {Foo: function() {}}};" +
      "data.baz.bar.Foo.prototype.toString = function() {};",
      "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testVarReferencedInClassDefinedInObjectLit1
  public void testVarReferencedInClassDefinedInObjectLit1() {
    testSame(
      "var ref = 3;" +
      "var data = {Foo: function() { this.x = ref; }};" +
      "window.Foo = data.Foo;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testVarReferencedInClassDefinedInObjectLit2
  public void testVarReferencedInClassDefinedInObjectLit2() {
    testSame(
      "var ref = 3;" +
      "var data = {Foo: function() { this.x = ref; }," +
      "            Bar: function() {}};" +
      "window.Bar = data.Bar;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testArrayExt
  public void testArrayExt() {
    testSame(
      "Array.prototype.foo = function() { return 1 };" +
      "var y = [];" +
      "switch (y.foo()) {" +
      "}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testArrayAliasExt
  public void testArrayAliasExt() {
    testSame(
      "Array$X = Array;" +
      "Array$X.prototype.foo = function() { return 1 };" +
      "function Array$X() {}" +
      "var y = [];" +
      "switch (y.foo()) {" +
      "}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExternalAliasInstanceof1
  public void testExternalAliasInstanceof1() {
    test(
      "Array$X = Array;" +
      "function Array$X() {}" +
      "var y = [];" +
      "if (y instanceof Array) {}",
      "var y = [];" +
      "if (y instanceof Array) {}"
      );
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExternalAliasInstanceof2
  public void testExternalAliasInstanceof2() {
    testSame(
      "Array$X = Array;" +
      "function Array$X() {}" +
      "var y = [];" +
      "if (y instanceof Array$X) {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExternalAliasInstanceof3
  public void testExternalAliasInstanceof3() {
    testSame(
      "var b = Array;" +
      "var y = [];" +
      "if (y instanceof b) {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAliasInstanceof4
  public void testAliasInstanceof4() {
    testSame(
      "function Foo() {};" +
      "var b = Foo;" +
      "var y = new Foo();" +
      "if (y instanceof b) {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAliasInstanceof5
  public void testAliasInstanceof5() {
    
    test(
      "function Foo() {}" +
      "function Bar() {}" +
      "var b = x ? Foo : Bar;" +
      "var y = new Foo();" +
      "if (y instanceof b) {}",
      "function Foo() {}" +
      "var y = new Foo;" +
      "if (false){}");
  }

// com.google.javascript.jscomp.NodeTraversalTest::testPruningCallbackShouldTraverse1
  public void testPruningCallbackShouldTraverse1() {
    PruningCallback include =
      new PruningCallback(ImmutableSet.of(Token.SCRIPT, Token.VAR), true);

    Node script = new Node(Token.SCRIPT);
    script.setIsSyntheticBlock(true);
    assertTrue(include.shouldTraverse(null, script, null));
    assertTrue(include.shouldTraverse(null, new Node(Token.VAR), null));
    assertFalse(include.shouldTraverse(null, new Node(Token.NAME), null));
    assertFalse(include.shouldTraverse(null, new Node(Token.ADD), null));
  }

// com.google.javascript.jscomp.NodeTraversalTest::testPruningCallbackShouldTraverse2
  public void testPruningCallbackShouldTraverse2() {
    PruningCallback include =
      new PruningCallback(ImmutableSet.of(Token.SCRIPT, Token.VAR), false);

    Node script = new Node(Token.SCRIPT);
    script.setIsSyntheticBlock(true);
    assertFalse(include.shouldTraverse(null, script, null));
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
    compiler.initCompilerOptionsIfTesting();

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

// com.google.javascript.jscomp.NormalizeTest::testDuplicateVarInExterns
  public void testDuplicateVarInExterns() {
    test("var extern;",
         " var extern = 3;", "var extern = 3;",
         null, null);
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

// com.google.javascript.jscomp.NormalizeTest::testForIn1
  public void testForIn1() {
    
    testSame("for(a in b) foo();");

    
    test("for(var a in b) foo()",
         "var a; for(a in b) foo()");

    
    test("a:for(var a in b) foo()",
         "var a; a:for(a in b) foo()");
    
    test("a:b:for(var a in b) foo()",
         "var a; a:b:for(a in b) foo()");

    
    test("if (x) for(var a in b) foo()",
         "if (x) { var a; for(a in b) foo() }");
  }

// com.google.javascript.jscomp.NormalizeTest::testForIn2
  public void testForIn2() {
    
    test("for(var a = foo() in b) foo()",
         "var a = foo(); for(a in b) foo()");
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
         "function f() { f(); a:{ var bar = function () {} }}");
    test("function f() { f(); {function bar() {}}}",
         "function f() { f(); {var bar = function () {}}}");
    test("function f() { f(); if (true) {function bar() {}}}",
         "function f() { f(); if (true) {var bar = function () {}}}");
  }

// com.google.javascript.jscomp.NormalizeTest::testNormalizeFunctionDeclarations
  public void testNormalizeFunctionDeclarations() throws Exception {
    testSame("function f() {}");
    testSame("var f = function () {}");
    test("var f = function f() {}",
         "var f = function f$$1() {}");
    testSame("var f = function g() {}");
    test("a:function g() {}",
         "a:{ var g = function () {} }");
    test("{function g() {}}",
         "{var g = function () {}}");
    testSame("if (function g() {}) {}");
    test("if (true) {function g() {}}",
         "if (true) {var g = function () {}}");
    test("if (true) {} else {function g() {}}",
         "if (true) {} else {var g = function () {}}");
    testSame("switch (function g() {}) {}");
    test("switch (1) { case 1: function g() {}}",
         "switch (1) { case 1: var g = function () {}}");

    testSameInFunction("function f() {}");
    testInFunction("f(); a:function g() {}",
                   "f(); a:{ var g = function () {} }");
    testInFunction("f(); {function g() {}}",
                   "f(); {var g = function () {}}");
    testInFunction("f(); if (true) {function g() {}}",
                   "f(); if (true) {var g = function () {}}");
    testInFunction("if (true) {} else {function g() {}}",
                   "if (true) {} else {var g = function () {}}");
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

    
    test("\nvar window;", "var window;");

    
    test("\nvar window;" +
         "\nvar window;", "var window;");

    
    test("function f() {var window}",
         "function f() {var window$$1}");
  }

// com.google.javascript.jscomp.NormalizeTest::testRemoveDuplicateVarDeclarations1
  public void testRemoveDuplicateVarDeclarations1() {
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

// com.google.javascript.jscomp.NormalizeTest::testRemoveDuplicateVarDeclarations2
  public void testRemoveDuplicateVarDeclarations2() {
    test("var e = 1; function f(){ try {} catch (e) {} var e = 2 }",
         "var e = 1; function f(){ try {} catch (e$$2) {} var e$$1 = 2 }");
  }

// com.google.javascript.jscomp.NormalizeTest::testRemoveDuplicateVarDeclarations3
  public void testRemoveDuplicateVarDeclarations3() {
    test("var f = 1; function f(){}",
         "f = 1; function f(){}");
    test("var f; function f(){}",
         "function f(){}");
    test("if (a) { var f = 1; } else { function f(){} }",
         "if (a) { var f = 1; } else { f = function (){} }");

    test("function f(){} var f = 1;",
         "function f(){} f = 1;");
    test("function f(){} var f;",
         "function f(){}");
    test("if (a) { function f(){} } else { var f = 1; }",
         "if (a) { var f = function (){} } else { f = 1; }");

    
    
    test("function f(){} function f(){}",
         "function f(){} function f(){}",
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
    test("if (a) { function f(){} } else { function f(){} }",
         "if (a) { var f = function (){} } else { f = function (){} }");
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

// com.google.javascript.jscomp.NormalizeTest::testIssue166a
  public void testIssue166a() {
    test("try { throw 1 } catch(e) {  var e=2 }",
         "try { throw 1 } catch(e) { var e=2 }",
         Normalize.CATCH_BLOCK_VAR_ERROR);
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166b
  public void testIssue166b() {
    test("function a() {" +
         "try { throw 1 } catch(e) {  var e=2 }" +
         "};",
         "function a() {" +
         "try { throw 1 } catch(e) { var e=2 }" +
         "}",
         Normalize.CATCH_BLOCK_VAR_ERROR);
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166c
  public void testIssue166c() {
    test("var e = 0; try { throw 1 } catch(e) {" +
             " var e=2 }",
         "var e = 0; try { throw 1 } catch(e) { var e=2 }",
         Normalize.CATCH_BLOCK_VAR_ERROR);
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166d
  public void testIssue166d() {
    test("function a() {" +
         "var e = 0; try { throw 1 } catch(e) {" +
             " var e=2 }" +
         "};",
         "function a() {" +
         "var e = 0; try { throw 1 } catch(e) { var e=2 }" +
         "}",
         Normalize.CATCH_BLOCK_VAR_ERROR);
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166e
  public void testIssue166e() {
    test("var e = 2; try { throw 1 } catch(e) {}",
         "var e = 2; try { throw 1 } catch(e$$1) {}");
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue166f
  public void testIssue166f() {
    test("function a() {" +
         "var e = 2; try { throw 1 } catch(e) {}" +
         "}",
         "function a() {" +
         "var e = 2; try { throw 1 } catch(e$$1) {}" +
         "}");
  }

// com.google.javascript.jscomp.NormalizeTest::testIssue
  public void testIssue() {
    super.allowExternsChanges(true);
    test("var a,b,c; var a,b", "a(), b()", "a(), b()", null, null);
  }

// com.google.javascript.jscomp.NormalizeTest::testNormalizeSyntheticCode
  public void testNormalizeSyntheticCode() {
    Compiler compiler = new Compiler();
    compiler.init(
        Lists.<JSSourceFile>newArrayList(),
        Lists.<JSSourceFile>newArrayList(), new CompilerOptions());
    Node code = Normalize.parseAndNormalizeSyntheticCode(
        compiler, "function f(x) {} function g(x) {}", "prefix_");
    assertEquals(
        "function f(x$$prefix_0){}function g(x$$prefix_1){}",
        compiler.toSource(code));
  }

// com.google.javascript.jscomp.NormalizeTest::testIsConstant
  public void testIsConstant() throws Exception {
    testSame("var CONST = 3; var b = CONST;");
    Node n = getLastCompiler().getRoot();

    Set<Node> constantNodes = findNodesWithProperty(n, Node.IS_CONSTANT_NAME);
    assertEquals(2, constantNodes.size());
    for (Node hasProp : constantNodes) {
      assertEquals("CONST", hasProp.getString());
    }
  }

// com.google.javascript.jscomp.NormalizeTest::testPropertyIsConstant1
  public void testPropertyIsConstant1() throws Exception {
    testSame("var a = {};a.CONST = 3; var b = a.CONST;");
    Node n = getLastCompiler().getRoot();

    Set<Node> constantNodes = findNodesWithProperty(n, Node.IS_CONSTANT_NAME);
    assertEquals(2, constantNodes.size());
    for (Node hasProp : constantNodes) {
      assertEquals("CONST", hasProp.getString());
    }
  }

// com.google.javascript.jscomp.NormalizeTest::testPropertyIsConstant2
  public void testPropertyIsConstant2() throws Exception {
    testSame("var a = {CONST: 3}; var b = a.CONST;");
    Node n = getLastCompiler().getRoot();

    Set<Node> constantNodes = findNodesWithProperty(n, Node.IS_CONSTANT_NAME);
    assertEquals(2, constantNodes.size());
    for (Node hasProp : constantNodes) {
      assertEquals("CONST", hasProp.getString());
    }
  }

// com.google.javascript.jscomp.NormalizeTest::testGetterPropertyIsConstant
  public void testGetterPropertyIsConstant() throws Exception {
    testSame("var a = { get CONST() {return 3} }; " +
             "var b = a.CONST;");
    Node n = getLastCompiler().getRoot();

    Set<Node> constantNodes = findNodesWithProperty(n, Node.IS_CONSTANT_NAME);
    assertEquals(2, constantNodes.size());
    for (Node hasProp : constantNodes) {
      assertEquals("CONST", hasProp.getString());
    }
  }

// com.google.javascript.jscomp.NormalizeTest::testSetterPropertyIsConstant
  public void testSetterPropertyIsConstant() throws Exception {
    
    testSame("var a = { set CONST(b) {throw 'invalid'} }; " +
             "var c = a.CONST;");
    Node n = getLastCompiler().getRoot();

    Set<Node> constantNodes = findNodesWithProperty(n, Node.IS_CONSTANT_NAME);
    assertEquals(2, constantNodes.size());
    for (Node hasProp : constantNodes) {
      assertEquals("CONST", hasProp.getString());
    }
  }

// com.google.javascript.jscomp.NormalizeTest::testRenamingConstantProperties
  public void testRenamingConstantProperties() {
    
    
    
    new WithCollapse().testConstantProperties();
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testNoFix
  public void testNoFix() {
    testSame("x = x");
    testSame("x = x = x");
    testSame("x = x = x(x)");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testFix
  public void testFix() {
    test("       var a,b,x; x = a[x] = b[x]",
         "var c; var a,b,x; c = a[x] = b[x], x = c");
    test("       var a,b,x; x = a[1] = x.b",
         "var c; var a,b,x; c = a[1] = x.b, x = c");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testCombinedFix
  public void testCombinedFix() {
    test("       var a,b,c, x; x = a[x] = b[x] = c[x]",
         "var d; var a,b,c, x; d = a[x] = b[x] = c[x], x = d");
    test("       var a,b,c, x; x = a[1] = b[1] = x[1]",
         "var d; var a,b,c, x; d = a[1] = b[1] = x[1], x = d");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testNestedFix1
  public void testNestedFix1() {
    test("            var a,b,c,x,y;y= x = a[x] = b[y] = c[x];",
         "var e;var d;var a,b,c,x,y;d=(e = a[x] = b[y] = c[x], x=e), y=d;");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testNestedFix2
  public void testNestedFix2() {
    test("            var a,b,c,x,y;y=a[x]= x=a[x]=b[y]=c[x];",
         "var e;var d;var a,b,c,x,y;d=a[x]=(e=a[x]=b[y]=c[x], x=e), y=d;");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testJqueryTest
  public void testJqueryTest() {
    test("       z = bar[z] = bar[z] || [];",
         "var a; a = bar[z] = bar[z] || [], z=a");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testNoCrossingScope
  public void testNoCrossingScope() {
    testSame("x = function(x) { return a[x] + b[x] }");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testForLoops
  public void testForLoops() {
    test("       var a,b,x;for(x = a[x] = b[x];;)        {}",
         "var c; var a,b,x;for(c = a[x] = b[x], x = c;;) {}");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testForInLoops
  public void testForInLoops() {
    test("       var a,b,x;for(var j in  x = a[x] = b[x])         {}",
         "var c; var a,b,x;for(var j in (c = a[x] = b[x], x = c)) {}");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testUsedInCondition
  public void testUsedInCondition() {
    test("       var a,b,x;if(x = a[x] = b[x]) {}",
         "var c; var a,b,x;if((c = a[x] = b[x], x = c)) {}");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testUsedInExpression
  public void testUsedInExpression() {
    test("       var a,b,x; FOO( x = a[x] = b[x]);",
         "var c; var a,b,x; FOO((c = a[x] = b[x], x = c));");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testLocalScope
  public void testLocalScope() {
    test("function FOO() {       var a,b,x; x = a[x] = b[x]}",
         "function FOO() {var c; var a,b,x; c = a[x] = b[x], x = c}");
    test("function FOO() {       var a,b,x; x = a[1] = x.b}",
         "function FOO() {var c; var a,b,x; c = a[1] = x.b, x = c}");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testProperNames1
  public void testProperNames1() {
    test("var a,b,c,d,x;" +
         "function f() {" +
         "  function g() { return a }" +
         "  x = a[x] = b[x];" +
         "  return g();" +
         "}",

         "var a,b,c,d,x;" +
         "function f() {" +
         "  var e;" +
         "  function g() { return a }" +
         "  e = a[x] = b[x], x = e;" +
         "  return g();" +
         "}");
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testProperNames2
  public void testProperNames2() {
    test("var a;",
         "function f() {" +
         " var b,x; x = a[x] = b[x];" +
         " return g();" +
         "}",

         "function f() {" +
         " var c;" +
         " var b,x; c = a[x] = b[x], x = c;" +
         " return g();" +
         "}", null, null);
  }

// com.google.javascript.jscomp.OperaCompoundAssignFixTest::testSaveShadowing
  public void testSaveShadowing() {
    
    test("       var a,b,x; x = a[x] = b[x];" +
         "function FOO() {       var a,b,x; x = a[x] = b[x]}",

         "var c; var a,b,x; c = a[x] = b[x], x = c;" +
         "function FOO() {var c; var a,b,x; c = a[x] = b[x], x = c}");

  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testSimple
  public void testSimple() {
    test("function foo()   { alert(arguments[0]); }",
         "function foo(p0) { alert(p0); }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testNoVarArgs
  public void testNoVarArgs() {
    testSame("function f(a,b,c) { alert(a + b + c) }");

    test("function f(a,b,c) { alert(arguments[0]) }",
         "function f(a,b,c) { alert(a) }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testMissingVarArgs
  public void testMissingVarArgs() {
    testSame("function f() { alert(arguments[x]) }");
  }

// com.google.javascript.jscomp.OptimizeArgumentsArrayTest::testArgumentRefOnNamedParameter
  public void testArgumentRefOnNamedParameter() {
    test("function f(a,b) { alert(arguments[0]) }",
         "function f(a,b) { alert(a) }");
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

// com.google.javascript.jscomp.OptimizeCallsTest::testRemovingReturnCallToFunctionWithUnusedParams
  public void testRemovingReturnCallToFunctionWithUnusedParams() {
    test("function foo() {var x; return x = bar(1)} foo(); function bar(x) {}",
         "function foo() {          bar(); return;} foo(); function bar()  {}");
  }

// com.google.javascript.jscomp.OptimizeCallsTest::testNestingFunctionCallWithUnsedParams
  public void testNestingFunctionCallWithUnsedParams() {
    test("function f1(x) { } function f2(x) { }" +
         "function f3(x) { } function f4(x) { }" +
         "f3(f1(f2()));",
         "function f1() {f2()} function f2() { }" +
         "function f3() {f1()} " +
         "f3();"
    );
  }

// com.google.javascript.jscomp.OptimizeCallsTest::testUnusedAssignOnFunctionWithUnusedParams
  public void testUnusedAssignOnFunctionWithUnusedParams() {
    test("var foo = function(a){}; function bar(){var x;x = foo} bar(); foo(1)",
         "var foo = function( ){}; function bar(){             } bar(); foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNoRemoval
  public void testNoRemoval() {
    testSame("function foo(p1) { } foo(1); foo(2)");
    testSame("function foo(p1) { } foo(1,2); foo(3,4)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testSimpleRemoval
  public void testSimpleRemoval() {
    test("function foo(p1) { } foo(); foo()",
         "function foo() {var p1;} foo(); foo()");
    test("function foo(p1) { } foo(1); foo(1)",
         "function foo() {var p1 = 1;} foo(); foo()");
    test("function foo(p1) { } foo(1,2); foo(1,4)",
         "function foo() {var p1 = 1;} foo(2); foo(4)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testNotAFunction
  public void testNotAFunction() {
    testSame("var x = 1; x; x = 2");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalNamedFunction
  public void testRemoveOneOptionalNamedFunction() {
    test("function foo(p1) { } foo()", "function foo() {var p1} foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDifferentScopes
  public void testDifferentScopes() {
    test("function f(a, b) {} f(1, 2); f(1, 3); " +
        "function h() {function g(a) {} g(4); g(5);} f(1, 2);",
        "function f(b) {var a = 1} f(2); f(3); " +
        "function h() {function g(a) {} g(4); g(5);} f(2);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testOptimizeOnlyImmutableValues
  public void testOptimizeOnlyImmutableValues() {
    test("function foo(a) {}; foo(undefined);",
         "function foo() {var a = undefined}; foo()");
    test("function foo(a) {}; foo(null);",
        "function foo() {var a = null}; foo()");
    test("function foo(a) {}; foo(1);",
         "function foo() {var a = 1}; foo()");
    test("function foo(a) {}; foo('abc');",
        "function foo() {var a = 'abc'}; foo()");

    test("var foo = function(a) {}; foo(undefined);",
         "var foo = function() {var a = undefined}; foo()");
    test("var foo = function(a) {}; foo(null);",
         "var foo = function() {var a = null}; foo()");
    test("var foo = function(a) {}; foo(1);",
         "var foo = function() {var a = 1}; foo()");
    test("var foo = function(a) {}; foo('abc');",
         "var foo = function() {var a = 'abc'}; foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalVarAssignment
  public void testRemoveOneOptionalVarAssignment() {
    test("var foo = function (p1) { }; foo()",
        "var foo = function () {var p1}; foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoOptimizeCall
  public void testDoOptimizeCall() {
    testSame("var foo = function () {}; foo(); foo.call();");
    
    testSame("var foo = function () {}; foo(); foo.call(this);");
    testSame("var foo = function (a, b) {}; foo(1); foo.call(this, 1);");
    testSame("var foo = function () {}; foo(); foo.call(null);");
    testSame("var foo = function (a, b) {}; foo(1); foo.call(null, 1);");

    testSame("var foo = function () {}; foo.call();");
    
    testSame("var foo = function () {}; foo.call(this);");
    testSame("var foo = function (a, b) {}; foo.call(this, 1);");
    testSame("var foo = function () {}; foo.call(null);");
    testSame("var foo = function (a, b) {}; foo.call(null, 1);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoOptimizeApply
  public void testDoOptimizeApply() {
    testSame("var foo = function () {}; foo(); foo.apply();");
    testSame("var foo = function () {}; foo(); foo.apply(this);");
    testSame("var foo = function (a, b) {}; foo(1); foo.apply(this, 1);");
    testSame("var foo = function () {}; foo(); foo.apply(null);");
    testSame("var foo = function (a, b) {}; foo(1); foo.apply(null, []);");

    testSame("var foo = function () {}; foo.apply();");
    testSame("var foo = function () {}; foo.apply(this);");
    testSame("var foo = function (a, b) {}; foo.apply(this, 1);");
    testSame("var foo = function () {}; foo.apply(null);");
    testSame("var foo = function (a, b) {}; foo.apply(null, []);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRemoveOneOptionalExpressionAssign
  public void testRemoveOneOptionalExpressionAssign() {
    
    
    testSame("var foo; foo = function (p1) { }; foo()");
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
    
    testSame(src);
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
    
    testSame(src);
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

// com.google.javascript.jscomp.OptimizeParametersTest::testMultipleUnknown
  public void testMultipleUnknown() {
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
    
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testSingleUnknown
  public void testSingleUnknown() {
    String src =
        "var goog2 = {};" +
        "goog2.foo = function (p1) { };" +
        "var x = getGoog();" +
        "x.foo()";

    String expected =
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
    

    test(
      "function foo(bar) {};" +
      "baz = function(a) {};" +
      "baz(1);" +
      "foo(baz);",
      "function foo() {var bar = baz};" +
      "baz = function(a) {};" +
      "baz(1);" +
      "foo();");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMethodsDefinedInArraysDontGetOptimized
  public void testMethodsDefinedInArraysDontGetOptimized() {
    String src =
        "var array = [true, function (a) {}];" +
        "array[1](1)";
    testSame(src);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMethodsDefinedInObjectDontGetOptimized
  public void testMethodsDefinedInObjectDontGetOptimized() {
    String src =
      "var object = { foo: function bar() {} };" +
      "object.foo(1)";
    testSame(src);
    src =
      "var object = { foo: function bar() {} };" +
      "object['foo'](1)";
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
    testSame("var foo = function(a,b,c) { return arguments.size }; foo(1);");
    testSame("var foo = function bar(a,b,c) { return arguments.size }; " +
             "foo(2); bar(2);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testFunctionWithTwoNames
  public void testFunctionWithTwoNames() {
    testSame("var foo = function bar(a,b) {};");
    testSame("var foo = function bar(a,b) {}; foo(1)");
    testSame("var foo = function bar(a,b) {}; bar(1);");
    testSame("var foo = function bar(a,b) {}; foo(1); foo(2)");
    testSame("var foo = function bar(a,b) {}; foo(1); bar(1)");
    testSame("var foo = function bar(a,b) {}; foo(1); bar(2)");
    testSame("var foo = function bar(a,b) {}; foo(1,2); bar(2,1)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testRecursion
  public void testRecursion() {
    test("var foo = function (a,b) {foo(1, b)}; foo(1, 2)",
         "var foo = function (b) {var a=1; foo(b)}; foo(2)");
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

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeArrayElements
  public void testDoNotOptimizeArrayElements() {
    testSame("var array = [function (a, b) {}];");
    testSame("var array = [function f(a, b) {}]");

    testSame("var array = [function (a, b) {}];" +
        "array[0](1, 2);" +
        "array[0](1);");

    testSame("var array = [];" +
        "function foo(a, b) {};" +
        "array[0] = foo;");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testOptimizeThis
  public void testOptimizeThis() {
    String src = "function foo() {" +
        "var bar = function (a, b) {};" +
        "this.bar = function (a, b) {};" +
        "this.bar(3);" +
        "bar(2);}";
    String expected = "function foo() {" +
        "var bar = function () {var b; var a = 2;};" +
        "this.bar = function () {var b; var a = 3;};" +
        "this.bar();" +
        "bar();}";
    test(src, expected);
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeWhenArgumentsPassedAsParameter
  public void testDoNotOptimizeWhenArgumentsPassedAsParameter() {
    testSame("function foo(a) {}; foo(arguments)");
    testSame("function foo(a) {}; foo(arguments[0])");

    test("function foo(a, b) {}; foo(arguments, 1)",
         "function foo(a) {var b = 1}; foo(arguments)");

    test("function foo(a, b) {}; foo(arguments)",
         "function foo(a) {var b}; foo(arguments)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeGoogExportFunctions
  public void testDoNotOptimizeGoogExportFunctions() {
    testSame("function foo(a, b) {}; foo(); goog.export_function(foo);");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeJSCompiler_renameProperty
  public void testDoNotOptimizeJSCompiler_renameProperty() {
    testSame("function JSCompiler_renameProperty(a) {return a};" +
             "JSCompiler_renameProperty('a');");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testDoNotOptimizeJSCompiler_ObjectPropertyString
  public void testDoNotOptimizeJSCompiler_ObjectPropertyString() {
    testSame("function JSCompiler_ObjectPropertyString(a, b) {return a[b]};" +
             "JSCompiler_renameProperty(window,'b');");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMutableValues1
  public void testMutableValues1() {
    test("function foo(p1) {} foo()",
         "function foo() {var p1} foo()");
    test("function foo(p1) {} foo(1)",
         "function foo() {var p1=1} foo()");
    test("function foo(p1) {} foo([])",
         "function foo() {var p1=[]} foo()");
    test("function foo(p1) {} foo({})",
         "function foo() {var p1={}} foo()");
    test("var x;function foo(p1) {} foo(x)",
         "var x;function foo() {var p1=x} foo()");
    test("var x;function foo(p1) {} foo(x())",
         "var x;function foo() {var p1=x()} foo()");
    test("var x;function foo(p1) {} foo(new x())",
         "var x;function foo() {var p1=new x()} foo()");
    test("var x;function foo(p1) {} foo('' + x)",
         "var x;function foo() {var p1='' + x} foo()");

    testSame("function foo(p1) {} foo(this)");
    testSame("function foo(p1) {} foo(arguments)");
    testSame("function foo(p1) {} foo(function(){})");
    testSame("function foo(p1) {} (function () {var x;foo(x)})()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMutableValues2
  public void testMutableValues2() {
    test("function foo(p1, p2) {} foo(1, 2)",
         "function foo() {var p1=1; var p2 = 2} foo()");
    test("var x; var y; function foo(p1, p2) {} foo(x(), y())",
         "var x; var y; function foo() {var p1=x(); var p2 = y()} foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMutableValues3
  public void testMutableValues3() {
    test(
        "var x; var y; var z;" +
        "function foo(p1, p2) {}" +
        "foo(x(), y()); foo(x(),y())",
        "var x; var y; var z;" +
        "function foo() {var p1=x(); var p2=y()}" +
        "foo(); foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMutableValues4
  public void testMutableValues4() {
    
    
    
    testSame(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "foo(x(), y(), z()); foo(x(),y(),3)");

    
    
    testSame(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "foo(x, y(), z()); foo(x,y(),3)");

    
    
    test(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "foo([], y(), z()); foo([],y(),3)",
        "var x; var y; var z;" +
        "function foo(p2, p3) {var p1=[]}" +
        "foo(y(), z()); foo(y(),3)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testMutableValues5
  public void testMutableValues5() {
    test(
        "var x; var y; var z;" +
        "function foo(p1, p2) {}" +
        "new foo(new x(), y()); new foo(new x(),y())",
        "var x; var y; var z;" +
        "function foo() {var p1=new x(); var p2=y()}" +
        "new foo(); new foo()");

    test(
        "var x; var y; var z;" +
        "function foo(p1, p2) {}" +
        "new foo(x(), y()); new foo(x(),y())",
        "var x; var y; var z;" +
        "function foo() {var p1=x(); var p2=y()}" +
        "new foo(); new foo()");

    testSame(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "new foo(x(), y(), z()); new foo(x(),y(),3)");

    testSame(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "new foo(x, y(), z()); new foo(x,y(),3)");

    test(
        "var x; var y; var z;" +
        "function foo(p1, p2, p3) {}" +
        "new foo([], y(), z()); new foo([],y(),3)",
        "var x; var y; var z;" +
        "function foo(p2, p3) {var p1=[]}" +
        "new foo(y(), z()); new foo(y(),3)");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testShadows
  public void testShadows() {
    testSame("function foo(a) {}" +
             "var x;" +
             "function f() {" +
             "  var x;" +
             "  function g() {" +
             "    foo(x());" +
             "  }" +
             "};" +
             "foo(x())");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testCrash
  public void testCrash() {
    test(
        "function foo(a) {}" +
        "foo({o:1});" +
        "foo({o:1})",
        "function foo() {var a = {o:1}}" +
        "foo();" +
        "foo()");
  }

// com.google.javascript.jscomp.OptimizeParametersTest::testGlobalCatch
  public void testGlobalCatch() {
    testSame("function foo(a) {} try {} catch (e) {foo(e)}");
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteUsedResult1
  public void testNoRewriteUsedResult1() throws Exception {
    String source = newlineJoin(
        "function a(){return 1}",
        "var x = a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteUsedResult2
  public void testNoRewriteUsedResult2() throws Exception {
    String source = newlineJoin(
        "var a = function(){return 1}",
        "a(); var b = a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult1
  public void testRewriteUnusedResult1() throws Exception {
    String source = newlineJoin(
        "function a(){return 1}",
        "a()");
    String expected = newlineJoin(
        "function a(){return}",
        "a()");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult2
  public void testRewriteUnusedResult2() throws Exception {
    String source = newlineJoin(
        "var a; a = function(){return 1}",
        "a()");
    String expected = newlineJoin(
        "var a; a = function(){return}",
        "a()");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult3
  public void testRewriteUnusedResult3() throws Exception {
    String source = newlineJoin(
        "var a = function(){return 1}",
        "a()");
    String expected = newlineJoin(
        "var a = function(){return}",
        "a()");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult4a
  public void testRewriteUnusedResult4a() throws Exception {
    String source = newlineJoin(
        "var a = function(){return a()}",
        "a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult4b
  public void testRewriteUnusedResult4b() throws Exception {
    String source = newlineJoin(
        "var a = function b(){return b()}",
        "a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult4c
  public void testRewriteUnusedResult4c() throws Exception {
    String source = newlineJoin(
        "function a(){return a()}",
        "a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult5
  public void testRewriteUnusedResult5() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return args};",
        "var o = new a;",
        "o.foo()");
    String expected = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return};",
        "var o = new a;",
        "o.foo()");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult6
  public void testRewriteUnusedResult6() throws Exception {
    String source = newlineJoin(
        "function a(){return (g = 1)}",
        "a()");
    String expected = newlineJoin(
        "function a(){g = 1;return}",
        "a()");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult7a
  public void testRewriteUnusedResult7a() throws Exception {
    String source = newlineJoin(
        "function a() { return 1 }",
        "function b() { return a() }",
        "function c() { return b() }",
        "c();");

    String expected = newlineJoin(
        "function a() { return 1 }",
        "function b() { return a() }",
        "function c() { b(); return }",
        "c();");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult7b
  public void testRewriteUnusedResult7b() throws Exception {
    String source = newlineJoin(
        "c();",
        "function c() { return b() }",
        "function b() { return a() }",
        "function a() { return 1 }");

    
    String expected = newlineJoin(
        "c();",
        "function c() { b(); return }",
        "function b() { return a() }",
        "function a() { return 1 }");
    test(source, expected);

    
    source = expected;
    expected = newlineJoin(
        "c();",
        "function c() { b(); return }",
        "function b() { a(); return }",
        "function a() { return 1 }");
    test(source, expected);

    
    source = expected;
    expected = newlineJoin(
        "c();",
        "function c() { b(); return }",
        "function b() { a(); return }",
        "function a() { return }");
    test(source, expected);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUnusedResult8
  public void testRewriteUnusedResult8() throws Exception {
    String source = newlineJoin(
        "function a() { return c() }",
        "function b() { return a() }",
        "function c() { return b() }",
        "c();");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteObjLit1
  public void testNoRewriteObjLit1() throws Exception {
    String source = newlineJoin(
        "var a = {b:function(){return 1;}}",
        "for(c in a) (a[c])();",
        "a.b()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteObjLit2
  public void testNoRewriteObjLit2() throws Exception {
    String source = newlineJoin(
        "var a = {b:function fn(){return 1;}}",
        "for(c in a) (a[c])();",
        "a.b()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testNoRewriteArrLit
  public void testNoRewriteArrLit() throws Exception {
    String source = newlineJoin(
        "var a = [function(){return 1;}]",
        "(a[0])();");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testPrototypeMethod1
  public void testPrototypeMethod1() throws Exception {
    String source = newlineJoin(
        "function c(){}",
        "c.prototype.a = function(){return 1}",
        "var x = new c;",
        "x.a()");
    String result = newlineJoin(
        "function c(){}",
        "c.prototype.a = function(){return}",
        "var x = new c;",
        "x.a()");
    test(source, result);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testPrototypeMethod2
  public void testPrototypeMethod2() throws Exception {
    String source = newlineJoin(
        "function c(){}",
        "c.prototype.a = function(){return 1}",
        "goog.reflect.object({a: 'v'})",
        "var x = new c;",
        "x.a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testPrototypeMethod3
  public void testPrototypeMethod3() throws Exception {
    String source = newlineJoin(
        "function c(){}",
        "c.prototype.a = function(){return 1}",
        "var x = new c;",
        "for(var key in goog.reflect.object({a: 'v'})){ x[key](); }",
        "x.a()");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testPrototypeMethod4
  public void testPrototypeMethod4() throws Exception {
    String source = newlineJoin(
        "function c(){}",
        "c.prototype.a = function(){return 1}",
        "var x = new c;",
        "for(var key in goog.reflect.object({a: 'v'})){ x[key](); }");
    testSame(source);
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testCallOrApply
  public void testCallOrApply() throws Exception {
    
    testSame("function a() {return 1}; a.call(new foo);");

    testSame("function a() {return 1}; a.apply(new foo);");
  }

// com.google.javascript.jscomp.OptimizeReturnsTest::testRewriteUseSiteRemoval
  public void testRewriteUseSiteRemoval() throws Exception {
    String source = newlineJoin(
        "function a() { return {\"_id\" : 1} }",
        "a();");
    String expected = newlineJoin(
        "function a() { return }",
        "a();");
    test(source, expected);
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofObject
  public void testFoldTypeofObject() {
    test("var x = {};typeof x",
         "var x = {};\"object\"");

    test("var x = [];typeof x",
         "var x = [];\"object\"");

    
    test("var x = null;typeof x",
         "var x = null;\"object\"");
  }

// com.google.javascript.jscomp.PeepholeFoldWithTypesTest::testFoldTypeofString
  public void testFoldTypeofString() {
    test("var x = \"foo\";typeof x",
         "var x = \"foo\";\"string\"");

    test("var x = new String(\"foo\");typeof x",
         "var x = new String(\"foo\");\"object\"");
  }
