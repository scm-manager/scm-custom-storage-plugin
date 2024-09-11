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

import React, { FC } from "react";
import { ErrorNotification, Level, Loading } from "@scm-manager/ui-components";
import { HalRepresentationWithEmbedded, Link, Repository } from "@scm-manager/ui-types";
import { apiClient } from "@scm-manager/ui-api";
import { Menu } from "@scm-manager/ui-overlays";
import ModifyDialog from "./ModifyDialog";
import { Icon } from "@scm-manager/ui-buttons";
import { useTranslation } from "react-i18next";
import { useQuery } from "react-query";

type Props = {
  repository: Repository;
};

type SingleRepositoryStorageType = HalRepresentationWithEmbedded<{ repository: Repository }> & {
  path: string;
};

const useStorageLocation = (repository: Repository) => {
  const link = (repository._links["repository-storage"] as Link).href;
  return useQuery<SingleRepositoryStorageType, Error>(["storageLocation", repository.namespace, repository.name], () =>
    apiClient.get(link).then(r => r.json())
  );
};

const SingleRepositoryStorage: FC<Props> = ({ repository }) => {
  const [t] = useTranslation("plugins");
  const { data: repositoryStorage, isLoading, error } = useStorageLocation(repository);
  if (isLoading) {
    return <Loading />;
  }
  if (error) {
    return <ErrorNotification error={error} />;
  }
  const path = repositoryStorage!.path;
  return (
    <Level
      left={
        <div>
          <h4 className="has-text-weight-bold">{t("scm-custom-storage-plugin.dangerZone.title")}</h4>
          <p>{t("scm-custom-storage-plugin.dangerZone.description")}</p>
        </div>
      }
      right={
        <Menu>
          <ModifyDialog repository={repository} path={path} keepOldLocation>
            <Icon>clone</Icon>
            {t("scm-custom-storage-plugin.moveDialog.copy")}
          </ModifyDialog>
          <ModifyDialog repository={repository} path={path}>
            <Icon>arrows-alt</Icon>
            {t("scm-custom-storage-plugin.moveDialog.move")}
          </ModifyDialog>
        </Menu>
      }
    />
  );
};

export default SingleRepositoryStorage;
