package ua.pp.mmwebstudio.instantcmsclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    Elements elements;
    HashMap<LinearLayout, String> links;
    private static final String TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NewsParseTask parseTask = new NewsParseTask();
        parseTask.execute();
    }

    class NewsParseTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Document newsDoc = null;
            try {
                newsDoc = Jsoup.connect("https://instantcms.ru/novosti").get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            elements = newsDoc.select("#main div.contentlist>h3.con_title, #main div.contentlist>div.con_desc, #main div.contentlist>div.con_details");
            return null;
        }

        @Override
        protected void onPostExecute(Void Result) {
            super.onPostExecute(Result);
            setNewsResult();
        }
    }

    public void setNewsResult()
    {
        LinearLayout newsList = (LinearLayout) findViewById(R.id.newsList);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lParams.setMargins(0, 0, 0, 10);
        LinearLayout article = new LinearLayout(this);
        article.setOrientation(LinearLayout.VERTICAL);
        article.setLayoutParams(lParams);
        links = new HashMap<LinearLayout, String>();
        for (Element el : elements)
        {
            if(el.hasClass("con_title"))
            {
                TextView title = new TextView(this);
                title.setTextSize(18);
                title.setTextColor(getResources().getColor(R.color.colorNewsTitle));
                Element TitleLinkA = el.select(":root>a").first();
                links.put(article, TitleLinkA.attr("href"));
                article.setOnClickListener(clickF);
                title.setText(TitleLinkA.text());
                article.addView(title);
            }
            if(el.hasClass("con_desc"))
            {
                TextView descr = new TextView(this);
                descr.setText("");
                Elements descrEl = el.select(":root>p");
                if(descrEl != null && !descrEl.isEmpty())
                {
                    descr.setText(descrEl.first().text());
                }
                article.addView(descr);
            }
            if(el.hasClass("con_details"))
            {
                TextView pubDate = new TextView(this);
                pubDate.setTextSize(12);
                pubDate.setTextColor(getResources().getColor(R.color.colorDate));
                el.select(":root>time>i").remove();
                pubDate.setText(el.select(":root>time").text());
                article.addView(pubDate);
                newsList.addView(article);
                article = new LinearLayout(this);
                article.setOrientation(LinearLayout.VERTICAL);
                article.setLayoutParams(lParams);
            }
        }
    }

    private View.OnClickListener clickF = new View.OnClickListener() {
        @Override
        public void onClick(View article) {
            Intent OpenLinkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://instantcms.ru" + links.get(article)));
            OpenLinkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(OpenLinkIntent);
        }
    };
}
