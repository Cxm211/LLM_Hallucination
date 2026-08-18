// buggy code
  private void removeDuplicateDeclarations(Node externs, Node root) {
    Callback tickler = new ScopeTicklingCallback();
    ScopeCreator scopeCreator =  new SyntacticScopeCreator(
        compiler, new DuplicateDeclarationHandler());
    NodeTraversal t = new NodeTraversal(compiler, tickler, scopeCreator);
    t.traverseRoots(externs, root);
  }

    public void onRedeclaration(
        Scope s, String name, Node n, Node parent, Node gramps,
        Node nodeWithLineNumber) {
      Preconditions.checkState(n.getType() == Token.NAME);
      Var v = s.getVar(name);

        // We allow variables to be duplicate declared if one
        // declaration appears in source and the other in externs.
        // This deals with issues where a browser built-in is declared
        // in one browser but not in another.

      // If name is "arguments", Var maybe null.
      if (v != null && v.getParentNode().getType() == Token.CATCH) {
        // Redeclaration of a catch expression variable is hard to model
        // without support for "with" expressions.
        // The EcmaScript spec (section 12.14), declares that a catch
        // "catch (e) {}" is handled like "with ({'e': e}) {}" so that
        // "var e" would refer to the scope variable, but any following
        // reference would still refer to "e" of the catch expression.
        // Until we have support for this disallow it.
        // Currently the Scope object adds the catch expression to the
        // function scope, which is technically not true but a good
        // approximation for most uses.

        // TODO(johnlenz): Consider improving how scope handles catch
        // expression.

        // Use the name of the var before it was made unique.
        name = MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName(
            name);
        compiler.report(
            JSError.make(
                NodeUtil.getSourceName(nodeWithLineNumber), nodeWithLineNumber,
                CATCH_BLOCK_VAR_ERROR, name));
      } else if (v != null && parent.getType() == Token.FUNCTION) {
        if (v.getParentNode().getType() == Token.VAR) {
          s.undeclare(v);
          s.declare(name, n, n.getJSType(), v.input);
          replaceVarWithAssignment(v.getNameNode(), v.getParentNode(),
              v.getParentNode().getParent());
        }
      } else if (parent.getType() == Token.VAR) {
        Preconditions.checkState(parent.hasOneChild());

        replaceVarWithAssignment(n, parent, gramps);
      }
    }

  private void scanRoot(Node n, Scope parent) {
    if (n.getType() == Token.FUNCTION) {
      sourceName = (String) n.getProp(Node.SOURCENAME_PROP);

      final Node fnNameNode = n.getFirstChild();
      final Node args = fnNameNode.getNext();
      final Node body = args.getNext();

      // Bleed the function name into the scope, if it hasn't
      // been declared in the outer scope.
      String fnName = fnNameNode.getString();
      if (!fnName.isEmpty() && NodeUtil.isFunctionExpression(n)) {
        declareVar(fnName, fnNameNode, n, null, null, n);
      }

      // Args: Declare function variables
      Preconditions.checkState(args.getType() == Token.LP);
      for (Node a = args.getFirstChild(); a != null;
           a = a.getNext()) {
        Preconditions.checkState(a.getType() == Token.NAME);
        declareVar(a.getString(), a, args, n, null, n);
      }

      // Body
      scanVars(body, n);
    } else {
      // It's the global block
      Preconditions.checkState(scope.getParent() == null);
      scanVars(n, null);
    }
  }

  private void scanVars(Node n, Node parent) {
    switch (n.getType()) {
      case Token.VAR:
        // Declare all variables. e.g. var x = 1, y, z;
        for (Node child = n.getFirstChild();
             child != null;) {
          Node next = child.getNext();
          Preconditions.checkState(child.getType() == Token.NAME);
          String name = child.getString();
          declareVar(name, child, n, parent, null, n);
          child = next;
        }
        return;

      case Token.FUNCTION:
        if (NodeUtil.isFunctionExpression(n)) {
          return;
        }

        String fnName = n.getFirstChild().getString();
        if (fnName.isEmpty()) {
          // This is invalid, but allow it so the checks can catch it.
          return;
        }
        declareVar(fnName, n.getFirstChild(), n, parent, null, n);
        return;   // should not examine function's children

      case Token.CATCH:
        Preconditions.checkState(n.getChildCount() == 2);
        Preconditions.checkState(n.getFirstChild().getType() == Token.NAME);
        // the first child is the catch var and the third child
        // is the code block

        final Node var = n.getFirstChild();
        final Node block = var.getNext();

        declareVar(var.getString(), var, n, parent, null, n);
        scanVars(block, n);
        return;  // only one child to scan

      case Token.SCRIPT:
        sourceName = (String) n.getProp(Node.SOURCENAME_PROP);
        break;
    }

    // Variables can only occur in statement-level nodes, so
    // we only need to traverse children in a couple special cases.
    if (NodeUtil.isControlStructure(n) || NodeUtil.isStatementBlock(n)) {
      for (Node child = n.getFirstChild();
           child != null;) {
        Node next = child.getNext();
        scanVars(child, n);
        child = next;
      }
    }
  }

    void onRedeclaration(
        Scope s, String name,
        Node n, Node parent, Node gramps, Node nodeWithLineNumber);

    public void onRedeclaration(
        Scope s, String name,
        Node n, Node parent, Node gramps, Node nodeWithLineNumber) {

      // Don't allow multiple variables to be declared at the top level scope
      if (scope.isGlobal()) {
        Scope.Var origVar = scope.getVar(name);
        Node origParent = origVar.getParentNode();
        if (origParent.getType() == Token.CATCH &&
            parent.getType() == Token.CATCH) {
          // Okay, both are 'catch(x)' variables.
          return;
        }

        boolean allowDupe = false;
        JSDocInfo info = n.getJSDocInfo();
        if (info == null) {
          info = parent.getJSDocInfo();
        }
        allowDupe =
            info != null && info.getSuppressions().contains("duplicate");

        if (!allowDupe) {
          compiler.report(
              JSError.make(sourceName, n,
                           VAR_MULTIPLY_DECLARED_ERROR,
                           name,
                           (origVar.input != null
                            ? origVar.input.getName()
                            : "??")));
        }
      } else if (name.equals(ARGUMENTS) && !NodeUtil.isVarDeclaration(n)) {
        // Disallow shadowing "arguments" as we can't handle with our current
        // scope modeling.
        compiler.report(
            JSError.make(sourceName, n,
                VAR_ARGUMENTS_SHADOWED_ERROR));
      }
    }

  private void declareVar(String name, Node n, Node parent,
                          Node gramps, com.google.javascript.rhino.jstype.JSType declaredType,
                          Node nodeWithLineNumber) {

    if (scope.isDeclared(name, false)
        || (scope.isLocal() && name.equals(ARGUMENTS))) {
      redeclarationHandler.onRedeclaration(
          scope, name, n, parent, gramps, nodeWithLineNumber);
    } else {
      scope.declare(name, n, declaredType, compiler.getInput(sourceName));
    }
  }

// relevant test
// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveAssign1
  public void testNoRemoveAssign1() {
    testSame("a = 1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveAssign2
  public void testNoRemoveAssign2() {
    testSame("a = b = 1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveAssign3
  public void testNoRemoveAssign3() {
    test("1 + (a = 2)", "a = 2");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveAssign4
  public void testNoRemoveAssign4() {
    testSame("x.a = 1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveAssign5
  public void testNoRemoveAssign5() {
    testSame("x.a = x.b = 1");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveAssign6
  public void testNoRemoveAssign6() {
    test("1 + (x.a = 2)", "x.a = 2");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveCall1
  public void testNoRemoveCall1() {
    testSame("a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveCall2
  public void testNoRemoveCall2() {
    test("a()+b()", "a();b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveCall3
  public void testNoRemoveCall3() {
    testSame("a() && b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveCall4
  public void testNoRemoveCall4() {
    testSame("a() || b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveCall5
  public void testNoRemoveCall5() {
    test("a() || 1", "a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveCall6
  public void testNoRemoveCall6() {
    testSame("1 || a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveThrow1
  public void testNoRemoveThrow1() {
    testSame("function f(){throw a()}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveThrow2
  public void testNoRemoveThrow2() {
    testSame("function f(){throw a}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveThrow3
  public void testNoRemoveThrow3() {
    testSame("function f(){throw 10}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveInControlStructure1
  public void testRemoveInControlStructure1() {
    test("if(x()) 1", "x()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveInControlStructure2
  public void testRemoveInControlStructure2() {
    test("while(2) 1", "while(2);");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveInControlStructure3
  public void testRemoveInControlStructure3() {
    test("for(1;2;3) 4", "for(;;);");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook1
  public void testHook1() {
    test("1 ? 2 : 3", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook2
  public void testHook2() {
    test("x ? a() : 3", "x && a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook3
  public void testHook3() {
    test("x ? 2 : a()", "x || a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook4
  public void testHook4() {
    testSame("x ? a() : b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook5
  public void testHook5() {
    test("a() ? 1 : 2", "a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook6
  public void testHook6() {
    test("a() ? b() : 2", "a() && b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook7
  public void testHook7() {
    test("a() ? 1 : b()", "a() || b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testHook8
  public void testHook8() {
    testSame("a() ? b() : c()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testShortCircuit1
  public void testShortCircuit1() {
    testSame("1 && a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testShortCircuit2
  public void testShortCircuit2() {
    test("1 && a() && 2", "1 && a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testShortCircuit3
  public void testShortCircuit3() {
    test("a() && 1 && 2", "a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testShortCircuit4
  public void testShortCircuit4() {
    testSame("a() && 1 && b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testComma1
  public void testComma1() {
    test("1, 2", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testComma2
  public void testComma2() {
    test("1, a()", "a()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testComma3
  public void testComma3() {
    test("1, a(), b()", "a();b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testComma4
  public void testComma4() {
    test("a(), b()", "a();b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testComma5
  public void testComma5() {
    test("a(), b(), 1", "a();b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testComplex1
  public void testComplex1() {
    test("1 && a() + b() + c()", "1 && (a(), b(), c())");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testComplex2
  public void testComplex2() {
    test("1 && (a() ? b() : 1)", "1 && a() && b()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testComplex3
  public void testComplex3() {
    test("1 && (a() ? b() : 1 + c())", "1 && (a() ? b() : c())");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testComplex4
  public void testComplex4() {
    test("1 && (a() ? 1 : 1 + c())", "1 && (a() || c())");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testComplex5
  public void testComplex5() {
    
    testSame("(a() ? 1 : 1 + c()) && foo()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveFunctionDeclaration1
  public void testNoRemoveFunctionDeclaration1() {
    testSame("function foo(){}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveFunctionDeclaration2
  public void testNoRemoveFunctionDeclaration2() {
    testSame("var foo = function (){}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoSimplifyFunctionArgs1
  public void testNoSimplifyFunctionArgs1() {
    testSame("f(1 + 2, 3 + g())");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoSimplifyFunctionArgs2
  public void testNoSimplifyFunctionArgs2() {
    testSame("1 && f(1 + 2, 3 + g())");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoSimplifyFunctionArgs3
  public void testNoSimplifyFunctionArgs3() {
    testSame("1 && foo(a() ? b() : 1 + c())");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveInherits1
  public void testNoRemoveInherits1() {
    testSame("var a = {}; this.b = {}; var goog = {}; goog.inherits(b, a)");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveInherits2
  public void testNoRemoveInherits2() {
    test("var a = {}; this.b = {}; var goog = {}; goog.inherits(b, a) + 1",
         "var a = {}; this.b = {}; var goog = {}; goog.inherits(b, a)");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveInherits3
  public void testNoRemoveInherits3() {
    testSame("this.a = {}; var b = {}; b.inherits(a);");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNoRemoveInherits4
  public void testNoRemoveInherits4() {
    test("this.a = {}; var b = {}; b.inherits(a) + 1;",
         "this.a = {}; var b = {}; b.inherits(a)");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveFromLabel1
  public void testRemoveFromLabel1() {
    test("LBL: void 0", "LBL: {}");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testRemoveFromLabel2
  public void testRemoveFromLabel2() {
    test("LBL: foo() + 1 + bar()", "LBL: foo(),bar()");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testCall1
  public void testCall1() {
    test("Math.sin(0);", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testCall2
  public void testCall2() {
    test("1 + Math.sin(0);", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNew1
  public void testNew1() {
    test("new Date;", "");
  }

// com.google.javascript.jscomp.PeepholeRemoveDeadCodeTest::testNew2
  public void testNew2() {
    test("1 + new Date;", "");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldOneChildBlocks
  public void testFoldOneChildBlocks() {
    fold("function(){if(x)a();x=3}",
        "function(){x&&a();x=3}");
    fold("function(){if(x){a()}x=3}",
        "function(){x&&a();x=3}");
    fold("function(){if(x){return 3}}",
        "function(){if(x)return 3}");
    fold("function(){if(x){a()}}",
        "function(){x&&a()}");
    fold("function(){if(x){throw 1}}", "function(){if(x)throw 1;}");

    
    fold("function(){if(x){foo()}}", "function(){x&&foo()}");
    fold("function(){if(x){foo()}else{bar()}}",
         "function(){x?foo():bar()}");

    
    fold("function(){if(x){a.b=1}}", "function(){if(x)a.b=1}");
    fold("function(){if(x){a.b*=1}}", "function(){if(x)a.b*=1}");
    fold("function(){if(x){a.b+=1}}", "function(){if(x)a.b+=1}");
    fold("function(){if(x){++a.b}}", "function(){x&&++a.b}");
    fold("function(){if(x){a.foo()}}", "function(){x&&a.foo()}");

    
    fold("function(){try{foo()}catch(e){bar(e)}finally{baz()}}",
         "function(){try{foo()}catch(e){bar(e)}finally{baz()}}");

    
    fold("function(){switch(x){case 1:break}}",
         "function(){switch(x){case 1:break}}");

    
    fold("function(){if(e1){do foo();while(e2)}else foo2()}",
         "function(){if(e1){do foo();while(e2)}else foo2()}");
    
    fold("if(x){do{foo()}while(y)}else bar()",
         "if(x){do foo();while(y)}else bar()");

    
    fold("function(){if(x){if(y)foo()}}",
         "function(){x&&y&&foo()}");
    fold("function(){if(x){if(y)foo();else bar()}}",
         "function(){if(x)y?foo():bar()}");
    fold("function(){if(x){if(y)foo()}else bar()}",
         "function(){if(x)y&&foo();else bar()}");
    fold("function(){if(x){if(y)foo();else bar()}else{baz()}}",
         "function(){if(x)y?foo():bar();else baz()}");

    fold("if(e1){while(e2){if(e3){foo()}}}else{bar()}",
         "if(e1)while(e2)e3&&foo();else bar()");

    fold("if(e1){with(e2){if(e3){foo()}}}else{bar()}",
         "if(e1)with(e2)e3&&foo();else bar()");

    fold("if(x){if(y){var x;}}", "if(x)if(y)var x");
    fold("if(x){ if(y){var x;}else{var z;} }",
         "if(x)if(y)var x;else var z");

    
    
    
    fold("if(x){ if(y){var x;}else{var z;} }else{var w}",
         "if(x)if(y)var x;else var z;else var w");
    fold("if (x) {var x;}else { if (y) { var y;} }",
         "if(x)var x;else if(y)var y");

    
    fold("if(a){if(b){f1();f2();}else if(c){f3();}}else {if(d){f4();}}",
         "if(a)if(b){f1();f2()}else c&&f3();else d&&f4()");

    fold("function(){foo()}", "function(){foo()}");
    fold("switch(x){case y: foo()}", "switch(x){case y:foo()}");
    fold("try{foo()}catch(ex){bar()}finally{baz()}",
         "try{foo()}catch(ex){bar()}finally{baz()}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldReturns
  public void testFoldReturns() {
    fold("function(){if(x)return 1;else return 2}",
         "function(){return x?1:2}");
    fold("function(){if(x)return 1+x;else return 2-x}",
         "function(){return x?1+x:2-x}");
    fold("function(){if(x)return y += 1;else return y += 2}",
         "function(){return x?(y+=1):(y+=2)}");

    
    foldSame("function(){if(x)return;else return 2-x}");
    foldSame("function(){if(x)return x;else return}");

    foldSame("function(){for(var x in y) { return x.y; } return k}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldAssignments
  public void testFoldAssignments() {
    fold("function(){if(x)y=3;else y=4;}", "function(){y=x?3:4}");
    fold("function(){if(x)y=1+a;else y=2+a;}", "function(){y=x?1+a:2+a}");

    
    fold("function(){if(x)y+=1;else y+=2;}", "function(){y+=x?1:2}");
    fold("function(){if(x)y-=1;else y-=2;}", "function(){y-=x?1:2}");
    fold("function(){if(x)y%=1;else y%=2;}", "function(){y%=x?1:2}");
    fold("function(){if(x)y|=1;else y|=2;}", "function(){y|=x?1:2}");

    
    foldSame("function(){if(x)y-=1;else y+=2}");

    
    foldSame("function(){if(x)y-=1;else z-=1}");

    
    foldSame("function(){if(x)y().a=3;else y().a=4}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testRemoveDuplicateStatements
  public void testRemoveDuplicateStatements() {
    fold("if (a) { x = 1; x++ } else { x = 2; x++ }",
         "x=(a) ? 1 : 2; x++");
    fold("if (a) { x = 1; x++; y += 1; z = pi; }" +
         " else  { x = 2; x++; y += 1; z = pi; }",
         "x=(a) ? 1 : 2; x++; y += 1; z = pi;");
    fold("function z() {" +
         "if (a) { foo(); return true } else { goo(); return true }" +
         "}",
         "function z() {(a) ? foo() : goo(); return true}");
    fold("function z() {if (a) { foo(); x = true; return true " +
         "} else { goo(); x = true; return true }}",
         "function z() {(a) ? foo() : goo(); x = true; return true}");

    fold("function z() {" +
         "  if (a) { bar(); foo(); return true }" +
         "    else { bar(); goo(); return true }" +
         "}",
         "function z() {" +
         "  if (a) { bar(); foo(); }" +
         "    else { bar(); goo(); }" +
         "  return true;" +
         "}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testNotCond
  public void testNotCond() {
    fold("function(){if(!x)foo()}", "function(){x||foo()}");
    fold("function(){if(!x)b=1}", "function(){x||(b=1)}");
    fold("if(!x)z=1;else if(y)z=2", "if(x){if(y)z=2}else z=1");
    foldSame("function(){if(!(x=1))a.b=1}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testAndParenthesesCount
  public void testAndParenthesesCount() {
    foldSame("function(){if(x||y)a.foo()}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldLogicalOpStringCompare
  public void testFoldLogicalOpStringCompare() {
    
    
    assertResultString("if(foo() && false) z()", "foo()&&0&&z()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldNot
  public void testFoldNot() {
    fold("while(!(x==y)){a=b;}" , "while(x!=y){a=b;}");
    fold("while(!(x!=y)){a=b;}" , "while(x==y){a=b;}");
    fold("while(!(x===y)){a=b;}", "while(x!==y){a=b;}");
    fold("while(!(x!==y)){a=b;}", "while(x===y){a=b;}");
    
    foldSame("while(!(x>y)){a=b;}");
    foldSame("while(!(x>=y)){a=b;}");
    foldSame("while(!(x<y)){a=b;}");
    foldSame("while(!(x<=y)){a=b;}");
    foldSame("while(!(x<=NaN)){a=b;}");

    
    fold("x = !(y() && true)", "x = !y()");
    
    fold("x = !true", "x = !1");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldRegExpConstructor
  public void testFoldRegExpConstructor() {
    enableNormalize();

    
    fold("x = new RegExp",                    "x = RegExp()");
    
    fold("x = new RegExp(\"\")",              "x = RegExp(\"\")");
    fold("x = new RegExp(\"\", \"i\")",       "x = RegExp(\"\",\"i\")");
    
    fold("x = new RegExp(\"foobar\", \"bogus\")",
         "x = RegExp(\"foobar\",\"bogus\")",
         PeepholeSubstituteAlternateSyntax.INVALID_REGULAR_EXPRESSION_FLAGS);
    
    fold("x = new RegExp(\"foobar\", \"g\")",
         "x = RegExp(\"foobar\",\"g\")");
    fold("x = new RegExp(\"foobar\", \"ig\")",
         "x = RegExp(\"foobar\",\"ig\")");

    
    fold("x = new RegExp(\"foobar\")",        "x = /foobar/");
    fold("x = RegExp(\"foobar\")",            "x = /foobar/");
    fold("x = new RegExp(\"foobar\", \"i\")", "x = /foobar/i");
    
    fold("x = new RegExp(\"\\\\.\", \"i\")",  "x = /\\./i");
    fold("x = new RegExp(\"/\", \"\")",       "x = /\\//");
    fold("x = new RegExp(\"///\", \"\")",     "x = /\\/\\/\\//");
    fold("x = new RegExp(\"\\\\\\/\", \"\")", "x = /\\//");
    
    
    fold("x = new RegExp(\"\\u2028\")", "x = RegExp(\"\\u2028\")");
    fold("x = new RegExp(\"\\\\\\\\u2028\")", "x = /\\\\u2028/");

    
    
    String longRegexp = "";
    for (int i = 0; i < 200; i++) longRegexp += "x";
    foldSame("x = RegExp(\"" + longRegexp + "\")");

    
    
    disableNormalize();

    foldSame("x = new RegExp(\"foobar\")");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldRegExpConstructorStringCompare
  public void testFoldRegExpConstructorStringCompare() {
    
    
    assertResultString("x=new RegExp(\"\\n\", \"i\")", "x=/\\n/i", true);
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testContainsUnicodeEscape
  public void testContainsUnicodeEscape() throws Exception {
    assertTrue(!PeepholeSubstituteAlternateSyntax.containsUnicodeEscape(""));
    assertTrue(!PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("foo"));
    assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape(
        "\u2028"));
    assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape(
        "\\u2028"));
    assertTrue(
        PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("foo\\u2028"));
    assertTrue(!PeepholeSubstituteAlternateSyntax.containsUnicodeEscape(
        "foo\\\\u2028"));
    assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape(
            "foo\\\\u2028bar\\u2028"));
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldLiteralObjectConstructors
  public void testFoldLiteralObjectConstructors() {
    enableNormalize();

    
    fold("x = new Object", "x = ({})");
    fold("x = new Object()", "x = ({})");
    fold("x = Object()", "x = ({})");

    disableNormalize();
    
    foldSame("x = new Object");
    foldSame("x = new Object()");
    foldSame("x = Object()");

    enableNormalize();

    
    foldSame("x = " +
         "(function(){function Object(){this.x=4};return new Object();})();");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldLiteralArrayConstructors
  public void testFoldLiteralArrayConstructors() {
    enableNormalize();

    
    fold("x = new Array", "x = []");
    fold("x = new Array()", "x = []");
    fold("x = Array()", "x = []");

    
    fold("x = new Array(0)", "x = []");
    fold("x = Array(0)", "x = []");
    fold("x = new Array(\"a\")", "x = [\"a\"]");
    fold("x = Array(\"a\")", "x = [\"a\"]");

    
    fold("x = new Array(7)", "x = Array(7)");
    fold("x = Array(7)", "x = Array(7)");
    fold("x = new Array(y)", "x = Array(y)");
    fold("x = Array(y)", "x = Array(y)");
    fold("x = new Array(foo())", "x = Array(foo())");
    fold("x = Array(foo())", "x = Array(foo())");

    
    fold("x = new Array(1, 2, 3, 4)", "x = [1, 2, 3, 4]");
    fold("x = Array(1, 2, 3, 4)", "x = [1, 2, 3, 4]");
    fold("x = new Array('a', 1, 2, 'bc', 3, {}, 'abc')",
         "x = ['a', 1, 2, 'bc', 3, {}, 'abc']");
    fold("x = Array('a', 1, 2, 'bc', 3, {}, 'abc')",
         "x = ['a', 1, 2, 'bc', 3, {}, 'abc']");
    fold("x = new Array(Array(1, '2', 3, '4'))", "x = [[1, '2', 3, '4']]");
    fold("x = Array(Array(1, '2', 3, '4'))", "x = [[1, '2', 3, '4']]");
    fold("x = new Array(Object(), Array(\"abc\", Object(), Array(Array())))",
         "x = [{}, [\"abc\", {}, [[]]]");
    fold("x = new Array(Object(), Array(\"abc\", Object(), Array(Array())))",
         "x = [{}, [\"abc\", {}, [[]]]");

    disableNormalize();
    
    foldSame("x = new Array");
    foldSame("x = new Array()");
    foldSame("x = Array()");

    foldSame("x = new Array(0)");
    foldSame("x = Array(0)");
    foldSame("x = new Array(\"a\")");
    foldSame("x = Array(\"a\")");
    foldSame("x = new Array(7)");
    foldSame("x = Array(7)");
    foldSame("x = new Array(foo())");
    foldSame("x = Array(foo())");

    foldSame("x = new Array(1, 2, 3, 4)");
    foldSame("x = Array(1, 2, 3, 4)");
    foldSame("x = new Array('a', 1, 2, 'bc', 3, {}, 'abc')");
    foldSame("x = Array('a', 1, 2, 'bc', 3, {}, 'abc')");
    foldSame("x = new Array(Array(1, '2', 3, '4'))");
    foldSame("x = Array(Array(1, '2', 3, '4'))");
    foldSame("x = new Array(Object(), Array(\"abc\", Object(), Array(Array())))");
    foldSame("x = new Array(Object(), Array(\"abc\", Object(), Array(Array())))");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testMinimizeExprCondition
  public void testMinimizeExprCondition() {
    fold("(x ? true : false) && y()", "x&&y()");
    fold("(x ? false : true) && y()", "(!x)&&y()");
    fold("(x ? true : y) && y()", "(x || y)&&y()");
    fold("(x ? y : false) && y()", "(x && y)&&y()");
    fold("(x && true) && y()", "x && y()");
    fold("(x && false) && y()", "0&&y()");
    fold("(x || true) && y()", "1&&y()");
    fold("(x || false) && y()", "x&&y()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testMinimizeWhileCondition
  public void testMinimizeWhileCondition() {
    
    fold("while(!!true) foo()", "while(1) foo()");
    
    fold("while(!!x) foo()", "while(x) foo()");
    fold("while(!(!x&&!y)) foo()", "while(x||y) foo()");
    fold("while(x||!!y) foo()", "while(x||y) foo()");
    fold("while(!(!!x&&y)) foo()", "while(!(x&&y)) foo()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testMinimizeForCondition
  public void testMinimizeForCondition() {
    
    
    fold("for(;!!true;) foo()", "for(;1;) foo()");
    
    fold("for(!!true;;) foo()", "for(!!1;;) foo()");

    
    fold("for(;!!x;) foo()", "for(;x;) foo()");

    
    foldSame("for(a in b) foo()");
    foldSame("for(a in {}) foo()");
    foldSame("for(a in []) foo()");
    fold("for(a in !!true) foo()", "for(a in !!1) foo()");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testMinimizeCondition_example1
  public void testMinimizeCondition_example1() {
    
    fold("if(!!(f() > 20)) {foo();foo()}", "if(f() > 20){foo();foo()}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldConditionalVarDeclaration
  public void testFoldConditionalVarDeclaration() {
    fold("if(x) var y=1;else y=2", "var y=x?1:2");
    fold("if(x) y=1;else var y=2", "var y=x?1:2");

    foldSame("if(x) var y = 1; z = 2");
    foldSame("if(x) y = 1; var z = 2");

    foldSame("if(x) { var y = 1; print(y)} else y = 2 ");
    foldSame("if(x) var y = 1; else {y = 2; print(y)}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldReturnResult
  public void testFoldReturnResult() {
    foldSame("function f(){return false;}");
    foldSame("function f(){return null;}");
    fold("function f(){return void 0;}",
         "function f(){return}");
    foldSame("function f(){return void foo();}");
    fold("function f(){return undefined;}",
         "function f(){return}");
    fold("function(){if(a()){return undefined;}}",
         "function(){if(a()){return}}");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testFoldStandardConstructors
  public void testFoldStandardConstructors() {
    foldSame("new Foo('a')");
    foldSame("var x = new goog.Foo(1)");
    foldSame("var x = new String(1)");
    foldSame("var x = new Number(1)");
    foldSame("var x = new Boolean(1)");

    enableNormalize();

    fold("var x = new Object('a')", "var x = Object('a')");
    fold("var x = new RegExp('')", "var x = RegExp('')");
    fold("var x = new Error('20')", "var x = Error(\"20\")");
    fold("var x = new Array(20)", "var x = Array(20)");
  }

// com.google.javascript.jscomp.PeepholeSubstituteAlternateSyntaxTest::testSubsituteReturn
  public void testSubsituteReturn() {

    fold("function f() { while(x) { return }}",
         "function f() { while(x) { break }}");

    foldSame("function f() { while(x) { return 5 } }");

    foldSame("function f() { a: { return 5 } }");

    fold("function f() { while(x) { return 5}  return 5}",
         "function f() { while(x) { break }    return 5}");

    fold("function f() { while(x) { return x}  return x}",
         "function f() { while(x) { break }    return x}");

    fold("function f() { while(x) { if (y) { return }}} ",
         "function f() { while(x) { if (y) { break  }}} ");

    fold("function f() { while(x) { if (y) { return }} return} ",
         "function f() { while(x) { if (y) { break  }} return} ");

    fold("function f() { while(x) { if (y) { return 5 }} return 5} ",
         "function f() { while(x) { if (y) { break    }} return 5} ");

    
    
    fold("function f() { while(x) { if (y) { return x } x = 1} return x} ",
         "function f() { while(x) { if (y) { break    } x = 1} return x} ");

    
    fold("function f() { while(x) { if (y) { return x } return x} return x}",
         "function f() { while(x) { if (y) { break    } break   } return x}");

    
    foldSame("function f() { while(x) { while (y) { return } } }");

    foldSame("function f() { while(1) { return 7}  return 5}");

    foldSame("function f() {" +
             "  try { while(x) {return f()}} catch (e) { } return f()}");

    foldSame("function f() {" +
             "  try { while(x) {return f()}} finally {alert(1)} return f()}");

    
    fold("function f() {" +
         "  try { while(x) { return f() } return f() } catch (e) { } }",
         "function f() {" +
         "  try { while(x) { break } return f() } catch (e) { } }");

    
    foldSame("function f() {" +
             "  try { while(x) { return foo() } } finally { alert(1) } "  +
             "  return foo()}");

    
    fold("function f() {" +
         "  try { while(x) { return 1 } } finally { alert(1) } return 1}",
         "function f() {" +
         "  try { while(x) { break    } } finally { alert(1) } return 1}"
         );

    foldSame("function f() { try{ return a } finally { a = 2 } return a; }");
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

// com.google.javascript.jscomp.PrepareAstTest::testJsDocNormalization
  public void testJsDocNormalization() throws Exception {
    Node root = parseExpectedJs(
        "var x = { a: function() {}," +
        "         c:  ('d')};");
    Node objlit = root.getFirstChild().getFirstChild().getFirstChild()
        .getFirstChild();
    assertEquals(Token.OBJECTLIT, objlit.getType());

    Node firstKey = objlit.getFirstChild();
    Node firstVal = firstKey.getFirstChild();

    Node secondKey = firstKey.getNext();
    Node secondVal = secondKey.getFirstChild();
    assertNotNull(firstKey.getJSDocInfo());
    assertNotNull(firstVal.getJSDocInfo());
    assertNull(secondKey.getJSDocInfo());
    assertNotNull(secondVal.getJSDocInfo());
  }

// com.google.javascript.jscomp.PrepareAstTest::testFreeCall1
  public void testFreeCall1() throws Exception {
    Node root = parseExpectedJs("foo();");
    Node script = root.getFirstChild();
    Preconditions.checkState(script.getType() == Token.SCRIPT);
    Node firstExpr = script.getFirstChild();
    Node call = firstExpr.getFirstChild();
    Preconditions.checkState(call.getType() == Token.CALL);

    assertTrue(call.getBooleanProp(Node.FREE_CALL));
  }

// com.google.javascript.jscomp.PrepareAstTest::testFreeCall2
  public void testFreeCall2() throws Exception {
    Node root = parseExpectedJs("x.foo();");
    Node script = root.getFirstChild();
    Preconditions.checkState(script.getType() == Token.SCRIPT);
    Node firstExpr = script.getFirstChild();
    Node call = firstExpr.getFirstChild();
    Preconditions.checkState(call.getType() == Token.CALL);

    assertFalse(call.getBooleanProp(Node.FREE_CALL));
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

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNoRemovalFunction2
  public void testNoRemovalFunction2() {
    test("goog.provide('foo'); function f(){var foo = 0}",
         "var foo = {}; function f(){var foo = 0}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignmentInIf1
  public void testRemovalMultipleAssignmentInIf1() {
    test("goog.provide('foo'); if (true) { var foo = 0 } else { foo = 1 }",
         "if (true) { var foo = 0 } else { foo = 1 }");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignmentInIf2
  public void testRemovalMultipleAssignmentInIf2() {
    test("goog.provide('foo'); if (true) { foo = 0 } else { var foo = 1 }",
         "if (true) { foo = 0 } else { var foo = 1 }");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignmentInIf3
  public void testRemovalMultipleAssignmentInIf3() {
    test("goog.provide('foo'); if (true) { foo = 0 } else { foo = 1 }",
         "if (true) { var foo = 0 } else { foo = 1 }");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalMultipleAssignmentInIf4
  public void testRemovalMultipleAssignmentInIf4() {
    test("goog.provide('foo.bar');" +
         "if (true) { foo.bar = 0 } else { foo.bar = 1 }",
         "var foo = {}; if (true) { foo.bar = 0 } else { foo.bar = 1 }");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMultipleDeclarationError1
  public void testMultipleDeclarationError1() {
    String rest = "if (true) { foo.bar = 0 } else { foo.bar = 1 }";
    test("goog.provide('foo.bar');" + "var foo = {};" + rest,
         "var foo = {};" + "var foo = {};" + rest);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMultipleDeclarationError2
  public void testMultipleDeclarationError2() {
    test("goog.provide('foo.bar');" +
         "if (true) { var foo = {}; foo.bar = 0 } else { foo.bar = 1 }",
         "var foo = {};" +
         "if (true) {" +
         "  var foo = {}; foo.bar = 0" +
         "} else {" +
         "  foo.bar = 1" +
         "}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMultipleDeclarationError3
  public void testMultipleDeclarationError3() {
    test("goog.provide('foo.bar');" +
         "if (true) { foo.bar = 0 } else { var foo = {}; foo.bar = 1 }",
         "var foo = {};" +
         "if (true) {" +
         "  foo.bar = 0" +
         "} else {" +
         "  var foo = {}; foo.bar = 1" +
         "}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideAfterDeclarationError
  public void testProvideAfterDeclarationError() {
    test("var x = 42; goog.provide('x');",
         "var x = 42; var x = {}");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideErrorCases
  public void testProvideErrorCases() {
    test("goog.provide();", "", NULL_ARGUMENT_ERROR);
    test("goog.provide(5);", "", INVALID_ARGUMENT_ERROR);
    test("goog.provide([]);", "", INVALID_ARGUMENT_ERROR);
    test("goog.provide({});", "", INVALID_ARGUMENT_ERROR);
    test("goog.provide('foo', 'bar');", "", TOO_MANY_ARGUMENTS_ERROR);
    test("goog.provide('foo'); goog.provide('foo');", "",
        DUPLICATE_NAMESPACE_ERROR);
    test("goog.provide('foo.bar'); goog.provide('foo'); goog.provide('foo');",
        "", DUPLICATE_NAMESPACE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRemovalOfRequires
  public void testRemovalOfRequires() {
    test("goog.provide('foo'); goog.require('foo');",
         "var foo={};");
    test("goog.provide('foo.bar'); goog.require('foo.bar');",
         "var foo={}; foo.bar={};");
    test("goog.provide('foo.bar.baz'); goog.require('foo.bar.baz');",
         "var foo={}; foo.bar={}; foo.bar.baz={};");
    test("goog.provide('foo'); var x = 3; goog.require('foo'); something();",
         "var foo={}; var x = 3; something();");
    testSame("foo.require('foo.bar');");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRequireErrorCases
  public void testRequireErrorCases() {
    test("goog.require();", "", NULL_ARGUMENT_ERROR);
    test("goog.require(5);", "", INVALID_ARGUMENT_ERROR);
    test("goog.require([]);", "", INVALID_ARGUMENT_ERROR);
    test("goog.require({});", "", INVALID_ARGUMENT_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testLateProvides
  public void testLateProvides() {
    test("goog.require('foo'); goog.provide('foo');",
         "var foo={};", LATE_PROVIDE_ERROR);
    test("goog.require('foo.bar'); goog.provide('foo.bar');",
         "var foo={}; foo.bar={};", LATE_PROVIDE_ERROR);
    test("goog.provide('foo.bar'); goog.require('foo'); goog.provide('foo');",
         "var foo={}; foo.bar={};", LATE_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMissingProvides
  public void testMissingProvides() {
    test("goog.require('foo');",
         "", MISSING_PROVIDE_ERROR);
    test("goog.provide('foo'); goog.require('Foo');",
         "var foo={};", MISSING_PROVIDE_ERROR);
    test("goog.provide('foo'); goog.require('foo.bar');",
         "var foo={};", MISSING_PROVIDE_ERROR);
    test("goog.provide('foo'); var EXPERIMENT_FOO = true; " +
             "if (EXPERIMENT_FOO) {goog.require('foo.bar');}",
         "var foo={}; var EXPERIMENT_FOO = true; if (EXPERIMENT_FOO) {}",
         MISSING_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testNewDateGoogNowSimplification
  public void testNewDateGoogNowSimplification() {
    test("var x = new Date(goog.now());", "var x = new Date();");
    testSame("var x = new Date(goog.now() + 1);");
    testSame("var x = new Date(goog.now(1));");
    testSame("var x = new Date(1, goog.now());");
    testSame("var x = new Date(1);");
    testSame("var x = new Date();");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testAddDependency
  public void testAddDependency() {
    test("goog.addDependency('x.js', ['A', 'B'], []);", "0");

    Compiler compiler = getLastCompiler();
    assertTrue(compiler.getTypeRegistry().isForwardDeclaredType("A"));
    assertTrue(compiler.getTypeRegistry().isForwardDeclaredType("B"));
    assertFalse(compiler.getTypeRegistry().isForwardDeclaredType("C"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidSetCssNameMapping
  public void testValidSetCssNameMapping() {
    test("goog.setCssNameMapping({foo:'bar',\"biz\":'baz'});", "");
    CssRenamingMap map = getLastCompiler().getCssRenamingMap();
    assertNotNull(map);
    assertEquals("bar", map.get("foo"));
    assertEquals("baz", map.get("biz"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSetCssNameMappingNonStringValueReturnsError
  public void testSetCssNameMappingNonStringValueReturnsError() {
    
    test("var BAR = {foo:'bar'}; goog.setCssNameMapping(BAR);", "",
        INVALID_ARGUMENT_ERROR);
    test("goog.setCssNameMapping([]);", "",
        INVALID_ARGUMENT_ERROR);
    test("goog.setCssNameMapping(false);", "",
        INVALID_ARGUMENT_ERROR);
    test("goog.setCssNameMapping(null);", "",
        INVALID_ARGUMENT_ERROR);
    test("goog.setCssNameMapping(undefined);", "",
        INVALID_ARGUMENT_ERROR);

    
    test("var BAR = 'bar'; goog.setCssNameMapping({foo:BAR});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
    test("goog.setCssNameMapping({foo:6});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
    test("goog.setCssNameMapping({foo:false});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
    test("goog.setCssNameMapping({foo:null});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
    test("goog.setCssNameMapping({foo:undefined});", "",
        NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testBadCrossModuleRequire
  public void testBadCrossModuleRequire() {
    test(
        createModuleStar(
            "",
            "goog.provide('goog.ui');",
            "goog.require('goog.ui');"),
        new String[] {
          "",
          "goog.ui = {};",
          ""
        },
        null,
        XMODULE_REQUIRE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testGoodCrossModuleRequire1
  public void testGoodCrossModuleRequire1() {
    test(
        createModuleStar(
            "goog.provide('goog.ui');",
            "",
            "goog.require('goog.ui');"),
        new String[] {
            "goog.ui = {};",
            "",
            "",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testGoodCrossModuleRequire2
  public void testGoodCrossModuleRequire2() {
    test(
        createModuleStar(
            "",
            "",
            "goog.provide('goog.ui'); goog.require('goog.ui');"),
        new String[] {
            "",
            "",
            "goog.ui = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSimpleAdditionalProvide
  public void testSimpleAdditionalProvide() {
    additionalCode = "goog.provide('b.B'); b.B = {};";
    test("goog.provide('a.A'); a.A = {};",
         "var b={};b.B={};var a={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSimpleAdditionalProvideAtEnd
  public void testSimpleAdditionalProvideAtEnd() {
    additionalEndCode = "goog.provide('b.B'); b.B = {};";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};var b={};b.B={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testSimpleDottedAdditionalProvide
  public void testSimpleDottedAdditionalProvide() {
    additionalCode = "goog.provide('a.b.B'); a.b.B = {};";
    test("goog.provide('c.d.D'); c.d.D = {};",
         "var a={};a.b={};a.b.B={};var c={};c.d={};c.d.D={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testOverlappingAdditionalProvide
  public void testOverlappingAdditionalProvide() {
    additionalCode = "goog.provide('a.B'); a.B = {};";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.B={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testOverlappingAdditionalProvideAtEnd
  public void testOverlappingAdditionalProvideAtEnd() {
    additionalEndCode = "goog.provide('a.B'); a.B = {};";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};a.B={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testOverlappingDottedAdditionalProvide
  public void testOverlappingDottedAdditionalProvide() {
    additionalCode = "goog.provide('a.b.B'); a.b.B = {};";
    test("goog.provide('a.b.C'); a.b.C = {};",
         "var a={};a.b={};a.b.B={};a.b.C={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRequireOfAdditionalProvide
  public void testRequireOfAdditionalProvide() {
    additionalCode = "goog.provide('b.B'); b.B = {};";
    test("goog.require('b.B'); goog.provide('a.A'); a.A = {};",
         "var b={};b.B={};var a={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testMissingRequireWithAdditionalProvide
  public void testMissingRequireWithAdditionalProvide() {
    additionalCode = "goog.provide('b.B'); b.B = {};";
    test("goog.require('b.C'); goog.provide('a.A'); a.A = {};",
         "var b={};b.B={};var a={};a.A={};",
         MISSING_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testLateRequire
  public void testLateRequire() {
    additionalEndCode = "goog.require('a.A');";
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testReorderedProvides
  public void testReorderedProvides() {
    additionalCode = "a.B = {};";  
    addAdditionalNamespace = true;
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.B={};a.A={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testReorderedProvides2
  public void testReorderedProvides2() {
    additionalEndCode = "a.B = {};";
    addAdditionalNamespace = true;
    test("goog.provide('a.A'); a.A = {};",
         "var a={};a.A={};a.B={};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder1
  public void testProvideOrder1() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    
    
    test("goog.provide('a.b');" +
         "goog.provide('a.b.c');" +
         "a.b.c;" +
         "a.b = function(x,y) {};",
         "var a = {};" +
         "a.b = {};" +
         "a.b.c = {};" +
         "a.b.c;" +
         "a.b = function(x,y) {};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder2
  public void testProvideOrder2() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    
    
    test("goog.provide('a.b');" +
         "goog.provide('a.b.c');" +
         "a.b = function(x,y) {};" +
         "a.b.c;",
         "var a = {};" +
         "a.b = {};" +
         "a.b.c = {};" +
         "a.b = function(x,y) {};" +
         "a.b.c;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder3a
  public void testProvideOrder3a() {
    test("goog.provide('a.b');" +
         "a.b = function(x,y) {};" +
         "goog.provide('a.b.c');" +
         "a.b.c;",
         "var a = {};" +
         "a.b = function(x,y) {};" +
         "a.b.c = {};" +
         "a.b.c;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder3b
  public void testProvideOrder3b() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    test("goog.provide('a.b');" +
         "a.b = function(x,y) {};" +
         "goog.provide('a.b.c');" +
         "a.b.c;",
         "var a = {};" +
         "a.b = function(x,y) {};" +
         "a.b.c = {};" +
         "a.b.c;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder4a
  public void testProvideOrder4a() {
    test("goog.provide('goog.a');" +
         "goog.provide('goog.a.b');" +
         "if (x) {" +
         "  goog.a.b = 1;" +
         "} else {" +
         "  goog.a.b = 2;" +
         "}",

         "goog.a={};" +
         "if(x)" +
         "  goog.a.b=1;" +
         "else" +
         "  goog.a.b=2;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideOrder4b
  public void testProvideOrder4b() {
    additionalEndCode = "";
    addAdditionalNamespace = false;
    
    test("goog.provide('goog.a');" +
         "goog.provide('goog.a.b');" +
         "if (x) {" +
         "  goog.a.b = 1;" +
         "} else {" +
         "  goog.a.b = 2;" +
         "}",

         "goog.a={};" +
         "if(x)" +
         "  goog.a.b=1;" +
         "else" +
         "  goog.a.b=2;");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidProvide
  public void testInvalidProvide() {
    test("goog.provide('a.class');", null, INVALID_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase1
  public void testInvalidBase1() {
    test("goog.base(this, 'method');", null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase2
  public void testInvalidBase2() {
    test("function Foo() {}" +
         "Foo.method = function() {" +
         "  goog.base(this, 'method');" +
         "};", null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase3
  public void testInvalidBase3() {
    test(String.format(METHOD_FORMAT, "goog.base();"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase4
  public void testInvalidBase4() {
    test(String.format(METHOD_FORMAT, "goog.base(this, 'bar');"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase5
  public void testInvalidBase5() {
    test(String.format(METHOD_FORMAT, "goog.base('foo', 'method');"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase6
  public void testInvalidBase6() {
    test(String.format(METHOD_FORMAT, "goog.base.call(null, this, 'method');"),
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase7
  public void testInvalidBase7() {
    test("function Foo() { goog.base(this); }",
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase8
  public void testInvalidBase8() {
    test("var Foo = function() { goog.base(this); }",
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testInvalidBase9
  public void testInvalidBase9() {
    test("var goog = {}; goog.Foo = function() { goog.base(this); }",
         null, BASE_CLASS_ERROR);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase1
  public void testValidBase1() {
    test(String.format(METHOD_FORMAT, "goog.base(this, 'method');"),
         String.format(METHOD_FORMAT, "Foo.superClass_.method.call(this)"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase2
  public void testValidBase2() {
    test(String.format(METHOD_FORMAT, "goog.base(this, 'method', 1, 2);"),
         String.format(METHOD_FORMAT,
             "Foo.superClass_.method.call(this, 1, 2)"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase3
  public void testValidBase3() {
    test(String.format(METHOD_FORMAT, "return goog.base(this, 'method');"),
         String.format(METHOD_FORMAT,
             "return Foo.superClass_.method.call(this)"));
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase4
  public void testValidBase4() {
    test("function Foo() { goog.base(this, 1, 2); }" + FOO_INHERITS,
         "function Foo() { BaseFoo.call(this, 1, 2); } " + FOO_INHERITS);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase5
  public void testValidBase5() {
    test("var Foo = function() { goog.base(this, 1); };" + FOO_INHERITS,
         "var Foo = function() { BaseFoo.call(this, 1); }; " + FOO_INHERITS);
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testValidBase6
  public void testValidBase6() {
    test("var goog = {}; goog.Foo = function() { goog.base(this); }; " +
         "goog.inherits(goog.Foo, goog.BaseFoo);",
         "var goog = {}; goog.Foo = function() { goog.BaseFoo.call(this); }; " +
         "goog.inherits(goog.Foo, goog.BaseFoo);");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitAndExplicitProvide
  public void testImplicitAndExplicitProvide() {
    test("var goog = {}; " +
         "goog.provide('goog.foo.bar'); goog.provide('goog.foo');",
         "var goog = {}; goog.foo = {}; goog.foo.bar = {};");
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitProvideInIndependentModules
  public void testImplicitProvideInIndependentModules() {
    test(
        createModuleStar(
            "",
            "goog.provide('apps.A');",
            "goog.provide('apps.B');"),
        new String[] {
            "var apps = {};",
            "apps.A = {};",
            "apps.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitProvideInIndependentModules2
  public void testImplicitProvideInIndependentModules2() {
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.A');",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {}; apps.foo = {};",
            "apps.foo.A = {};",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testImplicitProvideInIndependentModules3
  public void testImplicitProvideInIndependentModules3() {
    test(
        createModuleStar(
            "var goog = {};",
            "goog.provide('goog.foo.A');",
            "goog.provide('goog.foo.B');"),
        new String[] {
            "var goog = {}; goog.foo = {};",
            "goog.foo.A = {};",
            "goog.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules1
  public void testProvideInIndependentModules1() {
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo');",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {}; apps.foo = {};",
            "",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules2
  public void testProvideInIndependentModules2() {
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo'); apps.foo = {};",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {};",
            "apps.foo = {};",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules2b
  public void testProvideInIndependentModules2b() {
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo'); apps.foo = function() {};",
            "goog.provide('apps.foo.B');"),
        new String[] {
            "var apps = {};",
            "apps.foo = function() {};",
            "apps.foo.B = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules3
  public void testProvideInIndependentModules3() {
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.B');",
            "goog.provide('apps.foo'); goog.require('apps.foo');"),
        new String[] {
            "var apps = {}; apps.foo = {};",
            "apps.foo.B = {};",
            "",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules3b
  public void testProvideInIndependentModules3b() {
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.B');",
            "goog.provide('apps.foo'); apps.foo = function() {}; " +
            "goog.require('apps.foo');"),
        new String[] {
            "var apps = {};",
            "apps.foo.B = {};",
            "apps.foo = function() {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testProvideInIndependentModules4
  public void testProvideInIndependentModules4() {
    
    
    test(
        createModuleStar(
            "goog.provide('apps');",
            "goog.provide('apps.foo.bar.B');",
            "goog.provide('apps.foo.bar.C');"),
        new String[] {
            "var apps = {};apps.foo = {};apps.foo.bar = {}",
            "apps.foo.bar.B = {};",
            "apps.foo.bar.C = {};",
        });
  }

// com.google.javascript.jscomp.ProcessClosurePrimitivesTest::testRequireOfBaseGoog
  public void testRequireOfBaseGoog() {
    test("goog.require('goog');",
         "", MISSING_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine1
  public void testBasicDefine1() {
    test(" var DEF = true", "var DEF=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine2
  public void testBasicDefine2() {
    test(" var DEF = 'a'", "var DEF=\"a\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testBasicDefine3
  public void testBasicDefine3() {
    test(" var DEF = 0", "var DEF=0");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineBadType
  public void testDefineBadType() {
    test(" var DEF = {}",
        null, ProcessDefines.INVALID_DEFINE_TYPE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithBadValue1
  public void testDefineWithBadValue1() {
    test(" var DEF = new Boolean(true);", null,
        ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithBadValue2
  public void testDefineWithBadValue2() {
    test(" var DEF = 'x' + y;", null,
        ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithDependentValue
  public void testDefineWithDependentValue() {
    test(" var BASE = false;\n" +
         " var DEF = !BASE;",
         "var BASE=false;var DEF=!BASE");
    test("var a = {};\n" +
         " a.BASE = false;\n" +
         " a.DEF = !a.BASE;",
         "var a={};a.BASE=false;a.DEF=!a.BASE");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineWithInvalidDependentValue
  public void testDefineWithInvalidDependentValue() {
    test("var BASE = false;\n" +
         " var DEF = !BASE;",
         null,
          ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverriding1
  public void testOverriding1() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    overrides.put("DEF_OVERRIDE_TO_FALSE", new Node(Token.FALSE));
    test(
        " var DEF_OVERRIDE_TO_TRUE = false;" +
        " var DEF_OVERRIDE_TO_FALSE = true",
        "var DEF_OVERRIDE_TO_TRUE=true;var DEF_OVERRIDE_TO_FALSE=false");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverriding2
  public void testOverriding2() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    String normalConst = "var DEF_OVERRIDE_TO_FALSE=true;";
    testWithPrefix(
        normalConst,
        " var DEF_OVERRIDE_TO_TRUE = false",
        "var DEF_OVERRIDE_TO_TRUE=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverriding3
  public void testOverriding3() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    test(
        " var DEF_OVERRIDE_TO_TRUE = true;",
        "var DEF_OVERRIDE_TO_TRUE=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverridingString0
  public void testOverridingString0() {
    test(
        " var DEF_OVERRIDE_STRING = 'x';",
        "var DEF_OVERRIDE_STRING=\"x\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverridingString1
  public void testOverridingString1() {
    test(
        " var DEF_OVERRIDE_STRING = 'x' + 'y';",
        "var DEF_OVERRIDE_STRING=\"x\" + \"y\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverridingString2
  public void testOverridingString2() {
    overrides.put("DEF_OVERRIDE_STRING", Node.newString("foo"));
    test(
        " var DEF_OVERRIDE_STRING = 'x';",
        "var DEF_OVERRIDE_STRING=\"foo\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverridingString3
  public void testOverridingString3() {
    overrides.put("DEF_OVERRIDE_STRING", Node.newString("foo"));
    test(
        " var DEF_OVERRIDE_STRING = 'x' + 'y';",
        "var DEF_OVERRIDE_STRING=\"foo\"");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testMisspelledOverride
  public void testMisspelledOverride() {
    overrides.put("DEF_BAD_OVERIDE", new Node(Token.TRUE));
    test(" var DEF_BAD_OVERRIDE = true",
        "var DEF_BAD_OVERRIDE=true", null,
        ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testCompiledIsKnownDefine
  public void testCompiledIsKnownDefine() {
    overrides.put("COMPILED", new Node(Token.TRUE));
    testSame("");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testSimpleReassign1
  public void testSimpleReassign1() {
    test(" var DEF = false; DEF = true;",
        "var DEF=true;true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testSimpleReassign2
  public void testSimpleReassign2() {
    test(" var DEF=false;DEF=true;DEF=3",
        "var DEF=3;true;3");

    Name def = namespace.getNameIndex().get("DEF");
    assertEquals(0, def.refs.size());
    assertEquals(1, def.globalSets);
    assertNotNull(def.declaration);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testSimpleReassign3
  public void testSimpleReassign3() {
    test(" var DEF = false;var x;x = DEF = true;",
        "var DEF=true;var x;x=true");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDuplicateVar
  public void testDuplicateVar() {
    test(" var DEF = false; var DEF = true;",
         null, VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testAssignBeforeDeclaration1
  public void testAssignBeforeDeclaration1() {
    test("DEF=false;var b=false,DEF=true,c=false",
         null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testAssignBeforeDeclaration2
  public void testAssignBeforeDeclaration2() {
    overrides.put("DEF_OVERRIDE_TO_TRUE", new Node(Token.TRUE));
    test(
        "DEF_OVERRIDE_TO_TRUE = 3;" +
        " var DEF_OVERRIDE_TO_TRUE = false;",
        null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testEmptyDeclaration
  public void testEmptyDeclaration() {
    test(" var DEF;",
         null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterCall
  public void testReassignAfterCall() {
    test("var DEF=true;externMethod();DEF=false",
        null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterRef
  public void testReassignAfterRef() {
    test("var DEF=true;var x = DEF;DEF=false",
        null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignWithExpr
  public void testReassignWithExpr() {
    test("var DEF=true;var x;DEF=x=false",
        null, ProcessDefines.INVALID_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterNonGlobalRef
  public void testReassignAfterNonGlobalRef() {
    test(
        "var DEF=true;" +
        "var x=function(){var y=DEF}; DEF=false",
        "var DEF=false;var x=function(){var y=DEF};false");

    Name def = namespace.getNameIndex().get("DEF");
    assertEquals(1, def.refs.size());
    assertEquals(1, def.globalSets);
    assertNotNull(def.declaration);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testReassignAfterRefInConditional
  public void testReassignAfterRefInConditional() {
    test(
        "var DEF=true;" +
        "if (false) {var x=DEF} DEF=false;",
        null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testAssignInNonGlobalScope
  public void testAssignInNonGlobalScope() {
    test("var DEF=true;function foo() {DEF=false};",
        null, ProcessDefines.NON_GLOBAL_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDeclareInNonGlobalScope
  public void testDeclareInNonGlobalScope() {
    test("function foo() {var DEF=true;};",
        null, ProcessDefines.NON_GLOBAL_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testDefineAssignmentInLoop
  public void testDefineAssignmentInLoop() {
    test("var DEF=true;var x=0;while (x) {DEF=false;}",
        null, ProcessDefines.NON_GLOBAL_DEFINE_INIT_ERROR);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testWithNoDefines
  public void testWithNoDefines() {
    testSame("var DEF=true;var x={};x.foo={}");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine1
  public void testNamespacedDefine1() {
    test("var a = {};  a.B = false; a.B = true;",
         "var a = {}; a.B = true; true;");

    Name aDotB = namespace.getNameIndex().get("a.B");
    assertEquals(0, aDotB.refs.size());
    assertEquals(1, aDotB.globalSets);
    assertNotNull(aDotB.declaration);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine2
  public void testNamespacedDefine2() {
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {};  a.B = false;",
         "var a = {}; a.B = true;");
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testNamespacedDefine3
  public void testNamespacedDefine3() {
    overrides.put("a.B", new Node(Token.TRUE));
    test("var a = {};", "var a = {};", null,
         ProcessDefines.UNKNOWN_DEFINE_WARNING);
  }

// com.google.javascript.jscomp.ProcessDefinesTest::testOverrideAfterAlias
  public void testOverrideAfterAlias() {
    test("var x; var DEF=true; x=DEF; DEF=false;",
         null, ProcessDefines.DEFINE_NOT_ASSIGNABLE_ERROR);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new1
  public void testAnnotationInExterns_new1() throws Exception {
    checkMarkedCalls("externSENone()",
        ImmutableList.<String>of("externSENone"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new2
  public void testAnnotationInExterns_new2() throws Exception {
    checkMarkedCalls("externSEThis()",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new3
  public void testAnnotationInExterns_new3() throws Exception {
    checkMarkedCalls("new externObjSEThis()",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new4
  public void testAnnotationInExterns_new4() throws Exception {
    
    
    checkMarkedCalls("new externObjSEThis().externObjSEThisMethod('')",
        ImmutableList.<String>of(
            "externObjSEThis", "NEW STRING externObjSEThisMethod"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new5
  public void testAnnotationInExterns_new5() throws Exception {
    checkMarkedCalls(
        "function f() { new externObjSEThis() };" +
        "f();",
        ImmutableList.<String>of("externObjSEThis", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new6
  public void testAnnotationInExterns_new6() throws Exception {
    
    
    
    checkMarkedCalls(
        "function f() {" +
        "  new externObjSEThis().externObjSEThisMethod('') " +
        "};" +
        "f();",
        ImmutableList.<String>of(
            "externObjSEThis", "NEW STRING externObjSEThisMethod", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new7
  public void testAnnotationInExterns_new7() throws Exception {
    
    
    
    checkMarkedCalls(
        "function f() {" +
        "  var x = new externObjSEThis(); " +
        "  x.externObjSEThisMethod('') " +
        "};" +
        "f();",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new8
  public void testAnnotationInExterns_new8() throws Exception {
    
    
    
    checkMarkedCalls(
        "function f(x) {" +
        "  x.externObjSEThisMethod('') " +
        "};" +
        "f(new externObjSEThis());",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new9
  public void testAnnotationInExterns_new9() throws Exception {
    
    
    
    
    checkMarkedCalls(
        "function f(x) {" +
        "  x = new externObjSEThis(); " +
        "  x.externObjSEThisMethod('') " +
        "};" +
        "f(g);",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns_new10
  public void testAnnotationInExterns_new10() throws Exception {
    
    
    
    
    checkMarkedCalls(
        "function f() {" +
        "  new externObjSEThis().externObjSEThisMethod2('') " +
        "};" +
        "f();",
        ImmutableList.<String>of("externObjSEThis"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns1
  public void testAnnotationInExterns1() throws Exception {
    checkMarkedCalls("externSef1()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns2
  public void testAnnotationInExterns2() throws Exception {
    checkMarkedCalls("externSef2()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns3
  public void testAnnotationInExterns3() throws Exception {
    checkMarkedCalls("externNsef1()", ImmutableList.of("externNsef1"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns4
  public void testAnnotationInExterns4() throws Exception {
    checkMarkedCalls("externNsef2()", ImmutableList.of("externNsef2"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns5
  public void testAnnotationInExterns5() throws Exception {
    checkMarkedCalls("externNsef3()", ImmutableList.of("externNsef3"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns1
  public void testNamespaceAnnotationInExterns1() throws Exception {
    checkMarkedCalls("externObj.sef1()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns2
  public void testNamespaceAnnotationInExterns2() throws Exception {
    checkMarkedCalls("externObj.nsef1()", ImmutableList.of("externObj.nsef1"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns3
  public void testNamespaceAnnotationInExterns3() throws Exception {
    checkMarkedCalls("externObj.nsef2()", ImmutableList.of("externObj.nsef2"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns4
  public void testNamespaceAnnotationInExterns4() throws Exception {
    checkMarkedCalls("externObj.partialFn()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns5
  public void testNamespaceAnnotationInExterns5() throws Exception {
    
    
    
    String templateSrc = "var o = {}; o.<fnName> = function(){}; o.<fnName>()";

    
    checkMarkedCalls(templateSrc.replaceAll("<fnName>", "notPartialFn"),
                     ImmutableList.of("o.notPartialFn"));

    checkMarkedCalls(templateSrc.replaceAll("<fnName>", "partialFn"),
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns6
  public void testNamespaceAnnotationInExterns6() throws Exception {
    checkMarkedCalls("externObj.partialSharedFn()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns1
  public void testConstructorAnnotationInExterns1() throws Exception {
    checkMarkedCalls("new externSefConstructor()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns2
  public void testConstructorAnnotationInExterns2() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.sefFnOfSefObj()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns3
  public void testConstructorAnnotationInExterns3() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.nsefFnOfSefObj()",
                     ImmutableList.of("a.nsefFnOfSefObj"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns4
  public void testConstructorAnnotationInExterns4() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.externShared()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns5
  public void testConstructorAnnotationInExterns5() throws Exception {
    checkMarkedCalls("new externNsefConstructor()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns6
  public void testConstructorAnnotationInExterns6() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.sefFnOfNsefObj()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns7
  public void testConstructorAnnotationInExterns7() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.nsefFnOfNsefObj()",
                     ImmutableList.of("externNsefConstructor",
                                      "a.nsefFnOfNsefObj"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns8
  public void testConstructorAnnotationInExterns8() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.externShared()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testSharedFunctionName1
  public void testSharedFunctionName1() throws Exception {
    checkMarkedCalls("var a; " +
                     "if (true) {" +
                     "  a = new externNsefConstructor()" +
                     "} else {" +
                     "  a = new externSefConstructor()" +
                     "}" +
                     "a.externShared()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testSharedFunctionName2
  public void testSharedFunctionName2() throws Exception {
    
    
    boolean broken = true;
    if (broken) {
      checkMarkedCalls("var a; " +
                       "if (true) {" +
                       "  a = new externNsefConstructor()" +
                       "} else {" +
                       "  a = new externNsefConstructor2()" +
                       "}" +
                       "a.externShared()",
                       ImmutableList.of("externNsefConstructor",
                                        "externNsefConstructor2"));
    } else {
      checkMarkedCalls("var a; " +
                       "if (true) {" +
                       "  a = new externNsefConstructor()" +
                       "} else {" +
                       "  a = new externNsefConstructor2()" +
                       "}" +
                       "a.externShared()",
                       ImmutableList.of("externNsefConstructor",
                                        "externNsefConstructor2",
                                        "a.externShared"));
    }
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs1
  public void testAnnotationInExternStubs1() throws Exception {
    checkMarkedCalls("o.propWithStubBefore('a');",
        ImmutableList.<String>of("o.propWithStubBefore"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs1b
  public void testAnnotationInExternStubs1b() throws Exception {
    checkMarkedCalls("o.propWithStubBeforeWithJSDoc('a');",
        ImmutableList.<String>of("o.propWithStubBeforeWithJSDoc"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs2
  public void testAnnotationInExternStubs2() throws Exception {
    checkMarkedCalls("o.propWithStubAfter('a');",
        ImmutableList.<String>of("o.propWithStubAfter"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs2b
  public void testAnnotationInExternStubs2b() throws Exception {
    checkMarkedCalls("o.propWithStubAfter('a');",
        ImmutableList.<String>of("o.propWithStubAfter"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs3
  public void testAnnotationInExternStubs3() throws Exception {
    checkMarkedCalls("propWithAnnotatedStubAfter('a');",
        ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs4
  public void testAnnotationInExternStubs4() throws Exception {
    
    
    String externs =
      "function externObj5(){}\n" +

      "externObj5.prototype.propWithAnnotatedStubAfter = function(s) {};\n" +

      "\n" +
      "externObj5.prototype.propWithAnnotatedStubAfter;\n";

    List<String> expected = ImmutableList.<String>of();
    testSame(externs,
        "o.prototype.propWithAnnotatedStubAfter",
        TypeValidator.DUP_VAR_DECLARATION, false);
    assertEquals(expected, noSideEffectCalls);
    noSideEffectCalls.clear();
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExternStubs5
  public void testAnnotationInExternStubs5() throws Exception {
    
    
    String externs =
      "function externObj5(){}\n" +

      "\n" +
      "externObj5.prototype.propWithAnnotatedStubAfter = function(s) {};\n" +

      "\n" +
      "externObj5.prototype.propWithAnnotatedStubAfter;\n";

    List<String> expected = ImmutableList.<String>of();
    testSame(externs,
        "o.prototype.propWithAnnotatedStubAfter",
        TypeValidator.DUP_VAR_DECLARATION, false);
    assertEquals(expected, noSideEffectCalls);
    noSideEffectCalls.clear();
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNoSideEffectsSimple
  public void testNoSideEffectsSimple() throws Exception {
    String prefix = "function f(){";
    String suffix = "} f()";
    List<String> expected = ImmutableList.of("f");

    checkMarkedCalls(
        prefix + "" + suffix, expected);
    checkMarkedCalls(
        prefix + "return 1" + suffix, expected);
    checkMarkedCalls(
        prefix + "return 1 + 2" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = 1; return a" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = 1; a = 2; return a" + suffix, expected);
    checkMarkedCalls(
        prefix + "var a = 1; a = 2; return a + 1" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo" + suffix, expected);
    checkMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo + 1" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "return externObj" + suffix, expected);
    checkMarkedCalls(
        "function g(x) { x.foo = 3; }"  +
        prefix + "return externObj.foo" + suffix, expected);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testResultLocalitySimple
  public void testResultLocalitySimple() throws Exception {
    String prefix = "var g; function f(){";
    String suffix = "} f()";
    List<String> expected = ImmutableList.of("f");
    List<String> notExpected = ImmutableList.of();

    
    checkLocalityOfMarkedCalls(
        prefix + "" + suffix, expected);
    
    checkLocalityOfMarkedCalls(
        prefix + "return 1" + suffix, expected);
    checkLocalityOfMarkedCalls(
        prefix + "return 1 + 2" + suffix, expected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return g" + suffix, notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return 1; return 2" + suffix, expected);
    checkLocalityOfMarkedCalls(
        prefix + "return 1; return g" + suffix, notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "var a = 1; return a" + suffix, notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "var a = 1; a = 2; return a" + suffix, notExpected);
    checkLocalityOfMarkedCalls(
        prefix + "var a = 1; a = 2; return a + 1" + suffix, expected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return {foo : 1}.foo" + suffix,
        notExpected);
    checkLocalityOfMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo" + suffix,
        notExpected);

    
    checkLocalityOfMarkedCalls(
        prefix + "return externObj" + suffix, notExpected);
    checkLocalityOfMarkedCalls(
        "function inner(x) { x.foo = 3; }"  +
        prefix + "return externObj.foo" + suffix, notExpected);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testExternCalls
  public void testExternCalls() throws Exception {
    String prefix = "function f(){";
    String suffix = "} f()";

    checkMarkedCalls(prefix + "externNsef1()" + suffix,
                     ImmutableList.of("externNsef1", "f"));
    checkMarkedCalls(prefix + "externObj.nsef1()" + suffix,
                     ImmutableList.of("externObj.nsef1", "f"));

    checkMarkedCalls(prefix + "externSef1()" + suffix,
                     ImmutableList.<String>of());
    checkMarkedCalls(prefix + "externObj.sef1()" + suffix,
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testApply
  public void testApply() throws Exception {
    checkMarkedCalls("function f() {return 42}" +
                     "f.apply()",
                     ImmutableList.of("f.apply"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCall
  public void testCall() throws Exception {
    checkMarkedCalls("function f() {return 42}" +
                     "f.call()",
                     ImmutableList.<String>of("f.call"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference1
  public void testInference1() throws Exception {
    checkMarkedCalls("function f() {return g()}" +
                     "function g() {return 42}" +
                     "f()",
                     ImmutableList.of("g", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference2
  public void testInference2() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "function f() {g()}" +
                     "function g() {a=2}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference3
  public void testInference3() throws Exception {
    checkMarkedCalls("var f = function() {return g()};" +
                     "var g = function() {return 42};" +
                     "f()",
                     ImmutableList.of("g", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference4
  public void testInference4() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "var f = function() {g()};" +
                     "var g = function() {a=2};" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference5
  public void testInference5() throws Exception {
    checkMarkedCalls("var goog = {};" +
                     "goog.f = function() {return goog.g()};" +
                     "goog.g = function() {return 42};" +
                     "goog.f()",
                     ImmutableList.of("goog.g", "goog.f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference6
  public void testInference6() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "var goog = {};" +
                     "goog.f = function() {goog.g()};" +
                     "goog.g = function() {a=2};" +
                     "goog.f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects1
  public void testLocalizedSideEffects1() throws Exception {
    
    
    checkMarkedCalls("function f() {" +
                     "  var x = {foo : 0}; return function() {x.foo++};" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects2
  public void testLocalizedSideEffects2() throws Exception {
    
    
    checkMarkedCalls("function f() {" +
                     "  var x = {foo : 0}; (function() {x.foo++})();" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects3
  public void testLocalizedSideEffects3() throws Exception {
    
    
    checkMarkedCalls("var g = {foo:1}; function f() {var x = g; x.foo++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects4
  public void testLocalizedSideEffects4() throws Exception {
    
    
    checkMarkedCalls("function f() {var x = []; x[0] = 1;}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects5
  public void testLocalizedSideEffects5() throws Exception {
    
    
    checkMarkedCalls("var g = [];function f() {var x = g; x[0] = 1;}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects6
  public void testLocalizedSideEffects6() throws Exception {
    
    
    checkMarkedCalls("function f() {" +
                     "  var x = {}; x.foo = 1; return x;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects7
  public void testLocalizedSideEffects7() throws Exception {
    
    
    checkMarkedCalls(" function A() {};" +
                     "function f() {" +
                     "  var a = []; a[1] = 1; return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects8
  public void testLocalizedSideEffects8() throws Exception {
    
    
    checkMarkedCalls(" function A() {};" +
                     "function f() {" +
                     "  var a = new A; a.foo = 1; return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects9
  public void testLocalizedSideEffects9() throws Exception {
    
    
    checkMarkedCalls(" function A() {this.x = 1};" +
                     "function f() {" +
                     "  var a = new A; a.foo = 1; return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects10
  public void testLocalizedSideEffects10() throws Exception {
    
    
    checkMarkedCalls(" function A() {};" +
                     "A.prototype.g = function() {this.x = 1};" +
                     "function f() {" +
                     "  var a = new A; a.g(); return a;" +
                     "}" +
                     "f()",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testLocalizedSideEffects11
  public void testLocalizedSideEffects11() throws Exception {
    
    checkMarkedCalls(
        " function A() {}" +
        "A.prototype.update = function() { this.x = 1; };" +
        " function B() { " +
        "  this.a_ = new A();" +
        "}" +
        "B.prototype.updateA = function() {" +
        "  var b = this.a_;" +
        "  b.update();" +
        "};" +
        "var x = new B();" +
        "x.updateA();",
        ImmutableList.of("A", "B"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators1
  public void testUnaryOperators1() throws Exception {
    checkMarkedCalls("function f() {var x = 1; x++}" +
                     "f()",
                     ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators2
  public void testUnaryOperators2() throws Exception {
    checkMarkedCalls("var x = 1;" +
                     "function f() {x++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators3
  public void testUnaryOperators3() throws Exception {
    checkMarkedCalls("function f() {var x = {foo : 0}; x.foo++}" +
                     "f()",
                     ImmutableList.<String>of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators4
  public void testUnaryOperators4() throws Exception {
    checkMarkedCalls("var x = {foo : 0};" +
                     "function f() {x.foo++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators5
  public void testUnaryOperators5() throws Exception {
    checkMarkedCalls("function f(x) {x.foo++}" +
                     "f({foo : 0})",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testDeleteOperator1
  public void testDeleteOperator1() throws Exception {
    checkMarkedCalls("var x = {};" +
                     "function f() {delete x}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testDeleteOperator2
  public void testDeleteOperator2() throws Exception {
    checkMarkedCalls("function f() {var x = {}; delete x}" +
                     "f()",
                     ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator1
  public void testOrOperator1() throws Exception {
    checkMarkedCalls("var f = externNsef1 || externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator2
  public void testOrOperator2() throws Exception {
    checkMarkedCalls("var f = function(){} || externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator3
  public void testOrOperator3() throws Exception {
    checkMarkedCalls("var f = externNsef2 || function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperators4
  public void testOrOperators4() throws Exception {
    checkMarkedCalls("var f = function(){} || function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator1
  public void testAndOperator1() throws Exception {
    checkMarkedCalls("var f = externNsef1 && externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator2
  public void testAndOperator2() throws Exception {
    checkMarkedCalls("var f = function(){} && externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator3
  public void testAndOperator3() throws Exception {
    checkMarkedCalls("var f = externNsef2 && function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperators4
  public void testAndOperators4() throws Exception {
    checkMarkedCalls("var f = function(){} && function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator1
  public void testHookOperator1() throws Exception {
    checkMarkedCalls("var f = true ? externNsef1 : externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator2
  public void testHookOperator2() throws Exception {
    checkMarkedCalls("var f = true ? function(){} : externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator3
  public void testHookOperator3() throws Exception {
    checkMarkedCalls("var f = true ? externNsef2 : function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperators4
  public void testHookOperators4() throws Exception {
    checkMarkedCalls("var f = true ? function(){} : function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testThrow1
  public void testThrow1() throws Exception {
    checkMarkedCalls("function f(){throw Error()};\n" +
                     "f()",
                     ImmutableList.<String>of("Error"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testThrow2
  public void testThrow2() throws Exception {
    checkMarkedCalls("function A(){throw Error()};\n" +
                     "function f(){return new A()}\n" +
                     "f()",
                     ImmutableList.<String>of("Error"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAssignmentOverride
  public void testAssignmentOverride() throws Exception {
    checkMarkedCalls("function A(){}\n" +
                     "A.prototype.foo = function(){};\n" +
                     "var a = new A;\n" +
                     "a.foo();\n",
                     ImmutableList.<String>of("A", "a.foo"));

    checkMarkedCalls("function A(){}\n" +
                     "A.prototype.foo = function(){};\n" +
                     "var x = 1\n" +
                     "function f(){x = 10}\n" +
                     "var a = new A;\n" +
                     "a.foo = f;\n" +
                     "a.foo();\n",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInheritance1
  public void testInheritance1() throws Exception {
    String source =
        CompilerTypeTestCase.CLOSURE_DEFS +
        "function I(){}\n" +
        "I.prototype.foo = function(){};\n" +
        "I.prototype.bar = function(){this.foo()};\n" +
        "function A(){};\n" +
        "goog.inherits(A, I)\n;" +
        "A.prototype.foo = function(){var data=24};\n" +
        "var i = new I();i.foo();i.bar();\n" +
        "var a = new A();a.foo();a.bar();";

    checkMarkedCalls(source,
                     ImmutableList.of("this.foo", "goog.inherits",
                                      "I", "i.foo", "i.bar",
                                      "A", "a.foo", "a.bar"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInheritance2
  public void testInheritance2() throws Exception {
    String source =
        CompilerTypeTestCase.CLOSURE_DEFS +
        "function I(){}\n" +
        "I.prototype.foo = function(){};\n" +
        "I.prototype.bar = function(){this.foo()};\n" +
        "function A(){};\n" +
        "goog.inherits(A, I)\n;" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "var i = new I();i.foo();i.bar();\n" +
        "var a = new A();a.foo();a.bar();";

    checkMarkedCalls(source, ImmutableList.of("goog.inherits", "I", "A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallBeforeDefinition
  public void testCallBeforeDefinition() throws Exception {
    checkMarkedCalls("f(); function f(){}",
                     ImmutableList.of("f"));

    checkMarkedCalls("var a = {}; a.f(); a.f = function (){}",
                     ImmutableList.of("a.f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis1
  public void testConstructorThatModifiesThis1() throws Exception {
    String source = "function A(){this.foo = 1}\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis2
  public void testConstructorThatModifiesThis2() throws Exception {
    String source = "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis3
  public void testConstructorThatModifiesThis3() throws Exception {

    
    String source = "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){this.bar()};\n" +
        "A.prototype.bar = function(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis4
  public void testConstructorThatModifiesThis4() throws Exception {

    
    String source = "function A(){foo.call(this)}\n" +
        "function foo(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesGlobal1
  public void testConstructorThatModifiesGlobal1() throws Exception {
    String source = "var b = 0;" +
        "function A(){b=1};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesGlobal2
  public void testConstructorThatModifiesGlobal2() throws Exception {
    String source = "var b = 0;" +
        "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){b=1};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionThatModifiesThis
  public void testCallFunctionThatModifiesThis() throws Exception {
    String source = "function A(){}\n" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "function f(){var a = new A; return a}\n" +
        "function g(){var a = new A; a.foo(); return a}\n" +
        "f(); g()";

    checkMarkedCalls(source, ImmutableList.<String>of("A", "A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrG
  public void testCallFunctionFOrG() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){ (f || g)() }\n" +
        "h()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f || g)", "h"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGViaHook
  public void testCallFunctionFOrGViaHook() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){ (false ? f : g)() }\n" +
        "h()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f : g)", "h"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionForGorH
  public void testCallFunctionForGorH() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){}\n" +
        "function i(){ (false ? f : (g || h))() }\n" +
        "i()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f : (g || h))", "i"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionFOrGWithSideEffects
  public void testCallFunctionFOrGWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function f(){x = 10}\n" +
        "function g(){}\n" +
        "function h(){ (f || g)() }\n" +
        "function i(){ (g || f)() }\n" +
        "function j(){ (f || f)() }\n" +
        "function k(){ (g || g)() }\n" +
        "h(); i(); j(); k()";

    checkMarkedCalls(source, ImmutableList.<String>of("(g || g)", "k"));
  }
