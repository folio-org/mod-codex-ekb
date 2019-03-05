package org.folio.rest.impl;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import javax.ws.rs.core.Response;

import org.folio.codex.RMAPIToCodex;
import org.folio.cql2rmapi.CQLParameters;
import org.folio.cql2rmapi.PackageParameters;
import org.folio.cql2rmapi.QueryValidationException;
import org.folio.cql2rmapi.query.PaginationCalculator;
import org.folio.cql2rmapi.query.PaginationInfo;
import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.OkapiData;
import org.folio.holdingsiq.service.ConfigurationService;
import org.folio.holdingsiq.service.impl.ProviderHoldingsIQServiceImpl;
import org.folio.rest.jaxrs.model.PackageCollection;
import org.folio.rest.jaxrs.model.Source;
import org.folio.rest.jaxrs.model.SourceCollection;
import org.folio.rest.jaxrs.resource.CodexPackages;
import org.folio.rest.jaxrs.resource.CodexPackagesSources;
import org.folio.rest.tools.PomReader;
import org.folio.spring.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * Package related codex APIs.
 */
public final class CodexPackagesImpl implements CodexPackages, CodexPackagesSources {

  private static final String MODULE_SOURCE = "kb";

  @Autowired
  private ConfigurationService configurationService;

  public CodexPackagesImpl() {
    SpringContextUtil.autowireDependencies(this, Vertx.currentContext());
  }

  @Override
  public void getCodexPackages(String query, int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    CompletableFuture.completedFuture(null)
      .thenCompose(o -> configurationService.retrieveConfiguration(new OkapiData(okapiHeaders)))
      .thenCompose(rmAPIConfig -> getPackages(query, offset, limit, vertxContext, rmAPIConfig))
      .thenAccept(packages ->
        asyncResultHandler.handle(Future.succeededFuture(CodexPackages.GetCodexPackagesResponse.respond200WithApplicationJson(packages))));
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

  private CompletionStage<PackageCollection> getPackages(String query, int offset, int limit, Context vertxContext, Configuration rmAPIConfig) {
    ProviderHoldingsIQServiceImpl providerService = new ProviderHoldingsIQServiceImpl(rmAPIConfig, vertxContext.owner());
    return providerService.getVendorId()
      .thenCompose(vendorId -> getPackages(query, offset, limit, vendorId, vertxContext, rmAPIConfig));
  }

  private CompletionStage<PackageCollection> getPackages(String query, int offset, int limit, long providerId, Context vertxContext, Configuration rmAPIConfig) {
    try {
      CQLParameters cqlParameters = new CQLParameters(query);
      if (cqlParameters.isIdSearch()) {
        return getPackageById();
      }
      PackageParameters parameters = new PackageParameters(cqlParameters);
      PaginationInfo pagination = new PaginationCalculator().getPagination(offset, limit);
      return RMAPIToCodex.getPackages(parameters, pagination, providerId, vertxContext, rmAPIConfig);
    } catch (QueryValidationException e) {
      throw new CompletionException(e);
    }
  }

  private CompletionStage<PackageCollection> getPackageById() {
    throw new UnsupportedOperationException("Retrieval of package by id is not implemented");
  }
}
