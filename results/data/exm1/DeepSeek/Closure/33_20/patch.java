  public void matchConstraint(ObjectType constraintObj) {
    if (constraintObj.isRecordType()) {
      for (String prop : constraintObj.getOwnPropertyNames()) {
        JSType propType = constraintObj.getPropertyType(prop);
        if (!isPropertyTypeDeclared(prop)) {
          JSType typeToInfer = propType;
          if (!hasProperty(prop)) {
            typeToInfer = getNativeType(JSTypeNative.VOID_TYPE)
                .getLeastSupertype(propType);
          } else {
            JSType existingType = getPropertyType(prop);
            typeToInfer = existingType.getLeastSupertype(propType);
          }
          defineInferredProperty(prop, typeToInfer, null);
        }
      }
    }
  }