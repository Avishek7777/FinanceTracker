package com.avishek.financetracker.ui.common

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.avishek.financetracker.AppContainer
import com.avishek.financetracker.FinanceApp

/** Gives ViewModel factories access to the app's shared objects. */
fun CreationExtras.appContainer(): AppContainer = (this[APPLICATION_KEY] as FinanceApp).container
