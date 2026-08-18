// buggy function
	private void calculateHashCode(Object[] keys)
	{
		int total = 0;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != null) {
                total ^= keys[i].hashCode();
            }
        }
        hashCode = total;
	}

// trigger testcase
// org/apache/commons/collections/keyvalue/TestMultiKey.java::testEqualsAfterSerialization
public void testEqualsAfterSerialization() throws IOException, ClassNotFoundException
	{
        SystemHashCodeSimulatingKey sysKey = new SystemHashCodeSimulatingKey("test");
		MultiKey mk = new MultiKey(ONE, sysKey);
        Map map = new HashMap();
        map.put(mk, TWO);

        // serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(sysKey);
        out.writeObject(map);
        out.close();

        // deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);
        sysKey = (SystemHashCodeSimulatingKey)in.readObject(); // simulate deserialization in another process
        Map map2 = (Map) in.readObject();
        in.close();

        assertEquals(2, sysKey.hashCode()); // different hashCode now

        MultiKey mk2 = new MultiKey(ONE, sysKey);
        assertEquals(TWO, map2.get(mk2));		
	}
