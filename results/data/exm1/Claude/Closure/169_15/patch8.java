boolean checkEquivalenceHelper(JSType that, boolean tolerateUnknowns) {
    if (this == that) {
      return true;
    }

    boolean thisUnknown = isUnknownType();
    boolean thatUnknown = that.isUnknownType();
    if (thisUnknown || thatUnknown) {
      if (tolerateUnknowns) {
        return thisUnknown && thatUnknown;
      } else if (thisUnknown && thatUnknown &&
          (isNominalType() ^ that.isNominalType())) {
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
      boolean paramsMatch = false;
      if (thisParamType != null && thatParamType != null) {
        paramsMatch = thisParamType.getParameterType().checkEquivalenceHelper(
            thatParamType.getParameterType(), tolerateUnknowns);
      } else if (tolerateUnknowns) {
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

    return this == that;
  }