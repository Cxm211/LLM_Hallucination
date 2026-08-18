  private void visitNew(NodeTraversal t, Node n) {
    Node constructor = n.getFirstChild();
    JSType type = getJSType(constructor).restrictByNotNullOrUndefined();
    FunctionType fnType = type.toMaybeFunctionType();
    if (fnType != null) {
      // Allow 'new' with any function type. If it's a real constructor,
      // use its instance type; otherwise, type the result but leave it unknown.
      visitParameterList(t, n, fnType);
      if (fnType.isConstructor()) {
        ensureTyped(t, n, fnType.getInstanceType());
      } else {
        ensureTyped(t, n);
      }
    } else if (type.isUnknownType()) {
      // Unknown types are allowed, but we can't infer a specific instance type.
      ensureTyped(t, n);
    } else {
      report(t, n, NOT_A_CONSTRUCTOR);
      ensureTyped(t, n);
    }
  }