  boolean expectCanAssignTo(NodeTraversal t, Node n, JSType rightType,
      JSType leftType, String msg) {
    if (!rightType.canAssignTo(leftType)) {
      if ((leftType.isConstructor() || leftType.isEnumType()) && (rightType.isConstructor() || rightType.isEnumType())) {
        return true;
      } else {
      mismatch(t, n, msg, rightType, leftType);
      }
      return false;
    }
    return true;
  }