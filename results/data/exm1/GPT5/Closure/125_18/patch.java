  private void visitNew(NodeTraversal t, Node n) {
    Node constructor = n.getFirstChild();
    JSType type = getJSType(constructor).restrictByNotNullOrUndefined();

    // Allow 'new' with any function type (not just declared constructors),
    // as JavaScript permits constructing ordinary functions.
    FunctionType fnType = type.toMaybeFunctionType();
    if (fnType != null || type.isEmptyType() || type.isUnknownType()) {
      if (fnType != null) {
        visitParameterList(t, n, fnType);
        JSType instanceType = fnType.getInstanceType();
        if (instanceType != null) {
          ensureTyped(t, n, instanceType);
        } else {
          // For non-constructor functions used with 'new', fall back to unknown.
          ensureTyped(t, n);
        }
      } else {
        ensureTyped(t, n);
      }
    } else {
      report(t, n, NOT_A_CONSTRUCTOR);
      ensureTyped(t, n);
    }
  }