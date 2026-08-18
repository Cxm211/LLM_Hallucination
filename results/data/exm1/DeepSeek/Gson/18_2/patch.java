static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
    checkArgument(supertype.isAssignableFrom(contextRawType));
    // wildcards are useless for resolving supertypes; use the upper bound instead
    if (context instanceof WildcardType) {
        context = ((WildcardType) context).getUpperBounds()[0];
    }
    return resolve(context, contextRawType,
        $Gson$Types.getGenericSupertype(context, contextRawType, supertype));
}