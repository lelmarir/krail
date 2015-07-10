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

package uk.q3c.krail.core.sysadmin.option

import spock.lang.Specification
import uk.q3c.krail.core.persist.OptionSource
import uk.q3c.krail.core.persist.PersistenceInfo
import uk.q3c.krail.core.user.opt.Option
import uk.q3c.krail.i18n.Translate

/**
 * Created by David Sowerby on 07/07/15.
 */


class ActiveOptionSourcePanelTest extends Specification {

    Translate translate = Mock()
    OptionSource optionSource = Mock()
    PersistenceInfo persistenceInfo = Mock()
    Option option = Mock()


    def panel = new ActiveOptionSourcePanel(translate, optionSource, option);


    def "doSetPersistenceInfo gets info from optionSource using active source"() {

        given:

        optionSource.getActivePersistenceInfo() >> persistenceInfo

        when:

        panel.doSetPersistenceInfo()

        then:

        panel.getPersistenceInfo() == persistenceInfo

    }


}