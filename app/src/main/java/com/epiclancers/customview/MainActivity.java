package com.epiclancers.customview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private ModulesView modulesView;
    private boolean[] modules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modulesView = findViewById(R.id.moduleView);
        setUpModuleRectangles();

    }

    private void setUpModuleRectangles() {
        int correctNumberOfModules = 20;
        int incompleteModules = correctNumberOfModules/2;
        modules = new boolean[correctNumberOfModules];
        for (int i = 0; i < incompleteModules; i++) {
            modules[i] = true;
        }
        modulesView.setModules(modules);
    }
}
