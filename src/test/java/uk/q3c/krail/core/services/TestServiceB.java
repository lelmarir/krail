/*
 * Copyright (c) 2015. David Sowerby
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package uk.q3c.krail.core.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import uk.q3c.krail.core.eventbus.GlobalBusProvider;
import uk.q3c.krail.i18n.I18NKey;
import uk.q3c.krail.i18n.TestLabelKey;
import uk.q3c.krail.i18n.Translate;

public class TestServiceB extends AbstractService {

    private TestServiceC serviceC;

    @Inject
    protected TestServiceB(Translate translate, ServicesModel servicesModel,
                           GlobalBusProvider globalBusProvider) {
        super(translate, servicesModel, globalBusProvider);
    }

    @Override
    public I18NKey getNameKey() {
        return TestLabelKey.ServiceB;
    }

    @Override
    protected void doStop() throws Exception {

    }

    @Override
    protected void doStart() throws Exception {

    }
}
