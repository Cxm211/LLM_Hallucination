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
// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment1
  public void testInterfaceAssignment1() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment2
  public void testInterfaceAssignment2() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;",
        "initializing variable\n" +
        "found   : T\n" +
        "required: I");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment3
  public void testInterfaceAssignment3() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment4
  public void testInterfaceAssignment4() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment5
  public void testInterfaceAssignment5() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i1 = t;\n" +
        "var i2 = t;\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment6
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment7
  public void testInterfaceAssignment7() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i1 = t;\n" +
        "var i2 = t;\n" +
        "i1 = i2;\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment8
  public void testInterfaceAssignment8() throws Exception {
    testTypes("var I = function() {};\n" +
        "var i;\n" +
        "var o = i;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment9
  public void testInterfaceAssignment9() throws Exception {
    testTypes("var I = function() {};\n" +
        "function f() { return null; }\n" +
        "var i = f();\n",
        "initializing variable\n" +
        "found   : (I|null|undefined)\n" +
        "required: I");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment10
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment11
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment12
  public void testInterfaceAssignment12() throws Exception {
    testTypes("var I = function() {};\n" +
              "var T1 = function() {};\n" +
              "var T2 = function() {};\n" +
              "function f() { return new T2(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInterfaceAssignment13
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testGetprop1
  public void testGetprop1() throws Exception {
    testTypes("function foo(){foo().bar;}",
        "undefined has no properties\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testArrayAccess1
  public void testArrayAccess1() throws Exception {
    testTypes("var a = []; var b = a['hi'];");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testArrayAccess2
  public void testArrayAccess2() throws Exception {
    testTypes("var a = []; var b = a[[1,2]];",
        "array access\n" +
        "found   : Array\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testArrayAccess3
  public void testArrayAccess3() throws Exception {
    testTypes("var bar = [];" +
        "function baz(){};" +
        "var foo = bar[baz()];",
        "array access\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testArrayAccess4
  public void testArrayAccess4() throws Exception {
    testTypes("function foo(){};var bar = foo()[foo()];",
        "array access\n" +
        "found   : Array\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testArrayAccess6
  public void testArrayAccess6() throws Exception {
    testTypes("var bar = null[1];",
        "only arrays or objects can be accessed\n" +
        "found   : null\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testArrayAccess7
  public void testArrayAccess7() throws Exception {
    testTypes("var bar = void 0; bar[0];",
        "only arrays or objects can be accessed\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testArrayAccess8
  public void testArrayAccess8() throws Exception {
    
    
    testTypes("var bar = void 0; bar[0]; bar[1];",
        "only arrays or objects can be accessed\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropAccess
  public void testPropAccess() throws Exception {
    testTypes("var f = function(x) {\n" +
        "var o = String(x);\n" +
        "if (typeof o['a'] != 'undefined') { return o['a']; }\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropAccess2
  public void testPropAccess2() throws Exception {
    testTypes("var bar = void 0; bar.baz;",
        "undefined has no properties\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropAccess3
  public void testPropAccess3() throws Exception {
    
    
    testTypes("var bar = void 0; bar.baz; bar.bax;",
        "undefined has no properties\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropAccess4
  public void testPropAccess4() throws Exception {
    testTypes(" function f(x) { return x['hi']; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testSwitchCase1
  public void testSwitchCase1() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "switch(a){case b:;}",
        "case expression doesn't match switch\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testSwitchCase2
  public void testSwitchCase2() throws Exception {
    testTypes("var a = null; switch (typeof a) { case 'foo': }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar1
  public void testVar1() throws Exception {
    TypeCheckResult p =
        parseAndTypeCheckWithScope("var a = null");

    assertEquals(createUnionType(STRING_TYPE, NULL_TYPE),
        p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar2
  public void testVar2() throws Exception {
    testTypes(" var a = function(){}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar3
  public void testVar3() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = 3;");

    assertEquals(NUMBER_TYPE, p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar4
  public void testVar4() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var a = 3; a = 'string';");

    assertEquals(createUnionType(STRING_TYPE, NUMBER_TYPE),
        p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar5
  public void testVar5() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 'hello';" +
        "var a = goog.foo;",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar6
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar7
  public void testVar7() throws Exception {
    testTypes("var a, b;",
        "declaration of multiple variables with shared type information");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar8
  public void testVar8() throws Exception {
    testTypes("var a, b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar9
  public void testVar9() throws Exception {
    testTypes("var a;",
        "enum initializer must be an object literal or an enum");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar10
  public void testVar10() throws Exception {
    testTypes("var foo = 'abc';",
        "initializing variable\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar11
  public void testVar11() throws Exception {
    testTypes("var foo = 'abc';",
        "initializing variable\n" +
        "found   : string\n" +
        "required: Date");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar12
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar13
  public void testVar13() throws Exception {
    
    testTypes("var a,a;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar14
  public void testVar14() throws Exception {
    testTypes(" function f() { var x; return x; }",
        "inconsistent return type\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testVar15
  public void testVar15() throws Exception {
    testTypes("" +
        "function f() { var x = x || {}; return x; }",
        "inconsistent return type\n" +
        "found   : {}\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAssign1
  public void testAssign1() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 'hello';",
        "assignment to property foo of goog\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAssign2
  public void testAssign2() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 3;" +
        "goog.foo = 'hello';",
        "assignment to property foo of goog\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAssign3
  public void testAssign3() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 3;" +
        "goog.foo = 4;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAssign4
  public void testAssign4() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 3;" +
        "goog.foo = 'hello';");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAssignInference
  public void testAssignInference() throws Exception {
    testTypes(
        "" +
        "function f(x) {" +
        "  var y = null;" +
        "  y = x[0];" +
        "  if (y == null) { return 4; } else { return 6; }" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOr1
  public void testOr1() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a + b || undefined;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOr2
  public void testOr2() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "var c = a + b || undefined;",
        "initializing variable\n" +
        "found   : (number|undefined)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOr3
  public void testOr3() throws Exception {
    testTypes("var a;" +
        "var c = a || 3;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOr4
  public void testOr4() throws Exception {
     testTypes("var x;x=null || \"a\";",
         "assignment\n" +
         "found   : string\n" +
         "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOr5
  public void testOr5() throws Exception {
     testTypes("var x;x=undefined || \"a\";",
         "assignment\n" +
         "found   : string\n" +
         "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnd1
  public void testAnd1() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a + b && undefined;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnd2
  public void testAnd2() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "var c = a + b && undefined;",
        "initializing variable\n" +
        "found   : (number|undefined)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnd3
  public void testAnd3() throws Exception {
    testTypes("var a;" +
        "var c = a && undefined;",
        "initializing variable\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnd4
  public void testAnd4() throws Exception {
    testTypes("function f(x){};\n" +
        "var x; var y;\n" +
        "if (x && y) { f(y) }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnd5
  public void testAnd5() throws Exception {
    testTypes("function f(x,y){};\n" +
        "var x; var y;\n" +
        "if (x && y) { f(x, y) }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnd6
  public void testAnd6() throws Exception {
    testTypes("function f(x){};\n" +
        "var x;\n" +
        "if (x && f(x)) { f(x) }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnd7
  public void testAnd7() throws Exception {
    
    
    
    
    testTypes("var x; if (x && x) {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHook
  public void testHook() throws Exception {
    testTypes("function foo(){ var x=foo()?a:b; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHookRestrictsType1
  public void testHookRestrictsType1() throws Exception {
    testTypes("" +
        "function f() { return null;}" +
        " var a = f();" +
        "" +
        "var b = a ? a : 'default';");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHookRestrictsType2
  public void testHookRestrictsType2() throws Exception {
    testTypes("" +
        "var a = null;" +
        "" +
        "var b = a ? null : a;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHookRestrictsType3
  public void testHookRestrictsType3() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = (!a) ? a : null;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHookRestrictsType4
  public void testHookRestrictsType4() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = a != null ? a : true;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHookRestrictsType5
  public void testHookRestrictsType5() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = a == null ? a : undefined;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHookRestrictsType6
  public void testHookRestrictsType6() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = a == null ? 5 : a;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHookRestrictsType7
  public void testHookRestrictsType7() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = a == undefined ? 5 : a;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testWhileRestrictsType1
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testWhileRestrictsType2
  public void testWhileRestrictsType2() throws Exception {
    testTypes("\n" +
        "function f(x) {\nvar y = 0;" +
        "while (x) {\n" +
        "y = x;\n" +
        "x = x-1;\n}\n" +
        "return y;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHigherOrderFunctions1
  public void testHigherOrderFunctions1() throws Exception {
    testTypes(
        "var f;" +
        "f(true);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHigherOrderFunctions2
  public void testHigherOrderFunctions2() throws Exception {
    testTypes(
        "var f;" +
        "var a = f();",
        "initializing variable\n" +
        "found   : Date\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHigherOrderFunctions3
  public void testHigherOrderFunctions3() throws Exception {
    testTypes(
        "var f; new f",
        "cannot instantiate non-constructor");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testHigherOrderFunctions4
  public void testHigherOrderFunctions4() throws Exception {
    testTypes(
        "var f; new f",
        "cannot instantiate non-constructor");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias1
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias2
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias3
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias4
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias5
  public void testConstructorAlias5() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return new Foo(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias6
  public void testConstructorAlias6() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        " var FooAlias = Foo;" +
        " function foo() { " +
        "  return new FooAlias(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias7
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias8
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias9
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorAlias10
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testClosure1
  public void testClosure1() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = goog.isDef(a) ? a : 'default';",
        null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testClosure2
  public void testClosure2() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = goog.isNull(a) ? 'default' : a;",
        null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testClosure3
  public void testClosure3() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = goog.isDefAndNotNull(a) ? a : 'default';",
        null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testClosure4
  public void testClosure4() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = !goog.isDef(a) ? 'default' : a;",
        null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testClosure5
  public void testClosure5() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = !goog.isNull(a) ? a : 'default';",
        null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testClosure6
  public void testClosure6() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "var a;" +
        "" +
        "var b = !goog.isDefAndNotNull(a) ? 'default' : a;",
        null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testReturn1
  public void testReturn1() throws Exception {
    testTypes("function foo(){ return 3; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testReturn2
  public void testReturn2() throws Exception {
    testTypes("function foo(){ return; }",
        "inconsistent return type\n" +
        "found   : undefined\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testReturn3
  public void testReturn3() throws Exception {
    testTypes("function foo(){ return 'abc'; }",
        "inconsistent return type\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testReturn4
  public void testReturn4() throws Exception {
    testTypes("\n function a(){return new Array();}",
        "inconsistent return type\n" +
        "found   : Array\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testReturn5
  public void testReturn5() throws Exception {
    testTypes("function n(n){return};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testReturn6
  public void testReturn6() throws Exception {
    testTypes(
        "" +
        "function a(opt_a) { return opt_a }",
        "inconsistent return type\n" +
        "found   : (number|undefined)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testReturn7
  public void testReturn7() throws Exception {
    testTypes("var A = function() {};\n" +
        "var B = function() {};\n" +
        "A.f = function() { return 1; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: B");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testReturn8
  public void testReturn8() throws Exception {
    testTypes("var A = function() {};\n" +
        "var B = function() {};\n" +
        "A.prototype.f = function() { return 1; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: B");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis1
  public void testThis1() throws Exception {
    testTypes("var goog = {};" +
        "goog.A = function(){};" +
        "goog.A.prototype.n = " +
        "  function() { return this };",
        "inconsistent return type\n" +
        "found   : goog.A\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis2
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis3
  public void testThis3() throws Exception {
    testTypes("var goog = {};" +
        "goog.A = function(){" +
        "  this.foo = null;" +
        "  this.foo = 5;" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis4
  public void testThis4() throws Exception {
    testTypes("var goog = {};" +
        "goog.A = function(){" +
        "  this.foo = null;" +
        "};" +
        "goog.A.prototype.n = function() {" +
        "  return this.foo };",
        "inconsistent return type\n" +
        "found   : (null|string|undefined)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis5
  public void testThis5() throws Exception {
    testTypes("function h() { return this }",
        "inconsistent return type\n" +
        "found   : Date\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis6
  public void testThis6() throws Exception {
    testTypes("var goog = {};" +
        "" +
        "goog.A = function(){ return this };",
        "inconsistent return type\n" +
        "found   : goog.A\n" +
        "required: Date");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis7
  public void testThis7() throws Exception {
    testTypes("function A(){};" +
        "A.prototype.n = function() { return this };",
        "inconsistent return type\n" +
        "found   : A\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis8
  public void testThis8() throws Exception {
    testTypes("function A(){" +
        "  this.foo = null;" +
        "};" +
        "A.prototype.n = function() {" +
        "  return this.foo };",
        "inconsistent return type\n" +
        "found   : (null|string|undefined)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis9
  public void testThis9() throws Exception {
    
    testTypes("function A(){};" +
        "A.prototype.foo = 3;" +
        " A.bar = function() { return this.foo; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testThis10
  public void testThis10() throws Exception {
    
    testTypes("function A(){};" +
        "A.prototype.foo = 3;" +
        "" +
        "A.bar = function() { return this.foo; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGlobalThis1
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testGlobalThis2
  public void testGlobalThis2() throws Exception {
    testTypes(" function Bindow() {}" +
        " " +
        "Bindow.prototype.alert = function(msg) {};" +
        "this.alert = 3;" +
        "(new Bindow()).alert(this.alert)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGlobalThis3
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testGlobalThis4
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testGlobalThis5
  public void testGlobalThis5() throws Exception {
    testTypes(
        "function f() {" +
        "   " +
        "  var alert = function(msg) {};" +
        "}" +
        "this.alert(3);",
        "Property alert never defined on global this");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGlobalThis6
  public void testGlobalThis6() throws Exception {
    testTypes(
        " " +
        "var alert = function(msg) {};" +
        "var x = 3;" +
        "x = 'msg';" +
        "this.alert(this.x);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testControlFlowRestrictsType1a
  public void testControlFlowRestrictsType1a() throws Exception {
    testTypes(" function f() { return null; }\n" +
        " var a = f();\n" +
        " var b = new String('foo');\n" +
        " var c = null;\n" +
        "if (a) {\n" +
        "  b = a;\n" +
        "} else {\n" +
        "  c = a;\n" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testControlFlowRestrictsType1b
  public void testControlFlowRestrictsType1b() throws Exception {
    testTypes(" function f() { return null; }\n" +
        " var a = f();\n" +
        " var b = new String('foo');\n" +
        " var c = null;\n" +
        "if (a) {\n" +
        "  b = a;\n" +
        "} else {\n" +
        "  c = a;\n" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testControlFlowRestrictsType1c
  public void testControlFlowRestrictsType1c() throws Exception {
    testTypes("\n" +
        "function f() { return undefined; }\n" +
        " var a = f();\n" +
        " var b = new String('foo');\n" +
        " var c = undefined;\n" +
        "if (a) {\n" +
        "  b = a;\n" +
        "} else {\n" +
        "  c = a;\n" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testControlFlowRestrictsType2
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testControlFlowRestrictsType3
  public void testControlFlowRestrictsType3() throws Exception {
    testTypes("" +
        "var a;" +
        "" +
        "var b = 'foo';" +
        "if (a) {" +
        "  b = a;" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testControlFlowRestrictsType4
  public void testControlFlowRestrictsType4() throws Exception {
    testTypes(" function f(a){}" +
        " var a;" +
        "a && f(a);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testControlFlowRestrictsType5
  public void testControlFlowRestrictsType5() throws Exception {
    testTypes(" function f(a){}" +
        " var a;" +
        "a || f(a);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testControlFlowRestrictsType6
  public void testControlFlowRestrictsType6() throws Exception {
    testTypes(" function f(x) {}" +
        " var a;" +
        "a && f(a);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testControlFlowRestrictsType7
  public void testControlFlowRestrictsType7() throws Exception {
    testTypes(" function f(x) {}" +
        " var a;" +
        "a && f(a);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testControlFlowRestrictsType8
  public void testControlFlowRestrictsType8() throws Exception {
    testTypes(" function f(a){}" +
        " var a;" +
        "if (a || f(a)) {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testControlFlowRestrictsType9
  public void testControlFlowRestrictsType9() throws Exception {
    testTypes("\n" +
        "var f = function(x) {\n" +
        "if (!x || x == 1) { return 1; } else { return x; }\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testSwitchCase3
  public void testSwitchCase3() throws Exception {
    testTypes("" +
        "var a = new String('foo');" +
        "switch (a) { case 'A': }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testSwitchCase4
  public void testSwitchCase4() throws Exception {
    testTypes("" +
        "var a = 'foo';" +
        "switch (a) { case 'A':break; case null:break; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testSwitchCase5
  public void testSwitchCase5() throws Exception {
    testTypes("" +
        "var a = new String('foo');" +
        "switch (a) { case 'A':break; case null:break; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testSwitchCase6
  public void testSwitchCase6() throws Exception {
    testTypes("" +
        "var a = new Number(5);" +
        "switch (a) { case 5:break; case null:break; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testSwitchCase7
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testSwitchCase8
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testNoTypeCheck1
  public void testNoTypeCheck1() throws Exception {
    testTypes("function foo() { new 4 }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNoTypeCheck2
  public void testNoTypeCheck2() throws Exception {
    testTypes("var foo = function() { new 4 }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNoTypeCheck3
  public void testNoTypeCheck3() throws Exception {
    testTypes("var foo = function bar() { new 4 }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNoTypeCheck4
  public void testNoTypeCheck4() throws Exception {
    testTypes("var foo;" +
        "foo = function() { new 4 }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNoTypeCheck5
  public void testNoTypeCheck5() throws Exception {
    testTypes("var foo;" +
        "foo = function() { new 4 }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNoTypeCheck6
  public void testNoTypeCheck6() throws Exception {
    testTypes("var foo;" +
        "foo = function bar() { new 4 }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNoTypeCheck7
  public void testNoTypeCheck7() throws Exception {
    testTypes("var foo;" +
        "foo = function bar() { new 4 }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNoTypeCheck8
  public void testNoTypeCheck8() throws Exception {
    testTypes(" var foo;" +
        "var bar = 3;  function f(x) {} f(bar);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testImplicitCast
  public void testImplicitCast() throws Exception {
    testTypes(" function Element() {};\n" +
             "" +
             "Element.prototype.innerHTML;",
             "(new Element).innerHTML = new Array();", null, false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testImplicitCastSubclassAccess
  public void testImplicitCastSubclassAccess() throws Exception {
    testTypes(" function Element() {};\n" +
             "" +
             "Element.prototype.innerHTML;" +
             "" +
             "function DIVElement() {};",
             "(new DIVElement).innerHTML = new Array();", null, false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testImplicitCastNotInExterns
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testNumberNode
  public void testNumberNode() throws Exception {
    Node n = typeCheck(Node.newNumber(0));

    assertEquals(NUMBER_TYPE, n.getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStringNode
  public void testStringNode() throws Exception {
    Node n = typeCheck(Node.newString("hello"));

    assertEquals(STRING_TYPE, n.getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanNodeTrue
  public void testBooleanNodeTrue() throws Exception {
    Node trueNode = typeCheck(new Node(Token.TRUE));

    assertEquals(BOOLEAN_TYPE, trueNode.getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanNodeFalse
  public void testBooleanNodeFalse() throws Exception {
    Node falseNode = typeCheck(new Node(Token.FALSE));

    assertEquals(BOOLEAN_TYPE, falseNode.getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testUndefinedNode
  public void testUndefinedNode() throws Exception {
    Node p = new Node(Token.ADD);
    Node n = Node.newString(Token.NAME, "undefined");
    p.addChildToBack(n);
    p.addChildToBack(Node.newNumber(5));
    typeCheck(p);

    assertEquals(VOID_TYPE, n.getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNumberAutoboxing
  public void testNumberAutoboxing() throws Exception {
    testTypes("var a = 4;",
        "initializing variable\n" +
        "found   : number\n" +
        "required: (Number|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNumberUnboxing
  public void testNumberUnboxing() throws Exception {
    testTypes("var a = new Number(4);",
        "initializing variable\n" +
        "found   : Number\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStringAutoboxing
  public void testStringAutoboxing() throws Exception {
    testTypes("var a = 'hello';",
        "initializing variable\n" +
        "found   : string\n" +
        "required: (String|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStringUnboxing
  public void testStringUnboxing() throws Exception {
    testTypes("var a = new String('hello');",
        "initializing variable\n" +
        "found   : String\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanAutoboxing
  public void testBooleanAutoboxing() throws Exception {
    testTypes("var a = true;",
        "initializing variable\n" +
        "found   : boolean\n" +
        "required: (Boolean|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanUnboxing
  public void testBooleanUnboxing() throws Exception {
    testTypes("var a = new Boolean(false);",
        "initializing variable\n" +
        "found   : Boolean\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testIssue86
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testIssue124
  public void testIssue124() throws Exception {
    testTypes(
        "var t = null;" +
        "function test() {" +
        "  if (t != null) { t = null; }" +
        "  t = 1;" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testIssue124b
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testBug592170
  public void testBug592170() throws Exception {
    testTypes(
        "" +
        "function foo(opt_f) {" +
        "  " +
        "  return opt_f || function () {};" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBug901455
  public void testBug901455() throws Exception {
    testTypes(" function a() { return 3; }" +
        "var b = undefined === a()");
    testTypes(" function a() { return 3; }" +
        "var b = a() === undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBug908701
  public void testBug908701() throws Exception {
    testTypes("var s = new String('foo');" +
        "var b = s.match(/a/) != null;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBug908625
  public void testBug908625() throws Exception {
    testTypes("function A(){}" +
        "function B(){}" +
        "function foo(b){return b}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBug911118
  public void testBug911118() throws Exception {
    
    Scope s = parseAndTypeCheckWithScope("var a = function(){};").scope;
    JSType type = s.getVar("a").getType();
    assertEquals("function (): undefined", type.toString());

    
    testTypes("function nullFunction() {};" +
        "var foo = nullFunction;" +
        "foo = function() {};" +
        "foo();");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBug909000
  public void testBug909000() throws Exception {
    testTypes("function A(){}\n" +
        "\n" +
        "function y(a) { return a }",
        "inconsistent return type\n" +
        "found   : A\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBug930117
  public void testBug930117() throws Exception {
    testTypes(
        "function f(x){}" +
        "f(null);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : null\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBug1484445
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testBug1859535
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testBug1940591
  public void testBug1940591() throws Exception {
    testTypes(
        "" +
        "var a = {};\n" +
        "\n" +
        "a.name = 0;\n" +
        "\n" +
        "a.g = function(x) { x.name = 'a'; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBug1942972
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testBug1943776
  public void testBug1943776() throws Exception {
    testTypes(
        "" +
        "function bar() {" +
        "  return {foo: []};" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBug1987544
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testBug1940769
  public void testBug1940769() throws Exception {
    testTypes(
        " " +
        "function proto(obj) { return obj.prototype; }" +
        " function Map() {}" +
        "" +
        "function Map2() { Map.call(this); };" +
        "Map2.prototype = proto(Map);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBug2335992
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testBug2341812
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testScopedConstructors
  public void testScopedConstructors() throws Exception {
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testQualifiedNameInference1
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testQualifiedNameInference2
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testQualifiedNameInference3
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testQualifiedNameInference4
  public void testQualifiedNameInference4() throws Exception {
    testTypes(
        " function f(x) {}\n" +
        "" +
        "function Foo(x) { this.x_ = x; }\n" +
        "Foo.prototype.bar = function() {" +
        "  if (this.x_) { f(this.x_); }" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testSheqRefinedScope
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testAssignToUntypedVariable
  public void testAssignToUntypedVariable() throws Exception {
    Node n = parseAndTypeCheck("var z; z = 1;");

    Node assign = n.getLastChild().getFirstChild();
    Node node = assign.getFirstChild();
    assertFalse(node.getJSType().isUnknownType());
    assertEquals("number", node.getJSType().toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAssignToUntypedProperty
  public void testAssignToUntypedProperty() throws Exception {
    Node n = parseAndTypeCheck(
        " function Foo() {}\n" +
        "Foo.prototype.a = 1;" +
        "(new Foo).a;");

    Node node = n.getLastChild().getFirstChild();
    assertFalse(node.getJSType().isUnknownType());
    assertTrue(node.getJSType().isNumber());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew1
  public void testNew1() throws Exception {
    testTypes("new 4", TypeCheck.NOT_A_CONSTRUCTOR);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew2
  public void testNew2() throws Exception {
    testTypes("var Math = {}; new Math()", TypeCheck.NOT_A_CONSTRUCTOR);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew3
  public void testNew3() throws Exception {
    testTypes("new Date()");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew4
  public void testNew4() throws Exception {
    testTypes("function A(){}; new A();");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew5
  public void testNew5() throws Exception {
    testTypes("function A(){}; new A();", TypeCheck.NOT_A_CONSTRUCTOR);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew6
  public void testNew6() throws Exception {
    TypeCheckResult p =
      parseAndTypeCheckWithScope("function A(){};" +
      "var a = new A();");

    JSType aType = p.scope.getVar("a").getType();
    assertTrue(aType instanceof ObjectType);
    ObjectType aObjectType = (ObjectType) aType;
    assertEquals("A", aObjectType.getConstructor().getReferenceName());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew7
  public void testNew7() throws Exception {
    testTypes("" +
        "function foo(opt_constructor) {" +
        "if (opt_constructor) { new opt_constructor; }" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew8
  public void testNew8() throws Exception {
    testTypes("" +
        "function foo(opt_constructor) {" +
        "new opt_constructor;" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew9
  public void testNew9() throws Exception {
    testTypes("" +
        "function foo(opt_constructor) {" +
        "new (opt_constructor || Array);" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew10
  public void testNew10() throws Exception {
    testTypes("var goog = {};" +
        "" +
        "goog.Foo = function (opt_constructor) {" +
        "new (opt_constructor || Array);" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew11
  public void testNew11() throws Exception {
    testTypes("" +
        "function f(c1) {" +
        "  var c2 = function(){};" +
        "  c1.prototype = new c2;" +
        "}", TypeCheck.NOT_A_CONSTRUCTOR);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew12
  public void testNew12() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = new Array();");
    Var a = p.scope.getVar("a");

    assertEquals(ARRAY_TYPE, a.getType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew13
  public void testNew13() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "function FooBar(){};" +
        "var a = new FooBar();");
    Var a = p.scope.getVar("a");

    assertTrue(a.getType() instanceof ObjectType);
    assertEquals("FooBar", a.getType().toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew14
  public void testNew14() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var FooBar = function(){};" +
        "var a = new FooBar();");
    Var a = p.scope.getVar("a");

    assertTrue(a.getType() instanceof ObjectType);
    assertEquals("FooBar", a.getType().toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew15
  public void testNew15() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var goog = {};" +
        "goog.A = function(){};" +
        "var a = new goog.A();");
    Var a = p.scope.getVar("a");

    assertTrue(a.getType() instanceof ObjectType);
    assertEquals("goog.A", a.getType().toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNew16
  public void testNew16() throws Exception {
    testTypes(
        "" +
        "function Foo(x) {}" +
        "function g() { new Foo(1); }",
        "actual parameter 1 of Foo does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testName1
  public void testName1() throws Exception {
    assertEquals(VOID_TYPE, testNameNode("undefined"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testName2
  public void testName2() throws Exception {
    assertEquals(OBJECT_FUNCTION_TYPE, testNameNode("Object"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testName3
  public void testName3() throws Exception {
    assertEquals(ARRAY_FUNCTION_TYPE, testNameNode("Array"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testName4
  public void testName4() throws Exception {
    assertEquals(DATE_FUNCTION_TYPE, testNameNode("Date"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testName5
  public void testName5() throws Exception {
    assertEquals(REGEXP_FUNCTION_TYPE, testNameNode("RegExp"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation1
  public void testBitOperation1() throws Exception {
    testTypes("function foo(){ ~foo(); }",
        "operator ~ cannot be applied to undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation2
  public void testBitOperation2() throws Exception {
    testTypes("function foo(){var a = foo()<<3;}",
        "operator << cannot be applied to undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation3
  public void testBitOperation3() throws Exception {
    testTypes("function foo(){var a = 3<<foo();}",
        "operator << cannot be applied to undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation4
  public void testBitOperation4() throws Exception {
    testTypes("function foo(){var a = foo()>>>3;}",
        "operator >>> cannot be applied to undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation5
  public void testBitOperation5() throws Exception {
    testTypes("function foo(){var a = 3>>>foo();}",
        "operator >>> cannot be applied to undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation6
  public void testBitOperation6() throws Exception {
    testTypes("function foo(){var a = foo()&3;}",
        "bad left operand to bitwise operator\n" +
        "found   : Object\n" +
        "required: (boolean|null|number|string|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation7
  public void testBitOperation7() throws Exception {
    testTypes("var x = null; x |= undefined; x &= 3; x ^= '3'; x |= true;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation8
  public void testBitOperation8() throws Exception {
    testTypes("var x = void 0; x |= new Number(3);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBitOperation9
  public void testBitOperation9() throws Exception {
    testTypes("var x = void 0; x |= {};",
        "bad right operand to bitwise operator\n" +
        "found   : {}\n" +
        "required: (boolean|null|number|string|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall1
  public void testCall1() throws Exception {
    testTypes("3();", "number expressions are not callable");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall2
  public void testCall2() throws Exception {
    testTypes("function bar(foo){ bar('abc'); }",
        "actual parameter 1 of bar does not match formal parameter\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall3
  public void testCall3() throws Exception {
    
    
    testTypes("var opt_f;" +
        "var f1;" +
        "var f2 = opt_f || f1;" +
        "f2();",
        "Bad type annotation. Unknown type some.unknown.type");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall4
  public void testCall4() throws Exception {
    testTypes("var foo = function bar(a){ bar('abc'); }",
        "actual parameter 1 of bar does not match formal parameter\n" +
        "found   : string\n" +
        "required: RegExp");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall5
  public void testCall5() throws Exception {
    testTypes("var foo = function bar(a){ foo('abc'); }",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : string\n" +
        "required: RegExp");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall6
  public void testCall6() throws Exception {
    testTypes("function bar(foo){}" +
        "bar('abc');",
        "actual parameter 1 of bar does not match formal parameter\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall7
  public void testCall7() throws Exception {
    testTypes("var foo = function bar(a){};" +
        "foo('abc');",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : string\n" +
        "required: RegExp");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall8
  public void testCall8() throws Exception {
    testTypes("var f;f();",
        "(Function|number) expressions are " +
        "not callable");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall9
  public void testCall9() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Foo = function() {};" +
        " var bar = function(a){};" +
        "bar('abc');",
        "actual parameter 1 of bar does not match formal parameter\n" +
        "found   : string\n" +
        "required: goog.Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall10
  public void testCall10() throws Exception {
    testTypes("var f;f();");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCall11
  public void testCall11() throws Exception {
    testTypes("var f = new Function(); f();");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionCall1
  public void testFunctionCall1() throws Exception {
    testTypes(
        " var foo = function(x) {};" +
        "foo.call(null, 3);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionCall2
  public void testFunctionCall2() throws Exception {
    testTypes(
        " var foo = function(x) {};" +
        "foo.call(null, 'bar');",
        "actual parameter 2 of foo.call does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionCall3
  public void testFunctionCall3() throws Exception {
    testTypes(
        " " +
        "var Foo = function(x) { this.bar.call(null, x); };" +
        " Foo.prototype.bar;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionCall4
  public void testFunctionCall4() throws Exception {
    testTypes(
        " " +
        "var Foo = function(x) { this.bar.call(null, x); };" +
        " Foo.prototype.bar;",
        "actual parameter 2 of this.bar.call " +
        "does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionCall5
  public void testFunctionCall5() throws Exception {
    testTypes(
        " " +
        "var Foo = function(handler) { handler.call(this, x); };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionCall6
  public void testFunctionCall6() throws Exception {
    testTypes(
        " " +
        "var Foo = function(handler) { handler.apply(this, x); };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionCall7
  public void testFunctionCall7() throws Exception {
    testTypes(
        " " +
        "var Foo = function(handler, opt_context) { " +
        "  handler.call(opt_context, x);" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionCall8
  public void testFunctionCall8() throws Exception {
    testTypes(
        " " +
        "var Foo = function(handler, opt_context) { " +
        "  handler.apply(opt_context, x);" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast2
  public void testCast2() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n" +
        " var baz = new derived();\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast3
  public void testCast3() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n" +
        " var baz = new base();\n",
        "initializing variable\n" +
        "found   : base\n" +
        "required: derived");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast4
  public void testCast4() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n" +
        " var baz = " +
        "(new base());\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast5
  public void testCast5() throws Exception {
    
    testTypes("function foo() {}\n" +
        "function bar() {}\n" +
        "var baz = (new bar);\n",
        "invalid cast - must be a subtype or supertype\n" +
        "from: bar\n" +
        "to  : foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast6
  public void testCast6() throws Exception {
    
    testTypes("function foo() {}\n" +
        "function bar() {}\n" +
        "var baz = (new bar);\n" +
        "var baz = (new foo);\n" +
        "var baz = (new bar);\n" +
        "var baz = (new foo);\n" +
        "var baz = (new bar);\n" +
        "var baz = (new foo);\n" +
        "var baz = (new bar);\n" +
        "var baz = (new foo);\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast7
  public void testCast7() throws Exception {
    testTypes("var x =  (new Object());",
        "Bad type annotation. Unknown type foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast8
  public void testCast8() throws Exception {
    testTypes("function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast9
  public void testCast9() throws Exception {
    testTypes("var foo = {};" +
        "function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast10
  public void testCast10() throws Exception {
    testTypes("var foo = function() {};" +
        "function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast11
  public void testCast11() throws Exception {
    testTypes("var goog = {}; goog.foo = {};" +
        "function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type goog.foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast12
  public void testCast12() throws Exception {
    testTypes("var goog = {}; goog.foo = function() {};" +
        "function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type goog.foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast13
  public void testCast13() throws Exception {
    
    
    testClosureTypes("var goog = {}; " +
        "goog.addDependency('zzz.js', ['goog.foo'], []);" +
        "goog.foo = function() {};" +
        "function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type goog.foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast14
  public void testCast14() throws Exception {
    
    
    testClosureTypes("var goog = {}; " +
        "goog.addDependency('zzz.js', ['goog.bar'], []);" +
        "function f() { return  (new Object()); }",
        null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast15
  public void testCast15() throws Exception {
    
    
    
    
    
    
    
    testTypes(
        "for (var i = 0; i < 10; i++) {" +
          "var x =  ({foo: 3});" +
          " function f(x) {}" +
          "f(x.foo);" +
          "f([].foo);" +
        "}",
        "Property foo never defined on Array");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testCast16
  public void testCast16() throws Exception {
    
    testTypes(" function Foo() {} \n" +
        " var x =  ({})");

    testTypes(" function Foo() {} \n" +
        " var x = ( {})");

    
    testTypes(" function Foo() {} \n" +
        " var x =  {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNestedCasts
  public void testNestedCasts() throws Exception {
    testTypes("var T = function() {};\n" +
        "var V = function() {};\n" +
        "\n" +
        "function f(b) { return b ? new T() : new V(); }\n" +
        "\n" +
        "function g(b) { return b ? true : undefined; }\n" +
        "\n" +
        "function h() {\n" +
        "return  (f( (g(true))));\n" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNativeCast1
  public void testNativeCast1() throws Exception {
    testTypes(
        " function f(x) {}" +
        "f(String(true));",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNativeCast2
  public void testNativeCast2() throws Exception {
    testTypes(
        " function f(x) {}" +
        "f(Number(true));",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNativeCast3
  public void testNativeCast3() throws Exception {
    testTypes(
        " function f(x) {}" +
        "f(Boolean(''));",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNativeCast4
  public void testNativeCast4() throws Exception {
    testTypes(
        " function f(x) {}" +
        "f(Error(''));",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : Error\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBadConstructorCall
  public void testBadConstructorCall() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo();",
        "Constructor function (new:Foo): undefined should be called " +
        "with the \"new\" keyword");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeof
  public void testTypeof() throws Exception {
    testTypes("function foo(){ var a = typeof foo(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorType1
  public void testConstructorType1() throws Exception {
    testTypes("function Foo(){}" +
        "var f = new Date();",
        "initializing variable\n" +
        "found   : Date\n" +
        "required: Foo");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorType2
  public void testConstructorType2() throws Exception {
    testTypes("function Foo(){\n" +
        "this.bar = new Number(5);\n" +
        "}\n" +
        "var f = new Foo();\n" +
        "var n = f.bar;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorType3
  public void testConstructorType3() throws Exception {
    
    
    testTypes("var f = new Foo();\n" +
        "var n = f.bar;" +
        "function Foo(){\n" +
        "this.bar = new Number(5);\n" +
        "}\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorType4
  public void testConstructorType4() throws Exception {
    testTypes("function Foo(){\n" +
        "this.bar = new Number(5);\n" +
        "}\n" +
        "var f = new Foo();\n" +
        "var n = f.bar;",
        "initializing variable\n" +
        "found   : Number\n" +
        "required: String");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorType5
  public void testConstructorType5() throws Exception {
    testTypes("function Foo(){}\n" +
        "if (Foo){}\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorType6
  public void testConstructorType6() throws Exception {
    testTypes("\n" +
        "function bar() {}\n" +
        "function _foo() {\n" +
        " \n" +
        "  function f(x) {}\n" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testConstructorType7
  public void testConstructorType7() throws Exception {
    TypeCheckResult p =
        parseAndTypeCheckWithScope("function A(){};");

    JSType type = p.scope.getVar("A").getType();
    assertTrue(type instanceof FunctionType);
    FunctionType fType = (FunctionType) type;
    assertEquals("A", fType.getReferenceName());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnonymousType1
  public void testAnonymousType1() throws Exception {
    testTypes("function f() {}" +
        "\n" +
        "f().bar = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnonymousType2
  public void testAnonymousType2() throws Exception {
    testTypes("function f() {}" +
        "\n" +
        "f().bar = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAnonymousType3
  public void testAnonymousType3() throws Exception {
    testTypes("function f() {}" +
        "\n" +
        "f().bar = {FOO: 1};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBang1
  public void testBang1() throws Exception {
    testTypes("\n" +
        "function f(x) { return x; }",
        "inconsistent return type\n" +
        "found   : (Object|null|undefined)\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBang2
  public void testBang2() throws Exception {
    testTypes("\n" +
        "function f(x) { return x ? x : new Object(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBang3
  public void testBang3() throws Exception {
    testTypes("\n" +
        "function f(x) { return  (x); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBang4
  public void testBang4() throws Exception {
    testTypes("\n" +
        "function f(x, y) {\n" +
        "if (typeof x != 'undefined') { return x == y; }\n" +
        "else { return x != y; }\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBang5
  public void testBang5() throws Exception {
    testTypes("\n" +
        "function f(x, y) { return !!x && x == y; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBang6
  public void testBang6() throws Exception {
    testTypes("\n" +
        "function f(x) { return x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBang7
  public void testBang7() throws Exception {
    testTypes("function f(x) { return x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDefinePropertyOnNullableObject1
  public void testDefinePropertyOnNullableObject1() throws Exception {
    testTypes(" var n = {};\n" +
        " n.x = 1;\n" +
        "function f() { return n.x; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDefinePropertyOnNullableObject2
  public void testDefinePropertyOnNullableObject2() throws Exception {
    testTypes(" var T = function() {};\n" +
        "function f(t) {\n" +
        "t.x = 1; return t.x; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testUnknownConstructorInstanceType1
  public void testUnknownConstructorInstanceType1() throws Exception {
    testTypes(" function g(f) { return new f(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testUnknownConstructorInstanceType2
  public void testUnknownConstructorInstanceType2() throws Exception {
    testTypes("function g(f) { return  new f(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testUnknownConstructorInstanceType3
  public void testUnknownConstructorInstanceType3() throws Exception {
    testTypes("function g(f) { var x = new f(); x.a = 1; return x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testUnknownPrototypeChain
  public void testUnknownPrototypeChain() throws Exception {
    testTypes("\n" +
              "function inst(co) {\n" +
              " \n" +
              " var c = function() {};\n" +
              " c.prototype = co.prototype;\n" +
              " return new c;\n" +
              "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNamespacedConstructor
  public void testNamespacedConstructor() throws Exception {
    Node root = parseAndTypeCheck(
        "var goog = {};" +
        " goog.MyClass = function() {};" +
        " " +
        "function foo() { return new goog.MyClass(); }");

    JSType typeOfFoo = root.getLastChild().getJSType();
    assert(typeOfFoo instanceof FunctionType);

    JSType retType = ((FunctionType) typeOfFoo).getReturnType();
    assert(retType instanceof ObjectType);
    assertEquals("goog.MyClass", ((ObjectType) retType).getReferenceName());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testComplexNamespace
  public void testComplexNamespace() throws Exception {
    String js =
      "var goog = {};" +
      "goog.foo = {};" +
      "goog.foo.bar = 5;";

    TypeCheckResult p = parseAndTypeCheckWithScope(js);

    
    JSType googScopeType = p.scope.getVar("goog").getType();
    assertTrue(googScopeType instanceof ObjectType);
    assertTrue("foo property not present on goog type",
        ((ObjectType) googScopeType).hasProperty("foo"));
    assertFalse("bar property present on goog type",
        ((ObjectType) googScopeType).hasProperty("bar"));

    
    Node varNode = p.root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    JSType googNodeType = varNode.getFirstChild().getJSType();
    assertTrue(googNodeType instanceof ObjectType);

    
    assertTrue(googScopeType == googNodeType);

    
    Node getpropFoo1 = varNode.getNext().getFirstChild().getFirstChild();
    assertEquals(Token.GETPROP, getpropFoo1.getType());
    assertEquals("goog", getpropFoo1.getFirstChild().getString());
    JSType googGetpropFoo1Type = getpropFoo1.getFirstChild().getJSType();
    assertTrue(googGetpropFoo1Type instanceof ObjectType);

    
    assertTrue(googGetpropFoo1Type == googScopeType);

    
    JSType googFooType = ((ObjectType) googScopeType).getPropertyType("foo");
    assertTrue(googFooType instanceof ObjectType);

    
    
    Node getpropFoo2 = varNode.getNext().getNext()
        .getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.GETPROP, getpropFoo2.getType());
    assertEquals("goog", getpropFoo2.getFirstChild().getString());
    JSType googGetpropFoo2Type = getpropFoo2.getFirstChild().getJSType();
    assertTrue(googGetpropFoo2Type instanceof ObjectType);

    
    assertTrue(googGetpropFoo2Type == googScopeType);

    
    
    JSType googFooGetprop2Type = getpropFoo2.getJSType();
    assertTrue("goog.foo incorrectly annotated in goog.foo.bar selection",
        googFooGetprop2Type instanceof ObjectType);
    ObjectType googFooGetprop2ObjectType = (ObjectType) googFooGetprop2Type;
    assertFalse("foo property present on goog.foo type",
        googFooGetprop2ObjectType.hasProperty("foo"));
    assertTrue("bar property not present on goog.foo type",
        googFooGetprop2ObjectType.hasProperty("bar"));
    assertEquals("bar property on goog.foo type incorrectly inferred",
        NUMBER_TYPE, googFooGetprop2ObjectType.getPropertyType("bar"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAddingMethodsUsingPrototypeIdiomSimpleNamespace
  public void testAddingMethodsUsingPrototypeIdiomSimpleNamespace()
      throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype.m1 = 5");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals(NATIVE_PROPERTIES_COUNT + 1,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAddingMethodsUsingPrototypeIdiomComplexNamespace1
  public void testAddingMethodsUsingPrototypeIdiomComplexNamespace1()
      throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var goog = {};" +
        "goog.A = function() {};" +
        "goog.A.prototype.m1 = 5");

    testAddingMethodsUsingPrototypeIdiomComplexNamespace(p);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAddingMethodsUsingPrototypeIdiomComplexNamespace2
  public void testAddingMethodsUsingPrototypeIdiomComplexNamespace2()
      throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var goog = {};" +
        "goog.A = function() {};" +
        "goog.A.prototype.m1 = 5");

    testAddingMethodsUsingPrototypeIdiomComplexNamespace(p);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAddingMethodsPrototypeIdiomAndObjectLiteralSimpleNamespace
  public void testAddingMethodsPrototypeIdiomAndObjectLiteralSimpleNamespace()
      throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true}");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals(NATIVE_PROPERTIES_COUNT + 2,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", NUMBER_TYPE);
    checkObjectType(instanceType, "m2", BOOLEAN_TYPE);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDontAddMethodsIfNoConstructor
  public void testDontAddMethodsIfNoConstructor()
      throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true}");

    JSType functionAType = js1Node.getFirstChild().getJSType();
    assertEquals("function (): undefined", functionAType.toString());
    assertEquals(UNKNOWN_TYPE,
        U2U_FUNCTION_TYPE.getPropertyType("m1"));
    assertEquals(UNKNOWN_TYPE,
        U2U_FUNCTION_TYPE.getPropertyType("m2"));
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionAssignement
  public void testFunctionAssignement() throws Exception {
    testTypes("" +
        "function MSG_CALENDAR_ACCESS_ERROR(ph0, ph1) {return ''}" +
        "" +
        "var MSG_CALENDAR_ADD_ERROR = MSG_CALENDAR_ACCESS_ERROR;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAddMethodsPrototypeTwoWays
  public void testAddMethodsPrototypeTwoWays() throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true};" +
        "A.prototype.m3 = 'third property!';");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals("A", instanceType.toString());
    assertEquals(NATIVE_PROPERTIES_COUNT + 3,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", NUMBER_TYPE);
    checkObjectType(instanceType, "m2", BOOLEAN_TYPE);
    checkObjectType(instanceType, "m3", STRING_TYPE);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPrototypePropertyTypes
  public void testPrototypePropertyTypes() throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {\n" +
        "   this.m1;\n" +
        "   this.m2 = {};\n" +
        "   this.m3;\n" +
        "}\n" +
        " A.prototype.m4;\n" +
        " A.prototype.m5 = 0;\n" +
        " A.prototype.m6;\n");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals(NATIVE_PROPERTIES_COUNT + 6,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", STRING_TYPE);
    checkObjectType(instanceType, "m2",
        createUnionType(createUnionType(OBJECT_TYPE, NULL_TYPE), VOID_TYPE));
    checkObjectType(instanceType, "m3", BOOLEAN_TYPE);
    checkObjectType(instanceType, "m4", STRING_TYPE);
    checkObjectType(instanceType, "m5", NUMBER_TYPE);
    checkObjectType(instanceType, "m6", BOOLEAN_TYPE);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testValueTypeBuiltInPrototypePropertyType
  public void testValueTypeBuiltInPrototypePropertyType() throws Exception {
    Node node = parseAndTypeCheck("\"x\".charAt(0)");
    assertEquals(STRING_TYPE, node.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testDeclareBuiltInConstructor
  public void testDeclareBuiltInConstructor() throws Exception {
    
    
    Node node = parseAndTypeCheck(
        " var String = function(opt_str) {};\n" +
        "(new String(\"x\")).charAt(0)");
    assertEquals(STRING_TYPE, node.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testExtendBuiltInType1
  public void testExtendBuiltInType1() throws Exception {
    String externs =
        " var String = function(opt_str) {};\n" +
        "\n" +
        "String.prototype.substr = function(start, opt_length) {};\n";
    Node n1 = parseAndTypeCheck(externs + "(new String(\"x\")).substr(0,1);");
    assertEquals(STRING_TYPE, n1.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testExtendBuiltInType2
  public void testExtendBuiltInType2() throws Exception {
    String externs =
        " var String = function(opt_str) {};\n" +
        "\n" +
        "String.prototype.substr = function(start, opt_length) {};\n";
    Node n2 = parseAndTypeCheck(externs + "\"x\".substr(0,1);");
    assertEquals(STRING_TYPE, n2.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testExtendFunction1
  public void testExtendFunction1() throws Exception {
    Node n = parseAndTypeCheck("Function.prototype.f = " +
        "function() { return 1; };\n" +
        "(new Function()).f();");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertEquals(NUMBER_TYPE, type);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testExtendFunction2
  public void testExtendFunction2() throws Exception {
    Node n = parseAndTypeCheck("Function.prototype.f = " +
        "function() { return 1; };\n" +
        "(function() {}).f();");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertEquals(NUMBER_TYPE, type);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck1
  public void testInheritanceCheck1() throws Exception {
    testTypes(
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck2
  public void testInheritanceCheck2() throws Exception {
    testTypes(
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo not defined on any superclass of Sub");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck3
  public void testInheritanceCheck3() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo already defined on superclass Super; " +
        "use @override to override it");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck4
  public void testInheritanceCheck4() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck5
  public void testInheritanceCheck5() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo already defined on superclass Root; " +
        "use @override to override it");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck6
  public void testInheritanceCheck6() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck7
  public void testInheritanceCheck7() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = 5;",
        "property foo already defined on superclass goog.Super; " +
        "use @override to override it");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck8
  public void testInheritanceCheck8() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = 5;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck9_1
  public void testInheritanceCheck9_1() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() { return 3; };" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck9_2
  public void testInheritanceCheck9_2() throws Exception {
    testTypes(
        "function Super() {};" +
        "" +
        "Super.prototype.foo = function() { return 1; };" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() {};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInheritanceCheck9_3
  public void testInheritanceCheck9_3() throws Exception {
    testTypes(
        "function Super() {};" +
        "" +
        "Super.prototype.foo = function() { return 1; };" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return \"some string\" };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Super\n" +
        "original: function (this:Super): number\n" +
        "override: function (this:Sub): string");
  }
