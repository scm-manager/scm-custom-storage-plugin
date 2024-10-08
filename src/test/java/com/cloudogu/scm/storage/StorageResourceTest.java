/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.scm.storage;

import com.fasterxml.jackson.databind.JsonNode;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.JsonMockHttpRequest;
import sonia.scm.web.JsonMockHttpResponse;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.api.RepositoryToHalMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageResourceTest {

  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private RepositoryToHalMapper repositoryToHalMapper;
  @Mock
  private StorageService storageService;
  @Mock
  private StorageLinks storageLinks;

  @InjectMocks
  private StorageResource resource;

  private final RestDispatcher dispatcher = new RestDispatcher();
  private final JsonMockHttpResponse response = new JsonMockHttpResponse();

  @BeforeEach
  void initDispatcher() {
    dispatcher.addSingletonResource(resource);
  }

  @Test
  void shouldGetStorageOverview() throws URISyntaxException, UnsupportedEncodingException {
    when(repositoryToHalMapper.map(any()))
      .thenAnswer(invocation -> new HalRepresentation(Links.linkingTo().self(extractNamespaceAndName(invocation)).build()));
    when(storageService.getStorageLocation(any()))
      .thenAnswer(this::extractNamespaceAndName);
    when(repositoryManager.getAll())
      .thenReturn(asList(
        RepositoryTestData.createHeartOfGold(),
        RepositoryTestData.create42Puzzle()
      ));
    when(storageLinks.getModifyStorageLocationLink(any())).thenReturn("http://hitchhiker/");

    MockHttpRequest request = MockHttpRequest.get("/v2/repository-storage");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    System.out.println(response.getContentAsString());
    JsonNode jsonResponse = response.getContentAsJson();
    assertThat(jsonResponse.at("/storages/0/path").textValue())
      .isEqualTo("hitchhiker/HeartOfGold");
    assertThat(jsonResponse.at("/storages/1/path").textValue())
      .isEqualTo("hitchhiker/42Puzzle");
    assertThat(jsonResponse.at("/storages/0/_embedded/repository/_links/self/href").textValue())
      .isEqualTo("hitchhiker/HeartOfGold");
    assertThat(jsonResponse.at("/storages/1/_embedded/repository/_links/self/href").textValue())
      .isEqualTo("hitchhiker/42Puzzle");
  }

  @Test
  void shouldReturnNotFoundErrorForUnknownRepository() throws URISyntaxException, IOException {

    JsonMockHttpRequest request = JsonMockHttpRequest
      .post("/v2/repository-storage/unknown/repository")
      .json("{'newPath': '/tmp/somewhere'}");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(404);
    verify(storageService, never()).modifyStorageLocation(any(), any(), anyBoolean());
  }

  @Nested
  class ForSingleRepository {

    private final Repository repository = RepositoryTestData.createHeartOfGold();

    @BeforeEach
    void mockRepository() {
      when(repositoryManager.get(new NamespaceAndName("hitchhiker", "HeartOfGold")))
        .thenReturn(repository);
    }

    @Test
    void shouldModifyStorageLocationForExistingRepositoryWithMoveAsDefault() throws URISyntaxException, IOException {
      JsonMockHttpRequest request = JsonMockHttpRequest
        .post("/v2/repository-storage/hitchhiker/HeartOfGold")
        .json("{'newPath': '/tmp/somewhere'}");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(204);
      verify(storageService).modifyStorageLocation(repository, "/tmp/somewhere", false);
    }

    @Test
    void shouldModifyStorageLocationForExistingRepository() throws URISyntaxException, IOException {
      JsonMockHttpRequest request = JsonMockHttpRequest
        .post("/v2/repository-storage/hitchhiker/HeartOfGold")
        .json("{'newPath': '/tmp/somewhere', 'keepOldLocation': true}");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(204);
      verify(storageService).modifyStorageLocation(repository, "/tmp/somewhere", true);
    }
  }

  private String extractNamespaceAndName(InvocationOnMock invocation) {
    return invocation.getArgument(0, Repository.class).getNamespaceAndName().toString();
  }
}
