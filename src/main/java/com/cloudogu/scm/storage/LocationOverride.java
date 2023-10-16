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

import com.google.common.base.Strings;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationOverride;

import javax.inject.Inject;
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
