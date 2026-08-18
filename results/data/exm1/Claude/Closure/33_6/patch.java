public void matchConstraint(ObjectType constraintObj) {
    if (constraintObj.isRecordType()) {
      for (String prop : constraintObj.getOwnPropertyNames()) {
        JSType propType = constraintObj.getPropertyType(prop);
        if (!isPropertyTypeDeclared(prop)) {
          JSType typeToInfer = propType;
          if (!hasProperty(prop)) {
            JSType voidType = getNativeType(JSTypeNative.VOID_TYPE);
            if (voidType != null) {
              typeToInfer = voidType.getLeastSupertype(propType);
            }
          }
          defineInferredProperty(prop, typeToInfer, null);
        }
      }
    }
  }