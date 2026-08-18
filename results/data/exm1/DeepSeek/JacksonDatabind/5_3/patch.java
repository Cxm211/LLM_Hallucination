protected void _addMethodMixIns(Class<?> targetClass, AnnotatedMethodMap methods, Class<?> mixInCls, AnnotatedMethodMap mixIns) {
    List<Class<?>> parents = new ArrayList<Class<?>>();
    parents.add(mixInCls);
    ClassUtil.findSuperTypes(mixInCls, targetClass, parents);
    for (Class<?> mixin : parents) {
        for (Method m : mixin.getDeclaredMethods()) {
            if (!_isIncludableMemberMethod(m)) continue;
            AnnotatedMethod am = methods.find(m);
            if (am != null) {
                _addMixUnders(m, am);
            } else {
                // [Issue#515], multi-level merge within mixins...
                AnnotatedMethod mixinMethod = mixIns.find(m);
                if (mixinMethod != null) {
                    _addMixUnders(m, mixinMethod);
                } else {
                    mixIns.add(_constructMethod(m));
                }
            }
        }
    }
}