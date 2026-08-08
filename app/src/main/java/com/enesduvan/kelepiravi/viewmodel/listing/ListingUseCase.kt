package com.enesduvan.kelepiravi.viewmodel.listing

import com.enesduvan.kelepiravi.data.model.Listing
import com.enesduvan.kelepiravi.data.model.MarketItem
import com.enesduvan.kelepiravi.data.repository.KelepiraviRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ListingUseCase(
    private val repository: KelepiraviRepository
) {
    val activeListings: Flow<List<Listing>> = repository.getPlayerState().map {
        it.firstOrNull()?.activeListings ?: emptyList()
    }

    suspend fun createListing(item: MarketItem, price: String): Boolean {
        if (price.isBlank()) return false
        return repository.addListing(item, price)
    }

    suspend fun cancelListing(listing: Listing): Boolean {
        return repository.removeListing(listing)
    }

    suspend fun updateListingPrice(listing: Listing, newPrice: String): Boolean {
        if (newPrice.isBlank()) return false
        return repository.updateListingPrice(listing, newPrice)
    }

    suspend fun acceptOffer(listing: Listing, offerAmount: Double): Boolean {
        return repository.sellListing(listing, offerAmount)
    }

    suspend fun updateActiveListings(newListings: List<Listing>) {
        repository.updateActiveListings(newListings)
    }
}
