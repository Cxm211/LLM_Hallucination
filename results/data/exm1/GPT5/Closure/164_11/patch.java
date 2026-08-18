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
    //
    // If this.paramType[i] is required,
    // then that.paramType[i] is required.
    //
    // In practice, we allow the case where a function with fewer parameters
    // is a subtype of a function with more parameters (pretend extra args
    // are ignored). So, g < f when g has fewer params than f, but not the
    // reverse.
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

    // If "this" still has required parameters that "that" doesn't,
    // then "that" can't be a supertype.
    while (thisParam != null) {
      if (!thisParam.isOptionalArg() && !thisParam.isVarArgs()) {
        return false;
      }
      thisParam = thisParam.getNext();
    }

    return true;
  }