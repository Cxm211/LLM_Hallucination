// buggy code
  static boolean canBeSideEffected(Node n, Set<String> knownConstants) {
    switch (n.getType()) {
      case Token.CALL:
      case Token.NEW:
        // Function calls or constructor can reference changed values.
        // TODO(johnlenz): Add some mechanism for determining that functions
        // are unaffected by side effects.
        return true;
      case Token.NAME:
        // Non-constant names values may have been changed.
        return !NodeUtil.isConstantName(n)
            && !knownConstants.contains(n.getString());

      // Properties on constant NAMEs can still be side-effected.
      case Token.GETPROP:
      case Token.GETELEM:
        return true;

        // Anonymous functions definitions are not changed by side-effects,
        // and named functions are not part of expressions.
    }

    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      if (canBeSideEffected(c, knownConstants)) {
        return true;
      }
    }

    return false;
  }

  private static Collection<Definition> getCallableDefinitions(
      DefinitionProvider definitionProvider, Node name) {
      List<Definition> result = Lists.newArrayList();

      if (!NodeUtil.isGetProp(name) && !NodeUtil.isName(name)) {
        return null;
      }
      Collection<Definition> decls =
          definitionProvider.getDefinitionsReferencedAt(name);
      if (decls == null) {
        return null;
      }

      for (Definition current : decls) {
        Node rValue = current.getRValue();
        if ((rValue != null) && NodeUtil.isFunction(rValue)) {
          result.add(current);
        } else {
          return null;
        }
      }

      return result;
  }

// relevant test
// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testExtractingPrototypeWithNestedMembers
  public void testExtractingPrototypeWithNestedMembers() {
    extract(
        "x.prototype.y.a = 1;" +
        "x.prototype.y.b = 1;" +
        "x.prototype.y.c = 1;" +
        "x.prototype.y.d = 1;" +
        "x.prototype.y.e = 1;" +
        "x.prototype.y.f = 1;" +
        "x.prototype.y.g = 1;",
        loadPrototype("x") +
        TMP + ".y.a = 1;" +
        TMP + ".y.b = 1;" +
        TMP + ".y.c = 1;" +
        TMP + ".y.d = 1;" +
        TMP + ".y.e = 1;" +
        TMP + ".y.f = 1;" +
        TMP + ".y.g = 1;");
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testUsedNameInScope
  public void testUsedNameInScope() {
    test(
        "var a = 0;" +
        "x.prototype.y.a = 1;" +
        "x.prototype.y.b = 1;" +
        "x.prototype.y.c = 1;" +
        "x.prototype.y.d = 1;" +
        "x.prototype.y.e = 1;" +
        "x.prototype.y.f = 1;" +
        "x.prototype.y.g = 1;",
        "var b;" +
        "var a = 0;" +
        "b = x.prototype;" +
        "b.y.a = 1;" +
        "b.y.b = 1;" +
        "b.y.c = 1;" +
        "b.y.d = 1;" +
        "b.y.e = 1;" +
        "b.y.f = 1;" +
        "b.y.g = 1;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSimpleAssign
  public void testSimpleAssign() {
    inline("var x; x = 1; print(x)", "var x; print(1)");
    inline("var x; x = 1; x", "var x; 1");
    inline("var x; x = 1; var a = x", "var x; var a = 1");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSimpleVar
  public void testSimpleVar() {
    inline("var x = 1; print(x)", "var x; print(1)");
    inline("var x = 1; x", "var x; 1");
    inline("var x = 1; var a = x", "var x; var a = 1");    
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
    noInline("var x; x = 1 || x = 2; print(x)");
    noInline("var x; x = 1 && x = 2; print(x)");
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
    
    inline("var x = a + b; print(x);      x = a - b; print(x)",
           "var x;         print(a + b);             print(a - b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression9
  public void testInlineExpression9() {
    
    inline("var x; if (g) { x= a + b; print(x)    }  x = a - b; print(x)",
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
             "for(var x = 0; x < 10; x++) {j = x.next()};");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testRemoveWithLabels
  public void testRemoveWithLabels() {
    inline("var x = 1; L: x = 2; print(x)", "var x = 1; print(2)");
    inline("var x = 1; L: M: x = 2; print(x)", "var x = 1; print(2)");
    inline("var x = 1; L: M: N: x = 2; print(x)", "var x = 1; print(2)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineArguments
  public void testInlineArguments() {
    testSame("function _func(x) { print(x) }");
    testSame("function _func(x,y) { if(y) { x = 1 }; print(x) }");
    
    test("function(x, y) { x = 1; print(x) }",
         "function(x, y) { print(1) }");

    test("function(x, y) { if (y) { x = 1; print(x) }}",
         "function(x, y) { if (y) { print(1) }}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldBlock
  public void testFoldBlock() {
    fold("{{foo()}}", "foo()");
    fold("{foo();{}}", "foo()");
    fold("{{foo()}{}}", "foo()");
    fold("{{foo()}{bar()}}", "foo();bar()");
    fold("{if(false)foo(); {bar()}}", "bar()");
    fold("{if(false)if(false)if(false)foo(); {bar()}}", "bar()");

    fold("{'hi'}", "");
    fold("{x==3}", "");
    fold("{ (function(){x++}) }", "");
    fold("function(){return;}", "function(){return;}");
    fold("function(){return 3;}", "function(){return 3}");
    fold("function(){if(x)return; x=3; return; }",
         "function(){if(x)return; x=3; return; }");
    fold("{x=3;;;y=2;;;}", "x=3;y=2");

    
    fold("while(x()){x}", "while(x());");
    fold("while(x()){x()}", "while(x())x()");
    fold("for(x=0;x<100;x++){x}", "for(x=0;x<100;x++);");
    fold("for(x in y){x}", "for(x in y);");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldOneChildBlocks
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
    fold("function(){switch(x){default:{break}}}",
         "function(){switch(x){default:break}}");
    fold("function(){switch(x){default:{break}}}",
         "function(){switch(x){default:break}}");
    fold("function(){switch(x){default:x;case 1:return 2}}",
         "function(){switch(x){default:case 1:return 2}}");

    
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

    
    fold("if(x){if(true){foo();foo()}else{bar();bar()}}",
         "if(x){foo();foo()}");
    fold("if(x){if(false){foo();foo()}else{bar();bar()}}",
         "if(x){bar();bar()}");

    
    fold("if(x()){}", "x()");
    fold("if(x()){} else {x()}", "x()||x()");
    fold("if(x){}", ""); 
    fold("if(a()){A()} else if (b()) {} else {C()}",
         "if(a())A();else b()||C()");
    fold("if(a()){} else if (b()) {} else {C()}",
         "a()||b()||C()");
    fold("if(a()){A()} else if (b()) {} else if (c()) {} else{D()}",
         "if(a())A();else b()||c()||D()");
    fold("if(a()){} else if (b()) {} else if (c()) {} else{D()}",
         "a()||b()||c()||D()");
    fold("if(a()){A()} else if (b()) {} else if (c()) {} else{}",
         "if(a())A();else b()||c()");

    
    fold("function foo(){if(x()){}}", "function foo(){x()}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldOneChildBlocksStringCompare
  public void testFoldOneChildBlocksStringCompare() {
    
    assertResultString("if(x){if(y){var x;}}else{var z;}",
        "if(x){if(y)var x}else var z");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testNecessaryDanglingElse
  public void testNecessaryDanglingElse() {
    
    
    
    assertResultString(
        "if(x)if(y){y();z()}else;else x()", "if(x){if(y){y();z()}}else x()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldBlocksWithManyChildren
  public void testFoldBlocksWithManyChildren() {
    fold("function f() { if (false) {} }", "function f(){}");
    fold("function f() { { if (false) {} if (true) {} {} } }",
         "function f(){}");
    fold("{var x; var y; var z; function f() { { var a; { var b; } } } }",
         "var x;var y;var z;function f(){var a;var b}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldReturns
  public void testFoldReturns() {
    fold("function(){if(x)return 1;else return 2}",
         "function(){return x?1:2}");
    fold("function(){if(x)return 1+x;else return 2-x}",
         "function(){return x?1+x:2-x}");
    fold("function(){if(x)return y += 1;else return y += 2}",
         "function(){return x?(y+=1):(y+=2)}");

    
    foldSame("function(){if(x)return;else return 2-x}");
    foldSame("function(){if(x)return x;else return}");

    
    fold("function(){if(x)return;else return}",
         "function(){return}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldAssignments
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

// com.google.javascript.jscomp.FoldConstantsTest::testBug1059649
  public void testBug1059649() {
    
    fold("if(x){var y=3;}var z=5", "if(x)var y=3;var z=5");

    
    foldSame("if(x){var y=3;}else{var y=4;}var z=5");
    fold("while(x){var y=3;}var z=5", "while(x)var y=3;var z=5");
    fold("for(var i=0;i<10;i++){var y=3;}var z=5",
         "for(var i=0;i<10;i++)var y=3;var z=5");
    fold("for(var i in x){var y=3;}var z=5",
         "for(var i in x)var y=3;var z=5");
    fold("do{var y=3;}while(x);var z=5", "do var y=3;while(x);var z=5");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testUndefinedComparison
  public void testUndefinedComparison() {
    fold("if (0 == 0){ x = 1; } else { x = 2; }", "x=1");
    fold("if (undefined == undefined){ x = 1; } else { x = 2; }", "x=1");
    fold("if (undefined == null){ x = 1; } else { x = 2; }", "x=1");
    
    fold("if (undefined == 0){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined == 1){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined == 'hi'){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined == true){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined == false){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined === undefined){ x = 1; } else { x = 2; }", "x=1");
    fold("if (undefined === null){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined === void 0){ x = 1; } else { x = 2; }", "x=1");
    
    foldSame("x = (undefined == this) ? 1 : 2;");
    foldSame("x = (undefined == x) ? 1 : 2;");

    fold("if (undefined != undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined != null){ x = 1; } else { x = 2; }", "x=2");
    
    fold("if (undefined != 0){ x = 1; } else { x = 2; }", "x=1");
    fold("if (undefined != 1){ x = 1; } else { x = 2; }", "x=1");
    fold("if (undefined != 'hi'){ x = 1; } else { x = 2; }", "x=1");
    fold("if (undefined != true){ x = 1; } else { x = 2; }", "x=1");
    fold("if (undefined != false){ x = 1; } else { x = 2; }", "x=1");
    fold("if (undefined !== undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined !== null){ x = 1; } else { x = 2; }", "x=1");
    foldSame("x = (undefined != this) ? 1 : 2;");
    foldSame("x = (undefined != x) ? 1 : 2;");

    fold("if (undefined < undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined > undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined >= undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined <= undefined){ x = 1; } else { x = 2; }", "x=2");

    fold("if (0 < undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (true > undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if ('hi' >= undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (null <= undefined){ x = 1; } else { x = 2; }", "x=2");

    fold("if (undefined < 0){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined > true){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined >= 'hi'){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined <= null){ x = 1; } else { x = 2; }", "x=2");

    fold("if (null == undefined){ x = 1; } else { x = 2; }", "x=1");
    
    fold("if (0 == undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (1 == undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if ('hi' == undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (true == undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (false == undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (null === undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (void 0 === undefined){ x = 1; } else { x = 2; }", "x=1");
    
    foldSame("x = (this == undefined) ? 1 : 2;");
    foldSame("x = (x == undefined) ? 1 : 2;");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testHookIf
  public void testHookIf() {
    fold("if (1){ x=1; } else { x = 2;}", "x=1");
    fold("if (false){ x = 1; } else { x = 2; }", "x=2");
    fold("if (undefined){ x = 1; } else { x = 2; }", "x=2");
    fold("if (null){ x = 1; } else { x = 2; }", "x=2");
    fold("if (void 0){ x = 1; } else { x = 2; }", "x=2");
    
    fold("if (false){ x = 1; } else if (true) { x = 3; } else { x = 2; }",
         "x=3");
    fold("if (false){ x = 1; } else if (cond) { x = 2; } else { x = 3; }",
         "x=cond?2:3");
    fold("var x = (true) ? 1 : 0", "var x=1");
    fold("var y = (true) ? ((false) ? 12 : (cond ? 1 : 2)) : 13",
         "var y=cond?1:2");
    fold("if (x){ x = 1; } else if (false) { x = 3; }", "if(x)x=1");
    fold("x?void 0:y()", "x||y()");
    fold("!x?void 0:y()", "x&&y()");
    foldSame("var z=x?void 0:y()");
    foldSame("z=x?void 0:y()");
    foldSame("z*=x?void 0:y()");
    fold("x?y():void 0", "x&&y()");
    foldSame("var z=x?y():void 0");
    foldSame("(w?x:void 0).y=z");
    foldSame("(w?x:void 0).y+=z");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testRemoveDuplicateStatements
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
    fold("function z() {if (a) { return true }" +
         "else if (b) { return true }" +
         "else { return true }}",
         "function z() {return true;}");
    fold("function z() {if (a()) { return true }" +
         "else if (b()) { return true }" +
         "else { return true }}",
         "function z() {if (!a()) { b() } return true;}");
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

// com.google.javascript.jscomp.FoldConstantsTest::testNotCond
  public void testNotCond() {
    fold("function(){if(!x)foo()}", "function(){x||foo()}");
    fold("function(){if(!x)b=1}", "function(){x||(b=1)}");
    fold("if(!x)z=1;else if(y)z=2", "if(x){if(y)z=2}else z=1");
    foldSame("function(){if(!(x=1))a.b=1}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testAndParenthesesCount
  public void testAndParenthesesCount() {
    foldSame("function(){if(x||y)a.foo()}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testUnaryOps
  public void testUnaryOps() {
    fold("!foo()", "foo()");
    fold("~foo()", "foo()");
    fold("-foo()", "foo()");
    fold("a=!true", "a=false");
    fold("a=!10", "a=false");
    fold("a=!false", "a=true");
    fold("a=!foo()", "a=!foo()");
    fold("a=-0", "a=0");
    fold("a=-Infinity", "a=-Infinity");
    fold("a=-NaN", "a=NaN");
    fold("a=-foo()", "a=-foo()");
    fold("a=~~0", "a=0");
    fold("a=~~10", "a=10");
    fold("a=~-7", "a=6");
    fold("a=~0x100000000", "a=~0x100000000",
         FoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("a=~-0x100000000", "a=~-0x100000000",
         FoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("a=~.5", "~.5", FoldConstants.FRACTIONAL_BITWISE_OPERAND);
  }

// com.google.javascript.jscomp.FoldConstantsTest::testUnaryOpsStringCompare
  public void testUnaryOpsStringCompare() {
    
    assertResultString("a=-1", "a=-1");
    assertResultString("a=~0", "a=-1");
    assertResultString("a=~1", "a=-2");
    assertResultString("a=~101", "a=-102");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldLogicalOp
  public void testFoldLogicalOp() {
    fold("x = true && x", "x = x");
    fold("x = false && x", "x = false");
    fold("x = true || x", "x = true");
    fold("x = false || x", "x = x");
    fold("x = 0 && x", "x = 0");
    fold("x = 3 || x", "x = 3");
    fold("x = false || 0", "x = 0");

    fold("if(x && true) z()", "x&&z()");
    fold("if(x && false) z()", "");
    fold("if(x || 3) z()", "z()");
    fold("if(x || false) z()", "x&&z()");
    fold("if(x==y && false) z()", "");

    
    
    fold("if(y() || x || 3) z()", "if(y()||x||1)z()");

    
    fold("a = x && true", "a=x&&true");
    fold("a = x && false", "a=x&&false");
    fold("a = x || 3", "a=x||3");
    fold("a = x || false", "a=x||false");
    fold("a = b ? c : x || false", "a=b?c:x||false");
    fold("a = b ? x || false : c", "a=b?x||false:c");
    fold("a = b ? c : x && true", "a=b?c:x&&true");
    fold("a = b ? x && true : c", "a=b?x&&true:c");

    
    fold("a = x || false ? b : c", "a=x?b:c");
    fold("a = x && true ? b : c", "a=x?b:c");

    
    fold("if(foo() || true) z()", "if(foo()||1)z()");

    fold("x = foo() || true || bar()", "x = foo()||true");
    fold("x = foo() || false || bar()", "x = foo()||bar()");
    fold("x = foo() || true && bar()", "x = foo()||bar()");
    fold("x = foo() || false && bar()", "x = foo()||false");
    fold("x = foo() && false && bar()", "x = foo()&&false");
    fold("x = foo() && true && bar()", "x = foo()&&bar()");
    fold("x = foo() && false || bar()", "x = foo()&&false||bar()");

    
    
    
    fold("x = foo() && true || bar()", "x = foo()&&true||bar()");
    fold("foo() && true || bar()", "foo()&&1||bar()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldLogicalOpStringCompare
  public void testFoldLogicalOpStringCompare() {
    
    
    assertResultString("if(foo() && false) z()", "foo()&&0&&z()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldBitwiseOp
  public void testFoldBitwiseOp() {
    fold("x = 1 & 1", "x = 1");
    fold("x = 1 & 2", "x = 0");
    fold("x = 3 & 1", "x = 1");
    fold("x = 3 & 3", "x = 3");

    fold("x = 1 | 1", "x = 1");
    fold("x = 1 | 2", "x = 3");
    fold("x = 3 | 1", "x = 3");
    fold("x = 3 | 3", "x = 3");

    fold("x = -1 & 0", "x = 0");
    fold("x = 0 & -1", "x = 0");
    fold("x = 1 & 4", "x = 0");
    fold("x = 2 & 3", "x = 2");

    
    
    fold("x = 1 & 1.1", "x = 1&1.1");
    fold("x = 1.1 & 1", "x = 1.1&1");
    fold("x = 1 & 3000000000", "x = 1&3000000000");
    fold("x = 3000000000 & 1", "x = 3000000000&1");

    
    fold("x = 1 | 4", "x = 5");
    fold("x = 1 | 3", "x = 3");
    fold("x = 1 | 1.1", "x = 1|1.1");
    fold("x = 1 | 3000000000", "x = 1|3000000000");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldBitwiseOpStringCompare
  public void testFoldBitwiseOpStringCompare() {
    assertResultString("x = -1 | 0", "x=-1");
    assertResultString("-1 | 0", "1");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldBitShifts
  public void testFoldBitShifts() {
    fold("x = 1 << 0", "x = 1");
    fold("x = 1 << 1", "x = 2");
    fold("x = 3 << 1", "x = 6");
    fold("x = 1 << 8", "x = 256");

    fold("x = 1 >> 0", "x = 1");
    fold("x = 1 >> 1", "x = 0");
    fold("x = 2 >> 1", "x = 1");
    fold("x = 5 >> 1", "x = 2");
    fold("x = 127 >> 3", "x = 15");
    fold("x = 3 >> 1", "x = 1");
    fold("x = 3 >> 2", "x = 0");
    fold("x = 10 >> 1", "x = 5");
    fold("x = 10 >> 2", "x = 2");
    fold("x = 10 >> 5", "x = 0");

    fold("x = 10 >>> 1", "x = 5");
    fold("x = 10 >>> 2", "x = 2");
    fold("x = 10 >>> 5", "x = 0");
    fold("x = -1 >>> 1", "x = " + 0x7fffffff);

    fold("3000000000 << 1", "3000000000<<1",
         FoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("1 << 32", "1<<32",
         FoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    fold("1 << -1", "1<<32",
         FoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    fold("3000000000 >> 1", "3000000000>>1",
         FoldConstants.BITWISE_OPERAND_OUT_OF_RANGE);
    fold("1 >> 32", "1>>32",
         FoldConstants.SHIFT_AMOUNT_OUT_OF_BOUNDS);
    fold("1.5 << 0",  "1.5<<0",  FoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1 << .5",   "1.5<<0",  FoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1.5 >>> 0", "1.5>>>0", FoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1 >>> .5",  "1.5>>>0", FoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1.5 >> 0",  "1.5>>0",  FoldConstants.FRACTIONAL_BITWISE_OPERAND);
    fold("1 >> .5",   "1.5>>0",  FoldConstants.FRACTIONAL_BITWISE_OPERAND);
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldBitShiftsStringCompare
  public void testFoldBitShiftsStringCompare() {
    
    assertResultString("x = -1 << 1", "x=-2");
    assertResultString("x = -1 << 8", "x=-256");
    assertResultString("x = -1 >> 1", "x=-1");
    assertResultString("x = -2 >> 1", "x=-1");
    assertResultString("x = -1 >> 0", "x=-1");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testStringAdd
  public void testStringAdd() {
    fold("x = 'a' + \"bc\"", "x = \"abc\"");
    fold("x = 'a' + 5", "x = \"a5\"");
    fold("x = 5 + 'a'", "x = \"5a\"");
    fold("x = 'a' + ''", "x = \"a\"");
    fold("x = \"a\" + foo()", "x = \"a\"+foo()");
    fold("x = foo() + 'a' + 'b'", "x = foo()+\"ab\"");
    fold("x = (foo() + 'a') + 'b'", "x = foo()+\"ab\"");  
    fold("x = foo() + 'a' + 'b' + 'cd' + bar()", "x = foo()+\"abcd\"+bar()");
    fold("x = foo() + 2 + 'b'", "x = foo()+2+\"b\"");  
    fold("x = foo() + 'a' + 2", "x = foo()+\"a2\"");
    fold("x = '' + null", "x = \"null\"");
    fold("x = true + '' + false", "x = \"truefalse\"");
    fold("x = '' + []", "x = \"\"+[]");      
  }

// com.google.javascript.jscomp.FoldConstantsTest::testStringIndexOf
  public void testStringIndexOf() {
    fold("x = 'abcdef'.indexOf('b')", "x = 1");
    fold("x = 'abcdefbe'.indexOf('b', 2)", "x = 6");
    fold("x = 'abcdef'.indexOf('bcd')", "x = 1");
    fold("x = 'abcdefsdfasdfbcdassd'.indexOf('bcd', 4)", "x = 13");

    fold("x = 'abcdef'.lastIndexOf('b')", "x = 1");
    fold("x = 'abcdefbe'.lastIndexOf('b')", "x = 6");
    fold("x = 'abcdefbe'.lastIndexOf('b', 5)", "x = 1");

    
    
    fold("x = 'abc1def'.indexOf(1)", "x = 3");
    fold("x = 'abcNaNdef'.indexOf(NaN)", "x = 3");
    fold("x = 'abcundefineddef'.indexOf(undefined)", "x = 3");
    fold("x = 'abcnulldef'.indexOf(null)", "x = 3");
    fold("x = 'abctruedef'.indexOf(true)", "x = 3");

    
    
    foldSame("x = NaN.indexOf('bcd')");
    foldSame("x = undefined.indexOf('bcd')");
    foldSame("x = null.indexOf('bcd')");
    foldSame("x = true.indexOf('bcd')");
    foldSame("x = false.indexOf('bcd')");

    
    foldSame("x = 'abcdef'.indexOf(/b./)");
    foldSame("x = 'abcdef'.indexOf({a:2})");
    foldSame("x = 'abcdef'.indexOf([1,2])");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testStringJoinAdd
  public void testStringJoinAdd() {
    fold("x = ['a', 'b', 'c'].join('')", "x = \"abc\"");
    fold("x = [].join(',')", "x = \"\"");
    fold("x = ['a'].join(',')", "x = \"a\"");
    fold("x = ['a', 'b', 'c'].join(',')", "x = \"a,b,c\"");
    fold("x = ['a', foo, 'b', 'c'].join(',')", "x = [\"a\",foo,\"b,c\"].join(\",\")");
    fold("x = [foo, 'a', 'b', 'c'].join(',')", "x = [foo,\"a,b,c\"].join(\",\")");
    fold("x = ['a', 'b', 'c', foo].join(',')", "x = [\"a,b,c\",foo].join(\",\")");

    
    fold("x = ['a=', 5].join('')", "x = \"a=5\"");
    fold("x = ['a', '5'].join(7)", "x = \"a75\"");

    
    fold("x = ['a=', false].join('')", "x = \"a=false\"");
    fold("x = ['a', '5'].join(true)", "x = \"atrue5\"");
    fold("x = ['a', '5'].join(false)", "x = \"afalse5\"");

    
    fold("x = ['a', '5', 'c'].join('a very very very long chain')",
         "x = [\"a\",\"5\",\"c\"].join(\"a very very very long chain\")");

    
    foldSame("x = ['', foo].join(',')");
    foldSame("x = ['', foo, ''].join(',')");

    fold("x = ['', '', foo, ''].join(',')", "x = [',', foo, ''].join(',')");
    fold("x = ['', '', foo, '', ''].join(',')",
         "x = [',', foo, ','].join(',')");

    fold("x = ['', '', foo, '', '', bar].join(',')",
         "x = [',', foo, ',', bar].join(',')");

    fold("x = [1,2,3].join('abcdef')",
         "x = '1abcdef2abcdef3'");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testStringJoinAdd_b1992789
  public void testStringJoinAdd_b1992789() {
    fold("x = ['a'].join('')", "x = \"a\"");
    fold("x = [foo()].join('')", "x = '' + foo()");
    fold("[foo()].join('')", "'' + foo()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldArithmetic
  public void testFoldArithmetic() {
    fold("x = 10 + 20", "x = 30");
    fold("x = 2 / 4", "x = 0.5");
    fold("x = 2.25 * 3", "x = 6.75");
    fold("z = x * y", "z = x * y");
    fold("x = y * 5", "x = y * 5");
    fold("x = 1 / 0", "", FoldConstants.DIVIDE_BY_0_ERROR);
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldArithmeticStringComp
  public void testFoldArithmeticStringComp() {
    
    assertResultString("x = 10 - 20", "x=-10");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldComparison
  public void testFoldComparison() {
    fold("x = 0 == 0", "x = true");
    fold("x = 1 == 2", "x = false");
    fold("x = 'abc' == 'def'", "x = false");
    fold("x = 'abc' == 'abc'", "x = true");
    fold("x = \"\" == ''", "x = true");
    fold("x = foo() == bar()", "x = foo()==bar()");

    fold("x = 1 != 0", "x = true");
    fold("x = 'abc' != 'def'", "x = true");
    fold("x = 'a' != 'a'", "x = false");

    fold("x = 1 < 20", "x = true");
    fold("x = 3 < 3", "x = false");
    fold("x = 10 > 1.0", "x = true");
    fold("x = 10 > 10.25", "x = false");
    fold("x = y == y", "x = y==y");
    fold("x = y < y", "x = false");
    fold("x = y > y", "x = false");
    fold("x = 1 <= 1", "x = true");
    fold("x = 1 <= 0", "x = false");
    fold("x = 0 >= 0", "x = true");
    fold("x = -1 >= 9", "x = false");

    fold("x = true == true", "x = true");
    fold("x = true == true", "x = true");
    fold("x = false == null", "x = false");
    fold("x = false == true", "x = false");
    fold("x = true == null", "x = false");

    fold("0 == 0", "1");
    fold("1 == 2", "0");
    fold("'abc' == 'def'", "0");
    fold("'abc' == 'abc'", "1");
    fold("\"\" == ''", "1");
    fold("foo() == bar()", "foo()==bar()");

    fold("1 != 0", "1");
    fold("'abc' != 'def'", "1");
    fold("'a' != 'a'", "0");

    fold("1 < 20", "1");
    fold("3 < 3", "0");
    fold("10 > 1.0", "1");
    fold("10 > 10.25", "0");
    fold("x == x", "x==x");
    fold("x < x", "0");
    fold("x > x", "0");
    fold("1 <= 1", "1");
    fold("1 <= 0", "0");
    fold("0 >= 0", "1");
    fold("-1 >= 9", "0");

    fold("true == true", "1");
    fold("false == null", "0");
    fold("false == true", "0");
    fold("true == null", "0");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldComparison2
  public void testFoldComparison2() {
    fold("x = 0 === 0", "x = true");
    fold("x = 1 === 2", "x = false");
    fold("x = 'abc' === 'def'", "x = false");
    fold("x = 'abc' === 'abc'", "x = true");
    fold("x = \"\" === ''", "x = true");
    fold("x = foo() === bar()", "x = foo()===bar()");

    fold("x = 1 !== 0", "x = true");
    fold("x = 'abc' !== 'def'", "x = true");
    fold("x = 'a' !== 'a'", "x = false");

    fold("x = y === y", "x = y===y");

    fold("x = true === true", "x = true");
    fold("x = true === true", "x = true");
    fold("x = false === null", "x = false");
    fold("x = false === true", "x = false");
    fold("x = true === null", "x = false");

    fold("0 === 0", "1");
    fold("1 === 2", "0");
    fold("'abc' === 'def'", "0");
    fold("'abc' === 'abc'", "1");
    fold("\"\" === ''", "1");
    fold("foo() === bar()", "foo()===bar()");

    
    foldSame("1 === '1'");
    foldSame("1 === true");
    foldSame("1 !== '1'");
    foldSame("1 !== true");
    
    fold("1 !== 0", "1");
    fold("'abc' !== 'def'", "1");
    fold("'a' !== 'a'", "0");

    fold("x === x", "x===x");

    fold("true === true", "1");
    fold("false === null", "0");
    fold("false === true", "0");
    fold("true === null", "0");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldNot
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
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldGetElem
  public void testFoldGetElem() {
    fold("x = [10, 20][0]", "x = 10");
    fold("x = [10, 20][1]", "x = 20");
    fold("x = [10, 20][0.5]", "", FoldConstants.INVALID_GETELEM_INDEX_ERROR);
    fold("x = [10, 20][-1]",    "", FoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
    fold("x = [10, 20][2]",     "", FoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldComplex
  public void testFoldComplex() {
    fold("x = (3 / 1.0) + (1 * 2)", "x = 5");
    fold("x = (1 == 1.0) && foo() && true", "x = foo()&&true");
    fold("x = 'abc' + 5 + 10", "x = \"abc510\"");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldArrayLength
  public void testFoldArrayLength() {
    
    fold("x = [].length", "x = 0");
    fold("x = [1,2,3].length", "x = 3");
    fold("x = [a,b].length", "x = 2");

    
    fold("x = [foo(), 0].length", "x = [foo(),0].length");
    fold("x = y.length", "x = y.length");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldStringLength
  public void testFoldStringLength() {
    
    fold("x = ''.length", "x = 0");
    fold("x = '123'.length", "x = 3");

    
    fold("x = '123\u01dc'.length", "x = 4");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldRegExpConstructor
  public void testFoldRegExpConstructor() {
    
    
    fold("x = new RegExp",                    "x = new RegExp");
    
    fold("x = new RegExp(\"\")",              "x = new RegExp(\"\")");
    fold("x = new RegExp(\"\", \"i\")",       "x = new RegExp(\"\",\"i\")");
    
    fold("x = new RegExp(\"foobar\", \"bogus\")",
         "x = new RegExp(\"foobar\",\"bogus\")",
         FoldConstants.INVALID_REGULAR_EXPRESSION_FLAGS);
    
    fold("x = new RegExp(\"foobar\", \"g\")",
         "x = new RegExp(\"foobar\",\"g\")");
    fold("x = new RegExp(\"foobar\", \"ig\")",
         "x = new RegExp(\"foobar\",\"ig\")");

    
    fold("x = new RegExp(\"foobar\")",        "x = /foobar/");
    fold("x = new RegExp(\"foobar\", \"i\")", "x = /foobar/i");
    
    fold("x = new RegExp(\"\\\\.\", \"i\")",  "x = /\\./i");
    fold("x = new RegExp(\"/\", \"\")",       "x = /\\//");
    fold("x = new RegExp(\"///\", \"\")",     "x = /\\/\\/\\//");
    fold("x = new RegExp(\"\\\\\\/\", \"\")", "x = /\\//");
    
    
    fold("x = new RegExp(\"\\u2028\")", "x = new RegExp(\"\\u2028\")");
    fold("x = new RegExp(\"\\\\\\\\u2028\")", "x = /\\\\u2028/");

    
    
    String longRegexp = "";
    for (int i = 0; i < 200; i++) longRegexp += "x";
    foldSame("x = new RegExp(\"" + longRegexp + "\")");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldRegExpConstructorStringCompare
  public void testFoldRegExpConstructorStringCompare() {
    
    
    assertResultString("x=new RegExp(\"\\n\", \"i\")", "x=/\\n/i");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldTypeof
  public void testFoldTypeof() {
    fold("x = typeof 1", "x = \"number\"");
    fold("x = typeof 'foo'", "x = \"string\"");
    fold("x = typeof true", "x = \"boolean\"");
    fold("x = typeof false", "x = \"boolean\"");
    fold("x = typeof null", "x = \"object\"");
    fold("x = typeof undefined", "x = \"undefined\"");
    fold("x = typeof []", "x = \"object\"");
    fold("x = typeof [1]", "x = \"object\"");
    fold("x = typeof [1,[]]", "x = \"object\"");
    fold("x = typeof {}", "x = \"object\"");

    foldSame("x = typeof[1,[foo()]]");
    foldSame("x = typeof{bathwater:baby()}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldLiteralConstructors
  public void testFoldLiteralConstructors() {
    
    fold("x = new Array", "x = []");
    fold("x = new Array()", "x = []");
    fold("x = new Object", "x = ({})");
    fold("x = new Object()", "x = ({})");

    
    fold("x = new Array(7)", "x = new Array(7)");

    
    fold("x = " +
         "(function(){function Object(){this.x=4};return new Object();})();",
         "x = (function(){function Object(){this.x=4}return new Object})()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testVarLifting
  public void testVarLifting() {
    fold("if(true)var a", "var a");
    fold("if(false)var a", "var a");
    fold("if(true);else var a;", "var a");
    fold("if(false) foo();else var a;", "var a");
    fold("if(true)var a;else;", "var a");
    fold("if(false)var a;else;", "var a");
    fold("if(false)var a,b;", "var b; var a");
    fold("if(false){var a;var a;}", "var a");
    fold("if(false)var a=function(){var b};", "var a");
    fold("if(a)if(false)var a;else var b;", "var a;if(a)var b");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testContainsUnicodeEscape
  public void testContainsUnicodeEscape() throws Exception {
    assertTrue(!FoldConstants.containsUnicodeEscape(""));
    assertTrue(!FoldConstants.containsUnicodeEscape("foo"));
    assertTrue( FoldConstants.containsUnicodeEscape("\u2028"));
    assertTrue( FoldConstants.containsUnicodeEscape("\\u2028"));
    assertTrue( FoldConstants.containsUnicodeEscape("foo\\u2028"));
    assertTrue(!FoldConstants.containsUnicodeEscape("foo\\\\u2028"));
    assertTrue( FoldConstants.containsUnicodeEscape("foo\\\\u2028bar\\u2028"));
  }

// com.google.javascript.jscomp.FoldConstantsTest::testBug1438784
  public void testBug1438784() throws Exception {
    fold("for(var i=0;i<10;i++)if(x)x.y;", "for(var i=0;i<10;i++);");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldUselessWhile
  public void testFoldUselessWhile() {
    fold("while(false) { foo() }", "");
    fold("while(!true) { foo() }", "");
    fold("while(void 0) { foo() }", "");
    fold("while(undefined) { foo() }", "");
    fold("while(!false) foo() ", "while(1) foo()");
    fold("while(true) foo() ", "while(1) foo() ");
    fold("while(!void 0) foo()", "while(1) foo()");
    fold("while(false) { var a = 0; }", "var a");

    
    fold("while(false) { foo(); continue }", "");

    
    fold("if(foo())while(false){foo()}else bar()", "foo()||bar()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldUselessFor
  public void testFoldUselessFor() {
    fold("for(;false;) { foo() }", "");
    fold("for(;!true;) { foo() }", "");
    fold("for(;void 0;) { foo() }", "");
    fold("for(;undefined;) { foo() }", "");
    fold("for(;!false;) foo() ", "for(;;) foo()");
    fold("for(;true;) foo() ", "for(;;) foo() ");
    fold("for(;1;) foo()", "for(;;) foo()");
    foldSame("for(;;) foo()");
    fold("for(;!void 0;) foo()", "for(;;) foo()");
    fold("for(;false;) { var a = 0; }", "var a");

    
    fold("for(;false;) { foo(); continue }", "");

    
    fold("if(foo())for(;false;){foo()}else bar()", "foo()||bar()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldUselessDo
  public void testFoldUselessDo() {
    fold("do { foo() } while(false);", "foo()");
    fold("do { foo() } while(!true);", "foo()");
    fold("do { foo() } while(void 0);", "foo()");
    fold("do { foo() } while(undefined);", "foo()");
    fold("do { foo() } while(!false);", "do { foo() } while(1);");
    fold("do { foo() } while(true);", "do { foo() } while(1);");
    fold("do { foo() } while(!void 0);", "do { foo() } while(1);");
    fold("do { var a = 0; } while(false);", "var a=0");

    
    foldSame("do { foo(); continue; } while(0)");
    foldSame("do { foo(); break; } while(0)");

    
    fold("if(foo())do {foo()} while(false) else bar()", "foo()?foo():bar()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testMinimizeCondition
  public void testMinimizeCondition() {
    
    fold("while(!!true) foo()", "while(1) foo()");
    
    fold("while(!!x) foo()", "while(x) foo()");
    fold("while(!(!x&&!y)) foo()", "while(x||y) foo()");
    fold("while(x||!!y) foo()", "while(x||y) foo()");
    fold("while(!(!!x&&y)) foo()", "while(!(x&&y)) foo()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testMinimizeCondition_example1
  public void testMinimizeCondition_example1() {
    
    fold("if(!!(f() > 20)) {foo();foo()}", "if(f() > 20){foo();foo()}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testMinimizeWhileConstantCondition
  public void testMinimizeWhileConstantCondition() {
    fold("while(true) foo()", "while(1) foo()");
    fold("while(!false) foo()", "while(1) foo()");
    fold("while(202) foo()", "while(1) foo()");
    fold("while(Infinity) foo()", "while(1) foo()");
    fold("while('text') foo()", "while(1) foo()");
    fold("while([]) foo()", "while(1) foo()");
    fold("while({}) foo()", "while(1) foo()");
    fold("while(/./) foo()", "while(1) foo()");
    fold("while(0) foo()", "");
    fold("while(0.0) foo()", "");
    fold("while(NaN) foo()", "");
    fold("while(null) foo()", "");
    fold("while(undefined) foo()", "");
    fold("while('') foo()", "");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testMinimizeExpr
  public void testMinimizeExpr() {
    fold("!!true", "0");
    fold("!!x", "x");
    fold("!(!x&&!y)", "!x&&!y");
    fold("x||!!y", "x||y");
    fold("!(!!x&&y)", "x&&y");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testBug1509085
  public void testBug1509085() {
    new FoldConstantsTest() {
      @Override
      protected int getNumRepetitions() {
        return 1;
      }
    }.fold("x ? x() : void 0", "if(x) x();");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldInstanceOf
  public void testFoldInstanceOf() {
    
    fold("64 instanceof Object", "0");
    fold("64 instanceof Number", "0");
    fold("'' instanceof Object", "0");
    fold("'' instanceof String", "0");
    fold("true instanceof Object", "0");
    fold("true instanceof Boolean", "0");
    fold("false instanceof Object", "0");
    fold("null instanceof Object", "0");
    fold("undefined instanceof Object", "0");
    fold("NaN instanceof Object", "0");
    fold("Infinity instanceof Object", "0");

    
    fold("[] instanceof Object", "1");
    fold("({}) instanceof Object", "1");

    
    foldSame("new Foo() instanceof Object");
    
    foldSame("[] instanceof Foo");
    foldSame("({}) instanceof Foo");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testDivision
  public void testDivision() {
    
    fold("print(1/3)", "print(1/3)");

    
    
    fold("print(1/2)", "print(0.5)");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testAssignOps
  public void testAssignOps() {
    fold("x=x+y", "x+=y");
    fold("x=x*y", "x*=y");
    fold("x.y=x.y+z", "x.y+=z");
    foldSame("next().x = next().x + 1");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldConditionalVarDeclaration
  public void testFoldConditionalVarDeclaration() {
    fold("if(x) var y=1;else y=2", "var y=x?1:2");
    fold("if(x) y=1;else var y=2", "var y=x?1:2");

    foldSame("if(x) var y = 1; z = 2");
    foldSame("if(x) y = 1; var z = 2");

    foldSame("if(x) { var y = 1; print(y)} else y = 2 ");
    foldSame("if(x) var y = 1; else {y = 2; print(y)}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldReturnResult
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

// com.google.javascript.jscomp.FoldConstantsTest::testBugIssue3
  public void testBugIssue3() {
    foldSame("function foo() {" +
             "  if(sections.length != 1) children[i] = 0;" +
             "  else var selectedid = children[i]" +
             "}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testBugIssue43
  public void testBugIssue43() {
    foldSame("function foo() {" +
             "  if (a) { var b = 1; } else { a.b = 1; }" +
             "}");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testFoldConstantCommaExpressions
  public void testFoldConstantCommaExpressions() {
    fold("if (true, false) {foo()}", "");
    fold("if (false, true) {foo()}", "foo()");
    fold("true, foo()", "foo()");
    fold("(1 + 2 + ''), foo()", "foo()");
  }

// com.google.javascript.jscomp.FoldConstantsTest::testSplitCommaExpressions
  public void testSplitCommaExpressions() {
    
    foldSame("if (foo(), true) boo()");
    foldSame("var a = (foo(), true);");
    foldSame("a = (foo(), true);");

    fold("(x=2), foo()", "x=2; foo()");
    fold("foo(), boo();", "foo(); boo()");    
    fold("(a(), b()), (c(), d());", "a(); b(); c(); d();");    
    
    
    
    fold("foo(), true", "foo();1");
    fold("function x(){foo(), true}", "function x(){foo();}");
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testFindModifiedParameters1
  public void testFindModifiedParameters1() {
    assertEquals(Sets.newHashSet(),
        FunctionArgumentInjector.findModifiedParameters(
            parseFunction("function (a){ return a==0; }")));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testFindModifiedParameters2
  public void testFindModifiedParameters2() {
    assertEquals(Sets.newHashSet(),
        FunctionArgumentInjector.findModifiedParameters(
            parseFunction("function (a){ b=a }")));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testFindModifiedParameters3
  public void testFindModifiedParameters3() {
    assertEquals(Sets.newHashSet("a"),
        FunctionArgumentInjector.findModifiedParameters(
            parseFunction("function (a){ a=0 }")));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testFindModifiedParameters4
  public void testFindModifiedParameters4() {
    assertEquals(Sets.newHashSet("a", "b"),
        FunctionArgumentInjector.findModifiedParameters(
            parseFunction("function (a,b){ a=0;b=0 }")));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testFindModifiedParameters5
  public void testFindModifiedParameters5() {
    assertEquals(Sets.newHashSet("b"),
        FunctionArgumentInjector.findModifiedParameters(
            parseFunction("function (a,b){ a; if (a) b=0 }")));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments1
  public void testMaybeAddTempsForCallArguments1() {
    
    
    testNeededTemps(
        "function foo(a,b){}; foo(goo(),goo());",
        "foo",
        Sets.newHashSet("a", "b"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments2
  public void testMaybeAddTempsForCallArguments2() {
    
    
    testNeededTemps(
        "function foo(a,b){}; foo(1,2);",
        "foo",
        EMPTY_STRING_SET);
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments3
  public void testMaybeAddTempsForCallArguments3() {
    
    
    testNeededTemps(
        "function foo(a,b){a;b;}; foo(x,y);",
        "foo",
        EMPTY_STRING_SET);
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments4
  public void testMaybeAddTempsForCallArguments4() {
    
    
    testNeededTemps(
        "function foo(a,b){a;goo();b;}; foo(x,y);",
        "foo",
        Sets.newHashSet("b"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments5
  public void testMaybeAddTempsForCallArguments5() {
    
    
    testNeededTemps(
        "function foo(a,b){x = b; y = a;}; foo(x,y);",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments6
  public void testMaybeAddTempsForCallArguments6() {
    
    
    testNeededTemps(
        "function foo(a){x++;a;}; foo(x);",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments7
  public void testMaybeAddTempsForCallArguments7() {
    
    testNeededTemps(
        "function foo(a){var c; c=0; a;}; foo(x);",
        "foo",
        EMPTY_STRING_SET);
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments8
  public void testMaybeAddTempsForCallArguments8() {
    
    testNeededTemps(
        "function foo(a){var c = {}; c.goo=0; a;}; foo(x);",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments9
  public void testMaybeAddTempsForCallArguments9() {
    
    
    testNeededTemps(
        "function foo(a,b){while(true){a;goo();b;}}; foo(x,y);",
        "foo",
        Sets.newHashSet("a", "b"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments10
  public void testMaybeAddTempsForCallArguments10() {
    
    testNeededTemps(
        "function foo(a,b){while(true){a;true;b;}}; foo(x,y);",
        "foo",
        EMPTY_STRING_SET);
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments11
  public void testMaybeAddTempsForCallArguments11() {
    
    
    testNeededTemps(
        "function foo(a,b){do{a;b;}while(goo());}; foo(x,y);",
        "foo",
        Sets.newHashSet("a", "b"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments12
  public void testMaybeAddTempsForCallArguments12() {
    
    
    testNeededTemps(
        "function foo(a,b){for(;;){a;b;goo();}}; foo(x,y);",
        "foo",
        Sets.newHashSet("a", "b"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments13
  public void testMaybeAddTempsForCallArguments13() {
    
    
    testNeededTemps(
        "function foo(a,b){for(;;){for(;;){a;b;}goo();}}; foo(x,y);",
        "foo",
        Sets.newHashSet("a", "b"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments20
  public void testMaybeAddTempsForCallArguments20() {
    
    testNeededTemps(
        "function foo(a){a;a;}; foo(\"blah blah\");",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments21
  public void testMaybeAddTempsForCallArguments21() {
    
    testNeededTemps(
        "function foo(a){a;a;}; foo(\"\");",
        "foo",
        EMPTY_STRING_SET);
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments22
  public void testMaybeAddTempsForCallArguments22() {
    
    testNeededTemps(
        "function foo(a){}; foo({x:1});",
        "foo",
        EMPTY_STRING_SET);
    
    testNeededTemps(
        "function foo(a){a;}; foo({x:1});",
        "foo",
        Sets.newHashSet("a"));
    
    testNeededTemps(
        "function foo(a){a;a;}; foo({x:1});",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments23
  public void testMaybeAddTempsForCallArguments23() {
    
    testNeededTemps(
        "function foo(a){}; foo([1,2]);",
        "foo",
        EMPTY_STRING_SET);
    
    testNeededTemps(
        "function foo(a){a;}; foo([1,2]);",
        "foo",
        Sets.newHashSet("a"));
    
    testNeededTemps(
        "function foo(a){a;a;}; foo([1,2]);",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments24
  public void testMaybeAddTempsForCallArguments24() {
    
    testNeededTemps(
        "function foo(a){}; foo(/mac/);",
        "foo",
        EMPTY_STRING_SET);
    
    testNeededTemps(
        "function foo(a){a;}; foo(/mac/);",
        "foo",
        Sets.newHashSet("a"));
    
    testNeededTemps(
        "function foo(a){a;a;}; foo(/mac/);",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments25
  public void testMaybeAddTempsForCallArguments25() {
    
    testNeededTemps(
        "function foo(a){}; foo(new Date());",
        "foo",
        EMPTY_STRING_SET);
    
    testNeededTemps(
        "function foo(a){a;}; foo(new Date());",
        "foo",
        Sets.newHashSet("a"));
    
    
    testNeededTemps(
        "function foo(a){a;a;}; foo(new Date());",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments26
  public void testMaybeAddTempsForCallArguments26() {
    
    testNeededTemps(
        "function foo(a){}; foo(new Bar());",
        "foo",
        Sets.newHashSet("a"));
    
    testNeededTemps(
        "function foo(a){a;}; foo(new Bar());",
        "foo",
        Sets.newHashSet("a"));
    
    testNeededTemps(
        "function foo(a){a;a;}; foo(new Bar());",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArguments27
  public void testMaybeAddTempsForCallArguments27() {
    
    
    testNeededTemps(
        "function foo(a,b,c){}; foo.call(this,1,goo(),2);",
        "foo",
        Sets.newHashSet("b"));
  }

// com.google.javascript.jscomp.FunctionArgumentInjectorTest::testMaybeAddTempsForCallArgumentsInLoops
  public void testMaybeAddTempsForCallArgumentsInLoops() {
    
    
    testNeededTemps(
        "function foo(a){for(;;)a;}; foo(new Bar());",
        "foo",
        Sets.newHashSet("a"));

    testNeededTemps(
        "function foo(a){while(true)a;}; foo(new Bar());",
        "foo",
        Sets.newHashSet("a"));

    testNeededTemps(
        "function foo(a){do{a;}while(true)}; foo(new Bar());",
        "foo",
        Sets.newHashSet("a"));
  }

// com.google.javascript.jscomp.FunctionCheckTest::testFunctions
  public void testFunctions() {
    
    testSame("function foo() {} foo();");
    testSame("function foo(a) {} foo('a');");
    testSame("function foo(a,b) {} foo(10, 20);");

    
    testSame("function foo(a,b,opt_c) {} foo(1,2);");
    testSame("function foo(a,b,opt_c) {} foo(1,2,3);");
    testSame("function foo(a,opt_b,opt_c) {} foo(1);");

    
    testSame("function foo(var_args) {} foo();");
    testSame("function foo(var_args) {} foo(1,2);");
    testSame("function foo(a,b,var_args) {} foo(1,2);");
    testSame("function foo(a,b,var_args) {} foo(1,2,3);");
    testSame("function foo(a,b,var_args) {} foo(1,2,3,4,5);");
    testSame("function foo(a,opt_b,var_args) {} foo(1);");
    testSame("function foo(a,opt_b,var_args) {} foo(1,2);");
    testSame("function foo(a,opt_b,var_args) {} foo(1,2,3);");
    testSame("function foo(a,opt_b,var_args) {} foo(1,2,3,4,5);");
    
    
    testSame("function f(var_args) { function g(var_args) {} g(1,2,3); } f();");

    
    test("function foo(a,b,opt_c) {} foo(1);", null,
         FunctionCheck.WRONG_ARGUMENT_COUNT_ERROR);
    test("function foo(a,b,opt_c) {} foo(1,2,3,4);", null,
         FunctionCheck.WRONG_ARGUMENT_COUNT_ERROR);
    test("function foo(a,b) {} foo(1, 2, 3);", null,
         FunctionCheck.WRONG_ARGUMENT_COUNT_ERROR);
    test("function foo() {} foo(1, 2, 3);", null,
         FunctionCheck.WRONG_ARGUMENT_COUNT_ERROR);
    test("function foo(a,b,c,d) {} foo(1, 2, 3);", null,
         FunctionCheck.WRONG_ARGUMENT_COUNT_ERROR);
    test("function foo(a,b,var_args) {} foo(1);", null,
         FunctionCheck.WRONG_ARGUMENT_COUNT_ERROR);
    test("function foo(a,b,opt_c,var_args) {} foo(1);", null,
         FunctionCheck.WRONG_ARGUMENT_COUNT_ERROR);
    test("function foo(a,b,var_args,c) {} foo(1,2,3,4);", null,
         FunctionCheck.VAR_ARGS_ERROR);
  }

// com.google.javascript.jscomp.FunctionCheckTest::testFunctionsWithJsDoc1
  public void testFunctionsWithJsDoc1() {
    testSame(" function foo(a,b,c) {} foo(1,2);");
  }

// com.google.javascript.jscomp.FunctionCheckTest::testFunctionsWithJsDoc2
  public void testFunctionsWithJsDoc2() {
    testSame(" function foo(a,b,c) {} foo(1,2,3);");
  }

// com.google.javascript.jscomp.FunctionCheckTest::testFunctionsWithJsDoc3
  public void testFunctionsWithJsDoc3() {
    testSame(" " +
             "function foo(a,b,c) {} foo(1);");
  }

// com.google.javascript.jscomp.FunctionCheckTest::testFunctionsWithJsDoc4
  public void testFunctionsWithJsDoc4() {
    testSame(" var foo = function(a) {}; foo();");
  }

// com.google.javascript.jscomp.FunctionCheckTest::testFunctionsWithJsDoc5
  public void testFunctionsWithJsDoc5() {
    testSame(" var foo = function(a) {}; foo(1,2);");
  }

// com.google.javascript.jscomp.FunctionCheckTest::testFunctionsWithJsDoc6
  public void testFunctionsWithJsDoc6() {
    test(" var foo = function(a, b) {}; foo();",
         null, FunctionCheck.WRONG_ARGUMENT_COUNT_ERROR);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction1
  public void testIsSimpleFunction1() {
    assertTrue(getInjector().isDirectCallNodeReplacementPossible(
        prep("function(){}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction2
  public void testIsSimpleFunction2() {
    assertTrue(getInjector().isDirectCallNodeReplacementPossible(
        prep("function(){return 0;}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction3
  public void testIsSimpleFunction3() {
    assertTrue(getInjector().isDirectCallNodeReplacementPossible(
        prep("function(){return x ? 0 : 1}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction4
  public void testIsSimpleFunction4() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function(){return;}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction5
  public void testIsSimpleFunction5() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function(){return 0; return 0;}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction6
  public void testIsSimpleFunction6() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function(){var x=true;return x ? 0 : 1}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction7
  public void testIsSimpleFunction7() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function(){if (x) return 0; else return 1}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction1
  public void testCanInlineReferenceToFunction1() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){}; foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction2
  public void testCanInlineReferenceToFunction2() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){}; foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction3
  public void testCanInlineReferenceToFunction3() {
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return;}; foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction4
  public void testCanInlineReferenceToFunction4() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return;}; foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction5
  public void testCanInlineReferenceToFunction5() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction6
  public void testCanInlineReferenceToFunction6() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction7
  public void testCanInlineReferenceToFunction7() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x=foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction8
  public void testCanInlineReferenceToFunction8() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x=foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction9
  public void testCanInlineReferenceToFunction9() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x; x=foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction10
  public void testCanInlineReferenceToFunction10() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x; x=foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction11
  public void testCanInlineReferenceToFunction11() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x; x=x+foo();", "foo",
        INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction12
  public void testCanInlineReferenceToFunction12() {
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return true;}; var x; x=x+foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction12b
  public void testCanInlineReferenceToFunction12b() {
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(){return true;}; var x; x=x+foo();",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction14
  public void testCanInlineReferenceToFunction14() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; foo(x);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction15
  public void testCanInlineReferenceToFunction15() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; foo(x);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction16
  public void testCanInlineReferenceToFunction16() {
    
    
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){var b;return a;}; foo(goo());", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction17
  public void testCanInlineReferenceToFunction17() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a;}; " +
        "function x() { foo(goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction18
  public void testCanInlineReferenceToFunction18() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a;} foo(x++);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction19
  public void testCanInlineReferenceToFunction19() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo([]);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction20
  public void testCanInlineReferenceToFunction20() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo({});", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction21
  public void testCanInlineReferenceToFunction21() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo(new Date);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction22
  public void testCanInlineReferenceToFunction22() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo(true && new Date);", "foo",
        INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction23
  public void testCanInlineReferenceToFunction23() {
    
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return a;}; foo(x++);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction24
  public void testCanInlineReferenceToFunction24() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a;}; " +
        "function x() { foo(x++); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction25
  public void testCanInlineReferenceToFunction25() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a;}; foo(x++);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction26
  public void testCanInlineReferenceToFunction26() {
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return a+a;}; foo(x++);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction27
  public void testCanInlineReferenceToFunction27() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a+a;}; " +
        "function x() { foo(x++); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction28
  public void testCanInlineReferenceToFunction28() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; foo(goo());", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction29
  public void testCanInlineReferenceToFunction29() {
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return true;}; foo(goo());", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction30
  public void testCanInlineReferenceToFunction30() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo(goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction31
  public void testCanInlineReferenceToFunction31() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a) {return true;}; " +
        "function x() {foo.call(this, 1);}",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction32
  public void testCanInlineReferenceToFunction32() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.apply(this, [1]); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction33
  public void testCanInlineReferenceToFunction33() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.bar(this, 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction34
  public void testCanInlineReferenceToFunction34() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.call(this, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction35
  public void testCanInlineReferenceToFunction35() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.apply(this, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction36
  public void testCanInlineReferenceToFunction36() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.bar(this, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction37
  public void testCanInlineReferenceToFunction37() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(null, 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction38
  public void testCanInlineReferenceToFunction38() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(null, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction39
  public void testCanInlineReferenceToFunction39() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(bar, 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction40
  public void testCanInlineReferenceToFunction40() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(bar, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction41
  public void testCanInlineReferenceToFunction41() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(new bar(), 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction42
  public void testCanInlineReferenceToFunction42() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(new bar(), goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction43
  public void testCanInlineReferenceToFunction43() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return true;}; " +
        "function x() { foo.call(); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction44
  public void testCanInlineReferenceToFunction44() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return true;}; " +
        "function x() { foo.call(); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression1
  public void testCanInlineReferenceToFunctionInExpression1() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { if (foo(1)) throw 'test'; }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression2
  public void testCanInlineReferenceToFunctionInExpression2() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { return foo(1); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression3
  public void testCanInlineReferenceToFunctionInExpression3() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { switch(foo(1)) { default:break; } }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression4
  public void testCanInlineReferenceToFunctionInExpression4() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {foo(1)?0:1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression5
  public void testCanInlineReferenceToFunctionInExpression5() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() {true?foo(1):1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression5a
 public void testCanInlineReferenceToFunctionInExpression5a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(a){return true;}; " +
        "function x() {true?foo(1):1 }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression6
  public void testCanInlineReferenceToFunctionInExpression6() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {foo(1) && 1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression7
  public void testCanInlineReferenceToFunctionInExpression7() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() {1 && foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression7a
  public void testCanInlineReferenceToFunctionInExpression7a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(a){return true;}; " +
        "function x() {1 && foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression8
  public void testCanInlineReferenceToFunctionInExpression8() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression9
  public void testCanInlineReferenceToFunctionInExpression9() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {var b = 1 + foo(1)}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression10
  public void testCanInlineReferenceToFunctionInExpression10() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() {var b; b += 1 + foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression10a
  public void testCanInlineReferenceToFunctionInExpression10a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(a){return true;}; " +
        "function x() {var b; b += 1 + foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression12
  public void testCanInlineReferenceToFunctionInExpression12() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {var a,b,c; a = b = c = foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression13
  public void testCanInlineReferenceToFunctionInExpression13() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {var a,b,c; a = b = c = 1 + foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression14
  public void testCanInlineReferenceToFunctionInExpression14() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "var a = {}, b = {}, c;" +
        "a.test = 'a';" +
        "b.test = 'b';" +
        "c = a;" +
        "function foo(){c = b; return 'foo'};" +
        "c.test=foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression14a
  public void testCanInlineReferenceToFunctionInExpression14a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "var a = {}, b = {}, c;" +
        "a.test = 'a';" +
        "b.test = 'b';" +
        "c = a;" +
        "function foo(){c = b; return 'foo'};" +
        "c.test=foo();",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression18
  public void testCanInlineReferenceToFunctionInExpression18() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return _g();}; " +
        "function x() {1 + foo()() }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression19
  public void testCanInlineReferenceToFunctionInExpression19() {
    
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return a;}; " +
        "function x() {1 + _g(foo()) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression19a
  public void testCanInlineReferenceToFunctionInExpression19a() {
    
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(){return a;}; " +
        "function x() {1 + _g(foo()) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression21
  public void testCanInlineReferenceToFunctionInExpression21() {
    
    
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression21a
  public void testCanInlineReferenceToFunctionInExpression21a() {
    
    
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression22
  public void testCanInlineReferenceToFunctionInExpression22() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo()) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression22a
  public void testCanInlineReferenceToFunctionInExpression22a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo()) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression23
  public void testCanInlineReferenceToFunctionInExpression23() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo.call(this)) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression23a
  public void testCanInlineReferenceToFunctionInExpression23a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo.call(this)) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline1
  public void testInline1() {
    helperInlineReferenceToFunction(
        "function foo(){}; foo();",
        "function foo(){}; void 0",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline2
  public void testInline2() {
    helperInlineReferenceToFunction(
        "function foo(){}; foo();",
        "function foo(){}; {}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline3
  public void testInline3() {
    helperInlineReferenceToFunction(
        "function foo(){return;}; foo();",
        "function foo(){return;}; {}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline4
  public void testInline4() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; foo();",
        "function foo(){return true;}; true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline5
  public void testInline5() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; foo();",
        "function foo(){return true;}; {true;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline6
  public void testInline6() {
    
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x=foo();",
        "function foo(){return true;}; var x=true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline7
  public void testInline7() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x=foo();",
        "function foo(){return true;}; var x;" +
            "{x=true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline8
  public void testInline8() {
    
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x; x=foo();",
        "function foo(){return true;}; var x; x=true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline9
  public void testInline9() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x; x=foo();",
        "function foo(){return true;}; var x;{x=true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline10
  public void testInline10() {
    
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x; x=x+foo();",
        "function foo(){return true;}; var x; x=x+true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline11
  public void testInline11() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; foo(x);",
        "function foo(a){return true;}; true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline12
  public void testInline12() {
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; foo(x);",
        "function foo(a){return true;}; {true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline13
  public void testInline13() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a;}; " +
        "function x() { foo(x++); }",
        "function foo(a){return a;}; " +
        "function x() {{var a$$inline_1=x++;" +
            "a$$inline_1}}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline14
  public void testInline14() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a+a;}; foo(x++);",
        "function foo(a){return a+a;}; " +
            "{var a$$inline_1=x++;" +
            " a$$inline_1+" +
            "a$$inline_1;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline15
  public void testInline15() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a+a;}; foo(new Date());",
        "function foo(a){return a+a;}; " +
            "{var a$$inline_1=new Date();" +
            " a$$inline_1+" +
            "a$$inline_1;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline16
  public void testInline16() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a+a;}; foo(function(){});",
        "function foo(a){return a+a;}; " +
            "{var a$$inline_1=function(){};" +
            " a$$inline_1+" +
            "a$$inline_1;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline17
  public void testInline17() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; foo(goo());",
        "function foo(a){return true;};" +
            "{var a$$inline_1=goo();true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline18
  public void testInline18() {
    
    helperInlineReferenceToFunction(
        "function foo(a){var b;return a;}; " +
            "function x() { foo(goo()); }",
            "function foo(a){var b;return a;}; " +
            "function x() {{var a$$inline_2=goo();" +
                "var b$$inline_3;a$$inline_2}}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline19
  public void testInline19() {
    
    helperInlineReferenceToFunction(
        "var x = 1; var y = 2;" +
        "function foo(a,b){x = b; y = a;}; " +
        "function bar() { foo(x,y); }",
        "var x = 1; var y = 2;" +
        "function foo(a,b){x = b; y = a;}; " +
        "function bar() {" +
           "{var a$$inline_2=x;" +
            "x = y;" +
            "y = a$$inline_2;}" +
        "}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline19b
  public void testInline19b() {
    helperInlineReferenceToFunction(
        "var x = 1; var y = 2;" +
        "function foo(a,b){y = a; x = b;}; " +
        "function bar() { foo(x,y); }",
        "var x = 1; var y = 2;" +
        "function foo(a,b){y = a; x = b;}; " +
        "function bar() {" +
           "{var b$$inline_3=y;" +
            "y = x;" +
            "x = b$$inline_3;}" +
        "}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineIntoLoop
  public void testInlineIntoLoop() {
    helperInlineReferenceToFunction(
        "function foo(a){var b;return a;}; " +
        "for(;1;){ foo(1); }",
        "function foo(a){var b;return a;}; " +
        "for(;1;){ {" +
            "var b$$inline_3=void 0;1}}",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a){var b;return a;}; " +
        "do{ foo(1); } while(1)",
        "function foo(a){var b;return a;}; " +
        "do{ {" +
            "var b$$inline_3=void 0;1}}while(1)",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a){for(var b in c)return a;}; " +
        "for(;1;){ foo(1); }",
        "function foo(a){for(var b in c)return a;}; " +
        "for(;1;){ {JSCompiler_inline_label_foo_4:{" +
            "for(var b$$inline_3 in c){" +
              "1;break JSCompiler_inline_label_foo_4" +
            "}}}}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression1
  public void testInlineReferenceInExpression1() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { if (foo(1)) throw 'test'; }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result_0; " +
        "JSCompiler_inline_result_0=true;}" +
        "if (JSCompiler_inline_result_0) throw 'test'; }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression2
  public void testInlineReferenceInExpression2() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { return foo(1); }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result_0; " +
        "JSCompiler_inline_result_0=true;}" +
        "return JSCompiler_inline_result_0; }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression3
  public void testInlineReferenceInExpression3() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { switch(foo(1)) { default:break; } }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result_0; " +
        "JSCompiler_inline_result_0=true;}" +
        "switch(JSCompiler_inline_result_0) { default:break; } }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression4
  public void testInlineReferenceInExpression4() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1)?0:1 }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result_0; " +
        "JSCompiler_inline_result_0=true;}" +
        "JSCompiler_inline_result_0?0:1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression5
  public void testInlineReferenceInExpression5() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1)&&1 }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result_0; " +
        "JSCompiler_inline_result_0=true;}" +
        "JSCompiler_inline_result_0&&1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression6
  public void testInlineReferenceInExpression6() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result_0; " +
        "JSCompiler_inline_result_0=true;}" +
        "1 + JSCompiler_inline_result_0 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression7
  public void testInlineReferenceInExpression7() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1) && 1 }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result_0; " +
        "JSCompiler_inline_result_0=true;}" +
        "JSCompiler_inline_result_0&&1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression8
  public void testInlineReferenceInExpression8() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result_0; " +
        "JSCompiler_inline_result_0=true;}" +
        "1 + JSCompiler_inline_result_0 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression9
  public void testInlineReferenceInExpression9() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {var b = 1 + foo(1)}",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result_0; " +
        "JSCompiler_inline_result_0=true;}" +
        "var b = 1 + JSCompiler_inline_result_0 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression11
  public void testInlineReferenceInExpression11() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {a:foo(1)?0:1 }",
        "function foo(a){return true;}; " +
        "function x() { a:{{var JSCompiler_inline_result_0; " +
        "JSCompiler_inline_result_0=true;}" +
        "JSCompiler_inline_result_0?0:1 }}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression12
  public void testInlineReferenceInExpression12() {
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {1?foo(1):1 }",
        "function foo(a){return true;}; " +
        "function x() { if(1) { {true;} } else { 1 }}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression13
  public void testInlineReferenceInExpression13() {
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { goo() + (1?foo(1):1) }",
        "function foo(a){return true;}; " +
        "function x() { var JSCompiler_temp_const_0=goo();" +
            "var JSCompiler_temp_1;" +
            "if(1) {" +
            "  {JSCompiler_temp_1=true;} " +
            "} else {" +
            "  JSCompiler_temp_1=1;" +
            "}" +
            "JSCompiler_temp_const_0 + JSCompiler_temp_1" +
            "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression14
  public void testInlineReferenceInExpression14() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo(1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const_0=z;" +
            "{" +
             "var JSCompiler_inline_result_1;" +
             "z= {};" +
             "JSCompiler_inline_result_1 = true;" +
            "}" +
            "JSCompiler_temp_const_0.gack = JSCompiler_inline_result_1;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression15
  public void testInlineReferenceInExpression15() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo.call(this,1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const_0=z;" +
            "{" +
             "var JSCompiler_inline_result_1;" +
             "z= {};" +
             "JSCompiler_inline_result_1 = true;" +
            "}" +
            "JSCompiler_temp_const_0.gack = JSCompiler_inline_result_1;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression16
  public void testInlineReferenceInExpression16() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z[bar()] = foo(1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const_1=z;" +
            "var JSCompiler_temp_const_0=bar();" +
            "{" +
             "var JSCompiler_inline_result_2;" +
             "z= {};" +
             "JSCompiler_inline_result_2 = true;" +
            "}" +
            "JSCompiler_temp_const_1[JSCompiler_temp_const_0] = " +
                "JSCompiler_inline_result_2;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression17
  public void testInlineReferenceInExpression17() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.y.x.gack = foo(1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const_0=z.y.x;" +
            "{" +
             "var JSCompiler_inline_result_1;" +
             "z= {};" +
             "JSCompiler_inline_result_1 = true;" +
            "}" +
            "JSCompiler_temp_const_0.gack = JSCompiler_inline_result_1;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineWithinCalls1
  public void testInlineWithinCalls1() {
    
    helperInlineReferenceToFunction(
        "function foo(){return _g;}; " +
        "function x() {1 + foo()() }",
        "function foo(){return _g;}; " +
        "function x() { {var JSCompiler_inline_result_0; " +
        "JSCompiler_inline_result_0=_g;}" +
        "1 + JSCompiler_inline_result_0() }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineAssignmentToConstant
  public void testInlineAssignmentToConstant() {
    
    helperInlineReferenceToFunction(
        "function foo(){return _g;}; " +
        "function x(){var CONSTANT_RESULT = foo(); }",

        "function foo(){return _g;}; " +
        "function x() {" +
        "  {var JSCompiler_inline_result_0; JSCompiler_inline_result_0=_g;}" +
        "  var CONSTANT_RESULT = JSCompiler_inline_result_0;" +
        "}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testBug1897706
  public void testBug1897706() {
    helperInlineReferenceToFunction(
        "function foo(a){}; foo(x())",
        "function foo(a){}; {var a$$inline_1=x()}",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a){bar()}; foo(x())",
        "function foo(a){bar()}; {var a$$inline_1=x();bar()}",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a,b){bar()}; foo(x(),y())",
        "function foo(a,b){bar()};" +
        "{var a$$inline_2=x();var b$$inline_3=y();bar()}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionNamesTest::testFunctionsNamesAndIds
  public void testFunctionsNamesAndIds() {
    final String jsSource =
        "goog.widget = function(str) {\n" +
        "  this.member_fn = function() {};\n" +
        "  local_fn = function() {};\n" +
        "  (function(a){})(1);\n" +
        "}\n" +
        "function foo() {\n" +
        "  function bar() {}\n" +
        "}\n" +
        "literal = {f1 : function(){}, f2 : function(){}};\n" +
        "goog.array.map(arr, function named(){});\n" +
        "goog.array.map(arr, function(){});\n" +
        "named_twice = function quax(){};\n" +
        "recliteral = {l1 : {l2 : function(){}}};\n" +
        "namedliteral = {n1 : function litnamed(){}};\n" +
        "namedrecliteral = {n1 : {n2 : function reclitnamed(){}}};\n" +
        "numliteral = {1 : function(){}};\n" +
        "recnumliteral = {1 : {a : function(){}}};\n";

    testSame(jsSource);

    final Map<Integer, String> idNameMap = Maps.newLinkedHashMap();
    int count = 0;
    for (Node f : functionNames.getFunctionNodeList()) {
      int id = functionNames.getFunctionId(f);
      String name = functionNames.getFunctionName(f);
      idNameMap.put(id, name);
      count++;
    }

    assertEquals("Unexpected number of functions", 16, count);

    final Map<Integer, String> expectedMap = Maps.newLinkedHashMap();

    expectedMap.put(0, "goog.widget.member_fn");
    expectedMap.put(1, "goog.widget::local_fn");
    expectedMap.put(2, "goog.widget::<anonymous>");
    expectedMap.put(3, "goog.widget");
    expectedMap.put(4, "foo::bar");
    expectedMap.put(5, "foo");
    expectedMap.put(6, "literal.f1");
    expectedMap.put(7, "literal.f2");
    expectedMap.put(8, "named");
    expectedMap.put(9, "<anonymous>");
    expectedMap.put(10, "quax");
    expectedMap.put(11, "recliteral.l1.l2");
    expectedMap.put(12, "litnamed");
    expectedMap.put(13, "reclitnamed");
    expectedMap.put(14, "<anonymous>");
    expectedMap.put(15, "<anonymous>");
    assertEquals("Function id/name mismatch",
                 expectedMap, idNameMap);
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

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateNoReturnWithoutResultAssignment
  public void testMutateNoReturnWithoutResultAssignment() {
    helperMutate(
        "function foo(){}; foo();",
        "{}",
        "foo");
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateNoReturnWithResultAssignment
  public void testMutateNoReturnWithResultAssignment() {
    helperMutate(
        "function foo(){}; var result = foo();",
        "{result = void 0}",
        "foo", true, false);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateNoValueReturnWithoutResultAssignment
  public void testMutateNoValueReturnWithoutResultAssignment() {
    helperMutate(
        "function foo(){return;}; foo();",
        "{}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateNoValueReturnWithResultAssignment
  public void testMutateNoValueReturnWithResultAssignment() {
    helperMutate(
        "function foo(){return;}; var result = foo();",
        "{result = void 0}",
        "foo");
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateValueReturnWithoutResultAssignment
  public void testMutateValueReturnWithoutResultAssignment() {
    helperMutate(
        "function foo(){return true;}; foo();",
        "{true;}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateValueReturnWithResultAssignment
  public void testMutateValueReturnWithResultAssignment() {
    helperMutate(
        "function foo(){return true;}; var x=foo();",
        "{x=true}",
        "foo", "x", true, false);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateWithMultipleReturns
  public void testMutateWithMultipleReturns() {
    helperMutate(
        "function foo(){ if (0) {return 0} else {return 1} };" +
          "var result=foo();",
        "{" +
          "JSCompiler_inline_label_foo_0:{" +
            "if(0) {" +
              "result=0; break JSCompiler_inline_label_foo_0" +
            "} else {" +
              "result=1; break JSCompiler_inline_label_foo_0" +
            "} result=void 0" +
          "}" +
        "}",
        "foo", true, false);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateWithParameters1
  public void testMutateWithParameters1() {
    
    helperMutate(
        "function foo(a){return true;}; foo(x);",
        "{true}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateWithParameters2
  public void testMutateWithParameters2() {
    
    helperMutate(
        "function foo(a){return x;}; foo(x);",
        "{x}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateWithParameters3
  public void testMutateWithParameters3() {
    
    helperMutate(
        "function foo(a){return a;}; " +
        "function x() { foo(x++); }",
        "{var a$$inline_1 = x++; a$$inline_1}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutate8
  public void testMutate8() {
    
    helperMutate(
        "function foo(a){return a+a;}; foo(x++);",
        "{var a$$inline_1 = x++;" +
            "a$$inline_1 + a$$inline_1;}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateInitializeUninitializedVars1
  public void testMutateInitializeUninitializedVars1() {
    helperMutate(
        "function foo(a){var b;return a;}; foo(1);",
        "{var b$$inline_3=void 0;1}",
        "foo", null, false, true);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateInitializeUninitializedVars2
  public void testMutateInitializeUninitializedVars2() {
    helperMutate(
        "function foo(a){for(var b in c)return a;}; foo(1);",
        "{JSCompiler_inline_label_foo_4:" +
          "{" +
            "for(var b$$inline_3 in c){" +
                "1;break JSCompiler_inline_label_foo_4" +
             "}" +
          "}" +
        "}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateCallInLoopVars1
  public void testMutateCallInLoopVars1() {
    
    boolean callInLoop = false;
    helperMutate(
        "function foo(a){var B = bar(); a;}; foo(1);",
        "{var B$$inline_3=bar(); 1;}",
        "foo", null, false, callInLoop);
    
    
    callInLoop = true;
    helperMutate(
        "function foo(a){var B = bar(); a;}; foo(1);",
        "{var B$$inline_3 = bar(); 1;}",
        "foo", null, false, callInLoop);
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
        + "found   : function (this:String, *): number\n"
        + "expected: function (this:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentNumParams
  public void testBuiltInTypeDifferentNumParams() throws Exception {
    testSame(
        "\n"
        + "function String() {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (this:String): string\n"
        + "expected: function (this:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentNumParams2
  public void testBuiltInTypeDifferentNumParams2() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str, opt_nothing) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (this:String, ?, ?): string\n"
        + "expected: function (this:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentParamType
  public void testBuiltInTypeDifferentParamType() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (this:String, ?): string\n"
        + "expected: function (this:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBadFunctionTypeDefinition
  public void testBadFunctionTypeDefinition() throws Exception {
    testSame(
        "function Function(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type Function\n"
        + "found   : function (this:Function, ?): ?\n"
        + "expected: function (this:Function, ...[*]): ?");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testExternSubTypes
  public void testExternSubTypes() throws Exception {
    testSame(ALL_NATIVE_EXTERN_TYPES, "", null);

    List<FunctionType> subtypes = ((InstanceObjectType) getLastCompiler()
        .getTypeRegistry().getType("Error")).getConstructor().getSubTypes();
    for (FunctionType type : subtypes) {
      String typeName = type.getInstanceType().toString();
      FunctionType typeInRegistry = ((InstanceObjectType) getLastCompiler()
          .getTypeRegistry().getType(typeName)).getConstructor();
      assertTrue(typeInRegistry == type);
    }
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound1
  public void testExportsFound1() {
    assertExported("var a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound2
  public void testExportsFound2() {
    assertExported("window['a']", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound3
  public void testExportsFound3() {
    assertExported("window.a", "a");
  }
