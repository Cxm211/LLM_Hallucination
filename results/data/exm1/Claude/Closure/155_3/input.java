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
// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitch
  public void testContinueToSwitch() {
    assertInvalidContinue("switch(1) {case(1): continue; }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitchWithNoCases
  public void testContinueToSwitchWithNoCases() {
    assertNoError("switch(1){}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitchWithTwoCases
  public void testContinueToSwitchWithTwoCases() {
    assertInvalidContinue("switch(1){case(1):break;case(2):continue;}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitchWithDefault
  public void testContinueToSwitchWithDefault() {
    assertInvalidContinue("switch(1){case(1):break;case(2):default:continue;}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToLabelSwitch
  public void testContinueToLabelSwitch() {
    assertInvalidLabeledContinue(
        "while(1) {a: switch(1) {case(1): continue a; }}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueOutsideSwitch
  public void testContinueOutsideSwitch() {
    assertNoError("b: while(1) { a: switch(1) { case(1): continue b; } }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueNotCrossFunction1
  public void testContinueNotCrossFunction1() {
    assertNoError("a:switch(1){case(1):function f(){a:while(1){continue a;}}}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueNotCrossFunction2
  public void testContinueNotCrossFunction2() {
    assertUndefinedLabel(
        "a:switch(1){case(1):function f(){while(1){continue a;}}}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testUseOfWith1
  public void testUseOfWith1() {
    testSame("with(a){}", ControlStructureCheck.USE_OF_WITH);
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testUseOfWith2
  public void testUseOfWith2() {
    testSame("" +
             "with(a){}");
  }

// com.google.javascript.jscomp.ConvertToDottedPropertiesTest::testConvert
  public void testConvert() {
    test("a['p']", "a.p");
    test("a['_p_']", "a._p_");
    test("a['_']", "a._");
    test("a['$']", "a.$");
    test("a.b.c['p']", "a.b.c.p");
    test("a.b['c'].p", "a.b.c.p");
    test("a['p']();", "a.p();");
    test("a()['p']", "a().p");
    
    test("a['\u0041A']", "a.AA");
  }

// com.google.javascript.jscomp.ConvertToDottedPropertiesTest::testDoNotConvert
  public void testDoNotConvert() {
    testSame("a[0]");
    testSame("a['']");
    testSame("a[' ']");
    testSame("a[',']");
    testSame("a[';']");
    testSame("a[':']");
    testSame("a['.']");
    testSame("a['0']");
    testSame("a['p ']");
    testSame("a['p' + '']");
    testSame("a[p]");
    testSame("a[P]");
    testSame("a[$]");
    testSame("a[p()]");
    testSame("a['default']");
    
    
    test("a['\u1d17A']", "a['\u1d17A']");
    
    
    test("a['\u00d1StuffAfter']", "a['\u00d1StuffAfter']");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFold1
  public void testFold1() {
    test("function f() { if (x) return; y(); }",
         "function f(){x||y()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers1
  public void testFoldWithMarkers1() {
    testSame("function f(){startMarker();if(x)return;endMarker();y()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers1a
  public void testFoldWithMarkers1a() {
    testSame("function f(){startMarker();if(x)return;endMarker()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFold2
  public void testFold2() {
    test("function f() { if (x) return; y(); if (a) return; b(); }",
         "function f(){if(!x){y();a||b()}}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testFoldWithMarkers2
  public void testFoldWithMarkers2() {
    testSame("function f(){startMarker(\"FOO\");startMarker(\"BAR\");" +
             "if(x)return;endMarker(\"BAR\");y();if(a)return;" +
             "endMarker(\"FOO\");b()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testUnmatchedStartMarker
  public void testUnmatchedStartMarker() {
    testSame("startMarker()", CreateSyntheticBlocks.UNMATCHED_START_MARKER);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testUnmatchedEndMarker1
  public void testUnmatchedEndMarker1() {
    testSame("endMarker()", CreateSyntheticBlocks.UNMATCHED_END_MARKER);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testUnmatchedEndMarker2
  public void testUnmatchedEndMarker2() {
    test("if(y){startMarker();x()}endMarker()",
        "if(y){startMarker();x()}endMarker()", null,
         CreateSyntheticBlocks.UNMATCHED_END_MARKER);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testInvalid1
  public void testInvalid1() {
    test("startMarker() && true",
        "startMarker()", null,
         CreateSyntheticBlocks.INVALID_MARKER_USAGE);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testInvalid2
  public void testInvalid2() {
    test("false && endMarker()",
        "", null,
         CreateSyntheticBlocks.INVALID_MARKER_USAGE);
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testDenormalize
  public void testDenormalize() {
    testSame("startMarker();for(;;);endMarker()");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testNonMarkingUse
  public void testNonMarkingUse() {
    testSame("function foo(endMarker){}");
    testSame("function foo(){startMarker:foo()}");
  }

// com.google.javascript.jscomp.CreateSyntheticBlocksTest::testContainingBlockPreservation
  public void testContainingBlockPreservation() {
    testSame("if(y){startMarker();x();endMarker()}");
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement1
  public void testFunctionMovement1() {
    
    
    
    
    
    
    

    JSModule[] modules = createModuleStar(
      
      "function f1(a) { alert(a); }" +
      "function f2(a) { alert(a); }" +
      "function f3(a) { alert(a); }" +
      "function f4() { alert(1); }" +
      "function g() { alert('ciao'); }",
      
      "f1('hi'); f3('bye'); var a = f4;" +
      "function h(a) { alert('h:' + a); }",
      
      "f2('hi'); f2('hi'); f3('bye');");

    test(modules, new String[] {
      
      "function f3(a) { alert(a); }" +
      "function g() { alert('ciao'); }",
      
      "function f4() { alert(1); }" +
      "function f1(a) { alert(a); }" +
      "f1('hi'); f3('bye'); var a = f4;" +
      "function h(a) { alert('h:' + a); }",
      
      "function f2(a) { alert(a); }" +
      "f2('hi'); f2('hi'); f3('bye');",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement2
  public void testFunctionMovement2() {
    
    JSModule[] modules = createModuleStar(
      
      "function f(a) { alert(a); }" +
      "function g() {var f = 1; f++}",
      
      "f(1);");

    test(modules, new String[] {
      
      "function g() {var f = 1; f++}",
      
      "function f(a) { alert(a); }" +
      "f(1);",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement3
  public void testFunctionMovement3() {
    
    JSModule[] modules = createModuleStar(
      
      "function f(a) { alert(a); }" +
      "function g(f) {f++}",
      
      "f(1);");

    test(modules, new String[] {
      
      "function g(f) {f++}",
      
      "function f(a) { alert(a); }" +
      "f(1);",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement4
  public void testFunctionMovement4() {
    
    JSModule[] modules = createModuleStar(
      
      "function f(){return function(a){}}",
      
      "var a = f();"
    );

    test(modules, new String[] {
      
      "",
      
      "function f(){return function(a){}}" +
      "var a = f();",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement5
  public void testFunctionMovement5() {
    
    JSModule[] modules = createModuleStar(
      
      "function f(n){return (n<1)?1:f(n-1)}",
      
      "var a = f(4);"
    );

    test(modules, new String[] {
      
      "",
      
      "function f(n){return (n<1)?1:f(n-1)}" +
      "var a = f(4);",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement5b
  public void testFunctionMovement5b() {
    
    JSModule[] modules = createModuleStar(
      
      "var f = function(n){return (n<1)?1:f(n-1)};",
      
      "var a = f(4);"
    );

    test(modules, new String[] {
      
      "",
      
      "var f = function(n){return (n<1)?1:f(n-1)};" +
      "var a = f(4);",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement6
  public void testFunctionMovement6() {
    
    JSModule[] modules = createModuleChain(
      
      "function f(){return 1}",
      
      "var a = f();",
      
      "var b = f();"
    );

    test(modules, new String[] {
      
      "",
      
      "function f(){return 1}" +
      "var a = f();",
      
      "var b = f();",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement7
  public void testFunctionMovement7() {
    
    JSModule[] modules = createModules(
      
      "function f(){return 1}",
      
      "",
      
      "var a = f();",
      
      "var b = f();",
      
      "var c = f();"
    );

    modules[1].addDependency(modules[0]);
    modules[2].addDependency(modules[1]);
    modules[3].addDependency(modules[1]);
    modules[4].addDependency(modules[1]);

    test(modules, new String[] {
      
      "",
      
      "function f(){return 1}",
      
      "var a = f();",
      
      "var b = f();",
      
      "var c = f();",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionMovement8
  public void testFunctionMovement8() {
    
    JSModule[] modules = createModuleChain(
      
      "var v = function f(){return 1}",
      
      "v();"
    );

    test(modules, new String[] {
      
      "",
      
      "var v = function f(){return 1};" +
      "v();",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionNonMovement1
  public void testFunctionNonMovement1() {
    
    
    
    
    
    testSame(createModuleStar(
      
      "function f(){};f.prototype.bar=new f;" +
      "if(a)function f2(){}" +
      "{{while(a)function f3(){}}}",
      
      "var a = new f();f2();f3();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testFunctionNonMovement2
  public void testFunctionNonMovement2() {
    
    
    testSame(createModuleStar(
      
      "function f(){return 1}",
      
      "var a = f();",
      
      "var b = f();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement1
  public void testClassMovement1() {
    test(createModuleStar(
             
             "function f(){} f.prototype.bar=function (){};",
             
             "var a = new f();"),
         new String[] {
           "",
           "function f(){} f.prototype.bar=function (){};" +
           "var a = new f();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement2
  public void testClassMovement2() {
    
    test(createModuleChain(
             
             "function f(){} f.prototype.bar=3; f.prototype.baz=5;",
             
             "f.prototype.baq = 7;",
             
             "f.prototype.baz = 9;",
             
             "var a = new f();"),
         new String[] {
           
           "",
           
           "",
           
           "function f(){} f.prototype.bar=3; f.prototype.baz=5;" +
           "f.prototype.baq = 7;" +
           "f.prototype.baz = 9;",
           
           "var a = new f();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement3
  public void testClassMovement3() {
    
    test(createModuleChain(
             
             "var f = function() {}; f.prototype.bar=3; f.prototype.baz=5;",
             
             "f = 7;",
             
             "f = 9;",
             
             "f = 11;"),
         new String[] {
           
           "",
           
           "",
           
           "var f = function() {}; f.prototype.bar=3; f.prototype.baz=5;" +
           "f = 7;" +
           "f = 9;",
           
           "f = 11;"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement4
  public void testClassMovement4() {
    testSame(createModuleStar(
                 
                 "function f(){} f.prototype.bar=3; f.prototype.baz=5;",
                 
                 "f.prototype.baq = 7;",
                 
                 "var a = new f();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement5
  public void testClassMovement5() {
    JSModule[] modules = createModules(
        
        "function f(){} f.prototype.bar=3; f.prototype.baz=5;",
        
        "",
        
        "f.prototype.baq = 7;",
        
        "var a = new f();");

    modules[1].addDependency(modules[0]);
    modules[2].addDependency(modules[1]);
    modules[3].addDependency(modules[1]);

    test(modules,
         new String[] {
           
           "",
           
           "function f(){} f.prototype.bar=3; f.prototype.baz=5;",
           
           "f.prototype.baq = 7;",
           
           "var a = new f();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement6
  public void testClassMovement6() {
    test(createModuleChain(
             
             "function Foo(){} function Bar(){} goog.inherits(Bar, Foo);" +
             "new Foo();",
             
             "new Bar();"),
         new String[] {
           
           "function Foo(){} new Foo();",
           
           "function Bar(){} goog.inherits(Bar, Foo); new Bar();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClassMovement7
  public void testClassMovement7() {
    testSame(createModuleChain(
                 
                 "function Foo(){} function Bar(){} goog.inherits(Bar, Foo);" +
                 "new Bar();",
                 
                 "new Foo();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testStubMethodMovement1
  public void testStubMethodMovement1() {
    test(createModuleChain(
             
             "function Foo(){} " +
             "Foo.prototype.bar = JSCompiler_stubMethod(x);",
             
             "new Foo();"),
        new String[] {
          
          "",
          "function Foo(){} " +
          "Foo.prototype.bar = JSCompiler_stubMethod(x);" +
          "new Foo();"
        });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testStubMethodMovement2
  public void testStubMethodMovement2() {
    test(createModuleChain(
             
             "function Foo(){} " +
             "Foo.prototype.bar = JSCompiler_unstubMethod(x);",
             
             "new Foo();"),
        new String[] {
          
          "",
          "function Foo(){} " +
          "Foo.prototype.bar = JSCompiler_unstubMethod(x);" +
          "new Foo();"
        });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testNoMoveSideEffectProperty
  public void testNoMoveSideEffectProperty() {
    testSame(createModuleChain(
                 
                 "function Foo(){} " +
                 "Foo.prototype.bar = createSomething();",
                 
                 "new Foo();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testAssignMovement
  public void testAssignMovement() {
    test(createModuleChain(
             
             "var f = 3;" +
             "f = 5;",
             
             "var h = f;"),
        new String[] {
          
          "",
          
          "var f = 3;" +
          "f = 5;" +
          "var h = f;"
        });

    
    testSame(createModuleChain(
                 
                 "var f = 3;" +
                 "var g = f = 5;",
                 
                 "var h = f;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testNoClassMovement2
  public void testNoClassMovement2() {
    test(createModuleChain(
             
             "var f = {};" +
             "f.h = 5;",
             
             "var h = f;"),
        new String[] {
          
          "",
          
          "var f = {};" +
          "f.h = 5;" +
          "var h = f;"
        });

    
    testSame(createModuleChain(
                 
                 "var f = {};" +
                 "var g = f.h = 5;",
                 
                 "var h = f;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testLiteralMovement1
  public void testLiteralMovement1() {
    test(createModuleChain(
             
             "var f = {'hi': 'mom', 'bye': function() {}};",
             
             "var h = f;"),
        new String[] {
          
          "",
          
          "var f = {'hi': 'mom', 'bye': function() {}};" +
          "var h = f;"
        });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testLiteralMovement2
  public void testLiteralMovement2() {
    testSame(createModuleChain(
                 
                 "var f = {'hi': 'mom', 'bye': goog.nullFunction};",
                 
                 "var h = f;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testLiteralMovement3
  public void testLiteralMovement3() {
    test(createModuleChain(
             
             "var f = ['hi', function() {}];",
             
             "var h = f;"),
        new String[] {
          
          "",
          
          "var f = ['hi', function() {}];" +
          "var h = f;"
        });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testLiteralMovement4
  public void testLiteralMovement4() {
    testSame(createModuleChain(
                 
                 "var f = ['hi', goog.nullFunction];",
                 
                 "var h = f;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement1
  public void testVarMovement1() {
    
    JSModule[] modules = createModuleStar(
      
      "var a = 0;",
      
      "var x = a;"
    );

    test(modules, new String[] {
      
      "",
      
      "var a = 0;" +
      "var x = a;",
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement2
  public void testVarMovement2() {
    
    JSModule[] modules = createModuleStar(
      
      "var a = 0; var b = 1; var c = 2;",
      
      "var x = b;"
    );

    test(modules, new String[] {
      
      "var a = 0; var c = 2;",
      
      "var b = 1;" +
      "var x = b;"
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement3
  public void testVarMovement3() {
    
    JSModule[] modules = createModuleStar(
      
      "var a = 0; var b = 1;",
      
      "var x = a + b;"
    );

    test(modules, new String[] {
      
      "",
      
      "var b = 1;" +
      "var a = 0;" +
      "var x = a + b;"
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement4
  public void testVarMovement4() {
    
    JSModule[] modules = createModuleStar(
      
      "var a = function(){alert(1)};",
      
      "var x = a;"
    );

    test(modules, new String[] {
      
      "",
      
      "var a = function(){alert(1)};" +
      "var x = a;"
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement5
  public void testVarMovement5() {
    
    testSame(createModuleStar(
      
      "var a = alert;",
      
      "var x = a;"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement6
  public void testVarMovement6() {
    
    JSModule[] modules = createModuleStar(
      
      "var a;",
      
      "var x = a;"
    );

    test(modules, new String[] {
      
      "",
      
      "var a;" +
      "var x = a;"
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement7
  public void testVarMovement7() {
    
    testSame(createModuleStar(
      
      "function f() {g();}",
      
      "function g(){};"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement8
  public void testVarMovement8() {
    JSModule[] modules = createModuleBush(
      
      "var a = 0;",
      
      "",
      
      "var x = a;",
      
      "var y = a;"
    );

    test(modules, new String[] {
      
      "",
      
      "var a = 0;",
      
      "var x = a;",
      
      "var y = a;"
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testVarMovement9
  public void testVarMovement9() {
    JSModule[] modules = createModuleTree(
      
      "var a = 0; var b = 1; var c = 3;",
      
      "",
      
      "",
      
      "a;",
      
      "a;c;",
      
      "b;",
      
      "b;c;"
    );

    test(modules, new String[] {
      
      "var c = 3;",
      
      "var a = 0;",
      
      "var b = 1;",
      
      "a;",
      
      "a;c;",
      
      "b;",
      
      "b;c;"
    });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClone1
  public void testClone1() {
    test(createModuleChain(
             
             "function f(){} f.prototype.clone = function() { return new f };",
             
             "var a = (new f).clone();"),
         new String[] {
           
           "",
           "function f(){} f.prototype.clone = function() { return new f() };" +
           
           "var a = (new f).clone();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testClone2
  public void testClone2() {
    test(createModuleChain(
             
             "function f(){}" +
             "f.prototype.cloneFun = function() {" +
             "  return function() {new f}" +
             "};",
             
             "var a = (new f).cloneFun();"),
         new String[] {
           
           "",
           "function f(){}" +
           "f.prototype.cloneFun = function() {" +
           "  return function() {new f}" +
           "};" +
           
           "var a = (new f).cloneFun();"
         });
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testBug4118005
  public void testBug4118005() {
    testSame(createModuleChain(
             
             "var m = 1;\n" +
             "(function () {\n" +
             " var x = 1;\n" +
             " m = function() { return x };\n" +
             "})();\n",
             
             "m();"));
  }

// com.google.javascript.jscomp.CrossModuleCodeMotionTest::testEmptyModule
  public void testEmptyModule() {
    
    
    
    
    
    
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1", "function x() {}"));

    JSModule empty = new JSModule("empty");
    empty.addDependency(m1);

    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2", "x()"));
    m2.addDependency(empty);

    JSModule m3 = new JSModule("m3");
    m3.add(JSSourceFile.fromCode("m3", "x()"));
    m3.addDependency(empty);

    test(new JSModule[] {m1,empty,m2,m3},
        new String[] {
          "",
          "function x() {}",
          "x()",
          "x()"
    });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMovePrototypeMethod1
  public void testMovePrototypeMethod1() {
    testSame(createModuleChain(
                 "function Foo() {}" +
                 "Foo.prototype.bar = function() {};",
                 
                 "(new Foo).bar()"));

    canMoveExterns = true;
    test(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype.bar = function() {};",
             
             "(new Foo).bar()"),
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.bar = JSCompiler_stubMethod(0);",
             
             "Foo.prototype.bar = JSCompiler_unstubMethod(0, function() {});" +
             "(new Foo).bar()"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMovePrototypeMethod2
  public void testMovePrototypeMethod2() {
    test(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype = { method: function() {} };",
             
             "(new Foo).method()"),
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype = { method: JSCompiler_stubMethod(0) };",
             
             "Foo.prototype.method = " +
             "    JSCompiler_unstubMethod(0, function() {});" +
             "(new Foo).method()"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMovePrototypeMethod3
  public void testMovePrototypeMethod3() {
    testSame(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype = { get method() {} };",
             
             "(new Foo).method()"));
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMovePrototypeRecursiveMethod
  public void testMovePrototypeRecursiveMethod() {
    test(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype.baz = function() { this.baz(); };",
             
             "(new Foo).baz()"),
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.baz = JSCompiler_stubMethod(0);",
             
             "Foo.prototype.baz = JSCompiler_unstubMethod(0, " +
             "    function() { this.baz(); });" +
             "(new Foo).baz()"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testCantMovePrototypeProp
  public void testCantMovePrototypeProp() {
    testSame(createModuleChain(
                 "function Foo() {}" +
                 "Foo.prototype.baz = goog.nullFunction;",
                 
                 "(new Foo).baz()"));
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMoveMethodsInRightOrder
  public void testMoveMethodsInRightOrder() {
    test(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype.baz = function() { return 1; };" +
             "Foo.prototype.baz = function() { return 2; };",
             
             "(new Foo).baz()"),
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.baz = JSCompiler_stubMethod(1);" +
             "Foo.prototype.baz = JSCompiler_stubMethod(0);",
             
             "Foo.prototype.baz = " +
             "JSCompiler_unstubMethod(1, function() { return 1; });" +
             "Foo.prototype.baz = " +
             "JSCompiler_unstubMethod(0, function() { return 2; });" +
             "(new Foo).baz()"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMoveMethodsInRightOrder2
  public void testMoveMethodsInRightOrder2() {
    JSModule[] m = createModules(
        "function Foo() {}" +
        "Foo.prototype.baz = function() { return 1; };" +
        "function Goo() {}" +
        "Goo.prototype.baz = function() { return 2; };",
        
        "",
        
        "(new Foo).baz()",
        
        "",
        
        "(new Goo).baz()");

    m[1].addDependency(m[0]);
    m[2].addDependency(m[1]);
    m[3].addDependency(m[2]);
    m[4].addDependency(m[2]);

    test(m,
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.baz = JSCompiler_stubMethod(1);" +
             "function Goo() {}" +
             "Goo.prototype.baz = JSCompiler_stubMethod(0);",
             
             "",
             
             "Foo.prototype.baz = " +
             "JSCompiler_unstubMethod(1, function() { return 1; });" +
             "Goo.prototype.baz = " +
             "JSCompiler_unstubMethod(0, function() { return 2; });" +
             "(new Foo).baz()",
             
             "",
             
             "(new Goo).baz()"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMoveMethodsUsedInTwoModules
  public void testMoveMethodsUsedInTwoModules() {
    testSame(createModuleStar(
                 "function Foo() {}" +
                 "Foo.prototype.baz = function() {};",
                 
                 "(new Foo).baz()",
                 
                 "(new Foo).baz()"));
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMoveMethodsUsedInTwoModules2
  public void testMoveMethodsUsedInTwoModules2() {
    JSModule[] modules = createModules(
        "function Foo() {}" +
        "Foo.prototype.baz = function() {};",
        
        "", 
        
        "(new Foo).baz() + 1",
        
        "(new Foo).baz() + 2");

    modules[1].addDependency(modules[0]);
    modules[2].addDependency(modules[1]);
    modules[3].addDependency(modules[1]);
    test(modules,
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.baz = JSCompiler_stubMethod(0);",
             
             "Foo.prototype.baz = JSCompiler_unstubMethod(0, function() {});",
             
             "(new Foo).baz() + 1",
             
             "(new Foo).baz() + 2"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testTwoMethods
  public void testTwoMethods() {}

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testTwoMethods2
  public void testTwoMethods2() {
    
    
    test(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype.baz = function() {};",
             
             "(new Foo).callBaz()",
             
             "Foo.prototype.callBaz = function() { this.baz(); }"),
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.baz = JSCompiler_stubMethod(0);",
             
             "(new Foo).callBaz()",
             
             "Foo.prototype.baz = JSCompiler_unstubMethod(0, function() {});" +
             "Foo.prototype.callBaz = function() { this.baz(); };"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testGlobalFunctionsInGraph
  public void testGlobalFunctionsInGraph() {
    test(createModuleChain(
            "function Foo() {}" +
            "Foo.prototype.baz = function() {};" +
            "function x() { return (new Foo).baz(); }",
            
            "x();"),
        new String[] {
          STUB_DECLARATIONS +
          "function Foo() {}" +
          "Foo.prototype.baz = JSCompiler_stubMethod(0);" +
          "function x() { return (new Foo).baz(); }",
          
          "Foo.prototype.baz = JSCompiler_unstubMethod(0, function() {});" +
          "x();"
        });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testClosureVariableReads1
  public void testClosureVariableReads1() {
    testSame(createModuleChain(
            "function Foo() {}" +
            "(function() {" +
            "var x = 'x';" +
            "Foo.prototype.baz = function() {x};" +
            "})();",
            
            "var y = new Foo(); y.baz();"));
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testClosureVariableReads2
  public void testClosureVariableReads2() {
    test(createModuleChain(
            "function Foo() {}" +
            "Foo.prototype.b1 = function() {" +
            "  var x = 1;" +
            "  Foo.prototype.b2 = function() {" +
            "    Foo.prototype.b3 = function() {" +
            "      x;" +
            "    }" +
            "  }" +
            "};",
            
            "var y = new Foo(); y.b1();",
            
            "y = new Foo(); z.b2();",
            
            "y = new Foo(); z.b3();"
            ),
         new String[] {
           STUB_DECLARATIONS +
           "function Foo() {}" +
           "Foo.prototype.b1 = JSCompiler_stubMethod(0);",
           
           "Foo.prototype.b1 = JSCompiler_unstubMethod(0, function() {" +
           "  var x = 1;" +
           "  Foo.prototype.b2 = function() {" +
           "    Foo.prototype.b3 = function() {" +
           "      x;" +
           "    }" +
           "  }" +
           "});" +
           "var y = new Foo(); y.b1();",
           
           "y = new Foo(); z.b2();",
           
           "y = new Foo(); z.b3();"
        });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testClosureVariableReads3
  public void testClosureVariableReads3() {}

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testNoClosureVariableReads1
  public void testNoClosureVariableReads1() {
    test(createModuleChain(
            "function Foo() {}" +
            "var x = 'x';" +
            "Foo.prototype.baz = function(){x};",
            
            "var y = new Foo(); y.baz();"),
         new String[] {
           STUB_DECLARATIONS +
           "function Foo() {}" +
           "var x = 'x';" +
           "Foo.prototype.baz = JSCompiler_stubMethod(0);",
           
           "Foo.prototype.baz = JSCompiler_unstubMethod(0, function(){x});" +
           "var y = new Foo(); y.baz();"
        });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testNoClosureVariableReads2
  public void testNoClosureVariableReads2() {
    test(createModuleChain(
            "function Foo() {}" +
            "Foo.prototype.baz = function(){var x = 1;x};",
            
            "var y = new Foo(); y.baz();"),
         new String[] {
           STUB_DECLARATIONS +
           "function Foo() {}" +
           "Foo.prototype.baz = JSCompiler_stubMethod(0);",
           
           "Foo.prototype.baz = JSCompiler_unstubMethod(" +
           "    0, function(){var x = 1; x});" +
           "var y = new Foo(); y.baz();"
        });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testInnerFunctionClosureVariableReads
  public void testInnerFunctionClosureVariableReads() {
    test(createModuleChain(
            "function Foo() {}" +
            "Foo.prototype.baz = function(){var x = 1;" +
            "  return function(){x}};",
            
            "var y = new Foo(); y.baz();"),
         new String[] {
           STUB_DECLARATIONS +
           "function Foo() {}" +
           "Foo.prototype.baz = JSCompiler_stubMethod(0);",
           
           "Foo.prototype.baz = JSCompiler_unstubMethod(" +
           "    0, function(){var x = 1; return function(){x}});" +
           "var y = new Foo(); y.baz();"
        });
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testSimple
  public void testSimple() {
    inFunction("var a; a=1", "var a; 1");
    inFunction("var a; a=1+1", "var a; 1+1");
    inFunction("var a; a=foo();", "var a; foo()");
    inFunction("a=1; var a; a=foo();", "1; var a; foo();");
    
    
    inFunction("var a; a=function f(){}");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testLoops
  public void testLoops() {
    inFunction("for(var a=0; a<10; a++) {}");
    inFunction("var x; for(var a=0; a<10; a++) {x=a}; a(x)");
    inFunction("var x; for(var a=0; x=a<10; a++) {}",
        "var x; for(var a=0; a<10; a++) {}");
    inFunction("var x; for(var a=0; a<10; x=a) {}",
        "var x; for(var a=0; a<10; a) {}");
    inFunction("var x; for(var a=0; a<10; x=a,a++) {}",
        "var x; for(var a=0; a<10; a,a++) {}");
    inFunction("var x; for(var a=0; a<10; a++,x=a) {}",
        "var x; for(var a=0; a<10; a++,a) {}");
    inFunction("var x;for(var a=0; a<10; a++) {x=1}",
        "var x;for(var a=0; a<10; a++) {1}");
    inFunction("var x; x=1; do{x=2}while(0); x",
        "var x; 1; do{x=2}while(0); x");
    inFunction("var x; x=1; while(1){x=2}; x");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testMultiPaths
  public void testMultiPaths() {
    inFunction("var x,y; if(x)y=1;", "var x,y; if(x)1;");
    inFunction("var x,y; if(x)y=1; y=2; x(y)", "var x,y; if(x)1; y=2; x(y)");
    inFunction("var x; switch(x) { case(1): x=1; break; } x");
    inFunction("var x; switch(x) { case(1): x=1; break; }",
        "var x; switch(x) { case(1): 1; break; }");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testUsedAsConditions
  public void testUsedAsConditions() {
    inFunction("var x; while(x=1){}", "var x; while(1){}");
    inFunction("var x; if(x=1){}", "var x; if(1){}");
    inFunction("var x; do{}while(x=1)", "var x; do{}while(1)");
    inFunction("var x; if(x=1==4&&1){}", "var x; if(1==4&&1) {}");
    inFunction("var x; if(0&&(x=1)){}", "var x; if(0&&1){}");
    inFunction("var x; if((x=2)&&(x=1)){}", "var x; if(2&&1){}");
    inFunction("var x; x=2; if(0&&(x=1)){}; x");

    inFunction("var x,y; if( (x=1)+(y=2) > 3){}",
        "var x,y; if( 1+2 > 3){}");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testUsedAsConditionsInSwitchStatements
  public void testUsedAsConditionsInSwitchStatements() {
    inFunction("var x; switch(x=1){}","var x; switch(1){}");
    inFunction("var x; switch(x){case(x=1):break;}",
        "var x; switch(x){case(1):break;}");

    inFunction("var x,y; switch(y) { case (x += 1): break; case (x): break;}");

    inFunction("var x,y; switch(y) { case (x = 1): break; case (2): break;}",
               "var x,y; switch(y) { case (1): break; case (2): break;}");
    inFunction("var x,y; switch(y) { case (x+=1): break; case (x=2): break;}",
               "var x,y; switch(y) { case (x+1): break; case (2): break;}");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentInReturn
  public void testAssignmentInReturn() {
    inFunction("var x; return x = 1;", "var x; return 1");
    inFunction("var x; return");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentSamples
  public void testAssignmentSamples() {
    
    inFunction("var x = 2;");
    inFunction("var x = 2; x++;", "var x=2; void 0");
    inFunction("var x; x=x++;", "var x;x++");
    inFunction("var x; x+=1;", "var x;x+1");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentInArgs
  public void testAssignmentInArgs() {
    inFunction("var x; foo(x = 1);", "var x; foo(1);");
    inFunction("var x; return foo(x = 1);", "var x; return foo(1);");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignAndReadInCondition
  public void testAssignAndReadInCondition() {
    inFunction("var a, b; if ((a = 1) && (b = a)) {b}");
    inFunction("var a, b; if ((b = a) && (a = 1)) {b}",
               "var a, b; if ((b = a) && (1)) {b}");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testParameters
  public void testParameters() {
    inFunction("param1=1; param1=2; param2(param1)",
        "1; param1=2; param2(param1)");
    inFunction("param1=param2()", "param2()");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testErrorHandling
  public void testErrorHandling() {
    inFunction("var x; try{ x=1 } catch(e){ x=2 }; x");
    inFunction("var x; try{ x=1 } catch(e){ x=2 }",
        "var x;try{ 1 } catch(e) { 2 }");
    inFunction("var x; try{ x=1 } finally { x=2 }; x",
        "var x;try{ 1 } finally{ x=2 }; x");
    inFunction("var x; while(1) { try{x=1;break}finally{x} }");
    inFunction("var x; try{throw 1} catch(e){x=2} finally{x}");
    inFunction("var x; try{x=1;throw 1;x} finally{x=2}; x",
        "var x; try{1;throw 1;x} finally{x=2}; x");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testDeadVarDeclarations
  public void testDeadVarDeclarations() {
    
    inFunction("var x=1;");
    inFunction("var x=1; x=2; x");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testGlobal
  public void testGlobal() {
    
    test("var x; x=1; x=2; x=3;", "var x; x=1; x=2; x=3;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testInnerFunctions
  public void testInnerFunctions() {
    inFunction("var x = function() { var x; x=1; }",
        "var x = function() { var x; 1; }");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testInnerFunctions2
  public void testInnerFunctions2() {
    
    inFunction("var x = 0; print(x); x = 1; var y = function(){}; y()");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testSelfReAssignment
  public void testSelfReAssignment() {
    inFunction("var x; x = x;", "var x; x");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testSelfIncrement
  public void testSelfIncrement() {
    inFunction("var x; x = x + 1;", "var x; x + 1");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentOp
  public void testAssignmentOp() {
    
    inFunction("var x; x += foo()", "var x; x + foo()");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentOpUsedAsLhs
  public void testAssignmentOpUsedAsLhs() {
    inFunction("var x,y; y = x += foo(); print(y)",
               "var x,y; y = x +  foo(); print(y)");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentOpUsedAsCondition
  public void testAssignmentOpUsedAsCondition() {
    inFunction("var x; if(x += foo()) {}",
               "var x; if(x +  foo()) {}");

    inFunction("var x; if((x += foo()) > 1) {}",
               "var x; if((x +  foo()) > 1) {}");

    
    inFunction("var x; while((x += foo()) > 1) {}");

    inFunction("var x; for(;--x;){}");
    inFunction("var x; for(;x--;){}");
    inFunction("var x; for(;x -= 1;){}");
    inFunction("var x; for(;x = 0;){}", "var x; for(;0;){}");

    inFunction("var x; for(;;--x){}");
    inFunction("var x; for(;;x--){}");
    inFunction("var x; for(;;x -= 1){}");
    inFunction("var x; for(;;x = 0){}", "var x; for(;;0){}");

    inFunction("var x; for(--x;;){}", "var x; for(;;){}");
    inFunction("var x; for(x--;;){}", "var x; for(;;){}");
    inFunction("var x; for(x -= 1;;){}", "var x; for(x - 1;;){}");
    inFunction("var x; for(x = 0;;){}", "var x; for(0;;){}");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testDeadIncrement
  public void testDeadIncrement() {
    
    inFunction("var x; x ++", "var x; void 0");
    inFunction("var x; x --", "var x; void 0");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testDeadButAlivePartiallyWithinTheExpression
  public void testDeadButAlivePartiallyWithinTheExpression() {
    inFunction("var x; x = 100, print(x), x = 101;",
               "var x; x = 100, print(x),     101;");
    inFunction("var x; x = 100, print(x), print(x), x = 101;",
               "var x; x = 100, print(x), print(x),     101;");
    inFunction("var x; x = 100, print(x), x = 0, print(x), x = 101;",
               "var x; x = 100, print(x), x = 0, print(x),     101;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testMutipleDeadAssignmentsButAlivePartiallyWithinTheExpression
  public void testMutipleDeadAssignmentsButAlivePartiallyWithinTheExpression() {
    inFunction("var x; x = 1, x = 2, x = 3, x = 4, x = 5," +
               "  print(x), x = 0, print(x), x = 101;",

               "var x; 1, 2, 3, 4, x = 5, print(x), x = 0, print(x), 101;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testDeadPartiallyWithinTheExpression
  public void testDeadPartiallyWithinTheExpression() {
    
    
    inFunction("var x; x = 100, x = 101; print(x);");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentChain
  public void testAssignmentChain() {
    inFunction("var a,b,c,d,e; a = b = c = d = e = 1",
               "var a,b,c,d,e; 1");
    inFunction("var a,b,c,d,e; a = b = c = d = e = 1; print(c)",
               "var a,b,c,d,e;         c = 1        ; print(c)");
    inFunction("var a,b,c,d,e; a = b = c = d = e = 1; print(a + e)",
               "var a,b,c,d,e; a =             e = 1; print(a + e)");
    inFunction("var a,b,c,d,e; a = b = c = d = e = 1; print(b + d)",
               "var a,b,c,d,e;     b =     d     = 1; print(b + d)");
    inFunction("var a,b,c,d,e; a = b = c = d = e = 1; print(a + b + d + e)",
               "var a,b,c,d,e; a = b =     d = e = 1; print(a + b + d + e)");
    inFunction("var a,b,c,d,e; a = b = c = d = e = 1; print(a+b+c+d+e)");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignmentOpChain
  public void testAssignmentOpChain() {
    inFunction("var a,b,c,d,e; a = b = c += d = e = 1",
               "var a,b,c,d,e;         c + 1");
    inFunction("var a,b,c,d,e; a = b = c += d = e = 1;  print(e)",
               "var a,b,c,d,e;         c +     (e = 1); print(e)");
    inFunction("var a,b,c,d,e; a = b = c += d = e = 1;  print(d)",
               "var a,b,c,d,e;         c + (d = 1)  ;   print(d)");
    inFunction("var a,b,c,d,e; a = b = c += d = e = 1;  print(a)",
               "var a,b,c,d,e; a =     c +          1;  print(a)");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIncDecInSubExpressions
  public void testIncDecInSubExpressions() {
    inFunction("var a; a = 1, a++; a");
    inFunction("var a; a = 1, ++a; a");
    inFunction("var a; a = 1, a--; a");
    inFunction("var a; a = 1, --a; a");

    inFunction("var a; a = 1, a++, print(a)");
    inFunction("var a; a = 1, ++a, print(a)");
    inFunction("var a; a = 1, a--, print(a)");
    inFunction("var a; a = 1, --a, print(a)");

    inFunction("var a; a = 1, print(a++)");
    inFunction("var a; a = 1, print(++a)");

    inFunction("var a; a = 1, print(a++)");
    inFunction("var a; a = 1, print(++a)");

    inFunction("var a; a = 1, print(a--)");
    inFunction("var a; a = 1, print(--a)");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testNestedReassignments
  public void testNestedReassignments() {
    inFunction("var a; a = (a = 1)", "var a; 1");
    inFunction("var a; a = (a *= 2)", "var a; a*2");

    
    inFunction("var a; a = (a++)", "var a; a++"); 
    inFunction("var a; a = (++a)", "var a; ++a"); 

    inFunction("var a; a = (b = (a = 1))", "var a; b = 1");
    inFunction("var a; a = (b = (a *= 2))", "var a; b = a * 2");
    inFunction("var a; a = (b = (a++))", "var a; b=a++");
    inFunction("var a; a = (b = (++a))", "var a; b=++a");

    
    inFunction("var a,b; a = (b = (a = 1))", "var a,b; 1");
    inFunction("var a,b; a = (b = (a *= 2))", "var a,b; a * 2");
    inFunction("var a,b; a = (b = (a++))",
               "var a,b; a++"); 
    inFunction("var a,b; a = (b = (++a))",
               "var a,b; ++a"); 

    inFunction("var a; a += (a++)", "var a; a + a++");
    inFunction("var a; a += (++a)", "var a; a+ (++a)");

    
    inFunction("var a,b; a += (b = (a = 1))", "var a,b; a + 1");
    inFunction("var a,b; a += (b = (a *= 2))", "var a,b; a + (a * 2)");
    inFunction("var a,b; a += (b = (a++))", "var a,b; a + a++");
    inFunction("var a,b; a += (b = (++a))", "var a,b; a+(++a)");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIncrementalReassignmentInForLoops
  public void testIncrementalReassignmentInForLoops() {
    inFunction("for(;x+=1;x+=1) {}");
    inFunction("for(;x;x+=1){}");
    inFunction("for(;x+=1;){foo(x)}");
    inFunction("for(;1;x+=1){foo(x)}");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIdentityAssignments
  public void testIdentityAssignments() {
    inFunction("var x; x=x", "var x; x");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testBug8730257
  public void testBug8730257() {
    inFunction(
        "  try {" +
        "     var sortIndices = {};" +
        "     sortIndices = bar();" +
        "     for (var i = 0; i < 100; i++) {" +
        "       var sortIndex = sortIndices[i];" +
        "       bar(sortIndex);" +
        "     }" +
        "   } finally {" +
        "     bar();" +
        "   }" );
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testAssignToExtern
  public void testAssignToExtern() {
    inFunction("extern = true;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297a
  public void testIssue297a() {
    testSame("function f(p) {" +
         " var x;" +
         " return ((x=p.id) && (x=parseInt(x.substr(1))) && x>0);" +
         "}; f('');");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297b
  public void testIssue297b() {
    test("function f() {" +
         " var x;" +
         " return (x='') && (x = x.substr(1));" +
         "};",
         "function f() {" +
         " var x;" +
         " return (x='') && (x.substr(1));" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297c
  public void testIssue297c() {
    test("function f() {" +
         " var x;" +
         " return (x=1) && (x = f(x));" +
         "};",
         "function f() {" +
         " var x;" +
         " return (x=1) && f(x);" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297d
  public void testIssue297d() {
    test("function f(a) {" +
         " return (a=1) && (a = f(a));" +
         "};",
         "function f(a) {" +
         " return (a=1) && (f(a));" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297e
  public void testIssue297e() {
    test("function f(a) {" +
         " return (a=1) - (a = g(a));" +
         "};",
         "function f(a) {" +
         " return (a=1) - (g(a));" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297f
  public void testIssue297f() {
    test("function f(a) {" +
         " h((a=1) - (a = g(a)));" +
         "};",
         "function f(a) {" +
         " h((a=1) - (g(a)));" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297g
  public void testIssue297g() {
    test("function f(a) {" +
         " var b = h((b=1) - (b = g(b)));" +
         " return b;" +
         "};",
         
         "function f(a) {" +
         " var b = h((b=1) - (b = g(b)));" +
         " return b;" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue297h
  public void testIssue297h() {
    test("function f(a) {" +
         " var b = b=1;" +
         " return b;" +
         "};",
         
         "function f(a) {" +
         " var b = b = 1;" +
         " return b;" +
         "};");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testInExpression1
  public void testInExpression1() {
    inFunction("var a; return a=(a=(a=3));", "var a; return 3;");
    inFunction("var a; return a=(a=(a=a));", "var a; return a;");
    inFunction("var a; return a=(a=(a=a+1)+1);", "var a; return a+1+1;");
    inFunction("var a; return a=(a=(a=f(a)+1)+1);", "var a; return f(a)+1+1;");
    inFunction("var a; return a=f(a=f(a=f(a)));", "var a; return f(f(f(a)));");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testInExpression2
  public void testInExpression2() {
    
    
    inFunction(
        "var a; a = 1; if ((a = 2) || (a = 3) || (a)) {}",
        "var a; a = 1; if ((    2) || (a = 3) || (a)) {}");

    inFunction(
        "var a; (a = 1) || (a = 2)",
        "var a; 1 || 2");

    inFunction("var a; (a = 1) || (a = 2); return a");

    inFunction(
        "var a; a = 1; a ? a = 2 : a;",
        "var a; a = 1; a ?     2 : a;");

    inFunction("var a; a = 1; a ? a = 2 : a; return a");

    inFunction(
        "var a; a = 1; a ? a : a = 2;",
        "var a; a = 1; a ? a : 2;");

    inFunction("var a; a = 1; a ? a : a =2; return a");

    inFunction(
        "var a; (a = 1) ? a = 2 : a = 3;",
        "var a;      1  ?     2 :     3;");

    
    
    inFunction("var a; (a = 1) ? a = 2 : a = 3; return a");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue384a
  public void testIssue384a() {
    inFunction(
            " var a, b;\n" +
            " if (f(b = true) || f(b = false))\n" +
            "   a = b;\n" +
            " else\n" +
            "   a = null;\n" +
            " return a;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue384b
  public void testIssue384b() {
    inFunction(
            " var a, b;\n" +
            " (f(b = true) || f(b = false)) ? (a = b) : (a = null);\n" +
            " return a;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue384c
  public void testIssue384c() {
    inFunction(
            " var a, b;\n" +
            " (a ? f(b = true) : f(b = false)) && (a = b);\n" +
            " return a;");
  }

// com.google.javascript.jscomp.DeadAssignmentsEliminationTest::testIssue384d
  public void testIssue384d() {
    inFunction(
            " var a, b;\n" +
            " (f(b = true) || f(b = false)) && (a = b);\n" +
            " return a;");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testVarAndOptionalParams
  public void testVarAndOptionalParams() {
    Node args = new Node(Token.LP,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"));
    Node optArgs = new Node(Token.LP,
        Node.newString(Token.NAME, "opt_a"),
        Node.newString(Token.NAME, "opt_b"));

    assertFalse(conv.isVarArgsParameter(args.getFirstChild()));
    assertTrue(conv.isVarArgsParameter(args.getLastChild()));
    assertFalse(conv.isVarArgsParameter(optArgs.getFirstChild()));
    assertTrue(conv.isVarArgsParameter(optArgs.getLastChild()));

    assertTrue(conv.isOptionalParameter(args.getFirstChild()));
    assertFalse(conv.isOptionalParameter(args.getLastChild()));
    assertTrue(conv.isOptionalParameter(optArgs.getFirstChild()));
    assertFalse(conv.isOptionalParameter(optArgs.getLastChild()));
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInlineName
  public void testInlineName() {
    assertFalse(conv.isConstant("a"));
    assertFalse(conv.isConstant("XYZ123_"));
    assertFalse(conv.isConstant("ABC"));
    assertFalse(conv.isConstant("ABCdef"));
    assertFalse(conv.isConstant("aBC"));
    assertFalse(conv.isConstant("A"));
    assertFalse(conv.isConstant("_XYZ123"));
    assertFalse(conv.isConstant("a$b$XYZ123_"));
    assertFalse(conv.isConstant("a$b$ABC_DEF"));
    assertFalse(conv.isConstant("a$b$A"));
    assertFalse(conv.isConstant("a$b$a"));
    assertFalse(conv.isConstant("a$b$ABCdef"));
    assertFalse(conv.isConstant("a$b$aBC"));
    assertFalse(conv.isConstant("a$b$"));
    assertFalse(conv.isConstant("$"));
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testExportedName
  public void testExportedName() {
    assertFalse(conv.isExported("_a"));
    assertFalse(conv.isExported("_a_"));
    assertFalse(conv.isExported("a"));

    assertFalse(conv.isExported("$super", false));
    assertTrue(conv.isExported("$super", true));
    assertTrue(conv.isExported("$super"));
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testPrivateName
  public void testPrivateName() {
    assertFalse(conv.isPrivate("a_"));
    assertFalse(conv.isPrivate("a"));
    assertFalse(conv.isPrivate("_a_"));
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testEnumKey
  public void testEnumKey() {
    assertTrue(conv.isValidEnumKey("A"));
    assertTrue(conv.isValidEnumKey("123"));
    assertTrue(conv.isValidEnumKey("FOO_BAR"));

    assertTrue(conv.isValidEnumKey("a"));
    assertTrue(conv.isValidEnumKey("someKeyInCamelCase"));
    assertTrue(conv.isValidEnumKey("_FOO_BAR"));
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection1
  public void testInheritanceDetection1() {
    assertNotClassDefining("goog.foo(A, B);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection2
  public void testInheritanceDetection2() {
    assertNotClassDefining("goog.inherits(A, B);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection3
  public void testInheritanceDetection3() {
    assertNotClassDefining("A.inherits(B);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection4
  public void testInheritanceDetection4() {
    assertNotClassDefining("goog.inherits(goog.A, goog.B);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection5
  public void testInheritanceDetection5() {
    assertNotClassDefining("goog.A.inherits(goog.B);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection6
  public void testInheritanceDetection6() {
    assertNotClassDefining("A.inherits(this.B);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection7
  public void testInheritanceDetection7() {
    assertNotClassDefining("this.A.inherits(B);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection8
  public void testInheritanceDetection8() {
    assertNotClassDefining("goog.inherits(A, B, C);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection9
  public void testInheritanceDetection9() {
    assertNotClassDefining("A.mixin(B.prototype);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetection10
  public void testInheritanceDetection10() {
    assertNotClassDefining("goog.mixin(A.prototype, B.prototype);");
  }

// com.google.javascript.jscomp.DefaultCodingConventionTest::testInheritanceDetectionPostCollapseProperties
  public void testInheritanceDetectionPostCollapseProperties() {
    assertNotClassDefining("goog$inherits(A, B);");
    assertNotClassDefining("goog$inherits(A);");
  }

// com.google.javascript.jscomp.DefinitionsRemoverTest::testRemoveFunction
  public void testRemoveFunction() {
    testSame("{(function (){bar()})}");
    test("{function a(){bar()}}", "{}");
    test("foo(); function a(){} bar()", "foo(); bar();");
    test("foo(); function a(){} function b(){} bar()", "foo(); bar();");
  }

// com.google.javascript.jscomp.DefinitionsRemoverTest::testRemoveAssignment
  public void testRemoveAssignment() {
    test("x = 0;", "0");
    test("{x = 0}", "{0}");
    test("x = 0; y = 0;", "0; 0;");
    test("for (x = 0;x;x) {};", "for(0;x;x) {};");
  }

// com.google.javascript.jscomp.DefinitionsRemoverTest::testRemoveVarAssignment
  public void testRemoveVarAssignment() {
    test("var x = 0;", "0");
    test("{var x = 0}", "{0}");
    test("var x = 0; var y = 0;", "0;0");
    test("var x = 0; var y = 0;", "0;0");
  }

// com.google.javascript.jscomp.DefinitionsRemoverTest::testRemoveLiteral
  public void testRemoveLiteral() {
    test("foo({ 'one' : 1 })", "foo({ })");
    test("foo({ 'one' : 1 , 'two' : 2 })", "foo({ })");
  }

// com.google.javascript.jscomp.DefinitionsRemoverTest::testRemoveFunctionExpressionName
  public void testRemoveFunctionExpressionName() {
    test("foo(function f(){})", "foo(function (){})");
  }

// com.google.javascript.jscomp.DenormalizeTest::testFor
  public void testFor() {
    
    test("a = 0; for(; a < 2 ; a++) foo()",
         "for(a = 0; a < 2 ; a++) foo();");
    
    test("var a = 0; for(; c < b ; c++) foo()",
         "for(var a = 0; c < b ; c++) foo()");

    
    testSame("var a = 0; a:for(; c < b ; c++) foo()");
    testSame("var a = 0; a:b:for(; c < b ; c++) foo()");

    
    test("if(x){var a = 0; for(; c < b; c++) foo()}",
         "if(x){for(var a = 0; c < b; c++) foo()}");

    
    test("init(); for(; a < 2 ; a++) foo()",
         "for(init(); a < 2 ; a++) foo();");

    
    test("function f(){ var a; for(; a < 2 ; a++) foo() }",
         "function f(){ for(var a; a < 2 ; a++) foo() }");
    testSame("function f(){ return; for(; a < 2 ; a++) foo() }");
  }

// com.google.javascript.jscomp.DenormalizeTest::testForIn
  public void testForIn() {
    test("var a; for(a in b) foo()", "for (var a in b) foo()");
    testSame("a = 0; for(a in b) foo()");
    testSame("var a = 0; for(a in b) foo()");

    
    testSame("var a; a:for(a in b) foo()");
    testSame("var a; a:b:for(a in b) foo()");

    
    test("if(x){var a; for(a in b) foo()}",
         "if(x){for(var a in b) foo()}");

    
    testSame("init(); for(a in b) foo()");

    
    testSame("function f(){ return; for(a in b) foo() }");
  }

// com.google.javascript.jscomp.DenormalizeTest::testInOperatorNotInsideFor
  public void testInOperatorNotInsideFor() {
    
    
    
    

    
    testSame("function f(){ var a; var i=\"length\" in a;" +
        "for(; a < 2 ; a++) foo() }");
    
    testSame("function f(){ var a; var i=(\"length\" in a);" +
        "for(; a < 2 ; a++) foo() }");
    
    
    test("function f(){" +
         "var b,a=0; for (var i=(\"length\" in b);a<2; a++) foo()}",
         "function f(){var b; var a=0;var i=(\"length\" in b);" +
         "for (;a<2;a++) foo()}");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewritePrototypeMethods1
  public void testRewritePrototypeMethods1() throws Exception {
    
    disableTypeCheck();
    checkTypes(RewritePrototypeMethodTestInput.INPUT,
               RewritePrototypeMethodTestInput.EXPECTED,
               RewritePrototypeMethodTestInput.EXPECTED_TYPE_CHECKING_OFF);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewritePrototypeMethods2
  public void testRewritePrototypeMethods2() throws Exception {
    
    enableTypeCheck(CheckLevel.ERROR);
    checkTypes(RewritePrototypeMethodTestInput.INPUT,
               RewritePrototypeMethodTestInput.EXPECTED,
               RewritePrototypeMethodTestInput.EXPECTED_TYPE_CHECKING_ON);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteChained
  public void testRewriteChained() throws Exception {
    String source = newlineJoin(
        "A.prototype.foo = function(){return this.b};",
        "B.prototype.bar = function(){};",
        "o.foo().bar()");

    String expected = newlineJoin(
        "var JSCompiler_StaticMethods_foo = ",
        "function(JSCompiler_StaticMethods_foo$self) {",
        "  return JSCompiler_StaticMethods_foo$self.b",
        "};",
        "var JSCompiler_StaticMethods_bar = ",
        "function(JSCompiler_StaticMethods_bar$self) {",
        "};",
        "JSCompiler_StaticMethods_bar(JSCompiler_StaticMethods_foo(o))");
    test(source, expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteDeclIsExpressionStatement
  public void testRewriteDeclIsExpressionStatement() throws Exception {
    test(semicolonJoin(NoRewriteDeclarationUsedAsRValue.DECL,
                       NoRewriteDeclarationUsedAsRValue.CALL),
         "var JSCompiler_StaticMethods_foo =" +
         "function(JSCompiler_StaticMethods_foo$self) {};" +
         "JSCompiler_StaticMethods_foo(o)");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteDeclUsedAsAssignmentRhs
  public void testNoRewriteDeclUsedAsAssignmentRhs() throws Exception {
    testSame(semicolonJoin("var c = " + NoRewriteDeclarationUsedAsRValue.DECL,
                           NoRewriteDeclarationUsedAsRValue.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteDeclUsedAsCallArgument
  public void testNoRewriteDeclUsedAsCallArgument() throws Exception {
    testSame(semicolonJoin("f(" + NoRewriteDeclarationUsedAsRValue.DECL + ")",
                           NoRewriteDeclarationUsedAsRValue.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteInGlobalScope
  public void testRewriteInGlobalScope() throws Exception {
    String expected = newlineJoin(
        "function a(){}",
        "var JSCompiler_StaticMethods_foo = ",
        "function(JSCompiler_StaticMethods_foo$self) {",
        "  return JSCompiler_StaticMethods_foo$self.x",
        "};",
        "var o = new a;",
        "JSCompiler_StaticMethods_foo(o);");

    test(NoRewriteIfNotInGlobalScopeTestInput.INPUT, expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteIfNotInGlobalScope1
  public void testNoRewriteIfNotInGlobalScope1() throws Exception {
    testSame("if(true){" + NoRewriteIfNotInGlobalScopeTestInput.INPUT + "}");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteIfNotInGlobalScope2
  public void testNoRewriteIfNotInGlobalScope2() throws Exception {
    testSame("function enclosingFunction() {" +
             NoRewriteIfNotInGlobalScopeTestInput.INPUT +
             "}");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNamespaceFunctions
  public void testNoRewriteNamespaceFunctions() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.foo = function() {return this.x};",
        "a.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSingleDefinition1
  public void testRewriteSingleDefinition1() throws Exception {
    test(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_A,
                       NoRewriteMultipleDefinitionTestInput.CALL),
         NoRewriteMultipleDefinitionTestInput.SINGLE_DEFINITION_EXPECTED);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSingleDefinition2
  public void testRewriteSingleDefinition2() throws Exception {
    test(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_B,
                       NoRewriteMultipleDefinitionTestInput.CALL),
         NoRewriteMultipleDefinitionTestInput.SINGLE_DEFINITION_EXPECTED);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteMultipleDefinition1
  public void testNoRewriteMultipleDefinition1() throws Exception {
    testSame(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_A,
                           NoRewriteMultipleDefinitionTestInput.SOURCE_A,
                           NoRewriteMultipleDefinitionTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteMultipleDefinition2
  public void testNoRewriteMultipleDefinition2() throws Exception {
    testSame(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_B,
                           NoRewriteMultipleDefinitionTestInput.SOURCE_B,
                           NoRewriteMultipleDefinitionTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteMultipleDefinition3
  public void testNoRewriteMultipleDefinition3() throws Exception {
    testSame(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_A,
                           NoRewriteMultipleDefinitionTestInput.SOURCE_B,
                           NoRewriteMultipleDefinitionTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewritePrototypeNoObjectLiterals
  public void testRewritePrototypeNoObjectLiterals() throws Exception {
    test(semicolonJoin(NoRewritePrototypeObjectLiteralsTestInput.REGULAR,
                       NoRewritePrototypeObjectLiteralsTestInput.CALL),
         "var JSCompiler_StaticMethods_foo = " +
         "function(JSCompiler_StaticMethods_foo$self) {};" +
         "JSCompiler_StaticMethods_foo(o)");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewritePrototypeObjectLiterals1
  public void testNoRewritePrototypeObjectLiterals1() throws Exception {
    testSame(semicolonJoin(NoRewritePrototypeObjectLiteralsTestInput.OBJ_LIT,
                           NoRewritePrototypeObjectLiteralsTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewritePrototypeObjectLiterals2
  public void testNoRewritePrototypeObjectLiterals2() throws Exception {
    testSame(semicolonJoin(NoRewritePrototypeObjectLiteralsTestInput.OBJ_LIT,
                           NoRewritePrototypeObjectLiteralsTestInput.REGULAR,
                           NoRewritePrototypeObjectLiteralsTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteExternalMethods1
  public void testNoRewriteExternalMethods1() throws Exception {
    testSame("a.externalMethod()");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteExternalMethods2
  public void testNoRewriteExternalMethods2() throws Exception {
    testSame("A.prototype.externalMethod = function(){}; o.externalMethod()");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteCodingConvention
  public void testNoRewriteCodingConvention() throws Exception {
    
    testSame("a.prototype._foo = function() {};");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteNoVarArgs
  public void testRewriteNoVarArgs() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return args};",
        "var o = new a;",
        "o.foo()");

    String expected = newlineJoin(
        "function a(){}",
        "var JSCompiler_StaticMethods_foo = ",
        "  function(JSCompiler_StaticMethods_foo$self, args) {return args};",
        "var o = new a;",
        "JSCompiler_StaticMethods_foo(o)");

    test(source, expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteVarArgs
  public void testNoRewriteVarArgs() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(var_args) {return arguments};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteCallReference
  public void testRewriteCallReference() throws Exception {
    String expected = newlineJoin(
        "function a(){}",
        "var JSCompiler_StaticMethods_foo = ",
        "function(JSCompiler_StaticMethods_foo$self) {",
        "  return JSCompiler_StaticMethods_foo$self.x",
        "};",
        "var o = new a;",
        "JSCompiler_StaticMethods_foo(o);");

    test(NoRewriteNonCallReferenceTestInput.BASE + "o.foo()", expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNoReferences
  public void testNoRewriteNoReferences() throws Exception {
    testSame(NoRewriteNonCallReferenceTestInput.BASE);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNonCallReference
  public void testNoRewriteNonCallReference() throws Exception {
    testSame(NoRewriteNonCallReferenceTestInput.BASE + "o.foo && o.foo()");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteNoNestedFunction
  public void testRewriteNoNestedFunction() throws Exception {
    test(semicolonJoin(
             NoRewriteNestedFunctionTestInput.PREFIX + "}",
             NoRewriteNestedFunctionTestInput.SUFFIX,
             NoRewriteNestedFunctionTestInput.INNER),
         semicolonJoin(
             NoRewriteNestedFunctionTestInput.EXPECTED_PREFIX + "}",
             NoRewriteNestedFunctionTestInput.EXPECTED_SUFFIX,
             "var JSCompiler_StaticMethods_bar=" +
             "function(JSCompiler_StaticMethods_bar$self){}",
             "JSCompiler_StaticMethods_bar(o)"));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNestedFunction
  public void testNoRewriteNestedFunction() throws Exception {
    test(NoRewriteNestedFunctionTestInput.PREFIX +
         NoRewriteNestedFunctionTestInput.INNER + "};" +
         NoRewriteNestedFunctionTestInput.SUFFIX,
         NoRewriteNestedFunctionTestInput.EXPECTED_PREFIX +
         NoRewriteNestedFunctionTestInput.INNER + "};" +
         NoRewriteNestedFunctionTestInput.EXPECTED_SUFFIX);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod
  public void testRewriteImplementedMethod() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return args};",
        "var o = new a;",
        "o.foo()");
    String expected = newlineJoin(
        "function a(){}",
        "var JSCompiler_StaticMethods_foo = ",
        "  function(JSCompiler_StaticMethods_foo$self, args) {return args};",
        "var o = new a;",
        "JSCompiler_StaticMethods_foo(o)");
    test(source, expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod2
  public void testRewriteImplementedMethod2() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype['foo'] = function(args) {return args};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod3
  public void testRewriteImplementedMethod3() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return args};",
        "var o = new a;",
        "o['foo']");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod4
  public void testRewriteImplementedMethod4() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype['foo'] = function(args) {return args};",
        "var o = new a;",
        "o['foo']");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethodInObj
  public void testRewriteImplementedMethodInObj() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {foo: function(args) {return args}};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

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

// com.google.javascript.jscomp.DiagnosticGroupTest::testRegistration
  public void testRegistration() throws Exception {
    DiagnosticGroups dg = new DiagnosticGroups();
    assertEquals(DiagnosticGroups.DEPRECATED,
        dg.forName("deprecated"));
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
    
    
    String desired = "{a=[[Foo.prototype]]}";
    String expected = "{}";
    testSets(false, js, js, expected);

    
    
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
    
    
    String desired = "{a=[[Foo.prototype]]}";
    String expected = "{}";
    testSets(false, js, js, expected);

    
    
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

    
    
    testSets(false, js, js, "{}");
    
    
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

    
    
    testSets(false, js, js, "{}");
    
    
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
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Bar_prototype$a=0;"
        + "Foo.prototype.Bar_prototype$b=0;"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "Bar.prototype.Bar_prototype$b=0;"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0;"
        + "B.Bar_prototype$b=0;"
        + "function Baz(){}"
        + "Baz.prototype.a$Baz_prototype=0;"
        + "Baz.prototype.b$Baz_prototype=0;";
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
        + "function foo(foo) {\n"
        + "  var x = foo.Bar_prototype$a;\n"
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
        + "Foo.prototype.f = function() {"
        + "  Foo.superClass_.f();"
        + "};\n"
        + " function Bar() {}\n"
        + "goog.inherits(Bar, Foo);\n"
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
        + "Foo.prototype.a = 0;\n"
        + "(new Foo).a = 0;"
        + " function I() {};\n"
        + "I.prototype.a;\n";
    testSets(false, js, "{}");
    testSets(true, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testMultipleInterfaces
  public void testMultipleInterfaces() {
    String js = ""
        + " function I() {};\n"
        + " function I2() {};\n"
        + "I2.prototype.a;\n"
        + ""
        + "function Foo() {};\n"
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

// com.google.javascript.jscomp.ExploitAssignsTest::testExprExploitationTypes
  public void testExprExploitationTypes() {
    test("a = true; b = true",
         "b = a = true");
    test("a = !0; b = !0",
         "b = a = !0");
    test("a = !1; b = !1",
         "b = a = !1");
    test("a = void 0; b = void 0",
         "b = a = void 0");
    test("a = -Infinity; b = -Infinity",
         "b = a = -Infinity");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testExprExploitationTypes2
  public void testExprExploitationTypes2() {
    test("a = !0; b = !0",
         "b = a = !0");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testExprExploitation
  public void testExprExploitation() {
    test("a = null; b = null; var c = b",
         "var c = b = a = null");
    test("a = null; b = null",
         "b = a = null");
    test("a = undefined; b = undefined",
         "b = a = undefined");
    test("a = 0; b = 0", "b=a=0");
    test("a = 'foo'; b = 'foo'",
         "b = a = \"foo\"");
    test("a = c; b = c", "b=a=c");

    testSame("a = 0; b = 1");
    testSame("a = \"foo\"; b = \"foox\"");

    test("a = null; a && b;", "(a = null)&&b");
    test("a = null; a || b;", "(a = null)||b");

    test("a = null; a ? b : c;", "(a = null) ? b : c");

    test("a = null; this.foo = null;",
         "this.foo = a = null");
    test("function f(){ a = null; return null; }",
         "function f(){return a = null}");

    test("a = true; if (a) { foo(); }",
         "if (a = true) { foo() }");
    test("a = true; if (a && a) { foo(); }",
         "if ((a = true) && a) { foo() }");
    test("a = false; if (a) { foo(); }",
         "if (a = false) { foo() }");

    test("a = !0; if (a) { foo(); }",
        "if (a = !0) { foo() }");
    test("a = !0; if (a && a) { foo(); }",
        "if ((a = !0) && a) { foo() }");
    test("a = !1; if (a) { foo(); }",
        "if (a = !1) { foo() }");

    testSame("a = this.foo; a();");
    test("a = b; b = a;",
         "b = a = b");
    testSame("a = b; a.c = a");
    test("this.foo = null; this.bar = null;",
         "this.bar = this.foo = null");
    test("this.foo = null; this.bar = null; this.baz = this.bar",
         "this.baz = this.bar = this.foo = null");
    test("this.foo = null; a = null;",
         "a = this.foo = null");
    test("this.foo = null; a = this.foo;",
         "a = this.foo = null");
    test("a.b.c=null; a=null;",
         "a = a.b.c = null");
    testSame("a = null; a.b.c = null");
    test("(a=b).c = null; this.b = null;",
         "this.b = (a=b).c = null");
    testSame("if(x) a = null; else b = a");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testNestedExprExploitation
  public void testNestedExprExploitation() {
    test("this.foo = null; this.bar = null; this.baz = null;",
         "this.baz = this.bar = this.foo = null");

    test("a = 3; this.foo = a; this.bar = a; this.baz = 3;",
         "this.baz = this.bar = this.foo = a = 3");
    test("a = 3; this.foo = a; this.bar = this.foo; this.baz = a;",
         "this.baz = this.bar = this.foo = a = 3");
    test("a = 3; this.foo = a; this.bar = 3; this.baz = this.foo;",
         "this.baz = this.bar = this.foo = a = 3");
    test("a = 3; this.foo = a; a = 3; this.bar = 3; " +
         "a = 3; this.baz = this.foo;",
         "this.baz = a = this.bar = a = this.foo = a = 3");

    test("a = 4; this.foo = a; a = 3; this.bar = 3; " +
         "a = 3; this.baz = this.foo;",
         "this.foo = a = 4; a = this.bar = a = 3; this.baz = this.foo");
    test("a = 3; this.foo = a; a = 4; this.bar = 3; " +
         "a = 3; this.baz = this.foo;",
         "this.foo = a = 3; a = 4; a = this.bar = 3; this.baz = this.foo");
    test("a = 3; this.foo = a; a = 3; this.bar = 3; " +
         "a = 4; this.baz = this.foo;",
         "this.bar = a = this.foo = a = 3; a = 4; this.baz = this.foo");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testBug1840071
  public void testBug1840071() {
    
    
    test("a.b = a.x; if (a.x) {}", "if (a.b = a.x) {}");
    testSame("a.b = a.x; if (a.b) {}");
    test("a.b = a.c = a.x; if (a.x) {}", "if (a.b = a.c = a.x) {}");
    testSame("a.b = a.c = a.x; if (a.c) {}");
    testSame("a.b = a.c = a.x; if (a.b) {}");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testBug2072343
  public void testBug2072343() {
    testSame("a = a.x;a = a.x");
    testSame("a = a.x;b = a.x");
    test("b = a.x;a = a.x", "a = b = a.x");
    testSame("a.x = a;a = a.x");
    testSame("a.b = a.b.x;a.b = a.b.x");
    testSame("a.y = a.y.x;b = a.y;c = a.y.x");
    test("a = a.x;b = a;c = a.x", "b = a = a.x;c = a.x");
    test("b = a.x;a = b;c = a.x", "a = b = a.x;c = a.x");
 }

// com.google.javascript.jscomp.ExploitAssignsTest::testBadCollapseIntoCall
  public void testBadCollapseIntoCall() {
    
    
    testSame("this.foo = function() {}; this.foo();");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testBadCollapse
  public void testBadCollapse() {
    testSame("this.$e$ = []; this.$b$ = null;");
  }

// com.google.javascript.jscomp.ExportTestFunctionsTest::testFunctionsAreExported
  public void testFunctionsAreExported() {
    test(TEST_FUNCTIONS_WITH_NAMES,
        "function Foo(arg){}; "
        + "function setUp(arg3){} google_exportSymbol(\"setUp\",setUp);; "
        + "function tearDown(arg,arg2) {} "
        + "google_exportSymbol(\"tearDown\",tearDown);; "
        + "function testBar(arg){} google_exportSymbol(\"testBar\",testBar)"
    );
  }

// com.google.javascript.jscomp.ExportTestFunctionsTest::testBasicTestFunctionsAreExported
  public void testBasicTestFunctionsAreExported() {
    test("function Foo() {function testA() {}}",
         "function Foo() {function testA(){}}");
    test("function setUp() {}",
         "function setUp(){} google_exportSymbol(\"setUp\",setUp)");
    test("function setUpPage() {}",
         "function setUpPage(){} google_exportSymbol(\"setUpPage\",setUpPage)");
    test("function tearDown() {}",
         "function tearDown(){} google_exportSymbol(\"tearDown\",tearDown)");
    test("function tearDownPage() {}",
         "function tearDownPage(){} google_exportSymbol(\"tearDownPage\"," +
         "tearDownPage)");
    test("function testBar() { function testB() {}}",
         "function testBar(){function testB(){}}"
             + "google_exportSymbol(\"testBar\",testBar)");
    testSame("var testCase = {}; testCase.setUpPage = function() {}");
  }

// com.google.javascript.jscomp.ExportTestFunctionsTest::testFunctionExpressionsAreExported
  public void testFunctionExpressionsAreExported() {
    test("var Foo = function() {var testA = function() {}}",
         "var Foo = function() {var testA = function() {}}");
    test("var setUp = function() {}",
         "var setUp = function() {}; " +
         "google_exportSymbol(\"setUp\",setUp)");
    test("var setUpPage = function() {}",
         "var setUpPage = function() {}; " +
         "google_exportSymbol(\"setUpPage\",setUpPage)");
    test("var tearDown = function() {}",
         "var tearDown = function() {}; " +
         "google_exportSymbol(\"tearDown\",tearDown)");
    test("var tearDownPage = function() {}",
         "var tearDownPage = function() {}; " +
         "google_exportSymbol(\"tearDownPage\", tearDownPage)");
    test("var testBar = function() { var testB = function() {}}",
         "var testBar = function(){ var testB = function() {}}; " +
         "google_exportSymbol(\"testBar\",testBar)");
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

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression1
  public void testMoveExpression1() {
    
    helperMoveExpression("foo()", "foo", "var temp$$0 = foo(); temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression2
  public void testMoveExpression2() {
    helperMoveExpression(
        "x = foo()",
        "foo",
        "var temp$$0 = foo(); x = temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression3
  public void testMoveExpression3() {
    helperMoveExpression(
        "var x = foo()",
        "foo",
        "var temp$$0 = foo(); var x = temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression4
  public void testMoveExpression4() {
    helperMoveExpression(
        "if(foo()){}",
        "foo",
        "var temp$$0 = foo(); if (temp$$0);");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression5
  public void testMoveExpression5() {
    helperMoveExpression(
        "switch(foo()){}",
        "foo",
        "var temp$$0 = foo(); switch(temp$$0){}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression6
  public void testMoveExpression6() {
    helperMoveExpression(
        "switch(1 + foo()){}",
        "foo",
        "var temp$$0 = foo(); switch(1 + temp$$0){}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression7
  public void testMoveExpression7() {
    helperMoveExpression(
        "function f(){ return foo();}",
        "foo",
        "function f(){ var temp$$0 = foo(); return temp$$0;}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression8
  public void testMoveExpression8() {
    helperMoveExpression(
        "x = foo() && 1",
        "foo",
        "var temp$$0 = foo(); x = temp$$0 && 1");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression9
  public void testMoveExpression9() {
    helperMoveExpression(
        "x = foo() || 1",
        "foo",
        "var temp$$0 = foo(); x = temp$$0 || 1");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression10
  public void testMoveExpression10() {
    helperMoveExpression(
        "x = foo() ? 0 : 1",
        "foo",
        "var temp$$0 = foo(); x = temp$$0 ? 0 : 1");
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
        "var temp$$0=foo();var x = {get a() {}, b: temp$$0};");

    helperMoveExpression(
        "var x = {set a(p) {}, b: foo()};",
        "foo",
        "var temp$$0=foo();var x = {set a(p) {}, b: temp$$0};");
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
                    "var a = {};\n" +
                    "a.b = {};\n" +
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

                    "var a = {};\n" +
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

                    "var hello = {};\n" +
                    "hello.b = {};\n" +
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
                    "var foobar = {};\n");
  }
