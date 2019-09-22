//package vn.viethoang.truong.smartclass.View.Login;
//
//import android.content.Intent;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//
//import vn.viethoang.truong.smartclass.R;
//import vn.viethoang.truong.smartclass.View.Home.HomeActivity;
//
//public class LoginActivity extends AppCompatActivity {
//    private Button btnLogin;
//    private EditText edPass, edUser;
//    private TextView txtFail;
//    private String tk="admin";
//    private String mk="admin";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        btnLogin= findViewById(R.id.btnLogin);
//        edUser= findViewById(R.id.edAcc);
//        edPass= findViewById(R.id.edPass);
//        txtFail= findViewById(R.id.txtFail);
//
//        txtFail.setVisibility(View.INVISIBLE);
//
//        btnLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                xuLyLogin();
//            }
//        });
//    }
//
//    private void xuLyLogin() {
//        if(edUser.getText().toString().equals(tk) && edPass.getText().toString().equals(mk)) {
//            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//            startActivity(intent);
//            finish();
//        }else{
//            edUser.setText("");
//            edPass.setText("");
//            txtFail.setVisibility(View.VISIBLE);
//        }
//    }
//}
