  public void matchConstraint(JSType constraint) {
    // We only want to match constraints on anonymous types.
    if (hasReferenceName()) {
      return;
    }

    // Handle the case where the constraint object is a record type.
    //
    // param constraint {{prop: (number|undefined)}}
    // function f(constraint) {}
    // f({});
    //
    // We want to modify the object literal to match the constraint, by
    // taking any each property on the record and trying to match
    // properties on this object.
    if (constraint.isRecordType()) {
      matchRecordTypeConstraint(constraint.toObjectType());
    }
  }

// trigger testcase
public void testIssue785() {
    inFunction("/** @param {string|{prop: (string|undefined)}} x */" +
               "function f(x) {}" +
               "var out = {};" +
               "f(out);");
    assertEquals("{prop: (string|undefined)}", getType("out").toString());
  }

public void testRecordInference() {
    inFunction(
        "/** @param {{a: (boolean|undefined)}|{b: (string|undefined)}} x */" +
        "function f(x) {}" +
        "var out = {};" +
        "f(out);");
    assertEquals("{a: (boolean|undefined), b: (string|undefined)}",
        getType("out").toString());
  }
