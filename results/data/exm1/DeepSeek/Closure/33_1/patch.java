  public void matchConstraint(ObjectType constraintObj) {
    if (constraintObj.isRecordType()) {
      for (String prop : constraintObj.getOwnPropertyNames()) {
        JSType propType = constraintObj.getPropertyType(prop);
        if (!isPropertyTypeDeclared(prop)) {
          JSType typeToInfer;
          if (!hasProperty(prop)) {
            typeToInfer = getNativeType(JSTypeNative.VOID_TYPE)
                .getLeastSupertype(propType);
          } else {
            typeToInfer = getPropertyType(prop).getLeastSupertype(propType);
          }
          defineInferredProperty(prop, typeToInfer, null);
        }
      }
    }
  }