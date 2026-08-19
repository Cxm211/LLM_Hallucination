// ===== FIXED com.google.javascript.rhino.jstype.ArrowType :: checkArrowEquivalenceHelper(ArrowType, boolean) [lines 203-210] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-169-fixed/src/com/google/javascript/rhino/jstype/ArrowType.java =====
  boolean checkArrowEquivalenceHelper(
      ArrowType that, EquivalenceMethod eqMethod) {
    // Please keep this method in sync with the hashCode() method below.
    if (!returnType.checkEquivalenceHelper(that.returnType, eqMethod)) {
      return false;
    }
    return hasEqualParameters(that, eqMethod);
  }

// ===== FIXED com.google.javascript.rhino.jstype.ArrowType :: hasEqualParameters(ArrowType, boolean) [lines 177-201] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-169-fixed/src/com/google/javascript/rhino/jstype/ArrowType.java =====
  boolean hasEqualParameters(ArrowType that, EquivalenceMethod eqMethod) {
    Node thisParam = parameters.getFirstChild();
    Node otherParam = that.parameters.getFirstChild();
    while (thisParam != null && otherParam != null) {
      JSType thisParamType = thisParam.getJSType();
      JSType otherParamType = otherParam.getJSType();
      if (thisParamType != null) {
        // Both parameter lists give a type for this param, it should be equal
        if (otherParamType != null &&
            !thisParamType.checkEquivalenceHelper(
                otherParamType, eqMethod)) {
          return false;
        }
      } else {
        if (otherParamType != null) {
          return false;
        }
      }
      thisParam = thisParam.getNext();
      otherParam = otherParam.getNext();
    }
    // One of the parameters is null, so the types are only equal if both
    // parameter lists are null (they are equal).
    return thisParam == otherParam;
  }

// ===== FIXED com.google.javascript.rhino.jstype.FunctionType :: checkFunctionEquivalenceHelper(FunctionType, boolean) [lines 889-910] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-169-fixed/src/com/google/javascript/rhino/jstype/FunctionType.java =====
  boolean checkFunctionEquivalenceHelper(
      FunctionType that, EquivalenceMethod eqMethod) {
    if (isConstructor()) {
      if (that.isConstructor()) {
        return this == that;
      }
      return false;
    }
    if (isInterface()) {
      if (that.isInterface()) {
        return getReferenceName().equals(that.getReferenceName());
      }
      return false;
    }
    if (that.isInterface()) {
      return false;
    }

    return typeOfThis.checkEquivalenceHelper(
        that.typeOfThis, eqMethod) &&
        call.checkArrowEquivalenceHelper(that.call, eqMethod);
  }

// ===== FIXED com.google.javascript.rhino.jstype.FunctionType :: hasEqualCallType(FunctionType) [lines 917-920] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-169-fixed/src/com/google/javascript/rhino/jstype/FunctionType.java =====
  public boolean hasEqualCallType(FunctionType otherType) {
    return this.call.checkArrowEquivalenceHelper(
        otherType.call, EquivalenceMethod.IDENTITY);
  }

// ===== FIXED com.google.javascript.rhino.jstype.FunctionType :: tryMergeFunctionPiecewise(FunctionType, boolean) [lines 789-828] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-169-fixed/src/com/google/javascript/rhino/jstype/FunctionType.java =====
  private FunctionType tryMergeFunctionPiecewise(
      FunctionType other, boolean leastSuper) {
    Node newParamsNode = null;
    if (call.hasEqualParameters(other.call, EquivalenceMethod.IDENTITY)) {
      newParamsNode = call.parameters;
    } else {
      // If the parameters are not equal, don't try to merge them.
      // Someday, we should try to merge the individual params.
      return null;
    }

    JSType newReturnType = leastSuper ?
        call.returnType.getLeastSupertype(other.call.returnType) :
        call.returnType.getGreatestSubtype(other.call.returnType);

    ObjectType newTypeOfThis = null;
    if (isEquivalent(typeOfThis, other.typeOfThis)) {
      newTypeOfThis = typeOfThis;
    } else {
      JSType maybeNewTypeOfThis = leastSuper ?
          typeOfThis.getLeastSupertype(other.typeOfThis) :
          typeOfThis.getGreatestSubtype(other.typeOfThis);
      if (maybeNewTypeOfThis instanceof ObjectType) {
        newTypeOfThis = (ObjectType) maybeNewTypeOfThis;
      } else {
        newTypeOfThis = leastSuper ?
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE) :
            registry.getNativeObjectType(JSTypeNative.NO_OBJECT_TYPE);
      }
    }

    boolean newReturnTypeInferred =
        call.returnTypeInferred || other.call.returnTypeInferred;

    return new FunctionType(
        registry, null, null,
        new ArrowType(
            registry, newParamsNode, newReturnType, newReturnTypeInferred),
        newTypeOfThis, null, false, false);
  }

// ===== FIXED com.google.javascript.rhino.jstype.JSType :: checkEquivalenceHelper(JSType, boolean) [lines 520-608] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-169-fixed/src/com/google/javascript/rhino/jstype/JSType.java =====
  boolean checkEquivalenceHelper(JSType that, EquivalenceMethod eqMethod) {
    if (this == that) {
      return true;
    }

    boolean thisUnknown = isUnknownType();
    boolean thatUnknown = that.isUnknownType();
    if (thisUnknown || thatUnknown) {
      if (eqMethod == EquivalenceMethod.INVARIANT) {
        // If we're checking for invariance, the unknown type is invariant
        // with everyone.
        return true;
      } else if (eqMethod == EquivalenceMethod.DATA_FLOW) {
        // If we're checking data flow, then two types are the same if they're
        // both unknown.
        return thisUnknown && thatUnknown;
      } else if (thisUnknown && thatUnknown &&
          (isNominalType() ^ that.isNominalType())) {
        // If they're both unknown, but one is a nominal type and the other
        // is not, then we should fail out immediately. This ensures that
        // we won't unbox the unknowns further down.
        return false;
      }
    }

    if (isUnionType() && that.isUnionType()) {
      return this.toMaybeUnionType().checkUnionEquivalenceHelper(
          that.toMaybeUnionType(), eqMethod);
    }

    if (isFunctionType() && that.isFunctionType()) {
      return this.toMaybeFunctionType().checkFunctionEquivalenceHelper(
          that.toMaybeFunctionType(), eqMethod);
    }

    if (isRecordType() && that.isRecordType()) {
      return this.toMaybeRecordType().checkRecordEquivalenceHelper(
          that.toMaybeRecordType(), eqMethod);
    }

    ParameterizedType thisParamType = toMaybeParameterizedType();
    ParameterizedType thatParamType = that.toMaybeParameterizedType();
    if (thisParamType != null || thatParamType != null) {
      // Check if one type is parameterized, but the other is not.
      boolean paramsMatch = false;
      if (thisParamType != null && thatParamType != null) {
        paramsMatch = thisParamType.getParameterType().checkEquivalenceHelper(
            thatParamType.getParameterType(), eqMethod);
      } else if (eqMethod == EquivalenceMethod.IDENTITY) {
        paramsMatch = false;
      } else {
        // If one of the type parameters is unknown, but the other is not,
        // then we consider these the same for the purposes of data flow
        // and invariance.
        paramsMatch = true;
      }

      JSType thisRootType = thisParamType == null ?
          this : thisParamType.getReferencedTypeInternal();
      JSType thatRootType = thatParamType == null ?
          that : thatParamType.getReferencedTypeInternal();
      return paramsMatch &&
          thisRootType.checkEquivalenceHelper(thatRootType, eqMethod);
    }

    if (isNominalType() && that.isNominalType()) {
      return toObjectType().getReferenceName().equals(
          that.toObjectType().getReferenceName());
    }

    // Unbox other proxies.
    if (this instanceof ProxyObjectType) {
      return ((ProxyObjectType) this)
          .getReferencedTypeInternal().checkEquivalenceHelper(
              that, eqMethod);
    }

    if (that instanceof ProxyObjectType) {
      return checkEquivalenceHelper(
          ((ProxyObjectType) that).getReferencedTypeInternal(),
          eqMethod);
    }

    // Relies on the fact that for the base {@link JSType}, only one
    // instance of each sub-type will ever be created in a given registry, so
    // there is no need to verify members. If the object pointers are not
    // identical, then the type member must be different.
    return this == that;
  }

// ===== FIXED com.google.javascript.rhino.jstype.JSType :: differsFrom(JSType) [lines 513-515] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-169-fixed/src/com/google/javascript/rhino/jstype/JSType.java =====
  public final boolean differsFrom(JSType that) {
    return !checkEquivalenceHelper(that, EquivalenceMethod.DATA_FLOW);
  }

// ===== FIXED com.google.javascript.rhino.jstype.JSType :: isEquivalentTo(JSType) [lines 491-493] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-169-fixed/src/com/google/javascript/rhino/jstype/JSType.java =====
  public final boolean isEquivalentTo(JSType that) {
    return checkEquivalenceHelper(that, EquivalenceMethod.IDENTITY);
  }

// ===== FIXED com.google.javascript.rhino.jstype.JSType :: isInvariant(JSType) [lines 499-501] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-169-fixed/src/com/google/javascript/rhino/jstype/JSType.java =====
  public final boolean isInvariant(JSType that) {
    return checkEquivalenceHelper(that, EquivalenceMethod.INVARIANT);
  }

// ===== FIXED com.google.javascript.rhino.jstype.RecordType :: checkRecordEquivalenceHelper(RecordType, boolean) [lines 117-131] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-169-fixed/src/com/google/javascript/rhino/jstype/RecordType.java =====
  boolean checkRecordEquivalenceHelper(
      RecordType otherRecord, EquivalenceMethod eqMethod) {
    Set<String> keySet = properties.keySet();
    Map<String, JSType> otherProps = otherRecord.properties;
    if (!otherProps.keySet().equals(keySet)) {
      return false;
    }
    for (String key : keySet) {
      if (!otherProps.get(key).checkEquivalenceHelper(
              properties.get(key), eqMethod)) {
        return false;
      }
    }
    return true;
  }

// ===== FIXED com.google.javascript.rhino.jstype.RecordType :: getGreatestSubtypeHelper(JSType) [lines 153-212] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-169-fixed/src/com/google/javascript/rhino/jstype/RecordType.java =====
  JSType getGreatestSubtypeHelper(JSType that) {
    if (that.isRecordType()) {
      RecordType thatRecord = that.toMaybeRecordType();
      RecordTypeBuilder builder = new RecordTypeBuilder(registry);
      builder.setSynthesized(true);

      // The greatest subtype consists of those *unique* properties of both
      // record types. If any property conflicts, then the NO_TYPE type
      // is returned.
      for (String property : properties.keySet()) {
        if (thatRecord.hasProperty(property) &&
            !thatRecord.getPropertyType(property).isInvariant(
                getPropertyType(property))) {
          return registry.getNativeObjectType(JSTypeNative.NO_TYPE);
        }

        builder.addProperty(property, getPropertyType(property),
            getPropertyNode(property));
      }

      for (String property : thatRecord.properties.keySet()) {
        if (!hasProperty(property)) {
          builder.addProperty(property, thatRecord.getPropertyType(property),
              thatRecord.getPropertyNode(property));
        }
      }

      return builder.build();
    }

    JSType greatestSubtype = registry.getNativeType(
        JSTypeNative.NO_OBJECT_TYPE);
    JSType thatRestrictedToObj =
        registry.getNativeType(JSTypeNative.OBJECT_TYPE)
        .getGreatestSubtype(that);
    if (!thatRestrictedToObj.isEmptyType()) {
      // In this branch, the other type is some object type. We find
      // the greatest subtype with the following algorithm:
      // 1) For each property "x" of this record type, take the union
      //    of all classes with a property "x" with a compatible property type.
      //    and which are a subtype of {@code that}.
      // 2) Take the intersection of all of these unions.
      for (Map.Entry<String, JSType> entry : properties.entrySet()) {
        String propName = entry.getKey();
        JSType propType = entry.getValue();
        UnionTypeBuilder builder = new UnionTypeBuilder(registry);
        for (ObjectType alt :
                 registry.getEachReferenceTypeWithProperty(propName)) {
          JSType altPropType = alt.getPropertyType(propName);
          if (altPropType != null && !alt.isEquivalentTo(this) &&
              alt.isSubtype(that) &&
              propType.isInvariant(altPropType)) {
            builder.addAlternate(alt);
          }
        }
        greatestSubtype = greatestSubtype.getLeastSupertype(builder.build());
      }
    }
    return greatestSubtype;
  }

// ===== FIXED com.google.javascript.rhino.jstype.RecordType :: isSubtype(ObjectType, RecordType) [lines 242-283] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-169-fixed/src/com/google/javascript/rhino/jstype/RecordType.java =====
  static boolean isSubtype(ObjectType typeA, RecordType typeB) {
    // typeA is a subtype of record type typeB iff:
    // 1) typeA has all the properties declared in typeB.
    // 2) And for each property of typeB,
    //    2a) if the property of typeA is declared, it must be equal
    //        to the type of the property of typeB,
    //    2b) otherwise, it must be a subtype of the property of typeB.
    //
    // To figure out why this is true, consider the following pseudo-code:
    // /** @type {{a: (Object,null)}} */ var x;
    // /** @type {{a: !Object}} */ var y;
    // var z = {a: {}};
    // x.a = null;
    //
    // y cannot be assigned to x, because line 4 would violate y's declared
    // properties. But z can be assigned to x. Even though z and y are the
    // same type, the properties of z are inferred--and so an assignment
    // to the property of z would not violate any restrictions on it.
    for (String property : typeB.properties.keySet()) {
      if (!typeA.hasProperty(property)) {
        return false;
      }

      JSType propA = typeA.getPropertyType(property);
      JSType propB = typeB.getPropertyType(property);
      if (typeA.isPropertyTypeDeclared(property)) {
        // If one declared property isn't invariant,
        // then the whole record isn't covariant.
        if (!propA.isInvariant(propB)) {
          return false;
        }
      } else {
        // If one inferred property isn't a subtype,
        // then the whole record isn't covariant.
        if (!propA.isSubtype(propB)) {
          return false;
        }
      }
    }

    return true;
  }

// ===== FIXED com.google.javascript.rhino.jstype.UnionType :: checkUnionEquivalenceHelper(UnionType, boolean) [lines 333-345] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-169-fixed/src/com/google/javascript/rhino/jstype/UnionType.java =====
  boolean checkUnionEquivalenceHelper(
      UnionType that, EquivalenceMethod eqMethod) {
    if (eqMethod == EquivalenceMethod.IDENTITY
        && alternates.size() != that.alternates.size()) {
      return false;
    }
    for (JSType alternate : that.alternates) {
      if (!hasAlternate(alternate, eqMethod)) {
        return false;
      }
    }
    return true;
  }

// ===== FIXED com.google.javascript.rhino.jstype.UnionType :: hasAlternate(JSType, boolean) [lines 347-354] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-169-fixed/src/com/google/javascript/rhino/jstype/UnionType.java =====
  private boolean hasAlternate(JSType type, EquivalenceMethod eqMethod) {
    for (JSType alternate : alternates) {
      if (alternate.checkEquivalenceHelper(type, eqMethod)) {
        return true;
      }
    }
    return false;
  }
