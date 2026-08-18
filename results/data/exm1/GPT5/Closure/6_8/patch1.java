boolean expectCanAssignTo(NodeTraversal t, Node n, JSType rightType,
      JSType leftType, String msg) {
    if (!rightType.canAssignTo(leftType)) {
      if ((leftType.isConstructor() || leftType.isEnumType()) && (rightType.isConstructor() || rightType.isEnumType())) {
        // Register the mismatch and also report it to ensure errors are surfaced.
        registerMismatch(rightType, leftType, null);
        mismatch(t, n, msg, rightType, leftType);
      } else {
      mismatch(t, n, msg, rightType, leftType);
      }
      return false;
    }
    return true;
  }