  JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
    setResolvedTypeInternal(this);

    call = (ArrowType) safeResolve(call, t, scope);
    prototype = (FunctionPrototypeType) safeResolve(prototype, t, scope);

    // Warning about typeOfThis if it doesn't resolve to an ObjectType
    // is handled further upstream.
    // TODO(nicksantos): Handle this correctly if we have a UnionType.
    JSType resolvedThis = safeResolve(typeOfThis, t, scope);
    if (resolvedThis instanceof ObjectType) {
      typeOfThis = (ObjectType) resolvedThis;
    } // else leave typeOfThis as-is (may be null or non-object), upstream handles warnings

    boolean changed = false;
    ImmutableList.Builder<ObjectType> resolvedInterfaces =
        ImmutableList.builder();
    for (ObjectType iface : implementedInterfaces) {
      JSType resolved = safeResolve(iface, t, scope);
      if (resolved instanceof ObjectType) {
        ObjectType resolvedIface = (ObjectType) resolved;
        resolvedInterfaces.add(resolvedIface);
        changed |= (resolvedIface != iface);
      } else {
        // If it doesn't resolve to an ObjectType, keep the original to avoid ClassCast issues.
        resolvedInterfaces.add(iface);
      }
    }
    if (changed) {
      implementedInterfaces = resolvedInterfaces.build();
    }

    if (subTypes != null) {
      for (int i = 0; i < subTypes.size(); i++) {
        subTypes.set(i, (FunctionType) safeResolve(subTypes.get(i), t, scope));
      }
    }

    return super.resolveInternal(t, scope);
  }