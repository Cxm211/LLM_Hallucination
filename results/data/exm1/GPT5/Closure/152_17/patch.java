JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
    setResolvedTypeInternal(this);

    call = (ArrowType) safeResolve(call, t, scope);
    prototype = (FunctionPrototypeType) safeResolve(prototype, t, scope);

    // Warning about typeOfThis if it doesn't resolve to an ObjectType
    // is handled further upstream.
    // TODO(nicksantos): Handle this correctly if we have a UnionType.
    {
      JSType resolvedThis = safeResolve(typeOfThis, t, scope);
      if (resolvedThis instanceof ObjectType) {
        typeOfThis = (ObjectType) resolvedThis;
      } else {
        // Leave as-is (possibly null) if it does not resolve to an ObjectType
        // to avoid ClassCastException during resolution.
        typeOfThis = (resolvedThis == null) ? null : typeOfThis;
      }
    }

    boolean changed = false;
    ImmutableList.Builder<ObjectType> resolvedInterfaces =
        ImmutableList.builder();
    for (ObjectType iface : implementedInterfaces) {
      JSType ri = safeResolve(iface, t, scope);
      ObjectType resolvedIface = ri instanceof ObjectType ? (ObjectType) ri : iface;
      resolvedInterfaces.add(resolvedIface);
      changed |= (resolvedIface != iface);
    }
    if (changed) {
      implementedInterfaces = resolvedInterfaces.build();
    }

    if (subTypes != null) {
      for (int i = 0; i < subTypes.size(); i++) {
        JSType rs = safeResolve(subTypes.get(i), t, scope);
        if (rs instanceof FunctionType) {
          subTypes.set(i, (FunctionType) rs);
        } else {
          subTypes.set(i, (FunctionType) subTypes.get(i).resolve(t, scope));
        }
      }
    }

    return super.resolveInternal(t, scope);
  }