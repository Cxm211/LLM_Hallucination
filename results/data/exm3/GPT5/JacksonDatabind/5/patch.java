protected void _addMethodMixIns(Class<?> targetClass, AnnotatedMethodMap methods,
            Class<?> mixInCls, AnnotatedMethodMap mixIns)
    {
        List<Class<?>> parents = new ArrayList<Class<?>>();
        parents.add(mixInCls);
        ClassUtil.findSuperTypes(mixInCls, targetClass, parents);
        // Process from super-types to sub-types so that mix-in subclass annotations take precedence
        for (int i = parents.size() - 1; i >= 0; --i) {
            Class<?> mixin = parents.get(i);
            for (Method m : mixin.getDeclaredMethods()) {
                if (!_isIncludableMemberMethod(m)) {
                    continue;
                }
                AnnotatedMethod am = methods.find(m);
                if (am != null) {
                    _addMixUnders(m, am);
                } else {
                    mixIns.add(_constructMethod(m));
                }
            }
        }
    }