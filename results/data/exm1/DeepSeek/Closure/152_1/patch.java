JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
    setResolvedTypeInternal(this);

    call = (ArrowType) safeResolve(call, t, scope);
    prototype = (FunctionPrototypeType) safeResolve(prototype, t, scope);

    JSType resolvedTypeOfThis = safeResolve(typeOfThis, t, scope);
    if (resolvedTypeOfThis instanceof ObjectType) {
      typeOfThis = (ObjectType) resolvedTypeOfThis;
    }

    boolean changed = false;
    ImmutableList.Builder<ObjectType> resolvedInterfaces =
        ImmutableList.builder();
    for (ObjectType iface : implementedInterfaces) {
      JSType resolvedIface = iface.resolve(t, scope);
      if (resolvedIface instanceof ObjectType) {
        ObjectType objIface = (ObjectType) resolvedIface;
        resolvedInterfaces.add(objIface);
        changed |= (objIface != iface);
      } else {
        resolvedInterfaces.add(iface);
      }
    }
    if (changed) {
      implementedInterfaces = resolvedInterfaces.build();
    }

    if (subTypes != null) {
      for (int i = 0; i < subTypes.size(); i++) {
        JSType resolvedSub = subTypes.get(i).resolve(t, scope);
        if (resolvedSub instanceof FunctionType) {
          subTypes.set(i, (FunctionType) resolvedSub);
        }
      }
    }

    return super.resolveInternal(t, scope);
  }