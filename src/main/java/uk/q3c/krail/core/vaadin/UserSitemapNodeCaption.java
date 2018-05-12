/*
 * Copyright (C) 2014 David Sowerby
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.q3c.krail.core.vaadin;

import uk.q3c.krail.core.navigate.sitemap.UserSitemapNode;
import uk.q3c.util.forest.CaptionReader;

import java.io.Serializable;

public class UserSitemapNodeCaption implements CaptionReader<UserSitemapNode>, Serializable {

    @Override
    public String getCaption(UserSitemapNode sourceNode) {
        return sourceNode.getLabel();
    }

}
