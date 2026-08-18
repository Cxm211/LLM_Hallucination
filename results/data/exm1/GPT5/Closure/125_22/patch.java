  private void visitNew(NodeTraversal t, Node n) {
    Node constructor = n.getFirstChild();
    JSType type = getJSType(constructor).restrictByNotNullOrUndefined();

    // Try to interpret the constructor expression as a function first.
    FunctionType fnType = type.toMaybeFunctionType();
    if (fnType != null) {
      // Even if it's not explicitly marked as a constructor, in JS any function
      // can be called with 'new'. Infer parameters and type accordingly.
      visitParameterList(t, n, fnType);
      ObjectType instanceType = fnType.getInstanceType();
      if (instanceType != null) {
        ensureTyped(t, n, instanceType);
      } else {
        // If we can't determine an instance type, fall back to unknown.
        ensureTyped(t, n);
      }
      return;
    }

    if (type.isEmptyType() || type.isUnknownType() || type.isConstructor()) {
      // Handle empty/unknown/explicit constructor types as before.
      FunctionType ctorFnType = type.toMaybeFunctionType();
      if (ctorFnType != null) {
        visitParameterList(t, n, ctorFnType);
        ensureTyped(t, n, ctorFnType.getInstanceType());
      } else {
        ensureTyped(t, n);
      }
    } else {
      report(t, n, NOT_A_CONSTRUCTOR);
      ensureTyped(t, n);
    }
  }