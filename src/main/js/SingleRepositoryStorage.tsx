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
