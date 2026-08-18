public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || o.getClass() != this.getClass()) return false;
            try {
                java.lang.reflect.Field f = o.getClass().getDeclaredField("method");
                f.setAccessible(true);
                Object otherMethod = f.get(o);
                Object thisMethod = this.method;
                return thisMethod == null ? otherMethod == null : thisMethod.equals(otherMethod);
            } catch (Exception e) {
                return false;
            }
    }