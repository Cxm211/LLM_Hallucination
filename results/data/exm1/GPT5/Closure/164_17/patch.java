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
    // See notes in the original comments regarding requiredness.
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

    // If "that" has ended but "this" still has required params, then
    // "this" cannot be a subtype of "that" unless "that" is var_args.
    if (thatParam == null && thisParam != null) {
      Node thatLast = that.parameters.getLastChild();
      boolean thatAcceptsVarArgs = (thatLast != null && thatLast.isVarArgs());
      if (!thatAcceptsVarArgs) {
        while (thisParam != null) {
          if (!thisParam.isOptionalArg() && !thisParam.isVarArgs()) {
            return false;
          }
          thisParam = thisParam.getNext();
        }
      }
    }

    return true;
  }