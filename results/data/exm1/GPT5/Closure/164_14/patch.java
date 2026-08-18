public boolean isSubtype(JSType other) {
    if (!(other instanceof ArrowType)) {
      return false;
    }

    ArrowType that = (ArrowType) other;

    // This is described in Draft 2 of the ES4 spec,
    // Section 3.4.7: Subtyping Function Types.

    // this.returnType <: that.returnType (covariant)
    if (!this.returnType.isSubtype(that.returnType)) {
      return false;
    }

    // that.paramType[i] <: this.paramType[i] (contravariant)
    // See comments in original code for details on required-ness handling.
    Node thisParam = parameters.getFirstChild();
    Node thatParam = that.parameters.getFirstChild();
    while (thisParam != null && thatParam != null) {
      JSType thisParamType = thisParam.getJSType();
      JSType thatParamType = thatParam.getJSType();
      if (thisParamType != null) {
        if (thatParamType == null || !thatParamType.isSubtype(thisParamType)) {
          return false;
        }
      }

      boolean thisIsVarArgs = thisParam.isVarArgs();
      boolean thatIsVarArgs = thatParam.isVarArgs();

      // don't advance if we have variable arguments
      if (!thisIsVarArgs) {
        thisParam = thisParam.getNext();
      }
      if (!thatIsVarArgs) {
        thatParam = thatParam.getNext();
      }

      // both var_args indicates the end
      if (thisIsVarArgs && thatIsVarArgs) {
        thisParam = null;
        thatParam = null;
      }
    }

    // If "that" is missing any required arguments of "this",
    // then it cannot be a supertype.
    if (thatParam == null && thisParam != null) {
      while (thisParam != null) {
        if (!thisParam.isVarArgs() && !thisParam.isOptionalArg()) {
          return false;
        }
        thisParam = thisParam.getNext();
      }
    }

    return true;
  }