JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
    setResolvedTypeInternal(this);

    call = (ArrowType) safeResolve(call, t, scope);
    prototype = (FunctionPrototypeType) safeResolve(prototype, t, scope);

    typeOfThis = (ObjectType) safeResolve(typeOfThis, t, scope);

    boolean changed = false;
    ImmutableList.Builder<ObjectType> resolvedInterfaces = null;
    for (int i = 0; i < implementedInterfaces.size(); i++) {
      ObjectType iface = implementedInterfaces.get(i);
      ObjectType resolvedIface = (ObjectType) iface.resolve(t, scope);
      if (resolvedIface != iface) {
        if (!changed) {
          changed = true;
          resolvedInterfaces = ImmutableList.builder();
          for (int j = 0; j < i; j++) {
            resolvedInterfaces.add(implementedInterfaces.get(j));
          }
        }
        resolvedInterfaces.add(resolvedIface);
      } else if (changed) {
        resolvedInterfaces.add(iface);
      }
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