boolean hasEqualParameters(ArrowType that, boolean tolerateUnknowns) {
    Node thisParam = parameters.getFirstChild();
    Node otherParam = that.parameters.getFirstChild();
    while (thisParam != null && otherParam != null) {
      JSType thisParamType = thisParam.getJSType();
      JSType otherParamType = otherParam.getJSType();
      if (thisParamType != null) {
        if (otherParamType != null) {
          if (!thisParamType.checkEquivalenceHelper(
                  otherParamType, tolerateUnknowns)) {
            return false;
          }
        } else if (!tolerateUnknowns) {
          return false;
        }
      } else {
        if (otherParamType != null && !tolerateUnknowns) {
          return false;
        }
      }
      thisParam = thisParam.getNext();
      otherParam = otherParam.getNext();
    }
    return thisParam == otherParam;
  }