package com.nearapps.keyboard_simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nearapps.keyboard_simple.databinding.ActivityMainBinding
import com.nearapps.library.keyboard_simple.KeyboardSimple

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    private lateinit var keyboardSimple : KeyboardSimple

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        keyboardSimple = KeyboardSimple(this, binding.keyboardParent)
        keyboardSimple.register(binding.txt1, KeyboardSimple.KeyboardType.NORMAL)
        keyboardSimple.register(binding.txt3, KeyboardSimple.KeyboardType.NUMBER)

    }
}