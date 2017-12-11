package org.folio.codex;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.folio.config.RMAPIConfiguration;
import org.folio.cql2rmapi.CQLParserForRMAPI;
import org.folio.rest.jaxrs.model.Contributor;
import org.folio.rest.jaxrs.model.Identifier;
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
  private static final String ISSN_TYPE = "ISSN";
  private static final String ISBN_TYPE = "ISBN";
  private static final String ZDBID_TYPE = "ZDBID";
  private static final String PRINT_SUBTYPE = "Print";
  private static final String ONLINE_SUBTYPE = "Online";

  private RMAPIToCodex() {
    super();
  }

  public static CompletableFuture<Instance> getInstance(String id, Context vertxContext,
      RMAPIConfiguration rmAPIConfig) {
    log.info("Calling getInstance");

    final RMAPIService rmAPIService = new RMAPIService(rmAPIConfig.getCustomerId(), rmAPIConfig.getAPIKey(),
        rmAPIConfig.getUrl(), vertxContext.owner());

    final CompletableFuture<Instance> cf = new CompletableFuture<>();

    rmAPIService.getTileById(id).whenComplete((rmapiResult, throwable) -> {
      if (throwable != null) {
        cf.completeExceptionally(throwable);
      } else {
        try {
          final Instance codexInstance = convertRMAPITitleToCodex(rmapiResult);
          cf.complete(codexInstance);
        } catch (final Exception ex) {
          cf.completeExceptionally(ex);
        }
      }
    });

    return cf;
  }

  public static CompletableFuture<InstanceCollection> getInstances(CQLParserForRMAPI cql, Context vertxContext,
      RMAPIConfiguration rmAPIConfig) {
    log.info("Calling getInstances");

    final RMAPIService rmAPIService = new RMAPIService(rmAPIConfig.getCustomerId(), rmAPIConfig.getAPIKey(),
        rmAPIConfig.getUrl(), vertxContext.owner());
    final List<String> queries = cql.getRMAPIQueries();
    final String query = queries.get(0); //Loop over the queries instead of getting the first one
    final CompletableFuture<InstanceCollection> cf = new CompletableFuture<>();

    rmAPIService.getTitleList(query).whenComplete((rmapiResult, throwable) -> {
      if (throwable != null) {
        cf.completeExceptionally(throwable);
      } else {
        try {
          final InstanceCollection codexInstances = convertRMTitleListToCodex(rmapiResult);
          cf.complete(codexInstances);
        } catch (final Exception ex) {
          cf.completeExceptionally(ex);
        }
      }
    });
    return cf;
  }

  /**
   * Converts RMAPI Title to Codex instance
   *
   * @param svcTitle
   * @return
   */
  private static Instance convertRMAPITitleToCodex(Title svcTitle) {

    final Instance codexInstance = new Instance();

    codexInstance.setId(Integer.toString(svcTitle.titleId));
    codexInstance.setTitle(svcTitle.titleName);
    codexInstance.setPublisher(svcTitle.publisherName);
    codexInstance.setType(svcTitle.pubType);
    codexInstance.setFormat(E_RESOURCE_FORMAT);
    codexInstance.setSource(E_RESOURCE_SOURCE);
    codexInstance.setVersion(svcTitle.edition);

    if ((svcTitle.identifiersList != null) && (!svcTitle.identifiersList.isEmpty())) {
      final Set<Identifier> identifiers = new HashSet<>();
      svcTitle.identifiersList.forEach(id -> {
        final Identifier codexId = convertRMIdentifierToCodex(id);
        if (codexId != null) {
          identifiers.add(codexId);
        }
      });
      if (!identifiers.isEmpty()) {
        codexInstance.setIdentifier(identifiers);
      }
    }

    if ((svcTitle.contributorsList != null) && (!svcTitle.contributorsList.isEmpty())) {
      final Set<Contributor> contributors = new HashSet<>();
      svcTitle.contributorsList.forEach(contributor -> contributors.add(convertRMContributorToCodex(contributor)));
      codexInstance.setContributor(contributors);
    }
    return codexInstance;
  }

  private static Identifier convertRMIdentifierToCodex(org.folio.rmapi.model.Identifier rmIdentifier) {
    final Identifier codexIdentifier = new Identifier();
    final String type = getDisplayType(rmIdentifier.type);
    final String subType = getDisplaySubType(rmIdentifier.subtype);

    String codexType = null;
    if (!type.isEmpty()) {
      if (!subType.isEmpty()) {
        codexType = String.format("%s(%s)", type, subType);
      } else {
        codexType = type;
      }
      codexIdentifier.setType(codexType);
      codexIdentifier.setValue(rmIdentifier.id);
      return codexIdentifier;
    }
    return null;
  }

  private static Contributor convertRMContributorToCodex(org.folio.rmapi.model.Contributor rmContributor) {
    final Contributor codexContributor = new Contributor();
    codexContributor.setName(rmContributor.titleContributor);
    codexContributor.setType(rmContributor.type);
    return codexContributor;
  }

  private static String getDisplayType(Integer type) {

    switch (type) {
    case 0:
      return ISSN_TYPE;
    case 1:
      return ISBN_TYPE;
    case 6:
      return ZDBID_TYPE;
    default:
      return "";
    }
  }

  private static String getDisplaySubType(Integer subtype) {
    switch (subtype) {
    case 1:
      return PRINT_SUBTYPE;
    case 2:
      return ONLINE_SUBTYPE;
    default:
      return "";
    }
  }

  private static InstanceCollection convertRMTitleListToCodex(Titles rmTitles) {

    final InstanceCollection instances = new InstanceCollection();
    final List<Instance> codexInstances = rmTitles.titleList.stream().map(RMAPIToCodex::convertRMAPITitleToCodex)
        .collect(Collectors.toList());
    instances.setInstances(codexInstances);
    instances.setTotalRecords(rmTitles.totalResults);
    return instances;
  }

}
