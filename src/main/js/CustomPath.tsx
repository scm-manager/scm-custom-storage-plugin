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
import { Subtitle } from "@scm-manager/ui-components";
import { ConfigurationForm, Form } from "@scm-manager/ui-forms";
import { HalRepresentation } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";

type RepositoryStorageDefault = HalRepresentation & {
  path: string;
};

const CustomPath: FC<{ link: string }> = ({ link }) => {
  const [t] = useTranslation("plugins");
  return (
    <>
      <Subtitle>{t("scm-custom-storage-plugin.override.title")}</Subtitle>
      <div className="content">
        {t("scm-custom-storage-plugin.override.description1")}
        <dl>
          <dt>{"{namespace}"}</dt>
          <dd>{t("scm-custom-storage-plugin.override.template.namespace")}</dd>
          <dt>{"{name}"}</dt>
          <dd>{t("scm-custom-storage-plugin.override.template.name")}</dd>
          <dt>{"{type}"}</dt>
          <dd>{t("scm-custom-storage-plugin.override.template.type")}</dd>
          <dt>{"{id}"}</dt>
          <dd>{t("scm-custom-storage-plugin.override.template.id")}</dd>
        </dl>
        {t("scm-custom-storage-plugin.override.description2")}
      </div>
      <ConfigurationForm<RepositoryStorageDefault>
        link={link}
        translationPath={["plugins", "scm-custom-storage-plugin.override"]}
      >
        <Form.Row>
          <Form.Input name="path" label={t("scm-custom-storage-plugin.override.label")} />
        </Form.Row>
      </ConfigurationForm>
    </>
  );
};

export default CustomPath;
