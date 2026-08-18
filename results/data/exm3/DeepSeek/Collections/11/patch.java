private void calculateHashCode(Object[] keys)
	{
		int total = 0;
        for (int i = 0; i < keys.length; i++) {
            total = 31 * total + (keys[i] != null ? keys[i].hashCode() : 0);
        }
        hashCode = total;
	}