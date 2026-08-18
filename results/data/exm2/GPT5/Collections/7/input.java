    public Object getProperty(String key) {
        // first, try to get from the 'user value' store
        Object obj = this.get(key);

        if (obj == null) {
            // if there isn't a value there, get it from the
            // defaults if we have them
            if (defaults != null) {
                obj = defaults.get(key);
            }
        }

        return obj;
    }

    private void addPropertyDirect(String key, Object value) {
        // safety check
        if (!containsKey(key)) {
            keysAsListed.add(key);
        }
        put(key, value);
    }

    private void addPropertyInternal(String key, Object value) {
        Object current = this.get(key);

        if (current instanceof String) {
            // one object already in map - convert it to a vector
            List values = new Vector(2);
            values.add(current);
            values.add(value);
            put(key, values);
            
        } else if (current instanceof List) {
            // already a list - just add the new token
            ((List) current).add(value);
            
        } else {
            // brand new key - store in keysAsListed to retain order
            if (!containsKey(key)) {
                keysAsListed.add(key);
            }
            put(key, value);
        }
    }

    public void clearProperty(String key) {
        if (containsKey(key)) {
            // we also need to rebuild the keysAsListed or else
            // things get *very* confusing
            for (int i = 0; i < keysAsListed.size(); i++) {
                if (( keysAsListed.get(i)).equals(key)) {
                    keysAsListed.remove(i);
                    break;
                }
            }
            remove(key);
        }
    }

    public Vector getVector(String key, Vector defaultValue) {
        Object value = get(key);

        if (value instanceof List) {
            return new Vector((List) value);
            
        } else if (value instanceof String) {
            Vector values = new Vector(1);
            values.add(value);
            put(key, values);
            return values;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getVector(key, defaultValue);
            } else {
                return ((defaultValue == null) ? new Vector() : defaultValue);
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Vector object");
        }
    }

    public List getList(String key, List defaultValue) {
        Object value = get(key);

        if (value instanceof List) {
            return new ArrayList((List) value);
            
        } else if (value instanceof String) {
            List values = new ArrayList(1);
            values.add(value);
            put(key, values);
            return values;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getList(key, defaultValue);
            } else {
                return ((defaultValue == null) ? new ArrayList() : defaultValue);
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a List object");
        }
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {

        Object value = get(key);

        if (value instanceof Boolean) {
            return (Boolean) value;
            
        } else if (value instanceof String) {
            String s = testBoolean((String) value);
            Boolean b = new Boolean(s);
            put(key, b);
            return b;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getBoolean(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Boolean object");
        }
    }

    public Byte getByte(String key, Byte defaultValue) {
        Object value = get(key);

        if (value instanceof Byte) {
            return (Byte) value;
            
        } else if (value instanceof String) {
            Byte b = new Byte((String) value);
            put(key, b);
            return b;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getByte(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Byte object");
        }
    }

    public Short getShort(String key, Short defaultValue) {
        Object value = get(key);

        if (value instanceof Short) {
            return (Short) value;
            
        } else if (value instanceof String) {
            Short s = new Short((String) value);
            put(key, s);
            return s;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getShort(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Short object");
        }
    }

    public Integer getInteger(String key, Integer defaultValue) {
        Object value = get(key);

        if (value instanceof Integer) {
            return (Integer) value;
            
        } else if (value instanceof String) {
            Integer i = new Integer((String) value);
            put(key, i);
            return i;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getInteger(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Integer object");
        }
    }

    public Long getLong(String key, Long defaultValue) {
        Object value = get(key);

        if (value instanceof Long) {
            return (Long) value;
            
        } else if (value instanceof String) {
            Long l = new Long((String) value);
            put(key, l);
            return l;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getLong(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Long object");
        }
    }

    public Float getFloat(String key, Float defaultValue) {
        Object value = get(key);

        if (value instanceof Float) {
            return (Float) value;
            
        } else if (value instanceof String) {
            Float f = new Float((String) value);
            put(key, f);
            return f;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getFloat(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Float object");
        }
    }

    public Double getDouble(String key, Double defaultValue) {
        Object value = get(key);

        if (value instanceof Double) {
            return (Double) value;
            
        } else if (value instanceof String) {
            Double d = new Double((String) value);
            put(key, d);
            return d;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getDouble(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Double object");
        }
    }

    public static ExtendedProperties convertProperties(Properties props) {
        ExtendedProperties c = new ExtendedProperties();

        for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
            String s = (String) e.nextElement();
            c.setProperty(s, props.getProperty(s));
        }

        return c;
    }

    public void putAll(Map map) {
        if (map instanceof ExtendedProperties) {
            for (Iterator it = ((ExtendedProperties) map).getKeys(); it.hasNext(); ) {
                Object key = it.next();
                put(key, map.get(key));
            }
        } else {
            for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                put(entry.getKey(), entry.getValue());
            }
        }
    }

// trigger testcase
public void testKeySet2() {
        ExtendedProperties p = new ExtendedProperties();
        p.put("a", "foo");
        p.put("b", "bar");
        p.put("c", "bar");

        Iterator it = p.getKeys();
        assertEquals("a", (String) it.next());
        assertEquals("b", (String) it.next());
        assertEquals("c", (String) it.next());
        assertFalse(it.hasNext());
    }

public void testKeySet3() {
        ExtendedProperties q = new ExtendedProperties();
        q.addProperty("a", "foo");
        q.addProperty("b", "bar");
        q.addProperty("c", "bar");

        ExtendedProperties p = new ExtendedProperties();
        p.putAll(q);

        Iterator it = p.getKeys();
        assertEquals("a", (String) it.next());
        assertEquals("b", (String) it.next());
        assertEquals("c", (String) it.next());
        assertFalse(it.hasNext());
    }

public void testKeySet4() {
        ExtendedProperties q = new ExtendedProperties();
        q.addProperty("a", "foo");
        q.addProperty("b", "bar");
        q.addProperty("c", "bar");

        q.remove("b");

        Iterator it = q.getKeys();
        assertEquals("a", (String) it.next());
        assertEquals("c", (String) it.next());
        assertFalse(it.hasNext());
    }
