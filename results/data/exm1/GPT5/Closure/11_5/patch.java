  private void visitGetProp(NodeTraversal t, Node n, Node parent) {
    // obj.prop or obj.method()
    // Lots of types can appear on the left, a call to a void function can
    // never be on the left. getPropertyType will decide what is acceptable
    // and what isn't.
    Node property = n.getLastChild();
    Node objNode = n.getFirstChild();
    JSType childType = getJSType(objNode);

    if (childType.isDict()) {
      report(t, property, TypeValidator.ILLEGAL_PROPERTY_ACCESS, "'.'", "dict");
    } else if (n.getJSType() != null && parent.isAssign() && parent.getFirstChild() == n) {
      // If this GETPROP is the left side of an assignment, we may be defining
      // a new property; skip access checks in that case.
      return;
    } else if (validator.expectNotNullOrUndefined(t, n, childType,
        "No properties on this expression", getNativeType(OBJECT_TYPE))) {
      checkPropertyAccess(childType, property.getString(), t, n);
    }
    ensureTyped(t, n);
  }