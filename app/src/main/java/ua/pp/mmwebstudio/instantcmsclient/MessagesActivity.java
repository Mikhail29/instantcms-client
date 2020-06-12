package ua.pp.mmwebstudio.instantcmsclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

public class MessagesActivity extends AppCompatActivity {
    private Boolean is_network_error = false;
    private String error_message = "", mLink = "";
    int pageNum = 1;
    private Boolean is_autorized = false;
    SharedPreferences Pref;
    HashMap<String, String> cookies;
    final String pref_name_file = "ICMSClSettings";
    final String autorized_param = "is_autorized";
    final String cookies_params = "cookies";
    private HashMap<Button, String> pages;
    private Elements message_elements, pages_elements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        ImageButton backButton = (ImageButton) findViewById(R.id.messageBackButton);
        backButton.setOnClickListener(backClick);
        Pref = getSharedPreferences(pref_name_file, MODE_PRIVATE);
        cookies = new HashMap<String, String>();
        Button retry = (Button) findViewById(R.id.mretry);
        retry.setOnClickListener(retryClick);
        if(Pref.getString(autorized_param, "0").equals("1"))
        {
            is_autorized = true;
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            String cookie_string = Pref.getString(cookies_params, "");
            if(!cookie_string.equals("")) {
                cookies.putAll((Map)gson.fromJson(cookie_string, type));
            }
        }
        else
        {
            is_autorized = false;
        }
        pages = new HashMap<Button, String>();
        SwipeRefreshLayout content = (SwipeRefreshLayout) findViewById(R.id.messageContentContainer);
        content.setOnRefreshListener(swipeRefreshMessages);
        content.setColorSchemeResources
                (R.color.light_blue, R.color.middle_blue,R.color.deep_blue);
        GetMessages messagesLoader = new GetMessages();
        messagesLoader.execute();
    }

    private View.OnClickListener backClick = new View.OnClickListener() {
        @Override
        public void onClick(View article) {
            onBackPressed();
        }
    };

    class GetMessages extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Document mainDoc = null;
            Document messageDoc = null;
            Elements messageLink;
            mLink = "";
            if (is_autorized)
            {
                try {
                    mainDoc = Jsoup.connect("https://instantcms.ru/").cookies(cookies).get();
                    messageLink = mainDoc.select("#head_center > div.grid_9 > div > div > div > span:nth-child(2) > a");
                    is_network_error = false;
                    for (Element el : messageLink) {
                        mLink = "https://instantcms.ru" + el.attr("href");
                    }
                    messageDoc = Jsoup.connect(mLink).cookies(cookies).get();
                    message_elements = messageDoc.select("#main > div.component > div:nth-child(4) > div.usr_msg_entry");
                    pages_elements = messageDoc.select("#main div.component div.pagebar>span.pagebar_current, #main div.component div.pagebar>a.pagebar_page");
                } catch (IOException e) {
                    is_network_error = true;
                    error_message = e.getLocalizedMessage();
                }
            }
            else
            {
                is_network_error = true;
                error_message = getResources().getString(R.string.app_need_auth);
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
                showMessages();
            }
        }
    }

    private void showError()
    {
        SwipeRefreshLayout content = (SwipeRefreshLayout) findViewById(R.id.messageContentContainer);
        RelativeLayout progressBarContainer = (RelativeLayout) findViewById(R.id.message_progress_bar_container);
        RelativeLayout errorContainer = (RelativeLayout) findViewById(R.id.messageErrorContainer);
        TextView errorMessage = (TextView) findViewById(R.id.merrorMessage);
        errorMessage.setText(error_message);
        progressBarContainer.setVisibility(RelativeLayout.GONE);
        content.setVisibility(View.GONE);
        errorContainer.setVisibility(RelativeLayout.VISIBLE);
    }

    private View.OnClickListener retryClick = new View.OnClickListener() {
        @Override
        public void onClick(View article) {
            SwipeRefreshLayout content = (SwipeRefreshLayout) findViewById(R.id.messageContentContainer);
            RelativeLayout progressBarContainer = (RelativeLayout) findViewById(R.id.message_progress_bar_container);
            RelativeLayout errorContainer = (RelativeLayout) findViewById(R.id.messageErrorContainer);
            content.setVisibility(View.GONE);
            errorContainer.setVisibility(RelativeLayout.GONE);
            progressBarContainer.setVisibility(RelativeLayout.VISIBLE);
            GetMessages messagesLoader = new GetMessages();
            messagesLoader.execute();
        }
    };

    private void showMessages()
    {
        LinearLayout paginator = (LinearLayout) findViewById(R.id.messagePaginator);
        LinearLayout messagesList = (LinearLayout) findViewById(R.id.messagesList);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lParams.setMargins(0, 0, 0, 10);
        LinearLayout.LayoutParams lvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lvParams.setMargins(0, 0, 0, 0);
        LinearLayout message = new LinearLayout(this);
        message.setOrientation(LinearLayout.VERTICAL);
        message.setLayoutParams(lParams);
        messagesList.removeAllViews();
        paginator.removeAllViews();
        pages.clear();
        for (Element el : message_elements)
        {
            LinearLayout head = new LinearLayout(this);
            head.setOrientation(LinearLayout.HORIZONTAL);
            head.setLayoutParams(lvParams);
            LinearLayout body = new LinearLayout(this);
            body.setOrientation(LinearLayout.HORIZONTAL);
            body.setLayoutParams(lvParams);
        }
        RelativeLayout progressBarContainer = (RelativeLayout) findViewById(R.id.message_progress_bar_container);
        RelativeLayout errorContainer = (RelativeLayout) findViewById(R.id.messageErrorContainer);
        SwipeRefreshLayout content = (SwipeRefreshLayout) findViewById(R.id.messageContentContainer);
        progressBarContainer.setVisibility(RelativeLayout.GONE);
        errorContainer.setVisibility(RelativeLayout.GONE);
        content.setVisibility(View.VISIBLE);
    }

    private SwipeRefreshLayout.OnRefreshListener swipeRefreshMessages = new SwipeRefreshLayout.OnRefreshListener() {
        public void onRefresh() {
            SwipeRefreshLayout content = (SwipeRefreshLayout) findViewById(R.id.messageContentContainer);
            content.setRefreshing(false);
        }
    };
}
