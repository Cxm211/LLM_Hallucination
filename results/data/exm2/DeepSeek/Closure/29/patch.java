private boolean isInlinableObject(List<Reference> refs) {
  boolean ret = false;
  Set<String> definedProps = new HashSet<>();
  // First pass: collect defined properties and find acceptable object literal assignments.
  for (Reference ref : refs) {
    Node name = ref.getNode();
    Node parent = ref.getParent();
    Node gramps = ref.getGrandparent();

    if (parent.isGetProp()) {
      // Skip for now, will check in second pass.
      continue;
    }
    if (parent.isSetProp()) {
      // Property write: add property name to definedProps.
      String propName = parent.getLastChild().getString();
      definedProps.add(propName);
      continue;
    }
    if (!isVarOrAssignExprLhs(name)) {
      return false;
    }
    Node val = ref.getAssignedValue();
    if (val == null) {
      continue;
    }
    if (!val.isObjectLit()) {
      return false;
    }
    // Self-referential check
    for (Node child = val.getFirstChild(); child != null; child = child.getNext()) {
      if (child.isGetterDef() || child.isSetterDef()) {
        return false;
      }
      Node childVal = child.getFirstChild();
      for (Reference t : refs) {
        Node refNode = t.getParent();
        while (!NodeUtil.isStatementBlock(refNode)) {
          if (refNode == childVal) {
            return false;
          }
          refNode = refNode.getParent();
        }
      }
    }
    ret = true;
    // Add properties from this object literal.
    for (Node child = val.getFirstChild(); child != null; child = child.getNext()) {
      String propName = child.getString();
      definedProps.add(propName);
    }
  }
  // Second pass: check property accesses.
  for (Reference ref : refs) {
    Node parent = ref.getParent();
    if (parent.isGetProp()) {
      Node gramps = ref.getGrandparent();
      Preconditions.checkState(parent.getFirstChild() == ref.getNode());
      // A call target maybe using the object as a 'this' value.
      if (gramps.isCall() && gramps.getFirstChild() == parent) {
        return false;
      }
      String propName = parent.getLastChild().getString();
      if (!definedProps.contains(propName)) {
        return false;
      }
    }
  }
  return ret;
}