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
    // See extended comment in original code about required-ness and practical
    // handling of var_args.
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

    // If "that" is missing required arguments that "this" requires,
    // then "that" cannot be a supertype of "this".
    if (thatParam == null && thisParam != null) {
      while (thisParam != null) {
        // If any remaining parameter on "this" is required (not optional and
        // not var_args), then fail.
        if (!thisParam.isOptionalArg() && !thisParam.isVarArgs()) {
          return false;
        }
        // If var_args, it can accept zero additional arguments, so OK to break.
        if (thisParam.isVarArgs()) {
          break;
        }
        thisParam = thisParam.getNext();
      }
    }

    return true;
  }