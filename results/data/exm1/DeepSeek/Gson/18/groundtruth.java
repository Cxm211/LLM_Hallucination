// ===== FIXED com.google.gson.internal.$Gson$Types :: getSupertype(Type, Class, Class) [lines 277-285] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Gson/Gson-18-fixed/gson/src/main/java/com/google/gson/internal/$Gson$Types.java =====
  static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
    if (context instanceof WildcardType) {
      // wildcards are useless for resolving supertypes. As the upper bound has the same raw type, use it instead
      context = ((WildcardType)context).getUpperBounds()[0];
    }
    checkArgument(supertype.isAssignableFrom(contextRawType));
    return resolve(context, contextRawType,
        $Gson$Types.getGenericSupertype(context, contextRawType, supertype));
  }
