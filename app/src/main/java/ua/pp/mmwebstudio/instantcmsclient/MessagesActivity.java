package ua.pp.mmwebstudio.instantcmsclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MessagesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        ImageButton backButton = (ImageButton) findViewById(R.id.messageBackButton);
        backButton.setOnClickListener(backClick);
    }

    private View.OnClickListener backClick = new View.OnClickListener() {
        @Override
        public void onClick(View article) {
            onBackPressed();
        }
    };
}
