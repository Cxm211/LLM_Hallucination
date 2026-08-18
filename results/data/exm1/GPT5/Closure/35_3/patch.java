  private void inferPropertyTypesToMatchConstraint(
      JSType type, JSType constraint) {
    if (type == null || constraint == null) {
      return;
    }

    ObjectType constraintObj =
        ObjectType.cast(constraint.restrictByNotNullOrUndefined());
    if (constraintObj != null && constraintObj.isRecordType()) {
      ObjectType objType = ObjectType.cast(type.restrictByNotNullOrUndefined());
      // Only infer properties onto record types (e.g., object literals),
      // not onto arbitrary object instances like Array, to avoid masking
      // missing-property errors on native/object instance types.
      if (objType != null && objType.isRecordType()) {
        for (String prop : constraintObj.getOwnPropertyNames()) {
          JSType propType = constraintObj.getPropertyType(prop);
          if (!objType.isPropertyTypeDeclared(prop)) {
            JSType typeToInfer = propType;
            if (!objType.hasProperty(prop)) {
              typeToInfer =
                  getNativeType(VOID_TYPE).getLeastSupertype(propType);
            }
            objType.defineInferredProperty(prop, typeToInfer, null);
          }
        }
      }
    }
  }