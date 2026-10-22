package com.xg.bookforge;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.util.Base64;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
    private static final int EXPORT_REQUEST = 901;
    private static final int IMPORT_REQUEST = 902;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private WebView editor;
    private WebView printer;
    private FrameLayout root;
    private ValueCallback<Uri[]> importCallback;
    private byte[] exportData;
    private final Object exportLock = new Object();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().setStatusBarColor(Color.rgb(21,20,33));
        getWindow().setNavigationBarColor(Color.rgb(21,20,33));
        root = new FrameLayout(this);
        if (Build.VERSION.SDK_INT >= 35) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                Insets safe = insets.getInsets(WindowInsets.Type.statusBars()
                    | WindowInsets.Type.navigationBars() | WindowInsets.Type.displayCutout());
                view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
                return insets;
            });
        }
        editor = new WebView(this);
        editor.setBackgroundColor(Color.rgb(15,16,25));
        WebSettings settings = editor.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setSupportZoom(false);
        settings.setTextZoom(100);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        editor.addJavascriptInterface(new Bridge(), "AndroidBridge");
        editor.setWebViewClient(new WebViewClient());
        editor.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if(importCallback != null) importCallback.onReceiveValue(null);
                importCallback = callback;
                try {
                    startActivityForResult(params.createIntent(), IMPORT_REQUEST);
                    return true;
                } catch(Exception error) {
                    importCallback = null;
                    callback.onReceiveValue(null);
                    return false;
                }
            }
        });
        root.addView(editor, new FrameLayout.LayoutParams(-1,-1));
        setContentView(root);
        editor.loadUrl("file:///android_asset/index.html");
    }

    private final class Bridge {
        @JavascriptInterface public String readLibrary() {
            return getSharedPreferences("bookforge", MODE_PRIVATE).getString("library", "");
        }
        @JavascriptInterface public void writeLibrary(String json) {
            if(json != null && json.length() < 15000000)
                getSharedPreferences("bookforge", MODE_PRIVATE).edit().putString("library", json).apply();
        }
        @JavascriptInterface public void saveFile(String filename, String mime, String base64) {
            try {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                synchronized(exportLock) { exportData = bytes; }
                runOnUiThread(() -> {
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.setType(mime);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.putExtra(Intent.EXTRA_TITLE, filename);
                    try { startActivityForResult(intent, EXPORT_REQUEST); }
                    catch(Exception e) {
                        Toast.makeText(MainActivity.this, "Správce souborů není dostupný.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch(Exception error) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Export se nepodařil.", Toast.LENGTH_SHORT).show());
            }
        }
        @JavascriptInterface public void printBook(String encodedHtml, String title, String size) {
            try {
                String printable = new String(Base64.decode(encodedHtml, Base64.DEFAULT), StandardCharsets.UTF_8);
                runOnUiThread(() -> printHtml(printable,title,size));
            } catch(Exception error) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Tisk se nepodařil.", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private PrintAttributes.MediaSize parseMediaSize(String size) {
        try {
            String[] sides=size.split("x");
            if(sides.length==2) {
                float width=Float.parseFloat(sides[0]),height=Float.parseFloat(sides[1]);
                if(width>=50&&width<=500&&height>=50&&height<=500)
                    return new PrintAttributes.MediaSize("bookforge-"+Math.round(width)+"x"+Math.round(height),
                        "BookForge",Math.round(width*1000/25.4f),Math.round(height*1000/25.4f));
            }
        }catch(Exception ignored){}
        return PrintAttributes.MediaSize.ISO_A5;
    }

    private void printHtml(String html, String title, String size) {
        if(printer != null) {root.removeView(printer);printer.destroy();printer=null;}
        WebView p = new WebView(this);
        printer = p;
        p.getSettings().setDefaultTextEncodingName("UTF-8");
        p.setBackgroundColor(Color.WHITE);
        p.setAlpha(0.01f);
        root.addView(p, new FrameLayout.LayoutParams(1,1));
        p.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                PrintManager manager = (PrintManager)getSystemService(PRINT_SERVICE);
                PrintAttributes attrs = new PrintAttributes.Builder()
                    .setMediaSize(parseMediaSize(size))
                    .setResolution(new PrintAttributes.Resolution("BookForge","BookForge",300,300))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build();
                manager.print("BookForge - "+title, p.createPrintDocumentAdapter(title), attrs);
            }
        });
        p.loadDataWithBaseURL("https://bookforge.invalid/",html,"text/html","UTF-8",null);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == IMPORT_REQUEST) {
            if(importCallback != null) {
                importCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode,data));
                importCallback = null;
            }
            return;
        }
        if(requestCode == EXPORT_REQUEST) {
            if(resultCode != RESULT_OK || data == null || data.getData() == null) return;
            final Uri uri = data.getData();
            final byte[] bytes;
            synchronized(exportLock) {bytes=exportData;exportData=null;}
            if(bytes == null) return;
            io.execute(() -> {
                try(OutputStream output = getContentResolver().openOutputStream(uri)) {
                    if(output==null) throw new IllegalStateException("Cannot open file");
                    output.write(bytes);
                    runOnUiThread(() -> Toast.makeText(this,"Kniha uložena.",Toast.LENGTH_SHORT).show());
                } catch(Exception error) {
                    runOnUiThread(() -> Toast.makeText(this,"Chyba při ukládání.",Toast.LENGTH_LONG).show());
                }
            });
        }
    }
    @Override public void onBackPressed() {
        editor.evaluateJavascript("(function(){let m=document.getElementById('overlay');if(m&&m.classList.contains('visible')){m.classList.remove('visible');return true;}let s=document.getElementById('sidebar');if(s&&s.classList.contains('open')){s.classList.remove('open');return true;}return false;})()", result -> {
            if(!"true".equals(result)) MainActivity.super.onBackPressed();
        });
    }
    @Override protected void onDestroy() {
        if(printer!=null) {root.removeView(printer);printer.destroy();}
        if(editor!=null) {root.removeView(editor);editor.destroy();}
        io.shutdown();
        super.onDestroy();
    }
}
