package com.fanalbin.soedcare.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.fanalbin.soedcare.R

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        setupToolbar()
        if (savedInstanceState == null) {
            loadFragment()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set up navigation
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Set toolbar title
        supportActionBar?.title = getToolbarTitle()
    }

    protected open fun getToolbarTitle(): String = ""

    protected open fun shouldShowBackButton(): Boolean = true

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    private fun loadFragment() {
        val fragment = createFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitNow()
    }

    abstract fun createFragment(): Fragment
}