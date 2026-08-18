// buggy code
    private void doInlinesForScope(NodeTraversal t,
        Map<Var, ReferenceCollection> referenceMap) {

      for (Iterator<Var> it = t.getScope().getVars(); it.hasNext();) {
        Var v = it.next();

        ReferenceCollection referenceInfo = referenceMap.get(v);

        // referenceInfo will be null if we're in constants-only mode
        // and the variable is not a constant.
        if (referenceInfo == null || isVarInlineForbidden(v)) {
          // Never try to inline exported variables or variables that
          // were not collected or variables that have already been inlined.
          continue;
        } else if (isInlineableDeclaredConstant(v, referenceInfo)) {
          Reference init = referenceInfo.getInitializingReferenceForConstants();
          Node value = init.getAssignedValue();
          inlineDeclaredConstant(v, value, referenceInfo.references);
          staleVars.add(v);
        } else if (mode == Mode.CONSTANTS_ONLY) {
          // If we're in constants-only mode, don't run more aggressive
          // inlining heuristics. See InlineConstantsTest.
          continue;
        } else {
          inlineNonConstants(v, referenceInfo);
        }
      }
    }

    private void inlineNonConstants(
        Var v, ReferenceCollection referenceInfo) {
      int refCount = referenceInfo.references.size();
      Reference declaration = referenceInfo.references.get(0);
      Reference init = referenceInfo.getInitializingReference();
      int firstRefAfterInit = (declaration == init) ? 2 : 3;

      if (refCount > 1 &&
          isImmutableAndWellDefinedVariable(v, referenceInfo)) {
        // if the variable is referenced more than once, we can only
        // inline it if it's immutable and never defined before referenced.
        Node value;
        if (init != null) {
          value = init.getAssignedValue();
        } else {
          // Create a new node for variable that is never initialized.
          Node srcLocation = declaration.getNameNode();
          value = NodeUtil.newUndefinedNode(srcLocation);
        }
        Preconditions.checkNotNull(value);
        inlineWellDefinedVariable(v, value, referenceInfo.references);
        staleVars.add(v);
      } else if (refCount == firstRefAfterInit) {
        // The variable likely only read once, try some more
        // complex inlining heuristics.
        Reference reference = referenceInfo.references.get(
            firstRefAfterInit - 1);
        if (canInline(declaration, init, reference)) {
          inline(v, declaration, init, reference);
          staleVars.add(v);
        }
      } else if (declaration != init && refCount == 2) {
        if (isValidDeclaration(declaration) && isValidInitialization(init)) {
          // The only reference is the initialization, remove the assignment and
          // the variable declaration.
          Node value = init.getAssignedValue();
          Preconditions.checkNotNull(value);
          inlineWellDefinedVariable(v, value, referenceInfo.references);
          staleVars.add(v);
        }
      }

      // If this variable was not inlined normally, check if we can
      // inline an alias of it. (If the variable was inlined, then the
      // reference data is out of sync. We're better off just waiting for
      // the next pass.)
      if (
          !staleVars.contains(v) && referenceInfo.isWellDefined() &&
          referenceInfo.isAssignedOnceInLifetime()) {
        List<Reference> refs = referenceInfo.references;
        for (int i = 1 /* start from a read */; i < refs.size(); i++) {
          Node nameNode = refs.get(i).getNameNode();
          if (aliasCandidates.containsKey(nameNode)) {
            AliasCandidate candidate = aliasCandidates.get(nameNode);
            if (!staleVars.contains(candidate.alias) &&
                !isVarInlineForbidden(candidate.alias)) {
              Reference aliasInit;
              aliasInit = candidate.refInfo.getInitializingReference();
              Node value = aliasInit.getAssignedValue();
              Preconditions.checkNotNull(value);
              inlineWellDefinedVariable(candidate.alias,
                  value,
                  candidate.refInfo.references);
              staleVars.add(candidate.alias);
            }
          }
        }
      }
    }

  public void visit(NodeTraversal t, Node n, Node parent) {
    if (n.getType() == Token.NAME) {
      Var v = t.getScope().getVar(n.getString());
      if (v != null && varFilter.apply(v)) {
        addReference(t, v,
            new Reference(n, parent, t, blockStack.peek()));
      }
    }

    if (isBlockBoundary(n, parent)) {
      blockStack.pop();
    }
  }

    public String toString() {
      return "Scope.Var " + name;
    }

  public Var getVar(String name) {
    Var var = vars.get(name);
    if (var != null) {
      return var;
    } else if (parent != null) { // Recurse up the parent Scope
      return parent.getVar(name);
    } else {
      return null;
    }
  }

// relevant test
// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous6
  public void testAnonymous6() {
    testSame("function foo(){}" +
             "function bar(){}" +
             "foo(function(){externfoo = bar});");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous7
  public void testAnonymous7() {
    testSame("var foo;" +
             "(function (){ function bar(){ externfoo = foo; } bar(); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous8
  public void testAnonymous8() {
    testSame("var foo;" +
             "(function (){ var g=function(){ externfoo = foo; }; g(); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous9
  public void testAnonymous9() {
    testSame("function foo(){}" +
             "function bar(){}" +
             "foo(function(){ function baz(){ externfoo = bar; } baz(); });");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFunctions1
  public void testFunctions1() {
    testSame("var foo = null; function baz() {}" +
             "function bar() {foo=baz();} bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFunctions2
  public void testFunctions2() {
    testSame("var foo; foo = function() {var a = bar()};" +
             "var bar = function(){}; foo();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElem1
  public void testGetElem1() {
    testSame("var foo = {}; foo.bar = {}; foo.bar.baz = {a: 5, b: 10};" +
             "var fn = function() {window[foo.bar.baz.a] = 5;}; fn()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElem2
  public void testGetElem2() {
    testSame("var foo = {}; foo.bar = {}; foo.bar.baz = {a: 5, b: 10};" +
             "var fn = function() {this[foo.bar.baz.a] = 5;}; fn()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElem3
  public void testGetElem3() {
    testSame("var foo = {'i': 0, 'j': 1}; foo['k'] = 2; top.foo = foo;");
  }

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
    test("var cond = false;do {var a = 1} while (cond)",
         "var cond = false;do {} while (cond)");
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
         "var j = 0; f(); g(); h(); for (; 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct4
  public void testSetterInForStruct4() {
    test("var i = 0;var j = 0; for (i = 1 + f() + g() + h(); i = 0; j++);",
         "var j = 0; f(); g(); h(); for (; 0; j++);");
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

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct9
  public void testSetterInForStruct9() {
    test("var Class = function() {}; " +
         "for (var i = 1; Class.property_ = 0; i++);",
         "for (var i = 1; 0; i++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct10
  public void testSetterInForStruct10() {
    test("var Class = function() {}; " +
         "for (var i = 1; Class.property_ = 0; i = 2);",
         "for (; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct11
  public void testSetterInForStruct11() {
    test("var Class = function() {}; " +
         "for (;Class.property_ = 0;);",
         "for (;0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct12
  public void testSetterInForStruct12() {
    test("var a = 1; var Class = function() {}; " +
         "for (;Class.property_ = a;);",
         "var a = 1; for (; a;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct13
  public void testSetterInForStruct13() {
    test("var a = 1; var Class = function() {}; " +
         "for (Class.property_ = a; 0 ;);",
         "for (; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct14
  public void testSetterInForStruct14() {
    test("var a = 1; var Class = function() {}; " +
         "for (; 0; Class.property_ = a);",
         "for (; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct15
  public void testSetterInForStruct15() {
    test("var Class = function() {}; " +
         "for (var i = 1; 0; Class.prototype.property_ = 0);",
         "for (; 0; 0);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct16
  public void testSetterInForStruct16() {
    test("var Class = function() {}; " +
         "for (var i = 1; i = 0; Class.prototype.property_ = 0);",
         "for (; 0; 0);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn1
  public void testSetterInForIn1() {
    test("var foo = {}; var bar; for(e in bar = foo.a);",
         "var foo = {}; for(e in foo.a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn2
  public void testSetterInForIn2() {
    testSame("var foo = {}; var bar; for(e in bar = foo.a); bar");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn3
  public void testSetterInForIn3() {
    
    
    test("var foo = {}; var bar; for(e in bar = foo.a); bar.b = 3",
         "var foo = {}; for(e in foo.a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn4
  public void testSetterInForIn4() {
    
    
    test("var foo = {}; var bar; for (e in bar = foo.a); bar.b = 3; foo.a",
         "var foo = {}; for (e in foo.a); foo.a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn5
  public void testSetterInForIn5() {
    
    
    test("var foo = {}; var bar; for (e in foo.a) { bar = e } bar.b = 3; foo.a",
         "var foo={};for(e in foo.a);foo.a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn6
  public void testSetterInForIn6() {
    testSame("var foo = {};for(e in foo);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInIfPredicate
  public void testSetterInIfPredicate() {
    
    testSame("var a = 1;" +
             "var Class = function() {}; " +
             "if (Class.property_ = a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInWhilePredicate
  public void testSetterInWhilePredicate() {
    test("var a = 1;" +
         "var Class = function() {}; " +
         "while (Class.property_ = a);",
         "var a = 1; for (;a;) {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInDoWhilePredicate
  public void testSetterInDoWhilePredicate() {
    
    testSame("var a = 1;" +
             "var Class = function() {}; " +
             "do {} while(Class.property_ = a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInSwitchInput
  public void testSetterInSwitchInput() {
    
    testSame("var a = 1;" +
             "var Class = function() {}; " +
             "switch (Class.property_ = a) {" +
             "  default:" +
             "}");
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
    test("function f() { win['x' + this.id] = function(a){}; }",
         "function f() { win['x' + this.id] = function $(a){}; }");

    
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
    test("function f() {win['x' + this.id] = function(a){};}",
         "function f() {win['x' + this.id] = function $win$x$this$id$(a){};}");
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

// com.google.javascript.jscomp.NodeIteratorsTest::testFunctionExpression
  public void testFunctionExpression() {
    testVarMotionWithCode("var X = 3, Y = function() {}; 3;",
        Token.NAME, Token.VAR, Token.NUMBER, Token.EXPR_RESULT, Token.SCRIPT);
  }

// com.google.javascript.jscomp.NodeIteratorsTest::testFunctionExpression2
  public void testFunctionExpression2() {
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
    assertNotLiteral(getNode("void foo()"));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetBooleanValue
  public void testGetBooleanValue() {
    assertBooleanTrue("true");
    assertBooleanTrue("10");
    assertBooleanTrue("'0'");
    assertBooleanTrue("/a/");
    assertBooleanTrue("{}");
    assertBooleanTrue("[]");
    assertBooleanFalse("false");
    assertBooleanFalse("null");
    assertBooleanFalse("0");
    assertBooleanFalse("''");
    assertBooleanFalse("undefined");
    assertBooleanFalse("void 0");
    assertBooleanFalse("void foo()");
    assertBooleanUnknown("b");
    assertBooleanUnknown("-'0.0'");

    
    assertBooleanUnknown("{a:foo()}");
    assertBooleanUnknown("[foo()]");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetExpressionBooleanValue
  public void testGetExpressionBooleanValue() {
    assertExpressionBooleanTrue("a=true");
    assertExpressionBooleanFalse("a=false");

    assertExpressionBooleanTrue("a=(false,true)");
    assertExpressionBooleanFalse("a=(true,false)");

    assertExpressionBooleanTrue("a=(false || true)");
    assertExpressionBooleanFalse("a=(true && false)");

    assertExpressionBooleanTrue("a=!(true && false)");

    assertExpressionBooleanTrue("a,true");
    assertExpressionBooleanFalse("a,false");

    assertExpressionBooleanTrue("true||false");
    assertExpressionBooleanFalse("false||false");

    assertExpressionBooleanTrue("true&&true");
    assertExpressionBooleanFalse("true&&false");

    assertExpressionBooleanFalse("!true");
    assertExpressionBooleanTrue("!false");
    assertExpressionBooleanTrue("!''");

    
    assertExpressionBooleanUnknown("a *= 2");

    
    
    assertExpressionBooleanUnknown("2 + 2");

    assertExpressionBooleanTrue("a=1");
    assertExpressionBooleanTrue("a=/a/");
    assertExpressionBooleanTrue("a={}");

    assertExpressionBooleanTrue("true");
    assertExpressionBooleanTrue("10");
    assertExpressionBooleanTrue("'0'");
    assertExpressionBooleanTrue("/a/");
    assertExpressionBooleanTrue("{}");
    assertExpressionBooleanTrue("[]");
    assertExpressionBooleanFalse("false");
    assertExpressionBooleanFalse("null");
    assertExpressionBooleanFalse("0");
    assertExpressionBooleanFalse("''");
    assertExpressionBooleanFalse("undefined");
    assertExpressionBooleanFalse("void 0");
    assertExpressionBooleanFalse("void foo()");

    assertExpressionBooleanTrue("a?true:true");
    assertExpressionBooleanFalse("a?false:false");
    assertExpressionBooleanUnknown("a?true:false");
    assertExpressionBooleanUnknown("a?true:foo()");

    assertExpressionBooleanUnknown("b");
    assertExpressionBooleanUnknown("-'0.0'");

    assertExpressionBooleanTrue("{a:foo()}");
    assertExpressionBooleanTrue("[foo()]");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetStringValue
  public void testGetStringValue() {
    assertEquals("true", NodeUtil.getStringValue(getNode("true")));
    assertEquals("10", NodeUtil.getStringValue(getNode("10")));
    assertEquals("1", NodeUtil.getStringValue(getNode("1.0")));
    assertEquals("0", NodeUtil.getStringValue(getNode("'0'")));
    assertEquals(null, NodeUtil.getStringValue(getNode("/a/")));
    assertEquals("[object Object]", NodeUtil.getStringValue(getNode("{}")));
    assertEquals("", NodeUtil.getStringValue(getNode("[]")));
    assertEquals("false", NodeUtil.getStringValue(getNode("false")));
    assertEquals("null", NodeUtil.getStringValue(getNode("null")));
    assertEquals("0", NodeUtil.getStringValue(getNode("0")));
    assertEquals("", NodeUtil.getStringValue(getNode("''")));
    assertEquals("undefined", NodeUtil.getStringValue(getNode("undefined")));
    assertEquals("undefined", NodeUtil.getStringValue(getNode("void 0")));
    assertEquals("undefined", NodeUtil.getStringValue(getNode("void foo()")));

    assertEquals("NaN", NodeUtil.getStringValue(getNode("NaN")));
    assertEquals("Infinity", NodeUtil.getStringValue(getNode("Infinity")));
    assertEquals(null, NodeUtil.getStringValue(getNode("x")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetArrayStringValue
  public void testGetArrayStringValue() {
    assertEquals("", NodeUtil.getStringValue(getNode("[]")));
    assertEquals("", NodeUtil.getStringValue(getNode("['']")));
    assertEquals("", NodeUtil.getStringValue(getNode("[null]")));
    assertEquals("", NodeUtil.getStringValue(getNode("[undefined]")));
    assertEquals("", NodeUtil.getStringValue(getNode("[void 0]")));
    assertEquals("NaN", NodeUtil.getStringValue(getNode("[NaN]")));
    assertEquals(",", NodeUtil.getStringValue(getNode("[,'']")));
    assertEquals(",,", NodeUtil.getStringValue(getNode("[[''],[''],['']]")));
    assertEquals("1,2", NodeUtil.getStringValue(getNode("[[1.0],[2.0]]")));
    assertEquals(null, NodeUtil.getStringValue(getNode("[a]")));
    assertEquals(null, NodeUtil.getStringValue(getNode("[1,a]")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsObjectLiteralKey1
  public void testIsObjectLiteralKey1() throws Exception {
    testIsObjectLiteralKey(
      parseExpr("({})"), false);
    testIsObjectLiteralKey(
      parseExpr("a"), false);
    testIsObjectLiteralKey(
      parseExpr("'a'"), false);
    testIsObjectLiteralKey(
      parseExpr("1"), false);
    testIsObjectLiteralKey(
      parseExpr("({a: 1})").getFirstChild(), true);
    testIsObjectLiteralKey(
      parseExpr("({1: 1})").getFirstChild(), true);
    testIsObjectLiteralKey(
      parseExpr("({get a(){}})").getFirstChild(), true);
    testIsObjectLiteralKey(
      parseExpr("({set a(b){}})").getFirstChild(), true);
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetFunctionName1
  public void testGetFunctionName1() throws Exception {
    Compiler compiler = new Compiler();
    Node parent = compiler.parseTestCode("function name(){}");

    testGetFunctionName(parent.getFirstChild(), "name");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetFunctionName2
  public void testGetFunctionName2() throws Exception {
    Compiler compiler = new Compiler();
    Node parent = compiler.parseTestCode("var name = function(){}")
        .getFirstChild().getFirstChild();

    testGetFunctionName(parent.getFirstChild(), "name");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetFunctionName3
  public void testGetFunctionName3() throws Exception {
    Compiler compiler = new Compiler();
    Node parent = compiler.parseTestCode("qualified.name = function(){}")
        .getFirstChild().getFirstChild();

    testGetFunctionName(parent.getLastChild(), "qualified.name");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetFunctionName4
  public void testGetFunctionName4() throws Exception {
    Compiler compiler = new Compiler();
    Node parent = compiler.parseTestCode("var name2 = function name1(){}")
        .getFirstChild().getFirstChild();

    testGetFunctionName(parent.getFirstChild(), "name2");
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetFunctionName5
  public void testGetFunctionName5() throws Exception {
    Compiler compiler = new Compiler();
    Node n = compiler.parseTestCode("qualified.name2 = function name1(){}");
    Node parent = n.getFirstChild().getFirstChild();

    testGetFunctionName(parent.getLastChild(), "qualified.name2");
  }

// com.google.javascript.jscomp.NodeUtilTest::testContainsFunctionDeclaration
  public void testContainsFunctionDeclaration() {
    assertTrue(NodeUtil.containsFunction(
                   getNode("function foo(){}")));
    assertTrue(NodeUtil.containsFunction(
                   getNode("(b?function(){}:null)")));

    assertFalse(NodeUtil.containsFunction(
                   getNode("(b?foo():null)")));
    assertFalse(NodeUtil.containsFunction(
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

    assertSideEffect(false, "undefined");
    assertSideEffect(false, "void 0");
    assertSideEffect(true, "void foo()");
    assertSideEffect(false, "-Infinity");
    assertSideEffect(false, "Infinity");
    assertSideEffect(false, "NaN");

    assertSideEffect(false, "({}||[]).foo = 2;");
    assertSideEffect(false, "(true ? {} : []).foo = 2;");
    assertSideEffect(false, "({},[]).foo = 2;");
  }

// com.google.javascript.jscomp.NodeUtilTest::testObjectMethodSideEffects
  public void testObjectMethodSideEffects() {
    
    assertSideEffect(false, "o.toString()");
    assertSideEffect(false, "o.valueOf()");

    
    assertSideEffect(true, "o.watch()");
  }

// com.google.javascript.jscomp.NodeUtilTest::testRegExpSideEffect
  public void testRegExpSideEffect() {
    
    assertSideEffect(false, "/abc/gi", true);
    assertSideEffect(false, "/abc/gi", false);

    
    
    
    assertSideEffect(true, "(/abc/gi).test('')", true);
    assertSideEffect(false, "(/abc/gi).test('')", false);
    assertSideEffect(true, "(/abc/gi).test(a)", true);
    assertSideEffect(false, "(/abc/gi).test(b)", false);

    assertSideEffect(true, "(/abc/gi).exec('')", true);
    assertSideEffect(false, "(/abc/gi).exec('')", false);

    
    assertSideEffect(true, "(/abc/gi).foo('')", true);
    assertSideEffect(true, "(/abc/gi).foo('')", false);

    
    assertSideEffect(true, "''.match('a')", true);
    assertSideEffect(false, "''.match('a')", false);
    assertSideEffect(true, "''.match(/(a)/)", true);
    assertSideEffect(false, "''.match(/(a)/)", false);

    assertSideEffect(true, "''.replace('a')", true);
    assertSideEffect(false, "''.replace('a')", false);

    assertSideEffect(true, "''.search('a')", true);
    assertSideEffect(false, "''.search('a')", false);

    assertSideEffect(true, "''.split('a')", true);
    assertSideEffect(false, "''.split('a')", false);

    
    assertSideEffect(true, "''.foo('a')", true);
    assertSideEffect(true, "''.foo('a')", false);

    
    
    
    
    assertSideEffect(true, "''.match(a)", true);
    assertSideEffect(true, "''.match(a)", false);
  }

// com.google.javascript.jscomp.NodeUtilTest::testMayEffectMutableState
  public void testMayEffectMutableState() {
    assertMutableState(true, "i++");
    assertMutableState(true, "[b, [a, i++]]");
    assertMutableState(true, "i=3");
    assertMutableState(true, "[0, i=3]");
    assertMutableState(true, "b()");
    assertMutableState(true, "void b()");
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
    assertMutableState(true, "(function() { })");
    assertMutableState(true, "(function() { i++ })");
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
    assertMutableState(true, "(function(a, b) {  })");
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

// com.google.javascript.jscomp.NodeUtilTest::testIsFunctionExpression
  public void testIsFunctionExpression() {
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
    Node function = NodeUtil.newFunctionNode(
        "foo", params, body, -1, -1);
    Node actual = new Node(Token.SCRIPT);
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
        parse("function foo(){}"), Token.THIS,
            Predicates.<Node>alwaysTrue()));
    assertEquals(1, NodeUtil.getNodeTypeReferenceCount(
        parse("this"), Token.THIS,
            Predicates.<Node>alwaysTrue()));
    assertEquals(2, NodeUtil.getNodeTypeReferenceCount(
        parse("this;function foo(){}(this)"), Token.THIS,
            Predicates.<Node>alwaysTrue()));
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
            parse("function f(){var foo;}")));
    assertNodeNames(Sets.newHashSet("goo"),
        NodeUtil.getVarsDeclaredInBranch(
            parse("var goo;function f(){var foo;}")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsControlStructureCodeBlock
  public void testIsControlStructureCodeBlock() {
    Node root = parse("if (x) foo(); else boo();");
    Node ifNode = root.getFirstChild();

    Node ifCondition = ifNode.getFirstChild();
    Node ifCase = ifNode.getFirstChild().getNext();
    Node elseCase = ifNode.getLastChild();

    assertFalse(NodeUtil.isControlStructureCodeBlock(ifNode, ifCondition));
    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, ifCase));
    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, elseCase));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsFunctionExpression1
  public void testIsFunctionExpression1() {
    Node root = parse("(function foo() {})");
    Node StatementNode = root.getFirstChild();
    assertTrue(NodeUtil.isExpressionNode(StatementNode));
    Node functionNode = StatementNode.getFirstChild();
    assertTrue(NodeUtil.isFunction(functionNode));
    assertTrue(NodeUtil.isFunctionExpression(functionNode));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsFunctionExpression2
  public void testIsFunctionExpression2() {
    Node root = parse("function foo() {}");
    Node functionNode = root.getFirstChild();
    assertTrue(NodeUtil.isFunction(functionNode));
    assertFalse(NodeUtil.isFunctionExpression(functionNode));
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveChildBlock
  public void testRemoveChildBlock() {
    
    Node actual = parse("{{x()}}");

    Node outerBlockNode = actual.getFirstChild();
    Node innerBlockNode = outerBlockNode.getFirstChild();
    innerBlockNode.setIsSyntheticBlock(true);

    NodeUtil.removeChild(outerBlockNode, innerBlockNode);
    String expected = "{{}}";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveTryChild1
  public void testRemoveTryChild1() {
    
    Node actual = parse("try {foo()} catch(e) {} finally {}");

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
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveTryChild2
  public void testRemoveTryChild2() {
    
    Node actual = parse("try {foo()} catch(e) {} finally {}");

    Node tryNode = actual.getFirstChild();
    Node tryBlock = tryNode.getFirstChild();
    Node catchBlocks = tryNode.getFirstChild().getNext();

    NodeUtil.removeChild(tryNode, tryBlock);
    String expected = "try {} catch(e) {} finally {}";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveTryChild3
  public void testRemoveTryChild3() {
    
    Node actual = parse("try {foo()} catch(e) {} finally {}");

    Node tryNode = actual.getFirstChild();
    Node tryBlock = tryNode.getFirstChild();
    Node catchBlocks = tryNode.getFirstChild().getNext();
    Node catchBlock = catchBlocks.getFirstChild();
    Node finallyBlock = tryNode.getLastChild();

    NodeUtil.removeChild(catchBlocks, catchBlock);
    String expected = "try {foo()} finally {}";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveTryChild4
  public void testRemoveTryChild4() {
    
    Node actual = parse("try {foo()} catch(e) {} finally {}");

    Node tryNode = actual.getFirstChild();
    Node tryBlock = tryNode.getFirstChild();
    Node catchBlocks = tryNode.getFirstChild().getNext();
    Node catchBlock = catchBlocks.getFirstChild();
    Node finallyBlock = tryNode.getLastChild();

    NodeUtil.removeChild(tryNode, catchBlocks);
    String expected = "try {foo()} finally {}";
    String difference = parse(expected).checkTreeEquals(actual);
    if (difference != null) {
      assertTrue("Nodes do not match:\n" + difference, false);
    }
  }

// com.google.javascript.jscomp.NodeUtilTest::testRemoveTryChild5
  public void testRemoveTryChild5() {
    Node actual = parse("try {foo()} catch(e) {} finally {}");

    Node tryNode = actual.getFirstChild();
    Node tryBlock = tryNode.getFirstChild();
    Node catchBlocks = tryNode.getFirstChild().getNext();
    Node catchBlock = catchBlocks.getFirstChild();
    Node finallyBlock = tryNode.getLastChild();

    NodeUtil.removeChild(catchBlocks, catchBlock);
    String expected = "try {foo()} finally {}";
    String difference = parse(expected).checkTreeEquals(actual);
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

    assertFalse(NodeUtil.tryMergeBlock(childBlock));
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

// com.google.javascript.jscomp.NodeUtilTest::testLocalValue1
  public void testLocalValue1() throws Exception {
    
    assertFalse(testLocalValue("x"));
    assertFalse(testLocalValue("x()"));
    assertFalse(testLocalValue("this"));
    assertFalse(testLocalValue("arguments"));

    
    
    assertFalse(testLocalValue("new x()"));

    
    assertFalse(testLocalValue("(new x()).y"));
    assertFalse(testLocalValue("(new x())['y']"));

    
    assertTrue(testLocalValue("null"));
    assertTrue(testLocalValue("undefined"));
    assertTrue(testLocalValue("Infinity"));
    assertTrue(testLocalValue("NaN"));
    assertTrue(testLocalValue("1"));
    assertTrue(testLocalValue("'a'"));
    assertTrue(testLocalValue("true"));
    assertTrue(testLocalValue("false"));
    assertTrue(testLocalValue("[]"));
    assertTrue(testLocalValue("{}"));

    
    assertTrue(testLocalValue("[x]"));
    assertTrue(testLocalValue("{'a':x}"));

    
    assertTrue(testLocalValue("++x"));
    assertTrue(testLocalValue("--x"));

    
    assertFalse(testLocalValue("x++"));
    assertFalse(testLocalValue("x--"));

    
    assertTrue(testLocalValue("x=1"));
    assertFalse(testLocalValue("x=[]"));
    assertFalse(testLocalValue("x=y"));
    
    
    assertTrue(testLocalValue("x+=y"));
    assertTrue(testLocalValue("x*=y"));
    
    
    assertTrue(testLocalValue("x==y"));
    assertTrue(testLocalValue("x!=y"));
    assertTrue(testLocalValue("x>y"));
    
    assertTrue(testLocalValue("(1,2)"));
    assertTrue(testLocalValue("(x,1)"));
    assertFalse(testLocalValue("(x,y)"));

    
    assertTrue(testLocalValue("1||2"));
    assertFalse(testLocalValue("x||1"));
    assertFalse(testLocalValue("x||y"));
    assertFalse(testLocalValue("1||y"));

    
    assertTrue(testLocalValue("1&&2"));
    assertFalse(testLocalValue("x&&1"));
    assertFalse(testLocalValue("x&&y"));
    assertFalse(testLocalValue("1&&y"));

    
    assertTrue(testLocalValue("x?1:2"));
    assertFalse(testLocalValue("x?x:2"));
    assertFalse(testLocalValue("x?1:x"));
    assertFalse(testLocalValue("x?x:y"));

    
    assertTrue(testLocalValue("!y"));
    assertTrue(testLocalValue("~y"));
    assertTrue(testLocalValue("y + 1"));
    assertTrue(testLocalValue("y + z"));
    assertTrue(testLocalValue("y * z"));

    assertTrue(testLocalValue("'a' in x"));
    assertTrue(testLocalValue("typeof x"));
    assertTrue(testLocalValue("x instanceof y"));

    assertTrue(testLocalValue("void x"));
    assertTrue(testLocalValue("void 0"));

    assertFalse(testLocalValue("{}.x"));

    assertTrue(testLocalValue("{}.toString()"));
    assertTrue(testLocalValue("o.toString()"));

    assertFalse(testLocalValue("o.valueOf()"));

    assertTrue(testLocalValue("delete a.b"));
  }

// com.google.javascript.jscomp.NodeUtilTest::testLocalValue2
  public void testLocalValue2() {
    Node newExpr = getNode("new x()");
    assertFalse(NodeUtil.evaluatesToLocalValue(newExpr));

    Preconditions.checkState(newExpr.getType() == Token.NEW);
    Node.SideEffectFlags flags = new Node.SideEffectFlags();

    flags.clearAllFlags();
    newExpr.setSideEffectFlags(flags.valueOf());

    assertTrue(NodeUtil.evaluatesToLocalValue(newExpr));

    flags.clearAllFlags();
    flags.setMutatesThis();
    newExpr.setSideEffectFlags(flags.valueOf());

    assertTrue(NodeUtil.evaluatesToLocalValue(newExpr));

    flags.clearAllFlags();
    flags.setReturnsTainted();
    newExpr.setSideEffectFlags(flags.valueOf());

    assertTrue(NodeUtil.evaluatesToLocalValue(newExpr));

    flags.clearAllFlags();
    flags.setThrows();
    newExpr.setSideEffectFlags(flags.valueOf());

    assertFalse(NodeUtil.evaluatesToLocalValue(newExpr));

    flags.clearAllFlags();
    flags.setMutatesArguments();
    newExpr.setSideEffectFlags(flags.valueOf());

    assertFalse(NodeUtil.evaluatesToLocalValue(newExpr));

    flags.clearAllFlags();
    flags.setMutatesGlobalState();
    newExpr.setSideEffectFlags(flags.valueOf());

    assertFalse(NodeUtil.evaluatesToLocalValue(newExpr));
  }

// com.google.javascript.jscomp.NodeUtilTest::testCallSideEffects
  public void testCallSideEffects() {
    Node callExpr = getNode("new x().method()");
    assertTrue(NodeUtil.functionCallHasSideEffects(callExpr));

    Node newExpr = callExpr.getFirstChild().getFirstChild();
    Preconditions.checkState(newExpr.getType() == Token.NEW);
    Node.SideEffectFlags flags = new Node.SideEffectFlags();

    
    flags.clearAllFlags();
    newExpr.setSideEffectFlags(flags.valueOf());
    flags.clearAllFlags();
    callExpr.setSideEffectFlags(flags.valueOf());

    assertTrue(NodeUtil.evaluatesToLocalValue(callExpr));
    assertFalse(NodeUtil.functionCallHasSideEffects(callExpr));
    assertFalse(NodeUtil.mayHaveSideEffects(callExpr));

    
    flags.clearAllFlags();
    newExpr.setSideEffectFlags(flags.valueOf());
    flags.clearAllFlags();
    flags.setMutatesThis();
    callExpr.setSideEffectFlags(flags.valueOf());

    assertTrue(NodeUtil.evaluatesToLocalValue(callExpr));
    assertFalse(NodeUtil.functionCallHasSideEffects(callExpr));
    assertFalse(NodeUtil.mayHaveSideEffects(callExpr));

    
    flags.clearAllFlags();
    newExpr.setSideEffectFlags(flags.valueOf());
    flags.clearAllFlags();
    flags.setMutatesThis();
    flags.setReturnsTainted();
    callExpr.setSideEffectFlags(flags.valueOf());

    assertFalse(NodeUtil.evaluatesToLocalValue(callExpr));
    assertFalse(NodeUtil.functionCallHasSideEffects(callExpr));
    assertFalse(NodeUtil.mayHaveSideEffects(callExpr));

    
    flags.clearAllFlags();
    newExpr.setSideEffectFlags(flags.valueOf());
    flags.clearAllFlags();
    flags.setReturnsTainted();
    callExpr.setSideEffectFlags(flags.valueOf());

    assertFalse(NodeUtil.evaluatesToLocalValue(callExpr));
    assertFalse(NodeUtil.functionCallHasSideEffects(callExpr));
    assertFalse(NodeUtil.mayHaveSideEffects(callExpr));

    
    
    flags.clearAllFlags();
    flags.setMutatesGlobalState();
    newExpr.setSideEffectFlags(flags.valueOf());
    flags.clearAllFlags();
    callExpr.setSideEffectFlags(flags.valueOf());

    assertTrue(NodeUtil.evaluatesToLocalValue(callExpr));
    assertFalse(NodeUtil.functionCallHasSideEffects(callExpr));
    assertTrue(NodeUtil.mayHaveSideEffects(callExpr));
  }

// com.google.javascript.jscomp.NodeUtilTest::testValidDefine
  public void testValidDefine() {
    assertTrue(testValidDefineValue("1"));
    assertTrue(testValidDefineValue("-3"));
    assertTrue(testValidDefineValue("true"));
    assertTrue(testValidDefineValue("false"));
    assertTrue(testValidDefineValue("'foo'"));

    assertFalse(testValidDefineValue("x"));
    assertFalse(testValidDefineValue("null"));
    assertFalse(testValidDefineValue("undefined"));
    assertFalse(testValidDefineValue("NaN"));

    assertTrue(testValidDefineValue("!true"));
    assertTrue(testValidDefineValue("-true"));
    assertTrue(testValidDefineValue("1 & 8"));
    assertTrue(testValidDefineValue("1 + 8"));
    assertTrue(testValidDefineValue("'a' + 'b'"));

    assertFalse(testValidDefineValue("1 & foo"));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetNumberValue
  public void testGetNumberValue() {
    
    assertEquals(1.0, NodeUtil.getNumberValue(getNode("'\\uFEFF1'")));
    assertEquals(0.0, NodeUtil.getNumberValue(getNode("''")));
    assertEquals(0.0, NodeUtil.getNumberValue(getNode("' '")));
    assertEquals(0.0, NodeUtil.getNumberValue(getNode("' \\t'")));
    assertEquals(0.0, NodeUtil.getNumberValue(getNode("'+0'")));
    assertEquals(-0.0, NodeUtil.getNumberValue(getNode("'-0'")));
    assertEquals(2.0, NodeUtil.getNumberValue(getNode("'+2'")));
    assertEquals(-1.6, NodeUtil.getNumberValue(getNode("'-1.6'")));
    assertEquals(16.0, NodeUtil.getNumberValue(getNode("'16'")));
    assertEquals(16.0, NodeUtil.getNumberValue(getNode("' 16 '")));
    assertEquals(16.0, NodeUtil.getNumberValue(getNode("' 16 '")));
    assertEquals(12300.0, NodeUtil.getNumberValue(getNode("'123e2'")));
    assertEquals(12300.0, NodeUtil.getNumberValue(getNode("'123E2'")));
    assertEquals(1.23, NodeUtil.getNumberValue(getNode("'123e-2'")));
    assertEquals(1.23, NodeUtil.getNumberValue(getNode("'123E-2'")));
    assertEquals(-1.23, NodeUtil.getNumberValue(getNode("'-123e-2'")));
    assertEquals(-1.23, NodeUtil.getNumberValue(getNode("'-123E-2'")));
    assertEquals(1.23, NodeUtil.getNumberValue(getNode("'+123e-2'")));
    assertEquals(1.23, NodeUtil.getNumberValue(getNode("'+123E-2'")));
    assertEquals(12300.0, NodeUtil.getNumberValue(getNode("'+123e+2'")));
    assertEquals(12300.0, NodeUtil.getNumberValue(getNode("'+123E+2'")));

    assertEquals(15.0, NodeUtil.getNumberValue(getNode("'0xf'")));
    assertEquals(15.0, NodeUtil.getNumberValue(getNode("'0xF'")));

    
    
    assertEquals(null, NodeUtil.getNumberValue(getNode("'-0xf'")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("'-0xF'")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("'+0xf'")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("'+0xF'")));

    assertEquals(16.0, NodeUtil.getNumberValue(getNode("'0X10'")));
    assertEquals(Double.NaN, NodeUtil.getNumberValue(getNode("'0X10.8'")));
    assertEquals(77.0, NodeUtil.getNumberValue(getNode("'077'")));
    assertEquals(-77.0, NodeUtil.getNumberValue(getNode("'-077'")));
    assertEquals(-77.5, NodeUtil.getNumberValue(getNode("'-077.5'")));
    assertEquals(
        Double.NEGATIVE_INFINITY,
        NodeUtil.getNumberValue(getNode("'-Infinity'")));
    assertEquals(
        Double.POSITIVE_INFINITY,
        NodeUtil.getNumberValue(getNode("'Infinity'")));
    assertEquals(
        Double.POSITIVE_INFINITY,
        NodeUtil.getNumberValue(getNode("'+Infinity'")));
    
    assertEquals(null, NodeUtil.getNumberValue(getNode("'-infinity'")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("'infinity'")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("'+infinity'")));

    assertEquals(Double.NaN, NodeUtil.getNumberValue(getNode("'NaN'")));
    assertEquals(
        Double.NaN, NodeUtil.getNumberValue(getNode("'some unknown string'")));
    assertEquals(Double.NaN, NodeUtil.getNumberValue(getNode("'123 blah'")));

    
    assertEquals(1.0, NodeUtil.getNumberValue(getNode("1")));
    
    assertEquals(-1.0, NodeUtil.getNumberValue(getNode("-1")));
    
    assertEquals(null, NodeUtil.getNumberValue(getNode("+1")));
    assertEquals(22.0, NodeUtil.getNumberValue(getNode("22")));
    assertEquals(18.0, NodeUtil.getNumberValue(getNode("022")));
    assertEquals(34.0, NodeUtil.getNumberValue(getNode("0x22")));

    assertEquals(
        1.0, NodeUtil.getNumberValue(getNode("true")));
    assertEquals(
        0.0, NodeUtil.getNumberValue(getNode("false")));
    assertEquals(
        0.0, NodeUtil.getNumberValue(getNode("null")));
    assertEquals(
        Double.NaN, NodeUtil.getNumberValue(getNode("void 0")));
    assertEquals(
        Double.NaN, NodeUtil.getNumberValue(getNode("void f")));
    
    assertEquals(
        null, NodeUtil.getNumberValue(getNode("void f()")));
    assertEquals(
        Double.NaN, NodeUtil.getNumberValue(getNode("NaN")));
    assertEquals(
        Double.POSITIVE_INFINITY,
        NodeUtil.getNumberValue(getNode("Infinity")));
    assertEquals(
        Double.NEGATIVE_INFINITY,
        NodeUtil.getNumberValue(getNode("-Infinity")));

    
    assertEquals(null, NodeUtil.getNumberValue(getNode("infinity")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("-infinity")));

    
    assertEquals(null, NodeUtil.getNumberValue(getNode("x")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("x.y")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("1/2")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("1-2")));
    assertEquals(null, NodeUtil.getNumberValue(getNode("+1")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsNumbericResult
  public void testIsNumbericResult() {
    assertTrue(NodeUtil.isNumericResult(getNode("1")));
    assertFalse(NodeUtil.isNumericResult(getNode("true")));
    assertTrue(NodeUtil.isNumericResult(getNode("+true")));
    assertTrue(NodeUtil.isNumericResult(getNode("+1")));
    assertTrue(NodeUtil.isNumericResult(getNode("-1")));
    assertTrue(NodeUtil.isNumericResult(getNode("-Infinity")));
    assertTrue(NodeUtil.isNumericResult(getNode("Infinity")));
    assertTrue(NodeUtil.isNumericResult(getNode("NaN")));
    assertFalse(NodeUtil.isNumericResult(getNode("undefined")));
    assertFalse(NodeUtil.isNumericResult(getNode("void 0")));

    assertTrue(NodeUtil.isNumericResult(getNode("a << b")));
    assertTrue(NodeUtil.isNumericResult(getNode("a >> b")));
    assertTrue(NodeUtil.isNumericResult(getNode("a >>> b")));

    assertFalse(NodeUtil.isNumericResult(getNode("a == b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a != b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a === b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a !== b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a < b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a > b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a <= b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a >= b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a in b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a instanceof b")));

    assertFalse(NodeUtil.isNumericResult(getNode("'a'")));
    assertFalse(NodeUtil.isNumericResult(getNode("'a'+b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a+'b'")));
    assertFalse(NodeUtil.isNumericResult(getNode("a+b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a()")));
    assertFalse(NodeUtil.isNumericResult(getNode("''.a")));
    assertFalse(NodeUtil.isNumericResult(getNode("a.b")));
    assertFalse(NodeUtil.isNumericResult(getNode("a.b()")));
    assertFalse(NodeUtil.isNumericResult(getNode("a().b()")));
    assertFalse(NodeUtil.isNumericResult(getNode("new a()")));

    
    assertFalse(NodeUtil.isNumericResult(getNode("([1,2])")));
    assertFalse(NodeUtil.isNumericResult(getNode("({a:1})")));

    
    assertTrue(NodeUtil.isNumericResult(getNode("1 && 2")));
    assertTrue(NodeUtil.isNumericResult(getNode("1 || 2")));
    assertTrue(NodeUtil.isNumericResult(getNode("a ? 2 : 3")));
    assertTrue(NodeUtil.isNumericResult(getNode("a,1")));
    assertTrue(NodeUtil.isNumericResult(getNode("a=1")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testIsBooleanResult
  public void testIsBooleanResult() {
    assertFalse(NodeUtil.isBooleanResult(getNode("1")));
    assertTrue(NodeUtil.isBooleanResult(getNode("true")));
    assertFalse(NodeUtil.isBooleanResult(getNode("+true")));
    assertFalse(NodeUtil.isBooleanResult(getNode("+1")));
    assertFalse(NodeUtil.isBooleanResult(getNode("-1")));
    assertFalse(NodeUtil.isBooleanResult(getNode("-Infinity")));
    assertFalse(NodeUtil.isBooleanResult(getNode("Infinity")));
    assertFalse(NodeUtil.isBooleanResult(getNode("NaN")));
    assertFalse(NodeUtil.isBooleanResult(getNode("undefined")));
    assertFalse(NodeUtil.isBooleanResult(getNode("void 0")));

    assertFalse(NodeUtil.isBooleanResult(getNode("a << b")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a >> b")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a >>> b")));

    assertTrue(NodeUtil.isBooleanResult(getNode("a == b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a != b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a === b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a !== b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a < b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a > b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a <= b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a >= b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a in b")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a instanceof b")));

    assertFalse(NodeUtil.isBooleanResult(getNode("'a'")));
    assertFalse(NodeUtil.isBooleanResult(getNode("'a'+b")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a+'b'")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a+b")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a()")));
    assertFalse(NodeUtil.isBooleanResult(getNode("''.a")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a.b")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a.b()")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a().b()")));
    assertFalse(NodeUtil.isBooleanResult(getNode("new a()")));
    assertTrue(NodeUtil.isBooleanResult(getNode("delete a")));

    
    assertFalse(NodeUtil.isBooleanResult(getNode("([true,false])")));
    assertFalse(NodeUtil.isBooleanResult(getNode("({a:true})")));

    
    assertTrue(NodeUtil.isBooleanResult(getNode("true && false")));
    assertTrue(NodeUtil.isBooleanResult(getNode("true || false")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a ? true : false")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a,true")));
    assertTrue(NodeUtil.isBooleanResult(getNode("a=true")));
    assertFalse(NodeUtil.isBooleanResult(getNode("a=1")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testMayBeString
  public void testMayBeString() {
    assertFalse(NodeUtil.mayBeString(getNode("1")));
    assertFalse(NodeUtil.mayBeString(getNode("true")));
    assertFalse(NodeUtil.mayBeString(getNode("+true")));
    assertFalse(NodeUtil.mayBeString(getNode("+1")));
    assertFalse(NodeUtil.mayBeString(getNode("-1")));
    assertFalse(NodeUtil.mayBeString(getNode("-Infinity")));
    assertFalse(NodeUtil.mayBeString(getNode("Infinity")));
    assertFalse(NodeUtil.mayBeString(getNode("NaN")));
    assertFalse(NodeUtil.mayBeString(getNode("undefined")));
    assertFalse(NodeUtil.mayBeString(getNode("void 0")));
    assertFalse(NodeUtil.mayBeString(getNode("null")));

    assertFalse(NodeUtil.mayBeString(getNode("a << b")));
    assertFalse(NodeUtil.mayBeString(getNode("a >> b")));
    assertFalse(NodeUtil.mayBeString(getNode("a >>> b")));

    assertFalse(NodeUtil.mayBeString(getNode("a == b")));
    assertFalse(NodeUtil.mayBeString(getNode("a != b")));
    assertFalse(NodeUtil.mayBeString(getNode("a === b")));
    assertFalse(NodeUtil.mayBeString(getNode("a !== b")));
    assertFalse(NodeUtil.mayBeString(getNode("a < b")));
    assertFalse(NodeUtil.mayBeString(getNode("a > b")));
    assertFalse(NodeUtil.mayBeString(getNode("a <= b")));
    assertFalse(NodeUtil.mayBeString(getNode("a >= b")));
    assertFalse(NodeUtil.mayBeString(getNode("a in b")));
    assertFalse(NodeUtil.mayBeString(getNode("a instanceof b")));

    assertTrue(NodeUtil.mayBeString(getNode("'a'")));
    assertTrue(NodeUtil.mayBeString(getNode("'a'+b")));
    assertTrue(NodeUtil.mayBeString(getNode("a+'b'")));
    assertTrue(NodeUtil.mayBeString(getNode("a+b")));
    assertTrue(NodeUtil.mayBeString(getNode("a()")));
    assertTrue(NodeUtil.mayBeString(getNode("''.a")));
    assertTrue(NodeUtil.mayBeString(getNode("a.b")));
    assertTrue(NodeUtil.mayBeString(getNode("a.b()")));
    assertTrue(NodeUtil.mayBeString(getNode("a().b()")));
    assertTrue(NodeUtil.mayBeString(getNode("new a()")));

    
    assertFalse(NodeUtil.mayBeString(getNode("1 && 2")));
    assertFalse(NodeUtil.mayBeString(getNode("1 || 2")));
    assertFalse(NodeUtil.mayBeString(getNode("1 ? 2 : 3")));
    assertFalse(NodeUtil.mayBeString(getNode("1,2")));
    assertFalse(NodeUtil.mayBeString(getNode("a=1")));
    assertFalse(NodeUtil.mayBeString(getNode("1+1")));
    assertFalse(NodeUtil.mayBeString(getNode("true+true")));
    assertFalse(NodeUtil.mayBeString(getNode("null+null")));
    assertFalse(NodeUtil.mayBeString(getNode("NaN+NaN")));

    
    assertTrue(NodeUtil.mayBeString(getNode("([1,2])")));
    assertTrue(NodeUtil.mayBeString(getNode("({a:1})")));
    assertTrue(NodeUtil.mayBeString(getNode("({}+1)")));
    assertTrue(NodeUtil.mayBeString(getNode("(1+{})")));
    assertTrue(NodeUtil.mayBeString(getNode("([]+1)")));
    assertTrue(NodeUtil.mayBeString(getNode("(1+[])")));
  }

// com.google.javascript.jscomp.NodeUtilTest::testGetNearestFunctionName
  public void testGetNearestFunctionName() {
    testFunctionName("function a() {}", "a");
    testFunctionName("(function a() {})", "a");
    testFunctionName("({a:function () {}})", "a");
    testFunctionName("({get a() {}})", "a");
    testFunctionName("({set a(b) {}})", "a");
    testFunctionName("({set a(b) {}})", "a");
    testFunctionName("({1:function () {}})", "1");
    testFunctionName("var a = function a() {}", "a");
    testFunctionName("var a;a = function a() {}", "a");
    testFunctionName("var o;o.a = function a() {}", "o.a");
    testFunctionName("this.a = function a() {}", "this.a");
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

// com.google.javascript.jscomp.NormalizeTest::testForIn
  public void testForIn() {
    
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
