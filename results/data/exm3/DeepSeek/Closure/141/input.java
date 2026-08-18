// buggy function
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

// trigger testcase
// com/google/javascript/jscomp/ExpresssionDecomposerTest.java::testCanExposeExpression2
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
        DecompositionType.MOVABLE, "function (){ return foo();}", "foo");

    helperCanExposeExpression(
        DecompositionType.MOVABLE, "x = foo() && 1", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "x = foo() || 1", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "x = foo() ? 0 : 1", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "(function(a){b = a})(foo())", "foo");
  }

// com/google/javascript/jscomp/ExpresssionDecomposerTest.java::testCanExposeExpression7
public void testCanExposeExpression7() {
    // Verify calls to anonymous function are movable.
    helperCanExposeAnonymousFunctionExpression(
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

// com/google/javascript/jscomp/InlineFunctionsTest.java::testDecomposeAnonymousInCall
public void testDecomposeAnonymousInCall() {
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
        "var JSCompiler_inline_result_0;" +
        "var ret$$inline_2={};\n" +
        "ret$$inline_2[ONE]='a';\n" +
        "ret$$inline_2[TWO]='b';\n" +
        "JSCompiler_inline_result_0 = ret$$inline_2;\n" +
        "}" +
        "{" +
        "descriptions_=JSCompiler_inline_result_0;" +
        "}"
        );
  }

// com/google/javascript/jscomp/PureFunctionIdentifierTest.java::testCallFunctionFOrG
public void testCallFunctionFOrG() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){ (f || g)() }\n" +
        "h()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f || g)", "h"));
  }

// com/google/javascript/jscomp/PureFunctionIdentifierTest.java::testCallFunctionFOrGViaHook
public void testCallFunctionFOrGViaHook() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){ (false ? f : g)() }\n" +
        "h()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f : g)", "h"));
  }

// com/google/javascript/jscomp/PureFunctionIdentifierTest.java::testCallFunctionFOrGViaHookWithSideEffects
public void testCallFunctionFOrGViaHookWithSideEffects() throws Exception {
    String source = "var x = 0;\n" +
        "function f(){x = 10}\n" +
        "function g(){}\n" +
        "function h(){ (false ? f : g)() }\n" +
        "function i(){ (false ? g : f)() }\n" +
        "function j(){ (false ? f : f)() }\n" +
        "function k(){ (false ? g : g)() }\n" +
        "h(); i(); j(); k()";

    checkMarkedCalls(source, ImmutableList.<String>of("(g : g)", "k"));
  }

// com/google/javascript/jscomp/PureFunctionIdentifierTest.java::testCallFunctionFOrGWithSideEffects
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

// com/google/javascript/jscomp/PureFunctionIdentifierTest.java::testCallFunctionForGorH
public void testCallFunctionForGorH() throws Exception {
    String source = "function f(){}\n" +
        "function g(){}\n" +
        "function h(){}\n" +
        "function i(){ (false ? f : (g || h))() }\n" +
        "i()";

    checkMarkedCalls(source, ImmutableList.<String>of("(f : (g || h))", "i"));
  }
