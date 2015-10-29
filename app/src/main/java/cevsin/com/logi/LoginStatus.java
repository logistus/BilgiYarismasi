package cevsin.com.logi;

import android.app.Application;

/**
 * Created by Sinan on 10/18/2015.
 */
public class LoginStatus extends Application {
    private int login_status;

    public int getLoginStatus() {
        return login_status;
    }

    public void setLoginStatus(int newStatus) {
        login_status = newStatus;
    }

}
