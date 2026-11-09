package org.telegram.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class MoonGramModSettingsActivity extends BaseFragment {
    private static final String PREFS = "moonsgram_mod_settings";
    private static final String KEY_BANNER = "profile_banner";
    private static final String KEY_ANIMATIONS = "animations_enabled";
    private SharedPreferences preferences;

    private final int[][] banners = new int[][]{
        {0xFF151A46, 0xFF5337A8},
        {0xFF071D35, 0xFF087E9B},
        {0xFF25113F, 0xFF9B3D85},
        {0xFF101820, 0xFF46617B},
        {0xFF301B12, 0xFFB66A35},
        {0xFF101B28, 0xFF536D9B}
    };

    @Override
    public boolean onFragmentCreate() {
        preferences = ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        actionBar.setTitle("MoonGram MOD");
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
            }
        });

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(18), AndroidUtilities.dp(16), AndroidUtilities.dp(24));
        content.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        TextView bannerTitle = heading(context, "Баннер профиля");
        content.addView(bannerTitle, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 10));

        int selected = preferences.getInt(KEY_BANNER, 0);
        LinearLayout bannerGrid = new LinearLayout(context);
        bannerGrid.setOrientation(LinearLayout.VERTICAL);
        for (int row = 0; row < 3; row++) {
            LinearLayout line = new LinearLayout(context);
            line.setOrientation(LinearLayout.HORIZONTAL);
            for (int col = 0; col < 2; col++) {
                final int index = row * 2 + col;
                FrameLayout tile = new FrameLayout(context);
                GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, banners[index]);
                drawable.setCornerRadius(AndroidUtilities.dp(14));
                tile.setBackground(drawable);
                tile.setElevation(AndroidUtilities.dp(index == selected ? 4 : 0));
                TextView label = new TextView(context);
                label.setText(index == selected ? "✓ Выбран" : "Баннер " + (index + 1));
                label.setTextColor(0xFFFFFFFF);
                label.setTextSize(13);
                label.setGravity(Gravity.CENTER);
                tile.addView(label, new FrameLayout.LayoutParams(-1, -1));
                tile.setOnClickListener(v -> {
                    preferences.edit().putInt(KEY_BANNER, index).apply();
                    finishFragment();
                    presentFragment(new MoonGramModSettingsActivity());
                });
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, AndroidUtilities.dp(76), 1);
                lp.setMargins(AndroidUtilities.dp(4), AndroidUtilities.dp(4), AndroidUtilities.dp(4), AndroidUtilities.dp(4));
                line.addView(tile, lp);
            }
            bannerGrid.addView(line, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }
        content.addView(bannerGrid, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        content.addView(note(context, "Пока выбор сохраняется на этом устройстве. Чтобы баннер отображался другим пользователям, понадобится синхронизация с сервером."), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 8, 0, 18));

        TextView animationTitle = heading(context, "Анимации");
        content.addView(animationTitle, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));
        boolean enabled = preferences.getBoolean(KEY_ANIMATIONS, true);
        TextView animationToggle = option(context, "Плавные анимации", enabled ? "Включены" : "Выключены");
        animationToggle.setOnClickListener(v -> {
            boolean next = !preferences.getBoolean(KEY_ANIMATIONS, true);
            preferences.edit().putBoolean(KEY_ANIMATIONS, next).apply();
            animationToggle.setText("Плавные анимации     " + (next ? "Включены" : "Выключены"));
        });
        content.addView(animationToggle, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(54)));

        TextView privacyTitle = heading(context, "Приватность");
        content.addView(privacyTitle, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 18, 0, 8));
        TextView ghost = option(context, "Ghost 👻", "Требует подключения");
        ghost.setOnClickListener(v -> new AlertDialog.Builder(getParentActivity())
            .setTitle("Ghost 👻")
            .setMessage("Функция будет управлять поддерживаемыми настройками приватности Telegram и сохранять их исходные значения для восстановления. Сейчас она ещё не активирована, чтобы случайно не менять приватность аккаунта.")
            .setPositiveButton("Понятно", null)
            .show());
        content.addView(ghost, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(54)));

        TextView roles = heading(context, "Роли и оформление");
        content.addView(roles, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 18, 0, 8));
        content.addView(note(context, "Значки Owner, Team и Supporter, а также отображение баннера в публичной шапке профиля будут подключены следующим этапом после проверки серверной модели ролей и профиля."), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        ScrollViewContainer scroll = new ScrollViewContainer(context);
        scroll.addView(content);
        fragmentView = scroll;
        return fragmentView;
    }

    private TextView heading(Context context, String text) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        view.setTextSize(17);
        view.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        return view;
    }

    private TextView option(Context context, String title, String value) {
        TextView view = new TextView(context);
        view.setText(title + "     " + value);
        view.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        view.setTextSize(15);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setPadding(AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14), 0);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        bg.setCornerRadius(AndroidUtilities.dp(12));
        view.setBackground(bg);
        return view;
    }

    private TextView note(Context context, String text) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        view.setTextSize(13);
        view.setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(4), AndroidUtilities.dp(4), AndroidUtilities.dp(4));
        return view;
    }

    private static class ScrollViewContainer extends android.widget.ScrollView {
        ScrollViewContainer(Context context) {
            super(context);
            setFillViewport(true);
            setClipToPadding(false);
        }
    }
}
