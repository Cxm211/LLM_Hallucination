// ===== FIXED org.apache.commons.lang3.StringUtils :: containsAny(CharSequence, char[]) [lines 1440-1465] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-31-fixed/src/main/java/org/apache/commons/lang3/StringUtils.java =====
	public static boolean containsAny(CharSequence cs, char[] searchChars) {
		if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
			return false;
		}
		int csLength = cs.length();
		int searchLength = searchChars.length;
		int csLastIndex = csLength - 1;
		int searchLastIndex = searchLength - 1;
		for (int i = 0; i < csLength; i++) {
			char ch = cs.charAt(i);
			for (int j = 0; j < searchLength; j++) {
				if (searchChars[j] == ch) {
					if (i < csLastIndex && j < searchLastIndex && ch >= Character.MIN_HIGH_SURROGATE && ch <= Character.MAX_HIGH_SURROGATE) {
						// ch is a supplementary character
						if (searchChars[j + 1] == cs.charAt(i + 1)) {
							return true;
						}
					} else {
						// ch is in the Basic Multilingual Plane
						return true;
					}
				}
			}
		}
		return false;
	}
