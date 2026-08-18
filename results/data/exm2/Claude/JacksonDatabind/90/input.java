    public boolean canInstantiate() {
        return canCreateUsingDefault()
                || canCreateUsingDelegate() 
                || canCreateFromObjectWith() || canCreateFromString()
                || canCreateFromInt() || canCreateFromLong()
                || canCreateFromDouble() || canCreateFromBoolean();
    }

    public boolean canCreateFromObjectWith() {
        return (_withArgsCreator != null);
    }

// trigger testcase
public void testDelegatingArray1804() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MyType thing = mapper.readValue("[]", MyType.class);
        assertNotNull(thing);
    }
