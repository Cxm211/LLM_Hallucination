  JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
    setResolvedTypeInternal(this);

    call = (ArrowType) safeResolve(call, t, scope);
    prototype = (FunctionPrototypeType) safeResolve(prototype, t, scope);

    // Warning about typeOfThis if it doesn't resolve to an ObjectType
    // is handled further upstream.
    // TODO(nicksantos): Handle this correctly if we have a UnionType.
    JSType resolvedThis = safeResolve(typeOfThis, t, scope);
    typeOfThis = resolvedThis instanceof ObjectType ? (ObjectType) resolvedThis : typeOfThis;

    boolean changed = false;
    ImmutableList.Builder<ObjectType> resolvedInterfaces =
        ImmutableList.builder();
    for (ObjectType iface : implementedInterfaces) {
      ObjectType resolvedIface = (ObjectType) safeResolve(iface, t, scope);
      resolvedInterfaces.add(resolvedIface);
      changed |= (resolvedIface != iface);
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