JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
  setResolvedTypeInternal(this);

  call = (ArrowType) safeResolve(call, t, scope);
  prototype = (FunctionPrototypeType) safeResolve(prototype, t, scope);

  JSType maybeTypeOfThis = safeResolve(typeOfThis, t, scope);
  if (maybeTypeOfThis instanceof ObjectType) {
    typeOfThis = (ObjectType) maybeTypeOfThis;
  } else if (maybeTypeOfThis != null && maybeTypeOfThis.isUnionType()) {
    boolean allObjectTypes = true;
    for (JSType alternate : maybeTypeOfThis.toMaybeUnionType().getAlternates()) {
      if (!(alternate instanceof ObjectType)) {
        allObjectTypes = false;
        break;
      }
    }
    if (allObjectTypes) {
      typeOfThis = (ObjectType) maybeTypeOfThis.restrictByNotNullOrUndefined();
    }
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