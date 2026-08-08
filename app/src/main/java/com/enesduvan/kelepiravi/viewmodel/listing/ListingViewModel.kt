package com.enesduvan.kelepiravi.viewmodel.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.enesduvan.kelepiravi.data.local.SettingsManager
import com.enesduvan.kelepiravi.data.model.Listing
import com.enesduvan.kelepiravi.data.model.MarketItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.enesduvan.kelepiravi.data.listing.ListingEngine

class ListingViewModel(
    private val listingUseCase: ListingUseCase,
    private val settingsManager: SettingsManager
) : ViewModel() {

    val activeListings: StateFlow<List<Listing>> = listingUseCase.activeListings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            while (true) {
                delay(3000) // 3 saniyede bir piyasa hareketlenir
                val currentListings = activeListings.value
                if (currentListings.isNotEmpty()) {
                    val updatedListings = currentListings.map { ListingEngine.processTick(it) }
                    listingUseCase.updateActiveListings(updatedListings)
                }
            }
        }
    }

    val isFastSellEnabled = settingsManager.isFastSellEnabled
    val isSoundEnabled = settingsManager.isSoundEnabled
    val isHapticEnabled = settingsManager.isHapticEnabled
    val isOnboardingCompleted = settingsManager.isOnboardingCompleted

    fun setFastSellEnabled(enabled: Boolean) {
        settingsManager.setFastSellEnabled(enabled)
    }

    fun setSoundEnabled(enabled: Boolean) {
        settingsManager.setSoundEnabled(enabled)
    }

    fun setHapticEnabled(enabled: Boolean) {
        settingsManager.setHapticEnabled(enabled)
    }

    fun setOnboardingCompleted() {
        settingsManager.setOnboardingCompleted()
    }

    fun addListing(item: MarketItem, price: String) {
        viewModelScope.launch {
            listingUseCase.createListing(item, price)
        }
    }

    fun cancelListing(listing: Listing) {
        viewModelScope.launch {
            listingUseCase.cancelListing(listing)
        }
    }

    fun updateListingPrice(listing: Listing, newPrice: String) {
        viewModelScope.launch {
            listingUseCase.updateListingPrice(listing, newPrice)
        }
    }

    fun acceptOffer(listing: Listing, offerAmount: Double) {
        viewModelScope.launch {
            listingUseCase.acceptOffer(listing, offerAmount)
        }
    }

    fun removeOffer(listing: Listing, offerId: String) {
        val currentListings = activeListings.value.toMutableList()
        val index = currentListings.indexOfFirst { it.id == listing.id }
        if (index != -1) {
            val updatedListing = listing.copy(offers = listing.offers.filterNot { it.id == offerId })
            currentListings[index] = updatedListing
            viewModelScope.launch {
                listingUseCase.updateActiveListings(currentListings)
            }
        }
    }
}

class ListingViewModelFactory(
    private val listingUseCase: ListingUseCase,
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListingViewModel(listingUseCase, settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
