    protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        // Should we check that there is a sub-class relationship?
        // 15-Jan-2016, tatu: Almost yes, but there are some complications with
        //    placeholder values (`Void`, `NoClass`), so can not quite do yet.
        // TODO: fix in 2.8
            /*
            throw new IllegalArgumentException("Class "+subclass.getName()+" not sub-type of "
                    +_class.getName());
                    */
            return new SimpleType(subclass, _bindings, this, _superInterfaces,
                    _valueHandler, _typeHandler, _asStatic);
        // Otherwise, stitch together the hierarchy. First, super-class
        // if not found, try a super-interface
        // should not get here but...
    }

// trigger testcase
public void testIssue1125WithDefault() throws Exception
    {
        Issue1125Wrapper result = MAPPER.readValue(aposToQuotes("{'value':{'a':3,'def':9,'b':5}}"),
        		Issue1125Wrapper.class);
        assertNotNull(result.value);
        assertEquals(Default1125.class, result.value.getClass());
        Default1125 impl = (Default1125) result.value;
        assertEquals(3, impl.a);
        assertEquals(5, impl.b);
        assertEquals(9, impl.def);
    }
