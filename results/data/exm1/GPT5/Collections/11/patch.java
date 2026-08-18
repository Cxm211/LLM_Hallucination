private void calculateHashCode(Object[] keys)
	{
		int total = 0;
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                Object key = keys[i];
                total ^= (key == null ? 0 : key.hashCode());
            }
        }
        hashCode = total;
	}