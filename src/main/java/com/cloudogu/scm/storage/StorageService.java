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

import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.notifications.Notification;
import sonia.scm.notifications.NotificationSender;
import sonia.scm.notifications.Type;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;

import javax.inject.Inject;
import java.nio.file.Path;

class StorageService {

  private final RepositoryLocationResolver locationResolver;
  private final NotificationSender notificationSender;

  @Inject
  StorageService(RepositoryLocationResolver locationResolver, NotificationSender notificationSender) {
    this.locationResolver = locationResolver;
    this.notificationSender = notificationSender;
  }

  String getStorageLocation(Repository repository) {
    ConfigurationPermissions.custom("storage").check();
    return locationResolver.forClass(Path.class).getLocation(repository.getId()).toAbsolutePath().toString();
  }

  void modifyStorageLocation(Repository repository, String newPath, boolean keepOldLocation) {
    ConfigurationPermissions.custom("storage").check();
    String repositoryId = repository.getId();
    try {
      if (keepOldLocation) {
        locationResolver.forClass(Path.class).modifyLocationAndKeepOld(repositoryId, Path.of(newPath));
        notificationSender.send(new Notification(Type.SUCCESS, null, "copySuccess"));
      } else {
        locationResolver.forClass(Path.class).modifyLocation(repositoryId, Path.of(newPath));
        notificationSender.send(new Notification(Type.SUCCESS, null, "moveSuccess"));
      }
    } catch (RepositoryLocationResolver.RepositoryStorageException e) {
      notificationSender.send(new Notification(Type.ERROR, null, "modifyError"));
      throw e;
    }
  }
}
