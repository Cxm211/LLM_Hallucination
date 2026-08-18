private void visitGetProp(NodeTraversal t, Node n, Node parent) {
    // obj.prop or obj.method()
    // Lots of types can appear on the left, a call to a void function can
    // never be on the left. getPropertyType will decide what is acceptable
    // and what isn't.
    Node property = n.getLastChild();
    Node objNode = n.getFirstChild();
    JSType childType = getJSType(objNode);

    boolean isLhsOfAssign = parent.isAssign() && parent.getFirstChild() == n;

    if (childType.isDict()) {
      report(t, property, TypeValidator.ILLEGAL_PROPERTY_ACCESS, "'.'", "dict");
    }

    // Always check for null/undefined receiver to report appropriate errors.
    if (validator.expectNotNullOrUndefined(t, n, childType,
        "No properties on this expression", getNativeType(OBJECT_TYPE))) {
      // For left-hand side of assignments, skip property existence checks,
      // but still allow the null/undefined check above to run.
      if (!isLhsOfAssign) {
        checkPropertyAccess(childType, property.getString(), t, n);
      }
    }
    ensureTyped(t, n);
  }