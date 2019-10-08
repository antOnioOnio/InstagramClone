package com.example.instagramclone.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.instagramclone.Profile.AccountSettingsActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.utils.FilePaths;
import com.example.instagramclone.utils.FileSearch;
import com.example.instagramclone.utils.GridImageAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";

    private static final int NUM_GRID_COLUMNS = 3;
    private static final String mApend = "file:/";

    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar mProgressbar;
    private Spinner directorySpinner;

    private ArrayList<String> directories;
    private String mSelectedImage;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        Log.d(TAG, "onCreateView: started");
        galleryImage = (ImageView) view.findViewById(R.id.galleryImageView);
        gridView = (GridView) view.findViewById(R.id.gridview);
        directorySpinner = (Spinner)  view.findViewById(R.id.spinnerDirectory);
        mProgressbar = (ProgressBar) view.findViewById(R.id.progressBar);
        mProgressbar.setVisibility(View.GONE);
        directories = new ArrayList<>();

        ImageView shareClose = (ImageView) view.findViewById(R.id.ivCloseShare);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing gallery fragment");
                getActivity().finish();
            }
        });

        // here is where we have to know where are we coming from, it could be either to change the profile picture or to share a picture
        TextView nextScreen = (TextView) view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if ( isRootTask()) {
                    Log.d(TAG, "onClick: navigating to the final share screen ");
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                    startActivity(intent);
                }else {
                    Log.d(TAG, "onClick: navigating to the account settings screen, changing profile photo ");
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        init();
        return view;
    }

    private boolean isRootTask(){
        if (((ShareActivity)getActivity()).getTask() == 0){
            Log.d(TAG, "isRootTask: is root task  from galleryFragment: " +((ShareActivity)getActivity()).getTask());
            return true;
        }else {
            Log.d(TAG, "isRootTask: is not root task from galleryFragment: " +((ShareActivity)getActivity()).getTask());

            return false; //--> is not the root task, so we coming from editprofile
        }
    }

    private void init(){
        FilePaths filePaths = new FilePaths();

        if (FileSearch.getDirectoryPaths(filePaths.PICTURES) != null){
            directories = FileSearch.getDirectoryPaths(filePaths.PICTURES);
        }
        directories.add(filePaths.CAMERA);

        ArrayList<String> directoryNames = new ArrayList<>();
        for ( int i= 0; i < directories.size(); i++){
            int index = directories.get(i).lastIndexOf("/");
            String string = directories.get(i).substring(index);
            directoryNames.add(string);
        }

        Log.d(TAG, "init: Directories hasta ahora--->"+directories.toString());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, directoryNames);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        directorySpinner.setAdapter(adapter);

        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: selected : " + directories.get(position));
                // setup our image grid for the directory chosen
                setupGridView(directories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setupGridView(String selectedDirectory){
        Log.d(TAG, "setupGridView: directory chosen " + selectedDirectory);
        final ArrayList<String> imgURLs = FileSearch.getFilePaths(selectedDirectory);

        // set the grid colum width
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        // use the grid adapter to adapt images to gridview
        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, mApend, imgURLs);
        gridView.setAdapter(adapter);


        //set the first image to be displayed when the activity fragment view is inflated
        try{
            setImage(imgURLs.get(0), galleryImage, mApend);
            mSelectedImage = imgURLs.get(0);
        }catch(ArrayIndexOutOfBoundsException e){
            Log.e(TAG, "setupGridView: ArrayIndexOutOfBoundsException" + e.getMessage() );
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected and image: " + imgURLs.get(position));
                setImage(imgURLs.get(position), galleryImage, mApend);
                mSelectedImage = imgURLs.get(position);


            }
        });
    }



    private void setImage(String imgURL, ImageView image, String append){
        Log.d(TAG, "setImage: setting image");

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgressbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressbar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressbar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressbar.setVisibility(View.INVISIBLE);

            }
        });


    }
}
