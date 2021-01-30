package com.github.adamantcheese.chan.ui.cell;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.github.adamantcheese.chan.R;
import com.github.adamantcheese.chan.core.model.Post;
import com.github.adamantcheese.chan.core.model.PostImage;
import com.github.adamantcheese.chan.core.model.orm.Loadable;
import com.github.adamantcheese.chan.core.settings.ChanSettings;
import com.github.adamantcheese.chan.ui.layout.FixedRatioLinearLayout;
import com.github.adamantcheese.chan.ui.theme.Theme;
import com.github.adamantcheese.chan.ui.view.FloatingMenu;
import com.github.adamantcheese.chan.ui.view.FloatingMenuItem;
import com.github.adamantcheese.chan.ui.view.ThumbnailView;

import java.util.ArrayList;
import java.util.List;

import static com.github.adamantcheese.chan.utils.AndroidUtils.dp;

public class CardPostStubCell
        extends CardView
        implements PostCellInterface {
    private Post post;
    private PostCellInterface.PostCellCallback callback;

    private TextView title;
    private ImageView options;

    public CardPostStubCell(@NonNull Context context) {
        super(context);
    }

    public CardPostStubCell(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CardPostStubCell(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        title = findViewById(R.id.title);
        options = findViewById(R.id.options);

        if (!isInEditMode()) {
            setCompact(false);
        }

        options.setOnClickListener(v -> {
            List<FloatingMenuItem<Integer>> items = new ArrayList<>();
            List<FloatingMenuItem<Integer>> extraItems = new ArrayList<>();
            Object extraOption = callback.onPopulatePostOptions(post, items, extraItems);
            showOptions(v, items, extraItems, extraOption);
        });

        if (!isInEditMode() && ChanSettings.getBoardColumnCount() == 1) {
            ((FixedRatioLinearLayout) findViewById(R.id.card_content)).setRatio(0.0f);
            invalidate();
        }
    }

    private void showOptions(
            View anchor,
            List<FloatingMenuItem<Integer>> items,
            List<FloatingMenuItem<Integer>> extraItems,
            Object extraOption
    ) {
        FloatingMenu<Integer> menu = new FloatingMenu<>(getContext(), anchor, items);
        menu.setCallback(new FloatingMenu.ClickCallback<Integer>() {
            @Override
            public void onFloatingMenuItemClicked(FloatingMenu<Integer> menu, FloatingMenuItem<Integer> item) {
                if (item.getId() == extraOption) {
                    showOptions(anchor, extraItems, null, null);
                }

                callback.onPostOptionClicked(anchor, post, item.getId(), false);
            }
        });
        menu.show();
    }

    @Override
    public void setPost(
            Loadable loadable,
            Post post,
            PostCellCallback callback,
            boolean inPopup,
            boolean highlighted,
            int markedNo,
            boolean compact,
            Theme theme
    ) {
        this.post = post;
        this.callback = callback;

        // Spans are stripped here, to better distinguish a stub post
        if (!TextUtils.isEmpty(post.subjectSpan)) {
            title.setText(post.subjectSpan.toString());
        } else {
            title.setText(post.comment.toString());
        }

        setCompact(compact);
    }

    @Override
    public void unsetPost() {
        post = null;
    }

    @Override
    public Post getPost() {
        return post;
    }

    @Override
    public ThumbnailView getThumbnailView(PostImage postImage) {
        return null;
    }

    private void setCompact(boolean compact) {
        int textSizeSp = ChanSettings.fontSize.get() + (compact ? -2 : 0);
        title.setTextSize(textSizeSp);
        int p = compact ? dp(3) : dp(8);

        // Same as the layout.
        title.setPadding(p, p, p, 0);
        options.setPadding(p, p / 2, p / 2, p / 2);
    }
}
