package org.folio.codex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import io.vertx.core.Context;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import org.folio.config.RMAPIConfiguration;
import org.folio.converter.hld2cdx.ContributorConverter;
import org.folio.converter.hld2cdx.IdentifierConverter;
import org.folio.converter.hld2cdx.SubjectConverter;
import org.folio.converter.hld2cdx.TitleConverter;
import org.folio.cql2rmapi.query.RMAPIQueries;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.InstanceCollection;
import org.folio.rest.jaxrs.model.ResultInfo;
import org.folio.rmapi.RMAPIResourceNotFoundException;
import org.folio.rmapi.RMAPIService;
import org.folio.rmapi.model.Title;
import org.folio.rmapi.model.Titles;

/**
 * @author mreno
 *
 */
public final class RMAPIToCodex {
  private static final Logger log = LoggerFactory.getLogger(RMAPIToCodex.class);

  private static final Converter<Title, Instance> TITLE_CONVERTER = new TitleConverter(new IdentifierConverter(),
    new ContributorConverter(), new SubjectConverter());

  private RMAPIToCodex() {
    super();
  }

  public static CompletableFuture<Instance> getInstance(String id, Context vertxContext,
      RMAPIConfiguration rmAPIConfig) {
    log.info("Calling getInstance");

    final int titleId;
    try {
      titleId = Integer.parseInt(id);
    } catch (NumberFormatException e) {
      log.error("getInstance() called with an invalid id: " + id, e);
      final CompletableFuture<Instance> failed = new CompletableFuture<>();
      failed.completeExceptionally(new RMAPIResourceNotFoundException("Requested resource " + id + " not found"));
      return failed;
    }

    final RMAPIService rmAPIService = new RMAPIService(rmAPIConfig.getCustomerId(), rmAPIConfig.getAPIKey(),
        rmAPIConfig.getUrl(), vertxContext.owner());

    return rmAPIService.getTitleById(titleId)
        .thenApply(TITLE_CONVERTER::convert);
  }

  public static CompletableFuture<InstanceCollection> getInstances(RMAPIQueries cql, Context vertxContext,
                                                                   RMAPIConfiguration rmAPIConfig) {
    log.info("Calling getInstances");

    final List<CompletableFuture<Titles>> titleCfs = new ArrayList<>();

    // We need to create a new RMAPIService for each call so that we don't close
    // the HTTP client connection.
    for (String query : cql.getRMAPIQueries()) {
      titleCfs.add(new RMAPIService(rmAPIConfig.getCustomerId(), rmAPIConfig.getAPIKey(),
          rmAPIConfig.getUrl(), vertxContext.owner()).getTitleList(query));
    }

    return convertRMTitleListToCodex(titleCfs, cql.getFirstObjectIndex(), cql.getLimit());
  }

  public static CompletionStage<InstanceCollection> getInstanceById(Context vertxContext, RMAPIConfiguration rmAPIConfig, String id) {
    return RMAPIToCodex.getInstance(id, vertxContext, rmAPIConfig)
      .thenApply(instance ->
        new InstanceCollection()
          .withInstances(Collections.singletonList(instance))
          .withResultInfo(new ResultInfo().withTotalRecords(1))
      ).exceptionally(throwable ->
        new InstanceCollection().withResultInfo(new ResultInfo().withTotalRecords(0))
      );
  }

  private static CompletableFuture<InstanceCollection> convertRMTitleListToCodex(List<CompletableFuture<Titles>> titleCfs, int index, int limit) {
    return CompletableFuture.allOf(titleCfs.toArray(new CompletableFuture[titleCfs.size()]))
    .thenApply(result -> {
      final InstanceCollection instanceCollection = new InstanceCollection();
      List<Instance> instances = new ArrayList<>();
      int totalResults = 0;

      for (CompletableFuture<Titles> titleCf : titleCfs) {
        final Titles titles = titleCf.join();
        totalResults = Math.max(totalResults, titles.totalResults);
        instances.addAll(titles.titleList.stream()
            .map(TITLE_CONVERTER::convert)
            .collect(Collectors.toList()));
      }

      int start = Math.min(index, instances.size());
      int end = Math.min(index + limit, instances.size());
      instances = instances.subList(start, end);

      instanceCollection.setInstances(instances);
      instanceCollection.setResultInfo(new ResultInfo().withTotalRecords(totalResults));

      return instanceCollection;
    });
  }
}
