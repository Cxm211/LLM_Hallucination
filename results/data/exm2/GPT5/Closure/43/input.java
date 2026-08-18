  private JSType getNativeType(JSTypeNative nativeType) {
    return typeRegistry.getNativeType(nativeType);
  }

    public void visit(NodeTraversal t, Node n, Node parent) {
      inputId = t.getInputId();
      attachLiteralTypes(t, n);

      switch (n.getType()) {
        case Token.CALL:
          checkForClassDefiningCalls(t, n, parent);
          checkForCallingConventionDefiningCalls(n, delegateCallingConventions);
          break;

        case Token.FUNCTION:
          if (t.getInput() == null || !t.getInput().isExtern()) {
            nonExternFunctions.add(n);
          }

          // Hoisted functions are handled during pre-traversal.
          if (!NodeUtil.isHoistedFunctionDeclaration(n)) {
            defineFunctionLiteral(n, parent);
          }
          break;

        case Token.ASSIGN:
          // Handle initialization of properties.
          Node firstChild = n.getFirstChild();
          if (firstChild.isGetProp() &&
              firstChild.isQualifiedName()) {
            maybeDeclareQualifiedName(t, n.getJSDocInfo(),
                firstChild, n, firstChild.getNext());
          }
          break;

        case Token.CATCH:
          defineCatch(n, parent);
          break;

        case Token.VAR:
          defineVar(n, parent);
          break;

        case Token.GETPROP:
          // Handle stubbed properties.
          if (parent.isExprResult() &&
              n.isQualifiedName()) {
            maybeDeclareQualifiedName(t, n.getJSDocInfo(), n, parent, null);
          }
          break;
      }

      // Analyze any @lends object literals in this statement.
    }

    private void attachLiteralTypes(NodeTraversal t, Node n) {
      switch (n.getType()) {
        case Token.NULL:
          n.setJSType(getNativeType(NULL_TYPE));
          break;

        case Token.VOID:
          n.setJSType(getNativeType(VOID_TYPE));
          break;

        case Token.STRING:
          // Defer keys to the Token.OBJECTLIT case
          if (!NodeUtil.isObjectLitKey(n, n.getParent())) {
            n.setJSType(getNativeType(STRING_TYPE));
          }
          break;

        case Token.NUMBER:
          n.setJSType(getNativeType(NUMBER_TYPE));
          break;

        case Token.TRUE:
        case Token.FALSE:
          n.setJSType(getNativeType(BOOLEAN_TYPE));
          break;

        case Token.REGEXP:
          n.setJSType(getNativeType(REGEXP_TYPE));
          break;

        case Token.OBJECTLIT:
            defineObjectLiteral(n);
          break;

          // NOTE(nicksantos): If we ever support Array tuples,
          // we will need to put ARRAYLIT here as well.
      }
    }

// trigger testcase
public void testLends10() throws Exception {
    testTypes(
        "function defineClass(x) { return function() {}; } " +
        "/** @constructor */" +
        "var Foo = defineClass(" +
        "    /** @lends {Foo.prototype} */ ({/** @type {number} */ bar: 1}));" +
        "/** @return {string} */ function f() { return (new Foo()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

public void testLends11() throws Exception {
    testTypes(
        "function defineClass(x, y) { return function() {}; } " +
        "/** @constructor */" +
        "var Foo = function() {};" +
        "/** @return {*} */ Foo.prototype.bar = function() { return 3; };" +
        "/**\n" +
        " * @constructor\n" +
        " * @extends {Foo}\n" +
        " */\n" +
        "var SubFoo = defineClass(Foo, " +
        "    /** @lends {SubFoo.prototype} */ ({\n" +
        "      /** @return {number} */ bar: function() { return 3; }}));" +
        "/** @return {string} */ function f() { return (new SubFoo()).bar(); }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }
