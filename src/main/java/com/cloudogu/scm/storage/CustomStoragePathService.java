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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
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
