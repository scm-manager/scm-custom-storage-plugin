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
import de.otto.edison.hal.Links;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
