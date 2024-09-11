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
