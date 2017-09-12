package com.darklod.youtubedownloader;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;
import java.util.List;

import at.huber.youtubeExtractor.YouTubeUriExtractor;
import at.huber.youtubeExtractor.YtFile;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private List<Item> videos = new ArrayList<>();
    private RecyclerViewAdapter adapter;
    private ProgressBar progressBar;

    private Toolbar toolbar;
    private SearchBox search;

    private String searchText = "";
    private int maxResults = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search = (SearchBox) findViewById(R.id.searchbox);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbarText));

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_search && !search.isShown()) {
                    openSearch();
                    return true;
                }
                return false;
            }
        });

        adapter = new RecyclerViewAdapter(this, videos);
        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Item item) {
                Toast.makeText(MainActivity.this, "mp3", Toast.LENGTH_LONG).show();
            }
        }, new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Item item) {
                String youtubeLink = "http://youtube.com/watch?v=" + item.id.videoId;

                YouTubeUriExtractor ytEx = new YouTubeUriExtractor(getApplicationContext()) {
                    @Override
                    public void onUrisAvailable(String videoId, String videoTitle, SparseArray<YtFile> ytFiles) {
                        if (ytFiles != null) {
                            int itag = 22;
                            String downloadUrl = ytFiles.get(itag).getUrl();

                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
                            request.setDescription("");
                            request.setTitle("Downloading: " + videoTitle);
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, videoTitle + ".mp4");

                            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                            manager.enqueue(request);
                        }
                    }
                };

                ytEx.execute(youtubeLink);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(R.color.colorAccent),
                        PorterDuff.Mode.SRC_IN);
        progressBar.setVisibility(View.GONE);

        LoadLibrary();
    }

    private void openSearch() {
        search.revealFromMenuItem(R.id.action_search, this);
        search.setSearchListener(new SearchBox.SearchListener() {

            @Override
            public void onSearchOpened() {
                // Use this to tint the screen

            }

            @Override
            public void onSearchClosed() {
                // Use this to un-tint the screen
                closeSearch();
            }

            @Override
            public void onSearchTermChanged(String term) {
                // React to the search term changing
                // Called after it has updated results
            }

            @Override
            public void onSearch(String searchTerm) {
                toolbar.setTitle("Results for: " + searchTerm);
                progressBar.setVisibility(View.VISIBLE);
                findViewById(R.id.logo_image).setVisibility(View.GONE);
                performSearch(searchTerm, maxResults);
            }

            @Override
            public void onResultClick(SearchResult result) {
                //React to result being clicked
            }

            @Override
            public void onSearchCleared() {

            }
        });

    }

    private void closeSearch() {
        search.hideCircularlyToMenuItem(R.id.action_search, this);
        //if (search.getSearchText().isEmpty()) toolbar.setTitle("");
    }

    private void performSearch(String text, int maxResults) {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<SearchInfo> call = apiService.getVideosByKeyword("snippet", text, maxResults, ApiClient.API_KEY);
        call.enqueue(new Callback<SearchInfo>() {
            @Override
            public void onResponse(Call<SearchInfo> call, Response<SearchInfo> response) {
                SearchInfo info = response.body();

                videos.clear();
                videos.addAll(info.items);

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<SearchInfo> call, Throwable t) {
                // Log error here since request failed
                Log.e("PROVA", t.toString());
            }
        });
    }

    private void LoadLibrary() {
        FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
            Toast.makeText(getApplicationContext(), "Mp3 conversion is not supported by that device", Toast.LENGTH_LONG).show();
        }
    }

    private void ExecuteCommand(String[] cmd) {
        FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onProgress(String message) {
                }

                @Override
                public void onFailure(String message) {
                }

                @Override
                public void onSuccess(String message) {
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
            Toast.makeText(getApplicationContext(), "Already running", Toast.LENGTH_LONG).show();
        }
    }

    private void convertToMp3() {
        String[] cmds = new String[]{"a", "b"};
        ExecuteCommand(cmds);

        FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
       // ffmpeg.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

