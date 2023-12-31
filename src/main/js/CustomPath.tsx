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
