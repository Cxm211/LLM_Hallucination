// ===== FIXED com.google.javascript.rhino.jstype.JSType :: isEmptyType() [lines 162-166] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-82-fixed/src/com/google/javascript/rhino/jstype/JSType.java =====
  public final boolean isEmptyType() {
    return isNoType() || isNoObjectType() || isNoResolvedType() ||
        (registry.getNativeFunctionType(
             JSTypeNative.LEAST_FUNCTION_TYPE) == this);
  }
