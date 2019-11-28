package org.folio.codex;

import static org.folio.utils.Utils.readMockFile;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.folio.cql2rmapi.CQLParameters;
import org.folio.cql2rmapi.QueryValidationException;
import org.folio.cql2rmapi.TitleParameters;
import org.folio.cql2rmapi.query.PaginationCalculator;
import org.folio.cql2rmapi.query.PaginationInfo;
import org.folio.rest.RestVerticle;
import org.folio.rest.jaxrs.model.Contributor;
import org.folio.rest.jaxrs.model.Identifier;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.Subject;
import org.folio.utils.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * @author mreno
 *
 */
@RunWith(VertxUnitRunner.class)
public class RMAPIToCodexTest {
  private static final String MOCK_RMAPI_INSTANCE_TITLE_404_FILE = "RMAPIService/TitleNotFound.json";

  private final Logger logger = LoggerFactory.getLogger("okapi");

  private Vertx vertx;

  private static final String API_KEY = "8675309";
  private static final String CUSTOMER_ID = "test";
  private static final String URL = "http://localhost:51234";

  private org.folio.holdingsiq.model.Configuration configuration = org.folio.holdingsiq.model.Configuration.builder()
    .apiKey(API_KEY)
    .customerId(CUSTOMER_ID)
    .url(URL)
    .build();

  @Before
  public void setUp(TestContext context) {

    final int port = Utils.getRandomPort();

    vertx = Vertx.vertx();

    JsonObject conf = new JsonObject()
        .put("http.port", port);

    logger.info("RM API to Codex Test: Deploying "
        + RestVerticle.class.getName() + ' ' + Json.encode(conf));

    DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    vertx.deployVerticle(RestVerticle.class.getName(), opt,
        context.asyncAssertSuccess());

    final int serverPort = Integer.parseInt(System.getProperty("serverPort",
        Integer.toString(51234)));
    String host = "localhost";

    Async async = context.async();
    HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals("/rm/rmaccounts/test/titles")) {
        if ("searchfield=titlename&selection=all&resourcetype=all&searchtype=advanced&search=moby%2520dick&offset=1&count=5&orderby=titlename".equals(req.query())) {
          req.response().setStatusCode(200).end("{\"totalResults\": 524, \"titles\": [   {   \"titleId\": 9950115,   \"titleName\": \"[Resolute Dick]\",   \"publisherName\": \"Unspecified\",   \"identifiersList\": [   {   \"id\": \"4667067\",   \"source\": \"AtoZ\",   \"subtype\": 0,   \"type\": 9 } ],   \"subjectsList\": null,   \"isTitleCustom\": false,   \"pubType\": \"Journal\",   \"customerResourcesList\": [   {   \"titleId\": 13611278,   \"packageId\": 8537,   \"packageName\": \"Eighteenth Century Collections Online - Part II\",   \"isPackageCustom\": false,   \"vendorId\": 18,   \"vendorName\": \"Gale | Cengage\",   \"locationId\": 30087973,   \"isSelected\": false,   \"isTokenNeeded\": true,   \"packageType\": \"Complete\",   \"visibilityData\": {   \"isHidden\": false,   \"reason\": \"\" },   \"proxy\": null,   \"managedCoverageList\": [],   \"customCoverageList\": [],   \"coverageStatement\": null,   \"managedEmbargoPeriod\": {   \"embargoUnit\": null,   \"embargoValue\": 0 },   \"customEmbargoPeriod\": {   \"embargoUnit\": null,   \"embargoValue\": 0 },   \"url\": \"https://infotrac.gale.com/itweb/[[galesiteid]]?db=ECCO\",   \"userDefinedField1\": null,   \"userDefinedField2\": null,   \"userDefinedField3\": null,   \"userDefinedField4\": null,   \"userDefinedField5\": null } ] },   {   \"titleId\": 1816494,   \"titleName\": \"1851 - Herman Melville's Moby Dick is Published\",   \"publisherName\": \"Unspecified\",   \"identifiersList\": [   {   \"id\": \"1816494\",   \"source\": \"AtoZ\",   \"subtype\": 0,   \"type\": 9 } ],   \"subjectsList\": null,   \"isTitleCustom\": false,   \"pubType\": \"StreamingVideo\",   \"customerResourcesList\": [   {   \"titleId\": 1816494,   \"packageId\": 7349,   \"packageName\": \"Ambrose Video 2.0\",   \"packageType\": \"Variable\",   \"isPackageCustom\": false,   \"vendorId\": 933,   \"vendorName\": \"Ambrose Video Publishing, Inc.\",   \"locationId\": 5735819,   \"isSelected\": false,   \"isTokenNeeded\": true,   \"visibilityData\": {   \"isHidden\": false,   \"reason\": \"\" },   \"managedCoverageList\": [],   \"customCoverageList\": [],   \"coverageStatement\": null,   \"managedEmbargoPeriod\": {   \"embargoUnit\": null,   \"embargoValue\": 0 },   \"customEmbargoPeriod\": {   \"embargoUnit\": null,   \"embargoValue\": 0 },   \"url\": \"http://dma.iriseducation.org/?license=[[license code]]&DMA2&src=XLS&segment=300496&pid=2037&sku=GAA-002-03\",   \"userDefinedField1\": null,   \"userDefinedField2\": null,   \"userDefinedField3\": null,   \"userDefinedField4\": null,   \"userDefinedField5\": null } ] },   {   \"titleId\": 4634949,   \"titleName\": \"60 minutes. Dick Clarke\",   \"publisherName\": \"Unspecified\",   \"identifiersList\": [   {   \"id\": \"3140739\",   \"source\": \"AtoZ\",   \"subtype\": 0,   \"type\": 9 } ],   \"subjectsList\": null,   \"isTitleCustom\": false,   \"pubType\": \"Journal\",   \"customerResourcesList\": [   {   \"titleId\": 4634949,   \"packageId\": 1367800,   \"packageName\": \"60 Minutes 1997-2014\",   \"packageType\": \"Complete\",   \"isPackageCustom\": false,   \"vendorId\": 413,   \"vendorName\": \"Alexander Street Press\",   \"locationId\": 12526676,   \"isSelected\": false,   \"isTokenNeeded\": false,   \"visibilityData\": {   \"isHidden\": false,   \"reason\": \"\" },   \"managedCoverageList\": [   {   \"beginCoverage\": \"2008-01-01\",   \"endCoverage\": \"2008-12-31\" } ],   \"customCoverageList\": [],   \"coverageStatement\": null,   \"managedEmbargoPeriod\": {   \"embargoUnit\": null,   \"embargoValue\": 0 },   \"customEmbargoPeriod\": {   \"embargoUnit\": null,   \"embargoValue\": 0 },   \"url\": \"http://www.aspresolver.com/aspresolver.asp?CBSV;2774880\",   \"userDefinedField1\": null,   \"userDefinedField2\": null,   \"userDefinedField3\": null,   \"userDefinedField4\": null,   \"userDefinedField5\": null } ] },   {   \"titleId\": 4635334,   \"titleName\": \"60 minutes. The vice president [Dick Cheney]\",   \"publisherName\": \"Unspecified\",   \"identifiersList\": [   {   \"id\": \"3141124\",   \"source\": \"AtoZ\",   \"subtype\": 0,   \"type\": 9 } ],   \"subjectsList\": null,   \"isTitleCustom\": false,   \"pubType\": \"Journal\",   \"customerResourcesList\": [   {   \"titleId\": 4635334,   \"packageId\": 1367800,   \"packageName\": \"60 Minutes 1997-2014\",   \"packageType\": \"Complete\",   \"isPackageCustom\": false,   \"vendorId\": 413,   \"vendorName\": \"Alexander Street Press\",   \"locationId\": 12527074,   \"isSelected\": false,   \"isTokenNeeded\": false,   \"visibilityData\": {   \"isHidden\": false,   \"reason\": \"\" },   \"managedCoverageList\": [   {   \"beginCoverage\": \"2001-01-01\",   \"endCoverage\": \"2001-12-31\" } ],   \"customCoverageList\": [],   \"coverageStatement\": null,   \"managedEmbargoPeriod\": {   \"embargoUnit\": null,   \"embargoValue\": 0 },   \"customEmbargoPeriod\": {   \"embargoUnit\": null,   \"embargoValue\": 0 },   \"url\": \"http://www.aspresolver.com/aspresolver.asp?CBSV;2774149\",   \"userDefinedField1\": null,   \"userDefinedField2\": null,   \"userDefinedField3\": null,   \"userDefinedField4\": null,   \"userDefinedField5\": null } ] },   {   \"titleId\": 9948551,   \"titleName\": \"A catalogue of books in several faculties and languages, Consisting of a choice collection in divinity, philosophy, philology, phisick, cosmography, history, mathematicks and chronology. Together with\",   \"publisherName\": \"Unspecified\",   \"identifiersList\": [   {   \"id\": \"4665550\",   \"source\": \"AtoZ\",   \"subtype\": 0,   \"type\": 9 } ],   \"subjectsList\": null,   \"isTitleCustom\": false,   \"pubType\": \"Journal\",   \"customerResourcesList\": [   {   \"titleId\": 13609761,   \"packageId\": 8537,   \"packageName\": \"Eighteenth Century Collections Online - Part II\",   \"isPackageCustom\": false,   \"vendorId\": 18,   \"vendorName\": \"Gale | Cengage\",   \"locationId\": 30086146,   \"isSelected\": false,   \"isTokenNeeded\": true,   \"packageType\": \"Complete\",   \"visibilityData\": {   \"isHidden\": false,   \"reason\": \"\" },   \"proxy\": null,   \"managedCoverageList\": [],   \"customCoverageList\": [],   \"coverageStatement\": null,   \"managedEmbargoPeriod\": {   \"embargoUnit\": null,   \"embargoValue\": 0 },   \"customEmbargoPeriod\": {   \"embargoUnit\": null,   \"embargoValue\": 0 },   \"url\": \"https://infotrac.gale.com/itweb/[[galesiteid]]?db=ECCO\",   \"userDefinedField1\": null,   \"userDefinedField2\": null,   \"userDefinedField3\": null,   \"userDefinedField4\": null,   \"userDefinedField5\": null } ] } ]}");
        } else if ("searchfield=titlename&selection=all&resourcetype=all&searchtype=advanced&search=moby%2520dick&offset=2&count=5&orderby=titlename".equals(req.query())) {
          req.response().setStatusCode(200).end("{\"totalResults\":524,\"titles\":[{\"titleId\":1550515,\"titleName\":\"A Dialogue about the French government wars, cruelties, armies, fleet, &c. between Tom and Dick, two seamen.\",\"publisherName\":\"Unspecified\",\"identifiersList\":[{\"id\":\"1550515\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":null,\"isTitleCustom\":false,\"pubType\":\"Book\",\"customerResourcesList\":[{\"titleId\":1550515,\"packageId\":4207,\"packageName\":\"Early English Books Online (EEBO)\",\"packageType\":\"Complete\",\"isPackageCustom\":false,\"vendorId\":22,\"vendorName\":\"Proquest Info & Learning Co\",\"locationId\":4974961,\"isSelected\":true,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"1690-01-01\",\"endCoverage\":\"1690-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://gateway.proquest.com/openurl?ctx_ver=Z39.88-2003&res_id=xri:eebo&rft_id=xri:eebo:citation:7916128\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}]},{\"titleId\":9909627,\"titleName\":\"A dialogue between Dick --- and Tom ---, Esqrs; relating to the present divisions in I-d\",\"publisherName\":\"Unspecified\",\"identifiersList\":[{\"id\":\"4575830\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":null,\"isTitleCustom\":false,\"pubType\":\"Journal\",\"customerResourcesList\":[{\"titleId\":9909627,\"packageId\":4205,\"packageName\":\"Eighteenth Century Collections Online\",\"packageType\":\"Complete\",\"isPackageCustom\":false,\"vendorId\":18,\"vendorName\":\"Gale Group\",\"locationId\":19683097,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://find.galegroup.com/menu/start.do?prodId=ECCO&userGroupName=[[galesiteid]]\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}]},{\"titleId\":1533310,\"titleName\":\"A Dialogue between Tom and Dick over a dish of coffee concerning matters of religion and government.\",\"publisherName\":\"Unspecified\",\"identifiersList\":[{\"id\":\"1533310\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":null,\"isTitleCustom\":false,\"pubType\":\"Book\",\"customerResourcesList\":[{\"titleId\":1533310,\"packageId\":4207,\"packageName\":\"Early English Books Online (EEBO)\",\"packageType\":\"Complete\",\"isPackageCustom\":false,\"vendorId\":22,\"vendorName\":\"Proquest Info & Learning Co\",\"locationId\":4957756,\"isSelected\":true,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"1680-01-01\",\"endCoverage\":\"1680-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://gateway.proquest.com/openurl?ctx_ver=Z39.88-2003&res_id=xri:eebo&rft_id=xri:eebo:citation:10178611\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}]},{\"titleId\":1548192,\"titleName\":\"A dialogue between Dick and Tom, concerning the present posture of affairs in England\",\"publisherName\":\"Unspecified\",\"identifiersList\":[{\"id\":\"1548192\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":null,\"isTitleCustom\":false,\"pubType\":\"Book\",\"customerResourcesList\":[{\"titleId\":1548192,\"packageId\":4207,\"packageName\":\"Early English Books Online (EEBO)\",\"packageType\":\"Complete\",\"isPackageCustom\":false,\"vendorId\":22,\"vendorName\":\"Proquest Info & Learning Co\",\"locationId\":4972638,\"isSelected\":true,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"1689-01-01\",\"endCoverage\":\"1689-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://gateway.proquest.com/openurl?ctx_ver=Z39.88-2003&res_id=xri:eebo&rft_id=xri:eebo:citation:11759671\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}]},{\"titleId\":1568257,\"titleName\":\"A dialogue between Dick Brazenface the card-maker, and Tim. Meanwell, the clothier; being the dispute between the card-maker and the clothier fairly stated, in order to set the merits of that cause in\",\"publisherName\":\"Unspecified\",\"identifiersList\":[{\"id\":\"1568257\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":null,\"isTitleCustom\":false,\"pubType\":\"Book\",\"customerResourcesList\":[{\"titleId\":1568257,\"packageId\":4207,\"packageName\":\"Early English Books Online (EEBO)\",\"packageType\":\"Complete\",\"isPackageCustom\":false,\"vendorId\":22,\"vendorName\":\"Proquest Info & Learning Co\",\"locationId\":4992703,\"isSelected\":true,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"1711-01-01\",\"endCoverage\":\"1711-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://gateway.proquest.com/openurl?ctx_ver=Z39.88-2003&res_id=xri:eebo&rft_id=xri:eebo:citation:99893561\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}]}]}");
        } else if ("searchfield=titlename&selection=all&resourcetype=all&searchtype=advanced&search=moby%2520dick&offset=1&count=10&orderby=titlename".equals(req.query())) {
          req.response().setStatusCode(200).end("{\"totalResults\":5,\"titles\":[{\"titleId\":1550515,\"titleName\":\"A Dialogue about the French government wars, cruelties, armies, fleet, &c. between Tom and Dick, two seamen.\",\"publisherName\":\"Unspecified\",\"identifiersList\":[{\"id\":\"1550515\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":null,\"isTitleCustom\":false,\"pubType\":\"Book\",\"customerResourcesList\":[{\"titleId\":1550515,\"packageId\":4207,\"packageName\":\"Early English Books Online (EEBO)\",\"packageType\":\"Complete\",\"isPackageCustom\":false,\"vendorId\":22,\"vendorName\":\"Proquest Info & Learning Co\",\"locationId\":4974961,\"isSelected\":true,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"1690-01-01\",\"endCoverage\":\"1690-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://gateway.proquest.com/openurl?ctx_ver=Z39.88-2003&res_id=xri:eebo&rft_id=xri:eebo:citation:7916128\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}]},{\"titleId\":9909627,\"titleName\":\"A dialogue between Dick --- and Tom ---, Esqrs; relating to the present divisions in I-d\",\"publisherName\":\"Unspecified\",\"identifiersList\":[{\"id\":\"4575830\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":null,\"isTitleCustom\":false,\"pubType\":\"Journal\",\"customerResourcesList\":[{\"titleId\":9909627,\"packageId\":4205,\"packageName\":\"Eighteenth Century Collections Online\",\"packageType\":\"Complete\",\"isPackageCustom\":false,\"vendorId\":18,\"vendorName\":\"Gale Group\",\"locationId\":19683097,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://find.galegroup.com/menu/start.do?prodId=ECCO&userGroupName=[[galesiteid]]\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}]},{\"titleId\":1533310,\"titleName\":\"A Dialogue between Tom and Dick over a dish of coffee concerning matters of religion and government.\",\"publisherName\":\"Unspecified\",\"identifiersList\":[{\"id\":\"1533310\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":null,\"isTitleCustom\":false,\"pubType\":\"Book\",\"customerResourcesList\":[{\"titleId\":1533310,\"packageId\":4207,\"packageName\":\"Early English Books Online (EEBO)\",\"packageType\":\"Complete\",\"isPackageCustom\":false,\"vendorId\":22,\"vendorName\":\"Proquest Info & Learning Co\",\"locationId\":4957756,\"isSelected\":true,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"1680-01-01\",\"endCoverage\":\"1680-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://gateway.proquest.com/openurl?ctx_ver=Z39.88-2003&res_id=xri:eebo&rft_id=xri:eebo:citation:10178611\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}]},{\"titleId\":1548192,\"titleName\":\"A dialogue between Dick and Tom, concerning the present posture of affairs in England\",\"publisherName\":\"Unspecified\",\"identifiersList\":[{\"id\":\"1548192\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":null,\"isTitleCustom\":false,\"pubType\":\"Book\",\"customerResourcesList\":[{\"titleId\":1548192,\"packageId\":4207,\"packageName\":\"Early English Books Online (EEBO)\",\"packageType\":\"Complete\",\"isPackageCustom\":false,\"vendorId\":22,\"vendorName\":\"Proquest Info & Learning Co\",\"locationId\":4972638,\"isSelected\":true,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"1689-01-01\",\"endCoverage\":\"1689-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://gateway.proquest.com/openurl?ctx_ver=Z39.88-2003&res_id=xri:eebo&rft_id=xri:eebo:citation:11759671\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}]},{\"titleId\":1568257,\"titleName\":\"A dialogue between Dick Brazenface the card-maker, and Tim. Meanwell, the clothier; being the dispute between the card-maker and the clothier fairly stated, in order to set the merits of that cause in\",\"publisherName\":\"Unspecified\",\"identifiersList\":[{\"id\":\"1568257\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":null,\"isTitleCustom\":false,\"pubType\":\"Book\",\"customerResourcesList\":[{\"titleId\":1568257,\"packageId\":4207,\"packageName\":\"Early English Books Online (EEBO)\",\"packageType\":\"Complete\",\"isPackageCustom\":false,\"vendorId\":22,\"vendorName\":\"Proquest Info & Learning Co\",\"locationId\":4992703,\"isSelected\":true,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"1711-01-01\",\"endCoverage\":\"1711-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://gateway.proquest.com/openurl?ctx_ver=Z39.88-2003&res_id=xri:eebo&rft_id=xri:eebo:citation:99893561\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}]}]}");
        } else if ("searchfield=titlename&selection=all&resourcetype=all&searchtype=advanced&search=moby%2520dick&offset=2&count=10&orderby=titlename".equals(req.query())) {
          req.response().setStatusCode(200).end("{\"totalResults\":5,\"titles\":[]}");
        } else {
          req.response().setStatusCode(500).end("Unexpected call: " + req.path());
        }
      } else if (req.path().equals("/rm/rmaccounts/test/titles/1619585")) {
        req.response().setStatusCode(200).end("{\"titleId\":1619585,\"titleName\":\"Tom, Dick and Harry\",\"publisherName\":\"Project Gutenberg Literary Archive Foundation\",\"identifiersList\":[{\"id\":\"1619585\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":[],\"isTitleCustom\":false,\"pubType\":\"Book\",\"customerResourcesList\":[{\"titleId\":1619585,\"packageId\":6750,\"packageName\":\"Project Gutenberg eBooks\",\"packageType\":\"Variable\",\"proxy\":{\"id\":\"<n>\",\"inherited\":true},\"isPackageCustom\":false,\"vendorId\":953,\"vendorName\":\"Project Gutenberg\",\"locationId\":5137360,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://www.gutenberg.org/ebooks/20992\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null},{\"titleId\":1619585,\"packageId\":19153,\"packageName\":\"Project Gutenberg eBooks Archive Collection\",\"packageType\":\"Variable\",\"proxy\":{\"id\":\"<n>\",\"inherited\":true},\"isPackageCustom\":false,\"vendorId\":953,\"vendorName\":\"Project Gutenberg\",\"locationId\":7435416,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"https://archive.org/details/tomdickandharry20992gut\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}],\"description\":null,\"edition\":null,\"isPeerReviewed\":false,\"contributorsList\":[{\"type\":\"author\",\"contributor\":\"Reed, Talbot Baines\"}]}");
      } else if (req.path().equals("/rm/rmaccounts/test/titles/4581052")) {
        req.response().setStatusCode(200).end("{\"titleId\":4581052,\"titleName\":\"The World According to Philip K. Dick\",\"publisherName\":\"Palgrave Macmillan Ltd.\",\"identifiersList\":[{\"id\":\"3114209\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9},{\"id\":\"978-1-137-41458-8\",\"source\":\"ResourceIdentifier\",\"subtype\":1,\"type\":1},{\"id\":\"978-1-137-41459-5\",\"source\":\"ResourceIdentifier\",\"subtype\":2,\"type\":1},{\"id\":\"978-1-349-49032-5\",\"source\":\"ResourceIdentifier\",\"subtype\":1,\"type\":1},{\"id\":\"998217\",\"source\":\"ResourceIdentifier\",\"subtype\":0,\"type\":7}],\"subjectsList\":[{\"type\":\"BISAC\",\"subject\":\"LITERARY CRITICISM / Science Fiction & Fantasy\"}],\"isTitleCustom\":false,\"pubType\":\"Book\",\"customerResourcesList\":[{\"titleId\":4581052,\"packageId\":3814,\"packageName\":\"Palgrave Connect Literature & Performing Arts eBook Collection\",\"packageType\":\"Complete\",\"proxy\":{\"id\":\"<n>\",\"inherited\":true},\"isPackageCustom\":false,\"vendorId\":262,\"vendorName\":\"Palgrave Macmillan Ltd\",\"locationId\":16869169,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"2015-01-01\",\"endCoverage\":\"2015-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://www.palgraveconnect.com/pc/doifinder/10.1057/9781137414595\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null},{\"titleId\":4581052,\"packageId\":3831,\"packageName\":\"Palgrave Connect Complete eBook Collection\",\"packageType\":\"Variable\",\"proxy\":{\"id\":\"<n>\",\"inherited\":true},\"isPackageCustom\":false,\"vendorId\":262,\"vendorName\":\"Palgrave Macmillan Ltd\",\"locationId\":12282411,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"2015-01-01\",\"endCoverage\":\"2015-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://www.palgraveconnect.com/pc/doifinder/10.1057/9781137414595\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null},{\"titleId\":4581052,\"packageId\":5207,\"packageName\":\"EBSCO eBooks\",\"packageType\":\"Selectable\",\"proxy\":{\"id\":\"proxy-id-123\",\"inherited\":true},\"isPackageCustom\":false,\"vendorId\":19,\"vendorName\":\"EBSCO\",\"locationId\":12699213,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"2015-01-01\",\"endCoverage\":\"2015-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://search.ebscohost.com/login.aspx?direct=true&scope=site&db=nlebk&db=nlabk&AN=998217\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null},{\"titleId\":4581052,\"packageId\":1244867,\"packageName\":\"Palgrave Connect Literature eBook Collection 2015\",\"packageType\":\"Complete\",\"proxy\":{\"id\":\"<n>\",\"inherited\":true},\"isPackageCustom\":false,\"vendorId\":262,\"vendorName\":\"Palgrave Macmillan Ltd\",\"locationId\":16870606,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"2015-01-01\",\"endCoverage\":\"2015-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://www.palgraveconnect.com/pc/doifinder/10.1057/9781137414595\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}],\"description\":null,\"edition\":null,\"isPeerReviewed\":false,\"contributorsList\":[{\"type\":\"editor\",\"contributor\":\"Stefan Schlensag\"},{\"type\":\"editor\",\"contributor\":\"Alexander Dunst\"},{\"type\":\"author\",\"contributor\":\"Dunst, Alexander\"},{\"type\":\"author\",\"contributor\":\"Schlensag, Stefan\"}]}");
      } else if (req.path().equals("/rm/rmaccounts/test/titles/1619586")) {
        req.response().setStatusCode(200).end("{\"titleId\":1619586,\"titleName\":\"Tom, Dick and Harry\",\"publisherName\":\"Project Gutenberg Literary Archive Foundation\",\"subjectsList\":[],\"isTitleCustom\":false,\"pubType\":\"Book\",\"customerResourcesList\":[{\"titleId\":1619585,\"packageId\":6750,\"packageName\":\"Project Gutenberg eBooks\",\"packageType\":\"Variable\",\"proxy\":{\"id\":\"<n>\",\"inherited\":true},\"isPackageCustom\":false,\"vendorId\":953,\"vendorName\":\"Project Gutenberg\",\"locationId\":5137360,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://www.gutenberg.org/ebooks/20992\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null},{\"titleId\":1619585,\"packageId\":19153,\"packageName\":\"Project Gutenberg eBooks Archive Collection\",\"packageType\":\"Variable\",\"proxy\":{\"id\":\"<n>\",\"inherited\":true},\"isPackageCustom\":false,\"vendorId\":953,\"vendorName\":\"Project Gutenberg\",\"locationId\":7435416,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"https://archive.org/details/tomdickandharry20992gut\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}],\"description\":null,\"edition\":null,\"isPeerReviewed\":false,\"contributorsList\":[]}");
      } else if (req.path().equals("/rm/rmaccounts/test/titles/4581057")) {
        req.response().setStatusCode(200).end("{\"titleId\":4581057,\"titleName\":\"The World According to Philip K. Dick\",\"publisherName\":\"Palgrave Macmillan Ltd.\",\"identifiersList\":[{\"id\":\"3114209\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9},{\"id\":\"978-1-137-41458-8\",\"source\":\"ResourceIdentifier\",\"subtype\":9,\"type\":1},{\"id\":\"978-1-137-41459-5\",\"source\":\"ResourceIdentifier\",\"subtype\":2,\"type\":1},{\"id\":\"978-1-349-49032-5\",\"source\":\"ResourceIdentifier\",\"subtype\":1,\"type\":1},{\"id\":\"998217\",\"source\":\"ResourceIdentifier\",\"subtype\":0,\"type\":7}],\"subjectsList\":[{\"type\":\"BISAC\",\"subject\":\"LITERARY CRITICISM / Science Fiction & Fantasy\"}],\"isTitleCustom\":false,\"pubType\":\"Book\",\"customerResourcesList\":[{\"titleId\":4581052,\"packageId\":3814,\"packageName\":\"Palgrave Connect Literature & Performing Arts eBook Collection\",\"packageType\":\"Complete\",\"proxy\":{\"id\":\"<n>\",\"inherited\":true},\"isPackageCustom\":false,\"vendorId\":262,\"vendorName\":\"Palgrave Macmillan Ltd\",\"locationId\":16869169,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"2015-01-01\",\"endCoverage\":\"2015-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://www.palgraveconnect.com/pc/doifinder/10.1057/9781137414595\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null},{\"titleId\":4581052,\"packageId\":3831,\"packageName\":\"Palgrave Connect Complete eBook Collection\",\"packageType\":\"Variable\",\"proxy\":{\"id\":\"<n>\",\"inherited\":true},\"isPackageCustom\":false,\"vendorId\":262,\"vendorName\":\"Palgrave Macmillan Ltd\",\"locationId\":12282411,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"2015-01-01\",\"endCoverage\":\"2015-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://www.palgraveconnect.com/pc/doifinder/10.1057/9781137414595\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null},{\"titleId\":4581052,\"packageId\":5207,\"packageName\":\"EBSCO eBooks\",\"packageType\":\"Selectable\",\"proxy\":{\"id\":\"proxy-id-123\",\"inherited\":true},\"isPackageCustom\":false,\"vendorId\":19,\"vendorName\":\"EBSCO\",\"locationId\":12699213,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"2015-01-01\",\"endCoverage\":\"2015-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://search.ebscohost.com/login.aspx?direct=true&scope=site&db=nlebk&db=nlabk&AN=998217\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null},{\"titleId\":4581052,\"packageId\":1244867,\"packageName\":\"Palgrave Connect Literature eBook Collection 2015\",\"packageType\":\"Complete\",\"proxy\":{\"id\":\"<n>\",\"inherited\":true},\"isPackageCustom\":false,\"vendorId\":262,\"vendorName\":\"Palgrave Macmillan Ltd\",\"locationId\":16870606,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"2015-01-01\",\"endCoverage\":\"2015-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://www.palgraveconnect.com/pc/doifinder/10.1057/9781137414595\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}],\"description\":null,\"edition\":null,\"isPeerReviewed\":false,\"contributorsList\":[{\"type\":\"editor\",\"contributor\":\"Stefan Schlensag\"},{\"type\":\"editor\",\"contributor\":\"Alexander Dunst\"},{\"type\":\"author\",\"contributor\":\"Dunst, Alexander\"},{\"type\":\"author\",\"contributor\":\"Schlensag, Stefan\"}]}");
      } else if (req.path().equals("/rm/rmaccounts/test/titles/2619585")) {
        req.response().setStatusCode(200).end("{\"titleId\":2619585,\"titleName\":\"Tom, Dick and Harry\",\"publisherName\":\"Project Gutenberg Literary Archive Foundation\",\"identifiersList\":[],\"subjectsList\":[],\"isTitleCustom\":false,\"pubType\":\"Book\",\"customerResourcesList\":[{\"titleId\":1619585,\"packageId\":6750,\"packageName\":\"Project Gutenberg eBooks\",\"packageType\":\"Variable\",\"proxy\":{\"id\":\"<n>\",\"inherited\":true},\"isPackageCustom\":false,\"vendorId\":953,\"vendorName\":\"Project Gutenberg\",\"locationId\":5137360,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://www.gutenberg.org/ebooks/20992\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null},{\"titleId\":1619585,\"packageId\":19153,\"packageName\":\"Project Gutenberg eBooks Archive Collection\",\"packageType\":\"Variable\",\"proxy\":{\"id\":\"<n>\",\"inherited\":true},\"isPackageCustom\":false,\"vendorId\":953,\"vendorName\":\"Project Gutenberg\",\"locationId\":7435416,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"https://archive.org/details/tomdickandharry20992gut\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}],\"description\":null,\"edition\":null,\"isPeerReviewed\":false,\"contributorsList\":[{\"type\":\"author\",\"contributor\":\"Reed, Talbot Baines\"}]}");
      } else if (req.path().equals("/rm/rmaccounts/test/titles/1111111")) {
        req.response().setStatusCode(404).end(readMockFile(MOCK_RMAPI_INSTANCE_TITLE_404_FILE));
      } else {
        req.response().setStatusCode(500).end("Unexpected call: " + req.path());
      }
    });
    server.listen(serverPort, host, ar -> {
      context.assertTrue(ar.succeeded());
      context.put("port", serverPort);
      async.complete();
    });

  }

  @After
  public void tearDown(TestContext context) {
    logger.info("Test complete, cleaning up...");
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void testGetInstance(TestContext context) {
    Async async = context.async();

      RMAPIToCodex.getInstance(vertx.getOrCreateContext(), configuration, 1619585)
    .whenComplete((response, throwable) -> {
      context.assertEquals("1619585", response.getId());
      context.assertEquals("Tom, Dick and Harry", response.getTitle());
      context.assertEquals(1, response.getContributor().size());
      context.assertEquals("author", response.getContributor().iterator().next().getType());
      context.assertEquals("Reed, Talbot Baines", response.getContributor().iterator().next().getName());
      context.assertTrue(response.getSubject().isEmpty());
      context.assertEquals("Project Gutenberg Literary Archive Foundation", response.getPublisher());
      context.assertEquals(Instance.Type.EBOOKS, response.getType());
      context.assertEquals("Electronic Resource", response.getFormat());
      context.assertTrue(response.getIdentifier().isEmpty());
      context.assertEquals("kb", response.getSource());
      context.assertTrue(response.getLanguage().isEmpty());

      async.complete();
    }).exceptionally(throwable -> {
      context.fail(throwable);
      async.complete();
      return null;
    });
  }

  @Test
  public void testGetInstance2(TestContext context) {
    Async async = context.async();

    RMAPIToCodex.getInstance(vertx.getOrCreateContext(), configuration ,4581052)
      .whenComplete((response, throwable) -> {
      context.assertEquals("4581052", response.getId());
      context.assertEquals("The World According to Philip K. Dick", response.getTitle());
      context.assertEquals(4, response.getContributor().size());
      boolean foundAuthor1 = false;
      boolean foundAuthor2 = false;
      boolean foundEditor1 = false;
      boolean foundEditor2 = false;
      for (Contributor c : response.getContributor()) {
        if ("author".equals(c.getType()) && "Schlensag, Stefan".equals(c.getName())) {
          foundAuthor1 = true;
        } else if ("author".equals(c.getType()) && "Dunst, Alexander".equals(c.getName())) {
          foundAuthor2 = true;
        } else if ("editor".equals(c.getType()) && "Stefan Schlensag".equals(c.getName())) {
          foundEditor1 = true;
        } else if ("editor".equals(c.getType()) && "Alexander Dunst".equals(c.getName())) {
          foundEditor2 = true;
        } else {
          context.fail("Unknown contributor: " + c.getName() + ' ' + c.getType());
        }
      }
      context.assertTrue(foundAuthor1);
      context.assertTrue(foundAuthor2);
      context.assertTrue(foundEditor1);
      context.assertTrue(foundEditor2);
      context.assertEquals(1, response.getSubject().size());
      boolean foundSubject = false;
      for (Subject s : response.getSubject()) {
          if ("BISAC".equals(s.getType()) && "LITERARY CRITICISM / Science Fiction & Fantasy".equals(s.getName())) {
        	  foundSubject = true;
          } else {
            context.fail("Unknown subject: " + s.getName() + ' ' + s.getType());
          }
        }
      context.assertTrue(foundSubject);
      context.assertEquals("Palgrave Macmillan Ltd.", response.getPublisher());
      context.assertEquals(Instance.Type.EBOOKS, response.getType());
      context.assertEquals("Electronic Resource", response.getFormat());
      context.assertEquals(3, response.getIdentifier().size());
      boolean foundIdentifier1 = false;
      boolean foundIdentifier2 = false;
      boolean foundIdentifier3 = false;
      for (Identifier identifier : response.getIdentifier()) {
        if ("ISBN(Online)".equals(identifier.getType()) && "978-1-137-41459-5".equals(identifier.getValue())) {
          foundIdentifier1 = true;
        } else if ("ISBN(Print)".equals(identifier.getType()) && "978-1-137-41458-8".equals(identifier.getValue())) {
          foundIdentifier2 = true;
        } else if ("ISBN(Print)".equals(identifier.getType()) && "978-1-349-49032-5".equals(identifier.getValue())) {
          foundIdentifier3 = true;
        } else {
          context.fail("Unknown identifier: " + identifier.getValue() + ' ' + identifier.getType());
        }
      }
      context.assertTrue(foundIdentifier1);
      context.assertTrue(foundIdentifier2);
      context.assertTrue(foundIdentifier3);
      context.assertEquals("kb", response.getSource());
      context.assertTrue(response.getLanguage().isEmpty());

      async.complete();
    }).exceptionally(throwable -> {
      context.fail(throwable);
      async.complete();
      return null;
    });
  }

  @Test
  public void testGetInstance3(TestContext context) {
    Async async = context.async();

      RMAPIToCodex.getInstance(vertx.getOrCreateContext(), configuration, 4581057)
        .whenComplete((response, throwable) -> {
      context.assertEquals("4581057", response.getId());
      context.assertEquals("The World According to Philip K. Dick", response.getTitle());
      context.assertEquals(4, response.getContributor().size());
      boolean foundAuthor1 = false;
      boolean foundAuthor2 = false;
      boolean foundEditor1 = false;
      boolean foundEditor2 = false;
      for (Contributor c : response.getContributor()) {
        if ("author".equals(c.getType()) && "Schlensag, Stefan".equals(c.getName())) {
          foundAuthor1 = true;
        } else if ("author".equals(c.getType()) && "Dunst, Alexander".equals(c.getName())) {
          foundAuthor2 = true;
        } else if ("editor".equals(c.getType()) && "Stefan Schlensag".equals(c.getName())) {
          foundEditor1 = true;
        } else if ("editor".equals(c.getType()) && "Alexander Dunst".equals(c.getName())) {
          foundEditor2 = true;
        } else {
          context.fail("Unknown contributor: " + c.getName() + ' ' + c.getType());
        }
      }
      context.assertTrue(foundAuthor1);
      context.assertTrue(foundAuthor2);
      context.assertTrue(foundEditor1);
      context.assertTrue(foundEditor2);
      context.assertEquals("Palgrave Macmillan Ltd.", response.getPublisher());
      context.assertEquals(Instance.Type.EBOOKS, response.getType());
      context.assertEquals("Electronic Resource", response.getFormat());
      context.assertEquals(3, response.getIdentifier().size());
      boolean foundIdentifier1 = false;
      boolean foundIdentifier2 = false;
      boolean foundIdentifier3 = false;
      for (Identifier identifier : response.getIdentifier()) {
        if ("ISBN(Online)".equals(identifier.getType()) && "978-1-137-41459-5".equals(identifier.getValue())) {
          foundIdentifier1 = true;
        } else if ("ISBN".equals(identifier.getType()) && "978-1-137-41458-8".equals(identifier.getValue())) {
          foundIdentifier2 = true;
        } else if ("ISBN(Print)".equals(identifier.getType()) && "978-1-349-49032-5".equals(identifier.getValue())) {
          foundIdentifier3 = true;
        } else {
          context.fail("Unknown identifier: " + identifier.getValue() + ' ' + identifier.getType());
        }
      }
      context.assertTrue(foundIdentifier1);
      context.assertTrue(foundIdentifier2);
      context.assertTrue(foundIdentifier3);
      context.assertEquals(1, response.getSubject().size());
      boolean foundSubject = false;
      for (Subject s : response.getSubject()) {
          if ("BISAC".equals(s.getType()) && "LITERARY CRITICISM / Science Fiction & Fantasy".equals(s.getName())) {
        	  foundSubject = true;
          } else {
            context.fail("Unknown subject: " + s.getName() + ' ' + s.getType());
          }
        }
      context.assertTrue(foundSubject);
      context.assertEquals("kb", response.getSource());
      context.assertTrue(response.getLanguage().isEmpty());

      async.complete();
    }).exceptionally(throwable -> {
      context.fail(throwable);
      async.complete();
      return null;
    });
  }

  @Test
  public void testGetInstance4(TestContext context) {
    Async async = context.async();

    RMAPIToCodex.getInstance(vertx.getOrCreateContext(), configuration, 2619585)
      .whenComplete((response, throwable) -> {
        context.assertEquals("2619585", response.getId());
        context.assertEquals("Tom, Dick and Harry", response.getTitle());
        context.assertEquals(1, response.getContributor().size());
        context.assertEquals("author", response.getContributor().iterator().next().getType());
        context.assertEquals("Reed, Talbot Baines", response.getContributor().iterator().next().getName());
        context.assertTrue(response.getSubject().isEmpty());
        context.assertEquals("Project Gutenberg Literary Archive Foundation", response.getPublisher());
        context.assertEquals(Instance.Type.EBOOKS, response.getType());
        context.assertEquals("Electronic Resource", response.getFormat());
        context.assertTrue(response.getIdentifier().isEmpty());
        context.assertEquals("kb", response.getSource());
        context.assertTrue(response.getLanguage().isEmpty());

        async.complete();
      }).exceptionally(throwable -> {
      context.fail(throwable);
      async.complete();
      return null;
    });
  }

  @Test
  public void testGetInstanceEmptyContributorList(TestContext context) {
    Async async = context.async();

   RMAPIToCodex.getInstance(vertx.getOrCreateContext(), configuration, 1619586)
     .whenComplete((response, throwable) -> {
      context.assertEquals("1619586", response.getId());
      context.assertEquals("Tom, Dick and Harry", response.getTitle());
      context.assertTrue(response.getContributor().isEmpty());
      context.assertEquals("Project Gutenberg Literary Archive Foundation", response.getPublisher());
      context.assertEquals(Instance.Type.EBOOKS, response.getType());
      context.assertEquals("Electronic Resource", response.getFormat());
      context.assertTrue(response.getIdentifier().isEmpty());
      context.assertEquals("kb", response.getSource());
      context.assertTrue(response.getLanguage().isEmpty());

      async.complete();
    }).exceptionally(throwable -> {
      context.fail(throwable);
      async.complete();
      return null;
    });
  }

  @Test
  public void testGetInstanceEmptySubjectList(TestContext context) {
    Async async = context.async();

    RMAPIToCodex.getInstance(vertx.getOrCreateContext(), configuration, 1619586)
      .whenComplete((response, throwable) -> {
      context.assertEquals("1619586", response.getId());
      context.assertEquals("Tom, Dick and Harry", response.getTitle());
      context.assertTrue(response.getSubject().isEmpty());
      context.assertEquals("Project Gutenberg Literary Archive Foundation", response.getPublisher());
      context.assertEquals(Instance.Type.EBOOKS, response.getType());
      context.assertEquals("Electronic Resource", response.getFormat());
      context.assertTrue(response.getIdentifier().isEmpty());
      context.assertEquals("kb", response.getSource());
      context.assertTrue(response.getLanguage().isEmpty());

      async.complete();
    }).exceptionally(throwable -> {
      context.fail(throwable);
      async.complete();
      return null;
    });
  }

  @Test
  public void testGetInstances(TestContext context) throws QueryValidationException {
    Async async = context.async();
    TitleParameters parameters = new TitleParameters(new CQLParameters("title=moby%20dick"));
    PaginationInfo pagination = new PaginationCalculator().getPagination(0, 5);

    RMAPIToCodex.getInstances(parameters, pagination, vertx.getOrCreateContext(), configuration)
      .whenComplete((response, throwable) -> {
        context.assertEquals(524, response.getResultInfo().getTotalRecords());
        context.assertEquals(5, response.getInstances().size());

        async.complete();
      }).exceptionally(throwable -> {
      context.fail(throwable);
      async.complete();
      return null;
    });
  }

//  @Test
//  public void testGetInstancesIdSearch(TestContext context) {
//    Async async = context.async();
//
//    ConfigurationService configurationService(okapiHeaders).thenCompose(config -> {
//      final CQLParameters cql;
//      try {
//        cql = new CQLParameters("id=1619585");
//      } catch (QueryValidationException e) {
//        throw new CompletionException(e);
//      }
//
//      return RMAPIToCodex.getInstanceById(vertx.getOrCreateContext(), config, cql.getIdSearchValue());
//    }).whenComplete((response, throwable) -> {
//      context.assertEquals(1, response.getResultInfo().getTotalRecords());
//      context.assertEquals(1, response.getInstances().size());
//
//      async.complete();
//    }).exceptionally(throwable -> {
//      context.fail(throwable);
//      async.complete();
//      return null;
//    });
//  }

  @Test
  public void testGetInstancesPaging(TestContext context) throws QueryValidationException {
    Async async = context.async();
    TitleParameters parameters = new TitleParameters(new CQLParameters("title=moby%20dick"));
    PaginationInfo pagination = new PaginationCalculator().getPagination(2, 5);

    RMAPIToCodex.getInstances(parameters, pagination, vertx.getOrCreateContext(), configuration)
      .whenComplete((response, throwable) -> {
        context.assertEquals(524, response.getResultInfo().getTotalRecords());
        context.assertEquals(5, response.getInstances().size());
        context.assertEquals("60 minutes. Dick Clarke", response.getInstances().get(0).getTitle());
        async.complete();
      }).exceptionally(throwable -> {
      context.fail(throwable);
      async.complete();
      return null;
    });
  }

  @Test
  public void testGetInstancesPagingIndexGTCount(TestContext context) throws QueryValidationException {
    Async async = context.async();

    TitleParameters parameters = new TitleParameters(new CQLParameters("title=moby%20dick"));
    PaginationInfo pagination = new PaginationCalculator().getPagination(7, 10);

    RMAPIToCodex.getInstances(parameters, pagination, vertx.getOrCreateContext(), configuration)
      .whenComplete((response, throwable) -> {
        context.assertEquals(5, response.getResultInfo().getTotalRecords());
        context.assertEquals(0, response.getInstances().size());

        async.complete();
      }).exceptionally(throwable -> {
      context.fail(throwable);
      async.complete();
      return null;
    });
  }

  @Test
  public void constructorIsPrivateTest(TestContext context) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    Constructor<RMAPIToCodex> constructor = RMAPIToCodex.class.getDeclaredConstructor();
    context.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    constructor.newInstance();
  }

  @Test
  public void typeAndSubTypeTest(TestContext context) {
    context.assertEquals("ISSN", IdentifierType.ISSN.getDisplayName());
    context.assertEquals(0, IdentifierType.ISSN.getCode());

    context.assertEquals("Print", IdentifierSubType.PRINT.getDisplayName());
    context.assertEquals(1, IdentifierSubType.PRINT.getCode());
  }

  @Test
  public void pubTypeTest(TestContext context) {
    context.assertEquals("streamingaudio", PubType.fromCodex("audio").getRmAPI());

    try {
      PubType.fromRMAPI("bad-match");
    } catch (IllegalArgumentException e) {
      return;
    }

    context.fail("Expected exception");
  }
}
