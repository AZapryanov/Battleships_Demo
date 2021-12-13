package com.example.battleships_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        test("Hello World!")

    }

    fun test(test1: String): Int {
        return test1.length
    }

    fun sth1() {
        println("AZ branch change")
    }
    fun sth(){
        println("Kris branch 123")
    }
    fun sth3() {
        println("Test3 branch 123")
    }
    fun oburkanSum(){
        println("basi mamata")
    }
}