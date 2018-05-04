package uz.shukurov.thewallman.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import uz.shukurov.thewallman.R;
import uz.shukurov.thewallman.adapters.PixabayImageListAdapter;
import uz.shukurov.thewallman.api_services.InternetCheck;
import uz.shukurov.thewallman.api_services.PixabayService;
import uz.shukurov.thewallman.listeners.InfiniteScrollListener;
import uz.shukurov.thewallman.models.PixabayImage;
import uz.shukurov.thewallman.models.PixabayImageList;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private List<PixabayImage> pixabayImageList;
    private PixabayImageListAdapter pixabayImageListAdapter;
    private InfiniteScrollListener infiniteScrollListener;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private TextView noResults;
    private MenuItem searchMenuItem;
    private String currentQuery = "";
    private String currentOrientation = "";
    private ListView listView;
    private ArrayList<String> categoryList;
    private BottomNavigationView navigation;
    private boolean imageOrientation = false;
    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initRecyclerView();
        initToolbar();
        initListView();



        //Check internet connection...
        if (!InternetCheck.isInternetAvailable(this))
            initSnackbar(R.string.no_internet);
        else loadImages(1, currentQuery,currentOrientation);
    }

    private void initListView() {
        categoryList = new ArrayList<String>();
        getAnimalNames();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, categoryList);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3)
            {

                String category=categoryList.get(position);

                currentQuery = category;
                resetImageList();
                progressBar.setVisibility(View.VISIBLE);
                noResults.setVisibility(View.GONE);
                loadImages(1, currentQuery,currentOrientation);

                recyclerView.setVisibility(View.VISIBLE);
                listView.setVisibility(View.INVISIBLE);



                Toast.makeText(getApplicationContext(), "Category Selected : "+category,   Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getAnimalNames()
    {
        categoryList.add("Nature");
        categoryList.add("Fish");
        categoryList.add("Space");
        categoryList.add("Girls");
        categoryList.add("Star Wars");
        categoryList.add("Backgrounds");
        categoryList.add("Food");
        categoryList.add("Sports");
        categoryList.add("Music");
        categoryList.add("Computer");
        categoryList.add("Fruits");
        categoryList.add("Space");
        categoryList.add("White");
        categoryList.add("Black");
    }

    private void initViews() {
        recyclerView = (RecyclerView) findViewById(R.id.activity_main_list);
        progressBar = (ProgressBar) findViewById(R.id.activity_main_progress);
        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        noResults = (TextView) findViewById(R.id.activity_main_no_results_text);
        listView = (ListView) findViewById(R.id.listView);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        menu = navigation.getMenu();


    }

    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        pixabayImageList = new ArrayList<>();
        pixabayImageListAdapter = new PixabayImageListAdapter(pixabayImageList);
        recyclerView.setAdapter(pixabayImageListAdapter);
        initInfiniteScrollListener(mLayoutManager);
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
    }

    private void initSnackbar(int messageId) {
        progressBar.setVisibility(View.GONE);
        Snackbar snackbar = Snackbar.make(recyclerView, messageId, Snackbar.LENGTH_INDEFINITE).setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InternetCheck.isInternetAvailable(v.getContext())) {
                    resetImageList();
                    progressBar.setVisibility(View.VISIBLE);
                    loadImages(1, currentQuery,currentOrientation);

                } else initSnackbar(R.string.no_internet);
            }
        });
        snackbar.show();
    }

    private void initInfiniteScrollListener(LinearLayoutManager mLayoutManager) {
        infiniteScrollListener = new InfiniteScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page) {
                progressBar.setVisibility(View.VISIBLE);
                loadImages(page, currentQuery,currentOrientation);
            }
        };
        recyclerView.addOnScrollListener(infiniteScrollListener);
    }



    //Loading images from API, checking whether query is successful or not...
    private void loadImages(int page, String query, String currentOrientation) {
        PixabayService.createPixabayService().getImageResults(getString(R.string.PIXABAY_API_KEY),query,currentOrientation, page, 20).enqueue(new Callback<PixabayImageList>() {

            @Override
            public void onResponse(Call<PixabayImageList> call, Response<PixabayImageList> response) {
                if (response.isSuccessful())
                    addImagesToList(response.body());
                else progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<PixabayImageList> call, Throwable t) {
                initSnackbar(R.string.error);
            }

        });
    }



    private void addImagesToList(PixabayImageList response) {
        progressBar.setVisibility(View.GONE);

        int position = pixabayImageList.size();
        pixabayImageList.addAll(response.getHits());
        pixabayImageListAdapter.notifyItemRangeInserted(position, position + 20);


        if (pixabayImageList.isEmpty()) noResults.setVisibility(View.VISIBLE);
        else noResults.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(searchListener);
        return true;
    }


    private SearchView.OnQueryTextListener searchListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            searchMenuItem.collapseActionView();
            currentQuery = query;
            resetImageList();

            recyclerView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            noResults.setVisibility(View.GONE);

            loadImages(1, currentQuery,currentOrientation);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    private void resetImageList() {
        pixabayImageList.clear();
        infiniteScrollListener.resetCurrentPage();
        pixabayImageListAdapter.notifyDataSetChanged();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    currentQuery = "";
                    resetImageList();
                    progressBar.setVisibility(View.VISIBLE);
                    noResults.setVisibility(View.GONE);
                    loadImages(1, currentQuery,currentOrientation);


                    recyclerView.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.INVISIBLE);

                    menu.findItem(R.id.navigation_notifications).setTitle(getString(R.string.title_orientation)).setIcon(R.drawable.ic_original_black_24dp);
                    currentOrientation = "horizontal";

                    return true;
                case R.id.navigation_dashboard:

                    recyclerView.setVisibility(View.INVISIBLE);
                    listView.setVisibility(View.VISIBLE);

                    return true;
                case R.id.navigation_notifications:

                    changeNavigationOrientation();


                    resetImageList();
                    progressBar.setVisibility(View.VISIBLE);
                    noResults.setVisibility(View.GONE);
                    loadImages(1, currentQuery,currentOrientation);


                    recyclerView.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.INVISIBLE);

                    return true;
            }
            return false;
        }
    };

    private void changeNavigationOrientation() {

        if(!imageOrientation){
            menu.findItem(R.id.navigation_notifications).setTitle("Horizontal");
            currentOrientation = "horizontal";
            imageOrientation = true;
            menu.findItem(R.id.navigation_notifications).setIcon(R.drawable.ic_landscape_black_24dp);
        }
        else {
            menu.findItem(R.id.navigation_notifications).setTitle("Vertical");
            currentOrientation = "vertical";
            imageOrientation = false;
            menu.findItem(R.id.navigation_notifications).setIcon(R.drawable.ic_portrait_black_24dp);
        }

    }

}
