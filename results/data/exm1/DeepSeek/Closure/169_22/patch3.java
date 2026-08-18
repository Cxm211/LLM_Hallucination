boolean checkFunctionEquivalenceHelper(
    FunctionType that, boolean tolerateUnknowns) {
  if (isConstructor()) {
    if (that.isConstructor()) {
      return this == that;
    }
    return false;
  }
  if (isInterface()) {
    if (that.isInterface()) {
      return getReferenceName().equals(that.getReferenceName());
    }
    return false;
  }
  if (that.isInterface()) {
    return false;
  }
  return typeOfThis.checkEquivalenceHelper(
      that.typeOfThis, tolerateUnknowns) &&
      call.checkArrowEquivalenceHelper(that.call, tolerateUnknowns);
}