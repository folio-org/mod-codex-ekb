package org.folio.codex;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.folio.config.RMAPIConfiguration;
import org.folio.cql2rmapi.CQLParserForRMAPI;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.InstanceCollection;
import org.folio.rmapi.RMAPIService;
import org.folio.rmapi.model.Title;
import org.folio.rmapi.model.Titles;

import io.vertx.core.Context;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * @author mreno
 *
 */
public final class RMAPIToCodex {
  private static final Logger log = LoggerFactory.getLogger(RMAPIToCodex.class);

  private static final String E_RESOURCE_FORMAT = "Electronic Resource";
  private static final String E_RESOURCE_SOURCE = "kb";

  private RMAPIToCodex() {
    super();
  }

  public static CompletableFuture<Instance> getInstance(String id, Context vertxContext,
      RMAPIConfiguration rmAPIConfig) {
    log.info("Calling getInstance");

    final RMAPIService rmAPIService = new RMAPIService(rmAPIConfig.getCustomerId(), rmAPIConfig.getAPIKey(),
        rmAPIConfig.getUrl(), vertxContext.owner());

    final CompletableFuture<Instance> cf = new CompletableFuture<>();

    rmAPIService.getTileById(id).setHandler(rmapiResult -> {
      if (rmapiResult.failed()) {
        cf.completeExceptionally(rmapiResult.cause());
      } else {
        final Instance codexInstance = convertRMAPITitleToCodex(rmapiResult.result());
        cf.complete(codexInstance);
      }
    });

    return cf;
  }

  public static CompletableFuture<InstanceCollection> getInstances(CQLParserForRMAPI cql, Context vertxContext,
      RMAPIConfiguration rmAPIConfig) {
    log.info("Calling getInstances");

    final RMAPIService rmAPIService = new RMAPIService(rmAPIConfig.getCustomerId(), rmAPIConfig.getAPIKey(),
        rmAPIConfig.getUrl(), vertxContext.owner());
    final String query = cql.getRMAPIQuery();
    final CompletableFuture<InstanceCollection> cf = new CompletableFuture<>();

    rmAPIService.getTitleList(query).setHandler(rmapiResult -> {
      if (rmapiResult.failed()) {
        cf.completeExceptionally(rmapiResult.cause());
      } else {
        final InstanceCollection codexInstances = convertRMTitleListToCodex(rmapiResult.result());

        cf.complete(codexInstances);
      }
    });

    return cf;
  }

  /**
   * Converts RMAPI title to Codex instance still need to map the following fields
   * identifiersList, contributorsList note - contributorsList is only available
   * in title detail record update type to normalized values
   * 
   * @param svcTitle
   * @return
   */
  private static Instance convertRMAPITitleToCodex(Title svcTitle) {

    Instance codexInstance = new Instance();

    codexInstance.setId(Integer.toString(svcTitle.titleId));
    codexInstance.setTitle(svcTitle.titleName);
    codexInstance.setPublisher(svcTitle.publisherName);
    codexInstance.setType(svcTitle.pubType);
    codexInstance.setFormat(E_RESOURCE_FORMAT);
    codexInstance.setSource(E_RESOURCE_SOURCE);
    codexInstance.setVersion(svcTitle.edition);

    return codexInstance;
  }

  private static InstanceCollection convertRMTitleListToCodex(Titles rmTitles) {

    InstanceCollection instances = new InstanceCollection();
    List<Instance> codexInstances = rmTitles.titleList.stream().map(RMAPIToCodex::convertRMAPITitleToCodex)
        .collect(Collectors.toList());
    instances.setInstances(codexInstances);
    instances.setTotalRecords(rmTitles.totalResults);
    return instances;
  }

}
