/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.latin;

import android.content.Context;
import android.view.inputmethod.InputMethodSubtype;
import android.util.Pair;

import com.android.inputmethod.annotations.UsedForTesting;
import com.android.inputmethod.latin.ExpandableBinaryDictionary.UpdateEntriesForInputEventsCallback;
import com.android.inputmethod.latin.personalization.PersonalizationDataChunk;
import com.android.inputmethod.latin.settings.SettingsValuesForSuggestion;
import com.android.inputmethod.latin.settings.SpacingAndPunctuations;
import com.android.inputmethod.latin.utils.SuggestionResults;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface that facilitates interaction with different kinds of dictionaries. Provides APIs to
 * instantiate and select the correct dictionaries (based on language or account), update entries
 * and fetch suggestions. Currently AndroidSpellCheckerService and LatinIME both use
 * DictionaryFacilitator as a client for interacting with dictionaries.
 */
public interface DictionaryFacilitator {
    /**
     * Returns whether this facilitator is exactly for this list of locales.
     *
     * @param locales the list of locales to test against
     */
    boolean isForLocales(final Locale[] locales);

    /**
     * Returns whether this facilitator is exactly for this account.
     *
     * @param account the account to test against.
     */
    boolean isForAccount(@Nullable final String account);

    interface DictionaryInitializationListener {
        void onUpdateMainDictionaryAvailability(boolean isMainDictionaryAvailable);
    }

    void updateEnabledSubtypes(final List<InputMethodSubtype> enabledSubtypes);

    // TODO: remove this, it's confusing with seamless multiple language switching
    void setIsMonolingualUser(final boolean isMonolingualUser);

    boolean isActive();

    /**
     * Returns the most probable locale among all currently active locales. BE CAREFUL using this.
     *
     * DO NOT USE THIS just because it's convenient. Use it when it's correct, for example when
     * choosing what dictionary to put a word in, or when changing the capitalization of a typed
     * string.
     * @return the most probable locale
     */
    Locale getMostProbableLocale();

    Locale[] getLocales();

    void switchMostProbableLanguage(@Nullable final Locale locale);

    boolean isConfidentAboutCurrentLanguageBeing(final Locale mLocale);

    void resetDictionaries(final Context context, final Locale[] newLocales,
            final boolean useContactsDict, final boolean usePersonalizedDicts,
            final boolean forceReloadMainDictionary,
            @Nullable final String account,
            final DictionaryInitializationListener listener);

    void resetDictionariesWithDictNamePrefix(final Context context,
            final Locale[] newLocales,
            final boolean useContactsDict,
            final boolean usePersonalizedDicts,
            final boolean forceReloadMainDictionary,
            @Nullable final DictionaryInitializationListener listener,
            final String dictNamePrefix,
            @Nullable final String account);

    @UsedForTesting
    void resetDictionariesForTesting(final Context context, final Locale[] locales,
            final ArrayList<String> dictionaryTypes, final HashMap<String, File> dictionaryFiles,
            final Map<String, Map<String, String>> additionalDictAttributes,
            @Nullable final String account);

    void closeDictionaries();

    @UsedForTesting
    ExpandableBinaryDictionary getSubDictForTesting(final String dictName);

    // The main dictionaries are loaded asynchronously. Don't cache the return value
    // of these methods.
    boolean hasAtLeastOneInitializedMainDictionary();

    boolean hasAtLeastOneUninitializedMainDictionary();

    boolean hasPersonalizationDictionary();

    void flushPersonalizationDictionary();

    void waitForLoadingMainDictionaries(final long timeout, final TimeUnit unit)
            throws InterruptedException;

    @UsedForTesting
    void waitForLoadingDictionariesForTesting(final long timeout, final TimeUnit unit)
            throws InterruptedException;

    boolean isUserDictionaryEnabled();

    void addWordToUserDictionary(final Context context, final String word);

    void addToUserHistory(final String suggestion, final boolean wasAutoCapitalized,
            @Nonnull final NgramContext ngramContext, final int timeStampInSeconds,
            final boolean blockPotentiallyOffensive);

    void removeWordFromPersonalizedDicts(final String word);

    // TODO: Revise the way to fusion suggestion results.
    SuggestionResults getSuggestionResults(final WordComposer composer,
            final NgramContext ngramContext, final long proximityInfoHandle,
            final SettingsValuesForSuggestion settingsValuesForSuggestion, final int sessionId);

    boolean isValidWord(final String word, final boolean ignoreCase);

    int getFrequency(final String word);

    int getMaxFrequencyOfExactMatches(final String word);

    void clearUserHistoryDictionary();

    // This method gets called only when the IME receives a notification to remove the
    // personalization dictionary.
    void clearPersonalizationDictionary();

    void clearContextualDictionary();

    void addEntriesToPersonalizationDictionary(
            final PersonalizationDataChunk personalizationDataChunk,
            final SpacingAndPunctuations spacingAndPunctuations,
            final UpdateEntriesForInputEventsCallback callback);

    @UsedForTesting
    void addPhraseToContextualDictionary(final String[] phrase, final int probability,
            final int bigramProbabilityForWords, final int bigramProbabilityForPhrases);

    void dumpDictionaryForDebug(final String dictName);

    ArrayList<Pair<String, DictionaryStats>> getStatsOfEnabledSubDicts();
}
