  boolean hasEqualParameters(ArrowType that, boolean tolerateUnknowns) {
    Node thisParam = parameters.getFirstChild();
    Node otherParam = that.parameters.getFirstChild();
    while (thisParam != null && otherParam != null) {
      JSType thisParamType = thisParam.getJSType();
      JSType otherParamType = otherParam.getJSType();
      if (thisParamType != null) {
        // Both parameter lists give a type for this param, it should be equal
        if (otherParamType == null ||
            !thisParamType.checkEquivalenceHelper(
                otherParamType, tolerateUnknowns)) {
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

  boolean checkArrowEquivalenceHelper(
      ArrowType that, boolean tolerateUnknowns) {
    // Please keep this method in sync with the hashCode() method below.
    if (!returnType.checkEquivalenceHelper(that.returnType, tolerateUnknowns)) {
      return false;
    }
    return hasEqualParameters(that, tolerateUnknowns);
  }

  private FunctionType tryMergeFunctionPiecewise(
      FunctionType other, boolean leastSuper) {
    Node newParamsNode = null;
    if (call.hasEqualParameters(other.call, false)) {
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

  boolean checkFunctionEquivalenceHelper(
      FunctionType that, boolean tolerateUnknowns) {
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
        that.typeOfThis, tolerateUnknowns) &&
        call.checkArrowEquivalenceHelper(that.call, tolerateUnknowns);
  }

  public boolean hasEqualCallType(FunctionType otherType) {
    return this.call.checkArrowEquivalenceHelper(otherType.call, false);
  }

  public final boolean isEquivalentTo(JSType that) {
    return checkEquivalenceHelper(that, false);
  }

  public final boolean isInvariant(JSType that) {
    return checkEquivalenceHelper(that, true);
  }

  public final boolean differsFrom(JSType that) {
    return !checkEquivalenceHelper(that, true);
  }

  boolean checkEquivalenceHelper(JSType that, boolean tolerateUnknowns) {
    if (this == that) {
      return true;
    }

    boolean thisUnknown = isUnknownType();
    boolean thatUnknown = that.isUnknownType();
    if (thisUnknown || thatUnknown) {
      if (tolerateUnknowns) {
        // If we're checking for invariance, the unknown type is invariant
        // with everyone.
        return true;
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
          that.toMaybeUnionType(), tolerateUnknowns);
    }

    if (isFunctionType() && that.isFunctionType()) {
      return this.toMaybeFunctionType().checkFunctionEquivalenceHelper(
          that.toMaybeFunctionType(), tolerateUnknowns);
    }

    if (isRecordType() && that.isRecordType()) {
      return this.toMaybeRecordType().checkRecordEquivalenceHelper(
          that.toMaybeRecordType(), tolerateUnknowns);
    }

    ParameterizedType thisParamType = toMaybeParameterizedType();
    ParameterizedType thatParamType = that.toMaybeParameterizedType();
    if (thisParamType != null || thatParamType != null) {
      // Check if one type is parameterized, but the other is not.
      boolean paramsMatch = false;
      if (thisParamType != null && thatParamType != null) {
        paramsMatch = thisParamType.getParameterType().checkEquivalenceHelper(
            thatParamType.getParameterType(), tolerateUnknowns);
      } else if (tolerateUnknowns) {
        // If one of the type parameters is unknown, but the other is not,
        // then we consider these the same for the purposes of data flow
        // and invariance.
        paramsMatch = true;
      } else {
        paramsMatch = false;
      }

      JSType thisRootType = thisParamType == null ?
          this : thisParamType.getReferencedTypeInternal();
      JSType thatRootType = thatParamType == null ?
          that : thatParamType.getReferencedTypeInternal();
      return paramsMatch &&
          thisRootType.checkEquivalenceHelper(thatRootType, tolerateUnknowns);
    }

    if (isNominalType() && that.isNominalType()) {
      return toObjectType().getReferenceName().equals(
          that.toObjectType().getReferenceName());
    }

    // Unbox other proxies.
    if (this instanceof ProxyObjectType) {
      return ((ProxyObjectType) this)
          .getReferencedTypeInternal().checkEquivalenceHelper(
              that, tolerateUnknowns);
    }

    if (that instanceof ProxyObjectType) {
      return checkEquivalenceHelper(
          ((ProxyObjectType) that).getReferencedTypeInternal(),
          tolerateUnknowns);
    }

    // Relies on the fact that for the base {@link JSType}, only one
    // instance of each sub-type will ever be created in a given registry, so
    // there is no need to verify members. If the object pointers are not
    // identical, then the type member must be different.
    return this == that;
  }

  boolean checkRecordEquivalenceHelper(
      RecordType otherRecord, boolean tolerateUnknowns) {
    Set<String> keySet = properties.keySet();
    Map<String, JSType> otherProps = otherRecord.properties;
    if (!otherProps.keySet().equals(keySet)) {
      return false;
    }
    for (String key : keySet) {
      if (!otherProps.get(key).checkEquivalenceHelper(
              properties.get(key), tolerateUnknowns)) {
        return false;
      }
    }
    return true;
  }

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
              (propType.isUnknownType() || altPropType.isUnknownType() ||
                  altPropType.isEquivalentTo(propType))) {
            builder.addAlternate(alt);
          }
        }
        greatestSubtype = greatestSubtype.getLeastSupertype(builder.build());
      }
    }
    return greatestSubtype;
  }

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
      if (!propA.isUnknownType() && !propB.isUnknownType()) {
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
    }

    return true;
  }

  boolean checkUnionEquivalenceHelper(
      UnionType that, boolean tolerateUnknowns) {
    if (!tolerateUnknowns
        && alternates.size() != that.alternates.size()) {
      return false;
    }
    for (JSType alternate : that.alternates) {
      if (!hasAlternate(alternate, tolerateUnknowns)) {
        return false;
      }
    }
    return true;
  }

  private boolean hasAlternate(JSType type, boolean tolerateUnknowns) {
    for (JSType alternate : alternates) {
      if (alternate.checkEquivalenceHelper(type, tolerateUnknowns)) {
        return true;
      }
    }
    return false;
  }