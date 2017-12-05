package org.folio.rest.impl;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.folio.config.RMAPIConfiguration;
import org.folio.cql2rmapi.CQLParserForRMAPI;
import org.folio.cql2rmapi.QueryValidationException;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.resource.CodexInstancesResource;
import org.folio.rmapi.RMAPIService;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Instance related codex APIs.
 *
 * @author mreno
 *
 */
public final class CodexInstancesResourceImpl implements CodexInstancesResource {
  private final Logger log = LoggerFactory.getLogger(CodexInstancesResourceImpl.class);

  public CodexInstancesResourceImpl(Vertx vertx, String tenantId) {
    super();
  }

  /* (non-Javadoc)
   * @see org.folio.rest.jaxrs.resource.InstancesResource#getCodexInstances(java.lang.String, int, int, java.lang.String, java.util.Map, io.vertx.core.Handler, io.vertx.core.Context)
   */
  @Override
  @Validate
  public void getCodexInstances(String query, int offset, int limit, String lang,
      Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext)
      throws Exception {
    log.info("method call: getInstances");

    final Future<RMAPIConfiguration> config = RMAPIConfiguration.getConfiguration(okapiHeaders);

    config.setHandler(result -> {
      if (result.failed()) {
        log.error("Config call failed!", result.cause());
      } else {
        final RMAPIConfiguration rmAPIConfig = result.result();
        log.info("RM API Config: " + rmAPIConfig);
      }
    });

    log.info("Calling CQL Parser");
    try {
    	final CQLParserForRMAPI parserForRMAPI = new CQLParserForRMAPI(query, offset, limit);
    	final String queryForRMAPI = parserForRMAPI.getRMAPIQuery();
    	log.info("Query to be passed to RM API is " + queryForRMAPI);
    } catch (final QueryValidationException e) {
    	log.error("CQL Query Validation failed!", e);
    }

    throw new UnsupportedOperationException("Work in progress");
  }

  @Override
  @Validate
  public void getCodexInstancesById(String id, String lang,
      Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext)
      throws Exception {
    log.info("method call: getInstancesById");

    final Future<RMAPIConfiguration> config = RMAPIConfiguration.getConfiguration(okapiHeaders);

    config.setHandler(result -> {
      if (result.failed()) {
        log.error("Config call failed!", result.cause());
      } else {
        final RMAPIConfiguration rmAPIConfig = result.result();
        log.info("RM API Config: " + rmAPIConfig);
        
        RMAPIService svc = new RMAPIService(rmAPIConfig.getCustomerId(),rmAPIConfig.getAPIKey(), RMAPIService.getBaseURI(), vertxContext.owner());
        
        final Future<Instance> codexInstanceFuture = svc.getTileById(id);
        
        codexInstanceFuture.setHandler(rmapiResult -> {
          if (rmapiResult.failed()) {
            log.error("RMAPI call failed!", rmapiResult.cause());
          } else {
            final Instance codexInstance = rmapiResult.result();
            // Need to wrapper response
            log.info("Request Title Name: " + codexInstance.getTitle());
          }
        });
      }
    });

    throw new UnsupportedOperationException("Work in progress.");
  }
}
