package fr.insee.rmes.api.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.rmes.api.operations.documentations.CsvRubrique;
import fr.insee.rmes.api.operations.documentations.Document;
import fr.insee.rmes.api.operations.documentations.DocumentationSims;
import fr.insee.rmes.api.operations.documentations.Rubrique;
import fr.insee.rmes.api.utils.CSVUtils;
import fr.insee.rmes.api.utils.ResponseUtils;
import fr.insee.rmes.api.utils.SparqlUtils;

@Path("/operations")
public class OperationsAPI {

	private static Logger logger = LogManager.getLogger(OperationsAPI.class);
	
	@SuppressWarnings("unchecked")
	@Path("/tree")
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getOperationsTree(@HeaderParam("Accept") String header) {
		logger.debug("Received GET request operations tree");
		
		String csvResult = SparqlUtils.executeSparqlQuery(OperationsQueries.getOperationTree());
		List<FamilyToOperation> opList = (List<FamilyToOperation>) CSVUtils.populateMultiPOJO(csvResult, FamilyToOperation.class);
		
		if (opList.size() == 0) return Response.status(Status.NOT_FOUND).entity("").build();
		
		Map<String, Famille> familyMap = new HashMap<String, Famille>();
		Map<String, Serie> serieMap = new HashMap<String, Serie>();

		
		for (FamilyToOperation familyToOperation : opList) {
			if (!serieMap.containsKey(familyToOperation.getSeriesId())) {
				Serie s = new Serie(familyToOperation.getSeries(),familyToOperation.getSeriesId(),familyToOperation.getSeriesLabelLg1(), familyToOperation.getSeriesLabelLg2());
				serieMap.put(s.getId(), s);
				String fId = familyToOperation.getFamilyId();
				if (familyMap.containsKey(fId)) {
					familyMap.get(fId).addSerie(s);			
				}else {//create family
					Famille f = new Famille(familyToOperation.getFamily(),familyToOperation.getFamilyId(), familyToOperation.getFamilyLabelLg1(),familyToOperation.getFamilyLabelLg2(), s);
					familyMap.put(f.getId(), f);
				}
			}
			if (StringUtils.isNotEmpty(familyToOperation.getOperationId())) {
				Operation o = new Operation(familyToOperation.getOperation(),familyToOperation.getOperationId(),familyToOperation.getOpLabelLg1(), familyToOperation.getOpLabelLg2(), familyToOperation.getSimsId());
				serieMap.get(familyToOperation.getSeriesId()).addOperation(o);
			}else if (StringUtils.isNotEmpty(familyToOperation.getIndicId())) {
				Indicateur i = new Indicateur(familyToOperation.getIndic(),familyToOperation.getIndicId(),familyToOperation.getIndicLabelLg1(), familyToOperation.getIndicLabelLg2(), familyToOperation.getSimsId());
				serieMap.get(familyToOperation.getSeriesId()).addIndicateur(i);
			}else if (StringUtils.isNotEmpty(familyToOperation.getSimsId() )) { //sims linked to serie
				serieMap.get(familyToOperation.getSeriesId()).setSimsId(familyToOperation.getSimsId());
			}
		}
		
		
		if (header.equals(MediaType.APPLICATION_XML)) {
			Familles familles = new Familles(new ArrayList<Famille>(familyMap.values()));
			return Response.ok(ResponseUtils.produceResponse(familles, header)).build();
		}
		else return Response.ok(ResponseUtils.produceResponse(familyMap.values(), header)).build();
		
		
	}
	
	@SuppressWarnings("unchecked")
	@Path("/documentation/{id: [0-9]{4}}")
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getDocumentation(@HeaderParam("Accept") String header, @PathParam("id") String id) {
		logger.debug("Received GET request documentation");
		
		String csvResult = SparqlUtils.executeSparqlQuery(OperationsQueries.getDocumentationTitle(id));
		DocumentationSims sims =  new DocumentationSims();
		CSVUtils.populatePOJO(csvResult, sims);
				
		if (sims.getUri() == null) return Response.status(Status.NOT_FOUND).entity("").build();
		
		csvResult = SparqlUtils.executeSparqlQuery(OperationsQueries.getDocumentationRubrics(id));
		List<CsvRubrique> csvRubriques = (List<CsvRubrique>) CSVUtils.populateMultiPOJO(csvResult, CsvRubrique.class);
		List<Rubrique> rubriques = new ArrayList<>();
		for (CsvRubrique cr : csvRubriques) {
			Rubrique r = new Rubrique(cr.getId(),cr.getUri(),cr.getType());
			r.setTitre(cr.getTitreLg1(),cr.getTitreLg2());
			r.setIdParent(cr.getIdParent());
			switch (cr.getType()) {
			case "DATE":
				r.setValeurSimple(cr.getValeurSimple());
				break;
			case "CODE_LIST":
				SimpleObject valeurCode = new SimpleObject(cr.getValeurSimple(), cr.getCodeUri(), cr.getLabelObjLg1(), cr.getLabelObjLg2());
				r.setValeurCode(valeurCode);
				break;	
			case "ORGANISATION":
				SimpleObject valeurOrg = new SimpleObject(cr.getValeurSimple(), cr.getOrganisationUri(), cr.getLabelObjLg1(), cr.getLabelObjLg2());
				r.setValeurOrganisation(valeurOrg);
				break;	
			case "RICH_TEXT":
				if (cr.getHasDoc()) {
				String csvDocs = SparqlUtils.executeSparqlQuery(OperationsQueries.getDocuments(id,r.getId()));
				List<Document> docs = (List<Document>) CSVUtils.populateMultiPOJO(csvDocs, Document.class);
				r.setDocuments(docs);}
			case "TEXT":
				r.setLabelLg1(cr.getLabelLg1());
				r.setLabelLg2(cr.getLabelLg2());
				break;	

			default:
				break;
			}
			rubriques.add(r);
		}
		sims.setRubriques(rubriques);
		
		return Response.ok(ResponseUtils.produceResponse(sims, header)).build();
	}

}

