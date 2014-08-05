package org.openmrs.module.shr.cdahandler.api.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.interfaces.IResultDetail;
import org.marc.everest.rmim.uv.cdar2.rim.InfrastructureRoot;
import org.openmrs.GlobalProperty;
import org.openmrs.Relationship;
import org.openmrs.Visit;
import org.openmrs.activelist.Allergy;
import org.openmrs.activelist.AllergySeverity;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.api.CdaImportService;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfiguration;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.AntepartumHistoryAndPhysicalDocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.MedicalDocumentsDocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.MedicalSummaryDocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsConceptUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;


/**
 * Test class for CdaImportServiceImpl
 */
public class CdaImportServiceImplTest extends BaseModuleContextSensitiveTest  {
	
	private static final String ACTIVE_LIST_INITIAL_XML = "include/ActiveListTest.xml";
	protected final Log log = LogFactory.getLog(this.getClass());
	private CdaImportService m_service;

	@Before
	public void beforeEachTest() throws Exception {

		this.m_service = Context.getService(CdaImportService.class);
		GlobalProperty saveDir = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR, "C:\\data\\");
		Context.getAdministrationService().setGlobalProperty(CdaHandlerConfiguration.PROP_VALIDATE_CONCEPT_STRUCTURE, "false");
		Context.getAdministrationService().saveGlobalProperty(saveDir);
		executeDataSet(ACTIVE_LIST_INITIAL_XML);
		// TODO: Set properties
	}
	
	/**
	 * Do the parsing of a CDA
	 */
	private String doParseCda(String resourceName)
	{
		URL validAphpSample = this.getClass().getResource(resourceName);
		File fileUnderTest = new File(validAphpSample.getFile());
		FileInputStream fs = null;
		try
		{
			fs = new FileInputStream(fileUnderTest);
			Visit parsedVisit = this.m_service.importDocument(fs);
			assertEquals(parsedVisit, Context.getVisitService().getVisitByUuid(parsedVisit.getUuid()));
			return parsedVisit.getUuid();
		}
		catch(DocumentValidationException e)
		{
			log.error(String.format("Error in %s", FormatterUtil.toWireFormat(((InfrastructureRoot)e.getTarget()).getTemplateId())));
			for(IResultDetail dtl : e.getValidationIssues())
				log.error(String.format("%s %s", dtl.getType(), dtl.getMessage()));
			return null;
		}
		catch(DocumentImportException e)
		{
			log.error("Error generated", e);
			return null;
		}
        catch (FileNotFoundException e) {
	        // TODO Auto-generated catch block
	        log.error("Error generated", e);
	        return null;
        }

	}
	
	@Test
	public void shouldParseValidAphpTest() {
		String id = this.doParseCda("/validAphpSample.xml");
		assertTrue(id != null);
		assertEquals(new AntepartumHistoryAndPhysicalDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());

	}

	@Test
	public void shouldParseValidAphpPovichTest() {
		String id = this.doParseCda("/validAphpSamplePovich.xml");
		assertTrue(id != null);
		assertEquals(new AntepartumHistoryAndPhysicalDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());

	}

	@Test
	public void shouldParseValidAphpFullTest() throws DocumentImportException {
		OpenmrsConceptUtil.getInstance().createConcept(new CV<String>("49051-6", CdaHandlerConstants.CODE_SYSTEM_LOINC), new PQ(BigDecimal.ONE, "wks"));
		OpenmrsConceptUtil.getInstance().createConcept(new CV<String>("45371-2", CdaHandlerConstants.CODE_SYSTEM_LOINC), null);
		String id = this.doParseCda("/validAphpSampleFullSections.xml");
		assertTrue(id != null);
		assertEquals(new AntepartumHistoryAndPhysicalDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());

	}

	@Test
	public void shouldParseValidAphpPovichTest2() {
		String id = this.doParseCda("/validAphpSamplePovich.xml");
		assertTrue(id != null);
		assertEquals(new AntepartumHistoryAndPhysicalDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());
	}
	
	@Test
	public void shouldParseValidLevel3Test() {
		String id = this.doParseCda("/validCdaLevel3Sample.xml");
		assertTrue(id != null);
		assertEquals(new MedicalDocumentsDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());

	}

	@Test
	public void shouldParseValidCdaFromOscarTest() {
		String id = this.doParseCda("/cdaFromOscarEmr.xml");
		assertTrue(id != null);
	}

	@Test
	public void shouldParseValidCdaFromOscarTest2() {
		String id = this.doParseCda("/cdaFromOscarEmr2.xml");
		assertTrue(id != null);
		assertEquals(new MedicalSummaryDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());
	}
	
	@Test
	public void shouldParseValidLevel3Test2() {
		String id = this.doParseCda("/validCdaLevel3Sample2.xml");
		assertTrue(id != null);
		assertEquals(new MedicalSummaryDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());
	}

	@Test
	public void shouldParseCdaFromHl7() {
		String id = this.doParseCda("/cdaFromHl7.xml");
		assertTrue(id != null);
		assertEquals(new AntepartumHistoryAndPhysicalDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());
	}

}