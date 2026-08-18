// buggy code
  public void matchConstraint(ObjectType constraintObj) {
    // We only want to match contraints on anonymous types.

    // Only match constraints on anonymous object types (like object literals).
    // Named object types should not have their properties altered by record constraints.
    if (getReferenceName() != null) {
      return;
    }

    // Handle the case where the constraint object is a record type.
    //
    // param constraintObj {{prop: (number|undefined)}}
    // function f(constraintObj) {}
    // f({});
    //
    // We want to modify the object literal to match the constraint, by
    // taking any each property on the record and trying to match
    // properties on this object.
    if (constraintObj.isRecordType()) {
      for (String prop : constraintObj.getOwnPropertyNames()) {
        JSType propType = constraintObj.getPropertyType(prop);
        if (!isPropertyTypeDeclared(prop)) {
          JSType typeToInfer = propType;
          if (!hasProperty(prop)) {
            typeToInfer = getNativeType(JSTypeNative.VOID_TYPE)
                .getLeastSupertype(propType);
          }
          defineInferredProperty(prop, typeToInfer, null);
        }
      }
    }
  }