package com.peterkrauz.trab_dso2.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.observe
import com.peterkrauz.trab_dso2.R
import com.peterkrauz.trab_dso2.data.entities.PublicAgency
import com.peterkrauz.trab_dso2.utils.lazyViewModel
import kotlinx.android.synthetic.main.activity_public_agencies.*

class PublicAgenciesActivity : AppCompatActivity() {

    private var searchAgenciesBottomSheet: SearchPublicAgenciesBottomSheet? = null

    private val agenciesAdapter by lazy { PublicAgenciesAdapter() }

    private val viewModel by lazyViewModel {
        PublicAgenciesViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_public_agencies)

        setupToolbar()
        setupView()
        setupViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)

        supportActionBar?.run {
            title = "Órgãos do Governo Federal"
        }
    }

    private fun setupView() {
        recyclerViewPublicAgencies.apply {
            adapter = agenciesAdapter
            layoutManager = LinearLayoutManager(this@PublicAgenciesActivity)
            setHasFixedSize(true)
        }
        fabSearchAgencies.setOnClickListener {
            viewModel.onSearchAgencies()
        }
    }

    private fun setupViewModel() {
        viewModel.publicAgenciesLiveData.observe(this, ::setPublicAgencies)
        viewModel.loadingLiveData.observe(this, ::setLoading)
        viewModel.searchAgenciesLiveEvent.observe(this) { onSearchAgencies() }
    }

    private fun setPublicAgencies(publicAgencies: List<PublicAgency>) {
        agenciesAdapter.publicAgencies = publicAgencies
    }

    private fun setLoading(loading: Boolean) {
        progressBarPublicAgencies.isVisible = loading
    }

    private fun onSearchAgencies() {
        searchAgenciesBottomSheet = SearchPublicAgenciesBottomSheet()
        searchAgenciesBottomSheet?.show(supportFragmentManager, "SearchAgenciesBottomSheet")
    }
}