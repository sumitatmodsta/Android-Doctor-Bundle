package com.modastadoc.doctors.notify;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.modastadoc.doctors.App;
import com.modastadoc.doctors.BuildConfig;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.utils.AppCoreUtil;
import com.modastadoc.doctors.database.LocalDataManager;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;

/**
 * Created by kunasi on 14/12/17.
 */

public class AppUpdate {
    private static final String POPUP_SHOWN_DATE = "popup_shown_date";
    private static final long THRESHOLD =    24 * 60 * 60 * 1000L;

    private static AppUpdate instance;
    private static Context mContext;

    public static AppUpdate getInstance(Context context) {
        mContext = context;
        if(instance == null) {
            instance = new AppUpdate();
        }

        return instance;
    }

    public void checkAppUpdate() {
        if(isEligibleToCheckAppUpdate()) {
            new AppUpdateTask().execute();
        }
    }

    private boolean isEligibleToCheckAppUpdate() {
        long lastDate = LocalDataManager.getInstance().get(POPUP_SHOWN_DATE, 0L);

        return new Date().getTime() - lastDate >= THRESHOLD;
    }

    private void checkAppUpdate(String[] response) {
        try {
            if (response != null) {
                if(!AppCoreUtil.isEmpty(response[0]) && !AppCoreUtil.isEmpty(response[1])) {
                    if(response[0].matches("[0-9.]*")) {
                        if (!BuildConfig.VERSION_NAME.equalsIgnoreCase(response[0])) {
                            openUpdatePopup(response[1]);
                        }
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openUpdatePopup(String changes) {
        if(mContext != null) {
            LocalDataManager.getInstance().set(POPUP_SHOWN_DATE, new Date().getTime());
            new AlertDialog.Builder(mContext)
                    .setTitle("Update Available!")
                    .setMessage(changes)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id=" +
                                            App.getAppContext().getPackageName()));
                            mContext.startActivity(intent);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {}
                    })
                    .setIcon(R.drawable.appicon)
                    .show();
        }
    }

    private class AppUpdateTask extends AsyncTask<Void, Void, String[]> {
        @Override
        protected String[] doInBackground(Void... voids) {
            String url = "https://play.google.com/store/apps/details?id=com.modastadoc.doctors&hl=en";

            return getVersionInfo(url);
        }

        @Override
        protected void onPostExecute(String[] s) {
            super.onPostExecute(s);
            checkAppUpdate(s);
        }
    }

    private String[] getVersionInfo(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            String version = doc.getElementsContainingOwnText("Current Version").parents().first()
                    .getAllElements().last().text();
            List<Node> nodes = doc.getElementsContainingOwnText("What's New").parents().first()
                    .parent().child(1).child(0).child(0).childNodes();
            String changes = "";
            for(Node n : nodes) {
                if(!(n instanceof Element)) {
                    changes += n.outerHtml().trim() + "\n";
                }
            }

            return new String[]{version, changes};
        }catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String[] getAppVersionInfo(String playUrl) {
        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties props = cleaner.getProperties();
        props.setAllowHtmlInsideAttributes(true);
        props.setAllowMultiWordAttributes(true);
        props.setRecognizeUnicodeChars(true);
        props.setOmitComments(true);

        try {
            URL url = new URL(playUrl);
            URLConnection conn = url.openConnection();
            TagNode node = cleaner.clean(new InputStreamReader(conn.getInputStream()));
            Object[] new_nodes = node.evaluateXPath("//*[@class='recent-change']");
            Object[] version_nodes = node.evaluateXPath("//*[@itemprop='softwareVersion']");

            String version = "", whatsNew = "";
            for (Object new_node : new_nodes) {
                TagNode info_node = (TagNode) new_node;
                whatsNew += info_node.getAllChildren().get(0).toString().trim() + "\n";
            }

            if (version_nodes.length > 0) {
                TagNode ver = (TagNode) version_nodes[0];
                version = ver.getAllChildren().get(0).toString().trim();
            }

            return new String[]{version, whatsNew};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
