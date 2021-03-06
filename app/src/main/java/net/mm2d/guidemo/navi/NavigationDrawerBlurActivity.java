/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.guidemo.navi;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

import net.mm2d.guidemo.R;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener;

public class NavigationDrawerBlurActivity extends AppCompatActivity
    implements OnNavigationItemSelectedListener, DrawerListener {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView mNavigationView;
    private MenuBackground mMenuBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer_blur);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(
            this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mToggle);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        mToggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        assert mNavigationView != null;
        mNavigationView.setNavigationItemSelectedListener(this);
        mDrawerLayout.addDrawerListener(this);
        mMenuBackground = new MenuBackground(this);
        mNavigationView.setBackground(mMenuBackground);

        final float density = getResources().getDisplayMetrics().density;
        final int size = (int) (58 * density + 0.5f);
        final int margin = (int) (10 * density + 0.5f);
        final int padding = (int) (5 * density + 0.5f);
        final int activity_margin = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        final int width = getResources().getDisplayMetrics().widthPixels - activity_margin * 2;
        final int num = width / (size + margin);
        final GridView gridView = findViewById(R.id.gridView);
        assert gridView != null;
        gridView.setEnabled(false);
        gridView.setNumColumns(num);
        gridView.setVerticalSpacing(margin);
        gridView.setHorizontalSpacing(margin);
        gridView.setAdapter(new BaseAdapter() {
            private final float[] hsv = new float[]{0, 1, 1};

            @Override
            public int getCount() {
                return 200;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(
                int position,
                View convertView,
                ViewGroup parent) {
                final View view;
                if (convertView != null) {
                    view = convertView;
                } else {
                    final ImageView image = new AppCompatImageView(getBaseContext());
                    image.setImageResource(R.drawable.ic_account);
                    final GridView.LayoutParams params = new GridView.LayoutParams(size, size);
                    image.setLayoutParams(params);
                    image.setPadding(padding, padding, padding, padding);
                    view = image;
                }
                final GradientDrawable d = new GradientDrawable();
                d.setShape(GradientDrawable.OVAL);
                // hsv[0] = (position * 73) % 360;
                hsv[0] = (float) (Math.random() * 360);
                d.setColor(Color.HSVToColor(hsv));
                view.setBackground(d);
                return view;
            }
        });
        new Handler().postDelayed(() -> mMenuBackground.capture(), 200);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDrawerLayout.removeDrawerListener(mToggle);
        mDrawerLayout.removeDrawerListener(this);
    }

    @Override
    public void onBackPressed() {
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onDrawerSlide(
        @NonNull View drawerView,
        float slideOffset) {
        final int width = mNavigationView.getWidth();
        mMenuBackground.setOffset(width * (1.0f - slideOffset));
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    private static class MenuBackground extends Drawable {
        private final View mView;
        private Bitmap mBitmap;
        private final Paint mPaint;
        private float mOffset;
        private final float mDensity;

        MenuBackground(Activity activity) {
            mView = activity.findViewById(android.R.id.content);
            mPaint = new Paint();
            mDensity = activity.getResources().getDisplayMetrics().density;
        }

        void capture() {
            if (mBitmap != null) {
                return;
            }
            mView.setDrawingCacheEnabled(true);
            final Bitmap cache = mView.getDrawingCache();
            if (cache != null) {
                if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
                    mBitmap = blur(cache, 25, Color.argb(0x90, 0xe0, 0xf8, 0xff));
                } else {
                    mBitmap = fastBlur(cache, 25, Color.argb(0x90, 0xe0, 0xf8, 0xff));
                }
            }
            mView.setDrawingCacheEnabled(false);
        }

        void setOffset(float offset) {
            mOffset = offset;
            invalidateSelf();
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, mOffset, 0, mPaint);
            }
        }

        @Override
        public void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            mPaint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }

        @RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR1)
        private Bitmap blur(
            final @NonNull Bitmap sentBitmap,
            final int radius,
            final int color) {
            final Bitmap bitmap = Bitmap.createBitmap(sentBitmap.getWidth(), sentBitmap.getHeight(), sentBitmap.getConfig());
            final RenderScript rs = RenderScript.create(mView.getContext());
            final ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            final Allocation input = Allocation.createFromBitmap(rs, sentBitmap);
            final Allocation output = Allocation.createFromBitmap(rs, bitmap);
            blur.setRadius(radius);
            blur.setInput(input);
            blur.forEach(output);
            output.copyTo(bitmap);
            rs.destroy();
            final Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(color);
            return bitmap;
        }

        /**
         * Stack Blur v1.0 from
         * http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
         * Java Author: Mario Klingemann <mario at quasimondo.com>
         * http://incubator.quasimondo.com
         * <p/>
         * created Feburary 29, 2004
         * Android port : Yahel Bouaziz <yahel at kayenko.com>
         * http://www.kayenko.com
         * ported april 5th, 2012
         * <p/>
         * This is a compromise between Gaussian Blur and Box blur
         * It creates much better looking blurs than Box Blur, but is
         * 7x faster than my Gaussian Blur implementation.
         * <p/>
         * I called it Stack Blur because this describes best how this
         * filter works internally: it creates a kind of moving stack
         * of colors whilst scanning through the image. Thereby it
         * just has to add one new block of color to the right side
         * of the stack and remove the leftmost color. The remaining
         * colors on the topmost layer of the stack are either added on
         * or reduced by one, depending on if they are on the right or
         * on the left side of the stack.
         * <p/>
         * If you are using this algorithm in your code please add
         * the following line:
         * Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
         */
        Bitmap fastBlur(
            Bitmap sentBitmap,
            int radius,
            int color) {
            final Bitmap bitmap = Bitmap.createBitmap(sentBitmap);
            if (radius < 1) {
                return (null);
            }
            final int w = bitmap.getWidth();
            final int h = bitmap.getHeight();
            final int[] pix = new int[w * h];
            bitmap.getPixels(pix, 0, w, 0, 0, w, h);
            final int wm = w - 1;
            final int hm = h - 1;
            final int wh = w * h;
            final int div = radius + radius + 1;
            final int[] r = new int[wh];
            final int[] g = new int[wh];
            final int[] b = new int[wh];
            final int[] vmin = new int[Math.max(w, h)];
            int divsum = (div + 1) >> 1;
            divsum *= divsum;
            final int[] dv = new int[256 * divsum];
            for (int i = 0; i < 256 * divsum; i++) {
                dv[i] = (i / divsum);
            }
            int yw = 0;
            int yi = 0;
            final int[][] stack = new int[div][3];
            int stackpointer;
            int stackstart;
            int[] sir;
            int rbs;
            final int r1 = radius + 1;
            int routsum, goutsum, boutsum;
            int rinsum, ginsum, binsum;
            int rsum, gsum, bsum;
            for (int y = 0; y < h; y++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                for (int i = -radius; i <= radius; i++) {
                    final int p = pix[yi + Math.min(wm, Math.max(i, 0))];
                    sir = stack[i + radius];
                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }
                stackpointer = radius;
                for (int x = 0; x < w; x++) {
                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];
                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;
                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];
                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];
                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm);
                    }
                    final int p = pix[yw + vmin[x]];
                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;
                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[(stackpointer) % div];
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];
                    yi++;
                }
                yw += w;
            }
            for (int x = 0; x < w; x++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                int yp = -radius * w;
                for (int i = -radius; i <= radius; i++) {
                    yi = Math.max(0, yp) + x;
                    sir = stack[i + radius];
                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];
                    rbs = r1 - Math.abs(i);
                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                    if (i < hm) {
                        yp += w;
                    }
                }
                yi = x;
                stackpointer = radius;
                for (int y = 0; y < h; y++) {
                    pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];
                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;
                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];
                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];
                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w;
                    }
                    final int p = x + vmin[y];
                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;
                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer];
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];
                    yi += w;
                }
            }
            bitmap.setPixels(pix, 0, w, 0, 0, w, h);
            final Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(color);
            return (bitmap);
        }
    }
}
