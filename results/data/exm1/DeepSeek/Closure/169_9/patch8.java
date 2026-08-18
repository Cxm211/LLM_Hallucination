  boolean checkEquivalenceHelper(JSType that, boolean tolerateUnknowns) {
    if (this == that) {
      return true;
    }

    boolean thisUnknown = isUnknownType();
    boolean thatUnknown = that.isUnknownType();
    if (thisUnknown || thatUnknown) {
      if (tolerateUnknowns) {
        // If we're checking for invariance, the unknown type is invariant
        // with everyone.
        // If we're checking data flow, then two types are the same if they're
        // both unknown.
        return thisUnknown && thatUnknown;
      } else if (thisUnknown && thatUnknown &&
          (isNominalType() ^ that.isNominalType())) {
        // If they're both unknown, but one is a nominal type and the other
        // is not, then we should fail out immediately. This ensures that
        // we won't unbox the unknowns further down.
        return false;
      }
    }

    if (isUnionType() && that.isUnionType()) {
      return this.toMaybeUnionType().checkUnionEquivalenceHelper(
          that.toMaybeUnionType(), tolerateUnknowns);
    }

    if (isFunctionType() && that.isFunctionType()) {
      return this.toMaybeFunctionType().checkFunctionEquivalenceHelper(
          that.toMaybeFunctionType(), tolerateUnknowns);
    }

    if (isRecordType() && that.isRecordType()) {
      return this.toMaybeRecordType().checkRecordEquivalenceHelper(
          that.toMaybeRecordType(), tolerateUnknowns);
    }

    ParameterizedType thisParamType = toMaybeParameterizedType();
    ParameterizedType thatParamType = that.toMaybeParameterizedType();
    if (thisParamType != null || thatParamType != null) {
      // Check if one type is parameterized, but the other is not.
      boolean paramsMatch = false;
      if (thisParamType != null && thatParamType != null) {
        paramsMatch = thisParamType.getParameterType().checkEquivalenceHelper(
            thatParamType.getParameterType(), tolerateUnknowns);
      } else if (tolerateUnknowns) {
        // If one of the type parameters is unknown, but the other is not,
        // then we consider these the same for the purposes of data flow
        // and invariance.
        paramsMatch = true;
      } else {
        paramsMatch = false;
      }

      JSType thisRootType = thisParamType == null ?
          this : thisParamType.getReferencedTypeInternal();
      JSType thatRootType = thatParamType == null ?
          that : thatParamType.getReferencedTypeInternal();
      return paramsMatch &&
          thisRootType.checkEquivalenceHelper(thatRootType, tolerateUnknowns);
    }

    if (isNominalType() && that.isNominalType()) {
      return toObjectType().getReferenceName().equals(
          that.toObjectType().getReferenceName());
    }

    // Unbox other proxies.
    if (this instanceof ProxyObjectType) {
      return ((ProxyObjectType) this)
          .getReferencedTypeInternal().checkEquivalenceHelper(
              that, tolerateUnknowns);
    }

    if (that instanceof ProxyObjectType) {
      return checkEquivalenceHelper(
          ((ProxyObjectType) that).getReferencedTypeInternal(),
          tolerateUnknowns);
    }

    // Relies on the fact that for the base {@link JSType}, only one
    // instance of each sub-type will ever be created in a given registry, so
    // there is no need to verify members. If the object pointers are not
    // identical, then the type member must be different.
    return this == that;
  }