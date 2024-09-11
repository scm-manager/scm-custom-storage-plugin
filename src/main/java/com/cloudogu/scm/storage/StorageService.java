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

import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.notifications.Notification;
import sonia.scm.notifications.NotificationSender;
import sonia.scm.notifications.Type;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;

import jakarta.inject.Inject;
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
