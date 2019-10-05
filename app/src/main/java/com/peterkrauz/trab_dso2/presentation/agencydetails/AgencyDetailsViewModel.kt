package com.peterkrauz.trab_dso2.presentation.agencydetails

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.peterkrauz.trab_dso2.Injector
import com.peterkrauz.trab_dso2.data.entities.PublicAgency
import com.peterkrauz.trab_dso2.data.entities.Travel
import com.peterkrauz.trab_dso2.data.repositories.TravelRepository
import com.peterkrauz.trab_dso2.presentation.agencydetails.bottomsheet.TravelFieldErrorType.NO_ERROR
import com.peterkrauz.trab_dso2.presentation.agencydetails.bottomsheet.TravelFieldErrorType.BLANK_FIELD
import com.peterkrauz.trab_dso2.presentation.agencydetails.bottomsheet.TravelFieldErrorType.INVALID_RANGE
import com.peterkrauz.trab_dso2.presentation.agencydetails.bottomsheet.TravelFieldsErrorBody
import com.peterkrauz.trab_dso2.presentation.common.PaginatorViewModel
import com.peterkrauz.trab_dso2.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class AgencyDetailsViewModel(
    private val agency: PublicAgency,
    private val travelRepository: TravelRepository = Injector.travelRepository
) : PaginatorViewModel<Travel>() {

    override var pageSize: Int = 15
    override var pageNumber: Int = 1
    override var currentPage: List<Travel> = emptyList()
    lateinit var datePeriodToSearch: TravelsSearchBody

    val travelsLiveData = MutableLiveData<List<Travel>>()
    val searchTravelsLiveEvent = SingleLiveEvent<Unit>()

    val validationCompleteLiveEvent = SingleLiveEvent<Unit>()
    val travelsTextErrorLiveData = MutableLiveData<TravelFieldsErrorBody>()

    fun onSearchTravels() {
        searchTravelsLiveEvent.call()
    }

    private fun onSearchFieldError(errorBody: TravelFieldsErrorBody) {
        travelsTextErrorLiveData.value = errorBody
    }

    private fun searchTravels(searchBody: TravelsSearchBody) {
        travelsTextErrorLiveData.value = null
        clearItemsLiveEvent.call()
        datePeriodToSearch = searchBody
        pageNumber = 1

        viewModelScope.launch(errorHandler) {
            loadingLiveData.value = true
            travelsLiveData.value = travelRepository.getAllInsidePeriod(
                searchBody.startDateFrom,
                searchBody.startDateUntil,
                searchBody.endDateFrom,
                searchBody.endDateUntil,
                agency.code
            )
            loadingLiveData.value = false
        }
    }

    fun validateAndSearch(
        startDateFrom: String,
        startDateUntil: String,
        endDateFrom: String,
        endDateUntil: String
    ) {
        var noErrors = true
        var startDateFromError = NO_ERROR
        var startDateUntilError = NO_ERROR
        var endDateFromError = NO_ERROR
        var endDateUntilError = NO_ERROR

        // checking for no blank field
        if (startDateFrom.isBlank()) {
            startDateFromError = BLANK_FIELD
            noErrors = false
        }

        if (startDateUntil.isBlank()) {
            startDateUntilError = BLANK_FIELD
            noErrors = false
        }

        if (endDateFrom.isBlank()) {
            endDateFromError = BLANK_FIELD
            noErrors = false
        }

        if (endDateUntil.isBlank()) {
            endDateUntilError = BLANK_FIELD
            noErrors = false
        }
        // endregion

        // checking if the travels's ranges are inside the period of 1 month from each other
        if (
            startDateFromError == NO_ERROR &&
            startDateUntilError == NO_ERROR &&
            !isInValidRange(startDateFrom, startDateUntil)
        ) {
            startDateFromError = INVALID_RANGE
            startDateUntilError = INVALID_RANGE
        }

        if (
            endDateFromError == NO_ERROR &&
            endDateUntilError == NO_ERROR &&
            !isInValidRange(endDateFrom, endDateUntil)
        ) {
            endDateFromError = INVALID_RANGE
            endDateUntilError = INVALID_RANGE
        }
        // endregion

        if (noErrors) {
            searchTravels(
                TravelsSearchBody(
                    startDateFrom,
                    startDateUntil,
                    endDateFrom,
                    endDateUntil
                )
            )
        } else {
            onSearchFieldError(
                TravelFieldsErrorBody(
                    startDateFromError,
                    startDateUntilError,
                    endDateFromError,
                    endDateUntilError
                )
            )
        }

    }

    private fun isInValidRange(dateFrom: String, dateUntil: String): Boolean {
        // supposing that they're on the same year
        return dateUntil.month() - dateFrom.month() <= 1
    }

    override fun paginate() {
        if (currentPage.size % pageSize != 0) {
            pagedToEndLiveEvent.call()
        } else {
            pageNumber++
            paginateLiveEvent.value = pageNumber

            viewModelScope.launch(errorHandler) {
                loadingLiveData.value = true

                travelsLiveData.value = travelRepository.getAllInsidePeriod(
                    datePeriodToSearch.startDateFrom,
                    datePeriodToSearch.startDateUntil,
                    datePeriodToSearch.endDateFrom,
                    datePeriodToSearch.endDateUntil,
                    agency.code,
                    pageNumber
                )
                pageSize = travelsLiveData.value?.size!!

                loadingLiveData.value = false
            }
        }
    }

}

private fun String.month(): Int {
    return this.substring(2, 5).toInt()
}