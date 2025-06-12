package com.myskripsi.gokos.ui.fragment.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.myskripsi.gokos.R
import com.myskripsi.gokos.databinding.FragmentProfileBinding
import com.myskripsi.gokos.ui.activity.auth.login.LoginActivity
import com.myskripsi.gokos.ui.activity.profile.editProfile.EditProfileActivity
import com.myskripsi.gokos.ui.activity.profile.PrivacyPolicyActivity
import com.myskripsi.gokos.ui.activity.profile.TermsConditionActivity
import com.myskripsi.gokos.ui.activity.profile.personalData.PersonalDataActivity
import com.myskripsi.gokos.ui.fragment.customalertdialog.ConfirmationDialogFragment
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel

// Implementasikan listener dari dialog
class ProfileFragment : Fragment(), ConfirmationDialogFragment.ConfirmationDialogListener {
    private var _binding: FragmentProfileBinding? = null

    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModel()

    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        profileViewModel.loadUserProfile()
    }

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
            showLogoutConfirmationDialog()
        }

        profileViewModel.loadUserProfile()

        setupMenu()
        observeViewModel()
    }

    private fun setupMenu() {
        binding.optionAccount.apply {
            ivMenuIcon.setImageResource(R.drawable.ic_edit_account)
            tvMenuTitle.text = "Ubah Profile"
            tvMenuSubtitle.text = "Ubah data profile pada akun kamu"
            root.setOnClickListener {
                val intent = Intent(requireActivity(), EditProfileActivity::class.java)
                editProfileLauncher.launch(intent)
            }
        }

        binding.optionDataPribadi.apply {
            ivMenuIcon.setImageResource(R.drawable.ic_personal_data)
            tvMenuTitle.text = "Data Pribadi"
            tvMenuSubtitle.text = "Informasi data pribadi akun kamu"
            root.setOnClickListener {
                startActivity(Intent(requireActivity(), PersonalDataActivity::class.java))
            }
        }

        binding.optionLanguage.apply {
            ivMenuIcon.setImageResource(R.drawable.ic_translate)
            tvMenuTitle.text = "Bahasa"
            tvMenuSubtitle.text = "Ubah ke bahasa yang kamu inginkan"
            root.setOnClickListener {
                Toast.makeText(requireContext(), "Menu Bahasa diklik", Toast.LENGTH_SHORT).show()
            }
        }

        binding.optionAboutApp.apply {
            ivMenuIcon.setImageResource(R.drawable.ic_aboutapp)
            tvMenuTitle.text = "Tentang Aplikasi"
            tvMenuSubtitle.text = "Informasi tentang aplikasi Go-Kos"
            root.setOnClickListener {
                Toast.makeText(requireContext(), "Menu Tentang Aplikasi diklik", Toast.LENGTH_SHORT).show()
            }
        }

        binding.optionPrivacyPolicy.apply {
            ivMenuIcon.setImageResource(R.drawable.ic_privacy)
            tvMenuTitle.text = "Kebijakan Privasi"
            tvMenuSubtitle.text = "Lihat kebijakan privasi Go-Kos"
            root.setOnClickListener {
                startActivity(Intent(requireActivity(), PrivacyPolicyActivity::class.java))
            }
        }

        binding.optionTerms.apply {
            ivMenuIcon.setImageResource(R.drawable.ic_rule)
            tvMenuTitle.text = "Syarat & Ketentuan"
            tvMenuSubtitle.text = "Lihat syarat & ketentuan Go-Kos"
            root.setOnClickListener {
                startActivity(Intent(requireActivity(), TermsConditionActivity::class.java))
            }
        }
    }

    private fun observeViewModel() {
        profileViewModel.userProfile.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                // Tampilkan nama pengguna. Jika nama kosong (misal dari registrasi email), tampilkan "Pengguna GoKos"
                binding.tvUsername.text = if (user.displayName.isNullOrBlank()) "Pengguna GoKos" else user.displayName

                // Tampilkan email pengguna
                binding.tvEmail.text = user.email

                // Tampilkan foto profil
                Glide.with(this)
                    .load(user.photoUrl) // URL foto dari Firebase Auth
                    .placeholder(R.drawable.placeholder_image) // Gambar default saat loading
                    .error(R.drawable.placeholder_image) // Gambar default jika user tidak punya foto atau terjadi error
                    .circleCrop() // (Opsional) jika ImageView Anda bukan ShapeableImageView
                    .into(binding.profileImage)
            } else {
                // Handle jika tidak ada user yang login (seharusnya tidak terjadi di halaman ini)
                binding.tvUsername.text = "Tamu"
                binding.tvEmail.text = "Silakan login"
            }
        }

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