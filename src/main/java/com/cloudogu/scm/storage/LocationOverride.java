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

import com.google.common.base.Strings;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationOverride;

import jakarta.inject.Inject;
import java.nio.file.Path;

@Extension
@EagerSingleton
public class LocationOverride implements RepositoryLocationOverride {

  private final CustomStoragePathService service;

  @Inject
  public LocationOverride(CustomStoragePathService service) {
    this.service = service;
  }

  @Override
  public Path overrideLocation(Repository repository, Path defaultPath) {
    return service
      .getCustomStoragePath()
      .filter(p -> !Strings.isNullOrEmpty(p))
      .map(pattern -> createPath(repository, pattern))
      .orElse(defaultPath);
  }

  private static Path createPath(Repository repository, String pattern) {
    return Path.of(
      pattern
        .replace("{namespace}", repository.getNamespace())
        .replace("{name}", repository.getName())
        .replace("{type}", repository.getType())
        .replace("{id}", repository.getId())
    );
  }
}
