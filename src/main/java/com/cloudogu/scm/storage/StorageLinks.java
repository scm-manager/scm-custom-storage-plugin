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

import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

class StorageLinks {

  private final Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Inject
  StorageLinks(Provider<ScmPathInfoStore> scmPathInfoStoreProvider) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
  }

  String getGetStorageOverviewLink() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStoreProvider.get().get(), StorageResource.class);
    return linkBuilder.method("getStorageOverview").parameters().href();
  }

  String getModifyStorageLocationLink(Repository repository) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStoreProvider.get().get(), StorageResource.class);
    return linkBuilder.method("modifyStorageLocation").parameters(repository.getNamespace(), repository.getName()).href();
  }

  String getGetCustomStoragePathLink() {
    LinkBuilder defaultLinkBuilder = new LinkBuilder(scmPathInfoStoreProvider.get().get(), CustomStoragePathResource.class);
    return defaultLinkBuilder.method("getCustomStoragePath").parameters().href();
  }

  String getSetCustomStoragePathLink() {
    LinkBuilder defaultLinkBuilder = new LinkBuilder(scmPathInfoStoreProvider.get().get(), CustomStoragePathResource.class);
    return defaultLinkBuilder.method("setCustomStoragePath").parameters().href();
  }
}
