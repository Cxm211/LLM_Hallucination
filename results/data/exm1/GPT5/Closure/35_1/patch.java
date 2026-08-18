private void inferPropertyTypesToMatchConstraint(JSType type, JSType constraint) {
    if (type == null || constraint == null) {
      return;
    }

    ObjectType constraintObj =
        ObjectType.cast(constraint.restrictByNotNullOrUndefined());
    if (constraintObj != null && constraintObj.isRecordType()) {
      ObjectType objType = ObjectType.cast(type.restrictByNotNullOrUndefined());
      if (objType != null) {
        for (String prop : constraintObj.getOwnPropertyNames()) {
          JSType propType = constraintObj.getPropertyType(prop);
          if (!objType.isPropertyTypeDeclared(prop)) {
            JSType typeToInfer;
            if (!objType.hasProperty(prop)) {
              typeToInfer = getNativeType(VOID_TYPE).getLeastSupertype(propType);
            } else {
              JSType oldType = objType.getPropertyType(prop);
              typeToInfer = oldType.getLeastSupertype(propType);
            }
            objType.defineInferredProperty(prop, typeToInfer, null);
          }
        }
      }
    }
  }