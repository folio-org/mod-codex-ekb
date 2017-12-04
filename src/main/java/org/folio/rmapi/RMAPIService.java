package org.folio.rmapi;

import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.tools.utils.VertxUtils;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class RMAPIService {
  
    private static final Logger LOG = LoggerFactory.getLogger(RMAPIService.class);
    
    private String customerId;
    private String apiKey;
    private String baseURI;
    
    private HttpClient httpClient;
    private Vertx vertx;
    
    public RMAPIService(String customerId, String apiKey, String baseURI){
      LOG.info("method call: RMAPIService constructor");
      this.customerId = customerId;
      this.apiKey = apiKey;
      this.baseURI = baseURI;
     }
    
    public Future<Instance> GetTitleById(String titleId) {  
      LOG.info("method call: GetTitleById");
      
      vertx = VertxUtils.getVertxFromContextOrNew();
      HttpClientOptions options = new HttpClientOptions();
      httpClient = vertx.createHttpClient(options); 
      
      Future<Instance> future = Future.future();
      HttpClientRequest request = httpClient.getAbs(ConstructURL(String.format("titles/%s", titleId)));
 
      request.headers().add("Accept","application/json");
      request.headers().add("Content-Type", "application/json");
      request.headers().add("X-Api-Key", apiKey);
      
      LOG.info("absolute URL is" + request.absoluteURI().toString());
 
      request.handler(response -> {
      
        response.bodyHandler(body -> {
          
          LOG.info("request status code =" + response.statusCode());
          
          // need to only handle status code = 200
          // to do constants needed for codes
          // other status codes should return and throw an error
          if (response.statusCode() == 200)
          {
            try {
              LOG.info(body.toString());
              JsonObject InstanceJson = new JsonObject(body.toString()); 
              mapResultsFromClass(InstanceJson, future);
            }
            catch (Exception e) {
              LOG.info("failure  " + e.getMessage());
              future.fail("Error parsing return json object"); 
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
      
      //codexInstance.setId(svcTitle.titleId);
   
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
    
    private String ConstructURL(String path){
       String fullPath = String.format("%s/rm/rmaccounts/%s/%s", baseURI, customerId, path);
     
       LOG.info("constructurl - path=" + fullPath);
       return fullPath;
    }
}
