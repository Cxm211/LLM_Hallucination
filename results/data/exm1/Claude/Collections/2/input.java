// buggy code
    public String getInclude() {
            return include;  // backwards compatability
    }

    public void setInclude(String inc) {
        include = inc;
    }

    public synchronized void load(InputStream input, String enc) throws IOException {
        PropertiesReader reader = null;
        if (enc != null) {
            try {
                reader = new PropertiesReader(new InputStreamReader(input, enc));
                
            } catch (UnsupportedEncodingException ex) {
                // Another try coming up....
            }
        }
        
        if (reader == null) {
            try {
                reader = new PropertiesReader(new InputStreamReader(input, "8859_1"));
                
            } catch (UnsupportedEncodingException ex) {
                // ISO8859-1 support is required on java platforms but....
                // If it's not supported, use the system default encoding
                reader = new PropertiesReader(new InputStreamReader(input));
            }
        }

        try {
            while (true) {
                String line = reader.readProperty();
                if (line == null) {
                    return;  // EOF
                }
                int equalSign = line.indexOf('=');

                if (equalSign > 0) {
                    String key = line.substring(0, equalSign).trim();
                    String value = line.substring(equalSign + 1).trim();

                    // Configure produces lines like this ... just ignore them
                    if ("".equals(value)) {
                        continue;
                    }

                    if (getInclude() != null && key.equalsIgnoreCase(getInclude())) {
                        // Recursively load properties files.
                        File file = null;

                        if (value.startsWith(fileSeparator)) {
                            // We have an absolute path so we'll use this
                            file = new File(value);
                            
                        } else {
                            // We have a relative path, and we have two 
                            // possible forms here. If we have the "./" form
                            // then just strip that off first before continuing.
                            if (value.startsWith("." + fileSeparator)) {
                                value = value.substring(2);
                            }

                            file = new File(basePath + value);
                        }

                        if (file != null && file.exists() && file.canRead()) {
                            load(new FileInputStream(file));
                        }
                    } else {
                        addProperty(key, value);
                    }
                }
            }
        } finally {
            // Loading is initializing
            isInitialized = true;
        }
    }

// relevant test
// org.apache.commons.collections.TestExtendedProperties::testRetrieve
    public void testRetrieve() {
        
        assertEquals("This returns null", eprop.getProperty("foo"), null);

        
        eprop.setProperty("number", "1");
        assertEquals("This returns '1'", eprop.getProperty("number"), "1");
        assertEquals("This returns '1'", eprop.getString("number"), "1");

        
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
