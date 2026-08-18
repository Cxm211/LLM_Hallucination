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
        "function foo(a){var b;for(b in c)return a;}; " +
        "for(;1;){ {JSCompiler_inline_label_foo_4:{" +
            "var b$$inline_3=void 0;for(b$$inline_3 in c){" +
              "1;break JSCompiler_inline_label_foo_4" +
            "}}}}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction1
  public void testInlineFunctionWithInnerFunction1() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {return true;}}; foo();",
        "function foo(){return function() {return true;}};" +
            "(function() {return true;})",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction2
  public void testInlineFunctionWithInnerFunction2() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {return true;}}; foo();",
        "function foo(){return function() {return true;}};" +
            "{(function() {return true;})}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction3
  public void testInlineFunctionWithInnerFunction3() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {var a; return true;}}; foo();",
        "function foo(){return function() {var a; return true;}};" +
            "(function() {var a; return true;});",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction4
  public void testInlineFunctionWithInnerFunction4() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {var a; return true;}}; foo();",
        "function foo(){return function() {var a; return true;}};" +
            "{(function() {var a$$inline_0; return true;});}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction5
  public void testInlineFunctionWithInnerFunction5() {
    
    helperInlineReferenceToFunction(
        "function foo(){function x() {var a; return true;} return x}; foo();",
        "function foo(){function x(){var a;return true}return x};" +
            "{var x$$inline_1 = function(){" +
            "var a$$inline_2;return true};x$$inline_1}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression1
  public void testInlineReferenceInExpression1() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { if (foo(1)) throw 'test'; }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "if (JSCompiler_inline_result$$0) throw 'test'; }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression2
  public void testInlineReferenceInExpression2() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { return foo(1); }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "return JSCompiler_inline_result$$0; }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression3
  public void testInlineReferenceInExpression3() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { switch(foo(1)) { default:break; } }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "switch(JSCompiler_inline_result$$0) { default:break; } }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression4
  public void testInlineReferenceInExpression4() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1)?0:1 }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0?0:1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression5
  public void testInlineReferenceInExpression5() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1)&&1 }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0&&1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression6
  public void testInlineReferenceInExpression6() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "1 + JSCompiler_inline_result$$0 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression7
  public void testInlineReferenceInExpression7() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1) && 1 }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0&&1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression8
  public void testInlineReferenceInExpression8() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "1 + JSCompiler_inline_result$$0 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression9
  public void testInlineReferenceInExpression9() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {var b = 1 + foo(1)}",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "var b = 1 + JSCompiler_inline_result$$0 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression11
  public void testInlineReferenceInExpression11() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {a:foo(1)?0:1 }",
        "function foo(a){return true;}; " +
        "function x() { a:{{var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0?0:1 }}",
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
        "function x() { var JSCompiler_temp_const$$0=goo();" +
            "var JSCompiler_temp$$1;" +
            "if(1) {" +
            "  {JSCompiler_temp$$1=true;} " +
            "} else {" +
            "  JSCompiler_temp$$1=1;" +
            "}" +
            "JSCompiler_temp_const$$0 + JSCompiler_temp$$1" +
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
            "var JSCompiler_temp_const$$0=z;" +
            "{" +
             "var JSCompiler_inline_result$$1;" +
             "z= {};" +
             "JSCompiler_inline_result$$1 = true;" +
            "}" +
            "JSCompiler_temp_const$$0.gack = JSCompiler_inline_result$$1;" +
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
            "var JSCompiler_temp_const$$0=z;" +
            "{" +
             "var JSCompiler_inline_result$$1;" +
             "z= {};" +
             "JSCompiler_inline_result$$1 = true;" +
            "}" +
            "JSCompiler_temp_const$$0.gack = JSCompiler_inline_result$$1;" +
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
            "var JSCompiler_temp_const$$1=z;" +
            "var JSCompiler_temp_const$$0=bar();" +
            "{" +
             "var JSCompiler_inline_result$$2;" +
             "z= {};" +
             "JSCompiler_inline_result$$2 = true;" +
            "}" +
            "JSCompiler_temp_const$$1[JSCompiler_temp_const$$0] = " +
                "JSCompiler_inline_result$$2;" +
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
            "var JSCompiler_temp_const$$0=z.y.x;" +
            "{" +
             "var JSCompiler_inline_result$$1;" +
             "z= {};" +
             "JSCompiler_inline_result$$1 = true;" +
            "}" +
            "JSCompiler_temp_const$$0.gack = JSCompiler_inline_result$$1;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineWithinCalls1
  public void testInlineWithinCalls1() {
    
    helperInlineReferenceToFunction(
        "function foo(){return _g;}; " +
        "function x() {1 + foo()() }",
        "function foo(){return _g;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=_g;}" +
        "1 + JSCompiler_inline_result$$0() }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineAssignmentToConstant
  public void testInlineAssignmentToConstant() {
    
    helperInlineReferenceToFunction(
        "function foo(){return _g;}; " +
        "function x(){var CONSTANT_RESULT = foo(); }",

        "function foo(){return _g;}; " +
        "function x() {" +
        "  {var JSCompiler_inline_result$$0; JSCompiler_inline_result$$0=_g;}" +
        "  var CONSTANT_RESULT = JSCompiler_inline_result$$0;" +
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

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateFunctionDefinition
  public void testMutateFunctionDefinition() {
     
     
     helperMutate(
        "function foo(a){function g(){}}; foo(1);",
        "{var g$$inline_3=function(){};}",
        "foo", null);
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
        + "found   : function (new:String, *): number\n"
        + "expected: function (new:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentNumParams
  public void testBuiltInTypeDifferentNumParams() throws Exception {
    testSame(
        "\n"
        + "function String() {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String): string\n"
        + "expected: function (new:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentNumParams2
  public void testBuiltInTypeDifferentNumParams2() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str, opt_nothing) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String, ?, ?): string\n"
        + "expected: function (new:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentParamType
  public void testBuiltInTypeDifferentParamType() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String, ?): string\n"
        + "expected: function (new:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBadFunctionTypeDefinition
  public void testBadFunctionTypeDefinition() throws Exception {
    testSame(
        "function Function(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type Function\n"
        + "found   : function (new:Function, ?): ?\n"
        + "expected: function (new:Function, ...[*]): ?");
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

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound4
  public void testExportsFound4() {
    assertExported("this['a']", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound5
  public void testExportsFound5() {
    assertExported("this.a", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound6
  public void testExportsFound6() {
    assertExported("function f() { this['a'] }");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound7
  public void testExportsFound7() {
    assertExported("function f() { this.a }");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound8
  public void testExportsFound8() {
    assertExported("window['foo']", "foo");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound9
  public void testExportsFound9() {
    assertExported("window['a'] = 1;", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound10
  public void testExportsFound10() {
    assertExported("window['a']['b']['c'] = 1;", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound11
  public void testExportsFound11() {
    assertExported("if (window['a'] = 1) alert(x);", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound12
  public void testExportsFound12() {
    assertExported("function foo() { window['a'] = 1; }", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound13
  public void testExportsFound13() {
    assertExported("function foo() {var window; window['a'] = 1; }");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound14
  public void testExportsFound14() {
    assertExported("var a={window:{}}; a.window['b']");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound15
  public void testExportsFound15() {
    assertExported("window.window['b']", "window");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound16
  public void testExportsFound16() {
    
    
    assertExported("var a = window; a['b']");
  }

// com.google.javascript.jscomp.GlobalVarReferenceMapTest::testUpdateGlobalVarReferences_ResetReferences
  public void testUpdateGlobalVarReferences_ResetReferences() {
    
    for (int i = 0; i < 2; i++) {
      assertEquals(var1Refs.references,
          map.getReferences(globalScope.getVar(VAR1)).references);
      assertEquals(var2Refs.references,
          map.getReferences(globalScope.getVar(VAR2)).references);
      assertEquals(var3Refs.references,
          map.getReferences(globalScope.getVar(VAR3)).references);
      map.updateGlobalVarReferences(globalMap, root);
    }
  }

// com.google.javascript.jscomp.GlobalVarReferenceMapTest::testUpdateGlobalVarReferences_UpdateScriptNoRef
  public void testUpdateGlobalVarReferences_UpdateScriptNoRef() {
    Map<Var, ReferenceCollection> scriptMap = Maps.newHashMap();
    map.updateGlobalVarReferences(scriptMap, scriptRoot);
    ReferenceCollection refs = map.getReferences(globalScope.getVar(VAR2));
    assertEquals(var2Refs.references, refs.references);
    refs = map.getReferences(globalScope.getVar(VAR1));
    assertEquals(2, refs.references.size());
    assertEquals(var1Refs.references.get(0), refs.references.get(0));
    assertEquals(var1Refs.references.get(2), refs.references.get(1));
    refs = map.getReferences(globalScope.getVar(VAR3));
    assertEquals(1, refs.references.size());
    assertEquals(var3Refs.references.get(0), refs.references.get(0));
  }

// com.google.javascript.jscomp.GlobalVarReferenceMapTest::testUpdateGlobalVarReferences_UpdateScriptNewRefs
  public void testUpdateGlobalVarReferences_UpdateScriptNewRefs() {
    Map<Var, ReferenceCollection> scriptMap = Maps.newHashMap();

    ReferenceCollection newVar1Refs = new ReferenceCollection();
    Reference newVar1In2Ref = createRefForTest(INPUT2);
    newVar1Refs.references = Lists.newArrayList(newVar1In2Ref);

    ReferenceCollection newVar2Refs = new ReferenceCollection();
    Reference newVar2In2Ref = createRefForTest(INPUT2);
    newVar2Refs.references = Lists.newArrayList(newVar2In2Ref);

    ReferenceCollection newVar3Refs = new ReferenceCollection();
    Reference newVar3In2Ref = createRefForTest(INPUT2);
    newVar3Refs.references = Lists.newArrayList(newVar3In2Ref);

    scriptMap.put(globalScope.getVar(VAR1), newVar1Refs);
    scriptMap.put(globalScope.getVar(VAR2), newVar2Refs);
    scriptMap.put(globalScope.getVar(VAR3), newVar3Refs);
    map.updateGlobalVarReferences(scriptMap, scriptRoot);
    ReferenceCollection refs = map.getReferences(globalScope.getVar(VAR1));
    assertEquals(3, refs.references.size());
    assertEquals(var1Refs.references.get(0), refs.references.get(0));
    assertEquals(newVar1In2Ref, refs.references.get(1));
    assertEquals(var1Refs.references.get(2), refs.references.get(2));
    refs = map.getReferences(globalScope.getVar(VAR2));
    assertEquals(3, refs.references.size());
    assertEquals(var2Refs.references.get(0), refs.references.get(0));
    assertEquals(newVar2In2Ref, refs.references.get(1));
    assertEquals(var2Refs.references.get(1), refs.references.get(2));
    refs = map.getReferences(globalScope.getVar(VAR3));
    assertEquals(2, refs.references.size());
    assertEquals(var3Refs.references.get(0), refs.references.get(0));
    assertEquals(newVar3In2Ref, refs.references.get(1));
  }

// com.google.javascript.jscomp.GlobalVarReferenceMapTest::testUpdateGlobalVarReferences_UpdateScriptNewVar
  public void testUpdateGlobalVarReferences_UpdateScriptNewVar() {
    Map<Var, ReferenceCollection> scriptMap = Maps.newHashMap();
    final String var4 = "var4";
    globalScope.declare(var4, new Node(Token.NAME), null, INPUT2);
    ReferenceCollection newVar3Refs = new ReferenceCollection();
    Reference newVar3In2Ref = createRefForTest(INPUT2);
    newVar3Refs.references = Lists.newArrayList(newVar3In2Ref);
    scriptMap.put(globalScope.getVar(var4), newVar3Refs);
    map.updateGlobalVarReferences(scriptMap, scriptRoot);
    ReferenceCollection refs = map.getReferences(globalScope.getVar(var4));
    assertEquals(1, refs.references.size());
    assertEquals(newVar3In2Ref, refs.references.get(0));
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingUninitializedVarsInScope
  public void testGroupingUninitializedVarsInScope() {
    
    test("var a = 1; f1(); var b;", "var a = 1, b; f1();");
    
    test("var a = \"mangoes\"; f1(); alert(a); var b;",
         "var a = \"mangoes\", b; f1(); alert(a);");
    
    
    test("var a = 1; {var c; alert(c);} var b;",
         "var a = 1, c, b; {alert(c);}");
    
    test("var a = 1; var b = 1; f1(); f2(); var c; var d;",
         "var a = 1, b, c, d; b = 1; f1(); f2();");
    test("var a = 1; var b = 2; var c; f1(); f2(); var d, e;",
         "var a = 1, b, c, d, e; b = 2; f1(); f2();");
    test("var a = 1, b = 2, c; f1(); f2(); var d; var e; " +
         "f3(); f4(); var f = 10; var g; var h = a + b;",
         "var a = 1, b = 2, c, d, e, f, g, h; f1(); f2(); f3(); f4(); " +
         "f = 10; h = a + b;");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingInitializedVarsInScope
  public void testGroupingInitializedVarsInScope() {
    
    test("var a = 1; f1(); var b = 2;", "var a = 1, b; f1(); b = 2;");
    
    test("var a = \"mangoes\"; f1(); alert(a); var b = 2;",
         "var a = \"mangoes\", b; f1(); alert(a); b = 2;");
    
    
    test("var a = 1; {var c = 34; alert(c);} var b = 2;",
         "var a = 1, c, b; {c = 34; alert(c);} b = 2;");
    
    test("var a = 1; var b = 1; f1(); f2(); var c = 3; var d = 4;",
         "var a = 1, b, c, d; b = 1; f1(); f2(); c = 3; d = 4;");
    test("var a = 1; var b = 2; var c; f1(); f2(); var d = 4, e;",
         "var a = 1, b, c, d, e; b = 2; f1(); f2(); d = 4;");
    test("var a = 1, b = 2, c; f1(); f2(); var d; var e = 6; " +
         "f3(); f4(); var f; var g; var h = a + b;",
         "var a = 1, b = 2, c, d, e, f, g, h; f1(); f2(); e = 6; " +
         "f3(); f4(); h = a + b;");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingVarsInForAndForInLoops
  public void testGroupingVarsInForAndForInLoops() {
    
    test("var a = 1; for (var x = 0; x < 10; ++x) {a++;} var y;",
         "var a = 1, x, y; for (x = 0; x < 10; ++x) {a++;}");
    test("var a = 1, x; for (x = 0; x < 10; ++x) {a++;} var y;",
         "var a = 1, x, y; for (x = 0; x < 10; ++x) {a++;}");
    test("var a = 1, x; for (x; x < 10; ++x) {a++;} var y;",
         "var a = 1, x, y; for (x; x < 10; ++x) {a++;}");
    test("var a = 1; for (; a < 10; ++a) {alert(a);} var y;",
         "var a = 1, y; for (; a < 10; ++a) {alert(a);}");
    test("var a = 1; for (var x; x < 10; ++x) {a += 2;} var y = 5;",
         "var a = 1, x, y; for (; x < 10; ++x) {a += 2;} y = 5;");
    
    test("var a = 1; " +
         "for (var a1 = 0, a2 = 10; a1 < 10 && a2 > 0; ++a1, --a2) {}" +
         "var x = 5;",
         "var a = 1, x;" +
         "for (var a1 = 0, a2 = 10; a1 < 10 && a2 > 0; ++a1, --a2) {} " +
         "x = 5;");
    test("var a = 1; " +
         "for (var a1 = 0, a2; a1 < 10 && a2 > 0; ++a1, --a2) {}" +
         "var x = 5;",
         "var a = 1, a1, a2, x;" +
         "for (a1 = 0; a1 < 10 && a2 > 0; ++a1, --a2) {}" +
         "x = 5;");
    test("var a = 1; " +
         "for (var a1, a2; a1 < 10 && a2 > 0; ++a1, --a2) {}" +
         "var x = 5;",
         "var a = 1, a1, a2, x;" +
         "for (; a1 < 10 && a2 > 0; ++a1, --a2) {}" +
         "x = 5;");

    
    test("var a = [1, 2, 3, 4]; for (var z in a) {alert(z);} var y;",
         "var a = [1, 2, 3, 4], z, y; for (z in a) {alert(z);}");
    test("var a = [1, 2, 3, 4]; for (var z in a) {alert(z);} var y = 5;",
         "var a = [1, 2, 3, 4], z, y; for (z in a) {alert(z);} y = 5;");
    test("var a; for (var z in a = [1, 2, 3, 4]) {alert(z);} var y, x = 5;",
         "var a, z, y, x; for (z in a = [1, 2, 3, 4]) {alert(z);} x = 5;");
    test("var a; for (var z = 1 in a = [1, 2, 3, 4]) {alert(z);} var y, x = 5;",
         "var a, y, x; for (var z = 1 in a = [1, 2, 3, 4]) {alert(z);} x = 5;");
    test("var a, z; for (z in a = [1, 2, 3, 4]) {alert(z);} var y, x = 5;",
         "var a, z, y, x; for (z in a = [1, 2, 3, 4]) {alert(z);} x = 5;");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingVarsNestedFunction
  public void testGroupingVarsNestedFunction() {
    test("function f(b) {var x; function g() {var x; a = x; var y;} var a;}",
         "function f(b) {var x, a; function g() {var x, y; a = x;}}");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingVarsInnerFunction
  public void testGroupingVarsInnerFunction() {
    test("function f(b) {var x; h = x * x; var myfn = function() " +
         "{var x; a = x; var y;}; var a;}",
         "function f(b) {var x, myfn, a; h = x * x; myfn = function() " +
         "{var x, y; a = x;};}");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingVarsFirstStatementNotVar
  public void testGroupingVarsFirstStatementNotVar() {
    test("f(); var a; g(); var b;", "f(); var a, b; g();");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingVarsInScopeRegtest
  public void testGroupingVarsInScopeRegtest() {
    
    test("var x = 0, y = 1, z;" +
         "function f1(aa, bb) {" +
         "  if (y) {" +
         "    if (x === 0) {" +
         "      var h, r = 999;" +
         "    }" +
         "  } else {" +
         "    r = 1000;" +
         "  }" +
         "  var mylist = [1, 2, 3, 4];" +
         "  var k1 = 200, k2 = 400;" +
         "  for (var i1 = 0; i1 < 10; ++i1) {" +
         "    for (var i2 in mylist) {" +
         "      alert(i1);" +
         "    }" +
         "  }" +
         "  var jam, q = 100;" +
         "  var myfn = function() {" +
         "    var x = 1;" +
         "    f5();" +
         "    var z = 5;" +
         "  };" +
         "  function f5() {" +
         "    var aa = 5;" +
         "    if (y === 1) {" +
         "      var x = 100;" +
         "    }" +
         "  }" +
         "}" +
         "var h = x + y;" +
         "function g() {" +
         "  y = 0;" +
         "  { var x = 200;}" +
         "  var h = y + x;" +
         "}" +
         "var ggg = 0;",  
         "var x = 0, y = 1, z, h, ggg;" +
         "function f1(aa, bb) {" +
         "  if (y) {" +
         "    if (x === 0) {" +
         "      var h, r = 999, mylist, i1, i2, jam, q, myfn;" +
         "    }" +
         "  } else {" +
         "    r = 1000;" +
         "  }" +
         "  mylist = [1, 2, 3, 4];" +
         "  var k1 = 200, k2 = 400;" +
         "  for (i1 = 0; i1 < 10; ++i1) {" +
         "    for (i2 in mylist) {" +
         "      alert(i1);" +
         "    }" +
         "  }" +
         "  q = 100; " +
         "  myfn = function() {" +
         "    var x = 1, z;" +
         "    f5();" +
         "    z = 5;" +
         "  };" +
         "  function f5() {" +
         "    var aa = 5, x;" +
         "    if (y === 1) {" +
         "      x = 100;" +
         "    }" +
         "  }" +
         "}" +
         "h = x + y;" +
         "function g() {" +
         "  y = 0;" +
         "  { var x = 200, h;}" +
         "  h = y + x;" +
         "}" +
         "ggg = 0;");
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testNativeCtor
  public void testNativeCtor() {
    testSame(
        " " +
        "function Object(x) {};",
        "var x = new Object();" +
        " var y = new Object();", null);
    assertEquals(
        "Object.",
        findGlobalNameType("x").getJSDocInfo().getBlockDescription());
    assertEquals(
        "Object.",
        findGlobalNameType("y").getJSDocInfo().getBlockDescription());
    assertEquals(
        "Object.",
        globalScope.getVar("y").getType().getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testStructuralFunctions
  public void testStructuralFunctions() {
    testSame(
        " " +
        "function Object(x) {};",
        " " +
        "function fn(x) {};" +
        "var goog = {};" +
        " goog.x = new Object();" +
        " goog.y = fn;", null);
    assertEquals(
        "(Object|null)",
        globalScope.getVar("goog.x").getType().toString());
    assertEquals(
        "Object.",
        globalScope.getVar("goog.x").getType().restrictByNotNullOrUndefined()
        .getJSDocInfo().getBlockDescription());
    assertEquals(
        "Another function.",
        globalScope.getVar("goog.y").getType()
        .getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testInstanceObject
  public void testInstanceObject() {
    
    testSame(
        " function Foo() {}" +
        "var f = new Foo();" +
        " f.bar = 4;");
    ObjectType type = (ObjectType) globalScope.getVar("f").getType();
    assertEquals("Foo", type.toString());
    assertFalse(type.hasProperty("bar"));
    assertNull(type.getOwnPropertyJSDocInfo("bar"));
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testInterface
  public void testInterface() {
    testSame(
        " function Foo() {}" +
        "var f = new Foo();" +
        " f.bar = 4;");
    ObjectType type = (ObjectType) globalScope.getVar("Foo").getType();
    assertEquals(
        "An interface.",
        type.getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testNamespacedCtor
  public void testNamespacedCtor() {
    testSame(
        "var goog = {};" +
        " goog.Foo = function() {};" +
        "goog.Foo.bar = goog.Foo;" +
        "" +
        "goog.Foo.prototype.baz = goog.Foo;" +
        " var x = new goog.Foo();");
    assertEquals(
        "Hello!",
        findGlobalNameType("x").getJSDocInfo().getBlockDescription());
    assertEquals(
        "Hello!",
        findGlobalNameType("goog.Foo").getJSDocInfo().getBlockDescription());
    assertEquals(
        "Hello!",
        findGlobalNameType(
            "goog.Foo.bar").getJSDocInfo().getBlockDescription());

    assertEquals(
        "Hello!",
        findGlobalNameType(
            "goog.Foo.prototype.baz").getJSDocInfo().getBlockDescription());

    ObjectType proto = (ObjectType) findGlobalNameType("goog.Foo.prototype");
    assertEquals(
        "Bye!",
        proto.getPropertyType("baz").getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testAbstractMethod
  public void testAbstractMethod() {
    testSame(
        " var abstractMethod;" +
        " function Foo() {}" +
        "" +
        "Foo.prototype.bar = abstractMethod;");
    FunctionType abstractMethod =
        (FunctionType) findGlobalNameType("abstractMethod");
    assertNull(abstractMethod.getJSDocInfo());

    FunctionType ctor = (FunctionType) findGlobalNameType("Foo");
    ObjectType proto = ctor.getInstanceType().getImplicitPrototype();
    FunctionType method = (FunctionType) proto.getPropertyType("bar");
    assertEquals(
        "Block description.",
        method.getJSDocInfo().getBlockDescription());
    assertEquals(
        "Block description.",
        proto.getOwnPropertyJSDocInfo("bar").getBlockDescription());
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction1
  public void testInlineEmptyFunction1() {
    
    test("function foo(){}" +
        "foo();",
        "void 0;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction2
  public void testInlineEmptyFunction2() {
    
    test("function foo(){}" +
        "foo(1, new Date, function(){});",
        "void 0;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction3
  public void testInlineEmptyFunction3() {
    
    test("function foo(){}" +
        "foo();foo();foo();",
        "void 0;void 0;void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction4
  public void testInlineEmptyFunction4() {
    
    test("function foo(){}" +
        "foo(x());",
        "{var JSCompiler_inline_anon_param_0=x();}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction5
  public void testInlineEmptyFunction5() {
    
    
    allowBlockInlining = false;
    testSame("function foo(){}" +
        "foo(x());");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions1
  public void testInlineFunctions1() {
    
    test("function foo(){ return 4 }" +
        "foo();",
        "4");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions2
  public void testInlineFunctions2() {
    
    
    test("var t;var AB=function(){return 4};" +
         "function BC(){return 6;}" +
         "CD=function(x){return x + 5};x=CD(3);y=AB();z=BC();",
         "var t;CD=function(x){return x+5};x=CD(3);y=4;z=6"
         );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions3
  public void testInlineFunctions3() {
    
    test("var t;var AB=function(){return 4};" +
        "function BC(){return 6;}" +
        "var CD=function(x){return x + 5};x=CD(3);y=AB();z=BC();",
        "var t;x=3+5;y=4;z=6");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions4
  public void testInlineFunctions4() {
    
    test("var t; var AB = function() { return 4 }; " +
        "function BC() { return 6; }" +
        "CD = 0;" +
        "CD = function(x) { return x + 5 }; x = CD(3); y = AB(); z = BC();",

        "var t;CD=0;CD=function(x){return x+5};x=CD(3);y=4;z=6");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions5
  public void testInlineFunctions5() {
    
    test("var FOO_FN=function(x,y) { return \"de\" + x + \"nu\" + y };" +
         "var a = FOO_FN(\"ez\", \"ts\")",

         "var a=\"de\"+\"ez\"+\"nu\"+\"ts\"");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions6
  public void testInlineFunctions6() {
    
    test("function BAR_FN(x, y, z) { return z(foo(x + y)) }" +
         "alert(BAR_FN(1, 2, baz))",

         "alert(baz(foo(1+2)))");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions7
  public void testInlineFunctions7() {
    
    test("function FN(x,y,z){return x+x+y}" +
         "var b=FN(1,2,3)",

         "var b=1+1+2");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions8
  public void testInlineFunctions8() {
    
    test("function MUL(x,y){return x*y}function ADD(x,y){return x+y}" +
         "var a=1+MUL(2,3);var b=2*ADD(3,4)",

         "var a=1+2*3;var b=2*(3+4)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions9
  public void testInlineFunctions9() {
    
    test("function INC(x){return x++}" +
         "var y=INC(i)",
         "var y;{var x$$inline_1=i;" +
         "y=x$$inline_1++}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions10
  public void testInlineFunctions10() {
    test("function INC(x){return x++}" +
         "var y=INC(i);y=INC(i)",
         "var y;" +
         "{var x$$inline_1=i;" +
         "y=x$$inline_1++}" +
         "{var x$$inline_4=i;" +
         "y=x$$inline_4++}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions11
  public void testInlineFunctions11() {
    test("function f(x){return x}" +
          "var y=f(i)",
          "var y=i");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions12
  public void testInlineFunctions12() {
    
    allowBlockInlining = false;
    test("function f(x){return x}" +
          "var y=f(i)",
          "var y=i");
    testSame("function f(x){return x}" +
         "var y=f(i++)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions13
  public void testInlineFunctions13() {
    
    test("function f(x){return x}" +
         "var y=f(i++)",
         "var y;{var x$$inline_1=i++;y=x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions14
  public void testInlineFunctions14() {
    
    test("function FOO(x){return x}var BAR=function(y){return y}" +
             ";b=FOO;a(BAR);x=FOO(1);y=BAR(2)",

         "function FOO(x){return x}var BAR=function(y){return y}" +
             ";b=FOO;a(BAR);x=1;y=2");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions15a
  public void testInlineFunctions15a() {
    
    test("function foo(){return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "var d=b()+foo()",

         "var d=c+function(a){return a+1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions15b
  public void testInlineFunctions15b() {
    assumeMinimumCapture = false;

    
    test("function foo(){var x;return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "var d=b()+foo()",

         "function foo(){var x;return function(a){return a+1}}" +
         "var d=c+foo()");

    assumeMinimumCapture = true;

    test("function foo(){var x;return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "var d=b()+foo()",

         "var JSCompiler_temp_const$$0 = c;\n" +
         "{\n" +
         "var JSCompiler_inline_result$$1;\n" +
         "var x$$inline_3;\n" +
         "JSCompiler_inline_result$$1 = " +
         "    function(a$$inline_4){ return a$$inline_4+1 };\n" +
         "}" +
         "var d=JSCompiler_temp_const$$0 + JSCompiler_inline_result$$1");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions15c
  public void testInlineFunctions15c() {
    assumeMinimumCapture = false;

    
    test("function foo(){return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "function _x(){ var d=b()+foo() }",

         "function foo(){return function(a){return a+1}}" +
         "function _x(){ var d=c+foo() }");

    assumeMinimumCapture = true;

    
    test("function foo(){return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "function _x(){ var d=b()+foo() }",

         "function _x(){var d=c+function(a){return a+1}}");

  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions15d
  public void testInlineFunctions15d() {
    assumeMinimumCapture = false;

    
    test("function foo(){var x; return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "function _x(){ var d=b()+foo() }",

         "function foo(){var x; return function(a){return a+1}}" +
         "function _x(){ var d=c+foo() }");

    assumeMinimumCapture = true;

    
    test("function foo(){var x; return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "function _x(){ var d=b()+foo() }",

         "function _x() { \n" +
         "  var JSCompiler_temp_const$$0 = c;\n" +
         "  {\n" +
         "  var JSCompiler_inline_result$$1;\n" +
         "  var x$$inline_3;\n" +
         "  JSCompiler_inline_result$$1 = " +
         "      function(a$$inline_4) {return a$$inline_4+1};\n" +
         "  }\n" +
         "  var d = JSCompiler_temp_const$$0+JSCompiler_inline_result$$1\n" +
         "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions16a
  public void testInlineFunctions16a() {
    assumeMinimumCapture = false;

    testSame("function foo(b){return window.bar(function(){c(b)})}" +
         "var d=foo(e)");

    assumeMinimumCapture = true;

    test(
        "function foo(b){return window.bar(function(){c(b)})}" +
        "var d=foo(e)",
        "var d;{var b$$inline_1=e;" +
        "d=window.bar(function(){c(b$$inline_1)})}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions16b
  public void testInlineFunctions16b() {
    test("function foo(){return window.bar(function(){c()})}" +
         "var d=foo(e)",
         "var d=window.bar(function(){c()})");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions17
  public void testInlineFunctions17() {
    
    testSame("function foo(x){return x*x+foo(3)}var bar=foo(4)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions18
  public void testInlineFunctions18() {
    
    allowBlockInlining = false;
    test("function foo(a, b){return a+b}" +
         "function bar(d){return c}" +
         "var d=foo(bar(1),e)",
         "var d=c+e");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions19
  public void testInlineFunctions19() {
    
    
    test("function foo(a, b){return a+b}" +
        "function bar(d){return c}" +
        "var d=foo(bar(1),e)",
        "var d;{d=c+e}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions20
  public void testInlineFunctions20() {
    
    allowBlockInlining = false;
    test("function foo(a, b){return a+b}" +
         "function bar(d){return c}" +
         "var d=bar(foo(1,e));",
         "var d=c");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions21
  public void testInlineFunctions21() {
    
    test("function foo(a, b){return a+b}" +
        "function bar(d){return c}" +
        "var d=bar(foo(1,e))",
        "var d;{d=c}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions22
  public void testInlineFunctions22() {
    
    test("function plex(a){if(a) return 0;else return 1;}" +
         "function foo(a, b){return bar(a+b)}" +
         "function bar(d){return plex(d)}" +
         "var d=foo(1,2)",

         "var d;{JSCompiler_inline_label_plex_2:{" +
         "if(1+2){" +
         "d=0;break JSCompiler_inline_label_plex_2}" +
         "else{" +
         "d=1;break JSCompiler_inline_label_plex_2}d=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions23
  public void testInlineFunctions23() {
    
    test("function complex(a){if(a) return 0;else return 1;}" +
         "function bar(d){return complex(d)}" +
         "function foo(a, b){return bar(a+b)}" +
         "var d=foo(1,2)",

         "var d;{JSCompiler_inline_label_complex_2:{" +
         "if(1+2){" +
         "d=0;break JSCompiler_inline_label_complex_2" +
         "}else{" +
         "d=1;break JSCompiler_inline_label_complex_2" +
         "}d=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions24
  public void testInlineFunctions24() {
    
    testSame("function foo(x){return this}foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions25
  public void testInlineFunctions25() {
    testSame("function foo(){return arguments[0]}foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions26
  public void testInlineFunctions26() {
    
    testSame("function _foo(x){return x}_foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions27
  public void testInlineFunctions27() {
    test("var window = {}; function foo(){window.bar++; return 3;}" +
        "var x = {y: 1, z: foo(2)};",
        "var window={};" +
        "{" +
        "  var JSCompiler_inline_result$$0;" +
        "  window.bar++;" +
        "  JSCompiler_inline_result$$0 = 3;" +
        "}" +
        "var x = {y: 1, z: JSCompiler_inline_result$$0};");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions28
  public void testInlineFunctions28() {
    test("var window = {}; function foo(){window.bar++; return 3;}" +
        "var x = {y: alert(), z: foo(2)};",
        "var window = {};" +
        "var JSCompiler_temp_const$$0 = alert();" +
        "{" +
        " var JSCompiler_inline_result$$1;" +
        " window.bar++;" +
        " JSCompiler_inline_result$$1 = 3;}" +
        "var x = {" +
        "  y: JSCompiler_temp_const$$0," +
        "  z: JSCompiler_inline_result$$1" +
        "};");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions29
  public void testInlineFunctions29() {
    test("var window = {}; function foo(){window.bar++; return 3;}" +
        "var x = {a: alert(), b: alert2(), c: foo(2)};",
        "var window = {};" +
        "var JSCompiler_temp_const$$1 = alert();" +
        "var JSCompiler_temp_const$$0 = alert2();" +
        "{" +
        " var JSCompiler_inline_result$$2;" +
        " window.bar++;" +
        " JSCompiler_inline_result$$2 = 3;}" +
        "var x = {" +
        "  a: JSCompiler_temp_const$$1," +
        "  b: JSCompiler_temp_const$$0," +
        "  c: JSCompiler_inline_result$$2" +
        "};");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions30
  public void testInlineFunctions30() {
    
    testSame("function foo(){ return eval() }" +
        "foo();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions31
  public void testInlineFunctions31() {
    
    test("function foo(){ lab:{4;} }" +
        "lab:{foo();}",
        "lab:{{JSCompiler_inline_label_0:{4}}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInlining1
  public void testMixedModeInlining1() {
    
    test("function foo(){return 1}" +
        "foo();",
        "1;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInlining2
  public void testMixedModeInlining2() {
    
    
    test("function foo(){return 1}" +
        "foo(x());",
        "{var JSCompiler_inline_anon_param_0=x();1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInlining3
  public void testMixedModeInlining3() {
    
    test("function foo(){return 1}" +
        "foo();foo(x());",
        "1;{var JSCompiler_inline_anon_param_0=x();1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInlining4
  public void testMixedModeInlining4() {
    
    
    test("function foo(){return 1}" +
        "foo();foo(x());" +
        "foo(1);foo(1,x());",
        "1;{var JSCompiler_inline_anon_param_0=x();1}" +
        "1;{var JSCompiler_inline_anon_param_4=x();1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInliningCosting1
  public void testMixedModeInliningCosting1() {
    

    
    test(
        "function foo(a,b){return a+b+a+b+4+5+6+7+8+9+1+2+3+4+5}" +
        "foo(1,2);" +
        "foo(2,3)",

        "1+2+1+2+4+5+6+7+8+9+1+2+3+4+5;" +
        "2+3+2+3+4+5+6+7+8+9+1+2+3+4+5");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInliningCosting2
  public void testMixedModeInliningCosting2() {
    
    
    testSame(
        "function foo(a,b){return a+b+a+b+4+5+6+7+8+9+1+2+3+4+5}" +
        "foo(1,2);" +
        "foo(2,3,x())");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInliningCosting3
  public void testMixedModeInliningCosting3() {
    
    test(
        "function foo(a,b){return a+b+a+b+4+5+6+7+8+9+1+2+3+10}" +
        "foo(1,2);" +
        "foo(2,3,x())",

        "1+2+1+2+4+5+6+7+8+9+1+2+3+10;" +
        "{var JSCompiler_inline_anon_param_4=x();" +
        "2+3+2+3+4+5+6+7+8+9+1+2+3+10}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInliningCosting4
  public void testMixedModeInliningCosting4() {
    
    testSame(
        "function foo(a,b){return a+b+a+b+4+5+6+7+8+9+1+2+3+4+101}" +
        "foo(1,2);" +
        "foo(2,3,x())");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified1
  public void testNoInlineIfParametersModified1() {
    
    test("function f(x){return x=1}f(undefined)",
         "{var x$$inline_1=undefined;" +
         "x$$inline_1=1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified2
  public void testNoInlineIfParametersModified2() {
    test("function f(x){return (x)=1;}f(2)",
         "{var x$$inline_1=2;" +
         "x$$inline_1=1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified3
  public void testNoInlineIfParametersModified3() {
    
    test("function f(x){return x*=2}f(2)",
         "{var x$$inline_1=2;" +
         "x$$inline_1*=2}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified4
  public void testNoInlineIfParametersModified4() {
    
    test("function f(x){return x?(x=2):0}f(2)",
         "{var x$$inline_1=2;" +
         "x$$inline_1?(" +
         "x$$inline_1=2):0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified5
  public void testNoInlineIfParametersModified5() {
    
    test("function f(x,y){return x?(y=2):0}f(2,undefined)",
         "{var y$$inline_3=undefined;2?(" +
         "y$$inline_3=2):0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified6
  public void testNoInlineIfParametersModified6() {
    test("function f(x,y){return x?(y=2):0}f(2)",
         "{var y$$inline_3=void 0;2?(" +
         "y$$inline_3=2):0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified7
  public void testNoInlineIfParametersModified7() {
    
    test("function f(a){return++a<++a}f(1)",
         "{var a$$inline_1=1;" +
         "++a$$inline_1<" +
         "++a$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified8
  public void testNoInlineIfParametersModified8() {
    
    test("function f(a){return a.x=2}f(o)", "o.x=2");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified9
  public void testNoInlineIfParametersModified9() {
    
    test("function f(a){return a[2]=2}f(o)", "o[2]=2");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverPartialSubtitution1
  public void testInlineNeverPartialSubtitution1() {
    test("function f(z){return x.y.z;}f(1)",
         "x.y.z");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverPartialSubtitution2
  public void testInlineNeverPartialSubtitution2() {
    test("function f(z){return x.y[z];}f(a)",
         "x.y[a]");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverMutateConstants
  public void testInlineNeverMutateConstants() {
    test("function f(x){return x=1}f(undefined)",
         "{var x$$inline_1=undefined;" +
         "x$$inline_1=1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverOverrideNewValues
  public void testInlineNeverOverrideNewValues() {
    test("function f(a){return++a<++a}f(1)",
        "{var a$$inline_1=1;" +
        "++a$$inline_1<++a$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineMutableArgsReferencedOnce
  public void testInlineMutableArgsReferencedOnce() {
    test("function foo(x){return x;}foo([])", "[]");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMutableArgs1
  public void testNoInlineMutableArgs1() {
    allowBlockInlining = false;
    testSame("function foo(x){return x+x} foo([])");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMutableArgs2
  public void testNoInlineMutableArgs2() {
    allowBlockInlining = false;
    testSame("function foo(x){return x+x} foo(new Date)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMutableArgs3
  public void testNoInlineMutableArgs3() {
    allowBlockInlining = false;
    testSame("function foo(x){return x+x} foo(true&&new Date)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMutableArgs4
  public void testNoInlineMutableArgs4() {
    allowBlockInlining = false;
    testSame("function foo(x){return x+x} foo({})");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs1
  public void testInlineBlockMutableArgs1() {
    test("function foo(x){x+x}foo([])",
         "{var x$$inline_1=[];" +
         "x$$inline_1+x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs2
  public void testInlineBlockMutableArgs2() {
    test("function foo(x){x+x}foo(new Date)",
         "{var x$$inline_1=new Date;" +
         "x$$inline_1+x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs3
  public void testInlineBlockMutableArgs3() {
    test("function foo(x){x+x}foo(true&&new Date)",
         "{var x$$inline_1=true&&new Date;" +
         "x$$inline_1+x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs4
  public void testInlineBlockMutableArgs4() {
    test("function foo(x){x+x}foo({})",
         "{var x$$inline_1={};" +
         "x$$inline_1+x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables1
  public void testShadowVariables1() {
    
    

    
    
    test("var a=0;" +
         "function foo(a){return 3+a}" +
         "function bar(){var a=foo(4)}" +
         "bar();",

         "var a=0;" +
         "{var a$$inline_1=3+4}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables2
  public void testShadowVariables2() {
    
    
    
    test("var a=0;" +
        "function foo(a){return 3+a}" +
        "function bar(){a=foo(4)}" +
        "bar()",

        "var a=0;" +
        "{a=3+4}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables3
  public void testShadowVariables3() {
    
    test("var a=0;" +
        "function foo(){var a=2;return 3+a}" +
        "function _bar(){a=foo()}",

        "var a=0;" +
        "function _bar(){{var a$$inline_1=2;" +
        "a=3+a$$inline_1}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables4
  public void testShadowVariables4() {
    
    
    test("var a=0;" +
         "function foo(){return 3+a}" +
         "function _bar(a){a=foo(4)+a}",

         "var a=0;function _bar(a$$1){" +
         "a$$1=" +
         "3+a+a$$1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables5
  public void testShadowVariables5() {
    
    
    allowBlockInlining = false;
    testSame("var a=0;" +
        "function foo(){var a=4;return 3+a}" +
        "function _bar(a){a=foo(4)+a}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables6
  public void testShadowVariables6() {
    test("var a=0;" +
        "function foo(){var a=4;return 3+a}" +
        "function _bar(a){a=foo(4)}",

        "var a=0;function _bar(a$$2){{" +
        "var a$$inline_1=4;" +
        "a$$2=3+a$$inline_1}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables7
  public void testShadowVariables7() {
    assumeMinimumCapture = false;
    test("var a=3;" +
         "function foo(){return a}" +
         "(function(){var a=5;(function(){foo()})()})()",
         "var a=3;" +
         "{var a$$inline_1=5;{a}}"
         );

    assumeMinimumCapture = true;
    test("var a=3;" +
         "function foo(){return a}" +
         "(function(){var a=5;(function(){foo()})()})()",
         "var a=3;" +
         "{var a$$inline_2=5;{a}}"
         );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables8
  public void testShadowVariables8() {
    
    test("var a=0;" +
         "function foo(){return 3}" +
         "function _bar(){var a=foo()}",

         "var a=0;" +
         "function _bar(){var a=3}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables9
  public void testShadowVariables9() {
    
    test("function foo(){return 3}" +
         "function _bar(){var a=foo()}",

         "function _bar(){var a=3}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables10
  public void testShadowVariables10() {
    
    test("var a;function foo(){return a}" +
         "function _bar(){var a=foo()}",
         "var a;function _bar(){var a$$1=a}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables11
  public void testShadowVariables11() {
    
    
    test("var a=0;var b=1;" +
         "function foo(){return a+a}" +
         "function _bar(){var a=foo();alert(a)}",
         "var a=0;var b=1;" +
         "function _bar(){var a$$1=a+a;" +
         "alert(a$$1)}"
         );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables12
  public void testShadowVariables12() {
    
    test("var a=0;var b=1;" +
         "function foo(){return a+b}" +
         "function _bar(){var a=foo(),b;alert(a)}",
         "var a=0;var b=1;" +
         "function _bar(){var a$$1=a+b," +
         "b$$1;" +
         "alert(a$$1)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables13
  public void testShadowVariables13() {
    
    test("var a=0;var b=1;" +
         "function foo(){return a+a}" +
         "function _bar(){var c=foo();alert(c)}",

         "var a=0;var b=1;" +
         "function _bar(){var c=a+a;alert(c)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables14
  public void testShadowVariables14() {
    
    test("var a=0;var b=1;" +
         "function foo(){return a+b}" +
         "function _bar(){var c=foo(),b;alert(c)}",
         "var a=0;var b=1;" +
         "function _bar(){var c=a+b," +
         "b$$1;alert(c)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables15
  public void testShadowVariables15() {
    
    test("var a=0;var b=1;" +
         "function foo(){return a+a}" +
         "function _bar(){var c=foo();alert(c+a)}",

         "var a=0;var b=1;" +
         "function _bar(){var c=a+a;alert(c+a)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables16
  public void testShadowVariables16() {
    assumeMinimumCapture = false;
    
    test("var a=3;" +
         "function foo(){return a}" +
         "(function(){var a=5;(function(){foo()})()})()",
         "var a=3;" +
         "{var a$$inline_1=5;{a}}"
         );

    assumeMinimumCapture = true;
    
    test("var a=3;" +
         "function foo(){return a}" +
         "(function(){var a=5;(function(){foo()})()})()",
         "var a=3;" +
         "{var a$$inline_2=5;{a}}"
         );

  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables17
  public void testShadowVariables17() {
    test("var a=0;" +
         "function bar(){return a+a}" +
         "function foo(){return bar()}" +
         "function _goo(){var a=2;var x=foo();}",

         "var a=0;" +
         "function _goo(){var a$$1=2;var x=a+a}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables18
  public void testShadowVariables18() {
    test("var a=0;" +
        "function bar(){return a+a}" +
        "function foo(){var a=3;return bar()}" +
        "function _goo(){var a=2;var x=foo();}",

        "var a=0;" +
        "function _goo(){var a$$2=2;var x;" +
        "{var a$$inline_1=3;x=a+a}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining1
  public void testCostBasedInlining1() {
    testSame(
        "function foo(a){return a}" +
        "foo=new Function(\"return 1\");" +
        "foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining2
  public void testCostBasedInlining2() {
    
    
    test(
        "function foo(a){return a}" +
        "var b=foo;" +
        "function _t1(){return foo(1)}",

        "function foo(a){return a}" +
        "var b=foo;" +
        "function _t1(){return 1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining3
  public void testCostBasedInlining3() {
    
    test(
        "function foo(a,b){return a+b}" +
        "var b=foo;" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}",

        "function foo(a,b){return a+b}" +
        "var b=foo;" +
        "function _t1(){return 1+2}" +
        "function _t2(){return 2+3}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining4
  public void testCostBasedInlining4() {
    
    
    testSame(
        "function foo(a,b){return a+b+a+b}" +
        "var b=foo;" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining5
  public void testCostBasedInlining5() {
    
    test(
        "function foo(a,b){return a+b+a+b}" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}",

        "function _t1(){return 1+2+1+2}" +
        "function _t2(){return 2+3+2+3}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining6
  public void testCostBasedInlining6() {
    
    
    test(
        "function foo(a,b){return a+b+a+b+a+b+a+b+4+5+6+7+8+9+1+2+3+4+5}" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}",

        "function _t1(){return 1+2+1+2+1+2+1+2+4+5+6+7+8+9+1+2+3+4+5}" +
        "function _t2(){return 2+3+2+3+2+3+2+3+4+5+6+7+8+9+1+2+3+4+5}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining7
  public void testCostBasedInlining7() {
    
    testSame(
        "function foo(a,b){" +
        "    return a+b+a+b+a+b+a+b+4+5+6+7+8+9+1+2+3+4+5+6}" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining8
  public void testCostBasedInlining8() {
    
    
    
    
    
    
    
    allowBlockInlining = false;
    testSame("function f(a){return 1 + a + a;}" +
        "var a = f(f(1));");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining9
  public void testCostBasedInlining9() {
    
    
    
    test("function f(a){return 1 + a + a;}" +
         "var a = f(f(1));",
         "var a;" +
         "{var a$$inline_1=1+1+1;" +
         "a=1+a$$inline_1+a$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining10
  public void testCostBasedInlining10() {
    
    
    
    allowBlockInlining = false;
    test("function f(a){return a + a;}" +
        "var a = f(f(1));",
        "var a= 1+1+(1+1);");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining11
  public void testCostBasedInlining11() {
    
    test("function f(a){return a + a;}" +
         "var a = f(f(1))",
         "var a;" +
         "{var a$$inline_1=1+1;" +
         "a=a$$inline_1+a$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining12
  public void testCostBasedInlining12() {
    test("function f(a){return 1 + a + a;}" +
         "var a = f(1) + f(2);",

         "var a=1+1+1+(1+2+2)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex1
  public void testCostBasedInliningComplex1() {
    testSame(
        "function foo(a){a()}" +
        "foo=new Function(\"return 1\");" +
        "foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex2
  public void testCostBasedInliningComplex2() {
    
    
    test(
        "function foo(a){a()}" +
        "var b=foo;" +
        "function _t1(){foo(x)}",

        "function foo(a){a()}" +
        "var b=foo;" +
        "function _t1(){{x()}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex3
  public void testCostBasedInliningComplex3() {
    
    test(
        "function foo(a,b){a+b}" +
        "var b=foo;" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}",

        "function foo(a,b){a+b}" +
        "var b=foo;" +
        "function _t1(){{1+2}}" +
        "function _t2(){{2+3}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex4
  public void testCostBasedInliningComplex4() {
    
    
    testSame(
        "function foo(a,b){a+b+a+b}" +
        "var b=foo;" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex5
  public void testCostBasedInliningComplex5() {
    
    test(
        "function foo(a,b){a+b+a+b}" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}",

        "function _t1(){{1+2+1+2}}" +
        "function _t2(){{2+3+2+3}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex6
  public void testCostBasedInliningComplex6() {
    
    
    test(
        "function foo(a,b){a+b+a+b+a+b+a+b+4+5+6+7+8+9+1}" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}",

        "function _t1(){{1+2+1+2+1+2+1+2+4+5+6+7+8+9+1}}" +
        "function _t2(){{2+3+2+3+2+3+2+3+4+5+6+7+8+9+1}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex7
  public void testCostBasedInliningComplex7() {
    
    testSame(
        "function foo(a,b){a+b+a+b+a+b+a+b+4+5+6+7+8+9+1+2}" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex8
  public void testCostBasedInliningComplex8() {
    
    testSame("function _f(a){1+a+a}" +
             "a=_f(1)+_f(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex9
  public void testCostBasedInliningComplex9() {
    test("function f(a){1 + a + a;}" +
         "f(1);f(2);",
         "{1+1+1}{1+2+2}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testDoubleInlining1
  public void testDoubleInlining1() {
    allowBlockInlining = false;
    test("var foo = function(a) { return getWindow(a); };" +
         "var bar = function(b) { return b; };" +
         "foo(bar(x));",
         "getWindow(x)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testDoubleInlining2
  public void testDoubleInlining2() {
    test("var foo = function(a) { return getWindow(a); };" +
         "var bar = function(b) { return b; };" +
         "foo(bar(x));",
         "{getWindow(x)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineOfNonGlobalFunction1
  public void testNoInlineOfNonGlobalFunction1() {
    test("var g;function _f(){function g(){return 0}}" +
         "function _h(){return g()}",
         "var g;function _f(){}" +
         "function _h(){return g()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineOfNonGlobalFunction2
  public void testNoInlineOfNonGlobalFunction2() {
    test("var g;function _f(){var g=function(){return 0}}" +
         "function _h(){return g()}",
         "var g;function _f(){}" +
         "function _h(){return g()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineOfNonGlobalFunction3
  public void testNoInlineOfNonGlobalFunction3() {
    test("var g;function _f(){var g=function(){return 0}}" +
         "function _h(){return g()}",
         "var g;function _f(){}" +
         "function _h(){return g()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineOfNonGlobalFunction4
  public void testNoInlineOfNonGlobalFunction4() {
    test("var g;function _f(){function g(){return 0}}" +
         "function _h(){return g()}",
         "var g;function _f(){}" +
         "function _h(){return g()}");

  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMaskedFunction
  public void testNoInlineMaskedFunction() {
    
    
    test("var g=function(){return 0};" +
         "function _f(g){return g()}",
         "function _f(g$$1){return g$$1()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineNonFunction
  public void testNoInlineNonFunction() {
    testSame("var g=3;function _f(){return g()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineCall
  public void testInlineCall() {
    test("function f(g) { return g.h(); } f('x');",
         "\"x\".h()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctionWithArgsMismatch1
  public void testInlineFunctionWithArgsMismatch1() {
    test("function f(g) { return g; } f();",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctionWithArgsMismatch2
  public void testInlineFunctionWithArgsMismatch2() {
    test("function f() { return 0; } f(1);",
         "0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctionWithArgsMismatch3
  public void testInlineFunctionWithArgsMismatch3() {
    test("function f(one, two, three) { return one + two + three; } f(1);",
         "1+void 0+void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctionWithArgsMismatch4
  public void testInlineFunctionWithArgsMismatch4() {
    test("function f(one, two, three) { return one + two + three; }" +
         "f(1,2,3,4,5);",
         "1+2+3");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testArgumentsWithSideEffectsNeverInlined1
  public void testArgumentsWithSideEffectsNeverInlined1() {
    allowBlockInlining = false;
    testSame("function f(){return 0} f(new goo());");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testArgumentsWithSideEffectsNeverInlined2
  public void testArgumentsWithSideEffectsNeverInlined2() {
    allowBlockInlining = false;
    testSame("function f(g,h){return h+g}f(g(),h());");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testOneSideEffectCallDoesNotRuinOthers
  public void testOneSideEffectCallDoesNotRuinOthers() {
    allowBlockInlining = false;
    test("function f(){return 0}f(new goo());f()",
         "function f(){return 0}f(new goo());0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultNoParamCall1
  public void testComplexInlineNoResultNoParamCall1() {
    test("function f(){a()}f()",
         "{a()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultNoParamCall2
  public void testComplexInlineNoResultNoParamCall2() {
   test("function f(){if (true){return;}else;} f();",
         "{JSCompiler_inline_label_f_0:{" +
             "if(true)break JSCompiler_inline_label_f_0;else;}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultNoParamCall3
  public void testComplexInlineNoResultNoParamCall3() {
    
    
    

    
    test("function f(){a();b();var z=1+1}function _foo(){f()}",
         "function _foo(){{a();b();var z$$inline_1=1+1}}");

  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultWithParamCall1
  public void testComplexInlineNoResultWithParamCall1() {
    test("function f(x){a(x)}f(1)",
         "{a(1)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultWithParamCall2
  public void testComplexInlineNoResultWithParamCall2() {
    test("function f(x,y){a(x)}var b=1;f(1,b)",
         "var b=1;{a(1)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineNoResultWithParamCall3
  public void testComplexInlineNoResultWithParamCall3() {
    test("function f(x,y){if (x) y(); return true;}var b=1;f(1,b)",
         "var b=1;{if(1)b();true}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline1
  public void testComplexInline1() {
    test("function f(){if (true){return;}else;} z=f();",
         "{JSCompiler_inline_label_f_0:" +
         "{if(true){z=void 0;" +
         "break JSCompiler_inline_label_f_0}else;z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline2
  public void testComplexInline2() {
    test("function f(){if (true){return;}else return;} z=f();",
         "{JSCompiler_inline_label_f_0:{if(true){z=void 0;" +
         "break JSCompiler_inline_label_f_0}else{z=void 0;" +
         "break JSCompiler_inline_label_f_0}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline3
  public void testComplexInline3() {
    test("function f(){if (true){return 1;}else return 0;} z=f();",
         "{JSCompiler_inline_label_f_0:{if(true){z=1;" +
         "break JSCompiler_inline_label_f_0}else{z=0;" +
         "break JSCompiler_inline_label_f_0}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline4
  public void testComplexInline4() {
    test("function f(x){a(x)} z = f(1)",
         "{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline5
  public void testComplexInline5() {
    test("function f(x,y){a(x)}var b=1;z=f(1,b)",
         "var b=1;{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline6
  public void testComplexInline6() {
    test("function f(x,y){if (x) y(); return true;}var b=1;z=f(1,b)",
         "var b=1;{if(1)b();z=true}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline7
  public void testComplexInline7() {
    test("function f(x,y){if (x) return y(); else return true;}" +
         "var b=1;z=f(1,b)",
         "var b=1;{JSCompiler_inline_label_f_4:{if(1){z=b();" +
         "break JSCompiler_inline_label_f_4}else{z=true;" +
         "break JSCompiler_inline_label_f_4}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInline8
  public void testComplexInline8() {
    test("function f(x){a(x)}var z=f(1)",
         "var z;{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars1
  public void testComplexInlineVars1() {
    test("function f(){if (true){return;}else;}var z=f();",
         "var z;{JSCompiler_inline_label_f_0:{" +
         "if(true){z=void 0;break JSCompiler_inline_label_f_0}else;z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars2
  public void testComplexInlineVars2() {
    test("function f(){if (true){return;}else return;}var z=f();",
        "var z;{JSCompiler_inline_label_f_0:{" +
        "if(true){z=void 0;break JSCompiler_inline_label_f_0" +
        "}else{" +
        "z=void 0;break JSCompiler_inline_label_f_0}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars3
  public void testComplexInlineVars3() {
    test("function f(){if (true){return 1;}else return 0;}var z=f();",
         "var z;{JSCompiler_inline_label_f_0:{if(true){" +
         "z=1;break JSCompiler_inline_label_f_0" +
         "}else{" +
         "z=0;break JSCompiler_inline_label_f_0}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars4
  public void testComplexInlineVars4() {
    test("function f(x){a(x)}var z = f(1)",
         "var z;{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars5
  public void testComplexInlineVars5() {
    test("function f(x,y){a(x)}var b=1;var z=f(1,b)",
         "var b=1;var z;{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars6
  public void testComplexInlineVars6() {
    test("function f(x,y){if (x) y(); return true;}var b=1;var z=f(1,b)",
         "var b=1;var z;{if(1)b();z=true}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars7
  public void testComplexInlineVars7() {
    test("function f(x,y){if (x) return y(); else return true;}" +
         "var b=1;var z=f(1,b)",
         "var b=1;var z;" +
         "{JSCompiler_inline_label_f_4:{if(1){z=b();" +
         "break JSCompiler_inline_label_f_4" +
         "}else{" +
         "z=true;break JSCompiler_inline_label_f_4}z=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars8
  public void testComplexInlineVars8() {
    test("function f(x){a(x)}var x;var z=f(1)",
         "var x;var z;{a(1);z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars9
  public void testComplexInlineVars9() {
    test("function f(x){a(x)}var x;var z=f(1);var y",
         "var x;var z;{a(1);z=void 0}var y");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars10
  public void testComplexInlineVars10() {
    test("function f(x){a(x)}var x=blah();var z=f(1);var y=blah();",
          "var x=blah();var z;{a(1);z=void 0}var y=blah()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars11
  public void testComplexInlineVars11() {
    test("function f(x){a(x)}var x=blah();var z=f(1);var y;",
         "var x=blah();var z;{a(1);z=void 0}var y");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineVars12
  public void testComplexInlineVars12() {
    test("function f(x){a(x)}var x;var z=f(1);var y=blah();",
         "var x;var z;{a(1);z=void 0}var y=blah()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions1
  public void testComplexInlineInExpresssions1() {
    test("function f(){a()}var z=f()",
         "var z;{a();z=void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions2
  public void testComplexInlineInExpresssions2() {
    test("function f(){a()}c=z=f()",
         "{var JSCompiler_inline_result$$0;a();}" +
         "c=z=JSCompiler_inline_result$$0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions3
  public void testComplexInlineInExpresssions3() {
    test("function f(){a()}c=z=f()",
        "{var JSCompiler_inline_result$$0;a();}" +
        "c=z=JSCompiler_inline_result$$0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions4
  public void testComplexInlineInExpresssions4() {
    test("function f(){a()}if(z=f());",
        "{var JSCompiler_inline_result$$0;a();}" +
        "if(z=JSCompiler_inline_result$$0);");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexInlineInExpresssions5
  public void testComplexInlineInExpresssions5() {
    test("function f(){a()}if(z.y=f());",
         "var JSCompiler_temp_const$$0=z;" +
         "{var JSCompiler_inline_result$$1;a()}" +
         "if(JSCompiler_temp_const$$0.y=JSCompiler_inline_result$$1);");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexNoInline1
  public void testComplexNoInline1() {
    testSame("function f(){a()}while(z=f())continue");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexNoInline2
  public void testComplexNoInline2() {
    testSame("function f(){a()}do;while(z=f())");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexSample
  public void testComplexSample() {
    String result = "" +
      "{{" +
      "var styleSheet$$inline_9=null;" +
      "if(goog$userAgent$IE)" +
        "styleSheet$$inline_9=0;" +
      "else " +
        "var head$$inline_10=0;" +
      "{" +
        "var element$$inline_11=" +
            "styleSheet$$inline_9;" +
        "var stylesString$$inline_12=a;" +
        "if(goog$userAgent$IE)" +
          "element$$inline_11.cssText=" +
              "stylesString$$inline_12;" +
        "else " +
        "{" +
          "var propToSet$$inline_13=" +
              "\"innerText\";" +
          "element$$inline_11[" +
              "propToSet$$inline_13]=" +
                  "stylesString$$inline_12" +
        "}" +
      "}" +
      "styleSheet$$inline_9" +
      "}}";

    test("var foo = function(stylesString, opt_element) { " +
        "var styleSheet = null;" +
        "if (goog$userAgent$IE)" +
          "styleSheet = 0;" +
        "else " +
          "var head = 0;" +
        "" +
        "goo$zoo(styleSheet, stylesString);" +
        "return styleSheet;" +
     " };\n " +

     "var goo$zoo = function(element, stylesString) {" +
        "if (goog$userAgent$IE)" +
          "element.cssText = stylesString;" +
        "else {" +
          "var propToSet = 'innerText';" +
          "element[propToSet] = stylesString;" +
        "}" +
      "};" +
      "(function(){foo(a,b);})();",
     result);
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexSampleNoInline
  public void testComplexSampleNoInline() {
    
    String result =
    "foo=function(stylesString,opt_element){" +
        "var styleSheet=null;" +
        "if(goog$userAgent$IE){" +
          "styleSheet=0" +
        "}else{" +
          "var head=0" +
         "}" +
         "{var JSCompiler_inline_element_0=styleSheet;" +
         "var JSCompiler_inline_stylesString_1=stylesString;" +
         "if(goog$userAgent$IE){" +
           "JSCompiler_inline_element_0.cssText=" +
           "JSCompiler_inline_stylesString_1" +
         "}else{" +
           "var propToSet=goog$userAgent$WEBKIT?\"innerText\":\"innerHTML\";" +
           "JSCompiler_inline_element_0[propToSet]=" +
           "JSCompiler_inline_stylesString_1" +
         "}}" +
        "return styleSheet" +
     "}";

    testSame(
      "foo=function(stylesString,opt_element){" +
        "var styleSheet=null;" +
        "if(goog$userAgent$IE)" +
          "styleSheet=0;" +
        "else " +
          "var head=0;" +
        "" +
        "goo$zoo(styleSheet,stylesString);" +
        "return styleSheet" +
     "};" +
     "goo$zoo=function(element,stylesString){" +
        "if(goog$userAgent$IE)" +
          "element.cssText=stylesString;" +
        "else{" +
          "var propToSet=goog$userAgent$WEBKIT?\"innerText\":\"innerHTML\";" +
          "element[propToSet]=stylesString" +
        "}" +
      "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexNoVarSub
  public void testComplexNoVarSub() {
    test(
        "function foo(x){" +
          "var x;" +
          "y=x" +
        "}" +
        "foo(1)",

        "{y=1}"
        );
   }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexFunctionWithFunctionDefinition1
  public void testComplexFunctionWithFunctionDefinition1() {
    test("function f(){call(function(){return})}f()",
         "{call(function(){return})}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexFunctionWithFunctionDefinition2
  public void testComplexFunctionWithFunctionDefinition2() {
    assumeMinimumCapture = false;

    
    testSame("function f(a){call(function(){return})}f()");

    assumeMinimumCapture = true;

    test("(function(){" +
         "var f = function(a){call(function(){return a})};f()})()",
         "{{var a$$inline_1=void 0;call(function(){return a$$inline_1})}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexFunctionWithFunctionDefinition2a
  public void testComplexFunctionWithFunctionDefinition2a() {
    assumeMinimumCapture = false;

    
    testSame("(function(){" +
        "var f = function(a){call(function(){return a})};f()})()");

    assumeMinimumCapture = true;

    test("(function(){" +
         "var f = function(a){call(function(){return a})};f()})()",
         "{{var a$$inline_1=void 0;call(function(){return a$$inline_1})}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testComplexFunctionWithFunctionDefinition3
  public void testComplexFunctionWithFunctionDefinition3() {
    assumeMinimumCapture = false;

    
    testSame("function f(){var a; call(function(){return a})}f()");

    assumeMinimumCapture = true;

    test("function f(){var a; call(function(){return a})}f()",
         "{var a$$inline_1;call(function(){return a$$inline_1})}");

  }

// com.google.javascript.jscomp.InlineFunctionsTest::testDecomposePlusEquals
  public void testDecomposePlusEquals() {
    test("function f(){a=1;return 1} var x = 1; x += f()",
        "var x = 1;" +
        "var JSCompiler_temp_const$$0 = x;" +
        "{var JSCompiler_inline_result$$1; a=1;" +
        " JSCompiler_inline_result$$1=1}" +
        "x = JSCompiler_temp_const$$0 + JSCompiler_inline_result$$1;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testDecomposeFunctionExpressionInCall
  public void testDecomposeFunctionExpressionInCall() {
    test(
        "(function(map){descriptions_=map})(\n" +
           "function(){\n" +
              "var ret={};\n" +
              "ret[ONE]='a';\n" +
              "ret[TWO]='b';\n" +
              "return ret\n" +
           "}()\n" +
        ");",
        "{" +
        "var JSCompiler_inline_result$$0;" +
        "var ret$$inline_2={};\n" +
        "ret$$inline_2[ONE]='a';\n" +
        "ret$$inline_2[TWO]='b';\n" +
        "JSCompiler_inline_result$$0 = ret$$inline_2;\n" +
        "}" +
        "{" +
        "descriptions_=JSCompiler_inline_result$$0;" +
        "}"
        );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineConstructor1
  public void testInlineConstructor1() {
    test("function f() {} function _g() {f.call(this)}",
         "function _g() {void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineConstructor2
  public void testInlineConstructor2() {
    test("function f() {} f.prototype.a = 0; function _g() {f.call(this)}",
         "function f() {} f.prototype.a = 0; function _g() {void 0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineConstructor3
  public void testInlineConstructor3() {
    test("function f() {x.call(this)} f.prototype.a = 0;" +
         "function _g() {f.call(this)}",
         "function f() {x.call(this)} f.prototype.a = 0;" +
         "function _g() {{x.call(this)}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineConstructor4
  public void testInlineConstructor4() {
    test("function f() {x.call(this)} f.prototype.a = 0;" +
         "function _g() {var t = f.call(this)}",
         "function f() {x.call(this)} f.prototype.a = 0;" +
         "function _g() {var t; {x.call(this); t = void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionInlining1
  public void testFunctionExpressionInlining1() {
    test("(function(){})()",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionInlining2
  public void testFunctionExpressionInlining2() {
    test("(function(){foo()})()",
         "{foo()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionInlining3
  public void testFunctionExpressionInlining3() {
    test("var a = (function(){return foo()})()",
         "var a = foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionInlining4
  public void testFunctionExpressionInlining4() {
    test("var a; a = 1 + (function(){return foo()})()",
         "var a; a = 1 + foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining1
  public void testFunctionExpressionCallInlining1() {
    test("(function(){}).call(this)",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining2
  public void testFunctionExpressionCallInlining2() {
    test("(function(){foo(this)}).call(this)",
         "{foo(this)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining3
  public void testFunctionExpressionCallInlining3() {
    test("var a = (function(){return foo(this)}).call(this)",
         "var a = foo(this)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining4
  public void testFunctionExpressionCallInlining4() {
    test("var a; a = 1 + (function(){return foo(this)}).call(this)",
         "var a; a = 1 + foo(this)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining5
  public void testFunctionExpressionCallInlining5() {
    test("a:(function(){return foo()})()",
         "a:foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining6
  public void testFunctionExpressionCallInlining6() {
    test("a:(function(){return foo()}).call(this)",
         "a:foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining7
  public void testFunctionExpressionCallInlining7() {
    test("a:(function(){})()",
         "a:void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining8
  public void testFunctionExpressionCallInlining8() {
    test("a:(function(){}).call(this)",
         "a:void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining9
  public void testFunctionExpressionCallInlining9() {
    
    test("(function foo(){})()",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining10
  public void testFunctionExpressionCallInlining10() {
    
    test("(function foo(){}).call(this)",
         "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11a
  public void testFunctionExpressionCallInlining11a() {
    
    test("((function(){return function(){foo()}})())();", "{foo()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11b
  public void testFunctionExpressionCallInlining11b() {
    assumeMinimumCapture = false;
    
    testSame("((function(){var a; return function(){foo()}})())();");

    assumeMinimumCapture = true;
    test(
        "((function(){var a; return function(){foo()}})())();",

        "{var JSCompiler_inline_result$$0;" +
        "var a$$inline_2;" +
        "JSCompiler_inline_result$$0=function(){foo()};}" +
        "JSCompiler_inline_result$$0()");

  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11c
  public void testFunctionExpressionCallInlining11c() {
    
    assumeMinimumCapture = false;
    testSame("function _x() {" +
         "  ((function(){return function(){foo()}})())();" +
         "}");

    assumeMinimumCapture = true;
    test(
        "function _x() {" +
        "  ((function(){return function(){foo()}})())();" +
        "}",
        "function _x() {" +
        "  {foo()}" +
        "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11d
  public void testFunctionExpressionCallInlining11d() {
    
    
    assumeMinimumCapture = false;
    testSame("function _x() {" +
         "  eval();" +
         "  ((function(){return function(){foo()}})())();" +
         "}");

    assumeMinimumCapture = true;
    test(
        "function _x() {" +
        "  eval();" +
        "  ((function(){return function(){foo()}})())();" +
        "}",
        "function _x() {" +
        "  eval();" +
        "  {foo()}" +
        "}");

  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining11e
  public void testFunctionExpressionCallInlining11e() {
    
    
    assumeMinimumCapture = false;
    testSame("function _x() {" +
         "  eval();" +
         "  ((function(a){return function(){foo()}})())();" +
         "}");

    assumeMinimumCapture = true;
    test("function _x() {" +
        "  eval();" +
        "  ((function(a){return function(){foo()}})())();" +
        "}",
        "function _x() {" +
        "  eval();" +
        "  {foo();}" +
        "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionCallInlining12
  public void testFunctionExpressionCallInlining12() {
    
    testSame("(function foo(){foo()})()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionOmega
  public void testFunctionExpressionOmega() {
    
    test("(function (f){f(f)})(function(f){f(f)})",
         "{var f$$inline_1=function(f$$1){f$$1(f$$1)};" +
          "{{f$$inline_1(f$$inline_1)}}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining1
  public void testLocalFunctionInlining1() {
    test("function _f(){ function g() {} g() }",
         "function _f(){ void 0 }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining2
  public void testLocalFunctionInlining2() {
    test("function _f(){ function g() {foo(); bar();} g() }",
         "function _f(){ {foo(); bar();} }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining3
  public void testLocalFunctionInlining3() {
    test("function _f(){ function g() {foo(); bar();} g() }",
         "function _f(){ {foo(); bar();} }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining4
  public void testLocalFunctionInlining4() {
    test("function _f(){ function g() {return 1} return g() }",
         "function _f(){ return 1 }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining5
  public void testLocalFunctionInlining5() {
    testSame("function _f(){ function g() {this;} g() }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInlining6
  public void testLocalFunctionInlining6() {
    testSame("function _f(){ function g() {this;} return g; }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInliningOnly1
  public void testLocalFunctionInliningOnly1() {
    this.allowGlobalFunctionInlining = true;
    test("function f(){} f()", "void 0;");
    this.allowGlobalFunctionInlining = false;
    testSame("function f(){} f()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInliningOnly2
  public void testLocalFunctionInliningOnly2() {
    this.allowGlobalFunctionInlining = false;
    testSame("function f(){} f()");

    test("function f(){ function g() {return 1} return g() }; f();",
         "function f(){ return 1 }; f();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInliningOnly3
  public void testLocalFunctionInliningOnly3() {
    this.allowGlobalFunctionInlining = false;
    testSame("function f(){} f()");

    test("(function(){ function g() {return 1} return g() })();",
         "(function(){ return 1 })();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testLocalFunctionInliningOnly4
  public void testLocalFunctionInliningOnly4() {
    this.allowGlobalFunctionInlining = false;
    testSame("function f(){} f()");

    test("(function(){ return (function() {return 1})() })();",
         "(function(){ return 1 })();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineWithThis1
  public void testInlineWithThis1() {
    assumeStrictThis = false;
    
    
    testSame("function f(){} f.call();");
    testSame("function f(){this} f.call();");

    assumeStrictThis = true;
    
    test("function f(){} f.call();", "{}");
    test("function f(){this} f.call();",
         "{void 0;}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineWithThis2
  public void testInlineWithThis2() {
    
    assumeStrictThis = false;
    test("function f(){} f.call(this);", "void 0");

    assumeStrictThis = true;
    test("function f(){} f.call(this);", "void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineWithThis3
  public void testInlineWithThis3() {
    assumeStrictThis = false;
    
    
    testSame("function f(){} f.call([]);");

    assumeStrictThis = true;
    
    test("function f(){} f.call([]);", "{}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineWithThis4
  public void testInlineWithThis4() {
    assumeStrictThis = false;
    
    
    testSame("function f(){} f.call(new g);");

    assumeStrictThis = true;
    
    test("function f(){} f.call(new g);",
         "{var JSCompiler_inline_this_0=new g}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineWithThis5
  public void testInlineWithThis5() {
    assumeStrictThis = false;
    
    
    testSame("function f(){} f.call(g());");

    assumeStrictThis = true;
    
    test("function f(){} f.call(g());",
         "{var JSCompiler_inline_this_0=g()}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineWithThis6
  public void testInlineWithThis6() {
    assumeStrictThis = false;
    
    
    testSame("function f(){this} f.call(new g);");

    assumeStrictThis = true;
    
    test("function f(){this} f.call(new g);",
         "{var JSCompiler_inline_this_0=new g;JSCompiler_inline_this_0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineWithThis7
  public void testInlineWithThis7() {
    assumeStrictThis = true;
    
    test("function f(a){a=1;this} f.call();",
         "{var a$$inline_1=void 0; a$$inline_1=1; void 0;}");
    test("function f(a){a=1;this} f.call(x, x);",
         "{var a$$inline_1=x; a$$inline_1=1; x;}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testFunctionExpressionYCombinator
  public void testFunctionExpressionYCombinator() {
    assumeMinimumCapture = false;
    testSame(
        "var factorial = ((function(M) {\n" +
        "      return ((function(f) {\n" +
        "                 return M(function(arg) {\n" +
        "                            return (f(f))(arg);\n" +
        "                            })\n" +
        "               })\n" +
        "              (function(f) {\n" +
        "                 return M(function(arg) {\n" +
        "                            return (f(f))(arg);\n" +
        "                           })\n" +
        "                 }));\n" +
        "     })\n" +
        "    (function(f) {\n" +
        "       return function(n) {\n" +
        "        if (n === 0)\n" +
        "          return 1;\n" +
        "        else\n" +
        "          return n * f(n - 1);\n" +
        "       };\n" +
        "     }));\n" +
        "\n" +
        "factorial(5)\n");

    assumeMinimumCapture = true;
    test(
        "var factorial = ((function(M) {\n" +
        "      return ((function(f) {\n" +
        "                 return M(function(arg) {\n" +
        "                            return (f(f))(arg);\n" +
        "                            })\n" +
        "               })\n" +
        "              (function(f) {\n" +
        "                 return M(function(arg) {\n" +
        "                            return (f(f))(arg);\n" +
        "                           })\n" +
        "                 }));\n" +
        "     })\n" +
        "    (function(f) {\n" +
        "       return function(n) {\n" +
        "        if (n === 0)\n" +
        "          return 1;\n" +
        "        else\n" +
        "          return n * f(n - 1);\n" +
        "       };\n" +
        "     }));\n" +
        "\n" +
        "factorial(5)\n",
        "var factorial;\n" +
        "{\n" +
        "var M$$inline_6 = function(f$$2) {\n" +
        "  return function(n){if(n===0)return 1;else return n*f$$2(n-1)}\n" +
        "};\n" +
        "{\n" +
        "var f$$inline_1=function(f$$inline_9){\n" +
        "  return M$$inline_6(\n" +
        "    function(arg$$inline_10){\n" +
        "      return f$$inline_9(f$$inline_9)(arg$$inline_10)\n" +
        "     })\n" +
        "};\n" +
        "factorial=M$$inline_6(\n" +
        "  function(arg$$inline_2){\n" +
        "    return f$$inline_1(f$$inline_1)(arg$$inline_2)\n" +
        "});\n" +
        "}\n" +
        "}" +
        "factorial(5)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testRenamePropertyFunction
  public void testRenamePropertyFunction() {
    testSame("function JSCompiler_renameProperty(x) {return x} " +
             "JSCompiler_renameProperty('foo')");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testReplacePropertyFunction
  public void testReplacePropertyFunction() {
    
    
    test("function f(x) {return x} " +
         "foo(window, f); f(1)",
         "function f(x) {return x} " +
         "foo(window, f); 1");
    
    
    testSame("function f(x) {return x} " +
             "new JSCompiler_ObjectPropertyString(window, f); f(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineWithClosureContainingThis
  public void testInlineWithClosureContainingThis() {
    test("(function (){return f(function(){return this})})();",
         "f(function(){return this})");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testIssue5159924a
  public void testIssue5159924a() {
    test("function f() { if (x()) return y() }\n" +
         "while(1){ var m = f() || z() }",
         "for(;1;) {" +
         "  {" +
         "    var JSCompiler_inline_result$$0;" +
         "    JSCompiler_inline_label_f_1: {" +
         "      if(x()) {" +
         "        JSCompiler_inline_result$$0 = y();" +
         "        break JSCompiler_inline_label_f_1" +
         "      }" +
         "      JSCompiler_inline_result$$0 = void 0;" +
         "    }" +
         "  }" +
         "  var m=JSCompiler_inline_result$$0 || z()" +
         "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testIssue5159924b
  public void testIssue5159924b() {
    test("function f() { if (x()) return y() }\n" +
         "while(1){ var m = f() }",
         "for(;1;){" +
         "  var m;" +
         "  {" +
         "    JSCompiler_inline_label_f_0: { " +
         "      if(x()) {" +
         "        m = y();" +
         "        break JSCompiler_inline_label_f_0" +
         "      }" +
         "      m = void 0" +
         "    }" +
         "  }" +
         "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineObject
  public void testInlineObject() {
    new StringCompare().testInlineObject();
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineObject
    public void testInlineObject() {
      allowGlobalFunctionInlining = false;
      
      
      
      
      
      test("function inner(){function f(){return g.a}(f())()}",
           "function inner(){(0,g.a)()}");
    }

// com.google.javascript.jscomp.InlineFunctionsTest::testBug4944818
  public void testBug4944818() {
    test(
        "var getDomServices_ = function(self) {\n" +
        "  if (!self.domServices_) {\n" +
        "    self.domServices_ = goog$component$DomServices.get(" +
        "        self.appContext_);\n" +
        "  }\n" +
        "\n" +
        "  return self.domServices_;\n" +
        "};\n" +
        "\n" +
        "var getOwnerWin_ = function(self) {\n" +
        "  return getDomServices_(self).getDomHelper().getWindow();\n" +
        "};\n" +
        "\n" +
        "HangoutStarter.prototype.launchHangout = function() {\n" +
        "  var self = a.b;\n" +
        "  var myUrl = new goog.Uri(getOwnerWin_(self).location.href);\n" +
        "};",
        "HangoutStarter.prototype.launchHangout = function() { " +
        "  var self$$2 = a.b;" +
        "  var JSCompiler_temp_const$$0 = goog.Uri;" +
        "  {" +
        "  var JSCompiler_inline_result$$1;" +
        "  var self$$inline_3 = self$$2;" +
        "  if (!self$$inline_3.domServices_) {" +
        "    self$$inline_3.domServices_ = goog$component$DomServices.get(" +
        "        self$$inline_3.appContext_);" +
        "  }" +
        "  JSCompiler_inline_result$$1=self$$inline_3.domServices_;" +
        "  }" +
        "  var myUrl = new JSCompiler_temp_const$$0(" +
        "      JSCompiler_inline_result$$1.getDomHelper()." +
        "          getWindow().location.href)" +
        "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testIssue423
  public void testIssue423() {
    assumeMinimumCapture = false;
    test(
        "(function($) {\n" +
        "  $.fn.multicheck = function(options) {\n" +
        "    initialize.call(this, options);\n" +
        "  };\n" +
        "\n" +
        "  function initialize(options) {\n" +
        "    options.checkboxes = $(this).siblings(':checkbox');\n" +
        "    preload_check_all.call(this);\n" +
        "  }\n" +
        "\n" +
        "  function preload_check_all() {\n" +
        "    $(this).data('checkboxes');\n" +
        "  }\n" +
        "})(jQuery)",
        "(function($){" +
        "  $.fn.multicheck=function(options$$1){" +
        "    {" +
        "     options$$1.checkboxes=$(this).siblings(\":checkbox\");" +
        "     {" +
        "       $(this).data(\"checkboxes\")" +
        "     }" +
        "    }" +
        "  }" +
        "})(jQuery)");

    assumeMinimumCapture = true;
    test(
        "(function($) {\n" +
        "  $.fn.multicheck = function(options) {\n" +
        "    initialize.call(this, options);\n" +
        "  };\n" +
        "\n" +
        "  function initialize(options) {\n" +
        "    options.checkboxes = $(this).siblings(':checkbox');\n" +
        "    preload_check_all.call(this);\n" +
        "  }\n" +
        "\n" +
        "  function preload_check_all() {\n" +
        "    $(this).data('checkboxes');\n" +
        "  }\n" +
        "})(jQuery)",
        "{var $$$inline_3=jQuery;\n" +
        "$$$inline_3.fn.multicheck=function(options$$inline_7){\n" +
        "  {options$$inline_7.checkboxes=" +
            "$$$inline_3(this).siblings(\":checkbox\");\n" +
        "  {$$$inline_3(this).data(\"checkboxes\")}" +
        "  }\n" +
        "}\n" +
        "}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testAnonymous1
  public void testAnonymous1() {
    assumeMinimumCapture = false;
    test("(function(){var a=10;(function(){var b=a;a++;alert(b)})()})();",
         "{var a$$inline_2=10;" +
         "{var b$$inline_3=a$$inline_2;" +
         "a$$inline_2++;alert(b$$inline_3)}}");

    assumeMinimumCapture = true;
    test("(function(){var a=10;(function(){var b=a;a++;alert(b)})()})();",
        "{var a$$inline_4=10;" +
        "{var b$$inline_1=a$$inline_4;" +
        "a$$inline_4++;alert(b$$inline_1)}}");
  }
