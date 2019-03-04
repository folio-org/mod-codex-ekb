package org.folio.rest.impl;

import static io.vertx.core.Future.succeededFuture;

import java.util.Collections;
import java.util.Map;

import javax.validation.ValidationException;
import javax.ws.rs.core.Response;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.folio.codex.RMAPIToCodex;
import org.folio.holdingsiq.model.OkapiData;
import org.folio.holdingsiq.service.ConfigurationService;
import org.folio.holdingsiq.service.exception.ConfigurationServiceException;
import org.folio.holdingsiq.service.exception.ResourceNotFoundException;
import org.folio.parser.IdParser;
import org.folio.rest.jaxrs.model.Package;
import org.folio.rest.jaxrs.model.Source;
import org.folio.rest.jaxrs.model.SourceCollection;
import org.folio.rest.jaxrs.resource.CodexPackages;
import org.folio.rest.jaxrs.resource.CodexPackagesSources;
import org.folio.rest.tools.PomReader;
import org.folio.spring.SpringContextUtil;

/**
 * Package related codex APIs.
 */
public final class CodexPackagesImpl implements CodexPackages, CodexPackagesSources {

  private final Logger log = LoggerFactory.getLogger(CodexPackagesImpl.class);

  private static final String MODULE_SOURCE = "kb";

  @Autowired
  private ConfigurationService configurationService;
  @Autowired
  private IdParser idParser;


  public CodexPackagesImpl() {
    SpringContextUtil.autowireDependencies(this, Vertx.currentContext());
  }

  @Override
  public void getCodexPackages(String query, int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    asyncResultHandler.handle(succeededFuture(Response.status(Response.Status.NOT_IMPLEMENTED).build()));
  }

  @Override
  public void getCodexPackagesById(String id, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    log.info("method call: getCodexPackagesById");

    configurationService.retrieveConfiguration(new OkapiData(okapiHeaders))
      .thenCompose(config -> RMAPIToCodex.getPackage(vertxContext, config, idParser.parsePackageId(id)))
      .thenAccept(pkg -> successfulPkgById(pkg, asyncResultHandler))
      .exceptionally(throwable -> failedPkgById(id, throwable, asyncResultHandler));
  }

  private void successfulPkgById(Package pkg, Handler<AsyncResult<Response>> handler) {
    handler.handle(succeededFuture(GetCodexPackagesByIdResponse.respond200WithApplicationJson(pkg)));
  }

  private Void failedPkgById(String id, Throwable throwable, Handler<AsyncResult<Response>> handler) {
    log.error("getCodexPackagesById failed!", throwable);

    Response response;
    if (throwable.getCause() instanceof ResourceNotFoundException ||
        throwable.getCause() instanceof ValidationException) {
      response = GetCodexPackagesByIdResponse.respond404WithTextPlain(id);
    } else if (throwable.getCause() instanceof ConfigurationServiceException &&
        ((ConfigurationServiceException) throwable.getCause()).getStatusCode() == 401) {
      response = GetCodexPackagesByIdResponse.respond401WithTextPlain(throwable.getCause().getMessage());
    } else {
      response = GetCodexPackagesByIdResponse.respond500WithTextPlain(throwable.getCause().getMessage());
    }

    handler.handle(succeededFuture(response));

    return null;
  }

  @Override
  public void getCodexPackagesSources(String lang, Map<String, String> okapiHeaders,
                                      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    String moduleName = PomReader.INSTANCE.getModuleName().replace("_", "-");
    String moduleVersion = PomReader.INSTANCE.getVersion();
    asyncResultHandler.handle(succeededFuture(GetCodexPackagesSourcesResponse.respond200WithApplicationJson(new SourceCollection()
      .withSources(Collections.singletonList(
        new Source()
          .withId(MODULE_SOURCE)
          .withName(moduleName + "-" + moduleVersion)
      )))));
  }
}
