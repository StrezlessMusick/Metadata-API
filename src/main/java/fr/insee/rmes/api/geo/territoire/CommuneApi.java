package fr.insee.rmes.api.geo.territoire;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.rmes.api.geo.AbstractGeoApi;
import fr.insee.rmes.api.geo.ConstGeoApi;
import fr.insee.rmes.modeles.geo.territoire.Commune;
import fr.insee.rmes.modeles.geo.territoire.Territoire;
import fr.insee.rmes.modeles.geo.territoires.Communes;
import fr.insee.rmes.modeles.geo.territoires.Territoires;
import fr.insee.rmes.queries.geo.GeoQueries;
import fr.insee.rmes.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path(ConstGeoApi.PATH_GEO)
@Tag(name = ConstGeoApi.TAG_NAME, description = ConstGeoApi.TAG_DESCRIPTION)
public class CommuneApi extends AbstractGeoApi {

    private static final String LITTERAL_DATE_EXAMPLE = "1945-06-26";

    private static Logger logger = LogManager.getLogger(CommuneApi.class);

    private static final String CODE_PATTERN = "/{code: " + ConstGeoApi.PATTERN_COMMUNE + "}";
    private static final String LITTERAL_ID_OPERATION = "getcogcom";
    private static final String LITTERAL_OPERATION_SUMMARY =
        "Informations sur une commune française identifiée par son code (cinq caractères)";
    private static final String LITTERAL_OPERATION_DESCRIPTION =
        "Cette requête renvoie également les communes des collectivités d'Outre-Mer";
    private static final String LITTERAL_RESPONSE_DESCRIPTION = "Commune";
    private static final String LITTERAL_PARAMETER_DATE_DESCRIPTION =
        "Filtre pour renvoyer la commune active à la date donnée. Par défaut, c’est la date courante. (Format : 'AAAA-MM-JJ')";
    private static final String LITTERAL_PARAMETER_TYPE_DESCRIPTION = "Filtre sur le type de territoire renvoyé.";

    private static final String LITTERAL_CODE_EXAMPLE = "14475";

    
    @Path(ConstGeoApi.PATH_COMMUNE + CODE_PATTERN)
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(
        operationId = LITTERAL_ID_OPERATION,
        summary = LITTERAL_OPERATION_SUMMARY,
        description = LITTERAL_OPERATION_DESCRIPTION,
        responses = {
            @ApiResponse(
                content = @Content(schema = @Schema(implementation = Commune.class)),
                description = LITTERAL_RESPONSE_DESCRIPTION)
        })
    public Response getByCode(
        @Parameter(
            description = ConstGeoApi.PATTERN_COMMUNE_DESCRIPTION,
            required = true,
            schema = @Schema(
                pattern = ConstGeoApi.PATTERN_COMMUNE,
                type = Constants.TYPE_STRING, example=LITTERAL_CODE_EXAMPLE)) @PathParam(Constants.CODE) String code,
        @Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header,
        @Parameter(
            description = LITTERAL_PARAMETER_DATE_DESCRIPTION,
            required = false,
            schema = @Schema(type = Constants.TYPE_STRING, format = Constants.FORMAT_DATE)) @QueryParam(
                value = Constants.PARAMETER_DATE) String date) {

        logger.debug("Received GET request for commune {}", code);

        if ( ! this.verifyParameterDateIsRight(date)) {
            return this.generateBadRequestResponse();
        }
        else {
            return this
                .generateResponseATerritoireByCode(
                    sparqlUtils
                        .executeSparqlQuery(
                            GeoQueries.getCommuneByCodeAndDate(code, this.formatValidParameterDateIfIsNull(date))),
                    header,
                    new Commune(code));
        }
    }

    @Path(ConstGeoApi.PATH_COMMUNE + CODE_PATTERN + ConstGeoApi.PATH_ASCENDANT)
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(
        operationId = LITTERAL_ID_OPERATION + ConstGeoApi.ID_OPERATION_ASCENDANTS,
        summary = "Récupérer les informations concernant les territoires qui contiennent la commune",
        description = LITTERAL_OPERATION_DESCRIPTION,
        responses = {
            @ApiResponse(
                content = @Content(schema = @Schema(type = ARRAY, implementation = Territoire.class)),
                description = LITTERAL_RESPONSE_DESCRIPTION)
        })
    public Response getAscendants(
        @Parameter(
            description = ConstGeoApi.PATTERN_COMMUNE_DESCRIPTION,
            required = true,
            schema = @Schema(
                pattern = ConstGeoApi.PATTERN_COMMUNE,
                type = Constants.TYPE_STRING, example=LITTERAL_CODE_EXAMPLE)) @PathParam(Constants.CODE) String code,
        @Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header,
        @Parameter(
            description = LITTERAL_PARAMETER_DATE_DESCRIPTION,
            required = false,
            schema = @Schema(type = Constants.TYPE_STRING, format = Constants.FORMAT_DATE)) @QueryParam(
                value = Constants.PARAMETER_DATE) String date,
        @Parameter(
            description = LITTERAL_PARAMETER_TYPE_DESCRIPTION,
            required = false,
            schema = @Schema(type = Constants.TYPE_STRING)) @QueryParam(
                value = Constants.PARAMETER_TYPE) String typeTerritoire) {

        logger.debug("Received GET request for ascendants of commune {}", code);

        if ( ! this.verifyParametersTypeAndDateAreValid(typeTerritoire, date)) {
            return this.generateBadRequestResponse();
        }
        else {
            return this
                .generateResponseListOfTerritoire(
                    sparqlUtils
                        .executeSparqlQuery(
                            GeoQueries
                                .getAscendantsCommune(
                                    code,
                                    this.formatValidParameterDateIfIsNull(date),
                                    this.formatValidParametertypeTerritoireIfIsNull(typeTerritoire))),
                    header,
                    Territoires.class,
                    Territoire.class);
        }
    }

    @Path(ConstGeoApi.PATH_COMMUNE + CODE_PATTERN + ConstGeoApi.PATH_DESCENDANT)
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(
        operationId = LITTERAL_ID_OPERATION + ConstGeoApi.ID_OPERATION_DESCENDANTS,
        summary = "Récupérer les informations concernant les territoires inclus dans la commune",
        description = LITTERAL_OPERATION_DESCRIPTION,
        responses = {
            @ApiResponse(
                content = @Content(schema = @Schema(type = ARRAY, implementation = Territoire.class)),
                description = LITTERAL_RESPONSE_DESCRIPTION)
        })
    public Response getDescendants(
        @Parameter(
            description = ConstGeoApi.PATTERN_COMMUNE_DESCRIPTION,
            required = true,
            schema = @Schema(
                pattern = ConstGeoApi.PATTERN_COMMUNE,
                type = Constants.TYPE_STRING, example="13055")) @PathParam(Constants.CODE) String code,
        @Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header,
        @Parameter(
            description = LITTERAL_PARAMETER_DATE_DESCRIPTION,
            required = false,
            schema = @Schema(type = Constants.TYPE_STRING, format = Constants.FORMAT_DATE)) @QueryParam(
                value = Constants.PARAMETER_DATE) String date,
        @Parameter(
            description = LITTERAL_PARAMETER_TYPE_DESCRIPTION,
            required = false,
            schema = @Schema(type = Constants.TYPE_STRING, example="ArrondissementMunicipal")) @QueryParam(
                value = Constants.PARAMETER_TYPE) String typeTerritoire) {

        logger.debug("Received GET request for descendants of commune {}", code);

        if ( ! this.verifyParametersTypeAndDateAreValid(typeTerritoire, date)) {
            return this.generateBadRequestResponse();
        }
        else {
            return this
                .generateResponseListOfTerritoire(
                    sparqlUtils
                        .executeSparqlQuery(
                            GeoQueries
                                .getDescendantsCommune(
                                    code,
                                    this.formatValidParameterDateIfIsNull(date),
                                    this.formatValidParametertypeTerritoireIfIsNull(typeTerritoire))),
                    header,
                    Territoires.class,
                    Territoire.class);
        }
    }

    @Path(ConstGeoApi.PATH_LISTE_COMMUNES)
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(
        operationId = LITTERAL_ID_OPERATION + ConstGeoApi.ID_OPERATION_LISTE,
        summary = "La requête renvoie toutes les communes actives à la date donnée. Par défaut, c’est la date courante.",
        description = LITTERAL_OPERATION_DESCRIPTION,
        responses = {
            @ApiResponse(
                content = @Content(schema = @Schema(type = ARRAY, implementation = Commune.class)),
                description = LITTERAL_RESPONSE_DESCRIPTION)
        })
    public Response getListe(
        @Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header,
        @Parameter(
            description = LITTERAL_PARAMETER_DATE_DESCRIPTION,
            required = false,
            schema = @Schema(type = Constants.TYPE_STRING, format = Constants.FORMAT_DATE)) @QueryParam(
                value = Constants.PARAMETER_DATE) String date) {

        logger.debug("Received GET request for all communes");

        if ( ! this.verifyParameterDateIsRight(date, true)) {
            return this.generateBadRequestResponse();
        }
        else {
            return this
                .generateResponseListOfTerritoire(
                    sparqlUtils
                        .executeSparqlQuery(GeoQueries.getListCommunes(this.formatValidParameterDateIfIsNull(date))),
                    header,
                    Communes.class,
                    Commune.class);
        }
    }

    @Path(ConstGeoApi.PATH_COMMUNE + CODE_PATTERN + ConstGeoApi.PATH_SUIVANT)
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(
        operationId = LITTERAL_ID_OPERATION + ConstGeoApi.ID_OPERATION_SUIVANT,
        summary = "Récupérer les informations concernant les communes qui succèdent à la commune",
        responses = {
            @ApiResponse(
                content = @Content(schema = @Schema(implementation = Commune.class)),
                description = LITTERAL_RESPONSE_DESCRIPTION)
        })
    public Response getSuivant(
        @Parameter(
            description = ConstGeoApi.PATTERN_COMMUNE_DESCRIPTION,
            required = true,
            schema = @Schema(
                pattern = ConstGeoApi.PATTERN_COMMUNE,
                type = Constants.TYPE_STRING, example=LITTERAL_CODE_EXAMPLE)) @PathParam(Constants.CODE) String code,
        @Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header,
        @Parameter(
            description = "Filtre pour préciser la commune de départ. Par défaut, c’est la date courante qui est utilisée. (Format : 'AAAA-MM-JJ')",
            required = false,
            schema = @Schema(type = Constants.TYPE_STRING, format = Constants.FORMAT_DATE, example=LITTERAL_DATE_EXAMPLE)) @QueryParam(
                value = Constants.PARAMETER_DATE) String date) {

        logger.debug("Received GET request for suivant commune {}", code);

        if ( ! this.verifyParameterDateIsRight(date)) {
            return this.generateBadRequestResponse();
        }
        else {
            return this
                .generateResponseListOfTerritoire(
                    sparqlUtils
                        .executeSparqlQuery(
                            GeoQueries.getNextCommune(code, this.formatValidParameterDateIfIsNull(date))),
                    header,
                    Communes.class,
                    Commune.class);
        }
    }

    @Path(ConstGeoApi.PATH_COMMUNE + CODE_PATTERN + ConstGeoApi.PATH_PRECEDENT)
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(
        operationId = LITTERAL_ID_OPERATION + ConstGeoApi.ID_OPERATION_PRECEDENT,
        summary = "Récupérer les informations concernant les communes qui précèdent la commune",
        responses = {
            @ApiResponse(
                content = @Content(schema = @Schema(implementation = Commune.class)),
                description = LITTERAL_RESPONSE_DESCRIPTION)
        })
    public Response getPrecedent(
        @Parameter(
            description = ConstGeoApi.PATTERN_COMMUNE_DESCRIPTION,
            required = true,
            schema = @Schema(
                pattern = ConstGeoApi.PATTERN_COMMUNE,
                type = Constants.TYPE_STRING, example=LITTERAL_CODE_EXAMPLE)) @PathParam(Constants.CODE) String code,
        @Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header,
        @Parameter(
            description = "Filtre pour préciser la commune de départ. Par défaut, c’est la date courante qui est utilisée. (Format : 'AAAA-MM-JJ')",
            required = false,
            schema = @Schema(type = Constants.TYPE_STRING, format = Constants.FORMAT_DATE)) @QueryParam(
                value = Constants.PARAMETER_DATE) String date) {

        logger.debug("Received GET request for precedent commune {}", code);

        if ( ! this.verifyParameterDateIsRight(date)) {
            return this.generateBadRequestResponse();
        }
        else {
            return this
                .generateResponseListOfTerritoire(
                    sparqlUtils
                        .executeSparqlQuery(
                            GeoQueries.getPreviousCommune(code, this.formatValidParameterDateIfIsNull(date))),
                    header,
                    Communes.class,
                    Commune.class);
        }
    }

    @Path(ConstGeoApi.PATH_COMMUNE + CODE_PATTERN + ConstGeoApi.PATH_PROJECTION)
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(
        operationId = LITTERAL_ID_OPERATION + ConstGeoApi.ID_OPERATION_PROJECTION,
        summary = "Récupérer les informations concernant les communes qui résultent de la projection de la commune à la date passée en paramètre.",
        responses = {
            @ApiResponse(
                content = @Content(schema = @Schema(implementation = Commune.class)),
                description = LITTERAL_RESPONSE_DESCRIPTION)
        })
    public Response getProjection(
        @Parameter(
            description = ConstGeoApi.PATTERN_COMMUNE_DESCRIPTION,
            required = true,
            schema = @Schema(
                pattern = ConstGeoApi.PATTERN_COMMUNE,
                type = Constants.TYPE_STRING, example=LITTERAL_CODE_EXAMPLE)) @PathParam(Constants.CODE) String code,
        @Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header,
        @Parameter(
            description = "Filtre pour préciser la commune de départ. Par défaut, c’est la date courante qui est utilisée. (Format : 'AAAA-MM-JJ')",
            required = false,
            schema = @Schema(type = Constants.TYPE_STRING, format = Constants.FORMAT_DATE)) @QueryParam(
                value = Constants.PARAMETER_DATE) String date,
        @Parameter(
            description = "Date vers laquelle est projetée la commune. Paramètre obligatoire (Format : 'AAAA-MM-JJ', erreur 400 si absent)",
            required = true,
            schema = @Schema(type = Constants.TYPE_STRING, format = Constants.FORMAT_DATE, example=LITTERAL_DATE_EXAMPLE)) @QueryParam(
                value = Constants.PARAMETER_DATE_PROJECTION) String dateProjection) {

        logger.debug("Received GET request for commune {} projection", code);

        if ( ! this.verifyParameterDateIsRight(date) || ! this.verifyParameterDateIsRight(dateProjection)) {
            return this.generateBadRequestResponse();
        }
        else {
            return this
                .generateResponseListOfTerritoire(
                    sparqlUtils
                        .executeSparqlQuery(
                            GeoQueries
                                .getProjectionCommune(
                                    code,
                                    this.formatValidParameterDateIfIsNull(date),
                                    dateProjection)),
                    header,
                    Communes.class,
                    Commune.class);
        }
    }

   /* @Path(ConstGeoApi.PATH_LISTE_COMMUNES + ConstGeoApi.PATH_PROJECTION)
    @GET
    @Produces({
        MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
    })
    @Operation(
        operationId = LITTERAL_ID_OPERATION + ConstGeoApi.ID_OPERATION_PROJECTIONS,
        summary = "Récupérer la projection des communes vers la date passée en paramètre.",
        responses = {
            @ApiResponse(
                content = @Content(schema = @Schema(implementation = Projections.class)),
                description = LITTERAL_RESPONSE_DESCRIPTION)
        })
    public Response getAllProjections(
        @Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header,
        @Parameter(
            description = "Filtre pour préciser les communes de départ. Par défaut, c’est la date courante qui est utilisée.",
            required = false,
            schema = @Schema(type = Constants.TYPE_STRING, format = Constants.FORMAT_DATE)) @QueryParam(
                value = Constants.PARAMETER_DATE) String date,
        @Parameter(
            description = "Date vers laquelle sont projetées les communes. Paramètre obligatoire (erreur 400 si absent)",
            required = true,
            schema = @Schema(type = Constants.TYPE_STRING, format = Constants.FORMAT_DATE)) @QueryParam(
                value = Constants.PARAMETER_DATE_PROJECTION) String dateProjection) {

        logger.debug("Received GET request for all communes projections");

        if ( ! this.verifyParameterDateIsRight(date) || ! this.verifyParameterDateIsRight(dateProjection)) {
            return this.generateBadRequestResponse();
        }
        else {
            return this
                .generateResponseListOfProjection(
                    sparqlUtils
                        .executeSparqlQuery(
                            GeoQueries
                                .getAllProjectionCommune(this.formatValidParameterDateIfIsNull(date), dateProjection)),
                    header);
        }
    }*/
}
