// buggy code
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

