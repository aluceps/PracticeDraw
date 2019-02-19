package me.aluceps.practicedraw

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import me.aluceps.practicedraw.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupClick()
    }

    private fun setupClick() {
        binding.toolbar.undo.setOnClickListener { }
        binding.toolbar.redo.setOnClickListener { }
        binding.toolbar.reset.setOnClickListener { binding.drawView.reset() }
    }
}
