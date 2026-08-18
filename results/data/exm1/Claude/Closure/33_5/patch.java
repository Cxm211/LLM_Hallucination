public void matchConstraint(ObjectType constraintObj) {
    if (constraintObj.isRecordType()) {
      for (String prop : constraintObj.getOwnPropertyNames()) {
        JSType propType = constraintObj.getPropertyType(prop);
        if (!isPropertyTypeDeclared(prop)) {
          JSType typeToInfer = propType;
          if (!hasProperty(prop)) {
            typeToInfer = propType;
          }
          defineInferredProperty(prop, typeToInfer, null);
        }
      }
    }
  }