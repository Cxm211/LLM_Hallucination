  boolean hasEqualParameters(ArrowType that, boolean tolerateUnknowns) {
    Node thisParam = parameters.getFirstChild();
    Node otherParam = that.parameters.getFirstChild();
    while (thisParam != null && otherParam != null) {
      JSType thisParamType = thisParam.getJSType();
      JSType otherParamType = otherParam.getJSType();
      if (thisParamType != null && otherParamType != null) {
        if (!thisParamType.checkEquivalenceHelper(
                otherParamType, tolerateUnknowns)) {
          return false;
        }
      } else if (thisParamType != null || otherParamType != null) {
        return false;
      }
      thisParam = thisParam.getNext();
      otherParam = otherParam.getNext();
    }
    // One of the parameters is null, so the types are only equal if both
    // parameter lists are null (they are equal).
    return thisParam == otherParam;
  }