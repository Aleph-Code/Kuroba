/*
 * Kuroba - *chan browser https://github.com/Adamantcheese/Kuroba/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.adamantcheese.chan.core.di;

import android.content.Context;
import android.net.ConnectivityManager;

import com.github.adamantcheese.chan.BuildConfig;
import com.github.adamantcheese.chan.core.database.DatabaseHelper;
import com.github.adamantcheese.chan.core.database.DatabaseManager;
import com.github.adamantcheese.chan.core.repository.SiteRepository;
import com.github.adamantcheese.chan.core.saver.ImageSaver;
import com.github.adamantcheese.chan.core.site.SiteResolver;
import com.github.adamantcheese.chan.features.gesture_editor.Android10GesturesExclusionZonesHolder;
import com.github.adamantcheese.chan.ui.captcha.CaptchaHolder;
import com.github.adamantcheese.chan.ui.settings.base_directory.LocalThreadsBaseDirectory;
import com.github.adamantcheese.chan.ui.settings.base_directory.SavedFilesBaseDirectory;
import com.github.adamantcheese.chan.ui.theme.ThemeHelper;
import com.github.adamantcheese.chan.utils.Logger;
import com.github.k1rakishou.fsaf.BadPathSymbolResolutionStrategy;
import com.github.k1rakishou.fsaf.FileChooser;
import com.github.k1rakishou.fsaf.FileManager;
import com.github.k1rakishou.fsaf.manager.base_directory.DirectoryManager;
import com.google.gson.Gson;

import org.codejargon.feather.Provides;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import static com.github.adamantcheese.chan.utils.AndroidUtils.getAppContext;
import static com.github.adamantcheese.chan.utils.AndroidUtils.getMaxScreenSize;
import static com.github.adamantcheese.chan.utils.AndroidUtils.getMinScreenSize;
import static com.github.k1rakishou.fsaf.BadPathSymbolResolutionStrategy.ReplaceBadSymbols;
import static com.github.k1rakishou.fsaf.BadPathSymbolResolutionStrategy.ThrowAnException;

public class AppModule {
    private Context applicationContext;
    public static final String DI_TAG = "Dependency Injection";

    public AppModule(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Provides
    @Singleton
    public Context provideApplicationContext() {
        Logger.d(DI_TAG, "App Context");
        return applicationContext;
    }

    @Provides
    @Singleton
    public DatabaseHelper provideDatabaseHelper(Context applicationContext) {
        Logger.d(AppModule.DI_TAG, "Database helper");
        return new DatabaseHelper(applicationContext);
    }

    @Provides
    @Singleton
    public DatabaseManager provideDatabaseManager() {
        Logger.d(AppModule.DI_TAG, "Database manager");
        return new DatabaseManager();
    }

    @Provides
    @Singleton
    public SiteResolver provideSiteResolver(SiteRepository siteRepository) {
        Logger.d(AppModule.DI_TAG, "Site resolver");
        return new SiteResolver(siteRepository);
    }

    @Provides
    @Singleton
    public ConnectivityManager provideConnectivityManager() {
        Logger.d(DI_TAG, "Connectivity Manager");

        ConnectivityManager connectivityManager =
                (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            throw new NullPointerException("What's working in this ROM: You tell me ;) "
                    + "\nWhat doesn't work: Connectivity fucking manager");
        }

        return connectivityManager;
    }

    @Provides
    @Singleton
    public ThemeHelper provideThemeHelper() {
        Logger.d(DI_TAG, "Theme helper");
        return new ThemeHelper();
    }

    @Provides
    @Singleton
    public ImageSaver provideImageSaver(FileManager fileManager) {
        Logger.d(DI_TAG, "Image saver");
        return new ImageSaver(fileManager);
    }

    @Provides
    @Singleton
    public CaptchaHolder provideCaptchaHolder() {
        Logger.d(DI_TAG, "Captcha holder");
        return new CaptchaHolder();
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        Logger.d(AppModule.DI_TAG, "Gson module");
        return new Gson();
    }

    @Provides
    @Singleton
    public FileManager provideFileManager() {
        DirectoryManager directoryManager = new DirectoryManager(applicationContext);

        // Add new base directories here
        LocalThreadsBaseDirectory localThreadsBaseDirectory = new LocalThreadsBaseDirectory();
        SavedFilesBaseDirectory savedFilesBaseDirectory = new SavedFilesBaseDirectory();

        BadPathSymbolResolutionStrategy resolutionStrategy =
                BuildConfig.DEV_BUILD ? ThrowAnException : ReplaceBadSymbols;

        FileManager fileManager = new FileManager(applicationContext, resolutionStrategy, directoryManager);

        fileManager.registerBaseDir(LocalThreadsBaseDirectory.class, localThreadsBaseDirectory);
        fileManager.registerBaseDir(SavedFilesBaseDirectory.class, savedFilesBaseDirectory);

        return fileManager;
    }

    @Provides
    @Singleton
    public FileChooser provideFileChooser() {
        return new FileChooser(applicationContext);
    }

    static File getCacheDir() {
        // See also res/xml/filepaths.xml for the fileprovider.
        if (getAppContext().getExternalCacheDir() != null) {
            return getAppContext().getExternalCacheDir();
        } else {
            return getAppContext().getCacheDir();
        }
    }

    @Provides
    @Singleton
    public Android10GesturesExclusionZonesHolder provideAndroid10GesturesHolder(Gson gson) {
        Logger.d(DI_TAG, "Android10GesturesExclusionZonesHolder");

        return new Android10GesturesExclusionZonesHolder(gson, getMinScreenSize(), getMaxScreenSize());
    }

    @Provides
    @Singleton
    public ExecutorService provideBackgroundPool() {
        return Executors.newCachedThreadPool();
    }
}
