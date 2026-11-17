package com.jungcle.appropin;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/** ApproPin (나만의 숏게임 거리표) — 앱 안에 넣은 웹 화면(assets/index.html)을 띄우고, 사진 저장·공유·인쇄만 안드로이드 기능으로 연결해요. */
public class MainActivity extends Activity {

    private static final String START_URL = "file:///android_asset/index.html";
    private static final String PICTURES_FOLDER = "ApproPin";
    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // 거리표·메모·보관함 저장(localStorage)
        s.setSupportMultipleWindows(false);    // target=_blank 링크도 아래에서 가로채서 외부 앱으로
        s.setTextZoom(100);                    // 휴대폰 글꼴 크기 설정 때문에 표가 깨지지 않게

        web.addJavascriptInterface(new Bridge(), "ShortGameApp");
        web.setWebChromeClient(new WebChromeClient());   // confirm() 확인 창
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("file".equals(uri.getScheme())) return false;   // 앱 안 화면
                openExternal(uri);                                  // 인스타·유튜브·웹사이트는 해당 앱/브라우저로
                return true;
            }
        });

        if (savedInstanceState != null) web.restoreState(savedInstanceState);
        else web.loadUrl(START_URL);
    }

    /** 인스타·유튜브·토스 등 바깥 링크는 해당 앱(없으면 브라우저)으로 열어요. intent:// 링크도 처리해요. */
    private void openExternal(Uri uri) {
        try {
            if ("intent".equals(uri.getScheme())) {
                Intent it = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME);
                it.addCategory(Intent.CATEGORY_BROWSABLE);
                it.setComponent(null);
                it.setSelector(null);
                try {
                    startActivity(it);
                } catch (Exception notInstalled) {
                    String fallback = it.getStringExtra("browser_fallback_url");
                    if (fallback != null) startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fallback)));
                    else if (it.getPackage() != null)
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + it.getPackage())));
                    else throw notInstalled;
                }
                return;
            }
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (Exception notInstalled) {
                if (!"supertoss".equals(uri.getScheme())) throw notInstalled;
                // 토스 앱이 없으면 안내 후 플레이스토어의 토스로
                Toast.makeText(this, "토스 앱이 없어요. 계좌번호로 보내 주셔도 돼요", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=viva.republica.toss")));
            }
        } catch (Exception e) {
            Toast.makeText(this, "링크를 열 수 없어요", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (web != null) web.destroy();
        super.onDestroy();
    }

    // ---------- 웹 화면에서 부르는 기능 (window.ShortGameApp) ----------

    private static byte[] decodeDataUrl(String dataUrl) {
        int comma = dataUrl.indexOf(',');
        return Base64.decode(comma >= 0 ? dataUrl.substring(comma + 1) : dataUrl, Base64.DEFAULT);
    }

    private static String safeName(String name) {
        String n = (name == null || name.trim().isEmpty()) ? "short-game-chart.png" : name.replaceAll("[\\\\/:*?\"<>|]", "_");
        return n.toLowerCase().endsWith(".png") ? n : n + ".png";
    }

    private class Bridge {

        /** 갤러리(Pictures/ApproPin)에 PNG 저장. 안드로이드 10 이상은 권한이 필요 없어요. */
        @JavascriptInterface
        public boolean saveImage(String dataUrl, String name) {
            Uri uri = null;
            ContentResolver cr = getContentResolver();
            try {
                byte[] bytes = decodeDataUrl(dataUrl);
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Images.Media.DISPLAY_NAME, safeName(name));
                cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                cv.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + PICTURES_FOLDER);
                cv.put(MediaStore.Images.Media.IS_PENDING, 1);
                uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
                if (uri == null) return false;
                try (OutputStream os = cr.openOutputStream(uri)) {
                    if (os == null) throw new IllegalStateException("no stream");
                    os.write(bytes);
                }
                cv.clear();
                cv.put(MediaStore.Images.Media.IS_PENDING, 0);
                cr.update(uri, cv, null, null);
                return true;
            } catch (Exception e) {
                if (uri != null) try { cr.delete(uri, null, null); } catch (Exception ignored) { }
                return false;
            }
        }

        /** 이미지를 카톡·인스타 등으로 공유 */
        @JavascriptInterface
        public void shareImage(String dataUrl, String name) {
            try {
                File dir = new File(getCacheDir(), ShareProvider.DIR);
                if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("mkdir");
                File f = new File(dir, safeName(name));
                try (FileOutputStream os = new FileOutputStream(f)) { os.write(decodeDataUrl(dataUrl)); }
                Uri uri = ShareProvider.uriFor(f.getName());
                Intent send = new Intent(Intent.ACTION_SEND)
                        .setType("image/png")
                        .putExtra(Intent.EXTRA_STREAM, uri)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Intent chooser = Intent.createChooser(send, "거리표 공유");
                chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                runOnUiThread(() -> startActivity(chooser));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "공유하지 못했어요", Toast.LENGTH_SHORT).show());
            }
        }

        /** 후원하기(토스) 등 바깥 링크 열기 */
        @JavascriptInterface
        public void openUrl(String url) {
            if (url == null || url.isEmpty()) return;
            runOnUiThread(() -> openExternal(Uri.parse(url)));
        }

        /** 계좌번호 복사 */
        @JavascriptInterface
        public void copyText(String text) {
            runOnUiThread(() -> {
                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("account", text));
            });
        }

        /** 거리표 링크 공유 */
        @JavascriptInterface
        public void shareText(String text) {
            Intent send = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text);
            runOnUiThread(() -> startActivity(Intent.createChooser(send, "거리표 링크 공유")));
        }

        /** 인쇄 / PDF로 저장 (포켓 카드) */
        @JavascriptInterface
        public void print(String jobName) {
            runOnUiThread(() -> {
                PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
                String n = (jobName == null || jobName.isEmpty()) ? "ApproPin" : jobName;
                pm.print(n, web.createPrintDocumentAdapter(n), new PrintAttributes.Builder().build());
            });
        }
    }
}
