package czsem.gate.plugins;

import gate.Document;
import gate.Factory;
import gate.creole.gazetteer.DefaultGazetteer;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.gate.utils.GateUtils;
import czsem.gate.utils.PRSetup;

public class LevenshteinWholeLineMatchingGazetteerTest {
	
	@Test
	public static void removePunctuation() {
		Assert.assertEquals(
				LevenshteinWholeLineMatchingGazetteer.removePunctuation(
						"-1.2,3:4!5;6(7)8{9}[0]+/"), "1234567890");
	}

	@Test
	public static void inGateTest() throws Exception {
		GateUtils.initGateKeepLog();
		GateUtils.registerAllCzsemPrs();
		
		
		PRSetup[] prs = {
				new PRSetup.SinglePRSetup(LevenshteinWholeLineMatchingGazetteer.class)
				.putFeature(DefaultGazetteer.DEF_GAZ_CASE_SENSITIVE_PARAMETER_NAME, false) 
				.putFeature(DefaultGazetteer.DEF_GAZ_LISTS_URL_PARAMETER_NAME, 
						LevenshteinWholeLineMatchingGazetteerTest.class.getResource("/test_lev_gaze.def"))
		};
		Document doc = Factory.newDocument("textt");
		PRSetup.execGatePipeline(prs, "LevenshteinTest", doc);
		Assert.assertEquals(1, doc.getAnnotations().size());
		Factory.deleteResource(doc);
	}
	
	@Test
	public static void countDistanceOptimized() {
		LevenshteinWholeLineMatchingGazetteer g = new LevenshteinWholeLineMatchingGazetteer();
		
		Assert.assertEquals(g.countDistanceOptimized("aaa", "aaa", 0).distance, 0);
		Assert.assertEquals(g.countDistanceOptimized("aaa", "AAA", 0).distance, 3);
		g.setCaseSensitive(false);
		Assert.assertEquals(g.countDistanceOptimized("aaa", "AAA", 0).distance, 0);
		Assert.assertEquals(g.countDistanceOptimized("a a a", "AAA", 0).distance, 2);
		Assert.assertEquals(g.countDistanceOptimized("a  a  a", "AAA", 0).distance, 2);
		g.setRemoveRedundantSpaces(false);
		Assert.assertEquals(g.countDistanceOptimized("a  a  a", "AAA", 0).distance, 4);
		g.setRemoveAllSpaces(true);
		Assert.assertEquals(g.countDistanceOptimized("a  a  a", "AAA", 0).distance, 0);
		Assert.assertEquals(g.countDistanceOptimized("a  a  b", "AAA", 0).distance, 1);
		Assert.assertEquals(g.countDistanceOptimized("a  a  a", "A\tA\t\tA", 0).distance, 0);

		
		//Non-breaking_space
		Assert.assertEquals(g.countDistanceOptimized("a  a  a", "A aA", 0).distance, 0);
		Assert.assertEquals(g.countDistanceOptimized("a  a\u00a0  a", "A\tA\t\tA", 0).distance, 0);
		g.setRemoveAllSpaces(false);
		g.setRemoveRedundantSpaces(true);
		Assert.assertEquals(g.countDistanceOptimized("a  a", "A\tA", 0).distance, 0);
		Assert.assertEquals(g.countDistanceOptimized("a  a", "A A", 0).distance, 0);
		Assert.assertEquals(g.countDistanceOptimized("a  a", "A \tA", 0).distance, 0);
		Assert.assertEquals(g.countDistanceOptimized("a  a", "A \t            A", 0).distance, 0);
		Assert.assertEquals(g.countDistanceOptimized("a  a  a", "A a A", 0).distance, 0);
		Assert.assertEquals(g.countDistanceOptimized("a  a\u00a0  a", "A\tA\t\tA", 0).distance, 0);

		g.setRemovePunctuation(true);
		Assert.assertEquals(g.countDistanceOptimized("Předávkování : ", "Předávkování", 0).distance, 0);
		g.setRemovePunctuation(false);

		
		Assert.assertTrue(
				g.countDistanceOptimized(
						"                                                            Souhrn údajů o přípravku",
						"4.1. Terapeutické indikace", 0.0).distance > 0);
		Assert.assertTrue(
				g.countDistanceOptimized(
						"                                                            Souhrn údajů o přípravku",
						"4.1. Terapeutické indikace", 1.0).distance > 10);
	}
}
