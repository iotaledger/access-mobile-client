package org.iota.access.ui.auth.login

import android.os.Bundle
import android.util.Pair
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.iota.access.BaseFragment
import org.iota.access.R
import org.iota.access.SettingsFragment
import org.iota.access.databinding.FragmentLoginBinding
import org.iota.access.di.Injectable
import org.iota.access.user.UserManager
import org.iota.access.utils.EncryptHelper
import timber.log.Timber
import javax.inject.Inject


class LoginFragment : BaseFragment(R.layout.fragment_login), Injectable {

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: FragmentLoginBinding

    private var disposable: CompositeDisposable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java)

        binding.viewModel = viewModel
        binding.buttonConnect.setOnClickListener { onConnectButtonClick() }
        binding.buttonCreateAccount.setOnClickListener { onCreateAccountButtonClick() }

        setHasOptionsMenu(true)
        bindViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbindViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_login, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_settings) {
            onSettingsOptionsClick()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun bindViewModel() {
        disposable = CompositeDisposable().apply {
            add(viewModel.loginCompleted
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ onLoginComplete() }, Timber::e))

            add(viewModel
                    .observableShowLoadingMessage
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ pair: Pair<Boolean?, String?> -> showLoading(pair.first, pair.second) }, Timber::e))

            add(viewModel
                    .observableShowDialogMessage
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ message: String -> showInfoDialog(message) }, Timber::e))
        }
    }

    private fun unbindViewModel() = disposable?.dispose()

    private fun onLoginComplete() =
            navController.navigate(LoginFragmentDirections.actionLoginFragmentToActivityMain())

    private fun onCreateAccountButtonClick() {
        navController.navigate(R.id.action_loginFragment_to_registerFragment)
    }

    private fun onConnectButtonClick() {
//        CoroutineScope(IO).launch {
//
//        EncryptHelper.test()
//        }
        viewModel.logIn()
    }

    private fun onSettingsOptionsClick() {
        val args = SettingsFragment.createArgs(false)
        navController.navigate(R.id.action_loginFragment_to_settingsFragment, args)
    }

    companion object {
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}
