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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import jakarta.inject.Inject;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Optional;

class CustomStoragePathService {

  private final ConfigurationStoreFactory storeFactory;

  @Inject
  public CustomStoragePathService(ConfigurationStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  void setCustomStoragePath(String path) {
    ConfigurationPermissions.custom("storage").check();
    createStore().set(new StorageDefault(path));
  }

  Optional<String> getCustomStoragePath() {
    return createStore().getOptional().map(StorageDefault::getCustomPath);
  }

  private ConfigurationStore<StorageDefault> createStore() {
    return storeFactory.withType(StorageDefault.class).withName("custom-storage-path").build();
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static class StorageDefault {
    String customPath;
  }
}
