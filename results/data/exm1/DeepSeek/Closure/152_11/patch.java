  JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
    setResolvedTypeInternal(this);

    call = (ArrowType) safeResolve(call, t, scope);
    prototype = (FunctionPrototypeType) safeResolve(prototype, t, scope);

    // Warning about typeOfThis if it doesn't resolve to an ObjectType
    // is handled further upstream.
    // TODO(nicksantos): Handle this correctly if we have a UnionType.
    JSType resolvedTypeOfThis = safeResolve(typeOfThis, t, scope);
    if (resolvedTypeOfThis instanceof ObjectType) {
      typeOfThis = (ObjectType) resolvedTypeOfThis;
    }

    boolean changed = false;
    ImmutableList.Builder<ObjectType> resolvedInterfaces =
        ImmutableList.builder();
    for (ObjectType iface : implementedInterfaces) {
      JSType resolvedIface = safeResolve(iface, t, scope);
      if (resolvedIface instanceof ObjectType) {
        resolvedInterfaces.add((ObjectType) resolvedIface);
        changed |= (resolvedIface != iface);
      } else {
        resolvedInterfaces.add(iface);
      }
    }
    if (changed) {
      implementedInterfaces = resolvedInterfaces.build();
    }

    if (subTypes != null) {
      for (int i = 0; i < subTypes.size(); i++) {
        JSType resolvedSub = safeResolve(subTypes.get(i), t, scope);
        if (resolvedSub instanceof FunctionType) {
          subTypes.set(i, (FunctionType) resolvedSub);
        }
      }
    }

    return super.resolveInternal(t, scope);
  }