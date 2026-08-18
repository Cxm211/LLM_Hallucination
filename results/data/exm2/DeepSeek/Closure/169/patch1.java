  boolean checkArrowEquivalenceHelper(
      ArrowType that, boolean tolerateUnknowns) {
    if (!returnType.checkEquivalenceHelper(that.returnType, tolerateUnknowns)) {
      return false;
    }
    return hasEqualParameters(that, tolerateUnknowns);
  }