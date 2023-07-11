package com.example.pbxtrackingtracingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import trackingFragment
import OrdersFragment
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val trackingFragment=trackingFragment()
        val ordersFragment=OrdersFragment()

        setCurrentFragment(trackingFragment)

        findViewById<BottomNavigationView>(R.id.bottomNavigationView).setOnItemSelectedListener {
            when(it.itemId){
                R.id.cmp_tracking_bnv->setCurrentFragment(trackingFragment)
                R.id.orders_bnv->setCurrentFragment(ordersFragment)
            }
            true
        }

    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }

    }
}