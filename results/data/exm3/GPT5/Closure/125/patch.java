private void visitNew(NodeTraversal t, Node n) {
    Node constructor = n.getFirstChild();
    JSType type = getJSType(constructor).restrictByNotNullOrUndefined();

    // Handle union types that may include constructors.
    if (type.isUnionType()) {
      UnionType ut = type.toMaybeUnionType();
      FunctionType ctorType = null;
      for (JSType alt : ut.getAlternates()) {
        FunctionType f = alt.toMaybeFunctionType();
        if (f != null && f.isConstructor()) {
          ctorType = f;
          break;
        }
      }
      if (ctorType != null) {
        visitParameterList(t, n, ctorType);
        ensureTyped(t, n, ctorType.getInstanceType());
        return;
      }
    }

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