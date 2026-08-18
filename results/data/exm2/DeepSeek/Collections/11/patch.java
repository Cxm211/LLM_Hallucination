 private void calculateHashCode(Object[] keys)
	{
		int total = 0;
        for (int i = 0; i < keys.length; i++) {
            total = total * 31 + (keys[i] != null ? keys[i].hashCode() : 0);
        }
        hashCode = total;
	}