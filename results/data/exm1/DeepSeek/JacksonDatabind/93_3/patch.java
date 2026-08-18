    public void validateSubType(DeserializationContext ctxt, JavaType type) throws JsonMappingException
    {
        final Class<?> raw = type.getRawClass();
        String full = raw.getName();

        if (raw.isInterface()) {
            return;
        }

        main_check:
        do {
            if (_cfgIllegalClassNames.contains(full)) {
                break;
            }

            if (full.startsWith(PREFIX_STRING)) {
                for (Class<?> cls = raw; cls != null; cls = cls.getSuperclass()) {
                    String name = cls.getSimpleName();
                    if ("AbstractPointcutAdvisor".equals(name)
                            || "AbstractApplicationContext".equals(name)) {
                        break main_check;
                    }
                }
            }
            return;
        } while (false);

        throw JsonMappingException.from(ctxt,
                String.format("Illegal type (%s) to deserialize: prevented for security reasons", full));
    }