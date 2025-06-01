package com.myskripsi.gokos.ui.fragment.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // Untuk dialog konfirmasi (opsional)
import com.myskripsi.gokos.databinding.FragmentProfileBinding
import com.myskripsi.gokos.ui.activity.auth.login.LoginActivity // Impor LoginActivity
import com.myskripsi.gokos.utils.Result // Impor Result class Anda
import org.koin.androidx.viewmodel.ext.android.viewModel // Impor untuk Koin ViewModel

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Inject ProfileViewModel menggunakan Koin
    private val profileViewModel: ProfileViewModel by viewModel()

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

        // Di sini Anda bisa memuat data profil pengguna jika diperlukan
        // Contoh:
        // val currentUser = profileViewModel.currentUser // Jika Anda menambahkannya di ViewModel
        // binding.tvUserName.text = currentUser?.displayName
        // binding.tvUserEmail.text = currentUser?.email

        binding.btnLogout.setOnClickListener {
            // (Disarankan) Tampilkan dialog konfirmasi sebelum logout
            showLogoutConfirmationDialog()
        }

        observeLogoutState()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari akun ini?")
            .setPositiveButton("Logout") { dialog, _ ->
                profileViewModel.logoutUser() // Panggil fungsi logout di ViewModel
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun observeLogoutState() {
        profileViewModel.logoutState.observe(viewLifecycleOwner) { result ->
            // Asumsikan Anda memiliki ProgressBar dengan ID "progressBarProfile" di fragment_profile.xml
            // Jika tidak ada, binding.progressBarProfile akan null. // Ganti dengan ID ProgressBar Anda

            when (result) {
                is Result.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                    binding.btnLogout.isEnabled = false
                }
                is Result.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                    // Tombol logout bisa tetap nonaktif karena kita akan navigasi
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
        // Pastikan LoginActivity adalah titik masuk yang benar setelah logout
        val intent = Intent(requireActivity(), LoginActivity::class.java).apply {
            // Flags ini akan menghapus semua activity sebelumnya dalam task ini dan membuat LoginActivity sebagai root baru.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        // Menutup activity yang menampung fragment ini dan semua activity di atasnya dalam task yang sama.
        // Ini memastikan pengguna tidak bisa kembali ke halaman yang memerlukan autentikasi dengan tombol back.
        requireActivity().finishAffinity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Penting untuk mencegah memory leak
    }
}