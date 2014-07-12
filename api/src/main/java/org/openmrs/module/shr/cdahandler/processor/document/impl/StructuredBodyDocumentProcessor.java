package org.openmrs.module.shr.cdahandler.processor.document.impl;

import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;

/**
 * This is a generic document processor which can interpret any CDA
 * document with a structured body content (i.e. Level 2)
 * @author Justin Fyfe
 *
 */
public class StructuredBodyDocumentProcessor extends DocumentProcessorImpl {


	/**
	 * Implementation of validate for Processor
	 */
	@Override
	public Boolean validate(IGraphable object) {

		
		// Ensure that the document body is in fact structured
		Boolean isValid = super.validate(object);
		if(!isValid) return isValid;
		
		ClinicalDocument doc = (ClinicalDocument)object;
		
		// Must have component to be valid CDA
		if(doc.getComponent() == null || doc.getComponent().getNullFlavor() != null)
		{
			log.error(String.format("Document %s is missing component", doc.getId().toString()));
			isValid = false;
		}
		// Must have BodyChoice of StructuredBody
		else if(doc.getComponent().getBodyChoice() == null || 
				doc.getComponent().getBodyChoice().isPOCD_MT000040UVNonXMLBody() || 
				doc.getComponent().getBodyChoice().getNullFlavor() != null)
		{
			log.error(String.format("Document %s is missing body of structuredBody", doc.getId().toString()));
			isValid = false;
		}

		// Must have at least one section
		if(doc.getComponent().getBodyChoiceIfStructuredBody().getComponent().size() == 0 ||
				doc.getComponent().getBodyChoiceIfStructuredBody().getComponent().get(0).getNullFlavor() != null)
		{
			log.error(String.format("Document %s must have at least one entry", doc.getId().toString()));
			isValid = false;
		}
		return isValid;
		
	}

	/**
	 * Get the template name .. Since this is a generic handler it has no template name
	 */
	@Override
	public String getTemplateName() {
		return null;
	}
	
	
}
