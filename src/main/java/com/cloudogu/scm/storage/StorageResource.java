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

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import lombok.Data;
import lombok.Getter;
import sonia.scm.ContextEntry;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.api.RepositoryToHalMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
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
