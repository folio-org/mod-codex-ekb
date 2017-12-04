package org.folio.rmapi;

import java.util.List;

import org.folio.rest.jaxrs.model.Instance;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * @author cgodfrey
 *
 */
public class RMAPIService {
  
    private static final Logger LOG = LoggerFactory.getLogger(RMAPIService.class);
    private static final String RMAPI_PRODUCTION_BASEURI = "https://api.ebsco.io";
    private static final String RMAPI_SANDBOX_BASEURI = "https://sandbox.ebsco.io";
    private static final String ERESOURCE_FORMAT = "Electronic Resource";
    
    private String customerId;
    private String apiKey;
    private String baseURI;
    
    private HttpClient httpClient;
    private Vertx vertx;
    
    /**
     * TODO: selection of rmapi base uri needs to be more dynamic
     * @return base RMAPI URI 
     */
    public static String getBaseURI() {
      return RMAPI_SANDBOX_BASEURI;
    }
    
    /**
     * @param customerId
     * @param apiKey
     * @param baseURI
     * @param vertx
     */
    public RMAPIService(String customerId, String apiKey, String baseURI, Vertx vertx){
      this.customerId = customerId;
      this.apiKey = apiKey;
      this.baseURI = baseURI;
      this.vertx = vertx;
      httpClient = vertx.createHttpClient();
     }
    
    /**
     * @param titleId
     * @return
     */
    public Future<Instance> GetTitleById(String titleId) {  
      
      Future<Instance> future = Future.future();
      final HttpClientRequest request = httpClient.getAbs(ConstructURL(String.format("titles/%s", titleId)));
 
      request.headers().add("Accept","application/json");
      request.headers().add("Content-Type", "application/json");
      request.headers().add("X-Api-Key", apiKey);
      
      LOG.info("absolute URL is" + request.absoluteURI().toString());
 
      request.handler(response -> {
      
        response.bodyHandler(body -> {
          
          LOG.info("rmapi request status code =" + response.statusCode());
          
          // need to only handle status code = 200
          // to do constants needed for codes
          // other status codes should return and throw an error
          if (response.statusCode() == 200)
          {
            try {
              LOG.info(body.toString());
              final JsonObject InstanceJson = new JsonObject(body.toString()); 
              mapResultsFromClass(InstanceJson, future);
            }
            catch (Exception e) {
              LOG.info("failure  " + e.getMessage());
              future.fail("Error parsing return json object" + e.getMessage()); 
            }
          }
          else 
          {
            future.fail("Invalid status code from RMAPI" + response.statusCode());
          }
        });
       });     
      request.end();
      
      return future;
    }
    /**
     * @param rmapiQuery
     * @return
     */
    public Future<List<Instance>> GetTitleList(String rmapiQuery) {
      
      Future<List<Instance>> future = Future.future();
      
      
      return future;
    }
  
  
    /**
     * @param InstanceJson
     * @param future
     */
    private static void mapResultsFromClass(JsonObject InstanceJson, Future<Instance> future ) {
     
      Instance codexInstance = new Instance();
      
      RMAPITitle svcTitle = InstanceJson.mapTo(RMAPITitle.class);
         
      LOG.info("title name " + svcTitle.titleName);
      LOG.info("Edition " + svcTitle.edition);
      LOG.info("Publisher Name " + svcTitle.publisherName);
      LOG.info("PubType " + svcTitle.pubType);
      LOG.info("titleid " + svcTitle.titleId);
      LOG.info("identifiers count " + svcTitle.identifiers.size());
   
      svcTitle.identifiers.forEach(i -> {
        LOG.info("identifier id" + i.id);
        LOG.info("identifier source" + i.source);
        LOG.info("identifier subtype" + i.subtype);
        LOG.info("identifier type" + i.type);
      });
      
      LOG.info("contributors count " + svcTitle.contributors.size());
      svcTitle.contributors.forEach(c -> {        
        LOG.info("contributor type" + c.type);
        LOG.info("contributor" + c.contributor);
       });
      
      codexInstance.setId(Integer.toString(svcTitle.titleId));
      codexInstance.setTitle(svcTitle.titleName);
      codexInstance.setPublisher(svcTitle.publisherName);
      codexInstance.setType(svcTitle.pubType);
      codexInstance.setFormat(ERESOURCE_FORMAT);
      // storing source as kbid
      codexInstance.setSource(Integer.toString(svcTitle.titleId));
      codexInstance.setVersion(svcTitle.edition);

      // TO DO: need to include identifier and contributor collections
   
      future.complete(codexInstance);
    } 
    /*
    private static void mapResultsFromJson(JsonObject InstanceJson, Future<Instance> future ) {
      
      Instance codexInstance = new Instance();
          
      LOG.info("field names" + InstanceJson.fieldNames());
      
      LOG.info("title name" + InstanceJson.getString("titleName"));
      LOG.info("title id" + InstanceJson.getInteger("titleId"));
      LOG.info("publisher name" + InstanceJson.getString("publisherName"));
      LOG.info("contributors" + InstanceJson.getJsonArray("contributorsList"));
      LOG.info("identifiers" + InstanceJson.getJsonArray("identifiersList"));
      LOG.info("pubtype" + InstanceJson.getString("pubType"));
      LOG.info("format Electronic Resource");
      LOG.info("source" + InstanceJson.getInteger("titleId"));
      LOG.info("edition" + InstanceJson.getInteger("edition"));

      future.complete(codexInstance);
    } 
    */
    
    /**
     * @param path
     * @return
     */
    private String ConstructURL(String path){
       String fullPath = String.format("%s/rm/rmaccounts/%s/%s", baseURI, customerId, path);
     
       LOG.info("constructurl - path=" + fullPath);
       return fullPath;
    }
}
