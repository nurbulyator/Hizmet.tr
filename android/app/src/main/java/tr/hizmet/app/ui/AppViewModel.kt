package tr.hizmet.app.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import tr.hizmet.app.HizmetApp

/**
 * Factory for our ViewModels. Gives each ViewModel the shared app objects
 * and the navigation arguments (SavedStateHandle).
 */
inline fun <reified VM : ViewModel> appFactory(
    crossinline create: (app: HizmetApp, args: SavedStateHandle) -> VM,
): ViewModelProvider.Factory = viewModelFactory {
    initializer { create(this[APPLICATION_KEY] as HizmetApp, createSavedStateHandle()) }
}
