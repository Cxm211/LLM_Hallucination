boolean hasEqualParameters(ArrowType that, boolean tolerateUnknowns) {
    Node thisParam = parameters.getFirstChild();
    Node otherParam = that.parameters.getFirstChild();
    while (thisParam != null || otherParam != null) {
      if (thisParam == null || otherParam == null) {
        return false;
      }
      JSType thisParamType = thisParam.getJSType();
      JSType otherParamType = otherParam.getJSType();
      if (thisParamType != null) {
        // Both parameter lists give a type for this param, it should be equal
        if (otherParamType != null &&
            !thisParamType.checkEquivalenceHelper(
                otherParamType, tolerateUnknowns)) {
          return false;
        }
      } else {
        if (otherParamType != null) {
          return false;
        }
      }
      thisParam = thisParam.getNext();
      otherParam = otherParam.getNext();
    }
    return true;
  }