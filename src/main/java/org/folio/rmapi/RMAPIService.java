package org.folio.rmapi;

import java.util.ArrayList;
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
    private static final String RMAPI_SANDBOX_BASE_URI = "https://sandbox.ebsco.io";
    private static final String E_RESOURCE_FORMAT = "Electronic Resource";
    
    private String customerId;
    private String apiKey;
    private String baseURI;
    
    private HttpClient httpClient;
    
    /**
     * Returns base url of rmapi service. Setting will vary depending upon environment (sandbox or production)
     * @return base RMAPI URI 
     */
    public static String getBaseURI() {
      return RMAPI_SANDBOX_BASE_URI;
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
      httpClient = vertx.createHttpClient();
     }
    
    /**
     * @param titleId
     * @return
     */
    public Future<Instance> getTileById(String titleId) {  
      
      Future<Instance> future = Future.future();
      final HttpClientRequest request = httpClient.getAbs(constructURL(String.format("titles/%s", titleId)));
 
      request.headers().add("Accept","application/json");
      request.headers().add("Content-Type", "application/json");
      request.headers().add("X-Api-Key", apiKey);
      
      LOG.info("absolute URL is" + request.absoluteURI());
 
      request.handler(response -> 
      
        response.bodyHandler(body -> {
          
          LOG.info("rmapi request status code =" + response.statusCode());
          
          // need to only handle status code = 200
          // other status codes should return and throw an error
          if (response.statusCode() == 200)
          {
            try {
              LOG.info(body.toString());
              final JsonObject instanceJSON = new JsonObject(body.toString()); 
              mapResultsFromClass(instanceJSON, future);
            }
            catch (Exception e) {
              LOG.info("failure  " + e.getMessage());
              future.fail("Error parsing return json object" + e.getMessage()); 
            }
            finally
            {
              httpClient.close();
            }
          }
          else 
          {
            httpClient.close();
            future.fail("Invalid status code from RMAPI" + response.statusCode());
          }
        })
      );     
      request.end();
      
      return future;
    }
    /**
     * @param rmapiQuery
     * @return
     */
    public Future<List<Instance>> getTitleList(String rmapiQuery) {
      
      Future<List<Instance>> future = Future.future();
      
      future.complete(new ArrayList<Instance>());
      return future;
    }
  
  
    /**
     * @param instanceJSON
     * @param future
     */
    private static void mapResultsFromClass(JsonObject instanceJSON, Future<Instance> future ) {
     
      Instance codexInstance = new Instance();
      
      RMAPITitle svcTitle = instanceJSON.mapTo(RMAPITitle.class);
         
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
      codexInstance.setFormat(E_RESOURCE_FORMAT);
      // storing source as kbid
      codexInstance.setSource(Integer.toString(svcTitle.titleId));
      codexInstance.setVersion(svcTitle.edition);

      // TO DO: need to include identifier and contributor collections
   
      future.complete(codexInstance);
    } 
    
    /**
     * @param path
     * @return
     */
    private String constructURL(String path){
       String fullPath = String.format("%s/rm/rmaccounts/%s/%s", baseURI, customerId, path);
     
       LOG.info("constructurl - path=" + fullPath);
       return fullPath;
    }
}