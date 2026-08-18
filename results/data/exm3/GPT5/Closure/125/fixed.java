// ===== FIXED com.google.javascript.jscomp.TypeCheck :: visitNew(NodeTraversal, Node) [lines 1656-1671] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-125-fixed/src/com/google/javascript/jscomp/TypeCheck.java =====
  private void visitNew(NodeTraversal t, Node n) {
    Node constructor = n.getFirstChild();
    JSType type = getJSType(constructor).restrictByNotNullOrUndefined();
    if (type.isConstructor() || type.isEmptyType() || type.isUnknownType()) {
      FunctionType fnType = type.toMaybeFunctionType();
      if (fnType != null && fnType.hasInstanceType()) {
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
