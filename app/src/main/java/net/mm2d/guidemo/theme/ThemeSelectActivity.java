/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.guidemo.theme;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.mm2d.guidemo.R;

import java.util.ArrayList;
import java.util.List;

public class ThemeSelectActivity extends AppCompatActivity {
    public static final String EXTRA_THEME = "EXTRA_THEME";
    public static final String EXTRA_NAME = "EXTRA_NAME";

    private static class Entry {
        private final String mName;
        private final int mValue;

        Entry(
                String name,
                int value) {
            mName = name;
            mValue = value;
        }

        int getValue() {
            return mValue;
        }

        public String toString() {
            return mName;
        }
    }

    private final List<Entry> mEntries;

    public ThemeSelectActivity() {
        mEntries = new ArrayList<>();
        mEntries.add(new Entry("Theme",
                android.R.style.Theme));
        mEntries.add(new Entry("Theme.Black",
                android.R.style.Theme_Black));
        mEntries.add(new Entry("Theme.Light",
                android.R.style.Theme_Light));
        mEntries.add(new Entry("Theme.Holo",
                android.R.style.Theme_Holo));
        mEntries.add(new Entry("Theme.Holo.Light",
                android.R.style.Theme_Holo_Light));
        mEntries.add(new Entry("Theme.Holo.Light.DarkActionBar",
                android.R.style.Theme_Holo_Light_DarkActionBar));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mEntries.add(new Entry("Theme.Material",
                    android.R.style.Theme_Material));
            mEntries.add(new Entry("Theme.Material.Light",
                    android.R.style.Theme_Material_Light));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mEntries.add(new Entry("Theme.Material.Light.LightStatusBar",
                    android.R.style.Theme_Material_Light_LightStatusBar));
        }
        mEntries.add(new Entry("AppTheme(Custom)",
                R.style.AppTheme));
        mEntries.add(new Entry("Theme.AppCompat",
                androidx.appcompat.R.style.Theme_AppCompat));
        mEntries.add(new Entry("Theme.AppCompat.DayNight",
                androidx.appcompat.R.style.Theme_AppCompat_DayNight));
        mEntries.add(new Entry("Theme.AppCompat.DayNight.DarkActionBar",
                androidx.appcompat.R.style.Theme_AppCompat_DayNight_DarkActionBar));
        mEntries.add(new Entry("Theme.AppCompat.Light",
                androidx.appcompat.R.style.Theme_AppCompat_Light));
        mEntries.add(new Entry("Theme.AppCompat.Light.DarkActionBar",
                androidx.appcompat.R.style.Theme_AppCompat_Light_DarkActionBar));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_select);
        final ListView listView = findViewById(R.id.listView);
        assert listView != null;
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mEntries));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            final Entry entry = mEntries.get(position);
            final int value = entry.getValue();
            final Intent intent = new Intent();
            if (value < 0x7f000000) {
                intent.setClass(ThemeSelectActivity.this, ThemeCheckBaseActivity.class);
            } else {
                intent.setClass(ThemeSelectActivity.this, ThemeCheckCompatActivity.class);
            }
            intent.putExtra(EXTRA_THEME, value);
            intent.putExtra(EXTRA_NAME, entry.toString());
            startActivity(intent);
        });
    }
}
