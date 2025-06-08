package com.myskripsi.gokos.ui.fragment.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.myskripsi.gokos.databinding.FragmentProfileBinding
import com.myskripsi.gokos.ui.activity.auth.login.LoginActivity
import com.myskripsi.gokos.ui.activity.editProfile.EditProfileActivity
import com.myskripsi.gokos.ui.fragment.customalertdialog.ConfirmationDialogFragment
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel

// Implementasikan listener dari dialog
class ProfileFragment : Fragment(), ConfirmationDialogFragment.ConfirmationDialogListener {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModel()

    // Callback dari dialog, dipanggil saat user menekan "IYA" / tombol positif
    override fun onConfirm() {
        // Panggil fungsi logout di ViewModel
        profileViewModel.logoutUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogout.setOnClickListener {
            // Panggil dialog konfirmasi kustom
            showLogoutConfirmationDialog()
        }

        binding.optionAccount.setOnClickListener {
            val intent = Intent(requireActivity(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        observeLogoutState()
    }

    private fun showLogoutConfirmationDialog() {
        // Gunakan ConfirmationDialogFragment yang sudah kita buat
        val dialog = ConfirmationDialogFragment.newInstance(
            "Konfirmasi",
            "Apakah Anda yakin ingin keluar dari akun ini?",
            "IYA",
            "TIDAK"
        )
        // Karena fragment ini sudah mengimplementasikan listener,
        // dialog akan otomatis terhubung.
        dialog.show(childFragmentManager, "ConfirmLogoutDialog")
    }

    private fun observeLogoutState() {
        profileViewModel.logoutState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                    binding.btnLogout.isEnabled = false
                }
                is Result.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                    Toast.makeText(requireContext(), "Logout berhasil!", Toast.LENGTH_SHORT).show()
                    navigateToLoginScreen()
                }
                is Result.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    binding.btnLogout.isEnabled = true
                    Toast.makeText(requireContext(), "Logout gagal: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(requireActivity(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finishAffinity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}