// ===== FIXED com.google.javascript.jscomp.TypeInference :: inferPropertyTypesToMatchConstraint(JSType, JSType) [lines 1113-1124] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-35-fixed/src/com/google/javascript/jscomp/TypeInference.java =====
  private void inferPropertyTypesToMatchConstraint(
      JSType type, JSType constraint) {
    if (type == null || constraint == null) {
      return;
    }

    ObjectType constraintObj =
        ObjectType.cast(constraint.restrictByNotNullOrUndefined());
    if (constraintObj != null) {
      type.matchConstraint(constraintObj);
    }
  }
