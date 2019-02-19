package org.folio.codex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.folio.config.RMAPIConfiguration;
import org.folio.cql2rmapi.CQLParserForRMAPI;
import org.folio.rest.jaxrs.model.Contributor;
import org.folio.rest.jaxrs.model.Identifier;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.InstanceCollection;
import org.folio.rest.jaxrs.model.ResultInfo;
import org.folio.rest.jaxrs.model.Subject;
import org.folio.rmapi.RMAPIResourceNotFoundException;
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
        .thenApply(RMAPIToCodex::convertRMAPITitleToCodex);
  }

  public static CompletableFuture<InstanceCollection> getInstances(CQLParserForRMAPI cql, Context vertxContext,
      RMAPIConfiguration rmAPIConfig) {
    log.info("Calling getInstances");

    final List<CompletableFuture<Titles>> titleCfs = new ArrayList<>();

    // We need to create a new RMAPIService for each call so that we don't close
    // the HTTP client connection.
    for (String query : cql.getRMAPIQueries()) {
      titleCfs.add(new RMAPIService(rmAPIConfig.getCustomerId(), rmAPIConfig.getAPIKey(),
          rmAPIConfig.getUrl(), vertxContext.owner()).getTitleList(query));
    }

    return convertRMTitleListToCodex(titleCfs, cql.getInstanceIndex(), cql.getInstanceLimit());
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

  /**
   * Converts RMAPI Title to Codex instance
   *
   * @param svcTitle
   * @return
   */
  private static Instance convertRMAPITitleToCodex(final Title svcTitle) {
    final Instance codexInstance = new Instance();

    codexInstance.setId(Integer.toString(svcTitle.titleId));
    codexInstance.setTitle(svcTitle.titleName);
    codexInstance.setPublisher(svcTitle.publisherName);
    codexInstance.setType(PubType.fromRMAPI(svcTitle.pubType).getCodex());
    codexInstance.setFormat(E_RESOURCE_FORMAT);
    codexInstance.setSource(E_RESOURCE_SOURCE);
    codexInstance.setVersion(svcTitle.edition);

    if ((svcTitle.identifiersList != null) && (!svcTitle.identifiersList.isEmpty())) {
      final Set<Identifier> identifiers = svcTitle.identifiersList.stream()
          .map(RMAPIToCodex::convertRMIdentifierToCodex)
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());

      if (!identifiers.isEmpty()) {
        codexInstance.setIdentifier(identifiers);
      }
    }

    if ((svcTitle.contributorsList != null) && (!svcTitle.contributorsList.isEmpty())) {
      codexInstance.setContributor(svcTitle.contributorsList.stream()
          .map(RMAPIToCodex::convertRMContributorToCodex)
          .collect(Collectors.toSet()));
    }

    if ((svcTitle.subjectsList != null) && (!svcTitle.subjectsList.isEmpty())) {
        codexInstance.setSubject(svcTitle.subjectsList.stream()
            .map(RMAPIToCodex::convertRMSubjectToCodex)
            .collect(Collectors.toSet()));
      }

    return codexInstance;
  }

  private static Identifier convertRMIdentifierToCodex(org.folio.rmapi.model.Identifier rmIdentifier) {
    final Type type = Type.valueOf(rmIdentifier.type);
    final SubType subType = SubType.valueOf(rmIdentifier.subtype);

    final Identifier codexIdentifier;
    if (type != Type.UNKNOWN) {
      String codexType = type.getDisplayName();

      if (subType != SubType.UNKNOWN) {
        codexType += '(' + subType.getDisplayName() + ')';
      }

      codexIdentifier = new Identifier()
          .withType(codexType)
          .withValue(rmIdentifier.id);
    } else {
      codexIdentifier = null;
    }

    return codexIdentifier;
  }

  private static Contributor convertRMContributorToCodex(org.folio.rmapi.model.Contributor rmContributor) {
    return new Contributor()
        .withName(rmContributor.titleContributor)
        .withType(rmContributor.type);
  }

  private static Subject convertRMSubjectToCodex(org.folio.rmapi.model.Subject rmSubject) {
    return new Subject()
        .withName(rmSubject.titleSubject)
        .withType(rmSubject.type);
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
            .map(RMAPIToCodex::convertRMAPITitleToCodex)
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
