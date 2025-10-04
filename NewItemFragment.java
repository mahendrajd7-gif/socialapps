package finix.social.finixapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.provider.OpenableColumns;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import  finix.social.finixapp.libs.circularImageView.*;
import com.otaliastudios.transcoder.Transcoder;
import com.otaliastudios.transcoder.TranscoderListener;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import github.ankushsachdeva.emojicon.EditTextImeBackListener;
import github.ankushsachdeva.emojicon.EmojiconEditText;
import github.ankushsachdeva.emojicon.EmojiconGridView;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import finix.social.finixapp.adapter.FeelingsListAdapter;
import finix.social.finixapp.adapter.MediaListAdapter;
import finix.social.finixapp.app.App;
import finix.social.finixapp.constants.Constants;
import finix.social.finixapp.model.Feeling;
import finix.social.finixapp.model.Item;
import finix.social.finixapp.model.MediaItem;
import finix.social.finixapp.util.Api;
import finix.social.finixapp.util.CountingRequestBody;
import finix.social.finixapp.util.CustomRequest;
import finix.social.finixapp.util.Helper;

public class NewItemFragment extends Fragment implements Constants {

    private static final int BUFFER_SIZE = 1024 * 2;

    private static final int VIDEO_FILES_LIMIT = 1;
    private static final int IMAGE_FILES_LIMIT = 7;

    public static final int REQUEST_TAKE_GALLERY_VIDEO = 1001;

    private static final String STATE_LIST = "State Adapter Data";

    public static final int RESULT_OK = -1;

    private static final int ITEM_FEELINGS = 1;

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;

    private MaterialRippleLayout mOpenBottomSheet;

    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private View mBottomSheet;

    private RecyclerView mRecyclerView;
    private LinearLayout mImagesLayout;

    private LinearLayout mRepostLayout, mDeleteRepost;
    private TextView mRepostAuthorTitle, mRepostContent;
    private CircularImageView mRepostAuthorPhoto;
    private ImageView mRepostImage;
    private Button mViewRepostButton;

    private ArrayList<MediaItem> itemsList;
    private MediaListAdapter itemsAdapter;
    private CircularImageView mPhoto;
    private TextView mFullname;

    private ImageView mAccessModeIcon;
    private TextView mAccessModeTitle;
    private LinearLayout mAccessModeLayout;

    private ImageView mLocationIcon;
    private TextView mLocationTitle;
    private LinearLayout mLocationLayout;

    private ImageView mFeelingIcon;
    private TextView mFeelingTitle;
    private LinearLayout mFeelingLayout;

    private ProgressDialog pDialog;

    EmojiconEditText mPostEdit;
    ImageView mEmojiBtn;

    private long group_id = 0;
    private int position = 0;

    private Item item;

    private Boolean loading = false;
    private Boolean uploading = false;
    private Boolean compressing = false;

    private String selectedCameraImg = "";

    EmojiconsPopup popup;

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String[]> storagePermissionLauncher;
    private ActivityResultLauncher<Intent> imgFromCameraActivityResultLauncher;
    private ActivityResultLauncher<Intent> imgFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> videoFromGalleryActivityResultLauncher;

    private ActivityResultLauncher<Intent> videoFromCameraActivityResultLauncher;
    private ActivityResultLauncher<String> recordAudioPermissionLauncher;
    private ActivityResultLauncher<String[]> videoCapturePermissionLauncher;

    public NewItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);

        initpDialog();

        Intent i = getActivity().getIntent();

        group_id = i.getLongExtra("groupId", 0);

        position = i.getIntExtra("position", 0);

        Item repost = new Item();

        if (i.getExtras() != null) {

            item = (Item) i.getExtras().getParcelable("item");

            if (item == null) {

                item = new Item();
            }

            if (item.getGroupId() != 0) group_id = item.getGroupId();

            repost = (Item) i.getExtras().getParcelable("repost");

            if (repost == null) {

                repost = new Item();
            }

        } else {

            item = new Item();
        }

        if (repost.getId() != 0) {

            if (repost.getRePostId() == 0) {

                item.setRePostId(repost.getId());
                item.setRePostPost(repost.getPost());
                item.setRePostFromUserFullname(repost.getFromUserFullname());
                item.setRePostFromUserPhotoUrl(repost.getFromUserPhotoUrl());
                item.setRePostImgUrl(repost.getImgUrl());

            } else {

                item.setRePostId(repost.getRePostId());
                item.setRePostPost(repost.getRePostPost());
                item.setRePostFromUserFullname(repost.getRePostFromUserFullname());
                item.setRePostFromUserPhotoUrl(repost.getRePostFromUserPhotoUrl());
                item.setRePostImgUrl(repost.getRePostImgUrl());
            }


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_new_item, container, false);

        if (savedInstanceState != null) {

            item = savedInstanceState.getParcelable("item");

            loading = savedInstanceState.getBoolean("loading");
            uploading = savedInstanceState.getBoolean("uploading");
            compressing = savedInstanceState.getBoolean("compressing");

            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            itemsAdapter = new MediaListAdapter(getActivity(), itemsList);

        } else {

            loading = false;
            uploading = false;
            compressing = false;

            itemsList = new ArrayList<>();
            itemsAdapter = new MediaListAdapter(getActivity(), itemsList);
        }

        //

        videoCapturePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {

            boolean granted = true;

            for (Map.Entry<String, Boolean> x : isGranted.entrySet())

                if (!x.getValue()) granted = false;

            if (granted) {

                captureVideo();

            } else {

                // Permission is denied

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_camera_or_video_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_camera_and_audio_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }
        });

        //

        videoFromCameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == 100) {

                    File file =  new File(App.getInstance().getDirectory(), VIDEO_SRC_FILE);

                    if (file.exists()) {

                        Log.e("MyApp file size: ", Long.toString(file.length()));

                        String selectedImagePath = file.getPath();

                        Helper helper = new Helper(App.getInstance().getApplicationContext());
                        helper.createThumbnail(ThumbnailUtils.createVideoThumbnail(selectedImagePath, MediaStore.Images.Thumbnails.MINI_KIND), VIDEO_THUMBNAIL_FILE);

                        itemsList.add(new MediaItem(VIDEO_THUMBNAIL_FILE, selectedImagePath, "", "", 1));
                        itemsAdapter.notifyDataSetChanged();

                        updateMediaLayout();
                    }
                }
            }
        });

        //

        videoFromGalleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    // The document selected by the user won't be returned in the intent.
                    // Instead, a URI to that document will be contained in the return intent
                    // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

                    if (result.getData() != null) {

                        String selectedImagePath = Helper.getRealPath(result.getData().getData());

                        File videoFile = new File(selectedImagePath);

                        if (videoFile.length() > VIDEO_FILE_MAX_SIZE_FROM_GALLERY) {

                            Toast.makeText(getActivity(), getString(R.string.msg_video_too_large), Toast.LENGTH_SHORT).show();

                        } else {

                            Helper helper = new Helper(App.getInstance().getApplicationContext());
                            helper.createThumbnail(ThumbnailUtils.createVideoThumbnail(selectedImagePath, MediaStore.Images.Thumbnails.MINI_KIND), VIDEO_THUMBNAIL_FILE);

                            itemsList.add(new MediaItem(VIDEO_THUMBNAIL_FILE, selectedImagePath, "", "", 1));
                            itemsAdapter.notifyDataSetChanged();

                            updateMediaLayout();
                        }
                    }
                }
            }
        });

        //

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

            if (isGranted) {

                // Permission is granted
                Log.e("Permissions", "Permission is granted");

                choiceImageAction();

            } else {

                // Permission is denied

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_camera_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_camera_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }
        });

        //

        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {

            boolean granted = false;
            String storage_permission = Manifest.permission.READ_EXTERNAL_STORAGE;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

                storage_permission = Manifest.permission.READ_MEDIA_IMAGES;
            }

            for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {

                if (x.getKey().equals(storage_permission)) {

                    if (x.getValue()) {

                        granted = true;
                    }
                }
            }

            if (granted) {

                Log.e("Permissions", "granted");

                showBottomSheet();

            } else {

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_storage_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_storage_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }

        });

        //

        imgFromCameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    String selectedImagePath = new File(App.getInstance().getDirectory(), selectedCameraImg).getPath();

                    itemsList.add(new MediaItem(selectedImagePath, "", "", "", 0));
                    itemsAdapter.notifyDataSetChanged();

                    updateMediaLayout();
                }
            }
        });

        //

        imgFromGalleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    // The document selected by the user won't be returned in the intent.
                    // Instead, a URI to that document will be contained in the return intent
                    // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

                    if (result.getData() != null) {

                        Uri uri = result.getData().getData();

                        ContentResolver cr = getActivity().getContentResolver();

                        // Attempt 1: Get MIME type
                        String mimeType = cr.getType(uri);

                        String extension = "." +
                                ""; // Default to JPG

                        if (mimeType != null && mimeType.contains("png")) {
                            extension = ".png";
                        }
                        // Attempt 2: Fallback to checking the filename from the ContentResolver
                        else if (mimeType == null || mimeType.contains("*")) {

                            Cursor cursor = cr.query(uri, null, null, null, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                // Get the display name column index
                                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                                if (nameIndex != -1) {
                                    String fileName = cursor.getString(nameIndex);
                                    if (fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".png")) {
                                        extension = ".png";
                                    }
                                }
                                cursor.close();
                            }
                        }

                        // 3. Use the static Helper method for random string
                        String newFileName = Helper.randomString(6) + extension;

                        // 4. Create an instance of Helper to call the non-static createImage
                        Helper helper = new Helper(App.getInstance().getApplicationContext());
                        helper.createImage(uri, newFileName);

                        String selectedImagePath = new File(App.getInstance().getDirectory(), newFileName).getPath();

                        itemsList.add(new MediaItem(selectedImagePath, "", "", "", 0));
                        itemsAdapter.notifyDataSetChanged();

                        updateMediaLayout();
                    }

                }
            }
        });

        //

        popup = new EmojiconsPopup(rootView, getActivity());

        popup.setSizeForSoftKeyboard();

        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {

                mPostEdit.append(emojicon.getEmoji());
            }
        });

        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {

                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mPostEdit.dispatchKeyEvent(event);
            }
        });

        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {

                setIconEmojiKeyboard();
            }
        });

        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {

                if (popup.isShowing())

                    popup.dismiss();
            }
        });

        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {

                mPostEdit.append(emojicon.getEmoji());
            }
        });

        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {

                KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mPostEdit.dispatchKeyEvent(event);
            }
        });

        if (loading) {

            showpDialog();
        }

        //

        mOpenBottomSheet = (MaterialRippleLayout) rootView.findViewById(R.id.open_bottom_sheet_button);

        mOpenBottomSheet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showBottomSheet();
            }
        });

        // Prepare bottom sheet

        mBottomSheet = rootView.findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(mBottomSheet);

        //

        mPhoto = (CircularImageView) rootView.findViewById(R.id.photo_image);
        mFullname = (TextView) rootView.findViewById(R.id.fullname_label);

        //

        mRepostLayout = (LinearLayout) rootView.findViewById(R.id.repost_layout);
        mDeleteRepost = (LinearLayout) rootView.findViewById(R.id.repost_delete);

        mDeleteRepost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                item.setRePostId(0);

                updateRepostLayout();
            }
        });

        mRepostAuthorPhoto = (CircularImageView) rootView.findViewById(R.id.repost_author_photo_image);
        mRepostImage = (ImageView) rootView.findViewById(R.id.repost_image);

        mRepostAuthorTitle = (TextView) rootView.findViewById(R.id.repost_author_fullname_label);
        mRepostContent = (TextView) rootView.findViewById(R.id.repost_text);

        mViewRepostButton = (Button) rootView.findViewById(R.id.repost_view);

        mViewRepostButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ViewItemActivity.class);
                intent.putExtra("itemId", item.getRePostId());
                getActivity().startActivity(intent);
            }
        });

        //

        mImagesLayout = (LinearLayout) rootView.findViewById(R.id.images_layout);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setAdapter(itemsAdapter);

        itemsAdapter.setOnItemClickListener(new MediaListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, MediaItem obj, int position, int action) {

                if (action != 0) {

                    itemsList.remove(position);
                    itemsAdapter.notifyDataSetChanged();

                    updateMediaLayout();
                }
            }
        });

        //

        mAccessModeIcon = (ImageView) rootView.findViewById(R.id.access_mode_image);
        mAccessModeTitle = (TextView) rootView.findViewById(R.id.access_mode_label);
        mAccessModeLayout = (LinearLayout) rootView.findViewById(R.id.access_mode_layout);

        mAccessModeLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);

                arrayAdapter.add(getText(R.string.label_post_to_public).toString());
                arrayAdapter.add(getText(R.string.label_post_to_friends).toString());

                builderSingle.setTitle(getText(R.string.label_post_to_dialog_title));


                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        item.setAccessMode(which);;

                        updateAccessMode();
                    }
                });

                builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                AlertDialog d = builderSingle.create();
                d.show();
            }
        });

        mLocationIcon = (ImageView) rootView.findViewById(R.id.location_image);
        mLocationTitle = (TextView) rootView.findViewById(R.id.location_label);
        mLocationLayout = (LinearLayout) rootView.findViewById(R.id.location_layout);

        mLocationLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                deleteLocation();
            }
        });

        mFeelingIcon = (ImageView) rootView.findViewById(R.id.feeling_image);
        mFeelingTitle = (TextView) rootView.findViewById(R.id.feeling_label);
        mFeelingLayout = (LinearLayout) rootView.findViewById(R.id.feeling_layout);

        mFeelingLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                deleteFeeling();
            }
        });

        mEmojiBtn = (ImageView) rootView.findViewById(R.id.emojiBtn);
        mEmojiBtn.setVisibility(View.GONE);

        mPostEdit = (EmojiconEditText) rootView.findViewById(R.id.postEdit);
        mPostEdit.setText(item.getPost());

        mPostEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (isAdded()) {

                    if (hasFocus) {

                        //got focus

                        if (EMOJI_KEYBOARD) {

                            mEmojiBtn.setVisibility(View.VISIBLE);
                        }

                    } else {

                        mEmojiBtn.setVisibility(View.GONE);
                    }
                }
            }
        });

        setEditTextMaxLength(POST_CHARACTERS_LIMIT);

        mPostEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                int cnt = s.length();

                if (cnt == 0) {

                    updateTitle();

                } else {

                    getActivity().setTitle(Integer.toString(POST_CHARACTERS_LIMIT - cnt));
                }
            }

        });

        mEmojiBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!popup.isShowing()) {

                    if (popup.isKeyBoardOpen()){

                        popup.showAtBottom();
                        setIconSoftKeyboard();

                    } else {

                        mPostEdit.setFocusableInTouchMode(true);
                        mPostEdit.requestFocus();
                        popup.showAtBottomPending();

                        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(mPostEdit, InputMethodManager.SHOW_IMPLICIT);
                        setIconSoftKeyboard();
                    }

                } else {

                    popup.dismiss();
                }
            }
        });

        EditTextImeBackListener er = new EditTextImeBackListener() {

            @Override
            public void onImeBack(EmojiconEditText ctrl, String text) {

                hideEmojiKeyboard();
            }
        };

        mPostEdit.setOnEditTextImeBackListener(er);

        updateTitle();
        updateProfileInfo();
        updateAccessMode();
        updateLocation();
        updateFeeling();
        updateMediaLayout();
        updateRepostLayout();

        // Inflate the layout for this fragment
        return rootView;
    }

    private void updateTitle() {

        if (isAdded()) {

            if (item.getId() != 0) {

                getActivity().setTitle(getText(R.string.title_edit_item));

            } else {

                getActivity().setTitle(getText(R.string.title_new_item));
            }
        }
    }

    private void updateProfileInfo() {

        if (isAdded()) {

            if (App.getInstance().getPhotoUrl() != null && App.getInstance().getPhotoUrl().length() > 0) {

                App.getInstance().getImageLoader().get(App.getInstance().getPhotoUrl(), ImageLoader.getImageListener(mPhoto, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

            } else {

                mPhoto.setImageResource(R.drawable.profile_default_photo);
            }

            mFullname.setText(App.getInstance().getFullname());
        }
    }

    private void updateAccessMode() {

        if (group_id != 0) {

            mAccessModeLayout.setVisibility(View.GONE);

        } else {

            mAccessModeLayout.setVisibility(View.VISIBLE);

            if (item.getAccessMode() == 0) {

                mAccessModeTitle.setText(getString(R.string.label_post_to_public));
                mAccessModeIcon.setImageResource(R.drawable.ic_unlock);

            } else {

                mAccessModeTitle.setText(getString(R.string.label_post_to_friends));
                mAccessModeIcon.setImageResource(R.drawable.ic_lock);
            }
        }
    }

    private void updateLocation() {

        String location = "";

        mLocationLayout.setVisibility(View.GONE);

        if (item.getCountry().length() > 0 || item.getCity().length() > 0) {

            if (item.getCountry().length() > 0) {

                location = item.getCountry();
            }

            if (item.getCity().length() > 0) {

                if (item.getCountry().length() > 0) {

                    location = location + ", " + item.getCity();

                } else {

                    location = item.getCity();
                }
            }

            if (location.length() > 0) {

                mLocationLayout.setVisibility(View.VISIBLE);
                mLocationTitle.setText(location);
            }
        }
    }

    public void setLocation() {

        item.setCountry(App.getInstance().getCountry());
        item.setCity(App.getInstance().getCity());

        item.setLat(App.getInstance().getLat());
        item.setLng(App.getInstance().getLng());

        updateLocation();
    }

    public void deleteLocation() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getText(R.string.dlg_delete_location_title));

        alertDialog.setMessage(getText(R.string.dlg_delete_location_subtitle));
        alertDialog.setCancelable(true);

        alertDialog.setNegativeButton(getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.setPositiveButton(getText(R.string.action_yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                item.setCountry("");
                item.setCity("");

                item.setLat(0.000000);
                item.setLng(0.000000);

                updateLocation();
            }
        });

        alertDialog.show();
    }

    private void updateFeeling() {

        mFeelingLayout.setVisibility(View.GONE);

        if (item.getFeeling() != 0) {

            ImageLoader imageLoader = App.getInstance().getImageLoader();

            imageLoader.get(Constants.WEB_SITE + "feelings/" + Integer.toString(item.getFeeling()) + ".png", ImageLoader.getImageListener(mFeelingIcon, R.drawable.mood, R.drawable.mood));

            mFeelingLayout.setVisibility(View.VISIBLE);
        }
    }

    public void deleteFeeling() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);

        arrayAdapter.add(getText(R.string.action_remove).toString());
        arrayAdapter.add(getText(R.string.action_edit).toString());

        builderSingle.setTitle(getText(R.string.dlg_delete_feeling_title));


        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {

                    case 0: {

                        item.setFeeling(0);

                        updateFeeling();

                        break;
                    }

                    default: {

                        choiceFeeling();

                        break;
                    }
                }
            }
        });

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        AlertDialog d = builderSingle.create();
        d.show();
    }

    private void choiceFeeling() {

        choiceFeelingDialog();

//        Intent intent = new Intent(getActivity(), SelectFeelingActivity.class);
//        intent.putExtra("profileId", 0);
//        startActivityForResult(intent, ITEM_FEELINGS);
    }

    private void updateMediaLayout() {

        mImagesLayout.setVisibility(View.GONE);

        if (item.getId() != 0 && itemsAdapter.getItemCount() == 0) {

            if (item.getVideoUrl().length() > 0) {

                itemsList.add(new MediaItem("", "", item.getPreviewVideoImgUrl(), item.getVideoUrl(), 1));
            }

            if (item.getImgUrl().length() > 0) {

                itemsList.add(new MediaItem("", "", item.getImgUrl(), "", 0));
            }

            if (item.getImagesCount() != 0) {

                item.setImagesCount(0);

                getMediaItems();
            }

            item.setImgUrl("");
            item.setVideoUrl("");
            item.setPreviewVideoImgUrl("");
            item.getMediaList().clear();
        }

        if (itemsAdapter.getItemCount() > 0) {

            mImagesLayout.setVisibility(View.VISIBLE);

            itemsAdapter.notifyDataSetChanged();
        }
    }

    private void updateRepostLayout() {

        mRepostLayout.setVisibility(View.GONE);

        if (item.getRePostId() != 0) {

            mRepostLayout.setVisibility(View.VISIBLE);

            if (item.getRePostFromUserPhotoUrl().length() != 0) {

                App.getInstance().getImageLoader().get(item.getRePostFromUserPhotoUrl(), ImageLoader.getImageListener(mRepostAuthorPhoto, R.drawable.profile_default_photo, R.drawable.profile_default_photo));

            } else {

                mRepostAuthorPhoto.setImageResource(R.drawable.img_loading);
            }

            mRepostAuthorTitle.setText(item.getRePostFromUserFullname());

            if (item.getRePostImgUrl().length() != 0) {

                App.getInstance().getImageLoader().get(item.getRePostImgUrl(), ImageLoader.getImageListener(mRepostImage, R.drawable.img_loading, R.drawable.img_loading));

                mRepostImage.setVisibility(View.VISIBLE);

            } else {

                mRepostImage.setVisibility(View.GONE);
            }

            if (item.getRePostPost().length() != 0) {

                mRepostContent.setText(item.getRePostPost());

                mRepostContent.setVisibility(View.VISIBLE);

            } else {

                mRepostContent.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {

                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Check GPS is enabled
                    LocationManager lm = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

                        mFusedLocationClient.getLastLocation().addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {

                                if (task.isSuccessful() && task.getResult() != null) {

                                    mLastLocation = task.getResult();

                                    // Set geo data to App class

                                    App.getInstance().setLat(mLastLocation.getLatitude());
                                    App.getInstance().setLng(mLastLocation.getLongitude());

                                    // Save data

                                    App.getInstance().saveData();

                                    // Get address

                                    App.getInstance().getAddress(App.getInstance().getLat(), App.getInstance().getLng());

                                    setLocation();

                                } else {

                                    Log.d("GPS", "New Item getLastLocation:exception", task.getException());
                                }
                            }
                        });
                    }

                    setLocation();

                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) || !ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                        showNoLocationPermissionSnackbar();
                    }
                }

                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void showNoLocationPermissionSnackbar() {

        Snackbar.make(getView(), getString(R.string.label_no_location_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                openApplicationSettings();

                Toast.makeText(getActivity(), getString(R.string.label_grant_location_permission), Toast.LENGTH_SHORT).show();
            }

        }).show();
    }

    public void openApplicationSettings() {

        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
        startActivityForResult(appSettingsIntent, 10001);
    }

    public void setEditTextMaxLength(int length) {

        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(length);
        mPostEdit.setFilters(FilterArray);
    }

    public void hideEmojiKeyboard() {

        popup.dismiss();
    }

    public void setIconEmojiKeyboard() {

        mEmojiBtn.setBackgroundResource(R.drawable.ic_emoji);
    }

    public void setIconSoftKeyboard() {

        mEmojiBtn.setBackgroundResource(R.drawable.ic_keyboard);
    }

    public void onDestroy() {

        super.onDestroy();

        hidepDialog();
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (pDialog == null) {

            initpDialog();
        }

        if (uploading) {

            pDialog.setMessage(getString(R.string.msg_uploading));
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        } else {

            if (compressing) {

                pDialog.setMessage(getString(R.string.msg_compressing_video));
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            }
        }

        pDialog.setProgressNumberFormat(null);
        pDialog.setProgress(0);
        pDialog.setMax(100);

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing() && pDialog != null) pDialog.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(STATE_LIST, itemsList);
        outState.putParcelable("item", item);

        outState.putBoolean("loading", loading);
        outState.putBoolean("loading", loading);
        outState.putBoolean("compressing", compressing);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_post: {

                hideEmojiKeyboard();

                this.item.setPost(mPostEdit.getText().toString().trim());

                if (itemsList.size() == 0 && this.item.getRePostId() == 0 && this.item.getPost().length() == 0) {

                    Toast toast= Toast.makeText(getActivity(), getText(R.string.msg_enter_item), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                } else {

                    loading = true;

                    if (itemsList.size() != 0) {

                        uploadImages(0);

                        return true;
                    }

                    sendPost();
                }

                return true;
            }

            default: {

                break;
            }
        }

        return false;
    }

    private void uploadImages(int index) {

        Log.e("uploadImages", "uploadImages:" + Integer.toString(index));

        if (itemsList.size() > 0) {

            if (index < itemsList.size()) {

                boolean need_upload = false;

                for (int i = 0; i < itemsList.size(); i++) {

                    if (itemsList.get(i).getImageUrl().length() == 0) {

                        need_upload = true;
                    }
                }

                if (need_upload) {

                    for (int i = index; i < itemsList.size(); i++) {

                        if (itemsList.get(i).getImageUrl().length() == 0) {

                            if (itemsList.get(i).getType() == 0) {

                                File f = new File(itemsList.get(i).getSelectedImageFileName());

                                uploadFile(METHOD_ITEMS_UPLOAD_IMG, f, i);

                            } else {

                                File f = new File(itemsList.get(i).getSelectedVideoFileName());
                                File f_thumb = new File(App.getInstance().getDirectory(), VIDEO_THUMBNAIL_FILE);

                                if (f.length() > VIDEO_FILE_MAX_SIZE) {

                                    compressingVideoFile(f, new File(App.getInstance().getDirectory(), VIDEO_DEST_FILE), i);

                                } else {

                                    uploadVideoFile(METHOD_VIDEO_UPLOAD, f, f_thumb, i);
                                }
                            }

                            break;
                        }
                    }

                } else {

                    sendPost();
                }

            } else {

                sendPost();
            }

        } else {

            sendPost();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 77 && resultCode == getActivity().RESULT_OK) {

            setLocation();

        } else if (requestCode == ITEM_FEELINGS && resultCode == getActivity().RESULT_OK) {


            item.setFeeling(data.getIntExtra("feeling", 0));

            updateFeeling();
        }
    }

    public void choiceImageAction() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);

        arrayAdapter.add(getText(R.string.action_gallery).toString());
        arrayAdapter.add(getText(R.string.action_camera).toString());

        builderSingle.setTitle(getText(R.string.dlg_choice_image_title));


        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {

                    case 0: {

                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/jpeg");

                        imgFromGalleryActivityResultLauncher.launch(intent);

                        break;
                    }

                    default: {

                        Helper helper = new Helper(getActivity());

                        if (helper.checkPermission(Manifest.permission.CAMERA)) {

                            try {

                                selectedCameraImg = Helper.randomString(6) + ".jpg";

                                Uri selectedImage = FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(App.getInstance().getDirectory(), selectedCameraImg));

                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedImage);
                                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                                imgFromCameraActivityResultLauncher.launch(cameraIntent);

                            } catch (Exception e) {

                                Log.e("Camera", "Error occured. Please try again later.");
                            }

                        } else {

                            requestCameraPermission();
                        }

                        break;
                    }
                }
            }
        });

        builderSingle.setNegativeButton(getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        AlertDialog d = builderSingle.create();
        d.show();
    }

    public void sendPost() {

        item.getMediaList().clear();
        item.setImgUrl("");
        item.setVideoUrl("");
        item.setPreviewVideoImgUrl("");
        item.setImagesCount(0);

        if (itemsList.size() != 0) {

            for (int i = 0; i < itemsList.size(); i++) {

                if (itemsList.get(i).getType() == 0) {

                    if (item.getImgUrl().length() == 0) {

                        item.setImgUrl(itemsList.get(i).getImageUrl());

                    } else {

                        item.getMediaList().add(itemsList.get(i));
                        item.setImagesCount(item.getImagesCount() + 1);
                    }

                } else {

                    item.setVideoUrl(itemsList.get(i).getVideoUrl());
                    item.setPreviewVideoImgUrl(itemsList.get(i).getImageUrl());
                }
            }
        }

        if (this.item.getId() != 0) {

            savePost();

        } else {

            newPost();
        }
    }

    private void savePost() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEMS_EDIT, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {


                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            savePostSuccess();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                savePostSuccess();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("groupId", Long.toString(group_id));
                params.put("postId", Long.toString(item.getId()));
                params.put("rePostId", Long.toString(item.getRePostId()));
                params.put("postMode", Integer.toString(item.getAccessMode()));
                params.put("postText", item.getPost());
                params.put("postImg", item.getImgUrl());
                params.put("postArea", item.getArea());
                params.put("postCountry", item.getCountry());
                params.put("postCity", item.getCity());
                params.put("postLat", Double.toString(item.getLat()));
                params.put("postLng", Double.toString(item.getLng()));

                params.put("feeling", Integer.toString(item.getFeeling()));

                if (item.getMediaList().size() != 0) {

                    Collections.reverse(item.getMediaList());

                    for (int i = 0; i < item.getMediaList().size(); i++) {

                        if (item.getMediaList().get(i).getType() == 0) {

                            params.put("images[" + i + "]", item.getMediaList().get(i).getImageUrl());
                        }
                    }
                }

                params.put("videoImgUrl", item.getPreviewVideoImgUrl());
                params.put("videoUrl", item.getVideoUrl());

                return params;
            }
        };

        int socketTimeout = 0;//0 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void savePostSuccess() {

        loading = false;

        hidepDialog();

        if (isAdded()) {

            Intent i = new Intent();
            i.putExtra("item", item);
            i.putExtra("position", position);

            getActivity().setResult(RESULT_OK, i);

            Toast.makeText(getActivity(), getText(R.string.msg_item_saved), Toast.LENGTH_SHORT).show();

            deleteTmpDir();

            getActivity().finish();
        }
    }

    private void newPost() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEMS_NEW, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {


                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            sendPostSuccess();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                sendPostSuccess();

//                     Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("groupId", Long.toString(group_id));
                params.put("rePostId", Long.toString(item.getRePostId()));
                params.put("postMode", Integer.toString(item.getAccessMode()));
                params.put("postText", item.getPost());
                params.put("postImg", item.getImgUrl());
                params.put("postArea", item.getArea());
                params.put("postCountry", item.getCountry());
                params.put("postCity", item.getCity());
                params.put("postLat", Double.toString(item.getLat()));
                params.put("postLng", Double.toString(item.getLng()));

                params.put("feeling", Integer.toString(item.getFeeling()));

                if (item.getMediaList().size() != 0) {

                    Collections.reverse(item.getMediaList());

                    for (int i = 0; i < item.getMediaList().size(); i++) {

                        if (item.getMediaList().get(i).getType() == 0) {

                            params.put("images[" + i + "]", item.getMediaList().get(i).getImageUrl());
                        }
                    }
                }

                params.put("videoImgUrl", item.getPreviewVideoImgUrl());
                params.put("videoUrl", item.getVideoUrl());

                return params;
            }
        };

        int socketTimeout = 0;//0 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void sendPostSuccess() {

        loading = false;

        hidepDialog();

        if (isAdded()) {

            Intent i = new Intent();
            getActivity().setResult(RESULT_OK, i);

            Toast.makeText(getActivity(), getText(R.string.msg_item_posted), Toast.LENGTH_SHORT).show();

            deleteTmpDir();

            // Interstitial ad

            if (App.getInstance().getInterstitialAdSettings().getInterstitialAdAfterNewItem() != 0 && App.getInstance().getAdmob() == ADMOB_DISABLED) {

                App.getInstance().getInterstitialAdSettings().setCurrentInterstitialAdAfterNewItem(App.getInstance().getInterstitialAdSettings().getCurrentInterstitialAdAfterNewItem() + 1);

                if (App.getInstance().getInterstitialAdSettings().getCurrentInterstitialAdAfterNewItem() >= App.getInstance().getInterstitialAdSettings().getInterstitialAdAfterNewItem()) {

                    App.getInstance().getInterstitialAdSettings().setCurrentInterstitialAdAfterNewItem(0);

                    App.getInstance().showInterstitialAd(null);
                }

                App.getInstance().saveData();
            }

            //

            getActivity().finish();
        }
    }

    private void deleteTmpDir() {

        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + APP_TEMP_FOLDER);

        try {

            if (dir.exists()) {

                File[] entries = dir.listFiles();

                for (File currentFile: entries){

                    currentFile.delete();
                }
            }

        } catch (Exception e) {

            Log.e("deleteTmpDir", "Unable to delete tmp folder");
        }
    }



    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
    }

    @Override
    public void onDetach() {

        super.onDetach();
    }


    private void showBottomSheet() {

        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        if (App.getInstance().getCountry().length() == 0 && App.getInstance().getCity().length() == 0) {

            if (App.getInstance().getLat() != 0.000000 && App.getInstance().getLng() != 0.000000) {

                App.getInstance().getAddress(App.getInstance().getLat(), App.getInstance().getLng());
            }
        }

        final View view = getLayoutInflater().inflate(R.layout.item_editor_sheet_list, null);

        MaterialRippleLayout mAddImageButton = (MaterialRippleLayout) view.findViewById(R.id.add_image_button);
        MaterialRippleLayout mAddVideoButton = (MaterialRippleLayout) view.findViewById(R.id.add_video_button);
        MaterialRippleLayout mCaptureVideoButton = (MaterialRippleLayout) view.findViewById(R.id.capture_video_button);
        MaterialRippleLayout mAddLocationButton = (MaterialRippleLayout) view.findViewById(R.id.add_location_button);
        MaterialRippleLayout mAddFeelingButton = (MaterialRippleLayout) view.findViewById(R.id.add_feeling_button);

        mAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mBottomSheetDialog != null) {

                    mBottomSheetDialog.dismiss();
                }

                if (getMediaCount(0) < IMAGE_FILES_LIMIT) {

                    Helper helper = new Helper(App.getInstance().getApplicationContext());

                    if (!helper.checkStoragePermission()) {

                        requestStoragePermission();

                    } else {

                        choiceImageAction();
                    }

                } else {

                    Toast.makeText(getActivity(), String.format(Locale.getDefault(), getString(R.string.images_limit_of), IMAGE_FILES_LIMIT), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mAddVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mBottomSheetDialog != null) {

                    mBottomSheetDialog.dismiss();
                }

                if (getMediaCount(1) < VIDEO_FILES_LIMIT) {

                    Helper helper = new Helper(App.getInstance().getApplicationContext());

                    if (!helper.checkVideoPermission()) {

                        requestStoragePermission();

                    } else {

                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("video/mp4");

                        videoFromGalleryActivityResultLauncher.launch(intent);
                    }

                } else {

                    Toast.makeText(getActivity(), String.format(Locale.getDefault(), getString(R.string.video_limit_of), VIDEO_FILES_LIMIT), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCaptureVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mBottomSheetDialog != null) {

                    mBottomSheetDialog.dismiss();
                }

                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                    captureVideo();

                } else {

                    requestVideoCapturePermission();
                }
            }
        });

        mAddLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mBottomSheetDialog != null) {

                    mBottomSheetDialog.dismiss();
                }

                if (App.getInstance().getCountry().length() != 0 || App.getInstance().getCity().length() != 0) {

                    setLocation();

                } else {

                    if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)){

                            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

                        } else {

                            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
                        }

                    } else {

                        Intent i = new Intent(getActivity(), LocationActivity.class);
                        startActivityForResult(i, 77);
                    }
                }
            }
        });

        mAddFeelingButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mBottomSheetDialog != null) {

                    mBottomSheetDialog.dismiss();
                }

                choiceFeeling();
            }
        });

        mBottomSheetDialog = new BottomSheetDialog(getActivity());

        mBottomSheetDialog.setContentView(view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBottomSheetDialog.show();

        doKeepDialog(mBottomSheetDialog);

        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {

                mBottomSheetDialog = null;
            }
        });
    }

    // Prevent dialog dismiss when orientation changes
    private static void doKeepDialog(Dialog dialog){

        WindowManager.LayoutParams lp = new  WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
    }

    public void captureVideo() {

        Intent intent = new Intent(getActivity(), CaptureActivity.class);
        //intent.putExtra("profileId", obj.getId());
        videoFromCameraActivityResultLauncher.launch(intent);
    }

    private int getMediaCount(int type) {

        int count = 0;

        if (itemsList.size() > 0) {

            for (int i = 0; i < itemsList.size(); i++) {

                if (itemsList.get(i).getType() == type) {

                    count++;
                }
            }
        }

        return count;
    }

    public Boolean uploadFile(String serverURL, File file, final int index) {

        loading = true;
        uploading = true;

        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setProgressNumberFormat(null);
        pDialog.setProgress(0);
        pDialog.setMax(100);

        showpDialog();

        final CountingRequestBody.Listener progressListener = new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(long bytesRead, long contentLength) {

                if (bytesRead >= contentLength) {

                    if (isAdded() && getActivity() != null && pDialog != null) {

                        requireActivity().runOnUiThread(new Runnable() {

                            public void run() {
                                //            progressBar.setVisibility(View.GONE);
                            }
                        });
                    }

                } else {

                    if (contentLength > 0) {

                        final int progress = (int) (((double) bytesRead / contentLength) * 100);

                        if (isAdded() && getActivity() != null && pDialog != null) {

                            requireActivity().runOnUiThread(new Runnable() {

                                public void run() {

                                    pDialog.setProgress(progress);
//                                    progressBar.setVisibility(View.VISIBLE);
//                                    progressBar.setProgress(progress);
                                }
                            });
                        }

                        if (progress >= 100) {

                            if (isAdded() && getActivity() != null && pDialog != null) {

                                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            }
                        }

                        Log.e("uploadProgress called", progress+" ");
                    }
                }
            }
        };

        final okhttp3.OkHttpClient client = new okhttp3.OkHttpClient().newBuilder().addNetworkInterceptor(new Interceptor() {

                    @NonNull
                    @Override
                    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {

                        okhttp3.Request originalRequest = chain.request();

                        if (originalRequest.body() == null) {

                            return chain.proceed(originalRequest);
                        }

                        okhttp3.Request progressRequest = originalRequest.newBuilder()
                                .method(originalRequest.method(),
                                        new CountingRequestBody(originalRequest.body(), progressListener))
                                .build();

                        return chain.proceed(progressRequest);

                    }
                })
                .build();

        //client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));

        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());

        try {

            okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("uploaded_file", file.getName(), okhttp3.RequestBody.create(file, okhttp3.MediaType.parse(mime)))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .build();

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(serverURL)
                    .addHeader("Accept", "application/json;")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                    uploading = false;
                    loading = false;
                    compressing = false;

                    hidepDialog();

                    Log.e("failure", request.toString() + "|" + e.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {

                    String jsonData = response.body().string();

                    try {

                        JSONObject result = new JSONObject(jsonData);

                        if (!result.getBoolean("error")) {

                            itemsList.get(index).setImageUrl(result.getString("imgUrl"));

                        } else {

                            itemsList.remove(index);

                            //Toast.makeText(getActivity(), result.getString("error_description"), Toast.LENGTH_SHORT).show();
                        }

                        Log.d("My App", response.toString());

                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");

                    } finally {

                        Log.e("response", jsonData);

                        uploadImages(index);
                    }
                }

            });

            return true;

        } catch (Exception ex) {
            // Handle the error

            uploading = false;
            loading = false;
            compressing = false;

            hidepDialog();
        }

        return false;
    }

    private void compressingVideoFile(File src, File dest, final int index) {

        loading = true;
        compressing = true;

        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setProgressNumberFormat(null);
        pDialog.setProgress(0);
        pDialog.setMax(100);

        showpDialog();

        Transcoder.into(dest.getPath())
                .addDataSource(src.getPath()) // or...
                .setListener(new TranscoderListener() {
                    public void onTranscodeProgress(double progress) {

                        double prgs = progress * 100;

                        pDialog.setProgress((int) prgs);

                        Log.e("Dimon", "onTranscodeProgress:" + Integer.valueOf((int) prgs).toString());
                    }

                    public void onTranscodeCompleted(int successCode) {

                        uploading = false;
                        loading = false;
                        compressing = false;

                        pDialog.hide();

                        Log.e("compressing", "onTranscodeCompleted");
                        Log.e("compressing src", Long.toString(src.length()));
                        Log.e("compressing dest", Long.toString(dest.length()));

                        File f = new File(App.getInstance().getDirectory(), VIDEO_DEST_FILE);
                        File f_thumb = new File(App.getInstance().getDirectory(), VIDEO_THUMBNAIL_FILE);

                        uploadVideoFile(METHOD_GALLERY_UPLOAD_VIDEO, f, f_thumb, index);
                    }

                    public void onTranscodeCanceled() {

                        uploading = false;
                        loading = false;
                        compressing = false;

                        Log.e("compressing", "onTranscodeCanceled");
                        pDialog.hide();
                    }

                    public void onTranscodeFailed(@NonNull Throwable exception) {

                        uploading = false;
                        loading = false;
                        compressing = false;

                        pDialog.hide();
                        Log.e("compressing", exception.toString());
                    }
                }).transcode();
    }

    public Boolean uploadVideoFile(String serverURL, File videoFile,  File videoThumb, final int index) {

        loading = true;
        uploading = true;

        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setProgressNumberFormat(null);
        pDialog.setProgress(0);
        pDialog.setMax(100);

        showpDialog();

        final CountingRequestBody.Listener progressListener = new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(long bytesRead, long contentLength) {

                if (bytesRead >= contentLength) {

                    if (isAdded() && getActivity() != null && pDialog != null) {

                        requireActivity().runOnUiThread(new Runnable() {

                            public void run() {
                                //            progressBar.setVisibility(View.GONE);
                            }
                        });
                    }

                } else {

                    if (contentLength > 0) {

                        final int progress = (int) (((double) bytesRead / contentLength) * 100);

                        if (isAdded() && getActivity() != null && pDialog != null) {

                            requireActivity().runOnUiThread(new Runnable() {

                                public void run() {

                                    pDialog.setProgress(progress);
                                }
                            });
                        }

                        if (progress >= 100) {

                            if (isAdded() && getActivity() != null && pDialog != null) {

                                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            }
                        }

                        Log.e("uploadProgress called", progress+" ");
                    }
                }
            }
        };

        final okhttp3.OkHttpClient client = new okhttp3.OkHttpClient().newBuilder().addNetworkInterceptor(new Interceptor() {

                    @NonNull
                    @Override
                    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {

                        okhttp3.Request originalRequest = chain.request();

                        if (originalRequest.body() == null) {

                            return chain.proceed(originalRequest);
                        }

                        okhttp3.Request progressRequest = originalRequest.newBuilder()
                                .method(originalRequest.method(),
                                        new CountingRequestBody(originalRequest.body(), progressListener))
                                .build();

                        return chain.proceed(progressRequest);

                    }
                })
                .connectTimeout(OKHTTP3_REQUEST_SECONDS, TimeUnit.SECONDS)
                .readTimeout(OKHTTP3_REQUEST_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(OKHTTP3_REQUEST_SECONDS, TimeUnit.SECONDS)
                .build();

        //client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));

        String vidFileExtension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(videoFile).toString());
        String thumbFileExtension = MimeTypeMap.getFileExtensionFromUrl( Uri.fromFile(videoThumb).toString());
        String vidMime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(vidFileExtension.toLowerCase());
        String thumbMime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(thumbFileExtension.toLowerCase());

        try {

            okhttp3.RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("uploaded_file", videoThumb.getName(), okhttp3.RequestBody.create(videoThumb, okhttp3.MediaType.parse(thumbMime)))
                    .addFormDataPart("uploaded_video_file", videoFile.getName(), okhttp3.RequestBody.create(videoFile, okhttp3.MediaType.parse(vidMime)))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .build();

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(serverURL)
                    .addHeader("Accept", "application/json;")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                    uploading = false;
                    loading = false;
                    compressing = false;

                    hidepDialog();

                    Log.e("failure", request.toString());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {

                    String jsonData = response.body().string();

                    Log.e("response", jsonData);

                    try {

                        JSONObject result = new JSONObject(jsonData);

                        if (!result.getBoolean("error")) {

                            itemsList.get(index).setImageUrl(result.getString("imgFileUrl"));
                            itemsList.get(index).setVideoUrl(result.getString("videoFileUrl"));
                        }

                        Log.d("My App", response.toString());

                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");

                    } finally {

                        Log.e("response", jsonData);

                        uploadImages(index);
                    }
                }

            });

            return true;

        } catch (Exception ex) {
            // Handle the error

            uploading = false;
            loading = false;
            compressing = false;

            hidepDialog();
        }

        return false;
    }

    public void getMediaItems() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEM_GET_IMAGES, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "NewItemFragment Not Added to Activity");

                            return;
                        }

                        try {

                            int arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                if (response.has("items")) {

                                    JSONArray itemsArray = response.getJSONArray("items");

                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);

                                            MediaItem item = new MediaItem();

                                            item.setImageUrl(itemObj.getString("imgUrl"));
                                            item.setType(0);

                                            itemsList.add(item);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            updateMediaLayout();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "NewItemFragment Not Added to Activity");

                    return;
                }

                updateMediaLayout();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Long.toString(item.getId()));
                params.put("language", "en");

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    private void choiceFeelingDialog() {

        final FeelingsListAdapter feelingsAdapter;

        feelingsAdapter = new FeelingsListAdapter(getActivity(), App.getInstance().getFeelingsList());

        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_feelings);
        dialog.setCancelable(true);

        final ProgressBar mProgressBar = (ProgressBar) dialog.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        TextView mDlgTitle = (TextView) dialog.findViewById(R.id.title_label);
        mDlgTitle.setText(R.string.dlg_choice_feeling_title);

        AppCompatButton mDlgCancelButton = (AppCompatButton) dialog.findViewById(R.id.cancel_button);
        mDlgCancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        NestedScrollView mDlgNestedView = (NestedScrollView) dialog.findViewById(R.id.nested_view);
        final RecyclerView mDlgRecyclerView = (RecyclerView) dialog.findViewById(R.id.recycler_view);

        final LinearLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), Helper.getStickersGridSpanCount(getActivity()));
        mDlgRecyclerView.setLayoutManager(mLayoutManager);
        mDlgRecyclerView.setHasFixedSize(true);
        mDlgRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mDlgRecyclerView.setAdapter(feelingsAdapter);

        mDlgRecyclerView.setNestedScrollingEnabled(true);

        feelingsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {

                super.onChanged();

                if (App.getInstance().getFeelingsList().size() != 0) {

                     mDlgRecyclerView.setVisibility(View.VISIBLE);
                     mProgressBar.setVisibility(View.GONE);
                }
            }
        });

        feelingsAdapter.setOnItemClickListener(new FeelingsListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, Feeling obj, int position) {

                item.setFeeling(position);

                updateFeeling();

                dialog.dismiss();
            }
        });

        if (App.getInstance().getFeelingsList().size() == 0) {

            mDlgRecyclerView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);

            Api api = new Api(getActivity());
            api.getFeelings(feelingsAdapter);
        }

        dialog.show();

        doKeepDialog(dialog);
    }

    private void requestStoragePermission() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

            storagePermissionLauncher.launch(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.READ_MEDIA_VIDEO, android.Manifest.permission.ACCESS_MEDIA_LOCATION});

        } else {

            storagePermissionLauncher.launch(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
        }
    }

    private void requestCameraPermission() {

        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void requestVideoCapturePermission() {

        videoCapturePermissionLauncher.launch(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA});
    }
}