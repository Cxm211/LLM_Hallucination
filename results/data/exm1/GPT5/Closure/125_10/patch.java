// buggy code
  private void visitNew(NodeTraversal t, Node n) {
    Node constructor = n.getFirstChild();
    JSType type = getJSType(constructor).restrictByNotNullOrUndefined();
    FunctionType fnType = type.toMaybeFunctionType();

    if (fnType != null) {
      if (fnType.isConstructor()) {
        visitParameterList(t, n, fnType);
        ensureTyped(t, n, fnType.getInstanceType());
      } else {
        report(t, n, CANNOT_INSTANTIATE_NON_CONSTRUCTOR);
        ensureTyped(t, n);
      }
    } else if (type.isFunctionType()) {
      // The expression is a function (or may be a function), but not a constructor.
      report(t, n, CANNOT_INSTANTIATE_NON_CONSTRUCTOR);
      ensureTyped(t, n);
    } else if (type.isEmptyType() || type.isUnknownType()) {
      // Unknown/empty types: be permissive in loose type checking.
      ensureTyped(t, n);
    } else {
      report(t, n, NOT_A_CONSTRUCTOR);
      ensureTyped(t, n);
    }
  }