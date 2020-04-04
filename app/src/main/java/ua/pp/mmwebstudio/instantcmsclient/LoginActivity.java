package ua.pp.mmwebstudio.instantcmsclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private Boolean is_network_error = false, is_login_error = false;
    private String error_message = "";
    private String login = null, password = null;
    Connection.Response loginResponse = null;
    Document loginResult = null;
    SharedPreferences Pref;
    final String pref_name_file = "ICMSClSettings";
    final String autorized_param = "is_autorized";
    final String cookies_params = "cookies";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(backClick);
        EditText loginText = (EditText) findViewById(R.id.username);
        EditText passText = (EditText) findViewById(R.id.password);
        loginText.addTextChangedListener(loginDataWatcher);
        passText.addTextChangedListener(loginDataWatcher);
        Button loginButton = (Button) findViewById(R.id.login);
        loginButton.setOnClickListener(loginCLick);
    }

    private View.OnClickListener backClick = new View.OnClickListener() {
        @Override
        public void onClick(View article) {
            onBackPressed();
        }
    };

    private View.OnClickListener loginCLick = new View.OnClickListener() {
        @Override
        public void onClick(View article) {
            EditText loginText = (EditText) findViewById(R.id.username);
            EditText passText = (EditText) findViewById(R.id.password);
            RelativeLayout loadLayout = (RelativeLayout) findViewById(R.id.loadLayout);
            loadLayout.setVisibility(View.VISIBLE);
            login = loginText.getText().toString();
            password = passText.getText().toString();
            LoginTask ltask = new LoginTask();
            ltask.execute();
        }
    };

    class LoginTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                loginResponse = Jsoup.connect("https://instantcms.ru/login")
                        .data("login", login)
                        .data("pass", password)
                        .data("remember", "1")
                        .method(Connection.Method.POST)
                        .execute();;
                loginResult = loginResponse.parse();
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
                loginProcess();
            }
        }
    }

    private void loginProcess()
    {
        Elements errorElements = loginResult.select("body>table>tbody>tr>td>table>tbody>tr>td>h2");
        is_login_error = false;
        for(Element err : errorElements)
        {
            if(err.text().equals("Ошибка авторизации"))
            {
                is_login_error = true;
            }
        }
        RelativeLayout loadLayout = (RelativeLayout) findViewById(R.id.loadLayout);
        loadLayout.setVisibility(View.GONE);
        if(is_login_error)
        {
            Pref = getSharedPreferences(pref_name_file, MODE_PRIVATE);
            SharedPreferences.Editor ed = Pref.edit();
            ed.putString(cookies_params, "");
            ed.putString(autorized_param, "0");
            ed.apply();
            error_message = getResources().getString(R.string.wrongLoginData);
            showError();
        }
        else {
            HashMap<String, String> icmsCookies = new HashMap<String, String>();
            icmsCookies.putAll(loginResponse.cookies());
            Gson gson = new Gson();
            String icmsCookiesString = gson.toJson(icmsCookies);
            Pref = getSharedPreferences(pref_name_file, MODE_PRIVATE);
            SharedPreferences.Editor ed = Pref.edit();
            ed.putString(cookies_params, icmsCookiesString);
            ed.putString(autorized_param, "1");
            ed.apply();
            onBackPressed();
        }
    }

    private void showError()
    {
        Toast toast = Toast.makeText(getApplicationContext(),
                error_message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private final TextWatcher loginDataWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        public void afterTextChanged(Editable s) {
            EditText loginText = (EditText) findViewById(R.id.username);
            EditText passText = (EditText) findViewById(R.id.password);
            Button loginButton = (Button) findViewById(R.id.login);
            if(loginText.length() > 0 && passText.length() > 0)
            {
                loginButton.setEnabled(true);
            }
            else {
                loginButton.setEnabled(false);
            }
        }
    };
}
