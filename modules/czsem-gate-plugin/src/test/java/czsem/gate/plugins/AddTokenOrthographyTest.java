package czsem.gate.plugins;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AddTokenOrthographyTest {

	@Test
	public static void getOrthographyValue() {
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("dedek"), "lowercase");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("ded-ek"), "lowercase");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("děd-ek"), "lowercase");
		
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("Dedek"), "upperInitial");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("Ded-ek"), "upperInitial");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("Jañ"), "upperInitial");
		
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("DEDEK"), "allCaps");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("DED-EK"), "allCaps");
		
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("DedekJ"), "mixedCaps");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("Dedek-J"), "mixedCaps");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("JaÑ"), "mixedCaps");

		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("123"), null);
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("-123.5"), null);

		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("jan dědek"), "lowercase");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("jan_dědek"), "lowercase");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("jan/dědek"), "lowercase");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("jan+dědek"), "lowercase");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("jan1dědek"), "lowercase");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("jan!dedek"), "lowercase");
		
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("chlopeˇn"), "lowercase");

		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("TRITT|CO"), "allCaps");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("TRITT1CO"), "allCaps");

		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("R1234R"), "allCaps");
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("R1234"), null);
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("12R34"), null);
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("1RRRRR"), null);
		Assert.assertEquals(AddTokenOrthography.getOrthographyValue("|RRRRR"), null);
	}
}
