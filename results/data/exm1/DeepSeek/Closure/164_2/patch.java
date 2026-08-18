  public boolean isSubtype(JSType other) {
    if (!(other instanceof ArrowType)) {
      return false;
    }

    ArrowType that = (ArrowType) other;

    if (!this.returnType.isSubtype(that.returnType)) {
      return false;
    }

    Node thisParam = parameters.getFirstChild();
    Node thatParam = that.parameters.getFirstChild();
    while (thisParam != null && thatParam != null) {
      JSType thisParamType = thisParam.getJSType();
      JSType thatParamType = thatParam.getJSType();
      if (thisParamType != null) {
        if (thatParamType == null ||
            !thatParamType.isSubtype(thisParamType)) {
          return false;
        }
      }

      boolean thisIsVarArgs = thisParam.isVarArgs();
      boolean thatIsVarArgs = thatParam.isVarArgs();

      if (!thisIsVarArgs) {
        thisParam = thisParam.getNext();
      }
      if (!thatIsVarArgs) {
        thatParam = thatParam.getNext();
      }

      if (thisIsVarArgs && thatIsVarArgs) {
        thisParam = null;
        thatParam = null;
      }
    }

    // If this has extra parameters, it cannot be a subtype unless they are all varargs.
    while (thisParam != null) {
      if (!thisParam.isVarArgs()) {
        return false;
      }
      thisParam = thisParam.getNext();
    }

    return true;
  }