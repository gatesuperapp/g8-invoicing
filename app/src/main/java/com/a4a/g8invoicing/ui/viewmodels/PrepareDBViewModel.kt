package com.a4a.g8invoicing.ui.viewmodels

/*

@HiltViewModel
class PrepareDBViewModel @Inject constructor(
    private val clientDataSource: ClientLocalDataSourceInterface,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientsUiState())
    val uiState: StateFlow<ClientsUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    init {
        if (tableIsEmpty()) {
            saveClient()
        }
    }

    private fun tableIsEmpty(): Boolean {
        var isEmpty: Boolean = false
        isEmpty = clientDataSource.checkIfEmpty() <= 2

        return isEmpty
    }

    fun saveClient() {
        viewModelScope.launch {
            for (i in 1..2) {
                clientDataSource.saveClient(
                    ClientOrIssuerEditable(
                        firstName = "rakoos",
                        name = "Raks $i",
                        address1 = "9 rue liloss",
                        address2 = "appt 33",
                        zipCode = "34000",
                        city = "Tolosas",
                        phone = "09374747",
                        email = "raks@skaramouch",
                        notes = "la plus belle com.a4a.g8invoicing/com.a4a.g8invoicing.MainActicom.a4a.g8invoicing/com.a4a.g8invoicing.MainActicom.a4a.g8invoicing/com.a4a.g8invoicing.MainActicom.a4a.g8invoicing/com.a4a.g8invoicing.MainActi",
                        id = null,
                    )
                )
            }
        }
    }
}
*/
