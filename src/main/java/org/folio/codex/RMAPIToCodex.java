package org.folio.codex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.folio.converter.hld2cdx.ContributorConverter;
import org.folio.converter.hld2cdx.IdentifierConverter;
import org.folio.converter.hld2cdx.SubjectConverter;
import org.folio.converter.hld2cdx.TitleConverter;
import org.folio.cql2rmapi.TitleParameters;
import org.folio.cql2rmapi.query.Page;
import org.folio.cql2rmapi.query.PaginationInfo;
import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.Title;
import org.folio.holdingsiq.model.Titles;
import org.folio.holdingsiq.service.impl.TitlesHoldingsIQServiceImpl;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.InstanceCollection;
import org.folio.rest.jaxrs.model.ResultInfo;
import org.springframework.core.convert.converter.Converter;

import io.vertx.core.Context;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

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

  public static CompletableFuture<InstanceCollection> getInstances(TitleParameters parameters, PaginationInfo pagination, Context vertxContext,
                                                                   Configuration rmAPIConfig) {
    log.info("Calling getInstances");

    final List<CompletableFuture<Titles>> titleCfs = new ArrayList<>();

    for (Page page : pagination.getPages()) {
      titleCfs.add(new TitlesHoldingsIQServiceImpl(rmAPIConfig.getCustomerId(), rmAPIConfig.getApiKey(),
        rmAPIConfig.getUrl(), vertxContext.owner()).retrieveTitles(parameters.getFilterQuery(), parameters.getSortType(), page.getOffset(), page.getLimit()));
    }

    return CompletableFuture
      .allOf(titleCfs.toArray(new CompletableFuture[0]))
      .thenCompose(aVoid -> {
        final List<Titles> collect = titleCfs.stream().map(CompletableFuture::join).collect(Collectors.toList());
        return convertRMTitleListToCodex(collect, pagination.getFirstObjectIndex(), pagination.getLimit());
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
