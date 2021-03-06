package com.zolipe.communitycensus;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.digits.sdk.android.Digits;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;
import com.zolipe.communitycensus.app.AppData;
import com.zolipe.communitycensus.fragments.FamilyDetailsFragment;
import com.zolipe.communitycensus.fragments.FamilyHeadsFragment;
import com.zolipe.communitycensus.fragments.ReportsFragment;
import com.zolipe.communitycensus.fragments.SupervisorFragment;
import com.zolipe.communitycensus.util.CensusConstants;
import com.zolipe.communitycensus.util.ConnectToServer;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnShowcaseEventListener {

    private String TAG = HomeActivity.class.getSimpleName();
    private static final float ALPHA_DIM_VALUE = 0.1f;
    Toolbar toolbar;
    ImageView iv_edit_profile;
    CircleImageView iv_profile_image;
    TextView tv_profile_name, tv_role, tv_gender, tv_mobile;
    Context mContext;
    BottomBar mBottomBar;
    FloatingActionButton fab_add_member;
    int tabPosition = 0;
    int previousTabPos = 0, currentTabPos = 0;
    String mUserRole;
    ShowcaseView sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserRole = AppData.getString(this, CensusConstants.userRole);
        mContext = HomeActivity.this;

        if (mUserRole.equals("admin")) {
            showAdminUI();
        } else if (mUserRole.equals("supervisor")) {
            showSupervisorUI();
        } else {
            showFamilyMembersUI();
        }

        initDrawerLayout();
    }

    private void showFamilyMembersUI() {
        setContentView(R.layout.activity_home_screen_family);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Community Census");
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_member);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_supervisor);
        navigationView.setNavigationItemSelectedListener(this);

        FamilyDetailsFragment familyDetailsFragment = new FamilyDetailsFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_fragment_family, familyDetailsFragment).commit();

        View headerview = navigationView.getHeaderView(0);

        /*
        * Navigation Drawer Edit Profile
        * */
        iv_edit_profile = (ImageView) headerview.findViewById(R.id.iv_edit_profile);
        iv_profile_image = (CircleImageView) headerview.findViewById(R.id.iv_profile_image);
        tv_profile_name = (TextView) headerview.findViewById(R.id.tv_profile_name);
        tv_role = (TextView) headerview.findViewById(R.id.tv_role);
        tv_gender = (TextView) headerview.findViewById(R.id.tv_gender);
        tv_mobile = (TextView) headerview.findViewById(R.id.tv_mobile);

        iv_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(HomeActivity.this, EditProfile.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });
    }

    private void initDrawerLayout() {
        /*
        * Navigation Profile Name
        * */
        String fname = AppData.getString(mContext, CensusConstants.firstName);
        String lname = AppData.getString(mContext, CensusConstants.lastName);
        tv_profile_name.setText(fname + " " + lname);
        tv_role.setText(AppData.getString(mContext, CensusConstants.userRole));
        tv_gender.setText(AppData.getString(mContext, CensusConstants.gender));
        tv_mobile.setText(AppData.getString(mContext, CensusConstants.phoneNumber));

        /*Navigation Header Image View*/
        Glide.with(mContext).load(AppData.getString(mContext, CensusConstants.image_url))
//                .thumbnail(0.5f)
                .crossFade()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_supervisor_list)
                .into(iv_profile_image);
    }

    private void showSupervisorUI() {
        setContentView(R.layout.activity_home_screen_supervisor);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Community Census");
        setSupportActionBar(toolbar);

        fab_add_member = (FloatingActionButton) findViewById(R.id.fab_add_member);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_sup);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_supervisor);
        navigationView.setNavigationItemSelectedListener(this);

        FamilyHeadsFragment familyHeadsFragment = new FamilyHeadsFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_fragment, familyHeadsFragment).commit();

        View headerview = navigationView.getHeaderView(0);

        /*
        * Navigation Drawer Edit Profile
        * */
        iv_edit_profile = (ImageView) headerview.findViewById(R.id.iv_edit_profile);
        iv_profile_image = (CircleImageView) headerview.findViewById(R.id.iv_profile_image);
        tv_profile_name = (TextView) headerview.findViewById(R.id.tv_profile_name);
        tv_role = (TextView) headerview.findViewById(R.id.tv_role);
        tv_gender = (TextView) headerview.findViewById(R.id.tv_gender);
        tv_mobile = (TextView) headerview.findViewById(R.id.tv_mobile);

        iv_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(HomeActivity.this, EditProfile.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        fab_add_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AddMember.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        showcaseSupervisorTutorial();
    }

    private void showAdminUI() {
        setContentView(R.layout.activity_home_screen);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Community Census");
        setSupportActionBar(toolbar);

        fab_add_member = (FloatingActionButton) findViewById(R.id.fab_add_member);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupervisorFragment supervisorFragment = new SupervisorFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.bottombar_container, supervisorFragment).commit();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

        View headerview = navigationView.getHeaderView(0);

        /*
        * Navigation Drawer Edit Profile
        * */
        iv_edit_profile = (ImageView) headerview.findViewById(R.id.iv_edit_profile);
        iv_profile_image = (CircleImageView) headerview.findViewById(R.id.iv_profile_image);
        tv_profile_name = (TextView) headerview.findViewById(R.id.tv_profile_name);
        tv_role = (TextView) headerview.findViewById(R.id.tv_role);
        tv_gender = (TextView) headerview.findViewById(R.id.tv_gender);
        tv_mobile = (TextView) headerview.findViewById(R.id.tv_mobile);

        iv_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(HomeActivity.this, EditProfile.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);

        //for transitions
        currentTabPos = tabPosition;
        previousTabPos = currentTabPos;

        //select the tab programatically
        mBottomBar.selectTabAtPosition(tabPosition);
        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_supervisors) {
                    previousTabPos = currentTabPos;
                    currentTabPos = 0;
                    supervisorsSelected();
                } else if (tabId == R.id.tab_family_heads) {
                    previousTabPos = currentTabPos;
                    currentTabPos = 1;
                    familyHeadsSelected();
                } else if (tabId == R.id.tab_reports) {
                    previousTabPos = currentTabPos;
                    currentTabPos = 2;
                    reportsSelected();
                }
            }
        });

        mBottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_supervisors) {
                    // The tab with id R.id.tab_favorites was reselected,
                    // change your content accordingly.
                    previousTabPos = currentTabPos = 0;
                    supervisorsSelected();
                } else if (tabId == R.id.tab_family_heads) {
                    previousTabPos = currentTabPos = 0;
                    familyHeadsSelected();
                } else if (tabId == R.id.tab_reports) {
                    previousTabPos = currentTabPos = 0;
                    reportsSelected();
                }
            }
        });

        fab_add_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AddMember.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        showcaseAdminTutorial();
    }

    private void showcaseAdminTutorial(){
        boolean run;

        run = AppData.getBoolean(mContext, "run?");

        if(!run){//If the buyer already went through the showcases it won't do it again.
            final ViewTarget viewTarget1  = new ViewTarget(R.id.tab_supervisors , this);//Variable holds the item that the showcase will focus on.
            final ViewTarget viewTarget2 = new ViewTarget(R.id.tab_family_heads , this);
            final ViewTarget viewTarget3 = new ViewTarget(R.id.tab_reports , this);
            final ViewTarget viewTarget4 = new ViewTarget(R.id.fab_add_member , this);


            RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lps.addRule(RelativeLayout.CENTER_VERTICAL);
            int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
            lps.setMargins(margin, margin, margin, margin);

            sv = new ShowcaseView.Builder(this)
                    .withMaterialShowcase()
                    .setTarget(viewTarget1)
                    .setContentTitle(R.string.showcase_supervisor_title)
                    .setContentText(R.string.showcase_supervisor_message)
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .build();
            sv.setButtonText("next");
            sv.setButtonPosition(lps);


            //When the button is clicked then the switch statement will check the counter and make the new showcase.
            sv.overrideButtonClick(new View.OnClickListener() {
                int count1 = 0;

                @Override
                public void onClick(View v) {
                    count1++;
                    switch (count1) {
                        case 1:
                            sv.setTarget(viewTarget2);
                            sv.setContentTitle(getResources().getString(R.string.showcase_fh_title));
                            sv.setContentText(getResources().getString(R.string.showcase_fh_message));
                            sv.setButtonText("next");
                            break;

                        case 2:
                            sv.setTarget(viewTarget3);
                            sv.setContentTitle(getResources().getString(R.string.showcase_reports_title));
                            sv.setContentText(getResources().getString(R.string.showcase_reports_message));
                            sv.setButtonText("next");
                            break;

                        case 3:
                            sv.setTarget(viewTarget4);
                            sv.setContentTitle(getResources().getString(R.string.lbl_add_member));
                            sv.setContentText(getResources().getString(R.string.showcase_add_member_message));
                            sv.setButtonText("Got It");
                            break;

                        case 4:
                            AppData.saveBoolean(mContext, "run?", true);
                            sv.hide();
                            break;
                    }
                }
            });
        }
    }

    private void showcaseSupervisorTutorial(){
        boolean run;

        run = AppData.getBoolean(mContext, "run?");

        if(!run){
            //If the buyer already went through the showcases it won't do it again.
            final ViewTarget viewTarget1 = new ViewTarget(R.id.fab_add_member , this);

            RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lps.addRule(RelativeLayout.CENTER_VERTICAL);
            int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
            lps.setMargins(margin, margin, margin, margin);

            //This creates the first showcase.
            sv = new ShowcaseView.Builder(this)
                    .withMaterialShowcase()
                    .setTarget(viewTarget1)
                    .setContentTitle(R.string.lbl_add_member)
                    .setContentText(R.string.showcase_add_member_message)
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .build();
            sv.setButtonText("Got It !!!");
            sv.setButtonPosition(lps);


            //When the button is clicked then the switch statement will check the counter and make the new showcase.
            sv.overrideButtonClick(new View.OnClickListener() {
                int count1 = 0;

                @Override
                public void onClick(View v) {
                    count1++;
                    switch (count1) {
                        case 1:
                            AppData.saveBoolean(mContext, "run?", true);
                            sv.hide();
                            break;
                    }
                }
            });
        }
    }

    private void reportsSelected() {
        commitFragment(new ReportsFragment());
    }

    private void familyHeadsSelected() {
        commitFragment(new FamilyHeadsFragment());
    }

    private void supervisorsSelected() {
        commitFragment(new SupervisorFragment());
    }

    private void commitFragment(Fragment fragment) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (previousTabPos > currentTabPos) {
            fragmentTransaction.setCustomAnimations(R.anim.slide_from_left, R.anim.slide_to_right);
        } else if (previousTabPos < currentTabPos) {
            fragmentTransaction.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left);
        }

        fragmentTransaction.replace(R.id.bottombar_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        initDrawerLayout();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mUserRole.equals("supervisor")) {
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout_sup);
        } else if (mUserRole.equals("member")) {
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout_member);
        }

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /*if (id == R.id.nav_broadcast) {
            broadcastClicked();
        } else *//*if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show();
        } else*/
        if (id == R.id.nav_sign_out) {
            logoutClicked();
        } else if (id == R.id.nav_about_us) {
            aboutUsClicked();
        } else if (id == R.id.nav_privacy_policy) {
            privacyPolicyClicked();
        } else if (id == R.id.nav_send_sms) {
//            Toast.makeText(this, "Send SMS clicked", Toast.LENGTH_SHORT).show();
            sendSMSClicked();
        } else if (id == R.id.nav_send_telegram) {
//            Toast.makeText(this, "Send SMS clicked", Toast.LENGTH_SHORT).show();
            broadcastTelegramClicked();
        } else if (id == R.id.nav_send_email) {
//            Toast.makeText(this, "Send SMS clicked", Toast.LENGTH_SHORT).show();
            broadcastEmailClicked();
        } else if (id == R.id.nav_add_supervisor) {
            addSupervisorClicked();
        }

        if (mUserRole.equals("admin")) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        } else if (mUserRole.equals("supervisor")) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_sup);
            drawer.closeDrawer(GravityCompat.START);
        } else if (mUserRole.equals("member")) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_member);
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void addSupervisorClicked() {
        Intent intent = new Intent(HomeActivity.this, AddSupervisor.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    private void privacyPolicyClicked() {
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
        dialog.setContentView(R.layout.privacy_dialog);
        final ImageView iv_close = (ImageView) dialog
                .findViewById(R.id.ic_dlg_close);

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void aboutUsClicked() {
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
        dialog.setContentView(R.layout.about_us_dialog);
        final ImageView iv_close = (ImageView) dialog
                .findViewById(R.id.ic_dlg_close);

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void broadcastEmailClicked() {
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.bc_email_dialog);

        final EditText et_subject = (EditText) dialog.findViewById(R.id.et_subject);
        final EditText et_message = (EditText) dialog.findViewById(R.id.et_message);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        final Button btn_send = (Button) dialog.findViewById(R.id.btn_send);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = et_subject.getText().toString();
                String message = et_message.getText().toString();
                if (subject.equals("") || message.equals("")){
                    if (subject.equals("")){
                        et_subject.setError("Please enter subject for email");
                    }else if (message.equals("")){
                        et_message.setError("Please enter content.");
                    }
                }else {
                    dialog.dismiss();
                    new sendEmailAsyncTask().execute(subject, message);
                }
            }
        });

        dialog.show();
    }

    private void broadcastTelegramClicked() {
        Intent intent = new Intent(HomeActivity.this, TelegramBroadcast.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    private void sendSMSClicked() {
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.bc_sms_dialog);

        final EditText et_input = (EditText) dialog.findViewById(R.id.et_input);
        ImageButton ib_send = (ImageButton) dialog.findViewById(R.id.ib_send);

        ib_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_input.getText().toString().equals("")){
                    et_input.setError("message cannot be empty !");
                }else {
                    et_input.setError(null);
                    new sendSMSAsyncTask().execute(et_input.getText().toString());
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void logoutClicked() {
        final Dialog dialog = new Dialog(HomeActivity.this, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.logout_dialog);

        Button btn_no = (Button)dialog.findViewById(R.id.btn_cancel);
        Button btn_yes = (Button)dialog.findViewById(R.id.btn_send);

        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
//                mLoggedIn = false;
//                mDigitsSession = null;
                Digits.logout();
                AppData.saveBoolean(getApplicationContext(), CensusConstants.isLoggedIn, false);
                AppData.clearPreferences(getApplicationContext());
                finishAffinity();
                Intent intent = new Intent(getApplicationContext(), LoginSignupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
            }
        });

        dialog.show();
    }

    @Override
    public void onShowcaseViewHide(ShowcaseView showcaseView) {

    }

    @Override
    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

    }

    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {

    }

    @Override
    public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

    }

    private class sendSMSAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String sms_text = params[0];
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair("sms_text", sms_text));
            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.SEND_SMS_URL, parms);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(mContext, s, Toast.LENGTH_LONG).show();
        }
    }

    /*private class sendTelegramAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String sms_text = params[0];
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair("text_msg", sms_text));
            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.SEND_TELEGRAM_URL, parms);
        }
    }*/

    private class sendEmailAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String subject = params[0];
            String message = params[1];

            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair("subject_line", subject));
            parms.add(new BasicNameValuePair("body_content", message));
            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.SEND_EMAIL_URL, parms);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
        }
    }
}
