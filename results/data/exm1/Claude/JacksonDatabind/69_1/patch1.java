public void addPropertyCreator(AnnotatedWithParams creator, boolean explicit,
            SettableBeanProperty[] properties)
    {
        verifyNonDup(creator, C_PROPS, explicit);
        _propertyBasedCreator = _fixAccess(creator);
        if (properties.length > 1) {
            HashMap<String,Integer> names = new HashMap<String,Integer>();
            for (int i = 0, len = properties.length; i < len; ++i) {
                String name = properties[i].getName();
                if (name.length() == 0 && properties[i].getInjectableValueId() != null) {
                    continue;
                }
                Integer old = names.put(name, Integer.valueOf(i));
                if (old != null) {
                    throw new IllegalArgumentException("Duplicate creator property \""+name+"\" (index "+old+" vs "+i+")");
                }
            }
        }
        _propertyBasedArgs = properties;
    }