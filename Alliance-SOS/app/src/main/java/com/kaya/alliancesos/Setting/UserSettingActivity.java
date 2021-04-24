package com.kaya.alliancesos.Setting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kaya.alliancesos.DbForRingtone.ChoiceApplication;
import com.kaya.alliancesos.DoNotDisturb.NotDisturbActivity;
import com.kaya.alliancesos.InvitationActivity;
import com.kaya.alliancesos.LogInPage;
import com.kaya.alliancesos.Payment.TransferActivity;
import com.kaya.alliancesos.R;
import com.kaya.alliancesos.UserObject;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserSettingActivity extends AppCompatActivity {

    private static final int PICK_RING = 1;

    private RelativeLayout mLogOutLayout;
    private boolean emailChange, isEditMode;
    private ImageView mEditMode, mExitEditMode;
    private Button mUpdate_btn;
    private TextView mChosePhoto;

    private ImageView mBackUserImage;
    private CircleImageView mUserImage;
    private EditText mEmail, mPass, mUsername, mTime, mLanguage;
    private CheckBox mRingEnable;

    private String mUserId;
    private String mUpdatedImageUrl;
    private UserObject mCurrUserInfo;

    private DatabaseReference mUserRef;
    private StorageReference mUserImageProfileRef;

    private ViewDialog loadingDialog;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null)
                    goToLogInPage();
            }
        };

        loadingDialog = new ViewDialog(this);

        Intent fromGroup = getIntent();
        if (fromGroup != null) {
            mUserId = fromGroup.getStringExtra("userId");
        }
        Initialize();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                }
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return;
            }
        }
    }

    private void Initialize() {
        isEditMode = false;
        emailChange = false;
        mUpdatedImageUrl = "";

        mUserRef = FirebaseDatabase.getInstance().getReference().child("users");
        mUserImageProfileRef = FirebaseStorage.getInstance().getReference().child("user-profile-images");
        InitUI();
    }

    public void UpdateUserProfile(View view) {
        UserObject newInfo = getNewUserInfoFromText();
        checkUpdateCondition(newInfo);
    }

    private void checkUpdateCondition(UserObject newInfo) {
        if (!newInfo.getEmail().equals(mCurrUserInfo.getEmail())) {
            emailChange = true;
            updateEmailAddress(newInfo);
        } else {
            Toast.makeText(getApplicationContext(), "Email Has Updated...", Toast.LENGTH_SHORT).show();
        }
        if (!newInfo.getPassword().equals(mCurrUserInfo.getPassword())) {
            if (emailChange) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
                builder.setTitle("Alert !");
                builder.setMessage("You Can Not change email and password together" + "\n" + "Your Email has changed!!!"
                        + "\n" + "But Your Password did'nt change." + "\n" + "please press back and come here again to see changes ...");
                builder.setCancelable(false);
                builder.setIcon(R.drawable.sos_icon);
                builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            } else {
                updatePassword(newInfo);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Password Has Updated...", Toast.LENGTH_SHORT).show();
        }
        Updating(mUserId, "userName", mCurrUserInfo.getUserName(), newInfo.getUserName());
        Updating(mUserId, "ringEnable", mCurrUserInfo.isRingEnable(), newInfo.isRingEnable());
        Updating(mUserId, "timeZone", mCurrUserInfo.getTimeZone(), newInfo.getTimeZone());
        Updating(mUserId, "language", mCurrUserInfo.getLanguage(), newInfo.getLanguage());

        emailChange = false;
    }

    private void updateEmailAddress(final UserObject newInfo) {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.reauthenticate(EmailAuthProvider.getCredential(mCurrUserInfo.getEmail(), mCurrUserInfo.getPassword()))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updateEmail(newInfo.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Updating(mUserId, "email", mCurrUserInfo.getEmail(), newInfo.getEmail());
                                        Toast.makeText(UserSettingActivity.this, "Update Email database..", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(UserSettingActivity.this, task.getException().getMessage() + " Could not Authentication ...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(UserSettingActivity.this, task.getException().getMessage() + " Could not Authentication ...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updatePassword(final UserObject newInfo) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.reauthenticate(EmailAuthProvider.getCredential(mCurrUserInfo.getEmail(), mCurrUserInfo.getPassword())).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        user.updatePassword(newInfo.getPassword()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Updating(mUserId, "password", mCurrUserInfo.getPassword(), newInfo.getPassword());
                                    Toast.makeText(UserSettingActivity.this, "Update password database..", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(UserSettingActivity.this, task.getException().getMessage() + " error in Update password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
        );

    }

    private <Type> void Updating(String userId, String field, Type oldVal, Type newVal) {
        if (!oldVal.equals(newVal)) {
            mUserRef.child(userId).child(field).setValue(newVal);
            mCurrUserInfo.updateObject(field, newVal);
            Toast.makeText(getApplicationContext(), field + " Updated", Toast.LENGTH_SHORT).show();
        }
    }

    private UserObject getNewUserInfoFromText() {
        UserObject userObject = new UserObject();
        userObject.setEmail(mEmail.getText().toString());
        userObject.setPassword(mPass.getText().toString());
        userObject.setUserName(mUsername.getText().toString());
        userObject.setTimeZone(mTime.getText().toString());
        userObject.setLanguage(mLanguage.getText().toString());
        userObject.setRingEnable(mRingEnable.isChecked());
        userObject.setDeviceType(UserObject.ANDROID);
        userObject.setToken(mCurrUserInfo.getToken());
        userObject.setId(mCurrUserInfo.getId());
        userObject.setNotDisturb(false);
        return userObject;
    }

    private void getInfoOfCurrentUser() {
        progressBar.setVisibility(View.VISIBLE);
        mUserRef.child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    GetBasicInfoTask task = new GetBasicInfoTask(snapshot);
                    task.execute();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(UserSettingActivity.this, "Not Exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserSettingActivity.this, error.getMessage() + "\n" + error.getDetails(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void setInfoToUi() {
        mEmail.setText(mCurrUserInfo.getEmail());
        mPass.setText(mCurrUserInfo.getPassword());
        mUsername.setText(mCurrUserInfo.getUserName());
        mTime.setText(TimeZone.getDefault().getID());
        mLanguage.setText(mCurrUserInfo.getLanguage());
        mRingEnable.setChecked(true);
    }

    private void InitUI() {
        mLogOutLayout = findViewById(R.id.log_out_layout);
        progressBar = findViewById(R.id.progress_user_setting);

        mBackUserImage = findViewById(R.id.back_user_image);
        mUserImage = findViewById(R.id.userImage_setting);

        mEditMode = findViewById(R.id.edit_user_setting);
        mExitEditMode = findViewById(R.id.exit_edit_user_setting);
        mUpdate_btn = findViewById(R.id.update_user_setting_btn);
        mChosePhoto = findViewById(R.id.chose_photo_user_txt);

        mEmail = findViewById(R.id.email_setting);
        mPass = findViewById(R.id.password_setting);
        mUsername = findViewById(R.id.username_setting);
        mTime = findViewById(R.id.timeZone_setting);
        mLanguage = findViewById(R.id.language_setting);
        mRingEnable = findViewById(R.id.ring_before_event_setting);
        NotAllowedUseSpace();
    }

    public void selectLanguage(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserSettingActivity.this);
        LayoutInflater layoutInflater = LayoutInflater.from(UserSettingActivity.this);
        View searchView = layoutInflater.inflate(R.layout.zones_languages_pattern, null, false);
        setAllLanguages(searchView);

        builder.setTitle("pick Language");
        builder.setView(searchView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(mTime.getText().toString())) {
                    Toast.makeText(UserSettingActivity.this, "You should Chose on Item", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private void setAllLanguages(View root) {
        ListView languages_listView = root.findViewById(R.id.time_zone_listView);
        final SearchView searchView = root.findViewById(R.id.time_zone_search_view);
        final ArrayList<String> allLanguages = new ArrayList<>();
        String[] isoLanguages = Locale.getISOLanguages();
        for (int i = 0; i < isoLanguages.length; i++) {
            Locale loc = new Locale(isoLanguages[i]);
            allLanguages.add(loc.getDisplayLanguage());
        }
        final ArrayAdapter<String> languages_adapter = new ArrayAdapter<>(root.getContext(), android.R.layout.simple_list_item_1, allLanguages);
        languages_listView.setAdapter(languages_adapter);
        languages_listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        languages_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(UserSettingActivity.this, languages_adapter.getItem(position), Toast.LENGTH_SHORT).show();
                mLanguage.setText(languages_adapter.getItem(position));
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (allLanguages.contains(query)) {
                    languages_adapter.getFilter().filter(query);
                } else {
                    Toast.makeText(UserSettingActivity.this, "Not Found", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                languages_adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    public void setTimeZone(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserSettingActivity.this);
        LayoutInflater layoutInflater = LayoutInflater.from(UserSettingActivity.this);
        View searchView = layoutInflater.inflate(R.layout.zones_languages_pattern, null, false);

        setAllZoneIds(searchView);

        builder.setTitle("pick Time Zone");
        builder.setView(searchView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(mTime.getText().toString())) {
                    Toast.makeText(UserSettingActivity.this, "You should Chose on Item", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private void setAllZoneIds(View root) {
        final ListView zoneIds_listView = root.findViewById(R.id.time_zone_listView);
        final SearchView searchView = root.findViewById(R.id.time_zone_search_view);
        final String[] tmp = TimeZone.getAvailableIDs();
        final ArrayList<String> allZonesIds = new ArrayList<>();
        allZonesIds.addAll(Arrays.asList(tmp));
        final ArrayAdapter<String> mAllZonesId_adapter = new ArrayAdapter<>(root.getContext(), android.R.layout.simple_list_item_1, allZonesIds);
        zoneIds_listView.setAdapter(mAllZonesId_adapter);
        zoneIds_listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        zoneIds_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(UserSettingActivity.this, mAllZonesId_adapter.getItem(position), Toast.LENGTH_SHORT).show();
                mTime.setText(mAllZonesId_adapter.getItem(position) + "");
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (allZonesIds.contains(query)) {
                    mAllZonesId_adapter.getFilter().filter(query);
                } else {
                    Toast.makeText(UserSettingActivity.this, "Not Found", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAllZonesId_adapter.getFilter().filter(newText);
                return false;
            }
        });

    }


    public void onEditMode(View view) {
        goToEditMode();
    }

    public void onExitEditMode(View view) {
        exitEditMode();
    }

    public void choseUserPhoto(View view) {
        if (isEditMode) {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(UserSettingActivity.this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            final Uri photo = result.getUri();
            loadingDialog.showDialog();

            final StorageReference photoPath = mUserImageProfileRef.child(mUserId + ".jpg");
            photoPath.putFile(photo).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {

                        photoPath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {

                                    mUpdatedImageUrl = task.getResult().toString();
                                    mUserRef.child(mUserId).child("image").setValue(mUpdatedImageUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Glide.with(getApplicationContext())
                                                        .load(photo)
                                                        .into(mBackUserImage);

                                                loadingDialog.hideDialog();

                                                Glide.with(getApplicationContext())
                                                        .load(photo)
                                                        .into(mUserImage);

                                            } else {
                                                loadingDialog.hideDialog();
                                                Toast.makeText(UserSettingActivity.this, "Error :" + task.getException(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                } else {
                                    loadingDialog.hideDialog();
                                    Toast.makeText(UserSettingActivity.this, "Error :" + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        loadingDialog.hideDialog();
                        Toast.makeText(UserSettingActivity.this, "Error :" + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else if (requestCode == PICK_RING) {
            Uri uri = data.getData();
            SharedPreferences sharedPreferences = getSharedPreferences("User_Ring", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(mUserId, uri.toString());
            editor.apply();
            Toast.makeText(this, "Done.", Toast.LENGTH_LONG).show();
        }
    }

    public void choseCustomRingtone(View view) {
        if (isEditMode) {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent.createChooser(intent, "Music Files"), PICK_RING);
        }
    }

    @Override
    public void onBackPressed() {
        if (isEditMode) {
            exitEditMode();
        }
        super.onBackPressed();
    }

    public void pickDoNotDisturb(View view) {
        if (isEditMode) {
            Intent gotToNotDisturb = new Intent(UserSettingActivity.this, NotDisturbActivity.class);
            gotToNotDisturb.putExtra("userId", mUserId);
            startActivity(gotToNotDisturb);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuthStateListener != null)
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        if (!isEditMode) {
            getInfoOfCurrentUser();
        }
    }


    private void goToEditMode() {
        mLogOutLayout.setVisibility(View.VISIBLE);
        isEditMode = true;
        mEditMode.setVisibility(View.GONE);
        mExitEditMode.setVisibility(View.VISIBLE);
        mUpdate_btn.setVisibility(View.VISIBLE);
        mEmail.setEnabled(true);
        mPass.setEnabled(true);
        mUsername.setEnabled(true);
        mRingEnable.setEnabled(true);
        mLanguage.setEnabled(true);
        mChosePhoto.setVisibility(View.VISIBLE);
    }

    private void exitEditMode() {
        mLogOutLayout.setVisibility(View.GONE);
        isEditMode = false;
        mEditMode.setVisibility(View.VISIBLE);
        mExitEditMode.setVisibility(View.GONE);
        mUpdate_btn.setVisibility(View.GONE);
        mEmail.setEnabled(false);
        mPass.setEnabled(false);
        mUsername.setEnabled(false);
        mRingEnable.setEnabled(false);
        mLanguage.setEnabled(false);
        mChosePhoto.setVisibility(View.GONE);
    }

    public void exitUserSetting(View view) {
        onBackPressed();
    }

    public void LogOut(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setIcon(R.drawable.attention_icon);
        builder.setTitle("Attention");
        builder.setMessage("Are you Sure You Want to Logout ?");
        builder.setCancelable(true);
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Yes ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                makeTokenNull();
            }
        });
        builder.create().show();
    }

    private void makeTokenNull() {
        loadingDialog.showDialog();
        mUserRef.child(mUserId).child("token").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    FirebaseAuth.getInstance().signOut();
                } else {
                    Toast.makeText(UserSettingActivity.this, "Error " + task.getException(), Toast.LENGTH_SHORT).show();
                }
                loadingDialog.hideDialog();
            }
        });
    }

    private void goToLogInPage() {
        Intent goToLogIn = new Intent(getApplicationContext(), LogInPage.class);
        startActivity(goToLogIn);
        finish();
        return;
    }

    public void invitePage(View view) {
        Intent intent = new Intent(getApplicationContext(), InvitationActivity.class);
        intent.putExtra("userId", mUserId);
        startActivity(intent);
    }

    private void NotAllowedUseSpace() {
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.isWhitespace(source.charAt(i))) {
                        Toast.makeText(getApplicationContext(), "Not Space For this Field", Toast.LENGTH_SHORT).show();
                        return "";
                    }
                }
                return null;
            }
        };
        InputFilter[] filter1 = new InputFilter[]{filter};
        mEmail.setFilters(filter1);
        mUsername.setFilters(filter1);
    }

    public class GetBasicInfoTask extends AsyncTask<Void, Void, Void> {

        DataSnapshot dataSnapshot;
        RequestCreator requestCreator;

        public GetBasicInfoTask(DataSnapshot dataSnapshot) {
            this.dataSnapshot = dataSnapshot;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String id = dataSnapshot.child("id").getValue().toString();
            String userName = dataSnapshot.child("userName").getValue().toString();
            String email = dataSnapshot.child("email").getValue().toString();
            String token = dataSnapshot.child("token").getValue().toString();
            String pass = dataSnapshot.child("password").getValue().toString();
            String language = dataSnapshot.child("language").getValue().toString();
            String timeZone = dataSnapshot.child("timeZone").getValue().toString();
            mUpdatedImageUrl = dataSnapshot.child("image").getValue().toString();
            mCurrUserInfo = new UserObject(id, userName, email, pass, token);
            mCurrUserInfo.setLanguage(language);
            mCurrUserInfo.setTimeZone(timeZone);
            if (!TextUtils.isEmpty(mUpdatedImageUrl)) {
                requestCreator = Picasso.get().load(mUpdatedImageUrl);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setInfoToUi();
            if (requestCreator != null) {
                requestCreator.into(mBackUserImage);
                requestCreator.into(mUserImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(UserSettingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    public void goToDonatePage(View view) {
        if (isEditMode) {
            Intent intent = new Intent(this, TransferActivity.class);
            intent.putExtra("userId", mUserId);
            startActivity(intent);
        }
    }
}
