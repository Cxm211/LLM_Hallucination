// buggy function
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

// trigger testcase
// com/google/javascript/jscomp/TypeCheckTest.java::testIssue669
public void testIssue669() throws Exception {
    testTypes(
        "/** @return {{prop1: (Object|undefined)}} */" +
         "function f(a) {" +
         "  var results;" +
         "  if (a) {" +
         "    results = {};" +
         "    results.prop1 = {a: 3};" +
         "  } else {" +
         "    results = {prop2: 3};" +
         "  }" +
         "  return results;" +
         "}");
  }
