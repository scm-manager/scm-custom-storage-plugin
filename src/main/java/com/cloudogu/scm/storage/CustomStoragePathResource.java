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
import de.otto.edison.hal.Links;
import lombok.Data;
import lombok.NoArgsConstructor;
import sonia.scm.config.ConfigurationPermissions;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Path("v2/custom-storage-path")
public class CustomStoragePathResource {

  private final CustomStoragePathService service;
  private final StorageLinks links;

  @Inject
  CustomStoragePathResource(CustomStoragePathService service, StorageLinks links) {
    this.service = service;
    this.links = links;
  }

  @PUT
  @Path("")
  @Consumes(MediaType.APPLICATION_JSON)
  public void setCustomStoragePath(CustomStoragePathDto customStoragePathDto) {
    service.setCustomStoragePath(customStoragePathDto.getPath());
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  public CustomStoragePathDto getCustomStoragePath() {
    ConfigurationPermissions.custom("storage").check();
    return new CustomStoragePathDto(
      linkingTo()
        .single(link("update", links.getSetCustomStoragePathLink()))
        .self(links.getGetCustomStoragePathLink())
        .build(),
      service.getCustomStoragePath().orElse("")
    );
  }

  @Data
  @NoArgsConstructor
  static class CustomStoragePathDto extends HalRepresentation {
    private String path;

    public CustomStoragePathDto(Links links, String path) {
      super(links);
      this.path = path;
    }
  }
}
