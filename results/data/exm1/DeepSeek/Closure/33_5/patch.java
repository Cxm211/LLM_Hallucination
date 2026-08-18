public void matchConstraint(ObjectType constraintObj) {
    if (constraintObj.isRecordType()) {
      for (String prop : constraintObj.getOwnPropertyNames()) {
        JSType propType = constraintObj.getPropertyType(prop);
        if (!isPropertyTypeDeclared(prop)) {
          JSType typeToInfer = propType;
          if (!hasProperty(prop)) {
            typeToInfer = getNativeType(JSTypeNative.VOID_TYPE)
                .getGreatestSubtype(propType);
          }
          defineInferredProperty(prop, typeToInfer, null);
        }
      }
    }
  }