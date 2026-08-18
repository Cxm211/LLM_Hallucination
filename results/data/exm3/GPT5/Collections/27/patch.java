public T create() {
    try {
        return clazz.getDeclaredConstructor().newInstance();
    } catch (java.lang.NoSuchMethodException e) {
        throw new UnsupportedOperationException("Cannot instantiate class: " + clazz, e);
    } catch (java.lang.InstantiationException e) {
        throw new UnsupportedOperationException("Cannot instantiate class: " + clazz, e);
    } catch (java.lang.IllegalAccessException e) {
        throw new UnsupportedOperationException("Cannot instantiate class: " + clazz, e);
    } catch (java.lang.reflect.InvocationTargetException e) {
        throw new UnsupportedOperationException("Cannot instantiate class: " + clazz, e);
    } catch (SecurityException e) {
        throw new UnsupportedOperationException("Cannot instantiate class: " + clazz, e);
    }
}