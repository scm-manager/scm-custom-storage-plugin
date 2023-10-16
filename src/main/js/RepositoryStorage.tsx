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

import { ErrorNotification, Loading, Subtitle, Title } from "@scm-manager/ui-components";
import React, { FC } from "react";
import { useQuery } from "react-query";
import { apiClient, useIndex } from "@scm-manager/ui-api";
import { HalRepresentationWithEmbedded, Link as HalLink, Repository } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { Icon } from "@scm-manager/ui-buttons";
import { Card, CardList, CardListBox } from "@scm-manager/ui-layout";
import ModifyDialog from "./ModifyDialog";
import { Menu } from "@scm-manager/ui-overlays";
import CustomPath from "./CustomPath";
import { Link } from "react-router-dom";

type SingleRepositoryStorage = HalRepresentationWithEmbedded<{ repository: Repository }> & {
  path: string;
};

type RepositoryStorageOverview = {
  storages: SingleRepositoryStorage[];
};

const useStorageLocations = (link: string) =>
  useQuery<RepositoryStorageOverview, Error>("storageLocations", () => apiClient.get(link).then(r => r.json()));

const RepositoryStorageEntry: FC<{ path: string; repository: Repository }> = ({ path, repository }) => {
  const [t] = useTranslation("plugins");

  const menu = (
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
  );

  return (
    <CardList.Card rowGap="0.5rem" action={menu}>
      <Card.Title>
        <Link to={`/repo/${repository.namespace}/${repository.name}/info`}>
          {`${repository.namespace}/${repository.name}`}
        </Link>
      </Card.Title>
      <Card.Details>
        <div className="is-flex has-gap-2 is-overflow-hidden is-full-width is-align-items-center">
          <pre className="is-flex-shrink-1 is-flex-grow-1 is-overflow-auto is-relative">{path}</pre>
        </div>
      </Card.Details>
    </CardList.Card>
  );
};

const RepositoryStorageList: FC<{ link: string }> = ({ link }) => {
  const { data, isLoading, error } = useStorageLocations(link);
  if (isLoading) {
    return <Loading />;
  }
  if (error) {
    return <ErrorNotification error={error} />;
  }
  return (
    <CardListBox>
      {data!.storages.map(storage => (
        <RepositoryStorageEntry key={storage.path} path={storage.path} repository={storage._embedded!.repository} />
      ))}
    </CardListBox>
  );
};

const RepositoryStorage: FC<{ link: string }> = ({ link }) => {
  const [t] = useTranslation("plugins");
  const { data: index, isLoading, error } = useIndex();

  if (isLoading) {
    return <Loading />;
  }
  if (error) {
    return <ErrorNotification error={error} />;
  }

  return (
    <>
      <Title>{t("scm-custom-storage-plugin.title")}</Title>
      <CustomPath link={(index!._links["custom-storage-path"] as HalLink).href} />
      <hr />
      <Subtitle>{t("scm-custom-storage-plugin.overview.title")}</Subtitle>
      {t("scm-custom-storage-plugin.overview.introduction")}
      <RepositoryStorageList link={link} />
    </>
  );
};

export default RepositoryStorage;
