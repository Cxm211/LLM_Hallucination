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

    // Handle null typeOfThis
    if (typeOfThis == null) {
      if (that.typeOfThis != null) {
        return false;
      }
    } else if (!typeOfThis.checkEquivalenceHelper(
        that.typeOfThis, tolerateUnknowns)) {
      return false;
    }
    return call.checkArrowEquivalenceHelper(that.call, tolerateUnknowns);
  }