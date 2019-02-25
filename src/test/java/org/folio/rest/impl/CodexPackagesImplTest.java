package org.folio.rest.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.folio.rest.jaxrs.model.Source;
import org.folio.rest.jaxrs.model.SourceCollection;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class CodexPackagesImplTest extends VertxTestBase {

  @Test
  public void getCodexPackagesSourcesSuccessTest() {
    logger.info("Testing for successful instance id");

    final SourceCollection response = RestAssured
      .given()
      .header(tenantHeader)
      .header(urlHeader)
      .header(contentTypeHeader)
      .get("/codex-packages-sources")
      .then()
      .contentType(ContentType.JSON)
      .log()
      .ifValidationFails()
      .statusCode(200).extract().as(SourceCollection.class);

    List<Source> sources = response.getSources();
    assertEquals(1, sources.size());
    assertTrue(sources.get(0).getName().matches("mod-codex-ekb-.*"));
    assertTrue(sources.get(0).getId().matches("kb"));

    logger.info("Test done");
  }

}
