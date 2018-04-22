package com.example.jingj.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by jingj on 2018/3/11.
 */

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String FULL_IMAGE = "full_picture";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    private static final int REQUEST_PHOTO = 2;

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private String phoneNumber = null;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    public static CrimeFragment newInstance(UUID crimeID){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeID);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        mFragmentManager = getFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        mTitleField = v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //这个方法暂时不用
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                //同上
            }
        });
        mDateButton = v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DatePickerActivity.class);
                intent.putExtra(DatePickerFragment.ARG_DATE, mCrime.getDate());
                startActivityForResult(intent, REQUEST_DATE);
            }
        });
        mSolvedCheckBox = v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });
        mReportButton = v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareCompat.IntentBuilder i = ShareCompat.IntentBuilder.from(getActivity());
                i.setType("text/plain")
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setChooserTitle(getString(R.string.send_report))
                        .createChooserIntent();
                i.startChooser();
            }
        });
        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        //检查是否存在联系人应用
        final PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        mPhotoButton = v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //检查是否有存储空间和能拍照的应用
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //把本地文件路径转化为相机能够看见的uri模式
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.jingj.android.criminalIntent.fileprovider", mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                //把相应相机所有的应用程序都获取写入criminalIntent的权限
                List<ResolveInfo> cameraActivities = getActivity().getPackageManager()
                        .queryIntentActivities(captureImage, packageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    //授予权限
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        mPhotoView = v.findViewById(R.id.crime_photo);
        final ViewTreeObserver mPhotoObserver = mPhotoView.getViewTreeObserver();
        mPhotoObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mPhotoObserver.isAlive()) {
                    mPhotoObserver.removeOnGlobalLayoutListener(this);
                }
                updatePhotoView(mPhotoView.getMeasuredWidth(), mPhotoView.getMaxHeight());
            }
        });
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoFragment photo = photoFragment.newInstance(mPhotoFile);
                photo.show(mFragmentManager, "photo");
            }
        });
        updatePhotoView(mPhotoView.getMeasuredWidth(), mPhotoView.getMaxHeight());
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK){
            return;
        }
        if (requestCode == REQUEST_DATE){
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        }
        else if (requestCode == REQUEST_CONTACT) {
            Uri contactUri = data.getData();
            //确定哪个区域想要查询
            String[] queryFields = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            Cursor c = getActivity().getContentResolver().query(contactUri,
                    queryFields, null, null, null);

            try {
                //再次确定获得了数据
                if (c.getCount() == 0) {
                    return;
                }
                //获得数据的第一行的第一列，这就是嫌疑犯的名字
                c.moveToFirst();
                String suspect = c.getString(0);
                Cursor n = getActivity().getContentResolver().query(contactUri,
                        null, null, null, null);
                n.moveToFirst();
                String contactId = n.getString(n.getColumnIndex(ContactsContract.Contacts._ID));
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
                //获取联系人电话号码
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.READ_CONTACTS)) {

                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_CONTACTS},
                                MY_PERMISSIONS_REQUEST_CALL_PHONE);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }
                Cursor phoneName = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "_id = " + contactId,
                        null,
                        null,
                        null);
                while (phoneName.moveToNext()) {
                    phoneNumber = phoneName.getString(phoneName.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
                phoneName.close();
                n.close();
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            //收回授予的权限
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.jingj.android.criminalIntent.fileprovider", mPhotoFile);

            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updatePhotoView(mPhotoView.getMeasuredWidth(), mPhotoView.getMeasuredHeight());
        }
    }

    private void call() {
        Uri number = Uri.parse("tel:" + phoneNumber);
        Intent i = new Intent(Intent.ACTION_DIAL, number);
        startActivity(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {

        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_PHONE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                call();
            } else
            {
                // Permission Denied
                Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(),
                dateString, solvedString, suspect);

        return report;
    }

    private void updatePhotoView(int width, int height) {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), width, height);
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.delete_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_crime:
                CrimeLab crimeLab = CrimeLab.get(getActivity());
                crimeLab.deleteCrime(mCrime);
                getActivity().finish();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}

