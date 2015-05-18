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

package uk.q3c.krail.core.user.opt;

import com.google.common.cache.CacheStats;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mycila.testing.junit.MycilaJunitRunner;
import com.mycila.testing.plugin.guice.GuiceContext;
import com.mycila.testing.plugin.guice.ModuleProvider;
import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import uk.q3c.krail.core.shiro.SubjectIdentifier;
import uk.q3c.krail.core.shiro.SubjectProvider;
import uk.q3c.krail.core.user.opt.cache.DefaultOptionCache;
import uk.q3c.krail.core.user.opt.cache.DefaultOptionCacheLoader;
import uk.q3c.krail.core.user.opt.cache.DefaultOptionCacheProvider;
import uk.q3c.krail.core.user.opt.cache.OptionCacheConfig;
import uk.q3c.krail.core.user.profile.SimpleUserHierarchy;
import uk.q3c.krail.core.user.profile.UserHierarchy;
import uk.q3c.krail.core.view.component.LocaleContainer;
import uk.q3c.krail.i18n.DefaultCurrentLocale;
import uk.q3c.krail.i18n.Translate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MycilaJunitRunner.class)
@GuiceContext({})
public class Option_IntegrationTest {



    DefaultOptionCacheLoader cacheLoader;
    DefaultOption option;

    DefaultOptionCache optionCache;

    @Inject
    DefaultOptionCacheProvider cacheProvider;

    @Inject
    InMemoryOptionStore optionStore;

    @Mock
    Provider<OptionDao> daoProvider;

    InMemoryOptionDao dao;

    @Mock
    LocaleContainer localeContainer;
    List<String> allLayers;
    @Mock
    Subject subject1;

    @Mock
    Subject subject2;
    @Mock
    Translate translate;
    OptionKey key1 = LocaleContainer.optionKeyFlagSize;
    OptionKey key2 = DefaultCurrentLocale.optionPreferredLocale;
    private Class<? extends OptionContext> contextClass = LocaleContainer.class;
    private UserHierarchy hierarchy;
    private ArrayList<String> singleLayer;
    @Mock
    private SubjectIdentifier subjectIdentifier;
    @Mock
    private SubjectProvider subjectProvider;

    @Before
    public void setup() {
        dao = new InMemoryOptionDao(optionStore);
        when(daoProvider.get()).thenReturn(dao);
        when(subjectIdentifier.userId()).thenReturn("fbaton");
        when(subjectProvider.get()).thenReturn(subject1);
        hierarchy = new SimpleUserHierarchy(subjectProvider, subjectIdentifier, translate);

        cacheLoader = new DefaultOptionCacheLoader(daoProvider);
        optionCache = new DefaultOptionCache(daoProvider, cacheProvider);
        option = new DefaultOption(optionCache, hierarchy);
    }



    @Test
    public void putAndGet_user_authenticated() {
        //given
        when(subject1.isAuthenticated()).thenReturn(true);

        //when
        option.set(3, key1);
        //then
        assertThat(option.get(5, key1)).isEqualTo(3);
    }

    @Test
    public void putAndGet_user_not_authenticated() {
        //given
        when(subject1.isAuthenticated()).thenReturn(false);

        //when
        option.set(3, key1);
        //then
        assertThat(option.get(5, key1)).isEqualTo(3);
    }

    @Test
    public void put_and_get_override() {
        //given
        when(subject1.isAuthenticated()).thenReturn(true);
        //when
        option.set(3, key1);
        option.set(7, hierarchy, 1, key1);
        //then
        assertThat(option.get(5, key1)).isEqualTo(3);
        assertThat(option.get(Optional.of(5), key1)).isEqualTo(3);
    }


    @Test
    public void defaultValue_cache_empty_persistence_empty() {
        //given
        when(subject1.isAuthenticated()).thenReturn(true);
        //when

        //then
        assertThat(option.get(5, key1)).isEqualTo(5);
    }

    @Test
    public void defaultValue2() {
        //given
        when(subject1.isAuthenticated()).thenReturn(true);
        //when
        option.set(3, key2);
        //then
        assertThat(option.get(5, key1)).isEqualTo(5);
    }

    @Test
    public void stats() {
        //given

        //when
        CacheStats actual = optionCache.stats();
        //then
        assertThat(actual).isNotNull();
        assertThat(actual).isInstanceOf(CacheStats.class);
    }



    @Test
    public void multiUser() {
        //given
        when(subjectProvider.get()).thenReturn(subject1);
        when(subject1.isAuthenticated()).thenReturn(true);
        when(subjectIdentifier.userId()).thenReturn("fbaton");
        DefaultOption option2 = new DefaultOption(optionCache, hierarchy);
        //when
        option2.set(3, key1);
        when(subjectProvider.get()).thenReturn(subject2);
        when(subject2.isAuthenticated()).thenReturn(true);
        when(subjectIdentifier.userId()).thenReturn("equick");
        hierarchy = new SimpleUserHierarchy(subjectProvider, subjectIdentifier, translate);
        option2.set(9, key1);
        //then
        when(subjectProvider.get()).thenReturn(subject1);
        when(subject1.isAuthenticated()).thenReturn(true);
        when(subjectIdentifier.userId()).thenReturn("fbaton");

        Integer actual = option2.get(177, key1);
        assertThat(actual).isEqualTo(3);
        actual = option2.get(177, key1);
        actual = option2.get(177, key1);
        actual = option2.get(177, key1);
        actual = option2.get(177, key1);
        assertThat(optionCache.cacheSize()).isEqualTo(1);
        assertThat(optionCache.stats()
                              .hitCount()).isEqualTo(4);

        when(subjectProvider.get()).thenReturn(subject2);
        when(subject2.isAuthenticated()).thenReturn(true);
        when(subjectIdentifier.userId()).thenReturn("equick");

        actual = option2.get(277, key1);
        assertThat(actual).isEqualTo(9);
        actual = option2.get(177, key1);
        actual = option2.get(177, key1);
        actual = option2.get(177, key1);
        actual = option2.get(177, key1);
        assertThat(optionCache.cacheSize()).isEqualTo(2);
        assertThat(optionCache.stats()
                              .hitCount()).isEqualTo(8);
        actual = option2.getLowestRanked(5, key1);
        assertThat(optionCache.cacheSize()).isEqualTo(3);
        assertThat(optionCache.stats()
                              .hitCount()).isEqualTo(8);
        actual = option2.getLowestRanked(5, key2);
        assertThat(optionCache.cacheSize()).isEqualTo(4);
        assertThat(optionCache.stats()
                              .hitCount()).isEqualTo(8);
    }

    @ModuleProvider
    protected AbstractModule moduleProvider() {
        return new AbstractModule() {

            @Override
            protected void configure() {
                GuavaCacheConfiguration cacheConfig = new GuavaCacheConfiguration().recordStats();

                bind(OptionDao.class).to(InMemoryOptionDao.class);
                bind(GuavaCacheConfiguration.class).annotatedWith(OptionCacheConfig.class)
                                                   .toInstance(cacheConfig);
            }

        };
    }


}