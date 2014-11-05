package it.cnr.isti.hpc.dexter.annotate.wikinews;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WikiNewsTest {

	@Test
	public void testRemoveTemplates() throws Exception {
		String removed = WikiNews
				.removeTemplates("TEMPLATE[United_Kingdom] The governor");
		System.out.println(removed);
		assertEquals("June 5, 2014",
				WikiNews.removeTemplates("TEMPLATE[date, June 5, 2014]"));

		assertEquals("Falungong",
				WikiNews.removeTemplates("TEMPLATE[w, Falungong]"));
		assertEquals(
				"June 5, 2014 Falungong",
				WikiNews.removeTemplates("TEMPLATE[date, June 5, 2014] TEMPLATE[w, Falungong]"));
		assertEquals("qui, quo, qua",
				WikiNews.removeTemplates("TEMPLATE[w,qui,quo,qua]"));

		assertEquals(
				"Labour",
				WikiNews.removeTemplates("TEMPLATE[w, Labour Party (UK), Labour]"));

		assertEquals(
				"The United States, which signed but did not ratify the statute during the Bill Clinton administration",
				WikiNews.removeTemplates("The United States, which signed but did not ratify the statute during the TEMPLATE[w, Bill Clinton] administration"));

	}
}
