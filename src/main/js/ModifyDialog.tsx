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
