  private void visitNew(NodeTraversal t, Node n) {
    Node constructor = n.getFirstChild();
    JSType type = getJSType(constructor).restrictByNotNullOrUndefined();
    FunctionType fnType = type.toMaybeFunctionType();
    if (fnType != null || type.isEmptyType() || type.isUnknownType()) {
      if (fnType != null) {
        visitParameterList(t, n, fnType);
        if (fnType.isConstructor()) {
          ensureTyped(t, n, fnType.getInstanceType());
        } else {
          ensureTyped(t, n);
        }
      } else {
        // Unknown or empty type: still type the node to avoid spurious errors.
        ensureTyped(t, n);
      }
    } else {
      report(t, n, NOT_A_CONSTRUCTOR);
      ensureTyped(t, n);
    }
  }