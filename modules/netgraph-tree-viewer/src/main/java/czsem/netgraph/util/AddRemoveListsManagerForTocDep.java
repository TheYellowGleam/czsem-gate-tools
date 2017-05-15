package czsem.netgraph.util;

import czsem.fs.FSSentenceWriter.TokenDependecy;

public class AddRemoveListsManagerForTocDep extends AddRemoveListsManager<TokenDependecy> {
	private static final long serialVersionUID = -3938877582680436768L;

	@Override
	protected TokenDependecy createModelObjectFromString(String str) {
		
		if (str == null) return null;
		
		String[] split = str.split("\\.", 2);
		
		String tokenTypeName = split[0];
		String depFeatureName = split[0];
		
		if (split.length > 1)
			depFeatureName = split[1];
		
		return new TokenDependecy(tokenTypeName, depFeatureName);
	}
	
	

}
