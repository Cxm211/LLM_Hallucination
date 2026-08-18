  private void visitNew(NodeTraversal t, Node n) {
    Node constructor = n.getFirstChild();
    JSType type = getJSType(constructor).restrictByNotNullOrUndefined();
    if (type.isConstructor() || type.isEmptyType() || type.isUnknownType()) {
      FunctionType fnType = type.toMaybeFunctionType();
      if (fnType != null) {
        visitParameterList(t, n, fnType);
        ensureTyped(t, n, fnType.getInstanceType());
      } else {
        ensureTyped(t, n);
      }
    } else {
      report(t, n, NOT_A_CONSTRUCTOR);
      ensureTyped(t, n);
    }
  }

// trigger testcase
public void testIssue1002() throws Exception {
    testTypes(
        "/** @interface */" +
        "var I = function() {};" +
        "/** @constructor @implements {I} */" +
        "var A = function() {};" +
        "/** @constructor @implements {I} */" +
        "var B = function() {};" +
        "var f = function() {" +
        "  if (A === B) {" +
        "    new B();" +
        "  }" +
        "};");
  }
