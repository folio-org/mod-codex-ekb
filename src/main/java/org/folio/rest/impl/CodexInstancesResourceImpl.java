package org.folio.rest.impl;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.folio.config.RMAPIConfiguration;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.resource.CodexInstancesResource;

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

    Future<RMAPIConfiguration> config = RMAPIConfiguration.getConfiguration(okapiHeaders);

    config.setHandler(result -> {
      if (result.failed()) {
        log.error("Config call failed!", result.cause());
      } else {
        RMAPIConfiguration rmAPIConfig = result.result();
        log.info("RM API Config: " + rmAPIConfig);
      }
    });

    throw new UnsupportedOperationException("Operation not supported.");
  }

  /* (non-Javadoc)
   * @see org.folio.rest.jaxrs.resource.InstancesResource#getCodexInstancesById(java.lang.String, java.lang.String, java.util.Map, io.vertx.core.Handler, io.vertx.core.Context)
   */
  @Override
  @Validate
  public void getCodexInstancesById(String id, String lang,
      Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext)
      throws Exception {
    log.info("method call: getInstancesById");

    Future<RMAPIConfiguration> config = RMAPIConfiguration.getConfiguration(okapiHeaders);

    config.setHandler(result -> {
      if (result.failed()) {
        log.error("Config call failed!", result.cause());
      } else {
        RMAPIConfiguration rmAPIConfig = result.result();
        log.info("RM API Config: " + rmAPIConfig);
      }
    });

    throw new UnsupportedOperationException("Operation not supported.");
  }
}
