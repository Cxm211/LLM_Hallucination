private FunctionType tryMergeFunctionPiecewise(
      FunctionType other, boolean leastSuper) {
    Node newParamsNode = null;
    if (call.hasEqualParameters(other.call, false)) {
      newParamsNode = call.parameters;
    } else {
      return null;
    }
    JSType newReturnType = leastSuper ?
        call.returnType.getLeastSupertype(other.call.returnType) :
        call.returnType.getGreatestSubtype(other.call.returnType);
    ObjectType newTypeOfThis = null;
    if (isEquivalent(typeOfThis, other.typeOfThis)) {
      newTypeOfThis = typeOfThis;
    } else {
      JSType maybeNewTypeOfThis = leastSuper ?
          typeOfThis.getLeastSupertype(other.typeOfThis) :
          typeOfThis.getGreatestSubtype(other.typeOfThis);
      if (maybeNewTypeOfThis instanceof ObjectType) {
        newTypeOfThis = (ObjectType) maybeNewTypeOfThis;
      } else {
        newTypeOfThis = leastSuper ?
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE) :
            registry.getNativeObjectType(JSTypeNative.NO_OBJECT_TYPE);
      }
    }
    boolean newReturnTypeInferred =
        call.returnTypeInferred || other.call.returnTypeInferred;
    return new FunctionType(
        registry, null, null,
        new ArrowType(
            registry, newParamsNode, newReturnType, newReturnTypeInferred),
        newTypeOfThis, null, false, false);
  }