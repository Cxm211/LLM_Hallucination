  private void visitNew(NodeTraversal t, Node n) {
    Node constructor = n.getFirstChild();
    JSType type = getJSType(constructor).restrictByNotNullOrUndefined();
    FunctionType fnType = type.toMaybeFunctionType();
    if (type.isConstructor() || type.isEmptyType() || type.isUnknownType() || fnType != null) {
      if (fnType != null) {
        visitParameterList(t, n, fnType);
        if (fnType.isConstructor()) {
          ensureTyped(t, n, fnType.getInstanceType());
        } else {
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