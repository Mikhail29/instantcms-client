package ua.pp.mmwebstudio.instantcmsclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;

public class MainActivity extends AppCompatActivity {
    private Elements elements, pages_elements;
    private HashMap<LinearLayout, String> links;
    private HashMap<Button, String> pages;
    private static final String TAG = "myLogs";
    private Boolean is_network_error = false;
    private String error_message = "";
    private String newsLink = "https://instantcms.ru/novosti";
    int pageNum = 1;
    private Boolean is_autorized = false;
    SharedPreferences Pref;
    HashMap<String, String> cookies;
    final String pref_name_file = "ICMSClSettings";
    final String autorized_param = "is_autorized";
    final String cookies_params = "cookies";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button retry = (Button) findViewById(R.id.retry);
        Button login = (Button) findViewById(R.id.loginButton);
        Pref = getSharedPreferences(pref_name_file, MODE_PRIVATE);
        cookies = new HashMap<String, String>();
        ImageButton messageButton = (ImageButton) findViewById(R.id.messagesButton);
        messageButton.setOnClickListener(messagesClick);
        login.setOnClickListener(loginClick);
        if(Pref.getString(autorized_param, "0").equals("1"))
        {
            is_autorized = true;
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            String cookie_string = Pref.getString(cookies_params, "");
            if(!cookie_string.equals("")) {
                cookies.putAll((Map)gson.fromJson(cookie_string, type));
                login.setText(R.string.app_logout);
            }
        }
        else
        {
            is_autorized = false;
            messageButton.setOnClickListener(loginClick);
        }
        retry.setOnClickListener(retryClick);
        links = new HashMap<LinearLayout, String>();
        pages = new HashMap<Button, String>();
        NewsParseTask parseTask = new NewsParseTask();
        parseTask.execute();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Button login = (Button) findViewById(R.id.loginButton);
        Pref = getSharedPreferences(pref_name_file, MODE_PRIVATE);
        login.setEnabled(true);
        if(Pref.getString(autorized_param, "0").equals("1"))
        {
            is_autorized = true;
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            String cookie_string = Pref.getString(cookies_params, "");
            if(!cookie_string.equals("")) {
                cookies.clear();
                cookies.putAll((Map)gson.fromJson(cookie_string, type));
                login.setText(R.string.app_logout);
            }
        }
        else
        {
            is_autorized = false;
            login.setOnClickListener(loginClick);
        }
        LinearLayout content = (LinearLayout) findViewById(R.id.contentCOntainer);
        RelativeLayout progressBarContainer = (RelativeLayout) findViewById(R.id.progress_bar_container);
        RelativeLayout errorContainer = (RelativeLayout) findViewById(R.id.errorContainer);
        content.setVisibility(View.GONE);
        errorContainer.setVisibility(RelativeLayout.GONE);
        progressBarContainer.setVisibility(RelativeLayout.VISIBLE);
        NewsParseTask parseTask = new NewsParseTask();
        parseTask.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Button login = (Button) findViewById(R.id.loginButton);
        Pref = getSharedPreferences(pref_name_file, MODE_PRIVATE);
        login.setEnabled(true);
        if(Pref.getString(autorized_param, "0").equals("1"))
        {
            is_autorized = true;
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            String cookie_string = Pref.getString(cookies_params, "");
            if(!cookie_string.equals("")) {
                cookies.clear();
                cookies.putAll((Map)gson.fromJson(cookie_string, type));
                login.setText(R.string.app_logout);
            }
        }
        else
        {
            is_autorized = false;
            login.setOnClickListener(loginClick);
        }
    }

    class NewsParseTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Document newsDoc = null;
            try {
                if(cookies.isEmpty())
                {
                    newsDoc = Jsoup.connect(newsLink).get();
                }
                else {
                    newsDoc = Jsoup.connect(newsLink).cookies(cookies).get();
                }
                elements = newsDoc.select("#main div.contentlist>h3.con_title, #main div.contentlist>div.con_desc, #main div.contentlist>div.con_details");
                pages_elements = newsDoc.select("#main div.component div.pagebar>span.pagebar_current, #main div.component div.pagebar>a.pagebar_page");
                is_network_error = false;
            } catch (IOException e) {
                is_network_error = true;
                error_message = e.getLocalizedMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void Result) {
            super.onPostExecute(Result);
            if(is_network_error)
            {
                showError();
            }
            else {
                setNewsResult();
            }
        }
    }

    class ExitTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Jsoup.connect("https://instantcms.ru/logout").cookies(cookies).get();
                is_network_error = false;
            } catch (IOException e) {
                is_network_error = true;
                error_message = e.getLocalizedMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void Result) {
            super.onPostExecute(Result);
            if(is_network_error)
            {
                Toast toast = Toast.makeText(getApplicationContext(),
                        error_message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            else {
                Pref = getSharedPreferences(pref_name_file, MODE_PRIVATE);
                SharedPreferences.Editor ed = Pref.edit();
                ed.putString(cookies_params, "");
                ed.putString(autorized_param, "0");
                ed.apply();
                Button login = (Button) findViewById(R.id.loginButton);
                login.setText(R.string.app_login);
                cookies.clear();
                is_autorized = false;
            }
            RelativeLayout exitLayout = (RelativeLayout) findViewById(R.id.loadExit);
            exitLayout.setVisibility(View.GONE);
        }
    }

    private void setNewsResult()
    {
        LinearLayout paginator = (LinearLayout) findViewById(R.id.paginator);
        LinearLayout newsList = (LinearLayout) findViewById(R.id.newsList);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lParams.setMargins(0, 0, 0, 10);
        LinearLayout article = new LinearLayout(this);
        article.setOrientation(LinearLayout.VERTICAL);
        article.setLayoutParams(lParams);
        newsList.removeAllViews();
        paginator.removeAllViews();
        links.clear();
        pages.clear();
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
        for(Element page : pages_elements)
        {
            if(page.hasClass("pagebar_current"))
            {
                Button pageButton = new Button(this);
                pageButton.setText(page.text());
                pageButton.setEnabled(false);
                pageButton.setMinimumWidth(60);
                pageButton.setMinWidth(60);
                pageButton.setMinimumHeight(100);
                pageButton.setMinHeight(100);
                pageButton.setBackgroundColor(getResources().getColor(R.color.colorNewsTitle));
                pageButton.setTextColor(getResources().getColor(R.color.colorTextCurrentPage));
                paginator.addView(pageButton);
            }
            if(page.hasClass("pagebar_page") && !page.text().equals("Первая") && !page.text().equals("Последняя"))
            {
                if(!page.text().equals("Предыдущая") && !page.text().equals("Следующая") && ((Integer.parseInt(page.text()) >=  pageNum - 2 && Integer.parseInt(page.text()) < pageNum) || (Integer.parseInt(page.text()) <=  pageNum + 2 && Integer.parseInt(page.text()) > pageNum))) {
                    Button pageButton = new Button(this);
                    pageButton.setText(page.text());
                    pageButton.setMinimumWidth(60);
                    pageButton.setMinWidth(60);
                    pageButton.setMinimumHeight(100);
                    pageButton.setMinHeight(100);
                    pages.put(pageButton, "https://instantcms.ru" + page.attr("href"));
                    pageButton.setBackgroundColor(getResources().getColor(R.color.colorTextCurrentPage));
                    pageButton.setTextColor(getResources().getColor(R.color.colorNewsTitle));
                    pageButton.setOnClickListener(changePage);
                    paginator.addView(pageButton);
                }
                if(page.text().equals("Предыдущая") || page.text().equals("Следующая")) {
                    Button pageButton = new Button(this);
                    pageButton.setText(page.text());
                    pageButton.setMinimumWidth(60);
                    pageButton.setMinWidth(60);
                    pageButton.setMinimumHeight(100);
                    pageButton.setMinHeight(100);
                    if (page.text().equals("Предыдущая")) {
                        pageButton.setText("<");
                    }
                    if (page.text().equals("Следующая")) {
                        pageButton.setText(">");
                    }
                    pages.put(pageButton, "https://instantcms.ru" + page.attr("href"));
                    pageButton.setBackgroundColor(getResources().getColor(R.color.colorTextCurrentPage));
                    pageButton.setTextColor(getResources().getColor(R.color.colorNewsTitle));
                    pageButton.setOnClickListener(changePage);
                    paginator.addView(pageButton);
                }
            }
        }
        RelativeLayout progressBarContainer = (RelativeLayout) findViewById(R.id.progress_bar_container);
        RelativeLayout errorContainer = (RelativeLayout) findViewById(R.id.errorContainer);
        LinearLayout content = (LinearLayout) findViewById(R.id.contentCOntainer);
        progressBarContainer.setVisibility(RelativeLayout.GONE);
        errorContainer.setVisibility(RelativeLayout.GONE);
        content.setVisibility(View.VISIBLE);
    }

    private void showError()
    {
        LinearLayout content = (LinearLayout) findViewById(R.id.contentCOntainer);
        RelativeLayout progressBarContainer = (RelativeLayout) findViewById(R.id.progress_bar_container);
        RelativeLayout errorContainer = (RelativeLayout) findViewById(R.id.errorContainer);
        TextView errorMessage = (TextView) findViewById(R.id.errorMessage);
        errorMessage.setText(error_message);
        progressBarContainer.setVisibility(RelativeLayout.GONE);
        content.setVisibility(View.GONE);
        errorContainer.setVisibility(RelativeLayout.VISIBLE);
    }

    private View.OnClickListener clickF = new View.OnClickListener() {
        @Override
        public void onClick(View article) {
            Intent OpenLinkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://instantcms.ru" + links.get(article)));
            OpenLinkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(OpenLinkIntent);
        }
    };

    private View.OnClickListener retryClick = new View.OnClickListener() {
        @Override
        public void onClick(View article) {
            LinearLayout content = (LinearLayout) findViewById(R.id.contentCOntainer);
            RelativeLayout progressBarContainer = (RelativeLayout) findViewById(R.id.progress_bar_container);
            RelativeLayout errorContainer = (RelativeLayout) findViewById(R.id.errorContainer);
            content.setVisibility(View.GONE);
            errorContainer.setVisibility(RelativeLayout.GONE);
            progressBarContainer.setVisibility(RelativeLayout.VISIBLE);
            NewsParseTask parseTask = new NewsParseTask();
            parseTask.execute();
        }
    };

    private View.OnClickListener loginClick = new View.OnClickListener() {
        @Override
        public void onClick(View article) {
            if(!is_autorized) {
                Button login = (Button) findViewById(R.id.loginButton);
                login.setEnabled(false);
                Intent intent = new Intent(MainActivity.this.getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
            else {
                RelativeLayout exitLayout = (RelativeLayout) findViewById(R.id.loadExit);
                exitLayout.setVisibility(View.VISIBLE);
                ExitTask exit = new ExitTask();
                exit.execute();
            }
        }
    };

    private View.OnClickListener messagesClick = new View.OnClickListener() {
        @Override
        public void onClick(View article) {
            Intent intent = new Intent(MainActivity.this.getApplicationContext(), MessagesActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener changePage = new View.OnClickListener() {
        @Override
        public void onClick(View page) {
            newsLink = pages.get(page);
            if(((Button) page).getText().toString().trim().matches("^[0-9]*$")) {
                pageNum = Integer.parseInt(((Button) page).getText().toString());
            }
            else {
                if(((Button) page).getText().toString().equals(">"))
                {
                    pageNum++;
                }
                else {
                    pageNum--;
                }
            }
            LinearLayout content = (LinearLayout) findViewById(R.id.contentCOntainer);
            ScrollView newsScroll = (ScrollView) findViewById(R.id.newsScroll);
            RelativeLayout progressBarContainer = (RelativeLayout) findViewById(R.id.progress_bar_container);
            RelativeLayout errorContainer = (RelativeLayout) findViewById(R.id.errorContainer);
            content.setVisibility(View.GONE);
            errorContainer.setVisibility(RelativeLayout.GONE);
            progressBarContainer.setVisibility(RelativeLayout.VISIBLE);
            NewsParseTask parseTask = new NewsParseTask();
            parseTask.execute();
        }
    };
}
