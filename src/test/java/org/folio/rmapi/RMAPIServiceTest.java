package org.folio.rmapi;

import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.tools.utils.VertxUtils;
import org.junit.Ignore;
import org.junit.Test;
import io.vertx.core.Future;

public class RMAPIServiceTest {


  @Ignore("Not Unit Test - Temp Used for local testing against rmapi") @Test()
  public void testGetTitleById() {
 
    String custId = "XXXX";
    String apiKey = "XXXX";
    
    
    RMAPIService svc = new RMAPIService(custId,apiKey,RMAPIService.getBaseURI(), VertxUtils.getVertxFromContextOrNew());
    
    final Future<Instance> codexInstanceFuture = svc.getTileById("161509");
    
    codexInstanceFuture.setHandler(result -> {
      if (result.failed()) {
        System.out.println("get title failed" +result.cause()); 
        
      } else {
        final Instance codexInstance = result.result();
        System.out.println("get title success - title name is" +codexInstance.getTitle()); 
      }
    });
   
  }
}