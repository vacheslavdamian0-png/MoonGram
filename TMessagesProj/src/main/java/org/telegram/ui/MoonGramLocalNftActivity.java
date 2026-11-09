package org.telegram.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

/**
 * Local-only NFT gift preview. It never contacts Telegram or writes profile data.
 */
public class MoonGramLocalNftActivity extends BaseFragment {
    private EditText nftIdInput;
    private TextView giftPreview;
    private TextView giftId;

    @Override
    public View createView(Context context) {
        actionBar.setTitle("Local NFT");
        actionBar.setBackButtonImage(org.telegram.messenger.R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
            }
        });

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(24), AndroidUtilities.dp(20), AndroidUtilities.dp(28));
        content.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        TextView intro = new TextView(context);
        intro.setText("Создай локальный визуал подарка");
        intro.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        intro.setTextSize(21);
        intro.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        content.addView(intro, LayoutHelper.createLinear(-1, -2, 0, 0, 0, 8));

        TextView hint = new TextView(context);
        hint.setText("Введи ID NFT — подарок появится только в этом чате как анимация. Он не отправляется собеседнику и не добавляется в профиль.");
        hint.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        hint.setTextSize(14);
        content.addView(hint, LayoutHelper.createLinear(-1, -2, 0, 0, 0, 22));

        nftIdInput = new EditText(context);
        nftIdInput.setSingleLine(true);
        nftIdInput.setTextSize(16);
        nftIdInput.setHint("Например: 123456");
        nftIdInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        nftIdInput.setPadding(AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14), 0);
        nftIdInput.setBackground(roundRect(Theme.getColor(Theme.key_windowBackgroundWhite), 14));
        content.addView(nftIdInput, LayoutHelper.createLinear(-1, 52, 0, 0, 0, 12));

        TextView showButton = new TextView(context);
        showButton.setText("Показать подарок");
        showButton.setTextColor(Color.WHITE);
        showButton.setTextSize(15);
        showButton.setGravity(Gravity.CENTER);
        showButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        showButton.setBackground(roundRect(0xFF6945C7, 14));
        content.addView(showButton, LayoutHelper.createLinear(-1, 50, 0, 0, 0, 20));

        LinearLayout preview = new LinearLayout(context);
        preview.setOrientation(LinearLayout.VERTICAL);
        preview.setGravity(Gravity.CENTER);
        preview.setPadding(AndroidUtilities.dp(18), AndroidUtilities.dp(22), AndroidUtilities.dp(18), AndroidUtilities.dp(22));
        preview.setBackground(roundRect(0xFF17152B, 20));
        preview.setVisibility(View.GONE);
        preview.setScaleX(0.82f);
        preview.setScaleY(0.82f);
        preview.setAlpha(0f);

        giftPreview = new TextView(context);
        giftPreview.setText("🎁");
        giftPreview.setTextSize(76);
        giftPreview.setGravity(Gravity.CENTER);
        preview.addView(giftPreview, LayoutHelper.createLinear(-1, -2));

        TextView title = new TextView(context);
        title.setText("ТВОЙ LOCAL NFT");
        title.setTextColor(0xFFE8D8FF);
        title.setTextSize(16);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        preview.addView(title, LayoutHelper.createLinear(-1, -2, 0, 8, 0, 4));

        giftId = new TextView(context);
        giftId.setTextColor(Color.WHITE);
        giftId.setTextSize(14);
        giftId.setGravity(Gravity.CENTER);
        preview.addView(giftId, LayoutHelper.createLinear(-1, -2));

        content.addView(preview, LayoutHelper.createLinear(-1, -2, 0, 0, 0, 16));
        showButton.setOnClickListener(v -> {
            String id = nftIdInput.getText() == null ? "" : nftIdInput.getText().toString().trim();
            if (id.isEmpty() || id.length() > 80) {
                nftIdInput.setError("Введи ID длиной от 1 до 80 символов");
                return;
            }
            giftId.setText("NFT ID: " + id);
            preview.setVisibility(View.VISIBLE);
            preview.setAlpha(0f);
            preview.setScaleX(0.72f);
            preview.setScaleY(0.72f);
            preview.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(520)
                    .setInterpolator(new OvershootInterpolator(1.15f)).start();
            giftPreview.animate().rotationBy(12f).setDuration(180).withEndAction(() ->
                    giftPreview.animate().rotationBy(-12f).setDuration(180).start()).start();
        });

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.addView(content);
        fragmentView = scroll;
        return fragmentView;
    }

    private GradientDrawable roundRect(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(AndroidUtilities.dp(radiusDp));
        return drawable;
    }
}