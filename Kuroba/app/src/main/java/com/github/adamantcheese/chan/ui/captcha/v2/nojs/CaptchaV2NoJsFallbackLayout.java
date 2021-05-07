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
package com.github.adamantcheese.chan.ui.captcha.v2.nojs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import com.github.adamantcheese.chan.core.net.NetUtils;
import com.github.adamantcheese.chan.core.site.Site;
import com.github.adamantcheese.chan.core.site.SiteAuthentication;
import com.github.adamantcheese.chan.ui.captcha.AuthenticationLayoutCallback;
import com.github.adamantcheese.chan.ui.captcha.AuthenticationLayoutInterface;
import com.github.adamantcheese.chan.ui.captcha.CaptchaTokenHolder;
import com.github.adamantcheese.chan.utils.BackgroundUtils;
import com.github.adamantcheese.chan.utils.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import static com.github.adamantcheese.chan.Chan.inject;
import static com.github.adamantcheese.chan.utils.AndroidUtils.openLink;

/**
 * Loads a Captcha2 fallback url in a webview; not the same as a regular captcha2 in CaptchaLayout.
 */
public class CaptchaV2NoJsFallbackLayout
        extends WebView
        implements AuthenticationLayoutInterface {
    private static final long RECAPTCHA_TOKEN_LIVE_TIME = TimeUnit.MINUTES.toMillis(2);

    @Inject
    CaptchaTokenHolder captchaTokenHolder;

    private AuthenticationLayoutCallback callback;
    private String baseUrl;
    private String siteKey;

    private boolean isAutoReply = true;

    public CaptchaV2NoJsFallbackLayout(Context context) {
        this(context, null);
    }

    public CaptchaV2NoJsFallbackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptchaV2NoJsFallbackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getSettings().setUserAgentString(NetUtils.USER_AGENT);
        inject(this);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void initialize(Site site, AuthenticationLayoutCallback callback, boolean autoReply) {
        this.callback = callback;
        this.isAutoReply = autoReply;

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptThirdPartyCookies(this, true);

        SiteAuthentication authentication = site.actions().postAuthenticate();

        this.siteKey = authentication.siteKey;
        this.baseUrl = authentication.baseUrl;

        requestDisallowInterceptTouchEvent(true);

        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);

        setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(@NonNull ConsoleMessage consoleMessage) {
                Logger.i(
                        CaptchaV2NoJsFallbackLayout.this,
                        consoleMessage.lineNumber() + ":" + consoleMessage.message() + " " + consoleMessage.sourceId()
                );
                return true;
            }
        });

        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Fails if there is no token yet, which is ok.
                final String setResponseJavascript = "CaptchaCallback.onCaptchaEntered("
                        + "document.querySelector('.fbc-verification-token textarea').value);";
                view.loadUrl("javascript:" + setResponseJavascript);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                String host = Uri.parse(url).getHost();
                if (host == null) {
                    return false;
                }

                if (host.equals(Uri.parse(CaptchaV2NoJsFallbackLayout.this.baseUrl).getHost())) {
                    return false;
                } else {
                    openLink(url);
                    return true;
                }
            }
        });
        setBackgroundColor(0x00000000);

        addJavascriptInterface(new CaptchaInterface(this), "CaptchaCallback");
    }

    public void reset() {
        if (captchaTokenHolder.hasToken() && isAutoReply) {
            callback.onAuthenticationComplete(this, null, captchaTokenHolder.getToken(), true);
            return;
        }

        hardReset();
    }

    @Override
    public void hardReset() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", baseUrl);
        loadUrl("https://www.google.com/recaptcha/api/fallback?k=" + siteKey, headers);
    }

    private void onCaptchaEntered(String response) {
        if (TextUtils.isEmpty(response)) {
            reset();
        } else {
            captchaTokenHolder.addNewToken(response, RECAPTCHA_TOKEN_LIVE_TIME);

            String token;

            if (isAutoReply) {
                token = captchaTokenHolder.getToken();
            } else {
                token = response;
            }

            callback.onAuthenticationComplete(this, null, token, isAutoReply);
        }
    }

    public static class CaptchaInterface {
        private final CaptchaV2NoJsFallbackLayout layout;

        public CaptchaInterface(CaptchaV2NoJsFallbackLayout layout) {
            this.layout = layout;
        }

        @JavascriptInterface
        public void onCaptchaEntered(final String response) {
            BackgroundUtils.runOnMainThread(() -> layout.onCaptchaEntered(response));
        }
    }
}