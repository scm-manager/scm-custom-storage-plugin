/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cloudogu.scm.storage;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import lombok.Data;
import lombok.Getter;
import sonia.scm.ContextEntry;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.api.RepositoryToHalMapper;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Links.linkingTo;
import static sonia.scm.NotFoundException.notFound;

@Path("v2/repository-storage")
public class StorageResource {

  private final RepositoryManager repositoryManager;
  private final RepositoryToHalMapper repositoryToHalMapper;

  private final StorageService storageService;
  private final StorageLinks links;

  @Inject
  StorageResource(RepositoryManager repositoryManager, RepositoryToHalMapper repositoryToHalMapper, StorageService storageService, StorageLinks links) {
    this.repositoryManager = repositoryManager;
    this.repositoryToHalMapper = repositoryToHalMapper;
    this.storageService = storageService;
    this.links = links;
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  public RepositoryStorageOverviewDto getStorageOverview() {
    return new RepositoryStorageOverviewDto(
      repositoryManager
        .getAll()
        .stream()
        .map(RepositoryStorageDto::new)
        .collect(Collectors.toList())
    );
  }

  @GET
  @Path("{namespace}/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public RepositoryStorageDto getStorageLocation(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    Repository repository = repositoryManager.get(namespaceAndName);
    if (repository == null) {
      throw notFound(ContextEntry.ContextBuilder.entity(namespaceAndName));
    }
    return new RepositoryStorageDto(repository);
  }

  @POST
  @Path("{namespace}/{name}")
  public void modifyStorageLocation(@PathParam("namespace") String namespace, @PathParam("name") String name, RepositoryStorageModificationDto changeStorageLocation) throws IOException {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    Repository repository = repositoryManager.get(namespaceAndName);
    if (repository == null) {
      throw notFound(ContextEntry.ContextBuilder.entity(namespaceAndName));
    }
    storageService.modifyStorageLocation(repository, changeStorageLocation.getNewPath(), changeStorageLocation.isKeepOldLocation());
  }

  @Getter
  static class RepositoryStorageOverviewDto {

    private final Collection<RepositoryStorageDto> storages;

    public RepositoryStorageOverviewDto(Collection<RepositoryStorageDto> storages) {
      this.storages = storages;
    }
  }

  class RepositoryStorageDto extends HalRepresentation {

    private final String path;

    RepositoryStorageDto(Repository repository) {
      super(
        linkingTo().single(Link.link("update", links.getModifyStorageLocationLink(repository))).build(),
        embeddedBuilder().with("repository", repositoryToHalMapper.map(repository)).build()
      );
      this.path = storageService.getStorageLocation(repository);
    }

    public String getPath() {
      return path;
    }
  }

  @Data
  static class RepositoryStorageModificationDto {
    private String newPath;
    private boolean keepOldLocation = false;
  }
}
