package org.folio.codex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.vertx.core.Context;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.Title;
import org.folio.holdingsiq.model.Titles;
import org.folio.holdingsiq.service.impl.TitlesHoldingsIQServiceImpl;
import org.springframework.core.convert.converter.Converter;

import org.folio.converter.hld2cdx.ContributorConverter;
import org.folio.converter.hld2cdx.IdentifierConverter;
import org.folio.converter.hld2cdx.SubjectConverter;
import org.folio.converter.hld2cdx.TitleConverter;
import org.folio.cql2rmapi.query.RMAPIQueries;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.InstanceCollection;
import org.folio.rest.jaxrs.model.ResultInfo;

/**
 * @author mreno
 *
 */
public final class RMAPIToCodex {
  private static final Logger log = LoggerFactory.getLogger(RMAPIToCodex.class);

  private static final Converter<Title, Instance> TITLE_CONVERTER = new TitleConverter(
    new IdentifierConverter(), new ContributorConverter(), new SubjectConverter());

  private RMAPIToCodex() {
    super();
  }

  public static CompletableFuture<Instance> getInstance(Context vertxContext, Configuration rmAPIConfig, long id) {
    log.info("Calling getInstance");

    TitlesHoldingsIQServiceImpl titlesService = new TitlesHoldingsIQServiceImpl(rmAPIConfig.getCustomerId(), rmAPIConfig.getApiKey(),
        rmAPIConfig.getUrl(), vertxContext.owner());

    return titlesService.retrieveTitle(id)
        .thenApply(TITLE_CONVERTER::convert);
  }

  public static CompletableFuture<InstanceCollection> getInstances(RMAPIQueries cql, Context vertxContext,
                                                                   Configuration rmAPIConfig) {
    log.info("Calling getInstances");

    final List<CompletableFuture<Titles>> titleCfs = new ArrayList<>();

    // We need to create a new RMAPIService for each call so that we don't close
    // the HTTP client connection.
    for (String query : cql.getRMAPIQueries()) {
      titleCfs.add(new TitlesHoldingsIQServiceImpl(rmAPIConfig.getCustomerId(), rmAPIConfig.getApiKey(),
          rmAPIConfig.getUrl(), vertxContext.owner()).retrieveTitles(query));
    }


    return CompletableFuture
      .allOf(titleCfs.toArray(new CompletableFuture[0]))
      .thenCompose(aVoid ->  {
        final List<Titles> collect = titleCfs.stream().map(CompletableFuture::join).collect(Collectors.toList());
        return convertRMTitleListToCodex(collect, cql.getFirstObjectIndex(), cql.getLimit());
      });
  }


  private static CompletableFuture<InstanceCollection> convertRMTitleListToCodex(List<Titles> titles, int index, int limit) {

    final InstanceCollection instanceCollection = new InstanceCollection();
      List<Instance> instances = new ArrayList<>();
      int totalResults = 0;

      for (Titles title : titles) {
        totalResults = Math.max(totalResults, title.getTotalResults());
        instances.addAll(title.getTitleList().stream()
            .map(TITLE_CONVERTER::convert)
            .collect(Collectors.toList()));
      }

      int start = Math.min(index, instances.size());
      int end = Math.min(index + limit, instances.size());
      instances = instances.subList(start, end);

      instanceCollection.setInstances(instances);
      instanceCollection.setResultInfo(new ResultInfo().withTotalRecords(totalResults));

      return CompletableFuture.completedFuture(instanceCollection);
  }
}
