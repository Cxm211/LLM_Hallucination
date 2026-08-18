    protected final boolean _add(Annotation ann) {
        if (_annotations == null) {
            _annotations = new HashMap<Class<? extends Annotation>,Annotation>();
        }
        Annotation previous = _annotations.put(ann.annotationType(), ann);
        return (previous != null) && previous.equals(ann);
    }

// trigger testcase
public void testMixinWithBundles() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper().addMixIn(Foo.class, FooMixin.class);
        String result = mapper.writeValueAsString(new Foo("result"));
        assertEquals("{\"bar\":\"result\"}", result);
    }
