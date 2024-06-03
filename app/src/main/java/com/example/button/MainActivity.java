package com.example.button;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.button.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 1;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private boolean isServiceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION);
        }

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });

        Button buttonFirst = findViewById(R.id.buttonFirst);
        buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning) {
                    stopOverlayService();
                } else {
                    if (Settings.canDrawOverlays(MainActivity.this)) {
                        startOverlayService();
                    } else {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION);
                    }
                }
            }
        });

        SeekBar seekBarTransparency = findViewById(R.id.seekBarTransparency);
        seekBarTransparency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float alpha = progress / 100.0f;
                Intent intent = new Intent(MainActivity.this, FloatingButtonService.class);
                intent.putExtra("EXTRA_ALPHA", alpha);
                startService(intent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        SeekBar seekBarSize = findViewById(R.id.seekBarSize);
        seekBarSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Intent intent = new Intent(MainActivity.this, FloatingButtonService.class);
                intent.putExtra("EXTRA_SIZE", progress);
                startService(intent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        Button colorButtonRed = findViewById(R.id.colorButtonRed);
        Button colorButtonGreen = findViewById(R.id.colorButtonGreen);
        Button colorButtonBlue = findViewById(R.id.colorButtonBlue);
        Button colorButtonBrown = findViewById(R.id.colorButtonBrown);
        Button colorButtonGrey = findViewById(R.id.colorButtonGrey);
        Button colorButtonBlack = findViewById(R.id.colorButtonBlack);

        View.OnClickListener colorClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int color = Color.WHITE;
                if (v.getId() == R.id.colorButtonRed) {
                    color = Color.RED;
                } else if (v.getId() == R.id.colorButtonGreen) {
                    color = Color.GREEN;
                } else if (v.getId() == R.id.colorButtonBlue) {
                    color = Color.BLUE;
                } else if(v.getId() == R.id.colorButtonBrown) {
                    color = Color.parseColor("#A52A2A");
                } else if (v.getId() == R.id.colorButtonGrey) {
                    color = Color.parseColor("#757575");
                } else if (v.getId() == R.id.colorButtonBlack) {
                    color = Color.BLACK;
                }

                Intent intent = new Intent(MainActivity.this, FloatingButtonService.class);
                intent.putExtra("EXTRA_COLOR", color);
                startService(intent);
            }
        };

        colorButtonRed.setOnClickListener(colorClickListener);
        colorButtonGreen.setOnClickListener(colorClickListener);
        colorButtonBlue.setOnClickListener(colorClickListener);
        colorButtonBrown.setOnClickListener(colorClickListener);
        colorButtonGrey.setOnClickListener(colorClickListener);
        colorButtonBlack.setOnClickListener(colorClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                startOverlayService();
            } else {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startOverlayService() {
        Intent intent = new Intent(this, FloatingButtonService.class);
        startService(intent);
        isServiceRunning = true;
    }

    private void stopOverlayService() {
        Intent intent = new Intent(this, FloatingButtonService.class);
        stopService(intent);
        isServiceRunning = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
