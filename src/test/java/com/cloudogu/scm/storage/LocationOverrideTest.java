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

import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryByteConfigurationStoreFactory;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@ExtendWith(ShiroExtension.class)
@SubjectAware(value = "trillian", permissions = "configuration:storage")
class LocationOverrideTest {

  private final ConfigurationStoreFactory configurationStoreFactory = new InMemoryByteConfigurationStoreFactory();
  private final CustomStoragePathService service = new CustomStoragePathService(configurationStoreFactory);
  private final LocationOverride locationOverride = new LocationOverride(service);

  @Test
  void shouldOverridePathIfConfigured() {
    createStore()
      .set(new CustomStoragePathService.StorageDefault("/with/{type}/{namespace}/{name}"));

    Path newPath = locationOverride
      .overrideLocation(RepositoryTestData.createHeartOfGold("git"), Path.of("/somewhere/else"));

    assertThat(newPath).hasToString("/with/git/hitchhiker/HeartOfGold");
  }

  @Test
  void shouldNotOverridePathIfConfiguredEmpty() {
    createStore()
      .set(new CustomStoragePathService.StorageDefault(""));

    Path newPath = locationOverride
      .overrideLocation(RepositoryTestData.createHeartOfGold("git"), Path.of("/somewhere/else"));

    assertThat(newPath).hasToString("/somewhere/else");
  }

  @Test
  void shouldNotOverridePathIfNotConfigured() {
    Path newPath = locationOverride
      .overrideLocation(RepositoryTestData.createHeartOfGold("git"), Path.of("/somewhere/else"));

    assertThat(newPath).hasToString("/somewhere/else");
  }

  private ConfigurationStore<CustomStoragePathService.StorageDefault> createStore() {
    return configurationStoreFactory
      .withType(CustomStoragePathService.StorageDefault.class)
      .withName("custom-storage-path")
      .build();
  }
}
