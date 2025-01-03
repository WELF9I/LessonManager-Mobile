package com.example.lessonmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lessonmanager.auth.LoginActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private static final int ADD_LESSON_REQUEST_CODE = 1;
    private static final int EDIT_LESSON_REQUEST_CODE = 2;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabAddLesson;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupToolbar();
        setupNavigationDrawer();
        setupViewPager();
        setupFab();
        updateNavigationHeader();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        fabAddLesson = findViewById(R.id.fabAddLesson);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Lesson Manager");
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                viewPager.setCurrentItem(0);
            } else if (id == R.id.nav_upcoming) {
                viewPager.setCurrentItem(0);
            } else if (id == R.id.nav_completed) {
                viewPager.setCurrentItem(1);
            } else if (id == R.id.nav_logout) {
                performLogout();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView userName = headerView.findViewById(R.id.nav_user_name);
        TextView userEmail = headerView.findViewById(R.id.nav_user_email);
        CircleImageView userImage = headerView.findViewById(R.id.nav_user_image);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userName.setText(currentUser.getDisplayName());
            userEmail.setText(currentUser.getEmail());
        }
    }

    private void performLogout() {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupViewPager() {
        LessonPagerAdapter pagerAdapter = new LessonPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Upcoming" : "Completed");
        }).attach();
    }

    private void setupFab() {
        fabAddLesson.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddLessonActivity.class);
            startActivityForResult(intent, ADD_LESSON_REQUEST_CODE);
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == ADD_LESSON_REQUEST_CODE || requestCode == EDIT_LESSON_REQUEST_CODE) && resultCode == RESULT_OK) {
            fetchLessonsFromFirebase();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchLessonsFromFirebase();
    }

    private void fetchLessonsFromFirebase() {
        db.collection("lessons").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    setupViewPager();
                })
                .addOnFailureListener(e -> {
                });
    }
}