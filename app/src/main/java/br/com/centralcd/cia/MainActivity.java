package br.com.centralcd.cia;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import android.webkit.URLUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 9001;
    private static final String PREFS = "central_cd_mobile";
    private static final String PREF_URL = "central_url";
    private static final String PREF_LAST_UPDATE_CHECK = "last_native_update_check";
    private static final long UPDATE_CHECK_INTERVAL_MS = 6L * 60L * 60L * 1000L;
    private static final String UPDATE_API_URL = "https://api.github.com/repos/pcecentraldedistribuicao-svg/central-cd-android/releases/latest";

    private WebView webView;
    private ProgressBar progressBar;
    private ValueCallback<Uri[]> filePathCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        configureWebView();
        loadCentral();
        checkForNativeUpdate(false);
    }

    private void configureWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setSupportMultipleWindows(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setUserAgentString(s.getUserAgentString() + " CentralCDAndroid/1.0.3");

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleNavigation(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleNavigation(Uri.parse(url));
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                try {
                    Intent intent = params.createIntent();
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                    return true;
                } catch (ActivityNotFoundException e) {
                    filePathCallback = null;
                    Toast.makeText(MainActivity.this, "Não foi possível abrir o seletor de arquivos.", Toast.LENGTH_LONG).show();
                    return false;
                }
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
                WebView temp = new WebView(MainActivity.this);
                temp.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest request) {
                        openExternal(request.getUrl());
                        v.destroy();
                        return true;
                    }
                    @Override
                    public void onPageStarted(WebView v, String url, android.graphics.Bitmap favicon) {
                        if (url != null && !"about:blank".equals(url)) {
                            openExternal(Uri.parse(url));
                            v.stopLoading();
                            v.destroy();
                        }
                    }
                });
                android.webkit.WebView.WebViewTransport transport = (android.webkit.WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(temp);
                resultMsg.sendToTarget();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                    .setMessage(message)
                    .setPositiveButton("OK", (d, w) -> result.confirm())
                    .setNegativeButton("Cancelar", (d, w) -> result.cancel())
                    .setOnCancelListener(d -> result.cancel())
                    .show();
                return true;
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.addRequestHeader("User-Agent", userAgent);
                request.setMimeType(mimetype);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
                request.setTitle(filename);
                request.setDescription("Download pela Central CD 10&CIA");
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                ((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(request);
                Toast.makeText(this, "Download iniciado.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                openExternal(Uri.parse(url));
            }
        });
    }

    private boolean handleNavigation(Uri uri) {
        if (uri == null) return false;
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
        if ("mailto".equals(scheme) || "tel".equals(scheme) || "intent".equals(scheme)) {
            openExternal(uri); return true;
        }
        // Sistemas internos HTTP e links solicitados em nova aba saem no navegador do aparelho.
        if ("http".equals(scheme) || "10.180.191.200".equals(host)) {
            openExternal(uri); return true;
        }
        return false;
    }

    private void openExternal(Uri uri) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
        catch (Exception e) { Toast.makeText(this, "Não foi possível abrir o endereço.", Toast.LENGTH_LONG).show(); }
    }

    private void loadCentral() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String url = prefs.getString(PREF_URL, BuildConfig.CENTRAL_URL);
        if (url == null || url.trim().isEmpty() || url.contains("COLE_AQUI_O_LINK_EXEC_DA_CENTRAL")) {
            requestCentralUrl();
        } else {
            webView.loadUrl(url);
        }
    }

    private void requestCentralUrl() {
        final EditText input = new EditText(this);
        input.setHint("https://script.google.com/macros/s/.../exec");
        input.setSingleLine(true);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Configurar Central CD")
            .setMessage("Informe uma única vez o link /exec publicado da Central CD 10&CIA.")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Sair", (d, w) -> finish())
            .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String value = input.getText().toString().trim();
                if (!value.startsWith("https://") || !value.contains("/exec")) {
                    input.setError("Informe o link HTTPS terminado em /exec.");
                    return;
                }
                getSharedPreferences(PREFS, MODE_PRIVATE)
                    .edit()
                    .putString(PREF_URL, value)
                    .apply();
                dialog.dismiss();
                webView.loadUrl(value);
            });
        });

        dialog.show();
    }

    private void checkForNativeUpdate(boolean force) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        long now = System.currentTimeMillis();
        long last = prefs.getLong(PREF_LAST_UPDATE_CHECK, 0L);
        if (!force && now - last < UPDATE_CHECK_INTERVAL_MS) return;
        prefs.edit().putLong(PREF_LAST_UPDATE_CHECK, now).apply();

        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL api = new URL(UPDATE_API_URL);
                connection = (HttpURLConnection) api.openConnection();
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setRequestProperty("User-Agent", "CentralCDAndroid/" + BuildConfig.VERSION_NAME);

                if (connection.getResponseCode() != 200) return;

                StringBuilder body = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) body.append(line);
                }

                JSONObject release = new JSONObject(body.toString());
                String latest = release.optString("tag_name", "").replaceFirst("^[vV]", "");
                if (latest.isEmpty() || !isNewerVersion(latest, BuildConfig.VERSION_NAME)) return;

                String downloadUrl = "";
                JSONArray assets = release.optJSONArray("assets");
                if (assets != null) {
                    for (int i = 0; i < assets.length(); i++) {
                        JSONObject asset = assets.optJSONObject(i);
                        if (asset == null) continue;
                        String name = asset.optString("name", "").toLowerCase();
                        if (name.endsWith(".apk")) {
                            downloadUrl = asset.optString("browser_download_url", "");
                            break;
                        }
                    }
                }
                if (downloadUrl.isEmpty()) downloadUrl = release.optString("html_url", "");
                if (downloadUrl.isEmpty()) return;

                final String finalLatest = latest;
                final String finalDownloadUrl = downloadUrl;
                runOnUiThread(() -> showNativeUpdateDialog(finalLatest, finalDownloadUrl));
            } catch (Exception ignored) {
                // Atualização nativa é complementar; falhas não impedem o uso da Central web.
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private void showNativeUpdateDialog(String latestVersion, String downloadUrl) {
        if (isFinishing() || isDestroyed()) return;
        new AlertDialog.Builder(this)
            .setTitle("Nova versão do aplicativo")
            .setMessage("Central CD Android " + latestVersion + " está disponível.\n\n" +
                "Versão instalada: " + BuildConfig.VERSION_NAME +
                "\n\nAs atualizações normais da Central web continuam automáticas; este aviso aparece apenas quando o aplicativo Android também foi atualizado.")
            .setPositiveButton("Atualizar agora", (dialog, which) -> openExternal(Uri.parse(downloadUrl)))
            .setNegativeButton("Depois", null)
            .show();
    }

    private boolean isNewerVersion(String latest, String current) {
        try {
            String[] a = latest.split("\\.");
            String[] b = current.split("\\.");
            int max = Math.max(a.length, b.length);
            for (int i = 0; i < max; i++) {
                int av = i < a.length ? Integer.parseInt(a[i].replaceAll("[^0-9].*$", "")) : 0;
                int bv = i < b.length ? Integer.parseInt(b[i].replaceAll("[^0-9].*$", "")) : 0;
                if (av != bv) return av > bv;
            }
        } catch (Exception ignored) {}
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST) {
            Uri[] results = null;
            if (resultCode == RESULT_OK && data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    results = new Uri[count];
                    for (int i=0;i<count;i++) results[i] = data.getClipData().getItemAt(i).getUri();
                } else if (data.getData() != null) results = new Uri[]{data.getData()};
            }
            if (filePathCallback != null) filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
