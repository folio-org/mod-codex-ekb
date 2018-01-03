package org.folio.codex;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.folio.config.RMAPIConfiguration;
import org.folio.cql2rmapi.CQLParserForRMAPI;
import org.folio.rest.jaxrs.model.Contributor;
import org.folio.rest.jaxrs.model.Identifier;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.InstanceCollection;
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

    return convertRMTitleListToCodex(titleCfs, cql.getInstanceIndex());
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

    return codexInstance;
  }

  private static Identifier convertRMIdentifierToCodex(org.folio.rmapi.model.Identifier rmIdentifier) {
    final Type type = Type.valueOf(rmIdentifier.type);
    final SubType subType = SubType.valueOf(rmIdentifier.subtype);

    final Identifier codexIdentifier;
    if (type != Type.UNKNOWN) {
      codexIdentifier = new Identifier();
      String codexType = type.getDisplayName();

      if (subType != SubType.UNKNOWN) {
        codexType += '(' + subType.getDisplayName() + ')';
      }

      codexIdentifier.setType(codexType);
      codexIdentifier.setValue(rmIdentifier.id);
    } else {
      codexIdentifier = null;
    }

    return codexIdentifier;
  }

  private static Contributor convertRMContributorToCodex(org.folio.rmapi.model.Contributor rmContributor) {
    final Contributor codexContributor = new Contributor();
    codexContributor.setName(rmContributor.titleContributor);
    codexContributor.setType(rmContributor.type);
    return codexContributor;
  }

  private static CompletableFuture<InstanceCollection> convertRMTitleListToCodex(List<CompletableFuture<Titles>> titleCfs, int index) {
    return CompletableFuture.allOf(titleCfs.toArray(new CompletableFuture[titleCfs.size()]))
    .thenApply(result -> {
      final InstanceCollection instanceCollection = new InstanceCollection();
      List<Instance> instances = new ArrayList<>();
      int totalResults = 0;
      int page = 0;

      for (CompletableFuture<Titles> titleCf : titleCfs) {
        final Titles titles = titleCf.join();
        totalResults = Math.max(totalResults, titles.totalResults);
        page = Math.max(page, titles.titleList.size());
        instances.addAll(titles.titleList.stream()
            .map(RMAPIToCodex::convertRMAPITitleToCodex)
            .collect(Collectors.toList()));
      }

      if (index > 0) {
        if (instances.size() >= index) {
          int end = Math.min(instances.size(), (page * (titleCfs.size() - 1)) + index);
          instances = instances.subList(index, end);
        } else {
          instances.clear();
        }
      }

      instanceCollection.setInstances(instances);
      instanceCollection.setTotalRecords(totalResults);

      return instanceCollection;
    });
  }
}
