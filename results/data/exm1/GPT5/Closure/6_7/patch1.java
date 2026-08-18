boolean expectCanAssignTo(NodeTraversal t, Node n, JSType rightType, JSType leftType, String msg) {
    // The NoType check is a hack to make typedefs work OK, consistent with property assignments.
    if (!leftType.isNoType() && !rightType.canAssignTo(leftType)) {
      if ((leftType.isConstructor() || leftType.isEnumType()) && (rightType.isConstructor() || rightType.isEnumType())) {
        registerMismatch(rightType, leftType, null);
      } else {
        mismatch(t, n, msg, rightType, leftType);
      }
      return false;
    }
    return true;
  }