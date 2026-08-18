  private void visitNew(NodeTraversal t, Node n) {
    Node constructor = n.getFirstChild();
    JSType type = getJSType(constructor).restrictByNotNullOrUndefined();

    // Try to interpret the constructor as a function first.
    FunctionType fnType = type.toMaybeFunctionType();
    if (fnType != null) {
      // Always type-check the parameters if we know it's a function.
      visitParameterList(t, n, fnType);
      // If it's a real constructor, set the instance type. Otherwise, just ensure it's typed.
      if (fnType.isConstructor()) {
        ensureTyped(t, n, fnType.getInstanceType());
      } else {
        ensureTyped(t, n);
      }
      return;
    }

    // If the type information is empty or unknown, don't report an error.
    if (type.isEmptyType() || type.isUnknownType()) {
      ensureTyped(t, n);
      return;
    }

    // Otherwise, it's not a constructor.
    report(t, n, NOT_A_CONSTRUCTOR);
    ensureTyped(t, n);
  }