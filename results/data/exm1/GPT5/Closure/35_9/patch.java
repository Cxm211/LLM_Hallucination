  private void inferPropertyTypesToMatchConstraint(
      JSType type, JSType constraint) {
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
          // Only add inferred properties that don't already exist anywhere on the
          // object (including its prototype chain), to avoid shadowing declared
          // properties from prototypes.
          if (!objType.hasProperty(prop)) {
            JSType typeToInfer =
                getNativeType(VOID_TYPE).getLeastSupertype(propType);
            objType.defineInferredProperty(prop, typeToInfer, null);
          }
        }
      }
    }
  }