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
    // See detailed notes in the original comments about required-ness and var_args.
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

      // Special-case: If the supertype has var_args, do not allow a subtype
      // to require parameters that are matched against the supertype's var_args
      // unless the subtype is also var_args.
      if (thatIsVarArgs && !thisIsVarArgs && !thisParam.isOptionalArg()) {
        return false;
      }

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

    // If "that" ran out of parameters first (and isn't var_args),
    // then it can't be a supertype if "this" still has required params.
    if (thatParam == null && thisParam != null) {
      while (thisParam != null) {
        if (!thisParam.isOptionalArg() && !thisParam.isVarArgs()) {
          return false;
        }
        if (thisParam.isVarArgs()) {
          break;
        }
        thisParam = thisParam.getNext();
      }
    }

    return true;
  }