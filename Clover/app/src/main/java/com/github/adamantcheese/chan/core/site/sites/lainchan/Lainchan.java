/*
 * Clover4 - *chan browser https://github.com/Adamantcheese/Clover4/
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
package com.github.adamantcheese.chan.core.site.sites.lainchan;

import android.support.annotation.Nullable;

import com.github.adamantcheese.chan.core.model.Post;
import com.github.adamantcheese.chan.core.model.orm.Board;
import com.github.adamantcheese.chan.core.model.orm.Loadable;
import com.github.adamantcheese.chan.core.site.Site;
import com.github.adamantcheese.chan.core.site.SiteIcon;
import com.github.adamantcheese.chan.core.site.common.CommonSite;
import com.github.adamantcheese.chan.core.site.common.vichan.VichanActions;
import com.github.adamantcheese.chan.core.site.common.vichan.VichanApi;
import com.github.adamantcheese.chan.core.site.common.vichan.VichanCommentParser;
import com.github.adamantcheese.chan.core.site.common.vichan.VichanEndpoints;

import okhttp3.HttpUrl;

public class Lainchan extends CommonSite {
    public static final CommonSiteUrlHandler URL_HANDLER = new CommonSiteUrlHandler() {
        @Override
        public Class<? extends Site> getSiteClass() {
            return Lainchan.class;
        }

        @Override
        public HttpUrl getUrl() {
            return HttpUrl.parse("https://lainchan.org/");
        }

        @Override
        public String[] getNames() {
            return new String[]{"lainchan"};
        }

        @Override
        public String desktopUrl(Loadable loadable, @Nullable Post post) {
            if (loadable.isCatalogMode()) {
                return getUrl().newBuilder().addPathSegment(loadable.boardCode).toString();
            } else if (loadable.isThreadMode()) {
                return getUrl().newBuilder()
                        .addPathSegment(loadable.boardCode).addPathSegment("res")
                        .addPathSegment(loadable.no + ".html")
                        .toString();
            } else {
                return getUrl().toString();
            }
        }
    };

    @Override
    public void setup() {
        setName("Lainchan");
        setIcon(SiteIcon.fromFavicon(HttpUrl.parse("https://lainchan.org/favicon.ico")));

        setBoards(
                Board.fromSiteNameCode(this, "Programming", "λ"),
                Board.fromSiteNameCode(this, "Do It Yourself", "Δ"),
                Board.fromSiteNameCode(this, "Security", "sec"),
                Board.fromSiteNameCode(this, "Technology", "Ω"),
                Board.fromSiteNameCode(this, "Games and Interactive Media", "inter"),
                Board.fromSiteNameCode(this, "Literature", "lit"),
                Board.fromSiteNameCode(this, "Musical and Audible Media", "music"),
                Board.fromSiteNameCode(this, "Visual Media", "vis"),
                Board.fromSiteNameCode(this, "Humanity", "hum"),
                Board.fromSiteNameCode(this, "Drugs 3.0", "drug"),
                Board.fromSiteNameCode(this, "Consciousness and Dreams", "zzz"),
                Board.fromSiteNameCode(this, "layer", "layer"),
                Board.fromSiteNameCode(this, "Questions and Complaints", "q"),
                Board.fromSiteNameCode(this, "Random", "r"),
                Board.fromSiteNameCode(this, "Lain", "lain")
        );

        setResolvable(URL_HANDLER);

        setConfig(new CommonConfig() {
            @Override
            public boolean feature(Feature feature) {
                return feature == Feature.POSTING;
            }
        });

        setEndpoints(new VichanEndpoints(this,
                "https://lainchan.org",
                "https://lainchan.org"));
        setActions(new VichanActions(this));
        setApi(new VichanApi(this));
        setParser(new VichanCommentParser());
    }
}