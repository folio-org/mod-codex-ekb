package org.folio.rest.impl;

import static io.vertx.core.Future.succeededFuture;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import javax.validation.ValidationException;
import javax.ws.rs.core.Response;

import org.folio.codex.RMAPIToCodex;
import org.folio.cql2rmapi.CQLParameters;
import org.folio.cql2rmapi.QueryValidationException;
import org.folio.cql2rmapi.TitleParameters;
import org.folio.cql2rmapi.query.PaginationCalculator;
import org.folio.cql2rmapi.query.PaginationInfo;
import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.OkapiData;
import org.folio.holdingsiq.service.ConfigurationService;
import org.folio.holdingsiq.service.exception.ConfigurationServiceException;
import org.folio.holdingsiq.service.exception.ResourceNotFoundException;
import org.folio.parser.IdParser;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.InstanceCollection;
import org.folio.rest.jaxrs.model.ResultInfo;
import org.folio.rest.jaxrs.resource.CodexInstances;
import org.folio.rest.jaxrs.resource.CodexInstancesSources;
import org.folio.spring.SpringContextUtil;
import org.folio.validator.QueryValidator;
import org.springframework.beans.factory.annotation.Autowired;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Instance related codex APIs.
 *
 * @author mreno
 */
public final class CodexInstancesImpl implements CodexInstances, CodexInstancesSources {

  private final Logger log = LoggerFactory.getLogger(CodexInstancesImpl.class);

  @Autowired
  private ConfigurationService configurationService;
  @Autowired
  private QueryValidator queryValidator;
  @Autowired
  private IdParser idParser;

  public CodexInstancesImpl() {
    SpringContextUtil.autowireDependencies(this, Vertx.currentContext());
  }


  /* (non-Javadoc)
   * @see org.folio.rest.jaxrs.resource.InstancesResource#getCodexInstances(java.lang.String, int, int, java.lang.String, java.util.Map, io.vertx.core.Handler, io.vertx.core.Context)
   */
  @Override
  @Validate
  public void getCodexInstances(String query, int offset, int limit, String lang, Map<String, String> okapiHeaders,
                                Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    log.info("method call: getCodexInstances");

    CompletableFuture.completedFuture(null)
      .thenCompose(o -> {
        queryValidator.validate(query, limit);
        return configurationService.retrieveConfiguration(new OkapiData(okapiHeaders));
      })
      .thenCompose(rmAPIConfig -> getCodexInstances(query, offset, limit, vertxContext, rmAPIConfig))
      .thenAccept(instances ->
         asyncResultHandler.handle(succeededFuture(CodexInstances.GetCodexInstancesResponse.respond200WithApplicationJson(instances))))
      .exceptionally(throwable -> {
        log.error("getCodexInstances failed!", throwable);
        if (throwable.getCause() instanceof ValidationException || throwable.getCause() instanceof QueryValidationException) {
          asyncResultHandler.handle(succeededFuture(CodexInstances.GetCodexInstancesResponse.respond400WithTextPlain(throwable.getCause().getMessage())));
        } else if (throwable.getCause() instanceof ConfigurationServiceException && ((ConfigurationServiceException) throwable.getCause()).getStatusCode() == 401) {
          asyncResultHandler.handle(succeededFuture(CodexInstances.GetCodexInstancesResponse.respond401WithTextPlain(throwable.getCause().getMessage())));
        } else {
          asyncResultHandler.handle(succeededFuture(CodexInstances.GetCodexInstancesResponse.respond500WithTextPlain(throwable.getCause().getMessage())));
        }
        return null;
      });
  }

  @Override
  @Validate
  public void getCodexInstancesById(String id, String lang, Map<String, String> okapiHeaders,
                                    Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    log.info("method call: getCodexInstancesById");

    configurationService.retrieveConfiguration(new OkapiData(okapiHeaders))
      .thenCompose(rmAPIConfig ->
        RMAPIToCodex.getInstance(vertxContext, rmAPIConfig, idParser.parseTitleId(id))
      ).thenApply(instance -> {
      asyncResultHandler.handle(
            succeededFuture(CodexInstances.GetCodexInstancesByIdResponse.respond200WithApplicationJson(instance)));
      return instance;
    }).exceptionally(throwable -> {
      log.error("getCodexInstancesById failed!", throwable);
      if (throwable.getCause() instanceof ResourceNotFoundException
        || throwable.getCause() instanceof ValidationException) {
        asyncResultHandler.handle(
              succeededFuture(CodexInstances.GetCodexInstancesByIdResponse.respond404WithTextPlain(id)));
      } else if (throwable.getCause() instanceof ConfigurationServiceException && ((ConfigurationServiceException) throwable.getCause()).getStatusCode() == 401) {
        	asyncResultHandler.handle(succeededFuture(CodexInstances.GetCodexInstancesResponse.respond401WithTextPlain(throwable.getCause().getMessage())));
      } else {
        	asyncResultHandler.handle(succeededFuture(CodexInstances.GetCodexInstancesByIdResponse.respond500WithTextPlain(throwable.getCause().getMessage())));
      }
      return null;
    });
  }

  private CompletionStage<InstanceCollection> getCodexInstances(String query, int offset, int limit,
                                                                Context vertxContext, Configuration rmAPIConfig) {
    try {
      CQLParameters cqlParameters = new CQLParameters(query);
      if (cqlParameters.isIdSearch()) {
        return getInstanceById(vertxContext, rmAPIConfig, cqlParameters);
      }

      TitleParameters parameters = new TitleParameters(cqlParameters);

      PaginationInfo pagination = new PaginationCalculator().getPagination(offset, limit);
      return RMAPIToCodex.getInstances(parameters, pagination, vertxContext, rmAPIConfig);
    } catch (QueryValidationException e) {
      throw new CompletionException(e);
    }
  }

  private CompletionStage<InstanceCollection> getInstanceById(Context vertxContext, Configuration rmAPIConfig, CQLParameters cqlParameters) {
    return RMAPIToCodex.getInstance(vertxContext, rmAPIConfig, idParser.parseTitleId(cqlParameters.getIdSearchValue()))
      .thenApply(instance ->
        new InstanceCollection()
          .withInstances(Collections.singletonList(instance))
          .withResultInfo(new ResultInfo().withTotalRecords(1))
      ).exceptionally(throwable ->
        new InstanceCollection().withResultInfo(new ResultInfo().withTotalRecords(0))
      );
  }

  @Override
  public void getCodexInstancesSources(String lang, Map<String, String> okapiHeaders,
                                       Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {

    asyncResultHandler.handle(
      succeededFuture(GetCodexInstancesSourcesResponse.status(Response.Status.NOT_IMPLEMENTED).build()));
  }
}
