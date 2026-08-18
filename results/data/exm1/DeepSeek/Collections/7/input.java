// buggy code
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

// relevant test
// org.apache.commons.collections.TestExtendedProperties::testRetrieve
    public void testRetrieve() {
        
        assertNull("This returns null", eprop.getProperty("foo"));

        
        eprop.setProperty("number", "1");
        assertEquals("This returns '1'", "1", eprop.getProperty("number"));
        assertEquals("This returns '1'", "1", eprop.getString("number"));

        
        eprop.addProperty("number", "2");
        assertTrue("This returns array", (eprop.getVector("number") instanceof java.util.Vector));
        assertTrue("This returns array", (eprop.getList("number") instanceof java.util.List));

        
        assertTrue("This returns scalar", (eprop.getString("number") instanceof String));

        
        String prop = "hey, that's a test";
        eprop.setProperty("prop.string", prop);
        assertTrue("This returns vector", (eprop.getVector("prop.string") instanceof java.util.Vector));
        assertTrue("This returns list", (eprop.getList("prop.string") instanceof java.util.List));

        String prop2 = "hey\\, that's a test";
        eprop.remove("prop.string");
        eprop.setProperty("prop.string", prop2);
        assertTrue("This returns array", (eprop.getString("prop.string") instanceof java.lang.String));

        

        ExtendedProperties subEprop = eprop.subset("prop");

        assertTrue("Returns the full string", subEprop.getString("string").equals(prop));
        assertTrue("This returns string for subset", (subEprop.getString("string") instanceof java.lang.String));
        assertTrue("This returns array for subset", (subEprop.getVector("string") instanceof java.util.Vector));
        assertTrue("This returns array for subset", (subEprop.getList("string") instanceof java.util.List));

    }

// org.apache.commons.collections.TestExtendedProperties::testInterpolation
    public void testInterpolation() {
        eprop.setProperty("applicationRoot", "/home/applicationRoot");
        eprop.setProperty("db", "${applicationRoot}/db/hypersonic");
        String dbProp = "/home/applicationRoot/db/hypersonic";
        assertTrue("Checking interpolated variable", eprop.getString("db").equals(dbProp));
    }

// org.apache.commons.collections.TestExtendedProperties::testSaveAndLoad
    public void testSaveAndLoad() {
        ExtendedProperties ep1 = new ExtendedProperties();
        ExtendedProperties ep2 = new ExtendedProperties();

        try {
            
            String s1 = "one=Hello\\World\ntwo=Hello\\,World\nthree=Hello,World";
            byte[] bytes = s1.getBytes();
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ep1.load(bais);
            assertEquals("Back-slashes not interpreted properly", 
                    "Hello\\World", ep1.getString("one"));
            assertEquals("Escaped commas not interpreted properly", 
                    "Hello,World", ep1.getString("two"));
            assertEquals("Commas not interpreted properly", 
                    2, ep1.getVector("three").size());
            assertEquals("Commas not interpreted properly", 
                    "Hello", ep1.getVector("three").get(0));
            assertEquals("Commas not interpreted properly", 
                    "World", ep1.getVector("three").get(1));

            assertEquals("Commas not interpreted properly", 
                    2, ep1.getList("three").size());
            assertEquals("Commas not interpreted properly", 
                    "Hello", ep1.getList("three").get(0));
            assertEquals("Commas not interpreted properly", 
                    "World", ep1.getList("three").get(1));
                    
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ep1.save(baos, null);
            bytes = baos.toByteArray();
            bais = new ByteArrayInputStream(bytes);
            ep2.load(bais);
            assertEquals("Back-slash not same after being saved and loaded",
                    ep1.getString("one"), ep2.getString("one"));
            assertEquals("Escaped comma not same after being saved and loaded",
                    ep1.getString("two"), ep2.getString("two"));
            assertEquals("Comma not same after being saved and loaded",
                    ep1.getString("three"), ep2.getString("three"));
        } catch (IOException ioe) {
            fail("There was an exception saving and loading the EP");
        }
    }

// org.apache.commons.collections.TestExtendedProperties::testTrailingBackSlash
    public void testTrailingBackSlash() {
        ExtendedProperties ep1 = new ExtendedProperties();

        try {
            
            String s1 = "one=ONE\ntwo=TWO \\\\\nthree=THREE";
            byte[] bytes = s1.getBytes();
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ep1.load(bais);
            assertEquals("Trailing back-slashes not interpreted properly", 
                    3, ep1.size());
            assertEquals("Back-slash not escaped properly", 
                    "TWO \\", ep1.getString("two"));
        } catch (IOException ioe) {
            fail("There was an exception loading the EP");
        }
    }

// org.apache.commons.collections.TestExtendedProperties::testMultipleSameKey1
    public void testMultipleSameKey1() throws Exception {
        ExtendedProperties ep1 = new ExtendedProperties();

        
        String s1 = "one=a\none=b,c\n";
        byte[] bytes = s1.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ep1.load(bais);
        assertEquals(1, ep1.size());

        assertEquals(3, ep1.getVector("one").size());
        assertEquals("a", ep1.getVector("one").get(0));
        assertEquals("b", ep1.getVector("one").get(1));
        assertEquals("c", ep1.getVector("one").get(2));

        assertEquals(3, ep1.getList("one").size());
        assertEquals("a", ep1.getList("one").get(0));
        assertEquals("b", ep1.getList("one").get(1));
        assertEquals("c", ep1.getList("one").get(2));
    }

// org.apache.commons.collections.TestExtendedProperties::testMultipleSameKey2
    public void testMultipleSameKey2() throws Exception {
        ExtendedProperties ep1 = new ExtendedProperties();

        
        String s1 = "one=a,b\none=c,d\n";
        byte[] bytes = s1.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ep1.load(bais);
        assertEquals(1, ep1.size());

        assertEquals(4, ep1.getVector("one").size());
        assertEquals("a", ep1.getVector("one").get(0));
        assertEquals("b", ep1.getVector("one").get(1));
        assertEquals("c", ep1.getVector("one").get(2));
        assertEquals("d", ep1.getVector("one").get(3));

        assertEquals(4, ep1.getList("one").size());
        assertEquals("a", ep1.getList("one").get(0));
        assertEquals("b", ep1.getList("one").get(1));
        assertEquals("c", ep1.getList("one").get(2));
        assertEquals("d", ep1.getList("one").get(3));
    }

// org.apache.commons.collections.TestExtendedProperties::testMultipleSameKey3
    public void testMultipleSameKey3() throws Exception {
        ExtendedProperties ep1 = new ExtendedProperties();

        
        String s1 = "one=a,b\none=c\n";
        byte[] bytes = s1.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ep1.load(bais);
        assertEquals(1, ep1.size());

        assertEquals(3, ep1.getVector("one").size());
        assertEquals("a", ep1.getVector("one").get(0));
        assertEquals("b", ep1.getVector("one").get(1));
        assertEquals("c", ep1.getVector("one").get(2));

        assertEquals(3, ep1.getList("one").size());
        assertEquals("a", ep1.getList("one").get(0));
        assertEquals("b", ep1.getList("one").get(1));
        assertEquals("c", ep1.getList("one").get(2));
    }

// org.apache.commons.collections.TestExtendedProperties::testMultipleSameKeyByCode
    public void testMultipleSameKeyByCode() throws Exception {
        ExtendedProperties ep1 = new ExtendedProperties();

        ep1.addProperty("one", "a");
        assertEquals(1, ep1.size());

        assertEquals(1, ep1.getVector("one").size());
        assertEquals("a", ep1.getVector("one").get(0));

        assertEquals(1, ep1.getList("one").size());
        assertEquals("a", ep1.getList("one").get(0));
        
        ep1.addProperty("one", Boolean.TRUE);
        assertEquals(1, ep1.size());

        assertEquals(2, ep1.getVector("one").size());
        assertEquals("a", ep1.getVector("one").get(0));
        assertEquals(Boolean.TRUE, ep1.getVector("one").get(1));

        assertEquals(2, ep1.getList("one").size());
        assertEquals("a", ep1.getList("one").get(0));
        assertEquals(Boolean.TRUE, ep1.getList("one").get(1));
        
        ep1.addProperty("one", "c,d");
        assertEquals(1, ep1.size());

        assertEquals(4, ep1.getVector("one").size());
        assertEquals("a", ep1.getVector("one").get(0));
        assertEquals(Boolean.TRUE, ep1.getVector("one").get(1));
        assertEquals("c", ep1.getVector("one").get(2));
        assertEquals("d", ep1.getVector("one").get(3));

        assertEquals(4, ep1.getList("one").size());
        assertEquals("a", ep1.getList("one").get(0));
        assertEquals(Boolean.TRUE, ep1.getList("one").get(1));
        assertEquals("c", ep1.getList("one").get(2));
        assertEquals("d", ep1.getList("one").get(3));
    }

// org.apache.commons.collections.TestExtendedProperties::testInheritDefaultProperties
    public void testInheritDefaultProperties() {
        Properties defaults = new Properties();
        defaults.setProperty("resource.loader", "class");

        Properties properties = new Properties(defaults);
        properties.setProperty("test", "foo");

        ExtendedProperties extended = ExtendedProperties.convertProperties(properties);

        assertEquals("foo", extended.getString("test"));
        assertEquals("class", extended.getString("resource.loader"));
    }

// org.apache.commons.collections.TestExtendedProperties::testInclude
    public void testInclude() {
        ExtendedProperties a = new ExtendedProperties();
        ExtendedProperties b = new ExtendedProperties();
        
        assertEquals("include", a.getInclude());
        assertEquals("include", b.getInclude());
        
        a.setInclude("import");
        assertEquals("import", a.getInclude());
        assertEquals("include", b.getInclude());
        
        a.setInclude("");
        assertEquals(null, a.getInclude());
        assertEquals("include", b.getInclude());
        
        a.setInclude("hi");
        assertEquals("hi", a.getInclude());
        assertEquals("include", b.getInclude());
        
        a.setInclude(null);
        assertEquals(null, a.getInclude());
        assertEquals("include", b.getInclude());
    }

// org.apache.commons.collections.TestExtendedProperties::testKeySet1
    public void testKeySet1() {
            ExtendedProperties p = new ExtendedProperties();
            p.addProperty("a", "foo");
            p.addProperty("b", "bar");
            p.addProperty("c", "bar");

            Iterator it = p.getKeys();
            assertEquals("a", (String) it.next());
            assertEquals("b", (String) it.next());
            assertEquals("c", (String) it.next());
            assertFalse(it.hasNext());
    }

// org.apache.commons.collections.TestExtendedProperties::testKeySet2
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

// org.apache.commons.collections.TestExtendedProperties::testKeySet3
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

// org.apache.commons.collections.TestExtendedProperties::testKeySet4
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
