public void matchConstraint(ObjectType constraintObj) {
    if (constraintObj.isRecordType()) {
      for (String prop : constraintObj.getOwnPropertyNames()) {
        JSType propType = constraintObj.getPropertyType(prop);
        if (!isPropertyTypeDeclared(prop)) {
          JSType typeToInfer = propType;
          if (!hasProperty(prop)) {
            typeToInfer = propType.getLeastSupertype(getNativeType(JSTypeNative.VOID_TYPE));
          }
          defineInferredProperty(prop, typeToInfer, null);
        }
      }
    }
  }