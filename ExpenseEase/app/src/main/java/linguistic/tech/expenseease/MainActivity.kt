package linguistic.tech.expenseease

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.content.Intent
import android.net.Uri
import androidx.core.view.GravityCompat
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import linguistic.tech.expenseease.databinding.ActivityMainBinding
import linguistic.tech.expenseease.ui.AddExpenseBottomSheet

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.FirstFragment, R.id.SecondFragment, R.id.AnalyticsFragment, R.id.TransactionsFragment
            ), binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_privacy_policy -> {
                    val url = "https://expenseease.blogspot.com/p/expense-ease-policy-and-privacy.html"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> {
                    val handled = androidx.navigation.ui.NavigationUI.onNavDestinationSelected(menuItem, navController)
                    if (handled) {
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    handled
                }
            }
        }

        binding.appBarMain.fab.setOnClickListener { 
            val bottomSheet = AddExpenseBottomSheet()
            bottomSheet.show(supportFragmentManager, "AddExpenseBottomSheet")
        }

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {}
        
        loadBanner()
        loadInterstitial()
    }

    private fun loadBanner() {
        val adView = AdView(this)
        adView.adUnitId = getString(R.string.banner)
        adView.setAdSize(AdSize.BANNER)
        
        binding.appBarMain.content.adViewContainer.removeAllViews()
        binding.appBarMain.content.adViewContainer.addView(adView)
        
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun loadInterstitial() {
        InterstitialAd.load(
            this,
            getString(R.string.intertitial),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun showInterstitial(onFinished: () -> Unit = {}) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial()
                    onFinished()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    onFinished()
                }
            }
            interstitialAd?.show(this)
        } else {
            onFinished()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
