JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
    setResolvedTypeInternal(this);

    call = (ArrowType) safeResolve(call, t, scope);
    prototype = (FunctionPrototypeType) safeResolve(prototype, t, scope);

    JSType maybeTypeOfThis = safeResolve(typeOfThis, t, scope);
    if (maybeTypeOfThis instanceof ObjectType) {
      typeOfThis = (ObjectType) maybeTypeOfThis;
    } else if (maybeTypeOfThis.isUnionType()) {
      JSType restricted = maybeTypeOfThis.restrictByNotNullOrUndefined();
      if (restricted instanceof ObjectType) {
        typeOfThis = (ObjectType) restricted;
      }
    } else if (maybeTypeOfThis.isNullType() || maybeTypeOfThis.isVoidType()) {
      // Coerce null/void to the global object; for now, leave it as is
    }

    boolean changed = false;
    ImmutableList.Builder<ObjectType> resolvedInterfaces =
        ImmutableList.builder();
    for (ObjectType iface : implementedInterfaces) {
      ObjectType resolvedIface = (ObjectType) iface.resolve(t, scope);
      resolvedInterfaces.add(resolvedIface);
      changed |= (resolvedIface != iface);
    }
    if (changed) {
      implementedInterfaces = resolvedInterfaces.build();
    }

    if (subTypes != null) {
      for (int i = 0; i < subTypes.size(); i++) {
        subTypes.set(i, (FunctionType) subTypes.get(i).resolve(t, scope));
      }
    }

    return super.resolveInternal(t, scope);
  }