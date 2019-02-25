package org.folio.rest.impl;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.folio.rest.jaxrs.model.Source;
import org.folio.rest.jaxrs.model.SourceCollection;
import org.folio.rest.jaxrs.resource.CodexPackages;
import org.folio.rest.jaxrs.resource.CodexPackagesSources;
import org.folio.rest.tools.PomReader;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * Package related codex APIs.
 */
public final class CodexPackagesImpl implements CodexPackages, CodexPackagesSources {

  private static final String MODULE_SOURCE = "kb";

  @Override
  public void getCodexPackages(String query, int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    asyncResultHandler.handle(Future.succeededFuture(Response.status(Response.Status.NOT_IMPLEMENTED).build()));
  }

  @Override
  public void getCodexPackagesById(String id, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    asyncResultHandler.handle(Future.succeededFuture(Response.status(Response.Status.NOT_IMPLEMENTED).build()));
  }

  @Override
  public void getCodexPackagesSources(String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    String moduleName = PomReader.INSTANCE.getModuleName().replace("_", "-");
    String moduleVersion = PomReader.INSTANCE.getVersion();
    asyncResultHandler.handle(Future.succeededFuture(GetCodexPackagesSourcesResponse.respond200WithApplicationJson(new SourceCollection()
      .withSources(Collections.singletonList(
        new Source()
          .withId(MODULE_SOURCE)
          .withName(moduleName + "-" + moduleVersion)
      )))));
  }
}
