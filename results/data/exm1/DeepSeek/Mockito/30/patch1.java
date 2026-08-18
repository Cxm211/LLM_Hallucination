        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            ObjectMethodsGuru guru = new ObjectMethodsGuru();
            if (guru.isToString(method)) {
                return "SmartNull returned by unstubbed " + formatMethodCall()  + " method on mock";
            }
            if (guru.isHashCode(method)) {
                return new Integer(System.identityHashCode(obj));
            }
            if (guru.isEquals(method)) {
                return args[0] == obj ? Boolean.TRUE : Boolean.FALSE;
            }

            new Reporter().smartNullPointerException(location);
            return null;
        }