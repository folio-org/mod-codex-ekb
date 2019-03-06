package org.folio.codex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.folio.converter.hld2cdx.ContributorConverter;
import org.folio.converter.hld2cdx.CoverageConverter;
import org.folio.converter.hld2cdx.IdentifierConverter;
import org.folio.converter.hld2cdx.PackageConverter;
import org.folio.converter.hld2cdx.SubjectConverter;
import org.folio.converter.hld2cdx.TitleConverter;
import org.folio.cql2rmapi.PackageParameters;
import org.folio.cql2rmapi.TitleParameters;
import org.folio.cql2rmapi.query.Page;
import org.folio.cql2rmapi.query.PaginationInfo;
import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.PackageData;
import org.folio.holdingsiq.model.PackageId;
import org.folio.holdingsiq.model.Packages;
import org.folio.holdingsiq.model.Title;
import org.folio.holdingsiq.model.Titles;
import org.folio.holdingsiq.service.PackagesHoldingsIQService;
import org.folio.holdingsiq.service.TitlesHoldingsIQService;
import org.folio.holdingsiq.service.impl.PackagesHoldingsIQServiceImpl;
import org.folio.holdingsiq.service.impl.TitlesHoldingsIQServiceImpl;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.InstanceCollection;
import org.folio.rest.jaxrs.model.Package;
import org.folio.rest.jaxrs.model.PackageCollection;
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
  private static final Converter<PackageData, Package> PACKAGE_CONVERTER = new PackageConverter(new CoverageConverter());

  private RMAPIToCodex() {
    super();
  }

  public static CompletableFuture<Instance> getInstance(Context vertxContext, Configuration rmAPIConfig, long id) {
    log.info("Calling getInstance");

    TitlesHoldingsIQService titlesService = new TitlesHoldingsIQServiceImpl(rmAPIConfig, vertxContext.owner());

    return titlesService.retrieveTitle(id)
            .thenApply(TITLE_CONVERTER::convert);
  }

  public static CompletableFuture<Package> getPackage(Context vertxContext, Configuration rmAPIConfig, PackageId id) {
    log.info("Calling getPackage");

    PackagesHoldingsIQService service = new PackagesHoldingsIQServiceImpl(rmAPIConfig, vertxContext.owner());

    return service.retrievePackage(id)
            .thenApply(PACKAGE_CONVERTER::convert);
  }

  public static CompletableFuture<InstanceCollection> getInstances(TitleParameters parameters, PaginationInfo pagination, Context vertxContext,
                                                                   Configuration rmAPIConfig) {
    log.info("Calling getInstances");

    final List<CompletableFuture<Titles>> titleCfs = new ArrayList<>();

    for (Page page : pagination.getPages()) {
      titleCfs.add(new TitlesHoldingsIQServiceImpl(rmAPIConfig, vertxContext.owner()).retrieveTitles(parameters.getFilterQuery(), parameters.getSortType(), page.getOffset(), page.getLimit()));
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

    instances = getSublist(index, limit, instances);

    instanceCollection.setInstances(instances);
      instanceCollection.setResultInfo(new ResultInfo().withTotalRecords(totalResults));

      return CompletableFuture.completedFuture(instanceCollection);
  }

  public static CompletableFuture<PackageCollection> getPackages(PackageParameters parameters, PaginationInfo pagination,
                                                                  long providerId, Context vertxContext, Configuration rmAPIConfig) {
    log.info("Calling getPackages");

    final List<CompletableFuture<Packages>> futures = new ArrayList<>();

    for (Page page : pagination.getPages()) {
      futures.add(new PackagesHoldingsIQServiceImpl(rmAPIConfig, vertxContext.owner()).retrievePackages(
        parameters.getSelection(), parameters.getFilterType(),providerId,parameters.getSearchValue(), page.getOffset(), page.getLimit(), parameters.getSortType()));
    }

    return CompletableFuture
      .allOf(futures.toArray(new CompletableFuture[0]))
      .thenCompose(aVoid -> {
        final List<Packages> collect = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        return convertRMPackageListToCodex(collect, pagination.getFirstObjectIndex(), pagination.getLimit());
      });
  }

  private static CompletableFuture<PackageCollection> convertRMPackageListToCodex(List<Packages> packagesList, int index, int limit) {
    int totalResults = packagesList.stream()
      .mapToInt(Packages::getTotalResults)
      .sum();

    List<Package> packages = packagesList.stream()
      .flatMap(packagesObject -> packagesObject.getPackagesList().stream())
      .map(PACKAGE_CONVERTER::convert)
      .collect(Collectors.toList());

    final PackageCollection packageCollection = new PackageCollection()
      .withPackages(getSublist(index, limit, packages))
      .withResultInfo(new ResultInfo().withTotalRecords(totalResults));
    return CompletableFuture.completedFuture(packageCollection);
  }

  private static <T> List<T> getSublist(int firstIndex, int amount, List<T> list) {
    int start = Math.min(firstIndex, list.size());
    int end = Math.min(firstIndex + amount, list.size());
    list = list.subList(start, end);
    return list;
  }
}
