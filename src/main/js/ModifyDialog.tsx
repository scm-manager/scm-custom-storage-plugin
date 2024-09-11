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

import React, { FC, useState } from "react";
import { ErrorNotification, InputField, Notification } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { Link, Repository } from "@scm-manager/ui-types";
import { useMutation, useQueryClient } from "react-query";
import { apiClient } from "@scm-manager/ui-api";
import { Button, Icon } from "@scm-manager/ui-buttons";
import { Dialog, Menu } from "@scm-manager/ui-overlays";

type Props = {
  repository: Repository;
  path: string;
  keepOldLocation?: boolean;
};

type RepositoryStorageModification = {
  newPath: string;
  keepOldLocation?: boolean;
  close: () => void;
};

const useModifyStorageLocation = (repository: Repository) => {
  const queryClient = useQueryClient();
  const link = (repository._links["repository-storage"] as Link).href;
  const { mutate, isLoading, error } = useMutation<unknown, Error, RepositoryStorageModification>({
    mutationFn: ({ newPath, keepOldLocation }) => apiClient.post(link, { newPath, keepOldLocation }),
    onSuccess: (_, { close }) => {
      queryClient.invalidateQueries(["storageLocations"]);
      queryClient.invalidateQueries(["storageLocation", repository.namespace, repository.name]);
      close();
    }
  });
  return { mutate, isLoading, error };
};

const ModifyDialog: FC<Props> = ({ repository, path, keepOldLocation, children }) => {
  const { mutate, isLoading, error } = useModifyStorageLocation(repository);
  const [t] = useTranslation("plugins");
  const [newPath, setNewPath] = useState(path);

  const move = (close: () => void) => {
    mutate({ newPath, keepOldLocation, close });
  };

  const body = (
    <>
      {!keepOldLocation && (
        <Notification type="danger" className="my-4">
          {t("scm-custom-storage-plugin.moveDialog.warning")}
        </Notification>
      )}
      {error && <ErrorNotification error={error} />}
      <InputField
        value={newPath}
        onChange={setNewPath}
        label={t("scm-custom-storage-plugin.moveDialog.newPath.label")}
        helpText={t("scm-custom-storage-plugin.moveDialog.newPath.help")}
      />
    </>
  );

  return (
    <Menu.DialogButton
      title={t("scm-custom-storage-plugin.moveDialog.title")}
      description={t(`scm-custom-storage-plugin.moveDialog.notification.${keepOldLocation ? "copy" : "move"}`)}
      dialogContent={body}
      footer={close => [
        <Button onClick={() => move(close)} variant="signal" disabled={isLoading || path === newPath}>
          <Icon>exclamation-triangle</Icon>
          <span>{t(`scm-custom-storage-plugin.moveDialog.${keepOldLocation ? "copy" : "move"}`)}</span>
        </Button>,
        <Dialog.CloseButton autoFocus>{t("scm-custom-storage-plugin.moveDialog.cancel")}</Dialog.CloseButton>
      ]}
    >
      {children}
    </Menu.DialogButton>
  );
};

export default ModifyDialog;
