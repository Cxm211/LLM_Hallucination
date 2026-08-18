  private void visitNew(NodeTraversal t, Node n) {
    Node constructor = n.getFirstChild();
    JSType type = getJSType(constructor).restrictByNotNullOrUndefined();

    boolean valid = type.isConstructor() || type.isEmptyType() || type.isUnknownType();
    if (!valid && type.isUnionType()) {
      UnionType ut = type.toMaybeUnionType();
      for (JSType alt : ut.getAlternates()) {
        if (alt.isConstructor()) {
          valid = true;
          break;
        }
      }
    }

    if (valid) {
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